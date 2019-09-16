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

package com.vmware.mangle.model.notifier;

import java.util.ArrayList;
import java.util.HashMap;

import lombok.Data;


/**
 * @author dbhat
 *
 *
 */
@Data
public class WavefrontEventItemDto {
    private String table;
    private String name;
    private HashMap<String, String> annotations;
    private String id;
    private long endTime;
    private long startTime;
    private ArrayList<String> tags;
    private long createdAt;
    private ArrayList<String> hosts;
    private boolean isUserEvent;
    private String creatorId;
    private String updaterId;
    private long updatedAt;
    private Object summarizedEvents;
    private boolean isEphemeral;
    private long createdEpochMillis;
    private long updatedEpochMillis;
    private String runningState;
    private boolean canDelete;
    private boolean canClose;
    private ArrayList<String> creatorType;
}
