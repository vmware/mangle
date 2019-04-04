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

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vmware.mangle.model.enums.OperationStatus;
import com.vmware.mangle.services.repository.SchedulerRepository;

/**
 * Service class for supporting deletion operations for scheduled objects
 *
 * @author chetanc
 */
@Component
public class SchedulerDeletionService {

    private SchedulerRepository schedulerRepository;

    @Autowired
    public SchedulerDeletionService(SchedulerRepository schedulerRepository) {
        this.schedulerRepository = schedulerRepository;
    }

    public OperationStatus deleteSchedulerDetailsByJobIds(List<String> listOfIds) {
        if (!CollectionUtils.isEmpty(listOfIds)) {
            schedulerRepository.deleteByIdIn(listOfIds);
            return OperationStatus.SUCCESS;
        } else {
            return OperationStatus.FAILED;
        }
    }

    public OperationStatus deleteSchedulerDetailsByJobId(String jobId) {
        if (schedulerRepository.findById(jobId).isPresent()) {
            schedulerRepository.deleteById(jobId);
            return OperationStatus.SUCCESS;
        }
        return OperationStatus.FAILED;
    }

}
