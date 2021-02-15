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

package com.vmware.mangle.services.tasks.executor;

import static com.vmware.mangle.utils.constants.URLConstants.MANGLE_CURRENT_STATUS_MESSAGE;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.faults.specs.MultiTaskSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.FaultTask;
import com.vmware.mangle.cassandra.model.tasks.FaultTriggeringTask;
import com.vmware.mangle.cassandra.model.tasks.RemediableTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskInfo;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.enums.SchedulerJobType;
import com.vmware.mangle.model.enums.SchedulerStatus;
import com.vmware.mangle.services.PluginService;
import com.vmware.mangle.services.SchedulerService;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.enums.MangleNodeStatus;
import com.vmware.mangle.services.events.task.TaskCompletedEvent;
import com.vmware.mangle.services.events.task.TaskModifiedEvent;
import com.vmware.mangle.services.helpers.FaultInjectionHelper;
import com.vmware.mangle.services.scheduler.Scheduler;
import com.vmware.mangle.task.framework.helpers.AbstractTaskHelper;
import com.vmware.mangle.task.framework.skeletons.ITaskHelper;
import com.vmware.mangle.task.framework.skeletons.TaskRunner;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.KnownFailureConstants;
import com.vmware.mangle.utils.constants.URLConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleTaskException;
import com.vmware.mangle.utils.exceptions.handler.CustomErrorMessage;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;
import com.vmware.mangle.utils.messages.tasks.TaskExecutionMessages;

/**
 * The {@link TaskRunner} that uses the Executor-Framework of J2SE to run tasks in parallel.
 *
 * @param <T>
 *            The concrete {@link AbstractTaskHelper}-Type
 * @author hkilari
 * @since 5.0.0
 */

@Component
@Log4j2
public class TaskExecutor<T extends Task<? extends TaskSpec>> implements TaskRunner<T> {

    private final Map<String, T> runningTasks;
    private final ExecutorService threadPool;
    private final Lock runningTasksLock = new ReentrantLock();
    private final Condition taskDoneCondition = runningTasksLock.newCondition();
    private final Condition taskStartCondition = runningTasksLock.newCondition();


    @Autowired
    private TaskService taskService;

    @Autowired
    private CustomErrorMessage customErrorMessage;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private PluginService pluginService;

    @Autowired
    private FaultInjectionHelper faultInjectionHelper;

    private String node;

    /**
     * Constructor.
     */
    public TaskExecutor() {
        threadPool = Executors.newCachedThreadPool();
        runningTasks = new ConcurrentHashMap<>();
    }

    public Task<? extends TaskSpec> submitTask(final T task) throws MangleException {
        Object taskData = task.getTaskData();
        if ((TaskType.INJECTION.equals(task.getTaskType())
                || task.getTaskName().startsWith(Constants.NODESTATUS_TASK_NAME))
                && TaskSpec.class.isAssignableFrom(task.getTaskData().getClass())
                && null != ((TaskSpec) taskData).getSchedule()) {
            return scheduleTask(task);
        } else {
            log.info(String.format(TaskExecutionMessages.TASK_EXECUTION_START_MESSAGE, task.getTaskName()));
            try {
                execute(task);
            } catch (InterruptedException e) {
                log.error(e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                throw new MangleException(e, ErrorCode.TASK_EXECUTION_FAILED, task.getId(), task);
            }
            return task;
        }
    }

    private Task<? extends TaskSpec> scheduleTask(final T task) {
        try {
            TaskSpec taskData = task.getTaskData();
            if (null != taskData.getSchedule().getCronExpression()) {
                scheduler.scheduleCronTask(task, taskData.getSchedule().getCronExpression());
            } else {
                scheduler.scheduleSimpleTask(task, taskData.getSchedule().getTimeInMilliseconds());
            }
            return taskService.getTaskById(task.getId());
        } catch (MangleException e) {
            log.error("Scheduler Failed to schedule the Task. Reason: " + e.getMessage(), e);
            task.setTaskDescription("Scheduling Task failed with exception: " + e.getMessage());
            try {
                taskService.addOrUpdateTask(task);
            } catch (MangleException e1) {
                log.error("Failed to Update Task. Reason: ", e);
            }
            return task;
        }
    }

    @Override
    public void execute(final T task) throws InterruptedException, MangleException {
        if (CollectionUtils.isEmpty(task.getTriggers()) || (task.getTaskStatus() != TaskStatus.IN_PROGRESS
                && task.getTaskStatus() != TaskStatus.INITIALIZING)) {
            if (task.getTriggers() == null) {
                task.setTriggers(new Stack<>());
            }
            task.getTriggers().add(new TaskTrigger());
        }

        if (!validateNodeStatus(task)) {
            updateTaskInfo(task, TaskStatus.TASK_SKIPPED,
                    MANGLE_CURRENT_STATUS_MESSAGE + URLConstants.getMangleNodeCurrentStatus(), 0);
            return;
        }
        if (!task.isInitialized()) {
            throw new MangleTaskException(ErrorCode.TASK_NOT_INTIALIAZED, task);
        }
        runningTasksLock.lock();
        try {
            ITaskHelper<TaskSpec> itask = pluginService.getExtension(task.getExtensionName());
            itask.setEventPublisher(publisher);
            final Runnable runnable = () -> runTask(itask, task);
            runningTasks.put(task.getId(), task);
            threadPool.execute(runnable);
            taskStartCondition.await();
            if (task instanceof FaultTriggeringTask && task.getTaskData() instanceof MultiTaskSpec) {
                handleChildTasks((FaultTriggeringTask) task);
            }
        } finally {
            runningTasksLock.unlock();
        }
    }

    private void handleChildTasks(FaultTriggeringTask task) {
        final Runnable runnable = () -> {
            log.info("Waiting for Child Tasks Initialization...");
            //Wait for data of child Tasks gets prepared within 6 mins
            //Condition 1: Not ready for Child Execution
            //Condition 2: The Object Map should not be Empty
            //Condition 3: The TaskStatus should not be IN_PROGRESS
            LocalDateTime startTime = LocalDateTime.now();
            while ((!((MultiTaskSpec) task.getTaskData()).isReadyForChildExecution()
                    || CollectionUtils.isEmpty(task.getTaskObjmap())) && task.getTaskStatus() == TaskStatus.IN_PROGRESS
                    && Duration.between(startTime, LocalDateTime.now()).getSeconds() <= 360) {
                CommonUtils.delayInSecondsWithDebugLog(1);
            }

            triggerChildTasks(task);
            cleanUpTask(task);
        };
        threadPool.execute(runnable);
    }

    private void triggerChildTasks(FaultTriggeringTask task) {
        task.getTaskObjmap().values().forEach(childTask -> {
            try {
                log.info("Submitting Child Task {} for Execution...", ((FaultTask) childTask).getId());
                faultInjectionHelper.saveTask((Task<? extends TaskSpec>) childTask);
            } catch (MangleException e) {
                log.error("", e);
            }
        });
    }

    private void cleanUpTask(FaultTriggeringTask task) {
        task.getTaskObjmap().clear();
        ((MultiTaskSpec) task.getTaskData()).setReadyForChildExecution(false);
        try {
            taskService.addOrUpdateTask(task);
        } catch (MangleException e) {
            log.error(customErrorMessage.getErrorMessage(e), e);
        }
    }

    private void runTask(final ITaskHelper itask, T task) {
        Thread.currentThread().setName(task.getTaskName());
        try {
            runningTasksLock.lock();
            taskStartCondition.signal();
        } finally {
            runningTasksLock.unlock();
        }
        try {
            TaskTrigger trigger = task.getTriggers().peek();
            trigger.setNode(node);
            updateTaskInfo(task, TaskStatus.IN_PROGRESS, trigger.getTaskFailureReason(), 0);
            itask.run(task);
        } catch (final MangleException e) {
            String msg = new StringBuilder("ErrorCode : ").append(e.getErrorCode().getCode())
                    .append(", ErrorMessage : ").append(customErrorMessage.getErrorMessage(e)).toString();
            log.error(TaskExecutionMessages.TASK_EXECUTION_FAILED_MESSAGE + msg, e);
            updateTaskInfo(task, TaskStatus.FAILED, msg, 100);
        } catch (final Exception e) {
            log.error(TaskExecutionMessages.TASK_EXECUTION_FAILED_MESSAGE, e);
            updateTaskInfo(task, TaskStatus.FAILED, e.getMessage(), 100);
        } finally {
            task.updateTaskDescription(itask.getDescription(task));
            complete(task);
            if (task.getTaskStatus() == TaskStatus.COMPLETED || task.getTaskStatus() == TaskStatus.FAILED
                    || task.getTaskStatus() == TaskStatus.INJECTED) {
                publisher.publishEvent(new TaskCompletedEvent(task));
            }
        }
    }

    @Override
    public void cancel(final T task) throws MangleTaskException {
        runningTasksLock.lock();
        if ((this.runningTasks.get(task.getId()) == null) && task.getTaskStatus() != TaskStatus.COMPLETED) {
            throw new MangleTaskException(ErrorCode.TASK_NOT_BELONGS_TO_RUNNER);
        } else if (task.getTaskStatus() == TaskStatus.IN_PROGRESS) {
            task.setTaskStatus(TaskStatus.CANCELING);
            pluginService.getExtension(task.getExtensionName()).cancel();
        }
        runningTasksLock.unlock();
    }

    @Override
    public boolean isExecuting(final T task) {
        runningTasksLock.lock();
        try {
            return task.getTaskStatus() == TaskStatus.IN_PROGRESS || task.getTaskStatus() == TaskStatus.CANCELING;
        } finally {
            runningTasksLock.unlock();
        }
    }

    @Override
    public boolean isComplete(final T task) {
        runningTasksLock.lock();
        try {
            return task.getTaskStatus() == TaskStatus.COMPLETED;
        } finally {
            runningTasksLock.unlock();
        }
    }

    @Override
    public boolean hasStarted(final T task) {
        runningTasksLock.lock();
        try {
            return task.getTaskStatus() != TaskStatus.INITIALIZING;
        } finally {
            runningTasksLock.unlock();
        }
    }

    @Override
    public int getNumberOfExecutingTasks() {
        try {
            runningTasksLock.lock();
            return getRunningTasks().size();
        } finally {
            runningTasksLock.unlock();
        }
    }

    @Override
    public void join() throws InterruptedException, MangleTaskException {
        HashSet<T> set = null;
        try {
            runningTasksLock.lock();
            set = new HashSet<>(getRunningTasks().values());
        } finally {
            runningTasksLock.unlock();
        }
        join(set);
    }

    @Override
    public void join(final Iterable<T> tasks) throws InterruptedException, MangleTaskException {
        for (final T task : tasks) {
            join(task);
        }
    }

    @Override
    public void join(final T task) throws InterruptedException, MangleTaskException {
        if (task.getTaskStatus() == TaskStatus.INITIALIZING) {
            throw new MangleTaskException(ErrorCode.TASK_NOT_BEEN_STARTED, task);
        }
        while (task.getTaskStatus() != TaskStatus.COMPLETED) {
            try {
                runningTasksLock.lock();
                if (task.getTaskStatus() != TaskStatus.COMPLETED) {
                    taskDoneCondition.await();
                }
            } finally {
                runningTasksLock.unlock();
            }
        }
    }

    /**
     * Package-Private method which is called when a task has completed it's execution.
     *
     * @param task
     *            the task that completed execution
     *
     */
    private void complete(final T task) {
        runningTasksLock.lock();
        try {
            TaskTrigger trigger = task.getTriggers().peek();
            if (trigger.getTaskStatus() == TaskStatus.IN_PROGRESS) {
                if (task.getTaskType() == TaskType.INJECTION && !(task instanceof FaultTriggeringTask)
                        && (task.getExtensionName().equals(
                                "com.vmware.mangle.faults.plugin.tasks.helpers.SystemResourceFaultTaskHelper2"))) {
                    updateTaskInfo(task, TaskStatus.INJECTED, null, 0);
                } else {
                    updateTaskInfo(task, TaskStatus.COMPLETED, null, 100);
                }
            } else {
                updateTaskInfo(task, trigger.getTaskStatus(), trigger.getTaskFailureReason(), 100);
            }
            if (task.getTaskType() == TaskType.REMEDIATION && (trigger.getTaskStatus() == TaskStatus.COMPLETED
                    || (trigger.getTaskStatus() == TaskStatus.FAILED && trigger.getTaskFailureReason()
                            .contains(KnownFailureConstants.FAULT_ALREADY_REMEDIATED)))) {
                if (!(task.getExtensionName()
                        .equals("com.vmware.mangle.faults.plugin.tasks.helpers.SystemResourceFaultTaskHelper2"))) {
                    updateRemediationFieldOfTask(task);
                    updateTaskStatusFieldOfTask(task, TaskStatus.COMPLETED);
                }
            }

            if (task.isScheduledTask()
                    && SchedulerJobType.CRON != schedulerService.getSchedulerDetailsById(task.getId()).getJobType()) {
                schedulerService.updateSchedulerStatus(task.getId(), SchedulerStatus.FINISHED);
            }

            if (!runningTasks.remove(task.getId(), task)) {
                log.fatal(customErrorMessage.getErrorMessage(ErrorCode.TASK_REMOVAL_FAILED.getCode()), task.getId());
            }
        } finally {
            runningTasksLock.unlock();
        }
        runningTasksLock.lock();
        try {
            taskDoneCondition.signalAll();
        } finally {
            runningTasksLock.unlock();
        }
    }

    private void updateRemediationFieldOfTask(T task) {
        try {
            taskService.updateRemediationFieldofTaskById(((RemediableTask) task).getInjectionTaskId(), true);
        } catch (MangleException e) {
            log.error(customErrorMessage.getErrorMessage(e), e);
        }
    }

    private void updateTaskStatusFieldOfTask(T task, TaskStatus status) {
        try {
            taskService.updateTaskStatusFieldOfTaskById(((RemediableTask) task).getInjectionTaskId(), status);
        } catch (MangleException e) {
            log.error(customErrorMessage.getErrorMessage(e), e);
        }
    }

    @SuppressWarnings("deprecation")
    protected void updateTaskInfo(T task, TaskStatus taskStatus, String taskFailureReason, double percentageCompleted) {

        if (taskStatus == TaskStatus.IN_PROGRESS || taskStatus == TaskStatus.TASK_SKIPPED) {
            task.getTriggers().peek().setStartTime(new Date(System.currentTimeMillis()).toGMTString());
        }
        if (taskStatus == TaskStatus.FAILED || taskStatus == TaskStatus.COMPLETED
                || taskStatus == TaskStatus.TASK_SKIPPED) {
            task.getTriggers().peek().setEndTime(new Date(System.currentTimeMillis()).toGMTString());
            if (task instanceof RemediableTask && !(task instanceof FaultTriggeringTask)) {
                ((RemediableTask) task).setRemediated(false);
            }
        }
        task.getTriggers().peek().setTaskStatus(taskStatus);
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setPercentageCompleted(percentageCompleted);
        taskInfo.setTaskStatus(taskStatus);
        task.getTriggers().peek().setMangleTaskInfo(taskInfo);
        task.getTriggers().peek().setTaskFailureReason(taskFailureReason);
        log.info("Updating Task. Current task status: " + taskStatus);
        try {
            taskService.addOrUpdateTask(task);
        } catch (MangleException e) {
            log.error(customErrorMessage.getErrorMessage(e), e);
        }
        publisher.publishEvent(new TaskModifiedEvent<T>(task));
    }

    public Map<String, T> getRunningTasks() {
        return runningTasks;
    }

    @Override
    public void dispose() throws MangleTaskException, InterruptedException {
        join();
        threadPool.shutdown();
    }

    private boolean validateNodeStatus(T task) {
        return !((URLConstants.getMangleNodeCurrentStatus().equals(MangleNodeStatus.PAUSE)
                || URLConstants.getMangleNodeCurrentStatus().equals(MangleNodeStatus.MAINTENANCE_MODE))
                && !task.getTaskName().startsWith(Constants.NODESTATUS_TASK_NAME));
    }

    public void setNode(String node) {
        this.node = node;
    }
}
