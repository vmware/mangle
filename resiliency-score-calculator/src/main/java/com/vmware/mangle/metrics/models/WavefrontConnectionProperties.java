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

package com.vmware.mangle.metrics.models;

import java.util.HashMap;
import java.util.List;

import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * @author Chethan C(chetanc)
 */
@Data
public class WavefrontConnectionProperties {
    private String wavefrontUrl;
    private String wavefrontApiToken;
    private String wavefrontProxyUrl;
    private String wavefrontProxyPort;
    private String metricName;
    private String wavefrontMetricSource;
    private HashMap<String, String> tags;
    private List<Service> services;
    private int testReferenceWindow;
    private int resiliencyCalculationWindow;
    private String outputMetricName;
    private String outputUrlMetricName;
    private String functionScoreMetricName;
    private String nonFunctionalScoreMetricName;
    private String metricQueryGranularity;

    public String getWavefrontUrl() {
        String systemWavefrontUrl = System.getProperty("wavefrontUrl");
        return StringUtils.isEmpty(systemWavefrontUrl) ? wavefrontUrl : systemWavefrontUrl;
    }

    public String getWavefrontApiToken() {
        String systemWavefrontApiToken = System.getProperty("wavefrontApiToken");
        return StringUtils.isEmpty(systemWavefrontApiToken) ? wavefrontApiToken : systemWavefrontApiToken;
    }

    public String getWavefrontProxyUrl() {
        String systemWavefrontProxyUrl = System.getProperty("wavefrontProxyUrl");
        return StringUtils.isEmpty(systemWavefrontProxyUrl) ? wavefrontProxyUrl : systemWavefrontProxyUrl;
    }

    public String getWavefrontProxyPort() {
        String systemWavefrontProxyPort = System.getProperty("wavefrontProxyPort");
        return StringUtils.isEmpty(systemWavefrontProxyPort) ? wavefrontProxyPort : systemWavefrontProxyPort;
    }

    public String getMetricName() {
        String systemWavefrontApiToken = System.getProperty("metricName");
        return StringUtils.isEmpty(systemWavefrontApiToken) ? metricName : systemWavefrontApiToken;
    }

    public String getWavefrontMetricSource() {
        String systemMetricName = System.getProperty("wavefrontMetricSource");
        return StringUtils.isEmpty(systemMetricName) ? wavefrontProxyPort : systemMetricName;
    }

    public int getTestReferenceWindow() {
        String systemTestReferenceWindow = System.getProperty("testReferenceWindow");
        return StringUtils.isEmpty(systemTestReferenceWindow) ? testReferenceWindow : Integer.parseInt(systemTestReferenceWindow);
    }

    public int getResiliencyCalculationWindow() {
        String systemResiliencyCalculationWindow = System.getProperty("resiliencyCalculationWindow");
        return StringUtils.isEmpty(systemResiliencyCalculationWindow) ? resiliencyCalculationWindow : Integer.parseInt(systemResiliencyCalculationWindow);
    }

    public String getOutputMetricName() {
        String systemOutputMetricName = System.getProperty("outputMetricName");
        return StringUtils.isEmpty(systemOutputMetricName) ? outputMetricName : systemOutputMetricName;
    }

    public String getOutputUrlMetricName() {
        String systemOutputUrlMetricName = System.getProperty("outputUrlMetricName");
        return StringUtils.isEmpty(systemOutputUrlMetricName) ? outputUrlMetricName : systemOutputUrlMetricName;
    }

    public String getFunctionScoreMetricName() {
        String systemFunctionScoreMetricName = System.getProperty("functionScoreMetricName");
        return StringUtils.isEmpty(systemFunctionScoreMetricName) ? functionScoreMetricName : systemFunctionScoreMetricName;
    }

    public String getNonFunctionalScoreMetricName() {
        String systemNonFunctionalScoreMetricName = System.getProperty("nonFunctionalScoreMetricName");
        return StringUtils.isEmpty(systemNonFunctionalScoreMetricName) ? nonFunctionalScoreMetricName : systemNonFunctionalScoreMetricName;
    }

    public String getMetricQueryGranularity() {
        String systemMetricQueryGranularity = System.getProperty("metricQueryGranularity");
        return StringUtils.isEmpty(systemMetricQueryGranularity) ? metricQueryGranularity : systemMetricQueryGranularity;
    }
}