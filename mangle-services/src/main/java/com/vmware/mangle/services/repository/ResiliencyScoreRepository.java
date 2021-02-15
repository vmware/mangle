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

package com.vmware.mangle.services.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreTask;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;


/**
 * @author dbhat
 *
 */
@Repository
public interface ResiliencyScoreRepository extends CassandraRepository<ResiliencyScoreTask, String> {
    @SuppressWarnings("unchecked")
    ResiliencyScoreTask save(ResiliencyScoreTask resiliencyScore);

    List<ResiliencyScoreTask> findAll();

    @AllowFiltering
    Optional<ResiliencyScoreTask> findById(String id);

    @AllowFiltering
    Optional<ResiliencyScoreTask> findByServiceName(String serviceFamilyName, String serviceName);

    @AllowFiltering
    List<ResiliencyScoreTask> findByTaskStatus(TaskStatus taskStatus);

    @Query("DELETE from resiliencyScore_task WHERE id = ?0")
    void deleteById(String taskId);

    @Query("DELETE from resiliencyScore_task WHERE id IN ?0")
    void deleteByIdIn(Collection<String> taskIds);

    @Query("select * from resiliencyScore_task WHERE id IN ?0")
    List<ResiliencyScoreTask> findByIds(Collection<String> taskIds);
}
