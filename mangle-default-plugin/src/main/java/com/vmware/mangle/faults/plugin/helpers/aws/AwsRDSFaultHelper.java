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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandOutputProcessingInfo;
import com.vmware.mangle.model.aws.AwsRDSFaults;
import com.vmware.mangle.model.aws.AwsRDSInstance;
import com.vmware.mangle.model.aws.AwsService;
import com.vmware.mangle.model.aws.faults.spec.AwsRDSFaultSpec;
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
 *         AWS RDS fault helper, that constructs remediation and injections command
 */
@Component
public class AwsRDSFaultHelper implements ICommandExecutionFaultHelper {

    private EndpointClientFactory endpointClientFactory;
    private static final String CONNECTION_LOST_INSTANCES_PROPERTY = "ConnectionLostInstances";

    @Autowired
    public AwsRDSFaultHelper(EndpointClientFactory endpointClientFactory) {
        this.endpointClientFactory = endpointClientFactory;

    }

    @Override
    public void checkTaskSpecificPrerequisites() {
        //  No prerequitsites for the aws related faults
    }

    @Override
    public ICommandExecutor getExecutor(CommandExecutionFaultSpec awsRDSFaultSpec) throws MangleException {
        return new AWSCommandExecutor((CustomAwsClient) endpointClientFactory
                .getEndPointClient(awsRDSFaultSpec.getCredentials(), awsRDSFaultSpec.getEndpoint()), AwsService.RDS);
    }

    @Override
    public List<SupportScriptInfo> getAgentFaultInjectionScripts() {
        return new ArrayList<>();
    }

    @Override
    public List<CommandInfo> getInjectionCommandInfoList(ICommandExecutor executor,
            CommandExecutionFaultSpec awsRDSFaultSpec) {
        List<CommandOutputProcessingInfo> commandOutputProcessingInfoList = null;
        if (AwsRDSFaults.CONNECTION_LOSS.name().equalsIgnoreCase(awsRDSFaultSpec.getFaultName())) {
            commandOutputProcessingInfoList = createOutputProcessingInfo(CONNECTION_LOST_INSTANCES_PROPERTY);
        }
        CommandInfo commandInfo = CommandInfo.builder(buildInjectionCommand((AwsRDSFaultSpec) awsRDSFaultSpec))
                .ignoreExitValueCheck(false).commandOutputProcessingInfoList(commandOutputProcessingInfoList).build();
        List<CommandInfo> commandInfoList = new ArrayList<>();
        commandInfoList.add(commandInfo);
        return commandInfoList;
    }

    @Override
    public List<CommandInfo> getRemediationCommandInfoList(ICommandExecutor executor,
            CommandExecutionFaultSpec awsRDSFaultSpec) {
        List<CommandInfo> commandInfoList = new ArrayList<>();
        String remediationCommand = buildRemediationCommand((AwsRDSFaultSpec) awsRDSFaultSpec);
        if (remediationCommand != null) {
            CommandInfo commandInfo = CommandInfo.builder(remediationCommand).ignoreExitValueCheck(false).build();
            commandInfoList.add(commandInfo);
        }

        return commandInfoList;
    }

    private String buildInjectionCommand(AwsRDSFaultSpec awsRDSFaultSpec) {
        String injectionCommand = awsRDSFaultSpec.getFaultName() + ": ";
        if (!CollectionUtils.isEmpty(awsRDSFaultSpec.getArgs())) {
            injectionCommand += CommonUtils.convertMaptoDelimitedString(awsRDSFaultSpec.getArgs(), " ");
        }
        List<String> dbIdentifiers = new ArrayList<>();
        awsRDSFaultSpec.getSelectedRDSInstances()
                .forEach(instance -> dbIdentifiers.add(instance.getInstanceIdentifier()));
        return injectionCommand + " --dbIdentifiers " + String.join(",", dbIdentifiers);
    }

    public String buildRemediationCommand(AwsRDSFaultSpec awsRDSFaultSpec) {
        List<String> dbIdentifiers = new ArrayList<>();
        awsRDSFaultSpec.getSelectedRDSInstances()
                .forEach(instance -> dbIdentifiers.add(instance.getInstanceIdentifier()));
        if (AwsRDSFaults.CONNECTION_LOSS.name().equalsIgnoreCase(awsRDSFaultSpec.getFaultName())) {
            return (AwsRDSFaults.CONNECTION_LOSS.getRemediation().name() + ": --dbIdentifiers  $FI_ADD_INFO_"
                    + CONNECTION_LOST_INSTANCES_PROPERTY);
        }
        if (AwsRDSFaults.STOP_INSTANCES.name().equalsIgnoreCase(awsRDSFaultSpec.getFaultName())) {
            return (AwsRDSFaults.STOP_INSTANCES.getRemediation().name() + ": --dbIdentifiers "
                    + String.join(",", dbIdentifiers));
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

    public List<AwsRDSInstance> getRdsInstances(List<String> dbIdentifiers, boolean random,
            CommandExecutionFaultSpec awsRDSFaultSpec) throws MangleException {
        CustomAwsClient awsClient = (CustomAwsClient) endpointClientFactory
                .getEndPointClient(awsRDSFaultSpec.getCredentials(), awsRDSFaultSpec.getEndpoint());
        return AWSCommonUtils.verifyAndSelectInstances(awsClient, dbIdentifiers, random);
    }
}
