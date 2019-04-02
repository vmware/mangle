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

package com.vmware.mangle.faults.plugin.helpers.vcenter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandOutputProcessingInfo;
import com.vmware.mangle.services.enums.VCenterDiskFaults;
import com.vmware.mangle.services.enums.VCenterNicFaults;
import com.vmware.mangle.services.enums.VCenterStateFaults;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.skeletons.ICommandExecutionFaultHelper;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.vcenter.VCenterClient;
import com.vmware.mangle.utils.clients.vcenter.VCenterCommandExecutor;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author chetanc
 *
 *         Vcenter fault helper, that constructs remediation and injections command
 */
@Component
public class VCenterFaultHelper implements ICommandExecutionFaultHelper {

    private EndpointClientFactory endpointClientFactory;

    @Autowired
    public VCenterFaultHelper(EndpointClientFactory endpointClientFactory) {
        this.endpointClientFactory = endpointClientFactory;
    }

    @Override
    public void checkTaskSpecificPrerequisites() throws MangleException {
        //           No prerequitsites for the VCenter related faults
    }

    @Override
    public ICommandExecutor getExecutor(CommandExecutionFaultSpec vmFaultSpec) throws MangleException {
        return new VCenterCommandExecutor((VCenterClient) endpointClientFactory
                .getEndPointClient(vmFaultSpec.getCredentials(), vmFaultSpec.getEndpoint()));
    }

    @Override
    public List<SupportScriptInfo> getAgentFaultInjectionScripts() {
        return new ArrayList<>();
    }

    @Override
    public List<CommandInfo> getInjectionCommandInfoList(ICommandExecutor executor,
            CommandExecutionFaultSpec vmFaultSpec) throws MangleException {
        List<CommandInfo> commandInfoList = new ArrayList<>();
        CommandInfo commandInfo = new CommandInfo();
        commandInfo.setCommand(buildInjectionCommand(vmFaultSpec));
        commandInfo.setIgnoreExitValueCheck(false);
        commandInfoList.add(commandInfo);


        if (VCenterDiskFaults.DISCONNECT_DISK.name().equalsIgnoreCase(vmFaultSpec.getFaultName())) {
            List<CommandOutputProcessingInfo> commandOutputProcessingInfoList =
                    createDisconnectDiskOutputProcessingInfo();
            commandInfo.setCommandOutputProcessingInfoList(commandOutputProcessingInfoList);
        }

        return commandInfoList;
    }

    @Override
    public List<CommandInfo> getRemediationCommandInfoList(ICommandExecutor executor,
            CommandExecutionFaultSpec vmFaultSpec) throws MangleException {
        List<CommandInfo> commandInfoList = new ArrayList<>();
        String remediationCommand = buildRemediationCommand(vmFaultSpec);
        if (remediationCommand != null) {
            CommandInfo commandInfo = new CommandInfo();
            commandInfo.setCommand(remediationCommand);
            commandInfo.setIgnoreExitValueCheck(false);
            commandInfoList.add(commandInfo);
        }

        return commandInfoList;
    }

    /**
     * creates the list of output processing expressions for disconnect disk injection
     *
     * @return List of output processing infos
     */
    private List<CommandOutputProcessingInfo> createDisconnectDiskOutputProcessingInfo() {
        List<CommandOutputProcessingInfo> commandOutputProcessingInfoList = new ArrayList<>();
        CommandOutputProcessingInfo typeCOInfo = new CommandOutputProcessingInfo();

        typeCOInfo.setExtractedPropertyName("type");
        typeCOInfo.setRegExpression("(?<=type: ).*(?=; backing)");
        commandOutputProcessingInfoList.add(typeCOInfo);

        CommandOutputProcessingInfo backingTypeCOInfo = new CommandOutputProcessingInfo();
        backingTypeCOInfo.setExtractedPropertyName("backingType");
        backingTypeCOInfo.setRegExpression("(?<=backing_type: ).*(?=;)");
        commandOutputProcessingInfoList.add(backingTypeCOInfo);

        CommandOutputProcessingInfo backingFileCOInfo = new CommandOutputProcessingInfo();
        backingFileCOInfo.setExtractedPropertyName("backingVMDKFile");
        backingFileCOInfo.setRegExpression("(?<=vmdk_file: ).*(?=$)");
        commandOutputProcessingInfoList.add(backingFileCOInfo);
        return commandOutputProcessingInfoList;
    }

    private String buildInjectionCommand(CommandExecutionFaultSpec vmFaultSpec) {
        return vmFaultSpec.getFaultName() + ":" + CommonUtils.convertMaptoDelimitedString(vmFaultSpec.getArgs(), " ");
    }

    public String buildRemediationCommand(CommandExecutionFaultSpec vmFaultSpec) {
        if (VCenterStateFaults.POWEROFF_VM.name().equalsIgnoreCase(vmFaultSpec.getFaultName())) {
            return VCenterStateFaults.POWEROFF_VM.getRemediation().name() + ":"
                    + CommonUtils.convertMaptoDelimitedString(vmFaultSpec.getArgs(), " ");
        } else if (VCenterStateFaults.SUSPEND_VM.name().equalsIgnoreCase(vmFaultSpec.getFaultName())) {
            return VCenterStateFaults.SUSPEND_VM.getRemediation().name() + ":"
                    + CommonUtils.convertMaptoDelimitedString(vmFaultSpec.getArgs(), " ");
        } else if (VCenterDiskFaults.DISCONNECT_DISK.name().equalsIgnoreCase(vmFaultSpec.getFaultName())) {
            return VCenterDiskFaults.DISCONNECT_DISK.getRemediation().name() + ":"
                    + CommonUtils.convertMaptoDelimitedString(vmFaultSpec.getArgs(), " ")
                    + " --type $FI_ADD_INFO_type --backing_type $FI_ADD_INFO_backingType --vmdk_file $FI_ADD_INFO_backingVMDKFile";
        } else if (VCenterNicFaults.DISCONNECT_NIC.name().equalsIgnoreCase(vmFaultSpec.getFaultName())) {
            return VCenterNicFaults.DISCONNECT_NIC.getRemediation().name() + ":"
                    + CommonUtils.convertMaptoDelimitedString(vmFaultSpec.getArgs(), " ");
        }
        return null;
    }

}
