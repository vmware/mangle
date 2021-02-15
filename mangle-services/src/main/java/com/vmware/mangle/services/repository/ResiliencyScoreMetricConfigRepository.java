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

import java.util.List;
import java.util.Optional;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreMetricConfig;

/**
 * @author dbhat
 */
@Repository
public interface ResiliencyScoreMetricConfigRepository
        extends CassandraRepository<ResiliencyScoreMetricConfig, String> {

    @SuppressWarnings("unchecked")
    ResiliencyScoreMetricConfig save(ResiliencyScoreMetricConfig resiliencyScoreMetricConfig);

    Optional<ResiliencyScoreMetricConfig> findByName(String name);

    List<ResiliencyScoreMetricConfig> findAll();

    @Query("DELETE from resiliencyscore_metric_config WHERE name = ?0")
    void deleteByName(String name);
}