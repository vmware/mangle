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

package com.vmware.mangle.unittest.utils.clients.azure;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.VirtualMachines;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.utils.clients.azure.AzureCommonUtils;
import com.vmware.mangle.utils.clients.azure.AzureDisk;
import com.vmware.mangle.utils.clients.azure.AzureVMFaultOperations;
import com.vmware.mangle.utils.clients.azure.CustomAzureClient;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;


/**
 * Unit Test Case for AzureVMFaultOperations
 *
 * @author bkaranam
 */
@PrepareForTest(value = { AzureCommonUtils.class, Azure.class })
@PowerMockIgnore({ "javax.net.ssl.*", "javax.xml.parsers.*", "com.sun.org.apache.xerces.internal.jaxp.*",
        "org.apache.logging.log4j.*", "com.sun.org.apache.xalan.internal.xsltc.trax.*" })
public class AzureVMFaultOperationsTest extends PowerMockTestCase {

    @Mock
    private CustomAzureClient customAzureClient;

    @Mock
    private Azure azure;

    @Mock
    private VirtualMachines virtualMachines;
    @Mock
    private CommandExecutionResult commandExecutionResult;

    /**
     * @throws Exception
     */
    @BeforeMethod
    public void setUpBeforeMethod() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mock(Azure.class);
        when(customAzureClient.getClient()).thenReturn(azure);
        when(azure.virtualMachines()).thenReturn(virtualMachines);
        when(virtualMachines.getById(any())).thenReturn(new DummyVirtualMachineImpl());
    }

    /**
     * @throws Exception
     */
    @AfterClass
    public void tearDownAfterClass() throws Exception {
        this.customAzureClient = null;
    }

    /**
     * @throws Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
        validateMockitoUsage();
    }

    /**
     * Test method for {@link#AzureVMFaultOperations#deleteVirtualmachines}.
     *
     * @throws MangleException
     */
    @SuppressWarnings("unchecked")
    @Test(priority = 1)
    public void testDeleteVirtualmachines() throws MangleException {
        doNothing().when(virtualMachines).deleteById(any());
        CommandExecutionResult result =
                AzureVMFaultOperations.deleteVirtualmachines(customAzureClient, DummyVirtualMachineImpl.RESOURCE_ID);
        assertEquals(result.getExitCode(), 0,
                "Test delete virtual machines failed with exitcode: " + result.getExitCode());
        assertTrue(result.getCommandOutput().contains("DummyVM"),
                "Test delete virtual machines failed with output:" + result.getCommandOutput());
        verify(virtualMachines, times(1)).deleteById(any());
    }

    /**
     * Test method for {@link#AzureVMFaultOperations#deleteVirtualmachines}.
     *
     * @throws MangleException
     */
    @SuppressWarnings("unchecked")
    @Test(priority = 2)
    public void testDeleteVirtualmachinesWithCouldException() throws MangleException {
        doThrow(new CloudException("CloudException", null)).when(virtualMachines).deleteById(any());
        CommandExecutionResult result =
                AzureVMFaultOperations.deleteVirtualmachines(customAzureClient, DummyVirtualMachineImpl.RESOURCE_ID);
        assertEquals(result.getExitCode(), 1,
                "Test delete virtual machines failed with exitcode:" + result.getExitCode());
        assertTrue(result.getCommandOutput().contains("CloudException"),
                "Test delete virtual machines failed with output:" + result.getCommandOutput());
        verify(virtualMachines, times(1)).deleteById(any());
    }

    /**
     * Test method for {@link#AzureVMFaultOperations#deleteVirtualmachines}.
     *
     * @throws MangleException
     */
    @SuppressWarnings("unchecked")
    @Test(priority = 3)
    public void testDeleteVirtualmachinesWithNoVM() throws MangleException {
        when(virtualMachines.getById(any())).thenReturn(null);
        CommandExecutionResult result =
                AzureVMFaultOperations.deleteVirtualmachines(customAzureClient, DummyVirtualMachineImpl.RESOURCE_ID);
        assertEquals(result.getExitCode(), 1,
                "Test delete virtual machines failed with exitcode:" + result.getExitCode());
        assertTrue(result.getCommandOutput().contains("DummyVM not found"),
                "Test delete virtual machines failed with output:" + result.getCommandOutput());
        verify(virtualMachines, times(0)).deleteById(any());
        verify(virtualMachines, times(1)).getById(any());
    }

    /**
     * Test method for {@link#AzureVMFaultOperations#startVirtualmachines}.
     *
     * @throws MangleException
     */
    @SuppressWarnings("unchecked")
    @Test(priority = 4)
    public void testStartVirtualmachinesWithCouldException() throws MangleException {
        doThrow(new CloudException("CloudException", null)).when(virtualMachines).start(any(), any());
        CommandExecutionResult result =
                AzureVMFaultOperations.startVirtualmachines(customAzureClient, DummyVirtualMachineImpl.RESOURCE_ID);
        assertEquals(result.getExitCode(), 1,
                "Test start virtual machines failed with exitcode:" + result.getExitCode());
        assertTrue(result.getCommandOutput().contains("CloudException"),
                "Test start virtual machines failed with output:" + result.getCommandOutput());
        verify(virtualMachines, times(1)).start(any(), any());
    }

    /**
     * Test method for {@link#AzureVMFaultOperations#startVirtualmachines}.
     *
     * @throws MangleException
     */
    @SuppressWarnings("unchecked")
    @Test(priority = 5)
    public void testStartVirtualmachines() throws MangleException {
        doNothing().when(virtualMachines).start(any(), any());
        CommandExecutionResult result =
                AzureVMFaultOperations.startVirtualmachines(customAzureClient, DummyVirtualMachineImpl.RESOURCE_ID);
        assertEquals(result.getExitCode(), 0,
                "Test start virtual machines failed with exitcode:" + result.getExitCode());
        assertTrue(result.getCommandOutput().contains("DummyVM"),
                "Test start virtual machines failed with output:" + result.getCommandOutput());
        verify(virtualMachines, times(1)).start(any(), any());
    }

    /**
     * Test method for {@link#AzureVMFaultOperations#stopVirtualmachines}.
     *
     * @throws MangleException
     */
    @SuppressWarnings("unchecked")
    @Test(priority = 6)
    public void testStopVirtualmachinesWithCouldException() throws MangleException {
        doThrow(new CloudException("CloudException", null)).when(virtualMachines).powerOff(any(), any());
        CommandExecutionResult result =
                AzureVMFaultOperations.stopVirtualmachines(customAzureClient, DummyVirtualMachineImpl.RESOURCE_ID);
        assertEquals(result.getExitCode(), 1,
                "Test stop virtual machines failed with exitcode:" + result.getExitCode());
        assertTrue(result.getCommandOutput().contains("CloudException"),
                "Test stop virtual machines failed with output:" + result.getCommandOutput());
        verify(virtualMachines, times(1)).powerOff(any(), any());
    }

    /**
     * Test method for {@link#AzureVMFaultOperations#stopVirtualmachines}.
     *
     * @throws MangleException
     */
    @SuppressWarnings("unchecked")
    @Test(priority = 7)
    public void testStopVirtualmachines() throws MangleException {
        doNothing().when(virtualMachines).powerOff(any(), any());
        CommandExecutionResult result =
                AzureVMFaultOperations.stopVirtualmachines(customAzureClient, DummyVirtualMachineImpl.RESOURCE_ID);
        assertEquals(result.getExitCode(), 0,
                "Test stop virtual machines failed with exitcode:" + result.getExitCode());
        assertTrue(result.getCommandOutput().contains("DummyVM"),
                "Test stop virtual machines failed with output:" + result.getCommandOutput());
        verify(virtualMachines, times(1)).powerOff(any(), any());
    }

    /**
     * Test method for {@link#AzureVMFaultOperations#restartVirtualmachines}.
     *
     * @throws MangleException
     */
    @SuppressWarnings("unchecked")
    @Test(priority = 8)
    public void testRestartVirtualmachinesWithCouldException() throws MangleException {
        doThrow(new CloudException("CloudException", null)).when(virtualMachines).restart(any(), any());
        CommandExecutionResult result =
                AzureVMFaultOperations.restartVirtualmachines(customAzureClient, DummyVirtualMachineImpl.RESOURCE_ID);
        assertEquals(result.getExitCode(), 1,
                "Test restart virtual machines failed with exitcode:" + result.getExitCode());
        assertTrue(result.getCommandOutput().contains("CloudException"),
                "Test restart virtual machines failed with output:" + result.getCommandOutput());
        verify(virtualMachines, times(1)).restart(any(), any());
    }

    /**
     * Test method for {@link#AzureVMFaultOperations#restartVirtualmachines}.
     *
     * @throws MangleException
     */
    @SuppressWarnings("unchecked")
    @Test(priority = 9)
    public void testRestartVirtualmachines() throws MangleException {
        doNothing().when(virtualMachines).restart(any(), any());
        CommandExecutionResult result =
                AzureVMFaultOperations.restartVirtualmachines(customAzureClient, DummyVirtualMachineImpl.RESOURCE_ID);
        assertEquals(result.getExitCode(), 0,
                "Test restart virtual machines failed with exitcode:" + result.getExitCode());
        assertTrue(result.getCommandOutput().contains("DummyVM"),
                "Test restart virtual machines failed with output:" + result.getCommandOutput());
        verify(virtualMachines, times(1)).restart(any(), any());
    }

    /**
     * Test method for {@link#AzureVMFaultOperations#blockAll_VM_NetworkTraffic}.
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test(priority = 10)
    public void testBlockAll_VM_NetworkTraffic() throws Exception {
        PowerMockito.mockStatic(AzureCommonUtils.class);
        Map<String, String> securityGroupsMap = new HashMap<>();
        securityGroupsMap.put(DummyVirtualMachineImpl.NIC_ID, DummyVirtualMachineImpl.SECURITY_GROUP_ID);
        PowerMockito.when(AzureCommonUtils.getSecurityGroups(any(), any())).thenReturn(securityGroupsMap);
        PowerMockito.when(AzureCommonUtils.createSecurityGroup(any(), any(), any()))
                .thenReturn(DummyVirtualMachineImpl.SECURITY_GROUP_ID);
        PowerMockito.doNothing().when(AzureCommonUtils.class, "updateSecurityGroup", eq(customAzureClient), any(),
                any());
        CommandExecutionResult result = AzureVMFaultOperations.blockAll_VM_NetworkTraffic(customAzureClient,
                DummyVirtualMachineImpl.RESOURCE_ID);
        assertEquals(result.getExitCode(), 0,
                "Test blockall virtual machine's network failed with exitcode:" + result.getExitCode());
        assertTrue(result.getCommandOutput().contains(DummyVirtualMachineImpl.SECURITY_GROUP_ID),
                "Test blockall virtual machine's network failed with output:" + result.getCommandOutput());
        PowerMockito.when(AzureCommonUtils.getSecurityGroups(any(), any()))
                .thenThrow(new MangleException("TestBlockAllNetworkFailed", ErrorCode.AZURE_OPERATION_FAILURE));
        result = AzureVMFaultOperations.blockAll_VM_NetworkTraffic(customAzureClient,
                DummyVirtualMachineImpl.RESOURCE_ID);
        assertEquals(result.getExitCode(), 1,
                "Test blockall virtual machine's network failed with exitcode:" + result.getExitCode());
        assertTrue(result.getCommandOutput().contains("TestBlockAllNetworkFailed"),
                "Test blockall virtual machine's network failed with output:" + result.getCommandOutput());
    }

    /**
     * Test method for {@link#AzureVMFaultOperations#unblockAll_VM_NetworkTraffic}.
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test(priority = 11)
    public void testUnBlockAll_VM_NetworkTraffic() throws Exception {
        PowerMockito.mockStatic(AzureCommonUtils.class);
        String resourceIDsWithSecurtyGroups = DummyVirtualMachineImpl.RESOURCE_ID + "#" + DummyVirtualMachineImpl.NIC_ID
                + "=" + DummyVirtualMachineImpl.SECURITY_GROUP_ID + "#" + DummyVirtualMachineImpl.SECURITY_GROUP_ID;
        PowerMockito.doNothing().when(AzureCommonUtils.class);
        AzureCommonUtils.updateSecurityGroup(eq(customAzureClient), any());
        AzureCommonUtils.deleteSecurityGroups(eq(azure), any());
        CommandExecutionResult result =
                AzureVMFaultOperations.unblockAll_VM_NetworkTraffic(customAzureClient, resourceIDsWithSecurtyGroups);
        assertEquals(result.getExitCode(), 0,
                "Test unblock all virtual machine's network failed with exitcode:" + result.getExitCode());
        assertTrue(result.getCommandOutput().contains(DummyVirtualMachineImpl.RESOURCE_ID),
                "Test unblock all virtual machine's network failed with output:" + result.getCommandOutput());

    }

    /**
     * Test method for {@link#AzureVMFaultOperations#unblockAll_VM_NetworkTraffic}.
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test(priority = 12)
    public void testUnBlockAll_VM_NetworkTrafficWithException() throws Exception {
        PowerMockito.mockStatic(AzureCommonUtils.class);
        String resourceIDsWithSecurtyGroups = DummyVirtualMachineImpl.RESOURCE_ID + "#" + DummyVirtualMachineImpl.NIC_ID
                + "=" + DummyVirtualMachineImpl.SECURITY_GROUP_ID + "#" + DummyVirtualMachineImpl.SECURITY_GROUP_ID;
        PowerMockito.doThrow(new MangleException("TestUnBlockAllNetworkFailed", ErrorCode.AZURE_OPERATION_FAILURE))
                .when(AzureCommonUtils.class);
        AzureCommonUtils.updateSecurityGroup(any(), any());
        CommandExecutionResult result =
                AzureVMFaultOperations.unblockAll_VM_NetworkTraffic(customAzureClient, resourceIDsWithSecurtyGroups);
        assertEquals(result.getExitCode(), 1,
                "Test unblock virtual machine's network failed with exitcode:" + result.getExitCode());
        assertTrue(result.getCommandOutput().contains("TestUnBlockAllNetworkFailed"),
                "Test unblock virtual machine's network failed with output:" + result.getCommandOutput());
    }

    /**
     * Test method for {@link#AzureVMFaultOperations#extractPropertyFromResourceId}.
     */
    @SuppressWarnings("unchecked")
    @Test(priority = 13)
    public void testExtractPropertyFromResourceId() {
        when(customAzureClient.getClient()).thenReturn(azure);
        when(azure.virtualMachines()).thenReturn(virtualMachines);
        doNothing().when(virtualMachines).start(any(), any());
        try {
            CommandExecutionResult result =
                    AzureVMFaultOperations.startVirtualmachines(customAzureClient, "/vm/dummyvm");
            fail("Test extractPropertyFromResourceID with invalid resoud id failed");
        } catch (MangleException e) {
            assertEquals(e.getErrorCode(), ErrorCode.AZURE_INVALID_RESOURCE_ID);
            verify(virtualMachines, times(0)).start(any(), any());
        }
    }

    /**
     * Test method for {@link#AzureVMFaultOperations#detachDisksFromVirtualmachines}.
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test(priority = 14)
    public void testDetachDisksFromVirtualmachines() throws Exception {
        List<AzureDisk> disks = new ArrayList<>();
        disks.add(new AzureDisk());
        PowerMockito.mockStatic(AzureCommonUtils.class);
        PowerMockito.when(AzureCommonUtils.getAttachedDisks(any(), any(), anyBoolean())).thenReturn(disks);
        PowerMockito.doNothing().when(AzureCommonUtils.class, "detachDisks", any(), any(), any());
        CommandExecutionResult result = AzureVMFaultOperations.detachDisksFromVirtualmachines(customAzureClient, "true",
                DummyVirtualMachineImpl.RESOURCE_ID);
        assertEquals(result.getExitCode(), 0);
        PowerMockito.doThrow(new MangleException("TestDetachDiskFailed", ErrorCode.AZURE_OPERATION_FAILURE))
                .when(AzureCommonUtils.class);
        AzureCommonUtils.getAttachedDisks(any(), any(), anyBoolean());
        result = AzureVMFaultOperations.detachDisksFromVirtualmachines(customAzureClient, "true",
                DummyVirtualMachineImpl.RESOURCE_ID);
        assertEquals(result.getExitCode(), 1);
    }

    /**
     * Test method for {@link#AzureVMFaultOperations#attachDisksToVirtualmachines}.
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test(priority = 15)
    public void testAttachDisksToVirtualmachines() throws Exception {
        List<AzureDisk> disks = new ArrayList<>();
        disks.add(new AzureDisk());
        String attachedDisk = RestTemplateWrapper.objectToJson(disks);
        PowerMockito.mockStatic(AzureCommonUtils.class);
        PowerMockito.doNothing().when(AzureCommonUtils.class, "attachDisks", any(), any(), any());
        doNothing().when(virtualMachines).restart(any(), any());
        CommandExecutionResult result = AzureVMFaultOperations.attachDisksToVirtualmachines(customAzureClient,
                DummyVirtualMachineImpl.RESOURCE_ID + "#" + attachedDisk);
        assertEquals(result.getExitCode(), 0);
        PowerMockito.doThrow(new MangleException("TestAttachDiskFailed", ErrorCode.AZURE_OPERATION_FAILURE))
                .when(AzureCommonUtils.class);
        AzureCommonUtils.attachDisks(any(), any(), any());
        result = AzureVMFaultOperations.attachDisksToVirtualmachines(customAzureClient,
                DummyVirtualMachineImpl.RESOURCE_ID + "#" + attachedDisk);
        assertEquals(result.getExitCode(), 1);
    }
}
