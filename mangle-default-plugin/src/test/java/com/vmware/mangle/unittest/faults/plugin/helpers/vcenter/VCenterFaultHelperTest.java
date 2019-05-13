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

package com.vmware.mangle.unittest.faults.plugin.helpers.vcenter;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.VMDiskFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.faults.plugin.helpers.vcenter.VCenterFaultHelper;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.services.enums.VCenterFaultRemediation;
import com.vmware.mangle.services.enums.VCenterStateFaults;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.vcenter.VCenterClient;
import com.vmware.mangle.utils.clients.vcenter.VCenterCommandExecutor;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 *
 *
 * @author chetanc
 */
@PrepareForTest(value = { VCenterFaultHelper.class })
public class VCenterFaultHelperTest {

    private FaultsMockData mockData = new FaultsMockData();

    @Mock
    private EndpointClientFactory factory;
    @Mock
    private ICommandExecutor executor;
    @Mock
    private VCenterClient vCenterClient;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testVCenterFaultHelperInit() {
        VCenterFaultHelper helper = new VCenterFaultHelper(factory);
        Assert.assertNotNull(helper);
    }

    @Test
    public void testGetInjectionCommandInfoList() throws MangleException {
        VMFaultSpec spec = mockData.getVMStateFaultSpec();
        VCenterFaultHelper helper = new VCenterFaultHelper(factory);

        List<CommandInfo> commandInfos = helper.getInjectionCommandInfoList(executor, spec);
        Assert.assertEquals(1, commandInfos.size());
        CommandInfo commandInfo = commandInfos.get(0);
        Assert.assertTrue(commandInfo.getCommand().startsWith(spec.getFaultName()));
    }

    @Test
    public void testGetInjectionCommandInfoListForDisDisk() throws MangleException {
        VMFaultSpec spec = mockData.getVMDiskFaultSpec();
        spec.setFaultName(((VMDiskFaultSpec) spec).getFault().name());
        VCenterFaultHelper helper = new VCenterFaultHelper(factory);

        List<CommandInfo> commandInfos = helper.getInjectionCommandInfoList(executor, spec);
        Assert.assertEquals(1, commandInfos.size());
        CommandInfo commandInfo = commandInfos.get(0);
        Assert.assertEquals(3, commandInfo.getCommandOutputProcessingInfoList().size());
        Assert.assertTrue(commandInfo.getCommand().startsWith(spec.getFaultName()));
    }

    @Test
    public void testGetRemediationCommandListPowerOFFVM() throws MangleException {
        VMFaultSpec spec = mockData.getVMStateFaultSpec();
        VCenterFaultHelper helper = new VCenterFaultHelper(factory);
        List<CommandInfo> list = helper.getRemediationCommandInfoList(executor, spec);
        Assert.assertEquals(1, list.size());
        CommandInfo commandInfo = list.get(0);
        Assert.assertTrue(commandInfo.getCommand().contains(VCenterFaultRemediation.POWERON_VM.name()));
    }

    @Test
    public void testGetRemediationCommandListSuspendVM() throws MangleException {
        VMFaultSpec spec = mockData.getVMStateFaultSpec();
        spec.setFaultName(VCenterStateFaults.SUSPEND_VM.name());
        VCenterFaultHelper helper = new VCenterFaultHelper(factory);
        List<CommandInfo> list = helper.getRemediationCommandInfoList(executor, spec);
        Assert.assertEquals(1, list.size());
        CommandInfo commandInfo = list.get(0);
        Assert.assertTrue(commandInfo.getCommand().contains(VCenterFaultRemediation.POWERON_VM.name()));
    }

    @Test
    public void testGetRemediationCommandListDisconnectDisk() throws MangleException {
        VMFaultSpec spec = mockData.getVMDiskFaultSpec();
        VCenterFaultHelper helper = new VCenterFaultHelper(factory);
        List<CommandInfo> list = helper.getRemediationCommandInfoList(executor, spec);
        Assert.assertEquals(1, list.size());
        CommandInfo commandInfo = list.get(0);
        Assert.assertTrue(commandInfo.getCommand().contains(VCenterFaultRemediation.CONNECT_DISK.name()));
    }

    @Test
    public void testGetRemediationCommandListDisconnectNic() throws MangleException {
        VMFaultSpec spec = mockData.getVMNicFaultSpec();
        VCenterFaultHelper helper = new VCenterFaultHelper(factory);
        List<CommandInfo> list = helper.getRemediationCommandInfoList(executor, spec);
        Assert.assertEquals(1, list.size());
        CommandInfo commandInfo = list.get(0);
        Assert.assertTrue(commandInfo.getCommand().contains(VCenterFaultRemediation.CONNECT_NIC.name()));
    }

    @Test
    public void testGetRemediationCommandListEmptyFaultType() throws MangleException {
        VMFaultSpec spec = mockData.getVMNicFaultSpec();
        spec.setFaultName("");
        VCenterFaultHelper helper = new VCenterFaultHelper(factory);
        List<CommandInfo> list = helper.getRemediationCommandInfoList(executor, spec);
        Assert.assertEquals(list.size(), 0);
    }

    @Test
    public void testGetExecutor() throws Exception {
        VMFaultSpec spec = mockData.getVMNicFaultSpec();
        VCenterFaultHelper helper = new VCenterFaultHelper(factory);
        VCenterCommandExecutor vCenterCommandExecutor = Mockito.mock(VCenterCommandExecutor.class);
        PowerMockito.whenNew(VCenterCommandExecutor.class).withArguments(any(VCenterClient.class))
                .thenReturn(vCenterCommandExecutor);
        when(factory.getEndPointClient(any(), any())).thenReturn(vCenterClient);

        ICommandExecutor commandExecutor = helper.getExecutor(spec);
        Assert.assertNotNull(commandExecutor);
    }

    @Test
    public void testGetAgentFaultInjectionScripts() throws MangleException {
        VCenterFaultHelper helper = new VCenterFaultHelper(factory);

        List<SupportScriptInfo> supportScriptInfos = helper.getAgentFaultInjectionScripts();
        Assert.assertEquals(supportScriptInfos.size(), 0);
    }
}
