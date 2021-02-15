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
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.Disk;
import com.microsoft.azure.management.compute.Disks;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkInterface.Update;
import com.microsoft.azure.management.network.NetworkInterface.UpdateStages.WithNetworkSecurityGroup;
import com.microsoft.azure.management.network.NetworkInterfaces;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NetworkSecurityGroup.DefinitionStages.Blank;
import com.microsoft.azure.management.network.NetworkSecurityGroup.DefinitionStages.WithCreate;
import com.microsoft.azure.management.network.NetworkSecurityGroup.DefinitionStages.WithGroup;
import com.microsoft.azure.management.network.NetworkSecurityGroups;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import rx.Observable;
import rx.functions.Action1;

import com.vmware.mangle.utils.clients.azure.AzureCommonUtils;
import com.vmware.mangle.utils.clients.azure.AzureDisk;
import com.vmware.mangle.utils.clients.azure.CustomAzureClient;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;


/**
 * Unit Test Case for AzureCommonUtils.
 *
 * @author bkaranam
 */
@PowerMockIgnore({ "javax.net.ssl.*", "javax.xml.parsers.*", "com.sun.org.apache.xerces.internal.jaxp.*",
        "org.apache.logging.log4j.*", "javax.management.*", "com.nimbusds.oauth2.*", "javax.mail.*",
        "net.minidev.json.*", "com.sun.org.apache.xalan.internal.xsltc.trax.*" })
@PrepareForTest(Azure.class)
public class AzureCommonUtilsTest extends PowerMockTestCase {

    @Mock
    private CustomAzureClient customAzureClient;

    @Mock
    private VirtualMachines virtualMachines;

    @Mock
    private NetworkInterfaces networkInterfaces;

    @Mock
    private NetworkSecurityGroups networkSecurityGroups;

    @Mock
    private NetworkSecurityGroup networkSecurityGroup;

    @Mock
    private NetworkInterface networkInterface;

    @Mock
    private WithNetworkSecurityGroup withNetworkSecurityGroup;
    @Mock
    private Action1<VirtualMachine> action;

    @Mock
    private WithGroup withGroup;

    @Mock
    private WithCreate withCreate;

    @Mock
    private Update update;

    @Mock
    private com.microsoft.azure.management.compute.VirtualMachine.Update vmUpdate;
    @Mock
    private Blank blank;

    private HashMap<String, String> azureTags;

    private Observable<VirtualMachine> virtualMachineImpl = null;

    private Azure azure;

    private List<VirtualMachine> vmList = new ArrayList<>();

    /**
     * @throws Exception
     */
    @BeforeMethod
    public void setUpBeforeMethod() throws Exception {
        MockitoAnnotations.initMocks(this);
        azureTags = new HashMap<>();
        azureTags.put("Env", "MockTest");
        azure = PowerMockito.mock(Azure.class);
        when(customAzureClient.getClient()).thenReturn(azure);
        when(azure.virtualMachines()).thenReturn(virtualMachines);
        when(azure.networkInterfaces()).thenReturn(networkInterfaces);
        when(azure.networkSecurityGroups()).thenReturn(networkSecurityGroups);
        when(networkInterfaces.getById(any())).thenReturn(networkInterface);
        when(networkSecurityGroups.getById(any())).thenReturn(networkSecurityGroup);
        when(networkInterface.networkSecurityGroupId()).thenReturn(DummyVirtualMachineImpl.SECURITY_GROUP_ID);
        when(virtualMachines.listAsync()).thenReturn(virtualMachineImpl);
        when(virtualMachines.getById(any())).thenReturn(new DummyVirtualMachineImpl());
    }

    /**
     * @throws Exception
     */
    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        vmList.add(new DummyVirtualMachineImpl());
        virtualMachineImpl = Observable.from(vmList.toArray(new VirtualMachine[0]));
    }

    /**
     * @throws Exception
     */
    @AfterClass
    public void tearDownAfterClass() throws Exception {
        this.customAzureClient = null;
    }

    /**
     * Test method for {@link#AzureCommonUtils#getAzureVMResourceIds }.
     */
    @Test(priority = 1)
    public void testGetAzureVMResourceIds() {
        try {
            List<String> resourceIDs = AzureCommonUtils.getAzureVMResourceIds(customAzureClient, azureTags, false);
            assertTrue(resourceIDs.size() == 1, "Test Get Azure resourceID failed with valid azureTags");
        } catch (MangleException exception) {
            fail("Test Get Azure resourceID failed with valid azureTags with exception:" + exception.getErrorCode());
        }
    }

    /**
     * Test method for {@link#AzureCommonUtils#getAzureVMResourceIds }.
     */
    @Test(priority = 2)
    public void testGetAzureVMResourceIdsWithRandomInjection() {
        try {
            List<String> resourceIDs = AzureCommonUtils.getAzureVMResourceIds(customAzureClient, azureTags, true);
            assertTrue(resourceIDs.size() == 1, "Test Get Azure resourceID failed with valid azureTags");
        } catch (MangleException exception) {
            fail("Test Get Azure resourceID failed with valid azureTags with exception:" + exception.getErrorCode());
        }
    }

    /**
     * Test method for {@link#AzureCommonUtils#getAzureVMResourceIds }.
     */
    @Test(priority = 3)
    public void testGetAzureVMResourceIdsWithInvalidTags() {
        try {
            azureTags.put("OS", "Ubuntu");
            List<String> resourceIDs = AzureCommonUtils.getAzureVMResourceIds(customAzureClient, azureTags, true);
            assertTrue(resourceIDs.size() == 0, "Test Get Azure resourceID failed with invalid azureTags");
        } catch (MangleException exception) {
            Assert.assertTrue(exception.getErrorCode().equals(ErrorCode.AZURE_NO_RESOURCES_FOUND));
        }
    }

    /**
     * Test method for {@link#AzureCommonUtils#getAzureVMResourceIds }.
     */
    @Test(priority = 4)
    public void testGetAzureVMResourceIdsWithEmptyOrNullTags() {

        try {
            List<String> resourceIDs = AzureCommonUtils.getAzureVMResourceIds(customAzureClient, null, true);
            assertTrue(resourceIDs.size() == 0, "Test Get Azure resourceID failed with null azureTags");
            resourceIDs = AzureCommonUtils.getAzureVMResourceIds(customAzureClient, Collections.emptyMap(), true);
            assertTrue(resourceIDs.size() == 0, "Test Get Azure resourceID failed with empty azureTags");
        } catch (MangleException exception) {
            fail("Test Get Azure resourceID failed with empty or null azureTags with exception:"
                    + exception.getErrorCode());
        }
    }


    /**
     * Test method for {@link#AzureCommonUtils#getSecurityGroups }.
     */
    @Test(priority = 5)
    public void testGetSecurityGroups() {
        try {
            Map<String, String> securityGroups =
                    AzureCommonUtils.getSecurityGroups(customAzureClient, DummyVirtualMachineImpl.RESOURCE_ID);
            assertTrue(securityGroups.size() == 1, "Test Get Azure security groups failed");
        } catch (MangleException exception) {
            fail("Test Get Azure resourceID failed with empty or null azureTags with exception:"
                    + exception.getErrorCode());
        }
    }

    /**
     * Test method for {@link#AzureCommonUtils#createSecurityGroup }.
     */
    @SuppressWarnings("unchecked")
    @Test(priority = 6)
    public void testCreateSecurityGroup() {
        when(networkSecurityGroups.define(any())).thenReturn(blank);
        when(blank.withRegion(anyString())).thenReturn(withGroup);
        when(withGroup.withExistingResourceGroup(anyString())).thenReturn(withCreate);
        when(withCreate.create()).thenReturn(networkSecurityGroup);
        when(networkSecurityGroup.id()).thenReturn(DummyVirtualMachineImpl.SECURITY_GROUP_ID);
        try {
            String securityGroupId = AzureCommonUtils.createSecurityGroup(customAzureClient,
                    DummyVirtualMachineImpl.RESOURCE_ID, "DummySG");
            assertEquals(securityGroupId, DummyVirtualMachineImpl.SECURITY_GROUP_ID,
                    "Test Get Azure security groups failed");
        } catch (MangleException exception) {
            fail("Test create security group is failed with exception:" + exception.getErrorCode());
        }
        when(withCreate.create()).thenThrow(CloudException.class);
        try {
            AzureCommonUtils.createSecurityGroup(customAzureClient, DummyVirtualMachineImpl.RESOURCE_ID, "DummySG");
            fail("Test createSecurityGroup failed: not thrown mangleexception as expected");
        } catch (MangleException exception) {
            assertEquals(exception.getErrorCode(), ErrorCode.AZURE_OPERATION_FAILURE);
        }
    }

    /**
     * Test method for {@link#AzureCommonUtils#updateSecurityGroup }.
     */
    @Test(priority = 7)
    public void testUpdateSecurityGroup() {
        try {
            Map<String, String> securityGroupMap = new HashMap<>();
            securityGroupMap.put(DummyVirtualMachineImpl.NIC_ID, DummyVirtualMachineImpl.SECURITY_GROUP_ID);
            when(networkInterface.update()).thenReturn(update);
            when(update.apply()).thenReturn(networkInterface);
            when(update.withExistingNetworkSecurityGroup(networkSecurityGroup)).thenReturn(update);
            AzureCommonUtils.updateSecurityGroup(customAzureClient, securityGroupMap);
            verify(update, times(1)).apply();
        } catch (MangleException exception) {
            fail("Test updateSecurityGroup failed with exception:" + exception.getErrorCode());
        }
    }

    /**
     * Test method for {@link#AzureCommonUtils#deleteSecurityGroup }.
     */
    @Test(priority = 8)
    public void testDeleteSecurityGroup() {
        try {
            doNothing().when(networkSecurityGroups).deleteById(any());
            AzureCommonUtils.deleteSecurityGroups(azure, DummyVirtualMachineImpl.SECURITY_GROUP_ID);
            verify(networkSecurityGroups, times(1)).deleteById(any());
        } catch (MangleException exception) {
            fail("Test deleteSecurityGroups failed with exception:" + exception.getErrorCode());
        }
        try {
            doThrow(new CloudException("Failed to Delete Security Group", null)).when(networkSecurityGroups)
                    .deleteById(any());
            AzureCommonUtils.deleteSecurityGroups(azure, DummyVirtualMachineImpl.SECURITY_GROUP_ID);
            fail("Test deleteSecurityGroups failed to throw cloud exception as expected");
        } catch (MangleException exception) {
            Assert.assertEquals(exception.getErrorCode(), ErrorCode.AZURE_OPERATION_FAILURE);
        }
    }

    /**
     * Test method for {@link#AzureCommonUtils#getAttachedDisks }.
     */
    @Test(priority = 9)
    public void testGetAttachedDisks() {
        try {
            AzureCommonUtils.getAttachedDisks(azure, DummyVirtualMachineImpl.RESOURCE_ID, false);
            verify(virtualMachines, times(1)).getById(any());
            List<AzureDisk> disks = AzureCommonUtils.getAttachedDisks(azure, DummyVirtualMachineImpl.RESOURCE_ID, true);
            verify(virtualMachines, times(2)).getById(any());
            assertEquals(disks.size(), 1);
        } catch (MangleException exception) {
            fail("Test getattached disks failed with exception:" + exception.getErrorCode());
        }
        try {
            doThrow(new CloudException("Failed to get attached", null)).when(virtualMachines).getById(any());
            AzureCommonUtils.getAttachedDisks(azure, DummyVirtualMachineImpl.RESOURCE_ID, true);
            fail("Test getAttachedDisks failed to throw cloud exception as expected");
        } catch (MangleException exception) {
            Assert.assertEquals(exception.getErrorCode(), ErrorCode.AZURE_NO_RESOURCES_FOUND);
        }
    }

    /**
     * Test method for {@link#AzureCommonUtils#getAttachedDisks }.
     */
    @Test(priority = 10)
    public void testGetAttachedDisksWithEmptyDisks() {
        DummyVirtualMachineImpl vm = new DummyVirtualMachineImpl();
        vm.dataDisks().clear();
        System.out.println(vm.dataDisks().isEmpty());
        when(virtualMachines.getById(any())).thenReturn(vm);
        try {
            AzureCommonUtils.getAttachedDisks(azure, DummyVirtualMachineImpl.RESOURCE_ID, true);
            fail("Test getAttachedDisks failed to throw cloud exception as expected");
        } catch (MangleException exception) {
            Assert.assertEquals(exception.getErrorCode(), ErrorCode.AZURE_NO_DATA_DISK_ATTACHED);
        }
    }

    /**
     * Test method for {@link#AzureCommonUtils#detachDisks }.
     *
     * @throws MangleException
     */
    @Test(priority = 11)
    public void testDetachDisks() throws MangleException {
        VirtualMachine virtualMachine = Mockito.mock(VirtualMachine.class);
        when(virtualMachines.getById(any())).thenReturn(virtualMachine);
        when(virtualMachine.update()).thenReturn(vmUpdate);
        when(vmUpdate.withoutDataDisk(anyInt())).thenReturn(vmUpdate);
        when(vmUpdate.apply()).thenReturn(virtualMachine);
        List<AzureDisk> lunIds = new ArrayList<>();
        lunIds.add(new AzureDisk());
        AzureCommonUtils.detachDisks(azure, DummyVirtualMachineImpl.RESOURCE_ID, lunIds);
        verify(vmUpdate, times(1)).apply();
    }

    /**
     * Test method for {@link#AzureCommonUtils#attachDisks }.
     *
     * @throws MangleException
     */
    @Test(priority = 12)
    public void testAttachDisks() {
        VirtualMachine virtualMachine = Mockito.mock(VirtualMachine.class);
        Disks disks = Mockito.mock(Disks.class);
        Disk disk = Mockito.mock(Disk.class);
        when(azure.disks()).thenReturn(disks);
        when(disks.getById(any())).thenReturn(disk);
        when(virtualMachines.getById(any())).thenReturn(virtualMachine);
        when(virtualMachine.update()).thenReturn(vmUpdate);
        when(vmUpdate.withExistingDataDisk(any(), anyInt(), any())).thenReturn(vmUpdate);
        when(vmUpdate.apply()).thenReturn(virtualMachine);
        try {
            AzureCommonUtils.attachDisks(azure, DummyVirtualMachineImpl.RESOURCE_ID, new AzureDisk());
            verify(vmUpdate, times(1)).apply();
        } catch (MangleException exception) {
            fail("test attach disks failed with exception: " + exception.getMessage());
        }
        when(vmUpdate.apply()).thenThrow(new CloudException("Failed to Update vm", null));

        try {
            AzureCommonUtils.attachDisks(azure, DummyVirtualMachineImpl.RESOURCE_ID, new AzureDisk());
            fail("test attach disks failed to throw cloud exception");
        } catch (MangleException exception) {
            assertEquals(exception.getErrorCode(), ErrorCode.AZURE_NO_RESOURCES_FOUND);
            verify(vmUpdate, times(2)).apply();
        }
    }
}
