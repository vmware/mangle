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

package com.vmware.mangle.services;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vmware.mangle.cassandra.model.security.Privilege;
import com.vmware.mangle.services.repository.PrivilegeRepository;

/**
 *
 *
 * @author chetanc
 */
@Service
public class PrivilegeService {

    private PrivilegeRepository repository;

    @Autowired
    public PrivilegeService(PrivilegeRepository repository) {
        this.repository = repository;
    }

    public Privilege getPrivilege(String privilegeName) {
        return repository.findByName(privilegeName);
    }

    public List<Privilege> getPrivilegeByNames(Set<String> privilegeNames) {
        return repository.findByNameIn(privilegeNames);
    }

    public List<Privilege> getAllPrivileges() {
        return repository.findAll();
    }
}
