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

package com.vmware.mangle.unittest.faults.plugin.helpers.systemresource;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.DEFAULT_TEMP_DIR;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.LOAD_ARG;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j2;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMAgentFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.faults.plugin.helpers.systemresource.DockerSystemResourceFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.systemresource.SystemResourceFaultUtils;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.docker.CustomDockerClient;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Test Class for DockerSystemResourceFaultHelper
 *
 * @author rpraveen
 *
 */
@Log4j2
public class DockerSystemResourceFaultHelperTest {
    @Mock
    private EndpointClientFactory endpointClientFactory;

    @Mock
    CustomDockerClient customDockerClient;

    @Mock
    private SystemResourceFaultUtils systemResourceFaultUtils;

    private DockerSystemResourceFaultHelper dockerSystemResourceFaultHelper;

    private FaultsMockData faultsMockData = new FaultsMockData();
    @Mock
    List<SupportScriptInfo> supportScripts;

    private String injectionCommand;

    private String remediationCommand;


    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        faultsMockData = new FaultsMockData();
        dockerSystemResourceFaultHelper =
                new DockerSystemResourceFaultHelper(endpointClientFactory, systemResourceFaultUtils);
        CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getDockerCpuJvmAgentFaultSpec();
        injectionCommand = String.format("%s/cpuburn.sh --operation=inject --load=%s --timeout=%s", DEFAULT_TEMP_DIR,
                cpuFaultSpec.getArgs().get(LOAD_ARG), cpuFaultSpec.getTimeoutInMilliseconds()).toString();
        remediationCommand = String.format("/tmp/cpuburn.sh --operation=remediate");
    }

    @Test
    public void testGetExecutor() {
        ICommandExecutor executor = null;
        try {
            JVMAgentFaultSpec cpuFaultSpec = faultsMockData.getDockerCpuJvmAgentFaultSpec();
            cpuFaultSpec.setJvmProperties(null);
            Mockito.when(
                    endpointClientFactory.getEndPointClient(cpuFaultSpec.getCredentials(), cpuFaultSpec.getEndpoint()))
                    .thenReturn(customDockerClient);
            executor = dockerSystemResourceFaultHelper.getExecutor(cpuFaultSpec);
        } catch (MangleException e) {
            log.error("testGetExecutor failed with Exception: ", e);
            Assert.assertTrue(false);
        }
        Assert.assertNotNull(executor);
    }

    @Test
    public void testGetInjectionCommandInfoList() {


        try {
            CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getDockerCpuJvmAgentFaultSpec();

            Mockito.when(
                    endpointClientFactory.getEndPointClient(cpuFaultSpec.getCredentials(), cpuFaultSpec.getEndpoint()))
                    .thenReturn(customDockerClient);

            Mockito.when(systemResourceFaultUtils.buildInjectionCommand(any(), any())).thenReturn(injectionCommand);

            List<CommandInfo> injectionCommands =
                    dockerSystemResourceFaultHelper.getInjectionCommandInfoList(cpuFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(injectionCommands));

            List<CommandInfo> expectedCommands = getExpectedInjectionCommands();
            Assert.assertTrue(injectionCommands.size() > 0);
            Assert.assertEquals(injectionCommands, expectedCommands);
        } catch (MangleException e) {
            log.error("testGetInjectionCommandInfoListForCPU System resource Fault failed with Exception: ", e);
            Assert.assertTrue(false);

        }
    }

    @Test
    public void testGetRemediationCommandInfoList() {
        CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getDockerCpuJvmAgentFaultSpec();
        Mockito.when(endpointClientFactory.getEndPointClient(cpuFaultSpec.getCredentials(), cpuFaultSpec.getEndpoint()))
                .thenReturn(customDockerClient);

        Mockito.when(systemResourceFaultUtils.buildRemediationCommand(any(), any())).thenReturn(remediationCommand);
        try {
            List<CommandInfo> remediationCommands =
                    dockerSystemResourceFaultHelper.getRemediationcommandInfoList(cpuFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(remediationCommands));
            List<CommandInfo> expectedCommands = getExpectedRemediationCommandsforCPUFault();
            Assert.assertEquals(remediationCommands, expectedCommands);
            Assert.assertTrue(remediationCommands.size() > 0);
        } catch (MangleException e) {
            log.error("testGetInjectionCommandInfoListForCPU System resource Fault failed with Exception: ", e);
            Assert.assertTrue(false);

        }

    }

    @Test
    void testGetAgentFaultInjectionScripts() {
        DockerSystemResourceFaultHelper dockerSystemResourceFaultHelper =
                spy(new DockerSystemResourceFaultHelper(endpointClientFactory, systemResourceFaultUtils));
        when(dockerSystemResourceFaultHelper.getFaultInjectionScripts(any())).thenReturn(supportScripts);
        verify(dockerSystemResourceFaultHelper, times(1))
                .getFaultInjectionScripts(any(CommandExecutionFaultSpec.class));
    }

    private List<CommandInfo> getExpectedInjectionCommands() {
        List<CommandInfo> list = new ArrayList<>();
        CommandInfo injectionCmdInfo = new CommandInfo();
        injectionCmdInfo.setCommand(injectionCommand);
        injectionCmdInfo.setIgnoreExitValueCheck(false);
        injectionCmdInfo.setExpectedCommandOutputList(Collections.emptyList());
        injectionCmdInfo.setNoOfRetries(0);
        injectionCmdInfo.setRetryInterval(0);
        injectionCmdInfo.setTimeout(0);
        list.add(injectionCmdInfo);
        return list;
    }

    private List<CommandInfo> getExpectedRemediationCommandsforCPUFault() {
        List<CommandInfo> list = new ArrayList<>();
        CommandInfo remediationCmdInfo = new CommandInfo();
        remediationCmdInfo.setCommand(remediationCommand);
        remediationCmdInfo.setIgnoreExitValueCheck(false);
        remediationCmdInfo.setNoOfRetries(0);
        remediationCmdInfo.setRetryInterval(0);
        remediationCmdInfo.setTimeout(0);
        remediationCmdInfo.setExpectedCommandOutputList(Collections.emptyList());
        Map<String, String> knownFailureMap = new HashMap<>();
        knownFailureMap.put("No such file or directory", "Fault has been remediated already");
        remediationCmdInfo.setKnownFailureMap(knownFailureMap);
        list.add(remediationCmdInfo);
        return list;
    }

}
