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
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandOutputProcessingInfo;
import com.vmware.mangle.model.aws.AwsEC2NetworkFaults;
import com.vmware.mangle.model.aws.AwsEC2StateFaults;
import com.vmware.mangle.model.aws.faults.spec.AwsEC2FaultSpec;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.skeletons.ICommandExecutionFaultHelper;
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

    @Autowired
    public AwsEC2FaultHelper(EndpointClientFactory endpointClientFactory) {
        this.endpointClientFactory = endpointClientFactory;

    }

    @Override
    public void checkTaskSpecificPrerequisites() {
        //  No prerequitsites for the aws related faults
    }

    @Override
    public ICommandExecutor getExecutor(CommandExecutionFaultSpec awsEC2FaultSpec) {
        return new AWSCommandExecutor((CustomAwsClient) endpointClientFactory
                .getEndPointClient(awsEC2FaultSpec.getCredentials(), awsEC2FaultSpec.getEndpoint()));
    }

    @Override
    public List<SupportScriptInfo> getAgentFaultInjectionScripts() {
        return new ArrayList<>();
    }

    @Override
    public List<CommandInfo> getInjectionCommandInfoList(ICommandExecutor executor,
            CommandExecutionFaultSpec awsEC2FaultSpec) {
        List<CommandInfo> commandInfoList = new ArrayList<>();
        CommandInfo commandInfo = new CommandInfo();
        commandInfo.setCommand(buildInjectionCommand((AwsEC2FaultSpec) awsEC2FaultSpec));
        commandInfo.setIgnoreExitValueCheck(false);
        commandInfoList.add(commandInfo);

        if (AwsEC2NetworkFaults.BLOCK_ALL_NETWORK_TRAFFIC.name().equalsIgnoreCase(awsEC2FaultSpec.getFaultName())) {
            List<CommandOutputProcessingInfo> commandOutputProcessingInfoList =
                    createBlockAllNetworkOutputProcessingInfo();
            commandInfo.setCommandOutputProcessingInfoList(commandOutputProcessingInfoList);
        }

        return commandInfoList;
    }

    @Override
    public List<CommandInfo> getRemediationCommandInfoList(ICommandExecutor executor,
            CommandExecutionFaultSpec awsEC2FaultSpec) {
        List<CommandInfo> commandInfoList = new ArrayList<>();
        String remediationCommand = buildRemediationCommand((AwsEC2FaultSpec) awsEC2FaultSpec);
        if (remediationCommand != null) {
            CommandInfo commandInfo = new CommandInfo();
            commandInfo.setCommand(remediationCommand);
            commandInfo.setIgnoreExitValueCheck(false);
            commandInfoList.add(commandInfo);
        }

        return commandInfoList;
    }

    private String buildInjectionCommand(AwsEC2FaultSpec awsEC2FaultSpec) {
        return awsEC2FaultSpec.getFaultName() + ": --instanceIds " + String.join(",", awsEC2FaultSpec.getInstanceIds());
    }

    public String buildRemediationCommand(AwsEC2FaultSpec awsEC2FaultSpec) {
        if (AwsEC2StateFaults.STOP_INSTANCES.name().equalsIgnoreCase(awsEC2FaultSpec.getFaultName())) {
            return (AwsEC2StateFaults.STOP_INSTANCES.getRemediation().name() + ": --instanceIds "
                    + String.join(",", awsEC2FaultSpec.getInstanceIds()));
        }

        if (AwsEC2NetworkFaults.BLOCK_ALL_NETWORK_TRAFFIC.name().equalsIgnoreCase(awsEC2FaultSpec.getFaultName())) {
            return (AwsEC2NetworkFaults.BLOCK_ALL_NETWORK_TRAFFIC.getRemediation().name()
                    + ": --instanceIdsWithSecurityGroups  $FI_ADD_INFO_BlockedInstances");
        }
        return null;
    }

    /**
     * creates the list of output processing expressions for Block All Network Fault
     *
     * @return List of output processing infos
     */
    private List<CommandOutputProcessingInfo> createBlockAllNetworkOutputProcessingInfo() {
        List<CommandOutputProcessingInfo> commandOutputProcessingInfoList = new ArrayList<>();

        CommandOutputProcessingInfo blockAllNetworkCOInfo = new CommandOutputProcessingInfo();
        blockAllNetworkCOInfo.setExtractedPropertyName("BlockedInstances");
        blockAllNetworkCOInfo.setRegExpression("(?<=Instances->).*(?=$)");
        commandOutputProcessingInfoList.add(blockAllNetworkCOInfo);
        return commandOutputProcessingInfoList;
    }

    public List<String> getInstanceIds(HashMap<String, String> tags, boolean random,
            CommandExecutionFaultSpec awsEC2FaultSpec) throws MangleException {
        CustomAwsClient awsClient = (CustomAwsClient) endpointClientFactory
                .getEndPointClient(awsEC2FaultSpec.getCredentials(), awsEC2FaultSpec.getEndpoint());
        return AWSCommonUtils.getAwsInstances(awsClient, tags, random);
    }

}
