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

package com.vmware.mangle.faults.plugin.tasks.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.EndpointGroupFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMAgentFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMCodeLevelFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.NetworkPartitionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.FaultTriggeringTask;
import com.vmware.mangle.cassandra.model.tasks.RemediableTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.services.enums.FaultName;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.events.TaskSubstageEvent;
import com.vmware.mangle.task.framework.helpers.AbstractTaskHelper;
import com.vmware.mangle.task.framework.skeletons.IMultiTaskHelper;
import com.vmware.mangle.task.framework.utils.TaskDescriptionUtils;
import com.vmware.mangle.utils.constants.FaultConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Task to trigger any fault tasks on endpoint cluster
 *
 * @author bkaranam
 */
@Extension(ordinal = 1)
@Log4j2
@NoArgsConstructor
@SuppressWarnings("squid:CommentedOutCodeLine")
public class EndpointGroupFaultTriggerTaskHelper<T extends EndpointGroupFaultTriggerSpec, S extends CommandExecutionFaultSpec>
        extends AbstractTaskHelper<T> implements IMultiTaskHelper<T, S> {

    private EndpointClientFactory endpointClientFactory;

    private BytemanFaultTaskHelper<S> bytemanFaultTask;

    private SystemResourceFaultTaskHelper2<S> systemResourceFaultTask;


    @Autowired
    public void setEndpointClientFactory(EndpointClientFactory endpointClientFactory) {
        this.endpointClientFactory = endpointClientFactory;
    }

    @Autowired
    public void setBytemanFaultTaskHelper(BytemanFaultTaskHelper<S> bytemanFaultTask) {
        this.bytemanFaultTask = bytemanFaultTask;
    }

    @Autowired
    public void setSystemResourceFaultTaskHelper(SystemResourceFaultTaskHelper2<S> systemResourceFaultTask) {
        this.systemResourceFaultTask = systemResourceFaultTask;
    }

    @Override
    public Task<T> init(T faultSpec) throws MangleException {
        return init(faultSpec, null);
    }

    @Override
    public Task<T> init(T taskData, String injectedTaskId) {
        FaultTriggeringTask<T, S> task = new FaultTriggeringTask<>();
        init(task, taskData, injectedTaskId);
        return task;
    }

    @Override
    public void executeTask(Task<T> task) throws MangleException {
        handleSubstages(task);
        ((RemediableTask) task).setRemediated(true);
        if (task.getChildTaskIDs().isEmpty() && task.getTaskStatus() != TaskStatus.FAILED) {
            throw new MangleRuntimeException(ErrorCode.FAILED_TO_CREATE_CHILD_TASKS);
        }
    }

    private void handleSubstages(Task<T> task) throws MangleException {
        String substage = task.getTaskSubstage();
        if (StringUtils.isEmpty(substage)) {
            substage = SubStage.INITIALISED.name();
            updateSubstage(task, SubStage.INITIALISED);
            this.getPublisher().publishEvent(new TaskSubstageEvent(task));
        }
        switch (SubStage.valueOf(substage.toUpperCase())) {
        case INITIALISED:
            triggerChildTasks(task);
            break;
        case TRIGGER_CHILD_TASKS:
            break;
        default:
            break;
        }
    }

    private void triggerChildTasks(Task<T> task) throws MangleException {
        if (task.getTaskType() == TaskType.INJECTION) {
            List<EndpointSpec> endpoints = selectRandomEndpoints(task.getTaskData().getEndpoints(),
                    task.getTaskData().getFaultSpec().getRandomEndpoint());
            Map<String, String> childTaskMap = new HashMap<>();
            ((FaultTriggeringTask<T, S>) task).setTaskObjmap(new HashMap<>());
            int childCounter = 1;
            for (EndpointSpec endpoint : endpoints) {
                Task<S> childTask = null;
                S childFaultSpec = createChildFaultSpec(task, endpoint);
                childFaultSpec.setEndpoint(endpoint);
                if (childFaultSpec instanceof JVMAgentFaultSpec
                        && null != ((JVMAgentFaultSpec) childFaultSpec).getJvmProperties()
                        && null != ((JVMAgentFaultSpec) childFaultSpec).getJvmProperties().getJvmprocess()) {
                    childTask = bytemanFaultTask.init(childFaultSpec);
                } else {
                    childTask = systemResourceFaultTask.init(childFaultSpec);
                }
                childTask.setTaskName(childTask.getTaskName() + "-" + childCounter++);
                ((FaultTriggeringTask<T, S>) task).getTaskObjmap().put(endpoint.getName(), childTask);
                childTaskMap.put(endpoint.getName(), childTask.getId());
            }
            task.setChildTaskIDs(new ArrayList<String>(childTaskMap.values()));
            task.getTaskData().setChildTaskMap(childTaskMap);
        }
        if (task.getTaskType() == TaskType.REMEDIATION) {
            Map<String, String> childTaskMap = new HashMap<>();
            task.getTaskData().setChildTaskMap(childTaskMap);
        }
        task.getTaskData().setReadyForChildExecution(true);
        log.info("Completed Triggering child tasks");
        updateSubstage(task, SubStage.TRIGGER_CHILD_TASKS);
        this.getPublisher().publishEvent(new TaskSubstageEvent(task));
    }

    @Override
    public String getDescription(Task<T> task) {
        String description = new StringBuffer().append(task.getTaskData().getFaultSpec().getFaultName())
                .append(" on Endpoint Group: ").append(task.getTaskData().getFaultSpec().getEndpointName())
                .append(". More Details: [ ").append(task.getTaskData().getFaultSpec()).append(" ]").toString();
        description = TaskDescriptionUtils.removeNullMembersFromString(description);
        if (task.getTaskType() == TaskType.INJECTION) {
            return "Executing Fault: " + description;
        } else {
            return "Remediating Fault: " + description;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Task<S>> getChildTasks(Task<T> task) {
        return ((FaultTriggeringTask<T, S>) task).getTaskObjmap();
    }

    @Override
    public boolean isReadyForChildExecution(Task<T> task) {
        return task.getTaskData().isReadyForChildExecution();
    }

    public enum SubStage {
        INITIALISED, TRIGGER_CHILD_TASKS, COMPLETED
    }

    private void updateSubstage(Task<T> task, SubStage stage) {
        task.updateTaskSubstage(stage.name());
    }


    @SuppressWarnings("unchecked")
    private S createChildFaultSpec(Task<T> task, EndpointSpec endpoint) {
        CommandExecutionFaultSpec childFaultspec = null;
        /**
         * Condition to check for BytemanJVMCodelevel fault spec
         */
        if (isJVMCodeLevelFaultSpec(task.getTaskData().getFaultSpec())) {
            JVMCodeLevelFaultSpec parentFaultSpec = (JVMCodeLevelFaultSpec) task.getTaskData().getFaultSpec();
            JVMCodeLevelFaultSpec jvmCodefault = new JVMCodeLevelFaultSpec();
            jvmCodefault.setClassName(parentFaultSpec.getClassName());
            jvmCodefault.setMethodName(parentFaultSpec.getMethodName());
            jvmCodefault.setRuleEvent(parentFaultSpec.getRuleEvent());
            jvmCodefault.setJvmProperties(parentFaultSpec.getJvmProperties());
            childFaultspec = jvmCodefault;

        } else if (isJVMAgentFaultSpec(task.getTaskData().getFaultSpec())) {
            childFaultspec = new JVMAgentFaultSpec();
            ((JVMAgentFaultSpec) childFaultspec)
                    .setJvmProperties(((JVMAgentFaultSpec) task.getTaskData().getFaultSpec()).getJvmProperties());
        } else {
            childFaultspec = new CommandExecutionFaultSpec();
        }

        CommandExecutionFaultSpec parentFaultSpec = task.getTaskData().getFaultSpec();
        childFaultspec.setEndpointName(endpoint.getName());
        childFaultspec.setCredentials(parentFaultSpec.getCredentials());
        childFaultspec.setEndpoint(parentFaultSpec.getEndpoint());
        childFaultspec.setFaultName(parentFaultSpec.getFaultName());
        childFaultspec.setFaultType(parentFaultSpec.getFaultType());
        childFaultspec.setInjectionHomeDir(parentFaultSpec.getInjectionHomeDir());
        childFaultspec.setSchedule(null);
        childFaultspec.setTimeoutInMilliseconds(parentFaultSpec.getTimeoutInMilliseconds());

        if (FaultName.NETWORKPARTITIONFAULT.toString().equals(parentFaultSpec.getFaultName())) {
            networkfaultspeccheck(endpoint, parentFaultSpec, childFaultspec);
        } else {
            childFaultspec.setArgs(new HashMap<>(parentFaultSpec.getArgs()));
        }
        childFaultspec.setSpecType(childFaultspec.getClass().getName());
        childFaultspec.setTags(parentFaultSpec.getTags());
        childFaultspec.setNotifierNames(parentFaultSpec.getNotifierNames());
        childFaultspec.setEndpoint(endpoint);
        log.debug("Spec for child task created successfully.");
        return (S) childFaultspec;
    }

    private void networkfaultspeccheck(EndpointSpec endpoint, CommandExecutionFaultSpec parentFaultSpec,
            CommandExecutionFaultSpec childFaultSpec) {
        Map<String, String> hostListArgs = parentFaultSpec.getArgs();
        NetworkPartitionFaultSpec networkpartiton = (NetworkPartitionFaultSpec) parentFaultSpec;
        List<String> hosts = networkpartiton.getHosts();
        List<String> templist = new ArrayList<String>();
        templist.addAll(hosts);

        if (templist.contains(endpoint.getRemoteMachineConnectionProperties().getHost())) {
            templist.remove(endpoint.getRemoteMachineConnectionProperties().getHost());
        }
        hostListArgs.put(FaultConstants.HOSTS_ARG, templist.stream().collect(Collectors.joining(";")));
        childFaultSpec.setArgs(new HashMap<>(hostListArgs));
    }


    private boolean isJVMCodeLevelFaultSpec(CommandExecutionFaultSpec faultSpec) {
        return faultSpec instanceof JVMCodeLevelFaultSpec
                && ((JVMCodeLevelFaultSpec) faultSpec).getJvmProperties() != null;
    }

    private boolean isJVMAgentFaultSpec(CommandExecutionFaultSpec faultSpec) {
        return faultSpec instanceof JVMAgentFaultSpec && ((JVMAgentFaultSpec) faultSpec).getJvmProperties() != null;
    }

    private List<EndpointSpec> selectRandomEndpoints(List<EndpointSpec> endpoints, boolean random) {
        if (random) {
            Collections.shuffle(endpoints);
            return Arrays.asList(endpoints.get(0));
        } else {
            return endpoints;
        }
    }
}
