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

package com.vmware.mangle.unittest.services.services;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CpuFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DockerFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMStateFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.services.CredentialService;
import com.vmware.mangle.services.EndpointService;
import com.vmware.mangle.services.SchedulerService;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.helpers.FaultInjectionHelper;
import com.vmware.mangle.services.helpers.FaultTaskFactory;
import com.vmware.mangle.services.mockdata.CredentialsSpecMockData;
import com.vmware.mangle.services.mockdata.EndpointMockData;
import com.vmware.mangle.services.mockdata.FaultsMockData;
import com.vmware.mangle.services.mockdata.TasksMockData;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author chetanc
 * @author bkaranam
 */
public class FaultInjectionHelperTest {

    @Mock
    private TaskService taskService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private SchedulerService schedulerService;
    @Mock
    private FaultTaskFactory faultTaskFactory;
    @Mock
    private EndpointService endpointService;
    @Mock
    private CredentialService credentialService;

    @InjectMocks
    private FaultInjectionHelper faultInjectionHelper;

    private FaultsMockData faultsMockData;
    private TasksMockData<TaskSpec> tasksMockData;
    private EndpointMockData endpointMockData = new EndpointMockData();
    private CredentialsSpecMockData credentialsSpecMockData = new CredentialsSpecMockData();

    @BeforeClass
    public void initMockData() {
        faultsMockData = new FaultsMockData();
    }

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testTriggerRemediationFaultTask() throws MangleException {
        VMStateFaultSpec faultSpec = faultsMockData.getVMStateFaultSpec();
        tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> taskObj = tasksMockData.getDummyTask();
        Task<TaskSpec> remediationTask = tasksMockData.getRemediationTask();
        remediationTask.setScheduledTask(false);
        when(taskService.getTaskById(taskObj.getId())).thenReturn(taskObj);
        when(faultTaskFactory.getRemediationTask(any(), anyString())).thenReturn(remediationTask);
        doNothing().when(applicationEventPublisher).publishEvent(any());
        when(taskService.addOrUpdateTask(any())).thenReturn(remediationTask);

        when(credentialService.getCredentialByName(anyString()))
                .thenReturn(credentialsSpecMockData.getVCenterCredentialsData());
        when(endpointService.getEndpointByName(anyString())).thenReturn(endpointMockData.getVCenterEndpointSpecMock());

        Task<TaskSpec> triggeredRemediatedTask = faultInjectionHelper.triggerRemediation(taskObj.getId());
        Assert.assertEquals(triggeredRemediatedTask.getTaskType(), TaskType.REMEDIATION);
    }

    @Test
    public void testTriggerRemediationFaultTriggeringTask() throws MangleException {
        K8SFaultTriggerSpec faultSpec = faultsMockData.getK8SCPUFaultTriggerSpec();
        tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> taskObj = tasksMockData.getDummyTask();
        Task<TaskSpec> remediationTask = tasksMockData.getRemediationTask();
        remediationTask.setScheduledTask(false);
        when(taskService.getTaskById(taskObj.getId())).thenReturn(taskObj);
        when(faultTaskFactory.getRemediationTask(any(), anyString())).thenReturn(remediationTask);
        doNothing().when(applicationEventPublisher).publishEvent(any());
        when(taskService.addOrUpdateTask(any())).thenReturn(remediationTask);

        when(credentialService.getCredentialByName(anyString()))
                .thenReturn(credentialsSpecMockData.getVCenterCredentialsData());
        when(endpointService.getEndpointByName(anyString())).thenReturn(endpointMockData.getVCenterEndpointSpecMock());

        Task<TaskSpec> triggeredRemediatedTask = faultInjectionHelper.triggerRemediation(taskObj.getId());
        Assert.assertEquals(triggeredRemediatedTask.getTaskType(), TaskType.REMEDIATION);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testGetTask() throws MangleException {
        CpuFaultSpec faultSpec = faultsMockData.getScheduleFaultSpec();
        tasksMockData = new TasksMockData<>(faultSpec);
        Task taskObj = tasksMockData.getDummyTask();
        taskObj.setTaskType(TaskType.INJECTION);
        when(taskService.getTaskById(taskObj.getId())).thenReturn(taskObj);
        when(faultTaskFactory.getTask(any())).thenReturn(taskObj);
        doNothing().when(applicationEventPublisher).publishEvent(any());
        when(taskService.addOrUpdateTask(any())).thenReturn(taskObj);

        when(credentialService.getCredentialByName(anyString()))
                .thenReturn(credentialsSpecMockData.getVCenterCredentialsData());
        when(endpointService.getEndpointByName(anyString())).thenReturn(endpointMockData.getVCenterEndpointSpecMock());

        Task triggeredRemediatedTask = faultInjectionHelper.getTask(faultSpec);
        Assert.assertEquals(triggeredRemediatedTask.getTaskType(), TaskType.INJECTION);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testValidateSpec() throws MangleException {
        CpuFaultSpec faultSpec = faultsMockData.getScheduleFaultSpec();
        when(endpointService.getEndpointByName(anyString())).thenReturn(endpointMockData.getVCenterEndpointSpecMock());
        try {
            faultInjectionHelper.validateSpec(faultSpec);
        } catch (MangleException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorCode.INVALID_SCHEDULE_INPUTS);
            Assert.assertTrue(false);

        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testValidateSpecWithDockerEndpointType() throws MangleException {
        CpuFaultSpec faultSpec = faultsMockData.getK8SCPUFaultSpec();
        when(endpointService.getEndpointByName(anyString())).thenReturn(endpointMockData.getDockerEndpointSpecMock());
        try {
            faultInjectionHelper.validateSpec(faultSpec);
        } catch (MangleException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorCode.INVALID_SCHEDULE_INPUTS);
            Assert.assertTrue(false);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testValidateSpecWithInvalidCronExpression() throws MangleException {
        CpuFaultSpec faultSpec = faultsMockData.getScheduleFaultSpecWithInvalidCron();
        when(endpointService.getEndpointByName(anyString())).thenReturn(endpointMockData.getDockerEndpointSpecMock());
        try {
            faultInjectionHelper.validateSpec(faultSpec);
            faultSpec.getSchedule().setCronExpression("");
            faultInjectionHelper.validateSpec(faultSpec);
            faultSpec.getSchedule().setCronExpression(null);
            faultInjectionHelper.validateSpec(faultSpec);
        } catch (MangleException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorCode.INVALID_CRON_EXPRESSION);
            Assert.assertTrue(true);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testValidateSpecWithNullCronExpression() throws MangleException {
        CpuFaultSpec faultSpec = faultsMockData.getScheduleFaultSpecWithInvalidCron();
        when(endpointService.getEndpointByName(anyString())).thenReturn(endpointMockData.getDockerEndpointSpecMock());
        try {
            faultSpec.getSchedule().setCronExpression(null);
            faultInjectionHelper.validateSpec(faultSpec);
        } catch (MangleException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorCode.INVALID_SCHEDULE_INPUTS);
            Assert.assertTrue(true);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testValidateSpecWithEmptyCronExpression() throws MangleException {
        CpuFaultSpec faultSpec = faultsMockData.getScheduleFaultSpecWithInvalidCron();
        when(endpointService.getEndpointByName(anyString())).thenReturn(endpointMockData.getDockerEndpointSpecMock());
        try {
            faultSpec.getSchedule().setCronExpression("");
            faultInjectionHelper.validateSpec(faultSpec);
        } catch (MangleException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorCode.INVALID_SCHEDULE_INPUTS);
            Assert.assertTrue(true);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testValidateSpecWithTimeInMilliseconds() throws MangleException {
        CpuFaultSpec faultSpec = faultsMockData.getScheduleFaultSpecWithInvalidCron();
        when(endpointService.getEndpointByName(anyString())).thenReturn(endpointMockData.getDockerEndpointSpecMock());
        try {
            faultSpec.getSchedule().setCronExpression("");
            faultSpec.getSchedule().setTimeInMilliseconds(12345L);
            faultInjectionHelper.validateSpec(faultSpec);
        } catch (MangleException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorCode.INVALID_SCHEDULE_INPUTS);
            Assert.assertTrue(true);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testValidateSpecWithInvalidScheduleInfo() throws MangleException {
        CpuFaultSpec faultSpec = faultsMockData.getScheduleFaultSpecWithInvalidCron();
        when(endpointService.getEndpointByName(anyString())).thenReturn(endpointMockData.getDockerEndpointSpecMock());
        try {
            faultSpec.getSchedule().setCronExpression("");
            faultSpec.getSchedule().setTimeInMilliseconds(null);
            faultInjectionHelper.validateSpec(faultSpec);
        } catch (MangleException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorCode.INVALID_SCHEDULE_INPUTS);
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testValidateDockerSpecificArguments() {
        DockerFaultSpec faultSpec = faultsMockData.getDockerPauseFaultSpec();
        faultSpec.setDockerArguments(null);
        faultSpec.setEndpoint(endpointMockData.getDockerEndpointSpecMock());
        try {
            faultInjectionHelper.validateEndpointTypeSpecificArguments(faultSpec);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.DOCKER_SPECIFIC_ARGUMENTS_REQUIRED);
        }
    }

    @Test
    public void testValidateK8SSpecificArguments() {
        CpuFaultSpec faultSpec = faultsMockData.getK8SCPUFaultSpec();
        faultSpec.setK8sArguments(null);
        faultSpec.setEndpoint(endpointMockData.k8sEndpointMockData());
        try {
            faultInjectionHelper.validateEndpointTypeSpecificArguments(faultSpec);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.K8S_SPECIFIC_ARGUMENTS_REQUIRED);
        }
    }
}