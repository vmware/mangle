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

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.faults.plugin.helpers.k8s.K8sFaultHelper;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.faults.plugin.tasks.helpers.K8sSpecificFaultTaskHelper;

/**
 *
 *
 * @author hkilari
 */
public class K8sSpecificFaultTaskHelperTest {
    FaultsMockData faultsMockData = new FaultsMockData();

    @Mock
    private K8sFaultHelper k8sFaultHelper;
    @InjectMocks
    private K8sSpecificFaultTaskHelper<K8SFaultSpec> injectionTask;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInitOfInjection() {
        K8sSpecificFaultTaskHelper<K8SFaultSpec> injectionTask = new K8sSpecificFaultTaskHelper<>();

        Task<K8SFaultSpec> task = injectionTask.init(faultsMockData.getDeleteK8SResourceFaultSpec(), null);
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        Assert.assertEquals(task.getTaskDescription(), "Executing Fault: " + task.getTaskData().getFaultName()
                + " on K8Sendpoint: " + task.getTaskData().getEndpointName());

        task.getTriggers().add(new TaskTrigger());
    }
}
