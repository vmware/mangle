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

package com.vmware.mangle.utils.mockdata;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.vmware.mangle.services.dto.FaultEventSpec;

/**
 * @author dbhat
 *
 */
public class FaultEventSpecMockData {

    private FaultEventSpecMockData() {

    }

    public static FaultEventSpec getDummyFaultEventData() {
        Date startDate = new Date();
        Date endDate = new Date();
        Map<String, String> tags = new HashMap<String, String>();
        tags.put("env", "prod");
        FaultEventSpec eventInfo = new FaultEventSpec();
        eventInfo.setFaultDescription("Fault Description is here");
        eventInfo.setFaultEndTime(endDate.toString());
        eventInfo.setFaultEndTimeInEpoch(endDate.getTime());
        eventInfo.setFaultStartTime(startDate.toString());
        eventInfo.setFaultStartTimeInEpoch(startDate.getTime());
        eventInfo.setFaultEventClassification("info");
        eventInfo.setFaultEventType("fault-injection");
        eventInfo.setFaultName("dummy-fault");
        eventInfo.setFaultStatus("COMPLETED");
        eventInfo.setTags(tags);
        eventInfo.setTaskId(UUID.randomUUID().toString());
        return eventInfo;
    }

}
