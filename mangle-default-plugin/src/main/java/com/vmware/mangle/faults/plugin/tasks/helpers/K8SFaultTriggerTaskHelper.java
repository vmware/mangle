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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import lombok.extern.log4j.Log4j2;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMAgentFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMCodeLevelFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.tasks.FaultTriggeringTask;
import com.vmware.mangle.cassandra.model.tasks.K8SSpecificArguments;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.events.TaskSubstageEvent;
import com.vmware.mangle.task.framework.helpers.AbstractTaskHelper;
import com.vmware.mangle.task.framework.skeletons.IMultiTaskHelper;
import com.vmware.mangle.utils.clients.kubernetes.KubernetesCommandLineClient;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Task to trigger any fault task which support on k8s cluster
 *
 * @author bkaranam
 */
@Extension(ordinal = 1)
@Log4j2
@SuppressWarnings("squid:CommentedOutCodeLine")
public class K8SFaultTriggerTaskHelper<T extends K8SFaultTriggerSpec, S extends CommandExecutionFaultSpec>
        extends AbstractTaskHelper<T> implements IMultiTaskHelper<T, S> {
    @Autowired
    private EndpointClientFactory endpointClientFactory;

    @Autowired
    private BytemanFaultTaskHelper<S> bytemanFaultTask;

    @Autowired
    SystemResourceFaultTaskHelper<S> systemResourceFaultTask;

    public K8SFaultTriggerTaskHelper() {
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
            List<String> listOfPods = getK8SPods(task);
            Map<String, String> childTaskMap = new HashMap<>();
            ((FaultTriggeringTask<T, S>) task).setTaskObjmap(new HashMap<>());
            for (String podName : listOfPods) {
                Task<S> childTask = null;
                S childFaultSpec = createK8SFaultSpec(task, podName);
                if (childFaultSpec instanceof JVMAgentFaultSpec) {
                    childTask = bytemanFaultTask.init(createK8SFaultSpec(task, podName));
                } else {
                    childTask = systemResourceFaultTask.init(createK8SFaultSpec(task, podName));
                }
                ((FaultTriggeringTask<T, S>) task).getTaskObjmap().put(podName, childTask);
                childTaskMap.put(podName, childTask.getId());
            }
            task.setChildTaskIDs(new ArrayList<String>(childTaskMap.values()));
            task.getTaskData().setChildTaskMap(childTaskMap);
        }
        if (task.getTaskType() == TaskType.REMEDIATION) {
            Map<String, String> childTaskMap = new HashMap<>();
            /*Map<String, String> parentChildTaskMap = task.getTaskData().getChildTaskMap();
            for (String podName : parentChildTaskMap.keySet()) {
                Task<TaskSpec> remediatedTask =
                        (Task<TaskSpec>) FaultTaskFactory.getRemediationTask(parentChildTaskMap.get(podName)).task;
               taskObjmap.put(podName, remediatedTask);
                childTaskMap.put(podName, remediatedTask.getId());
            }*/
            task.getTaskData().setChildTaskMap(childTaskMap);
        }
        task.getTaskData().setReadyForChildExecution(true);
        log.info("Completed Triggering child tasks");
        updateSubstage(task, SubStage.TRIGGER_CHILD_TASKS);
        this.getPublisher().publishEvent(new TaskSubstageEvent(task));
    }

    @Override
    public String getDescription(Task<T> task) {
        return "Executing Fault: " + task.getTaskData().getFaultSpec().getFaultName() + " on K8Sendpoint: "
                + task.getTaskData().getFaultSpec().getEndpointName() + "["
                + task.getTaskData().getFaultSpec().getK8sArguments() + "]";
    }

    protected List<String> getK8SPods(Task<T> task) throws MangleException {
        List<String> pods = getClient(task).getPODClient()
                .getPodsWithLabels(task.getTaskData().getFaultSpec().getK8sArguments().getPodLabels());
        if (null == pods || pods.isEmpty()) {
            throw new MangleException(ErrorCode.NO_PODS_IDENTIFIED,
                    task.getTaskData().getFaultSpec().getK8sArguments().getPodLabels());
        }
        if (task.getTaskData().getFaultSpec().getK8sArguments().getEnableRandomInjection()) {
            String podName = pods.get((new Random().nextInt(pods.size())));
            pods = new ArrayList<>();
            pods.add(podName);
        }
        return pods;
    }

    private KubernetesCommandLineClient getClient(Task<T> task) {
        return (KubernetesCommandLineClient) endpointClientFactory.getEndPointClient(
                task.getTaskData().getFaultSpec().getCredentials(), task.getTaskData().getFaultSpec().getEndpoint());
    }

    /**
     * Method to create K8SFaultSpec for each podName
     *
     * @param podName
     */
    @SuppressWarnings("unchecked")
    private S createK8SFaultSpec(Task<T> task, String podName) {
        CommandExecutionFaultSpec k8sFaultspec = null;
        /**
         * Condition to check for BytemanJVMCodelevel fault spec
         */
        if (isJVMCodeLevelFaultSpec(task.getTaskData().getFaultSpec())) {
            JVMCodeLevelFaultSpec parentFaultSpec = (JVMCodeLevelFaultSpec) task.getTaskData().getFaultSpec();
            JVMCodeLevelFaultSpec jvmCodefault = new JVMCodeLevelFaultSpec();
            jvmCodefault.setClassName(parentFaultSpec.getClassName());
            jvmCodefault.setMethodName(parentFaultSpec.getMethodName());
            jvmCodefault.setRuleEvent(parentFaultSpec.getRuleEvent());
            k8sFaultspec = jvmCodefault;
        } else if (isJVMAgentFaultSpec(task.getTaskData().getFaultSpec())) {
            k8sFaultspec = new JVMAgentFaultSpec();
            ((JVMAgentFaultSpec) k8sFaultspec)
                    .setJvmProperties(((JVMAgentFaultSpec) task.getTaskData().getFaultSpec()).getJvmProperties());
        } else {
            k8sFaultspec = new CommandExecutionFaultSpec();
        }

        CommandExecutionFaultSpec parentFaultSpec = task.getTaskData().getFaultSpec();
        k8sFaultspec.setEndpointName(parentFaultSpec.getEndpointName());
        k8sFaultspec.setCredentials(parentFaultSpec.getCredentials());
        k8sFaultspec.setEndpoint(parentFaultSpec.getEndpoint());
        k8sFaultspec.setFaultName(parentFaultSpec.getFaultName());
        k8sFaultspec.setFaultType(parentFaultSpec.getFaultType());
        k8sFaultspec.setK8sArguments(getK8SArguments(task, podName));
        k8sFaultspec.setSchedule(null);
        k8sFaultspec.setTimeoutInMilliseconds(parentFaultSpec.getTimeoutInMilliseconds());
        k8sFaultspec.setArgs(new HashMap<>(parentFaultSpec.getArgs()));
        k8sFaultspec.setSpecType(k8sFaultspec.getClass().getName());
        log.debug("Spec for child task created successfully.");
        return (S) k8sFaultspec;
    }

    private boolean isJVMCodeLevelFaultSpec(CommandExecutionFaultSpec faultSpec) {
        return faultSpec instanceof JVMCodeLevelFaultSpec
                && ((JVMCodeLevelFaultSpec) faultSpec).getJvmProperties() != null;
    }

    private boolean isJVMAgentFaultSpec(CommandExecutionFaultSpec faultSpec) {
        return faultSpec instanceof JVMAgentFaultSpec && ((JVMAgentFaultSpec) faultSpec).getJvmProperties() != null;
    }

    private K8SSpecificArguments getK8SArguments(Task<T> task, String podName) {
        K8SSpecificArguments k8sArgs = new K8SSpecificArguments();
        k8sArgs.setContainerName(task.getTaskData().getFaultSpec().getK8sArguments().getContainerName());
        k8sArgs.setEnableRandomInjection(
                task.getTaskData().getFaultSpec().getK8sArguments().getEnableRandomInjection());
        k8sArgs.setPodLabels(task.getTaskData().getFaultSpec().getK8sArguments().getPodLabels());
        k8sArgs.setPodInAction(podName);
        return k8sArgs;
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
}
