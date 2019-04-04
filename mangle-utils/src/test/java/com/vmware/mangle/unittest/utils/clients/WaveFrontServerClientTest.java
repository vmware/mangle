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

package com.vmware.mangle.unittest.utils.clients;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.metricprovider.WaveFrontConnectionProperties;
import com.vmware.mangle.metric.reporter.constants.MetricReporterTestConstants;
import com.vmware.mangle.utils.clients.metricprovider.WaveFrontServerClient;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;

/**
 * Unit Test Case for WaveFrontServerClient.
 *
 * @author kumargautam
 */
@PrepareForTest(value = { WaveFrontServerClient.class })
@PowerMockIgnore(value = { "javax.net.ssl.*" })
public class WaveFrontServerClientTest extends PowerMockTestCase {

    private WaveFrontServerClient waveFrontServerClientTest;
    @Mock
    private WaveFrontServerClient restTemplateWrapper;
    private WaveFrontConnectionProperties waveFrontConnectionProperties;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.waveFrontConnectionProperties = new WaveFrontConnectionProperties();
        this.waveFrontConnectionProperties.setWavefrontInstance(MetricReporterTestConstants.WAVEFRONT_INSTANCE);
        this.waveFrontConnectionProperties.setWavefrontAPIToken(MetricReporterTestConstants.WAVEFRONT_API_TOKEN);
        this.waveFrontServerClientTest = new WaveFrontServerClient(waveFrontConnectionProperties);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public void tearDownAfterClass() throws Exception {
        this.waveFrontServerClientTest = null;
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
     * {@link com.vmware.mangle.WaveFrontServerClient.metricprovider.MangleWaveFrontServerClient#testConnection()}.
     */
    @Test
    @SuppressWarnings("rawtypes")
    public void testTestConnection() {
        ResponseEntity responseEntity = mock(ResponseEntity.class);
        Method method = PowerMockito.method(RestTemplateWrapper.class, "get", String.class, Class.class);
        PowerMockito.replace(method).with((new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return responseEntity;
            }
        }));

        when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        Assert.assertTrue(waveFrontServerClientTest.testConnection());
        verify(responseEntity, times(1)).getStatusCode();
    }

}
