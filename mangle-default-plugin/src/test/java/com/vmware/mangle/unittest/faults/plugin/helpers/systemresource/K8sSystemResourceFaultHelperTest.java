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

import static com.vmware.mangle.utils.constants.FaultConstants.FAULT_NAME_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.TIMEOUT_IN_MILLI_SEC_ARG;

import java.util.ArrayList;
import java.util.Collections;
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
import com.vmware.mangle.faults.plugin.helpers.KnownFailuresHelper;
import com.vmware.mangle.faults.plugin.helpers.systemresource.K8sSystemResourceFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.systemresource.SystemResourceFaultUtils;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
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
public class K8sSystemResourceFaultHelperTest {
    @Mock
    private EndpointClientFactory endpointClientFactory;
    @Mock
    KubernetesCommandLineClient kubernetesCommandLineClient;
    @Spy
    private SystemResourceFaultUtils systemResourceFaultUtils;

    private K8sSystemResourceFaultHelper k8sSystemResourceFaultHelper;

    private FaultsMockData faultsMockData = new FaultsMockData();
    @Mock
    List<SupportScriptInfo> supportScripts;

    @BeforeMethod
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        faultsMockData = new FaultsMockData();
        k8sSystemResourceFaultHelper =
                new K8sSystemResourceFaultHelper(endpointClientFactory, systemResourceFaultUtils);
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
            executor = k8sSystemResourceFaultHelper.getExecutor(cpuFaultSpec);
        } catch (MangleException e) {
            log.error("testGetExecutor failed with Exception: ", e);
            Assert.assertTrue(false);
        }
        Assert.assertEquals(executor, kubernetesCommandLineClient);
    }

    @Test
    public void testGetInjectionCommandInfoListforCPUFault() {
        try {
            CommandExecutionFaultSpec cpuFaultSpec = getTestFaultSpecForCPUFault();
            Mockito.when(
                    endpointClientFactory.getEndPointClient(cpuFaultSpec.getCredentials(), cpuFaultSpec.getEndpoint()))
                    .thenReturn(kubernetesCommandLineClient);
            List<CommandInfo> injectionCommands =
                    k8sSystemResourceFaultHelper.getInjectionCommandInfoList(cpuFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(injectionCommands));
            List<CommandInfo> expectedCommands = getExpectedInjectionCommands();
            Assert.assertEquals(injectionCommands, expectedCommands);
        } catch (MangleException e) {
            log.error("testGetInjectionCommandInfoList For CPU System resource Fault on K8s failed with Exception: ",
                    e);
            Assert.assertTrue(false);
        }
    }

    private CommandExecutionFaultSpec getTestFaultSpecForCPUFault() {
        CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getK8SCPUFaultSpec();
        cpuFaultSpec.getArgs().put(FAULT_NAME_ARG, "cpuFault");
        cpuFaultSpec.getArgs().put(TIMEOUT_IN_MILLI_SEC_ARG, "20000");
        cpuFaultSpec.getK8sArguments().setPodInAction("testPod");
        return cpuFaultSpec;
    }

    @Test
    public void testGetRemediationCommandInfoListForCPUFault() {
        CommandExecutionFaultSpec cpuFaultSpec = getTestFaultSpecForCPUFault();

        Mockito.when(endpointClientFactory.getEndPointClient(cpuFaultSpec.getCredentials(), cpuFaultSpec.getEndpoint()))
                .thenReturn(kubernetesCommandLineClient);
        try {
            List<CommandInfo> remediationCommands =
                    k8sSystemResourceFaultHelper.getRemediationcommandInfoList(cpuFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(remediationCommands));
            List<CommandInfo> expectedCommands = getExpectedRemediationCommandsforCPUFault();
            Assert.assertEquals(remediationCommands, expectedCommands);
        } catch (MangleException e) {
            log.error("testGetRemediationCommandInfoList For CPU System resource Fault on K8s failed with Exception: ",
                    e);
            Assert.assertTrue(false);
        }
    }

    private List<CommandInfo> getExpectedRemediationCommandsforCPUFault() {
        List<CommandInfo> list = new ArrayList<>();
        CommandInfo remediationCommand = new CommandInfo();
        remediationCommand.setCommand(
                "exec -it testPod -c testContainer -- /bin/sh /testDirectory/cpuburn.sh --operation=remediate");
        remediationCommand.setIgnoreExitValueCheck(false);
        remediationCommand
                .setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfSystemResourceFaultRemediationRequest());
        remediationCommand.setNoOfRetries(0);
        remediationCommand.setExpectedCommandOutputList(Collections.emptyList());
        remediationCommand.setRetryInterval(0);
        remediationCommand.setTimeout(0);
        list.add(remediationCommand);
        return list;
    }

    @Test
    void testGetAgentFaultInjectionScripts() {
        CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getK8SCPUFaultSpec();
        K8sSystemResourceFaultHelper k8sSystemResourceFaultHelper =
                spy(new K8sSystemResourceFaultHelper(endpointClientFactory, systemResourceFaultUtils));
        Mockito.doReturn(supportScripts).when(systemResourceFaultUtils).getAgentFaultScripts(any());
        k8sSystemResourceFaultHelper.getFaultInjectionScripts(cpuFaultSpec);
        verify(systemResourceFaultUtils, times(1)).getAgentFaultScripts(any(CommandExecutionFaultSpec.class));
    }

    private List<CommandInfo> getExpectedInjectionCommands() {
        List<CommandInfo> list = new ArrayList<>();
        CommandInfo copyCommand = new CommandInfo();
        copyCommand.setCommand("cp " + ConstantsUtils.getMangleSupportScriptDirectory()
                + "cpuburn.sh testPod:/testDirectory/cpuburn.sh -c testContainer");
        copyCommand.setIgnoreExitValueCheck(false);
        copyCommand.setNoOfRetries(0);
        copyCommand.setRetryInterval(0);
        copyCommand.setTimeout(0);
        copyCommand.setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfSystemResourceK8SCopyRequest());


        CommandInfo makeExecutableCommand = new CommandInfo();
        makeExecutableCommand.setCommand("exec -it testPod -c testContainer -- chmod -R u+x /testDirectory/cpuburn.sh");
        makeExecutableCommand.setIgnoreExitValueCheck(false);
        makeExecutableCommand.setNoOfRetries(0);
        makeExecutableCommand.setRetryInterval(0);
        makeExecutableCommand.setTimeout(0);

        CommandInfo injectionCommand = new CommandInfo();
        injectionCommand.setCommand(
                "exec -it testPod -c testContainer -- /bin/sh /testDirectory/cpuburn.sh --operation=inject --load=80 --timeout=20000");
        injectionCommand.setIgnoreExitValueCheck(false);
        injectionCommand.setNoOfRetries(0);
        injectionCommand.setRetryInterval(0);
        injectionCommand.setTimeout(0);
        injectionCommand
                .setKnownFailureMap(KnownFailuresHelper.getKnownFailuresOfSystemResourceFaultInjectionRequest());

        list.add(copyCommand);
        list.add(makeExecutableCommand);
        list.add(injectionCommand);
        return list;
    }
}
