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
 * @author ranjans
 * 
 *         DTO for wavefront QueryEvent
 */
@Data
public class QueryEvent {

    private Object tags;
    private long start;
    private String name;
    private int summarized;
    private String[] hosts;
    private boolean isEphemeral;
    private long end;

    @Override
    public String toString() {
        return "QueryEvent [tags=" + tags + ", start=" + start + ", name=" + name + ", summarized=" + summarized
                + ", hosts=" + Arrays.toString(hosts) + ", isEphemeral=" + isEphemeral + ", end=" + end + "]";
    }

}
