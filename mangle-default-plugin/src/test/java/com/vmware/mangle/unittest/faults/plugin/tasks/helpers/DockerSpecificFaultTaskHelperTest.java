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

import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.mockito.Mock;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.DockerFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.faults.plugin.helpers.docker.DockerFaultHelper;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.faults.plugin.tasks.helpers.DockerSpecificFaultTaskHelper;


/**
 *
 *
 * @author rpraveen
 */
@Log4j2
public class DockerSpecificFaultTaskHelperTest {
    FaultsMockData faultsMockData = new FaultsMockData();

    /**
     * @throws java.lang.Exception
     */


    @Mock
    DockerFaultHelper dockerFaultHelper;

    @Test
    public void testInitOfInjection() {
        DockerSpecificFaultTaskHelper<DockerFaultSpec> injectionTask = new DockerSpecificFaultTaskHelper<>();
        Task<DockerFaultSpec> task = injectionTask.init(faultsMockData.getDockerPauseFaultSpec());
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(TaskType.INJECTION, task.getTaskType());
        task.getTriggers().add(new TaskTrigger());
        List<CommandInfo> injectionCommands = injectionTask.getInjectionExecutionInfo(task);
        List<CommandInfo> remediationCommands = injectionTask.getRemediationExecutionInfo(task);
        log.info(injectionCommands);
        log.info(remediationCommands);
    }
}
