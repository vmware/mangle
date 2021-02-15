package com.vmware.mangle.resiliency.score.utils;

import com.vmware.mangle.metric.common.Metric;
import com.vmware.mangle.metrics.models.TimeseriesData;
import com.vmware.mangle.metrics.models.WavefrontEvent;

import java.util.List;
import java.util.Map;

/**
 * @author dbhat
 *
 *         Metric Provider helper for calculating the Resiliency score.
 */
public interface IMetricProviderHelper {
    List<TimeseriesData> getTimeSeriesData(String query, long startTime, long endTime, String granularity);

    List<WavefrontEvent> getEvents(Map<String, String> tags, String serviceFamilyName, String serviceName,
            long startTime, long endTime);

    void sendMetric(Metric metric);
}
