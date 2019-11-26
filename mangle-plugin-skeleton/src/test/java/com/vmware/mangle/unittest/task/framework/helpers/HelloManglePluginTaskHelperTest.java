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

package com.vmware.mangle.unittest.task.framework.helpers;


import java.util.HashMap;
import java.util.Map;

import lombok.extern.log4j.Log4j2;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.endpoint.RemoteMachineConnectionProperties;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.model.enums.OSType;
import com.vmware.mangle.plugin.model.faults.specs.HelloMangleFaultSpec;
import com.vmware.mangle.plugin.tasks.impl.HelloManglePluginTaskHelper;
import com.vmware.mangle.plugin.utils.CustomPluginUtils;
import com.vmware.mangle.task.framework.helpers.AbstractCommandExecutionTaskHelper;
import com.vmware.mangle.task.framework.helpers.CommandInfoExecutionHelper;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author hkilari
 * @author bkaranam (bhanukiran karanam)
 */
@Log4j2
public class HelloManglePluginTaskHelperTest {
    @InjectMocks
    CommandInfoExecutionHelper commandInfoExecutionHelper;
    @InjectMocks
    CustomPluginUtils pluginUtils;
    @Mock
    private ApplicationEventPublisher publisher;

    HelloMangleFaultSpec helloMangleFaultSpec;

    @BeforeTest
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        helloMangleFaultSpec = new HelloMangleFaultSpec();
        helloMangleFaultSpec.setField1("field1");
        helloMangleFaultSpec.setField2("field2");
        EndpointSpec endpoint = new EndpointSpec();
        endpoint.setEndPointType(EndpointType.MACHINE);
        RemoteMachineConnectionProperties remoteMachineConnectionProperties = new RemoteMachineConnectionProperties();
        remoteMachineConnectionProperties.setOsType(OSType.LINUX);
        endpoint.setRemoteMachineConnectionProperties(remoteMachineConnectionProperties);
        helloMangleFaultSpec.setEndpoint(endpoint);
    }


    @Test(priority = 0)
    public void testInitOfInjection() {
        HelloManglePluginTaskHelper<HelloMangleFaultSpec> injectionTask = new HelloManglePluginTaskHelper<>();
        injectionTask.setPluginUtils(pluginUtils);
        Task<HelloMangleFaultSpec> task = injectionTask.init(helloMangleFaultSpec, null);
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
    }

    @Test(priority = 1)
    public void testInitOfRemediation() {
        HelloManglePluginTaskHelper<HelloMangleFaultSpec> remediationTask = new HelloManglePluginTaskHelper<>();
        remediationTask.setPluginUtils(pluginUtils);
        HelloMangleFaultSpec spec = helloMangleFaultSpec;
        Task<HelloMangleFaultSpec> task = remediationTask.init(spec, "12345");
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.REMEDIATION);
    }

    @Test(priority = 2)
    public void testInitOfRemediationWithEmptyInjectionId() {
        HelloManglePluginTaskHelper<HelloMangleFaultSpec> remediationTask = new HelloManglePluginTaskHelper<>();
        remediationTask.setPluginUtils(pluginUtils);
        HelloMangleFaultSpec spec = helloMangleFaultSpec;
        Task<HelloMangleFaultSpec> task = remediationTask.init(spec, " ");
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.REMEDIATION);
    }

    @Test(priority = 3)
    public void testExecuteInjectionTask() {
        HelloManglePluginTaskHelper<HelloMangleFaultSpec> injectionTask = new HelloManglePluginTaskHelper<>();
        injectionTask.setEventPublisher(publisher);
        injectionTask.setPluginUtils(pluginUtils);
        injectionTask.setCommandInfoExecutionHelper(commandInfoExecutionHelper);
        Task<HelloMangleFaultSpec> task = injectionTask.init(helloMangleFaultSpec, null);
        try {
            injectionTask.run(task);
        } catch (Exception e) {
            log.error("Task Failed with Exception: " + e);
            e.printStackTrace();
        }

        Assert.assertEquals(task.getTriggers().size(), 0);
        Assert.assertEquals(task.getTaskStatus(), TaskStatus.INITIALIZING);
    }

    @Test(priority = 4)
    public void testExecuteRemediationTask() throws MangleException {
        HelloManglePluginTaskHelper<HelloMangleFaultSpec> InjectionTaskHelper = new HelloManglePluginTaskHelper<>();
        InjectionTaskHelper.setPluginUtils(pluginUtils);
        InjectionTaskHelper.setCommandInfoExecutionHelper(commandInfoExecutionHelper);
        InjectionTaskHelper.setEventPublisher(publisher);
        Map<String, String> args = new HashMap<>();
        args.put("field1", "field1");
        args.put("field2", "field2");
        helloMangleFaultSpec.setArgs(args);
        Task<HelloMangleFaultSpec> injectionTask = InjectionTaskHelper.init(helloMangleFaultSpec, null);

        HelloManglePluginTaskHelper<HelloMangleFaultSpec> remediationTaskHelper = new HelloManglePluginTaskHelper<>();
        remediationTaskHelper.setPluginUtils(pluginUtils);
        remediationTaskHelper.setCommandInfoExecutionHelper(commandInfoExecutionHelper);
        remediationTaskHelper.setEventPublisher(publisher);
        Task<HelloMangleFaultSpec> remediationTask = remediationTaskHelper.init(helloMangleFaultSpec, "12345");
        remediationTask.setTaskData(injectionTask.getTaskData());
        remediationTask.setTaskTroubleShootingInfo(null);
        remediationTaskHelper.run(remediationTask);
        Assert.assertEquals(remediationTask.getTriggers().size(), 0);
        Assert.assertEquals(remediationTask.getTaskStatus(), TaskStatus.INITIALIZING);
    }

    @Test(priority = 5)
    public void testPrerequisiteCheckStageOfInjectionTask() {
        HelloManglePluginTaskHelper<HelloMangleFaultSpec> injectionTaskHelper = new HelloManglePluginTaskHelper<>();
        injectionTaskHelper.setPluginUtils(pluginUtils);
        injectionTaskHelper.setCommandInfoExecutionHelper(commandInfoExecutionHelper);
        injectionTaskHelper.setEventPublisher(publisher);
        Task<HelloMangleFaultSpec> injectionTask = injectionTaskHelper.init(helloMangleFaultSpec, null);
        injectionTask.updateTaskSubstage(AbstractCommandExecutionTaskHelper.SubStage.PREREQUISITES_CHECK.name());

        try {
            injectionTaskHelper.run(injectionTask);
        } catch (Exception e) {
            log.error("Task Failed with Exception: " + e);
            e.printStackTrace();
        }

        Assert.assertEquals(injectionTask.getTriggers().size(), 0);
        Assert.assertEquals(TaskStatus.INITIALIZING, injectionTask.getTaskStatus());
    }

    @Test(priority = 6)
    public void testPrepareTestmachineStageOfInjectionTask() {
        HelloManglePluginTaskHelper<HelloMangleFaultSpec> injectionTaskHelper = new HelloManglePluginTaskHelper<>();
        injectionTaskHelper.setPluginUtils(pluginUtils);
        injectionTaskHelper.setCommandInfoExecutionHelper(commandInfoExecutionHelper);
        injectionTaskHelper.setEventPublisher(publisher);
        Task<HelloMangleFaultSpec> injectionTask = injectionTaskHelper.init(helloMangleFaultSpec, null);
        injectionTask.updateTaskSubstage(AbstractCommandExecutionTaskHelper.SubStage.PREPARE_TARGET_MACHINE.name());

        try {
            injectionTaskHelper.run(injectionTask);
        } catch (Exception e) {
            log.error("Task Failed with Exception: " + e);
            e.printStackTrace();
        }

        Assert.assertEquals(injectionTask.getTriggers().size(), 0);
        Assert.assertEquals(TaskStatus.INITIALIZING, injectionTask.getTaskStatus());
    }

    @Test(priority = 7)
    public void testPrerequisitesCheckStageOfRemediationTask() throws MangleException {
        HelloManglePluginTaskHelper<HelloMangleFaultSpec> remediationTaskHelper = new HelloManglePluginTaskHelper<>();
        remediationTaskHelper.setPluginUtils(pluginUtils);
        remediationTaskHelper.setEventPublisher(publisher);
        remediationTaskHelper.setCommandInfoExecutionHelper(commandInfoExecutionHelper);
        Task<HelloMangleFaultSpec> remediationTask = remediationTaskHelper.init(helloMangleFaultSpec, "1235");
        remediationTask
                .updateTaskSubstage(AbstractCommandExecutionTaskHelper.SubStage.REMEDIATION_PREREQUISITES_CHECK.name());
        remediationTaskHelper.run(remediationTask);
        Assert.assertEquals(remediationTask.getTriggers().size(), 0);
        Assert.assertEquals(TaskStatus.INITIALIZING, remediationTask.getTaskStatus());
    }

    @Test(priority = 8)
    public void testTriggerRemediationStageOfRemediationTask() throws MangleException {
        HelloManglePluginTaskHelper<HelloMangleFaultSpec> remediationTaskHelper = new HelloManglePluginTaskHelper<>();
        remediationTaskHelper.setPluginUtils(pluginUtils);
        remediationTaskHelper.setEventPublisher(publisher);
        remediationTaskHelper.setCommandInfoExecutionHelper(commandInfoExecutionHelper);
        Task<HelloMangleFaultSpec> remediationTask = remediationTaskHelper.init(helloMangleFaultSpec, "1235");
        remediationTask.updateTaskSubstage(AbstractCommandExecutionTaskHelper.SubStage.TRIGGER_REMEDIATION.name());
        remediationTaskHelper.run(remediationTask);
        Assert.assertEquals(remediationTask.getTriggers().size(), 0);
        Assert.assertEquals(TaskStatus.INITIALIZING, remediationTask.getTaskStatus());
    }

    @Test(priority = 9)
    public void testExecuteInjectionTaskWithNullTaskTroubleShootingInfo() {
        HelloManglePluginTaskHelper<HelloMangleFaultSpec> injectionTask = new HelloManglePluginTaskHelper<>();
        injectionTask.setPluginUtils(pluginUtils);
        injectionTask.setEventPublisher(publisher);
        injectionTask.setCommandInfoExecutionHelper(commandInfoExecutionHelper);
        Task<HelloMangleFaultSpec> task = injectionTask.init(helloMangleFaultSpec, null);
        task.setTaskTroubleShootingInfo(null);
        try {
            injectionTask.run(task);
        } catch (Exception e) {
            log.error("Task Failed with Exception: " + e);
            e.printStackTrace();
        }

        Assert.assertEquals(task.getTriggers().size(), 0);
        Assert.assertEquals(task.getTaskStatus(), TaskStatus.INITIALIZING);
    }

}
