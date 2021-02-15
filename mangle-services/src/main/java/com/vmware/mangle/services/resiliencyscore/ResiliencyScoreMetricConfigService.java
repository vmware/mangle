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

import java.util.List;
import java.util.Optional;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreMetricConfig;
import com.vmware.mangle.services.repository.ResiliencyScoreMetricConfigRepository;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author dbhat
 */

@Component
@Log4j2
public class ResiliencyScoreMetricConfigService {

    @Autowired
    private ResiliencyScoreMetricConfigRepository metricConfigRepository;

    public ResiliencyScoreMetricConfig addMetricConfig(ResiliencyScoreMetricConfig metricConfig)
            throws MangleException {
        if (metricConfig != null) {
            List<ResiliencyScoreMetricConfig> metricConfigsInDb = metricConfigRepository.findAll();
            if (CollectionUtils.isEmpty(metricConfigsInDb)) {
                log.debug("Updating the Resiliency Score Metric configuration in Db");
                return metricConfigRepository.save(metricConfig);
            }
            log.error(ErrorConstants.RESILIENCY_SCORE_METRIC_CONFIG_NAME
                    + ErrorConstants.ONLY_ONE_ACTIVE_CONFIG_IS_ALLOWED);
            throw new MangleException(ErrorConstants.RESILIENCY_SCORE_METRIC_CONFIG + ErrorConstants.SAME_RECORD_EXISTS
                    + ErrorConstants.ONLY_ONE_ACTIVE_CONFIG_IS_ALLOWED, ErrorCode.SAME_RECORD_EXISTS);
        }
        log.error(ErrorConstants.RESILIENCY_SCORE_METRIC_CONFIG + ErrorConstants.FIELD_VALUE_EMPTY);
        throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.RESILIENCY_SCORE_METRIC_CONFIG);
    }

    public ResiliencyScoreMetricConfig updateMetricConfig(String metricConfigName,
            ResiliencyScoreMetricConfig metricConfig) throws MangleException {
        log.debug("Trying to update the resiliency score metric Configuration with Name: " + metricConfigName);
        if (!StringUtils.isEmpty(metricConfigName) && null != metricConfig) {
            log.debug(" Updating the Resiliency Score metric configuration in DB ");
            Optional<ResiliencyScoreMetricConfig> configInDb = metricConfigRepository.findByName(metricConfigName);
            if (!configInDb.isPresent()) {
                throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND,
                        ErrorConstants.RESILIENCY_SCORE_METRIC_CONFIG_NAME, metricConfigName);
            }
            return metricConfigRepository.save(metricConfig);
        }
        log.error(ErrorConstants.RESILIENCY_SCORE_METRIC_CONFIG_NAME + ErrorConstants.FIELD_VALUE_EMPTY);
        throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.RESILIENCY_SCORE_METRIC_CONFIG_NAME);
    }

    public boolean deleteMetricConfig(String metricConfigName) throws MangleException {
        log.debug("Deleting the Resiliency score metric config: " + metricConfigName);
        if (!StringUtils.isEmpty(metricConfigName)) {
            Optional<ResiliencyScoreMetricConfig> metricConfig = metricConfigRepository.findByName(metricConfigName);
            if (metricConfig.isPresent()) {
                metricConfigRepository.deleteByName(metricConfigName);
                Optional<ResiliencyScoreMetricConfig> verifyDeleting =
                        metricConfigRepository.findByName(metricConfigName);
                if (!verifyDeleting.isPresent()) {
                    return true;
                }
            } else {
                throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND,
                        ErrorConstants.RESILIENCY_SCORE_METRIC_CONFIG, metricConfigName);
            }
        } else {
            log.error(ErrorConstants.RESILIENCY_SCORE_METRIC_CONFIG_NAME + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.RESILIENCY_SCORE_METRIC_CONFIG_NAME);
        }
        return false;
    }

    public List<ResiliencyScoreMetricConfig> getAllResiliencyScoreMetricConfigs() {
        return metricConfigRepository.findAll();
    }

    public ResiliencyScoreMetricConfig getResiliencyScoreMetricConfig() {
        List<ResiliencyScoreMetricConfig> allConfigs = getAllResiliencyScoreMetricConfigs();
        if (CollectionUtils.isEmpty(allConfigs)) {
            log.error(ErrorConstants.RESILIENCY_SCORE_METRIC_CONFIG + ErrorConstants.NOT_FOUND);
            return new ResiliencyScoreMetricConfig();
        }
        return allConfigs.get(0);
    }

}
