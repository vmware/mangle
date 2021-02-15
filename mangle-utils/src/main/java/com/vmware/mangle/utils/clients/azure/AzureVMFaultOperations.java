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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.Azure;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.constants.AzureConstants;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author bkaranam
 *
 *         Provides methods for orchestrating different fault operations on azure virtual machines
 */
@Log4j2
public class AzureVMFaultOperations {

    private AzureVMFaultOperations() {
    }


    /**
     * Delete azure virtual machines
     *
     * @param resourceIDs
     * @param CustomAzureClient
     *            client
     * @return CommandExecutionResult object with exit code set to 0 if deleting virtual machines is
     *         successful, else sets it to 1
     */
    public static CommandExecutionResult deleteVirtualmachines(CustomAzureClient client, String resourceIds)
            throws MangleException {
        log.debug("Deleting virtual machines {}", resourceIds);
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        Azure azureClient = client.getClient();
        List<String> deletedVMs = new ArrayList<>();

        for (String resourceID : resourceIds.split(",")) {
            String vmName = AzureCommonUtils.extractPropertyFromResourceId(resourceID, AzureConstants.VIRTUALMACHINES);
            try {
                azureClient.virtualMachines().getById(resourceID).name();
                azureClient.virtualMachines().deleteById(resourceID);
                deletedVMs.add(vmName);
            } catch (CloudException cloudException) {
                commandExecutionResult.setCommandOutput(cloudException.getMessage());
                commandExecutionResult.setExitCode(1);
                return commandExecutionResult;
            } catch (NullPointerException npe) {
                commandExecutionResult.setCommandOutput("Virtualmachine " + vmName + " not found");
                commandExecutionResult.setExitCode(1);
                return commandExecutionResult;
            }
        }
        commandExecutionResult.setCommandOutput(deletedVMs.toString());
        commandExecutionResult.setExitCode(0);
        return commandExecutionResult;
    }

    /**
     * start azure virtual machines
     *
     * @param resourceIDs
     * @param CustomAzureClient
     *            client
     * @return CommandExecutionResult object with exit code set to 0 if starting virtual machines is
     *         successful, else sets it to 1
     */
    public static CommandExecutionResult startVirtualmachines(CustomAzureClient client, String resourceIds)
            throws MangleException {
        log.debug("Starting virtual machines {}", resourceIds);
        Azure azureClient = client.getClient();
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        List<String> startedVMs = new ArrayList<>();
        for (String resourceID : resourceIds.split(",")) {
            try {
                String vmName =
                        AzureCommonUtils.extractPropertyFromResourceId(resourceID, AzureConstants.VIRTUALMACHINES);
                azureClient.virtualMachines().start(
                        AzureCommonUtils.extractPropertyFromResourceId(resourceID, AzureConstants.RESOURCEGROUPS),
                        vmName);
                startedVMs.add(vmName);
            } catch (CloudException cloudException) {
                commandExecutionResult.setCommandOutput(cloudException.getMessage());
                commandExecutionResult.setExitCode(1);
                return commandExecutionResult;
            }
        }
        commandExecutionResult.setCommandOutput(startedVMs.toString());
        commandExecutionResult.setExitCode(0);
        return commandExecutionResult;
    }

    /**
     * stop azure virtual machines
     *
     * @param resourceIDs
     * @param CustomAzureClient
     *            client
     * @return CommandExecutionResult object with exit code set to 0 if stopping virtual machines is
     *         successful, else sets it to 1
     */
    public static CommandExecutionResult stopVirtualmachines(CustomAzureClient client, String resourceIds)
            throws MangleException {
        log.debug("Stopping virtual machines {}", resourceIds);
        Azure azureClient = client.getClient();
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        List<String> stoppedVMs = new ArrayList<>();
        for (String resourceID : resourceIds.split(",")) {
            try {
                String vmName =
                        AzureCommonUtils.extractPropertyFromResourceId(resourceID, AzureConstants.VIRTUALMACHINES);
                azureClient.virtualMachines().powerOff(
                        AzureCommonUtils.extractPropertyFromResourceId(resourceID, AzureConstants.RESOURCEGROUPS),
                        vmName);
                stoppedVMs.add(vmName);
            } catch (CloudException cloudException) {
                commandExecutionResult.setCommandOutput(cloudException.getMessage());
                commandExecutionResult.setExitCode(1);
                return commandExecutionResult;
            }
        }
        commandExecutionResult.setCommandOutput(stoppedVMs.toString());
        commandExecutionResult.setExitCode(0);
        return commandExecutionResult;
    }

    /**
     * restart azure virtual machines
     *
     * @param resourceIDs
     * @param CustomAzureClient
     *            client
     * @return CommandExecutionResult object with exit code set to 0 if restarting virtual machines
     *         is successful, else sets it to 1
     */
    public static CommandExecutionResult restartVirtualmachines(CustomAzureClient client, String resourceIds)
            throws MangleException {
        log.debug("Restarting virtual machines {}", resourceIds);
        Azure azureClient = client.getClient();
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        List<String> restartedVMs = new ArrayList<>();
        for (String resourceID : resourceIds.split(",")) {
            try {
                String vmName =
                        AzureCommonUtils.extractPropertyFromResourceId(resourceID, AzureConstants.VIRTUALMACHINES);
                azureClient.virtualMachines().restart(
                        AzureCommonUtils.extractPropertyFromResourceId(resourceID, AzureConstants.RESOURCEGROUPS),
                        vmName);
                restartedVMs.add(vmName);
            } catch (CloudException cloudException) {
                commandExecutionResult.setCommandOutput(cloudException.getMessage());
                commandExecutionResult.setExitCode(1);
                return commandExecutionResult;
            }
        }
        commandExecutionResult.setCommandOutput(restartedVMs.toString());
        commandExecutionResult.setExitCode(0);
        return commandExecutionResult;
    }

    /**
     * Block all the network traffic to specified azure vritual machines by assigning dummy security
     * group
     *
     * @param resourceIDs
     * @param CustomAzureClient
     *            client
     * @return CommandExecutionResult object with exit code set to 0 if blocking all the network is
     *         successful, else sets it to 1
     */
    public static CommandExecutionResult blockAll_VM_NetworkTraffic(CustomAzureClient client, String resourceIds) {
        log.debug("Blocking all network traffic for vritual machines {}", resourceIds);
        List<String> affectedVMsWithSGs = new ArrayList<>();

        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        for (String resourceID : resourceIds.split(",")) {
            String dummySecurityGroupID = null;
            try {
                Map<String, String> existingSecurityGroupIDs = AzureCommonUtils.getSecurityGroups(client, resourceID);
                String vmName =
                        AzureCommonUtils.extractPropertyFromResourceId(resourceID, AzureConstants.VIRTUALMACHINES);
                dummySecurityGroupID = AzureCommonUtils.createSecurityGroup(client, resourceID,
                        AzureCommonUtils.getRandomSecurityGroupName(vmName));
                AzureCommonUtils.updateSecurityGroup(client, dummySecurityGroupID,
                        existingSecurityGroupIDs.keySet().toArray(new String[0]));
                affectedVMsWithSGs.add(
                        (resourceID + "#" + CommonUtils.maptoDelimitedKeyValuePairString(existingSecurityGroupIDs, ",")
                                + "#" + dummySecurityGroupID));
            } catch (MangleException e) {
                commandExecutionResult.setCommandOutput(e.getMessage());
                commandExecutionResult.setExitCode(1);
            }
        }

        if (!CollectionUtils.isEmpty(affectedVMsWithSGs)) {
            commandExecutionResult.setCommandOutput(
                    "Successfully blocked all the network to VirtualMachines->" + String.join("&", affectedVMsWithSGs));
            commandExecutionResult.setExitCode(0);
        }
        return commandExecutionResult;
    }

    /**
     * unblock all the network traffic to specified azure virtual machines by reassigning existing
     * security groups
     *
     * @param resourceIDsWithSecurtyGroups
     * @param CustomAzureClient
     *            client
     * @return CommandExecutionResult object with exit code set to 0 if unblocking all the networks
     *         is successful, else sets it to 1
     */
    public static CommandExecutionResult unblockAll_VM_NetworkTraffic(CustomAzureClient client,
            String resourceIDsWithSecurtyGroups) {
        Azure azure = client.getClient();
        log.debug("Unblocking all network traffic for virtual machines with security groups {}",
                Arrays.asList(resourceIDsWithSecurtyGroups));
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        Set<String> securityGroupsToDelete = new HashSet<>();
        for (String resourceIDWithSecurityGroup : resourceIDsWithSecurtyGroups.split("&")) {
            String[] resourceAndSecurityGroups = resourceIDWithSecurityGroup.split("#");
            String resourceID = resourceAndSecurityGroups[0];
            Map<String, String> securityGroupsMap = CommonUtils.stringKeyValuePairToMap(resourceAndSecurityGroups[1]);
            securityGroupsToDelete.add(resourceAndSecurityGroups[2]);
            try {
                AzureCommonUtils.updateSecurityGroup(client, securityGroupsMap);
                commandExecutionResult.setCommandOutput("Successfully unblocked virtual machine " + resourceID + "\n");
            } catch (MangleException e) {
                if (StringUtils.hasText(commandExecutionResult.getCommandOutput())) {
                    commandExecutionResult
                            .setCommandOutput(commandExecutionResult.getCommandOutput() + "\n" + e.getMessage());
                } else {
                    commandExecutionResult.setCommandOutput(e.getMessage());
                }
                commandExecutionResult.setExitCode(1);
            }
        }
        try {
            AzureCommonUtils.deleteSecurityGroups(azure, securityGroupsToDelete.toArray(new String[0]));
        } catch (MangleException e) {
            commandExecutionResult.setCommandOutput(commandExecutionResult.getCommandOutput() + "\n" + e.getMessage());
            commandExecutionResult.setExitCode(1);
        }
        return commandExecutionResult;
    }

    /**
     * Detach disks from specified azure virtual machines
     *
     * @param resourceIds
     * @param random
     * @param CustomAzureClient
     *            client
     * @return CommandExecutionResult object with exit code set to 0 if detaching disks from virtual
     *         machines is successful, else sets it to 1
     */
    public static CommandExecutionResult detachDisksFromVirtualmachines(CustomAzureClient client,
            String selectRandomDisks, String resourceIds) {
        log.debug("Detaching disks from virtual machines {}", resourceIds);
        List<String> affectedVMsWithDisks = new ArrayList<>();
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();

        for (String resourceId : resourceIds.split(",")) {
            try {
                List<AzureDisk> attachedDisks = AzureCommonUtils.getAttachedDisks(client.getClient(), resourceId,
                        Boolean.parseBoolean(selectRandomDisks));
                AzureCommonUtils.detachDisks(client.getClient(), resourceId, attachedDisks);
                affectedVMsWithDisks.add(resourceId + "#" + RestTemplateWrapper.objectToJson(attachedDisks));
            } catch (MangleException e) {
                commandExecutionResult.setCommandOutput(e.getMessage());
                commandExecutionResult.setExitCode(1);
            }
        }

        if (!CollectionUtils.isEmpty(affectedVMsWithDisks)) {
            commandExecutionResult.setCommandOutput("Successfully detached all the disks from VirtualMachines->"
                    + String.join("&", affectedVMsWithDisks));
            commandExecutionResult.setExitCode(0);
        }
        return commandExecutionResult;
    }

    /**
     * Attach disks to specified azure virtual machines
     *
     * @param resourceIdsWithDisks
     * @param CustomAzureClient
     *            client
     * @return CommandExecutionResult object with exit code set to 0 if attaching disks to all the
     *         vms is successful, else sets it to 1
     */
    public static CommandExecutionResult attachDisksToVirtualmachines(CustomAzureClient client,
            String resourceIdsWithDisks) {
        log.debug("Attaching disks to virtual machines {}", resourceIdsWithDisks);
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        for (String resourceIDsWithDisks : resourceIdsWithDisks.split("&")) {
            String[] resourceIDWithDisks = resourceIDsWithDisks.split("#");
            String resourceId = resourceIDWithDisks[0];
            AzureDisk[] disksToAttach = RestTemplateWrapper.jsonToObject(resourceIDWithDisks[1], AzureDisk[].class);
            try {
                restartVirtualmachines(client, resourceId);
                AzureCommonUtils.attachDisks(client.getClient(), resourceId, disksToAttach);
                commandExecutionResult.setCommandOutput("Successfully attached disks " + resourceIDWithDisks[1]
                        + " to virtual machine " + resourceId + "\n");
            } catch (MangleException e) {
                if (StringUtils.hasText(commandExecutionResult.getCommandOutput())) {
                    commandExecutionResult
                            .setCommandOutput(commandExecutionResult.getCommandOutput() + "\n" + e.getMessage());
                } else {
                    commandExecutionResult.setCommandOutput(e.getMessage());
                }
                commandExecutionResult.setExitCode(1);
            }
        }
        return commandExecutionResult;
    }
}
