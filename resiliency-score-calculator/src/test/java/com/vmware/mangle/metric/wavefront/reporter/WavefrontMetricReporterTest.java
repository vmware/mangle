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

package com.vmware.mangle.metric.wavefront.reporter;

import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.wavefront.integrations.Wavefront;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.common.MockDataConstants;
import com.vmware.mangle.metric.common.Metric;

/**
 * Unit tests for class WavefrontMetricReporter
 * 
 * @author ranjans
 */
@SuppressWarnings("unchecked")
@PowerMockIgnore("javax.management.*")
@PrepareForTest(WavefrontMetricReporter.class)
public class WavefrontMetricReporterTest extends PowerMockTestCase {

    @Mock
    private Wavefront wavefront;
    private WavefrontMetricReporter wavefrontMetricReporter;

    @BeforeMethod
    public void initTests() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.whenNew(Wavefront.class).withArguments(anyString(), anyInt()).thenReturn(wavefront);
        PowerMockito.doNothing().when(wavefront).send(anyString(), anyDouble(), anyString(), anyMap());
        PowerMockito.doNothing().when(wavefront).send(anyString(), anyDouble(), anyLong(), anyString(), anyMap());
        PowerMockito.doNothing().when(wavefront).flush();
        HashMap<String, String> staticTags = new HashMap<>();
        staticTags.put(MockDataConstants.ANY_STR, MockDataConstants.ANY_STR);
        wavefrontMetricReporter =
                new WavefrontMetricReporter(MockDataConstants.ZERO_IP, 2878, MockDataConstants.ANY_STR, staticTags);
        PowerMockito.verifyNew(Wavefront.class, times(1)).withArguments(anyString(), anyInt());
    }

    @AfterMethod
    public void tearDown() {
        validateMockitoUsage();
    }

    @Test
    public void sendMetricByNameAndValTest() throws IOException {
        HashMap<String, String> tags = new HashMap<>();
        tags.put(MockDataConstants.ANY_STR, MockDataConstants.ANY_STR);
        boolean actualResult = wavefrontMetricReporter.sendMetric(MockDataConstants.VALID_METRIC, 0.0, tags);
        verify(wavefront, times(1)).send(anyString(), anyDouble(), anyString(), anyMap());
        verify(wavefront, times(1)).flush();
        Assert.assertTrue(actualResult);
    }

    @Test
    public void sendMetricByMetricTest() throws IOException {
        boolean actualResult = wavefrontMetricReporter
                .sendMetric(new Metric(MockDataConstants.VALID_METRIC, 0.0, MockDataConstants.ANY_STR));
        verify(wavefront, times(1)).send(anyString(), anyDouble(), anyLong(), anyString(), anyMap());
        verify(wavefront, times(1)).flush();
        Assert.assertTrue(actualResult);
    }

    @Test
    public void sendMetricsTest() throws IOException {
        boolean actualResult = wavefrontMetricReporter.sendMetrics(new ArrayList<Metric>(
                Arrays.asList(new Metric(MockDataConstants.VALID_METRIC, 0.0, MockDataConstants.ANY_STR))));
        verify(wavefront, times(1)).send(anyString(), anyDouble(), anyLong(), anyString(), anyMap());
        verify(wavefront, times(1)).flush();
        Assert.assertTrue(actualResult);
    }

    @Test
    public void validateMetricTest() {
        Assert.assertTrue(wavefrontMetricReporter.validateMetric(MockDataConstants.VALID_METRIC, 0.0));
    }

    @Test
    public void getWavefrontProxyTest() {
        Assert.assertNotNull(wavefrontMetricReporter.getWavefrontProxy());
    }

    @Test
    public void getMetricSourceTest() {
        Assert.assertNotNull(wavefrontMetricReporter.getMetricSource());
    }

}
