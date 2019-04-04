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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.metricprovider.WaveFrontConnectionProperties;
import com.vmware.mangle.metric.reporter.constants.MetricReporterTestConstants;
import com.vmware.mangle.utils.clients.metricprovider.WaveFrontProxyClient;
import com.vmware.mangle.utils.clients.metricprovider.WaveFrontServerClient;

/**
 * Unit Test Case for WaveFrontProxyClient.
 *
 * @author kumargautam
 */
@PrepareForTest(value = { WaveFrontProxyClient.class })
public class WaveFrontProxyClientTest extends PowerMockTestCase {

    @Mock
    private WaveFrontServerClient waveFrontServerClient;
    private WaveFrontProxyClient waveFrontProxyClient;
    private WaveFrontConnectionProperties waveFrontConnectionProperties;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        this.waveFrontConnectionProperties = this.getWaveFrontConnectionProperties();
        this.waveFrontProxyClient = new WaveFrontProxyClient(waveFrontConnectionProperties);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public void tearDownAfterClass() throws Exception {
        this.waveFrontProxyClient = null;
        this.waveFrontConnectionProperties = null;
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
        validateMockitoUsage();
    }

    /**
     *
     * @return
     */
    private WaveFrontConnectionProperties getWaveFrontConnectionProperties() {
        WaveFrontConnectionProperties waveFrontConnectionProperties = new WaveFrontConnectionProperties();
        waveFrontConnectionProperties.setWavefrontInstance(MetricReporterTestConstants.WAVEFRONT_INSTANCE);
        waveFrontConnectionProperties.setWavefrontAPIToken(MetricReporterTestConstants.WAVEFRONT_API_TOKEN);
        waveFrontConnectionProperties.setWaveFrontProxy(MetricReporterTestConstants.WAVEFRONT_PROXY);
        waveFrontConnectionProperties.setWaveFrontProxyPort(MetricReporterTestConstants.WAVEFRONT_PROXY_PORT);
        waveFrontConnectionProperties.setSource(MetricReporterTestConstants.WAVEFRONT_SOURCE);
        HashMap<String, String> staticTags = new HashMap<>();
        staticTags.put(MetricReporterTestConstants.WAVEFRONT_STATIC_TAG_KEY,
                MetricReporterTestConstants.WAVEFRONT_STATIC_TAG_VALUE);
        waveFrontConnectionProperties.setStaticTags(staticTags);
        return waveFrontConnectionProperties;
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.WaveFrontProxyClient.metricprovider.MangleWaveFrontProxyClient#testConnection()}.
     *
     * @throws Exception
     */
    @Test
    public void testTestConnection() throws Exception {
        PowerMockito.whenNew(WaveFrontServerClient.class).withArguments(any(WaveFrontConnectionProperties.class))
                .thenReturn(waveFrontServerClient);
        when(waveFrontServerClient.testConnection()).thenReturn(true);
        Assert.assertTrue(waveFrontProxyClient.testConnection());
        PowerMockito.verifyNew(WaveFrontServerClient.class, times(1))
                .withArguments(any(WaveFrontConnectionProperties.class));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.WaveFrontProxyClient.metricprovider.MangleWaveFrontProxyClient#getWaveFrontConnectionProperties()}.
     */
    @Test
    public void testGetWaveFrontConnectionProperties() {
        Assert.assertEquals(waveFrontProxyClient.getWaveFrontConnectionProperties(),
                waveFrontConnectionProperties);
    }

}
