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

import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.AGENT_NAME;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.DEFAULT_TEMP_DIR;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.FI_ADD_INFO_FAULTID;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.GET_FAULT_COMMAND;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.PID_ATTACH_MXBEANS_COMMAND_WITH_PORT;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.PORT_9091;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.REMEDIATION_COMMAND_WITH_PORT;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.SUBMIT_COMMAND_WITH_PORT;

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
import com.vmware.mangle.faults.plugin.helpers.JavaAgentFaultUtils;
import com.vmware.mangle.faults.plugin.helpers.byteman.DockerBytemanFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.docker.DockerCommandUtils;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.docker.CustomDockerClient;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Test Class for DockerBytemanFaultHelper
 *
 * @author rpraveen
 *
 */
@Log4j2
public class DockerBytemanFaultHelperTest {

    @Mock
    EndpointClientFactory endpointClientFactory;
    @Mock
    CustomDockerClient customDockerClient;

    @Mock
    JavaAgentFaultUtils javaAgentFaultUtils;

    @Mock
    DockerCommandUtils dockerCommandUtils;

    @Mock
    ICommandExecutor commandExecutor;

    private DockerBytemanFaultHelper dockerBytemanFaultHelper;
    private FaultsMockData faultsMockData = new FaultsMockData();

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        faultsMockData = new FaultsMockData();
        dockerBytemanFaultHelper = new DockerBytemanFaultHelper(endpointClientFactory, javaAgentFaultUtils);
    }

    @Test
    public void testGetExecutor() {
        ICommandExecutor executor = null;
        try {
            CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getDockerCpuJvmAgentFaultSpec();
            Mockito.when(endpointClientFactory.getEndPointClient(null, cpuFaultSpec.getEndpoint()))
                    .thenReturn(customDockerClient);
            executor = dockerBytemanFaultHelper.getExecutor(cpuFaultSpec);
        } catch (MangleException e) {
            log.error("testGetExecutor failed with Exception: ", e);
            Assert.assertTrue(false);
        }
        Assert.assertNotNull(executor);
    }

    @Test
    public void testGetJVMAgentInjectionCommandInfoList() {
        try {
            CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getDockerCpuJvmAgentFaultSpec();
            Mockito.when(endpointClientFactory.getEndPointClient(null, cpuFaultSpec.getEndpoint()))
                    .thenReturn(customDockerClient);
            List<CommandInfo> injectionCommands = dockerBytemanFaultHelper.getInjectionCommandInfoList(cpuFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(injectionCommands));
            Assert.assertTrue(injectionCommands.size() > 0);
        } catch (MangleException e) {
            log.error("testGetInjectionCommandInfoListForCPUJVMAgentFault failed with Exception: ", e);
            Assert.assertTrue(false);

        }
    }

    @Test
    public void testGetJVMAgentInjectionCommandInfoListWithJavaHomeSet() {
        try {
            CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getDockerCpuJvmAgentFaultSpecV2();
            Mockito.when(endpointClientFactory.getEndPointClient(null, cpuFaultSpec.getEndpoint()))
                    .thenReturn(customDockerClient);
            List<CommandInfo> injectionCommands = dockerBytemanFaultHelper.getInjectionCommandInfoList(cpuFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(injectionCommands));
            Assert.assertTrue(injectionCommands.size() > 0);
        } catch (MangleException e) {
            log.error("testGetInjectionCommandInfoListForCPUJVMAgentFault failed with Exception: ", e);
            Assert.assertTrue(false);

        }
    }


    @Test
    public void testGetJVMCodeLevelInjectionCommandInfoList() {
        try {
            JVMCodeLevelFaultSpec springServiceExceptionFaultSpec =
                    faultsMockData.getDockerJvmCodelevelFaultSpec();
            Mockito.when(
                    endpointClientFactory.getEndPointClient(null, springServiceExceptionFaultSpec.getEndpoint()))
                    .thenReturn(customDockerClient);
            List<CommandInfo> injectionCommands =
                    dockerBytemanFaultHelper.getInjectionCommandInfoList(springServiceExceptionFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(injectionCommands));
            Assert.assertTrue(injectionCommands.size() > 0);
            Assert.assertTrue(injectionCommands.get(0).getCommand().toString().contains(".btm"));
            Assert.assertTrue(injectionCommands.get(1).getCommand()
                    .contains(String.format(PID_ATTACH_MXBEANS_COMMAND_WITH_PORT, DEFAULT_TEMP_DIR, PORT_9091, null)));

            Assert.assertTrue(injectionCommands.get(2).getCommand().toString().contains(String
                    .format(SUBMIT_COMMAND_WITH_PORT, DEFAULT_TEMP_DIR, PORT_9091, DEFAULT_TEMP_DIR + "/123456;.btm")));
        } catch (MangleException e) {
            log.error("testGetJVMCodeLevelInjectionCommandInfoListForSpringServiceFault failed with Exception: ", e);
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testGetJVMAgentRemediationCommandInfoList() {
        String remediation =
                String.format(REMEDIATION_COMMAND_WITH_PORT, DEFAULT_TEMP_DIR, PORT_9091, FI_ADD_INFO_FAULTID);
        String getFault = String.format(GET_FAULT_COMMAND, DEFAULT_TEMP_DIR, FI_ADD_INFO_FAULTID);

        try {
            CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getDockerCpuJvmAgentFaultSpec();
            Mockito.when(endpointClientFactory.getEndPointClient(null, cpuFaultSpec.getEndpoint()))
                    .thenReturn(customDockerClient);

            Mockito.when(javaAgentFaultUtils.buildRemediationCommand(cpuFaultSpec.getInjectionHomeDir(), PORT_9091))
                    .thenReturn(remediation);

            Mockito.when(javaAgentFaultUtils.buildGetFaultCommand(cpuFaultSpec.getInjectionHomeDir(), PORT_9091))
                    .thenReturn(getFault);

            List<CommandInfo> remediationCommands =
                    dockerBytemanFaultHelper.getRemediationCommandInfoList(cpuFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(remediationCommands));
            Assert.assertEquals(remediationCommands.get(0).getCommand().toString(),
                    String.format(REMEDIATION_COMMAND_WITH_PORT, DEFAULT_TEMP_DIR, PORT_9091, FI_ADD_INFO_FAULTID));
            Assert.assertEquals(remediationCommands.get(1).getCommand().toString(),
                    String.format(GET_FAULT_COMMAND, DEFAULT_TEMP_DIR, FI_ADD_INFO_FAULTID));
        } catch (MangleException e) {
            log.error("testGetJVMCodeLevelInjectionCommandInfoListForSpringServiceFault failed with Exception: ", e);
            Assert.assertTrue(false);
        }

    }

    @Test
    public void testGetJVMCodeLevelRemediationCommandInfoList() {
        try {
            JVMCodeLevelFaultSpec springServiceExceptionFaultSpec =
                    faultsMockData.getDockerJvmCodelevelFaultSpec();
            Mockito.when(
                    endpointClientFactory.getEndPointClient(null, springServiceExceptionFaultSpec.getEndpoint()))
                    .thenReturn(customDockerClient);
            List<CommandInfo> remediationCommands =
                    dockerBytemanFaultHelper.getRemediationCommandInfoList(springServiceExceptionFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(remediationCommands));
            Assert.assertTrue(remediationCommands.get(0).getCommand().contains(String.format(SUBMIT_COMMAND_WITH_PORT,
                    DEFAULT_TEMP_DIR, PORT_9091, "-u " + DEFAULT_TEMP_DIR + "/123456;.btm")));
        } catch (MangleException e) {
            log.error("testGetJVMCodeLevelRemediationCommandInfoListForSpringServiceFault failed with Exception: ", e);
            Assert.assertTrue(false);
        }
    }

    @Test
    void testGetAgentFaultInjectionScripts() {
        CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getDockerCpuJvmAgentFaultSpec();
        List<SupportScriptInfo> supportScripts = dockerBytemanFaultHelper.getAgentFaultInjectionScripts(cpuFaultSpec);
        Assert.assertEquals(AGENT_NAME, supportScripts.get(0).getScriptFileName());
        Assert.assertEquals(DEFAULT_TEMP_DIR, supportScripts.get(0).getTargetDirectoryPath());
    }

}
