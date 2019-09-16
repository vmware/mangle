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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

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

import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMAgentFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMCodeLevelFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.faults.plugin.helpers.byteman.BytemanFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.byteman.BytemanFaultHelperFactory;
import com.vmware.mangle.faults.plugin.helpers.byteman.DockerBytemanFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.byteman.K8sBytemanFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.byteman.LinuxBytemanFaultHelper;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.faults.plugin.tasks.helpers.BytemanFaultTaskHelper;
import com.vmware.mangle.faults.plugin.utils.PluginUtils;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.helpers.CommandInfoExecutionHelper;
import com.vmware.mangle.utils.clients.docker.CustomDockerClient;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.clients.ssh.SSHUtils;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 *
 *
 * @author hkilari
 */
@Log4j2
public class BytemanFaultTaskHelperTest {
    FaultsMockData faultsMockData = new FaultsMockData();

    @Mock
    private BytemanFaultHelper bytemanFaultHelper;
    @Mock
    private BytemanFaultHelperFactory bytemanFaultHelperFactory;
    @Mock
    private K8sBytemanFaultHelper k8sBytemanFaultHelper;
    @InjectMocks
    LinuxBytemanFaultHelper linuxBytemanFaultHelper;
    @Mock
    List<CommandInfo> value;
    @Mock
    private CommandExecutionFaultSpec taskData;
    @Mock
    private SSHUtils sshUtils;
    @Mock
    ApplicationEventPublisher publisher;
    @Mock
    EndpointClientFactory endpointClientFactory;
    @Spy
    CommandInfoExecutionHelper commandInfoExecutionHelper;
    @InjectMocks
    private BytemanFaultTaskHelper<JVMAgentFaultSpec> jvmInjectionTask;

    @InjectMocks
    private BytemanFaultTaskHelper<JVMCodeLevelFaultSpec> codeLevelInjectionTask;
    @Spy
    private DockerBytemanFaultHelper dockerBytemanFaultHelper;
    @Mock
    private CustomDockerClient customDockerClient;
    @Mock
    private PluginUtils pluginUtils;

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInitOfInjection() {
        BytemanFaultTaskHelper<JVMAgentFaultSpec> injectionTask = jvmInjectionTask;
        injectionTask.setEventPublisher(publisher);
        injectionTask.setCommandInfoExecutionHelper(commandInfoExecutionHelper);
        Task<JVMAgentFaultSpec> task = null;
        try {
            when(bytemanFaultHelperFactory.getHelper(Mockito.any())).thenReturn(bytemanFaultHelper);
            doNothing().when(taskData).setInjectionCommandInfoList(value);
            when(bytemanFaultHelper.getInjectionCommandInfoList(Mockito.any())).thenReturn(value);
            Mockito.doNothing().when(commandInfoExecutionHelper).runCommands(Mockito.any(), Mockito.any(),
                    Mockito.any(), Mockito.any());

            task = injectionTask.init(faultsMockData.getK8sCpuJvmAgentFaultSpec(), null);
            jvmInjectionTask.init(faultsMockData.getK8sCpuJvmAgentFaultSpec(), null);

            verify(bytemanFaultHelperFactory, times(2)).getHelper(any(EndpointSpec.class));
            verify(bytemanFaultHelper, times(2)).getInjectionCommandInfoList(any(CommandExecutionFaultSpec.class));
        } catch (MangleException e1) {
            e1.printStackTrace();
        }
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        Assert.assertEquals(task.getTaskDescription(),
                "Executing Fault: cpuFault on endpoint: k8sEPTest. More Details: [ CpuFaultSpec("
                        + "super=JVMAgentFaultSpec(jvmProperties=JVMProperties(jvmprocess=app.jar, javaHomePath=/usr/java/latest,"
                        + " user=testUser, port=9091)), cpuLoad=80) ], [ K8SSpecificArguments(podLabels=app=testPod, "
                        + "containerName=testContainer, enableRandomInjection=true, podInAction=testPod) ]");
        task.getTriggers().add(new TaskTrigger());
        try {
            injectionTask.executeTask(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info(RestTemplateWrapper.objectToJson(task));
        Assert.assertEquals(task.getTaskSubstage(), "COMPLETED");
        Assert.assertEquals(task.getExtensionName(),
                "com.vmware.mangle.faults.plugin.tasks.helpers.BytemanFaultTaskHelper");
    }

    @Test
    public void testInjectionOfJVMCodeLevelFaultonLinux() {
        BytemanFaultTaskHelper<JVMCodeLevelFaultSpec> injectionTask = codeLevelInjectionTask;
        injectionTask.setPluginUtils(new PluginUtils());
        Task<JVMCodeLevelFaultSpec> task = null;
        injectionTask.setEventPublisher(publisher);
        try {
            linuxBytemanFaultHelper.setEndpointClientFactory(endpointClientFactory);
            when(endpointClientFactory.getEndPointClient(Mockito.any(), Mockito.any())).thenReturn(sshUtils);
            when(bytemanFaultHelperFactory.getHelper(Mockito.any())).thenReturn(linuxBytemanFaultHelper);
            task = injectionTask.init(faultsMockData.getLinuxJvmCodelevelFaultSpec());
            Mockito.doNothing().when(commandInfoExecutionHelper).runCommands(Mockito.any(), Mockito.any(),
                    Mockito.any(), Mockito.any());
        } catch (MangleException e1) {
            e1.printStackTrace();
        }
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        Assert.assertEquals(task.getTaskDescription(),
                "Executing Fault: SPRING_SERVICE_EXCEPTION on endpoint: rmEPTest. More Details: [ JVMCodeLevelFaultSpec"
                        + "(super=JVMAgentFaultSpec(jvmProperties=JVMProperties(jvmprocess=app.jar, javaHomePath=/usr/java/latest, "
                        + "user=testUser, port=9091))) ]");

        task.getTriggers().add(new TaskTrigger());
        try {
            codeLevelInjectionTask.executeTask(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info(RestTemplateWrapper.objectToJson(task));
        Assert.assertEquals(task.getTaskSubstage(), "COMPLETED");
        Assert.assertEquals(task.getExtensionName(),
                "com.vmware.mangle.faults.plugin.tasks.helpers.BytemanFaultTaskHelper");
    }

    @Test
    public void testInjectionOfJVMCodeLevelFaultOnK8s() {
        BytemanFaultTaskHelper<JVMCodeLevelFaultSpec> injectionTask = codeLevelInjectionTask;
        Task<JVMCodeLevelFaultSpec> task = null;
        try {
            when(bytemanFaultHelperFactory.getHelper(Mockito.any())).thenReturn(k8sBytemanFaultHelper);
            task = injectionTask.init(faultsMockData.getK8sSpringExceptionJVMCodeLevelFaultSpec());
        } catch (MangleException e1) {
            e1.printStackTrace();
        }
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        Assert.assertEquals(task.getTaskDescription(),
                "Executing Fault: SPRING_SERVICE_EXCEPTION on endpoint: k8sEPTest. More Details: [ "
                        + "JVMCodeLevelFaultSpec(super=JVMAgentFaultSpec(jvmProperties=JVMProperties(jvmprocess=app.jar"
                        + ", javaHomePath=/usr/java/latest, user=testUser, port=9091))"
                        + ") ], [ K8SSpecificArguments(podLabels=app=testPod, containerName=testContainer, "
                        + "enableRandomInjection=true, podInAction=testPod) ]");

        task.getTriggers().add(new TaskTrigger());
        try {
            injectionTask.executeTask(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInjectionOfJVMCodeLevelFaultOnDocker() {
        BytemanFaultTaskHelper<JVMCodeLevelFaultSpec> injectionTask = codeLevelInjectionTask;

        injectionTask.setPluginUtils(pluginUtils);
        Task<JVMCodeLevelFaultSpec> task = null;
        injectionTask.setEventPublisher(publisher);
        try {
            dockerBytemanFaultHelper.setEndpointClientFactory(endpointClientFactory);
            dockerBytemanFaultHelper.setPluginUtils(pluginUtils);
            JVMCodeLevelFaultSpec faultSpec = faultsMockData.getDockerJvmCodelevelFaultSpec();
            when(endpointClientFactory.getEndPointClient(Mockito.any(), Mockito.any())).thenReturn(customDockerClient);
            when(bytemanFaultHelperFactory.getHelper(Mockito.any())).thenReturn(dockerBytemanFaultHelper);
            Mockito.doNothing().when(dockerBytemanFaultHelper).checkTaskSpecificPrerequisites();
            task = injectionTask.init(faultSpec);
            Mockito.doNothing().when(commandInfoExecutionHelper).runCommands(Mockito.any(), Mockito.any(),
                    Mockito.any(), Mockito.any());
        } catch (MangleException e1) {
            e1.printStackTrace();
        }
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        Assert.assertEquals(task.getTaskDescription(),
                "Executing Fault: SPRING_SERVICE_EXCEPTION on endpoint: dockerEPTest. More Details: [ JVMCodeLevelFaultSpec("
                        + "super=JVMAgentFaultSpec(jvmProperties=JVMProperties(jvmprocess=app.jar, javaHomePath=/usr/java/latest, "
                        + "user=testUser, port=9091))) ], "
                        + "[ DockerSpecificArguments(containerName=testContainer) ]");

        task.getTriggers().add(new TaskTrigger());
        try {
            codeLevelInjectionTask.executeTask(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info(RestTemplateWrapper.objectToJson(task));
        Assert.assertEquals(task.getTaskSubstage(), "COMPLETED");
        Assert.assertEquals(task.getExtensionName(),
                "com.vmware.mangle.faults.plugin.tasks.helpers.BytemanFaultTaskHelper");
    }
}
