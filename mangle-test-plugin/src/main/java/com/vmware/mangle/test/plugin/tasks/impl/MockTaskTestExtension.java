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

package com.vmware.mangle.test.plugin.tasks.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.pf4j.Extension;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.FaultTask;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandOutputProcessingInfo;
import com.vmware.mangle.task.framework.helpers.AbstractCommandExecutionTaskHelper;
import com.vmware.mangle.utils.CommandUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.exceptions.MangleException;


/**
 * @author hkilari
 *
 */

@Extension(ordinal = 1)
public class MockTaskTestExtension<T extends CommandExecutionFaultSpec> extends AbstractCommandExecutionTaskHelper<T> {

    private ICommandExecutor commandExecutor;

    @Override
    public Task<T> init(T faultSpec, String injectionTaskId) {
        Task<T> task = init(new FaultTask<T>(), faultSpec, injectionTaskId);

        if (StringUtils.isEmpty(injectionTaskId)) {
            faultSpec.setInjectionCommandInfoList(getInjectionCommandInfoList());
            faultSpec.setRemediationCommandInfoList(getRemediationcommandInfoList());
        }
        return task;
    }

    @Override
    public Task<T> init(T faultSpec) throws MangleException {
        return init(new FaultTask<T>(), faultSpec, null);
    }

    private List<CommandInfo> getInjectionCommandInfoList() {
        List<CommandOutputProcessingInfo> commandOutputProcessingInfoList = new ArrayList<>();
        CommandOutputProcessingInfo commandOutputProcessingInfo = new CommandOutputProcessingInfo();
        commandOutputProcessingInfoList.add(commandOutputProcessingInfo);
        List<String> expectedCommandOutputList = new ArrayList<>();
        expectedCommandOutputList.add("Injecting Fault");
        CommandInfo commandInfo = CommandInfo.builder("echo \"Injecting Fault\"")
                .commandOutputProcessingInfoList(commandOutputProcessingInfoList)
                .expectedCommandOutputList(expectedCommandOutputList).ignoreExitValueCheck(false).noOfRetries(2)
                .retryInterval(1).timeout(1).build();
        List<CommandInfo> commandInfoList = new ArrayList<>();
        commandInfoList.add(commandInfo);
        return commandInfoList;
    }

    private List<CommandInfo> getRemediationcommandInfoList() {
        List<CommandOutputProcessingInfo> commandOutputProcessingInfoList = new ArrayList<>();
        CommandOutputProcessingInfo commandOutputProcessingInfo = new CommandOutputProcessingInfo();
        commandOutputProcessingInfoList.add(commandOutputProcessingInfo);
        List<String> expectedCommandOutputList = new ArrayList<>();
        expectedCommandOutputList.add("Remediating Fault");
        CommandInfo commandInfo = CommandInfo.builder("echo \"Remediating Fault\"")
                .commandOutputProcessingInfoList(commandOutputProcessingInfoList)
                .expectedCommandOutputList(expectedCommandOutputList).ignoreExitValueCheck(false).noOfRetries(2)
                .retryInterval(1).timeout(1).build();
        List<CommandInfo> commandInfoList = new ArrayList<>();
        commandInfoList.add(commandInfo);
        return commandInfoList;
    }

    @Override
    public ICommandExecutor getExecutor(Task<T> task) throws MangleException {
        if (commandExecutor == null) {
            commandExecutor = new CommandUtils();
        }
        return commandExecutor;
    }

    @Override
    protected void prepareEndpoint(Task<T> task, List<SupportScriptInfo> listOfFaultInjectionScripts)
            throws MangleException {
        //No requirement identified
    }

    @Override
    public String getDescription(Task<T> task) {
        return "Executing Fault: " + task.getTaskData().getFaultName() + " on endpoint: "
                + task.getTaskData().getEndpointName();
    }

    @Override
    public List<SupportScriptInfo> listFaultInjectionScripts(Task<T> task) {
        return Collections.emptyList();
    }

    @Override
    public void checkInjectionPreRequisites(Task<T> task) throws MangleException {
        //No requirement identified
    }

    @Override
    public void checkRemediationPreRequisites(Task<T> task) throws MangleException {
        //No requirement identified
    }

    @Override
    protected void checkTaskSpecificPrerequisites(Task<T> task) throws MangleException {
        //No requirement identified
    }
}
