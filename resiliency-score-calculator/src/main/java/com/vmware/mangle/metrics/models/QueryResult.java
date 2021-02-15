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

package com.vmware.mangle.metrics.models;

import java.util.Arrays;

import lombok.Data;

/**
 * @author ranjans DTO for wavefront QueryResult
 */
@Data
public class QueryResult {

    private Timeseries[] timeseries;
    private QueryEvent[] events;
    private StatsModel stats;
    private String query;
    private long granularity;
    private String name;
    private String warnings;

    @Override
    public String toString() {
        return "QueryResult [timeseries=" + Arrays.toString(timeseries) + ", events=" + Arrays.toString(events)
                + ", stats=" + stats + ", query=" + query + ", granularity=" + granularity + ", name=" + name
                + ", warnings=" + warnings + "]";
    }

}
