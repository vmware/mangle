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

import java.util.Date;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.vmware.mangle.cassandra.model.security.User;
import com.vmware.mangle.cassandra.model.security.UserLoginAttempts;
import com.vmware.mangle.services.repository.UserLoginAttemptsRepository;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author chetanc
 */
@Service
@Log4j2
public class UserLoginAttemptsService {

    private UserLoginAttemptsRepository repository;
    private UserService userService;

    @Autowired
    public UserLoginAttemptsService(UserLoginAttemptsRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    public UserLoginAttempts getUserAttemptsForUser(String username) {
        return repository.findByUsername(username);
    }

    public void updateFailAttempts(String username) {
        UserLoginAttempts userAttempts = getUserAttemptsForUser(username);
        if (ObjectUtils.isEmpty(userAttempts)) {
            userAttempts = new UserLoginAttempts(username, 1, new Date());
        } else {
            userAttempts.setAttempts(userAttempts.getAttempts() + 1);
            userAttempts.setLastAttempt(new Date());
        }
        User user = userService.getUserByName(username);
        if (null != user) {
            user.setAccountLocked(true);
            userService.terminateUserSession(username);
            try {
                userService.updateUser(user);
            } catch (MangleException e) {
                log.error("Failed to update user, exception {}", e.getMessage());
            }
        }
        repository.save(userAttempts);
    }

    public void resetFailAttempts(String username) {
        UserLoginAttempts userLoginAttempts = getUserAttemptsForUser(username);
        if (ObjectUtils.isEmpty(userLoginAttempts)) {
            userLoginAttempts = new UserLoginAttempts(username, 0, new Date());
        }
        userLoginAttempts.setLastAttempt(new Date());
        userLoginAttempts.setAttempts(0);
        User user = userService.getUserByName(username);
        if (user != null && (user.getAccountLocked() == null || user.getAccountLocked())) {
            user.setAccountLocked(false);
            try {
                userService.updateUser(user);
            } catch (MangleException e) {
                log.error("Failed to update user, exception {}", e.getMessage());
            }
        }
        repository.save(userLoginAttempts);
    }


}
