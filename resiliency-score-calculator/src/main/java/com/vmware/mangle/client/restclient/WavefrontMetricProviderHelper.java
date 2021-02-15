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

package com.vmware.mangle.client.restclient;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import com.vmware.mangle.metrics.models.MetricProviderHelper;
import com.vmware.mangle.metrics.models.Timeseries;
import com.vmware.mangle.metrics.models.WavefrontEventResponse;
import com.vmware.mangle.metrics.models.WavefrontMetricQueryResponse;
import com.vmware.mangle.resiliency.commons.ResiliencyConstants;

/**
 * @author ranjans
 *
 *         Endpoint client for WaveFront Server
 */
@Log4j2
public class WavefrontMetricProviderHelper extends RestTemplateWrapper implements MetricProviderHelper {

    private static final String QUERY_AND_OPERATOR = " and ";

    public WavefrontMetricProviderHelper(String wavefrontUrl, String wavefrontAPIToken) {
        setHeadersAndBaseUrl(wavefrontUrl, wavefrontAPIToken);
    }

    @Override
    public WavefrontEventResponse queryEvents(Map<String, String> tags, String serviceFamilyName, String serviceName,
            String startTime, String endTime) {

        log.info("Querying events for the service family {} between {} and {}", serviceFamilyName, startTime, endTime);
        String requestUrl = String.format(ResiliencyConstants.WAVEFRONT_QUERY_URL_MINUTE_GRANULARITY, startTime,
                endTime, getEventQueryFilterForTags(tags, serviceFamilyName, serviceName));
        log.info("Request url for the events for the serviceFamily {}: {}", serviceFamilyName, requestUrl);
        ResponseEntity<WavefrontEventResponse> object =
                (ResponseEntity<WavefrontEventResponse>) get(requestUrl, WavefrontEventResponse.class);
        return object == null ? null : object.getBody();
    }

    @Override
    public WavefrontMetricQueryResponse queryMetrics(String metricName, String serviceName, long startTime,
            long endTime, String queryFilterString, String granularity) {
        log.info("querying metrics for the service {} between {} and {}", serviceName, startTime, endTime);
        @SuppressWarnings("unchecked")
        ResponseEntity<WavefrontMetricQueryResponse> object = (ResponseEntity<WavefrontMetricQueryResponse>) get(
                String.format(ResiliencyConstants.WAVEFRONT_METRIC_QUERY_URL, granularity, startTime, endTime,
                        getMetricQueryFilterForTags(metricName, serviceName, queryFilterString)),
                WavefrontMetricQueryResponse.class);
        return object == null ? null : object.getBody();
    }

    public WavefrontMetricQueryResponse queryMetrics(String query, long startTime, long endTime, String granularity) {
        log.info("Running query for the time between {} and {}", startTime, endTime);
        @SuppressWarnings("unchecked")
        ResponseEntity<WavefrontMetricQueryResponse> queryResponse = (ResponseEntity<WavefrontMetricQueryResponse>) get(
                String.format(ResiliencyConstants.WAVEFRONT_METRIC_QUERY_URL, granularity, startTime, endTime, query),
                WavefrontMetricQueryResponse.class);
        return queryResponse == null ? null : queryResponse.getBody();
    }

    public Map<String, Double> queryMetricsCountGroupByUrl(String metricName, String serviceName, long startTime,
            long endTime, String granularity) {
        log.info("querying total metrics count for the service {} between {} and {}", serviceName, startTime, endTime);
        WavefrontMetricQueryResponse metricQueryResponse = queryMetrics(metricName, serviceName, startTime, endTime,
                ResiliencyConstants.METRIC_QUERY_TOTAL_FILTER_GROUP_BY_URL, granularity);
        return extractApiToCountMap(metricQueryResponse);
    }

    public Map<String, Double> queryMetricsSuccessCountGroupByUrl(String metricName, String serviceName, long startTime,
            long endTime, String granularity) {
        log.info("querying total success metrics count for the service {} between {} and {}", serviceName, startTime,
                endTime);
        WavefrontMetricQueryResponse metricQueryResponse = queryMetrics(metricName, serviceName, startTime, endTime,
                ResiliencyConstants.METRIC_QUERY_SUCCESS_FILTER_GROUP_BY_URL, granularity);
        return extractApiToCountMap(metricQueryResponse);
    }

    public Map<String, Double> queryMetricsCountGroupByUrl(String metricName, String wavefrontMetricSource,
            String serviceName, long startTime, long endTime, String granularity) {
        log.info("querying total metrics count for the service {} between {} and {}", serviceName, startTime, endTime);
        WavefrontMetricQueryResponse metricQueryResponse =
                queryMetricsWithSource(metricName, wavefrontMetricSource, serviceName, startTime, endTime,
                        ResiliencyConstants.METRIC_QUERY_TOTAL_FILTER_GROUP_BY_URL_SOURCE, granularity);
        return extractApiToCountMap(metricQueryResponse);
    }

    public Map<String, Double> queryMetricsSuccessCountGroupByUrl(String metricName, String wavefrontMetricSource,
            String serviceName, Long startTime, Long endTime, String granularity) {
        WavefrontMetricQueryResponse metricQueryResponse =
                queryMetricsWithSource(metricName, wavefrontMetricSource, serviceName, startTime, endTime,
                        ResiliencyConstants.METRIC_QUERY_SUCCESS_FILTER_GROUP_BY_URL_SOURCE, granularity);
        return extractApiToCountMap(metricQueryResponse);
    }

    public WavefrontMetricQueryResponse queryMetricsWithSource(String metricName, String wavefrontMetricSource,
            String serviceName, long startTime, long endTime, String queryFilterString, String granularity) {
        log.info("querying metrics for the service {} between {} and {}", serviceName, startTime, endTime);
        @SuppressWarnings("unchecked")
        ResponseEntity<WavefrontMetricQueryResponse> object = (ResponseEntity<WavefrontMetricQueryResponse>) get(
                String.format(ResiliencyConstants.WAVEFRONT_METRIC_QUERY_URL, granularity, startTime, endTime,
                        getMetricQueryFilterForTags(metricName, wavefrontMetricSource, serviceName, queryFilterString)),
                WavefrontMetricQueryResponse.class);
        return object == null ? null : object.getBody();
    }

    private String getMetricQueryFilterForTags(String metricName, String wavefrontMetricSource, String serviceName,
            String queryFilterString) {
        return String.format(queryFilterString, metricName, wavefrontMetricSource, serviceName);
    }

    private Map<String, Double> extractApiToCountMap(WavefrontMetricQueryResponse metricQueryResponse) {
        Map<String, Double> apiToCountMap = new HashMap<>();
        double result;
        if (null != metricQueryResponse && null != metricQueryResponse.getTimeseries()) {
            for (Timeseries timeseries : metricQueryResponse.getTimeseries()) {
                result = 0.0;
                Double[][] dataArray = timeseries.getData();
                for (int row = 0; row < dataArray.length; row++) {
                    result += dataArray[row][1];
                }
                apiToCountMap.put(timeseries.getTags().get("URL"), result);
            }
        }
        return apiToCountMap;
    }

    private void setHeadersAndBaseUrl(String wavefrontUrl, String wavefrontAPIToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept-Charset", "UTF-8");
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");
        headers.set("Authorization", "Bearer " + wavefrontAPIToken);
        turnOffSslChecking();
        setHeaders(headers);
        setBaseUrl(wavefrontUrl);
    }

    private String getEventQueryFilterForTags(Map<String, String> tags, String serviceFamilyName, String serviceName) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            stringBuilder.append(String.format("eventTag=\"\\\"%s:%s\\\"\"", entry.getKey(), entry.getValue()));
            stringBuilder.append(QUERY_AND_OPERATOR);
        }

        stringBuilder.append(String.format("eventTag=\"\\\"ServiceFamily:%s\\\"\"", serviceFamilyName));

        if (StringUtils.hasText(serviceName)) {
            stringBuilder.append(QUERY_AND_OPERATOR);
            stringBuilder.append(String.format("eventTag=\"\\\"service:%s\\\"\"", serviceName));
        }
        String eventQuery = String.format(ResiliencyConstants.EVENT_QUERY_FILTER, stringBuilder.toString());
        log.info("The event query for querying events of the service family {} is {}", serviceFamilyName, eventQuery);
        return eventQuery;
    }

    private String getMetricQueryFilterForTags(String metricName, String serviceName, String queryFilterString) {
        return String.format(queryFilterString, metricName, serviceName);
    }
}
