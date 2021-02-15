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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.wavefront.sdk.common.WavefrontSender;
import com.wavefront.sdk.direct.ingestion.WavefrontDirectIngestionClient;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.metricprovider.MetricProviderSpec;
import com.vmware.mangle.metric.reporter.common.Metric;
import com.vmware.mangle.model.metricprovider.wavefront.WavefrontEvent;
import com.vmware.mangle.model.metricprovider.wavefront.WavefrontEventResponse;
import com.vmware.mangle.model.metricprovider.wavefront.WavefrontMetricQueryResponse;
import com.vmware.mangle.model.resiliencyscore.Timeseries;
import com.vmware.mangle.model.resiliencyscore.TimeseriesData;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.constants.ResiliencyConstants;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;


/**
 * @author dbhat
 *
 *         Helper class to retrieve events and metrics from Wavefront. The helper methods will also
 *         take care converting the retrieved data from Wavefront to the data consumable by
 *         Resiliency score calculator
 */

@Log4j2

public class WavefrontDataSourceHelper implements IMetricProviderHelper {
    @Setter
    private WavefrontMetricProviderHelper metricProviderHelper;
    private WavefrontDirectIngestionClient.Builder wavefrontDirectIngestionClient;
    private WavefrontSender wavefrontSender;

    public WavefrontDataSourceHelper(MetricProviderSpec monitoringToolProperties) {
        trimWavefrontInstanceName(monitoringToolProperties);
        metricProviderHelper = new WavefrontMetricProviderHelper(
                monitoringToolProperties.getWaveFrontConnectionProperties().getWavefrontInstance(),
                monitoringToolProperties.getWaveFrontConnectionProperties().getWavefrontAPIToken());
        wavefrontDirectIngestionClient = new WavefrontDirectIngestionClient.Builder(
                monitoringToolProperties.getWaveFrontConnectionProperties().getWavefrontInstance(),
                monitoringToolProperties.getWaveFrontConnectionProperties().getWavefrontAPIToken());
        wavefrontDirectIngestionClient
                .flushIntervalSeconds(ResiliencyConstants.DEFAULT_METRIC_FLUSH_INTERVAL_IN_SECOND);
        wavefrontSender = wavefrontDirectIngestionClient.build();
    }

    private void trimWavefrontInstanceName(MetricProviderSpec monitoringToolProperties) {
        log.debug("Removing the trailing / in the wavefront instance name ");
        String url = monitoringToolProperties.getWaveFrontConnectionProperties().getWavefrontInstance();
        if (StringUtils.isEmpty(url)) {
            log.error(ErrorConstants.EMPTY_METRIC_PROVIDER_INSTANCE);
            return;
        }
        url = url.replaceAll("/+$", "");
        log.debug("Wavefront instance name after trimming the trailing /: " + url);
        monitoringToolProperties.getWaveFrontConnectionProperties().setWavefrontInstance(url);
    }

    /**
     * Retrieve events from Wavefront Monitoring tool for the specified duration of time along with
     * the tags specified.
     *
     * @param tags
     *            : Tags to be included while querying the event.
     * @param servieFamilyName
     *            : Service family name to be included as tag while querying event.
     * @param serviceName
     *            : Service name to be included as tag while querying event.
     * @param startTime
     *            : Start time for events query in epoch milliseconds
     * @param endTime
     *            : End time for events query in epoch milliseconds.
     * @return : List of events retrieved.
     */
    public List<WavefrontEvent> getEvents(Map<String, String> tags, String servieFamilyName, String serviceName,
            long startTime, long endTime) {
        log.info(" Querying events from Wavefront. ");
        WavefrontEventResponse response = metricProviderHelper.queryEvents(tags, servieFamilyName, serviceName,
                String.valueOf(startTime), String.valueOf(endTime));
        if (null == response || null == response.getEvents() || response.getEvents().isEmpty()) {
            log.debug(" Event Query returned error OR no events found . ");
            return new ArrayList<>();
        }
        List<WavefrontEvent> events = response.getEvents();
        return filterEvents(events, startTime, endTime);
    }

    /**
     * The events retrieved from the method: retrieveEvents(..) contains events out of the time
     * range specified. Hence, we will apply the filter to remove all the events those fall outside
     * of time boundary specified
     *
     * @param wavefrontEvents
     *            : List of events retrieved already.
     * @param startTime
     *            : Event start time.
     * @param endTime
     *            : Event end time.
     * @return : List of events sliced with the time range.
     */
    private List<WavefrontEvent> filterEvents(List<WavefrontEvent> wavefrontEvents, Long startTime, Long endTime) {
        List<WavefrontEvent> filterEvents = wavefrontEvents.stream()
                .filter(event -> event.getEnd() >= startTime && event.getEnd() <= endTime).collect(Collectors.toList());
        log.debug("{} events were found out of time bound, startTime: {} and endTime: {}",
                wavefrontEvents.size() - filterEvents.size(), startTime, endTime);
        return filterEvents;
    }

    /**
     * Helper method to retrieve the time series data for the query specified between specified time
     * range.
     *
     * @param query
     *            : Query to be executed
     * @param startTimeInEpocMillis
     *            : Start Time of the query in epoch milliseconds.
     * @param endTimeInEpochMillis
     *            : End time of the query in epoch milliseconds.
     * @param granulirity
     *            : Granularity with which the data need to be retrieved.
     * @return : Query response having time series data.
     */
    public WavefrontMetricQueryResponse queryMetric(String query, long startTimeInEpocMillis, long endTimeInEpochMillis,
            String granulirity) {
        log.info(" Running the query: " + query + " between start time: " + startTimeInEpocMillis + " and end time: "
                + endTimeInEpochMillis);
        WavefrontMetricQueryResponse queryResponse =
                metricProviderHelper.queryMetrics(query, startTimeInEpocMillis, endTimeInEpochMillis, granulirity);
        if (null == queryResponse) {
            log.error(" Query didn't return any thing. Please check the query. ");
            return null;
        }
        return queryResponse;
    }

    /**
     * Retrieve only the time series data from the specified query in the specified time window. The
     * Time Series data retrieved will have only tags and time series data in it. We will keep the
     * "host" value as well in the tags list.
     *
     * @param query
     *            : Query to be executed.
     * @param startTimeInEpocMillis
     *            : Start time of the query in epoch milliseconds.
     * @param endTimeInEpochMillis
     *            : End time of the query in epoch milliseconds.
     * @param granularity
     *            : Granularity with which the time series data need to be retrieved.
     * @return : List of time series data retrieved.
     */
    public List<TimeseriesData> getTimeSeriesData(String query, long startTimeInEpocMillis, long endTimeInEpochMillis,
            String granularity) {
        List<TimeseriesData> timeSeriesData = new ArrayList<>();
        WavefrontMetricQueryResponse queryResponse =
                queryMetric(query, startTimeInEpocMillis, endTimeInEpochMillis, granularity);
        if (null != queryResponse && null != queryResponse.getTimeseries()) {
            for (Timeseries timeseries : queryResponse.getTimeseries()) {
                TimeseriesData data = new TimeseriesData();
                if (CollectionUtils.isEmpty(timeseries.getTags())) {
                    data.setTags(new HashMap<>());
                } else {
                    data.setTags(timeseries.getTags());
                }
                data.setData(timeseries.getData());
                data.getTags().put("host", timeseries.getHost());
                timeSeriesData.add(data);
            }
            return timeSeriesData;
        }
        return timeSeriesData;
    }

    public void sendMetric(Metric metric) {
        validateMetric(metric);
        ImmutableMap tags = ImmutableMap.copyOf(metric.getTags());
        try {
            wavefrontSender.sendMetric(metric.getMetricName(), metric.getMetricValue(), metric.getMetricTimeStamp(),
                    metric.getSource(), tags);
            log.info("Metric sent successfully");
        } catch (IOException ioException) {
            log.error(String.format(ErrorConstants.SENDING_METRIC_FAILED, metric.getMetricName()));
            log.error(ioException.getMessage());
            throw new MangleRuntimeException(
                    String.format(ErrorConstants.SENDING_METRIC_FAILED, metric.getMetricName()),
                    ErrorCode.SEND_METRIC_FAILED, metric.getMetricName());
        } finally {
            try {
                wavefrontSender.flush();
                wavefrontSender.close();
            } catch (IOException ioException) {
                log.error(ErrorConstants.WAVEFRONT_SEND_METRIC_CLOSE_FAILED);
                log.error(ioException.getMessage());
            }
        }
    }

    private void validateMetric(Metric metric) {
        if (StringUtils.isEmpty(metric.getMetricName())) {
            throw new MangleRuntimeException(ErrorConstants.INVALID_METRIC_NAME, ErrorCode.INVALID_METRIC_NAME);
        }
        if (null == metric.getMetricTimeStamp()) {
            log.debug(" Setting the time to current time.");
            Calendar calendar = Calendar.getInstance();
            metric.setMetricTimeStamp(calendar.getTimeInMillis());
        }
    }
}
