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

package com.vmware.mangle.faults.plugin.helpers.azure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandOutputProcessingInfo;
import com.vmware.mangle.model.azure.AzureNetworkFaults;
import com.vmware.mangle.model.azure.AzureStorageFaults;
import com.vmware.mangle.model.azure.AzureVMStateFaults;
import com.vmware.mangle.model.azure.faults.spec.AzureVMFaultSpec;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.skeletons.ICommandExecutionFaultHelper;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.azure.AzureCommandExecutor;
import com.vmware.mangle.utils.clients.azure.AzureCommonUtils;
import com.vmware.mangle.utils.clients.azure.CustomAzureClient;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author bkaranam
 *
 *         Azure Virtual machine fault helper, that constructs remediation and injections command
 */
@Component
public class AzureVMFaultHelper implements ICommandExecutionFaultHelper {

    private EndpointClientFactory endpointClientFactory;
    private static final String EXTRACT_BLOCKED_VMS_PROPERTY = "BlockedVirtualMachines";
    private static final String EXTRACT_DETACHED_DISKS_PROPERTY = "VirtualMachinesWithDetachedDisks";

    @Autowired
    public AzureVMFaultHelper(EndpointClientFactory endpointClientFactory) {
        this.endpointClientFactory = endpointClientFactory;

    }

    @Override
    public void checkTaskSpecificPrerequisites() {
        //  No prerequitsites for the azure related faults
    }

    @Override
    public ICommandExecutor getExecutor(CommandExecutionFaultSpec azureVMFaultSpec) throws MangleException {
        return new AzureCommandExecutor((CustomAzureClient) endpointClientFactory
                .getEndPointClient(azureVMFaultSpec.getCredentials(), azureVMFaultSpec.getEndpoint()));
    }

    @Override
    public List<SupportScriptInfo> getAgentFaultInjectionScripts() {
        return new ArrayList<>();
    }

    @Override
    public List<CommandInfo> getInjectionCommandInfoList(ICommandExecutor executor,
            CommandExecutionFaultSpec azureVMFaultSpec) {
        List<CommandOutputProcessingInfo> commandOutputProcessingInfoList = null;
        if (AzureNetworkFaults.BLOCK_ALL_VM_NETWORK_TRAFFIC.name().equalsIgnoreCase(azureVMFaultSpec.getFaultName())) {
            commandOutputProcessingInfoList = createOutputProcessingInfo(EXTRACT_BLOCKED_VMS_PROPERTY);
        }
        if (AzureStorageFaults.DETACH_DISKS.name().equalsIgnoreCase(azureVMFaultSpec.getFaultName())) {
            commandOutputProcessingInfoList = createOutputProcessingInfo(EXTRACT_DETACHED_DISKS_PROPERTY);
        }

        CommandInfo commandInfo = CommandInfo.builder(buildInjectionCommand((AzureVMFaultSpec) azureVMFaultSpec))
                .ignoreExitValueCheck(false).commandOutputProcessingInfoList(commandOutputProcessingInfoList).build();
        List<CommandInfo> commandInfoList = new ArrayList<>();
        commandInfoList.add(commandInfo);
        return commandInfoList;
    }

    @Override
    public List<CommandInfo> getRemediationCommandInfoList(ICommandExecutor executor,
            CommandExecutionFaultSpec azureVMFaultSpec) {
        List<CommandInfo> commandInfoList = new ArrayList<>();
        String remediationCommand = buildRemediationCommand((AzureVMFaultSpec) azureVMFaultSpec);
        if (remediationCommand != null) {
            CommandInfo commandInfo = CommandInfo.builder(remediationCommand).ignoreExitValueCheck(false).build();
            commandInfoList.add(commandInfo);
        }

        return commandInfoList;
    }

    private String buildInjectionCommand(AzureVMFaultSpec azureVMFaultSpec) {
        String injectionCommand = azureVMFaultSpec.getFaultName() + ": ";
        if (!CollectionUtils.isEmpty(azureVMFaultSpec.getArgs())) {
            injectionCommand += CommonUtils.convertMaptoDelimitedString(azureVMFaultSpec.getArgs(), " ");
        }
        return injectionCommand + " --resourceIds " + String.join(",", azureVMFaultSpec.getResourceIds());
    }

    public String buildRemediationCommand(AzureVMFaultSpec azureVMFaultSpec) {
        if (AzureVMStateFaults.STOP_VMS.name().equalsIgnoreCase(azureVMFaultSpec.getFaultName())) {
            return (AzureVMStateFaults.STOP_VMS.getRemediation().name() + ": --resourceIds "
                    + String.join(",", azureVMFaultSpec.getResourceIds()));
        }
        if (AzureNetworkFaults.BLOCK_ALL_VM_NETWORK_TRAFFIC.name().equalsIgnoreCase(azureVMFaultSpec.getFaultName())) {
            return (AzureNetworkFaults.BLOCK_ALL_VM_NETWORK_TRAFFIC.getRemediation().name()
                    + ": --resourceIdsWithSecurityGroups  $FI_ADD_INFO_" + EXTRACT_BLOCKED_VMS_PROPERTY);
        }
        if (AzureStorageFaults.DETACH_DISKS.name().equalsIgnoreCase(azureVMFaultSpec.getFaultName())) {
            return (AzureStorageFaults.DETACH_DISKS.getRemediation().name() + ": --resourceIdsWithDisks  $FI_ADD_INFO_"
                    + EXTRACT_DETACHED_DISKS_PROPERTY);
        }
        return null;
    }

    /**
     * creates the list of output processing expressions using property name
     *
     * @return List of output processing infos
     */
    private List<CommandOutputProcessingInfo> createOutputProcessingInfo(String propertyName) {
        List<CommandOutputProcessingInfo> commandOutputProcessingInfoList = new ArrayList<>();
        CommandOutputProcessingInfo processingInfoCommand = new CommandOutputProcessingInfo();
        processingInfoCommand.setExtractedPropertyName(propertyName);
        processingInfoCommand.setRegExpression("(?<=VirtualMachines->).*(?=$)");
        commandOutputProcessingInfoList.add(processingInfoCommand);
        return commandOutputProcessingInfoList;
    }


    public List<String> getResourceIds(Map<String, String> tags, boolean random,
            CommandExecutionFaultSpec azureVMFaultSpec) throws MangleException {
        CustomAzureClient azureClient = (CustomAzureClient) endpointClientFactory
                .getEndPointClient(azureVMFaultSpec.getCredentials(), azureVMFaultSpec.getEndpoint());
        return AzureCommonUtils.getAzureVMResourceIds(azureClient, tags, random);
    }

}
