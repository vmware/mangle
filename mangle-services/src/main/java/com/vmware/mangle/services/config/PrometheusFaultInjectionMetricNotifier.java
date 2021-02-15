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

package com.vmware.mangle.services.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tags;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.services.dto.FaultEventSpec;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.constants.MetricProviderConstants;
import com.vmware.mangle.utils.helpers.notifiers.MetricProviderNotifier;

/**
 * @author hkilari
 * @author kumargautam
 */
@Component
@Log4j2
public class PrometheusFaultInjectionMetricNotifier implements MetricProviderNotifier {

    @Autowired
    private PrometheusMeterRegistry prometheusMeterRegistry;
    private final ExecutorService threadPool;
    private final Map<String, Meter> metrics;

    public PrometheusFaultInjectionMetricNotifier() {
        this.threadPool = Executors.newCachedThreadPool();
        this.metrics = new HashMap<>();
    }

    @Override
    public boolean sendEvent(FaultEventSpec faultEventSpec) {
        Counter counter =
                Counter.builder("mangle_fault_injection").description("indicates instance count of the object")
                        .tags(getTags(faultEventSpec)).register(prometheusMeterRegistry);
        counter.increment(1.0);
        metrics.put(getEventName(faultEventSpec), counter);

        //Closing Event for Auto remedition fault
        if (faultEventSpec.getTimeoutInMilliseconds() != null) {
            scheduledCloseEvent(faultEventSpec);
        } else if (faultEventSpec.getFaultName().contains(TaskType.REMEDIATION.name())) {
            faultEventSpec.setTimeoutInMilliseconds(10000);
            scheduledCloseEvent(faultEventSpec);
        }
        log.info("Sending event to Prometheus was successful!");
        return true;
    }

    private Tags getTags(FaultEventSpec faultEventSpec) {
        Tags tags = Tags.of("description", faultEventSpec.getFaultDescription())
                .and("endtime", faultEventSpec.getFaultEndTimeInEpoch() + "")
                .and("starttime", faultEventSpec.getFaultStartTimeInEpoch() + "")
                .and("faultName", faultEventSpec.getFaultName())
                .and("faulteventtype", faultEventSpec.getFaultEventType()).and("taskid", faultEventSpec.getTaskId())
                .and("faultStatus", faultEventSpec.getFaultStatus()).and("FaultID_Remediated", "");
        for (Entry<String, String> entry : faultEventSpec.getTags().entrySet()) {
            tags = Tags.of(tags).and(entry.getKey(), entry.getValue());
        }
        return tags;
    }

    private String getEventName(FaultEventSpec faultEventInfo) {
        String eventName = faultEventInfo.getFaultName() + MetricProviderConstants.HYPHEN + faultEventInfo.getTaskId();
        log.debug("Setting the Prometheus Event Name to : {}", eventName);
        return eventName;
    }

    @Override
    public boolean closeEvent(FaultEventSpec faultEventInfo, String taskID, String taskExtension) {
        String eventName = getEventName(faultEventInfo);
        if (metrics.containsKey(eventName)) {
            prometheusMeterRegistry.remove(metrics.get(eventName));
            metrics.remove(eventName);
        }
        return true;
    }

    public boolean closeEvent(FaultEventSpec faultEventInfo) {
        return closeEvent(faultEventInfo, faultEventInfo.getTaskId(), "");
    }

    private void scheduledCloseEvent(FaultEventSpec faultEventSpec) {
        final Runnable runnable = () -> {
            Thread.currentThread().setName(getEventName(faultEventSpec));
            CommonUtils.delayInMilliSeconds(faultEventSpec.getTimeoutInMilliseconds());
            closeEvent(faultEventSpec);
        };
        this.threadPool.execute(runnable);
    }
}