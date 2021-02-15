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

package com.vmware.mangle.resiliency.score.email;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Properties;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.client.restclient.WavefrontMetricProviderHelper;
import com.vmware.mangle.common.DtoMockData;
import com.vmware.mangle.exception.MangleException;
import com.vmware.mangle.resiliency.score.utils.MailUtils;
import com.vmware.mangle.resiliency.score.utils.ReadProperty;

/**
 * Unit tests for class ResiliencyScoreEmail
 * 
 * @author ranjans
 */
@PowerMockIgnore("javax.management.*")
@PrepareForTest({ ResiliencyScoreEmail.class, MailUtils.class })
public class ResiliencyScoreEmailTest extends PowerMockTestCase {

    @Mock
    private WavefrontMetricProviderHelper waveFrontServerClient;
    private ResiliencyScoreEmail resiliencyScoreEmail;
    private Properties properties;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @BeforeMethod
    public void initTests() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(MailUtils.class);
        this.properties = ReadProperty.readProperty("example.properties");
        resiliencyScoreEmail = new ResiliencyScoreEmail(waveFrontServerClient,
                Integer.parseInt(properties.getProperty("score.email.days")),
                properties.getProperty("service.email.template"), properties.getProperty("resiliency.score.images"),
                properties.getProperty("resiliency.score.metrics"), properties.getProperty("berserker.api.metrics"));
        ResponseEntity responseEntity = new ResponseEntity<>(DtoMockData.getQueryResultMockData(), HttpStatus.OK);
        Mockito.when(waveFrontServerClient.get(anyString(), any(Class.class))).thenReturn(responseEntity);
        Mockito.when(MailUtils.mail(any(String[].class), anyString(), anyString(), any(String[].class), anyMap()))
                .thenReturn(true);
    }

    @Test
    public void sendResiliencyScoreEmailTest() throws MangleException {
        resiliencyScoreEmail.sendResiliencyScoreEmail(properties.getProperty("service.families").trim().split(","),
                properties.getProperty("score.email.to").trim());
        verify(waveFrontServerClient, times(145)).get(anyString(), any(Class.class));
        PowerMockito.verifyStatic(MailUtils.class, times(1));
    }

}
