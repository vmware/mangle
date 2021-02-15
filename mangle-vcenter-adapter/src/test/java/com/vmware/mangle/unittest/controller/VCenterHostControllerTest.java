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

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.controller.VCenterHostController;
import com.vmware.mangle.mockdata.VCenterSpecMockData;
import com.vmware.mangle.model.Host;
import com.vmware.mangle.model.VCenterSpec;
import com.vmware.mangle.model.resource.VCenterTaskTriggeredResponse;
import com.vmware.mangle.service.HostOperationsService;
import com.vmware.mangle.service.VCenterOperationsTaskStore;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author chetanc
 */
public class VCenterHostControllerTest {
    public static String CLUSTER_NAME = "cluster_name";
    public static String DATACENTER_NAME = "datacenter_name";
    public static String HOST_NAME = "host_name";
    public static String FOLDER_NAME = "folder_name";

    VCenterHostController vCenterHostController;

    @Mock
    private HostOperationsService hostOperationsService;

    private VCenterOperationsTaskStore vCenterOperationsTaskStore = new VCenterOperationsTaskStore();

    VCenterSpec vCenterSpec = VCenterSpecMockData.getVCenterSpec();

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        vCenterHostController = new VCenterHostController(hostOperationsService, vCenterOperationsTaskStore);
    }

    @Test
    public void testGetHost() throws MangleException {
        List<Host> hosts = VCenterSpecMockData.getHosts();
        when(hostOperationsService.getHosts(CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME, vCenterSpec)).thenReturn(hosts);

        ResponseEntity<List<Host>> responseEntity =
                vCenterHostController.getHost(vCenterSpec, DATACENTER_NAME, CLUSTER_NAME, FOLDER_NAME);

        Assert.assertEquals(responseEntity.getBody(), hosts);
        verify(hostOperationsService, times(1)).getHosts(CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME, vCenterSpec);
    }

    @Test
    public void testDisconnectHost() throws MangleException {
        doNothing().when(hostOperationsService).disconnectHost(anyString(), eq(HOST_NAME), eq(CLUSTER_NAME),
                eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(vCenterSpec));
        ResponseEntity<VCenterTaskTriggeredResponse> responseEntity = vCenterHostController.disconnectHost(vCenterSpec,
                HOST_NAME, DATACENTER_NAME, CLUSTER_NAME, FOLDER_NAME);

        verify(hostOperationsService, times(1)).disconnectHost(anyString(), eq(HOST_NAME), eq(CLUSTER_NAME),
                eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(vCenterSpec));
    }

    @Test
    public void testConnectHost() throws MangleException {
        doNothing().when(hostOperationsService).connectHost(anyString(), eq(HOST_NAME), eq(CLUSTER_NAME),
                eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(vCenterSpec));
        ResponseEntity<VCenterTaskTriggeredResponse> responseEntity =
                vCenterHostController.connectHost(vCenterSpec, HOST_NAME, DATACENTER_NAME, CLUSTER_NAME, FOLDER_NAME);

        verify(hostOperationsService, times(1)).connectHost(anyString(), eq(HOST_NAME), eq(CLUSTER_NAME),
                eq(DATACENTER_NAME), eq(FOLDER_NAME), eq(vCenterSpec));
    }

    @Test
    public void testDisconnectHostById() throws MangleException {
        doNothing().when(hostOperationsService).disconnectHostById(anyString(), eq(HOST_NAME), eq(vCenterSpec));
        ResponseEntity<VCenterTaskTriggeredResponse> responseEntity =
                vCenterHostController.disconnectHostById(vCenterSpec, HOST_NAME);

        verify(hostOperationsService, times(1)).disconnectHostById(anyString(), eq(HOST_NAME), eq(vCenterSpec));
    }

    @Test
    public void testConnectHostById() throws MangleException {
        doNothing().when(hostOperationsService).connectHostById(anyString(), eq(HOST_NAME), eq(vCenterSpec));

        ResponseEntity<VCenterTaskTriggeredResponse> responseEntity =
                vCenterHostController.connectHostById(vCenterSpec, HOST_NAME);

        verify(hostOperationsService, times(1)).connectHostById(anyString(), eq(HOST_NAME), eq(vCenterSpec));
    }
}
