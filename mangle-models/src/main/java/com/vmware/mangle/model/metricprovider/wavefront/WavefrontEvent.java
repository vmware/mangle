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

package com.vmware.mangle.model.metricprovider.wavefront;

import java.util.LinkedHashMap;

import lombok.Data;

/**
 * @author dbhat
 */
@Data
public class WavefrontEvent implements MetricProviderEventResponse {
    private String name;
    private Long start;
    private Long end;
    private LinkedHashMap<String, String> tags;
}
