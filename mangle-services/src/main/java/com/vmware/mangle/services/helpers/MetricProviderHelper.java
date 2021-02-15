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

package com.vmware.mangle.services.helpers;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.metricprovider.MetricProviderSpec;
import com.vmware.mangle.cassandra.model.tasks.RemediableTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.enums.MetricProviderType;
import com.vmware.mangle.services.config.MangleMetricsConfiguration;
import com.vmware.mangle.services.config.PrometheusFaultInjectionMetricNotifier;
import com.vmware.mangle.services.dto.FaultEventSpec;
import com.vmware.mangle.services.repository.MetricProviderRepository;
import com.vmware.mangle.task.framework.helpers.AbstractCommandExecutionTaskHelper.SubStage;
import com.vmware.mangle.task.framework.metric.providers.MetricProviderClientFactory;
import com.vmware.mangle.utils.PopulateFaultEventData;
import com.vmware.mangle.utils.clients.metricprovider.DatadogClient;
import com.vmware.mangle.utils.clients.metricprovider.MetricProviderClient;
import com.vmware.mangle.utils.clients.metricprovider.WaveFrontServerClient;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.constants.MetricProviderConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.helpers.notifiers.DatadogEventNotifier;
import com.vmware.mangle.utils.helpers.notifiers.MetricProviderNotifier;
import com.vmware.mangle.utils.helpers.notifiers.WavefrontNotifier;

/**
 * @author dbhat
 *
 */
@Component
@Log4j2
public class MetricProviderHelper {
    @Autowired
    private MetricProviderClientFactory metrciProviderClientFactory;

    @Autowired
    private MetricProviderRepository metricProviderRepository;

    @Autowired
    @Lazy
    private PrometheusFaultInjectionMetricNotifier prometheusFaultInjectionMetricNotifier;

    @Autowired
    private MangleMetricsConfiguration mangleMetricsConfiguration;


    public MetricProviderNotifier getActiveNotificationProvider() {
        try {
            MetricProviderSpec activeMetricProvider = getActiveMetricProvider();
            if (null == activeMetricProvider) {
                log.debug(" No Active metric providers are found. ");
                return null;
            }
            if (activeMetricProvider.getMetricProviderType() == MetricProviderType.PROMETHEUS) {
                return prometheusFaultInjectionMetricNotifier;
            }
            MetricProviderClient client = metrciProviderClientFactory.getMetricProviderClient(activeMetricProvider);
            if (client instanceof DatadogClient) {
                DatadogClient datadogClient = (DatadogClient) client;
                return new DatadogEventNotifier(datadogClient);
            }
            if (client instanceof WaveFrontServerClient) {
                WaveFrontServerClient wavefrontClient = (WaveFrontServerClient) client;
                return new WavefrontNotifier(wavefrontClient);
            }
            return null;
        } catch (MangleException e) {
            return null;
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void sendFaultEvent(Task task) {
        log.debug("TaskCreatedEvent", "Created Task: " + task.getClass().getName() + " With Id: " + task.getId());
        MetricProviderNotifier activeNotifier = getActiveNotificationProvider();
        if (activeNotifier == null) {
            log.warn(
                    "Cannot find an active metric provider. Please check if the metric providers are created and marked as Active");
            log.warn("Cannot send events to Metric Provider");
            return;
        }

        if (!hasChildTasks(task)) {
            log.debug(" Task is of type: Parent and contains Child tasks. We will not send event for Parent task");
            return;
        }
        if (task.getTaskData() instanceof CommandExecutionFaultSpec
                || task.getTaskData() instanceof K8SFaultTriggerSpec) {
            PopulateFaultEventData populateFaultEventData = new PopulateFaultEventData(task);
            FaultEventSpec faultEventInfo = populateFaultEventData.getFaultEventSpec();
            if (faultEventInfo == null) {
                log.error("Can not find valid data to send the event.");
                return;
            }
            log.debug("TaskCompleted Event is generated and here are the details: " + faultEventInfo.toString());

            if (task.getTaskType().equals(TaskType.INJECTION)
                    && (task.getExtensionName()
                            .equals("com.vmware.mangle.faults.plugin.tasks.helpers.SystemResourceFaultTaskHelper2"))
                    && (task.getTaskStatus() == TaskStatus.COMPLETED || task.getTaskStatus() == TaskStatus.FAILED)) {
                if (!task.getTaskSubstage().equals(SubStage.COMPLETED.name())) {
                    activeNotifier.sendEvent(faultEventInfo);
                    return;
                }
                activeNotifier.closeEvent(faultEventInfo, task.getId(), task.getExtensionName());
            } else {
                activeNotifier.sendEvent(faultEventInfo);
                if (task.getTaskType() == TaskType.REMEDIATION && !(task.getExtensionName()
                        .equals("com.vmware.mangle.faults.plugin.tasks.helpers.SystemResourceFaultTaskHelper2"))) {
                    activeNotifier.closeEvent(faultEventInfo, ((RemediableTask) task).getInjectionTaskId(),
                            task.getExtensionName());
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private boolean hasChildTasks(Task task) {
        if (null != task.getTriggers().peek()) {
            TaskTrigger trigger = (TaskTrigger) task.getTriggers().peek();
            return (CollectionUtils.isEmpty(trigger.getChildTaskIDs()));
        }
        log.warn("The task doesn't have triggers even. Can't have child tasks");
        return false;
    }

    private String getFaultEventName(String faultName, TaskType taskType, String taskId) {
        StringBuilder eventName = new StringBuilder();
        eventName.append(faultName.split(MetricProviderConstants.HYPHEN)[0]);
        eventName.append(MetricProviderConstants.HYPHEN);
        eventName.append(taskType);
        eventName.append(MetricProviderConstants.HYPHEN);
        eventName.append(taskId);

        return eventName.toString();
    }

    public MetricProviderSpec getActiveMetricProvider() {
        log.debug("Finding active metric provider.");
        if (!StringUtils.isEmpty(mangleMetricsConfiguration.getActiveMetricProvider())) {
            return this.metricProviderRepository.findByName(mangleMetricsConfiguration.getActiveMetricProvider())
                    .orElse(null);
        } else {
            log.warn(ErrorConstants.NO_ACTIVE_METRIC_PROVIDER);
            return null;
        }
    }
}
