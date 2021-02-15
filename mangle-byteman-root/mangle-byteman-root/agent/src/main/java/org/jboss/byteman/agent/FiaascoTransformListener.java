/*
 * Copyright (c) 2016-2019 VMware, Inc. All Rights Reserved.
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with separate copyright notices
 * and license terms. Your use of these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.jboss.byteman.agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import org.jboss.byteman.check.RuleCheckResult;
import org.jboss.byteman.rule.Rule;
import org.jboss.byteman.rule.helper.Helper;

import com.vmware.mangle.java.agent.faults.AgentFault;
import com.vmware.mangle.java.agent.faults.ApplicationFault;
import com.vmware.mangle.java.agent.faults.ApplicationFaultInfo;
import com.vmware.mangle.java.agent.faults.FaultStatus;
import com.vmware.mangle.java.agent.faults.helpers.FaultsHelper;
import com.vmware.mangle.java.agent.utils.DumpUtils;
import com.vmware.mangle.java.agent.utils.IOUtils;
import com.vmware.mangle.java.agent.utils.RuntimeUtils;

/**
 * a socket based listener class which reads scripts from stdin and installs them in the current
 * runtime
 *
 * @author Andrew Dinn
 * @author hkilari
 *
 */
public class FiaascoTransformListener extends Thread {
    private static final Logger LOG = Logger.getLogger(FiaascoTransformListener.class.getName());
    public static int DEFAULT_PORT = 9091;
    public static String DEFAULT_HOST = "localhost";
    public static Integer myPort;
    private static FiaascoTransformListener theTransformListener = null;
    private static ServerSocket theServerSocket;
    private static boolean forceExit;
    private Retransformer retransformer;
    private FaultsHelper faultsHelper;
    private boolean isTroubleshootingEnabled = false;
    private boolean shutdown;

    private FiaascoTransformListener(Retransformer retransformer) {
        this.retransformer = retransformer;
        faultsHelper = FaultsHelper.getInstance();
        setDaemon(true);
        System.out.println("Started Listener by Mangle for Injecting Java Application Faults");
    }

    public static synchronized boolean initialize(Retransformer retransformer) {
        return (initialize(retransformer, null, null));
    }

    public static synchronized boolean initialize(Retransformer retransformer, String hostname, Integer port) {
        if (theTransformListener == null) {
            try {
                if (hostname == null) {
                    hostname = DEFAULT_HOST;
                }
                if (port == null) {
                    myPort = Integer.valueOf(DEFAULT_PORT);
                } else {
                    myPort = port;
                }
                theServerSocket = new ServerSocket();
                theServerSocket.bind(new InetSocketAddress(hostname, myPort.intValue()));
                Helper.verbose("TransformListener() : accepting requests on " + hostname + ":" + myPort);

            } catch (IOException e) {
                Helper.err("TransformListener() : unexpected exception opening server socket " + e);
                Helper.errTraceException(e);
                return false;
            }

            theTransformListener = new FiaascoTransformListener(retransformer);
            theTransformListener.start();
        }
        return true;
    }

    public static synchronized boolean forceTerminate(PrintWriter out) {
        forceExit = true;
        return terminate(out);
    }

    public static synchronized boolean terminate(PrintWriter out) {
        // we don't want the listener shutdown to be aborted because of
        // triggered rules
        LOG.info("Trying to Terminate Listener");
        boolean enabled = true;
        try {
            enabled = Rule.disableTriggersInternal();
            if (theTransformListener != null) {
                if (canListenerExit(out)) {
                    try {
                        out.println("TransformListener() :  closing port " + DEFAULT_PORT + "\n");
                        out.println("OK");
                        out.flush();
                        theTransformListener.shutdown = true;
                        theServerSocket.close();
                        Helper.verbose("TransformListener() :  closing port " + DEFAULT_PORT);
                        LOG.info("TransformListener() :  closing port " + DEFAULT_PORT);
                        LOG.info("Terminated Listener");
                        LOG.info("shutdown Value:" + theTransformListener.shutdown);
                    } catch (IOException e) {
                        LOG.severe(e.getMessage());
                    }
                    System.setProperty(AgentMain.BYTEMAN_LISTENER_LOADED, Boolean.FALSE.toString());
                    theTransformListener = null;
                    theServerSocket = null;
                } else {
                    out.println("Not Closing the Listener as Other Rules/Faults are in progress.\n");
                    out.println("OK");
                    out.flush();
                }
            }

            return true;
        } finally {
            if (enabled) {
                Rule.enableTriggersInternal();
            }
        }
    }

    private static boolean canListenerExit(PrintWriter out) {
        Map<String, AgentFault> liveFaults = FaultsHelper.getInstance().getRunningFaults();
        List<String> rules = new ArrayList<>();

        if (forceExit) {
            try {
                theTransformListener.retransformer.removeScripts(null, out);
            } catch (Exception e) {
                Helper.err("TransformListener() : unexpected exception while unloading rules " + e);
                Helper.errTraceException(e);
            }
            for (Entry<String, AgentFault> entry : liveFaults.entrySet()) {
                entry.getValue().remediateFault();
            }
            return true;
        }

        if (theTransformListener != null && liveFaults.isEmpty()) {
            if (theTransformListener.retransformer.scriptRepository.currentRules().isEmpty()) {
                return true;
            }
            if (rules.size() == 1) {
                rules.get(0).toString().contains("RULE Trace - Capture Troubleshooting bundle.");
                return true;
            }
        }
        return false;
    }

    @Override
    public void run() {
        // we don't want to see any triggers in the listener thread

        Rule.disableTriggersInternal();

        while (!shutdown) {
            if (theServerSocket.isClosed()) {
                return;
            }
            Socket socket = null;
            try {
                socket = theServerSocket.accept();
            } catch (IOException e) {
                if (!theServerSocket.isClosed()) {
                    Helper.err("TransformListener.run : exception from server socket accept " + e);
                    Helper.errTraceException(e);
                }
                return;
            }

            Helper.verbose("TransformListener() : handling connection on port " + socket.getLocalPort());

            try {
                handleConnection(socket);
            } catch (Exception e) {
                Helper.err("TransformListener() : error handling connection on port " + socket.getLocalPort());
                try {
                    socket.close();
                } catch (IOException e1) {
                    Helper.err("Failed to Terminate Listener. Reason: " + e1.getMessage());
                }
            }
        }
    }

    private void handleConnection(Socket socket) {
        InputStream is = null;
        try {
            is = socket.getInputStream();
        } catch (IOException e) {
            // oops. cannot handle this
            Helper.err("TransformListener.run : error opening socket input stream " + e);
            Helper.errTraceException(e);

            try {
                socket.close();
            } catch (IOException e1) {
                Helper.err("TransformListener.run : exception closing socket after failed input stream open" + e1);
                Helper.errTraceException(e1);
            }
            return;
        }

        OutputStream os = null;
        try {
            os = socket.getOutputStream();
        } catch (IOException e) {
            // oops. cannot handle this
            Helper.err("TransformListener.run : error opening socket output stream " + e);
            Helper.errTraceException(e);

            try {
                socket.close();
            } catch (IOException e1) {
                Helper.err("TransformListener.run : exception closing socket after failed output stream open" + e1);
                Helper.errTraceException(e1);
            }
            return;
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        PrintWriter out = new PrintWriter(new OutputStreamWriter(os));

        String line = null;
        try {
            line = in.readLine();
        } catch (IOException e) {
            Helper.err("TransformListener.run : exception " + e + " while reading command");
            Helper.errTraceException(e);
        }

        try {
            if (line == null) {
                out.println("ERROR");
                out.println("Expecting input command");
                out.println("OK");
                out.flush();
            } else if (line.equals("BOOT")) {
                loadJars(in, out, true);
            } else if (line.equals("SYS")) {
                loadJars(in, out, false);
            } else if (line.equals("LOAD")) {
                loadScripts(in, out);
            } else if (line.equals("DELETE")) {
                deleteScripts(in, out);
            } else if (line.equals("LIST")) {
                listScripts(in, out);
            } else if (line.equals("DELETEALL")) {
                purgeScripts(in, out);
            } else if (line.equals("VERSION")) {
                getVersion(in, out);
            } else if (line.equals("LISTBOOT")) {
                listBootJars(in, out);
            } else if (line.equals("LISTSYS")) {
                listSystemJars(in, out);
            } else if (line.equals("LISTSYSPROPS")) {
                listSystemProperties(in, out);
            } else if (line.equals("SETSYSPROPS")) {
                setSystemProperties(in, out);
            } else if (line.contains("INJECTFAULT")) {
                injectFault(in, out);
            } else if (line.contains("REMEDIATEFAULT")) {
                remediateFault(in, out);
            } else if (line.contains("LISTLIVEFAULTS")) {
                listLiveFaults(in, out);
            } else if (line.contains("LISTALLFAULTS")) {
                listAllFaults(in, out);
            } else if (line.contains("GETFAULT")) {
                getFaultInfo(in, out);
            } else if (line.contains("THREADDUMP")) {
                getThreadDump(in, out);
            } else if (line.contains("HEAPDUMP")) {
                getHeapDump(in, out);
            } else if (line.contains("HEAPUSAGE")) {
                getCurrentMemoryUsage(in, out);
            } else if (line.contains("GETALLFAULTS")) {
                listAllFaultsInfo(in, out);
            } else if (line.contains("FORCETERMINATE")) {
                forceTerminate(out);
            } else if (line.contains("TERMINATE")) {
                terminate(out);
            } else if (line.contains("ENABLETROUBLESHOOTING")) {
                enableCollectionOfTroubleshootingData(in, out);
            } else if (line.contains("PING")) {
                sendStatus(in, out);
            } else {
                out.println("ERROR");
                out.println("Unexpected command " + line);
                out.println("OK");
                out.flush();
            }
        } catch (Exception e) {
            Helper.err("TransformListener.run : exception " + e + " processing command " + line);
            Helper.errTraceException(e);
        } finally {
            try {
                socket.close();
            } catch (IOException e1) {
                Helper.err("TransformListener.run : exception closing socket " + e1);
                Helper.errTraceException(e1);
            }
        }
    }

    private void sendStatus(BufferedReader in, PrintWriter out) {
        out.println(FiaascoTransformListener.class.getName() + "- I am here on Pid: " + RuntimeUtils.getPid());
        out.flush();
        try {
            in.close();
        } catch (IOException e) {
            out.append("Unable to respond to Ping Request" + "\n");
            out.append(e.toString());
            out.append("\n");
            e.printStackTrace(out);
        }
    }

    private void listLiveFaults(BufferedReader in, PrintWriter out) {
        out.println(FaultsHelper.objectToJson(faultsHelper.getLiveFaults()));
        out.println(FaultsHelper.objectToJson(faultsHelper.getLiveApplicationFaults()));
        out.flush();
        try {
            in.close();
        } catch (IOException e) {
            out.append("Unable to Retrieve Live Faults" + "\n");
            out.append(e.toString());
            out.append("\n");
            e.printStackTrace(out);
        }
    }

    private void listAllFaults(BufferedReader in, PrintWriter out) {
        out.println(FaultsHelper.objectToJson(faultsHelper.getAllFaults()));
        out.println(FaultsHelper.objectToJson(faultsHelper.getAllApplicationFaults()));
        out.flush();
        try {
            in.close();
        } catch (IOException e) {
            out.append("Unable to Retrieve All Faults" + "\n");
            out.append(e.toString());
            out.append("\n");
            e.printStackTrace(out);
        }
    }

    private void listAllFaultsInfo(BufferedReader in, PrintWriter out) {
        out.println(faultsHelper.getAllFaultsInfo());
        out.println(faultsHelper.getAllApplicationFaultsInfo());
        out.flush();
        try {
            in.close();
        } catch (IOException e) {
            out.append("Unable to Retrieve All Faults Info" + "\n");
            out.append(e.toString());
            out.append("\n");
            e.printStackTrace(out);
        }
    }

    private void getFaultInfo(BufferedReader in, PrintWriter out) {
        try {
            String command = in.readLine().trim();
            String faultId = command.substring(command.indexOf("[") + 1, command.indexOf("]"));
            out.println(faultsHelper.getFault(faultId));
            out.flush();
            in.close();
        } catch (IOException e) {
            out.append("Unable to Retrieve a Fault with ID: " + "faultId. Reason: \n");
            out.append(e.toString());
            out.append("\n");
            e.printStackTrace(out);
        }
    }

    private void getThreadDump(BufferedReader in, PrintWriter out) {
        try {
            String command = in.readLine().trim();
            String filePath = command.substring(command.indexOf("[") + 1, command.indexOf("]"));
            out.println(DumpUtils.captureThreadDump(filePath));
            out.flush();
            in.close();
        } catch (IOException e) {
            out.append("Unable to capture Thread Dump. Reason: \n");
            out.append(e.toString());
            out.append("\n");
            e.printStackTrace(out);
        }
    }

    private void getHeapDump(BufferedReader in, PrintWriter out) {
        try {
            String command = in.readLine().trim();
            String filePath = command.substring(command.indexOf("[") + 1, command.indexOf("]"));
            out.println(DumpUtils.captureHeapDump(filePath));
            out.flush();
            in.close();
        } catch (IOException e) {
            out.append("Unable to capture heapdump. Reason: \n");
            out.append(e.toString());
            out.append("\n");
            e.printStackTrace(out);
        }
    }

    private void getCurrentMemoryUsage(BufferedReader in, PrintWriter out) {
        try {
            out.println("Current HeapUsage: " + RuntimeUtils.getUsedHeapSpace());
            out.flush();
            in.close();
        } catch (IOException e) {
            out.append("Unable to retrieve current heap usage. Reason: \n");
            out.append(e.toString());
            out.append("\n");
            e.printStackTrace(out);
        }
    }

    private void enableCollectionOfTroubleshootingData(BufferedReader in, PrintWriter out) throws IOException {
        if (!isTroubleshootingEnabled) {
            out.append("Enabling Colection of Troubleshooting Information on the Java Process" + "\n");
            StringBuffer scriptsText = new StringBuffer();
            scriptsText.append(addRuleScript("TroubleshootingCollectionRule.btm"));
            scriptsText.append("\nENDLOAD");
            Reader inputString = new StringReader(scriptsText.toString());
            BufferedReader scriptsReader = new BufferedReader(inputString);
            handleScripts(scriptsReader, out, false);
            isTroubleshootingEnabled = true;
        } else {
            out.println("Troubleshooting Already Enabled.");
            out.println("OK");
            out.flush();
        }
    }

    private void injectFault(BufferedReader in, PrintWriter out) throws IOException {
        String command = in.readLine().trim();
        String fault = faultsHelper.injectFault(command.substring(command.indexOf("[") + 1, command.indexOf("]")));
        out.println(fault);
        out.flush();

        try {
            in.close();
        } catch (IOException e) {
            out.append("Unable to Inject Fault" + "\n");
            out.append(e.toString());
            out.append("\n");
            e.printStackTrace(out);
        }
    }

    private StringBuffer addRuleScript(String ruleFileName) throws IOException {
        StringBuffer scriptsText = new StringBuffer();
        InputStream initialStream = ClassLoader.getSystemClassLoader().getResourceAsStream(ruleFileName);
        scriptsText.append("SCRIPT " + ruleFileName + "\n");
        scriptsText.append(IOUtils.getStringFromInputStream(initialStream));
        scriptsText.append("\nENDSCRIPT");
        return scriptsText;
    }

    private void remediateFault(BufferedReader in, PrintWriter out) throws IOException {
        String command = in.readLine().trim();
        String result = faultsHelper.remediateFault(command.substring(command.indexOf("[") + 1, command.indexOf("]")));
        out.append(result);
        out.append("\n");
        out.flush();
        try {
            in.close();
        } catch (IOException e) {
            out.append("Unable to Remediate Fault" + "\n");
            out.append(e.toString());
            out.append("\n");
            e.printStackTrace(out);
        }

    }

    private void getVersion(BufferedReader in, PrintWriter out) {
        String version = this.getClass().getPackage().getImplementationVersion();
        if (version == null) {
            version = "0";
        }
        out.println(version);
        out.println("OK");
        out.flush();
    }

    private void loadScripts(BufferedReader in, PrintWriter out) throws IOException {
        handleScripts(in, out, false);
    }

    private void loadJars(BufferedReader in, PrintWriter out, boolean isBoot) throws IOException {
        final String endMarker = (isBoot) ? "ENDBOOT" : "ENDSYS";
        String line = in.readLine().trim();
        while (line != null && !line.equals(endMarker)) {
            if (validateJarPath(line)) {
                try {
                    JarFile jarfile = new JarFile(new File(line));
                    retransformer.appendJarFile(out, jarfile, isBoot);
                } catch (Exception e) {
                    out.append("EXCEPTION ");
                    out.append("Unable to add jar file " + line + "\n");
                    out.append(e.toString());
                    out.append("\n");
                    e.printStackTrace(out);
                }
                line = in.readLine().trim();
            } else {
                out.append("Line validation is failed, input is not in the correct format");
            }
        }
        if (line == null || !line.equals(endMarker)) {
            out.append("ERROR\n");
            out.append("Unexpected end of line reading " + ((isBoot) ? "boot" : "system") + " jars\n");
        }
        out.println("OK");
        out.flush();
    }

    private boolean validateJarPath(String line) {
        String pattern = "^[a-zA-Z0-9]*.jar$";
        return line.matches(pattern);
    }

    private void deleteScripts(BufferedReader in, PrintWriter out) throws IOException {
        handleScripts(in, out, true);
    }

    private void handleScripts(BufferedReader in, PrintWriter out, boolean doDelete) throws IOException {
        List<String> scripts = new LinkedList<String>();
        List<String> scriptNames = new LinkedList<String>();
        Map<String, ApplicationFault> applicationFaultsMap = new HashMap<>();

        String line = in.readLine().trim();
        String scriptName = "<unknown>";
        long timeout = 0;
        try {
            while (line.startsWith("SCRIPT ")) {
                StringBuffer stringBuffer = new StringBuffer();
                scriptName = line.substring("SCRIPT ".length());
                line = in.readLine();
                while (line != null && !line.equals("ENDSCRIPT")) {
                    stringBuffer.append(line);
                    stringBuffer.append('\n');
                    line = in.readLine();
                }
                if (line == null || !line.equals("ENDSCRIPT")) {
                    out.append("ERROR\n");
                    out.append("Unexpected end of line reading script " + scriptName + "\n");
                    out.append("OK");
                    out.flush();
                    return;
                }
                line = in.readLine();
                if (line != null && line.startsWith("TIMEOUT ")) {
                    timeout = Long.parseLong(line.substring("TIMEOUT ".length()));
                    line = in.readLine();
                    checkRule(scriptName, stringBuffer.toString(), out);
                }

                String script = stringBuffer.toString();
                scripts.add(script);
                scriptNames.add(scriptName);
                ApplicationFault applicationFault = new ApplicationFault();
                applicationFault.setId(scriptName);
                ApplicationFaultInfo faultInfo = new ApplicationFaultInfo();
                faultInfo.setFaultStatus(FaultStatus.IN_PROGRESS);
                faultInfo.setTimeout(timeout);
                faultInfo.setScriptText(script);
                applicationFault.setFaultInfo(faultInfo);
                applicationFaultsMap.put(scriptName, applicationFault);
            }

            if ((doDelete && !line.equals("ENDDELETE")) || (!doDelete && !line.equals("ENDLOAD"))) {
                out.append("ERROR ");
                out.append("Unexpected end of line reading script " + scriptName + "\n");
                out.println("OK");
                out.flush();
                return;
            }


            if (doDelete) {
                retransformer.removeScripts(scripts, out);
                for (String keyName : scriptNames) {
                    faultsHelper.getApplicationFaultsMap().get(keyName).getFaultInfo()
                            .setFaultStatus(FaultStatus.COMPLETED);
                }
            } else {
                retransformer.installScript(scripts, scriptNames, out);
                faultsHelper.getApplicationFaultsMap().putAll(applicationFaultsMap);
            }
        } catch (Exception e) {
            out.append("EXCEPTION ");
            out.append(e.toString());
            out.append('\n');
            e.printStackTrace(out);
        }
        out.println("OK");
        out.flush();
    }

    /**
     * The method perform below steps - Is same rule running already - Is the content of the Rule
     * valid.
     *
     * @param scriptName
     * @param script
     * @param out
     * @throws Exception
     */
    private void checkRule(String scriptName, String script, PrintWriter out) throws Exception {
        ApplicationFaultInfo faultinfo = null;
        if (faultsHelper.getApplicationFaultsMap() != null
                && faultsHelper.getApplicationFaultsMap().get(scriptName) != null) {
            faultinfo = faultsHelper.getApplicationFaultsMap().get(scriptName).getFaultInfo();
        }
        if (faultinfo != null && faultinfo.getFaultStatus() == FaultStatus.IN_PROGRESS
                && faultinfo.getScriptText().equals(script)) {
            throw new Exception("Given Fault is already running");
        }
        RuleCheck check = new RuleCheck();
        check.addRule(scriptName, script);

        check.checkRules();

        RuleCheckResult result = check.getResult();

        if (result.hasError()) {
            int parseErrorCount = result.getParseErrorCount();
            int typeErrorCount = result.getTypeErrorCount();
            int typeWarningCount = result.getTypeWarningCount();
            int warningCount = result.getWarningCount() + typeWarningCount;
            int errorCount = result.getErrorCount() + parseErrorCount + typeErrorCount + typeWarningCount;
            out.append("TestScript: " + errorCount + " total errors\n");
            out.append("            " + warningCount + " total warnings\n");
            out.append("            " + parseErrorCount + " parse errors\n");
            out.append("            " + typeErrorCount + " type errors\n");
            out.append("            " + typeWarningCount + " type warnings\n");
        } else if (result.hasWarning()) {
            int typeWarningCount = result.getTypeWarningCount();
            int warningCount = result.getWarningCount() + typeWarningCount;
            out.append("TestScript: " + warningCount + " total warnings\n");
            out.append("            " + typeWarningCount + " type warnings\n");
            //As it mean, there is no errors, the better stdout is Helper.out
            out.append("TestScript: no errors\n");
        } else {
            out.append("TestScript: no errors\n");
        }
    }

    private void purgeScripts(BufferedReader in, PrintWriter out) throws Exception {
        retransformer.removeScripts(null, out);
        out.println("OK");
        out.flush();
    }

    private void listScripts(BufferedReader in, PrintWriter out) throws Exception {
        retransformer.listScripts(out);
        out.println("OK");
        out.flush();
    }

    private void listBootJars(BufferedReader in, PrintWriter out) throws Exception {
        Set<String> jars = retransformer.getLoadedBootJars();
        for (String jar : jars) {
            out.println(new File(jar).getAbsolutePath());
        }
        out.println("OK");
        out.flush();
    }

    private void listSystemJars(BufferedReader in, PrintWriter out) throws Exception {
        Set<String> jars = retransformer.getLoadedSystemJars();
        for (String jar : jars) {
            out.println(new File(jar).getAbsolutePath());
        }
        out.println("OK");
        out.flush();
    }

    private void listSystemProperties(BufferedReader in, PrintWriter out) throws Exception {
        Properties sysProps = System.getProperties();
        boolean strictMode = false;
        if (Boolean.parseBoolean(sysProps.getProperty(Transformer.SYSPROPS_STRICT_MODE, "true"))) {
            strictMode = true;
        }

        for (Map.Entry<Object, Object> entry : sysProps.entrySet()) {
            String name = entry.getKey().toString();
            if (!strictMode || name.startsWith("org.jboss.byteman.")) {
                String value = entry.getValue().toString();
                out.println(name + "=" + value.replace("\n", "\\n").replace("\r", "\\r"));
            }
        }
        out.println("OK");
        out.flush();
    }

    private void setSystemProperties(BufferedReader in, PrintWriter out) throws Exception {
        boolean strictMode = false;
        if (Boolean.parseBoolean(System.getProperty(Transformer.SYSPROPS_STRICT_MODE, "true"))) {
            strictMode = true;
        }

        final String endMarker = "ENDSETSYSPROPS";
        String line = in.readLine().trim();
        while (line != null && !line.equals(endMarker)) {
            try {
                String[] nameValuePair = line.split("=", 2);
                if (nameValuePair.length != 2) {
                    throw new Exception("missing '='");
                }
                String name = nameValuePair[0];
                String value = nameValuePair[1];
                if (strictMode && !name.startsWith("org.jboss.byteman.")) {
                    throw new Exception("strict mode is enabled, cannot set non-byteman system property");
                }
                if (name.equals(Transformer.SYSPROPS_STRICT_MODE) && !value.equals("true")) {
                    // nice try
                    throw new Exception("cannot turn off strict mode");
                }

                // everything looks good and we are allowed to set the system
                // property now
                if (value.length() > 0) {
                    // "some.sys.prop=" means the client wants to delete the
                    // system property
                    System.setProperty(name, value);
                    out.append("Set system property [" + name + "] to value [" + value + "]\n");
                } else {
                    System.clearProperty(name);
                    out.append("Deleted system property [" + name + "]\n");
                }
                // ok, now tell the transformer a property has changed
                retransformer.updateConfiguration(name);
            } catch (Exception e) {
                out.append("EXCEPTION ");
                out.append("Unable to set system property [" + line + "]\n");
                out.append(e.toString());
                out.append("\n");
                e.printStackTrace(out);
            }
            line = in.readLine().trim();
        }
        if (line == null || !line.equals(endMarker)) {
            out.append("ERROR\n");
            out.append("Unexpected end of line reading system properties\n");
        }
        out.println("OK");
        out.flush();
    }
}
