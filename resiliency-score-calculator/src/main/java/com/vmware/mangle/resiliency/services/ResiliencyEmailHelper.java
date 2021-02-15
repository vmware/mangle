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

package com.vmware.mangle.resiliency.services;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.vmware.mangle.client.restclient.WavefrontMetricProviderHelper;
import com.vmware.mangle.exception.MangleException;
import com.vmware.mangle.resiliency.commons.ResiliencyConstants;
import com.vmware.mangle.resiliency.score.email.ResiliencyScoreEmail;
import com.vmware.mangle.resiliency.score.utils.ReadProperty;

/**
 * @author chetanc
 *
 *
 */
@Log4j2
public class ResiliencyEmailHelper {

    private Map<String, String> configuration;
    private WavefrontMetricProviderHelper metricProviderHelper;

    public void sendResiliencyScoreEmail() throws MangleException {
        this.configuration = readConfiguration();
        this.metricProviderHelper = getWavefrontMetricProviderHelper(configuration.get("wavefront.url"),
                configuration.get("wavefront.api.token"));
        String emailTo = configuration.get("score.email.to").trim();
        int emailDays = Integer.parseInt(configuration.get("score.email.days").trim());
        String serviceEmailTemplate = configuration.get("service.email.template").trim();
        String resiliencyScoreImages = configuration.get("resiliency.score.images").trim();
        String[] serviceFamilies = configuration.get("service.families").trim().split(",");
        String resiliencyScoreMetrics = configuration.get("resiliency.score.metrics").trim();
        String berserkerApiMetrics = configuration.get("berserker.api.metrics").trim();
        ResiliencyScoreEmail resiliencyScoreEmail = new ResiliencyScoreEmail(metricProviderHelper, emailDays,
                serviceEmailTemplate, resiliencyScoreImages, resiliencyScoreMetrics, berserkerApiMetrics);
        resiliencyScoreEmail.sendResiliencyScoreEmail(serviceFamilies, emailTo);
    }

    private WavefrontMetricProviderHelper getWavefrontMetricProviderHelper(String wavefrontUrl,
            String wavefrontApiToken) {
        return new WavefrontMetricProviderHelper(wavefrontUrl, wavefrontApiToken);
    }

    private static Map<String, String> readConfiguration() throws MangleException {
        log.info("Reading the configuration for resiliency calculator");
        String propertyFile = System.getProperty(ResiliencyConstants.PROPERTY_FILE);
        Map<String, String> configuration = new HashMap<>();
        if (!StringUtils.hasText(propertyFile)) {
            throw new MangleException("Configuration properties file for sending email is not provided");
        } else {
            ReadProperty.readPropertiesAsMap(configuration, propertyFile);
            if (CollectionUtils.isEmpty(configuration)) {
                throw new MangleException(
                        "Properties could not be found in the provided property file: " + propertyFile);
            }
            return configuration;
        }
    }

}
