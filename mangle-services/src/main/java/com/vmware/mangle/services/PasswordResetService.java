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

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vmware.mangle.cassandra.model.security.PasswordReset;
import com.vmware.mangle.services.repository.PasswordResetRepository;

/**
 *
 *
 * @author chetanc
 */
@Service
@Log4j2
public class PasswordResetService {

    private PasswordResetRepository repository;

    @Autowired
    public PasswordResetService(PasswordResetRepository repository) {
        this.repository = repository;
    }

    public boolean readResetStatus() {
        log.info("Retrieving default user password status");
        List<PasswordReset> passwordResetList = repository.findAll();
        PasswordReset passwordReset = passwordResetList.get(0);
        return passwordReset.isReset();
    }

    public boolean updateResetStatus() {
        log.info("Updating default user password status");
        PasswordReset persistedPasswordReset = repository.findAll().get(0);
        persistedPasswordReset.setReset(true);
        repository.save(persistedPasswordReset);
        return true;
    }

}
