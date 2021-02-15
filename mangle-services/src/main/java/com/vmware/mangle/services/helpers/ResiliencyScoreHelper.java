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

package com.vmware.mangle.services.helpers;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreConfigSpec;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreTask;
import com.vmware.mangle.cassandra.model.resiliencyscore.Service;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.resiliencyscore.ResiliencyScoreProperties;
import com.vmware.mangle.services.constants.CommonConstants;
import com.vmware.mangle.services.events.task.ResiliencyScoreTaskCreatedEvent;
import com.vmware.mangle.services.resiliencyscore.ResiliencyScoreMetricConfigService;
import com.vmware.mangle.services.resiliencyscore.ResiliencyScoreService;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;



/**
 * @author dbhat
 *
 */
@Component
@Log4j2
public class ResiliencyScoreHelper {

    @Autowired
    private ApplicationEventPublisher publisher;
    @Autowired
    private ResiliencyScoreService resiliencyScoreService;
    @Autowired
    private ResiliencyScoreMetricConfigService resiliencyScoreMetricConfigService;
    @Autowired
    private MetricProviderHelper metricProviderHelper;

    public ResiliencyScoreTask calculateResiliencyScore(ResiliencyScoreConfigSpec configSpec) throws MangleException {
        validateResiliencyScoreSpec(configSpec);
        Service service = resiliencyScoreService.getServiceByName(configSpec.getServiceName());
        validateServiceSpec(service);
        ResiliencyScoreTask task = getResiliencyScoreTask(configSpec);
        task.setTaskData(configSpec);
        resiliencyScoreService.addOrUpdateTask(task);

        publisher.publishEvent(new ResiliencyScoreTaskCreatedEvent(task));
        return task;
    }

    /**
     * Validating ResiliencyScore Calculating configurations for mandatory fields. Following are the
     * checks: 1. Atleast 1 service family must be defined. 2. Every Service family definition must
     * have at least 1 Service defined. 3. Query conditions cannot be empty for services.
     *
     * @param configSpec
     *            : Resiliency Score config spec having definition for ServiceFamilies, Services and
     *            Query Conditions.
     */
    private void validateResiliencyScoreSpec(ResiliencyScoreConfigSpec configSpec) throws MangleException {
        if (configSpec == null) {
            log.error(ErrorConstants.INVALID_RESILIENCY_SCORE_CONFIGURATION);
            throw new MangleException(ErrorConstants.INVALID_RESILIENCY_SCORE_CONFIGURATION,
                    ErrorCode.INVALID_RESILIENCY_SCORE_SPEC);
        }
        if (null == configSpec.getServiceName()) {
            log.error(ErrorConstants.INVALID_RESILIENCY_SCORE_CONFIGURATION + ErrorConstants.SERVICE_CANNOT_BE_EMPTY);
            throw new MangleException(ErrorConstants.SERVICE_CANNOT_BE_EMPTY, ErrorCode.SERVICES_CANNOT_BE_EMPTY);
        }
    }

    private void validateServiceSpec(Service service) throws MangleException {
        if (CollectionUtils.isEmpty(service.getQueryNames())) {
            log.error(ErrorConstants.INVALID_RESILIENCY_SCORE_CONFIGURATION
                    + ErrorConstants.QUERY_CONDITIONS_CANNOT_BE_EMPTY);
            throw new MangleException(ErrorConstants.QUERY_CONDITIONS_CANNOT_BE_EMPTY,
                    ErrorCode.QUERY_CONDITIONS_CANNOT_BE_EMPTY);
        }
    }

    public ResiliencyScoreTask getResiliencyScoreTask(ResiliencyScoreConfigSpec configSpec) {
        ResiliencyScoreTask task = new ResiliencyScoreTask();
        task.setServiceName(configSpec.getServiceName());
        task.setTaskType(TaskType.RESILIENCY_SCORE);
        task.setTaskName(CommonConstants.RESILIENCY_SCORE_CALCULATING + task.getId());
        task.setTaskDescription(CommonConstants.RESILIENCY_SCORE_CALCULATING_DESCRIPTION);
        return task;
    }

    public ResiliencyScoreProperties getResiliencyScoreTaskSpec() {
        ResiliencyScoreProperties taskSpec = new ResiliencyScoreProperties();
        taskSpec.setResiliencyScoreMetricConfig(resiliencyScoreMetricConfigService.getResiliencyScoreMetricConfig());
        taskSpec.setMetricProviderSpec(metricProviderHelper.getActiveMetricProvider());
        return taskSpec;
    }

}
