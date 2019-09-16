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

package com.vmware.mangle.unittest.task.framework.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j2;

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
@Log4j2
public class MockCommandExecutionTask<T extends CommandExecutionFaultSpec> extends AbstractCommandExecutionTaskHelper<T>
        implements ILongDurationTaskHelper<T> {

    private ICommandExecutor commandExecutor;

    public MockCommandExecutionTask() {

    }


    @Override
    public Task<T> init(T taskspec) throws MangleException {
        return init(taskspec, null);
    }

    @Override
    public FaultTask<T> init(T faultSpec, String injectionTaskId) {
        FaultTask<T> task = (FaultTask<T>) init(new FaultTask<>(), faultSpec, injectionTaskId);

        if (null == injectionTaskId) {
            task.getTaskData().setInjectionCommandInfoList(getInjectionCommandInfoList(task));
            task.getTaskData().setRemediationCommandInfoList(getRemediationcommandInfoList());
        }
        return task;
    }

    private List<CommandInfo> getInjectionCommandInfoList(FaultTask<T> task) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("0", "testArg");
        task.getTaskData().setArgs(args);
        List<CommandInfo> commandInfoList = new ArrayList<>();
        CommandInfo commandInfo = new CommandInfo();
        commandInfoList.add(commandInfo);
        commandInfo.setCommand("echo Injecting Fault");
        List<CommandOutputProcessingInfo> commandOutputProcessingInfoList = new ArrayList<>();
        CommandOutputProcessingInfo commandOutputProcessingInfo = new CommandOutputProcessingInfo();
        commandOutputProcessingInfo.setExtractedPropertyName("testProperty");
        commandOutputProcessingInfo.setRegExpression("F.*t");

        commandOutputProcessingInfoList.add(commandOutputProcessingInfo);
        commandInfo.setCommandOutputProcessingInfoList(commandOutputProcessingInfoList);
        List<String> expectedCommandOutputList = new ArrayList<>();
        expectedCommandOutputList.add("Injecting Fault");
        commandInfo.setExpectedCommandOutputList(expectedCommandOutputList);
        commandInfo.setIgnoreExitValueCheck(false);
        commandInfo.setNoOfRetries(2);
        commandInfo.setRetryInterval(1);
        commandInfo.setTimeout(1);

        CommandInfo commandInfo2 = new CommandInfo();
        commandInfoList.add(commandInfo2);
        commandInfo2.setCommand("echo $FI_STACK arg: $FI_ARG_id, property: $FI_ADD_INFO_testProperty");
        List<CommandOutputProcessingInfo> commandOutputProcessingInfoList2 = new ArrayList<>();
        CommandOutputProcessingInfo commandOutputProcessingInfo2 = new CommandOutputProcessingInfo();
        commandOutputProcessingInfoList2.add(commandOutputProcessingInfo2);
        commandInfo2.setCommandOutputProcessingInfoList(null);
        List<String> expectedCommandOutputList2 = new ArrayList<>();
        expectedCommandOutputList2.add("Injecting Fault");
        commandInfo2.setExpectedCommandOutputList(expectedCommandOutputList2);
        commandInfo2.setIgnoreExitValueCheck(false);
        commandInfo2.setNoOfRetries(0);
        commandInfo2.setRetryInterval(1);
        commandInfo2.setTimeout(1);
        return commandInfoList;
    }

    private List<CommandInfo> getRemediationcommandInfoList() {
        List<CommandInfo> commandInfoList = new ArrayList<>();
        CommandInfo commandInfo = new CommandInfo();
        commandInfoList.add(commandInfo);
        commandInfo.setCommand("echo Remediating Fault");
        List<String> expectedCommandOutputList = new ArrayList<>();
        expectedCommandOutputList.add("Remediating Fault");
        commandInfo.setExpectedCommandOutputList(expectedCommandOutputList);
        commandInfo.setIgnoreExitValueCheck(false);
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

    }

    @Override
    public String getDescription(Task<T> task) {
        return "Executing Fault: " + task.getTaskData().getFaultName() + " on endpoint: "
                + task.getTaskData().getEndpointName();
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
    public boolean isTaskSupportingPause() {
        return true;
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
        //When needed we will implement this
    }

}
