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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;

/**
 * Repository class for Task<?>.
 *
 * @author kumargautam
 */
@Repository
public interface TaskRepository extends CassandraRepository<Task<TaskSpec>, String> {

    @AllowFiltering
    Optional<Task<TaskSpec>> findById(String id);

    List<Task<TaskSpec>> findAll();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    Task save(Task taskSpec);

    @Query("DELETE from task WHERE id IN ?0")
    void deleteByIdIn(Collection<String> taskIds);

    void deleteById(String taskId);

    Slice<Task<TaskSpec>> findAll(Pageable pageable);

    @Query(value = "Select * from task where isScheduledTask = ?0", allowFiltering = true)
    List<Task<TaskSpec>> findByIsScheduledTask(boolean isScheduledTask);

    @Query(value = "select * from task where id in ?0", allowFiltering = true)
    List<Task<TaskSpec>> findByIds(List<String> ids);

}
