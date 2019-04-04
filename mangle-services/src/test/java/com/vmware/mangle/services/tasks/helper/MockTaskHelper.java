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

package com.vmware.mangle.services.tasks.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.pf4j.Extension;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.FaultTask;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandOutputProcessingInfo;
import com.vmware.mangle.task.framework.helpers.AbstractCommandExecutionTaskHelper;
import com.vmware.mangle.task.framework.skeletons.ILongDurationTaskHelper;
import com.vmware.mangle.utils.CommandUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.exceptions.MangleException;


/**
 * @author hkilari
 *
 */

@Extension(ordinal = 1)
@Log4j2
public class MockTaskHelper<T extends CommandExecutionFaultSpec> extends AbstractCommandExecutionTaskHelper<T>
        implements ILongDurationTaskHelper<T> {

    private ICommandExecutor commandExecutor;

    @Override
    public Task<T> init(T faultSpec, String injectionTaskId) {
        Task<T> task = init(new FaultTask<T>(), faultSpec, injectionTaskId);
        if (null == injectionTaskId) {
            task.getTaskData().setInjectionCommandInfoList(getInjectionCommandInfoList());
            task.getTaskData().setRemediationCommandInfoList(getRemediationcommandInfoList());
        }
        return task;
    }

    @Override
    public Task<T> init(T faultSpec) {
        return init(faultSpec, null);
    }

    private List<CommandInfo> getInjectionCommandInfoList() {
        List<CommandInfo> commandInfoList = new ArrayList<>();
        CommandInfo commandInfo = new CommandInfo();
        commandInfoList.add(commandInfo);
        commandInfo.setCommand("echo \"Injecting Fault\"");
        List<CommandOutputProcessingInfo> commandOutputProcessingInfoList = new ArrayList<>();
        CommandOutputProcessingInfo commandOutputProcessingInfo = new CommandOutputProcessingInfo();
        commandOutputProcessingInfoList.add(commandOutputProcessingInfo);
        commandInfo.setCommandOutputProcessingInfoList(null);
        List<String> expectedCommandOutputList = new ArrayList<>();
        expectedCommandOutputList.add("Injecting Fault");
        commandInfo.setExpectedCommandOutputList(expectedCommandOutputList);
        commandInfo.setIgnoreExitValueCheck(false);
        commandInfo.setNoOfRetries(2);
        commandInfo.setRetryInterval(1);
        commandInfo.setTimeout(1);
        return commandInfoList;
    }

    private List<CommandInfo> getRemediationcommandInfoList() {
        List<CommandInfo> commandInfoList = new ArrayList<>();
        CommandInfo commandInfo = new CommandInfo();
        commandInfoList.add(commandInfo);
        commandInfo.setCommand("echo \"Remediating Fault\"");
        List<CommandOutputProcessingInfo> commandOutputProcessingInfoList = new ArrayList<>();
        CommandOutputProcessingInfo commandOutputProcessingInfo = new CommandOutputProcessingInfo();
        commandOutputProcessingInfoList.add(commandOutputProcessingInfo);
        commandInfo.setCommandOutputProcessingInfoList(commandOutputProcessingInfoList);
        List<String> expectedCommandOutputList = new ArrayList<>();
        expectedCommandOutputList.add("Remediating Fault");
        commandInfo.setExpectedCommandOutputList(expectedCommandOutputList);
        commandInfo.setIgnoreExitValueCheck(false);
        commandInfo.setNoOfRetries(2);
        commandInfo.setRetryInterval(1);
        commandInfo.setTimeout(1);
        return commandInfoList;
    }

    @Override
    protected ICommandExecutor getExecutor(Task<T> task) throws MangleException {
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
    protected void checkInjectionPreRequisites(Task<T> task) throws MangleException {
        //No requirement identified

    }

    @Override
    protected void checkRemediationPreRequisites(Task<T> task) throws MangleException {
        //No requirement identified

    }

    @Override
    public boolean isTaskSupportingPause() {
        return false;
    }

    @Override
    public boolean pauseTask(Task<T> task) throws MangleException {
        log.info("Pausing the Task: " + task.getTaskData().getId());
        return true;
    }

    @Override
    public boolean resumeTask(Task<T> task) throws MangleException {
        log.info("Resuming the Task: " + task.getTaskData().getId());
        return true;
    }

    @Override
    protected void checkTaskSpecificPrerequisites(Task<T> task) throws MangleException {
        //No requirement identified
    }

}
