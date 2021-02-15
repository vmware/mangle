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

package com.vmware.mangle.resiliency.services;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.client.restclient.WavefrontMetricProviderHelper;
import com.vmware.mangle.common.DtoMockData;
import com.vmware.mangle.common.MockDataConstants;
import com.vmware.mangle.metric.wavefront.reporter.WavefrontMetricReporter;

/**
 * Unit tests for class ResiliencyCalculator
 * 
 * @author ranjans
 */
@PowerMockIgnore("javax.management.*")
@PrepareForTest(ResiliencyCalculator.class)
public class ResiliencyCalculatorTest extends PowerMockTestCase {

    @Mock
    private WavefrontMetricProviderHelper wavefrontMetricProviderHelper;
    @Mock
    private WavefrontMetricReporter wavefrontMetricReporter;

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void initTests() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.whenNew(WavefrontMetricProviderHelper.class).withArguments(anyString(), anyString())
                .thenReturn(wavefrontMetricProviderHelper);
        PowerMockito.whenNew(WavefrontMetricReporter.class)
                .withArguments(anyString(), anyInt(), anyString(), anyMapOf(String.class, String.class))
                .thenReturn(wavefrontMetricReporter);
        Mockito.when(wavefrontMetricProviderHelper.queryEvents(anyMapOf(String.class, String.class), anyString(),
                anyString(), anyString(), anyString())).thenReturn(DtoMockData.getWavefrontEventResponse());
        Mockito.when(wavefrontMetricReporter.sendMetrics(any(ArrayList.class))).thenReturn(true);
        Mockito.when(wavefrontMetricReporter.sendMetric(anyString(), anyDouble(), any(HashMap.class))).thenReturn(true);
    }

    @AfterMethod
    public void tearDown() {
        validateMockitoUsage();
    }

    @Test
    public void runResiliencyCalculatorTest() throws Exception {
        ResiliencyCalculator resiliencyCalculator =
                new ResiliencyCalculator(DtoMockData.getService(), DtoMockData.getWavefrontConnectionProperties(),
                        MockDataConstants.ONE, MockDataConstants.ONE, new ArrayList<>());
        Thread thread = new Thread(resiliencyCalculator, MockDataConstants.ANY_STR);
        thread.start();
        PowerMockito.verifyNew(WavefrontMetricProviderHelper.class, times(1)).withArguments(anyString(), anyString());
        PowerMockito.verifyNew(WavefrontMetricReporter.class, times(1)).withArguments(anyString(), anyInt(),
                anyString(), anyMapOf(String.class, String.class));
        verify(wavefrontMetricProviderHelper, times(1)).queryEvents(anyMapOf(String.class, String.class), anyString(),
                anyString(), anyString(), anyString());
    }

}