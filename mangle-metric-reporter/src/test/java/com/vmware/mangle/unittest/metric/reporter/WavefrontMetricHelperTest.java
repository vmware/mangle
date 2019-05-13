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

package com.vmware.mangle.unittest.metric.reporter;

import static org.mockito.Mockito.validateMockitoUsage;

import java.util.HashMap;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.metric.reporter.WavefrontMetricHelper;
import com.vmware.mangle.unittest.metric.constants.MetricReporterTestConstants;

/**
 * Unit Test Case for WavefrontMetricHelper.
 *
 * @author kumargautam
 */
public class WavefrontMetricHelperTest extends PowerMockTestCase {

    @InjectMocks
    private WavefrontMetricHelper wavefrontMetricHelper;
    private Map<String, String> staticTags;

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.staticTags = new HashMap<>();
        this.staticTags.put("load", "80");
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public void tearDownAfterClass() throws Exception {
        this.staticTags = null;
        this.wavefrontMetricHelper = null;
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
     * {@link com.vmware.mangle.reporter.WavefrontMetricHelper#addStaticTags(java.util.Map, java.util.Map)}.
     */
    @Test(description = "Verify the addition of statictags and customtags to the wavefrontMetricHelper, they should both be added into the same map")
    public void testAddStaticTags() {
        Map<String, String> customTags = new HashMap<>();
        customTags.put(MetricReporterTestConstants.CPU_USAGE, "80.4");
        Map<String, String> actualResult = WavefrontMetricHelper.addStaticTags(customTags, this.staticTags);
        Assert.assertEquals(actualResult.size(), 2);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.reporter.WavefrontMetricHelper#addStaticTags(java.util.Map, java.util.Map)}.
     */
    @Test(description = "Verify the addition of statictags and null customtags to the wavefrontMetricHelper, the return map should only contain statictags")
    public void testAddStaticTags1() {
        Map<String, String> customTags = new HashMap<>();
        customTags.put(com.vmware.mangle.unittest.metric.constants.MetricReporterTestConstants.CPU_USAGE, "80.4");
        Map<String, String> actualResult = WavefrontMetricHelper.addStaticTags(null, this.staticTags);
        Assert.assertEquals(actualResult.size(), 1);
    }

}
