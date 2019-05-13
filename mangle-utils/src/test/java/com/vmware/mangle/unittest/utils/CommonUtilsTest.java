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

package com.vmware.mangle.unittest.utils;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.regex.Matcher;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.RemoteHost;
import com.vmware.mangle.utils.clients.ssh.SSHUtils;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.mockdata.CommandResultUtils;

/**
 *
 *
 * @author chetanc
 */
@PrepareForTest(value = { CommonUtils.class })
public class CommonUtilsTest extends PowerMockTestCase {

    Double value = 234.1341361234132;
    private static final String filename1 =
            "src/main/resources/FaultOperationProperties/VCenterFaultOperations.properties";
    private static final String filename3 = "src/test/resources/MockFile.properties";
    private String command = "dummy command";
    private String ip = "10.10.10.10";
    private int port = 80;
    private String username = "username";
    private String pwd = "pwd";
    private String service = "mangle";

    @Mock
    private RemoteHost remoteHost;

    @Mock
    private File file;

    @BeforeClass
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(priority = 0)
    public void testRound() {
        double result = CommonUtils.round(value, 2);
        Assert.assertEquals(result, 234.13);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, priority = 1)
    public void testRoundFailure() {
        CommonUtils.round(value, -1);
    }

    @Test(priority = 2)
    public void testToSecods() {
        int result = CommonUtils.toSeconds(1);
        Assert.assertEquals(result, 3600);
    }

    @Test(priority = 3)
    public void testIsFileExists() {
        boolean result = CommonUtils.isFileExists(filename1);
        Assert.assertTrue(result);
    }

    @Test(priority = 4)
    public void testRunCommand() throws Exception {
        PowerMockito.whenNew(RemoteHost.class).withNoArguments().thenReturn(remoteHost);
        PowerMockito.when(remoteHost.executeCommand(any(), any(), any(), any())).thenReturn("Successful");
        String result = CommonUtils.runCommand(command, ip, username, pwd);
        Assert.assertEquals(result, "Successful");
    }

    @Test(priority = 5)
    public void testRunCommandList() throws Exception {
        PowerMockito.whenNew(RemoteHost.class).withNoArguments().thenReturn(remoteHost);
        PowerMockito.when(remoteHost.executeCommand(any(), any(), any(), any())).thenReturn("Successful");
        List<String> list = new ArrayList<>(Arrays.asList(command));
        String result = CommonUtils.runCommandList(list, ip, username, pwd);
        Assert.assertEquals(result, "Successful");
    }

    @Test(priority = 12)
    public void testGetPropertiesfromString() {
        String propertiesString = "command:echo test#name: command file";
        Properties properties = CommonUtils.getPropertiesfromString(propertiesString);
        Assert.assertEquals(properties.get("name"), "command file");
        Assert.assertEquals(properties.get("command"), "echo test");
    }

    @Test(priority = 13)
    public void testGetPropertiesfromStringFailure() throws Exception {
        // These two lines are tightly bound.
        PowerMockito.whenNew(StringReader.class).withAnyArguments().thenThrow(new IOException());
        String propertiesString = "command:echo test#name: command file";
        Properties properties = CommonUtils.getPropertiesfromString(propertiesString);
        Assert.assertEquals(properties.size(), 0);
    }

    @Test(priority = 14)
    public void testExtractValue() {
        String regex = "[0-9]{3}";
        String text = "number is 576";
        Matcher matcher = CommonUtils.extractValue(regex, text);
        Assert.assertNotNull(matcher);
    }

    @Test(description = "Test to verify the extraction of the value matching the given regex, the method should return null when the given string doesn't have any matching value", priority = 15)
    public void testExtractValue2() {
        String regex = "[0-9]{3}";
        String text = "number is nothing";
        Matcher matcher = CommonUtils.extractValue(regex, text);
        Assert.assertNull(matcher);
    }

    @Test(priority = 16)
    public void testExtractField() {
        String regex = "[0-9]{3}";
        String text = "number is 576";
        String matched = CommonUtils.extractField(text, regex);
        Assert.assertNotNull(matched);
        Assert.assertEquals(matched, "576");
    }

    @Test(description = "Test to verify the extraction of value matching the given regex should return null when the given string doesn't have any matching value", priority = 17)
    public void testExtractField2() {
        String regex = "[0-9]{3}";
        String text = "number is nothing";
        String matched = CommonUtils.extractField(text, regex);
        Assert.assertNull(matched);
    }

    @Test(priority = 18)
    public void testConvertMaptoDelimitedString() {
        Map<String, String> map = new HashMap<>();
        map.put("name", "mangle");
        String returnString = CommonUtils.convertMaptoDelimitedString(map, " ");
        boolean doesContainString = returnString.contains("name") && returnString.contains("mangle");
        Assert.assertTrue(doesContainString);
    }

    @Test(priority = 19)
    public void testMaptoDelimitedKeyValuePairString() {
        Map<String, String> map = new HashMap<>();
        map.put("name", "mangle");
        map.put("tech", "qe");
        String returnString = CommonUtils.maptoDelimitedKeyValuePairString(map, " ");
        boolean doesContainString = returnString.contains("name") && returnString.contains("mangle");
        Assert.assertTrue(doesContainString);
        Assert.assertTrue(returnString.contains("name=mangle"));
        Assert.assertTrue(returnString.contains("tech=qe"));
    }

    @Test(priority = 20)
    public void testGetValuesFromCommandArgsString() {
        String text = "--name:mangle --tech:qe";
        String[] mapString = CommonUtils.getValuesFromCommandArgsString(text, null);
        Assert.assertNotNull(mapString);
    }

    @Test(description = "Test to verify that the getValuesFromCommandArgsString returns the proper tags for a given string with the matching argsprefix", priority = 21)
    public void testGetValuesFromCommandArgsString1() {
        String text = "__name:mangle __tech:qe";
        String[] mapString = CommonUtils.getValuesFromCommandArgsString(text, "__");
        Assert.assertNotNull(mapString);
    }

    @Test(priority = 22)
    public void testIsServerListening() throws Exception {
        Socket socket = Mockito.mock(Socket.class);
        PowerMockito.whenNew(Socket.class).withAnyArguments().thenReturn(socket);
        boolean isServerRunning = CommonUtils.isServerListening(ip, port);
        Assert.assertTrue(isServerRunning);
    }

    @Test(priority = 23)
    public void testIsServerListeningFailure() throws Exception {
        PowerMockito.whenNew(Socket.class).withArguments(ip, port).thenThrow(new IOException());
        boolean isServerRunning = CommonUtils.isServerListening(ip, port);
        Assert.assertFalse(isServerRunning);
    }

    @Test(priority = 24)
    public void testStartServiceInLinux() throws Exception {
        SSHUtils sshUtils = Mockito.mock(SSHUtils.class);
        PowerMockito.whenNew(SSHUtils.class).withAnyArguments().thenReturn(sshUtils);
        when(sshUtils.runCommand(any())).thenReturn(CommandResultUtils.getCommandResult(""));
        boolean isServiceRunning = CommonUtils.startServiceInLinux(ip, username, pwd, port, service, "", "");
        Assert.assertTrue(isServiceRunning);

    }

    @Test(description = "Test to verify that starting of the service, is executed with a non null execution output, thus the method startServiceInLinux will return true indicating service is running", priority = 25)
    public void testStartServiceInLinux1() throws Exception {
        SSHUtils sshUtils = Mockito.mock(SSHUtils.class);
        PowerMockito.whenNew(SSHUtils.class).withAnyArguments().thenReturn(sshUtils);
        when(sshUtils.runCommand(any())).thenReturn(CommandResultUtils.getCommandResult("echo"));
        boolean isServiceRunning = CommonUtils.startServiceInLinux(ip, username, pwd, port, service, "", "");
        Assert.assertTrue(isServiceRunning);
    }

    @Test(priority = 26)
    public void testConvertListToDelimitedString() {
        List<String> list = new ArrayList<>(Arrays.asList("mangle", "qe"));
        String returnString = CommonUtils.convertListToDelimitedString(list, "-");
        Assert.assertTrue(returnString.contains("mangle"));
        Assert.assertTrue(returnString.contains("qe"));

    }

    @Test(priority = 27)
    public void testSendFileDownloadResponse() throws IOException {
        HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
        ServletOutputStream stream = Mockito.mock(ServletOutputStream.class);
        doNothing().when(httpServletResponse).setContentType(any());
        doNothing().when(httpServletResponse).setHeader(anyString(), anyString());
        doNothing().when(httpServletResponse).setStatus(anyInt());
        when(httpServletResponse.getOutputStream()).thenReturn(stream);
        doNothing().when(stream).write(any());
        HttpServletResponse response = CommonUtils.sendFileDownloadResponse(httpServletResponse, new File(filename3));
        Assert.assertEquals(response, httpServletResponse);
        verify(httpServletResponse, times(1)).setContentType(anyString());
        verify(httpServletResponse, times(2)).setHeader(anyString(), anyString());
    }

    @Test(description = "Test to verify the sendFileDownloadResponse method when the file provided is null", priority = 28)
    public void testSendFileDownloadResponse1() throws IOException {
        HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
        ServletOutputStream stream = Mockito.mock(ServletOutputStream.class);
        doNothing().when(httpServletResponse).setContentType(any());
        doNothing().when(httpServletResponse).setHeader(anyString(), anyString());
        doNothing().when(httpServletResponse).setStatus(anyInt());
        when(httpServletResponse.getOutputStream()).thenReturn(stream);
        doNothing().when(stream).write(any());
        HttpServletResponse response = CommonUtils.sendFileDownloadResponse(httpServletResponse, null);
        Assert.assertEquals(response, httpServletResponse);
        verify(httpServletResponse, times(1)).setStatus(anyInt());
    }

    @Test(description = "Test to validate method String to Date object when String is in default format DEFAULT_DATE_FORMAT = \"EEE MMM dd HH:mm:ss ZZZ yyyy\"", priority = 29)
    public void testgetDateObjectForString() {
        String timeInDifferentFormat = "Febraury 28, 2019 Thu 10:40:48";
        Date dateForDifferentTimeFormat = CommonUtils.getDateObjectFor(timeInDifferentFormat);
        Assert.assertEquals(dateForDifferentTimeFormat, null);
    }

    @Test(description = " Test to validate method Epoch to String ", priority = 30)
    public void testGetTime() {
        Date timeNow = new Date();
        long timeInEpoch = timeNow.getTime();

        DateFormat formatter = new SimpleDateFormat(Constants.GMT_DATE_FORMAT);
        formatter.setTimeZone(TimeZone.getTimeZone(Constants.GMT));
        String expectedTimeStamp = formatter.format(timeNow);

        String timeStamp = CommonUtils.getTime(timeInEpoch);
        Assert.assertEquals(timeStamp, expectedTimeStamp);
    }

    @Test
    public void testValidateName() {
        Assert.assertTrue(CommonUtils.validateName("dummy_name-test.com"));
        Assert.assertFalse(CommonUtils.validateName("^#test@test.com$"));
        Assert.assertFalse(CommonUtils.validateName(null));
    }
}