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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.testng.Assert.assertEquals;

import static com.vmware.mangle.utils.constants.FaultConstants.AGENT_NAME;
import static com.vmware.mangle.utils.constants.FaultConstants.DEFAULT_TEMP_DIR;
import static com.vmware.mangle.utils.constants.FaultConstants.FORWARD_SLASH;
import static com.vmware.mangle.utils.constants.FaultConstants.PID_ATTACH_MXBEANS_COMMAND_WITH_PORT;
import static com.vmware.mangle.utils.constants.FaultConstants.PORT_9091;
import static com.vmware.mangle.utils.constants.FaultConstants.SUBMIT_COMMAND_WITH_PORT;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMCodeLevelFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandOutputProcessingInfo;
import com.vmware.mangle.faults.plugin.helpers.JavaAgentFaultUtils;
import com.vmware.mangle.faults.plugin.helpers.KnownFailuresHelper;
import com.vmware.mangle.faults.plugin.helpers.byteman.DockerBytemanFaultHelper;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.faults.plugin.utils.PluginUtils;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.utils.DockerCommandUtils;
import com.vmware.mangle.utils.CommandUtils;
import com.vmware.mangle.utils.ConstantsUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.docker.CustomDockerClient;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.constants.FaultConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Test Class for DockerBytemanFaultHelper
 *
 * @author rpraveen
 * @author jayasankarr
 *
 */
@Log4j2
@PrepareForTest(value = { CommandUtils.class, ConstantsUtils.class })
public class DockerBytemanFaultHelperTest extends PowerMockTestCase {

    @Mock
    EndpointClientFactory endpointClientFactory;
    @Mock
    CustomDockerClient customDockerClient;
    @Mock
    JavaAgentFaultUtils javaAgentFaultUtils;
    @Mock
    PluginUtils pluginUtils;
    @Mock
    DockerCommandUtils dockerCommandUtils;
    @Mock
    ICommandExecutor commandExecutor;
    @Mock
    File file;
    @Mock
    CommandExecutionResult result;

    private DockerBytemanFaultHelper dockerBytemanFaultHelper;
    private FaultsMockData faultsMockData = new FaultsMockData();

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        faultsMockData = new FaultsMockData();
        dockerBytemanFaultHelper = new DockerBytemanFaultHelper();
        dockerBytemanFaultHelper.setEndpointClientFactory(endpointClientFactory);
        dockerBytemanFaultHelper.setJavaAgentFaultUtils(javaAgentFaultUtils);
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
            List<CommandInfo> expectedCommands = getJVMAgentInjectionCommandInfo();
            Assert.assertEquals(injectionCommands, expectedCommands);
        } catch (MangleException e) {
            log.error("testGetInjectionCommandInfoListForCPUJVMAgentFault failed with Exception: ", e);
            Assert.assertTrue(false);

        }
    }

    private List<CommandInfo> getJVMAgentInjectionCommandInfo() {
        CommandInfo agentInstallation = CommandInfo
                .builder("/bin/sh /tmp/" + FaultConstants.AGENT_NAME + "/bin/bminstall.sh -p 9091 -s -b null")
                .ignoreExitValueCheck(true)
                .expectedCommandOutputList(Arrays.asList(new String[] { "Started Byteman Listener Successfully",
                        "Agent is already running on requested process" }))
                .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentInstallationRequest()).build();

        List<CommandOutputProcessingInfo> commandOutputProcessingInfoList = new ArrayList<>();
        CommandOutputProcessingInfo commandOutputProcessingInfo = new CommandOutputProcessingInfo();
        commandOutputProcessingInfo.setExtractedPropertyName("faultId");
        commandOutputProcessingInfo.setRegExpression("[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}");
        commandOutputProcessingInfoList.add(commandOutputProcessingInfo);
        CommandInfo faultInjection = CommandInfo.builder(null).ignoreExitValueCheck(false)
                .expectedCommandOutputList(Arrays.asList(new String[] { "Created Fault Successfully" }))
                .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentFaultInjectionRequest())
                .commandOutputProcessingInfoList(commandOutputProcessingInfoList).build();

        List<CommandInfo> commandInfoList = new ArrayList<>();
        commandInfoList.add(agentInstallation);
        commandInfoList.add(faultInjection);
        return commandInfoList;
    }

    @Test
    public void testGetJVMAgentInjectionCommandInfoListWithJavaHomeSet() {
        try {
            CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getDockerCpuJvmAgentFaultSpecV2();
            Mockito.when(endpointClientFactory.getEndPointClient(null, cpuFaultSpec.getEndpoint()))
                    .thenReturn(customDockerClient);
            List<CommandInfo> injectionCommands = dockerBytemanFaultHelper.getInjectionCommandInfoList(cpuFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(injectionCommands));
            List<CommandInfo> expectedCommands = getJVMAgentInjectionCommandInfoListWithJavaHomeSet();
            Assert.assertEquals(injectionCommands, expectedCommands);
        } catch (MangleException e) {
            log.error("testGetInjectionCommandInfoListForCPUJVMAgentFault failed with Exception: ", e);
            Assert.assertTrue(false);
        }
    }

    private List<CommandInfo> getJVMAgentInjectionCommandInfoListWithJavaHomeSet() {
        CommandInfo agentInstallation =
                CommandInfo
                        .builder("export JAVA_HOME=/usr/java/latest&&/bin/sh /tmp/"
                                + FaultConstants.AGENT_NAME + "/bin/bminstall.sh -p 9091 -s -b null")
                        .ignoreExitValueCheck(true)
                        .expectedCommandOutputList(Arrays.asList(new String[] { "Started Byteman Listener Successfully",
                                "Agent is already running on requested process" }))
                        .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentInstallationRequest()).build();

        List<CommandOutputProcessingInfo> commandOutputProcessingInfoList = new ArrayList<>();
        CommandOutputProcessingInfo commandOutputProcessingInfo = new CommandOutputProcessingInfo();
        commandOutputProcessingInfo.setExtractedPropertyName("faultId");
        commandOutputProcessingInfo.setRegExpression("[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}");
        commandOutputProcessingInfoList.add(commandOutputProcessingInfo);
        CommandInfo faultInjection =
                CommandInfo.builder("export JAVA_HOME=/usr/java/latest&&null").ignoreExitValueCheck(false)
                        .expectedCommandOutputList(Arrays.asList(new String[] { "Created Fault Successfully" }))
                        .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentFaultInjectionRequest())
                        .commandOutputProcessingInfoList(commandOutputProcessingInfoList).build();

        List<CommandInfo> commandInfoList = new ArrayList<>();
        commandInfoList.add(agentInstallation);
        commandInfoList.add(faultInjection);
        return commandInfoList;
    }

    @Test
    public void testGetJVMCodeLevelInjectionCommandInfoList() {
        try {
            JVMCodeLevelFaultSpec springServiceExceptionFaultSpec = faultsMockData.getDockerJvmCodelevelFaultSpec();
            Mockito.when(endpointClientFactory.getEndPointClient(null, springServiceExceptionFaultSpec.getEndpoint()))
                    .thenReturn(customDockerClient);
            List<CommandInfo> injectionCommands =
                    dockerBytemanFaultHelper.getInjectionCommandInfoList(springServiceExceptionFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(injectionCommands));
            Assert.assertTrue(injectionCommands.size() > 0);
            Assert.assertTrue(injectionCommands.get(0).getCommand().contains(String
                    .format(PID_ATTACH_MXBEANS_COMMAND_WITH_PORT, DEFAULT_TEMP_DIR + FORWARD_SLASH, PORT_9091, null)));

            Assert.assertTrue(
                    injectionCommands.get(1).getCommand().toString().contains(String.format(SUBMIT_COMMAND_WITH_PORT,
                            DEFAULT_TEMP_DIR + FORWARD_SLASH, PORT_9091, DEFAULT_TEMP_DIR + "/123456;.btm")));
        } catch (MangleException e) {
            log.error("testGetJVMCodeLevelInjectionCommandInfoListForSpringServiceFault failed with Exception: ", e);
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testGetJVMAgentRemediationCommandInfoList() {
        try {
            CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getDockerCpuJvmAgentFaultSpec();
            Mockito.when(endpointClientFactory.getEndPointClient(null, cpuFaultSpec.getEndpoint()))
                    .thenReturn(customDockerClient);
            dockerBytemanFaultHelper.setJavaAgentFaultUtils(new JavaAgentFaultUtils());
            List<CommandInfo> remediationCommands =
                    dockerBytemanFaultHelper.getRemediationCommandInfoList(cpuFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(remediationCommands));
            List<CommandInfo> expectedCommands = getJVMAgentRemediationCommandInfoList();
            Assert.assertEquals(remediationCommands, expectedCommands);
        } catch (MangleException e) {
            log.error("testGetJVMCodeLevelInjectionCommandInfoListForSpringServiceFault failed with Exception: ", e);
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testGetJVMAgentRemediationCommandInfoListforFileHandler() {
        try {
            CommandExecutionFaultSpec fileHandlerFaultSpec = faultsMockData.getFilehandlerLeakFaultSpec();
            Mockito.when(endpointClientFactory.getEndPointClient(null, fileHandlerFaultSpec.getEndpoint()))
                    .thenReturn(customDockerClient);
            dockerBytemanFaultHelper.setJavaAgentFaultUtils(new JavaAgentFaultUtils());
            List<CommandInfo> remediationCommands =
                    dockerBytemanFaultHelper.getRemediationCommandInfoList(fileHandlerFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(remediationCommands));
            Assert.assertEquals(remediationCommands, Collections.emptyList());
        } catch (MangleException e) {
            log.error("testGetJVMAgentRemediationCommandInfoListforFileHandler failed with Exception: ", e);
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testGetJVMAgentRemediationCommandInfoListforThreadLeakFault() {
        try {
            CommandExecutionFaultSpec threadLeakFaultSpec = faultsMockData.getThreadLeakFaultSpec();
            Mockito.when(endpointClientFactory.getEndPointClient(null, threadLeakFaultSpec.getEndpoint()))
                    .thenReturn(customDockerClient);
            dockerBytemanFaultHelper.setJavaAgentFaultUtils(new JavaAgentFaultUtils());
            List<CommandInfo> remediationCommands =
                    dockerBytemanFaultHelper.getRemediationCommandInfoList(threadLeakFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(remediationCommands));
            Assert.assertEquals(remediationCommands, Collections.emptyList());
        } catch (MangleException e) {
            log.error("test Get JVMAgent Remediation CommandInfoList for ThreadLeak failed with Exception: ", e);
            Assert.assertTrue(false);
        }
    }

    private List<CommandInfo> getJVMAgentRemediationCommandInfoList() {
        CommandInfo faultRemediationRequest = CommandInfo
                .builder("/bin/sh /tmp/" + FaultConstants.AGENT_NAME
                        + "/bin/bmsubmit.sh -p 9091 -rf $FI_ADD_INFO_faultId")
                .ignoreExitValueCheck(false)
                .expectedCommandOutputList(Arrays.asList(new String[] { "Received Remediation Request Successfully" }))
                .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentFaultRemediationRequest()).build();

        CommandInfo faultInjection = CommandInfo
                .builder("/bin/sh /tmp/"
                        + FaultConstants.AGENT_NAME + "/bin/bmsubmit.sh -p 9091 -gf $FI_ADD_INFO_faultId")
                .ignoreExitValueCheck(true)
                .expectedCommandOutputList(Arrays.asList(new String[] { "\"faultStatus\":\"COMPLETED\"",
                        "Failed to process request: java.net.ConnectException: Connection refused" }))
                .noOfRetries(6).retryInterval(10).build();

        List<CommandInfo> commandInfoList = new ArrayList<>();
        commandInfoList.add(faultRemediationRequest);
        commandInfoList.add(faultInjection);
        return commandInfoList;
    }

    @Test
    public void testGetJVMCodeLevelRemediationCommandInfoList() {
        try {
            JVMCodeLevelFaultSpec springServiceExceptionFaultSpec = faultsMockData.getDockerJvmCodelevelFaultSpec();
            Mockito.when(endpointClientFactory.getEndPointClient(null, springServiceExceptionFaultSpec.getEndpoint()))
                    .thenReturn(customDockerClient);
            List<CommandInfo> remediationCommands =
                    dockerBytemanFaultHelper.getRemediationCommandInfoList(springServiceExceptionFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(remediationCommands));
            List<CommandInfo> expectedCommands = getJVMCodeLevelRemediationCommandInfoList();
            Assert.assertEquals(remediationCommands, expectedCommands);
        } catch (MangleException e) {
            log.error("testGetJVMCodeLevelRemediationCommandInfoListForSpringServiceFault failed with Exception: ", e);
            Assert.assertTrue(false);
        }
    }

    private List<CommandInfo> getJVMCodeLevelRemediationCommandInfoList() {
        CommandInfo faultRemediationRequest = CommandInfo
                .builder("/bin/sh /tmp/" + FaultConstants.AGENT_NAME + "/bin/bmsubmit.sh -p 9091 -u /tmp/123456.btm")
                .ignoreExitValueCheck(true)
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
        CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getDockerCpuJvmAgentFaultSpec();
        List<SupportScriptInfo> supportScripts = dockerBytemanFaultHelper.getAgentFaultInjectionScripts(cpuFaultSpec);
        Assert.assertEquals(AGENT_NAME, supportScripts.get(1).getScriptFileName());
        Assert.assertEquals(DEFAULT_TEMP_DIR + FORWARD_SLASH, supportScripts.get(1).getTargetDirectoryPath());
    }

    @Test
    void testCheckTaskPrerequisites() throws Exception {
        PowerMockito.mockStatic(ConstantsUtils.class);
        PowerMockito.when(ConstantsUtils.getMangleSupportScriptDirectory()).thenReturn("target" + File.separator);
        dockerBytemanFaultHelper.setPluginUtils(new PluginUtils());
        Mockito.doNothing().when(pluginUtils).copyFileFromJarToDestination(any(), any());
        PowerMockito.mockStatic(CommandUtils.class);
        PowerMockito.when(CommandUtils.runCommand(any(), anyInt())).thenReturn(result);
        Mockito.when(file.exists()).thenReturn(false);
        Mockito.when(result.getExitCode()).thenReturn(0);
        dockerBytemanFaultHelper.checkTaskSpecificPrerequisites();
    }

    @Test
    void testCheckTaskPrerequisitesWithAgentExtractionFailed() throws Exception {
        boolean exceptionCalled = false;
        PowerMockito.mockStatic(ConstantsUtils.class);
        PowerMockito.when(ConstantsUtils.getMangleSupportScriptDirectory()).thenReturn("target" + File.separator);
        dockerBytemanFaultHelper.setPluginUtils(new PluginUtils());
        Mockito.doNothing().when(pluginUtils).copyFileFromJarToDestination(any(), any());
        PowerMockito.mockStatic(CommandUtils.class);
        PowerMockito.when(CommandUtils.runCommand(any(), anyInt())).thenReturn(result);
        Mockito.when(file.exists()).thenReturn(false);
        Mockito.when(result.getExitCode()).thenReturn(1);
        try {
            dockerBytemanFaultHelper.checkTaskSpecificPrerequisites();
        } catch (MangleException e) {
            exceptionCalled = true;
            assertEquals(e.getErrorCode(), ErrorCode.AGENT_EXTRACTION_FAILED);
        }
        Assert.assertTrue(exceptionCalled, "Extraction failed exception is not invoked");

    }
}
