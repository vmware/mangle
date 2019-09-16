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

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.CpuFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.FaultTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.faults.plugin.helpers.KnownFailuresHelper;
import com.vmware.mangle.faults.plugin.helpers.systemresource.DockerSystemResourceFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.systemresource.K8sSystemResourceFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.systemresource.LinuxSystemResourceFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.systemresource.SystemResourceFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.systemresource.SystemResourceFaultHelperFactory;
import com.vmware.mangle.faults.plugin.helpers.systemresource.SystemResourceFaultUtils;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.faults.plugin.tasks.helpers.SystemResourceFaultTaskHelper;
import com.vmware.mangle.faults.plugin.utils.PluginUtils;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.helpers.CommandInfoExecutionHelper;
import com.vmware.mangle.utils.ConstantsUtils;
import com.vmware.mangle.utils.clients.docker.CustomDockerClient;
import com.vmware.mangle.utils.clients.ssh.SSHUtils;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author jayasankarr
 *
 *         Test for SystemResourceFaultTask
 */
public class SystemResourceFaultTaskHelperTest {
    private FaultsMockData faultsMockData;
    private CpuFaultSpec k8sCpuFaultSpec;
    private CpuFaultSpec dockerCpuFaultSpec;
    private CpuFaultSpec remoteMachineCpuFaultSpec;
    @Mock
    private SystemResourceFaultHelper systemResourceFaultHelper;
    @Mock
    private SystemResourceFaultHelperFactory systemResourceFaultHelperFactory;
    @Mock
    private SystemResourceFaultUtils systemResourceFaultUtils;
    @Mock
    private PluginUtils pluginUtils;
    List<CommandInfo> value;
    @Mock
    EndpointClientFactory endpointClientFactory;
    @Mock
    CustomDockerClient customDockerClient;
    @Mock
    SSHUtils sshUtils;
    @Mock
    ApplicationEventPublisher publisher;
    @Mock
    private CommandExecutionFaultSpec taskData;
    @Mock
    CommandInfoExecutionHelper commandInfoExecutionHelper;

    @InjectMocks
    private SystemResourceFaultTaskHelper<CommandExecutionFaultSpec> injectionTaskK8s;

    @InjectMocks
    private SystemResourceFaultTaskHelper<CommandExecutionFaultSpec> injectionTaskDocker;

    @InjectMocks
    private SystemResourceFaultTaskHelper<CommandExecutionFaultSpec> injectionTaskRemoteMachine;

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        faultsMockData = new FaultsMockData();
        k8sCpuFaultSpec = faultsMockData.getK8sCpuJvmAgentFaultSpec();
        k8sCpuFaultSpec.setJvmProperties(null);
        dockerCpuFaultSpec = faultsMockData.getDockerCpuJvmAgentFaultSpec();
        dockerCpuFaultSpec.setJvmProperties(null);
        remoteMachineCpuFaultSpec = faultsMockData.getLinuxCpuJvmAgentFaultSpec();
        remoteMachineCpuFaultSpec.setJvmProperties(null);
    }


    @Test(priority = 1)
    public void testInitOfInjection() throws MangleException {
        Task<CommandExecutionFaultSpec> task = null;
        Mockito.doNothing().when(publisher).publishEvent(Mockito.any());
        injectionTaskK8s.setEventPublisher(publisher);
        SystemResourceFaultHelperFactory factory = new SystemResourceFaultHelperFactory();
        factory.setK8sSystemResourceFaultHelper(
                new K8sSystemResourceFaultHelper(endpointClientFactory, new SystemResourceFaultUtils()));
        injectionTaskK8s.setSystemResourceFaultHelperFactory(factory);
        injectionTaskK8s.setSystemResourceFaultUtils(new SystemResourceFaultUtils());
        task = injectionTaskK8s.init(k8sCpuFaultSpec, null);
        assertTrue(task.isInitialized());
        assertEquals(task.getTaskType(), TaskType.INJECTION);
        task.getTriggers().add(new TaskTrigger());
        assertEquals(task.getTaskDescription(),
                "Executing Fault: cpuFault on endpoint: k8sEPTest. More Details: [ CpuFaultSpec(cpuLoad=80) ], "
                        + "[ K8SSpecificArguments(podLabels=app=testPod, containerName=testContainer, "
                        + "enableRandomInjection=true, podInAction=testPod) ]");
        injectionTaskK8s.executeTask(task);
        assertEquals(task.getTaskSubstage(), "COMPLETED");
        assertEquals(task.getExtensionName(),
                "com.vmware.mangle.faults.plugin.tasks.helpers.SystemResourceFaultTaskHelper");
        assertEquals(task.getTaskData().getInjectionCommandInfoList(), getK8sCpuInjectionCommandInfoList(),
                "Injection command not matching for K8s system resource cpu fault");
        assertEquals(task.getTaskData().getRemediationCommandInfoList(), getK8sCpuRemediationCommandInfoList(),
                "Remediation command not matching for K8s system resource cpu fault");
    }

    @Test(priority = 2, dependsOnMethods = { "testInitOfInjection" })
    public void testInitOfRemediation() throws MangleException {
        SystemResourceFaultTaskHelper<CommandExecutionFaultSpec> taksForRemediation = injectionTaskK8s;
        FaultTask<CommandExecutionFaultSpec> task = null;
        String injectionTaskId = "12345";
        Mockito.doNothing().when(publisher).publishEvent(Mockito.any());
        task = (FaultTask<CommandExecutionFaultSpec>) taksForRemediation.init(k8sCpuFaultSpec, injectionTaskId);
        taksForRemediation.init(k8sCpuFaultSpec, null);
        assertTrue(task.isInitialized());
        assertEquals(task.getTaskType(), TaskType.REMEDIATION);
        assertEquals(task.getTaskDescription(),
                "Remediating Fault: cpuFault on endpoint: k8sEPTest. More Details: [ CpuFaultSpec(cpuLoad=80) ], "
                        + "[ K8SSpecificArguments(podLabels=app=testPod, containerName=testContainer,"
                        + " enableRandomInjection=true, podInAction=testPod) ]");
        assertEquals(task.getInjectionTaskId(), injectionTaskId);
        assertEquals(task.getExtensionName(),
                "com.vmware.mangle.faults.plugin.tasks.helpers.SystemResourceFaultTaskHelper");
    }

    @Test(priority = 3, dependsOnMethods = { "testInitOfRemediation" })
    public void testInitOfInjectionOnDockerContainer() throws MangleException {
        Task<CommandExecutionFaultSpec> task = null;
        when(systemResourceFaultHelper.getFaultInjectionScripts(Mockito.any()))
                .thenReturn(dockerCpuFaultSpec.getSupportScriptInfo());
        SystemResourceFaultHelperFactory factory = new SystemResourceFaultHelperFactory();
        factory.setDockerSystemResourceFaultHelper(
                new DockerSystemResourceFaultHelper(endpointClientFactory, new SystemResourceFaultUtils()));
        injectionTaskDocker.setSystemResourceFaultHelperFactory(factory);
        injectionTaskDocker.setSystemResourceFaultUtils(new SystemResourceFaultUtils());
        doNothing().when(pluginUtils).copyFileFromJarToDestination(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(publisher).publishEvent(Mockito.any());
        when(endpointClientFactory.getEndPointClient(Mockito.any(), Mockito.any())).thenReturn(customDockerClient);
        when(customDockerClient.copyFileToContainerByName(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(true);
        doNothing().when(commandInfoExecutionHelper).makeExecutable(Mockito.any(), Mockito.any());
        doNothing().when(commandInfoExecutionHelper).runCommands(Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any());
        injectionTaskDocker.setEventPublisher(publisher);
        task = injectionTaskDocker.init(dockerCpuFaultSpec, null);
        assertTrue(task.isInitialized());
        assertEquals(task.getTaskType(), TaskType.INJECTION);
        task.getTriggers().add(new TaskTrigger());
        assertEquals(task.getTaskDescription(),
                "Executing Fault: cpuFault on endpoint: dockerEPTest. More Details: [ CpuFaultSpec(cpuLoad=80) ], "
                        + "[ DockerSpecificArguments(containerName=testContainer) ]");
        injectionTaskDocker.executeTask(task);
        assertEquals(task.getTaskSubstage(), "COMPLETED");
        assertEquals(task.getExtensionName(),
                "com.vmware.mangle.faults.plugin.tasks.helpers.SystemResourceFaultTaskHelper");
        assertEquals(task.getTaskData().getInjectionCommandInfoList(), getCpuInjectionCommandInfoList(),
                "Injection command not matching for docker system resource cpu fault");
        assertEquals(task.getTaskData().getRemediationCommandInfoList(), getCpuRemediationCommandInfoList(),
                "Remediation command not matching for docker system resource cpu fault");
    }

    @Test(priority = 4)
    public void testInitOfInjectionOnRemoteMachine() throws MangleException {
        Task<CommandExecutionFaultSpec> task = null;
        when(systemResourceFaultHelper.getFaultInjectionScripts(Mockito.any()))
                .thenReturn(remoteMachineCpuFaultSpec.getSupportScriptInfo());
        SystemResourceFaultHelperFactory factory = new SystemResourceFaultHelperFactory();
        factory.setLinuxSystemResourceFaultHelper(
                new LinuxSystemResourceFaultHelper(endpointClientFactory, new SystemResourceFaultUtils()));
        injectionTaskRemoteMachine.setSystemResourceFaultHelperFactory(factory);
        injectionTaskRemoteMachine.setSystemResourceFaultUtils(new SystemResourceFaultUtils());
        doNothing().when(pluginUtils).copyFileFromJarToDestination(Mockito.any(), Mockito.any());
        Mockito.doNothing().when(publisher).publishEvent(Mockito.any());
        when(endpointClientFactory.getEndPointClient(Mockito.any(), Mockito.any())).thenReturn(sshUtils);
        when(sshUtils.putFile(Mockito.any(), Mockito.any())).thenReturn(true);
        doNothing().when(commandInfoExecutionHelper).makeExecutable(Mockito.any(), Mockito.any());
        doNothing().when(commandInfoExecutionHelper).runCommands(Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any());
        injectionTaskRemoteMachine.setEventPublisher(publisher);
        task = injectionTaskRemoteMachine.init(remoteMachineCpuFaultSpec, null);
        assertTrue(task.isInitialized());
        assertEquals(task.getTaskType(), TaskType.INJECTION);
        task.getTriggers().add(new TaskTrigger());
        assertEquals(task.getTaskDescription(),
                "Executing Fault: cpuFault on endpoint: rmEPTest. More Details: " + "[ CpuFaultSpec(cpuLoad=80) ]");
        injectionTaskRemoteMachine.executeTask(task);
        assertEquals(task.getTaskSubstage(), "COMPLETED");
        assertEquals(task.getExtensionName(),
                "com.vmware.mangle.faults.plugin.tasks.helpers.SystemResourceFaultTaskHelper");
        assertEquals(task.getTaskData().getInjectionCommandInfoList(), getCpuInjectionCommandInfoList(),
                "Injection command not matching for remote machine system resource cpu fault");
        assertEquals(task.getTaskData().getRemediationCommandInfoList(), getCpuRemediationCommandInfoList(),
                "Remediation command not matching for remote machine system resource cpu fault");
    }

    public static List<CommandInfo> getK8sCpuInjectionCommandInfoList() {
        List<CommandInfo> list = new ArrayList<>();
        CommandInfo copyCommand = new CommandInfo();
        copyCommand.setCommand("cp " + ConstantsUtils.getMangleSupportScriptDirectory()
                + "cpuburn.sh testPod:/tmp/cpuburn.sh -c testContainer");
        copyCommand.setIgnoreExitValueCheck(false);
        copyCommand.setNoOfRetries(0);
        copyCommand.setRetryInterval(0);
        copyCommand.setTimeout(0);
        copyCommand.setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfSystemResourceK8SCopyRequest());
        list.add(copyCommand);

        CommandInfo changePermissionCommand = new CommandInfo();
        changePermissionCommand.setCommand("exec -it testPod -c testContainer -- chmod -R u+x /tmp/cpuburn.sh");
        changePermissionCommand.setIgnoreExitValueCheck(false);
        changePermissionCommand.setNoOfRetries(0);
        changePermissionCommand.setRetryInterval(0);
        changePermissionCommand.setTimeout(0);
        changePermissionCommand.setExpectedCommandOutputList(null);
        list.add(changePermissionCommand);

        CommandInfo execCommand = new CommandInfo();
        execCommand.setCommand(
                "exec -it testPod -c testContainer -- /bin/sh /tmp/cpuburn.sh --operation=inject --load=80 --timeout=10000");
        execCommand.setIgnoreExitValueCheck(false);
        execCommand.setNoOfRetries(0);
        execCommand.setRetryInterval(0);
        execCommand.setTimeout(0);
        execCommand.setExpectedCommandOutputList(null);
        execCommand.setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfSystemResourceFaultInjectionRequest());
        list.add(execCommand);

        return list;
    }

    private List<CommandInfo> getCpuInjectionCommandInfoList() {
        List<CommandInfo> list = new ArrayList<>();
        CommandInfo injectionCommandInfo = new CommandInfo();
        injectionCommandInfo.setCommand("/tmp/cpuburn.sh --operation=inject --load=80 --timeout=10000");
        injectionCommandInfo.setIgnoreExitValueCheck(false);
        injectionCommandInfo.setNoOfRetries(0);
        injectionCommandInfo.setRetryInterval(0);
        injectionCommandInfo.setTimeout(0);
        injectionCommandInfo.setExpectedCommandOutputList(Collections.emptyList());
        injectionCommandInfo
                .setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfSystemResourceFaultInjectionRequest());
        list.add(injectionCommandInfo);
        return list;
    }

    private List<CommandInfo> getK8sCpuRemediationCommandInfoList() {
        List<CommandInfo> list = new ArrayList<>();
        CommandInfo remediationCommandInfo = new CommandInfo();
        remediationCommandInfo
                .setCommand("exec -it testPod -c testContainer -- /bin/sh /tmp/cpuburn.sh --operation=remediate");
        remediationCommandInfo.setIgnoreExitValueCheck(false);
        remediationCommandInfo.setNoOfRetries(0);
        remediationCommandInfo.setRetryInterval(0);
        remediationCommandInfo.setTimeout(0);
        remediationCommandInfo.setExpectedCommandOutputList(Collections.emptyList());
        remediationCommandInfo
                .setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfSystemResourceFaultRemediationRequest());
        list.add(remediationCommandInfo);
        return list;
    }

    private List<CommandInfo> getCpuRemediationCommandInfoList() {
        List<CommandInfo> list = new ArrayList<>();
        CommandInfo remediationCommandInfo = new CommandInfo();
        remediationCommandInfo.setCommand("/tmp/cpuburn.sh --operation=remediate");
        remediationCommandInfo.setIgnoreExitValueCheck(false);
        remediationCommandInfo.setNoOfRetries(0);
        remediationCommandInfo.setRetryInterval(0);
        remediationCommandInfo.setTimeout(0);
        remediationCommandInfo.setExpectedCommandOutputList(Collections.emptyList());
        remediationCommandInfo
                .setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfSystemResourceFaultRemediationRequest());
        list.add(remediationCommandInfo);
        return list;
    }

}
