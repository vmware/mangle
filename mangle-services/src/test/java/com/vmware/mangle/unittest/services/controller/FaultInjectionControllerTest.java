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

package com.vmware.mangle.unittest.services.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.vmware.mangle.services.constants.CommonConstants.DEFAULT_BLOCK_SIZE;
import static com.vmware.mangle.services.constants.CommonConstants.IO_SIZE_ARG;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.CredentialsSpec;
import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.endpoint.RemoteMachineCredentials;
import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.CpuFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DiskIOFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DiskSpaceSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DockerFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.FilehandlerLeakFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SDeleteResourceFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SResourceNotReadyFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SServiceUnavailableFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.KernelPanicSpec;
import com.vmware.mangle.cassandra.model.faults.specs.KillProcessFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.MemoryFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.faults.specs.ThreadLeakFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMDiskFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMNicFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMStateFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.model.aws.AwsEC2NetworkFaults;
import com.vmware.mangle.model.aws.AwsEC2StateFaults;
import com.vmware.mangle.model.aws.faults.spec.AwsEC2InstanceStateFaultSpec;
import com.vmware.mangle.model.aws.faults.spec.AwsEC2NetworkFaultSpec;
import com.vmware.mangle.services.controller.FaultInjectionController;
import com.vmware.mangle.services.enums.AgentFaultName;
import com.vmware.mangle.services.enums.DockerFaultName;
import com.vmware.mangle.services.enums.K8SFaultName;
import com.vmware.mangle.services.enums.VCenterDiskFaults;
import com.vmware.mangle.services.enums.VCenterNicFaults;
import com.vmware.mangle.services.enums.VCenterStateFaults;
import com.vmware.mangle.services.helpers.FaultInjectionHelper;
import com.vmware.mangle.services.mockdata.CredentialsSpecMockData;
import com.vmware.mangle.services.mockdata.EndpointMockData;
import com.vmware.mangle.services.mockdata.FaultsMockData;
import com.vmware.mangle.services.mockdata.TasksMockData;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Testing FaultInjectionController
 *
 * @author bkaranam
 * @author rpraveen
 * @author jayasankarr
 */
public class FaultInjectionControllerTest {

    @Mock
    private FaultInjectionHelper faultInjectionHelper;

    @InjectMocks
    private FaultInjectionController faultInjectionController;

    private EndpointMockData endpointMockData = new EndpointMockData();

    private CredentialsSpecMockData credentialsMockData = new CredentialsSpecMockData();

    private FaultsMockData faultsMockData = new FaultsMockData();

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInjectionCPUFault() throws MangleException {
        CpuFaultSpec faultSpec = faultsMockData.getK8SCPUFaultSpec();
        TasksMockData tasksMockData = new TasksMockData(faultSpec);
        Task taskObj = tasksMockData.getDummyTask();
        EndpointSpec endpointSpec = endpointMockData.k8sEndpointMockData();
        CredentialsSpec credentialsSpec = credentialsMockData.getk8SCredentialsData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);
        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        ResponseEntity<Task<TaskSpec>> responseEntity = faultInjectionController.injectCPUFault(faultSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) responseEntity.getBody().getTaskData()).getFaultName(),
                AgentFaultName.INJECT_CPU_FAULT.toString());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @Test
    public void testk8SDeleteResourceFault() throws MangleException {
        K8SDeleteResourceFaultSpec faultSpec = faultsMockData.getDeleteK8SResourceFaultSpec();
        TasksMockData tasksMockData = new TasksMockData(faultSpec);
        Task taskObj = tasksMockData.getDummyTask();
        EndpointSpec endpointSpec = endpointMockData.k8sEndpointMockData();
        CredentialsSpec credentialsSpec = credentialsMockData.getk8SCredentialsData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Task<TaskSpec>> responseEntity = faultInjectionController.deleteK8SResourceFault(faultSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) responseEntity.getBody().getTaskData()).getFaultName(),
                K8SFaultName.DELETE_RESOURCE.name());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @Test
    public void testk8SResourceNotReadyFault() throws MangleException {
        K8SResourceNotReadyFaultSpec faultSpec = faultsMockData.getK8SResourceNotReadyFaultSpec();
        TasksMockData tasksMockData = new TasksMockData(faultSpec);
        Task taskObj = tasksMockData.getDummyTask();
        EndpointSpec endpointSpec = endpointMockData.k8sEndpointMockData();
        CredentialsSpec credentialsSpec = credentialsMockData.getk8SCredentialsData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Task<TaskSpec>> responseEntity = faultInjectionController.k8SResourceNotReadyFault(faultSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) responseEntity.getBody().getTaskData()).getFaultName(),
                K8SFaultName.NOTREADY_RESOURCE.name());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @Test
    public void testk8SServiceUnavailableFault() throws MangleException {
        K8SServiceUnavailableFaultSpec faultSpec = faultsMockData.getK8SServiceUnavailableFaultSpec();
        TasksMockData tasksMockData = new TasksMockData(faultSpec);
        Task taskObj = tasksMockData.getDummyTask();
        EndpointSpec endpointSpec = endpointMockData.k8sEndpointMockData();
        CredentialsSpec credentialsSpec = credentialsMockData.getk8SCredentialsData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Task<TaskSpec>> responseEntity = faultInjectionController.K8SServiceUnavailableyFault(faultSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) responseEntity.getBody().getTaskData()).getFaultName(),
                K8SFaultName.SERVICE_UNAVAILABLE.name());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @Test
    public void testInjectionDockerPause() throws MangleException {
        DockerFaultSpec faultSpec = faultsMockData.getDockerPauseFaultSpec();

        TasksMockData tasksMockData = new TasksMockData(faultSpec);
        Task taskObj = tasksMockData.getDummyTask();
        EndpointSpec endpointSpec = endpointMockData.dockerEndpointMockData();
        faultSpec.setEndpoint(endpointSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Task<TaskSpec>> responseEntity = faultInjectionController.injectDockerFault(faultSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) responseEntity.getBody().getTaskData()).getFaultName(),
                DockerFaultName.DOCKER_PAUSE.name());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }


    @Test
    public void testInjectionDockerStop() throws MangleException {
        DockerFaultSpec faultSpec = faultsMockData.getDockerStopFaultSpec();

        TasksMockData tasksMockData = new TasksMockData(faultSpec);
        Task taskObj = tasksMockData.getDummyTask();
        EndpointSpec endpointSpec = endpointMockData.dockerEndpointMockData();
        faultSpec.setEndpoint(endpointSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Task<TaskSpec>> responseEntity = faultInjectionController.injectDockerFault(faultSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) responseEntity.getBody().getTaskData()).getFaultName(),
                DockerFaultName.DOCKER_STOP.name());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testInjectionDockerPauseFaultFailure() throws MangleException {
        DockerFaultSpec faultSpec = faultsMockData.getDockerPauseFaultSpec();
        TasksMockData tasksMockData = new TasksMockData(faultSpec);
        doThrow(new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.ENDPOINT_NAME))
                .when(faultInjectionHelper).validateSpec(faultSpec);
        try {
            faultInjectionController.injectDockerFault(faultSpec);
        } catch (MangleException e) {
            Assert.assertEquals(ErrorCode.FIELD_VALUE_EMPTY, e.getErrorCode());
            verify(faultInjectionHelper, times(1)).validateSpec(faultSpec);
            throw e;
        }
    }

    @Test
    public void testInjectionVCenterNicFault() throws MangleException {
        VMNicFaultSpec faultSpec = faultsMockData.getVMNicFaultSpec();
        TasksMockData tasksMockData = new TasksMockData(faultSpec);
        Task taskObj = tasksMockData.getDummyTask();
        EndpointSpec endpointSpec = endpointMockData.getVCenterEndpointSpecMock();
        CredentialsSpec credentialsSpec = credentialsMockData.getVCenterCredentialsData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Task<TaskSpec>> responseEntity = faultInjectionController.injectVCenterNicFault(faultSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) responseEntity.getBody().getTaskData()).getFaultName(),
                VCenterNicFaults.DISCONNECT_NIC.name());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @Test
    public void testInjectionVCenterDiskFault() throws MangleException {
        VMDiskFaultSpec faultSpec = faultsMockData.getVMDiskFaultSpec();
        TasksMockData tasksMockData = new TasksMockData(faultSpec);
        Task taskObj = tasksMockData.getDummyTask();
        EndpointSpec endpointSpec = endpointMockData.getVCenterEndpointSpecMock();
        CredentialsSpec credentialsSpec = credentialsMockData.getVCenterCredentialsData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Task<TaskSpec>> responseEntity = faultInjectionController.injectVCenterDiskFault(faultSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) responseEntity.getBody().getTaskData()).getFaultName(),
                VCenterDiskFaults.DISCONNECT_DISK.name());
    }

    @Test
    public void testInjectionVCenterStateFault() throws MangleException {
        VMStateFaultSpec faultSpec = faultsMockData.getVMStateFaultSpec();
        TasksMockData tasksMockData = new TasksMockData(faultSpec);
        EndpointSpec endpointSpec = endpointMockData.getVCenterEndpointSpecMock();
        CredentialsSpec credentialsSpec = credentialsMockData.getVCenterCredentialsData();
        Task taskObj = tasksMockData.getDummyTask();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Task<TaskSpec>> responseEntity = faultInjectionController.injectVCenterStateFault(faultSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) responseEntity.getBody().getTaskData()).getFaultName(),
                VCenterStateFaults.POWEROFF_VM.name());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testInjectionVCenterNicFaultFailure() throws MangleException {
        VMNicFaultSpec faultSpec = faultsMockData.getVMNicFaultSpec();
        doThrow(new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.ENDPOINT_NAME))
                .when(faultInjectionHelper).validateSpec(faultSpec);
        try {
            faultInjectionController.injectVCenterNicFault(faultSpec);
        } catch (MangleException e) {
            Assert.assertEquals(ErrorCode.FIELD_VALUE_EMPTY, e.getErrorCode());
            verify(faultInjectionHelper, times(1)).validateSpec(faultSpec);
            throw e;
        }
    }

    @Test
    public void testRemediationTask() throws MangleException {
        VMStateFaultSpec faultSpec = faultsMockData.getVMStateFaultSpec();
        TasksMockData<TaskSpec> tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> taskObj = tasksMockData.getDummyTask();
        when(faultInjectionHelper.triggerRemediation(taskObj.getId())).thenReturn(taskObj);

        ResponseEntity<Task<TaskSpec>> responseEntity = faultInjectionController.remediateFault(taskObj.getId());

        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);

    }

    @Test(expectedExceptions = MangleException.class)
    public void testInjectionVCenterDiskFaultFailure() throws MangleException {
        VMDiskFaultSpec faultSpec = faultsMockData.getVMDiskFaultSpec();
        doThrow(new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.ENDPOINT_NAME))
                .when(faultInjectionHelper).validateSpec(faultSpec);
        try {
            faultInjectionController.injectVCenterDiskFault(faultSpec);
        } catch (MangleException e) {
            Assert.assertEquals(ErrorCode.FIELD_VALUE_EMPTY, e.getErrorCode());
            verify(faultInjectionHelper, times(1)).validateSpec(faultSpec);
            throw e;
        }
    }

    @Test(expectedExceptions = MangleException.class)
    public void testInjectionVCenterStateFaultFailure() throws MangleException {
        VMStateFaultSpec faultSpec = faultsMockData.getVMStateFaultSpec();
        TasksMockData tasksMockData = new TasksMockData(faultSpec);
        Task taskObj = tasksMockData.getDummyTask();

        EndpointSpec endpointSpec = endpointMockData.getVCenterEndpointSpecMock();
        CredentialsSpec credentialsSpec = credentialsMockData.getVCenterCredentialsData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        doThrow(new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.ENDPOINT_NAME))
                .when(faultInjectionHelper).validateSpec(faultSpec);
        try {
            faultInjectionController.injectVCenterStateFault(faultSpec);
        } catch (MangleException e) {
            Assert.assertEquals(ErrorCode.FIELD_VALUE_EMPTY, e.getErrorCode());
            throw e;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testInjectKillProcessFault() throws MangleException {
        KillProcessFaultSpec faultSpec = faultsMockData.getKillProcessSpec();
        TasksMockData tasksMockData = new TasksMockData(faultSpec);
        Task taskObj = tasksMockData.getDummyTask();
        RemoteMachineCredentials credentialsSpec = credentialsMockData.getRMCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.rmEndpointMockData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);
        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Task<TaskSpec>> responseEntity = faultInjectionController.injectKillProcessFault(faultSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) responseEntity.getBody().getTaskData()).getFaultName(),
                AgentFaultName.INJECT_KILL_PROCESS_FAULT.toString());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testInjectMemoryFault() throws MangleException {
        MemoryFaultSpec faultSpec = faultsMockData.getMemoryFaultSpecOfRemoteMachine();
        TasksMockData tasksMockData = new TasksMockData(faultSpec);
        Task taskObj = tasksMockData.getDummyTask();
        RemoteMachineCredentials credentialsSpec = credentialsMockData.getRMCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.rmEndpointMockData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Task<TaskSpec>> responseEntity = faultInjectionController.injectMemoryFault(faultSpec);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) responseEntity.getBody().getTaskData()).getFaultName(),
                AgentFaultName.INJECT_MEMORY_FAULT.toString());
        verify(faultInjectionHelper, times(1)).getTask(any());

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testInjectDiskIOFault() throws MangleException {
        DiskIOFaultSpec faultSpec = faultsMockData.getDiskIOFaultSpecOfRemoteMachine();
        TasksMockData tasksMockData = new TasksMockData(faultSpec);
        Task taskObj = tasksMockData.getDummyTask();
        RemoteMachineCredentials credentialsSpec = credentialsMockData.getRMCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.rmEndpointMockData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Task<TaskSpec>> responseEntity = faultInjectionController.injectDiskIOFault(faultSpec);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) responseEntity.getBody().getTaskData()).getFaultName(),
                AgentFaultName.INJECT_DISK_IO_FAULT.toString());
        Assert.assertEquals(
                ((CommandExecutionFaultSpec) responseEntity.getBody().getTaskData()).getArgs().get(IO_SIZE_ARG),
                DEFAULT_BLOCK_SIZE);
        verify(faultInjectionHelper, times(1)).getTask(any());

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testInjectFileHandlerLeakFault() throws MangleException {
        FilehandlerLeakFaultSpec faultSpec = faultsMockData.getFileHandlerLeakFaultSpecOfDockerEndpoint();
        TasksMockData tasksMockData = new TasksMockData(faultSpec);
        Task taskObj = tasksMockData.getDummyTask();
        EndpointSpec endpointSpec = endpointMockData.dockerEndpointMockData();
        faultSpec.setEndpoint(endpointSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Task<TaskSpec>> responseEntity = faultInjectionController.injectfileHandlerLeakFault(faultSpec);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) responseEntity.getBody().getTaskData()).getFaultName(),
                AgentFaultName.INJECT_FILE_HANDLER_FAULT.toString());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testInjectThreadLeakFault() throws MangleException {
        ThreadLeakFaultSpec faultSpec = faultsMockData.getThreadLeakFaultSpecOfDockerEndpoint();
        TasksMockData tasksMockData = new TasksMockData(faultSpec);
        Task taskObj = tasksMockData.getDummyTask();
        EndpointSpec endpointSpec = endpointMockData.dockerEndpointMockData();
        faultSpec.setEndpoint(endpointSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Task<TaskSpec>> responseEntity = faultInjectionController.injectThreadLeakFault(faultSpec);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) responseEntity.getBody().getTaskData()).getFaultName(),
                AgentFaultName.INJECT_THREAD_LEAK_FAULT.toString());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testInjectDiskSpaceFault() throws MangleException {
        DiskSpaceSpec faultSpec = faultsMockData.getDiskSpaceSpec();
        TasksMockData<?> tasksMockData = new TasksMockData<>(faultSpec);
        Task taskObj = tasksMockData.getDummyTask();
        RemoteMachineCredentials credentialsSpec = credentialsMockData.getRMCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.rmEndpointMockData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);
        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        doNothing().when(faultInjectionHelper).validateEndpointTypeSpecificArguments(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Task<TaskSpec>> responseEntity = faultInjectionController.injectDiskSpaceFault(faultSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) responseEntity.getBody().getTaskData()).getFaultName(),
                AgentFaultName.INJECT_DISK_SPACE_FAULT.toString());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @Test
    public void testInjectionAwsEc2StateFault() throws MangleException {
        AwsEC2InstanceStateFaultSpec faultSpec = faultsMockData.getAwsEC2InstanceStateFaultSpec();
        TasksMockData tasksMockData = new TasksMockData(faultSpec);
        EndpointSpec endpointSpec = endpointMockData.awsEndpointSpecMock();
        CredentialsSpec credentialsSpec = credentialsMockData.getAWSCredentialsData();
        Task taskObj = tasksMockData.getDummyTask();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Task<TaskSpec>> responseEntity =
                faultInjectionController.injectAwsEC2InstanceStateFault(faultSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) responseEntity.getBody().getTaskData()).getFaultName(),
                AwsEC2StateFaults.STOP_INSTANCES.name());
    }

    @Test
    public void testInjectionAwsEc2BlockAllNetworkFault() throws MangleException {
        AwsEC2NetworkFaultSpec faultSpec = faultsMockData.getAwsEC2BlockAllNetworkFaultSpec();
        TasksMockData tasksMockData = new TasksMockData(faultSpec);
        EndpointSpec endpointSpec = endpointMockData.awsEndpointSpecMock();
        CredentialsSpec credentialsSpec = credentialsMockData.getAWSCredentialsData();
        Task taskObj = tasksMockData.getDummyTask();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Task<TaskSpec>> responseEntity =
                faultInjectionController.injectAwsEC2InstanceBlockAllNetworkFault(faultSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) responseEntity.getBody().getTaskData()).getFaultName(),
                AwsEC2NetworkFaults.BLOCK_ALL_NETWORK_TRAFFIC.name());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testInjectionAwsEc2StateFaultFailure() throws MangleException {
        AwsEC2InstanceStateFaultSpec faultSpec = faultsMockData.getAwsEC2InstanceStateFaultSpec();
        TasksMockData tasksMockData = new TasksMockData(faultSpec);
        Task taskObj = tasksMockData.getDummyTask();

        EndpointSpec endpointSpec = endpointMockData.awsEndpointSpecMock();
        CredentialsSpec credentialsSpec = credentialsMockData.getAWSCredentialsData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        doThrow(new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.ENDPOINT_NAME))
                .when(faultInjectionHelper).validateSpec(faultSpec);
        try {
            faultInjectionController.injectAwsEC2InstanceStateFault(faultSpec);
        } catch (MangleException e) {
            Assert.assertEquals(ErrorCode.FIELD_VALUE_EMPTY, e.getErrorCode());
            throw e;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testInjectKernelPanicFault() throws MangleException {
        KernelPanicSpec faultSpec = faultsMockData.getKernelPanicSpec();
        TasksMockData<?> tasksMockData = new TasksMockData<>(faultSpec);
        Task taskObj = tasksMockData.getDummyTask();
        RemoteMachineCredentials credentialsSpec = credentialsMockData.getRMCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.rmEndpointMockData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);
        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        doNothing().when(faultInjectionHelper).validateEndpointTypeSpecificArguments(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Task<TaskSpec>> responseEntity = faultInjectionController.injectKernelPanicFault(faultSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) responseEntity.getBody().getTaskData()).getFaultName(),
                AgentFaultName.KERNEL_PANIC_FAULT.toString());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }
}
