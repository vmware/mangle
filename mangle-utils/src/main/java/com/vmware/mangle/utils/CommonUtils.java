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

package com.vmware.mangle.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.Description;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.utils.clients.ssh.SSHUtils;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author Dinesh Babu TG (dgnaneswaran)
 * @author Bhanu Karanam (bkaranam)
 * @author dbhat
 */

@Log4j2
public class CommonUtils {

    public static final int TASK_TIMEOUT_SEC = 2400;
    public static final int TASK_POLLING_SEC = 20;

    private CommonUtils() {
    }

    public static void delayInSeconds(int second) {
        try {
            log.info("Sleeping for " + second + " seconds");
            Thread.sleep(second * 1000L);
        } catch (InterruptedException e) {
            log.error(e);
            Thread.currentThread().interrupt();
        }
    }

    public static void delayInSecondsWithDebugLog(long seconds) {
        try {
            log.debug("Sleeping for " + seconds + " seconds");
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            log.error(e);
            Thread.currentThread().interrupt();
        }
    }

    public static void delayInMilliSeconds(int milliSecons) {
        try {
            log.info("Sleeping for " + milliSecons + " milli seconds");
            Thread.sleep(milliSecons);
        } catch (InterruptedException e) {
            log.error(e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Utility Method to Rond double Values
     *
     * @param value
     * @param places
     * @return
     */
    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * @param String
     *            Input with properties seperated by '#'
     * @return List of NameValuePairs
     */
    public static Properties getPropertiesfromString(String input) {
        String keyValues = input.replaceAll("#", "\n");
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(keyValues));
        } catch (IOException e) {
            log.error("Unable to read properties ", e);
        }
        return properties;
    }

    /**
     * to seconds from hours
     *
     * @param hours
     * @return
     */
    public static Integer toSeconds(Integer hours) {
        return hours * 60 * 60;
    }

    public static boolean isFileExists(String fileNamePath) {
        return new File(fileNamePath).exists();
    }

    /**
     * Method to execute command in a remote machine
     *
     * @param command
     * @param vmIP
     * @param vmUser
     * @param vmPassword
     * @return output(string)
     */
    public static String runCommand(String command, String vmIP, String vmUser, String vmPassword) {
        RemoteHost remoteHost = new RemoteHost();
        return remoteHost.executeCommand(vmIP, vmUser, vmPassword, command);
    }

    /**
     * Method to execute list of commands in a remote machine
     *
     * @param commands
     * @param vmIP
     * @param vmUser
     * @param vmPassword
     * @return output(string)
     */

    public static String runCommandList(List<String> commands, String vmIP, String vmUser, String vmPassword) {
        StringBuilder output = new StringBuilder();
        for (String cmd : commands) {
            output.append(runCommand(cmd, vmIP, vmUser, vmPassword));
        }
        return output.toString();
    }

    public static String getLogfilePath(String executionName) {
        return getMangleLogDirectory() + File.separator + executionName.replace(":", "") + ".log";
    }

    private static String getMangleLogDirectory() {
        String catalinaHome = System.getProperty("catalina.home");
        return (catalinaHome != null) ? catalinaHome + File.separator + "logs"
                : System.getProperty("User.dir") + File.separator + "logs";
    }

    /**
     * Method to download file
     *
     * @param response
     * @param file
     * @return HttpServletResponse
     */
    public static HttpServletResponse sendFileDownloadResponse(HttpServletResponse response, File file) {
        int length;
        byte[] byteArray = new byte[] {};
        if (file != null) {
            try (InputStream logfileInputstream = new FileInputStream(file)) {

                length = logfileInputstream.available();
                byteArray = new byte[length];
                int bytesread = logfileInputstream.read(byteArray);
                log.info("Bytes read" + bytesread);
            } catch (IOException e) {
                log.info(e);
            }
            response.setContentType("application/octet-stream");
            if (!file.getName().contains("html")) {
                response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
            } else {
                response.setHeader("Content-Disposition", "inline; filename=" + file.getName());
            }
            response.setHeader("Content-Type", "text/html;charset=UTF-8");
        } else {
            response.setStatus(404);
        }
        try {
            response.getOutputStream().write(byteArray);
            response.flushBuffer();
        } catch (IOException e) {
            log.info(e);
        }
        return response;
    }

    @Description("this method extract the value from regex")
    public static Matcher extractValue(String regex, String text) {
        Pattern r = Pattern.compile(regex);
        Matcher m = r.matcher(text);
        if (m.find()) {
            return m;
        }
        return null;
    }

    /**
     * Method to see service status in linux
     *
     * @param sshUtils
     * @param service
     * @return boolean
     */
    public static boolean checkServiceStatusInLinux(SSHUtils sshUtils, String service, String message) {
        String command = "service " + service + " status";
        String output = sshUtils.runCommand(command).getCommandOutput();
        return output.contains(message) ? true : false;
    }

    /**
     * Method to start a stopped service in linux
     *
     * @param ip
     * @param userName
     * @param password
     * @param port
     * @param service
     * @param failureMessage
     * @param successMessage
     * @return boolean
     */
    public static boolean startServiceInLinux(String ip, String userName, String password, int port, String service,
            String failureMessage, String successMessage) {
        SSHUtils sshUtils = new SSHUtils(ip, userName, password, port);
        if (checkServiceStatusInLinux(sshUtils, service, failureMessage)) {
            sshUtils.runCommand("service " + service + " start");
            return checkServiceStatusInLinux(sshUtils, service, successMessage);
        } else {
            return checkServiceStatusInLinux(sshUtils, service, successMessage);
        }
    }

    /**
     * Method to check server listening on port
     *
     * @param host
     *            ip or hostname
     * @param port
     * @return boolean
     */
    public static boolean isServerListening(String host, int port) {
        try (Socket s = new Socket(host, port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Utility Method to Convert given List as a Comma Seperated Values String
     *
     * @param args
     * @return
     */
    public static String convertListToDelimitedString(List<String> args, String delimiter) {
        StringBuilder text = new StringBuilder();
        for (String arg : args) {
            text.append(arg + delimiter);
        }
        return text.substring(0, text.length() - 1);
    }

    /**
     * Utility method to copy Script File to Mangle Support Scripts directory from classpath
     * resource
     *
     * @param faultInjectionScriptInfo
     * @throws MangleException
     */
    public static void copyScriptFileToMangleDirectory(SupportScriptInfo faultInjectionScriptInfo)
            throws MangleException {
        String filePath = ConstantsUtils.getMangleSupportScriptDirectory() + File.separator
                + faultInjectionScriptInfo.getScriptFileName();
        File srcFile = new File(filePath);
        // Checking if the Script file is already available in Mangle Support
        // Script Folder
        if (!srcFile.exists()) {
            if (faultInjectionScriptInfo.isClassPathResource()) {
                try {
                    // Copying File to Mangle Support Script Folder from
                    // Classpath
                    FileUtils.copyFile(
                            ResourceUtils.getFile("classpath:" + faultInjectionScriptInfo.getScriptFileName()),
                            srcFile);
                } catch (IOException e) {
                    throw new MangleException(ErrorCode.SUPPORT_SCRIPT_FILE_NOT_FOUND,
                            faultInjectionScriptInfo.getScriptFileName());
                }
            } else {
                throw new MangleException(ErrorCode.SUPPORT_SCRIPT_FILE_NOT_FOUND, srcFile.getName());
            }
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

    /**
     * @param args
     * @param delimiter
     * @return
     */
    public static String convertMaptoDelimitedString(Map<String, String> args, String delimiter) {
        StringBuilder text = new StringBuilder();
        for (Entry<String, String> entry : args.entrySet()) {
            String value = entry.getValue();
            if (StringUtils.isEmpty(value)) {
                value = " ";
            }
            text.append(entry.getKey() + delimiter + value + delimiter);
        }
        return text.substring(0, text.length() - 1);
    }

    /**
     * Method to Convert Map to string with delimiter as a separator
     *
     * @param args
     * @param delimiter
     * @return String (Ex: if map is {"app":"mangle","build":"4.0.0.1"} and output string will be
     *         "app=mangle,build=4.0.0.1"
     */
    public static String maptoDelimitedKeyValuePairString(Map<String, String> args, String delimiter) {
        StringBuilder text = new StringBuilder();
        for (Entry<String, String> entry : args.entrySet()) {
            text.append(entry.getKey() + "=" + entry.getValue() + delimiter);
        }
        return text.substring(0, text.length() - 1);
    }

    /**
     * Method to extract command argument values from arguments string (ex: --vmname vm1 --faultName
     * POWEROFF_VM)
     *
     * @param argsString
     * @param argPrefix
     * @return
     */
    public static String[] getValuesFromCommandArgsString(String argsString, String argPrefix) {
        if (null == argPrefix) {
            argPrefix = "--";
        }
        StringTokenizer stringTokenizer = new StringTokenizer(argsString);
        List<String> argValues = new ArrayList<>();

        while (stringTokenizer.hasMoreTokens()) {
            String token = stringTokenizer.nextToken().trim();
            if (token.startsWith(argPrefix)) {
                argValues.add(stringTokenizer.nextToken().trim());
            }
        }
        return argValues.toArray(new String[0]);
    }

    /**
     * @param time:
     *            String representation of the time which is in the format "EEE MMM dd HH:mm:ss ZZZ
     *            yyyy"
     * @return : Date object representing the string time specified.
     */
    public static Date getDateObjectFor(String time) {
        SimpleDateFormat formatter = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);
        Date dateOf = null;
        try {
            dateOf = formatter.parse(time);
        } catch (ParseException e) {
            log.error(
                    "Parsing of the specified time failed. It could be due to the Specified input is not in the format: "
                            + Constants.DEFAULT_DATE_FORMAT);
            log.error("Returning null");
        }
        return dateOf;
    }

    /**
     * @param epochMillis:
     *            Time in miliseconds
     * @return : String version of time corresponding to the epoch miliseconds specified.
     */
    public static String getTime(long epochMillis) {
        DateFormat formatter = new SimpleDateFormat(Constants.GMT_DATE_FORMAT);
        formatter.setTimeZone(TimeZone.getTimeZone(Constants.GMT));
        return formatter.format(new Date(epochMillis));
    }

    /**
     * @param stringMap
     * @return: String array constructed by the provided map.
     */
    public static String[] getStringArrayFromMap(Map<String, String> stringMap) {

        String[] stringArray = new String[stringMap.size() * 2];
        int count = 0;
        for (Entry<String, String> each : stringMap.entrySet()) {
            stringArray[count] = each.getKey();
            count++;
            stringArray[count] = each.getValue();
            count++;
        }
        return stringArray;
    }

    /**
     * @param str
     * @return true if matches the regexp else false
     */
    public static boolean validateName(String str) {
        if (StringUtils.hasLength(str)) {
            String regexp = "^[A-Za-z0-9-_.]+$";
            return str.matches(regexp);
        }
        return false;
    }
}
