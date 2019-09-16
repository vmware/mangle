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

import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

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

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.CpuFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMAgentFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMCodeLevelFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.tasks.FaultTriggeringTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.faults.plugin.helpers.byteman.BytemanFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.byteman.BytemanFaultHelperFactory;
import com.vmware.mangle.faults.plugin.helpers.systemresource.SystemResourceFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.systemresource.SystemResourceFaultHelperFactory;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.faults.plugin.tasks.helpers.BytemanFaultTaskHelper;
import com.vmware.mangle.faults.plugin.tasks.helpers.K8SFaultTriggerTaskHelper;
import com.vmware.mangle.faults.plugin.tasks.helpers.SystemResourceFaultTaskHelper;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.events.TaskSubstageEvent;
import com.vmware.mangle.task.framework.helpers.CommandInfoExecutionHelper;
import com.vmware.mangle.unittest.faults.plugin.helpers.CommandResultUtils;
import com.vmware.mangle.unittest.faults.plugin.helpers.k8s.K8sFaultHelperTest;
import com.vmware.mangle.utils.clients.kubernetes.KubernetesCommandLineClient;
import com.vmware.mangle.utils.clients.kubernetes.PODClient;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author hkilari
 *
 */
@Log4j2
public class K8sFaultTriggerTaskTest {
    FaultsMockData faultsMockData = new FaultsMockData();

    @Mock
    private BytemanFaultHelperFactory bytemanFaultHelperFactory;
    @Mock
    private BytemanFaultHelper bytemanFaultHelper;
    @Mock
    private SystemResourceFaultHelper systemResourceFaultHelper;
    @Mock
    private SystemResourceFaultHelperFactory systemResourceFaultHelperFactory;
    @InjectMocks
    K8SFaultTriggerTaskHelper<K8SFaultTriggerSpec, JVMAgentFaultSpec> injectionTask;
    @InjectMocks
    K8SFaultTriggerTaskHelper<K8SFaultTriggerSpec, JVMCodeLevelFaultSpec> jvmCodeInjectionTask;
    @Mock
    ApplicationEventPublisher publisher;
    @Mock
    EndpointClientFactory endpointClientFactory;
    @Mock
    KubernetesCommandLineClient kubernetesCommandLineClient;
    @Spy
    BytemanFaultTaskHelper<JVMAgentFaultSpec> bytemanFaultTask;
    @Spy
    SystemResourceFaultTaskHelper<JVMAgentFaultSpec> systemResourceFaultTask;
    @Spy
    CommandInfoExecutionHelper commandInfoExecutionHelper;
    @Mock
    PODClient podClient;

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInitOfInjection() throws MangleException {
        K8SFaultTriggerTaskHelper<K8SFaultTriggerSpec, JVMAgentFaultSpec> injectionTask =
                new K8SFaultTriggerTaskHelper<>();

        Task<K8SFaultTriggerSpec> task = injectionTask.init(faultsMockData.getK8SCPUFaultTriggerSpec(), null);
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        Assert.assertEquals(task.getTaskData().isReadyForChildExecution(), false);
        Assert.assertEquals(task.getTaskDescription(),
                "Executing Fault: cpuFault on K8Sendpoint: k8sEPTest. "
                        + "More Details: [ CpuFaultSpec(super=JVMAgentFaultSpec(jvmProperties=JVMProperties(jvmprocess="
                        + "app.jar, javaHomePath=/usr/java/latest, user=testUser, port=9091)), cpuLoad=80) ], "
                        + "[ K8SSpecificArguments(podLabels=app=testPod, containerName=testContainer, "
                        + "enableRandomInjection=true, podInAction=testPod) ]");

        task.getTriggers().add(new TaskTrigger());
    }

    @Test
    public void testInitOfRemediation() throws MangleException {
        K8SFaultTriggerTaskHelper<K8SFaultTriggerSpec, JVMAgentFaultSpec> injectionTask =
                new K8SFaultTriggerTaskHelper<>();

        String injectionTaskId = "12345";
        FaultTriggeringTask<K8SFaultTriggerSpec, JVMAgentFaultSpec> task =
                (FaultTriggeringTask<K8SFaultTriggerSpec, JVMAgentFaultSpec>) injectionTask
                        .init(faultsMockData.getK8SCPUFaultTriggerSpec(), injectionTaskId);
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.REMEDIATION);
        Assert.assertEquals(task.getTaskData().isReadyForChildExecution(), false);
        Assert.assertEquals(task.getTaskDescription(),
                "Remediating Fault: cpuFault on K8Sendpoint: k8sEPTest. More Details: "
                        + "[ CpuFaultSpec(super=JVMAgentFaultSpec(jvmProperties=JVMProperties(jvmprocess=app.jar, "
                        + "javaHomePath=/usr/java/latest, user=testUser, port=9091)), cpuLoad=80) ], "
                        + "[ K8SSpecificArguments(podLabels=app=testPod, containerName=testContainer, enableRandomInjection=true,"
                        + " podInAction=testPod) ]");
        Assert.assertEquals(task.getInjectionTaskId(), injectionTaskId);
    }

    @Test
    public void testExecutionOfRandomInjection() throws MangleException {
        Task<K8SFaultTriggerSpec> task = injectionTask.init(faultsMockData.getK8SCPUFaultTriggerSpec());
        bytemanFaultTask.setBytemanFaultHelperFactory(bytemanFaultHelperFactory);
        injectionTask.setBytemanFaultTasky(bytemanFaultTask);
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        Assert.assertEquals(task.getTaskDescription(),
                "Executing Fault: cpuFault on K8Sendpoint: k8sEPTest. "
                        + "More Details: [ CpuFaultSpec(super=JVMAgentFaultSpec(jvmProperties=JVMProperties(jvmprocess="
                        + "app.jar, javaHomePath=/usr/java/latest, user=testUser, port=9091)), cpuLoad=80) ], "
                        + "[ K8SSpecificArguments(podLabels=app=testPod, containerName=testContainer, "
                        + "enableRandomInjection=true, podInAction=testPod) ]");
        task.getTriggers().add(new TaskTrigger());
        injectionTask.setEventPublisher(publisher);
        Mockito.when(endpointClientFactory.getEndPointClient(task.getTaskData().getFaultSpec().getCredentials(),
                task.getTaskData().getFaultSpec().getEndpoint())).thenReturn(kubernetesCommandLineClient);
        Mockito.when(kubernetesCommandLineClient.executeCommand(Mockito.any()))
                .thenReturn(CommandResultUtils.getCommandResult(K8sFaultHelperTest.getPodsListString()));
        Mockito.when(kubernetesCommandLineClient.getPODClient()).thenReturn(podClient);
        Mockito.when(podClient.getPodsWithLabels(Mockito.any())).thenReturn(K8sFaultHelperTest.getPodsAsList());
        when(bytemanFaultHelperFactory.getHelper(Mockito.any())).thenReturn(bytemanFaultHelper);

        injectionTask.executeTask(task);
        log.info(RestTemplateWrapper.objectToJson(task));
        Assert.assertEquals(injectionTask.getChildTasks(task).size(), 1);
        Assert.assertEquals(injectionTask.isReadyForChildExecution(task), true);
        Assert.assertEquals(task.getTaskSubstage(), "TRIGGER_CHILD_TASKS");
        Mockito.verify(publisher, Mockito.times(2)).publishEvent(Mockito.any(TaskSubstageEvent.class));
    }

    @Test
    public void testExecutionOfRandomJVMCodeLevelInjection() throws MangleException {
        Task<K8SFaultTriggerSpec> task = jvmCodeInjectionTask.init(faultsMockData.getK8SJVMCodeLevelFaultTriggerSpec());
        bytemanFaultTask.setBytemanFaultHelperFactory(bytemanFaultHelperFactory);
        injectionTask.setBytemanFaultTasky(bytemanFaultTask);
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        Assert.assertEquals(task.getTaskDescription(),
                "Executing Fault: SPRING_SERVICE_EXCEPTION on K8Sendpoint: k8sEPTest. "
                        + "More Details: [ JVMCodeLevelFaultSpec(super=JVMAgentFaultSpec(jvmProperties=JVMProperties("
                        + "jvmprocess=app.jar, javaHomePath=/usr/java/latest, user=testUser, port=9091))) ], "
                        + "[ K8SSpecificArguments(podLabels=app=testPod, "
                        + "containerName=testContainer, enableRandomInjection=true, podInAction=testPod) ]");
        task.getTriggers().add(new TaskTrigger());
        injectionTask.setEventPublisher(publisher);
        Mockito.when(endpointClientFactory.getEndPointClient(task.getTaskData().getFaultSpec().getCredentials(),
                task.getTaskData().getFaultSpec().getEndpoint())).thenReturn(kubernetesCommandLineClient);
        Mockito.when(kubernetesCommandLineClient.executeCommand(Mockito.any()))
                .thenReturn(CommandResultUtils.getCommandResult(K8sFaultHelperTest.getPodsListString()));
        Mockito.when(kubernetesCommandLineClient.getPODClient()).thenReturn(podClient);
        Mockito.when(podClient.getPodsWithLabels(Mockito.any())).thenReturn(K8sFaultHelperTest.getPodsAsList());
        when(bytemanFaultHelperFactory.getHelper(Mockito.any())).thenReturn(bytemanFaultHelper);

        injectionTask.executeTask(task);
        log.info(RestTemplateWrapper.objectToJson(task));
        Assert.assertEquals(injectionTask.getChildTasks(task).size(), 1);
        Assert.assertEquals(injectionTask.isReadyForChildExecution(task), true);
        Assert.assertEquals(task.getTaskSubstage(), "TRIGGER_CHILD_TASKS");
        Mockito.verify(publisher, Mockito.times(2)).publishEvent(Mockito.any(TaskSubstageEvent.class));
    }

    @Test
    public void testExecutionOfSystemResourceFaultInjection() throws MangleException {
        K8SFaultTriggerSpec spec = faultsMockData.getK8SCPUFaultTriggerSpec();
        ((CpuFaultSpec) spec.getFaultSpec()).setJvmProperties(null);
        Task<K8SFaultTriggerSpec> task = injectionTask.init(spec);
        injectionTask.setSystemResourceFaultTaskHelper(systemResourceFaultTask);
        systemResourceFaultTask.setSystemResourceFaultHelperFactory(systemResourceFaultHelperFactory);
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        Assert.assertEquals(task.getTaskDescription(),
                "Executing Fault: cpuFault on K8Sendpoint: k8sEPTest. More Details: [ CpuFaultSpec(cpuLoad=80) ], "
                        + "[ K8SSpecificArguments(podLabels=app=testPod, containerName=testContainer, "
                        + "enableRandomInjection=true, podInAction=testPod) ]");
        task.getTriggers().add(new TaskTrigger());
        injectionTask.setEventPublisher(publisher);
        Mockito.when(endpointClientFactory.getEndPointClient(task.getTaskData().getFaultSpec().getCredentials(),
                task.getTaskData().getFaultSpec().getEndpoint())).thenReturn(kubernetesCommandLineClient);
        Mockito.when(kubernetesCommandLineClient.executeCommand(Mockito.any()))
                .thenReturn(CommandResultUtils.getCommandResult(K8sFaultHelperTest.getPodsListString()));
        Mockito.when(kubernetesCommandLineClient.getPODClient()).thenReturn(podClient);
        Mockito.when(podClient.getPodsWithLabels(Mockito.any())).thenReturn(K8sFaultHelperTest.getPodsAsList());
        when(systemResourceFaultHelperFactory.getHelper(Mockito.any())).thenReturn(systemResourceFaultHelper);

        injectionTask.executeTask(task);
        log.info(RestTemplateWrapper.objectToJson(task));
        Assert.assertEquals(injectionTask.getChildTasks(task).size(), 1);
        Task<CommandExecutionFaultSpec> childTask =
                (Task<CommandExecutionFaultSpec>) injectionTask.getChildTasks(task).values().toArray()[0];
        Assert.assertEquals(childTask.getTaskData().getInjectionHomeDir(),
                task.getTaskData().getFaultSpec().getInjectionHomeDir());
        //Assert.assertEquals(childTask.getTaskData().getInjectionCommandInfoList(),SystemResourceFaultTaskHelperTest.getK8sCpuInjectionCommandInfoList());
        Assert.assertEquals(injectionTask.isReadyForChildExecution(task), true);
        Assert.assertEquals(task.getTaskSubstage(), "TRIGGER_CHILD_TASKS");
        Mockito.verify(publisher, Mockito.times(2)).publishEvent(Mockito.any(TaskSubstageEvent.class));
    }

    @Test
    public void testExecutionOfNonRandomInjection() throws MangleException {
        K8SFaultTriggerSpec spec = faultsMockData.getK8SCPUFaultTriggerSpec();
        spec.getFaultSpec().getK8sArguments().setEnableRandomInjection(false);
        Task<K8SFaultTriggerSpec> task = injectionTask.init(spec);
        bytemanFaultTask.setBytemanFaultHelperFactory(bytemanFaultHelperFactory);
        injectionTask.setBytemanFaultTasky(bytemanFaultTask);
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        Assert.assertEquals(task.getTaskDescription(),
                "Executing Fault: cpuFault on K8Sendpoint: k8sEPTest. "
                        + "More Details: [ CpuFaultSpec(super=JVMAgentFaultSpec(jvmProperties=JVMProperties("
                        + "jvmprocess=app.jar, javaHomePath=/usr/java/latest, user=testUser, port=9091)), cpuLoad=80) "
                        + "], [ K8SSpecificArguments(podLabels=app=testPod, containerName=testContainer, "
                        + "enableRandomInjection=false, podInAction=testPod) ]");
        task.getTriggers().add(new TaskTrigger());
        injectionTask.setEventPublisher(publisher);
        Mockito.when(endpointClientFactory.getEndPointClient(task.getTaskData().getFaultSpec().getCredentials(),
                task.getTaskData().getFaultSpec().getEndpoint())).thenReturn(kubernetesCommandLineClient);
        Mockito.when(kubernetesCommandLineClient.executeCommand(Mockito.any()))
                .thenReturn(CommandResultUtils.getCommandResult(K8sFaultHelperTest.getPodsListString()));
        Mockito.when(kubernetesCommandLineClient.getPODClient()).thenReturn(podClient);
        Mockito.when(podClient.getPodsWithLabels(Mockito.any())).thenReturn(K8sFaultHelperTest.getPodsAsList());
        when(bytemanFaultHelperFactory.getHelper(Mockito.any())).thenReturn(bytemanFaultHelper);

        injectionTask.executeTask(task);
        log.info(RestTemplateWrapper.objectToJson(task));
        Assert.assertEquals(injectionTask.getChildTasks(task).size(), 3);
        Assert.assertTrue(verifyChildTaskNames(injectionTask.getChildTasks(task)),
                "Found Duplicate Task Names for Child Tasks");
        Assert.assertEquals(injectionTask.isReadyForChildExecution(task), true);
        Assert.assertEquals(task.getTaskSubstage(), "TRIGGER_CHILD_TASKS");
        Mockito.verify(publisher, Mockito.times(2)).publishEvent(Mockito.any(TaskSubstageEvent.class));
    }

    private boolean verifyChildTaskNames(Map<String, Task<JVMAgentFaultSpec>> childTasks) {
        HashSet<String> set = new HashSet<>();
        for (Task<JVMAgentFaultSpec> task : childTasks.values()) {
            set.add(task.getTaskName());
        }
        return set.size() == childTasks.size();
    }

    @Test
    public void testExecutionOfInjectionForPODSelectionCommandFailure() {
        K8SFaultTriggerSpec spec = faultsMockData.getK8SCPUFaultTriggerSpec();
        spec.getFaultSpec().getK8sArguments().setEnableRandomInjection(false);
        Task<K8SFaultTriggerSpec> task = null;
        try {
            task = injectionTask.init(spec);
        } catch (MangleException e1) {
            log.error(e1);
            Assert.assertTrue(false);
        }
        bytemanFaultTask.setBytemanFaultHelperFactory(bytemanFaultHelperFactory);
        injectionTask.setBytemanFaultTasky(bytemanFaultTask);
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        Assert.assertEquals(task.getTaskDescription(),
                "Executing Fault: cpuFault on K8Sendpoint: k8sEPTest. More Details:"
                        + " [ CpuFaultSpec(super=JVMAgentFaultSpec(jvmProperties=JVMProperties"
                        + "(jvmprocess=app.jar, javaHomePath=/usr/java/latest, user=testUser, "
                        + "port=9091)), cpuLoad=80) ], [ K8SSpecificArguments(podLabels="
                        + "app=testPod, containerName=testContainer, enableRandomInjection=false, "
                        + "podInAction=testPod) ]");
        task.getTriggers().add(new TaskTrigger());
        injectionTask.setEventPublisher(publisher);
        Mockito.when(endpointClientFactory.getEndPointClient(task.getTaskData().getFaultSpec().getCredentials(),
                task.getTaskData().getFaultSpec().getEndpoint())).thenReturn(kubernetesCommandLineClient);
        Mockito.when(kubernetesCommandLineClient.executeCommand(Mockito.any()))
                .thenReturn(CommandResultUtils.getCommandResult(K8sFaultHelperTest.getPodsListString()));
        Mockito.when(kubernetesCommandLineClient.getPODClient()).thenReturn(podClient);
        Mockito.when(podClient.getPodsWithLabels(Mockito.any())).thenReturn(Arrays.asList());
        when(bytemanFaultHelperFactory.getHelper(Mockito.any())).thenReturn(bytemanFaultHelper);

        log.info(RestTemplateWrapper.objectToJson(task));
        try {
            injectionTask.executeTask(task);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.NO_PODS_IDENTIFIED);
        }
        Assert.assertEquals(task.getTaskSubstage(), "INITIALISED");
        Mockito.verify(publisher, Mockito.times(1)).publishEvent(Mockito.any(TaskSubstageEvent.class));
    }
}
