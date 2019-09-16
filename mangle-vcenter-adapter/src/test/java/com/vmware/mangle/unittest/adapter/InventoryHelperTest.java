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
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.adapter.InventoryHelper;
import com.vmware.mangle.adapter.VCenterClient;
import com.vmware.mangle.mockdata.InventoryHelperMockData;
import com.vmware.mangle.model.ResourceList;

/**
 * @author chetanc
 */

@PrepareForTest(VCenterClient.class)
public class InventoryHelperTest {
    public static String VM_NAME = "centos";
    public static String VM_ID = "vm-15";
    private ResourceList vmMockData = InventoryHelperMockData.getVCenterVMMockData();
    private ResponseEntity response = new ResponseEntity(vmMockData, HttpStatus.OK);
    @Mock
    private VCenterClient VCenterClient;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetVMId() throws Exception {
        when(VCenterClient.get(anyString(), eq(ResourceList.class))).thenReturn(response);
        String vm_id = InventoryHelper.getVMID(VCenterClient, VM_NAME, null);
        Assert.assertEquals(vm_id, VM_ID);
    }

}
