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

package com.vmware.mangle.unittest.faults.plugin.helpers.k8s;

import java.util.ArrayList;
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

import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.faults.plugin.helpers.k8s.K8sFaultHelper;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.services.enums.K8SFaultName;
import com.vmware.mangle.services.enums.K8SResource;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.unittest.faults.plugin.helpers.CommandResultUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.kubernetes.KubernetesCommandLineClient;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;


/**
 * Test Class for K8sFaultHelper
 *
 * @author hkilari
 *
 */
@Log4j2
public class K8sFaultHelperTest {
    @Mock
    EndpointClientFactory endpointClientFactory;
    @Mock
    KubernetesCommandLineClient kubernetesCommandLineClient;

    K8sFaultHelper k8sFaultHelper;
    private FaultsMockData faultsMockData = new FaultsMockData();


    private String getPodsListString() {
        return "sym-inventory-service-243-0 sym-inventory-service-243-1 sym-inventory-service-243-2";
    }


    private List<String> getPodsAsList() {
        List<String> list = new ArrayList<>();
        list.add("sym-inventory-service-243-0");
        list.add("sym-inventory-service-243-1");
        list.add("sym-inventory-service-243-2");
        return list;
    }

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        faultsMockData = new FaultsMockData();
        k8sFaultHelper = new K8sFaultHelper(endpointClientFactory);
    }

    @Test
    public void testGetExecutor() {
        ICommandExecutor executor = null;
        try {
            K8SFaultSpec k8sFaultSpec = faultsMockData.getDeleteK8SResourceFaultSpec();
            Mockito.when(endpointClientFactory.getEndPointClient(k8sFaultSpec.getCredentials(),
                    k8sFaultSpec.getEndpoint())).thenReturn(kubernetesCommandLineClient);
            executor = k8sFaultHelper.getExecutor(k8sFaultSpec);
        } catch (MangleException e) {
            log.error("testGetExecutor failed with Exception: ", e);
            Assert.assertTrue(false);
        }
        Assert.assertEquals(executor, kubernetesCommandLineClient);
    }

    @Test
    public void testGetResouceList() {
        try {
            K8SFaultSpec k8sFaultSpec = getK8sDeleteResourceFault();
            Mockito.when(endpointClientFactory.getEndPointClient(Mockito.any(), Mockito.any()))
                    .thenReturn(kubernetesCommandLineClient);
            ICommandExecutor executor = k8sFaultHelper.getExecutor(k8sFaultSpec);
            Map<String, String> resourceLabels = new HashMap<>();
            resourceLabels.put("app", "sym-inventory-service");
            k8sFaultSpec.setResourceLabels(resourceLabels);

            Mockito.when(kubernetesCommandLineClient.executeCommand(Mockito.any()))
                    .thenReturn(CommandResultUtils.getCommandResult(getPodsListString()));
            List<String> resources = k8sFaultHelper.getResouceList(executor, k8sFaultSpec);
            Assert.assertEquals(resources, getPodsAsList());
        } catch (MangleException e) {
            log.error("getK8sDeleteResourceFault failed with Exception: ", e);
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testGetResouceListWithRandomInjectionTrue() {
        try {
            K8SFaultSpec k8sFaultSpec = getK8sDeleteResourceFault();
            k8sFaultSpec.setRandomInjection(true);
            Mockito.when(endpointClientFactory.getEndPointClient(k8sFaultSpec.getCredentials(),
                    k8sFaultSpec.getEndpoint())).thenReturn(kubernetesCommandLineClient);
            ICommandExecutor executor = k8sFaultHelper.getExecutor(k8sFaultSpec);
            Map<String, String> resourceLabels = new HashMap<>();
            resourceLabels.put("app", "sym-inventory-service");
            k8sFaultSpec.setResourceLabels(resourceLabels);

            Mockito.when(kubernetesCommandLineClient.executeCommand(Mockito.any()))
                    .thenReturn(CommandResultUtils.getCommandResult(getPodsListString()));
            List<String> resources = k8sFaultHelper.getResouceList(executor, k8sFaultSpec);
            Assert.assertEquals(resources.size(), 1);
            Assert.assertTrue(getPodsAsList().toString().contains(resources.get(0)));
        } catch (MangleException e) {
            log.error("testGetResouceListWithRandomInjectionTrue failed with Exception: ", e);
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testGetResouceListWithInValidResourceLabels() {
        try {
            K8SFaultSpec k8sFaultSpec = getK8sDeleteResourceFault();
            k8sFaultSpec.setRandomInjection(true);
            Mockito.when(endpointClientFactory.getEndPointClient(k8sFaultSpec.getCredentials(),
                    k8sFaultSpec.getEndpoint())).thenReturn(kubernetesCommandLineClient);
            ICommandExecutor executor = k8sFaultHelper.getExecutor(k8sFaultSpec);

            Mockito.when(kubernetesCommandLineClient.executeCommand(Mockito.any()))
                    .thenReturn(CommandResultUtils.getCommandResult(getPodsListString()));
            List<String> resources = k8sFaultHelper.getResouceList(executor, k8sFaultSpec);
        } catch (MangleException e) {
            log.error("testGetResouceListWithInValidResourceLabels failed with Exception: ", e);
            Assert.assertEquals(e.getErrorCode(), ErrorCode.INVALID_RESOURCE_LABELS);
        }
    }

    @Test(enabled = false)
    public void testGetResouceListWithEmptyResponseFromCommandExecutor() {
        Mockito.reset(kubernetesCommandLineClient);
        try {
            K8SFaultSpec k8sFaultSpec = getK8sDeleteResourceFault();
            k8sFaultSpec.setRandomInjection(true);
            Mockito.when(endpointClientFactory.getEndPointClient(k8sFaultSpec.getCredentials(),
                    k8sFaultSpec.getEndpoint())).thenReturn(kubernetesCommandLineClient);
            ICommandExecutor executor = k8sFaultHelper.getExecutor(k8sFaultSpec);
            Map<String, String> resourceLabels = new HashMap<>();
            resourceLabels.put("app", "sym-inventory-service");
            k8sFaultSpec.setResourceLabels(resourceLabels);
            Mockito.when(kubernetesCommandLineClient.executeCommand(Mockito.any()))
                    .thenReturn(CommandResultUtils.getCommandResult(""));
            List<String> resources = k8sFaultHelper.getResouceList(executor, k8sFaultSpec);
        } catch (MangleException e) {
            log.error("testGetResouceListWithEmptyResponseFromCommandExecutor failed with Exception: ", e);
            Assert.assertEquals(e.getErrorCode(), ErrorCode.INVALID_RESOURCE_LABELS);
            Mockito.verify(kubernetesCommandLineClient, Mockito.times(6)).executeCommand(Mockito.any());
        }
    }

    @Test(enabled = false)
    public void testGetResouceListWithErrorResponseFromCommandExecutor() {
        Mockito.reset(kubernetesCommandLineClient);
        try {
            K8SFaultSpec k8sFaultSpec = getK8sDeleteResourceFault();
            k8sFaultSpec.setRandomInjection(true);
            Mockito.when(endpointClientFactory.getEndPointClient(k8sFaultSpec.getCredentials(),
                    k8sFaultSpec.getEndpoint())).thenReturn(kubernetesCommandLineClient);
            ICommandExecutor executor = k8sFaultHelper.getExecutor(k8sFaultSpec);
            Map<String, String> resourceLabels = new HashMap<>();
            resourceLabels.put("app", "sym-inventory-service");
            k8sFaultSpec.setResourceLabels(resourceLabels);
            Mockito.when(kubernetesCommandLineClient.executeCommand(Mockito.any()))
                    .thenReturn(CommandResultUtils.getCommandResult("error"));
            List<String> resources = k8sFaultHelper.getResouceList(executor, k8sFaultSpec);
        } catch (MangleException e) {
            log.error("testGetResouceListWithEmptyResponseFromCommandExecutor failed with Exception: ", e);
            Assert.assertEquals(e.getErrorCode(), ErrorCode.INVALID_RESOURCE_LABELS);
            Mockito.verify(kubernetesCommandLineClient, Mockito.times(6)).executeCommand(Mockito.any());
        }
    }

    @Test(enabled = false)
    public void testGetResouceListWithNonZeroExitCodeResponseFromCommandExecutor() {
        Mockito.reset(kubernetesCommandLineClient);
        try {
            K8SFaultSpec k8sFaultSpec = getK8sDeleteResourceFault();
            k8sFaultSpec.setRandomInjection(true);
            Mockito.when(endpointClientFactory.getEndPointClient(k8sFaultSpec.getCredentials(),
                    k8sFaultSpec.getEndpoint())).thenReturn(kubernetesCommandLineClient);
            ICommandExecutor executor = k8sFaultHelper.getExecutor(k8sFaultSpec);
            Map<String, String> resourceLabels = new HashMap<>();
            resourceLabels.put("app", "sym-inventory-service");
            k8sFaultSpec.setResourceLabels(resourceLabels);
            CommandExecutionResult result = CommandResultUtils.getCommandResult("error");
            result.setExitCode(126);
            Mockito.when(kubernetesCommandLineClient.executeCommand(Mockito.any())).thenReturn(result);
            List<String> resources = k8sFaultHelper.getResouceList(executor, k8sFaultSpec);
        } catch (MangleException e) {
            log.error("testGetResouceListWithEmptyResponseFromCommandExecutor failed with Exception: ", e);
            Assert.assertEquals(e.getErrorCode(), ErrorCode.INVALID_RESOURCE_LABELS);
            Mockito.verify(kubernetesCommandLineClient, Mockito.times(6)).executeCommand(Mockito.any());
        }
    }

    @Test
    public void testGetInjectionCommandInfoListForDeleteResourceFault() {
        try {
            K8SFaultSpec k8sFaultSpec = getK8sDeleteResourceFault();
            Mockito.when(endpointClientFactory.getEndPointClient(k8sFaultSpec.getCredentials(),
                    k8sFaultSpec.getEndpoint())).thenReturn(kubernetesCommandLineClient);
            ICommandExecutor executor = k8sFaultHelper.getExecutor(k8sFaultSpec);
            Map<String, String> resourceLabels = new HashMap<>();
            resourceLabels.put("app", "sym-inventory-service");
            k8sFaultSpec.setResourceLabels(resourceLabels);

            k8sFaultSpec.setResourcesList(getPodsAsList());
            List<CommandInfo> injectionCommands = k8sFaultHelper.getInjectionCommandInfoList(executor, k8sFaultSpec);

            log.info(RestTemplateWrapper.objectToJson(injectionCommands));
            Assert.assertEquals(injectionCommands.size(), 1);
            Assert.assertEquals(injectionCommands.get(0),
                    getInjectionCommand(" delete  pod -l app=sym-inventory-service"));
            List<CommandInfo> remediationCommands =
                    k8sFaultHelper.getRemediationCommandInfoList(executor, k8sFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(remediationCommands));
            Assert.assertEquals(remediationCommands.size(), 0);
        } catch (MangleException e) {
            log.error("testGetInjectionCommandInfoListForDeleteResourceFault failed with Exception: ", e);
            Assert.assertTrue(false);
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "No enum constant com.vmware.mangle.services.enums.K8SFaultName.testFaultName")
    public void testGetInjectionCommandInfoListForUnSupportedFault() {
        try {
            K8SFaultSpec k8sFaultSpec = getK8sDeleteResourceFault();
            Map<String, String> specificArgs = new HashMap<>();
            specificArgs.put("operation", "testFaultName");
            k8sFaultSpec.setArgs(specificArgs);
            k8sFaultSpec.setRandomInjection(true);
            Mockito.when(endpointClientFactory.getEndPointClient(k8sFaultSpec.getCredentials(),
                    k8sFaultSpec.getEndpoint())).thenReturn(kubernetesCommandLineClient);
            ICommandExecutor executor = k8sFaultHelper.getExecutor(k8sFaultSpec);
            Map<String, String> resourceLabels = new HashMap<>();
            resourceLabels.put("app", "sym-inventory-service");
            k8sFaultSpec.setResourceLabels(resourceLabels);
            k8sFaultSpec.setResourcesList(getPodsAsList());
            List<CommandInfo> injectionCommands = k8sFaultHelper.getInjectionCommandInfoList(executor, k8sFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(injectionCommands));
            Assert.assertEquals(injectionCommands.size(), 1);
            Assert.assertEquals(injectionCommands.get(0),
                    getInjectionCommand(" delete  pod -l app=sym-inventory-service"));
            List<CommandInfo> remediationCommands =
                    k8sFaultHelper.getRemediationCommandInfoList(executor, k8sFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(remediationCommands));
            Assert.assertEquals(remediationCommands.size(), 0);
        } catch (MangleException e) {
            log.error("testGetInjectionCommandInfoListForUnSupportedFault failed with Exception: ", e);
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testGetInjectionCommandInfoListForDeleteResourceFaultWithRandomInjection() {
        try {
            K8SFaultSpec k8sFaultSpec = getK8sDeleteResourceFault();
            k8sFaultSpec.setRandomInjection(true);
            Mockito.when(endpointClientFactory.getEndPointClient(k8sFaultSpec.getCredentials(),
                    k8sFaultSpec.getEndpoint())).thenReturn(kubernetesCommandLineClient);
            ICommandExecutor executor = k8sFaultHelper.getExecutor(k8sFaultSpec);
            Map<String, String> resourceLabels = new HashMap<>();
            resourceLabels.put("app", "sym-inventory-service");
            k8sFaultSpec.setResourceLabels(resourceLabels);

            Mockito.when(kubernetesCommandLineClient.executeCommand(Mockito.any()))
                    .thenReturn(CommandResultUtils.getCommandResult(getPodsListString()));
            k8sFaultSpec.setResourcesList(k8sFaultHelper.getResouceList(executor, k8sFaultSpec));
            List<CommandInfo> injectionCommands = k8sFaultHelper.getInjectionCommandInfoList(executor, k8sFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(injectionCommands));
            Assert.assertEquals(injectionCommands.size(), 1);
            Assert.assertTrue(verifyInjectionCommandInfoListForDeleteResource(injectionCommands.get(0),
                    getInjectionCommand(" delete  pod -l app=sym-inventory-service")));
            List<CommandInfo> remediationCommands =
                    k8sFaultHelper.getRemediationCommandInfoList(executor, k8sFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(remediationCommands));
            Assert.assertEquals(remediationCommands.size(), 0);
        } catch (MangleException e) {
            log.error(
                    "testGetInjectionCommandInfoListForDeleteResourceFaultWithRandomInjection failed with Exception: ",
                    e);
            Assert.assertTrue(false);
        }
    }


    private boolean verifyInjectionCommandInfoListForDeleteResource(CommandInfo commandInfo,
            CommandInfo injectionCommand) {
        if (commandInfo.equals(getInjectionCommand(" delete  pod sym-inventory-service-243-0"))) {
            return true;
        }
        if (commandInfo.equals(getInjectionCommand(" delete  pod sym-inventory-service-243-1"))) {
            return true;
        }
        if (commandInfo.equals(getInjectionCommand(" delete  pod sym-inventory-service-243-2"))) {
            return true;
        }
        return false;
    }

    private K8SFaultSpec getK8sDeleteResourceFault() {
        K8SFaultSpec k8sFaultSpec = faultsMockData.getDeleteK8SResourceFaultSpec();
        Map<String, String> specificArgs = new HashMap<>();
        specificArgs.put("operation", K8SFaultName.DELETE_RESOURCE.name());
        k8sFaultSpec.setArgs(specificArgs);
        k8sFaultSpec.setResourceType(K8SResource.POD);
        k8sFaultSpec.setRandomInjection(false);
        return k8sFaultSpec;
    }

    @Test
    public void testgetInjectResourceNotReadyFault() {
        try {
            K8SFaultSpec k8sFaultSpec = faultsMockData.getK8SResourceNotReadyFaultSpec();
            Map<String, String> specificArgs = new HashMap<>();
            specificArgs.put("operation", K8SFaultName.NOTREADY_RESOURCE.name());
            k8sFaultSpec.setArgs(specificArgs);
            k8sFaultSpec.setResourceType(K8SResource.POD);
            k8sFaultSpec.setRandomInjection(false);
            Mockito.when(endpointClientFactory.getEndPointClient(k8sFaultSpec.getCredentials(),
                    k8sFaultSpec.getEndpoint())).thenReturn(kubernetesCommandLineClient);
            ICommandExecutor executor = k8sFaultHelper.getExecutor(k8sFaultSpec);
            Map<String, String> resourceLabels = new HashMap<>();
            resourceLabels.put("app", "sym-inventory-service");
            k8sFaultSpec.setResourceLabels(resourceLabels);

            k8sFaultSpec.setResourcesList(getPodsAsList());
            log.info(k8sFaultSpec.getResourcesList());
            List<CommandInfo> injectionCommands = k8sFaultHelper.getInjectionCommandInfoList(executor, k8sFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(injectionCommands));
            Assert.assertEquals(injectionCommands.size(), 3);
            Assert.assertEquals(injectionCommands.get(0), getInjectionCommand(
                    " patch pod  sym-inventory-service-243-0 -p '{\"spec\":{\"containers\":[{\"name\":\"testContainer\",\"image\":\"nginx\"}]}}' "));
            List<CommandInfo> remediationCommands =
                    k8sFaultHelper.getRemediationCommandInfoList(executor, k8sFaultSpec);
            log.info(RestTemplateWrapper.objectToJson(remediationCommands));
            Assert.assertEquals(remediationCommands.size(), 3);
        } catch (MangleException e) {
            log.error("testgetInjectResourceNotReadyFault failed with Exception: ", e);
            Assert.assertTrue(false);
        }
    }

    private CommandInfo getInjectionCommand(String command) {
        CommandInfo commandInfo = new CommandInfo();
        commandInfo.setCommand(command);
        commandInfo.setIgnoreExitValueCheck(false);
        commandInfo.setNoOfRetries(0);
        commandInfo.setRetryInterval(0);
        commandInfo.setTimeout(0);
        return commandInfo;
    }

}
