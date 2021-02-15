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
import java.util.Map;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import com.vmware.mangle.model.notifier.DatadogEventDto;
import com.vmware.mangle.services.dto.FaultEventSpec;
import com.vmware.mangle.utils.ApiUtils;
import com.vmware.mangle.utils.clients.metricprovider.DatadogClient;
import com.vmware.mangle.utils.constants.MetricProviderConstants;

/**
 * @author dbhat
 *
 *         Code to construct the DataDog Event DTO with the help of FaultEventSpec and send the
 *         event details to Datadog
 *
 */
@Log4j2
public class DatadogEventNotifier implements MetricProviderNotifier {

    DatadogClient datadogClient;

    public DatadogEventNotifier(DatadogClient datadogClient) {
        this.datadogClient = datadogClient;
    }

    @Override
    public boolean sendEvent(FaultEventSpec faultEventInfo) {
        DatadogEventDto eventSpec = getEventSpec(faultEventInfo);
        log.debug("Event Data constucted: " + eventSpec.toString());
        if (validateEventData(eventSpec)) {
            if (send(eventSpec)) {
                log.debug("Sending the fault event to Datadog was successful");
            }
            return true;
        }
        log.error("Couldn't send the event to Datadog. Validation of Event Spec has failed");
        return false;
    }

    public DatadogEventDto getEventSpec(FaultEventSpec faultEventInfo) {
        log.debug("Constructing the Datadog event spec");
        DatadogEventDto datadogEventData = new DatadogEventDto();
        datadogEventData.setTitle(getEventName(faultEventInfo));
        datadogEventData.setSource_type_name(faultEventInfo.getFaultEventType());
        datadogEventData.setAlert_type(faultEventInfo.getFaultEventClassification());
        datadogEventData.setText(getEventDetails(faultEventInfo));
        datadogEventData.setTags(getEventTags(faultEventInfo));
        log.debug("Datadog Event data is populated and is : " + datadogEventData.toString());
        return datadogEventData;
    }

    private String getEventName(FaultEventSpec faultEventInfo) {
        String eventName = faultEventInfo.getFaultName() + MetricProviderConstants.HYPHEN + faultEventInfo.getTaskId();
        log.debug("Setting the Datadog Event Name to : " + eventName);
        return eventName;
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

    private boolean validateEventData(DatadogEventDto datadogEventData) {
        log.debug("Validating the event data before sending to Datadog ");
        if ((StringUtils.isEmpty(datadogEventData.getTitle())) || StringUtils.isEmpty(datadogEventData.getText())) {
            log.error("Event Name OR start time has invalid entries. Cannot send the event");
            return false;
        }
        log.debug("Validating of event data is successful");
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean send(DatadogEventDto eventSpec) {
        ResponseEntity<String> response =
                (ResponseEntity<String>) datadogClient.post(MetricProviderConstants.DATADOG_API_SEND_EVENT,
                        DatadogClient.objectToJson(eventSpec), String.class);
        log.debug("API Response : " + response);
        return !(StringUtils.isEmpty(response)) && (ApiUtils.isResponseCodeSuccess(response.getStatusCode().value()));
    }

    // Nothing to implement in close Event for now.
    @Override
    public boolean closeEvent(FaultEventSpec faultEventInfo, String taskID, String extension) {
        if (extension.equals("com.vmware.mangle.faults.plugin.tasks.helpers.SystemResourceFaultTaskHelper2")) {
            return sendEvent(faultEventInfo);
        }
        return true;
    }
}
