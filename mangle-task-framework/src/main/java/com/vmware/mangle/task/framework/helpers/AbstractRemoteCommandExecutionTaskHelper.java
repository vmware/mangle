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

package com.vmware.mangle.task.framework.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.model.enums.OSType;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author bkaranam
 */
public abstract class AbstractRemoteCommandExecutionTaskHelper<T extends CommandExecutionFaultSpec>
        extends AbstractCommandExecutionTaskHelper<T> {

    public AbstractRemoteCommandExecutionTaskHelper() {

    }

    @Override
    public abstract List<SupportScriptInfo> listFaultInjectionScripts(Task<T> task);

    @Override
    public void checkInjectionPreRequisites(Task<T> task) throws MangleException {
        checkTaskSpecificPrerequisites(task);
    }

    public List<CommandInfo> getPrerequisiteCommands() {
        List<CommandInfo> commandInfos = new ArrayList<>();
        CommandInfo commandInfo = new CommandInfo();
        commandInfo.setCommand(Constants.LINUX_FILE_EXIST_CHECK_COMMAND.replaceAll("FILE_LOCATION",
                Constants.LINUX_TARGET_CONFIG_FILE_LOCATION));
        commandInfo.setIgnoreExitValueCheck(false);
        commandInfo.setExpectedCommandOutputList(
                Arrays.asList(Constants.LINUX_TARGET_CONFIG_FILE_LOCATION + " does not exist"));
        commandInfos.add(commandInfo);
        return commandInfos;
    }

    protected CommandInfo getDefaultCleanUpCommand() {
        return null;
    }

    protected List<CommandInfo> getDefaultRemoteMachinePreperationCommands() {
        return new ArrayList<>();
    }

    @Override
    public void checkRemediationPreRequisites(Task<T> task) throws MangleException {
        EndpointSpec endpoint = task.getTaskData().getEndpoint();
        if ((endpoint.getEndPointType().equals(EndpointType.MACHINE)
                && (endpoint.getRemoteMachineConnectionProperties().getOsType().equals(OSType.LINUX)))
                || endpoint.getEndPointType().equals(EndpointType.DOCKER)) {
            checkTaskSpecificPrerequisites(task);
        }
    }
}
