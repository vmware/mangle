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

package com.vmware.mangle.unittest.metric.reporter.helpers;

import static org.mockito.Mockito.validateMockitoUsage;

import java.util.HashMap;
import java.util.Map;

import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.metric.reporter.constants.MetricReporterTestConstants;
import com.vmware.mangle.metric.reporter.helpers.metric.MetricsHelper;

/**
 * Unit Test Case for MetricsHelper.
 *
 * @author kumargautam
 */
@PrepareForTest(value = { MetricsHelper.class })
public class MetricsHelperTest extends PowerMockTestCase {

    @InjectMocks
    private MetricsHelper metricsHelper;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
        validateMockitoUsage();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.helpers.metric.MetricsHelper#isNull(java.lang.String)}.
     */
    @Test(description = "Test to verify if the given string is empty")
    public void testIsNull() {
        Assert.assertTrue(MetricsHelper.isNull(""));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.helpers.metric.MetricsHelper#isNull(java.lang.String)}.
     */
    @Test(description = "Test to verify if the given string is null")
    public void testIsNull1() {
        Assert.assertTrue(MetricsHelper.isNull(null));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.helpers.metric.MetricsHelper#isNull(java.lang.String)}.
     */
    @Test(description = "Test to verify if the given string is null, should return false for a non-empty string")
    public void testIsNullNonEmptyString() {
        Assert.assertFalse(MetricsHelper.isNull("dummy-string"));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.helpers.metric.MetricsHelper#isAValidMetricName(java.lang.String)}.
     */
    @Test(description = "Test to verify if the given name is a valid metric name; valid metric should be of the format <string>.<string>")
    public void testIsAValidMetricName() {
        Assert.assertTrue(MetricsHelper.isAValidMetricName("Test.data"));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.helpers.metric.MetricsHelper#isAValidMetricValue(java.lang.Object)}.
     */
    @Test(description = "Test to verify if the given name is a valid metric name; valid metric should be of the format <string>.<string>")
    public void testIsAValidMetricValue() {
        Assert.assertTrue(MetricsHelper.isAValidMetricValue("Test.data.cpu"));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.helpers.metric.MetricsHelper#isAValidMetricValue(java.lang.Object)}.
     */
    @Test(description = "Test to verify if the method handle null metric name; valid metric should be of the format <string>.<string>")
    public void testIsAValidMetricValue1() {
        Assert.assertFalse(MetricsHelper.isAValidMetricValue(null));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.helpers.metric.MetricsHelper#isAValidMetricValue(java.lang.Object)}.
     */
    @Test(description = "Test to verify if the method handle invalid metric name; valid metric should be of the format <string>.<string>")
    public void testIsAValidMetricValue2() {
        Assert.assertFalse(MetricsHelper.isAValidMetricValue(new HashMap<>()));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.helpers.metric.MetricsHelper#isEmptyTag(java.util.HashMap)}.
     */
    @Test(description = "Test to verify if the configured set of tags are empty")
    public void testIsEmptyTag() {
        Assert.assertTrue(MetricsHelper.isEmptyTag(new HashMap<>()));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.helpers.metric.MetricsHelper#isEmptyTag(java.util.HashMap)}.
     */
    @Test(description = "Test to verify if the configured set of tags are not empty")
    public void testIsEmptyTag1() {
        HashMap<String, String> map = new HashMap<>();
        map.put("name", "mangle");
        Assert.assertFalse(MetricsHelper.isEmptyTag(map));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.helpers.metric.MetricsHelper#getCurrentTimeStampInMillis()}.
     */
    @Test(description = "Test to verify if the getCurrentTimeStampInMillis doesn't return null value")
    public void testGetCurrentTimeStampInMillis() {
        Assert.assertNotNull(MetricsHelper.getCurrentTimeStampInMillis());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.helpers.metric.MetricsHelper#getDoubleEquivalent(java.lang.Object)}.
     */
    @Test(description = "Test to verify if the method getDoubleEquivalent method returns double value for a given integer")
    public void testGetDoubleEquivalent() {
        Assert.assertNotNull(MetricsHelper.getDoubleEquivalent(123));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.helpers.metric.MetricsHelper#getDoubleEquivalent(java.lang.Object)}.
     */
    @Test(description = "Test to verify if the method getDoubleEquivalent method returns double value for a given double value")
    public void testGetDoubleEquivalent1() {
        double num = 3134.61341;
        Assert.assertNotNull(MetricsHelper.getDoubleEquivalent(num));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.helpers.metric.MetricsHelper#getDoubleEquivalent(java.lang.Object)}.
     */
    @Test(description = "Test to verify if the method getDoubleEquivalent method returns double value for a given float value")
    public void testGetDoubleEquivalent2() {
        Float num = (float) 31.23;
        Assert.assertNotNull(MetricsHelper.getDoubleEquivalent(num));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.helpers.metric.MetricsHelper#getDoubleEquivalent(java.lang.Object)}.
     */
    @Test(description = "Test to verify if the method getDoubleEquivalent method returns double value for a given double string")
    public void testGetDoubleEquivalent3() {
        String num = "31.23";
        Assert.assertNotNull(MetricsHelper.getDoubleEquivalent(num));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.helpers.metric.MetricsHelper#stringToHashMap(java.lang.String)}.
     */
    @Test(description = "Test to verify the conversion of string to tags in the format of key:values, the give string should be in the format of key1=value1,key2=value2")
    public void testStringToHashMap() {
        Map<String, String> tags = MetricsHelper.stringToHashMap(MetricReporterTestConstants.TAGS);
        Assert.assertEquals(tags.size(), 2);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.helpers.metric.MetricsHelper#stringToHashMap(java.lang.String)}.
     */
    @Test(description = "Test to verify the failure of conversion of empty string to tags in the format of key:values, the give string should be in the format of key1=value1,key2=value2")
    public void testStringToHashMap1() {
        Map<String, String> tags = MetricsHelper.stringToHashMap("");
        Assert.assertEquals(tags.size(), 0);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.helpers.metric.MetricsHelper#stringToHashMap(java.lang.String)}.
     */
    @Test(description = "Test to verify the failure of conversion of improper string to tags in the format of key:values, the give string should be in the format of key1=value1,key2=value2")
    public void testStringToHashMap2() {
        Map<String, String> tags = MetricsHelper.stringToHashMap(",");
        Assert.assertEquals(tags.size(), 0);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.helpers.metric.MetricsHelper#stringToHashMap(java.lang.String)}.
     */
    @Test(description = "Test to verify the failure of conversion of improper string to tags in the format of key:values, the give string should be in the format of key1=value1,key2=value2")
    public void testStringToHashMap3() {
        Map<String, String> tags = MetricsHelper.stringToHashMap("Mangle");
        Assert.assertEquals(tags.size(), 0);
    }

    @Test(description = "Test to verify the failure fo isValidMetricName for null value; valid metric should be of the format <string>.<string>")
    public void testIsAValidMetricNameForNull() {
        boolean result = MetricsHelper.isAValidMetricName(null);
        Assert.assertFalse(result);
    }

    @Test(description = "Test to verify the failure fo isValidMetricName for improper string value; valid metric should be of the format <string>.<string>")
    public void testIsAValidMetricName2() {
        boolean result = MetricsHelper.isAValidMetricName("dummy-metric");
        Assert.assertFalse(result);
    }

    @Test(description = "Test to verify the failure fo isValidMetricName for improper string value(containing special characters); valid metric should be of the format <string>.<string>")
    public void testIsAValidMetricName3() {
        boolean result = MetricsHelper.isAValidMetricName("dummy-metric%#$%$#.metric");
        Assert.assertFalse(result);
    }


}
