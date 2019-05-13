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

import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.model.enums.EndpointType;

/**
 * Repository class for Endpoint.
 *
 * @author kumargautam
 */
@Repository
public interface EndpointRepository extends CassandraRepository<EndpointSpec, String> {

    @AllowFiltering
    Optional<EndpointSpec> findById(String id);

    List<EndpointSpec> findAll();

    @AllowFiltering
    Optional<EndpointSpec> findByName(String endpointName);

    @Query("select * from EndPointSpec WHERE name IN ?0")
    List<EndpointSpec> findByNames(List<String> endpointName);

    @AllowFiltering
    List<EndpointSpec> findByEndPointType(EndpointType endPointType);

    @SuppressWarnings("unchecked")
    EndpointSpec save(EndpointSpec endpointSpec);

    @Query("DELETE from EndPointSpec WHERE name IN ?0")
    void deleteByNameIn(Collection<String> endpointNames);

    void deleteByName(String endpointName);

    Slice<EndpointSpec> findAll(Pageable pageable);

    @AllowFiltering
    List<EndpointSpec> findByCredentialsName(String credential);

}
