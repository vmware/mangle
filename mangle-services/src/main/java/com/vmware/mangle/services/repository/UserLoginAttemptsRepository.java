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

import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;

import com.vmware.mangle.cassandra.model.security.UserLoginAttempts;

/**
 * @author chetanc
 */
public interface UserLoginAttemptsRepository extends CassandraRepository<UserLoginAttempts, String> {

    @AllowFiltering
    @Query("SELECT * FROM userAttempt WHERE name = ?0")
    UserLoginAttempts findByUsername(String username);
}
