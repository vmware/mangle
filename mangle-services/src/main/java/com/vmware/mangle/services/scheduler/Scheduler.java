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

package com.vmware.mangle.services.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.scheduler.ScheduledTaskStatus;
import com.vmware.mangle.cassandra.model.scheduler.SchedulerSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.model.enums.OperationStatus;
import com.vmware.mangle.model.enums.SchedulerJobType;
import com.vmware.mangle.model.enums.SchedulerStatus;
import com.vmware.mangle.model.response.DeleteSchedulerResponse;
import com.vmware.mangle.services.SchedulerService;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.config.SchedulerConfig;
import com.vmware.mangle.services.deletionutils.SchedulerDeletionService;
import com.vmware.mangle.services.deletionutils.TaskDeletionService;
import com.vmware.mangle.services.events.schedule.ScheduleCreatedEvent;
import com.vmware.mangle.services.events.schedule.ScheduleUpdatedEvent;
import com.vmware.mangle.services.tasks.executor.TaskExecutor;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Class to Intialize the Scheduler and helpers method to start the jobs
 *
 * @author bkaranam
 * @author ashrimali
 */

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
@Log4j2
public class Scheduler {

    @Autowired
    private TaskService taskService;

    @Autowired
    SchedulerService schedulerService;

    @Autowired
    TaskExecutor<Task<? extends TaskSpec>> concurrentTaskRunner;

    @Autowired
    private TaskDeletionService taskDeletionService;

    @Autowired
    private SchedulerDeletionService schedulerDeletionService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private Map<String, ScheduledFuture<?>> scheduledJobs;
    private ThreadPoolTaskScheduler taskScheduler;

    private static final String JOB_CANCELLED_MESSAGE = "Cancelled Successfully";
    private static final String JOB_PAUSED_MESSAGE = "Paused Successfully";
    private static final String JOB_RESUMED_MESSAGE = "Resumed Successfully";
    private static final String JOB_NOTSCHEDULED_MESSAGE = "job is not scheduled";
    private static final String JOB_NOTPAUSED_MESSAGE = "job is not in paused state to resume";

    private Scheduler() {
        log.info("Initializing Mangle Scheduler...");
        getScheduler();
        log.info("Initialized Mangle Scheduler...");
    }

    private void getScheduler() {
        @SuppressWarnings("resource")
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(SchedulerConfig.class);
        this.taskScheduler = (ThreadPoolTaskScheduler) applicationContext.getBean("Scheduler");
        this.scheduledJobs = new HashMap<>();
    }

    /**
     * Method to schedule task with cron expression
     *
     * @param task
     * @param cronExpression
     * @return
     */
    @SuppressWarnings("unchecked")
    public ScheduledFuture<Task<TaskSpec>> scheduleCronTask(Task<?> task, String cronExpression)
            throws MangleException {
        ScheduledFuture<? extends Task<? extends TaskSpec>> future =
                (ScheduledFuture<? extends Task<? extends TaskSpec>>) this.taskScheduler.schedule(() -> {
                    try {
                        concurrentTaskRunner.execute(task);
                    } catch (MangleException | InterruptedException e) {
                        log.error(e.getMessage());
                    }
                }, new CronTrigger(cronExpression));
        if (null != future) {
            task.setScheduledTask(true);
            this.scheduledJobs.put(task.getId(), future);
            taskService.addOrUpdateTask(task);
            addOrUpdateScheduleJobStatus(task.getId(), SchedulerJobType.CRON, null, cronExpression);
            return (ScheduledFuture<Task<TaskSpec>>) future;
        } else {
            schedulerDeletionService.deleteSchedulerDetailsByJobId(task.getId());
            taskDeletionService.deleteTaskById(task.getId());
            throw new MangleException(ErrorCode.CRON_JOB_SCHEDULE_FAILURE, cronExpression);
        }
    }

    /**
     * Method to schedule task with Simpletask which takes time in milliseconds as input
     *
     * @param task
     * @param timeInMilliseconds
     * @return ScheduledFuture
     */
    @SuppressWarnings("unchecked")
    public ScheduledFuture<Task<TaskSpec>> scheduleSimpleTask(Task<? extends TaskSpec> task, Long timeInMilliseconds)
            throws MangleException {
        ScheduledFuture<? extends Task<? extends TaskSpec>> future =
                (ScheduledFuture<? extends Task<? extends TaskSpec>>) this.taskScheduler.schedule(() -> {
                    try {
                        concurrentTaskRunner.execute(task);
                    } catch (MangleException | InterruptedException e) {
                        log.error(e.getMessage());
                    }
                }, new Date(timeInMilliseconds));
        if (null != future) {
            task.setScheduledTask(true);
            taskService.addOrUpdateTask(task);
            this.scheduledJobs.put(task.getId(), future);
            addOrUpdateScheduleJobStatus(task.getId(), SchedulerJobType.SIMPLE, timeInMilliseconds, null);
            return (ScheduledFuture<Task<TaskSpec>>) future;
        } else {
            schedulerDeletionService.deleteSchedulerDetailsByJobId(task.getId());
            taskDeletionService.deleteTaskById(task.getId());
            throw new MangleException(ErrorCode.SIMPLE_JOB_SCHEDULE_FAILURE, new Date(timeInMilliseconds).toString());
        }
    }

    /**
     * Method to cancel scheduled job using list of job ids
     *
     * @param jobIds
     * @return
     * @throws MangleException
     */
    public Map<String, ScheduledTaskStatus> cancelScheduledJobs(List<String> jobIds) {
        Map<String, ScheduledTaskStatus> statusMap = new HashMap<>();
        ScheduledTaskStatus scheduledTaskStatus;
        for (String jobId : jobIds) {
            scheduledTaskStatus = new ScheduledTaskStatus();
            SchedulerSpec schedulerDao =
                    schedulerService.getScheduledJobByIdandStatus(jobId, SchedulerStatus.SCHEDULED);
            if (null != schedulerDao) {
                try {
                    this.cancelScheduledJob(schedulerDao.getId(), SchedulerStatus.CANCELLED);
                    scheduledTaskStatus.setStatus(SchedulerStatus.CANCELLED);
                    scheduledTaskStatus.setMessage(JOB_CANCELLED_MESSAGE);
                    statusMap.put(jobId, scheduledTaskStatus);
                } catch (MangleException exception) {
                    scheduledTaskStatus.setStatus(SchedulerStatus.CANCELLED);
                    scheduledTaskStatus.setMessage(exception.getMessage());
                    statusMap.put(jobId, scheduledTaskStatus);
                }
            } else {
                scheduledTaskStatus.setStatus(SchedulerStatus.CANCELLED);
                scheduledTaskStatus.setMessage(JOB_NOTSCHEDULED_MESSAGE);
                statusMap.put(jobId, scheduledTaskStatus);
            }
        }
        return statusMap;
    }

    /**
     * Method to pause scheduled job using list of job ids
     *
     * @param jobIds
     * @return
     * @throws MangleException
     */
    public Map<String, ScheduledTaskStatus> pauseScheduledJobs(List<String> jobIds) {
        Map<String, ScheduledTaskStatus> statusMap = new HashMap<>();
        ScheduledTaskStatus scheduledTaskStatus;
        for (String jobId : jobIds) {
            scheduledTaskStatus = new ScheduledTaskStatus();
            SchedulerSpec schedulerDao =
                    schedulerService.getScheduledJobByIdandStatus(jobId, SchedulerStatus.SCHEDULED);
            if (null != schedulerDao) {
                try {
                    this.cancelScheduledJob(schedulerDao.getId(), SchedulerStatus.PAUSED);
                    scheduledTaskStatus.setStatus(SchedulerStatus.PAUSED);
                    scheduledTaskStatus.setMessage(JOB_PAUSED_MESSAGE);
                    statusMap.put(jobId, scheduledTaskStatus);
                } catch (MangleException exception) {
                    scheduledTaskStatus.setStatus(SchedulerStatus.PAUSE_FAILED);
                    scheduledTaskStatus.setMessage(exception.getMessage());
                    statusMap.put(jobId, scheduledTaskStatus);
                }
            } else {
                scheduledTaskStatus.setStatus(SchedulerStatus.PAUSE_FAILED);
                scheduledTaskStatus.setMessage(JOB_NOTSCHEDULED_MESSAGE);
                statusMap.put(jobId, scheduledTaskStatus);
            }
        }
        return statusMap;
    }

    /**
     * Method to resume/reschedule paused job using list of job ids
     *
     * @param jobIds
     * @return
     * @throws MangleException
     */
    public Map<String, ScheduledTaskStatus> resumeJobs(List<String> jobIds) {
        Map<String, ScheduledTaskStatus> statusMap = new HashMap<>();
        ScheduledTaskStatus scheduledTaskStatus;
        for (String jobId : jobIds) {
            scheduledTaskStatus = new ScheduledTaskStatus();
            SchedulerSpec schedulerDao = schedulerService.getScheduledJobByIdandStatus(jobId, SchedulerStatus.PAUSED);
            if (null != schedulerDao) {
                try {

                    this.rescheduleJob(schedulerDao);
                    scheduledTaskStatus.setStatus(SchedulerStatus.SCHEDULED);
                    scheduledTaskStatus.setMessage(JOB_RESUMED_MESSAGE);
                    statusMap.put(jobId, scheduledTaskStatus);
                } catch (MangleException exception) {
                    scheduledTaskStatus.setStatus(SchedulerStatus.RESUME_FAILED);
                    scheduledTaskStatus.setMessage(exception.getMessage());
                    statusMap.put(jobId, scheduledTaskStatus);
                }
            } else {
                scheduledTaskStatus.setStatus(SchedulerStatus.RESUME_FAILED);
                scheduledTaskStatus.setMessage(JOB_NOTPAUSED_MESSAGE);
                statusMap.put(jobId, scheduledTaskStatus);
            }
        }
        return statusMap;
    }

    /**
     * Method to cancel the scheduled job
     *
     * @param jobId
     * @return
     * @throws MangleException
     */

    public boolean cancelScheduledJob(String jobId, SchedulerStatus status) throws MangleException {
        if (null != this.scheduledJobs.get(jobId)) {
            return cancelAndUpdateJobsMap(jobId, status);
        }
        throw new MangleException(ErrorCode.JOB_NOT_ACTIVE, jobId);
    }

    /**
     * Method to cancel all scheduled jobs
     *
     * @return
     * @throws MangleException
     */
    public Map<String, ScheduledTaskStatus> cancelAllScheduledJobs() throws MangleException {
        if (null == scheduledJobs.keySet() || scheduledJobs.keySet().isEmpty()) {
            throw new MangleException(ErrorCode.NO_ACTIVE_JOBS);
        }
        return cancelScheduledJobs(new ArrayList<String>(scheduledJobs.keySet()));
    }

    private boolean cancelAndUpdateJobsMap(String jobID, SchedulerStatus status) {
        boolean cancelledStatus = this.scheduledJobs.get(jobID).cancel(true);
        if (cancelledStatus) {
            schedulerService.updateSchedulerStatus(jobID, status);
            this.scheduledJobs.remove(jobID);
            eventPublisher.publishEvent(new ScheduleUpdatedEvent(jobID, status));
        }
        return cancelledStatus;
    }

    /**
     * Method to get all the scheduled jobs
     *
     * @return
     */
    public List<SchedulerSpec> getAllScheduledJobs() {
        return schedulerService.getAllSchedulerDetails();
    }

    /**
     * Method to get all the scheduled jobs
     *
     * @param taskIds
     * @return
     */
    public Map<String, DeleteSchedulerResponse> deleteScheduledJobs(List<String> taskIds, boolean deleteTask)
            throws MangleException {
        Map<String, DeleteSchedulerResponse> resultMap = new HashMap<>();
        List<String> deletedTasks = new ArrayList<>();
        String message;
        for (String taskId : taskIds) {
            if (!StringUtils.isEmpty(taskId)) {
                SchedulerSpec job = getScheduledJob(taskId);
                if (job != null && job.getStatus() != SchedulerStatus.SCHEDULED) {
                    resultMap.put(taskId, new DeleteSchedulerResponse(deletScheduledJob(Arrays.asList(taskId)), null));
                    deletedTasks.add(taskId);
                } else {
                    message = "Failed to Delete Job with ID: " + taskId + ". Please verify Job Status";
                    log.trace("Failed to delete schedule job with id {}", taskId);
                    resultMap.put(taskId, new DeleteSchedulerResponse(OperationStatus.FAILED, message));
                }
            } else {
                message = "Failed to Delete Job with ID: " + taskId;
                resultMap.put(taskId, new DeleteSchedulerResponse(OperationStatus.FAILED, message));
            }
        }

        if (deleteTask && !CollectionUtils.isEmpty(deletedTasks)) {
            taskDeletionService.deleteTasksByIds(deletedTasks);
        }

        return resultMap;
    }


    public OperationStatus deletScheduledJob(List<String> listOfIds) {
        return schedulerDeletionService.deleteSchedulerDetailsByJobIds(listOfIds);
    }

    /**
     * Method to get all the scheduled jobs with status
     *
     * @return
     */

    public List<SchedulerSpec> getAllScheduledJobs(SchedulerStatus status) {
        return schedulerService.getAllScheduledJobByStatus(status);
    }

    /**
     * Method to get the list of active schedules
     *
     * @return list of active schedules
     */
    public List<String> getActiveScheduleJobs() {
        return schedulerService.getActiveScheduleJobs();
    }

    /**
     * Method to shutdown the Scheduler
     */
    public void shutdownScheduler() {
        log.info("Closing Scheduler");
        if (null != taskScheduler) {
            taskScheduler.shutdown();
        }
        log.info("Closed Scheduler");
    }

    /**
     * Method to reschedule a Job
     *
     * @param scheduledJob
     * @throws MangleException
     */
    private void rescheduleJob(SchedulerSpec scheduledJob) throws MangleException {
        try {
            Task<TaskSpec> taskDTO = taskService.getTaskById(scheduledJob.getId());
            if (SchedulerJobType.CRON.equals(scheduledJob.getJobType())) {
                scheduleCronTask(taskDTO, scheduledJob.getCronExpression());
            }
            if (SchedulerJobType.SIMPLE.equals(scheduledJob.getJobType())
                    && new Date(System.currentTimeMillis()).before(new Date(scheduledJob.getScheduledTime()))) {
                scheduleSimpleTask(taskDTO, scheduledJob.getScheduledTime());
            }
        } catch (MangleException e) {
            String errorMessage = "Exception while triggering miss fired job by mangle scheduler" + e.getMessage();
            log.error(errorMessage);
            throw new MangleException(e, ErrorCode.MISFIRED_JOB_TRIGGER_FAILURE);
        }
    }

    private SchedulerSpec addOrUpdateScheduleJobStatus(String jobId, SchedulerJobType jobType, Long scheduledTime,
            String cronExpression) {
        SchedulerSpec schedulerDAO = new SchedulerSpec();
        schedulerDAO.setId(jobId);
        schedulerDAO.setJobType(jobType);
        schedulerDAO.setScheduledTime(scheduledTime);
        schedulerDAO.setCronExpression(cronExpression);
        schedulerDAO.setStatus(SchedulerStatus.SCHEDULED);
        SchedulerSpec persistedSpec = schedulerService.addOrUpdateSchedulerDetails(schedulerDAO);
        eventPublisher.publishEvent(new ScheduleCreatedEvent(jobId, persistedSpec.getStatus()));
        return persistedSpec;
    }

    public SchedulerSpec getScheduledJob(String taskId) {
        return schedulerService.getSchedulerDetailsById(taskId);
    }
}
