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

package com.vmware.mangle.metric.utils;

import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.vmware.mangle.common.MockDataConstants;

/**
 * Unit tests for class MetricsHelper
 * 
 * @author ranjans
 */
public class MetricsHelperTest {

    @Test
    public void isNullTest() {
        Assert.assertTrue(MetricsHelper.isNull(null));
        Assert.assertTrue(!MetricsHelper.isNull(MockDataConstants.ANY_STR));
    }

    @Test
    public void isAValidMetricNameTest() {
        Assert.assertTrue(!MetricsHelper.isAValidMetricName(null));
        Assert.assertTrue(!MetricsHelper.isAValidMetricName(MockDataConstants.ANY_STR));
        Assert.assertTrue(MetricsHelper.isAValidMetricName(MockDataConstants.VALID_METRIC));
    }

    @Test
    public void isAValidMetricValueTest() {
        Assert.assertTrue(!MetricsHelper.isAValidMetricValue(null));
        Assert.assertTrue(MetricsHelper.isAValidMetricValue(MockDataConstants.ANY_STR));
        Assert.assertTrue(MetricsHelper.isAValidMetricValue(0));
        Assert.assertTrue(MetricsHelper.isAValidMetricValue(0.0));
    }

    @Test
    public void isEmptyTagTest() {
        HashMap<String, String> tags = new HashMap<>();
        Assert.assertTrue(MetricsHelper.isEmptyTag(tags));
        tags.put(MockDataConstants.ANY_STR, MockDataConstants.ANY_STR);
        Assert.assertTrue(!MetricsHelper.isEmptyTag(tags));
    }

    @Test
    public void getCurrentTimeStampInMillisTest() {
        Assert.assertNotNull(MetricsHelper.getCurrentTimeStampInMillis());
    }

    @Test
    public void getDoubleEquivalentTest() {
        Assert.assertNotNull(MetricsHelper.getDoubleEquivalent(0));
        Assert.assertNotNull(MetricsHelper.getDoubleEquivalent("0"));
        Assert.assertNotNull(MetricsHelper.getDoubleEquivalent(0.0));
    }

    @Test
    public void stringToHashMapTest() {
        Assert.assertTrue(MetricsHelper.stringToHashMap(null).isEmpty());
        Assert.assertTrue(!MetricsHelper.stringToHashMap(MockDataConstants.ANY_STR + "=" + MockDataConstants.ANY_STR
                + "," + MockDataConstants.ANY_STR + "=" + MockDataConstants.ANY_STR).isEmpty());
    }

}
