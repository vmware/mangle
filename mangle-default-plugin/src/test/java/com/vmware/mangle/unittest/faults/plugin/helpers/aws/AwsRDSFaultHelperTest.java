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

import java.util.ArrayList;
import java.util.Collections;
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

import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.faults.plugin.helpers.aws.AwsRDSFaultHelper;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.model.aws.AwsRDSFaultRemediation;
import com.vmware.mangle.model.aws.AwsRDSFaults;
import com.vmware.mangle.model.aws.AwsRDSInstance;
import com.vmware.mangle.model.aws.AwsService;
import com.vmware.mangle.model.aws.faults.spec.AwsRDSFaultSpec;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.aws.AWSCommandExecutor;
import com.vmware.mangle.utils.clients.aws.AWSCommonUtils;
import com.vmware.mangle.utils.clients.aws.CustomAwsClient;
import com.vmware.mangle.utils.exceptions.MangleException;


/**
 * Test Class for AwsRDSFaultHelper
 *
 * @author bkaranam
 *
 */
@PrepareForTest({ AWSCommonUtils.class, AwsRDSFaultHelper.class })
@PowerMockIgnore({ "javax.xml.parsers.*", "com.sun.org.apache.xerces.internal.jaxp.*", "org.apache.logging.log4j.*" })
public class AwsRDSFaultHelperTest extends PowerMockTestCase {

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
    public void testAwsRDSFaultHelperInit() {
        AwsRDSFaultHelper helper = new AwsRDSFaultHelper(factory);
        Assert.assertNotNull(helper);
    }

    @Test
    public void testGetInjectionCommandInfoList() throws MangleException {
        AwsRDSFaultSpec spec = mockData.getAwsRDSFaultSpec();
        AwsRDSFaultHelper helper = new AwsRDSFaultHelper(factory);

        List<CommandInfo> commandInfos = helper.getInjectionCommandInfoList(executor, spec);
        Assert.assertEquals(1, commandInfos.size());
        CommandInfo commandInfo = commandInfos.get(0);
        Assert.assertTrue(commandInfo.getCommand().startsWith(spec.getFaultName()));
    }

    @Test
    public void testGetInjectionCommandInfoListForConnectionLossFault() throws MangleException {
        AwsRDSFaultSpec spec = mockData.getAwsRDSFaultSpec();
        spec.setFaultName(AwsRDSFaults.CONNECTION_LOSS.name());
        AwsRDSFaultHelper helper = new AwsRDSFaultHelper(factory);

        List<CommandInfo> commandInfos = helper.getInjectionCommandInfoList(executor, spec);
        Assert.assertEquals(1, commandInfos.size());
        CommandInfo commandInfo = commandInfos.get(0);
        Assert.assertTrue(commandInfo.getCommandOutputProcessingInfoList().size() == 1);
        Assert.assertTrue(commandInfo.getCommand().startsWith(spec.getFaultName()));
    }

    @Test
    public void testGetRemediationCommandListStopInstances() throws MangleException {
        AwsRDSFaultSpec spec = mockData.getAwsRDSFaultSpec();
        AwsRDSFaultHelper helper = new AwsRDSFaultHelper(factory);

        List<CommandInfo> commandInfos = helper.getRemediationCommandInfoList(executor, spec);
        Assert.assertEquals(1, commandInfos.size());
        CommandInfo commandInfo = commandInfos.get(0);
        Assert.assertTrue(commandInfo.getCommand().contains(AwsRDSFaultRemediation.START_INSTANCES.name()));
    }

    @Test
    public void testGetRemediationCommandListRebootInstances() throws MangleException {
        AwsRDSFaultSpec spec = mockData.getAwsRDSFaultSpec();
        spec.setFaultName(AwsRDSFaults.REBOOT_INSTANCES.name());
        AwsRDSFaultHelper helper = new AwsRDSFaultHelper(factory);

        List<CommandInfo> commandInfos = helper.getRemediationCommandInfoList(executor, spec);
        Assert.assertEquals(commandInfos.size(), 0);
    }

    @Test
    public void testGetRemediationCommandListConnectionLossInstances() throws MangleException {
        AwsRDSFaultSpec spec = mockData.getAwsRDSFaultSpec();
        spec.setFaultName(AwsRDSFaults.CONNECTION_LOSS.name());
        AwsRDSFaultHelper helper = new AwsRDSFaultHelper(factory);
        List<CommandInfo> commandInfos = helper.getRemediationCommandInfoList(executor, spec);
        Assert.assertEquals(commandInfos.size(), 1);
        Assert.assertTrue(commandInfos.get(0).getCommand().contains(AwsRDSFaultRemediation.CONNECTION_RESET.name()));
    }

    @Test
    public void testGetExecutor() throws Exception {
        AwsRDSFaultSpec spec = mockData.getAwsRDSFaultSpec();
        AwsRDSFaultHelper helper = new AwsRDSFaultHelper(factory);
        AWSCommandExecutor awsCommandExecutor = Mockito.mock(AWSCommandExecutor.class);
        PowerMockito.whenNew(AWSCommandExecutor.class).withArguments(any(CustomAwsClient.class), any(AwsService.class))
                .thenReturn(awsCommandExecutor);
        when(factory.getEndPointClient(any(), any())).thenReturn(awsClient);
        ICommandExecutor commandExecutor = helper.getExecutor(spec);
        Assert.assertNotNull(commandExecutor);
    }

    @Test
    public void testGetRdsInstances() throws MangleException {
        AwsRDSFaultSpec spec = mockData.getAwsRDSFaultSpec();
        AwsRDSFaultHelper helper = new AwsRDSFaultHelper(factory);

        when(factory.getEndPointClient(any(), any())).thenReturn(awsClient);
        PowerMockito.when(AWSCommonUtils.verifyAndSelectInstances(awsClient, new ArrayList<>(), true))
                .thenReturn(Collections.emptyList());
        List<AwsRDSInstance> rdsInstances = helper.getRdsInstances(new ArrayList<String>(), true, spec);
        Assert.assertTrue(rdsInstances.isEmpty());
    }
}