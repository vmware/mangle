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

package com.vmware.mangle.utils.helpers.metricprovider;

import java.util.Map;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.vmware.mangle.model.metricprovider.wavefront.WavefrontEventResponse;
import com.vmware.mangle.model.metricprovider.wavefront.WavefrontMetricQueryResponse;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.constants.ResiliencyConstants;


/**
 * @author ranjans
 *
 *         Endpoint client for WaveFront Server
 */
@Log4j2
public class WavefrontMetricProviderHelper extends RestTemplateWrapper implements IMetricDataSourceHelper {

    private static final String QUERY_AND_OPERATOR = " and ";

    public WavefrontMetricProviderHelper(String wavefrontUrl, String wavefrontAPIToken) {
        setHeadersAndBaseUrl(wavefrontUrl, wavefrontAPIToken);
    }

    @Override
    public WavefrontEventResponse queryEvents(Map<String, String> tags, String serviceFamilyName, String serviceName,
            String startTime, String endTime) {

        log.info("Querying events for the service {} between {} and {}", serviceName, startTime, endTime);
        @SuppressWarnings("unchecked")
        String requestUrl = String.format(ResiliencyConstants.WAVEFRONT_QUERY_URL_MINUTE_GRANULARITY, startTime,
                endTime, getEventQueryFilterForTags(tags, serviceFamilyName, serviceName));
        log.info("Request url for the events on the service {} of the serviceFamily {}: {}", serviceName,
                serviceFamilyName, requestUrl);
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
        if (!CollectionUtils.isEmpty(tags)) {
            int iterator = 0;
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                iterator++;
                stringBuilder.append(String.format("eventTag=\"\\\"%s:%s\\\"\"", entry.getKey(), entry.getValue()));
                if (iterator < tags.size()) {
                    stringBuilder.append(QUERY_AND_OPERATOR);
                }
            }
        }

        if (StringUtils.hasText(serviceFamilyName)) {
            stringBuilder.append(QUERY_AND_OPERATOR);
            stringBuilder.append(String.format("eventTag=\"\\\"ServiceFamily:%s\\\"\"", serviceFamilyName));
        }

        if (StringUtils.hasText(serviceName)) {
            stringBuilder.append(QUERY_AND_OPERATOR);
            stringBuilder.append(String.format("eventTag=\"\\\"service:%s\\\"\"", serviceName));
        }
        String eventQuery = String.format(ResiliencyConstants.EVENT_QUERY_FILTER, stringBuilder.toString());
        log.info("The event query for querying event for the service {} of the service family {} is {}", serviceName,
                serviceFamilyName, eventQuery);
        return eventQuery;
    }

    private String getMetricQueryFilterForTags(String metricName, String serviceName, String queryFilterString) {
        return String.format(queryFilterString, metricName, serviceName);
    }
}
