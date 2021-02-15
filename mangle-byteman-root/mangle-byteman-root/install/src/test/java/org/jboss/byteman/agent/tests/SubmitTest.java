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

package org.jboss.byteman.agent.tests;

/**
 * @author hkilari
 *
 */

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.byteman.agent.install.Install;
import org.jboss.byteman.agent.submit.Submit;
import org.testng.Assert;

import com.vmware.mangle.java.agent.utils.RuntimeUtils;
import com.vmware.mangle.java.agent.utils.ThreadUtils;

public class SubmitTest {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream standard = System.out;

    //@Test
    public void bytemanTest() {
        String userDir = System.getProperty("user.dir");
        String bytemanHome = userDir.substring(0, userDir.lastIndexOf(File.separator) + 1) + "agent" + File.separator
                + "target" + File.separator + "mangle-byteman-3.0.0.jar";
        System.out.println(bytemanHome);
        System.setProperty("org.jboss.byteman.jar",
                "D:\\HKILARI\\opensource\\mangle\\mangle-byteman-root\\agent\\target\\mangle-byteman-3.0.0.jar");
        System.out.println("Now the output is redirected to internal Stream");
        PrintStream printStream = new PrintStream(baos);
        System.setOut(printStream);
        System.setErr(printStream);
        Install.main(new String[] { "-s", "-b", "JConsole" });

        Assert.assertTrue((baos.toString().contains("Started Byteman Listener Successfully")), baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Install.main(new String[] { "-s", "-b", RuntimeUtils.getPid() });

        Assert.assertTrue((baos.toString().contains("Given Port: 9091 already in Use.")), baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Install.main(new String[] { "-s", "-b", "-p", "9092", RuntimeUtils.getPid() });

        Assert.assertTrue((baos.toString().contains("Started Byteman Listener Successfully")), baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Install.main(new String[] { "-s", "-b", "-p", "9093", RuntimeUtils.getPid() });

        Assert.assertTrue((baos.toString().contains("Main : attempting to load Byteman Listener more than once")),
                baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Submit.main(new String[] { "-if", "__timeoutVariance", "2000", "__gap", "20000", "__gapVariance", "5000",
                "__loadVariance", "30", "__longLasting", "true", "__faultName", "cpuFault", "__timeOutInMilliSeconds",
                "10000", "__load", "30" });

        Assert.assertTrue(baos.toString().contains("Created Fault Successfully"), baos.toString());
        String faultId = extractField(baos.toString(), "[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}");
        Assert.assertTrue(faultId != null && !faultId.trim().equals(""));
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Submit.main(new String[] { "-llf" });
        Assert.assertTrue(baos.toString().contains("{\"" + faultId + "\":\"cpuFault\"}"), baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ThreadUtils.delayInSeconds(2);
        Submit.main(new String[] { "-rf", faultId });
        Assert.assertTrue(baos.toString().contains("Received Remediation Request Successfully"), baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Submit.main(new String[] { "-rf", faultId });
        Assert.assertTrue(baos.toString().contains("Requested Fault is already Remediated."), baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Submit.main(new String[] { "-rf", "bcdadaa0-33ea-4ef5-b19e-d25a66a6bd70" });
        Assert.assertTrue(
                baos.toString().contains("No fault found with provided ID: bcdadaa0-33ea-4ef5-b19e-d25a66a6bd70"),
                baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Submit.main(new String[] { "-rf", "" });
        Assert.assertTrue(baos.toString().contains("No fault found with provided ID:"), baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Submit.main(new String[] { "-rf", " " });
        Assert.assertTrue(baos.toString().contains("No fault found with provided ID:"), baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Submit.main(new String[] { "-laf" });
        Assert.assertTrue(baos.toString().contains("{\"" + faultId + "\":\"cpuFault\"}"), baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Submit.main(new String[] { "-gf", "267be1f0-08f9-48bc-97f0-b92b02c68950" });
        Assert.assertTrue(
                baos.toString().contains("No fault fault found with provided ID: 267be1f0-08f9-48bc-97f0-b92b02c68950"),
                baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Submit.main(new String[] { "-gaf" });
        Assert.assertTrue(baos.toString().contains("\"taskActivity\":"), baos.toString());
        Assert.assertTrue(baos.toString().contains("Initializing the Fault"), baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Submit.main(new String[] { "-enableTroubleshooting" });
        Assert.assertTrue(baos.toString().contains("install rule Trace - Capture Troubleshooting bundle."),
                baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Submit.main(new String[] { "-ping" });
        Assert.assertTrue(
                baos.toString().contains("org.jboss.byteman.agent.FiaascoTransformListener: I am here on Pid: "),
                baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Submit.main(new String[] { "-threadDump", "D:/" });
        Assert.assertTrue(baos.toString().contains("Captured Thread Dump Successfully"), baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Submit.main(new String[] { "-heapDump", "D:/" });
        Assert.assertTrue(baos.toString().contains("Captured Heap Dump Successfully"), baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Submit.main(new String[] { "-exit" });
        Assert.assertTrue(baos.toString().contains("Not Closing the Listener as Other Rules/Faults are in progress."),
                baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Submit.main(new String[] { "-forceExit" });
        Assert.assertTrue(baos.toString().contains("TransformListener() :  closing port"), baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        runCommand("Taskkill /IM jconsole.exe /F");
    }

    //@AfterClass(enabled = true, alwaysRun = true)
    public void afterSuite() {
        System.out.println("Now the output is redirected to System.out");
        System.setOut(standard);
        System.out.println(baos.toString());
    }

    private void runCommand(String cmd) {
        String s = null;
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            // read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

            // read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }

        } catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * @param latestCommandOutput
     * @param regExpression
     * @return
     */
    public static String extractField(String latestCommandOutput, String regExpression) {
        Pattern p = Pattern.compile(regExpression);
        Matcher m = p.matcher(latestCommandOutput);
        if (m.find()) {
            return m.group(0);
        }
        return null;
    }
}