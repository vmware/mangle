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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.scheduler.SchedulerSpec;
import com.vmware.mangle.model.enums.OperationStatus;
import com.vmware.mangle.services.cassandra.model.events.basic.EntityDeletedEvent;
import com.vmware.mangle.services.events.web.CustomEventPublisher;
import com.vmware.mangle.services.repository.SchedulerRepository;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Service class for supporting deletion operations for scheduled objects
 *
 * @author chetanc
 */
@Component
public class SchedulerDeletionService {

    private SchedulerRepository schedulerRepository;
    private TaskDeletionService taskDeletionService;
    private CustomEventPublisher eventPublisher;

    @Autowired
    public SchedulerDeletionService(SchedulerRepository schedulerRepository,
            TaskDeletionService taskDeletionService, CustomEventPublisher eventPublisher) {
        this.schedulerRepository = schedulerRepository;
        this.taskDeletionService = taskDeletionService;
        this.eventPublisher = eventPublisher;
    }

    public List<String> verifyDeletionPrecheck(List<String> jobIds) throws MangleException {
        Set<SchedulerSpec> persistedSpecs = schedulerRepository.findByIds(jobIds);
        List<String> persistedSpecIds = persistedSpecs.stream().map(SchedulerSpec::getId).collect(Collectors.toList());
        jobIds.removeAll(persistedSpecIds);

        if (!jobIds.isEmpty()) {
            throw new MangleException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.SCHEDULE, jobIds.toString());
        }
        return persistedSpecIds;
    }

    public void deleteSchedulerDetailsByJobIds(List<String> jobIds, boolean deleteAssociatedTask)
            throws MangleException {
        if (!CollectionUtils.isEmpty(jobIds)) {
            List<String> persistedSpecIds = verifyDeletionPrecheck(jobIds);
            schedulerRepository.deleteByIdIn(persistedSpecIds);
            persistedSpecIds.forEach(s -> eventPublisher.publishAnEvent(new EntityDeletedEvent(s,
                    SchedulerSpec.class.getName())));
            if (deleteAssociatedTask) {
                taskDeletionService.deleteTasksByIds(persistedSpecIds);
            }
        }
    }

    public OperationStatus deleteSchedulerDetailsByJobId(String jobId) throws MangleException {
        if (schedulerRepository.findById(jobId).isPresent()) {
            schedulerRepository.deleteByIdIn(Collections.singletonList(jobId));
            eventPublisher.publishAnEvent(new EntityDeletedEvent(jobId, SchedulerSpec.class.getName()));
            return OperationStatus.SUCCESS;
        } else {
            throw new MangleException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.SCHEDULE, jobId);
        }
    }

}
