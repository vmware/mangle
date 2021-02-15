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
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.client.restclient.WavefrontMetricProviderHelper;
import com.vmware.mangle.resiliency.commons.ResiliencyConstants;
import com.vmware.mangle.resiliency.score.email.ResiliencyScoreEmail;

/**
 * Unit tests for class ResiliencyEmailHelper
 * 
 * @author ranjans
 */
@PowerMockIgnore("javax.management.*")
@PrepareForTest(ResiliencyEmailHelper.class)
public class ResiliencyEmailHelperTest extends PowerMockTestCase {

    @Mock
    private ResiliencyScoreEmail resiliencyScoreEmail;
    @Mock
    private WavefrontMetricProviderHelper wavefrontMetricProviderHelper;

    @BeforeMethod
    public void initTests() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @AfterMethod
    public void tearDown() {
        validateMockitoUsage();
    }

    @Test
    public void sendResiliencyScoreEmailTest() throws Exception {
        PowerMockito.whenNew(ResiliencyScoreEmail.class).withArguments(any(WavefrontMetricProviderHelper.class),
                anyInt(), anyString(), anyString(), anyString(), anyString()).thenReturn(resiliencyScoreEmail);
        PowerMockito.whenNew(WavefrontMetricProviderHelper.class).withArguments(anyString(), anyString())
                .thenReturn(wavefrontMetricProviderHelper);
        PowerMockito.doNothing().when(resiliencyScoreEmail).sendResiliencyScoreEmail(any(String[].class), anyString());
        System.setProperty(ResiliencyConstants.PROPERTY_FILE,
                ResiliencyEmailHelperTest.class.getClassLoader().getResource("example.properties").getPath());
        new ResiliencyEmailHelper().sendResiliencyScoreEmail();
        PowerMockito.verifyNew(ResiliencyScoreEmail.class, times(1)).withArguments(
                any(WavefrontMetricProviderHelper.class), anyInt(), anyString(), anyString(), anyString(), anyString());
        PowerMockito.verifyNew(WavefrontMetricProviderHelper.class, times(1)).withArguments(anyString(), anyString());
        verify(resiliencyScoreEmail, times(1)).sendResiliencyScoreEmail(any(String[].class), anyString());
    }

}
