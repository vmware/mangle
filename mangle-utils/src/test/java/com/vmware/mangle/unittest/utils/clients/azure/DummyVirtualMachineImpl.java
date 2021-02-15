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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.compute.BillingProfile;
import com.microsoft.azure.management.compute.CachingTypes;
import com.microsoft.azure.management.compute.DiagnosticsProfile;
import com.microsoft.azure.management.compute.OSProfile;
import com.microsoft.azure.management.compute.OperatingSystemTypes;
import com.microsoft.azure.management.compute.Plan;
import com.microsoft.azure.management.compute.PowerState;
import com.microsoft.azure.management.compute.ProximityPlacementGroup;
import com.microsoft.azure.management.compute.ResourceIdentityType;
import com.microsoft.azure.management.compute.RunCommandInput;
import com.microsoft.azure.management.compute.RunCommandInputParameter;
import com.microsoft.azure.management.compute.RunCommandResult;
import com.microsoft.azure.management.compute.StorageAccountTypes;
import com.microsoft.azure.management.compute.StorageProfile;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineDataDisk;
import com.microsoft.azure.management.compute.VirtualMachineEncryption;
import com.microsoft.azure.management.compute.VirtualMachineEvictionPolicyTypes;
import com.microsoft.azure.management.compute.VirtualMachineExtension;
import com.microsoft.azure.management.compute.VirtualMachineInstanceView;
import com.microsoft.azure.management.compute.VirtualMachinePriorityTypes;
import com.microsoft.azure.management.compute.VirtualMachineSize;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.compute.VirtualMachineUnmanagedDataDisk;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.compute.implementation.VirtualMachineInner;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.AvailabilityZoneId;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;

/**
 * Dummy virtual machine implementation for testing
 *
 * @author bkaranam
 */
public class DummyVirtualMachineImpl implements VirtualMachine {

    public static final String RESOURCE_ID =
            "/subscriptions/39a283-fa9d-4904-99f4-0fc746c2fhg/resourceGroups/RESOURCE_GROUP_1/providers/Microsoft.Compute/virtualMachines/DummyVM";
    public static final String NIC_ID =
            "/subscriptions/39a283-fa9d-4904-99f4-0fc746c2fhg/resourceGroups/RESOURCE_GROUP_1/Microsoft.Network/networkInterfaces/DummyNIC";
    public static final String SECURITY_GROUP_ID =
            "/subscriptions/39a283-fa9d-4904-99f4-0fc746c2fhg/resourceGroups/RESOURCE_GROUP_1/Microsoft.Network/networkSecurityGroups/DummySecurityGroup";
    private Map<Integer, VirtualMachineDataDisk> dataDiskMap = new HashMap<>();

    public DummyVirtualMachineImpl() {
        dataDiskMap.put(0, new DummyVirtualMachineDiskImpl());
    }

    @Override
    public String type() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public String regionName() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public Region region() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public Map<String, String> tags() {
        Map<String, String> tags = new HashMap<String, String>();
        tags.put("Env", "MockTest");
        return tags;
    }

    @Override
    public String key() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public String id() {
        return RESOURCE_ID;
    }

    @Override
    public String name() {
        // Dummy Implementation for Testing
        return "DummyVM";
    }

    @Override
    public String resourceGroupName() {
        return "DummyRG";
    }

    @Override
    public ComputeManager manager() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public VirtualMachineInner inner() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public VirtualMachine refresh() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public Observable<VirtualMachine> refreshAsync() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public Update update() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public NetworkInterface getPrimaryNetworkInterface() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public List<String> networkInterfaceIds() {
        List<String> interfaceIds = new ArrayList<>();
        interfaceIds.add(NIC_ID);
        return interfaceIds;
    }

    @Override
    public String primaryNetworkInterfaceId() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public void deallocate() {
        // Dummy Implementation for Testing

    }

    @Override
    public Completable deallocateAsync() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public ServiceFuture<Void> deallocateAsync(ServiceCallback<Void> callback) {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public void generalize() {
        // Dummy Implementation for Testing

    }

    @Override
    public Completable generalizeAsync() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public ServiceFuture<Void> generalizeAsync(ServiceCallback<Void> callback) {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public void powerOff() {
        // Dummy Implementation for Testing

    }

    @Override
    public Completable powerOffAsync() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public ServiceFuture<Void> powerOffAsync(ServiceCallback<Void> callback) {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public void restart() {
        // Dummy Implementation for Testing

    }

    @Override
    public Completable restartAsync() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public ServiceFuture<Void> restartAsync(ServiceCallback<Void> callback) {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public void start() {
        // Dummy Implementation for Testing

    }

    @Override
    public Completable startAsync() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public ServiceFuture<Void> startAsync(ServiceCallback<Void> callback) {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public void redeploy() {
        // Dummy Implementation for Testing

    }

    @Override
    public Completable redeployAsync() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public ServiceFuture<Void> redeployAsync(ServiceCallback<Void> callback) {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public VirtualMachineEncryption diskEncryption() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public void convertToManaged() {
        // Dummy Implementation for Testing

    }

    @Override
    public Completable convertToManagedAsync() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public ServiceFuture<Void> convertToManagedAsync(ServiceCallback<Void> callback) {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public PagedList<VirtualMachineSize> availableSizes() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public String capture(String containerName, String vhdPrefix, boolean overwriteVhd) {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public Observable<String> captureAsync(String containerName, String vhdPrefix, boolean overwriteVhd) {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public ServiceFuture<String> captureAsync(String containerName, String vhdPrefix, boolean overwriteVhd,
            ServiceCallback<String> callback) {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public VirtualMachineInstanceView refreshInstanceView() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public Observable<VirtualMachineInstanceView> refreshInstanceViewAsync() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public RunCommandResult runPowerShellScript(String groupName, String name, List<String> scriptLines,
            List<RunCommandInputParameter> scriptParameters) {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public Observable<RunCommandResult> runPowerShellScriptAsync(List<String> scriptLines,
            List<RunCommandInputParameter> scriptParameters) {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public RunCommandResult runShellScript(List<String> scriptLines, List<RunCommandInputParameter> scriptParameters) {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public Observable<RunCommandResult> runShellScriptAsync(List<String> scriptLines,
            List<RunCommandInputParameter> scriptParameters) {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public RunCommandResult runCommand(RunCommandInput inputCommand) {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public Observable<RunCommandResult> runCommandAsync(RunCommandInput inputCommand) {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public boolean isManagedDiskEnabled() {
        // Dummy Implementation for Testing
        return false;
    }

    @Override
    public String computerName() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public VirtualMachineSizeTypes size() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public OperatingSystemTypes osType() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public String osUnmanagedDiskVhdUri() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public CachingTypes osDiskCachingType() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public int osDiskSize() {
        // Dummy Implementation for Testing
        return 0;
    }

    @Override
    public StorageAccountTypes osDiskStorageAccountType() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public String osDiskId() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public Map<Integer, VirtualMachineUnmanagedDataDisk> unmanagedDataDisks() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public Map<Integer, VirtualMachineDataDisk> dataDisks() {
        return dataDiskMap;
    }

    @Override
    public PublicIPAddress getPrimaryPublicIPAddress() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public String getPrimaryPublicIPAddressId() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public String availabilitySetId() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public String provisioningState() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public String licenseType() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public ProximityPlacementGroup proximityPlacementGroup() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public Observable<VirtualMachineExtension> listExtensionsAsync() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public Map<String, VirtualMachineExtension> listExtensions() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public Plan plan() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public StorageProfile storageProfile() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public OSProfile osProfile() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public DiagnosticsProfile diagnosticsProfile() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public String vmId() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public PowerState powerState() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public VirtualMachineInstanceView instanceView() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public Set<AvailabilityZoneId> availabilityZones() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public boolean isBootDiagnosticsEnabled() {
        // Dummy Implementation for Testing
        return false;
    }

    @Override
    public String bootDiagnosticsStorageUri() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public boolean isManagedServiceIdentityEnabled() {
        // Dummy Implementation for Testing
        return false;
    }

    @Override
    public String systemAssignedManagedServiceIdentityTenantId() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public String systemAssignedManagedServiceIdentityPrincipalId() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public ResourceIdentityType managedServiceIdentityType() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public Set<String> userAssignedManagedServiceIdentityIds() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public VirtualMachinePriorityTypes priority() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public VirtualMachineEvictionPolicyTypes evictionPolicy() {
        // Dummy Implementation for Testing
        return null;
    }

    @Override
    public BillingProfile billingProfile() {
        // Dummy Implementation for Testing
        return null;
    }

}
