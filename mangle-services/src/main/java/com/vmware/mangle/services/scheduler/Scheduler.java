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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.scheduler.SchedulerSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.model.enums.SchedulerJobType;
import com.vmware.mangle.model.enums.SchedulerStatus;
import com.vmware.mangle.services.SchedulerService;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.config.SchedulerConfig;
import com.vmware.mangle.services.deletionutils.SchedulerDeletionService;
import com.vmware.mangle.services.deletionutils.TaskDeletionService;
import com.vmware.mangle.services.events.schedule.ScheduleCreatedEvent;
import com.vmware.mangle.services.events.schedule.ScheduleUpdatedEvent;
import com.vmware.mangle.services.tasks.executor.TaskExecutor;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Class to Intialize the Scheduler and helpers method to start the jobs
 *
 * @author bkaranam
 * @author ashrimali
 * @author jayasankarr
 */

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
@Log4j2
public class Scheduler {

    @Autowired
    private TaskService taskService;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    private TaskExecutor<Task<? extends TaskSpec>> concurrentTaskRunner;

    @Autowired
    private TaskDeletionService taskDeletionService;

    @Autowired
    private SchedulerDeletionService schedulerDeletionService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private Map<String, ScheduledFuture<?>> scheduledJobs;
    private ThreadPoolTaskScheduler taskScheduler;

    public static final String PAUSE_SCHEDULE = "PAUSE";
    public static final String CANCEL_SCHEDULE = "CANCEL";
    public static final String DELETE_SCHEDULE = "DELETE";
    public static final String DELETE_SCHEDULE_AND_TASKS = "DELETE_SCHEDULE_AND_TASKS";

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
                    } catch (MangleException e) {
                        log.error(e);
                    } catch (InterruptedException e) {
                        log.error(e.getMessage());
                        Thread.currentThread().interrupt();
                    }
                }, new CronTrigger(cronExpression));
        if (null != future) {
            log.trace("Successfully created cron based schedule for the job {}", task.getId());
            task.setScheduledTask(true);
            this.scheduledJobs.put(task.getId(), future);
            taskService.addOrUpdateTask(task);
            addOrUpdateScheduleJobStatus(task.getId(), SchedulerJobType.CRON, null, cronExpression,
                    task.getTaskData().getSchedule().getDescription());
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
                    } catch (MangleException e) {
                        log.error(e);
                    } catch (InterruptedException e) {
                        log.error(e.getMessage());
                        Thread.currentThread().interrupt();
                    }
                }, new Date(timeInMilliseconds));

        if (null != future) {
            log.trace("Successfully created simple schedule for the job {}", task.getId());
            task.setScheduledTask(true);
            taskService.addOrUpdateTask(task);
            this.scheduledJobs.put(task.getId(), future);
            addOrUpdateScheduleJobStatus(task.getId(), SchedulerJobType.SIMPLE, timeInMilliseconds, null,
                    task.getTaskData().getSchedule().getDescription());
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
    public Set<String> cancelScheduledJobs(List<String> jobIds) throws MangleException {
        log.trace("Processing request to cancel the schedule for the jobs: {}", jobIds.toString());
        Set<String> scheduleIds = verifyJobIds(jobIds);
        for (String scheduleId : scheduleIds) {
            eventPublisher.publishEvent(new ScheduleUpdatedEvent(scheduleId, CANCEL_SCHEDULE));
        }
        return scheduleIds;
    }


    /**
     * Method to pause scheduled job using list of job ids
     *
     * @param jobIds
     * @return
     * @throws MangleException
     */
    public Set<String> pauseScheduledJobs(List<String> jobIds) throws MangleException {
        log.trace("Processing request to pause the schedule for the jobs: {}", jobIds.toString());
        Set<String> scheduleIds = verifyJobIds(jobIds);
        for (String scheduleId : scheduleIds) {
            eventPublisher.publishEvent(new ScheduleUpdatedEvent(scheduleId, PAUSE_SCHEDULE));
        }
        return scheduleIds;
    }


    /**
     * Method to get all the scheduled jobs
     *
     * @param jobIds
     * @return
     */
    public Set<String> deleteScheduledJobs(List<String> jobIds, boolean deleteAssociatedTasks) throws MangleException {
        log.trace("Processing request to delete the schedules for the jobs: {}", jobIds.toString());
        Set<SchedulerSpec> schedulerSpecSet = schedulerService.getSchedulesForIds(jobIds);
        Set<String> scheduleIds = schedulerSpecSet.stream().map(SchedulerSpec::getId).collect(Collectors.toSet());

        verifyJobsExists(jobIds, scheduleIds);

        List<String> inActiveSchedules = schedulerSpecSet.stream()
                .filter(schedulerSpec -> schedulerSpec.getStatus() != SchedulerStatus.SCHEDULED)
                .map(SchedulerSpec::getId).collect(Collectors.toList());
        List<String> activeSchedules = schedulerSpecSet.stream()
                .filter(schedulerSpec -> schedulerSpec.getStatus() == SchedulerStatus.SCHEDULED)
                .map(SchedulerSpec::getId).collect(Collectors.toList());

        log.trace("Deleting all the in-active schedules");
        schedulerDeletionService.deleteSchedulerDetailsByJobIds(inActiveSchedules, deleteAssociatedTasks);
        log.trace("Submitting active schedule jobs {} for deletion", activeSchedules.toString());
        String deletionOperationMode = DELETE_SCHEDULE;
        if (deleteAssociatedTasks) {
            deletionOperationMode = DELETE_SCHEDULE_AND_TASKS;
        }
        for (String scheduleId : activeSchedules) {
            eventPublisher.publishEvent(new ScheduleUpdatedEvent(scheduleId, deletionOperationMode));
        }
        return scheduleIds;
    }


    /**
     * Method to resume/reschedule paused job using list of job ids
     *
     * @param jobIds
     * @return
     * @throws MangleException
     */
    public Set<String> resumeJobs(List<String> jobIds) throws MangleException {
        log.trace("Processing request to resume the schedule for the jobs: {}", jobIds.toString());
        Set<SchedulerSpec> schedulerSpecSet = schedulerService.getSchedulesForIds(jobIds);
        Set<String> scheduleIds = schedulerSpecSet.stream().map(SchedulerSpec::getId).collect(Collectors.toSet());

        verifyJobsExists(jobIds, scheduleIds);
        verifyJobsInSchedule(schedulerSpecSet, SchedulerStatus.PAUSED);
        for (String jobId : scheduleIds) {
            SchedulerSpec schedulerDao = schedulerService.getScheduledJobByIdandStatus(jobId, SchedulerStatus.PAUSED);
            this.rescheduleJob(schedulerDao);
        }

        return scheduleIds;
    }

    private Set<String> verifyJobIds(List<String> jobIds) throws MangleException {
        Set<SchedulerSpec> schedulerSpecSet = schedulerService.getSchedulesForIds(jobIds);
        Set<String> scheduleIds = schedulerSpecSet.stream().map(SchedulerSpec::getId).collect(Collectors.toSet());

        verifyJobsExists(jobIds, scheduleIds);
        verifyJobsInSchedule(schedulerSpecSet, SchedulerStatus.SCHEDULED);

        return scheduleIds;
    }

    private void verifyJobsExists(List<String> jobIds, Set<String> persistedJobIds) throws MangleException {
        jobIds.removeAll(persistedJobIds);

        if (!CollectionUtils.isEmpty(jobIds)) {
            throw new MangleException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.SCHEDULE, jobIds);
        }
    }

    private void verifyJobsInSchedule(Set<SchedulerSpec> schedules, SchedulerStatus status) throws MangleException {
        Set<String> inActiveSchedules = schedules.stream().filter(schedulerSpec -> schedulerSpec.getStatus() != status)
                .map(SchedulerSpec::getId).collect(Collectors.toSet());
        if (!CollectionUtils.isEmpty(inActiveSchedules)) {
            throw new MangleException(ErrorCode.INVALID_STATE_SCHEDULED_JOBIDS, inActiveSchedules, status);
        }
    }

    public boolean isTaskAlreadyScheduled(String jobId) {
        return scheduledJobs.containsKey(jobId);
    }


    /**
     * This method is used by the mangle to sync the schedules across the multi-node setup
     *
     * @param jobId
     *            job id of the schedule that needs to be removed
     * @param status
     * @return
     */
    public boolean removeScheduleFromCurrentNode(String jobId, String status) {
        boolean isOperationSuccessful = false;
        if (PAUSE_SCHEDULE.equals(status)) {
            log.info("Pausing the schedule {}", jobId);
            isOperationSuccessful = cancelAndUpdateJobsMap(jobId, SchedulerStatus.PAUSED);
        } else if (CANCEL_SCHEDULE.equals(status)) {
            log.info("Cancelling the schedule {}", jobId);
            isOperationSuccessful = cancelAndUpdateJobsMap(jobId, SchedulerStatus.CANCELLED);
        } else if (DELETE_SCHEDULE.equals(status) || DELETE_SCHEDULE_AND_TASKS.equals(status)) {
            log.info("Deleting the schedule {}", jobId);
            isOperationSuccessful = cancelAndUpdateJobsMap(jobId, SchedulerStatus.CANCELLED);
            if (isOperationSuccessful) {
                try {
                    schedulerDeletionService.deleteSchedulerDetailsByJobId(jobId);
                    if (DELETE_SCHEDULE_AND_TASKS.equals(status)) {
                        taskDeletionService.deleteTaskById(jobId);
                    }
                } catch (MangleException e) {
                    log.error("Deletion of the job entry failed with the exception: {}", jobId);
                }
            }
        }
        return isOperationSuccessful;
    }

    /**
     * Cancel the job in the current node This is usually triggered when the partition to which the
     * schedule belongs is migrated to the other node
     *
     * @param jobId
     * @return
     */
    public boolean removeScheduleFromCurrentNode(String jobId) {
        log.info("Removing schedule for the job {}", jobId);
        ScheduledFuture<?> scheduledJob = this.scheduledJobs.get(jobId);
        boolean cancelledStatus = false;
        if (scheduledJob != null) {
            cancelledStatus = scheduledJob.cancel(true);
            if (cancelledStatus) {
                this.scheduledJobs.remove(jobId);
                log.debug("Successfully removed the scheduled job {}", jobId);
            }
        }
        return cancelledStatus;
    }

    public void removeAllSchedulesFromCurrentNode() {
        List<String> scheduledJobIds = new ArrayList<>(this.scheduledJobs.keySet());
        for (String jobId : scheduledJobIds) {
            removeScheduleFromCurrentNode(jobId);
            eventPublisher.publishEvent(new ScheduleUpdatedEvent(jobId, SchedulerStatus.CANCELLED.name()));
        }
    }

    /**
     * Method to cancel all scheduled jobs
     *
     * @return
     * @throws MangleException
     */
    public Set<String> cancelAllScheduledJobs() throws MangleException {
        return cancelScheduledJobs(new ArrayList<>(scheduledJobs.keySet()));
    }

    private boolean cancelAndUpdateJobsMap(String jobID, SchedulerStatus status) {
        ScheduledFuture<?> scheduledJob = this.scheduledJobs.get(jobID);
        boolean cancelledStatus = false;
        if (scheduledJob != null) {
            cancelledStatus = scheduledJob.cancel(true);
            if (cancelledStatus) {
                schedulerService.updateSchedulerStatus(jobID, status);
                this.scheduledJobs.remove(jobID);
                eventPublisher.publishEvent(new ScheduleUpdatedEvent(jobID, status.name()));
            }
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
    private void rescheduleJob(SchedulerSpec scheduledJob) {
        schedulerService.updateSchedulerStatus(scheduledJob.getId(), SchedulerStatus.INITIALIZING);
        eventPublisher.publishEvent(new ScheduleCreatedEvent(scheduledJob.getId(), SchedulerStatus.INITIALIZING));
    }

    private SchedulerSpec addOrUpdateScheduleJobStatus(String jobId, SchedulerJobType jobType, Long scheduledTime,
            String cronExpression, String description) {
        SchedulerSpec schedulerDAO = new SchedulerSpec();
        schedulerDAO.setId(jobId);
        schedulerDAO.setJobType(jobType);
        schedulerDAO.setScheduledTime(scheduledTime);
        schedulerDAO.setCronExpression(cronExpression);
        schedulerDAO.setStatus(SchedulerStatus.SCHEDULED);
        schedulerDAO.setDescription(description);
        SchedulerSpec persistedSpec = schedulerService.addOrUpdateSchedulerDetails(schedulerDAO);
        eventPublisher.publishEvent(new ScheduleUpdatedEvent(jobId, persistedSpec.getStatus().name()));
        return persistedSpec;
    }

    public SchedulerSpec getScheduledJob(String taskId) {
        return schedulerService.getSchedulerDetailsById(taskId);
    }
}
