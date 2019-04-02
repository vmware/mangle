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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMAgentFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandOutputProcessingInfo;
import com.vmware.mangle.faults.plugin.helpers.FaultConstants;
import com.vmware.mangle.faults.plugin.helpers.JavaAgentFaultUtils;
import com.vmware.mangle.faults.plugin.helpers.byteman.K8sBytemanFaultHelper;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.faults.plugin.utils.PluginUtils;
import com.vmware.mangle.services.enums.BytemanFaultType;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.ConstantsUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.kubernetes.KubernetesCommandLineClient;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Test Class for K8sSystemResourceFaultHelper
 *
 * @author hkilari
 *
 */
@Log4j2
public class K8sBytemanFaultHelperTest {
    @Mock
    private EndpointClientFactory endpointClientFactory;
    @Mock
    KubernetesCommandLineClient kubernetesCommandLineClient;
    @Spy
    JavaAgentFaultUtils javaAgentFaultUtils;

    PluginUtils pluginUtils;

    private K8sBytemanFaultHelper k8sBytemanFaultHelper;

    private FaultsMockData faultsMockData = new FaultsMockData();
    @Mock
    List<SupportScriptInfo> supportScripts;

    @BeforeMethod
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        faultsMockData = new FaultsMockData();
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
            Assert.assertEquals(injectionCommands, expectedCommands);
        } catch (MangleException e) {
            log.error("testGetJVMAgentInjectionCommandInfoList For CPU Fault on K8s failed with Exception: ", e);
            Assert.assertTrue(false);
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
            Assert.assertEquals(remediationCommands, expectedCommands);
        } catch (MangleException e) {
            log.error("testGetJVMAgentRemediationCommandInfoList For CPU Fault on K8s failed with Exception: ", e);
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
            Assert.assertEquals(injectionCommands, expectedCommands);
        } catch (MangleException e) {
            log.error(
                    "testGetJVMAgentInjectionCommandInfoList For Spring Exception Fault on K8s failed with Exception: ",
                    e);
            Assert.assertTrue(false);
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
            Assert.assertEquals(remediationCommands, expectedCommands);
        } catch (MangleException e) {
            log.error(
                    "testGetJVMCodeLevelFaultCommandInfoList For Spring Exception Fault on K8s failed with Exception: ",
                    e);
            Assert.assertTrue(false);
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
        remediationCommand.setCommand(
                "exec -it testPod -c testContainer -- /bin/sh /testDirectory/byteman-download-3.0.10/bin/bmsubmit.sh -p 9091 -rf $FI_ADD_INFO_faultId");
        remediationCommand.setIgnoreExitValueCheck(false);
        remediationCommand
                .setExpectedCommandOutputList(Arrays.asList(new String("Received Remediation Request Successfully")));
        remediationCommand.setNoOfRetries(0);
        remediationCommand.setRetryInterval(0);
        remediationCommand.setTimeout(0);
        list.add(remediationCommand);

        CommandInfo remediationVerificationCommand = new CommandInfo();
        remediationVerificationCommand.setCommand(
                "exec -it testPod -c testContainer -- /bin/sh /testDirectory/byteman-download-3.0.10/bin/bmsubmit.sh -p 9091 -gf $FI_ADD_INFO_faultId");
        remediationVerificationCommand.setIgnoreExitValueCheck(false);
        remediationVerificationCommand
                .setExpectedCommandOutputList(Arrays.asList(new String("\"faultStatus\":\"COMPLETED\"")));
        remediationVerificationCommand.setNoOfRetries(6);
        remediationVerificationCommand.setRetryInterval(10);
        remediationVerificationCommand.setTimeout(0);
        list.add(remediationVerificationCommand);
        return list;
    }

    private List<CommandInfo> getExpectedRemediationCommandsforSpringLatency() {
        List<CommandInfo> list = new ArrayList<>();
        CommandInfo remediationCommand = new CommandInfo();
        remediationCommand.setCommand(
                "exec -it testPod -c testContainer -- /bin/sh /tmp/byteman-download-3.0.10/bin/bmsubmit.sh -p 9091 -u /tmp/byteman-download-3.0.10/123456.btm ");
        remediationCommand.setIgnoreExitValueCheck(true);
        remediationCommand.setExpectedCommandOutputList(Arrays.asList(new String("uninstall RULE 123456")));
        remediationCommand.setNoOfRetries(0);
        remediationCommand.setRetryInterval(0);
        remediationCommand.setTimeout(0);
        list.add(remediationCommand);
        return list;
    }

    private List<CommandInfo> getExpectedInjectionCommands() {
        List<CommandInfo> list = new ArrayList<>();
        CommandInfo copyAgentCommand = new CommandInfo();
        copyAgentCommand.setCommand("cp " + ConstantsUtils.getMangleSupportScriptDirectory()
                + "byteman-download-3.0.10 testPod:/testDirectory/byteman-download-3.0.10 -c testContainer");
        copyAgentCommand.setIgnoreExitValueCheck(false);
        copyAgentCommand.setNoOfRetries(0);
        copyAgentCommand.setRetryInterval(0);
        copyAgentCommand.setTimeout(0);

        CommandInfo installAgentCommand = new CommandInfo();
        installAgentCommand.setCommand(
                "exec -it testPod -c testContainer -- /bin/sh /testDirectory/byteman-download-3.0.10/bin/bminstall.sh -p 9091 -s -b null");
        installAgentCommand.setIgnoreExitValueCheck(true);
        installAgentCommand.setExpectedCommandOutputList(Arrays.asList(new String[] {
                "Started Byteman Listener Successfully", "Agent JAR loaded but agent failed to initialize" }));
        installAgentCommand.setNoOfRetries(10);
        installAgentCommand.setRetryInterval(5);
        installAgentCommand.setTimeout(0);

        CommandInfo enableTroubleshootingCommand = new CommandInfo();
        enableTroubleshootingCommand.setCommand(
                "exec -it testPod -c testContainer -- /bin/sh /testDirectory/byteman-download-3.0.10/bin/bmsubmit.sh -p 9091 -enableTroubleshooting");
        enableTroubleshootingCommand.setIgnoreExitValueCheck(true);
        enableTroubleshootingCommand.setExpectedCommandOutputList(Arrays.asList(new String[] {
                "install rule Trace - Capture Troubleshooting bundle.", "Troubleshooting Already Enabled." }));
        enableTroubleshootingCommand.setNoOfRetries(10);
        enableTroubleshootingCommand.setRetryInterval(5);
        enableTroubleshootingCommand.setTimeout(0);

        CommandInfo injectionCommand = new CommandInfo();
        injectionCommand.setCommand(
                "exec -it testPod -c testContainer -- /bin/sh /testDirectory/byteman-download-3.0.10/bin/bmsubmit.sh -p 9091 -if __faultName cpuFault __load 80 __timeOutInMilliSeconds 20000");
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
        copyAgentCommand.setCommand("cp " + ConstantsUtils.getMangleSupportScriptDirectory()
                + "byteman-download-3.0.10 testPod:/tmp/byteman-download-3.0.10 -c testContainer");
        copyAgentCommand.setIgnoreExitValueCheck(false);
        copyAgentCommand.setNoOfRetries(0);
        copyAgentCommand.setRetryInterval(0);
        copyAgentCommand.setTimeout(0);

        CommandInfo installAgentCommand = new CommandInfo();
        installAgentCommand.setCommand(
                "exec -it testPod -c testContainer -- /bin/sh /tmp/byteman-download-3.0.10/bin/bminstall.sh -p 9091 -s -b null");
        installAgentCommand.setIgnoreExitValueCheck(true);
        installAgentCommand.setExpectedCommandOutputList(Arrays.asList(new String[] {
                "Started Byteman Listener Successfully", "Agent JAR loaded but agent failed to initialize" }));
        installAgentCommand.setNoOfRetries(10);
        installAgentCommand.setRetryInterval(5);
        installAgentCommand.setTimeout(0);

        CommandInfo enableTroubleshootingCommand = new CommandInfo();
        enableTroubleshootingCommand.setCommand(
                "exec -it testPod -c testContainer -- /bin/sh /tmp/byteman-download-3.0.10/bin/bmsubmit.sh -p 9091 -enableTroubleshooting");
        enableTroubleshootingCommand.setIgnoreExitValueCheck(true);
        enableTroubleshootingCommand.setExpectedCommandOutputList(Arrays.asList(new String[] {
                "install rule Trace - Capture Troubleshooting bundle.", "Troubleshooting Already Enabled." }));
        enableTroubleshootingCommand.setNoOfRetries(10);
        enableTroubleshootingCommand.setRetryInterval(5);
        enableTroubleshootingCommand.setTimeout(0);

        CommandInfo injectionCommand = new CommandInfo();
        injectionCommand.setCommand(
                "exec -it testPod -c testContainer -- /bin/sh /tmp/byteman-download-3.0.10/bin/bmsubmit.sh -p 9091 /tmp/byteman-download-3.0.10/123456.btm");
        injectionCommand.setIgnoreExitValueCheck(false);
        injectionCommand.setExpectedCommandOutputList(Arrays.asList(new String[] { "install rule 123456" }));
        injectionCommand.setNoOfRetries(10);
        injectionCommand.setRetryInterval(5);
        injectionCommand.setTimeout(0);

        list.add(copyAgentCommand);
        list.add(installAgentCommand);
        list.add(enableTroubleshootingCommand);
        list.add(injectionCommand);
        return list;
    }
}