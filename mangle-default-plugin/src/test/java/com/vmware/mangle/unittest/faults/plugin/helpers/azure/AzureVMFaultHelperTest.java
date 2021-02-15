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

package com.vmware.mangle.unittest.faults.plugin.helpers.azure;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.faults.plugin.helpers.aws.AwsEC2FaultHelper;
import com.vmware.mangle.faults.plugin.helpers.azure.AzureVMFaultHelper;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.model.azure.AzureNetworkFaultRemediation;
import com.vmware.mangle.model.azure.AzureNetworkFaults;
import com.vmware.mangle.model.azure.AzureVMFaultRemediation;
import com.vmware.mangle.model.azure.AzureVMStateFaults;
import com.vmware.mangle.model.azure.faults.spec.AzureVMFaultSpec;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.azure.AzureCommandExecutor;
import com.vmware.mangle.utils.clients.azure.AzureCommonUtils;
import com.vmware.mangle.utils.clients.azure.CustomAzureClient;
import com.vmware.mangle.utils.exceptions.MangleException;


/**
 * Test Class for AzureVMFaultHelper
 *
 * @author bkaranam
 *
 */
@PrepareForTest({ AzureCommonUtils.class, AzureVMFaultHelper.class })
@PowerMockIgnore({ "javax.xml.parsers.*", "com.sun.org.apache.xerces.internal.jaxp.*", "org.apache.logging.log4j.*" })
public class AzureVMFaultHelperTest extends PowerMockTestCase {

    private FaultsMockData mockData = new FaultsMockData();

    @Mock
    private EndpointClientFactory factory;
    @Mock
    private ICommandExecutor executor;
    @Mock
    private CustomAzureClient azureClient;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(AzureCommonUtils.class);
    }

    @Test
    public void testAzureVMFaultHelperInit() {
        AzureVMFaultHelper helper = new AzureVMFaultHelper(factory);
        Assert.assertNotNull(helper);
    }

    @Test
    public void testGetInjectionCommandInfoList() throws MangleException {
        AzureVMFaultSpec spec = mockData.getAzureVMStateFaultSpec();
        AzureVMFaultHelper helper = new AzureVMFaultHelper(factory);

        List<CommandInfo> commandInfos = helper.getInjectionCommandInfoList(executor, spec);
        Assert.assertEquals(1, commandInfos.size());
        CommandInfo commandInfo = commandInfos.get(0);
        Assert.assertTrue(commandInfo.getCommand().startsWith(spec.getFaultName()));
    }

    @Test
    public void testGetInjectionCommandInfoListWithNetworkBlockFault() throws MangleException {
        AzureVMFaultSpec spec = mockData.getAzureVMStateFaultSpec();
        spec.setFaultName(AzureNetworkFaults.BLOCK_ALL_VM_NETWORK_TRAFFIC.name());
        AzureVMFaultHelper helper = new AzureVMFaultHelper(factory);

        List<CommandInfo> commandInfos = helper.getInjectionCommandInfoList(executor, spec);
        Assert.assertEquals(1, commandInfos.size());
        CommandInfo commandInfo = commandInfos.get(0);
        Assert.assertTrue(commandInfo.getCommand().startsWith(spec.getFaultName()));
        Assert.assertEquals(commandInfo.getCommandOutputProcessingInfoList().get(0).getExtractedPropertyName(),
                "BlockedVirtualMachines");
    }

    @Test
    public void testGetRemediationCommandListStopVMs() throws MangleException {
        AzureVMFaultSpec spec = mockData.getAzureVMStateFaultSpec();
        AzureVMFaultHelper helper = new AzureVMFaultHelper(factory);

        List<CommandInfo> commandInfos = helper.getRemediationCommandInfoList(executor, spec);
        Assert.assertEquals(1, commandInfos.size());
        CommandInfo commandInfo = commandInfos.get(0);
        Assert.assertTrue(commandInfo.getCommand().contains(AzureVMFaultRemediation.START_VMS.name()));
    }

    @Test
    public void testGetRemediationCommandListRestartVMs() throws MangleException {
        AzureVMFaultSpec spec = mockData.getAzureVMStateFaultSpec();
        spec.setFaultName(AzureVMStateFaults.RESTART_VMS.name());
        AzureVMFaultHelper helper = new AzureVMFaultHelper(factory);

        List<CommandInfo> commandInfos = helper.getRemediationCommandInfoList(executor, spec);
        Assert.assertEquals(commandInfos.size(), 0);
    }

    @Test
    public void testGetRemediationCommandListDeleteVMs() throws MangleException {
        AzureVMFaultSpec spec = mockData.getAzureVMStateFaultSpec();
        spec.setFaultName(AzureVMStateFaults.DELETE_VMS.name());
        AzureVMFaultHelper helper = new AzureVMFaultHelper(factory);

        List<CommandInfo> commandInfos = helper.getRemediationCommandInfoList(executor, spec);
        Assert.assertEquals(commandInfos.size(), 0);
    }

    @Test
    public void testGetRemediationCommandListBlockNetworkFaultVMs() throws MangleException {
        AzureVMFaultSpec spec = mockData.getAzureVMStateFaultSpec();
        spec.setFaultName(AzureNetworkFaults.BLOCK_ALL_VM_NETWORK_TRAFFIC.name());
        AzureVMFaultHelper helper = new AzureVMFaultHelper(factory);

        List<CommandInfo> commandInfos = helper.getRemediationCommandInfoList(executor, spec);
        Assert.assertEquals(1, commandInfos.size());
        CommandInfo commandInfo = commandInfos.get(0);
        Assert.assertTrue(
                commandInfo.getCommand().contains(AzureNetworkFaultRemediation.UNBLOCK_ALL_VM_NETWORK_TRAFFIC.name()));
    }

    @Test
    public void testGetRemediationCommandListEmptyFaultType() throws MangleException {
        AzureVMFaultSpec spec = mockData.getAzureVMStateFaultSpec();
        spec.setFaultName("");
        AzureVMFaultHelper helper = new AzureVMFaultHelper(factory);
        List<CommandInfo> list = helper.getRemediationCommandInfoList(executor, spec);
        Assert.assertEquals(list.size(), 0);
    }

    @Test
    public void testGetExecutor() throws Exception {
        AzureVMFaultSpec spec = mockData.getAzureVMStateFaultSpec();
        AzureVMFaultHelper helper = new AzureVMFaultHelper(factory);
        AzureCommandExecutor azureCommandExecutor = Mockito.mock(AzureCommandExecutor.class);
        PowerMockito.whenNew(AzureCommandExecutor.class).withArguments(any(CustomAzureClient.class))
                .thenReturn(azureCommandExecutor);
        when(factory.getEndPointClient(any(), any())).thenReturn(azureClient);

        ICommandExecutor commandExecutor = helper.getExecutor(spec);
        Assert.assertNotNull(commandExecutor);
    }

    @Test
    public void testGetAgentFaultInjectionScripts() throws MangleException {
        AwsEC2FaultHelper helper = new AwsEC2FaultHelper(factory);
        List<SupportScriptInfo> supportScriptInfos = helper.getAgentFaultInjectionScripts();
        Assert.assertEquals(supportScriptInfos.size(), 0);
    }

    @Test
    public void testGetInstanceIds() throws MangleException {
        AzureVMFaultSpec spec = mockData.getAzureVMStateFaultSpec();
        AzureVMFaultHelper helper = new AzureVMFaultHelper(factory);

        when(factory.getEndPointClient(any(), any())).thenReturn(azureClient);
        PowerMockito.when(AzureCommonUtils.getAzureVMResourceIds(azureClient, new HashMap<>(), true))
                .thenReturn(Collections.emptyList());
        List<String> supportScriptInfos = helper.getResourceIds(new HashMap<>(), true, spec);
        Assert.assertTrue(supportScriptInfos.isEmpty());
    }
}