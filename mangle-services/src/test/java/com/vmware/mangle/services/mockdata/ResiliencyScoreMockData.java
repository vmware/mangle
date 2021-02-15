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

package com.vmware.mangle.services.mockdata;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.resiliencyscore.FaultEventResiliencyScore;
import com.vmware.mangle.cassandra.model.resiliencyscore.QueryDto;
import com.vmware.mangle.cassandra.model.resiliencyscore.QueryResiliencyScore;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreConfigSpec;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreMetricConfig;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreTask;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreTaskTrigger;
import com.vmware.mangle.cassandra.model.resiliencyscore.Service;
import com.vmware.mangle.cassandra.model.resiliencyscore.ServiceResiliencyScore;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.ResiliencyScoreVO;
import com.vmware.mangle.model.resiliencyscore.ResiliencyScoreProperties;

/**
 * @author dbhat
 */

public class ResiliencyScoreMockData {

    private ResiliencyScoreMockData() {

    }

    public static ResiliencyScoreProperties getResiliencyScoreProperties() {
        ResiliencyScoreProperties resiliencyScorePropertiesMockData = new ResiliencyScoreProperties();
        MetricProviderMockData metricProviderMockData = new MetricProviderMockData();
        resiliencyScorePropertiesMockData.setMetricProviderSpec(metricProviderMockData.metricProviderWavefront());
        resiliencyScorePropertiesMockData.setResiliencyScoreMetricConfig(getResiliencyScoreMetricConfig());
        resiliencyScorePropertiesMockData.setTags(new HashMap<>());
        resiliencyScorePropertiesMockData.setTaskId(getRandomUUID());
        return resiliencyScorePropertiesMockData;
    }

    public static ResiliencyScoreMetricConfig getResiliencyScoreMetricConfig() {
        ResiliencyScoreMetricConfig metricConfig = new ResiliencyScoreMetricConfig();
        metricConfig.setMetricName(MockDataConstants.RESILIENCY_SCORE_METRIC_CONFIG_METRIC_NAME);
        metricConfig.setName(MockDataConstants.MANGLE);
        metricConfig.setMetricQueryGranularity(MockDataConstants.RESILIENCY_SCORE_METRIC_CONFIG_GRANULARITY);
        metricConfig.setMetricSource(MockDataConstants.SOURCE);
        metricConfig
                .setResiliencyCalculationWindow(MockDataConstants.RESILIENCY_SCORE_METRIC_CONFIG_TEST_REFERENCE_WINDOW);
        metricConfig.setTestReferenceWindow(MockDataConstants.RESILIENCY_SCORE_METRIC_CONFIG_CALCULATION_WINDOW);
        return metricConfig;
    }

    public static String getRandomUUID() {
        return UUID.randomUUID().toString();
    }

    public static Service getServiceProperties() {
        Service service = new Service();
        List<String> queries = new ArrayList<>();
        queries.add(getQueryProperties().getName());
        service.setQueryNames(queries);
        service.setName(MockDataConstants.SERVICE_NAME);
        service.setId(getRandomUUID());
        service.setLastUpdatedTime(System.currentTimeMillis());
        return service;
    }

    public static ResiliencyScoreConfigSpec getResiliencyScoreConfigSpec() {
        ResiliencyScoreConfigSpec spec = new ResiliencyScoreConfigSpec();
        spec.setServiceName(MockDataConstants.SERVICE_NAME);
        return spec;
    }

    public static ResiliencyScoreTask getResiliencyScoreTask1() {
        ResiliencyScoreTask resiliencyScoreTask = new ResiliencyScoreTask();
        Stack<ResiliencyScoreTaskTrigger> taskTriggers = new Stack<>();
        taskTriggers.add(getResiliencyScoreTaskTrigger());
        resiliencyScoreTask.setTaskName(MockDataConstants.TASK_NAME);
        resiliencyScoreTask.setTaskDescription(MockDataConstants.TASK_DESCRIPTION);
        resiliencyScoreTask.setTriggers(taskTriggers);
        resiliencyScoreTask.setTaskStatus(TaskStatus.COMPLETED);
        resiliencyScoreTask.setId(getRandomUUID());
        resiliencyScoreTask.setServiceName(MockDataConstants.SERVICE_NAME);
        resiliencyScoreTask.setTaskData(getResiliencyScoreConfigSpec());
        resiliencyScoreTask.setLastUpdated(System.currentTimeMillis());
        resiliencyScoreTask.setTaskType(TaskType.RESILIENCY_SCORE);
        return resiliencyScoreTask;
    }

    public static ResiliencyScoreTask getResiliencyScoreTask2() {
        ResiliencyScoreTask resiliencyScoreTask = new ResiliencyScoreTask();
        Stack<ResiliencyScoreTaskTrigger> taskTriggers = new Stack<>();
        taskTriggers.add(getResiliencyScoreTaskTrigger());
        resiliencyScoreTask.setTaskName(MockDataConstants.TASK_NAME1);
        resiliencyScoreTask.setTaskDescription(MockDataConstants.TASK_DESCRIPTION1);
        resiliencyScoreTask.setTriggers(taskTriggers);
        resiliencyScoreTask.setTaskStatus(TaskStatus.COMPLETED);
        resiliencyScoreTask.setId(getRandomUUID());
        resiliencyScoreTask.setServiceName(MockDataConstants.SERVICE_NAME);
        resiliencyScoreTask.setTaskData(getResiliencyScoreConfigSpec());
        resiliencyScoreTask.setLastUpdated(System.currentTimeMillis());
        resiliencyScoreTask.setTaskType(TaskType.RESILIENCY_SCORE);
        return resiliencyScoreTask;
    }

    public static ResiliencyScoreVO getResiliencyScoreVO() {
        ResiliencyScoreVO resiliencyScoreVO = new ResiliencyScoreVO();
        resiliencyScoreVO.setResiliencyScore(1);
        resiliencyScoreVO.setMessage("");
        return resiliencyScoreVO;
    }

    public static QueryDto getQueryProperties() {
        QueryDto querySpec = new QueryDto();
        querySpec.setId(getRandomUUID());
        querySpec.setLastUpdatedTime(System.currentTimeMillis());
        querySpec.setQueryCondition(MockDataConstants.DUMMY_QUERY_1);
        querySpec.setWeight(MockDataConstants.WEIGHT_1);
        querySpec.setName(MockDataConstants.QUERY_NAME);
        return querySpec;
    }

    private static ResiliencyScoreTaskTrigger getResiliencyScoreTaskTrigger() {
        ResiliencyScoreTaskTrigger taskTrigger = new ResiliencyScoreTaskTrigger();
        taskTrigger.setTaskStatus(TaskStatus.COMPLETED);
        taskTrigger.setResiliencyScore(MockDataConstants.RESILIENCY_SCORE);
        taskTrigger.setResiliencyScoreDetails(getServiceResiliencyScore());
        taskTrigger.setServiceId(getRandomUUID());
        taskTrigger.setStartTime(new Date().toString());
        taskTrigger.setTaskFailureReason(MockDataConstants.STATUS_MESSAGE);
        return taskTrigger;
    }

    private static ServiceResiliencyScore getServiceResiliencyScore() {
        ServiceResiliencyScore serviceResiliencyScore = new ServiceResiliencyScore();
        serviceResiliencyScore.setServiceName(MockDataConstants.SERVICE_NAME);
        List<FaultEventResiliencyScore> faultEventResiliencyScores = new ArrayList<>();
        faultEventResiliencyScores.add(getFaultEventResiliencyScore());
        serviceResiliencyScore.setFaultInjectionEventResiliencyScore(faultEventResiliencyScores);
        return serviceResiliencyScore;
    }

    private static FaultEventResiliencyScore getFaultEventResiliencyScore() {
        FaultEventResiliencyScore faultEventResiliencyScore = new FaultEventResiliencyScore();
        faultEventResiliencyScore.setFaultInjectionEventName(MockDataConstants.FAULT_EVENT_NAME);
        List<QueryResiliencyScore> queryResiliencyScores = new ArrayList<>();
        queryResiliencyScores.add(getQueryResiliencyScore());
        faultEventResiliencyScore.setQueryResiliencyScore(queryResiliencyScores);
        return faultEventResiliencyScore;
    }

    private static QueryResiliencyScore getQueryResiliencyScore() {
        QueryResiliencyScore queryResiliencyScore = new QueryResiliencyScore();
        queryResiliencyScore.setQueryName(MockDataConstants.DUMMY_QUERY_1);
        List<Double> scores = new ArrayList<>();
        scores.add(0.99);
        queryResiliencyScore.setResiliencyScore(scores);
        return queryResiliencyScore;
    }

    public static Task<? extends TaskSpec> getTask() {
        Task task = new Task<ResiliencyScoreConfigSpec>();
        task.setId(getRandomUUID());
        task.setTaskData(new ResiliencyScoreConfigSpec());
        task.setScheduledTask(false);
        task.setTaskType(TaskType.RESILIENCY_SCORE);
        task.setTriggers(new Stack<>());
        return task;
    }

}
