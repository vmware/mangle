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

package com.vmware.mangle.unittest.utils.helpers.notifier;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.metricprovider.WaveFrontConnectionProperties;
import com.vmware.mangle.model.notifier.WavefrontEventDetailsDto;
import com.vmware.mangle.model.notifier.WavefrontEventItemDto;
import com.vmware.mangle.model.response.WavefrontEventSearchResponse;
import com.vmware.mangle.services.dto.FaultEventSpec;
import com.vmware.mangle.utils.clients.metricprovider.WaveFrontServerClient;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.helpers.notifiers.WavefrontNotifier;
import com.vmware.mangle.utils.mockdata.ApiResponseMockData;
import com.vmware.mangle.utils.mockdata.FaultEventSpecMockData;
import com.vmware.mangle.utils.mockdata.MetricProviderMock;
import com.vmware.mangle.utils.mockdata.WavefrontEventMockData;


/**
 * @author dbhat
 */

public class WavefrontNotifierTest {
    FaultEventSpec faultEventData;
    WaveFrontConnectionProperties wfProperties;
    ApiResponseMockData apiResponseMockData;
    WavefrontEventMockData wavefrontEventMockData;

    @Mock
    WaveFrontServerClient wfc;

    @BeforeClass
    public void initFaultEventData() throws MangleException {
        faultEventData = FaultEventSpecMockData.getDummyFaultEventData();
        wfProperties = MetricProviderMock.getDummyWavefrontConnectionProperties();
        apiResponseMockData = new ApiResponseMockData();
        wavefrontEventMockData = new WavefrontEventMockData();
    }

    @BeforeMethod
    public void initMock() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(description = " Send event should return failure when send Event API returned error")
    public void sendEventWhenSendApiFails() {
        WavefrontNotifier wfn = spy(new WavefrontNotifier(wfc));
        when(wfc.post(any(String.class), any(String.class), any(Class.class))).thenReturn(null);
        boolean status = wfn.sendEvent(faultEventData);
        Assert.assertFalse(status, "The test has failed for the validation of sending event");
    }

    @Test(description = " Send event should return failure when send Event API returned success")
    public void sendEventWhenApiReturnsSuccess() {
        WavefrontNotifier wfn = spy(new WavefrontNotifier(wfc));
        String response = apiResponseMockData.getApiResponse(200);
        when(wfc.post(any(String.class), any(String.class), any(Class.class)))
                .thenReturn(new ResponseEntity(response, HttpStatus.ACCEPTED));
        boolean status = wfn.sendEvent(faultEventData);
        Assert.assertTrue(status, "The test has failed for the validation of sending event");
    }

    @Test(description = " Send event should return failure when send Event API returned success")
    public void sendEventWhenTagsAreNull() {
        WavefrontNotifier wfn = spy(new WavefrontNotifier(wfc));
        String response = apiResponseMockData.getApiResponse(200);
        when(wfc.post(any(String.class), any(String.class), any(Class.class)))
                .thenReturn(new ResponseEntity(response, HttpStatus.ACCEPTED));
        FaultEventSpec tagsNull = FaultEventSpecMockData.getDummyFaultEventData();
        tagsNull.setTags(null);
        boolean status = wfn.sendEvent(faultEventData);
        Assert.assertTrue(status, "The test has failed for the validation of sending event");
    }

    @Test(description = " Validate retrieve events from wavefront ")
    public void retrieveEventDetails() {
        WavefrontNotifier wfn = spy(new WavefrontNotifier(wfc));
        WavefrontEventSearchResponse response = wavefrontEventMockData.getRetrieveEventMockData();
        when(wfc.post(any(String.class), any(String.class), any(Class.class)))
                .thenReturn(new ResponseEntity(response, HttpStatus.ACCEPTED));

        WavefrontEventDetailsDto eventDetails = wfn.retrieveEvents("dummy-event-name");
        Assert.assertTrue(eventDetails.getItems().size() > 0, " Retrieving events resulted in error");
    }

    @Test(description = " Validate retrieve events when wavefront API returns null")
    public void retrieveEventsWhenApiResponseIsNull() {
        WavefrontNotifier wfn = spy(new WavefrontNotifier(wfc));
        WavefrontEventSearchResponse response = wavefrontEventMockData.getRetrieveEventMockData();
        when(wfc.post(any(String.class), any(String.class), any(Class.class))).thenReturn(null);

        WavefrontEventDetailsDto eventDetails = wfn.retrieveEvents("dummy-event-name");
        Assert.assertNull(eventDetails, " Retrieving events resulted in error");
    }

    @Test(description = " Validate retrieve events when wavefront API returns non 2xx return")
    public void retrieveEventWhenWfApiReturnsFailure() {
        WavefrontNotifier wfn = spy(new WavefrontNotifier(wfc));
        WavefrontEventSearchResponse response = wavefrontEventMockData.getRetrieveEventMockData();
        when(wfc.post(any(String.class), any(String.class), any(Class.class)))
                .thenReturn(new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR));

        WavefrontEventDetailsDto eventDetails = wfn.retrieveEvents("dummy-event-name");
        Assert.assertNull(eventDetails, " Retrieving events resulted in error");
    }

    @Test(description = " Close event when retrieve events has failed. ")
    public void closeEventWhenEventsFailedToRetrieve() {
        WavefrontNotifier wfn = spy(new WavefrontNotifier(wfc));
        when(wfn.retrieveEvents(any(String.class))).thenReturn(null);
        boolean status = wfn.closeEvent(faultEventData, "dummy", "dummyid");
        Assert.assertFalse(status, "Event close should have failed");
    }

    @Test(description = " Validate Close event failing when retrieve events unable to find specified event ")
    public void closeEventWhenSpecifiedEventNotPresent() {
        WavefrontNotifier wfn = spy(new WavefrontNotifier(wfc));
        WavefrontEventDetailsDto eventDetails = wavefrontEventMockData.getEventDetailsDto();
        eventDetails.setItems(new ArrayList<>());
        doReturn(eventDetails).when(wfn).retrieveEvents(any(String.class));

        boolean status = wfn.closeEvent(faultEventData, "dummy", "dummyId");
        Assert.assertFalse(status, "Event close should have failed");
    }

    @Test(description = "Validating close event when specified event is not in ONGOING state .")
    public void closeEventWhenEventNotInOngoingState() {
        WavefrontNotifier wfn = spy(new WavefrontNotifier(wfc));
        WavefrontEventDetailsDto eventDetails = wavefrontEventMockData.getEventDetailsDto();
        WavefrontEventItemDto eventItem = eventDetails.getItems().get(0);
        eventItem.setRunningState("COMPLETED");
        doReturn(eventDetails).when(wfn).retrieveEvents(any(String.class));

        boolean status = wfn.closeEvent(faultEventData, "dummy", "dummId");
        Assert.assertFalse(status, "Event close should have failed when specified event is not in ONGOING state");
        verify(wfc, times(0)).delete(any(String.class), any(Class.class));
        verify(wfc, times(0)).post(any(String.class), any(String.class), any(Class.class));
    }

    @Test(description = "Validating close event when specified event is not having any end time")
    public void closeEventWhenEventIsNotHavingEndTime() {
        WavefrontNotifier wfn = spy(new WavefrontNotifier(wfc));
        String response = apiResponseMockData.getApiResponse(200);
        WavefrontEventDetailsDto eventDetails = wavefrontEventMockData.getEventDetailsDto();
        WavefrontEventItemDto eventItem = eventDetails.getItems().get(0);
        eventItem.setEndTime(0L);

        doReturn(eventDetails).when(wfn).retrieveEvents(any(String.class));
        when(wfc.delete(any(String.class), any(Class.class)))
                .thenReturn(new ResponseEntity(response, HttpStatus.ACCEPTED));
        when(wfc.post(any(String.class), any(String.class), any(Class.class)))
                .thenReturn(new ResponseEntity(response, HttpStatus.ACCEPTED));

        boolean status = wfn.closeEvent(faultEventData, "dummy", "dummyId");
        Assert.assertTrue(true, "Event close should have failed when specified event is not in ONGOING state");
        verify(wfc, times(0)).delete(any(String.class), any(Class.class));
        verify(wfc, times(1)).post(any(String.class), any(String.class), any(Class.class));
    }

    @Test(description = "Validating close event when specified event is in OnGoing state and end time is already set")
    public void validateCloseEventForEventWithEndTimeAlreadySet() {
        WavefrontNotifier wfn = spy(new WavefrontNotifier(wfc));
        String response = apiResponseMockData.getApiResponse(200);
        WavefrontEventDetailsDto eventDetails = wavefrontEventMockData.getEventDetailsDto();

        doReturn(eventDetails).when(wfn).retrieveEvents(any(String.class));
        when(wfc.delete(any(String.class), any(Class.class)))
                .thenReturn(new ResponseEntity(response, HttpStatus.ACCEPTED));
        when(wfc.post(any(String.class), any(String.class), any(Class.class)))
                .thenReturn(new ResponseEntity(response, HttpStatus.ACCEPTED));

        boolean status = wfn.closeEvent(faultEventData, "dummy-event-name", "dummyid");
        Assert.assertTrue(true, "Event close should have failed when specified event is not in ONGOING state");
        verify(wfc, times(1)).delete(any(String.class), any(Class.class));
        verify(wfc, times(1)).post(any(String.class), any(String.class), any(Class.class));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test(description = "Validating close event when specified event is in OnGoing state and end time is already set")
    public void validateCloseEventForEventWithEndTimeAndRunningStateEnded() {
        WavefrontNotifier wfn = spy(new WavefrontNotifier(wfc));
        String response = apiResponseMockData.getApiResponse(200);
        WavefrontEventDetailsDto eventDetails = wavefrontEventMockData.getEventDetailsDtoForEndedEvent();
        doReturn(eventDetails).when(wfn).retrieveEvents(any(String.class));
        when(wfc.delete(any(String.class), any(Class.class)))
                .thenReturn(new ResponseEntity(response, HttpStatus.ACCEPTED));
        when(wfc.post(any(String.class), any(String.class), any(Class.class)))
                .thenReturn(new ResponseEntity(response, HttpStatus.ACCEPTED));

        boolean status = wfn.closeEvent(faultEventData, "dummy-event-name", "dummyid");
        Assert.assertTrue(true, "Event close should have failed when specified event is not in ONGOING state");
        verify(wfc, times(1)).delete(any(String.class), any(Class.class));
        verify(wfc, times(1)).post(any(String.class), any(String.class), any(Class.class));
    }
}
