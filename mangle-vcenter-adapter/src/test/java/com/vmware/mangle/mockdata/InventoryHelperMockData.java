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

package com.vmware.mangle.mockdata;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.http.ResponseEntity;

import com.vmware.mangle.model.ResourceList;
import com.vmware.mangle.model.ResourceObject;
import com.vmware.mangle.model.VCenterVMNic;
import com.vmware.mangle.model.VCenterVMState;
import com.vmware.mangle.model.VMStates;
import com.vmware.mangle.utils.status.VCenterVmNicStatus;

/**
 * @author chetanc
 */

public class InventoryHelperMockData {

    public static ResourceList getVCenterVMMockData() {
        LinkedHashMap<String, String> map = new LinkedHashMap();
        map.put("memory_size_MiB", "16384");
        map.put("vm", "vm-15");
        map.put("name", "centos");
        map.put("power_state", "POWERED_ON");
        map.put("cpu_count", "4");

        List<LinkedHashMap<String, String>> list = new ArrayList<>();

        ResourceList resourceList = new ResourceList<>();
        list.add(map);
        resourceList.setValue(list);
        return resourceList;
    }

    public ResourceObject getDummyResourceObject() {
        return new ResourceObject<>();
    }

    public ResponseEntity getSuccessResponseEntityWithDummyResponseObject() {
        return ResponseEntity.accepted().body(new ResourceObject());
    }

    public ResponseEntity getFailureResponseEntityWithDummyResponseObject() {
        return ResponseEntity.badRequest().body(new ResourceObject());
    }

    public VCenterVMState getSuccessVCenterVMGuestOSState() {
        VCenterVMState vCenterVMState = new VCenterVMState();
        vCenterVMState.setState(VMStates.POWERED_ON.name());
        return vCenterVMState;
    }

    public VCenterVMState getFailureVCenterVMGuestOSState() {
        VCenterVMState vCenterVMState = new VCenterVMState();
        vCenterVMState.setState(VMStates.POWERED_OFF.name());
        return vCenterVMState;
    }

    public VCenterVMNic getSuccessVCenterVMNicState() {
        VCenterVMNic vCenterVMNic = new VCenterVMNic();
        vCenterVMNic.setState(VCenterVmNicStatus.STATE_CONNECTED);
        return vCenterVMNic;
    }

    public VCenterVMNic getFailedVCenterVMNicState() {
        VCenterVMNic vCenterVMNic = new VCenterVMNic();
        vCenterVMNic.setState(VCenterVmNicStatus.STATE_DISCONNECTED);
        return vCenterVMNic;
    }
}
