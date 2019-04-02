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

package com.vmware.mangle.metric.reporter;

import java.util.Map;

import lombok.extern.log4j.Log4j2;


/**
 * Wavefront Metric helper class hosting the helper methods corresponding to wavefront Reporter.
 *
 * @author dbhat
 */
@Log4j2
public class WavefrontMetricHelper {

    private WavefrontMetricHelper() {}

    public static Map<String, String> addStaticTags(Map<String, String> customTags, Map<String, String> staticTags) {
        log.info(" Appending the static tags to the existing Tags");
        if (null == customTags) {
            return staticTags;
        }
        staticTags.putAll(customTags);
        return staticTags;
    }
}
