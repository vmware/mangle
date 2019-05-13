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

package com.vmware.mangle.services.deletionutils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.faults.specs.FaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.model.response.DeleteOperationResponse;
import com.vmware.mangle.services.SchedulerService;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.repository.EndpointRepository;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author chetanc
 *
 *
 */
@Log4j2
@Service
public class EndpointDeletionService {

    private EndpointRepository endpointRepository;
    private SchedulerService schedulerService;
    private TaskService taskService;

    @Autowired
    public EndpointDeletionService(EndpointRepository endpointRepository, SchedulerService schedulerService,
            TaskService taskService) {
        this.endpointRepository = endpointRepository;
        this.schedulerService = schedulerService;
        this.taskService = taskService;
    }

    public boolean deleteEndpointByName(String endpointName) throws MangleException {
        log.info("Deleting Endpoint by names : " + endpointName);
        if (endpointName != null && !endpointName.isEmpty()) {
            endpointRepository.deleteByName(endpointName);
            return true;
        } else {
            log.error(ErrorConstants.ENDPOINT_NAME + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.ENDPOINT_NAME);
        }
    }

    public DeleteOperationResponse deleteEndpointByNames(List<String> endpointNames) throws MangleException {
        log.info("Deleting endpoints by names : " + endpointNames);
        if (!CollectionUtils.isEmpty(endpointNames)) {
            List<EndpointSpec> persistedEndpointSpecs = endpointRepository.findByNames(endpointNames);
            List<String> persistedendpointNames =
                    persistedEndpointSpecs.stream().map(EndpointSpec::getName).collect(Collectors.toList());
            endpointNames.removeAll(persistedendpointNames);
            if (!endpointNames.isEmpty()) {
                throw new MangleException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.ENDPOINT_NAME,
                        persistedendpointNames.toString());
            }


            DeleteOperationResponse response = new DeleteOperationResponse();
            response.setAssociations(processEndpointDeletionPrecheck(endpointNames));
            if (response.getAssociations().size() > 0) {
                return response;
            }

            endpointRepository.deleteByNameIn(persistedendpointNames);
            return response;
        } else {
            log.error(ErrorConstants.ENDPOINT_NAME + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.ENDPOINT_NAME);
        }
    }

    /**
     * find if the given list of endpoints are bound to the schedules that are active
     *
     * Active schedules are those that are either in Scheduled or Initializing or Paused
     *
     * @param endpointNames
     *            Endpoint which needs to be verified
     *
     * @return Mapping between the endpoints to tasks, to which the endpoints are bound
     */
    private Map<String, List<String>> processEndpointDeletionPrecheck(List<String> endpointNames) {
        List<String> activeScheduleIds = schedulerService.getActiveScheduleJobs();
        List<Task<TaskSpec>> tasks = taskService.getTasksByIds(activeScheduleIds);

        Map<String, List<String>> endpointToTasksMapping = new HashMap<>();

        for (Task<TaskSpec> task : tasks) {
            String endpointName;
            if (task.getTaskData() instanceof FaultSpec) {
                endpointName = ((FaultSpec) task.getTaskData()).getEndpointName();
            } else {
                endpointName = ((K8SFaultTriggerSpec) task.getTaskData()).getFaultSpec().getEndpointName();
            }
            if (endpointToTasksMapping.containsKey(endpointName)) {
                endpointToTasksMapping.get(endpointName).add(task.getId());
            } else {
                endpointToTasksMapping.put(endpointName, new ArrayList<>(Arrays.asList(task.getId())));
            }
        }

        Map<String, List<String>> assocations = new HashMap<>();

        for (String endpoint : endpointNames) {
            if (!CollectionUtils.isEmpty(endpointToTasksMapping.get(endpoint))) {
                assocations.put(endpoint, endpointToTasksMapping.get(endpoint));
            }
        }

        return assocations;
    }

}
