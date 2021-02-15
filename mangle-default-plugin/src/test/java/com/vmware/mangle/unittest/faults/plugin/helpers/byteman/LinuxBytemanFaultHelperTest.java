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

package com.vmware.mangle.unittest.faults.plugin.helpers.byteman;

import static com.vmware.mangle.utils.constants.FaultConstants.AGENT_JAR_EXTENSION;
import static com.vmware.mangle.utils.constants.FaultConstants.AGENT_NAME;
import static com.vmware.mangle.utils.constants.FaultConstants.DEFAULT_TEMP_DIR;
import static com.vmware.mangle.utils.constants.FaultConstants.EXTRACT_AGENT_COMMAND;
import static com.vmware.mangle.utils.constants.FaultConstants.FI_ADD_INFO_FAULTID;
import static com.vmware.mangle.utils.constants.FaultConstants.FORWARD_SLASH;
import static com.vmware.mangle.utils.constants.FaultConstants.GET_FAULT_COMMAND_WITH_PORT;
import static com.vmware.mangle.utils.constants.FaultConstants.LOAD_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.PID_AGENT_COMMAND_WITH_PORT;
import static com.vmware.mangle.utils.constants.FaultConstants.PID_ATTACH_MXBEANS_COMMAND_WITH_PORT;
import static com.vmware.mangle.utils.constants.FaultConstants.PORT_9091;
import static com.vmware.mangle.utils.constants.FaultConstants.REMEDIATION_COMMAND_WITH_PORT;
import static com.vmware.mangle.utils.constants.FaultConstants.SUBMIT_COMMAND_WITH_PORT;

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
import com.vmware.mangle.cassandra.model.faults.specs.JVMCodeLevelFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.faults.plugin.helpers.byteman.LinuxBytemanFaultHelper;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.clients.ssh.SSHUtils;
import com.vmware.mangle.utils.constants.FaultConstants;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Test Class for LinuxBytemanFaultHelper
 *
 * @author jayasankarr
 *
 */
@Log4j2
public class LinuxBytemanFaultHelperTest {

    @Mock
    EndpointClientFactory endpointClientFactory;
    @Mock
    SSHUtils sshUtils;

    private LinuxBytemanFaultHelper linuxBytemanFaultHelper;
    private FaultsMockData faultsMockData = new FaultsMockData();

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        faultsMockData = new FaultsMockData();
        linuxBytemanFaultHelper = new LinuxBytemanFaultHelper();
        linuxBytemanFaultHelper.setEndpointClientFactory(endpointClientFactory);
    }

    @Test
    public void testGetExecutor() {
        ICommandExecutor executor = null;
        try {
            CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getLinuxCpuJvmAgentFaultSpec();
            Mockito.when(
                    endpointClientFactory.getEndPointClient(cpuFaultSpec.getCredentials(), cpuFaultSpec.getEndpoint()))
                    .thenReturn(sshUtils);
            executor = linuxBytemanFaultHelper.getExecutor(cpuFaultSpec);
        } catch (MangleException e) {
            log.error("testGetExecutor failed with Exception: ", e);
            Assert.assertTrue(false);
        }
        Assert.assertEquals(executor, sshUtils);
    }

    @Test
    public void testGetJVMAgentInjectionCommandInfoList() {
        try {
            CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getLinuxCpuJvmAgentFaultSpec();
            Mockito.when(
                    endpointClientFactory.getEndPointClient(cpuFaultSpec.getCredentials(), cpuFaultSpec.getEndpoint()))
                    .thenReturn(sshUtils);
            List<CommandInfo> injectionCommands = linuxBytemanFaultHelper.getInjectionCommandInfoList(cpuFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(injectionCommands));
            Assert.assertTrue(injectionCommands.size() > 0);
            Assert.assertEquals(injectionCommands.get(2).getCommand(), String
                    .format(PID_ATTACH_MXBEANS_COMMAND_WITH_PORT, DEFAULT_TEMP_DIR + FORWARD_SLASH, PORT_9091, null));
            Assert.assertEquals(injectionCommands.get(3).getCommand(), String.format(PID_AGENT_COMMAND_WITH_PORT,
                    DEFAULT_TEMP_DIR + FORWARD_SLASH, PORT_9091, LOAD_ARG + " 80"));
        } catch (MangleException e) {
            log.error("testGetInjectionCommandInfoListForCPUJVMAgentFault failed with Exception: ", e);
            Assert.assertTrue(false);


        }
    }

    @Test
    public void testGetJVMAgentRemediationCommandInfoList() throws MangleException {
        CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getLinuxCpuJvmAgentFaultSpec();
        Mockito.when(endpointClientFactory.getEndPointClient(cpuFaultSpec.getCredentials(), cpuFaultSpec.getEndpoint()))
                .thenReturn(sshUtils);
        List<CommandInfo> remediationCommands = linuxBytemanFaultHelper.getRemediationCommandInfoList(cpuFaultSpec);
        log.info(RestTemplateWrapper.objectToJson(remediationCommands));
        Assert.assertEquals(remediationCommands.get(0).getCommand(), String.format(REMEDIATION_COMMAND_WITH_PORT,
                DEFAULT_TEMP_DIR + FORWARD_SLASH, PORT_9091, FI_ADD_INFO_FAULTID));
        Assert.assertEquals(remediationCommands.get(1).getCommand(), String.format(GET_FAULT_COMMAND_WITH_PORT,
                DEFAULT_TEMP_DIR + FORWARD_SLASH, PORT_9091, FI_ADD_INFO_FAULTID));

    }

    @Test
    public void testGetJVMAgentRemediationCommandInfoListforFileHandler() throws MangleException {
        CommandExecutionFaultSpec fileHandlerFaultSpec = faultsMockData.getFilehandlerLeakFaultSpec();
        Mockito.when(endpointClientFactory.getEndPointClient(null, fileHandlerFaultSpec.getEndpoint()))
                .thenReturn(sshUtils);
        List<CommandInfo> remediationCommands =
                linuxBytemanFaultHelper.getRemediationCommandInfoList(fileHandlerFaultSpec);
        log.info(RestTemplateWrapper.objectToJson(remediationCommands));
        Assert.assertEquals(remediationCommands, Collections.emptyList());
    }

    @Test
    public void testGetJVMCodeLevelInjectionCommandInfoList() {
        try {
            JVMCodeLevelFaultSpec springServiceExceptionFaultSpec = faultsMockData.getLinuxJvmCodelevelFaultSpec();
            Mockito.when(endpointClientFactory.getEndPointClient(springServiceExceptionFaultSpec.getCredentials(),
                    springServiceExceptionFaultSpec.getEndpoint())).thenReturn(sshUtils);
            List<CommandInfo> injectionCommands =
                    linuxBytemanFaultHelper.getInjectionCommandInfoList(springServiceExceptionFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(injectionCommands));
            Assert.assertTrue(injectionCommands.size() > 0);
            Assert.assertEquals(injectionCommands.get(0).getCommand(),
                    String.format(EXTRACT_AGENT_COMMAND, DEFAULT_TEMP_DIR + "/", AGENT_NAME + AGENT_JAR_EXTENSION));
            Assert.assertEquals(injectionCommands.get(1).getCommand(), "chmod -R 777 " + DEFAULT_TEMP_DIR + "/"
                    + AGENT_NAME + ";chmod -R 777 " + DEFAULT_TEMP_DIR + "/" + AGENT_NAME + "/*");
            Assert.assertTrue(injectionCommands.get(2).getCommand().contains(String
                    .format(PID_ATTACH_MXBEANS_COMMAND_WITH_PORT, DEFAULT_TEMP_DIR + FORWARD_SLASH, PORT_9091, null)));
            Assert.assertTrue(
                    injectionCommands.get(4).getCommand().toString().contains(String.format(SUBMIT_COMMAND_WITH_PORT,
                            DEFAULT_TEMP_DIR + FORWARD_SLASH, PORT_9091, DEFAULT_TEMP_DIR + "/123456;.btm")));
        } catch (MangleException e) {
            log.error("testGetRemediationCommandInfoListForSpringServiceFault failed with Exception: ", e);
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testGetJVMCodeLevelRemediationCommandInfoList() throws MangleException {

        JVMCodeLevelFaultSpec springServiceExceptionFaultSpec = faultsMockData.getLinuxJvmCodelevelFaultSpec();
        Mockito.when(endpointClientFactory.getEndPointClient(springServiceExceptionFaultSpec.getCredentials(),
                springServiceExceptionFaultSpec.getEndpoint())).thenReturn(sshUtils);
        List<CommandInfo> remediationCommands =
                linuxBytemanFaultHelper.getRemediationCommandInfoList(springServiceExceptionFaultSpec);

        List<CommandInfo> expectedCommands = getJVMCodeLevelRemediationCommandInfoList();
        Assert.assertEquals(remediationCommands, expectedCommands);

        log.info(RestTemplateWrapper.objectToJson(remediationCommands));
        log.info(String.format(SUBMIT_COMMAND_WITH_PORT, DEFAULT_TEMP_DIR, PORT_9091,
                "-u " + DEFAULT_TEMP_DIR + "/123456;.btm"));
        Assert.assertTrue(remediationCommands.get(0).getCommand().contains(String.format(SUBMIT_COMMAND_WITH_PORT,
                DEFAULT_TEMP_DIR + FORWARD_SLASH, PORT_9091, "-u " + DEFAULT_TEMP_DIR + "/123456;.btm")));
    }

    private List<CommandInfo> getJVMCodeLevelRemediationCommandInfoList() {
        CommandInfo faultRemediationRequest = CommandInfo.builder(
                "sudo -u bytemanUser bash -c \"export JAVA_HOME=/usr/java/latest;export PATH=$JAVA_HOME/bin:$PATH;/bin/sh /tmp/"
                        + FaultConstants.AGENT_NAME + "/bin/bmsubmit.sh -p 9091 -u /tmp/123456.btm\"")
                .ignoreExitValueCheck(false)
                .expectedCommandOutputList(Arrays.asList(new String[] { "uninstall RULE 123456" }))
                .knownFailureMap(null).build();

        CommandInfo deleteBytemanRuleRequest =
                CommandInfo.builder("rm -rf /tmp/123456.btm").ignoreExitValueCheck(true).build();

        List<CommandInfo> commandInfoList = new ArrayList<>();
        commandInfoList.add(faultRemediationRequest);
        commandInfoList.add(deleteBytemanRuleRequest);
        return commandInfoList;
    }

    @Test
    void testGetAgentFaultInjectionScripts() {
        CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getLinuxCpuJvmAgentFaultSpec();
        List<SupportScriptInfo> supportScripts = linuxBytemanFaultHelper.getAgentFaultInjectionScripts(cpuFaultSpec);
        Assert.assertEquals(supportScripts.get(1).getScriptFileName(), AGENT_NAME + AGENT_JAR_EXTENSION);
        Assert.assertEquals(supportScripts.get(1).getTargetDirectoryPath(), DEFAULT_TEMP_DIR + FORWARD_SLASH);
    }

}
