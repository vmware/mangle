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

package com.vmware.mangle.unittest.utils;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.resiliencyscore.QueryDto;
import com.vmware.mangle.cassandra.model.resiliencyscore.Service;
import com.vmware.mangle.model.ResiliencyScoreVO;
import com.vmware.mangle.model.metricprovider.wavefront.WavefrontEvent;
import com.vmware.mangle.model.resiliencyscore.ResiliencyScoreProperties;
import com.vmware.mangle.model.resiliencyscore.TimeseriesData;
import com.vmware.mangle.utils.ResiliencyScoreUtils;
import com.vmware.mangle.utils.constants.MockDataConstants;
import com.vmware.mangle.utils.constants.ResiliencyConstants;
import com.vmware.mangle.utils.helpers.metricprovider.WavefrontDataSourceHelper;
import com.vmware.mangle.utils.mockdata.ResiliencyScoreMockData;


/**
 * @author dbhat
 */

public class ResiliencyScoreUtilsTest {
    private ResiliencyScoreProperties properties;
    private Service service;
    private long startTime;
    private long endTime;
    private WavefrontDataSourceHelper metricHelperSpy;
    private ResiliencyScoreUtils rScoreSpy;
    private List<QueryDto> allQueries;

    @BeforeMethod
    public void init() {
        MockitoAnnotations.initMocks(this);
        properties = ResiliencyScoreMockData.getResiliencyScoreProperties();
        service = ResiliencyScoreMockData.getServiceProperties(MockDataConstants.SERVICE_NAME);
        endTime = ResiliencyScoreMockData.getEndTime();
        startTime = ResiliencyScoreMockData
                .getStartTime(properties.getResiliencyScoreMetricConfig().getResiliencyCalculationWindow());
        allQueries = ResiliencyScoreMockData.getListOfQueries();
        rScoreSpy = Mockito.spy(new ResiliencyScoreUtils(properties, service, allQueries, startTime, endTime));
        metricHelperSpy = Mockito.spy(new WavefrontDataSourceHelper(properties.getMetricProviderSpec()));
    }


    @Test(priority = 1, description = "Validate resiliency score calculation when no metric providers are defined ")
    public void noMetricProvidersDefined() {
        ResiliencyScoreUtils rScoreSpy =
                Mockito.spy(new ResiliencyScoreUtils(properties, service, allQueries, startTime, endTime));
        rScoreSpy.setMetricProvider(null);
        ResiliencyScoreVO rScore = rScoreSpy.calculateResiliencyScore();
        Assert.assertEquals(rScore.getResiliencyScore(), ResiliencyConstants.INVALID_SCORE,
                "Resiliency score validation failed");
    }

    @Test(priority = 2, description = "Validate resiliency score calculation when no wavefront events are present ")
    public void noWavefrontEventsFound() {
        rScoreSpy.setMetricProvider(metricHelperSpy);
        when(metricHelperSpy.getEvents(anyMap(), anyString(), anyString(), anyLong(), anyLong()))
                .thenReturn(new ArrayList());
        ResiliencyScoreVO rScore = rScoreSpy.calculateResiliencyScore();
        Assert.assertTrue(rScore.getResiliencyScore() == ResiliencyConstants.INVALID_SCORE,
                "Resiliency score validation failed");
    }

    @Test(priority = 4, description = "Validate resiliency score calculation with all valid data ")
    public void calculateRScoreForAllValidData() {
        rScoreSpy.setMetricProvider(metricHelperSpy);

        when(metricHelperSpy.getEvents(anyMap(), anyString(), anyString(), anyLong(), anyLong()))
                .thenReturn(getFaultEvents());
        when(metricHelperSpy.getTimeSeriesData(anyString(), anyLong(), anyLong(), anyString()))
                .thenReturn(getTimeSeriesData());
        doNothing().when(metricHelperSpy).sendMetric(any());
        ResiliencyScoreVO rScore = rScoreSpy.calculateResiliencyScore();
        Assert.assertTrue((rScore.getResiliencyScore() > 0.0 && rScore.getResiliencyScore() <= 1.0),
                "Resiliency score calculation failed");
    }

    @Test(priority = 5, description = "Validate resiliency score when there are no queries defined in properties")
    public void noServiceSpecificQueriesInProperties() {
        ResiliencyScoreUtils spyRScoreUtils =
                Mockito.spy(new ResiliencyScoreUtils(properties, service, new ArrayList<>(), startTime, endTime));
        spyRScoreUtils.setMetricProvider(metricHelperSpy);

        when(metricHelperSpy.getEvents(anyMap(), anyString(), anyString(), anyLong(), anyLong()))
                .thenReturn(getFaultEvents());
        when(metricHelperSpy.getTimeSeriesData(anyString(), anyLong(), anyLong(), anyString()))
                .thenReturn(getTimeSeriesData());
        doNothing().when(metricHelperSpy).sendMetric(any());
        ResiliencyScoreVO rScore = spyRScoreUtils.calculateResiliencyScore();
        Assert.assertTrue(rScore.getResiliencyScore() == 0.0, "Resiliency score calculation failed");
    }

    @Test(priority = 6, description = "Validate resiliency score calculation when TimeSeries data having empty tags")
    public void emptyTagsInTimeSeriesData() {
        rScoreSpy.setMetricProvider(metricHelperSpy);

        List<TimeseriesData> timeseriesData = new ArrayList<>();
        timeseriesData.add(ResiliencyScoreMockData.getTimeSeriesData(new HashMap<>()));
        when(metricHelperSpy.getEvents(anyMap(), anyString(), anyString(), anyLong(), anyLong()))
                .thenReturn(getFaultEvents());
        when(metricHelperSpy.getTimeSeriesData(anyString(), anyLong(), anyLong(), anyString()))
                .thenReturn(timeseriesData);
        doNothing().when(metricHelperSpy).sendMetric(any());
        ResiliencyScoreVO rScore = rScoreSpy.calculateResiliencyScore();
        Assert.assertTrue((rScore.getResiliencyScore() > 0.0 && rScore.getResiliencyScore() <= 1.0),
                "Resiliency score calculation failed");
    }

    @Test(priority = 7, description = "Validate resiliency score calculation when TimeSeries data having empty time series data")
    public void emptyDataInTimeSeriesData() {
        rScoreSpy.setMetricProvider(metricHelperSpy);

        List<TimeseriesData> timeseriesData = new ArrayList<>();
        TimeseriesData data = ResiliencyScoreMockData.getTimeSeriesData();
        data.setData(new Double[0][0]);
        timeseriesData.add(data);
        when(metricHelperSpy.getEvents(anyMap(), anyString(), anyString(), anyLong(), anyLong()))
                .thenReturn(getFaultEvents());
        when(metricHelperSpy.getTimeSeriesData(anyString(), anyLong(), anyLong(), anyString()))
                .thenReturn(timeseriesData);
        doNothing().when(metricHelperSpy).sendMetric(any());
        ResiliencyScoreVO rScore = rScoreSpy.calculateResiliencyScore();
        Assert.assertTrue(rScore.getResiliencyScore() == ResiliencyConstants.INVALID_SCORE,
                "Resiliency score calculation failed");
    }

    @Test(priority = 8, description = "Validate resiliency score calculation with all valid data - Service tags empty ")
    public void calculateRScoreForAllValidDataWithEmptyServiceTags() {
        ResiliencyScoreProperties resiliencyScoreProperties = ResiliencyScoreMockData.getResiliencyScoreProperties();
        Service serviceValue = ResiliencyScoreMockData.getServiceProperties(MockDataConstants.SERVICE_NAME);
        serviceValue.setTags(new HashMap<>());
        resiliencyScoreProperties.setTags(ResiliencyScoreMockData.getDummyTags());
        ResiliencyScoreUtils utilsSpy = Mockito
                .spy(new ResiliencyScoreUtils(resiliencyScoreProperties, serviceValue, allQueries, startTime, endTime));
        utilsSpy.setMetricProvider(metricHelperSpy);

        when(metricHelperSpy.getEvents(anyMap(), anyString(), anyString(), anyLong(), anyLong()))
                .thenReturn(getFaultEvents());
        when(metricHelperSpy.getTimeSeriesData(anyString(), anyLong(), anyLong(), anyString()))
                .thenReturn(getTimeSeriesData());
        ResiliencyScoreVO rScore = utilsSpy.calculateResiliencyScore();
        Assert.assertTrue((rScore.getResiliencyScore() > 0.0 && rScore.getResiliencyScore() <= 1.0),
                "Resiliency score calculation failed");
        verify(metricHelperSpy, times(1)).getEvents(anyMap(), anyString(), anyString(), anyLong(), anyLong());
    }

    @Test(priority = 9, description = "Validate resiliency score calculation with all valid data - All tags empty ")
    public void calculateRScoreForAllValidDataWithEmptyTags() {
        ResiliencyScoreProperties resiliencyScoreProperties = ResiliencyScoreMockData.getResiliencyScoreProperties();
        Service serviceValue = ResiliencyScoreMockData.getServiceProperties(MockDataConstants.SERVICE_NAME);
        serviceValue.setTags(new HashMap<>());
        resiliencyScoreProperties.setTags(new HashMap<>());
        ResiliencyScoreUtils utilsSpy = Mockito
                .spy(new ResiliencyScoreUtils(resiliencyScoreProperties, serviceValue, allQueries, startTime, endTime));
        utilsSpy.setMetricProvider(metricHelperSpy);

        when(metricHelperSpy.getEvents(anyMap(), anyString(), anyString(), anyLong(), anyLong()))
                .thenReturn(getFaultEvents());
        when(metricHelperSpy.getTimeSeriesData(anyString(), anyLong(), anyLong(), anyString()))
                .thenReturn(getTimeSeriesData());
        ResiliencyScoreVO rScore = utilsSpy.calculateResiliencyScore();
        Assert.assertTrue((rScore.getResiliencyScore() > 0.0 && rScore.getResiliencyScore() <= 1.0),
                "Resiliency score calculation failed");
        verify(metricHelperSpy, times(1)).getEvents(anyMap(), anyString(), anyString(), anyLong(), anyLong());
    }

    private List<WavefrontEvent> getFaultEvents() {
        LinkedHashMap<String, String> tags = new LinkedHashMap<>();
        tags.put(MockDataConstants.SOURCE, MockDataConstants.MANGLE);
        List<WavefrontEvent> events = new ArrayList<>();
        events.add(ResiliencyScoreMockData.getWavefrontEvent(15, tags));
        return events;
    }

    private List<TimeseriesData> getTimeSeriesData() {
        List<TimeseriesData> timeseriesData = new ArrayList<>();
        timeseriesData.add(ResiliencyScoreMockData.getTimeSeriesData());
        return timeseriesData;
    }

}
