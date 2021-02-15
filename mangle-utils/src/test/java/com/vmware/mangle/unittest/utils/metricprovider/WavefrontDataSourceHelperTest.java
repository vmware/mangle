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

package com.vmware.mangle.unittest.utils.metricprovider;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Mockito;
import org.springframework.util.CollectionUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.metricprovider.MetricProviderSpec;
import com.vmware.mangle.metric.reporter.common.Metric;
import com.vmware.mangle.model.metricprovider.wavefront.WavefrontEvent;
import com.vmware.mangle.model.metricprovider.wavefront.WavefrontEventResponse;
import com.vmware.mangle.model.metricprovider.wavefront.WavefrontMetricQueryResponse;
import com.vmware.mangle.model.resiliencyscore.ResiliencyScoreProperties;
import com.vmware.mangle.model.resiliencyscore.Timeseries;
import com.vmware.mangle.model.resiliencyscore.TimeseriesData;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.constants.MockDataConstants;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.helpers.metricprovider.WavefrontDataSourceHelper;
import com.vmware.mangle.utils.helpers.metricprovider.WavefrontMetricProviderHelper;
import com.vmware.mangle.utils.mockdata.ResiliencyScoreMockData;

/**
 * @author dbhat
 */

public class WavefrontDataSourceHelperTest {
    private ResiliencyScoreProperties properties;
    private WavefrontMetricProviderHelper metricProviderHelper;
    private WavefrontDataSourceHelper wavefrontDataSourceHelper;
    private long startTime;
    private long endTime;

    @BeforeClass
    public void init() {
        properties = ResiliencyScoreMockData.getResiliencyScoreProperties();
        MetricProviderSpec metricProviderSpec = properties.getMetricProviderSpec();
        metricProviderHelper = Mockito.spy(new WavefrontMetricProviderHelper(
                metricProviderSpec.getWaveFrontConnectionProperties().getWavefrontInstance(),
                metricProviderSpec.getWaveFrontConnectionProperties().getWavefrontAPIToken()));
        wavefrontDataSourceHelper = new WavefrontDataSourceHelper(metricProviderSpec);
        wavefrontDataSourceHelper.setMetricProviderHelper(metricProviderHelper);
        startTime = ResiliencyScoreMockData.getStartTime(1);
        endTime = ResiliencyScoreMockData.getEndTime();
    }

    @Test(priority = 1, description = "Validation for get events method handle the empty events from wavefront API")
    public void noEventsFoundInWavefront() {
        WavefrontEventResponse eventResponse = new WavefrontEventResponse();
        when(metricProviderHelper.queryEvents(anyMap(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(eventResponse);

        List<WavefrontEvent> events = wavefrontDataSourceHelper.getEvents(new HashMap<>(),
                MockDataConstants.SERVICE_FAMILY_NAME, MockDataConstants.SERVICE_NAME, startTime, endTime);
        Assert.assertTrue(CollectionUtils.isEmpty(events), "Retrieval of Wavefront events returned error.");
    }

    @Test(priority = 2, description = "Validation for get events method to handle the null value from wavefront API")
    public void nullGetEventsResponse() {
        when(metricProviderHelper.queryEvents(anyMap(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(null);

        List<WavefrontEvent> events = wavefrontDataSourceHelper.getEvents(new HashMap<>(),
                MockDataConstants.SERVICE_FAMILY_NAME, MockDataConstants.SERVICE_NAME, startTime, endTime);
        Assert.assertTrue(CollectionUtils.isEmpty(events), "Retrieval of Wavefront events returned error.");
    }

    @Test(priority = 3, description = "Validation for get events method with all valid data ")
    public void getWavefrontEvents() {
        when(metricProviderHelper.queryEvents(anyMap(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(ResiliencyScoreMockData.getWavefrontEventResponse());
        List<WavefrontEvent> events = wavefrontDataSourceHelper.getEvents(new HashMap<>(),
                MockDataConstants.SERVICE_FAMILY_NAME, MockDataConstants.SERVICE_NAME, startTime, endTime);
        Assert.assertFalse(CollectionUtils.isEmpty(events), "Retrieval of Wavefront events returned error.");
    }

    @Test(priority = 4, description = "Validate query metric method to validate null API response")
    public void nullValueForGetMetricQuery() {
        when(metricProviderHelper.queryMetrics(anyString(), anyLong(), anyLong(), anyString())).thenReturn(null);
        WavefrontMetricQueryResponse response =
                wavefrontDataSourceHelper.queryMetric(anyString(), anyLong(), anyLong(), anyString());
        Assert.assertNull(response, "QueryDto metric failed when QueryDto API returned Null ");
    }

    @Test(priority = 5, description = "Validate query metric")
    public void getTimeSeriesDataWhenAPIReturnsNull() {
        when(metricProviderHelper.queryMetrics(anyString(), anyLong(), anyLong(), anyString())).thenReturn(null);
        List<TimeseriesData> timeseriesData =
                wavefrontDataSourceHelper.getTimeSeriesData(anyString(), anyLong(), anyLong(), anyString());
        Assert.assertTrue(CollectionUtils.isEmpty(timeseriesData),
                "Failed to retrieve time series data when QueryMetric API returned failure.");
    }

    @Test(priority = 6, description = "Validate retrieve Timeseries data from query metric response")
    public void getTimeSeriesDataWithEmptyTagsInQueryResponse() {
        WavefrontMetricQueryResponse queryResponse = new WavefrontMetricQueryResponse();
        queryResponse.setLabel(MockDataConstants.MANGLE);
        ArrayList<Timeseries> timeseries = new ArrayList<>();
        timeseries.add(ResiliencyScoreMockData.getTimeSeries());
        queryResponse.setTimeseries(timeseries);

        when(metricProviderHelper.queryMetrics(anyString(), anyLong(), anyLong(), anyString()))
                .thenReturn(queryResponse);
        List<TimeseriesData> timeseriesData =
                wavefrontDataSourceHelper.getTimeSeriesData(anyString(), anyLong(), anyLong(), anyString());
        Assert.assertFalse(timeseriesData.isEmpty(),
                "Failed to retrieve time series data when QueryMetric API returned failure.");
    }

    @Test(priority = 7, description = "Validate retrieve Timeseries data from query metric response")
    public void getTimeSeriesData() {
        WavefrontMetricQueryResponse queryResponse = new WavefrontMetricQueryResponse();
        queryResponse.setLabel(MockDataConstants.MANGLE);
        ArrayList<Timeseries> timeseries = new ArrayList<>();
        Timeseries eachTimeSeries = ResiliencyScoreMockData.getTimeSeries();
        Map<String, String> tags = new HashMap<>();
        tags.put(MockDataConstants.SOURCE, MockDataConstants.MANGLE);
        eachTimeSeries.setTags(tags);
        timeseries.add(eachTimeSeries);
        queryResponse.setTimeseries(timeseries);

        when(metricProviderHelper.queryMetrics(anyString(), anyLong(), anyLong(), anyString()))
                .thenReturn(queryResponse);
        List<TimeseriesData> timeseriesData =
                wavefrontDataSourceHelper.getTimeSeriesData(anyString(), anyLong(), anyLong(), anyString());
        Assert.assertFalse(timeseriesData.isEmpty(),
                "Failed to retrieve time series data when QueryMetric API returned failure.");
    }

    @Test(priority = 8, description = "Validate Send metric when invalid metric name is found")
    public void sendMetricForInvalidMetricName() {
        Metric metric = ResiliencyScoreMockData.getMetric();
        metric.setMetricName(null);
        try {
            wavefrontDataSourceHelper.sendMetric(metric);
            Assert.fail("Invalid metric name is specified and hence, expected MangleRunTimeExpcetion to be thrown.");
        } catch (MangleRuntimeException mangleException) {
            Assert.assertEquals(mangleException.getMessage(), ErrorConstants.INVALID_METRIC_NAME);
        }
    }

    @Test(priority = 9, description = "Validate Send metric when invalid timestamp is specified")
    public void sendMetricForInvalidTimeStamp() {
        Metric metric = ResiliencyScoreMockData.getMetric();
        metric.setMetricTimeStamp(null);
        try {
            wavefrontDataSourceHelper.sendMetric(metric);
            Assert.assertNotNull(metric.getMetricTimeStamp());
        } catch (MangleRuntimeException mangleException) {
            Assert.fail("The invalid time stamp should be handled and no exceptions were expected here.");
        }
    }

}