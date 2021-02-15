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

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.faults.plugin.helpers.azure.AzureVMFaultHelper;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.faults.plugin.tasks.helpers.AzureVMSpecificFaultTaskHelper;
import com.vmware.mangle.model.azure.faults.spec.AzureVMFaultSpec;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.helpers.CommandInfoExecutionHelper;
import com.vmware.mangle.unittest.faults.plugin.helpers.CommandResultUtils;
import com.vmware.mangle.unittest.faults.plugin.helpers.k8s.K8sFaultHelperTest;
import com.vmware.mangle.utils.clients.azure.AzureCommandExecutor;
import com.vmware.mangle.utils.clients.azure.CustomAzureClient;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Test class for AzureVMSpecificFaultTaskHelper
 *
 * @author bkaranam
 */
public class AzureVMSpecificFaultTaskHelperTest {
    FaultsMockData faultsMockData = new FaultsMockData();

    @Mock
    AzureVMFaultHelper azureVMFaultHelper;
    @Mock
    EndpointClientFactory endpointClientFactory;
    @InjectMocks
    private AzureVMSpecificFaultTaskHelper<AzureVMFaultSpec> azureVMSpecificFaultTask;
    @Mock
    ApplicationEventPublisher publisher;

    @Mock
    CustomAzureClient customAzureClient;
    @Mock
    AzureCommandExecutor executor;

    @Mock
    CommandInfoExecutionHelper commandInfoExecutionHelper;

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInitOfInjection() {
        AzureVMSpecificFaultTaskHelper<AzureVMFaultSpec> injectionTask = new AzureVMSpecificFaultTaskHelper<>();
        injectionTask.setAzureVMFaultHelper(azureVMFaultHelper);
        Task<AzureVMFaultSpec> task = injectionTask.init(faultsMockData.getAzureVMStateFaultSpec());
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
    }

    @Test
    public void testExecutionOfInjection() throws MangleException {
        Task<AzureVMFaultSpec> task = azureVMSpecificFaultTask.init(faultsMockData.getAzureVMStateFaultSpec(), null);
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        task.getTriggers().add(new TaskTrigger());
        task.getTaskData().setRandomInjection(false);
        azureVMSpecificFaultTask.setEventPublisher(publisher);
        azureVMSpecificFaultTask.setAzureVMFaultHelper(new AzureVMFaultHelper(endpointClientFactory));
        Mockito.when(endpointClientFactory.getEndPointClient(task.getTaskData().getCredentials(),
                task.getTaskData().getEndpoint())).thenReturn(customAzureClient);
        Mockito.doNothing().when(publisher).publishEvent(Mockito.any());
        Mockito.when(commandInfoExecutionHelper.runCommands(Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any())).thenReturn("");
        Mockito.when(executor.executeCommand(Mockito.any()))
                .thenReturn(CommandResultUtils.getCommandResult(K8sFaultHelperTest.getPodsListString()));
        azureVMSpecificFaultTask.executeTask(task);
        Assert.assertEquals(task.getTaskSubstage(), "COMPLETED");
    }
}
