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

import com.vmware.mangle.cassandra.model.plugin.PluginDetails;

/**
 * PluginDetails Repository class.
 *
 * @author kumargautam
 */
public interface PluginDetailsRepository extends CassandraRepository<PluginDetails, String> {

    @AllowFiltering
    Optional<PluginDetails> findByPluginId(String pluginId);

    List<PluginDetails> findAll();

    @SuppressWarnings("unchecked")
    PluginDetails save(PluginDetails pluginDetails);

    @Query("DELETE FROM plugininfo WHERE pluginid=?0")
    void deleteByPluginId(String pluginId);

    @Query("DELETE from PluginInfo WHERE pluginId IN ?0")
    void deleteByPluginIdIn(Collection<String> pluginIds);
}
