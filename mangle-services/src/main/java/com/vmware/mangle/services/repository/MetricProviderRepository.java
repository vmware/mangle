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

import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.vmware.mangle.cassandra.model.metricprovider.MetricProviderSpec;
import com.vmware.mangle.model.enums.MetricProviderType;

/**
 * Repository for MetricProvider
 * @author ashrimali
 *
 */
@Repository
public interface MetricProviderRepository extends CassandraRepository<MetricProviderSpec, String> {
    List<MetricProviderSpec> findAll();

    @SuppressWarnings("unchecked")
    MetricProviderSpec save(MetricProviderSpec metricProviderSpec);

    @AllowFiltering
    Optional<MetricProviderSpec> findByName(String metricProviderName);

    @AllowFiltering
    Optional<MetricProviderSpec> findById(String id);

    @Query("DELETE from metricprovider WHERE name=?0")
    void deleteByName(String metricProviderName);

    Slice<MetricProviderSpec> findAll(Pageable pageable);

    @AllowFiltering
    List<MetricProviderSpec> findByMetricProviderType(MetricProviderType metricProviderType);
}
