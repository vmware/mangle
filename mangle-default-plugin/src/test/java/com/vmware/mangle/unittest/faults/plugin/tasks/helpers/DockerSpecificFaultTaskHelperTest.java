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
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DockerFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.FaultTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandOutputProcessingInfo;
import com.vmware.mangle.faults.plugin.helpers.KnownFailuresHelper;
import com.vmware.mangle.faults.plugin.helpers.docker.DockerFaultHelper;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.faults.plugin.tasks.helpers.DockerSpecificFaultTaskHelper;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.helpers.CommandInfoExecutionHelper;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.docker.CustomDockerClient;
import com.vmware.mangle.utils.exceptions.MangleException;


/**
 *
 *
 * @author rpraveen
 */
@Log4j2
public class DockerSpecificFaultTaskHelperTest {
    FaultsMockData faultsMockData = new FaultsMockData();

    @Mock
    DockerFaultHelper dockerFaultHelper;

    @Mock
    List<CommandInfo> value;

    private DockerFaultSpec dockerPauseFaultSpec;
    @Mock
    private CommandExecutionFaultSpec taskData;

    @InjectMocks
    private DockerSpecificFaultTaskHelper<DockerFaultSpec> injectionTask;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private CommandInfoExecutionHelper commandInfoExecutionHelper;

    @Mock
    private EndpointClientFactory factory;

    @Mock
    private ICommandExecutor executor;

    @Mock
    private CustomDockerClient customDockerClient;


    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        faultsMockData = new FaultsMockData();
        dockerPauseFaultSpec = faultsMockData.getDockerPauseFaultSpec();
    }

    @Test(priority = 1)
    public void testInitOfInjection() {
        DockerSpecificFaultTaskHelper<DockerFaultSpec> injectionTask = new DockerSpecificFaultTaskHelper<>();
        Task<DockerFaultSpec> task = injectionTask.init(faultsMockData.getDockerPauseFaultSpec());
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(TaskType.INJECTION, task.getTaskType());
        task.getTriggers().add(new TaskTrigger());
        List<CommandInfo> injectionCommands = injectionTask.getInjectionExecutionInfo(task);
        List<CommandInfo> remediationCommands = injectionTask.getRemediationExecutionInfo(task);
        log.info(injectionCommands);
        log.info(remediationCommands);
    }


    @Test(priority = 2)
    public void testExecutionOfInjection() {
        Task<DockerFaultSpec> task = null;
        try {
            ICommandExecutor executor1 = Mockito.mock(ICommandExecutor.class);
            DockerFaultHelper dockerFaultHelper1 = Mockito.spy(new DockerFaultHelper(factory));
            doReturn(executor1).when(dockerFaultHelper1).getExecutor(any(CommandExecutionFaultSpec.class));
            injectionTask.setDockerFaultHelper(dockerFaultHelper1);
            Mockito.doNothing().when(publisher).publishEvent(Mockito.any());
            injectionTask.setEventPublisher(publisher);
            Mockito.when(
                    commandInfoExecutionHelper.runCommands(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                    .thenReturn("");
            injectionTask.setCommandInfoExecutionHelper(commandInfoExecutionHelper);
            task = injectionTask.init(dockerPauseFaultSpec, null);

        } catch (MangleException e1) {
            e1.printStackTrace();
        }
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        task.getTriggers().add(new TaskTrigger());

        try {
            injectionTask.executeTask(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info(task.getTaskDescription());
        Assert.assertEquals(task.getTaskSubstage(), "COMPLETED");
        Assert.assertEquals(task.getExtensionName(),
                "com.vmware.mangle.faults.plugin.tasks.helpers.DockerSpecificFaultTaskHelper");

        Assert.assertEquals(task.getTaskDescription(),
                "Executing Fault: DOCKER_PAUSE on endpoint: dockerEPTest. More Details: [ DockerFaultSpec(super="
                        + "CommandExecutionFaultSpec(super=AutoRemediatedFaultSpec(timeoutInMilliseconds=null), "
                        + "args={--containerName=testContainer}, injectionHomeDir=/tmp/), dockerFaultName=DOCKER_PAUSE) ]");

        Assert.assertEquals(task.getTaskData().getInjectionCommandInfoList(), getDockerPauseInjectionCommandInfoList(),
                "Injection command not matching for Docker pause fault");
        Assert.assertEquals(task.getTaskData().getRemediationCommandInfoList(),
                getDockerPauseRemediationCommandInfoList(), "Remediation command not matching for Docker pause fault");

    }


    @Test(priority = 3, dependsOnMethods = { "testExecutionOfInjection" })
    public void testInitOfRemediation() {
        DockerSpecificFaultTaskHelper<DockerFaultSpec> taksForRemediation = injectionTask;
        FaultTask<DockerFaultSpec> task = null;
        String injectionTaskId = "12345";
        Mockito.doNothing().when(publisher).publishEvent(Mockito.any());
        task = (FaultTask<DockerFaultSpec>) taksForRemediation.init(dockerPauseFaultSpec, injectionTaskId);
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.REMEDIATION);
        Assert.assertEquals(task.getInjectionTaskId(), injectionTaskId);
        Assert.assertEquals(task.getTaskDescription(),
                "Remediating Fault: DOCKER_PAUSE on endpoint: dockerEPTest. More Details: [ DockerFaultSpec(super="
                        + "CommandExecutionFaultSpec(super=AutoRemediatedFaultSpec(timeoutInMilliseconds=null), "
                        + "args={--containerName=testContainer, id=" + task.getTaskData().getArgs().get("id") + "}, "
                        + "injectionHomeDir=/tmp/), dockerFaultName=DOCKER_PAUSE) ]");

        Assert.assertEquals(task.getTaskData().getInjectionCommandInfoList(), getDockerPauseInjectionCommandInfoList(),
                "Injection command not matching for Docker pause fault");
        Assert.assertEquals(task.getTaskData().getRemediationCommandInfoList(),
                getDockerPauseRemediationCommandInfoList(), "Remediation command not matching for Docker pause fault");
    }

    private List<CommandInfo> getDockerPauseInjectionCommandInfoList() {
        List<CommandOutputProcessingInfo> commandOutputProcessingInfoList = new ArrayList<>();
        CommandOutputProcessingInfo commandOutputProcessingInfo = new CommandOutputProcessingInfo();
        commandOutputProcessingInfo.setExtractedPropertyName("containerId");
        commandOutputProcessingInfo.setRegExpression("^.*$");
        commandOutputProcessingInfoList.add(commandOutputProcessingInfo);
        CommandInfo injectionCommandInfo =
                CommandInfo.builder("DOCKER_PAUSE:--containerName testContainer").ignoreExitValueCheck(false)
                        .noOfRetries(0).retryInterval(0).timeout(0).expectedCommandOutputList(Collections.emptyList())
                        .knownFailureMap(KnownFailuresHelper.getKnownFailureOfDockerFaultInjectionRequest())
                        .commandOutputProcessingInfoList(commandOutputProcessingInfoList).build();

        List<CommandInfo> commandInfoList = new ArrayList<>();
        commandInfoList.add(injectionCommandInfo);
        return commandInfoList;
    }

    private List<CommandInfo> getDockerPauseRemediationCommandInfoList() {
        List<CommandInfo> list = new ArrayList<>();
        CommandInfo remediationCommandInfo =
                CommandInfo.builder("DOCKER_UNPAUSE:--containerName testContainer").ignoreExitValueCheck(false)
                        .noOfRetries(0).retryInterval(0).timeout(0).expectedCommandOutputList(Collections.emptyList())
                        .knownFailureMap(KnownFailuresHelper.getKnownFailureOfDockerFaultRemediationRequest()).build();
        list.add(remediationCommandInfo);
        return list;
    }
}
