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

package com.vmware.mangle.utils;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.log4j.Log4j2;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.services.dto.FaultEventSpec;
import com.vmware.mangle.utils.constants.MetricProviderConstants;


/**
 * @author dbhat
 *
 * @param <T>
 *            Responsible to populate the Task Data on to FaultEventSpec which can further be used
 *            by Notifiers (monitoring system , Slack etc.)
 */
@Log4j2
public class PopulateFaultEventData<T extends Task<TaskSpec>> {

    private FaultEventSpec faultEventInfo;
    private CommandExecutionFaultSpec commandExecutionFaultSpec;
    private T task;
    private TaskTrigger trigger;

    public PopulateFaultEventData(T task) {
        updateFaultSpecInstance(task.getTaskData());
        faultEventInfo = new FaultEventSpec();
        this.task = task;
        this.trigger = task.getTriggers().peek();
    }

    public FaultEventSpec getFaultEventSpec() {
        if (!isTaskDataValid()) {
            log.error(" Event Data cannot be populated as the Task Data is not valid");
            return null;
        }
        updateFaultName();
        updateFaultStartTime();
        updateFaultEndTime();
        updateFaultEventType();
        updateFaultEventClassification();
        updateFaultEventDescription();
        updateFaultEventTags();
        updateTaskId();
        updateFaultStatus();
        return faultEventInfo;
    }

    private boolean isTaskDataValid() {
        log.info("Validating if the Task Data is valid before proceeding ");
        if (commandExecutionFaultSpec.getEndpoint() == null) {
            log.error(
                    " Task data is not valid. The Task Data doesn't have endpoint information which is important to inject a fault");
            return false;
        }
        return true;
    }

    private void updateFaultSpecInstance(TaskSpec spec) {
        if (spec instanceof K8SFaultTriggerSpec) {
            this.commandExecutionFaultSpec = ((K8SFaultTriggerSpec) spec).getFaultSpec();
        } else {
            this.commandExecutionFaultSpec = (CommandExecutionFaultSpec) spec;
        }
    }

    private void updateFaultName() {
        log.debug("Updating the Mangle Fault Name: " + commandExecutionFaultSpec.getFaultName());
        faultEventInfo.setFaultName(commandExecutionFaultSpec.getFaultName());

    }

    private void updateFaultStartTime() {
        if (task.getTaskStatus() == TaskStatus.COMPLETED) {
            faultEventInfo.setFaultStartTime(trigger.getEndTime());
            faultEventInfo.setFaultStartTimeInEpoch(CommonUtils.getDateObjectFor(trigger.getEndTime()).getTime());
            log.debug("Updating the taskCompletedEvent spec with date: " + trigger.getEndTime());
            return;
        }
        log.debug("Updating the taskCompletedEvent spec with date: " + trigger.getStartTime());
        faultEventInfo.setFaultStartTime(trigger.getStartTime());
        faultEventInfo.setFaultStartTimeInEpoch(CommonUtils.getDateObjectFor(trigger.getStartTime()).getTime());
    }

    private void updateFaultEndTime() {
        String faultTimeOut = commandExecutionFaultSpec.getTimeoutInMilliseconds();
        if (null != faultTimeOut) {

            if (task.getTaskStatus() == TaskStatus.COMPLETED) {
                long faultTimeOutInMilis =
                        CommonUtils.getDateObjectFor(trigger.getEndTime()).getTime() + Long.parseLong(faultTimeOut);
                log.debug("Updating the Fault time out timestamp as : " + faultTimeOutInMilis);
                faultEventInfo.setFaultEndTime(CommonUtils.getTime(faultTimeOutInMilis));
                faultEventInfo.setFaultEndTimeInEpoch(faultTimeOutInMilis);
                return;
            }
        } else {
            log.debug("Can't update the time out time as Time Out is not specified in the fault spec");
        }
        log.debug("The fault didn't run. Setting the end time as the trigger end time ");
        faultEventInfo.setFaultEndTime(trigger.getEndTime());
        faultEventInfo.setFaultEndTimeInEpoch(CommonUtils.getDateObjectFor(trigger.getEndTime()).getTime());
    }

    private void updateFaultEventType() {
        log.debug("Updating the fault Event Type to : " + MetricProviderConstants.MANGLE_FAULT_EVENT_TYPE);
        faultEventInfo.setFaultEventType(MetricProviderConstants.MANGLE_FAULT_EVENT_TYPE);
    }

    private void updateFaultEventClassification() {
        log.debug("Updating the Fault Event Classification to: "
                + MetricProviderConstants.MANGLE_FAULT_EVENT_CLASSIFICATION);
        faultEventInfo.setFaultEventClassification(MetricProviderConstants.MANGLE_FAULT_EVENT_CLASSIFICATION);
    }

    private void updateFaultEventDescription() {
        String description = "Fault " + commandExecutionFaultSpec.getFaultName() + " injected. ";
        log.debug("Updating the event description as : " + description);
        faultEventInfo.setFaultDescription(description);
    }

    private void updateTaskId() {
        log.debug("Updating the Event Info with Task ID: " + task.getId());
        faultEventInfo.setTaskId(task.getId());
    }

    private void updateFaultStatus() {
        log.debug("Updating the fault status as : " + trigger.getTaskStatus().name());
        faultEventInfo.setFaultStatus(trigger.getTaskStatus().name());
    }

    private void updateFaultEventTags() {
        Map<String, String> allTags = new HashMap<>();
        allTags.put("EndpointName", commandExecutionFaultSpec.getEndpointName());
        if (commandExecutionFaultSpec.getTags() != null) {
            allTags.putAll(commandExecutionFaultSpec.getTags());
        }
        if (commandExecutionFaultSpec.getEndpoint().getTags() != null) {
            allTags.putAll(commandExecutionFaultSpec.getEndpoint().getTags());
        }
        faultEventInfo.setTags(allTags);
    }
}
