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

package com.vmware.mangle.utils.helpers.notifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.model.notifier.WavefrontEventDetailsDto;
import com.vmware.mangle.model.notifier.WavefrontEventDto;
import com.vmware.mangle.model.notifier.WavefrontEventItemDto;
import com.vmware.mangle.model.notifier.WavefrontEventSearchDto;
import com.vmware.mangle.model.notifier.WavefrontEventSearchQueryDto;
import com.vmware.mangle.model.response.WavefrontEventSearchResponse;
import com.vmware.mangle.services.dto.FaultEventSpec;
import com.vmware.mangle.utils.ApiUtils;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.clients.metricprovider.WaveFrontServerClient;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.constants.MetricProviderConstants;

/*
 *@author dbhat
 *
 * Constructs the Wavefront Event DTO with details from FaultEventSpec and sends the event to Wavefront.
 */

@Log4j2
public class WavefrontNotifier implements Notifier {

    private WaveFrontServerClient wavefrontClient;

    public WavefrontNotifier(WaveFrontServerClient wavefrontClient) {
        this.wavefrontClient = wavefrontClient;
    }

    public boolean sendEvent(FaultEventSpec faultEventInfo) {
        WavefrontEventDto eventSpec = getEventSpec(faultEventInfo);
        log.debug("Event Data constructed: " + eventSpec.toString());
        if (validateEventData(eventSpec)) {
            if (send(eventSpec)) {
                log.debug("Sending event to Wavefront was successful");
                return true;
            }
        }
        log.error("Couldn't send the event to Wavefront. Event Data seems to have issues.");
        return false;
    }

    private WavefrontEventDto getEventSpec(FaultEventSpec faultEventInfo) {
        log.debug("Constructing the Wavefront event spec");
        WavefrontEventDto wavefrontEventData = new WavefrontEventDto();
        wavefrontEventData.setName(getEventName(faultEventInfo));
        wavefrontEventData.setStartTime(faultEventInfo.getFaultStartTimeInEpoch());
        wavefrontEventData.setEndTime(faultEventInfo.getFaultEndTimeInEpoch());
        wavefrontEventData.setAnnotations(getEventAnnotations(faultEventInfo));
        wavefrontEventData.setTags(getEventTags(faultEventInfo));
        log.debug("Wavefront Event data is populated and is : " + wavefrontEventData.toString());
        return wavefrontEventData;
    }

    private String getEventName(FaultEventSpec faultEventInfo) {
        String eventName = faultEventInfo.getFaultName() + MetricProviderConstants.HYPHEN + faultEventInfo.getTaskId();
        log.debug("Setting the Wavefront Name to : " + eventName);
        return eventName;
    }

    private Map<String, String> getEventAnnotations(FaultEventSpec faultEventInfo) {
        Map<String, String> annotations = new HashMap<>();
        annotations.put("severity", faultEventInfo.getFaultEventClassification());
        annotations.put("type", faultEventInfo.getFaultEventType());
        annotations.put("details", getEventDetails(faultEventInfo));
        log.debug("Setting the Event Annotations values to : " + annotations.toString());
        return annotations;
    }

    private String getEventDetails(FaultEventSpec faultEventInfo) {
        StringBuilder details = new StringBuilder();
        details.append(MetricProviderConstants.START_TIME_TEXT + faultEventInfo.getFaultStartTime());
        details.append(MetricProviderConstants.SEPERATOR + MetricProviderConstants.END_TIME_TEXT
                + faultEventInfo.getFaultEndTime());
        details.append(MetricProviderConstants.SEPERATOR + faultEventInfo.getFaultDescription());
        details.append(MetricProviderConstants.NEW_LINE + MetricProviderConstants.STATUS_TEXT
                + faultEventInfo.getFaultStatus());
        return details.toString();
    }

    private ArrayList<String> getEventTags(FaultEventSpec faultEventInfo) {
        ArrayList<String> tags = new ArrayList<>();
        if (null == faultEventInfo.getTags() || faultEventInfo.getTags().isEmpty()) {
            log.warn("Empty tags from the fault spec. Hence, wavefront event will even have empty tags");
            return tags;
        }
        for (Map.Entry<String, String> tag : faultEventInfo.getTags().entrySet()) {
            tags.add(MetricProviderConstants.DOUBLEQUOTE + tag.getKey() + MetricProviderConstants.COLON + tag.getValue()
                    + MetricProviderConstants.DOUBLEQUOTE);
        }
        log.debug("Following Tags are populated from fault spec: " + tags);
        return tags;
    }

    private boolean validateEventData(WavefrontEventDto wavefrontEventData) {
        log.debug("Validating the event data before sending to Wavefront ");
        if ((wavefrontEventData.getStartTime() == 0L) || (wavefrontEventData.getName().isEmpty())
                || (null == wavefrontEventData.getName())) {
            log.error("Event Name OR start time has invalid entries. Cannot send the event");
            return false;
        }
        log.debug("Validating of event data is successful");
        return true;
    }

    private boolean send(WavefrontEventDto eventSpec) {
        ResponseEntity<String> response =
                (ResponseEntity<String>) wavefrontClient.post(MetricProviderConstants.WAVEFRONT_API_SEND_EVENT,
                        RestTemplateWrapper.objectToJson(eventSpec), String.class);
        if (response == null) {
            log.error(" Sending event API has returned error. Sending the event to Wavefront has failed");
            return false;
        }
        return ApiUtils.isResponseCodeSuccess(response.getStatusCode().value());
    }

    public WavefrontEventDetailsDto retrieveEvents(String eventName) {
        WavefrontEventSearchQueryDto query = new WavefrontEventSearchQueryDto();
        query.setKey("name");
        query.setValue(eventName);
        query.setMatchingMethod("CONTAINS");
        query.setNegated(false);
        ArrayList<WavefrontEventSearchQueryDto> queries = new ArrayList<>();
        queries.add(query);
        WavefrontEventSearchDto search = new WavefrontEventSearchDto();
        search.setLimit(100);
        search.setSortTimeAscending(true);
        search.setTimeRange(new HashMap<String, Object>());
        search.setQuery(queries);

        log.debug("Event Search request Body: " + search.toString());
        ResponseEntity<WavefrontEventSearchResponse> apiResponse =
                (ResponseEntity<WavefrontEventSearchResponse>) wavefrontClient.post(
                        MetricProviderConstants.WAVEFRONT_SEARCH_EVENT, RestTemplateWrapper.objectToJson(search),
                        WavefrontEventSearchResponse.class);
        if (null != apiResponse && ApiUtils.isResponseCodeSuccess(apiResponse.getStatusCode().value())) {
            log.debug("Response is : " + apiResponse.toString());
            WavefrontEventSearchResponse response = apiResponse.getBody();
            WavefrontEventDetailsDto events = response.getResponse();
            return events;
        }
        log.error(" Request to retrieve events has failed. Returning null");
        return null;
    }

    /**
     * @param eventName:
     *            Event with the specified event Name to be closed. Please provide the complete
     *
     */
    public boolean closeEvent(String eventName) {
        WavefrontEventDetailsDto eventDetails = retrieveEvents(eventName);
        if (null != eventDetails && !CollectionUtils.isEmpty(eventDetails.getItems())) {
            ArrayList<WavefrontEventItemDto> events = eventDetails.getItems();
            WavefrontEventItemDto firstEventInTheList = events.get(0);

            if (firstEventInTheList.getRunningState().contains(MetricProviderConstants.WAVEFRONT_ONGOING_STATE)
                    && firstEventInTheList.getEndTime() > 0) {
                log.debug(
                        "The event is ONGoing with endtime is already set. The Event cannot be closed. Need to be recreated");
                deleteAndReCreateEvent(firstEventInTheList);
                return true;
            }
            if (firstEventInTheList.getRunningState().contains(MetricProviderConstants.WAVEFRONT_ONGOING_STATE)) {
                String api = String.format(MetricProviderConstants.WAVEFRONT_CLOSE_EVENT, firstEventInTheList.getId());
                ResponseEntity<String> response = (ResponseEntity<String>) wavefrontClient.post(api, "", String.class);
                if (null != response && ApiUtils.isResponseCodeSuccess(response.getStatusCode().value())) {
                    log.info(" Event : " + eventName + " with ID: " + firstEventInTheList.getId() + " is closed");
                    return true;
                }
                log.error("Closing of event: " + eventName + " corresponding ID: " + firstEventInTheList.getId()
                        + " has Failed");
                return false;
            }
            log.warn(" The event: " + firstEventInTheList.getName() + " with ID: " + firstEventInTheList.getId()
                    + " is in: " + firstEventInTheList.getRunningState() + " and hence, cannot be closed");
            return false;
        }
        log.warn("NO events found corresponding to event Name: " + eventName);
        return false;
    }

    private void deleteAndReCreateEvent(WavefrontEventItemDto eventInfo) {
        if (deleteEvent(eventInfo.getId())) {
            log.debug("Recreating the event: " + eventInfo.getName());
            WavefrontEventDto event = new WavefrontEventDto();
            event.setName(eventInfo.getName());
            event.setStartTime(eventInfo.getStartTime());
            event.setTags(eventInfo.getTags());
            event.setAnnotations(eventInfo.getAnnotations());
            String endTime =
                    CommonUtils.getTime(System.currentTimeMillis() + MetricProviderConstants.ONE_SECOND_IN_MILLIS);
            event.setEndTime(CommonUtils.getDateObjectFor(endTime).getTime());
            log.debug("Recreating the event with details: " + event.toString());
            if (send(event)) {
                log.debug("Recreating of the event was successful");
                return;
            }
            log.warn("Recreating of the event has Failed");
        }
    }

    private boolean deleteEvent(String eventId) {
        log.debug("Deleting the event with ID: " + eventId);
        String url = String.format(MetricProviderConstants.WAVEFRONT_DELETE_EVENT, eventId);
        log.debug("URL: " + url);
        ResponseEntity<String> response = (ResponseEntity<String>) wavefrontClient.delete(url, String.class);
        if (null != response && ApiUtils.isResponseCodeSuccess(response.getStatusCode().value())) {
            log.debug("Deleting of the event was successful");
            return true;
        }
        log.warn("Deleting of the event has failed");
        return false;
    }
}
