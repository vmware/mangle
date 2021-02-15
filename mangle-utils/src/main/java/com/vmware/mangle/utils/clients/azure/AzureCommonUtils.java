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

package com.vmware.mangle.utils.clients.azure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.CachingTypes;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.network.NetworkInterfaces;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NetworkSecurityGroups;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;
import rx.Observable;

import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.constants.AzureConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author bkaranam
 */
@Log4j2
public class AzureCommonUtils {
    private AzureCommonUtils() {

    }

    public static List<String> getAzureVMResourceIds(CustomAzureClient client, Map<String, String> azureTags,
            boolean random) throws MangleException {
        if (CollectionUtils.isEmpty(azureTags)) {
            log.debug("azureTags should not be null or empty map");
            return Collections.emptyList();
        }
        List<String> resourceIds = new ArrayList<>();
        Azure azureClient = client.getClient();
        Observable<VirtualMachine> vms = azureClient.virtualMachines().listAsync();
        vms.subscribe(vm -> {
            if ((vm.tags().entrySet().containsAll(azureTags.entrySet()))) {
                resourceIds.add(vm.id());
            }
        });

        if (CollectionUtils.isEmpty(resourceIds)) {
            throw new MangleException(ErrorCode.AZURE_NO_RESOURCES_FOUND,
                    CommonUtils.maptoDelimitedKeyValuePairString(azureTags, ","));
        }
        if (random) {
            Random rand = new Random();
            return Arrays.asList(resourceIds.get(rand.nextInt(resourceIds.size())));
        } else {
            return resourceIds;
        }
    }

    public static Map<String, String> getSecurityGroups(CustomAzureClient client, String resourceId)
            throws MangleException {
        Map<String, String> nic_sg_map = new HashMap<String, String>();
        Azure azure = client.getClient();
        azure.virtualMachines().getById(resourceId).networkInterfaceIds().stream()
                .forEach(id -> nic_sg_map.put(id, azure.networkInterfaces().getById(id).networkSecurityGroupId()));
        return nic_sg_map;
    }

    public static String getRandomSecurityGroupName(String vmName) {
        return "Mangle-" + vmName + "-" + UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }

    public static String createSecurityGroup(CustomAzureClient client, String resourceID, String sgName)
            throws MangleException {
        Azure azure = client.getClient();
        VirtualMachine virtualMachine = azure.virtualMachines().getById(resourceID);
        NetworkSecurityGroup securityGroup = null;
        try {
            securityGroup = azure.networkSecurityGroups().define(sgName).withRegion(virtualMachine.regionName())
                    .withExistingResourceGroup(virtualMachine.resourceGroupName()).create();
        } catch (CloudException exception) {
            String errorMessage = "Creating a dummy securitygroup failed with exception:";
            throw new MangleException(errorMessage + exception.getMessage(), ErrorCode.AZURE_OPERATION_FAILURE,
                    errorMessage + exception.getMessage());
        }
        return securityGroup.id();
    }

    public static String extractPropertyFromResourceId(String resourceId, String property) throws MangleException {
        Pattern pattern = Pattern.compile(property + "/(.*?)(/|$)");
        Matcher matcher = pattern.matcher(resourceId);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new MangleException(ErrorCode.AZURE_INVALID_RESOURCE_ID,
                "Couldn't extract property:" + property + " from resource id:" + resourceId);
    }

    public static void updateSecurityGroup(CustomAzureClient client, String securityGroupID,
            String... networkInterfaceIds) throws MangleException {
        Azure azure = client.getClient();
        NetworkInterfaces networkInterfaces = azure.networkInterfaces();
        NetworkSecurityGroups securityGroups = azure.networkSecurityGroups();
        List<FailedResource> failedResources = new ArrayList<>();
        Stream.of(networkInterfaceIds).forEach(nicId -> {
            try {
                networkInterfaces.getById(nicId).update()
                        .withExistingNetworkSecurityGroup(securityGroups.getById(securityGroupID)).apply();
            } catch (CloudException azureException) {
                failedResources.add(new FailedResource(AzureResourceType.NETWORK_INTERFACE,
                        AzureResourceOperation.UPDATE, nicId, azureException.getMessage()));
            }
        });
        if (!CollectionUtils.isEmpty(failedResources)) {
            throw new MangleException(failedResources.toString(), ErrorCode.AZURE_OPERATION_FAILURE,
                    failedResources.toString());
        }
    }

    public static void updateSecurityGroup(CustomAzureClient client, Map<String, String> securityGroupsMap)
            throws MangleException {
        List<String> failureMessages = new ArrayList<>();
        securityGroupsMap.entrySet().stream().forEach(entry -> {
            try {
                updateSecurityGroup(client, entry.getValue(), entry.getKey());
            } catch (MangleException e) {
                failureMessages.add(e.getMessage());
            }
        });
        if (!CollectionUtils.isEmpty(failureMessages)) {
            throw new MangleException(failureMessages.toString(), ErrorCode.AZURE_OPERATION_FAILURE,
                    failureMessages.toString());
        }
    }

    public static void deleteSecurityGroups(Azure azure, String... securityGroupIds) throws MangleException {
        List<FailedResource> failedResources = new ArrayList<>();
        Stream.of(securityGroupIds).forEach(groupId -> {
            try {
                azure.networkSecurityGroups().deleteById(groupId);
            } catch (CloudException azureException) {
                failedResources.add(new FailedResource(AzureResourceType.NETWORK_SECURITY_GROUP,
                        AzureResourceOperation.DELETE, groupId, azureException.getMessage()));
            }
        });
        if (!CollectionUtils.isEmpty(failedResources)) {
            throw new MangleException(failedResources.toString(), ErrorCode.AZURE_OPERATION_FAILURE,
                    failedResources.toString());
        }
    }

    public static List<AzureDisk> getAttachedDisks(Azure azure, String resourceId, boolean random)
            throws MangleException {
        List<AzureDisk> attachedDisks = new ArrayList<>();
        try {
            azure.virtualMachines().getById(resourceId).dataDisks().entrySet().stream().forEach(entry -> {
                AzureDisk disk = new AzureDisk();
                disk.setLun(entry.getKey());
                disk.setId(entry.getValue().id());
                disk.setCachingType(entry.getValue().cachingType().name());
                attachedDisks.add(disk);
            });
        } catch (CloudException azureException) {
            throw new MangleException(ErrorCode.AZURE_NO_RESOURCES_FOUND);
        }
        if (CollectionUtils.isEmpty(attachedDisks)) {
            String vmName = extractPropertyFromResourceId(resourceId, AzureConstants.VIRTUALMACHINES);
            throw new MangleException("no data disk is attached to the virtual machine:" + vmName,
                    ErrorCode.AZURE_NO_DATA_DISK_ATTACHED, vmName);
        }
        if (random && !CollectionUtils.isEmpty(attachedDisks)) {
            Collections.shuffle(attachedDisks);
            return Arrays.asList(attachedDisks.get(0));
        }
        return attachedDisks;
    }

    public static void detachDisks(Azure azure, String resourceId, List<AzureDisk> lunIds) throws MangleException {
        lunIds.stream().forEach(
                disk -> azure.virtualMachines().getById(resourceId).update().withoutDataDisk(disk.getLun()).apply());
    }

    public static void attachDisks(Azure azure, String resourceId, AzureDisk... disksToAttach) throws MangleException {
        try {
            Stream.of(disksToAttach)
                    .forEach(disk -> azure.virtualMachines().getById(resourceId).update()
                            .withExistingDataDisk(azure.disks().getById(disk.getId()), disk.getLun(),
                                    CachingTypes.fromString(disk.getCachingType()))
                            .apply());
        } catch (CloudException azureException) {
            throw new MangleException(ErrorCode.AZURE_NO_RESOURCES_FOUND);
        }
    }
}

