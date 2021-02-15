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

import com.vmware.mangle.cassandra.model.slack.Notifier;

/**
 * This class is used to perform crudq operation for Slack info.
 *
 * @author kumargautam
 */
@Repository
public interface NotifierRepository extends CassandraRepository<Notifier, String> {

    List<Notifier> findAll();

    @AllowFiltering
    Optional<Notifier> findByName(String name);

    @Query("select * from Notifier WHERE name IN ?0")
    List<Notifier> findByNameIn(Collection<String> names);

    @SuppressWarnings("unchecked")
    Notifier save(Notifier slackInfo);

    @Query("DELETE from Notifier WHERE name IN ?0")
    void deleteByNameIn(Collection<String> names);
}
