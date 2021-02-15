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
import java.util.Arrays;
import java.util.List;

import com.vmware.mangle.model.Host;
import com.vmware.mangle.model.VCenterSpec;
import com.vmware.mangle.model.VM;

/**
 * @author Chethan C(chetanc)
 */
public class VCenterSpecMockData {

    private static String vm1 = "vm-1";
    private static String vmName1 = "vm-1";
    private static String vmPowerState1 = "POWER_ON";

    private static String vm2 = "vm-2";
    private static String vmName2 = "vm-2";
    private static String vmPowerState2 = "POWER_ON";

    private static String host1 = "host1";
    private static String hostName1 = "host_name1";
    private static String connectionState1 = "CONNECTED1";
    private static String powerState1 = "power_state1";

    private static String host2 = "host2";
    private static String hostName2 = "host_name2";
    private static String connectionState2 = "CONNECTED2";
    private static String powerState2 = "power_state2";

    private VCenterSpecMockData() {
    }

    public static VCenterSpec getVCenterSpec() {
        return new VCenterSpec();
    }

    public static List<VM> getVMs() {
        VM vm_1 = new VM(0, vm1, vmName1, vmPowerState1, 0);
        VM vm_2 = new VM(0, vm1, vmName1, vmPowerState1, 0);
        return new ArrayList<>(Arrays.asList(vm_1, vm_2));
    }

    public static List<Host> getHosts() {
        Host hostObj1 = new Host(host1, hostName1, connectionState1, powerState1);
        Host hostObj2 = new Host(host2, hostName2, connectionState2, powerState2);
        return new ArrayList<>(Arrays.asList(hostObj1, hostObj2));
    }


}
