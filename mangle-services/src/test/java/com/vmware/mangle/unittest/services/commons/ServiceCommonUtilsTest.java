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

package com.vmware.mangle.unittest.services.commons;

import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.services.MetricProviderService;
import com.vmware.mangle.services.commons.ServiceCommonUtils;

/**
 * @author chetanc
 *
 *
 */
public class ServiceCommonUtilsTest {

    @Mock
    private ApplicationContext applicationContext;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        ServiceCommonUtils.setApplicationContext(applicationContext);
    }

    @Test
    public void testGetApplicationContext() {
        ApplicationContext retrieveContext = ServiceCommonUtils.getApplicationContext();
        Assert.assertEquals(retrieveContext, applicationContext);
    }

    @Test
    public void testGetBean() {
        MetricProviderService providerService = new MetricProviderService();
        when(applicationContext.getBean(MetricProviderService.class)).thenReturn(providerService);
        Object retrieveProviderService = ServiceCommonUtils.getBean(MetricProviderService.class);
        Assert.assertTrue(retrieveProviderService instanceof MetricProviderService);
        Assert.assertEquals(retrieveProviderService, providerService);
    }

}
