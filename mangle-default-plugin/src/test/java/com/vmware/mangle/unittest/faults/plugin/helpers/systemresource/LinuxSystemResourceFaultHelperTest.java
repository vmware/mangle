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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.vmware.mangle.utils.constants.FaultConstants.DEFAULT_TEMP_DIR;
import static com.vmware.mangle.utils.constants.FaultConstants.LOAD_ARG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import com.vmware.mangle.faults.plugin.helpers.FaultConstants;
import com.vmware.mangle.faults.plugin.helpers.KnownFailuresHelper;
import com.vmware.mangle.faults.plugin.helpers.systemresource.LinuxSystemResourceFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.systemresource.SystemResourceFaultUtils;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.clients.ssh.SSHUtils;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Test Class for LinuxSystemResourceFaultHelper
 *
 * @author jayasankarr
 *
 */
@Log4j2
public class LinuxSystemResourceFaultHelperTest {
    @Mock
    private EndpointClientFactory endpointClientFactory;
    @Mock
    SSHUtils sshUtils;
    @Mock
    private SystemResourceFaultUtils systemResourceFaultUtils;

    private LinuxSystemResourceFaultHelper linuxSystemResourceFaultHelper;

    private FaultsMockData faultsMockData = new FaultsMockData();
    @Mock
    List<SupportScriptInfo> supportScripts;

    private String injectionCommand;
    private String builtInjectionCommand;
    private String agentExtractionCommand;
    private String agentInstallationCommand;
    private String remediationCommand;
    private String builtRemediationCommand;

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        faultsMockData = new FaultsMockData();
        linuxSystemResourceFaultHelper =
                new LinuxSystemResourceFaultHelper(endpointClientFactory, systemResourceFaultUtils);
        CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getLinuxCpuJvmAgentFaultSpec();

        injectionCommand =
                String.format("cd %s;./%s --operation inject --faultname %s --load %s --timeout %s --faultId %s",
                        DEFAULT_TEMP_DIR + "/" + FaultConstants.INFRA_AGENT_NAME_FOLDER, FaultConstants.INFRA_SUBMIT,
                        cpuFaultSpec.getFaultName(), cpuFaultSpec.getArgs().get(LOAD_ARG),
                        cpuFaultSpec.getTimeoutInMilliseconds(), cpuFaultSpec.getFaultName());
        builtInjectionCommand = String.format("%s --operation inject --faultname %s --load %s --timeout %s",
                FaultConstants.INFRA_SUBMIT, cpuFaultSpec.getFaultName(), cpuFaultSpec.getArgs().get(LOAD_ARG),
                cpuFaultSpec.getTimeoutInMilliseconds(), cpuFaultSpec.getFaultName());
        remediationCommand = String.format("cd %s;./%s --operation remediate --faultId %s",
                DEFAULT_TEMP_DIR + "/" + FaultConstants.INFRA_AGENT_NAME_FOLDER, FaultConstants.INFRA_SUBMIT,
                cpuFaultSpec.getFaultName());
        builtRemediationCommand = String.format("%s --operation remediate", FaultConstants.INFRA_SUBMIT);


    }

    @Test
    public void testGetExecutor() {
        ICommandExecutor executor = null;
        try {
            JVMAgentFaultSpec cpuFaultSpec = faultsMockData.getLinuxCpuJvmAgentFaultSpec();
            cpuFaultSpec.setJvmProperties(null);
            Mockito.when(
                    endpointClientFactory.getEndPointClient(cpuFaultSpec.getCredentials(), cpuFaultSpec.getEndpoint()))
                    .thenReturn(sshUtils);
            executor = linuxSystemResourceFaultHelper.getExecutor(cpuFaultSpec);
        } catch (MangleException e) {
            log.error("testGetExecutor failed with Exception: ", e);
            Assert.assertTrue(false);
        }
        Assert.assertEquals(executor, sshUtils);
    }

    @Test
    public void testGetInjectionCommandInfoList() {
        try {
            JVMAgentFaultSpec cpuFaultSpec = faultsMockData.getLinuxCpuJvmAgentFaultSpec();
            cpuFaultSpec.setJvmProperties(null);
            Mockito.when(
                    endpointClientFactory.getEndPointClient(cpuFaultSpec.getCredentials(), cpuFaultSpec.getEndpoint()))
                    .thenReturn(sshUtils);
            Mockito.when(systemResourceFaultUtils.buildInjectionCommand(any(), any()))
                    .thenReturn(builtInjectionCommand);
            Mockito.when(systemResourceFaultUtils
                    .getLinuxPythonAgentExtractCommandInfo(any(CommandExecutionFaultSpec.class)))
                    .thenReturn(CommandInfo.builder(agentExtractionCommand).ignoreExitValueCheck(true)
                            .expectedCommandOutputList(Arrays.asList("")).build());
            Mockito.when(
                    systemResourceFaultUtils.getPythonAgentInstallCommandInfo(any(CommandExecutionFaultSpec.class)))
                    .thenReturn(CommandInfo.builder(agentInstallationCommand).ignoreExitValueCheck(true)
                            .expectedCommandOutputList(Arrays.asList(""))
                            .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentInstallationRequest()).build());
            List<CommandInfo> injectionCommands =
                    linuxSystemResourceFaultHelper.getInjectionCommandInfoList(cpuFaultSpec);
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
    public void testGetRemediationCommandInfoList() throws MangleException {
        JVMAgentFaultSpec cpuFaultSpec = faultsMockData.getLinuxCpuJvmAgentFaultSpec();
        cpuFaultSpec.setJvmProperties(null);
        Mockito.when(endpointClientFactory.getEndPointClient(cpuFaultSpec.getCredentials(), cpuFaultSpec.getEndpoint()))
                .thenReturn(sshUtils);
        Mockito.when(systemResourceFaultUtils.buildRemediationCommand(any(), any()))
                .thenReturn(builtRemediationCommand);
        when(systemResourceFaultUtils.isManualRemediationSupported(anyString())).thenReturn(true);
        try {
            List<CommandInfo> remediationCommands =
                    linuxSystemResourceFaultHelper.getRemediationcommandInfoList(cpuFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(remediationCommands));
            List<CommandInfo> expectedCommands = getExpectedRemediationCommandsforCPUFault();
            Assert.assertTrue(remediationCommands.size() > 0);
            Assert.assertEquals(remediationCommands.get(0).getCommand(), expectedCommands.get(0).getCommand());
        } catch (MangleException e) {
            log.error("testGetInjectionCommandInfoListForCPU System resource Fault failed with Exception: ", e);
            Assert.assertTrue(false);

        }
    }

    @Test
    public void testGetRemediationCommandInfoListforFileHandler() {
        try {
            CommandExecutionFaultSpec fileHandlerFaultSpec = faultsMockData.getFilehandlerLeakFaultSpec();
            Mockito.when(endpointClientFactory.getEndPointClient(null, fileHandlerFaultSpec.getEndpoint()))
                    .thenReturn(sshUtils);
            when(systemResourceFaultUtils.isManualRemediationSupported(anyString())).thenReturn(false);
            List<CommandInfo> remediationCommands =
                    linuxSystemResourceFaultHelper.getRemediationcommandInfoList(fileHandlerFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(remediationCommands));
            Assert.assertEquals(remediationCommands, Collections.emptyList());
        } catch (MangleException e) {
            log.error("testGetRemediationCommandInfoListforFileHandler failed with Exception: ", e);
            Assert.assertTrue(false);
        }
    }

    @Test
    void testGetAgentFaultInjectionScripts() {
        CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getDockerCpuJvmAgentFaultSpec();
        LinuxSystemResourceFaultHelper linuxSystemResourceFaultHelper =
                new LinuxSystemResourceFaultHelper(endpointClientFactory, systemResourceFaultUtils);
        Mockito.when(systemResourceFaultUtils.getAgentFaultScriptsPython(any())).thenReturn(supportScripts);
        linuxSystemResourceFaultHelper.getFaultInjectionScripts(cpuFaultSpec);
        verify(systemResourceFaultUtils, times(1)).getAgentFaultScriptsPython(any(String.class));

    }

    private List<CommandInfo> getExpectedInjectionCommands() {
        List<CommandInfo> list = new ArrayList<>();
        CommandInfo injectionCmdInfo = CommandInfo.builder(injectionCommand).ignoreExitValueCheck(false)
                .expectedCommandOutputList(Collections.emptyList()).noOfRetries(3).retryInterval(2).timeout(0)
                .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfSystemResourceFaultInjectionRequest()).build();
        list.add(CommandInfo.builder(agentExtractionCommand).ignoreExitValueCheck(true)
                .expectedCommandOutputList(Arrays.asList("")).build());
        list.add(CommandInfo.builder(agentInstallationCommand).ignoreExitValueCheck(true)
                .expectedCommandOutputList(Arrays.asList(""))
                .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentInstallationRequest()).build());
        list.add(injectionCmdInfo);
        return list;
    }

    private List<CommandInfo> getExpectedRemediationCommandsforCPUFault() {
        List<CommandInfo> list = new ArrayList<>();
        CommandInfo remediationCmdInfo = CommandInfo.builder(remediationCommand).ignoreExitValueCheck(false)
                .noOfRetries(0).retryInterval(0).timeout(0).expectedCommandOutputList(Collections.emptyList()).build();
        list.add(remediationCmdInfo);
        return list;
    }
}
