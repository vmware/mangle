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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.cassandra.model.tasks.TaskTroubleShootingInfo;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.task.framework.events.TaskSubstageEvent;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author bkaranam
 */
@Log4j2
public abstract class AbstractCommandExecutionTaskHelper<T extends CommandExecutionFaultSpec> extends AbstractTaskHelper<T> {
    protected CommandInfoExecutionHelper commandInfoExecutionHelper;

    public AbstractCommandExecutionTaskHelper() {
    }

    @Autowired(required = true)
    public void setCommandInfoExecutionHelper(CommandInfoExecutionHelper commandInfoExecutionHelper) {
        this.commandInfoExecutionHelper = commandInfoExecutionHelper;
    }

    public List<CommandInfo> getInjectionExecutionInfo(Task<T> task) {
        return task.getTaskData().getInjectionCommandInfoList();
    }

    public List<CommandInfo> getRemediationExecutionInfo(Task<T> task) {
        return task.getTaskData().getRemediationCommandInfoList();
    }

    public List<SupportScriptInfo> listFaultInjectionScripts(Task<T> task) {
        return task.getTaskData().getSupportScriptInfo();
    }

    public List<CommandInfo> getCleanUpExecutionInfo(Task<T> task) {
        return task.getTaskData().getCleanUpCommandInfoList();
    }

    protected abstract void prepareEndpoint(Task<T> task, List<SupportScriptInfo> listFaultInjectionScripts)
            throws MangleException;

    protected abstract ICommandExecutor getExecutor(Task<T> task) throws MangleException;

    protected abstract void checkRemediationPreRequisites(Task<T> task) throws MangleException;

    protected abstract void checkInjectionPreRequisites(Task<T> task) throws MangleException;

    protected abstract void checkTaskSpecificPrerequisites(Task<T> task) throws MangleException;

    @Override
    public void executeTask(Task<T> task) throws MangleException {
        handleSubstages(task);
    }

    public enum SubStage {
        INITIALISED,
        PREREQUISITES_CHECK,
        PREPARE_TARGET_MACHINE,
        TRIGGER_INJECTION,
        REMEDIATION_PREREQUISITES_CHECK,
        TRIGGER_REMEDIATION,
        CLEANUP_EXECUTION_INFO,
        COMPLETED
    }

    private void handleSubstages(Task<T> task) throws MangleException {
        String substage = task.getTaskSubstage();
        if (StringUtils.isEmpty(substage)) {
            substage = SubStage.INITIALISED.name();
            updateSubstage(task, SubStage.INITIALISED);
            getPublisher().publishEvent(new TaskSubstageEvent(task));
        }
        // Verify If the Task is created to Invoke Fault
        if (task.getTaskType() == TaskType.INJECTION) {
            // Run Prerequisites before Injecting Fault
            setMandatoryCommandArgs(task);
            switch (SubStage.valueOf(substage.toUpperCase())) {
            case PREREQUISITES_CHECK:
                prepareTestmachineStage(task);
                break;
            case PREPARE_TARGET_MACHINE:
                triggerFaultInjectionStage(task);
                break;
            default:
                checkInjectionPrequisitesStage(task);
                break;
            }
        }
        if (task.getTaskType() == TaskType.REMEDIATION) {
            switch (SubStage.valueOf(substage.toUpperCase())) {
            case REMEDIATION_PREREQUISITES_CHECK:
                triggerFaultRemediationStage(task);
                break;
            case TRIGGER_REMEDIATION:
                cleanupExecutionInfoStage(task);
                break;
            default:
                checkRemediationPrequisitesStage(task);
                break;
            }
        }
    }

    private void checkInjectionPrequisitesStage(Task<T> task) throws MangleException {
        checkInjectionPreRequisites(task);
        updateSubstage(task, SubStage.PREREQUISITES_CHECK);
        this.getPublisher().publishEvent(new TaskSubstageEvent(task));
        log.info("Completed Prerequisite Check");
        prepareTestmachineStage(task);
    }

    private void prepareTestmachineStage(Task<T> task) throws MangleException {
        // Verify if the Task is running for First time in the Target
        // Machine.
        Stack<TaskTrigger> triggers = task.getTriggers();
        if (triggers.isEmpty() || !TaskStatus.COMPLETED.equals(triggers.peek().getTaskStatus())) {
            // Prepare Target Machine if it is getting executed for the
            // first Time.
            prepareEndpoint(task, listFaultInjectionScripts(task));
            log.info("Completed Test Machine Preperation");
        }
        updateSubstage(task, SubStage.PREPARE_TARGET_MACHINE);
        this.getPublisher().publishEvent(new TaskSubstageEvent(task));
        triggerFaultInjectionStage(task);
    }

    private void triggerFaultInjectionStage(Task<T> task) throws MangleException {
        // Trigger Fault Injection Commands
        if (task.getTaskTroubleShootingInfo() == null) {
            TaskTroubleShootingInfo taskTroubleShootingInfo = new TaskTroubleShootingInfo();
            taskTroubleShootingInfo.setAdditionalInfo(new HashMap<>());
            task.setTaskTroubleShootingInfo(taskTroubleShootingInfo);
        }
        commandInfoExecutionHelper.runCommands(getExecutor(task), getInjectionExecutionInfo(task),
                task.getTaskTroubleShootingInfo(), getArgs(task));
        log.info("Completed Fault Injection");
        updateSubstage(task, SubStage.COMPLETED);
        this.getPublisher().publishEvent(new TaskSubstageEvent(task));
    }

    private void checkRemediationPrequisitesStage(Task<T> task) throws MangleException {
        // Run Prerequisites before Remediating Fault
        checkRemediationPreRequisites(task);
        log.info("Completed Prerequisite Check");
        updateSubstage(task, SubStage.REMEDIATION_PREREQUISITES_CHECK);
        this.getPublisher().publishEvent(new TaskSubstageEvent(task));
        triggerFaultRemediationStage(task);
    }

    private void triggerFaultRemediationStage(Task<T> task) throws MangleException {
        // Trigger Fault Remediation commands
        commandInfoExecutionHelper.runCommands(getExecutor(task), getRemediationExecutionInfo(task),
                task.getTaskTroubleShootingInfo(), getArgs(task));
        log.info("Completed Fault Remediation");
        updateSubstage(task, SubStage.TRIGGER_REMEDIATION);
        this.getPublisher().publishEvent(new TaskSubstageEvent(task));
        cleanupExecutionInfoStage(task);
    }

    private void cleanupExecutionInfoStage(Task<T> task) throws MangleException {
        commandInfoExecutionHelper.runCommands(getExecutor(task), getCleanUpExecutionInfo(task),
                task.getTaskTroubleShootingInfo(), getArgs(task));
        updateSubstage(task, SubStage.COMPLETED);
        this.getPublisher().publishEvent(new TaskSubstageEvent(task));
    }

    private void updateSubstage(Task<T> task, SubStage stage) {
        task.updateTaskSubstage(stage.name());
    }


    protected Map<String, String> getArgs(Task<T> task) {
        return ((CommandExecutionFaultSpec) task.getTaskData()).getArgs();
    }

    protected void setMandatoryCommandArgs(Task<T> task) {
        if (((CommandExecutionFaultSpec) task.getTaskData()).getArgs().isEmpty()) {
            ((CommandExecutionFaultSpec) task.getTaskData()).setArgs(new HashMap<>());
        }
        if (task.getTaskType().equals(TaskType.INJECTION)) {
            Map<String, String> commandInfo = new HashMap<>();

            commandInfo.put("id", task.getId());
            ((CommandExecutionFaultSpec) task.getTaskData()).getArgs().putAll(commandInfo);
        }
    }


}
