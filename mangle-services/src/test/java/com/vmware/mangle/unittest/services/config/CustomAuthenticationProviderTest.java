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

package com.vmware.mangle.unittest.services.config;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.security.User;
import com.vmware.mangle.cassandra.model.security.UserLoginAttempts;
import com.vmware.mangle.services.CustomUserDetailsService;
import com.vmware.mangle.services.UserLoginAttemptsService;
import com.vmware.mangle.services.config.CustomAuthenticationProvider;
import com.vmware.mangle.services.mockdata.UserMockData;

/**
 * @author chetanc
 */
public class CustomAuthenticationProviderTest {
    @Mock
    private UserLoginAttemptsService userLoginAttemptsService;
    @Mock
    private CustomUserDetailsService userDetailsService;

    private CustomAuthenticationProvider authenticationProvider;

    private UserMockData userMockData = new UserMockData();

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        authenticationProvider = spy(new CustomAuthenticationProvider(userLoginAttemptsService, userDetailsService,
                userMockData.getPasswordEncoder()));
    }

    @Test
    public void testAuthentication() {
        User user = userMockData.getMockUser();
        Authentication token = new UsernamePasswordAuthenticationToken(user.getName(), user.getPassword());

        doNothing().when(userLoginAttemptsService).resetFailAttempts(user.getName());
        when(userDetailsService.loadUserByUsername(user.getName())).thenReturn(userMockData.getMockUserDetails());

        Authentication auth = authenticationProvider.authenticate(token);

        verify(userLoginAttemptsService, times(1)).resetFailAttempts(user.getName());
        verify(userLoginAttemptsService, times(0)).updateFailAttempts(user.getName());
    }

    @Test(expectedExceptions = BadCredentialsException.class)
    public void testAuthenticationBadCreds() {
        User user = userMockData.getMockUser();
        Authentication token = new UsernamePasswordAuthenticationToken(user.getName(), UUID.randomUUID().toString());

        doNothing().when(userLoginAttemptsService).resetFailAttempts(user.getName());
        when(userDetailsService.loadUserByUsername(user.getName())).thenReturn(userMockData.getMockUserDetails());

        try {
            Authentication auth = authenticationProvider.authenticate(token);
        } catch (BadCredentialsException e) {
            verify(userLoginAttemptsService, times(0)).resetFailAttempts(user.getName());
            verify(userLoginAttemptsService, times(1)).updateFailAttempts(user.getName());
            throw e;
        }
    }

    @Test(expectedExceptions = LockedException.class)
    public void testAuthenticationLockedUser() {
        User user = userMockData.getMockUser();
        Authentication token = new UsernamePasswordAuthenticationToken(user.getName(), UUID.randomUUID().toString());
        UserLoginAttempts userLoginAttempts = userMockData.getUserLoginAttemptsForUser(user.getName());

        doNothing().when(userLoginAttemptsService).resetFailAttempts(user.getName());
        when(userDetailsService.loadUserByUsername(user.getName())).thenReturn(userMockData.getMockLockedUserDetails());
        when(userLoginAttemptsService.getUserAttemptsForUser(user.getName())).thenReturn(userLoginAttempts);

        try {
            Authentication auth = authenticationProvider.authenticate(token);
        } catch (LockedException e) {
            verify(userLoginAttemptsService, times(0)).resetFailAttempts(user.getName());
            verify(userLoginAttemptsService, times(0)).updateFailAttempts(user.getName());
            throw e;
        }
    }

}