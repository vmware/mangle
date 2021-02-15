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

package com.vmware.mangle.utils.mockdata;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.vmware.mangle.cassandra.model.metricprovider.MetricProviderSpec;
import com.vmware.mangle.cassandra.model.resiliencyscore.QueryDto;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreMetricConfig;
import com.vmware.mangle.cassandra.model.resiliencyscore.Service;
import com.vmware.mangle.metric.reporter.common.Metric;
import com.vmware.mangle.model.enums.MetricProviderType;
import com.vmware.mangle.model.metricprovider.wavefront.WavefrontEvent;
import com.vmware.mangle.model.metricprovider.wavefront.WavefrontEventResponse;
import com.vmware.mangle.model.resiliencyscore.ResiliencyScoreProperties;
import com.vmware.mangle.model.resiliencyscore.Timeseries;
import com.vmware.mangle.model.resiliencyscore.TimeseriesData;
import com.vmware.mangle.utils.constants.MockDataConstants;

/**
 * @author dbhat
 */

public class ResiliencyScoreMockData {
    private ResiliencyScoreMockData() {

    }

    public static ResiliencyScoreProperties getResiliencyScoreProperties() {
        ResiliencyScoreProperties resiliencyScorePropertiesMockData = new ResiliencyScoreProperties();
        resiliencyScorePropertiesMockData.setMetricProviderSpec(getMonitoringToolConnectionProperties());
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

    public static MetricProviderSpec getMonitoringToolConnectionProperties() {
        MetricProviderSpec mockData = new MetricProviderSpec();
        mockData.setName(MockDataConstants.METRIC_PROVIDER_NAME);
        mockData.setMetricProviderType(MetricProviderType.WAVEFRONT);
        mockData.setWaveFrontConnectionProperties(MetricProviderMock.getDummyWavefrontConnectionProperties());
        return mockData;
    }

    public static Service getServiceProperties(String serviceName) {
        Service service = new Service();
        List<String> queries = new ArrayList<>();
        queries.add(getQueryProperties().getName());
        service.setQueryNames(queries);
        service.setName(serviceName);
        service.setTags(getDummyTags());
        return service;
    }

    public static Map<String, String> getDummyTags() {
        Map<String, String> tags = new HashMap<>();
        tags.put(MockDataConstants.APP, MockDataConstants.SERVICE_NAME);
        return tags;
    }

    public static long getEndTime() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTimeInMillis();
    }

    public static long getStartTime(int resiliencyCalculationTimeWindowInHour) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -resiliencyCalculationTimeWindowInHour);
        return calendar.getTimeInMillis();
    }

    public static WavefrontEvent getWavefrontEvent(int minutesBeforeEventStarted, LinkedHashMap<String, String> tags) {
        WavefrontEvent event = new WavefrontEvent();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -minutesBeforeEventStarted);
        event.setEnd(calendar.getTimeInMillis());

        calendar.add(Calendar.MINUTE,
                -(minutesBeforeEventStarted + MockDataConstants.FAULT_INJECTION_DURATION_IN_MINUTE));
        event.setStart(calendar.getTimeInMillis());

        event.setName(MockDataConstants.FAULT_INJECTED_EVENT_NAME + String.valueOf(minutesBeforeEventStarted));
        event.setTags(tags);
        return event;
    }

    public static WavefrontEvent getWavefrontEvent() {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put(MockDataConstants.SOURCE, MockDataConstants.MANGLE);
        return getWavefrontEvent(15, tags);
    }

    public static TimeseriesData getTimeSeriesData(Map<String, String> tags) {
        TimeseriesData timeseriesData = new TimeseriesData();
        timeseriesData.setTags(tags);
        timeseriesData.setData(getData());
        timeseriesData.setData(getData());
        return timeseriesData;
    }

    private static Double[][] getData() {
        Double[][] data = new Double[5][5];
        for (int row = 0; row < 5; row++) {
            for (int column = 0; column < 5; column++) {
                double metricValue = Math.random();
                if (metricValue < 0.5) {
                    data[row][column] = 0.0;
                } else {
                    data[row][column] = 1.0;
                }
            }
        }
        return data;
    }

    public static TimeseriesData getTimeSeriesData() {
        Map<String, String> tags = new HashMap<>();
        tags.put("service", "mangle");
        return getTimeSeriesData(tags);
    }

    public static WavefrontEventResponse getWavefrontEventResponse() {
        WavefrontEventResponse response = new WavefrontEventResponse();
        response.setGranularity(1);
        List<WavefrontEvent> events = new ArrayList<>();
        events.add(getWavefrontEvent());
        response.setEvents(events);
        return response;
    }

    public static Timeseries getTimeSeries() {
        Timeseries timeSeries = new Timeseries();
        timeSeries.setHost(MockDataConstants.MANGLE);
        timeSeries.setLabel(MockDataConstants.FAULT_INJECTED_EVENT_NAME);
        timeSeries.setData(getData());
        timeSeries.setTags(new HashMap<>());
        return timeSeries;
    }

    public static String getRandomUUID() {
        return UUID.randomUUID().toString();
    }

    public static Metric getMetric() {
        Map<String, String> tags = new HashMap<>();
        tags.put(MockDataConstants.APP, MockDataConstants.SERVICE_NAME);
        return new Metric(MockDataConstants.METRIC_NAME, MockDataConstants.METRIC_VALUE, tags,
                MockDataConstants.MANGLE);
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

    public static List<QueryDto> getListOfQueries() {
        List<QueryDto> allQueries = new ArrayList<>();
        allQueries.add(getQueryProperties());
        return allQueries;
    }
}
