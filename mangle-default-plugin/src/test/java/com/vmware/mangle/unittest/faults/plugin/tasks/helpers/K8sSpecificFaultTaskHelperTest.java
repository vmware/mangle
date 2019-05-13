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

import lombok.extern.log4j.Log4j2;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.faults.plugin.helpers.k8s.K8sFaultHelper;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.faults.plugin.tasks.helpers.K8sSpecificFaultTaskHelper;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.helpers.CommandInfoExecutionHelper;
import com.vmware.mangle.unittest.faults.plugin.helpers.CommandResultUtils;
import com.vmware.mangle.unittest.faults.plugin.helpers.k8s.K8sFaultHelperTest;
import com.vmware.mangle.utils.clients.kubernetes.KubernetesCommandLineClient;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 *
 *
 * @author hkilari
 */
@Log4j2
public class K8sSpecificFaultTaskHelperTest {
    FaultsMockData faultsMockData = new FaultsMockData();

    @Mock
    EndpointClientFactory endpointClientFactory;
    @InjectMocks
    private K8sSpecificFaultTaskHelper<K8SFaultSpec> injectionTask;
    @Mock
    KubernetesCommandLineClient kubernetesCommandLineClient;
    @Mock
    ApplicationEventPublisher publisher;
    @Spy
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
        K8sSpecificFaultTaskHelper<K8SFaultSpec> injectionTask = new K8sSpecificFaultTaskHelper<>();

        Task<K8SFaultSpec> task = injectionTask.init(faultsMockData.getDeleteK8SResourceFaultSpec(), null);
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        Assert.assertEquals(task.getTaskDescription(),
                "Executing Fault: null on endpoint: k8sEPTest. More Details: "
                        + "[ K8SFaultSpec(resourceType=POD, resourceLabels={app=app-inventory-service}, "
                        + "randomInjection=false) ]");

        task.getTriggers().add(new TaskTrigger());
    }

    @Test
    public void testExecutionOfInjection() throws MangleException {
        Task<K8SFaultSpec> task = injectionTask.init(faultsMockData.getK8SResourceNotReadyFaultSpec(), null);
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        Assert.assertEquals(task.getTaskDescription(),
                "Executing Fault: NOTREADY_RESOURCE on endpoint: k8sEPTest. More Details: "
                        + "[ K8SResourceNotReadyFaultSpec(super=K8SFaultSpec(resourceType=POD, resourceLabels="
                        + "{app=app-inventory-service}, randomInjection=false), appContainerName=testContainer) ]");

        task.getTriggers().add(new TaskTrigger());
        task.getTaskData().setRandomInjection(false);
        injectionTask.setEventPublisher(publisher);
        injectionTask.setK8sFaultHelper(new K8sFaultHelper(endpointClientFactory));
        /*task.getTaskData().setInjectionCommandInfoList(
                K8sFaultHelperTest.getExpectedInjectionCommandsForNonRandomResourceNotReadyFaultInjection());
        task.getTaskData().setRemediationCommandInfoList(
                K8sFaultHelperTest.getExpectedRemediationCommandsForNonRandomResourceNotReadyFaultRemediation());
        */ Mockito.when(endpointClientFactory.getEndPointClient(task.getTaskData().getCredentials(),
                task.getTaskData().getEndpoint())).thenReturn(kubernetesCommandLineClient);
        Mockito.doNothing().when(publisher).publishEvent(Mockito.any());
        Mockito.doNothing().when(commandInfoExecutionHelper).runCommands(Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any());
        Mockito.when(kubernetesCommandLineClient.executeCommand(Mockito.any()))
                .thenReturn(CommandResultUtils.getCommandResult(K8sFaultHelperTest.getPodsListString()));
        injectionTask.executeTask(task);
        log.info(RestTemplateWrapper.objectToJson(task));
        Assert.assertEquals(task.getTaskSubstage(), "COMPLETED");
        Assert.assertEquals(task.getTaskData().getInjectionCommandInfoList(),
                K8sFaultHelperTest.getExpectedInjectionCommandsForNonRandomResourceNotReadyFaultInjection());
        Assert.assertEquals(task.getTaskData().getRemediationCommandInfoList(),
                K8sFaultHelperTest.getExpectedRemediationCommandsForNonRandomResourceNotReadyFaultRemediation());
    }

    @Test
    public void testExecutionOfInjectionWithCommandExecutionFailure() {
        Task<K8SFaultSpec> task = injectionTask.init(faultsMockData.getDeleteK8SResourceFaultSpec(), null);
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        Assert.assertEquals(task.getTaskDescription(),
                "Executing Fault: null on endpoint: k8sEPTest. More Details: [ K8SFaultSpec(resourceType=POD,"
                        + " resourceLabels={app=app-inventory-service}, randomInjection=false) ]");

        task.getTriggers().add(new TaskTrigger());
        injectionTask.setEventPublisher(publisher);
        injectionTask.setK8sFaultHelper(new K8sFaultHelper(endpointClientFactory));
        task.getTaskData().setInjectionCommandInfoList(
                K8sFaultHelperTest.getExpectedInjectionCommandsForNonRandomResourceNotReadyFaultInjection());
        task.getTaskData().setRemediationCommandInfoList(
                K8sFaultHelperTest.getExpectedRemediationCommandsForNonRandomResourceNotReadyFaultRemediation());

        Mockito.when(endpointClientFactory.getEndPointClient(task.getTaskData().getCredentials(),
                task.getTaskData().getEndpoint())).thenReturn(kubernetesCommandLineClient);

        Mockito.doNothing().when(publisher).publishEvent(Mockito.any());
        Mockito.when(kubernetesCommandLineClient.executeCommand(Mockito.any()))
                .thenReturn(CommandResultUtils.getFailureErrorCodeCommandResult(""));
        try {
            injectionTask.executeTask(task);
        } catch (MangleException e) {
            Assert.assertEquals(e.getMessage(), "");
        }
        log.info(RestTemplateWrapper.objectToJson(task));
        Assert.assertEquals(task.getTaskSubstage(), "PREPARE_TARGET_MACHINE");
    }
}
