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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import com.vmware.mangle.model.notifier.WavefrontEventDetailsDto;
import com.vmware.mangle.model.notifier.WavefrontEventItemDto;
import com.vmware.mangle.model.response.WavefrontEventSearchResponse;

/**
 * @author dbhat
 *
 */
public class WavefrontEventMockData {

    public WavefrontEventItemDto getEventItemData() {
        Long startTime = System.currentTimeMillis();
        Long endTime = System.currentTimeMillis() + 60000;
        WavefrontEventItemDto eventItemData = new WavefrontEventItemDto();

        eventItemData.setAnnotations(new HashMap<>());
        eventItemData.setCanClose(true);
        eventItemData.setEndTime(endTime);
        eventItemData.setCanDelete(true);
        eventItemData.setCreatedAt(startTime);
        eventItemData.setCreatedEpochMillis(startTime);
        eventItemData.setCreatorId("mangle@vmware.com");
        eventItemData.setCreatorType(new ArrayList<String>() {
            {
                add("USER");
            }
        });
        eventItemData.setEphemeral(false);
        eventItemData.setHosts(new ArrayList<>());
        eventItemData.setId(UUID.randomUUID().toString());
        eventItemData.setName("dummy-event-name");
        eventItemData.setRunningState("ONGOING");
        eventItemData.setStartTime(startTime);
        eventItemData.setTable("vmware-ddreplace");
        eventItemData.setTags(new ArrayList<>());
        eventItemData.setUpdatedAt(startTime);
        eventItemData.setUpdatedEpochMillis(startTime);
        eventItemData.setUpdaterId("mangle@vmware.com");
        eventItemData.setUserEvent(true);
        eventItemData.setSummarizedEvents(0);

        return eventItemData;
    }

    public WavefrontEventDetailsDto getEventDetailsDto() {
        WavefrontEventDetailsDto eventDetails = new WavefrontEventDetailsDto();
        ArrayList<WavefrontEventItemDto> items = new ArrayList<>();
        items.add(getEventItemData());

        eventDetails.setItems(items);
        eventDetails.setEventSearchStats(new HashMap<>());
        eventDetails.setMoreItems(false);
        eventDetails.setLimit(100);

        return eventDetails;
    }

    public WavefrontEventSearchResponse getRetrieveEventMockData() {
        WavefrontEventSearchResponse response = new WavefrontEventSearchResponse();
        response.setResponse(getEventDetailsDto());

        return response;
    }
}
