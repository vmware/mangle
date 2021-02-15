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

package com.vmware.mangle.metric.wavefront.reporter;

import java.util.HashMap;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.vmware.mangle.common.MockDataConstants;

/**
 * Unit tests for class WavefrontMetricHelper
 * 
 * @author ranjans
 */
public class WavefrontMetricHelperTest {

    @Test
    public void constructTagsTest() {
        HashMap<String, String> customTags = new HashMap<>();
        customTags.put(MockDataConstants.ANY_STR + "1", MockDataConstants.ANY_STR);
        HashMap<String, String> staticTags = new HashMap<>();
        customTags.put(MockDataConstants.ANY_STR + "2", MockDataConstants.ANY_STR);
        HashMap<String, String> finalTags = WavefrontMetricHelper.constructTags(customTags, staticTags);
        Assert.assertEquals(finalTags.size(), 2);
    }

}
