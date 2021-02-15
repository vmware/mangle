package com.vmware.mangle.resiliency.score.utils;


import com.vmware.mangle.client.restclient.WavefrontMetricProviderHelper;
import com.vmware.mangle.metric.common.Metric;
import com.vmware.mangle.metric.wavefront.reporter.WavefrontMetricReporter;
import com.vmware.mangle.metrics.models.*;

import com.vmware.mangle.resiliency.commons.ResiliencyConstants;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author dbhat
 * 
 *         Helper class to retrieve events and metrics from Wavefront. The helper methods will also
 *         take care converting the retrieved data from Wavefront to the data consumable by
 *         Resiliency score calculater
 */

@Log4j2
public class WavefrontDataSourceHelper implements IMetricProviderHelper {
    private WavefrontMetricProviderHelper metricProviderHelper;
    private WavefrontMetricReporter wavefrontMetricReporter;

    public WavefrontDataSourceHelper(MonitoringToolConnectionProperties monitoringToolProperties) {
        metricProviderHelper = new WavefrontMetricProviderHelper(monitoringToolProperties.getUrl(),
                monitoringToolProperties.getApiToken());
        this.wavefrontMetricReporter = new WavefrontMetricReporter(monitoringToolProperties.getProxy(),
                monitoringToolProperties.getPort(), ResiliencyConstants.METRIC_SOURCE, new HashMap<>());
    }

    /**
     * Retrieve events from Wavefront Monitoring tool for the specified duration of time along with the
     * tags specified.
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
        if (null == response || response.getEvents().isEmpty()) {
            log.debug(" Event Query returned error OR no events found . ");
            return new ArrayList<>();
        }
        List<WavefrontEvent> events = response.getEvents();
        return filterEvents(events, startTime, endTime);
    }

    /**
     * The events retrieved from the method: retrieveEvents(..) contains events out of the time range
     * specified. Hence, we will apply the filter to remove all the events those fall outside of time
     * boundary specified
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
        wavefrontMetricReporter.sendMetric(metric);
    }

}
