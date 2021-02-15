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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import static com.vmware.mangle.services.constants.CommonConstants.DEFAULT_BLOCK_SIZE;
import static com.vmware.mangle.services.constants.CommonConstants.IO_SIZE_ARG;

import java.util.Collection;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.CredentialsSpec;
import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.endpoint.RemoteMachineCredentials;
import com.vmware.mangle.cassandra.model.faults.specs.ClockSkewSpec;
import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.CpuFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DbFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DbTransactionErrorFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DbTransactionLatencyFaultSpec;
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
import com.vmware.mangle.cassandra.model.faults.specs.NetworkPartitionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.faults.specs.ThreadLeakFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMDiskFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMNicFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMStateFaultSpec;
import com.vmware.mangle.cassandra.model.redis.faults.specs.RedisDelayFaultSpec;
import com.vmware.mangle.cassandra.model.redis.faults.specs.RedisDropConnectionFaultSpec;
import com.vmware.mangle.cassandra.model.redis.faults.specs.RedisFaultSpec;
import com.vmware.mangle.cassandra.model.redis.faults.specs.RedisReturnErrorFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.model.aws.AwsEC2NetworkFaults;
import com.vmware.mangle.model.aws.AwsEC2StateFaults;
import com.vmware.mangle.model.aws.AwsEC2StorageFaults;
import com.vmware.mangle.model.aws.AwsRDSFaults;
import com.vmware.mangle.model.aws.faults.spec.AwsEC2InstanceStateFaultSpec;
import com.vmware.mangle.model.aws.faults.spec.AwsEC2NetworkFaultSpec;
import com.vmware.mangle.model.aws.faults.spec.AwsEC2StorageFaultSpec;
import com.vmware.mangle.model.aws.faults.spec.AwsRDSFaultSpec;
import com.vmware.mangle.model.azure.AzureNetworkFaults;
import com.vmware.mangle.model.azure.AzureStorageFaults;
import com.vmware.mangle.model.azure.AzureVMStateFaults;
import com.vmware.mangle.model.azure.faults.spec.AzureVMNetworkFaultSpec;
import com.vmware.mangle.model.azure.faults.spec.AzureVMStateFaultSpec;
import com.vmware.mangle.model.azure.faults.spec.AzureVMStorageFaultSpec;
import com.vmware.mangle.model.enums.DatabaseType;
import com.vmware.mangle.services.controller.FaultInjectionController;
import com.vmware.mangle.services.enums.AgentFaultName;
import com.vmware.mangle.services.enums.DockerFaultName;
import com.vmware.mangle.services.enums.K8SFaultName;
import com.vmware.mangle.services.enums.RedisFaultName;
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

    private FaultInjectionController faultInjectionController;

    private EndpointMockData endpointMockData = new EndpointMockData();

    private CredentialsSpecMockData credentialsMockData = new CredentialsSpecMockData();

    private FaultsMockData faultsMockData = new FaultsMockData();

    @BeforeMethod
    public void initMocks() {
        Link link = mock(Link.class);
        MockitoAnnotations.initMocks(this);
        faultInjectionController = spy(new FaultInjectionController(faultInjectionHelper));
        doReturn(link).when(faultInjectionController).getSelfLink();
    }

    @Test
    public void testInjectionCPUFault() throws MangleException {
        CpuFaultSpec faultSpec = faultsMockData.getK8SCPUFaultSpec();
        TasksMockData<TaskSpec> tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();
        EndpointSpec endpointSpec = endpointMockData.k8sEndpointMockData();
        CredentialsSpec credentialsSpec = credentialsMockData.getk8SCredentialsData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);
        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity = faultInjectionController.injectCPUFault(faultSpec);

        Resource<Task<TaskSpec>> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Task<TaskSpec> task = resource.getContent();

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getFaultName(),
                AgentFaultName.INJECT_CPU_FAULT.toString());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @Test
    public void testk8SDeleteResourceFault() throws MangleException {
        K8SDeleteResourceFaultSpec faultSpec = faultsMockData.getDeleteK8SResourceFaultSpec();
        TasksMockData<TaskSpec> tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();
        EndpointSpec endpointSpec = endpointMockData.k8sEndpointMockData();
        CredentialsSpec credentialsSpec = credentialsMockData.getk8SCredentialsData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.deleteK8SResourceFault(faultSpec);

        Resource<Task<TaskSpec>> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Task<TaskSpec> task = resource.getContent();

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getFaultName(),
                K8SFaultName.DELETE_RESOURCE.name());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @Test
    public void testk8SResourceNotReadyFault() throws MangleException {
        K8SResourceNotReadyFaultSpec faultSpec = faultsMockData.getK8SResourceNotReadyFaultSpec();
        TasksMockData<TaskSpec> tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();
        EndpointSpec endpointSpec = endpointMockData.k8sEndpointMockData();
        CredentialsSpec credentialsSpec = credentialsMockData.getk8SCredentialsData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.k8SResourceNotReadyFault(faultSpec);

        Resource<Task<TaskSpec>> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Task<TaskSpec> task = resource.getContent();

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getFaultName(),
                K8SFaultName.NOTREADY_RESOURCE.name());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @Test
    public void testk8SServiceUnavailableFault() throws MangleException {
        K8SServiceUnavailableFaultSpec faultSpec = faultsMockData.getK8SServiceUnavailableFaultSpec();
        TasksMockData<TaskSpec> tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();
        EndpointSpec endpointSpec = endpointMockData.k8sEndpointMockData();
        CredentialsSpec credentialsSpec = credentialsMockData.getk8SCredentialsData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.k8SServiceUnavailableyFault(faultSpec);

        Resource<Task<TaskSpec>> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Task<TaskSpec> task = resource.getContent();

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getFaultName(),
                K8SFaultName.SERVICE_UNAVAILABLE.name());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @Test
    public void testInjectionDockerPause() throws MangleException {
        DockerFaultSpec faultSpec = faultsMockData.getDockerPauseFaultSpec();

        TasksMockData<TaskSpec> tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();
        EndpointSpec endpointSpec = endpointMockData.dockerEndpointMockData();
        faultSpec.setEndpoint(endpointSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity = faultInjectionController.injectDockerFault(faultSpec);

        Resource<Task<TaskSpec>> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Task<TaskSpec> task = resource.getContent();

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getFaultName(),
                DockerFaultName.DOCKER_PAUSE.name());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }


    @Test
    public void testInjectionDockerStop() throws MangleException {
        DockerFaultSpec faultSpec = faultsMockData.getDockerStopFaultSpec();

        TasksMockData<TaskSpec> tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();
        EndpointSpec endpointSpec = endpointMockData.dockerEndpointMockData();
        faultSpec.setEndpoint(endpointSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity = faultInjectionController.injectDockerFault(faultSpec);

        Resource<Task<TaskSpec>> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Task<TaskSpec> task = resource.getContent();

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getFaultName(),
                DockerFaultName.DOCKER_STOP.name());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testInjectionDockerPauseFaultFailure() throws MangleException {
        DockerFaultSpec faultSpec = faultsMockData.getDockerPauseFaultSpec();
        EndpointSpec endpointSpec = endpointMockData.dockerEndpointMockData();
        faultSpec.setEndpoint(endpointSpec);
        doThrow(new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.ENDPOINT_NAME))
                .when(faultInjectionHelper).getTask(any());
        try {
            faultInjectionController.injectDockerFault(faultSpec);
        } catch (MangleException e) {
            Assert.assertEquals(ErrorCode.FIELD_VALUE_EMPTY, e.getErrorCode());
            verify(faultInjectionHelper, times(1)).getTask(any());
            throw e;
        }
    }

    @Test
    public void testInjectionVCenterNicFault() throws MangleException {
        VMNicFaultSpec faultSpec = faultsMockData.getVMNicFaultSpec();
        TasksMockData<TaskSpec> tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();
        EndpointSpec endpointSpec = endpointMockData.getVCenterEndpointSpecMock();
        CredentialsSpec credentialsSpec = credentialsMockData.getVCenterCredentialsData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.injectVCenterNicFault(faultSpec);

        Resource<Task<TaskSpec>> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Task<TaskSpec> task = resource.getContent();

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getFaultName(),
                VCenterNicFaults.DISCONNECT_NIC.name());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @Test
    public void testInjectionVCenterDiskFault() throws MangleException {
        VMDiskFaultSpec faultSpec = faultsMockData.getVMDiskFaultSpec();
        TasksMockData<TaskSpec> tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();
        EndpointSpec endpointSpec = endpointMockData.getVCenterEndpointSpecMock();
        CredentialsSpec credentialsSpec = credentialsMockData.getVCenterCredentialsData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.injectVCenterDiskFault(faultSpec);

        Resource<Task<TaskSpec>> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Task<TaskSpec> task = resource.getContent();

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getFaultName(),
                VCenterDiskFaults.DISCONNECT_DISK.name());
    }

    @Test
    public void testInjectionVCenterStateFault() throws MangleException {
        VMStateFaultSpec faultSpec = faultsMockData.getVMStateFaultSpec();
        TasksMockData<TaskSpec> tasksMockData = new TasksMockData<>(faultSpec);
        EndpointSpec endpointSpec = endpointMockData.getVCenterEndpointSpecMock();
        CredentialsSpec credentialsSpec = credentialsMockData.getVCenterCredentialsData();
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.injectVCenterStateFault(faultSpec);

        Resource<Task<TaskSpec>> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Task<TaskSpec> task = resource.getContent();

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getFaultName(),
                VCenterStateFaults.POWEROFF_VM.name());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testInjectionVCenterNicFaultFailure() throws MangleException {
        VMNicFaultSpec faultSpec = faultsMockData.getVMNicFaultSpec();
        EndpointSpec endpointSpec = endpointMockData.getVCenterEndpointSpecMock();
        CredentialsSpec credentialsSpec = credentialsMockData.getVCenterCredentialsData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);
        doThrow(new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.ENDPOINT_NAME))
                .when(faultInjectionHelper).getTask(any());
        try {
            faultInjectionController.injectVCenterNicFault(faultSpec);
        } catch (MangleException e) {
            Assert.assertEquals(ErrorCode.FIELD_VALUE_EMPTY, e.getErrorCode());
            verify(faultInjectionHelper, times(1)).getTask(any());
            throw e;
        }
    }

    @Test
    public void testRemediationTask() throws MangleException {
        VMStateFaultSpec faultSpec = faultsMockData.getVMStateFaultSpec();
        TasksMockData<TaskSpec> tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();
        when(faultInjectionHelper.triggerRemediation(taskObj.getId())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.remediateFault(taskObj.getId());

        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);

    }

    @Test(expectedExceptions = MangleException.class)
    public void testInjectionVCenterDiskFaultFailure() throws MangleException {
        VMDiskFaultSpec faultSpec = faultsMockData.getVMDiskFaultSpec();
        EndpointSpec endpointSpec = endpointMockData.getVCenterEndpointSpecMock();
        CredentialsSpec credentialsSpec = credentialsMockData.getVCenterCredentialsData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);
        doThrow(new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.ENDPOINT_NAME))
                .when(faultInjectionHelper).getTask(any());
        try {
            faultInjectionController.injectVCenterDiskFault(faultSpec);
        } catch (MangleException e) {
            Assert.assertEquals(ErrorCode.FIELD_VALUE_EMPTY, e.getErrorCode());
            verify(faultInjectionHelper, times(1)).getTask(any());
            throw e;
        }
    }

    @Test(expectedExceptions = MangleException.class)
    public void testInjectionVCenterStateFaultFailure() throws MangleException {
        VMStateFaultSpec faultSpec = faultsMockData.getVMStateFaultSpec();
        TasksMockData<TaskSpec> tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();

        EndpointSpec endpointSpec = endpointMockData.getVCenterEndpointSpecMock();
        CredentialsSpec credentialsSpec = credentialsMockData.getVCenterCredentialsData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        doThrow(new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.ENDPOINT_NAME))
                .when(faultInjectionHelper).getTask(any());
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
        Task taskObj = tasksMockData.getDummy1Task();
        RemoteMachineCredentials credentialsSpec = credentialsMockData.getRMCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.rmEndpointMockData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);
        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.injectKillProcessFault(faultSpec);

        Resource<Task<TaskSpec>> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Task<TaskSpec> task = resource.getContent();

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getFaultName(),
                AgentFaultName.INJECT_KILL_PROCESS_FAULT.toString());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testInjectMemoryFault() throws MangleException {
        MemoryFaultSpec faultSpec = faultsMockData.getMemoryFaultSpecOfRemoteMachine();
        TasksMockData tasksMockData = new TasksMockData(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
        RemoteMachineCredentials credentialsSpec = credentialsMockData.getRMCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.rmEndpointMockData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity = faultInjectionController.injectMemoryFault(faultSpec);

        Resource<Task<TaskSpec>> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Task<TaskSpec> task = resource.getContent();

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getFaultName(),
                AgentFaultName.INJECT_MEMORY_FAULT.toString());
        verify(faultInjectionHelper, times(1)).getTask(any());

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testInjectDiskIOFault() throws MangleException {
        DiskIOFaultSpec faultSpec = faultsMockData.getDiskIOFaultSpecOfRemoteMachine();
        TasksMockData tasksMockData = new TasksMockData(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
        RemoteMachineCredentials credentialsSpec = credentialsMockData.getRMCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.rmEndpointMockData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity = faultInjectionController.injectDiskIOFault(faultSpec);

        Resource<Task<TaskSpec>> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Task<TaskSpec> task = resource.getContent();

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getFaultName(),
                AgentFaultName.INJECT_DISK_IO_FAULT.toString());
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getArgs().get(IO_SIZE_ARG),
                DEFAULT_BLOCK_SIZE);
        verify(faultInjectionHelper, times(1)).getTask(any());

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testInjectFileHandlerLeakFault() throws MangleException {
        FilehandlerLeakFaultSpec faultSpec = faultsMockData.getFileHandlerLeakFaultSpecOfDockerEndpoint();
        TasksMockData tasksMockData = new TasksMockData(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
        EndpointSpec endpointSpec = endpointMockData.dockerEndpointMockData();
        faultSpec.setEndpoint(endpointSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.injectfileHandlerLeakFault(faultSpec);

        Resource<Task<TaskSpec>> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Task<TaskSpec> task = resource.getContent();

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getFaultName(),
                AgentFaultName.INJECT_FILE_HANDLER_FAULT.toString());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testInjectThreadLeakFault() throws MangleException {
        ThreadLeakFaultSpec faultSpec = faultsMockData.getThreadLeakFaultSpecOfDockerEndpoint();
        TasksMockData tasksMockData = new TasksMockData(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
        EndpointSpec endpointSpec = endpointMockData.dockerEndpointMockData();
        faultSpec.setEndpoint(endpointSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.injectThreadLeakFault(faultSpec);
        Resource<Task<TaskSpec>> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Task<TaskSpec> task = resource.getContent();

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getFaultName(),
                AgentFaultName.INJECT_THREAD_LEAK_FAULT.toString());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testInjectDiskSpaceFault() throws MangleException {
        DiskSpaceSpec faultSpec = faultsMockData.getDiskSpaceSpec();
        TasksMockData<?> tasksMockData = new TasksMockData<>(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
        RemoteMachineCredentials credentialsSpec = credentialsMockData.getRMCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.rmEndpointMockData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);
        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        doNothing().when(faultInjectionHelper).validateEndpointTypeSpecificArguments(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.injectDiskSpaceFault(faultSpec);

        Resource<Task<TaskSpec>> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Task<TaskSpec> task = resource.getContent();

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getFaultName(),
                AgentFaultName.INJECT_DISK_SPACE_FAULT.toString());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @Test
    public void testInjectionAwsEc2StateFault() throws MangleException {
        AwsEC2InstanceStateFaultSpec faultSpec = faultsMockData.getAwsEC2InstanceStateFaultSpec();
        TasksMockData<TaskSpec> tasksMockData = new TasksMockData<>(faultSpec);
        EndpointSpec endpointSpec = endpointMockData.awsEndpointSpecMock();
        CredentialsSpec credentialsSpec = credentialsMockData.getAWSCredentialsData();
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.injectAwsEC2InstanceStateFault(faultSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) taskObj.getTaskData()).getFaultName(),
                AwsEC2StateFaults.STOP_INSTANCES.name());
    }

    @Test
    public void testInjectionAwsEc2BlockAllNetworkFault() throws MangleException {
        AwsEC2NetworkFaultSpec faultSpec = faultsMockData.getAwsEC2BlockAllNetworkFaultSpec();
        TasksMockData<TaskSpec> tasksMockData = new TasksMockData<>(faultSpec);
        EndpointSpec endpointSpec = endpointMockData.awsEndpointSpecMock();
        CredentialsSpec credentialsSpec = credentialsMockData.getAWSCredentialsData();
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.injectAwsEC2InstanceBlockAllNetworkFault(faultSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) taskObj.getTaskData()).getFaultName(),
                AwsEC2NetworkFaults.BLOCK_ALL_NETWORK_TRAFFIC.name());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testInjectionAwsEc2StateFaultFailure() throws MangleException {
        AwsEC2InstanceStateFaultSpec faultSpec = faultsMockData.getAwsEC2InstanceStateFaultSpec();
        TasksMockData<TaskSpec> tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();

        EndpointSpec endpointSpec = endpointMockData.awsEndpointSpecMock();
        CredentialsSpec credentialsSpec = credentialsMockData.getAWSCredentialsData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        doThrow(new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.ENDPOINT_NAME))
                .when(faultInjectionHelper).getTask(any());
        try {
            faultInjectionController.injectAwsEC2InstanceStateFault(faultSpec);
        } catch (MangleException e) {
            Assert.assertEquals(ErrorCode.FIELD_VALUE_EMPTY, e.getErrorCode());
            throw e;
        }
    }

    @Test
    public void testInjectionAwsEC2DetachVolumesFault() throws MangleException {
        AwsEC2StorageFaultSpec faultSpec = faultsMockData.getAwsEC2DetachVolumesFaultSpec();
        TasksMockData<TaskSpec> tasksMockData = new TasksMockData<>(faultSpec);
        EndpointSpec endpointSpec = endpointMockData.awsEndpointSpecMock();
        CredentialsSpec credentialsSpec = credentialsMockData.getAWSCredentialsData();
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.injectAwsEC2InstanceDetachVolumes(faultSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) taskObj.getTaskData()).getFaultName(),
                AwsEC2StorageFaults.DETACH_VOLUMES.name());
    }

    @Test
    public void testInjectionAwsRDSFault() throws MangleException {
        AwsRDSFaultSpec faultSpec = faultsMockData.getAwsRDSFaultSpec();
        TasksMockData<TaskSpec> tasksMockData = new TasksMockData<>(faultSpec);
        EndpointSpec endpointSpec = endpointMockData.awsEndpointSpecMock();
        CredentialsSpec credentialsSpec = credentialsMockData.getAWSCredentialsData();
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.injectAwsRDSFaults(faultSpec);

        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(((CommandExecutionFaultSpec) taskObj.getTaskData()).getFaultName(),
                AwsRDSFaults.STOP_INSTANCES.name());
    }

    @Test
    public void testInjectionAzureVMStateFault() throws MangleException {
        AzureVMStateFaultSpec faultSpec = faultsMockData.getAzureVMStateFaultSpec();
        TasksMockData<TaskSpec> tasksMockData = new TasksMockData<>(faultSpec);
        EndpointSpec endpointSpec = endpointMockData.azureEndpointMockData();
        CredentialsSpec credentialsSpec = credentialsMockData.getAzureCredentialsData();
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.injectAzureVMStateFault(faultSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) taskObj.getTaskData()).getFaultName(),
                AzureVMStateFaults.STOP_VMS.name());
    }

    @Test
    public void testInjectionAzureVMNetworkBlockFault() throws MangleException {
        AzureVMNetworkFaultSpec faultSpec = faultsMockData.getAzureVMNetworkBlockFaultSpec();
        TasksMockData<TaskSpec> tasksMockData = new TasksMockData<>(faultSpec);
        EndpointSpec endpointSpec = endpointMockData.azureEndpointMockData();
        CredentialsSpec credentialsSpec = credentialsMockData.getAzureCredentialsData();
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.injectAzureVMNetworkFault(faultSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) taskObj.getTaskData()).getFaultName(),
                AzureNetworkFaults.BLOCK_ALL_VM_NETWORK_TRAFFIC.name());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testInjectionAzureVMStateFaultFailure() throws MangleException {
        AzureVMStateFaultSpec faultSpec = faultsMockData.getAzureVMStateFaultSpec();
        TasksMockData<TaskSpec> tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();

        EndpointSpec endpointSpec = endpointMockData.azureEndpointMockData();
        CredentialsSpec credentialsSpec = credentialsMockData.getAzureCredentialsData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        doThrow(new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.ENDPOINT_NAME))
                .when(faultInjectionHelper).getTask(any());
        try {
            faultInjectionController.injectAzureVMStateFault(faultSpec);
        } catch (MangleException e) {
            Assert.assertEquals(ErrorCode.FIELD_VALUE_EMPTY, e.getErrorCode());
            throw e;
        }
    }

    @Test
    public void testInjectionAzureVMDetachDisksFault() throws MangleException {
        AzureVMStorageFaultSpec faultSpec = faultsMockData.getAzureVMDetachDisksFaultSpec();
        TasksMockData<TaskSpec> tasksMockData = new TasksMockData<>(faultSpec);
        EndpointSpec endpointSpec = endpointMockData.azureEndpointMockData();
        CredentialsSpec credentialsSpec = credentialsMockData.getAzureCredentialsData();
        Task<TaskSpec> taskObj = tasksMockData.getDummy1Task();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);

        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.injectAzureVMDetachDisk(faultSpec);

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) taskObj.getTaskData()).getFaultName(),
                AzureStorageFaults.DETACH_DISKS.name());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testInjectKernelPanicFault() throws MangleException {
        KernelPanicSpec faultSpec = faultsMockData.getKernelPanicSpec();
        TasksMockData<?> tasksMockData = new TasksMockData<>(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
        RemoteMachineCredentials credentialsSpec = credentialsMockData.getRMCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.rmEndpointMockData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);
        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        doNothing().when(faultInjectionHelper).validateEndpointTypeSpecificArguments(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.injectKernelPanicFault(faultSpec);

        Resource<Task<TaskSpec>> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Task<TaskSpec> task = resource.getContent();

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getFaultName(),
                AgentFaultName.KERNEL_PANIC_FAULT.toString());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }


    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testInjectDbConnectionLeakFault() throws MangleException {
        DbFaultSpec faultSpec = faultsMockData.getDbConnectionLeakFaultSpecOfRemoteMachine();
        TasksMockData<?> tasksMockData = new TasksMockData<>(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        doNothing().when(faultInjectionHelper).validateEndpointTypeSpecificArguments(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.injectDbConnectionLeakFault(faultSpec);

        Resource<Task<TaskSpec>> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Task<TaskSpec> task = resource.getContent();

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getFaultName(),
                AgentFaultName.INJECT_DB_CONNECTION_LEAK_FAULT.toString() + "_"
                        + DatabaseType.POSTGRES.toString().toLowerCase());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testInjectDbTransactionErrorFault() throws MangleException {
        DbTransactionErrorFaultSpec faultSpec = faultsMockData.getDbTransactionErrorFaultSpecOfRemoteMachine();
        TasksMockData<?> tasksMockData = new TasksMockData<>(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        doNothing().when(faultInjectionHelper).validateEndpointTypeSpecificArguments(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.injectDbTransactionErrorFault(faultSpec);

        Resource<Task<TaskSpec>> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Task<TaskSpec> task = resource.getContent();

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getFaultName(),
                AgentFaultName.INJECT_DB_TRANSACTION_ERROR_FAULT.toString() + "_"
                        + DatabaseType.POSTGRES.toString().toLowerCase());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testInjectDbTransactionErrorFaultForMangleException() {
        DbTransactionErrorFaultSpec faultSpec = faultsMockData.getDbTransactionErrorFaultSpecOfRemoteMachine();
        faultSpec.setErrorCode("test_transaction");
        TasksMockData<?> tasksMockData = new TasksMockData<>(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
        try {
            doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
            doNothing().when(faultInjectionHelper).validateEndpointTypeSpecificArguments(faultSpec);
            when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);
            faultInjectionController.injectDbTransactionErrorFault(faultSpec);
        } catch (MangleException e) {
            assertEquals(e.getErrorCode(), ErrorCode.GENERIC_ERROR);
        }
    }

    @Test
    public void testGetDbTransactionErrorCodes() {
        ResponseEntity<Resources<String>> responseEntity =
                faultInjectionController.getDbTransactionErrorCodes(DatabaseType.POSTGRES);
        Resources<String> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Collection<String> errorCodes = resource.getContent();
        assertTrue(!errorCodes.isEmpty());
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testInjectClockSkewFault() throws MangleException {
        ClockSkewSpec faultSpec = faultsMockData.getClockSkewSpec();
        TasksMockData<?> tasksMockData = new TasksMockData<>(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
        RemoteMachineCredentials credentialsSpec = credentialsMockData.getRMCredentialsData();
        EndpointSpec endpointSpec = endpointMockData.rmEndpointMockData();
        faultSpec.setEndpoint(endpointSpec);
        faultSpec.setCredentials(credentialsSpec);
        doNothing().when(faultInjectionHelper).validateSpec(faultSpec);
        doNothing().when(faultInjectionHelper).validateEndpointTypeSpecificArguments(faultSpec);
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.injectClockSkewFault(faultSpec);

        Resource<Task<TaskSpec>> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Task<TaskSpec> task = resource.getContent();

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getFaultName(),
                AgentFaultName.INJECT_CLOCK_SKEW.toString());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testInjectDbTransactionLatencyFault() throws MangleException {
        DbTransactionLatencyFaultSpec faultSpec = faultsMockData.getDbTransactionLatencyFaultSpecOfRemoteMachine();
        TasksMockData<?> tasksMockData = new TasksMockData<>(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.injectDbTransactionLatencyFault(faultSpec);

        Resource<Task<TaskSpec>> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Task<TaskSpec> task = resource.getContent();

        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getFaultName(),
                AgentFaultName.INJECT_DB_TRANSACTION_LATENCY_FAULT.toString() + "_"
                        + DatabaseType.POSTGRES.toString().toLowerCase());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testInjectRedisDelayFault() throws MangleException {
        RedisDelayFaultSpec faultSpec = faultsMockData.getRedisDelayFaultSpec();
        TasksMockData<?> tasksMockData = new TasksMockData<>(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.injectRedisDelayFault(faultSpec);

        Resource<Task<TaskSpec>> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Task<TaskSpec> task = resource.getContent();

        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getFaultName(),
                RedisFaultName.REDISDBDELAYFAULT.toString());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testInjectRedisReturnErrorFault() throws MangleException {
        RedisReturnErrorFaultSpec faultSpec = faultsMockData.getRedisReturnErrorFaultSpec();
        TasksMockData<?> tasksMockData = new TasksMockData<>(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.injectRedisReturnErrorFault(faultSpec);

        Resource<Task<TaskSpec>> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Task<TaskSpec> task = resource.getContent();

        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getFaultName(),
                RedisFaultName.REDISDBRETURNERRORFAULT.toString());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testInjectRedisDropConnectionFault() throws MangleException {
        RedisDropConnectionFaultSpec faultSpec = faultsMockData.getRedisDropConnectionFaultSpec();
        TasksMockData<?> tasksMockData = new TasksMockData<>(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.injectRedisDropConnectionFault(faultSpec);

        Resource<Task<TaskSpec>> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Task<TaskSpec> task = resource.getContent();

        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getFaultName(),
                RedisFaultName.REDISDBDROPCONNECTIONFAULT.toString());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testInjectRedisReturnEmptyFault() throws MangleException {
        RedisFaultSpec faultSpec = faultsMockData.getRedisFaultSpec();
        TasksMockData<?> tasksMockData = new TasksMockData<>(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.injectRedisReturnEmptyFault(faultSpec);

        Resource<Task<TaskSpec>> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Task<TaskSpec> task = resource.getContent();

        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getFaultName(),
                RedisFaultName.REDISDBRETURNEMPTYFAULT.toString());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testInjectNetworkPartitionFault() throws MangleException {
        NetworkPartitionFaultSpec faultSpec = faultsMockData.getNetworkPartitionFaultSpecOfRemoteMachine();
        TasksMockData<?> tasksMockData = new TasksMockData<>(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
        when(faultInjectionHelper.getTask(any())).thenReturn(taskObj);

        ResponseEntity<Resource<Task<TaskSpec>>> responseEntity =
                faultInjectionController.injectNetworkPartitionFault(faultSpec);

        Resource<Task<TaskSpec>> resource = responseEntity.getBody();
        Assert.assertNotNull(resource);
        Task<TaskSpec> task = resource.getContent();

        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(((CommandExecutionFaultSpec) task.getTaskData()).getFaultName(),
                AgentFaultName.INJECT_NETWORK_PARTITION_FAULT.toString());
        verify(faultInjectionHelper, times(1)).getTask(any());
    }
}
