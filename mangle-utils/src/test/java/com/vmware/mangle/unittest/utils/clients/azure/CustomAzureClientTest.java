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

package com.vmware.mangle.unittest.utils.clients.azure;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.azure.management.Azure;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.Test;

import com.vmware.mangle.utils.clients.azure.CustomAzureClient;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 *
 *
 * @author bkaranam
 */
@PowerMockIgnore({ "javax.net.ssl.*", "javax.xml.parsers.*", "com.sun.org.apache.xerces.internal.jaxp.*",
        "org.apache.logging.log4j.*", "javax.management.*", "com.nimbusds.oauth2.*", "javax.mail.*",
        "net.minidev.json.*", "com.sun.org.apache.xalan.internal.xsltc.trax.*" })
@PrepareForTest(Azure.class)
public class CustomAzureClientTest extends PowerMockTestCase {

    private final String SUBSCRIPTION_ID = "azure endpoint";
    private final String TENANT = "teant-1";
    private final String CLIENT_ID = "client-id";
    private final String CLIENT_KEY = "client-key";
    // private CustomAzureClient customAzureClient;

    @Test
    public void testAzureClientInstantiation() throws Exception {
        CustomAzureClient customAzureClient = new CustomAzureClient(SUBSCRIPTION_ID, TENANT, CLIENT_ID, CLIENT_KEY);
        assertNotNull(customAzureClient);
    }

    @Test
    public void testTestConnectionWithInvalidCredentials() throws Exception {
        CustomAzureClient customAzureClient = new CustomAzureClient(SUBSCRIPTION_ID, TENANT, CLIENT_ID, CLIENT_KEY);
        CustomAzureClient mockcustomAzureClient = Mockito.spy(customAzureClient);
        RuntimeException runtimeException = Mockito.mock(RuntimeException.class);
        AuthenticationException authException = Mockito.mock(AuthenticationException.class);
        Mockito.doThrow(runtimeException).when(mockcustomAzureClient).getClient();
        when(runtimeException.getCause()).thenReturn(authException);
        when(authException.getCause()).thenReturn(authException);
        boolean result = false;
        try {
            result = mockcustomAzureClient.testConnection();
        } catch (MangleException exception) {
            assertEquals(exception.getErrorCode(), ErrorCode.AZURE_INVALID_CREDENTIALS);
        }
        assertFalse(result);
    }

    @Test
    public void testTestConnectionForUnknownError() throws Exception {
        CustomAzureClient customAzureClient = new CustomAzureClient(SUBSCRIPTION_ID, TENANT, CLIENT_ID, CLIENT_KEY);
        CustomAzureClient mockcustomAzureClient = Mockito.spy(customAzureClient);
        Azure mockAzure = PowerMockito.mock(Azure.class);
        Mockito.doReturn(mockAzure).when(mockcustomAzureClient).getClient();
        when(mockAzure.subscriptions()).thenThrow(new NullPointerException());
        try {
            mockcustomAzureClient.testConnection();
            fail("CustomAzureClient testConnection test failed by not throwing mangle exception as expected");
        } catch (MangleException exception) {
            assertEquals(exception.getErrorCode(), ErrorCode.AZURE_UNKNOWN_ERROR);
        }
    }
}
