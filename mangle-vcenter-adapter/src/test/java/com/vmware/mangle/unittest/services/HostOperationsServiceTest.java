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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.adapter.HostOperations;
import com.vmware.mangle.adapter.VCenterClient;
import com.vmware.mangle.inventory.helpers.HostInventoryHelper;
import com.vmware.mangle.mockdata.VCenterSpecMockData;
import com.vmware.mangle.model.Host;
import com.vmware.mangle.model.VCenterSpec;
import com.vmware.mangle.service.HostOperationsService;
import com.vmware.mangle.service.VCenterClientInstantiationService;
import com.vmware.mangle.service.VCenterOperationsTaskStore;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.status.VCenterOperationsTaskStatus;

/**
 * @author chetanc
 */

public class HostOperationsServiceTest {
    public static String CLUSTER_NAME = "cluster_name";
    public static String DATACENTER_NAME = "datacenter_name";
    public static String HOST_NAME = "host_name";
    public static String FOLDER_NAME = "folder_name";
    public static String HOST_ID = "host_id";

    public static String taskId;

    @Mock
    private VCenterClientInstantiationService vCenterClientInstantiationService;
    @Mock
    private HostInventoryHelper hostInventoryHelper;
    @Mock
    private VCenterClient vCenterClient;
    @Mock
    private HostOperations hostOperations;
    private VCenterOperationsTaskStore vCenterOperationsTaskStore = new VCenterOperationsTaskStore();

    HostOperationsService hostOperationsService;
    VCenterSpec vCenterSpec = VCenterSpecMockData.getVCenterSpec();

    @BeforeMethod
    public void initMocks() throws MangleException {
        MockitoAnnotations.initMocks(this);
        hostOperationsService = new HostOperationsService(hostInventoryHelper, vCenterOperationsTaskStore,
                vCenterClientInstantiationService, hostOperations);
        when(vCenterClientInstantiationService.getVCenterClient(vCenterSpec)).thenReturn(vCenterClient);
        taskId = UUID.randomUUID().toString();
    }

    @Test
    public void testGetHosts() throws MangleException {
        List<Host> hosts = VCenterSpecMockData.getHosts();
        when(hostInventoryHelper.getAllHost(vCenterClient, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME))
                .thenReturn(hosts);
        List<Host> responseHosts =
                hostOperationsService.getHosts(CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME, vCenterSpec);
        Assert.assertEquals(responseHosts, hosts);
        verify(hostInventoryHelper, times(1)).getAllHost(vCenterClient, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME);
    }

    @Test
    public void testDisconnectHost() throws MangleException {
        when(hostInventoryHelper.getHostId(vCenterClient, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME))
                .thenReturn(HOST_ID);
        when(hostOperations.disconnectHost(vCenterClient, HOST_ID)).thenReturn(true);
        when(hostOperations.isHostConnected(vCenterClient, HOST_ID)).thenReturn(false);

        hostOperationsService.disconnectHost(taskId, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME,
                vCenterSpec);

        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.COMPLETED.toString());
        verify(hostInventoryHelper, times(1)).getHostId(vCenterClient, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME,
                FOLDER_NAME);
        verify(hostOperations, times(1)).disconnectHost(vCenterClient, HOST_ID);
        verify(hostOperations, times(1)).isHostConnected(vCenterClient, HOST_ID);

    }

    @Test
    public void testDisconnectHostFailure() throws MangleException {
        when(hostInventoryHelper.getHostId(vCenterClient, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME))
                .thenReturn(HOST_ID);
        when(hostOperations.disconnectHost(vCenterClient, HOST_ID)).thenThrow(new MangleException());

        hostOperationsService.disconnectHost(taskId, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME,
                vCenterSpec);

        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.FAILED.toString());
        verify(hostInventoryHelper, times(1)).getHostId(vCenterClient, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME,
                FOLDER_NAME);
        verify(hostOperations, times(1)).disconnectHost(vCenterClient, HOST_ID);

    }

    @Test
    public void testConnectHost() throws MangleException {
        when(hostInventoryHelper.getHostId(vCenterClient, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME))
                .thenReturn(HOST_ID);
        when(hostOperations.connectHost(vCenterClient, HOST_ID)).thenReturn(true);
        when(hostOperations.isHostConnected(vCenterClient, HOST_ID)).thenReturn(true);

        hostOperationsService.connectHost(taskId, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME, vCenterSpec);

        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.COMPLETED.toString());
        verify(hostInventoryHelper, times(1)).getHostId(vCenterClient, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME,
                FOLDER_NAME);
        verify(hostOperations, times(1)).connectHost(vCenterClient, HOST_ID);
        verify(hostOperations, times(1)).isHostConnected(vCenterClient, HOST_ID);
    }

    @Test
    public void testConnectHostFailure() throws MangleException {
        when(hostInventoryHelper.getHostId(vCenterClient, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME))
                .thenReturn(HOST_ID);
        when(hostOperations.connectHost(vCenterClient, HOST_ID)).thenThrow(new MangleException());

        hostOperationsService.connectHost(taskId, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME, FOLDER_NAME, vCenterSpec);

        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.FAILED.toString());
        verify(hostInventoryHelper, times(1)).getHostId(vCenterClient, HOST_NAME, CLUSTER_NAME, DATACENTER_NAME,
                FOLDER_NAME);
        verify(hostOperations, times(1)).connectHost(vCenterClient, HOST_ID);
    }

    @Test
    public void testDisconnectHostById() throws MangleException {
        when(hostOperations.disconnectHost(vCenterClient, HOST_ID)).thenReturn(true);
        when(hostOperations.isHostConnected(vCenterClient, HOST_ID)).thenReturn(false);

        hostOperationsService.disconnectHostById(taskId, HOST_ID, vCenterSpec);

        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.COMPLETED.toString());
        verify(hostOperations, times(1)).disconnectHost(vCenterClient, HOST_ID);
        verify(hostOperations, times(1)).isHostConnected(vCenterClient, HOST_ID);
    }

    @Test
    public void testConnectHostById() throws MangleException {
        when(hostOperations.connectHost(vCenterClient, HOST_ID)).thenReturn(true);
        when(hostOperations.isHostConnected(vCenterClient, HOST_ID)).thenReturn(true);

        hostOperationsService.connectHostById(taskId, HOST_ID, vCenterSpec);

        Assert.assertEquals(vCenterOperationsTaskStore.getTaskStatus(taskId),
                VCenterOperationsTaskStatus.COMPLETED.toString());
        verify(hostOperations, times(1)).connectHost(vCenterClient, HOST_ID);
        verify(hostOperations, times(1)).isHostConnected(vCenterClient, HOST_ID);

    }
}
