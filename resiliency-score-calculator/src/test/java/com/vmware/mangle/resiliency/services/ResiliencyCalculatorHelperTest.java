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
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;

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
import com.vmware.mangle.metrics.models.Service;
import com.vmware.mangle.metrics.models.WavefrontConnectionProperties;
import com.vmware.mangle.metrics.models.WavefrontEvent;
import com.vmware.mangle.resiliency.commons.ResiliencyConstants;

/**
 * Unit tests for class ResiliencyCalculatorHelper
 * 
 * @author ranjans
 */
@PowerMockIgnore("javax.management.*")
@PrepareForTest(ResiliencyCalculatorHelper.class)
public class ResiliencyCalculatorHelperTest extends PowerMockTestCase {

    @Mock
    private WavefrontMetricProviderHelper wavefrontMetricProviderHelper;
    @Mock
    private ResiliencyCalculator resiliencyCalculator;
    @Mock
    private Thread thread;
    private ResiliencyCalculatorHelper resiliencyCalculatorHelper;

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void initTests() throws Exception {
        MockitoAnnotations.initMocks(this);
        System.setProperty(ResiliencyConstants.PROPERTY_FILE, ResiliencyCalculatorHelperTest.class.getClassLoader()
                .getResource("wavefront-properties-test.yaml").getPath());
        PowerMockito.whenNew(WavefrontMetricProviderHelper.class).withArguments(anyString(), anyString())
                .thenReturn(wavefrontMetricProviderHelper);
        PowerMockito.whenNew(ResiliencyCalculator.class).withArguments(any(Service.class),
                any(WavefrontConnectionProperties.class), anyString(), anyString(), anyListOf(WavefrontEvent.class))
                .thenReturn(resiliencyCalculator);
        PowerMockito.whenNew(Thread.class).withParameterTypes(Runnable.class, String.class)
                .withArguments(any(ResiliencyCalculator.class), anyString()).thenReturn(thread);
        PowerMockito.doNothing().when(thread).start();
        Mockito.when(
                wavefrontMetricProviderHelper.queryEvents(anyMap(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(DtoMockData.getWavefrontEventResponse());
        resiliencyCalculatorHelper = new ResiliencyCalculatorHelper();
    }

    @AfterMethod
    public void tearDown() {
        validateMockitoUsage();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void calculateResiliencyScoreTest() throws Exception {
        resiliencyCalculatorHelper.calculateResiliencyScore();
        PowerMockito.verifyNew(ResiliencyCalculator.class, times(10)).withArguments(any(Service.class),
                any(WavefrontConnectionProperties.class), anyString(), anyString(), anyListOf(WavefrontEvent.class));
        verify(thread, times(10)).start();
        verify(wavefrontMetricProviderHelper, times(4)).queryEvents(anyMap(), anyString(), anyString(),
                anyString(),
                anyString());
    }

}
