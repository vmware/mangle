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

package com.vmware.mangle.unittest.adapter;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.adapter.HostOperations;
import com.vmware.mangle.adapter.VCenterClient;
import com.vmware.mangle.model.resource.VMOperationsRepsonse;

/**
 * @author chetanc
 */
public class HostOperationsTest {
    private String HOST_ID = "host_id";

    @Mock
    private VCenterClient vCenterClient;
    private HostOperations hostOperations;


    private ResponseEntity responseSuccess = new ResponseEntity(HttpStatus.OK);

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        hostOperations = new HostOperations();

    }

    @Test
    public void testDisconnectHost() throws Exception {
        when(vCenterClient.post(anyString(), anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseSuccess);
        Assert.assertTrue(hostOperations.disconnectHost(vCenterClient, HOST_ID));
    }

    @Test
    public void testConnectHost() throws Exception {
        when(vCenterClient.post(anyString(), anyString(), eq(VMOperationsRepsonse.class))).thenReturn(responseSuccess);
        Assert.assertTrue(hostOperations.connectHost(vCenterClient, HOST_ID));
    }


}
