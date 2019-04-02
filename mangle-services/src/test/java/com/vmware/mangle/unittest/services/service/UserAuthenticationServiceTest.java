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

package com.vmware.mangle.unittest.services.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import lombok.extern.log4j.Log4j2;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.security.UserAuthentication;
import com.vmware.mangle.services.UserAuthenticationService;
import com.vmware.mangle.services.UserService;
import com.vmware.mangle.services.mockdata.RolesMockData;
import com.vmware.mangle.services.mockdata.UserAuthenticationServiceMockData;
import com.vmware.mangle.services.repository.UserAuthenticationRepository;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 *
 *
 * @author chetanc
 */
@Log4j2
public class UserAuthenticationServiceTest {

    @Mock
    private UserAuthenticationRepository repository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UserAuthenticationService service;

    private UserAuthenticationServiceMockData mockData = new UserAuthenticationServiceMockData();
    private RolesMockData rolesMockData = new RolesMockData();

    @Mock
    private UserService userService;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        service = new UserAuthenticationService(repository, userService, passwordEncoder);
    }

    @Test
    public void testGetUserByUsername() throws MangleException {
        log.info("Executing the test testGetUserByUsername on the method UserAuthenticationService#getUserByUsername");
        UserAuthentication user = mockData.getDummyUser1();

        when(repository.findByUsername(anyString())).thenReturn(user);
        UserAuthentication persistedUser = service.getUserByUsername(user.getUsername());
        Assert.assertEquals(persistedUser, user);
        verify(repository, times(1)).findByUsername(anyString());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testGetUserByUsernameFailureEmptyString() throws MangleException {
        log.info("Executing the test testGetUserByUsername on the method UserAuthenticationService#getUserByUsername");
        UserAuthentication user = mockData.getDummyUser1();

        when(repository.findByUsername(anyString())).thenReturn(user);
        try {
            service.getUserByUsername("");
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
            verify(repository, times(0)).findByUsername(anyString());
            throw e;
        }
    }

    @Test
    public void testCreateUser() throws MangleException {
        log.info("Executing the test testCreateUser on the method UserAuthenticationService#createUser");
        UserAuthentication user = mockData.getDummyUser1();

        when(repository.save(any())).thenReturn(user);
        when(userService.getDefaultUserRole()).thenReturn(rolesMockData.getDummyRole());
        UserAuthentication persistedUser = service.createUser(user);
        Assert.assertEquals(persistedUser, user);
        verify(repository, times(1)).save(any());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testCreateUserFailure() throws MangleException {
        log.info("Executing the test testCreateUser on the method UserAuthenticationService#createUser");
        UserAuthentication user = mockData.getDummyUser1();

        when(repository.save(any())).thenReturn(user);
        try {
            service.createUser(null);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
            verify(repository, times(0)).save(any());
            throw e;
        }
    }

    @Test
    public void testGetAllUsers() {
        log.info("Executing the test testGetAllUsers on the method UserAuthenticationService#getAllUsers");
        UserAuthentication user1 = mockData.getDummyUser1();
        UserAuthentication user2 = mockData.getDummyUser2();
        List<UserAuthentication> users = new ArrayList<>(Arrays.asList(user1, user2));

        when(repository.findAll()).thenReturn(users);

        List<UserAuthentication> persistedUsers = service.getAllUsers();
        Assert.assertEquals(persistedUsers.size(), 2);
        verify(repository, times(1)).findAll();
    }

    @Test
    public void testUpdateUser() throws MangleException {
        log.info("Executing the test testUpdateUser on the method UserAuthenticationService#updateUser");
        UserAuthentication user = mockData.getDummyUser1();
        UserAuthentication user1 = mockData.getDummyUser2();

        when(repository.findByUsername(anyString())).thenReturn(user);
        when(repository.save(any())).thenReturn(user1);

        UserAuthentication persitedUser = service.updateUser(user1);
        Assert.assertEquals(persitedUser, user1);
        verify(repository, times(1)).findByUsername(anyString());
        verify(repository, times(1)).save(any());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testUpdateUserFailureWithNull() throws MangleException {
        log.info("Executing the test testUpdateUserFailureWithNull on the method UserAuthenticationService#updateUser");
        try {
            service.updateUser(null);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
            verify(repository, times(0)).findByUsername(anyString());
            verify(repository, times(0)).save(any());
            throw e;
        }
    }

    @Test(expectedExceptions = MangleRuntimeException.class)
    public void testUpdateUserFailureWithNoRecord() throws MangleException {
        log.info(
                "Executing the test testUpdateUserFailureWithNoRecord on the method UserAuthenticationService#updateUser");
        UserAuthentication user1 = mockData.getDummyUser1();

        when(repository.findByUsername(anyString())).thenReturn(null);

        try {
            service.updateUser(user1);
        } catch (MangleRuntimeException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
            verify(repository, times(1)).findByUsername(anyString());
            verify(repository, times(0)).save(any());
            throw e;
        }
    }

    @Test
    public void testDeleteUsersByUsername() {
        log.info(
                "Executing the test testDeleteUsersByUsername on the method UserAuthenticationService#deleteUsersByUsername");
        UserAuthentication user1 = mockData.getDummyUser1();
        UserAuthentication user2 = mockData.getDummyUser2();
        List<UserAuthentication> users = new ArrayList<>(Arrays.asList(user1, user2));
        List<String> usernames = new ArrayList<>(Arrays.asList(user1.getUsername(), user2.getUsername()));

        when(repository.findByNameIn(any())).thenReturn(users);
        doNothing().when(repository).deleteByNameIn(any());

        Set<String> failureUsers = service.deleteUsersByUsername(usernames);
        Assert.assertEquals(failureUsers.size(), 0);
        verify(repository, times(1)).deleteByNameIn(any());
        verify(repository, times(1)).findByNameIn(any());
    }

    @Test
    public void testGetDefaultDomainName() {
        log.info(
                "Executing test testGetDefaultDomainName on the method UserAuthenticationService#getDefaultDomainName");
        String result = service.getDefaultDomainName();
        Assert.assertEquals(result, Constants.LOCAL_DOMAIN_NAME);
    }
}
