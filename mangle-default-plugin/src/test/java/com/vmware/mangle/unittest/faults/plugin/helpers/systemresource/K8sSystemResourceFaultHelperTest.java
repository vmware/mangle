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

import static com.vmware.mangle.utils.constants.FaultConstants.FAULT_NAME_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.TIMEOUT_IN_MILLI_SEC_ARG;

import java.util.ArrayList;
import java.util.Arrays;
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
    public void testGetstatusCommandInfoList() throws MangleException {
        CommandExecutionFaultSpec cpuFaultSpec = getTestFaultSpecForCPUFault();
        List<CommandInfo> statusCommands = k8sSystemResourceFaultHelper.getStatusCommandInfoList(cpuFaultSpec);
        log.info(RestTemplateWrapper.objectToJson(statusCommands));
        List<CommandInfo> expectedCommands = getExpectedStatusCommandsForCpuFault();
        Assert.assertEquals(statusCommands, expectedCommands);
    }

    @Test
    public void testGetRemediationCommandInfoListForCPUFault() throws MangleException {
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

    @Test
    public void testGetRemediationCommandInfoListforFileHandler() {
        try {
            CommandExecutionFaultSpec fileHandlerFaultSpec = faultsMockData.getFilehandlerLeakFaultSpec();
            Mockito.when(endpointClientFactory.getEndPointClient(null, fileHandlerFaultSpec.getEndpoint()))
                    .thenReturn(kubernetesCommandLineClient);
            List<CommandInfo> remediationCommands =
                    k8sSystemResourceFaultHelper.getRemediationcommandInfoList(fileHandlerFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(remediationCommands));
            Assert.assertEquals(remediationCommands, Collections.emptyList());
        } catch (MangleException e) {
            log.error("testGetRemediationCommandInfoListforFileHandler failed with Exception: ", e);
            Assert.assertTrue(false);
        }
    }

    private List<CommandInfo> getExpectedRemediationCommandsforCPUFault() {
        List<CommandInfo> list = new ArrayList<>();
        CommandInfo remediationCommand = CommandInfo
                .builder(
                        "exec -it testPod -c testContainer -- /bin/sh -c \"cd /testDirectory//infra_agent;./infra_submit  --operation remediate  --faultId cpuFault\"")
                .ignoreExitValueCheck(false)
                .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfSystemResourceFaultRemediationRequest())
                .noOfRetries(0).expectedCommandOutputList(Collections.emptyList()).retryInterval(0).timeout(0).build();
        list.add(remediationCommand);
        return list;
    }

    private List<CommandInfo> getExpectedStatusCommandsForCpuFault() {
        List<CommandInfo> list = new ArrayList<>();
        CommandInfo statusCommand = CommandInfo
                .builder(
                        "exec -it testPod -c testContainer -- /bin/sh -c \"cd /testDirectory//infra_agent;./infra_submit  --operation status  --faultId cpuFault\"")
                .ignoreExitValueCheck(false)
                .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfSystemResourceFaultInjectionRequest())
                .noOfRetries(0).expectedCommandOutputList(Collections.emptyList()).retryInterval(0).timeout(0).build();
        list.add(statusCommand);
        return list;
    }

    @Test
    void testGetAgentFaultInjectionScripts() {
        CommandExecutionFaultSpec cpuFaultSpec = faultsMockData.getK8SCPUFaultSpec();
        K8sSystemResourceFaultHelper k8sSystemResourceFaultHelper =
                new K8sSystemResourceFaultHelper(endpointClientFactory, systemResourceFaultUtils);
        Mockito.doReturn(supportScripts).when(systemResourceFaultUtils).getAgentFaultScriptsPython(any());
        k8sSystemResourceFaultHelper.getFaultInjectionScripts(cpuFaultSpec);
        verify(systemResourceFaultUtils, times(1)).getAgentFaultScriptsPython(anyString());
    }

    private List<CommandInfo> getExpectedInjectionCommands() {
        List<CommandInfo> list = new ArrayList<>();
        CommandInfo copyCommand = CommandInfo
                .builder("cp " + ConstantsUtils.getMangleSupportScriptDirectory()
                        + "infra-agent.tar.gz testPod:/testDirectory/infra-agent.tar.gz -c testContainer")
                .ignoreExitValueCheck(false).noOfRetries(0).retryInterval(0).timeout(0)
                .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfSystemResourceK8SCopyRequest()).build();


        CommandInfo makeExecutableCommand = CommandInfo
                .builder("exec -it testPod -c testContainer -- chmod -R u+x /testDirectory/infra-agent.tar.gz")
                .ignoreExitValueCheck(false).noOfRetries(0).retryInterval(0).timeout(0).build();

        CommandInfo injectionCommand = CommandInfo
                .builder(
                        "exec -it testPod -c testContainer -- /bin/sh -c \"cd /testDirectory//infra_agent;./infra_submit  --operation inject --faultname cpuFault --load 80 --timeout 20000 --faultId cpuFault\"")
                .ignoreExitValueCheck(false).noOfRetries(3).retryInterval(2).timeout(0)
                .expectedCommandOutputList(Arrays.asList(""))
                .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfSystemResourceFaultInjectionRequest()).build();

        CommandInfo agentExtractionCommand = CommandInfo
                .builder(
                        "exec -it testPod -c testContainer -- /bin/sh -c \'tar -zxvf /testDirectory//infra-agent.tar.gz -C /testDirectory/\'")
                .ignoreExitValueCheck(false).expectedCommandOutputList(Arrays.asList("")).build();
        CommandInfo agentStartCommand = CommandInfo
                .builder(
                        "exec -it testPod -c testContainer -- nohup /bin/sh -c \"cd /testDirectory//infra_agent;./infra_agent > /dev/null 2>&1 &\"")
                .ignoreExitValueCheck(false).expectedCommandOutputList(Arrays.asList(""))
                .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentInstallationRequest()).build();
        list.add(copyCommand);
        list.add(makeExecutableCommand);
        list.add(agentExtractionCommand);
        list.add(agentStartCommand);
        list.add(injectionCommand);
        return list;
    }
}
