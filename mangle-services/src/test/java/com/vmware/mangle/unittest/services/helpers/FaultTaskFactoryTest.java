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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;

import java.util.Arrays;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.CpuFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DockerFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SResourceNotReadyFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMDiskFaultSpec;
import com.vmware.mangle.cassandra.model.redis.faults.specs.RedisDelayFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.model.aws.faults.spec.AwsEC2InstanceStateFaultSpec;
import com.vmware.mangle.model.aws.faults.spec.AwsRDSFaultSpec;
import com.vmware.mangle.model.azure.faults.spec.AzureVMNetworkFaultSpec;
import com.vmware.mangle.services.PluginService;
import com.vmware.mangle.services.helpers.FaultTaskFactory;
import com.vmware.mangle.services.mockdata.FaultsMockData;
import com.vmware.mangle.services.mockdata.TasksMockData;
import com.vmware.mangle.task.framework.helpers.AbstractTaskHelper;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Unit test cases for {@link FaultTaskFactory}
 *
 * @author kumargautam
 */
public class FaultTaskFactoryTest {

    @Mock
    private PluginService pluginService;
    @Mock
    private AbstractTaskHelper<TaskSpec> taskHelper;
    @InjectMocks
    private FaultTaskFactory faultTaskFactory;
    private FaultsMockData faultsMockData = new FaultsMockData();

    @BeforeMethod
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.helpers.FaultTaskFactory#getTask(com.vmware.mangle.cassandra.model.faults.specs.FaultSpec)}.
     *
     * @throws MangleException
     */
    @SuppressWarnings({ "rawtypes" })
    @Test
    public void testGetTaskFaultSpec() throws MangleException {
        RedisDelayFaultSpec faultSpec = faultsMockData.getRedisDelayFaultSpec();
        TasksMockData<?> tasksMockData = new TasksMockData<>(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
        mockGetTaskTest(taskObj);
        assertNotNull(faultTaskFactory.getTask(faultSpec));
        verifyGetTaskTest();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.helpers.FaultTaskFactory#getTask(com.vmware.mangle.cassandra.model.faults.specs.FaultSpec)}.
     *
     * @throws MangleException
     */
    @SuppressWarnings({ "rawtypes" })
    @Test
    public void testGetTaskFaultForK8SFaultSpec() throws MangleException {
        K8SResourceNotReadyFaultSpec faultSpec = faultsMockData.getK8SResourceNotReadyFaultSpec();
        TasksMockData<?> tasksMockData = new TasksMockData<>(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
        mockGetTaskTest(taskObj);
        assertNotNull(faultTaskFactory.getTask(faultSpec));
        verifyGetTaskTest();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.helpers.FaultTaskFactory#getTask(com.vmware.mangle.cassandra.model.faults.specs.FaultSpec)}.
     *
     * @throws MangleException
     */
    @SuppressWarnings({ "rawtypes" })
    @Test
    public void testGetTaskFaultForDockerFaultSpec() throws MangleException {
        DockerFaultSpec faultSpec = faultsMockData.getDockerPauseFaultSpec();
        TasksMockData<?> tasksMockData = new TasksMockData<>(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
        mockGetTaskTest(taskObj);
        assertNotNull(faultTaskFactory.getTask(faultSpec));
        verifyGetTaskTest();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.helpers.FaultTaskFactory#getTask(com.vmware.mangle.cassandra.model.faults.specs.FaultSpec)}.
     *
     * @throws MangleException
     */
    @SuppressWarnings({ "rawtypes" })
    @Test
    public void testGetTaskFaultForVMFaultSpec() throws MangleException {
        VMDiskFaultSpec faultSpec = faultsMockData.getVMDiskFaultSpec();
        TasksMockData<?> tasksMockData = new TasksMockData<>(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
        mockGetTaskTest(taskObj);
        assertNotNull(faultTaskFactory.getTask(faultSpec));
        verifyGetTaskTest();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.helpers.FaultTaskFactory#getTask(com.vmware.mangle.cassandra.model.faults.specs.FaultSpec)}.
     *
     * @throws MangleException
     */
    @SuppressWarnings({ "rawtypes" })
    @Test
    public void testGetTaskFaultForAwsEC2FaultSpec() throws MangleException {
        AwsEC2InstanceStateFaultSpec faultSpec = faultsMockData.getAwsEC2InstanceStateFaultSpec();
        TasksMockData<?> tasksMockData = new TasksMockData<>(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
        mockGetTaskTest(taskObj);
        assertNotNull(faultTaskFactory.getTask(faultSpec));
        verifyGetTaskTest();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.helpers.FaultTaskFactory#getTask(com.vmware.mangle.cassandra.model.faults.specs.FaultSpec)}.
     *
     * @throws MangleException
     */
    @SuppressWarnings({ "rawtypes" })
    @Test
    public void testGetTaskFaultForAwsRDSFaultSpec() throws MangleException {
        AwsRDSFaultSpec faultSpec = faultsMockData.getAwsRDSFaultSpec();
        TasksMockData<?> tasksMockData = new TasksMockData<>(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
        mockGetTaskTest(taskObj);
        assertNotNull(faultTaskFactory.getTask(faultSpec));
        verifyGetTaskTest();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.helpers.FaultTaskFactory#getTask(com.vmware.mangle.cassandra.model.faults.specs.FaultSpec)}.
     *
     * @throws MangleException
     */
    @SuppressWarnings({ "rawtypes" })
    @Test
    public void testGetTaskFaultForAzureVMFaultSpec() throws MangleException {
        AzureVMNetworkFaultSpec faultSpec = faultsMockData.getAzureVMNetworkBlockFaultSpec();
        TasksMockData<?> tasksMockData = new TasksMockData<>(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
        mockGetTaskTest(taskObj);
        assertNotNull(faultTaskFactory.getTask(faultSpec));
        verifyGetTaskTest();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.helpers.FaultTaskFactory#getTask(com.vmware.mangle.cassandra.model.faults.specs.FaultSpec)}.
     *
     * @throws MangleException
     */
    @SuppressWarnings({ "rawtypes" })
    @Test
    public void testGetTaskFaultForK8SFaultTrigger() throws MangleException {
        CpuFaultSpec faultSpec = faultsMockData.getK8SCPUFaultSpec();
        TasksMockData<?> tasksMockData = new TasksMockData<>(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
        mockGetTaskTest(taskObj);
        assertNotNull(faultTaskFactory.getTask(faultSpec));
        verifyGetTaskTest();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.helpers.FaultTaskFactory#getRemediationTask(Task, String)}.
     *
     * @throws MangleException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testGetRemediationTask() throws MangleException {
        CpuFaultSpec faultSpec = faultsMockData.getK8SCPUFaultSpec();
        TasksMockData<?> tasksMockData = new TasksMockData<>(faultSpec);
        Task taskObj = tasksMockData.getDummy1Task();
        ((TaskTrigger) taskObj.getTriggers().peek()).setTaskStatus(TaskStatus.COMPLETED);
        ((CommandExecutionFaultSpec) taskObj.getTaskData())
                .setRemediationCommandInfoList(Arrays.asList(CommandInfo.builder("test").build()));
        mockGetTaskTest(taskObj);
        assertNotNull(faultTaskFactory.getRemediationTask(taskObj, taskObj.getId()));
        verifyGetTaskTest();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void mockGetTaskTest(Task taskObj) throws MangleException {
        when(pluginService.getExtension(anyString())).thenReturn(taskHelper);
        when(taskHelper.init(any(TaskSpec.class), anyString())).thenReturn(taskObj);
    }

    private void verifyGetTaskTest() throws MangleException {
        verify(pluginService, times(1)).getExtension(anyString());
        verify(taskHelper, times(1)).init(any(TaskSpec.class), anyString());
    }
}
