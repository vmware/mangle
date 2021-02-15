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

package com.vmware.mangle.unittest.controller;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.controller.VCenterController;
import com.vmware.mangle.mockdata.VCenterSpecMockData;
import com.vmware.mangle.model.VCenterSpec;
import com.vmware.mangle.model.resource.VCenterAdapterGeneralReponse;
import com.vmware.mangle.service.VCenterOperationService;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author chetanc
 */
public class VCenterControllerTest {

    private VCenterController vCenterController;

    VCenterSpec vCenterSpec = VCenterSpecMockData.getVCenterSpec();
    @Mock
    private com.vmware.mangle.adapter.VCenterClient VCenterClient;
    @Mock
    private VCenterOperationService vmOperationsTaskService;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        vCenterController = new VCenterController(vmOperationsTaskService);
    }

    @Test
    public void testSuccessfulTestConnection() throws MangleException {
        when(vmOperationsTaskService.testConnection(vCenterSpec)).thenReturn(true);
        ResponseEntity response = vCenterController.testConnection(vCenterSpec);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(vmOperationsTaskService, times(1)).testConnection(vCenterSpec);
    }

    @Test
    public void testFailureTestConnection() throws MangleException {
        when(VCenterClient.testConnection()).thenReturn(false);
        ResponseEntity response = vCenterController.testConnection(vCenterSpec);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        Assert.assertNotNull(response.getBody());
        Assert.assertEquals(((VCenterAdapterGeneralReponse) response.getBody()).getMessage(),
                "VCenter " + "Authentication Failed");


        verify(vmOperationsTaskService, times(1)).testConnection(vCenterSpec);
    }
}
