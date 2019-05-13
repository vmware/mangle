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

package com.vmware.mangle.unittest.metric.reporter;

import static org.mockito.Matchers.any;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wavefront.integrations.Wavefront;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.metricprovider.WaveFrontConnectionProperties;
import com.vmware.mangle.metric.reporter.WavefrontMetricReporter;
import com.vmware.mangle.metric.reporter.common.Metric;
import com.vmware.mangle.metric.reporter.helpers.metric.MetricsHelper;
import com.vmware.mangle.unittest.metric.constants.MetricReporterTestConstants;

/**
 * Unit Test Case for WavefrontMetricReporter.
 *
 * @author kumargautam
 */
@PrepareForTest(value = { WavefrontMetricReporter.class, MetricsHelper.class })
public class WavefrontMetricReporterTest extends PowerMockTestCase {


    private WavefrontMetricReporter wavefrontMetricReporter;
    @Mock
    private Wavefront wavefront;

    private WaveFrontConnectionProperties waveFrontConnectionProperties;
    private Map<String, String> pointTags;

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUpBeforeClass() throws Exception {
        PowerMockito.mockStatic(MetricsHelper.class);
        MockitoAnnotations.initMocks(this);
        this.waveFrontConnectionProperties = this.getWaveFrontConnectionProperties();
        pointTags = new HashMap<>();
        pointTags.put("load", "80");
    }

    /**
     *
     * @return
     */
    private WaveFrontConnectionProperties getWaveFrontConnectionProperties() {
        WaveFrontConnectionProperties waveFrontConnectionProperties = new WaveFrontConnectionProperties();
        waveFrontConnectionProperties.setWavefrontInstance(MetricReporterTestConstants.WAVEFRONT_INSTANCE);
        waveFrontConnectionProperties.setWavefrontAPIToken(MetricReporterTestConstants.WAVEFRONT_API_TOKEN);
        waveFrontConnectionProperties.setSource(MetricReporterTestConstants.WAVEFRONT_SOURCE);
        HashMap<String, String> staticTags = new HashMap<>();
        staticTags.put(MetricReporterTestConstants.WAVEFRONT_STATIC_TAG_KEY,
                MetricReporterTestConstants.WAVEFRONT_STATIC_TAG_VALUE);
        waveFrontConnectionProperties.setStaticTags(staticTags);
        return waveFrontConnectionProperties;
    }

    private String getWavefrontProxyHost() {
        return MetricReporterTestConstants.WAVEFRONT_PROXY;
    }

    private int getWavefrontProxyPort() {
        return MetricReporterTestConstants.WAVEFRONT_PROXY_PORT;
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public void tearDownAfterClass() throws Exception {
        this.waveFrontConnectionProperties = null;
        this.pointTags = null;
        this.wavefrontMetricReporter = null;
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
     * {@link com.vmware.mangle.reporter.WavefrontMetricReporter#sendMetric(java.lang.String, double, java.util.Map)}.
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSendMetricStringDoubleMapOfStringString() throws Exception {
        PowerMockito.whenNew(Wavefront.class).withArguments(anyString(), anyInt()).thenReturn(wavefront);
        this.wavefrontMetricReporter = new WavefrontMetricReporter(getWavefrontProxyHost(), getWavefrontProxyPort(),
                waveFrontConnectionProperties.getSource(), waveFrontConnectionProperties.getStaticTags());
        PowerMockito.verifyNew(Wavefront.class, times(1)).withArguments(anyString(), anyInt());
        PowerMockito.mockStatic(MetricsHelper.class);
        PowerMockito.when(MetricsHelper.getDoubleEquivalent(any(Object.class))).thenReturn(75.5);
        PowerMockito.when(MetricsHelper.isAValidMetricName(anyString())).thenReturn(true);
        PowerMockito.when(MetricsHelper.isAValidMetricValue(any(Object.class))).thenReturn(true);
        PowerMockito.doNothing().when(wavefront).send(anyString(), anyDouble(), anyString(), anyMap());
        PowerMockito.doNothing().when(wavefront).flush();
        boolean actualResult =
                wavefrontMetricReporter.sendMetric(MetricReporterTestConstants.CPU_USAGE, 75.5, pointTags);
        verify(wavefront, times(1)).send(anyString(), anyDouble(), anyString(), anyMap());
        verify(wavefront, times(1)).flush();
        Assert.assertTrue(actualResult);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.reporter.WavefrontMetricReporter#sendMetric(java.lang.String, double, java.util.Map)}.
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test(description = "Test is to verify IOException failure, while sending metric to wavefront")
    public void testSendMetricStringDoubleMapOfStringString1() throws Exception {
        PowerMockito.whenNew(Wavefront.class).withArguments(anyString(), anyInt()).thenReturn(wavefront);
        this.wavefrontMetricReporter = new WavefrontMetricReporter(getWavefrontProxyHost(), getWavefrontProxyPort(),
                waveFrontConnectionProperties.getSource(), waveFrontConnectionProperties.getStaticTags());
        PowerMockito.verifyNew(Wavefront.class, times(1)).withArguments(anyString(), anyInt());
        PowerMockito.mockStatic(MetricsHelper.class);
        PowerMockito.when(MetricsHelper.getDoubleEquivalent(any(Object.class))).thenReturn(75.5);
        PowerMockito.when(MetricsHelper.isAValidMetricName(anyString())).thenReturn(true);
        PowerMockito.when(MetricsHelper.isAValidMetricValue(any(Object.class))).thenReturn(true);
        PowerMockito.doThrow(new IOException()).when(wavefront).send(anyString(), anyDouble(), anyString(), anyMap());
        boolean actualResult =
                wavefrontMetricReporter.sendMetric(MetricReporterTestConstants.CPU_USAGE, 75.5, pointTags);
        verify(wavefront, times(1)).send(anyString(), anyDouble(), anyString(), anyMap());
        Assert.assertFalse(actualResult);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.reporter.WavefrontMetricReporter#sendMetric(java.lang.String, double, java.util.Map)}.
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test(description = "Test is to verify the failure during the transmission of metric to wavefront, when the metric value is invalid")
    public void testSendMetricStringDoubleMapOfStringString2() throws Exception {
        PowerMockito.whenNew(Wavefront.class).withArguments(anyString(), anyInt()).thenReturn(wavefront);
        this.wavefrontMetricReporter = new WavefrontMetricReporter(getWavefrontProxyHost(), getWavefrontProxyPort(),
                waveFrontConnectionProperties.getSource(), waveFrontConnectionProperties.getStaticTags());
        PowerMockito.verifyNew(Wavefront.class, times(1)).withArguments(anyString(), anyInt());
        PowerMockito.mockStatic(MetricsHelper.class);
        PowerMockito.when(MetricsHelper.getDoubleEquivalent(any(Object.class))).thenReturn(75.5);
        PowerMockito.when(MetricsHelper.isAValidMetricName(anyString())).thenReturn(true);
        PowerMockito.when(MetricsHelper.isAValidMetricValue(any(Object.class))).thenReturn(false);
        boolean actualResult =
                wavefrontMetricReporter.sendMetric(MetricReporterTestConstants.CPU_USAGE, 75.5, pointTags);
        verify(wavefront, times(0)).send(anyString(), anyDouble(), anyString(), anyMap());
        verify(wavefront, times(0)).flush();
        Assert.assertFalse(actualResult);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.reporter.WavefrontMetricReporter#sendMetric(com.vmware.mangle.metric.common.Metric)}.
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test(description = "Test to verify the successful transmission of metric to wavefront")
    public void testSendMetric() throws Exception {
        PowerMockito.whenNew(Wavefront.class).withArguments(anyString(), anyInt()).thenReturn(wavefront);
        this.wavefrontMetricReporter = new WavefrontMetricReporter(getWavefrontProxyHost(), getWavefrontProxyPort(),
                waveFrontConnectionProperties.getSource(), waveFrontConnectionProperties.getStaticTags());
        PowerMockito.verifyNew(Wavefront.class, times(1)).withArguments(anyString(), anyInt());
        PowerMockito.mockStatic(MetricsHelper.class);
        PowerMockito.when(MetricsHelper.getDoubleEquivalent(any(Object.class))).thenReturn(75.5);
        PowerMockito.when(MetricsHelper.isAValidMetricName(anyString())).thenReturn(true);
        PowerMockito.when(MetricsHelper.isAValidMetricValue(any(Object.class))).thenReturn(true);
        PowerMockito.doNothing().when(wavefront).send(anyString(), anyDouble(), anyLong(), anyString(), anyMap());
        PowerMockito.doNothing().when(wavefront).flush();
        Metric metric = new Metric(MetricReporterTestConstants.CPU_USAGE, 75.5, (HashMap<String, String>) pointTags,
                waveFrontConnectionProperties.getSource());
        boolean actualResult = wavefrontMetricReporter.sendMetric(metric);
        verify(wavefront, times(1)).send(anyString(), anyDouble(), anyLong(), anyString(), anyMap());
        verify(wavefront, times(1)).flush();
        Assert.assertTrue(actualResult);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.reporter.WavefrontMetricReporter#sendMetric(com.vmware.mangle.metric.common.Metric)}.
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test(description = "Test is to verify failure with IOException Failure while transmission of metric to wavefront fails with false output")
    public void testSendMetric1() throws Exception {
        PowerMockito.whenNew(Wavefront.class).withArguments(anyString(), anyInt()).thenReturn(wavefront);
        this.wavefrontMetricReporter = new WavefrontMetricReporter(getWavefrontProxyHost(), getWavefrontProxyPort(),
                waveFrontConnectionProperties.getSource(), waveFrontConnectionProperties.getStaticTags());
        PowerMockito.verifyNew(Wavefront.class, times(1)).withArguments(anyString(), anyInt());
        PowerMockito.mockStatic(MetricsHelper.class);
        PowerMockito.when(MetricsHelper.getDoubleEquivalent(any(Object.class))).thenReturn(75.5);
        PowerMockito.when(MetricsHelper.isAValidMetricName(anyString())).thenReturn(true);
        PowerMockito.when(MetricsHelper.isAValidMetricValue(any(Object.class))).thenReturn(true);
        PowerMockito.doThrow(new IOException()).when(wavefront).send(anyString(), anyDouble(), anyLong(), anyString(),
                anyMap());
        Metric metric = new Metric(MetricReporterTestConstants.CPU_USAGE, 75.5, (HashMap<String, String>) pointTags,
                waveFrontConnectionProperties.getSource());

        boolean actualResult = wavefrontMetricReporter.sendMetric(metric);
        verify(wavefront, times(1)).send(anyString(), anyDouble(), anyLong(), anyString(), anyMap());
        Assert.assertFalse(actualResult);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.reporter.WavefrontMetricReporter#sendMetric(com.vmware.mangle.metric.common.Metric)}.
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test(description = "Test is to verify the failure of transmission of metric to wavefront when the given metric value is not valid ")
    public void testSendMetricMetric2() throws Exception {
        PowerMockito.whenNew(Wavefront.class).withArguments(anyString(), anyInt()).thenReturn(wavefront);
        this.wavefrontMetricReporter = new WavefrontMetricReporter(getWavefrontProxyHost(), getWavefrontProxyPort(),
                waveFrontConnectionProperties.getSource(), waveFrontConnectionProperties.getStaticTags());
        PowerMockito.verifyNew(Wavefront.class, times(1)).withArguments(anyString(), anyInt());
        PowerMockito.mockStatic(MetricsHelper.class);
        PowerMockito.when(MetricsHelper.getDoubleEquivalent(any(Object.class))).thenReturn(75.5);
        PowerMockito.when(MetricsHelper.isAValidMetricName(anyString())).thenReturn(true);
        PowerMockito.when(MetricsHelper.isAValidMetricValue(any(Object.class))).thenReturn(false);
        Metric metric = new Metric(MetricReporterTestConstants.CPU_USAGE, 75.5, (HashMap<String, String>) pointTags,
                waveFrontConnectionProperties.getSource());
        boolean actualResult = wavefrontMetricReporter.sendMetric(metric);
        verify(wavefront, times(0)).send(anyString(), anyDouble(), anyLong(), anyString(), anyMap());
        verify(wavefront, times(0)).flush();
        Assert.assertFalse(actualResult);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.reporter.WavefrontMetricReporter#sendMetrics(java.util.List)}.
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test(description = "Test to verify the successful transmission of metric to wavefront")
    public void testSendMetrics() throws Exception {
        PowerMockito.whenNew(Wavefront.class).withArguments(anyString(), anyInt()).thenReturn(wavefront);
        this.wavefrontMetricReporter = new WavefrontMetricReporter(getWavefrontProxyHost(), getWavefrontProxyPort(),
                waveFrontConnectionProperties.getSource(), waveFrontConnectionProperties.getStaticTags());
        PowerMockito.verifyNew(Wavefront.class, times(1)).withArguments(anyString(), anyInt());
        PowerMockito.mockStatic(MetricsHelper.class);
        PowerMockito.when(MetricsHelper.getDoubleEquivalent(any(Object.class))).thenReturn(75.5);
        PowerMockito.when(MetricsHelper.isAValidMetricName(anyString())).thenReturn(true);
        PowerMockito.when(MetricsHelper.isAValidMetricValue(any(Object.class))).thenReturn(true);
        PowerMockito.doNothing().when(wavefront).send(anyString(), anyDouble(), anyLong(), anyString(), anyMap());
        PowerMockito.doNothing().when(wavefront).flush();
        List<Metric> metricList = new ArrayList<>();
        Metric metric = new Metric(MetricReporterTestConstants.CPU_USAGE, 75.5, (HashMap<String, String>) pointTags,
                waveFrontConnectionProperties.getSource());
        metricList.add(metric);
        boolean actualResult = wavefrontMetricReporter.sendMetrics(metricList);
        verify(wavefront, times(1)).send(anyString(), anyDouble(), anyLong(), anyString(), anyMap());
        verify(wavefront, times(1)).flush();
        Assert.assertTrue(actualResult);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.reporter.WavefrontMetricReporter#sendMetrics(java.util.List)}.
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test(description = "Test to verify failure, when user tries to send an empty list as a metric value to the wavefront")
    public void testSendMetrics1() throws Exception {
        PowerMockito.whenNew(Wavefront.class).withArguments(anyString(), anyInt()).thenReturn(wavefront);
        this.wavefrontMetricReporter = new WavefrontMetricReporter(getWavefrontProxyHost(), getWavefrontProxyPort(),
                waveFrontConnectionProperties.getSource(), waveFrontConnectionProperties.getStaticTags());
        PowerMockito.verifyNew(Wavefront.class, times(1)).withArguments(anyString(), anyInt());
        List<Metric> metricList = new ArrayList<>();
        Metric metric = new Metric(MetricReporterTestConstants.CPU_USAGE, 75.5, (HashMap<String, String>) pointTags,
                waveFrontConnectionProperties.getSource());
        boolean actualResult = wavefrontMetricReporter.sendMetrics(metricList);
        verify(wavefront, times(0)).send(anyString(), anyDouble(), anyLong(), anyString(), anyMap());
        verify(wavefront, times(0)).flush();
        Assert.assertFalse(actualResult);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.reporter.WavefrontMetricReporter#sendMetrics(java.util.List)}.
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test(description = "Test to verify the failure of transmission of metric to wavefront when the metric value is invalid")
    public void testSendMetrics2() throws Exception {
        PowerMockito.whenNew(Wavefront.class).withArguments(anyString(), anyInt()).thenReturn(wavefront);
        this.wavefrontMetricReporter = new WavefrontMetricReporter(getWavefrontProxyHost(), getWavefrontProxyPort(),
                waveFrontConnectionProperties.getSource(), waveFrontConnectionProperties.getStaticTags());
        PowerMockito.verifyNew(Wavefront.class, times(1)).withArguments(anyString(), anyInt());
        PowerMockito.mockStatic(MetricsHelper.class);
        PowerMockito.when(MetricsHelper.getDoubleEquivalent(any(Object.class))).thenReturn(75.5);
        PowerMockito.when(MetricsHelper.isAValidMetricName(anyString())).thenReturn(true);
        PowerMockito.when(MetricsHelper.isAValidMetricValue(any(Object.class))).thenReturn(false);
        PowerMockito.doNothing().when(wavefront).send(anyString(), anyDouble(), anyLong(), anyString(), anyMap());
        PowerMockito.doNothing().when(wavefront).flush();
        List<Metric> metricList = new ArrayList<>();
        Metric metric = new Metric(MetricReporterTestConstants.CPU_USAGE, 75.5, (HashMap<String, String>) pointTags,
                waveFrontConnectionProperties.getSource());
        metricList.add(metric);
        boolean actualResult = wavefrontMetricReporter.sendMetrics(metricList);
        Assert.assertFalse(actualResult);
    }


    /**
     * Test method for
     * {@link com.vmware.mangle.reporter.WavefrontMetricReporter#validateMetric(java.lang.String, java.lang.Object)}.
     *
     * @throws Exception
     */
    @Test(description = "Test to validate the correct metric value, which should either be double, integer, string or float and metric name which shouldn't contain any special characters and should be in the format of <string>.<string>")
    public void testValidateMetric() throws Exception {
        PowerMockito.whenNew(Wavefront.class).withArguments(anyString(), anyInt()).thenReturn(wavefront);
        this.wavefrontMetricReporter = new WavefrontMetricReporter(getWavefrontProxyHost(), getWavefrontProxyPort(),
                waveFrontConnectionProperties.getSource(), waveFrontConnectionProperties.getStaticTags());
        PowerMockito.verifyNew(Wavefront.class, times(1)).withArguments(anyString(), anyInt());
        PowerMockito.mockStatic(MetricsHelper.class);
        PowerMockito.when(MetricsHelper.isAValidMetricName(anyString())).thenReturn(true);
        PowerMockito.when(MetricsHelper.isAValidMetricValue(any(Object.class))).thenReturn(true);

        boolean actualResult = wavefrontMetricReporter.validateMetric(MetricReporterTestConstants.CPU_USAGE, 75.8);
        PowerMockito.verifyStatic(MetricsHelper.class, times(1));
        MetricsHelper.isAValidMetricName(anyString());
        PowerMockito.verifyStatic(MetricsHelper.class, times(1));
        MetricsHelper.isAValidMetricValue(anyString());
        Assert.assertTrue(actualResult);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.reporter.WavefrontMetricReporter#validateMetric(java.lang.String, java.lang.Object)}.
     *
     * @throws Exception
     */
    @Test(description = "Test to verify failure when the given metric name is in improper format. Metric name which shouldn't contain any special characters and should be in the format of <string>.<string>")
    public void testValidateMetric1() throws Exception {
        PowerMockito.whenNew(Wavefront.class).withArguments(anyString(), anyInt()).thenReturn(wavefront);
        this.wavefrontMetricReporter = new WavefrontMetricReporter(getWavefrontProxyHost(), getWavefrontProxyPort(),
                waveFrontConnectionProperties.getSource(), waveFrontConnectionProperties.getStaticTags());
        PowerMockito.verifyNew(Wavefront.class, times(1)).withArguments(anyString(), anyInt());
        PowerMockito.mockStatic(MetricsHelper.class);
        PowerMockito.when(MetricsHelper.isAValidMetricName(anyString())).thenReturn(false);

        boolean actualResult = wavefrontMetricReporter.validateMetric(MetricReporterTestConstants.CPU_USAGE, 75.8);
        PowerMockito.verifyStatic(MetricsHelper.class, times(1));
        MetricsHelper.isAValidMetricName(anyString());
        Assert.assertFalse(actualResult);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.reporter.WavefrontMetricReporter#validateMetric(java.lang.String, java.lang.Object)}.
     *
     * @throws Exception
     */
    @Test(description = "Test to verify failure when the given metric is not in the acceptable format, metric value should either be double, integer, string or float")
    public void testValidateMetric2() throws Exception {
        PowerMockito.whenNew(Wavefront.class).withArguments(anyString(), anyInt()).thenReturn(wavefront);
        this.wavefrontMetricReporter = new WavefrontMetricReporter(getWavefrontProxyHost(), getWavefrontProxyPort(),
                waveFrontConnectionProperties.getSource(), waveFrontConnectionProperties.getStaticTags());
        PowerMockito.verifyNew(Wavefront.class, times(1)).withArguments(anyString(), anyInt());
        PowerMockito.mockStatic(MetricsHelper.class);
        PowerMockito.when(MetricsHelper.isAValidMetricName(anyString())).thenReturn(true);
        PowerMockito.when(MetricsHelper.isAValidMetricValue(anyString())).thenReturn(false);

        boolean actualResult = wavefrontMetricReporter.validateMetric(MetricReporterTestConstants.CPU_USAGE, 75.8);
        PowerMockito.verifyStatic(MetricsHelper.class, times(1));
        MetricsHelper.isAValidMetricName(anyString());
        PowerMockito.verifyStatic(MetricsHelper.class, times(1));
        MetricsHelper.isAValidMetricValue(anyString());
        Assert.assertFalse(actualResult);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.reporter.WavefrontMetricReporter#getWavefrontProxy()}.
     *
     * @throws Exception
     */
    @Test(description = "verify the getWaveFrontProxy method")
    public void testGetWavefrontProxy() throws Exception {
        PowerMockito.whenNew(Wavefront.class).withArguments(anyString(), anyInt()).thenReturn(wavefront);
        this.wavefrontMetricReporter = new WavefrontMetricReporter(getWavefrontProxyHost(), getWavefrontProxyPort(),
                waveFrontConnectionProperties.getSource(), waveFrontConnectionProperties.getStaticTags());
        PowerMockito.verifyNew(Wavefront.class, times(1)).withArguments(anyString(), anyInt());
        String actualResult = wavefrontMetricReporter.getWavefrontProxy();
        Assert.assertEquals(actualResult, getWavefrontProxyHost());
    }

    /**
     * Test method for {@link com.vmware.mangle.reporter.WavefrontMetricReporter#getMetricSource()}.
     *
     * @throws Exception
     */
    @Test(description = "verify the getMetricSource method")
    public void testGetMetricSource() throws Exception {
        PowerMockito.whenNew(Wavefront.class).withArguments(anyString(), anyInt()).thenReturn(wavefront);
        this.wavefrontMetricReporter = new WavefrontMetricReporter(getWavefrontProxyHost(), getWavefrontProxyPort(),
                waveFrontConnectionProperties.getSource(), waveFrontConnectionProperties.getStaticTags());
        PowerMockito.verifyNew(Wavefront.class, times(1)).withArguments(anyString(), anyInt());
        String actualResult = wavefrontMetricReporter.getMetricSource();
        Assert.assertEquals(actualResult, waveFrontConnectionProperties.getSource());
    }
}
