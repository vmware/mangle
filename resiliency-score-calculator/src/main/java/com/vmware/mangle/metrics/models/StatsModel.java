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

import lombok.Data;

/**
 * @author ranjans DTO for wavefront StatsModel
 */
@Data
public class StatsModel {

    private long query_tasks;
    private long skipped_compacted_keys;
    private long hosts_used;
    private long compacted_points;
    private long keys;
    private long cpu_ns;
    private long latency;
    private long s3_keys;
    private long compacted_keys;
    private long summaries;
    private long queries;
    private long points;
    private long buffer_keys;
    private long cached_compacted_keys;
    private long metrics_used;

    @Override
    public String toString() {
        return "StatsModel [query_tasks=" + query_tasks + ", skipped_compacted_keys=" + skipped_compacted_keys
                + ", hosts_used=" + hosts_used + ", compacted_points=" + compacted_points + ", keys=" + keys
                + ", cpu_ns=" + cpu_ns + ", latency=" + latency + ", s3_keys=" + s3_keys + ", compacted_keys="
                + compacted_keys + ", summaries=" + summaries + ", queries=" + queries + ", points=" + points
                + ", buffer_keys=" + buffer_keys + ", cached_compacted_keys=" + cached_compacted_keys
                + ", metrics_used=" + metrics_used + "]";
    }

}
