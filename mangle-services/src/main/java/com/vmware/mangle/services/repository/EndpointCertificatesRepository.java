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

import com.vmware.mangle.cassandra.model.endpoint.CertificatesSpec;
import com.vmware.mangle.model.enums.EndpointType;

/**
 * Repository class for Credential.
 *
 * @author bkaranam
 */
@Repository
public interface EndpointCertificatesRepository extends CassandraRepository<CertificatesSpec, String> {

    @AllowFiltering
    Optional<CertificatesSpec> findById(String id);

    List<CertificatesSpec> findAll();

    @AllowFiltering
    Optional<CertificatesSpec> findByName(String endpointName);

    @Query("select * from CertificatesSpec WHERE name IN ?0")
    List<CertificatesSpec> findByNames(Collection<String> certificateNames);

    @AllowFiltering
    List<CertificatesSpec> findByType(EndpointType endPointType);

    @SuppressWarnings("unchecked")
    CertificatesSpec save(CertificatesSpec endpointSpec);

    @Query("DELETE from CertificatesSpec WHERE name IN ?0")
    void deleteByNameIn(Collection<String> certificateNames);

    void deleteByName(String credentialName);

    Slice<CertificatesSpec> findAll(Pageable pageable);
}
