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

package com.vmware.mangle.services.dto;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

/**
 * @author dbhat
 *
 *         DTO to hold the Task Completed Event. The Information will be used to send the Fault Data
 *         to Monitoring systems and Alerting Channels.
 *
 */

@Data
public class FaultEventSpec {
    private String faultName;
    private String faultStartTime;
    private long faultStartTimeInEpoch;
    private String faultEndTime;
    private long faultEndTimeInEpoch;
    private String faultDescription;
    private Map<String, String> tags = new HashMap<>();
    private String faultEventClassification;
    private String faultEventType;
    private String taskId;
    private String faultStatus;
    private Integer timeoutInMilliseconds;
}
