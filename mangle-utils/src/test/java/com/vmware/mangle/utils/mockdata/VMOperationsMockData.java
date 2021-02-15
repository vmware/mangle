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

package com.vmware.mangle.utils.mockdata;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chetanc
 */
public class VMOperationsMockData {
    public LinkedHashMap<String, String> getErrorMockDetails() {
        LinkedHashMap<String, String> errorDetails = new LinkedHashMap<>();
        errorDetails.put("timestamp", "");
        errorDetails.put("status", "");
        errorDetails.put("error", "");
        errorDetails.put("message", "");
        errorDetails.put("path", "");
        return errorDetails;
    }

    public List<LinkedHashMap<String, String>> getVMList() {
        LinkedHashMap<String, String> vm1 = new LinkedHashMap<>();
        LinkedHashMap<String, String> vm2 = new LinkedHashMap<>();
        vm1.put("name", "");
        vm1.put("vm","");
        vm2.put("name", "");
        vm2.put("vm","");
        List<LinkedHashMap<String, String>> vms = new ArrayList<>(Arrays.asList(vm1, vm2));
        return vms;
    }

    public List<LinkedHashMap<String, String>> getVMNicList() {
        LinkedHashMap<String, String> vmNic1 = new LinkedHashMap<>();
        LinkedHashMap<String, String> vmNic2 = new LinkedHashMap<>();
        vmNic1.put("nic", "");
        vmNic2.put("nic", "");
        return new ArrayList<>(Arrays.asList(vmNic1, vmNic2));
    }

    public List<LinkedHashMap<String, String>> getVMDiskList() {
        LinkedHashMap<String, String> vmDisk1 = new LinkedHashMap<>();
        LinkedHashMap<String, String> vmDisk2 = new LinkedHashMap<>();
        vmDisk1.put("disk", "");
        vmDisk2.put("disk", "");
        return new ArrayList<>(Arrays.asList(vmDisk1, vmDisk2));
    }

    public Map<String, String> getFilters() {
        Map<String, String> filters = new HashMap<>();
        filters.put("dcName", "Dummy_DC");
        filters.put("clusterName", "Dummy_cluster");
        return filters;
    }
}
