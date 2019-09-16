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


import lombok.extern.log4j.Log4j2;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.FaultTask;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.task.framework.helpers.AbstractCommandExecutionTaskHelper;
import com.vmware.mangle.task.framework.helpers.CommandInfoExecutionHelper;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author hkilari
 * @author bkaranam (bhanukiran karanam)
 */
@Log4j2
public class AbstractCommandExecutionTaskTest {
    @InjectMocks
    CommandInfoExecutionHelper commandInfoExecutionHelper;

    @BeforeClass
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Mock
    private ApplicationEventPublisher publisher;

    @Test(priority = 0)
    public void testInitOfInjection() {
        MockCommandExecutionTask<CommandExecutionFaultSpec> injectionTask = new MockCommandExecutionTask<>();
        FaultTask<CommandExecutionFaultSpec> task =
                injectionTask.init(new CommandExecutionFaultSpec(), null);
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
    }

    @Test(priority = 1)
    public void testInitOfRemediation() {
        MockCommandExecutionTask<CommandExecutionFaultSpec> remediationTask = new MockCommandExecutionTask<>();
        CommandExecutionFaultSpec spec = new CommandExecutionFaultSpec();
        FaultTask<CommandExecutionFaultSpec> task = remediationTask.init(spec, "12345");
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.REMEDIATION);
    }

    @Test(priority = 2)
    public void testInitOfRemediationWithEmptyInjectionId() {
        MockCommandExecutionTask<CommandExecutionFaultSpec> remediationTask = new MockCommandExecutionTask<>();
        CommandExecutionFaultSpec spec = new CommandExecutionFaultSpec();
        FaultTask<CommandExecutionFaultSpec> task = remediationTask.init(spec, " ");
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.REMEDIATION);
    }

    @Test(priority = 3)
    public void testExecuteInjectionTask() {
        MockCommandExecutionTask<CommandExecutionFaultSpec> injectionTask = new MockCommandExecutionTask<>();
        injectionTask.setEventPublisher(publisher);
        injectionTask.setCommandInfoExecutionHelper(commandInfoExecutionHelper);
        FaultTask<CommandExecutionFaultSpec> task =
                injectionTask.init(new CommandExecutionFaultSpec(), null);
        try {
            injectionTask.run(task);
        } catch (Exception e) {
            log.error("Task Failed with Exception: " + e);
            e.printStackTrace();
        }

        Assert.assertEquals(task.getTriggers().size(), 0);
        Assert.assertEquals(task.getTaskStatus(), TaskStatus.INITIALIZING);
    }

    @Test(priority = 4, enabled = false)
    public void testExecuteRemediationTask() throws MangleException {
        MockCommandExecutionTask<CommandExecutionFaultSpec> InjectionTaskHelper =
                new MockCommandExecutionTask<>();
        InjectionTaskHelper.setCommandInfoExecutionHelper(commandInfoExecutionHelper);
        InjectionTaskHelper.setEventPublisher(publisher);
        FaultTask<CommandExecutionFaultSpec> injectionTask =
                InjectionTaskHelper.init(new CommandExecutionFaultSpec(), null);

        MockCommandExecutionTask<CommandExecutionFaultSpec> remediationTaskHelper =
                new MockCommandExecutionTask<>();
        remediationTaskHelper.setCommandInfoExecutionHelper(commandInfoExecutionHelper);
        remediationTaskHelper.setEventPublisher(publisher);
        FaultTask<CommandExecutionFaultSpec> remediationTask =
                remediationTaskHelper.init(new CommandExecutionFaultSpec(), "12345");
        remediationTask.setTaskData(injectionTask.getTaskData());
        remediationTask.setTaskTroubleShootingInfo(null);
        remediationTaskHelper.run(remediationTask);
        Assert.assertEquals(remediationTask.getTriggers().size(), 1);
        Assert.assertEquals(remediationTask.getTaskStatus(), TaskStatus.INITIALIZING);
    }

    @Test(priority = 5)
    public void testPrerequisiteCheckStageOfInjectionTask() {
        MockCommandExecutionTask<CommandExecutionFaultSpec> injectionTaskHelper =
                new MockCommandExecutionTask<>();
        injectionTaskHelper.setCommandInfoExecutionHelper(commandInfoExecutionHelper);
        injectionTaskHelper.setEventPublisher(publisher);
        FaultTask<CommandExecutionFaultSpec> injectionTask =
                injectionTaskHelper.init(new CommandExecutionFaultSpec(), null);
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
        MockCommandExecutionTask<CommandExecutionFaultSpec> injectionTaskHelper =
                new MockCommandExecutionTask<>();
        injectionTaskHelper.setCommandInfoExecutionHelper(commandInfoExecutionHelper);
        injectionTaskHelper.setEventPublisher(publisher);
        FaultTask<CommandExecutionFaultSpec> injectionTask =
                injectionTaskHelper.init(new CommandExecutionFaultSpec(), null);
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
        MockCommandExecutionTask<CommandExecutionFaultSpec> remediationTaskHelper =
                new MockCommandExecutionTask<>();
        remediationTaskHelper.setEventPublisher(publisher);
        remediationTaskHelper.setCommandInfoExecutionHelper(commandInfoExecutionHelper);
        FaultTask<CommandExecutionFaultSpec> remediationTask =
                remediationTaskHelper.init(new CommandExecutionFaultSpec(), "1235");
        remediationTask
                .updateTaskSubstage(AbstractCommandExecutionTaskHelper.SubStage.REMEDIATION_PREREQUISITES_CHECK.name());
        remediationTaskHelper.run(remediationTask);
        Assert.assertEquals(remediationTask.getTriggers().size(), 0);
        Assert.assertEquals(TaskStatus.INITIALIZING, remediationTask.getTaskStatus());
    }

    @Test(priority = 8)
    public void testTriggerRemediationStageOfRemediationTask() throws MangleException {
        MockCommandExecutionTask<CommandExecutionFaultSpec> remediationTaskHelper =
                new MockCommandExecutionTask<>();
        remediationTaskHelper.setEventPublisher(publisher);
        remediationTaskHelper.setCommandInfoExecutionHelper(commandInfoExecutionHelper);
        FaultTask<CommandExecutionFaultSpec> remediationTask =
                remediationTaskHelper.init(new CommandExecutionFaultSpec(), "1235");
        remediationTask.updateTaskSubstage(AbstractCommandExecutionTaskHelper.SubStage.TRIGGER_REMEDIATION.name());
        remediationTaskHelper.run(remediationTask);
        Assert.assertEquals(remediationTask.getTriggers().size(), 0);
        Assert.assertEquals(TaskStatus.INITIALIZING, remediationTask.getTaskStatus());
    }

    @Test(priority = 9)
    public void testExecuteInjectionTaskWithNullTaskTroubleShootingInfo() {
        MockCommandExecutionTask<CommandExecutionFaultSpec> injectionTask = new MockCommandExecutionTask<>();
        injectionTask.setEventPublisher(publisher);
        injectionTask.setCommandInfoExecutionHelper(commandInfoExecutionHelper);
        FaultTask<CommandExecutionFaultSpec> task =
                injectionTask.init(new CommandExecutionFaultSpec(), null);
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
