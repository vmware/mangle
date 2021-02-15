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

package com.vmware.mangle.unittest.faults.plugin.helpers.aws;

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
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.model.aws.AwsEC2FaultRemediation;
import com.vmware.mangle.model.aws.AwsEC2NetworkFaults;
import com.vmware.mangle.model.aws.AwsEC2StateFaults;
import com.vmware.mangle.model.aws.AwsService;
import com.vmware.mangle.model.aws.faults.spec.AwsEC2FaultSpec;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.aws.AWSCommandExecutor;
import com.vmware.mangle.utils.clients.aws.AWSCommonUtils;
import com.vmware.mangle.utils.clients.aws.CustomAwsClient;
import com.vmware.mangle.utils.exceptions.MangleException;


/**
 * Test Class for AwsEC2FaultHelper
 *
 * @author bkaranam
 *
 */
@PrepareForTest({ AWSCommonUtils.class, AwsEC2FaultHelper.class })
@PowerMockIgnore({ "javax.xml.parsers.*", "com.sun.org.apache.xerces.internal.jaxp.*", "org.apache.logging.log4j.*" })
public class AwsEC2FaultHelperTest extends PowerMockTestCase {

    private FaultsMockData mockData = new FaultsMockData();

    @Mock
    private EndpointClientFactory factory;
    @Mock
    private ICommandExecutor executor;
    @Mock
    private CustomAwsClient awsClient;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(AWSCommonUtils.class);
    }

    @Test
    public void testAwsEC2FaultHelperInit() {
        AwsEC2FaultHelper helper = new AwsEC2FaultHelper(factory);
        Assert.assertNotNull(helper);
    }

    @Test
    public void testGetInjectionCommandInfoList() throws MangleException {
        AwsEC2FaultSpec spec = mockData.getAwsEC2InstanceStateFaultSpec();
        AwsEC2FaultHelper helper = new AwsEC2FaultHelper(factory);

        List<CommandInfo> commandInfos = helper.getInjectionCommandInfoList(executor, spec);
        Assert.assertEquals(1, commandInfos.size());
        CommandInfo commandInfo = commandInfos.get(0);
        Assert.assertTrue(commandInfo.getCommand().startsWith(spec.getFaultName()));
    }

    @Test
    public void testGetRemediationCommandListStopInstances() throws MangleException {
        AwsEC2FaultSpec spec = mockData.getAwsEC2InstanceStateFaultSpec();
        AwsEC2FaultHelper helper = new AwsEC2FaultHelper(factory);

        List<CommandInfo> commandInfos = helper.getRemediationCommandInfoList(executor, spec);
        Assert.assertEquals(1, commandInfos.size());
        CommandInfo commandInfo = commandInfos.get(0);
        Assert.assertTrue(commandInfo.getCommand().contains(AwsEC2FaultRemediation.START_INSTANCES.name()));
    }

    @Test
    public void testGetRemediationCommandListRebootInstances() throws MangleException {
        AwsEC2FaultSpec spec = mockData.getAwsEC2InstanceStateFaultSpec();
        spec.setFaultName(AwsEC2StateFaults.REBOOT_INSTANCES.name());
        AwsEC2FaultHelper helper = new AwsEC2FaultHelper(factory);

        List<CommandInfo> commandInfos = helper.getRemediationCommandInfoList(executor, spec);
        Assert.assertEquals(commandInfos.size(), 0);
    }

    @Test
    public void testGetRemediationCommandListTerminateInstances() throws MangleException {
        AwsEC2FaultSpec spec = mockData.getAwsEC2InstanceStateFaultSpec();
        spec.setFaultName(AwsEC2StateFaults.TERMINATE_INSTANCES.name());
        AwsEC2FaultHelper helper = new AwsEC2FaultHelper(factory);

        List<CommandInfo> commandInfos = helper.getRemediationCommandInfoList(executor, spec);
        Assert.assertEquals(commandInfos.size(), 0);
    }

    @Test
    public void testGetRemediationCommandListBlockAllNetworkFault() throws MangleException {
        AwsEC2FaultSpec spec = mockData.getAwsEC2InstanceStateFaultSpec();
        spec.setFaultName(AwsEC2NetworkFaults.BLOCK_ALL_NETWORK_TRAFFIC.name());
        AwsEC2FaultHelper helper = new AwsEC2FaultHelper(factory);

        List<CommandInfo> commandInfos = helper.getRemediationCommandInfoList(executor, spec);
        CommandInfo commandInfo = commandInfos.get(0);
        Assert.assertEquals(commandInfos.size(), 1);
        Assert.assertTrue(commandInfo.getCommand().contains(AwsEC2FaultRemediation.UNBLOCK_ALL_NETWORK_TRAFFIC.name()));
    }

    @Test
    public void testGetInjectionCommandInfoListForBlockAllNetworkFault() throws MangleException {
        AwsEC2FaultSpec spec = mockData.getAwsEC2InstanceStateFaultSpec();
        AwsEC2FaultHelper helper = new AwsEC2FaultHelper(factory);
        spec.setFaultName(AwsEC2NetworkFaults.BLOCK_ALL_NETWORK_TRAFFIC.name());
        List<CommandInfo> commandInfos = helper.getInjectionCommandInfoList(executor, spec);
        Assert.assertEquals(1, commandInfos.size());
        CommandInfo commandInfo = commandInfos.get(0);
        Assert.assertTrue(commandInfo.getCommand().startsWith(spec.getFaultName()));
    }

    @Test
    public void testGetRemediationCommandListEmptyFaultType() throws MangleException {
        AwsEC2FaultSpec spec = mockData.getAwsEC2InstanceStateFaultSpec();
        spec.setFaultName("");
        AwsEC2FaultHelper helper = new AwsEC2FaultHelper(factory);
        List<CommandInfo> list = helper.getRemediationCommandInfoList(executor, spec);
        Assert.assertEquals(list.size(), 0);
    }

    @Test
    public void testGetExecutor() throws Exception {
        AwsEC2FaultSpec spec = mockData.getAwsEC2InstanceStateFaultSpec();
        AwsEC2FaultHelper helper = new AwsEC2FaultHelper(factory);
        AWSCommandExecutor awsCommandExecutor = Mockito.mock(AWSCommandExecutor.class);
        PowerMockito.whenNew(AWSCommandExecutor.class).withArguments(any(CustomAwsClient.class), any(AwsService.class))
                .thenReturn(awsCommandExecutor);
        when(factory.getEndPointClient(any(), any())).thenReturn(awsClient);

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
        AwsEC2FaultSpec spec = mockData.getAwsEC2InstanceStateFaultSpec();
        AwsEC2FaultHelper helper = new AwsEC2FaultHelper(factory);

        when(factory.getEndPointClient(any(), any())).thenReturn(awsClient);
        PowerMockito.when(AWSCommonUtils.getAwsInstances(awsClient, new HashMap<>(), true))
                .thenReturn(Collections.emptyList());
        List<String> supportScriptInfos = helper.getInstanceIds(new HashMap<>(), true, spec);
        Assert.assertTrue(supportScriptInfos.isEmpty());
    }
}