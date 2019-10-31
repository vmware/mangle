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
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.CpuFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DiskIOFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DiskSpaceSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DockerFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMAgentFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.KillProcessFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.MemoryFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMDiskFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMNicFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMStateFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.RemediableTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
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
    private static final String SEPERATOR = " ; ";

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
        log.debug("Validating if the Task Data is valid before proceeding ");
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
        faultEventInfo.setFaultName(
                commandExecutionFaultSpec.getFaultName() + MetricProviderConstants.HYPHEN + this.task.getTaskType());
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
        Integer faultTimeOut = commandExecutionFaultSpec.getTimeoutInMilliseconds();
        if (!(commandExecutionFaultSpec instanceof JVMAgentFaultSpec) && faultTimeOut == null) {
            log.debug("The fault : " + commandExecutionFaultSpec.getFaultName()
                    + " does not have timeout property , hence setting the event end time to current time");
            updateFaultEventEndTimeAsNow();
            return;
        }
        if (faultTimeOut != null && task.getTaskType() == TaskType.INJECTION) {
            if (task.getTaskStatus() == TaskStatus.COMPLETED) {
                long faultTimeOutInMilis = CommonUtils.getDateObjectFor(trigger.getEndTime()).getTime() + faultTimeOut;
                log.debug("Updating the Fault time out timestamp as : " + faultTimeOutInMilis);
                faultEventInfo.setFaultEndTime(CommonUtils.getTime(faultTimeOutInMilis));
                faultEventInfo.setFaultEndTimeInEpoch(faultTimeOutInMilis);
                return;
            }
            log.debug(" The task status is: " + task.getTaskStatus() + " hence updating the event endtime as now");
            updateFaultEventEndTimeAsNow();
            return;
        } else if (task.getTaskType() == TaskType.REMEDIATION) {
            log.debug("Can't update the event end time as Fault Time Out is not specified in the fault spec");
            updateFaultEventEndTimeAsNow();
            return;
        } else if (task.getTaskStatus() == TaskStatus.FAILED) {
            log.debug("The task has failed and hence, end time to current time");
            updateFaultEventEndTimeAsNow();
            return;
        }
        log.debug(" Fault timeout is NOT specified. Hence, corresponding fault event will have NO end time.");
        log.debug(" The event corresponding to fault will automatically be closed once fault is remediated.");
    }

    private void updateFaultEventEndTimeAsNow() {
        String endTime = CommonUtils.getTime(System.currentTimeMillis() + MetricProviderConstants.ONE_SECOND_IN_MILLIS);
        faultEventInfo.setFaultEndTime(endTime);
        faultEventInfo.setFaultEndTimeInEpoch(CommonUtils.getDateObjectFor(endTime).getTime());
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
        StringBuilder description = new StringBuilder(
                commandExecutionFaultSpec.getFaultName() + " " + task.getTaskType() + " Submitted." + SEPERATOR);
        if (task.getTaskType() == TaskType.INJECTION) {
            description.append(updateTaskData());
        }
        log.debug("Updating the event description as : " + description);
        faultEventInfo.setFaultDescription(description.toString());
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
        if (task.getTaskType() == TaskType.REMEDIATION) {
            allTags.put("FaultID_Remediated", ((RemediableTask) task).getInjectionTaskId());
        }
        if (commandExecutionFaultSpec.getTags() != null) {
            allTags.putAll(commandExecutionFaultSpec.getTags());
        }
        if (commandExecutionFaultSpec.getEndpoint().getTags() != null) {
            allTags.putAll(commandExecutionFaultSpec.getEndpoint().getTags());
        }
        faultEventInfo.setTags(allTags);
    }

    private String updateTaskData() {
        StringBuilder taskData = new StringBuilder("");
        if (task.getTaskData() instanceof CpuFaultSpec) {
            taskData.append(getCpuFaultTaskData());
        } else if (task.getTaskData() instanceof MemoryFaultSpec) {
            taskData.append(getMemoryFaultTaskData());
        } else if (task.getTaskData() instanceof DiskIOFaultSpec) {
            taskData.append(getDiskIOFaultTaskData());
        } else if (task.getTaskData() instanceof DiskSpaceSpec) {
            taskData.append(getDiskSpaceFaultTaskData());
        } else if (task.getTaskData() instanceof KillProcessFaultSpec) {
            taskData.append(getKillProcessFaultTaskData());
        } else if (task.getTaskData() instanceof K8SFaultSpec) {
            taskData.append(getK8SFaultTaskData());
        } else if (task.getTaskData() instanceof VMStateFaultSpec) {
            taskData.append(getVMStateFaultTaskData());
        } else if (task.getTaskData() instanceof VMNicFaultSpec) {
            taskData.append(getVMNicFautltTaskData());
        } else if (task.getTaskData() instanceof VMDiskFaultSpec) {
            taskData.append(getVMDiskFautltTaskData());
        } else if (task.getTaskData() instanceof DockerFaultSpec) {
            taskData.append(getDockerFaultData());
        } else {
            log.debug("Couldn't find Fault type. Hence, not appending any fault task Data");
        }
        return taskData.toString();
    }

    private String getCpuFaultTaskData() {
        StringBuilder taskData = new StringBuilder();
        CpuFaultSpec cpuSpec = (CpuFaultSpec) task.getTaskData();
        taskData.append("CPU " + MetricProviderConstants.LOAD_INJECTED + cpuSpec.getCpuLoad() + "%" + SEPERATOR);
        if (!StringUtils.isEmpty(cpuSpec.getJvmProperties())) {
            taskData.append("CPU load on process: " + cpuSpec.getJvmProperties().getJvmprocess() + SEPERATOR);
        }
        return taskData.toString();
    }

    private String getMemoryFaultTaskData() {
        StringBuilder taskData = new StringBuilder();
        MemoryFaultSpec memorySpec = (MemoryFaultSpec) task.getTaskData();
        taskData.append(
                "Memory " + MetricProviderConstants.LOAD_INJECTED + memorySpec.getMemoryLoad() + "%" + SEPERATOR);
        if (!StringUtils.isEmpty(memorySpec.getJvmProperties())) {
            taskData.append(
                    "Memory is injected on process: " + memorySpec.getJvmProperties().getJvmprocess() + SEPERATOR);
        }
        return taskData.toString();
    }

    private String getDiskIOFaultTaskData() {
        StringBuilder taskData = new StringBuilder("");
        DiskIOFaultSpec spec = (DiskIOFaultSpec) task.getTaskData();
        taskData.append(MetricProviderConstants.TARGET_DIRECTORY + spec.getTargetDir() + SEPERATOR);
        taskData.append("IO Size: " + spec.getIoSize() + SEPERATOR);
        return taskData.toString();
    }

    private String getDiskSpaceFaultTaskData() {
        DiskSpaceSpec spec = (DiskSpaceSpec) task.getTaskData();
        return MetricProviderConstants.TARGET_DIRECTORY + spec.getDirectoryPath() + SEPERATOR;
    }

    private String getKillProcessFaultTaskData() {
        KillProcessFaultSpec spec = (KillProcessFaultSpec) task.getTaskData();
        return "Process to be killed: " + spec.getProcessIdentifier() + SEPERATOR;
    }

    private String getK8SFaultTaskData() {
        StringBuilder taskData = new StringBuilder("");
        K8SFaultSpec spec = (K8SFaultSpec) task.getTaskData();
        taskData.append("Target resource type: " + spec.getResourceType() + SEPERATOR);
        if (!StringUtils.isEmpty(spec.getResourceName())) {
            taskData.append("Target Resource Name: " + spec.getResourceName() + SEPERATOR);
        }
        if (!StringUtils.isEmpty(spec.getResourceLabels())) {
            taskData.append("Target K8S label: " + spec.getResourceLabels().toString() + SEPERATOR);
        }
        return taskData.toString();
    }

    private String getVMStateFaultTaskData() {
        VMStateFaultSpec taskData = (VMStateFaultSpec) task.getTaskData();
        return MetricProviderConstants.TARGET_VM_TEXT + taskData.getVmName() + SEPERATOR;
    }

    private String getVMNicFautltTaskData() {
        StringBuilder taskData = new StringBuilder("");
        VMNicFaultSpec spec = (VMNicFaultSpec) task.getTaskData();
        taskData.append(MetricProviderConstants.TARGET_VM_TEXT + spec.getVmName() + SEPERATOR);
        taskData.append(MetricProviderConstants.VM_NIC_ID_TEXT + spec.getVmNicId() + SEPERATOR);
        return taskData.toString();
    }

    private String getVMDiskFautltTaskData() {
        StringBuilder taskData = new StringBuilder("");
        VMDiskFaultSpec spec = (VMDiskFaultSpec) task.getTaskData();
        taskData.append(MetricProviderConstants.TARGET_VM_TEXT + spec.getVmName() + SEPERATOR);
        taskData.append(MetricProviderConstants.VM_DISK_ID_TEXT + spec.getVmDiskId() + SEPERATOR);
        return taskData.toString();
    }

    private String getDockerFaultData() {
        DockerFaultSpec spec = (DockerFaultSpec) task.getTaskData();
        return MetricProviderConstants.TARGET_CONTAINER_TEXT + spec.getDockerArguments().getContainerName() + SEPERATOR;
    }
}
