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
import java.util.Set;

import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.vmware.mangle.cassandra.model.scheduler.SchedulerSpec;
import com.vmware.mangle.model.enums.SchedulerStatus;

/**
 * @author ashrimali
 *
 */
@Repository
public interface SchedulerRepository extends CassandraRepository<SchedulerSpec, String> {

    @Override
    @AllowFiltering
    Optional<SchedulerSpec> findById(String id);

    @Override
    List<SchedulerSpec> findAll();

    @Override
    @SuppressWarnings({ "unchecked" })
    SchedulerSpec save(SchedulerSpec schedulerSpec);

    @Query("DELETE from schedulerSpec WHERE id IN ?0")
    void deleteByIdIn(Collection<String> taskIds);

    @Override
    Slice<SchedulerSpec> findAll(Pageable pageable);

    @AllowFiltering
    Optional<SchedulerSpec> findByIdAndStatus(String id, SchedulerStatus status);

    @AllowFiltering
    Optional<List<SchedulerSpec>> findByStatus(SchedulerStatus status);

    @Query(value = "select * from schedulerspec where id in ?0")
    Set<SchedulerSpec> findByIds(Collection<String> taskIds);
}
