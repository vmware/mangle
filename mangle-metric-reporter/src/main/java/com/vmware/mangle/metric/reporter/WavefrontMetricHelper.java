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

import java.util.HashMap;

import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;

/**
 * Wavefront Metric helper class hosting the helper methods corresponding to wavefront Reporter.
 *
 * @author dbhat
 */
@Log4j2
public class WavefrontMetricHelper {

    private WavefrontMetricHelper() {
    }

    public static HashMap<String, String> constructTags(HashMap<String, String> customTags,
            HashMap<String, String> staticTags) {
        log.debug("Constructing tags for metric from static tags and custom tags");
        HashMap<String, String> finalTags = new HashMap<>(staticTags);
        if (!CollectionUtils.isEmpty(customTags)) {
            finalTags.putAll(customTags);
        }
        return finalTags;
    }
}
