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

import java.util.HashMap;
import java.util.Map;

import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.model.metricprovider.wavefront.WavefrontEventResponse;
import com.vmware.mangle.model.metricprovider.wavefront.WavefrontMetricQueryResponse;
import com.vmware.mangle.utils.constants.MockDataConstants;
import com.vmware.mangle.utils.helpers.metricprovider.WavefrontMetricProviderHelper;
import com.vmware.mangle.utils.mockdata.ResiliencyScoreMockData;

/**
 * @author dbhat
 */
public class WavefrontMetricProviderHelperTest {

    WavefrontMetricProviderHelper helperSpy;

    @BeforeMethod
    public void initMockito() {
        MockitoAnnotations.initMocks(this);
        WavefrontMetricProviderHelper helper = new WavefrontMetricProviderHelper(MockDataConstants.WAVEFRONT_INSTANCE,
                MockDataConstants.DUMMY_API_TOKEN);
        helperSpy = Mockito.spy(helper);
    }

    @Test
    public void queryMetricWithQuery() {
        WavefrontMetricQueryResponse response = helperSpy.queryMetrics(MockDataConstants.DUMMY_QUERY_1,
                ResiliencyScoreMockData.getStartTime(1), ResiliencyScoreMockData.getEndTime(),
                MockDataConstants.RESILIENCY_SCORE_METRIC_CONFIG_GRANULARITY);
        //Asserting null as the wavefront instance being used here is dummy instance.
        Assert.assertNull(response);
    }

    @Test
    public void queryMetricsWithMetricName() {
        WavefrontMetricQueryResponse response = helperSpy.queryMetrics(
                MockDataConstants.RESILIENCY_SCORE_METRIC_CONFIG_METRIC_NAME, MockDataConstants.SERVICE_NAME,
                ResiliencyScoreMockData.getStartTime(1), ResiliencyScoreMockData.getEndTime(), "",
                MockDataConstants.RESILIENCY_SCORE_METRIC_CONFIG_GRANULARITY);
        Assert.assertNull(response);
    }

    @Test
    public void queryEvents() {
        String startTime = String.valueOf(ResiliencyScoreMockData.getStartTime(1));
        String endTime = String.valueOf(ResiliencyScoreMockData.getEndTime());
        Map<String, String> tags = new HashMap<>();
        tags.put(MockDataConstants.SOURCE, MockDataConstants.SERVICE_NAME);
        WavefrontEventResponse response = helperSpy.queryEvents(tags, MockDataConstants.SERVICE_FAMILY_NAME,
                MockDataConstants.SERVICE_NAME, startTime, endTime);
        Assert.assertNull(response);
    }
}
