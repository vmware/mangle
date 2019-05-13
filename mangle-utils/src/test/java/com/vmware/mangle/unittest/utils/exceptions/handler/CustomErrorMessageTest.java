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

package com.vmware.mangle.unittest.utils.exceptions.handler;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.core.env.Environment;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.CustomErrorMessage;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Unit Test case for CustomErrorMessage.
 *
 * @author kumargautam
 */
@PrepareForTest(value = { Environment.class })
public class CustomErrorMessageTest extends PowerMockTestCase {

    @InjectMocks
    private CustomErrorMessage customErrorMessage;
    @Mock
    private Environment env;

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @AfterClass
    public void tearDownAfterClass() throws Exception {
        env = null;
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
        validateMockitoUsage();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.exceptions.handler.CustomErrorMessage#getErrorMessage(MangleException mangleException)}.
     */
    @Test
    public void testGetErrorMessageMangleException() {
        when(env.containsProperty(anyString())).thenReturn(true);
        when(env.getProperty(anyString())).thenReturn("Script File is Not Available.");
        MangleException mangleException =
                new MangleException(ErrorCode.SUPPORT_SCRIPT_FILE_NOT_FOUND, (Object) null);
        String actualResult = customErrorMessage.getErrorMessage(mangleException);
        verify(env, times(1)).containsProperty(anyString());
        verify(env, times(1)).getProperty(anyString());
        String expectedResult = "Script File is Not Available.";
        Assert.assertEquals(actualResult, expectedResult);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.exceptions.handler.CustomErrorMessage#getErrorMessage(com.vmware.mangle.MangleRuntimeException.MangleRuntimeException mangleRuntimeException)}.
     */
    @Test
    public void testGetErrorMessageMangleRuntimeException() {
        when(env.containsProperty(anyString())).thenReturn(true);
        when(env.getProperty(anyString())).thenReturn("File size exceeded max file size limit of {0} .");
        MangleRuntimeException mangleException = new MangleRuntimeException(ErrorCode.FILE_SIZE_EXCEEDED, "1MB");
        String actualResult = customErrorMessage.getErrorMessage(mangleException);
        verify(env, times(1)).containsProperty(anyString());
        verify(env, times(1)).getProperty(anyString());
        String expectedResult = "File size exceeded max file size limit of 1MB .";
        Assert.assertEquals(actualResult, expectedResult);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.exceptions.handler.CustomErrorMessage#getErrorMessage(String errorCode, String errorMsg, Object[] args)}.
     */
    @Test
    public void testGetErrorMessageString() {
        when(env.getProperty(anyString())).thenReturn("File size exceeded max file size limit of {0}");
        String actualResult = customErrorMessage.getErrorMessage(ErrorCode.FILE_SIZE_EXCEEDED.getCode());
        verify(env, times(1)).getProperty(anyString());
        String expectedResult = "File size exceeded max file size limit of ";
        Assert.assertEquals(actualResult, expectedResult);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.exceptions.handler.CustomErrorMessage#formatErrorMessage(String errorMsg, Object... args)}.
     */
    @Test
    public void testFormatErrorMessage() {
        String actualResult = customErrorMessage.formatErrorMessage("No Record Found for {0} is {1} .",
                ErrorConstants.ENDPOINT_NAME, "docker53");
        String expectedResult = "No Record Found for EndpointName is docker53 .";
        Assert.assertEquals(actualResult, expectedResult);
    }

}
