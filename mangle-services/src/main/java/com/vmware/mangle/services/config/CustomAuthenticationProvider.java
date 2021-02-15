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

package com.vmware.mangle.services.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.vmware.mangle.cassandra.model.security.UserLoginAttempts;
import com.vmware.mangle.services.CustomUserDetailsService;
import com.vmware.mangle.services.UserLoginAttemptsService;
import com.vmware.mangle.utils.constants.ErrorConstants;

/**
 *
 *
 * @author chetanc
 */
@Component
public class CustomAuthenticationProvider extends DaoAuthenticationProvider {

    private UserLoginAttemptsService userLoginAttemptsService;

    @Autowired
    public CustomAuthenticationProvider(UserLoginAttemptsService userLoginAttemptsService,
            CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userLoginAttemptsService = userLoginAttemptsService;
        setUserDetailsService(userDetailsService);
        setPasswordEncoder(passwordEncoder);
    }

    @Override
    public Authentication authenticate(Authentication authentication) {

        try {

            Authentication auth = super.authenticate(authentication);

            //if reach here, means login success, else an exception will be thrown
            //reset the user_attempts
            userLoginAttemptsService.resetFailAttempts(authentication.getName());

            return auth;

        } catch (BadCredentialsException e) {

            //invalid login, update to user_attempts
            userLoginAttemptsService.updateFailAttempts(authentication.getName());
            throw e;

        } catch (LockedException e) {
            UserLoginAttempts loginAttempts = userLoginAttemptsService.getUserAttemptsForUser(authentication.getName());
            long remainingTimeForUserUnlock = getRemainingTimeoutForUserLockout(loginAttempts);

            throw new LockedException(String.format(ErrorConstants.USER_ACCOUNT_LOCKED_ERROR_MSG, remainingTimeForUserUnlock));
        }
    }

    private long getRemainingTimeoutForUserLockout(UserLoginAttempts loginAttempts) {
        double totalTimeoutTime = Math.pow(2, loginAttempts.getAttempts()) * 1000;
        return Math.round(
                (totalTimeoutTime - (System.currentTimeMillis() - loginAttempts.getLastAttempt().getTime())) / 1000);

    }
}
