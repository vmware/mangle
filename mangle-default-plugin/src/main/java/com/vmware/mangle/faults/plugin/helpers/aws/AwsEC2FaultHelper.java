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

package com.vmware.mangle.faults.plugin.helpers.aws;

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
import com.vmware.mangle.model.aws.AwsEC2NetworkFaults;
import com.vmware.mangle.model.aws.AwsEC2StateFaults;
import com.vmware.mangle.model.aws.AwsEC2StorageFaults;
import com.vmware.mangle.model.aws.AwsService;
import com.vmware.mangle.model.aws.faults.spec.AwsEC2FaultSpec;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.skeletons.ICommandExecutionFaultHelper;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.aws.AWSCommandExecutor;
import com.vmware.mangle.utils.clients.aws.AWSCommonUtils;
import com.vmware.mangle.utils.clients.aws.CustomAwsClient;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author bkaranam
 *
 *         AWS EC2 fault helper, that constructs remediation and injections command
 */
@Component
public class AwsEC2FaultHelper implements ICommandExecutionFaultHelper {

    private EndpointClientFactory endpointClientFactory;
    private static final String EXTRACT_BLOCKED_INSTANCES_PROPERTY = "BlockedInstances";
    private static final String EXTRACT_DETACHED_VOLUMES_PROPERTY = "InstancesWithDetachedVolumes";

    @Autowired
    public AwsEC2FaultHelper(EndpointClientFactory endpointClientFactory) {
        this.endpointClientFactory = endpointClientFactory;

    }

    @Override
    public void checkTaskSpecificPrerequisites() {
        //  No prerequitsites for the aws related faults
    }

    @Override
    public ICommandExecutor getExecutor(CommandExecutionFaultSpec awsEC2FaultSpec) throws MangleException {
        return new AWSCommandExecutor((CustomAwsClient) endpointClientFactory
                .getEndPointClient(awsEC2FaultSpec.getCredentials(), awsEC2FaultSpec.getEndpoint()), AwsService.EC2);
    }

    @Override
    public List<SupportScriptInfo> getAgentFaultInjectionScripts() {
        return new ArrayList<>();
    }

    @Override
    public List<CommandInfo> getInjectionCommandInfoList(ICommandExecutor executor,
            CommandExecutionFaultSpec awsEC2FaultSpec) {
        List<CommandOutputProcessingInfo> commandOutputProcessingInfoList = null;
        if (AwsEC2NetworkFaults.BLOCK_ALL_NETWORK_TRAFFIC.name().equalsIgnoreCase(awsEC2FaultSpec.getFaultName())) {
            commandOutputProcessingInfoList = createOutputProcessingInfo(EXTRACT_BLOCKED_INSTANCES_PROPERTY);
        }
        if (AwsEC2StorageFaults.DETACH_VOLUMES.name().equalsIgnoreCase(awsEC2FaultSpec.getFaultName())) {
            commandOutputProcessingInfoList = createOutputProcessingInfo(EXTRACT_DETACHED_VOLUMES_PROPERTY);
        }

        CommandInfo commandInfo = CommandInfo.builder(buildInjectionCommand((AwsEC2FaultSpec) awsEC2FaultSpec))
                .ignoreExitValueCheck(false).commandOutputProcessingInfoList(commandOutputProcessingInfoList).build();
        List<CommandInfo> commandInfoList = new ArrayList<>();
        commandInfoList.add(commandInfo);
        return commandInfoList;
    }

    @Override
    public List<CommandInfo> getRemediationCommandInfoList(ICommandExecutor executor,
            CommandExecutionFaultSpec awsEC2FaultSpec) {
        List<CommandInfo> commandInfoList = new ArrayList<>();
        String remediationCommand = buildRemediationCommand((AwsEC2FaultSpec) awsEC2FaultSpec);
        if (remediationCommand != null) {
            CommandInfo commandInfo = CommandInfo.builder(remediationCommand).ignoreExitValueCheck(false).build();
            commandInfoList.add(commandInfo);
        }

        return commandInfoList;
    }

    private String buildInjectionCommand(AwsEC2FaultSpec awsEC2FaultSpec) {
        String injectionCommand = awsEC2FaultSpec.getFaultName() + ": ";
        if (!CollectionUtils.isEmpty(awsEC2FaultSpec.getArgs())) {
            injectionCommand += CommonUtils.convertMaptoDelimitedString(awsEC2FaultSpec.getArgs(), " ");
        }
        return injectionCommand + " --instanceIds " + String.join(",", awsEC2FaultSpec.getInstanceIds());
    }

    public String buildRemediationCommand(AwsEC2FaultSpec awsEC2FaultSpec) {
        if (AwsEC2StateFaults.STOP_INSTANCES.name().equalsIgnoreCase(awsEC2FaultSpec.getFaultName())) {
            return (AwsEC2StateFaults.STOP_INSTANCES.getRemediation().name() + ": --instanceIds "
                    + String.join(",", awsEC2FaultSpec.getInstanceIds()));
        }

        if (AwsEC2NetworkFaults.BLOCK_ALL_NETWORK_TRAFFIC.name().equalsIgnoreCase(awsEC2FaultSpec.getFaultName())) {
            return (AwsEC2NetworkFaults.BLOCK_ALL_NETWORK_TRAFFIC.getRemediation().name()
                    + ": --instanceIdsWithSecurityGroups  $FI_ADD_INFO_" + EXTRACT_BLOCKED_INSTANCES_PROPERTY);
        }

        if (AwsEC2StorageFaults.DETACH_VOLUMES.name().equalsIgnoreCase(awsEC2FaultSpec.getFaultName())) {
            return (AwsEC2StorageFaults.DETACH_VOLUMES.getRemediation().name()
                    + ": --instanceIDsWithVolumeIds  $FI_ADD_INFO_" + EXTRACT_DETACHED_VOLUMES_PROPERTY);
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

        CommandOutputProcessingInfo outputProcessInfo = new CommandOutputProcessingInfo();
        outputProcessInfo.setExtractedPropertyName(propertyName);
        outputProcessInfo.setRegExpression("(?<=Instances->).*(?=$)");
        commandOutputProcessingInfoList.add(outputProcessInfo);
        return commandOutputProcessingInfoList;
    }

    public List<String> getInstanceIds(Map<String, String> tags, boolean random,
            CommandExecutionFaultSpec awsEC2FaultSpec) throws MangleException {
        CustomAwsClient awsClient = (CustomAwsClient) endpointClientFactory
                .getEndPointClient(awsEC2FaultSpec.getCredentials(), awsEC2FaultSpec.getEndpoint());
        return AWSCommonUtils.getAwsInstances(awsClient, tags, random);
    }

}
