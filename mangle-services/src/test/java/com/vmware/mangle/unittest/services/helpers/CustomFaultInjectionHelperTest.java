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

package com.vmware.mangle.unittest.services.helpers;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.custom.faults.CustomFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.PluginFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.services.PluginDetailsService;
import com.vmware.mangle.services.PluginService;
import com.vmware.mangle.services.helpers.CustomFaultInjectionHelper;
import com.vmware.mangle.services.helpers.FaultInjectionHelper;
import com.vmware.mangle.services.mockdata.CredentialsSpecMockData;
import com.vmware.mangle.services.mockdata.CustomFaultMockData;
import com.vmware.mangle.services.mockdata.EndpointMockData;
import com.vmware.mangle.services.mockdata.TasksMockData;
import com.vmware.mangle.task.framework.helpers.AbstractTaskHelper;
import com.vmware.mangle.task.framework.helpers.faults.AbstractCustomFault;
import com.vmware.mangle.utils.exceptions.MangleException;


/**
 * Unit Test cases for CustomFaultInjectionHelper.
 *
 * @author kumargautam
 */
@SuppressWarnings("deprecation")
public class CustomFaultInjectionHelperTest {
    @Mock
    private PluginService pluginService;
    @Mock
    private PluginDetailsService pluginDetailsService;
    @Mock
    private FaultInjectionHelper faultInjectionHelper;
    @Mock
    private ObjectMapper mapper;
    @InjectMocks
    private CustomFaultInjectionHelper customFaultInjectionHelper;
    private CustomFaultSpec customFaultSpec;
    private CustomFault customFault;
    private EndpointMockData endpointMockData;
    private CredentialsSpecMockData credentialsSpecMockData;
    private ObjectMapper mapper1 = new ObjectMapper();
    private TasksMockData<TaskSpec> tasksMockData;

    @BeforeMethod
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        this.customFaultSpec = new CustomFaultMockData().getCustomFaultSpec();
        this.customFault = new CustomFault();
        this.endpointMockData = new EndpointMockData();
        this.credentialsSpecMockData = new CredentialsSpecMockData();
        this.tasksMockData = new TasksMockData<>(new CommandExecutionFaultSpec());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.helpers.CustomFaultInjectionHelper#getCustomFault(com.vmware.mangle.cassandra.model.custom.faults.CustomFaultSpec)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetCustomFault() throws MangleException {
        when(pluginService.getExtensionForCustomFault(anyString())).thenReturn(mock(AbstractCustomFault.class));
        when(pluginDetailsService.isPluginAvailable(customFaultSpec.getPluginId())).thenReturn(true);
        assertNotNull(customFaultInjectionHelper.getCustomFault(customFaultSpec));
        verify(pluginService, times(1)).getExtensionForCustomFault(anyString());
        verify(pluginDetailsService, times(1)).isPluginAvailable(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.helpers.CustomFaultInjectionHelper#getFaultSpec(com.vmware.mangle.cassandra.model.custom.faults.CustomFaultSpec, com.vmware.mangle.task.framework.helpers.faults.AbstractCustomFault)}.
     */
    @Test
    public void testGetFaultSpec() {
        customFaultInjectionHelper.setMapper(mapper1);
        assertNotNull(customFaultInjectionHelper.getFaultSpec(customFaultSpec, customFault));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.helpers.CustomFaultInjectionHelper#validateFields(com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec, com.vmware.mangle.task.framework.helpers.faults.AbstractCustomFault)}.
     *
     * @throws MangleException
     */
    @Test
    public void testValidateFields() throws MangleException {
        customFaultInjectionHelper.setMapper(mapper1);
        PluginFaultSpec requestJson = customFaultInjectionHelper.getFaultSpec(customFaultSpec, customFault);
        requestJson.setEndpoint(endpointMockData.rmEndpointMockData());
        requestJson.setCredentials(credentialsSpecMockData.getRMCredentialsData());
        doNothing().when(faultInjectionHelper).validateSpec(any(CommandExecutionFaultSpec.class));
        customFaultInjectionHelper.validateFields(requestJson, customFault);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.helpers.CustomFaultInjectionHelper#invokeFault(com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec, java.lang.String)}.
     *
     * @throws MangleException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testInvokeFault() throws MangleException {
        customFaultInjectionHelper.setMapper(mapper1);
        PluginFaultSpec requestJson = customFaultInjectionHelper.getFaultSpec(customFaultSpec, customFault);
        requestJson.setEndpoint(endpointMockData.rmEndpointMockData());
        requestJson.setCredentials(credentialsSpecMockData.getRMCredentialsData());
        Task<TaskSpec> task = tasksMockData.getDummyTask();
        when(faultInjectionHelper.saveTask(any(Task.class))).thenReturn(task);
        AbstractTaskHelper<TaskSpec> taskHelper = mock(AbstractTaskHelper.class);
        when(pluginService.getExtension(anyString())).thenReturn(taskHelper);
        when(taskHelper.init(any(TaskSpec.class), anyString())).thenReturn(task);
        assertNotNull(customFaultInjectionHelper.invokeFault(requestJson, "test"));
        verify(faultInjectionHelper, times(1)).saveTask(any(Task.class));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.helpers.CustomFaultInjectionHelper#invokeFault(com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec, java.lang.String)}.
     *
     * @throws MangleException
     */
    @SuppressWarnings("unchecked")
    @Test(expectedExceptions = { MangleException.class })
    public void testInvokeFaultWithMangleException() throws MangleException {
        customFaultInjectionHelper.setMapper(mapper1);
        PluginFaultSpec requestJson = customFaultInjectionHelper.getFaultSpec(customFaultSpec, customFault);
        requestJson.setEndpoint(endpointMockData.rmEndpointMockData());
        requestJson.setCredentials(credentialsSpecMockData.getRMCredentialsData());
        Task<TaskSpec> task = tasksMockData.getDummyTask();
        when(faultInjectionHelper.saveTask(any(Task.class))).thenReturn(task);
        AbstractTaskHelper<TaskSpec> taskHelper = mock(AbstractTaskHelper.class);
        when(pluginService.getExtension(anyString())).thenReturn(null);
        when(taskHelper.init(any(TaskSpec.class), anyString())).thenReturn(task);
        assertNull(customFaultInjectionHelper.invokeFault(requestJson, "test1"));
        verify(faultInjectionHelper, times(1)).saveTask(any(Task.class));
    }
}

