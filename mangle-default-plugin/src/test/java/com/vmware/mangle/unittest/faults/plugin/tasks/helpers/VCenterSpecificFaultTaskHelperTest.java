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

package com.vmware.mangle.unittest.faults.plugin.tasks.helpers;

import org.junit.Assert;
import org.mockito.Mock;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.VMStateFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.faults.plugin.helpers.vcenter.VCenterFaultHelper;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.faults.plugin.tasks.helpers.VCenterSpecificFaultTaskHelper;

/**
 *
 *
 * @author chetanc
 */
public class VCenterSpecificFaultTaskHelperTest {
    FaultsMockData faultsMockData = new FaultsMockData();

    @Mock
    VCenterFaultHelper vCenterFaultHelper;

    @Test
    public void testInitOfInjection() {
        VCenterSpecificFaultTaskHelper<VMStateFaultSpec> injectionTask = new VCenterSpecificFaultTaskHelper<>();
        injectionTask.setvCenterFaultHelper(vCenterFaultHelper);
        Task<VMStateFaultSpec> task = injectionTask.init(faultsMockData.getVMStateFaultSpec());
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(TaskType.INJECTION, task.getTaskType());
    }
}
