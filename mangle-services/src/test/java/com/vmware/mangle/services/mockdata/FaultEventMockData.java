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

package com.vmware.mangle.services.mockdata;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.services.dto.FaultEventSpec;
import com.vmware.mangle.utils.constants.MetricProviderConstants;

/**
 * @author dbhat
 */

public class FaultEventMockData {
    public FaultEventMockData() {

    }

    public FaultEventSpec getFaultEventMockData() {
        FaultEventSpec spec = new FaultEventSpec();
        spec.setFaultName("dummyFault");
        spec.setFaultStartTime(Long.toString(System.currentTimeMillis()));
        spec.setFaultStartTimeInEpoch(System.currentTimeMillis());
        spec.setFaultEndTime(Long.toString(System.currentTimeMillis() + 60000));
        spec.setFaultEndTimeInEpoch(System.currentTimeMillis() + 60000);
        Map<String, String> tags = new HashMap<>();
        tags.put("env", "test");
        spec.setTags(tags);
        spec.setFaultDescription(" Unit Test");
        spec.setFaultEventClassification(MetricProviderConstants.MANGLE_FAULT_EVENT_CLASSIFICATION);
        spec.setFaultEventType(MetricProviderConstants.MANGLE_FAULT_EVENT_TYPE);
        spec.setFaultStatus(TaskStatus.COMPLETED.name());
        spec.setTaskId(UUID.randomUUID().toString());
        return spec;
    }
}
