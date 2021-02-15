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

package com.vmware.mangle.unittest.services.helpers;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CpuFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DbFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DockerFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.KillProcessFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMStateFaultSpec;
import com.vmware.mangle.cassandra.model.plugin.PluginMetaInfo;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.services.CredentialService;
import com.vmware.mangle.services.EndpointService;
import com.vmware.mangle.services.PluginDetailsService;
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
    @Mock
    private PluginDetailsService pluginDetailsService;

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
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();
        Task<TaskSpec> remediationTask = tasksMockData.getRemediationTask();
        remediationTask.setScheduledTask(false);
        when(taskService.getTaskById(taskObj.getId())).thenReturn(taskObj);
        when(faultTaskFactory.getRemediationTask(any(), anyString())).thenReturn(remediationTask);
        doNothing().when(applicationEventPublisher).publishEvent(any());
        when(taskService.addOrUpdateTask(any())).thenReturn(remediationTask);

        when(credentialService.getCredentialByName(anyString()))
                .thenReturn(credentialsSpecMockData.getVCenterCredentialsData());
        when(endpointService.getEndpointByName(anyString())).thenReturn(endpointMockData.getDockerEndpointSpecMock());

        Task<TaskSpec> triggeredRemediatedTask = faultInjectionHelper.triggerRemediation(taskObj.getId());
        Assert.assertEquals(triggeredRemediatedTask.getTaskType(), TaskType.REMEDIATION);
    }

    @Test
    public void testTriggerRemediationFaultTriggeringTask() throws MangleException {
        K8SFaultTriggerSpec faultSpec = faultsMockData.getK8SCPUFaultTriggerSpec();
        tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();
        Task<TaskSpec> remediationTask = tasksMockData.getRemediationTask();
        remediationTask.setScheduledTask(false);
        when(taskService.getTaskById(taskObj.getId())).thenReturn(taskObj);
        when(faultTaskFactory.getRemediationTask(any(), anyString())).thenReturn(remediationTask);
        doNothing().when(applicationEventPublisher).publishEvent(any());
        when(taskService.addOrUpdateTask(any())).thenReturn(remediationTask);

        when(credentialService.getCredentialByName(anyString()))
                .thenReturn(credentialsSpecMockData.getVCenterCredentialsData());
        when(endpointService.getEndpointByName(anyString())).thenReturn(endpointMockData.getDockerEndpointSpecMock());

        Task<TaskSpec> triggeredRemediatedTask = faultInjectionHelper.triggerRemediation(taskObj.getId());
        Assert.assertEquals(triggeredRemediatedTask.getTaskType(), TaskType.REMEDIATION);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testGetTask() throws MangleException {
        CpuFaultSpec faultSpec = faultsMockData.getScheduleFaultSpec();
        tasksMockData = new TasksMockData<>(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
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
    @Test(expectedExceptions = MangleException.class)
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
            throw ex;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test(expectedExceptions = MangleException.class)
    public void testValidateSpecWithNullCronExpression() throws MangleException {
        CpuFaultSpec faultSpec = faultsMockData.getScheduleFaultSpecWithInvalidCron();
        when(endpointService.getEndpointByName(anyString())).thenReturn(endpointMockData.getDockerEndpointSpecMock());
        try {
            faultSpec.getSchedule().setCronExpression(null);
            faultInjectionHelper.validateSpec(faultSpec);
        } catch (MangleException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorCode.INVALID_SCHEDULE_INPUTS);
            throw ex;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test(expectedExceptions = MangleException.class)
    public void testValidateSpecWithEmptyCronExpression() throws MangleException {
        CpuFaultSpec faultSpec = faultsMockData.getScheduleFaultSpecWithInvalidCron();
        when(endpointService.getEndpointByName(anyString())).thenReturn(endpointMockData.getDockerEndpointSpecMock());
        try {
            faultSpec.getSchedule().setCronExpression("");
            faultInjectionHelper.validateSpec(faultSpec);
        } catch (MangleException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorCode.INVALID_SCHEDULE_INPUTS);
            throw ex;
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
            assertTrue(false);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test(expectedExceptions = MangleException.class)
    public void testValidateSpecWithInvalidScheduleInfo() throws MangleException {
        CpuFaultSpec faultSpec = faultsMockData.getScheduleFaultSpecWithInvalidCron();
        when(endpointService.getEndpointByName(anyString())).thenReturn(endpointMockData.getDockerEndpointSpecMock());
        try {
            faultSpec.getSchedule().setCronExpression("");
            faultSpec.getSchedule().setTimeInMilliseconds(null);
            faultInjectionHelper.validateSpec(faultSpec);
        } catch (MangleException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorCode.INVALID_SCHEDULE_INPUTS);
            throw ex;
        }
    }

    @Test(expectedExceptions = MangleException.class)
    public void testValidateDockerSpecificArguments() throws MangleException {
        DockerFaultSpec faultSpec = faultsMockData.getDockerPauseFaultSpec();
        faultSpec.setDockerArguments(null);
        faultSpec.setEndpoint(endpointMockData.getDockerEndpointSpecMock());
        try {
            faultInjectionHelper.validateEndpointTypeSpecificArguments(faultSpec);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.DOCKER_SPECIFIC_ARGUMENTS_REQUIRED);
            throw e;
        }
    }

    @Test(expectedExceptions = MangleException.class)
    public void testValidateK8SSpecificArguments() throws MangleException {
        CpuFaultSpec faultSpec = faultsMockData.getK8SCPUFaultSpec();
        faultSpec.setK8sArguments(null);
        faultSpec.setEndpoint(endpointMockData.k8sEndpointMockData());
        try {
            faultInjectionHelper.validateEndpointTypeSpecificArguments(faultsMockData.getDeleteK8SResourceFaultSpec());
            faultInjectionHelper.validateEndpointTypeSpecificArguments(faultSpec);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.K8S_SPECIFIC_ARGUMENTS_REQUIRED);
            throw e;
        }
    }

    @Test(expectedExceptions = MangleException.class)
    private void testRerunFaultWithInValidStatusOnTaskObj() throws MangleException {
        CpuFaultSpec faultSpec = faultsMockData.getK8SCPUFaultSpec();
        tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();
        try {
            when(taskService.getTaskById(taskObj.getId())).thenReturn(taskObj);
            faultInjectionHelper.rerunFault(taskObj.getId());
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.CANNOT_RERUN_FAULT);
            verify(taskService, times(1)).getTaskById(taskObj.getId());
            throw e;
        }
    }

    @Test(expectedExceptions = MangleException.class)
    private void testRerunFaultWithInValidTaskIdentifier() throws MangleException {
        CpuFaultSpec faultSpec = faultsMockData.getK8SCPUFaultSpec();
        tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();
        try {
            when(taskService.getTaskById(taskObj.getId())).thenReturn(null);
            faultInjectionHelper.rerunFault(taskObj.getId());
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.NO_TASK_FOUND);
            verify(taskService, times(1)).getTaskById(taskObj.getId());
            throw e;
        }
    }

    @Test
    private void testRerunFaultWithCompletedStatusOnTaskObj() throws MangleException {
        CpuFaultSpec faultSpec = faultsMockData.getK8SCPUFaultSpec();
        tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();
        taskObj.setTaskStatus(TaskStatus.COMPLETED);
        Task<TaskSpec> receivedTaskObj = null;
        try {
            when(taskService.getTaskById(taskObj.getId())).thenReturn(taskObj);
            when(taskService.addOrUpdateTask(taskObj)).thenReturn(taskObj);
            receivedTaskObj = faultInjectionHelper.rerunFault(taskObj.getId());
        } catch (MangleException e) {
            Assert.assertTrue(false, "Failed due to unexpected Exception: " + e.getMessage());
        }
        verify(taskService, times(1)).getTaskById(taskObj.getId());
        verify(taskService, times(1)).addOrUpdateTask(taskObj);
        Assert.assertEquals(receivedTaskObj.isTaskRetriggered(), true);
    }

    @Test(expectedExceptions = MangleException.class)
    private void testRerunFaultWithPluginUnavailabilityException() throws MangleException {
        CpuFaultSpec faultSpec = faultsMockData.getK8SCPUFaultSpec();
        tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();
        taskObj.setTaskStatus(TaskStatus.COMPLETED);
        PluginMetaInfo pluginMetaInfo = new PluginMetaInfo();
        pluginMetaInfo.setFaultName("cpuFault");
        ((CpuFaultSpec) taskObj.getTaskData()).setPluginMetaInfo(pluginMetaInfo);
        Task<TaskSpec> receivedTaskObj = null;
        try {
            when(taskService.getTaskById(taskObj.getId())).thenReturn(taskObj);
            when(pluginDetailsService.isPluginAvailable(pluginMetaInfo)).thenReturn(false);
            receivedTaskObj = faultInjectionHelper.rerunFault(taskObj.getId());
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.CUSTOM_FAULT_RE_RUN_FAILURE_DUE_TO_PLUGIN_STATE);
            verify(taskService, times(1)).getTaskById(taskObj.getId());
            verify(pluginDetailsService, times(1)).isPluginAvailable(pluginMetaInfo);
            Assert.assertEquals(receivedTaskObj, null);
            throw e;
        }
    }

    @Test
    private void testRerunFaultSuccessWithPluginMetaInfo() throws MangleException {
        CpuFaultSpec faultSpec = faultsMockData.getK8SCPUFaultSpec();
        tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();
        taskObj.setTaskStatus(TaskStatus.COMPLETED);
        PluginMetaInfo pluginMetaInfo = new PluginMetaInfo();
        pluginMetaInfo.setFaultName("cpuFault");
        ((CpuFaultSpec) taskObj.getTaskData()).setPluginMetaInfo(pluginMetaInfo);
        Task<TaskSpec> receivedTaskObj = null;
        try {
            when(taskService.getTaskById(taskObj.getId())).thenReturn(taskObj);
            when(taskService.addOrUpdateTask(taskObj)).thenReturn(taskObj);
            when(pluginDetailsService.isPluginAvailable(pluginMetaInfo)).thenReturn(true);
            receivedTaskObj = faultInjectionHelper.rerunFault(taskObj.getId());
        } catch (MangleException e) {
            Assert.assertTrue(false, "Failed due to unexpected Exception: " + e.getMessage());
        }
        verify(taskService, times(1)).getTaskById(taskObj.getId());
        verify(taskService, times(1)).addOrUpdateTask(taskObj);
        Assert.assertEquals(receivedTaskObj.isTaskRetriggered(), true);
        verify(pluginDetailsService, times(1)).isPluginAvailable(pluginMetaInfo);
    }

    @Test
    private void testRerunFaultWithFailedStatusOnTaskObj() throws MangleException {
        CpuFaultSpec faultSpec = faultsMockData.getK8SCPUFaultSpec();
        tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();
        taskObj.setTaskStatus(TaskStatus.FAILED);
        Task<TaskSpec> receivedTaskObj = null;
        try {
            when(taskService.getTaskById(taskObj.getId())).thenReturn(taskObj);
            when(taskService.addOrUpdateTask(taskObj)).thenReturn(taskObj);
            receivedTaskObj = faultInjectionHelper.rerunFault(taskObj.getId());
        } catch (MangleException e) {
            Assert.assertTrue(false, "Failed due to unexpected Exception: " + e.getMessage());
        }
        verify(taskService, times(1)).getTaskById(taskObj.getId());
        Assert.assertEquals(receivedTaskObj.isTaskRetriggered(), true);
    }

    @Test
    public void testUpdateDbFaultSpec() throws MangleException {
        DbFaultSpec faultSpec = faultsMockData.getDbConnectionLeakFaultSpecOfRemoteMachine();
        when(endpointService.getEndpointByName(anyString())).thenReturn(faultSpec.getChildEndpoint(),
                faultSpec.getEndpoint());
        when(credentialService.getCredentialByName(anyString())).thenReturn(faultSpec.getChildCredentials(),
                faultSpec.getCredentials());
        faultInjectionHelper.updateFaultSpec(faultSpec);
        verify(endpointService, times(2)).getEndpointByName(anyString());
        verify(credentialService, times(2)).getCredentialByName(anyString());
    }

    @Test
    public void testValidateKillProcessFaultSpec() throws MangleException {
        KillProcessFaultSpec faultSpec = faultsMockData.getKillProcessFaultOnRemoteMachine();
        try {
            faultInjectionHelper.validateKillProcessFaultSpec(faultSpec);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.BAD_REQUEST);
        }
    }
}