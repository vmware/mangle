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

package com.vmware.mangle.unittest.services;

import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.adapter.VCenterClient;
import com.vmware.mangle.mockdata.VCenterSpecMockData;
import com.vmware.mangle.model.VCenterSpec;
import com.vmware.mangle.service.VCenterClientInstantiationService;

/**
 * @author Chethan C(chetanc)
 */
@PrepareForTest({ VCenterClientInstantiationService.class })
public class VCenterClientInstantiationServiceTest extends PowerMockTestCase {

    @InjectMocks
    private VCenterClientInstantiationService VCenterClientInstantiationService;

    private VCenterClient FVCSpy;

    @BeforeMethod(alwaysRun = true)
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeClass
    public void initSpy() {
        FVCSpy = PowerMockito.spy(new VCenterClient());
    }

    @Test
    public void testGetVCenterClient() throws Exception {
        VCenterSpec vCenterSpec = VCenterSpecMockData.getVCenterSpec();
        PowerMockito.whenNew(VCenterClient.class).withAnyArguments().thenReturn(FVCSpy);
        PowerMockito.doReturn(true).when(FVCSpy).testConnection();
        VCenterClientInstantiationService.createNewVCenterBean(vCenterSpec);
        Assert.assertEquals(VCenterClientInstantiationService.getVCenterClient(vCenterSpec), FVCSpy);
    }

    @Test
    public void testGetVCenterClientSessionExpire() throws Exception {
        VCenterSpec vCenterSpec = VCenterSpecMockData.getVCenterSpec();
        PowerMockito.whenNew(VCenterClient.class).withAnyArguments().thenReturn(FVCSpy);
        PowerMockito.doThrow(new NullPointerException("")).when(FVCSpy).testConnection();
        Assert.assertEquals(VCenterClientInstantiationService.getVCenterClient(vCenterSpec), FVCSpy);
    }
}
