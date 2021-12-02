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
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import com.vmware.mangle.model.notifier.dynatrace.event.AttachRules;
import com.vmware.mangle.model.notifier.dynatrace.event.CustomProperties;
import com.vmware.mangle.model.notifier.dynatrace.event.DynatraceEventDto;
import com.vmware.mangle.services.dto.FaultEventSpec;
import com.vmware.mangle.utils.ApiUtils;
import com.vmware.mangle.utils.clients.metricprovider.DynatraceApiClient;
import com.vmware.mangle.utils.constants.MetricProviderConstants;

/**
 * @author dbhat
 *
 *         Description: To send fault injection events from Mangle to Dynatrace metric provider. We
 *         construct the JSON body for Dynatrace event API and populate it with fault injection
 *         details. Dynatrace events API is used to send the fault injection to Dynatrace metric
 *         provider.
 */

@Log4j2
public class DynatraceEventNotifier implements MetricProviderNotifier {
    DynatraceApiClient dynatraceApiClient;

    public DynatraceEventNotifier(DynatraceApiClient dynatraceApiClient) {
        this.dynatraceApiClient = dynatraceApiClient;
    }

    @Override
    public boolean sendEvent(FaultEventSpec faultEventInfo) {
        DynatraceEventDto eventDetails = getEventSpec(faultEventInfo);
        log.debug("Fault injection event details constructed : ", eventDetails);
        if (validateEventData(eventDetails)) {
            if (send(eventDetails)) {
                log.debug("Sending the fault injection event to Dynatrace was successful");
                return true;
            }
        }
        log.error("Error: Failure in sending fault injection event to Dynatrace.");
        return false;
    }

    @Override
    public boolean closeEvent(FaultEventSpec faultEventInfo, String taskID, String extension) {
        if (extension.equals("com.vmware.mangle.faults.plugin.tasks.helpers.SystemResourceFaultTaskHelper2")) {
            return sendEvent(faultEventInfo);
        }
        return true;
    }

    public DynatraceEventDto getEventSpec(FaultEventSpec faultEventSpec) {
        log.debug("Constructing the Dynatrace Event Details from Fault injection details: ", faultEventSpec);

        DynatraceEventDto dynatraceEventSpec = new DynatraceEventDto();
        dynatraceEventSpec.setAnnotationType(MetricProviderConstants.MANGLE_FAULT_EVENT_TYPE);
        dynatraceEventSpec.setSource(MetricProviderConstants.EVENT_SOURCE);
        dynatraceEventSpec.setAnnotationDescription(getAnnotationDescription(faultEventSpec));
        dynatraceEventSpec.setCustomProperties(getCustomProperties(faultEventSpec));
        dynatraceEventSpec.setEventType(MetricProviderConstants.DYNATRACE_EVENT_TYPE);
        dynatraceEventSpec.setStartTime(faultEventSpec.getFaultStartTimeInEpoch());
        dynatraceEventSpec.setEndTime(faultEventSpec.getFaultEndTimeInEpoch());
        dynatraceEventSpec.setAttachRules(getAttachRules(faultEventSpec));
        return dynatraceEventSpec;
    }

    private String getEventName(FaultEventSpec faultEventInfo) {
        String eventName = faultEventInfo.getFaultName() + MetricProviderConstants.HYPHEN + faultEventInfo.getTaskId();
        log.debug("Setting the Datadog Event Name to : " + eventName);
        return eventName;
    }

    private String getAnnotationDescription(FaultEventSpec faultEventSpec) {
        StringBuilder annotationDescription = new StringBuilder();
        annotationDescription.append(faultEventSpec.getFaultName());
        annotationDescription.append(" ");
        String faultStatus =
                faultEventSpec.getFaultStatus().contains("INJECTED") ? "IN PROGRESS" : faultEventSpec.getFaultStatus();
        annotationDescription.append(faultStatus);
        return annotationDescription.toString();
    }

    private CustomProperties getCustomProperties(FaultEventSpec faultEventSpec) {
        CustomProperties customProperties = new CustomProperties();
        customProperties.setTitle(getEventName(faultEventSpec));
        customProperties.setDescription(getEventDetails(faultEventSpec));
        customProperties.setFaultInjectionStatus(faultEventSpec.getFaultStatus());
        return customProperties;
    }

    private List<String> getArtifactIds(FaultEventSpec faultEventSpec) {
        List<String> artifactIds = new ArrayList<String>();
        if (null == faultEventSpec.getTags() || faultEventSpec.getTags().isEmpty()) {
            log.warn("Empty tags from the fault spec. Hence, Dynatrace event will even have empty artifact IDs");
        } else {
            for (Map.Entry<String, String> tag : faultEventSpec.getTags().entrySet()) {
                if (tag.getKey().toLowerCase().contains(MetricProviderConstants.DYNATRACE_ARTIFACT)) {
                    artifactIds.add(tag.getValue());
                }
            }
        }
        log.debug("Following artifact IDs are populated from fault spec: " + artifactIds);
        return artifactIds;
    }

    private AttachRules getAttachRules(FaultEventSpec faultEventSpec) {
        AttachRules attachRules = new AttachRules();
        // We will be adding TagRules object as well in future.
        attachRules.setEntityIds(getArtifactIds(faultEventSpec));
        return attachRules;
    }

    @SuppressWarnings("unchecked")
    private boolean send(DynatraceEventDto eventDetail) {
        ResponseEntity<String> response =
                (ResponseEntity<String>) dynatraceApiClient.post(MetricProviderConstants.DYNATRACE_API_SEND_EVENT,
                        DynatraceApiClient.objectToJson(eventDetail), String.class);
        log.debug("Send Event API Response : " + response);
        return !(StringUtils.isEmpty(response)) && (ApiUtils.isResponseCodeSuccess(response.getStatusCode().value()));
    }

    private boolean validateEventData(DynatraceEventDto eventDetail) {
        log.debug("Validating the fault injection event before sending to Dynatrace");
        if (StringUtils.isEmpty(eventDetail.getCustomProperties().getTitle())
                || eventDetail.getAttachRules().getEntityIds().isEmpty()) {
            log.error("Mandatory properties: Fault Event Title OR Artifact IDs are missing.");
            return false;
        }
        return true;
    }
}
