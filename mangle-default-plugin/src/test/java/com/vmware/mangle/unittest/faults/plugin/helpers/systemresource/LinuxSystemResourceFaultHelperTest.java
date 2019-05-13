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

import static com.vmware.mangle.utils.constants.FaultConstants.DEFAULT_TEMP_DIR;
import static com.vmware.mangle.utils.constants.FaultConstants.LOAD_ARG;

import java.util.ArrayList;
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

    private String remediationCommand;

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        faultsMockData = new FaultsMockData();
        linuxSystemResourceFaultHelper =
                new LinuxSystemResourceFaultHelper(endpointClientFactory, systemResourceFaultUtils);
        CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getLinuxCpuJvmAgentFaultSpec();
        injectionCommand = String.format("%s/cpuburn.sh --operation=inject --load=%s --timeout=%s", DEFAULT_TEMP_DIR,
                cpuFaultSpec.getArgs().get(LOAD_ARG), cpuFaultSpec.getTimeoutInMilliseconds()).toString();
        remediationCommand = String.format("/tmp/cpuburn.sh --operation=remediate");
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
            Mockito.when(systemResourceFaultUtils.buildInjectionCommand(any(), any())).thenReturn(injectionCommand);
            List<CommandInfo> injectionCommands =
                    linuxSystemResourceFaultHelper.getInjectionCommandInfoList(cpuFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(injectionCommands));
            List<CommandInfo> expectedCommands = getExpectedInjectionCommands();
            Assert.assertTrue(injectionCommands.size() > 0);
            Assert.assertEquals(injectionCommands.get(0).getCommand(), expectedCommands.get(0).getCommand());
        } catch (MangleException e) {
            log.error("testGetInjectionCommandInfoListForCPU System resource Fault failed with Exception: ", e);
            Assert.assertTrue(false);


        }
    }


    @Test
    public void testGetRemediationCommandInfoList() {
        JVMAgentFaultSpec cpuFaultSpec = faultsMockData.getLinuxCpuJvmAgentFaultSpec();
        cpuFaultSpec.setJvmProperties(null);
        Mockito.when(endpointClientFactory.getEndPointClient(cpuFaultSpec.getCredentials(), cpuFaultSpec.getEndpoint()))
                .thenReturn(sshUtils);
        Mockito.when(systemResourceFaultUtils.buildRemediationCommand(any(), any())).thenReturn(remediationCommand);
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
    void testGetAgentFaultInjectionScripts() {

        LinuxSystemResourceFaultHelper linuxSystemResourceFaultHelper =
                spy(new LinuxSystemResourceFaultHelper(endpointClientFactory, systemResourceFaultUtils));
        when(linuxSystemResourceFaultHelper.getFaultInjectionScripts(any())).thenReturn(supportScripts);
        verify(linuxSystemResourceFaultHelper, times(1)).getFaultInjectionScripts(any(CommandExecutionFaultSpec.class));
    }

    private List<CommandInfo> getExpectedInjectionCommands() {
        List<CommandInfo> list = new ArrayList<>();
        CommandInfo injectionCmdInfo = new CommandInfo();
        injectionCmdInfo.setCommand(injectionCommand);
        injectionCmdInfo.setIgnoreExitValueCheck(false);
        injectionCmdInfo.setExpectedCommandOutputList(Collections.EMPTY_LIST);
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
        remediationCmdInfo.setExpectedCommandOutputList(Collections.EMPTY_LIST);
        list.add(remediationCmdInfo);
        return list;
    }
}
