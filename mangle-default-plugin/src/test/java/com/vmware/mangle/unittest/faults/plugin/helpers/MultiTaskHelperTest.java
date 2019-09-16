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

package com.vmware.mangle.unittest.faults.plugin.helpers;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.log4j.Log4j2;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.JVMAgentFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.faults.plugin.helpers.MultiTaskHelper;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.faults.plugin.tasks.helpers.K8SFaultTriggerTaskHelper;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author hkilari
 *
 */
@Log4j2
public class MultiTaskHelperTest {
    FaultsMockData faultsMockData = new FaultsMockData();
    @Mock
    private EndpointClientFactory endpointClientFactory;

    @BeforeMethod
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        endpointClientFactory = new EndpointClientFactory();
    }


    private Task<K8SFaultTriggerSpec> getParentTask() {
        log.info("Preparing parent task");
        K8SFaultTriggerTaskHelper<K8SFaultTriggerSpec, JVMAgentFaultSpec> parentTask = new K8SFaultTriggerTaskHelper<>();
        Task<K8SFaultTriggerSpec> task = parentTask.init(faultsMockData.getK8SCPUFaultTriggerSpec(), null);
        task.getTriggers().add(new TaskTrigger());
        task.getTriggers().peek().setTaskStatus(TaskStatus.IN_PROGRESS);
        return task;
    }

    private Task<K8SFaultTriggerSpec> getCompletedChildTask() {
        log.info("Intializing child task with COMPLETED status");
        K8SFaultTriggerTaskHelper<K8SFaultTriggerSpec, JVMAgentFaultSpec> completedChildTask = new K8SFaultTriggerTaskHelper<>();
        Task<K8SFaultTriggerSpec> task = completedChildTask.init(faultsMockData.getK8SCPUFaultTriggerSpec(), null);
        task.getTriggers().add(new TaskTrigger());
        task.getTriggers().peek().setTaskStatus(TaskStatus.COMPLETED);
        return task;
    }

    private Task<K8SFaultTriggerSpec> getFailedChildTask() {
        log.info("Intializing child task with Failure status");
        K8SFaultTriggerTaskHelper<K8SFaultTriggerSpec, JVMAgentFaultSpec> failedChildTask = new K8SFaultTriggerTaskHelper<>();
        Task<K8SFaultTriggerSpec> task = failedChildTask.init(faultsMockData.getK8SCPUFaultTriggerSpec(), null);
        task.getTriggers().add(new TaskTrigger());
        task.getTriggers().peek().setTaskStatus(TaskStatus.FAILED);
        task.updateTaskFailureReason("Test Child Task Failure");
        return task;
    }

    @Test
    public void processCompletedTaskMap() throws MangleException {
        Map<String, Task<K8SFaultTriggerSpec>> taskObjmap = new HashMap<>();

        Task<K8SFaultTriggerSpec> completedChildTask = getCompletedChildTask();
        taskObjmap.put("testPod", completedChildTask);

        Task<K8SFaultTriggerSpec> parentTask = getParentTask();
        new MultiTaskHelper<K8SFaultTriggerSpec, K8SFaultTriggerSpec>().processTaskMap(taskObjmap,
                (Task<K8SFaultTriggerSpec>) parentTask);
        Assert.assertEquals(parentTask.getTaskOutput(),
                "PODS affeted by the Task\n Pod: testPod Result: SUCCESS Agent Task Id: " + completedChildTask.getId());
        Assert.assertEquals(parentTask.getTaskStatus(), TaskStatus.IN_PROGRESS);
    }

    @Test
    public void processFailedfultTaskMap() throws MangleException {
        Task<K8SFaultTriggerSpec> parentTask = getParentTask();

        Map<String, Task<K8SFaultTriggerSpec>> taskObjmap = new HashMap<>();
        Task<K8SFaultTriggerSpec> failedChildTask = getFailedChildTask();
        taskObjmap.put("testPod", failedChildTask);

        new MultiTaskHelper<K8SFaultTriggerSpec, K8SFaultTriggerSpec>().processTaskMap(taskObjmap,
                (Task<K8SFaultTriggerSpec>) parentTask);
        Assert.assertEquals(parentTask.getTaskOutput(),
                "PODS affeted by the Task\n Pod: testPod Result: FAILED Agent Task Id: " + failedChildTask.getId());
        Assert.assertEquals(parentTask.getTaskStatus(), TaskStatus.IN_PROGRESS);
        Assert.assertTrue(parentTask.getTaskFailureReason().contains(failedChildTask.getTaskFailureReason()));
    }

    @Test
    public void processTaskMapWithPassedAndFailedChildTasks() throws MangleException {
        Task<K8SFaultTriggerSpec> parentTask = getParentTask();

        Map<String, Task<K8SFaultTriggerSpec>> taskObjmap = new HashMap<>();
        Task<K8SFaultTriggerSpec> failedChildTask = getFailedChildTask();
        Task<K8SFaultTriggerSpec> completedChildTask = getCompletedChildTask();

        taskObjmap.put("testFailedPod", failedChildTask);
        taskObjmap.put("testCompletedPod", completedChildTask);

        new MultiTaskHelper<K8SFaultTriggerSpec, K8SFaultTriggerSpec>().processTaskMap(taskObjmap,
                (Task<K8SFaultTriggerSpec>) parentTask);
        Assert.assertTrue(parentTask.getTaskOutput()
                .contains("Pod: testFailedPod Result: FAILED Agent Task Id: " + failedChildTask.getId()));
        Assert.assertTrue(parentTask.getTaskOutput()
                .contains("Pod: testCompletedPod Result: SUCCESS Agent Task Id: " + completedChildTask.getId()));
        Assert.assertEquals(parentTask.getTaskStatus(), TaskStatus.IN_PROGRESS);
        Assert.assertTrue(parentTask.getTaskFailureReason().contains(failedChildTask.getTaskFailureReason()));
    }


}
