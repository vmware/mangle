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

package com.vmware.mangle.unittest.faults.plugin.tasks.helpers;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.faults.plugin.helpers.systemresource.SystemResourceFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.systemresource.SystemResourceFaultHelperFactory;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.faults.plugin.tasks.helpers.SystemResourceFaultTaskHelper;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author jayasankarr
 *
 *         Test for SystemResourceFaultTask
 */
public class SystemResourceFaultHelperTest {
    private FaultsMockData faultsMockData;

    @Mock
    private SystemResourceFaultHelper systemResourceFaultHelper;
    @Mock
    private SystemResourceFaultHelperFactory systemResourceFaultHelperFactory;
    @Mock
    List<CommandInfo> value;

    @Mock
    private CommandExecutionFaultSpec taskData;

    @InjectMocks
    private SystemResourceFaultTaskHelper<CommandExecutionFaultSpec> injectionTask;

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        faultsMockData = new FaultsMockData();
    }

    @Test
    public void testInitOfInjection() {
        Task<CommandExecutionFaultSpec> task = null;
        try {
            when(systemResourceFaultHelperFactory.getHelper(Mockito.any()))
                    .thenReturn(systemResourceFaultHelper);
            doNothing().when(taskData).setInjectionCommandInfoList(value);
            when(systemResourceFaultHelper.getInjectionCommandInfoList(Mockito.any())).thenReturn(value);

            task = injectionTask.init(faultsMockData.getK8sCpuJvmAgentFaultSpec(), null);

            verify(systemResourceFaultHelperFactory, times(1)).getHelper(any(EndpointSpec.class));
            verify(systemResourceFaultHelper, times(1))
                    .getInjectionCommandInfoList(any(CommandExecutionFaultSpec.class));
        } catch (MangleException e1) {
            e1.printStackTrace();
        }
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        task.getTriggers().add(new TaskTrigger());
    }


}
