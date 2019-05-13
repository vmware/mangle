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

package com.vmware.mangle.unittest.faults.plugin.helpers.docker;

import static org.mockito.Matchers.any;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DockerFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.faults.plugin.helpers.docker.DockerFaultHelper;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.services.enums.DockerFaultName;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.docker.CustomDockerClient;
import com.vmware.mangle.utils.clients.docker.DockerCommandExecutor;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Unit Tests for DockerFaultHelper class
 *
 * @author rpraveen
 */

@Log4j2
@PrepareForTest(value = { DockerFaultHelper.class })
public class DockerFaultHelperTest {

    private FaultsMockData mockData = new FaultsMockData();

    @Mock
    private EndpointClientFactory factory;
    @Mock
    private ICommandExecutor executor;

    @Mock
    private CustomDockerClient customDockerClient;
    private DockerFaultHelper dockerFaultHelper;

    @BeforeMethod
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        dockerFaultHelper = new DockerFaultHelper(factory);
    }

    @Test
    public void testDockerFaultHelperInit() {
        DockerFaultHelper helper = new DockerFaultHelper(factory);
        Assert.assertNotNull(helper);
    }

    @Test
    public void testGetExecutor() throws Exception {
        ICommandExecutor executor = null;
        try {
            CommandExecutionFaultSpec dockerFaultSpec = mockData.getDockerPauseFaultSpec();
            Mockito.when(factory.getEndPointClient(null, dockerFaultSpec.getEndpoint())).thenReturn(customDockerClient);
            DockerCommandExecutor dockerCommandExecutor = Mockito.mock(DockerCommandExecutor.class);
            PowerMockito.whenNew(DockerCommandExecutor.class).withArguments(any(CustomDockerClient.class))
                    .thenReturn(dockerCommandExecutor);
            executor = dockerFaultHelper.getExecutor(dockerFaultSpec);
        } catch (MangleException e) {
            log.error("testGetExecutor failed with Exception: ", e);
            Assert.assertTrue(false);
        }
        Assert.assertNotNull(executor);
    }

    @Test
    public void testGetInjectionCommandInfoList() throws MangleException {
        DockerFaultSpec spec = mockData.getDockerPauseFaultSpec();
        DockerFaultHelper helper = new DockerFaultHelper(factory);
        List<CommandInfo> commandInfos = helper.getInjectionCommandInfoList(executor, spec);
        Assert.assertEquals(1, commandInfos.size());
        CommandInfo commandInfo = commandInfos.get(0);
        Assert.assertTrue(commandInfo.getCommand().startsWith(spec.getDockerFaultName().name()));
    }

    @Test
    public void testGetRemediationCommandListDOCKER_STOP() throws MangleException {
        DockerFaultSpec spec = mockData.getDockerStopFaultSpec();
        spec.setDockerFaultName(DockerFaultName.DOCKER_STOP);
        DockerFaultHelper helper = new DockerFaultHelper(factory);
        List<CommandInfo> list = helper.getRemediationCommandInfoList(executor, spec);
        Assert.assertEquals(1, list.size());
        CommandInfo commandInfo = list.get(0);
        Assert.assertTrue(commandInfo.getCommand().contains(DockerFaultName.DOCKER_START.name()));
    }

    @Test
    public void testGetRemediationCommandListDOCKER_PAUSE() throws MangleException {
        DockerFaultSpec spec = mockData.getDockerPauseFaultSpec();
        spec.setDockerFaultName(DockerFaultName.DOCKER_PAUSE);
        DockerFaultHelper helper = new DockerFaultHelper(factory);
        List<CommandInfo> list = helper.getRemediationCommandInfoList(executor, spec);
        Assert.assertEquals(1, list.size());
        CommandInfo commandInfo = list.get(0);
        Assert.assertTrue(commandInfo.getCommand().contains(DockerFaultName.DOCKER_UNPAUSE.name()));
    }

    @Test
    public void testGetRemediationCommandListUNSUPPORTED_FAULT() throws MangleException {
        DockerFaultSpec spec = mockData.getDockerPauseFaultSpec();
        spec.setDockerFaultName(DockerFaultName.DOCKER_UNPAUSE);
        DockerFaultHelper helper = new DockerFaultHelper(factory);
        List<CommandInfo> list = new ArrayList<>();
        try {
            list = helper.getRemediationCommandInfoList(executor, spec);
        } catch (Exception e) {
            log.error(e);
        }
        Assert.assertEquals(0, list.size());
    }
}
