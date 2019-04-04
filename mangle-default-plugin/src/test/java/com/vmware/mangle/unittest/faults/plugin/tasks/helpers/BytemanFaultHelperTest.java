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
import com.vmware.mangle.cassandra.model.faults.specs.JVMAgentFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMCodeLevelFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.faults.plugin.helpers.byteman.BytemanFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.byteman.BytemanFaultHelperFactory;
import com.vmware.mangle.faults.plugin.helpers.byteman.K8sBytemanFaultHelper;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.faults.plugin.tasks.helpers.BytemanFaultTaskHelper;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 *
 *
 * @author hkilari
 */
public class BytemanFaultHelperTest {
    FaultsMockData faultsMockData = new FaultsMockData();

    @Mock
    private BytemanFaultHelper bytemanFaultHelper;
    @Mock
    private BytemanFaultHelperFactory bytemanFaultHelperFactory;
    @Mock
    private K8sBytemanFaultHelper k8sBytemanFaultHelper;
    @Mock
    List<CommandInfo> value;
    @Mock
    private CommandExecutionFaultSpec taskData;

    @InjectMocks
    private BytemanFaultTaskHelper<JVMAgentFaultSpec> jvmInjectionTask;

    @InjectMocks
    private BytemanFaultTaskHelper<JVMCodeLevelFaultSpec> codeLevelInjectionTask;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void testInitOfInjection() {
        BytemanFaultTaskHelper<JVMAgentFaultSpec> injectionTask = jvmInjectionTask;
        Task<JVMAgentFaultSpec> task = null;
        try {
            when(bytemanFaultHelperFactory.getHelper(Mockito.any())).thenReturn(bytemanFaultHelper);
            doNothing().when(taskData).setInjectionCommandInfoList(value);
            when(bytemanFaultHelper.getInjectionCommandInfoList(Mockito.any())).thenReturn(value);

            task = injectionTask.init(faultsMockData.getK8sCpuJvmAgentFaultSpec(), null);
            jvmInjectionTask.init(faultsMockData.getK8sCpuJvmAgentFaultSpec(), null);

            verify(bytemanFaultHelperFactory, times(2)).getHelper(any(EndpointSpec.class));
            verify(bytemanFaultHelper, times(2))
                    .getInjectionCommandInfoList(any(CommandExecutionFaultSpec.class));
        } catch (MangleException e1) {
            e1.printStackTrace();
        }
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        Assert.assertEquals(task.getTaskDescription(),
                "Executing Fault: cpuFault on endpoint:k8sEPTest. More Details: JVMAgentFaultSpec(jvmProperties=JVMProperties(jvmprocess=app.jar, javaHomePath=/usr/java/latest, user=testUser, port=9091))");

        task.getTriggers().add(new TaskTrigger());
        try {
            injectionTask.executeTask(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInjectionOfJVMCodeLevelFault() {
        BytemanFaultTaskHelper<JVMCodeLevelFaultSpec> injectionTask = codeLevelInjectionTask;
        Task<JVMCodeLevelFaultSpec> task = null;
        try {
            when(bytemanFaultHelperFactory.getHelper(Mockito.any())).thenReturn(bytemanFaultHelper);
            doNothing().when(taskData).setInjectionCommandInfoList(value);
            when(bytemanFaultHelper.getInjectionCommandInfoList(Mockito.any())).thenReturn(value);
            task = injectionTask.init(faultsMockData.getK8sSpringExceptionJVMCodeLevelFaultSpec(), null);
            when(bytemanFaultHelperFactory.getHelper(Mockito.any())).thenReturn(bytemanFaultHelper);
            doNothing().when(taskData).setInjectionCommandInfoList(value);
            when(bytemanFaultHelper.getInjectionCommandInfoList(Mockito.any())).thenReturn(value);
            codeLevelInjectionTask.init(faultsMockData.getK8sSpringExceptionJVMCodeLevelFaultSpec(), null);
        } catch (MangleException e1) {
            e1.printStackTrace();
        }
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        Assert.assertEquals(task.getTaskDescription(),
                "Executing Fault: SPRING_SERVICE_EXCEPTION on endpoint:k8sEPTest. More Details: JVMCodeLevelFaultSpec(className=null, ruleEvent=null, methodName=null)");

        task.getTriggers().add(new TaskTrigger());
        try {
            injectionTask.executeTask(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
