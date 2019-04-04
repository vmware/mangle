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

import com.vmware.mangle.cassandra.model.endpoint.CredentialsSpec;
import com.vmware.mangle.model.enums.EndpointType;

/**
 * Repository class for Credential.
 *
 * @author kumargautam
 */
@Repository
public interface CredentialRepository extends CassandraRepository<CredentialsSpec, String> {

    @AllowFiltering
    Optional<CredentialsSpec> findById(String id);

    List<CredentialsSpec> findAll();

    @AllowFiltering
    Optional<CredentialsSpec> findByName(String endpointName);

    @AllowFiltering
    List<CredentialsSpec> findByType(EndpointType endPointType);

    @SuppressWarnings("unchecked")
    CredentialsSpec save(CredentialsSpec endpointSpec);

    @Query("DELETE from CredentialsSpec WHERE name IN ?0")
    void deleteByNameIn(Collection<String> credentialNames);

    void deleteByName(String credentialName);

    Slice<CredentialsSpec> findAll(Pageable pageable);
}
