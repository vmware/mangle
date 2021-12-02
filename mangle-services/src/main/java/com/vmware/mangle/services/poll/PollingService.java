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

package com.vmware.mangle.services.poll;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.cassandra.CassandraConnectionFailureException;
import org.springframework.stereotype.Component;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.RemediableTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskInfo;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.enums.FaultName;
import com.vmware.mangle.services.events.task.TaskCompletedEvent;
import com.vmware.mangle.services.events.task.TaskModifiedEvent;
import com.vmware.mangle.services.helpers.FaultInjectionHelper;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.helpers.CommandInfoExecutionHelper;
import com.vmware.mangle.task.framework.utils.DockerCommandUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.docker.CustomDockerClient;
import com.vmware.mangle.utils.clients.endpoint.EndpointClient;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.constants.KnownFailureConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.CustomErrorMessage;

/**
 * @author jayasankarr
 * @param <T>
 *
 */
@Log4j2
@Component
@DependsOn("cassandraConfig")
public class PollingService<T extends Task<? extends TaskSpec>> {

    private ExecutorService threadService;
    private boolean stopThread = false;
    private TaskService taskService;
    Thread thread = null;
    private FaultInjectionHelper faultInjectionHelper;
    @Autowired
    private CustomErrorMessage customErrorMessage;

    private ApplicationEventPublisher publisher;

    private CommandInfoExecutionHelper commandInfoExecutionHelper;
    private EndpointClientFactory endpointClientFactory;
    private static final int DEFAULT_RECOVERY_TIME = 120000;

    public void setCommandInfoExecutionHelper(CommandInfoExecutionHelper commandInfoExecutionHelper) {
        this.commandInfoExecutionHelper = commandInfoExecutionHelper;

    }

    public void setEndpointClientFactory(EndpointClientFactory endpointClientFactory) {
        this.endpointClientFactory = endpointClientFactory;

    }

    @Autowired
    public PollingService(FaultInjectionHelper faultInjectionHelper, CustomErrorMessage customErrorMessage,
            ApplicationEventPublisher publisher, TaskService taskService,
            CommandInfoExecutionHelper commandInfoExecutionHelper, EndpointClientFactory endpointClientFactory) {

        this.commandInfoExecutionHelper = commandInfoExecutionHelper;
        this.endpointClientFactory = endpointClientFactory;
        this.faultInjectionHelper = faultInjectionHelper;
        this.customErrorMessage = customErrorMessage;
        this.taskService = taskService;
        this.publisher = publisher;
        this.threadService = Executors.newSingleThreadExecutor();
    }

    @SuppressWarnings({ "unchecked" })
    public void startPollingThread() {

        Runnable pollingThread = () -> {
            log.info("Starting polling thread..");
            while (!stopThread) {
                try {
                    List<Task<TaskSpec>> inprogressTasks = taskService.getInjectedSystemResourceTasks();
                    if (!inprogressTasks.isEmpty()) {
                        for (Object obTask : inprogressTasks) {
                            Task<CommandExecutionFaultSpec> task = (Task<CommandExecutionFaultSpec>) obTask;
                            log.debug("Remediation status of current polling task:"
                                    + ((RemediableTask) obTask).isRemediated());
                            CommandExecutionFaultSpec spec = task.getTaskData();
                            try {
                                faultInjectionHelper.updateFaultSpec(spec);
                                EndpointClient endpointClient = endpointClientFactory
                                        .getEndPointClient(spec.getCredentials(), spec.getEndpoint());

                                //Incase if task got remediated between one round of polling adding type.
                                if (task.getTaskType() == TaskType.INJECTION
                                        && ((RemediableTask) obTask).isRemediated() == false) {
                                    String output = "";
                                    if (endpointClient instanceof CustomDockerClient) {
                                        DockerCommandUtils dockerClient =
                                                new DockerCommandUtils(spec, endpointClientFactory);
                                        output = commandInfoExecutionHelper.runCommands((ICommandExecutor) dockerClient,
                                                spec.getStatusCommandInfoList(), task.getTaskTroubleShootingInfo(),
                                                null);
                                    } else {
                                        output = commandInfoExecutionHelper.runCommands(
                                                (ICommandExecutor) endpointClient, spec.getStatusCommandInfoList(),
                                                task.getTaskTroubleShootingInfo(), null);
                                    }
                                    log.info("Status: " + output);
                                    if (output.contains(TaskStatus.COMPLETED.name()) && (task.getTaskStatus().equals(
                                            TaskStatus.IN_PROGRESS) || task.getTaskStatus().equals(TaskStatus.INJECTED)
                                            || task.getTaskStatus().equals(TaskStatus.TEST_MACHINE_INVALID_STATE))) {
                                        updateTaskInfo((T) task, TaskStatus.COMPLETED, task.getTaskFailureReason(),
                                                100);
                                        taskService.updateRemediationFieldofTaskById(task.getId(), true);
                                    }
                                    if (output.contains(TaskStatus.FAILED.name())
                                            && (task.getTaskStatus().equals(TaskStatus.IN_PROGRESS)
                                                    || task.getTaskStatus().equals(TaskStatus.INJECTED))) {
                                        updateTaskInfo((T) task, TaskStatus.FAILED, output, 100);
                                        taskService.updateRemediationFieldofTaskById(task.getId(), true);
                                    }
                                }
                            } catch (MangleException | NullPointerException e) {
                                log.error(e);
                                if (e.getMessage().contains("socket is not established")) {
                                    handleSocketError(e, task, spec);
                                } else if (e.getMessage().contains(
                                        KnownFailureConstants.INJECTION_FILES_IS_MISSING_IN_THE_ENDPOINT_OUTPUT1)
                                        || e.getMessage().contains(
                                                KnownFailureConstants.INJECTION_FILES_IS_MISSING_IN_THE_ENDPOINT_OUTPUT2)) {
                                    if (task.getTaskStatus().equals(TaskStatus.INJECTED)
                                            || task.getTaskStatus().equals(TaskStatus.TEST_MACHINE_INVALID_STATE)) {
                                        updateTaskInfo((T) task, TaskStatus.FAILED,
                                                KnownFailureConstants.INFRA_AGENT_FILES_MISSING_MESSAGE, 100);
                                    }
                                } else if (e.getMessage()
                                        .contains(KnownFailureConstants.INFRA_AGENT_NOT_RUNNING_AT_ENDPOINT_OUTPUT)) {
                                    updateTaskInfo((T) task, TaskStatus.FAILED,
                                            KnownFailureConstants.INFRA_AGENT_NOT_RUNNING_AT_ENDPOINT_MESSAGE, 100);
                                } else if (spec.getEndpoint().getEndPointType().equals(EndpointType.K8S_CLUSTER)) {
                                    handleK8sErrors(e, task, spec);
                                } else if (spec.getEndpoint().getEndPointType().equals(EndpointType.DOCKER)) {
                                    handleDockerErrors(e, task, spec);
                                }
                            } catch (Exception e) {
                                log.error(e);
                            }
                        }
                    }
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("Exception while sleeping polling thread:", e.getMessage());
                    }
                } catch (CassandraConnectionFailureException e) {
                    //when connection fails,this creates to many logs.so keeping it as part of debug
                    log.debug("Database connection failed: ", e.getMessage());
                } catch (Exception e) {
                    log.error("Exception in polling thread:", e);
                }
            }
        };
        threadService.submit(pollingThread);
    }

    @SuppressWarnings("unchecked")
    private void handleSocketError(Exception e, Task<CommandExecutionFaultSpec> task, CommandExecutionFaultSpec spec)
            throws MangleException {
        if (spec.getFaultName().equals(FaultName.KERNELPANICFAULT.getValue())) {
            updateTaskInfo((T) task, TaskStatus.COMPLETED, task.getTaskFailureReason(), 100);
            taskService.updateRemediationFieldofTaskById(task.getId(), true);
        } else if ((getTimeInMilliseconds(task.getTriggers().peek().getStartTime()) + spec.getTimeoutInMilliseconds()
                + DEFAULT_RECOVERY_TIME) > System.currentTimeMillis()) {
            updateTaskInfo((T) task, TaskStatus.TEST_MACHINE_INVALID_STATE,
                    "Machine is unreachable now.Will Poll the endpoint until fault timeout happens", 50);
        } else {
            updateTaskInfo((T) task, TaskStatus.TEST_ENDPOINT_UNKNOWN_STATE,
                    "Endpoint is unreachable even after time out and recovery time.", 100);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleDockerErrors(Exception e, Task<CommandExecutionFaultSpec> task, CommandExecutionFaultSpec spec) {
        if (e.getMessage().contains(KnownFailureConstants.DOCKER_CONTAINER_NOT_AVAILABLE_FAILURE_OUTPUT)) {
            if (getTimeInMilliseconds(task.getTriggers().peek().getStartTime()) + spec.getTimeoutInMilliseconds()
                    + DEFAULT_RECOVERY_TIME > System.currentTimeMillis()) {
                updateTaskInfo((T) task, TaskStatus.TEST_MACHINE_INVALID_STATE,
                        "Container is unreachable now.Will Poll the container until fault timeout happens", 50);
            } else {
                updateTaskInfo((T) task, TaskStatus.TEST_ENDPOINT_UNKNOWN_STATE,
                        "Container is unreachable even after the fault time out", 50);
            }
        } else if (e.getMessage().contains("No such container")) {
            updateTaskInfo((T) task, TaskStatus.TEST_ENDPOINT_UNKNOWN_STATE,
                    "Container where injection happened is not available or deleted to check the status", 100);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleK8sErrors(Exception e, Task<CommandExecutionFaultSpec> task, CommandExecutionFaultSpec spec) {
        if (e.getMessage().contains(ErrorConstants.K8S_CONTAINER_NOT_FOUND)) {
            if (getTimeInMilliseconds(task.getTriggers().peek().getStartTime()) + spec.getTimeoutInMilliseconds()
                    + DEFAULT_RECOVERY_TIME > System.currentTimeMillis()) {
                updateTaskInfo((T) task, TaskStatus.TEST_MACHINE_INVALID_STATE,
                        "Container is unreachable now.Will Poll the endpoint until fault timeout happens", 50);
            } else {
                updateTaskInfo((T) task, TaskStatus.TEST_ENDPOINT_UNKNOWN_STATE,
                        "Container is unreachable even after the fault time out", 50);
            }
        } else if (e.getMessage().contains(ErrorConstants.K8S_RESOURCE_NOT_FOUND)) {
            updateTaskInfo((T) task, TaskStatus.TEST_ENDPOINT_UNKNOWN_STATE,
                    "Pod where injection happened is not available to check the status", 50);
        }
    }

    public boolean isThreadalive() {
        return null != thread && thread.isAlive() ? true : false;
    }


    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void updateTaskInfo(T task, TaskStatus taskStatus, String taskFailureReason, double percentageCompleted) {
        TaskTrigger trigger = task.getTriggers().peek();
        if (taskStatus == TaskStatus.IN_PROGRESS || taskStatus == TaskStatus.TASK_SKIPPED) {
            trigger.setStartTime(new Date(System.currentTimeMillis()).toGMTString());
        }
        if (taskStatus == TaskStatus.FAILED || taskStatus == TaskStatus.COMPLETED
                || taskStatus == TaskStatus.TASK_SKIPPED) {
            trigger.setEndTime(new Date(System.currentTimeMillis()).toGMTString());
        }
        trigger.setTaskStatus(taskStatus);
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setPercentageCompleted(percentageCompleted);
        taskInfo.setTaskStatus(taskStatus);
        trigger.setMangleTaskInfo(taskInfo);
        trigger.setTaskFailureReason(taskFailureReason);
        log.info("Updating Task. Current task status: " + taskStatus);
        try {
            taskService.addOrUpdateTask(task);
        } catch (MangleException e) {
            log.error(customErrorMessage.getErrorMessage(e), e);
        }
        publisher.publishEvent(new TaskModifiedEvent<T>(task));
        if (task.getTaskStatus() == TaskStatus.FAILED || task.getTaskStatus() == TaskStatus.COMPLETED) {
            publisher.publishEvent(new TaskCompletedEvent(task));
        }
    }


    private long getTimeInMilliseconds(String date) {
        DateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date today = new Date();
        try {
            today = df.parse(date);
        } catch (ParseException e) {
            log.error(e);
        }
        return today.getTime();
    }

    @PreDestroy
    public void destroy() {
        log.info("Stopping the polling service thread instance");
        this.stopThread = true;
        this.threadService.shutdownNow();
    }

}