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

package com.vmware.mangle.services.updateutils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.faults.specs.EndpointGroupFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.FaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.SchedulerService;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.events.schedule.ScheduleUpdatedEvent;
import com.vmware.mangle.services.events.web.CustomEventPublisher;
import com.vmware.mangle.services.repository.EndpointRepository;
import com.vmware.mangle.services.scheduler.Scheduler;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author bkaranam
 */
@Log4j2
@Service
public class EndpointUpdateService {

    private EndpointRepository endpointRepository;
    private SchedulerService schedulerService;
    private TaskService taskService;
    private CustomEventPublisher eventPublisher;

    @Autowired
    public EndpointUpdateService(EndpointRepository endpointRepository, SchedulerService schedulerService,
            TaskService taskService, CustomEventPublisher eventPublisher) {
        this.endpointRepository = endpointRepository;
        this.schedulerService = schedulerService;
        this.taskService = taskService;
        this.eventPublisher = eventPublisher;
    }

    public EndpointSpec updateEndpointByEndpointName(String name, EndpointSpec endpointSpec) throws MangleException {
        log.info("Updating Endpoint by Endpoint name : " + name);
        if (name != null && endpointSpec != null) {
            Optional<EndpointSpec> optional = endpointRepository.findByName(name);
            if (!optional.isPresent()) {
                throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.ENDPOINT_NAME, name);
            }
            EndpointSpec dbEndpointSpec = optional.orElse(null);

            if (dbEndpointSpec != null && !endpointSpec.getEndPointType().equals(dbEndpointSpec.getEndPointType())) {
                throw new MangleException(ErrorCode.DUPLICATE_RECORD_FOR_ENDPOINT, endpointSpec.getName(),
                        dbEndpointSpec.getEndPointType());
            }
            EndpointSpec updatedEndpointSpec = endpointRepository.save(endpointSpec);
            handleScheduledJobsOnEndpoint(name);
            return updatedEndpointSpec;
        } else {
            log.error(ErrorConstants.ENDPOINT_NAME + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.ENDPOINT_NAME);
        }
    }

    private void handleScheduledJobsOnEndpoint(String endpointName) {
        List<String> scheduledJobIds = getActiveSchedulesOnEndpoint(endpointName);
        scheduledJobIds.parallelStream().forEach(jobId -> {
            eventPublisher.publishEvent(new ScheduleUpdatedEvent(jobId, Scheduler.RESYNC_SCHEDULE));
        });
    }

    public List<String> getActiveSchedulesOnEndpoint(String endpointName) {
        List<String> scheduledTaskIds = new ArrayList<>();
        List<String> activeScheduleIds = schedulerService.getActiveScheduleJobs();
        if (!CollectionUtils.isEmpty(activeScheduleIds)) {
            List<Task<TaskSpec>> tasks = taskService.getTasksByIds(activeScheduleIds);
            List<String> endpointNames = endpointRepository.findByEndPointType(EndpointType.ENDPOINT_GROUP).stream()
                    .filter(e -> e.getEndpointNames().contains(endpointName)).map(EndpointSpec::getName)
                    .collect(Collectors.toList());
            endpointNames.add(endpointName);

            for (Task<TaskSpec> task : tasks) {
                String taskEndpointName;
                if (task.getTaskData() instanceof FaultSpec) {
                    taskEndpointName = ((FaultSpec) task.getTaskData()).getEndpointName();
                } else if (task.getTaskData() instanceof K8SFaultTriggerSpec) {
                    taskEndpointName = ((K8SFaultTriggerSpec) task.getTaskData()).getFaultSpec().getEndpointName();
                } else {
                    taskEndpointName =
                            ((EndpointGroupFaultTriggerSpec) task.getTaskData()).getFaultSpec().getEndpointName();
                }
                if (endpointNames.contains(taskEndpointName)) {
                    scheduledTaskIds.add(task.getId());
                }
            }
        }
        return scheduledTaskIds;
    }
}
