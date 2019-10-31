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

package com.vmware.mangle.unittest.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

import lombok.extern.log4j.Log4j2;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.CpuFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMProperties;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.MemoryFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.services.dto.FaultEventSpec;
import com.vmware.mangle.utils.PopulateFaultEventData;
import com.vmware.mangle.utils.helpers.notifiers.Notifier;


/**
 * @author dbhat
 *
 */

@Log4j2
public class PopulateFaultEventDataTest extends PowerMockTestCase {
    private final String faultName = "dummyFault";
    private final String faultStartTime = (new Date()).toGMTString();
    private final String faultEndTime = (new Date()).toGMTString();
    private final String taskID = UUID.randomUUID().toString();
    private final Map<String, String> endpointTags = new HashMap<String, String>();
    private final Map<String, String> faultTags = new HashMap<String, String>();

    @Mock
    Notifier wavefrontNotifier;

    @Mock
    Task task;

    @Mock
    TaskTrigger trigger;

    @Mock
    CommandExecutionFaultSpec commandExecutionFaultSpec;

    @Mock
    K8SFaultTriggerSpec k8sFaultTriggerSpec;
    @Mock
    CpuFaultSpec cpuSpec;
    @Mock
    MemoryFaultSpec memorySpec;
    @Mock
    JVMProperties jvmProps;

    @Mock
    EndpointSpec endpoint;

    @Mock
    FaultEventSpec faultEventInfo;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @SuppressWarnings("unchecked")
    @Test(description = "Validate for invalid Task data - null condition")
    public void validateInvalidTaskData() {
        log.info("Validating when there is a invalid task Data - null condition");
        populateBaseData();
        when(commandExecutionFaultSpec.getEndpoint()).thenReturn(null);

        @SuppressWarnings("rawtypes")
        PopulateFaultEventData populateTaskData = new PopulateFaultEventData(task);
        Assert.assertNull(populateTaskData.getFaultEventSpec());
    }

    @Test(description = "Validate populating of Fault Event data when all valid data is provided")
    public void validateFaultSpecData() {
        log.info("Validating the fault event data when all valid data are populated and Task status is completed");
        populateBaseData();
        when(commandExecutionFaultSpec.getEndpoint()).thenReturn(endpoint);
        when(trigger.getTaskStatus()).thenReturn(TaskStatus.COMPLETED);
        when(task.getTaskType()).thenReturn(TaskType.INJECTION);
        getFaultData();

        PopulateFaultEventData populateTaskData = new PopulateFaultEventData(task);
        FaultEventSpec eventData = populateTaskData.getFaultEventSpec();

        Assert.assertEquals(faultName + "-" + TaskType.INJECTION, eventData.getFaultName());
        Assert.assertEquals(faultStartTime, eventData.getFaultStartTime());
        Assert.assertEquals(taskID, eventData.getTaskId());
        Assert.assertEquals(TaskStatus.COMPLETED.name(), eventData.getFaultStatus());
    }

    @Test(description = "Validate populating of Fault Event data when Task status is Failed")
    public void validateFaultSpecDataWhenTaskFailed() {
        log.info("Validating the fault event data when all valid data are populated");
        populateBaseData();
        when(task.getTaskType()).thenReturn(TaskType.INJECTION);
        when(commandExecutionFaultSpec.getEndpoint()).thenReturn(endpoint);
        when(trigger.getTaskStatus()).thenReturn(TaskStatus.FAILED);
        getFaultData();

        PopulateFaultEventData populateTaskData = new PopulateFaultEventData(task);
        FaultEventSpec eventData = populateTaskData.getFaultEventSpec();

        Assert.assertEquals(faultName + "-" + TaskType.INJECTION, eventData.getFaultName());
        Assert.assertEquals(faultStartTime, eventData.getFaultStartTime());
        Assert.assertEquals(taskID, eventData.getTaskId());
        Assert.assertEquals(TaskStatus.FAILED.name(), eventData.getFaultStatus());
        Assert.assertNotNull(eventData.getFaultEndTime());
    }

    @Test(description = "Validate populating of Fault Event data when Task status is Failed and fautt time out is null")
    public void faultSpecDataWhenTaskFailedAndTimeOutIsNull() {
        log.info("Validating the fault event data when all valid data are populated");
        populateBaseData();
        when(task.getTaskType()).thenReturn(TaskType.INJECTION);
        when(commandExecutionFaultSpec.getEndpoint()).thenReturn(endpoint);
        when(commandExecutionFaultSpec.getTimeoutInMilliseconds()).thenReturn(null);
        when(trigger.getTaskStatus()).thenReturn(TaskStatus.FAILED);
        getFaultData();

        PopulateFaultEventData populateTaskData = new PopulateFaultEventData(task);
        FaultEventSpec eventData = populateTaskData.getFaultEventSpec();

        Assert.assertEquals(faultName + "-" + TaskType.INJECTION, eventData.getFaultName());
        Assert.assertEquals(faultStartTime, eventData.getFaultStartTime());
        Assert.assertEquals(taskID, eventData.getTaskId());
        Assert.assertEquals(TaskStatus.FAILED.name(), eventData.getFaultStatus());
        Assert.assertNotNull(eventData.getFaultEndTime());
    }

    private void getFaultData() {
        populateTags();

        when(commandExecutionFaultSpec.getFaultName()).thenReturn(faultName);
        when(commandExecutionFaultSpec.getTags()).thenReturn(faultTags);
        when(commandExecutionFaultSpec.getTimeoutInMilliseconds()).thenReturn(60000);
        when(endpoint.getTags()).thenReturn(endpointTags);
        when(trigger.getStartTime()).thenReturn(faultStartTime);
        when(trigger.getEndTime()).thenReturn(faultEndTime);
        when(task.getId()).thenReturn(taskID);
        when(commandExecutionFaultSpec.getEndpointName()).thenReturn("dummyEndpointName");
        when(cpuSpec.getCpuLoad()).thenReturn(40);
        when(cpuSpec.getJvmProperties()).thenReturn(jvmProps);
        when(jvmProps.getJvmprocess()).thenReturn("dummyProcess");
        when(memorySpec.getMemoryLoad()).thenReturn(80);
    }

    private void populateTags() {
        endpointTags.put("env", "prod");
        endpointTags.put("instance", "Platform01");
        faultTags.put("service", "vrni");
        faultTags.put("app", "platform");
    }

    private void populateBaseData() {
        Stack<TaskTrigger> triggers = mock(Stack.class);
        triggers.add(trigger);
        when(task.getTaskData()).thenReturn(k8sFaultTriggerSpec);
        when(task.getTriggers()).thenReturn(triggers);
        when(task.getTriggers().peek()).thenReturn(trigger);
        when(k8sFaultTriggerSpec.getFaultSpec()).thenReturn(commandExecutionFaultSpec);
    }


}
