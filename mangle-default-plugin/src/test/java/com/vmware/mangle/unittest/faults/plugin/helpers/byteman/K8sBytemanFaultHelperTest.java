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
import static org.testng.Assert.assertTrue;

import static com.vmware.mangle.utils.constants.FaultConstants.AGENT_NAME;

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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.CpuFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMAgentFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandOutputProcessingInfo;
import com.vmware.mangle.faults.plugin.helpers.JavaAgentFaultUtils;
import com.vmware.mangle.faults.plugin.helpers.KnownFailuresHelper;
import com.vmware.mangle.faults.plugin.helpers.byteman.K8sBytemanFaultHelper;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.faults.plugin.utils.PluginUtils;
import com.vmware.mangle.services.enums.BytemanFaultType;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.CommandUtils;
import com.vmware.mangle.utils.ConstantsUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.kubernetes.KubernetesCommandLineClient;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.constants.FaultConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Test Class for K8sSystemResourceFaultHelper
 *
 * @author hkilari
 *
 */
@Log4j2
@PrepareForTest(value = { CommandUtils.class, ConstantsUtils.class })
public class K8sBytemanFaultHelperTest extends PowerMockTestCase {
    @Mock
    private EndpointClientFactory endpointClientFactory;
    @Mock
    KubernetesCommandLineClient kubernetesCommandLineClient;
    @Mock
    PluginUtils pluginUtils;

    private K8sBytemanFaultHelper k8sBytemanFaultHelper;

    private FaultsMockData faultsMockData = new FaultsMockData();
    @Mock
    List<SupportScriptInfo> supportScripts;

    @Mock
    File file;

    @Mock
    CommandExecutionResult result;
    JavaAgentFaultUtils javaAgentFaultUtils;

    @BeforeMethod
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        faultsMockData = new FaultsMockData();
        javaAgentFaultUtils = PowerMockito.spy(new JavaAgentFaultUtils());
        k8sBytemanFaultHelper = new K8sBytemanFaultHelper(endpointClientFactory, javaAgentFaultUtils, pluginUtils);
    }

    @Test
    public void testGetExecutor() {
        ICommandExecutor executor = null;
        try {
            JVMAgentFaultSpec cpuFaultSpec = faultsMockData.getK8SCPUFaultSpec();
            cpuFaultSpec.setJvmProperties(null);
            Mockito.when(
                    endpointClientFactory.getEndPointClient(cpuFaultSpec.getCredentials(), cpuFaultSpec.getEndpoint()))
                    .thenReturn(kubernetesCommandLineClient);
            executor = k8sBytemanFaultHelper.getExecutor(cpuFaultSpec);
        } catch (MangleException e) {
            log.error("testGetExecutor failed with Exception: ", e);
            Assert.assertTrue(false);
        }
        Assert.assertEquals(executor, kubernetesCommandLineClient);
    }

    @Test
    public void testGetJVMAgentInjectionCommandInfoListForCPUFault() {

        try {
            CommandExecutionFaultSpec cpuFaultSpec = getTestFaultSpecForCPUFault();
            Mockito.when(
                    endpointClientFactory.getEndPointClient(cpuFaultSpec.getCredentials(), cpuFaultSpec.getEndpoint()))
                    .thenReturn(kubernetesCommandLineClient);
            List<CommandInfo> injectionCommands = k8sBytemanFaultHelper.getInjectionCommandInfoList(cpuFaultSpec);
            List<CommandInfo> expectedCommands = getExpectedInjectionCommands();
            log.info(RestTemplateWrapper.objectToJson(injectionCommands));
            assertEquals(injectionCommands, expectedCommands);
        } catch (MangleException e) {
            log.error("testGetJVMAgentInjectionCommandInfoList For CPU Fault on K8s failed with Exception: ", e);
            assertTrue(false);
        }
    }

    @Test
    public void testGetJVMAgentInjectionCommandInfoListForCPUFaultWithCustomJavaHome() {
        try {
            CommandExecutionFaultSpec cpuFaultSpec = getTestFaultSpecForCPUFault();
            String javaHomePath = "/usr/bin";
            ((CpuFaultSpec) cpuFaultSpec).getJvmProperties().setJavaHomePath(javaHomePath);
            cpuFaultSpec.getArgs().put(FaultConstants.JAVA_HOME_PATH, javaHomePath);
            Mockito.when(
                    endpointClientFactory.getEndPointClient(cpuFaultSpec.getCredentials(), cpuFaultSpec.getEndpoint()))
                    .thenReturn(kubernetesCommandLineClient);
            List<CommandInfo> injectionCommands = k8sBytemanFaultHelper.getInjectionCommandInfoList(cpuFaultSpec);
            List<CommandInfo> expectedCommands = getExpectedInjectionCommandsForJavaHomePath();
            log.info(RestTemplateWrapper.objectToJson(injectionCommands));
            assertEquals(injectionCommands, expectedCommands);
        } catch (MangleException e) {
            log.error("testGetJVMAgentInjectionCommandInfoList For CPU Fault on K8s failed with Exception: ", e);
            assertTrue(false);
        }
    }

    @Test
    public void testGetJVMAgentRemediationCommandInfoListForCPUFault() {
        CommandExecutionFaultSpec cpuFaultSpec = getTestFaultSpecForCPUFault();

        Mockito.when(endpointClientFactory.getEndPointClient(cpuFaultSpec.getCredentials(), cpuFaultSpec.getEndpoint()))
                .thenReturn(kubernetesCommandLineClient);
        try {
            List<CommandInfo> remediationCommands = k8sBytemanFaultHelper.getRemediationCommandInfoList(cpuFaultSpec);
            List<CommandInfo> expectedCommands = getExpectedRemediationCommandsforCPUFault();
            log.info(RestTemplateWrapper.objectToJson(remediationCommands));
            assertEquals(remediationCommands, expectedCommands);
        } catch (MangleException e) {
            log.error("testGetJVMAgentRemediationCommandInfoList For CPU Fault on K8s failed with Exception: ", e);
            assertTrue(false);
        }
    }

    @Test
    public void testGetJVMAgentRemediationCommandInfoListforFileHandler() {
        CommandExecutionFaultSpec fileHandlerFaultSpec = faultsMockData.getFilehandlerLeakFaultSpec();
        Mockito.when(endpointClientFactory.getEndPointClient(null, fileHandlerFaultSpec.getEndpoint()))
                .thenReturn(kubernetesCommandLineClient);
        List<CommandInfo> remediationCommands = null;
        try {
            remediationCommands = k8sBytemanFaultHelper.getRemediationCommandInfoList(fileHandlerFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(remediationCommands));
            Assert.assertEquals(remediationCommands, Collections.emptyList());
        } catch (MangleException e) {
            log.error("testGetJVMAgentRemediationCommandInfoListforFileHandler failed with Exception: ", e);
            Assert.assertTrue(false);
        }

    }

    @Test
    public void testGetJVMAgentInjectionCommandInfoListForSpringExceptionFault() {
        try {
            CommandExecutionFaultSpec springExceptionFaultSpec = getK8sSpringExceptionJVMCodeLevelFaultSpec();
            Mockito.when(endpointClientFactory.getEndPointClient(springExceptionFaultSpec.getCredentials(),
                    springExceptionFaultSpec.getEndpoint())).thenReturn(kubernetesCommandLineClient);
            List<CommandInfo> injectionCommands =
                    k8sBytemanFaultHelper.getInjectionCommandInfoList(springExceptionFaultSpec);
            List<CommandInfo> expectedCommands = getExpectedInjectionCommandsForSpringException();
            log.info(RestTemplateWrapper.objectToJson(injectionCommands));
            assertEquals(injectionCommands, expectedCommands);
        } catch (MangleException e) {
            log.error(
                    "testGetJVMAgentInjectionCommandInfoList For Spring Exception Fault on K8s failed with Exception: ",
                    e);
            assertTrue(false);
        }
    }


    @Test
    public void testGetJVMCodeLevelFaultCommandInfoListForSpringExceptionFault() {
        CommandExecutionFaultSpec cpuFaultSpec = getK8sSpringExceptionJVMCodeLevelFaultSpec();

        Mockito.when(endpointClientFactory.getEndPointClient(cpuFaultSpec.getCredentials(), cpuFaultSpec.getEndpoint()))
                .thenReturn(kubernetesCommandLineClient);
        try {
            List<CommandInfo> remediationCommands = k8sBytemanFaultHelper.getRemediationCommandInfoList(cpuFaultSpec);
            List<CommandInfo> expectedCommands = getExpectedRemediationCommandsforSpringLatency();
            log.info(RestTemplateWrapper.objectToJson(remediationCommands));
            assertEquals(remediationCommands, expectedCommands);
        } catch (MangleException e) {
            log.error(
                    "testGetJVMCodeLevelFaultCommandInfoList For Spring Exception Fault on K8s failed with Exception: ",
                    e);
            assertTrue(false);
        }
    }


    private CommandExecutionFaultSpec getK8sSpringExceptionJVMCodeLevelFaultSpec() {
        CommandExecutionFaultSpec springExceptionFaultSpec =
                faultsMockData.getK8sSpringExceptionJVMCodeLevelFaultSpec();
        springExceptionFaultSpec.getArgs().put(FaultConstants.FAULT_NAME_ARG,
                BytemanFaultType.SPRING_SERVICE_EXCEPTION.toString());
        springExceptionFaultSpec.getArgs().put(FaultConstants.TIMEOUT_IN_MILLI_SEC_ARG, "20000");
        springExceptionFaultSpec.getK8sArguments().setPodInAction("testPod");
        return springExceptionFaultSpec;
    }

    private CommandExecutionFaultSpec getTestFaultSpecForCPUFault() {
        CommandExecutionFaultSpec springExceptionFaultSpec = faultsMockData.getK8SCPUFaultSpec();
        springExceptionFaultSpec.getArgs().put(FaultConstants.FAULT_NAME_ARG, "cpuFault");
        springExceptionFaultSpec.getArgs().put(FaultConstants.TIMEOUT_IN_MILLI_SEC_ARG, "20000");
        springExceptionFaultSpec.getK8sArguments().setPodInAction("testPod");
        return springExceptionFaultSpec;
    }

    private List<CommandInfo> getExpectedRemediationCommandsforCPUFault() {
        List<CommandInfo> list = new ArrayList<>();
        CommandInfo remediationCommand = new CommandInfo();
        remediationCommand.setCommand("exec -it testPod -c testContainer -- /bin/sh /testDirectory/" + AGENT_NAME
                + "/bin/bmsubmit.sh -p 9091 -rf $FI_ADD_INFO_faultId");
        remediationCommand.setIgnoreExitValueCheck(false);
        remediationCommand
                .setExpectedCommandOutputList(Arrays.asList(new String("Received Remediation Request Successfully")));
        remediationCommand.setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentFaultRemediationRequest());
        remediationCommand.setNoOfRetries(0);
        remediationCommand.setRetryInterval(0);
        remediationCommand.setTimeout(0);
        list.add(remediationCommand);

        CommandInfo remediationVerificationCommand = new CommandInfo();
        remediationVerificationCommand.setCommand("exec -it testPod -c testContainer -- /bin/sh /testDirectory/"
                + AGENT_NAME + "/bin/bmsubmit.sh -p 9091 -gf $FI_ADD_INFO_faultId");
        remediationVerificationCommand.setIgnoreExitValueCheck(true);
        remediationVerificationCommand.setExpectedCommandOutputList(Arrays.asList("\"faultStatus\":\"COMPLETED\"",
                "Failed to process request: java.net.ConnectException: Connection refused"));
        remediationVerificationCommand.setNoOfRetries(6);
        remediationVerificationCommand.setRetryInterval(10);
        remediationVerificationCommand.setTimeout(0);
        list.add(remediationVerificationCommand);
        return list;
    }

    private List<CommandInfo> getExpectedRemediationCommandsforSpringLatency() {
        List<CommandInfo> list = new ArrayList<>();
        CommandInfo remediationCommand = new CommandInfo();
        remediationCommand.setCommand("exec -it testPod -c testContainer -- /bin/sh /tmp/" + AGENT_NAME
                + "/bin/bmsubmit.sh -p 9091 -u /tmp/" + AGENT_NAME + "/123456.btm ");
        remediationCommand.setIgnoreExitValueCheck(true);
        remediationCommand.setExpectedCommandOutputList(Arrays.asList(new String("uninstall RULE 123456")));
        remediationCommand.setNoOfRetries(0);
        remediationCommand.setRetryInterval(0);
        remediationCommand.setTimeout(0);

        CommandInfo deleteBytemanRuleCommand = new CommandInfo();
        deleteBytemanRuleCommand.setCommand("rm -rf /tmp/" + AGENT_NAME + "/123456.btm");
        deleteBytemanRuleCommand.setIgnoreExitValueCheck(true);
        list.add(remediationCommand);
        list.add(deleteBytemanRuleCommand);
        return list;
    }

    private List<CommandInfo> getExpectedInjectionCommands() {
        List<CommandInfo> list = new ArrayList<>();
        CommandInfo copyAgentCommand = new CommandInfo();
        copyAgentCommand.setCommand("cp " + ConstantsUtils.getMangleSupportScriptDirectory() + AGENT_NAME
                + " testPod:/testDirectory/" + AGENT_NAME + " -c testContainer");
        copyAgentCommand.setIgnoreExitValueCheck(false);
        copyAgentCommand.setNoOfRetries(0);
        copyAgentCommand.setRetryInterval(0);
        copyAgentCommand.setTimeout(0);
        copyAgentCommand.setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentCopyOnK8sPod());

        CommandInfo installAgentCommand = new CommandInfo();
        installAgentCommand.setCommand("exec -it testPod -c testContainer -- /bin/sh /testDirectory/" + AGENT_NAME
                + "/bin/bminstall.sh -p 9091 -s -b null");
        installAgentCommand.setIgnoreExitValueCheck(true);
        installAgentCommand.setExpectedCommandOutputList(Arrays.asList(new String[] {
                "Started Byteman Listener Successfully", "Agent is already running on requested process" }));
        installAgentCommand.setNoOfRetries(10);
        installAgentCommand.setRetryInterval(5);
        installAgentCommand.setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentInstallationRequest());
        installAgentCommand.setTimeout(0);

        CommandInfo enableTroubleshootingCommand = new CommandInfo();
        enableTroubleshootingCommand.setCommand("exec -it testPod -c testContainer -- /bin/sh /testDirectory/"
                + AGENT_NAME + "/bin/bmsubmit.sh -p 9091 -enableTroubleshooting");
        enableTroubleshootingCommand.setIgnoreExitValueCheck(true);
        enableTroubleshootingCommand.setExpectedCommandOutputList(Arrays.asList(new String[] {
                "install rule Trace - Capture Troubleshooting bundle.", "Troubleshooting Already Enabled." }));
        enableTroubleshootingCommand.setNoOfRetries(10);
        enableTroubleshootingCommand.setRetryInterval(5);
        enableTroubleshootingCommand.setTimeout(0);

        CommandInfo injectionCommand = new CommandInfo();
        injectionCommand.setCommand("exec -it testPod -c testContainer -- /bin/sh /testDirectory/" + AGENT_NAME
                + "/bin/bmsubmit.sh -p 9091 -if __faultName cpuFault __load 80 __timeOutInMilliSeconds 20000");
        injectionCommand.setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentFaultInjectionRequest());
        injectionCommand.setIgnoreExitValueCheck(false);
        injectionCommand.setExpectedCommandOutputList(Arrays.asList(new String[] { "Created Fault Successfully" }));
        injectionCommand.setNoOfRetries(10);
        injectionCommand.setRetryInterval(5);
        injectionCommand.setTimeout(0);
        List<CommandOutputProcessingInfo> commandOutputProcessingInfoList = new ArrayList<>();
        CommandOutputProcessingInfo commandOutputProcessingInfo = new CommandOutputProcessingInfo();
        commandOutputProcessingInfo.setRegExpression("[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}");
        commandOutputProcessingInfo.setExtractedPropertyName("faultId");
        commandOutputProcessingInfoList.add(commandOutputProcessingInfo);
        injectionCommand.setCommandOutputProcessingInfoList(commandOutputProcessingInfoList);

        list.add(copyAgentCommand);
        list.add(installAgentCommand);
        list.add(enableTroubleshootingCommand);
        list.add(injectionCommand);
        return list;
    }

    private List<CommandInfo> getExpectedInjectionCommandsForJavaHomePath() {
        List<CommandInfo> list = new ArrayList<>();
        CommandInfo copyAgentCommand = new CommandInfo();
        copyAgentCommand.setCommand("cp " + ConstantsUtils.getMangleSupportScriptDirectory() + AGENT_NAME
                + " testPod:/testDirectory/" + AGENT_NAME + " -c testContainer");
        copyAgentCommand.setIgnoreExitValueCheck(false);
        copyAgentCommand.setNoOfRetries(0);
        copyAgentCommand.setRetryInterval(0);
        copyAgentCommand.setTimeout(0);
        copyAgentCommand.setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentCopyOnK8sPod());

        CommandInfo installAgentCommand = new CommandInfo();
        //exec -it testPod -c testContainer -- /bin/sh -c "export JAVA_HOME=/usr/bin && /testDirectory/mangle-java-agent-2.0.0/bin/bminstall.sh -p 9091 -s -b null"
        installAgentCommand.setCommand(
                "exec -it testPod -c testContainer -- /bin/sh -c \"export JAVA_HOME=/usr/bin && /testDirectory/"
                        + AGENT_NAME + "/bin/bminstall.sh -p 9091 -s -b null\"");
        installAgentCommand.setIgnoreExitValueCheck(true);
        installAgentCommand.setExpectedCommandOutputList(Arrays.asList(new String[] {
                "Started Byteman Listener Successfully", "Agent is already running on requested process" }));
        installAgentCommand.setNoOfRetries(10);
        installAgentCommand.setRetryInterval(5);
        installAgentCommand.setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentInstallationRequest());
        installAgentCommand.setTimeout(0);

        CommandInfo enableTroubleshootingCommand = new CommandInfo();
        enableTroubleshootingCommand.setCommand("exec -it testPod -c testContainer -- /bin/sh /testDirectory/"
                + AGENT_NAME + "/bin/bmsubmit.sh -p 9091 -enableTroubleshooting");
        enableTroubleshootingCommand.setIgnoreExitValueCheck(true);
        enableTroubleshootingCommand.setExpectedCommandOutputList(Arrays.asList(new String[] {
                "install rule Trace - Capture Troubleshooting bundle.", "Troubleshooting Already Enabled." }));
        enableTroubleshootingCommand.setNoOfRetries(10);
        enableTroubleshootingCommand.setRetryInterval(5);
        enableTroubleshootingCommand.setTimeout(0);

        CommandInfo injectionCommand = new CommandInfo();
        injectionCommand.setCommand("exec -it testPod -c testContainer -- /bin/sh /testDirectory/" + AGENT_NAME
                + "/bin/bmsubmit.sh -p 9091 -if javaHomePath /usr/bin __faultName cpuFault __load 80 __timeOutInMilliSeconds 20000");
        injectionCommand.setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentFaultInjectionRequest());
        injectionCommand.setIgnoreExitValueCheck(false);
        injectionCommand.setExpectedCommandOutputList(Arrays.asList(new String[] { "Created Fault Successfully" }));
        injectionCommand.setNoOfRetries(10);
        injectionCommand.setRetryInterval(5);
        injectionCommand.setTimeout(0);
        List<CommandOutputProcessingInfo> commandOutputProcessingInfoList = new ArrayList<>();
        CommandOutputProcessingInfo commandOutputProcessingInfo = new CommandOutputProcessingInfo();
        commandOutputProcessingInfo.setRegExpression("[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}");
        commandOutputProcessingInfo.setExtractedPropertyName("faultId");
        commandOutputProcessingInfoList.add(commandOutputProcessingInfo);
        injectionCommand.setCommandOutputProcessingInfoList(commandOutputProcessingInfoList);

        list.add(copyAgentCommand);
        list.add(installAgentCommand);
        list.add(enableTroubleshootingCommand);
        list.add(injectionCommand);
        return list;
    }

    private List<CommandInfo> getExpectedInjectionCommandsForSpringException() {
        List<CommandInfo> list = new ArrayList<>();
        CommandInfo copyAgentCommand = new CommandInfo();
        copyAgentCommand.setCommand("cp " + ConstantsUtils.getMangleSupportScriptDirectory() + AGENT_NAME
                + " testPod:/tmp/" + AGENT_NAME + " -c testContainer");
        copyAgentCommand.setIgnoreExitValueCheck(false);
        copyAgentCommand.setNoOfRetries(0);
        copyAgentCommand.setRetryInterval(0);
        copyAgentCommand.setTimeout(0);
        copyAgentCommand.setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentCopyOnK8sPod());

        CommandInfo installAgentCommand = new CommandInfo();
        installAgentCommand.setCommand("exec -it testPod -c testContainer -- /bin/sh /tmp/" + AGENT_NAME
                + "/bin/bminstall.sh -p 9091 -s -b null");
        installAgentCommand.setIgnoreExitValueCheck(true);
        installAgentCommand.setExpectedCommandOutputList(Arrays.asList(new String[] {
                "Started Byteman Listener Successfully", "Agent is already running on requested process" }));
        installAgentCommand.setNoOfRetries(10);
        installAgentCommand.setRetryInterval(5);
        installAgentCommand.setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentInstallationRequest());
        installAgentCommand.setTimeout(0);

        CommandInfo k8sCopyBytemanRuleFileCommandInfo = new CommandInfo();
        k8sCopyBytemanRuleFileCommandInfo.setCommand("cp " + ConstantsUtils.getMangleSupportScriptDirectory()
                + "123456.btm" + " testPod:/tmp/" + AGENT_NAME + "/123456.btm -c testContainer");
        k8sCopyBytemanRuleFileCommandInfo.setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentCopyOnK8sPod());

        CommandInfo enableTroubleshootingCommand = new CommandInfo();
        enableTroubleshootingCommand.setCommand("exec -it testPod -c testContainer -- /bin/sh /tmp/" + AGENT_NAME
                + "/bin/bmsubmit.sh -p 9091 -enableTroubleshooting");
        enableTroubleshootingCommand.setIgnoreExitValueCheck(true);
        enableTroubleshootingCommand.setExpectedCommandOutputList(Arrays.asList(new String[] {
                "install rule Trace - Capture Troubleshooting bundle.", "Troubleshooting Already Enabled." }));
        enableTroubleshootingCommand.setNoOfRetries(10);
        enableTroubleshootingCommand.setRetryInterval(5);
        enableTroubleshootingCommand.setTimeout(0);

        CommandInfo injectionCommand = new CommandInfo();
        injectionCommand.setCommand("exec -it testPod -c testContainer -- /bin/sh /tmp/" + AGENT_NAME
                + "/bin/bmsubmit.sh -p 9091 /tmp/" + AGENT_NAME + "/123456.btm");
        injectionCommand.setIgnoreExitValueCheck(false);
        injectionCommand.setExpectedCommandOutputList(
                Arrays.asList(new String[] { "install rule 123456", "redefine rule 123456" }));
        injectionCommand.setNoOfRetries(10);
        injectionCommand.setRetryInterval(5);
        injectionCommand.setTimeout(0);

        list.add(copyAgentCommand);
        list.add(installAgentCommand);
        list.add(enableTroubleshootingCommand);
        list.add(k8sCopyBytemanRuleFileCommandInfo);
        list.add(injectionCommand);
        return list;
    }

    @Test
    void testCheckTaskPrerequisites() throws Exception {
        PowerMockito.mockStatic(ConstantsUtils.class);
        PowerMockito.when(ConstantsUtils.getMangleSupportScriptDirectory()).thenReturn("target" + File.separator);
        Mockito.doNothing().when(pluginUtils).copyFileFromJarToDestination(any(), any());
        PowerMockito.mockStatic(CommandUtils.class);
        PowerMockito.when(CommandUtils.runCommand(any(), anyInt())).thenReturn(result);
        Mockito.when(result.getExitCode()).thenReturn(0);
        k8sBytemanFaultHelper.checkTaskSpecificPrerequisites();
    }

    @Test
    void testCheckTaskPrerequisitesWithAgentExtractionFailed() throws Exception {
        boolean exceptionCalled = false;
        PowerMockito.mockStatic(ConstantsUtils.class);
        PowerMockito.when(ConstantsUtils.getMangleSupportScriptDirectory()).thenReturn("target" + File.separator);
        Mockito.doNothing().when(pluginUtils).copyFileFromJarToDestination(any(), any());
        PowerMockito.mockStatic(CommandUtils.class);
        PowerMockito.when(CommandUtils.runCommand(any(), anyInt())).thenReturn(result);
        Mockito.when(result.getExitCode()).thenReturn(1);
        try {
            k8sBytemanFaultHelper.checkTaskSpecificPrerequisites();
        } catch (MangleException e) {
            exceptionCalled = true;
            assertEquals(e.getErrorCode(), ErrorCode.AGENT_EXTRACTION_FAILED);
        }
        assertTrue(exceptionCalled, "Extraction failed exception is not invoked");

    }

    @Test
    void testGetAgentFaultInjectionScripts() {
        CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getLinuxCpuJvmAgentFaultSpec();
        List<SupportScriptInfo> supportScripts = k8sBytemanFaultHelper.getAgentFaultInjectionScripts(cpuFaultSpec);
        assertTrue(supportScripts.isEmpty());
    }
}
