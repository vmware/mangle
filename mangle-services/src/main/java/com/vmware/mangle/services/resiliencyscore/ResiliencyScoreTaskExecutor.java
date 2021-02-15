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

package com.vmware.mangle.services.resiliencyscore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.resiliencyscore.QueryDto;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreTask;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreTaskTrigger;
import com.vmware.mangle.cassandra.model.resiliencyscore.Service;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.metric.reporter.common.Metric;
import com.vmware.mangle.model.ResiliencyScoreVO;
import com.vmware.mangle.model.resiliencyscore.ResiliencyScoreProperties;
import com.vmware.mangle.services.constants.CommonConstants;
import com.vmware.mangle.services.helpers.ResiliencyScoreHelper;
import com.vmware.mangle.services.scheduler.Scheduler;
import com.vmware.mangle.utils.ResiliencyScoreUtils;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.constants.ResiliencyConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;
import com.vmware.mangle.utils.helpers.metricprovider.IMetricProviderHelper;
import com.vmware.mangle.utils.helpers.metricprovider.MetricProviderFactory;


/**
 * @author dbhat
 */
@Component
@Log4j2
public class ResiliencyScoreTaskExecutor<T extends Task<? extends TaskSpec>> {
    private long startTime;
    private long endTime;
    @Setter
    private ResiliencyScoreProperties properties;
    @Getter
    private ResiliencyScoreTask resiliencyScoreTask;
    @Setter
    private Service service;
    private List<QueryDto> allQueries;

    @Autowired
    private ResiliencyScoreService resiliencyScoreService;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ResiliencyScoreHelper resiliencyScoreHelper;

    public void runTask(T task) {
        initData(task);
        if (!isDependentConfigsDefined()) {
            updateResiliencyScoreCalculatingFailure("", false);
            return;
        }
        service = resiliencyScoreService.getServiceByName(resiliencyScoreTask.getServiceName());
        if (null == service) {
            updateResiliencyScoreCalculatingFailure(ErrorConstants.INVALID + ErrorConstants.SERVICE, true);
            return;
        }
        allQueries = getQueries(service);
        if (CollectionUtils.isEmpty(allQueries)) {
            updateResiliencyScoreCalculatingFailure(ErrorConstants.INVALID + ErrorConstants.QUERY, true);
            return;
        }
        resiliencyScoreTask.currentTaskTrigger().setServiceId(service.getId());
        triggerCalculation();
    }

    private void updateResiliencyScoreCalculatingFailure(String failureReason, boolean setFailureReason) {
        if (setFailureReason) {
            resiliencyScoreTask.setStatusMessage(failureReason);
        }
        resiliencyScoreTask.setResiliencyScore(ResiliencyConstants.INVALID_SCORE);
        setResiliencyScoreTaskEndTime();
        updateResiliencyScoreTaskStatus(TaskStatus.FAILED);
    }

    public void submitTask(T task) {
        if (null != task.getTaskData().getSchedule()) {
            log.info("Scheduling the ResiliencyScore task with ID: " + task.getId());
            scheduleTask(task);
            return;
        }
        runTask(task);
    }

    private void initData(T task) {
        setEndTime();
        validateTask(task);
        this.properties = resiliencyScoreHelper.getResiliencyScoreTaskSpec();
        properties.setTaskId(task.getId());
        log.info(" Running Resiliency score calculation task: " + properties.getTaskId());
        setResiliencyScoreTask(task.getId());
        updateTaskTrigger();
        // Setting end Time as Resiliency score calculation begin time as the start time will be 1 hour (by default) if current time.
        updateResiliencyScoreStartTime(endTime);
    }

    private void triggerCalculation() {
        setStartTime();
        log.info("Calculating Resiliency score for the service: " + service.getName());
        ResiliencyScoreUtils resiliencyScoreUtils =
                new ResiliencyScoreUtils(properties, service, allQueries, startTime, endTime);
        updateResiliencyScoreTaskStatus(TaskStatus.IN_PROGRESS);

        ResiliencyScoreVO resiliencyScoreForService = resiliencyScoreUtils.calculateResiliencyScore();
        saveResiliencyScoreToDb(resiliencyScoreForService);
        sendResiliencyScoreMetric(resiliencyScoreForService);
    }

    private void saveResiliencyScoreToDb(ResiliencyScoreVO resiliencyScoreForService) {
        log.debug("Updating the resiliency score task : " + properties.getTaskId() + " with score: "
                + resiliencyScoreForService.getResiliencyScore());
        try {
            resiliencyScoreTask.setResiliencyScore(resiliencyScoreForService.getResiliencyScore());
            if (resiliencyScoreForService.getResiliencyScore() == ResiliencyConstants.INVALID_SCORE) {
                log.error(resiliencyScoreForService.getMessage());
                resiliencyScoreTask.setStatusMessage(resiliencyScoreForService.getMessage());
                resiliencyScoreTask.setTaskStatus(TaskStatus.FAILED);
            } else {
                resiliencyScoreTask.setTaskStatus(TaskStatus.COMPLETED);
                resiliencyScoreTask.setResiliencyScoreDetails(resiliencyScoreForService.getServiceResiliencyScore());
            }
            setResiliencyScoreTaskEndTime();
            resiliencyScoreService.addOrUpdateTask(resiliencyScoreTask);
        } catch (MangleException mangleException) {
            log.error(String.format(ErrorConstants.FAILED_TO_PERSIST_DATA, ErrorConstants.RESILIENCY_SCORE));
            log.error(mangleException.getMessage());
        }
    }

    private void updateResiliencyScoreTaskStatus(TaskStatus taskStatus) {
        log.debug("Updating the resiliency score task status to : " + taskStatus.name());
        resiliencyScoreTask.setTaskStatus(taskStatus);
        updateResiliencyScoreTaskState();
    }

    @SuppressWarnings("deprecation")
    private void updateResiliencyScoreStartTime(long startTime) {
        log.debug("Updating the resiliency score task with start resiliency score time as : " + startTime);
        resiliencyScoreTask.setStartTime(new Date(startTime).toGMTString());
        updateResiliencyScoreTaskState();
    }

    @SuppressWarnings("deprecation")
    private void setResiliencyScoreTaskEndTime() {
        resiliencyScoreTask.setEndTime(new Date(System.currentTimeMillis()).toGMTString());
    }

    private void updateResiliencyScoreTaskState() {
        log.debug("Updating the resiliency score task state: " + resiliencyScoreTask.toString());
        try {
            resiliencyScoreService.addOrUpdateTask(resiliencyScoreTask);
        } catch (MangleException mangleException) {
            log.error(mangleException.getMessage());
        }
    }

    private void updateTaskTrigger() {
        if (resiliencyScoreTask.getTriggers() == null) {
            resiliencyScoreTask.setTriggers(new Stack<>());
        }
        resiliencyScoreTask.getTriggers().add(new ResiliencyScoreTaskTrigger());
    }

    private void setResiliencyScoreTask(String taskId) {
        try {
            this.resiliencyScoreTask = resiliencyScoreService.getTaskById(taskId);
        } catch (MangleException mangleException) {
            log.error(mangleException.getMessage());
        }
    }

    /**
     * Initialises the start time and end time for calculating the resiliency score. End time will
     * be initialised to current time and start time will be initialised to 1 hour (as defined in
     * properties file) earlier.
     */
    private void setStartTime() {
        log.debug("Finding the calender start time and endtime to retrieve the events from Metric provider.");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -properties.getResiliencyScoreMetricConfig().getResiliencyCalculationWindow());
        this.startTime = calendar.getTimeInMillis();
        log.debug("Time window details - Start time: " + startTime + "  End Time: " + endTime);
    }

    private void setEndTime() {
        Calendar calendar = Calendar.getInstance();
        this.endTime = calendar.getTimeInMillis();
    }

    private boolean isDependentConfigsDefined() {
        if (null == properties.getMetricProviderSpec()) {
            log.error(ErrorConstants.NO_ACTIVE_METRIC_PROVIDER + ErrorConstants.RESILIENCY_SCORE_ERROR_MESSAGE);
            resiliencyScoreTask.setStatusMessage(
                    ErrorConstants.NO_ACTIVE_METRIC_PROVIDER + ErrorConstants.RESILIENCY_SCORE_ERROR_MESSAGE);
            return false;
        }
        if (null == properties.getResiliencyScoreMetricConfig()) {
            log.error(ErrorConstants.NO_RESILIENCY_METRIC_CONFIG_FOUND + ErrorConstants.RESILIENCY_SCORE_ERROR_MESSAGE);
            resiliencyScoreTask.setStatusMessage(
                    ErrorConstants.NO_RESILIENCY_METRIC_CONFIG_FOUND + ErrorConstants.RESILIENCY_SCORE_ERROR_MESSAGE);
            return false;
        }
        log.debug("All the dependent configurations are defined.");
        return true;
    }

    private void validateTask(T task) {
        if (null == task.getId()) {
            log.error(ErrorConstants.NO_RECORD_FOUND_MSG + " " + ErrorConstants.TASK_ID + task.getId());
            throw new MangleRuntimeException(ErrorCode.NO_TASK_FOUND, task.getId(),
                    ErrorConstants.RESILIENCY_SCORE_TASK_NOT_FOUND);
        }
    }

    public boolean sendResiliencyScoreMetric(ResiliencyScoreVO resiliencyScoreVO) {
        log.debug(
                "Sending the resiliency score metric : " + properties.getResiliencyScoreMetricConfig().getMetricName());
        if (resiliencyScoreVO.getResiliencyScore() != ResiliencyConstants.INVALID_SCORE) {
            IMetricProviderHelper metricProviderHelper =
                    MetricProviderFactory.getActiveMetricProvider(properties.getMetricProviderSpec());
            Map<String, String> tags = new HashMap<>();
            tags.put(CommonConstants.SERVICE_NAME, service.getName());
            if (!CollectionUtils.isEmpty(service.getTags())) {
                tags.putAll(service.getTags());
            }
            Metric metric = new Metric(properties.getResiliencyScoreMetricConfig().getMetricName(),
                    resiliencyScoreVO.getResiliencyScore(), endTime, tags, CommonConstants.MANGLE);
            metricProviderHelper.sendMetric(metric);
            return true;
        } else {
            log.error(ErrorConstants.INVALID_RESILIENCY_SCORE + ErrorConstants.RESILIENCY_SCORE_METRIC_IS_NOT_SENT);
            return false;
        }
    }

    private void scheduleTask(T task) {
        TaskSpec taskData = task.getTaskData();
        setResiliencyScoreTask(task.getId());
        try {
            if (null != taskData.getSchedule().getCronExpression()) {
                scheduler.scheduleCronTask(task, taskData.getSchedule().getCronExpression());
            } else {
                scheduler.scheduleSimpleTask(task, taskData.getSchedule().getTimeInMilliseconds());
            }
            log.debug("Updating the scheduling status of the task to Scheduled.");
            resiliencyScoreTask.setScheduledTask(true);
            updateResiliencyScoreTaskState();
        } catch (MangleException e) {
            log.error("Scheduler Failed to schedule the Task. Reason: " + e.getMessage(), e);
            resiliencyScoreTask.setStatusMessage(ErrorConstants.SCHEDULING_FAILED);
            updateResiliencyScoreTaskState();
        }
    }

    private List<QueryDto> getQueries(Service service) {
        log.debug("Retrieving the queries associated with service: " + service.getName());
        List<QueryDto> queries = new ArrayList<>();
        for (String query : service.getQueryNames()) {
            queries.add(resiliencyScoreService.getLastUpdatedValueOfQuery(query));
        }
        log.debug("all queries have been retrieved");
        return queries;
    }

}
