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
public class MockCommandExecutionTask<T extends CommandExecutionFaultSpec>
        extends AbstractCommandExecutionTaskHelper<T> {

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

        List<CommandOutputProcessingInfo> commandOutputProcessingInfoList = new ArrayList<>();
        CommandOutputProcessingInfo commandOutputProcessingInfo = new CommandOutputProcessingInfo();
        commandOutputProcessingInfo.setExtractedPropertyName("testProperty");
        commandOutputProcessingInfo.setRegExpression("F.*t");
        commandOutputProcessingInfoList.add(commandOutputProcessingInfo);
        List<String> expectedCommandOutputList = new ArrayList<>();
        expectedCommandOutputList.add("Injecting Fault");

        CommandInfo commandInfo = CommandInfo.builder("echo Injecting Fault")
                .commandOutputProcessingInfoList(commandOutputProcessingInfoList)
                .expectedCommandOutputList(expectedCommandOutputList).ignoreExitValueCheck(false).noOfRetries(2)
                .retryInterval(1).timeout(1).build();

        List<CommandOutputProcessingInfo> commandOutputProcessingInfoList2 = new ArrayList<>();
        CommandOutputProcessingInfo commandOutputProcessingInfo2 = new CommandOutputProcessingInfo();
        commandOutputProcessingInfoList2.add(commandOutputProcessingInfo2);
        List<String> expectedCommandOutputList2 = new ArrayList<>();
        expectedCommandOutputList2.add("Injecting Fault");
        CommandInfo commandInfo2 =
                CommandInfo.builder("echo $FI_STACK arg: $FI_ARG_id, property: $FI_ADD_INFO_testProperty")
                        .commandOutputProcessingInfoList(commandOutputProcessingInfoList2)
                        .expectedCommandOutputList(expectedCommandOutputList2).ignoreExitValueCheck(false)
                        .noOfRetries(0).retryInterval(1).timeout(1).build();
        List<CommandInfo> commandInfoList = new ArrayList<>();
        commandInfoList.add(commandInfo);
        commandInfoList.add(commandInfo2);
        return commandInfoList;
    }

    private List<CommandInfo> getRemediationcommandInfoList() {
        List<String> expectedCommandOutputList = new ArrayList<>();
        expectedCommandOutputList.add("Remediating Fault");
        CommandInfo commandInfo =
                CommandInfo.builder("echo Remediating Fault").expectedCommandOutputList(expectedCommandOutputList)
                        .ignoreExitValueCheck(false).retryInterval(1).timeout(1).build();
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
    protected void checkTaskSpecificPrerequisites(Task<T> task) throws MangleException {
        //When needed we will implement this
    }

}
