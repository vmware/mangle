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

package com.vmware.mangle.unittest.services.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.security.UserAuthentication;
import com.vmware.mangle.services.PasswordResetService;
import com.vmware.mangle.services.UserAuthenticationService;
import com.vmware.mangle.services.controller.AuthenticationController;
import com.vmware.mangle.services.mockdata.UserAuthenticationServiceMockData;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 *
 *
 * @author chetanc
 */
public class UserAuthenticationControllerTest {

    @Mock
    private UserAuthenticationService userAuthenticationService;

    @Mock
    private PasswordResetService passwordResetService;

    private AuthenticationController userAuthenticationController;

    private UserAuthenticationServiceMockData mockData = new UserAuthenticationServiceMockData();

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        userAuthenticationController = new AuthenticationController(userAuthenticationService, passwordResetService);
    }

    /**
     * Test method for {@link AuthenticationController#createUser(UserAuthentication)}
     *
     * @throws MangleException
     */
    @Test
    public void testCreateUser() throws MangleException {
        UserAuthentication user1 = mockData.getDummyUser1();

        when(userAuthenticationService.createUser(any())).thenReturn(user1);

        ResponseEntity<Resource<UserAuthentication>> responseEntity = userAuthenticationController.createUser(user1);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.CREATED);
        Assert.assertEquals((responseEntity.getBody()).getContent(), user1);
        verify(userAuthenticationService, times(1)).createUser(any());
    }

    /**
     * Test method for {@link AuthenticationController#getUsers()}
     *
     * @throws MangleException
     */
    @Test
    public void testGetUsers() throws MangleException {
        UserAuthentication user1 = mockData.getDummyUser1();
        UserAuthentication user2 = mockData.getDummyUser2();
        List<UserAuthentication> users = new ArrayList<>(Arrays.asList(user1, user2));
        when(userAuthenticationService.getAllUsers()).thenReturn(users);

        ResponseEntity<Resources<UserAuthentication>> responseEntity = userAuthenticationController.getUsers();

        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        verify(userAuthenticationService, times(1)).getAllUsers();
    }

    /**
     * Test method for {@link AuthenticationController#updateUser(UserAuthentication)}
     *
     * @throws MangleException
     */
    @Test
    public void testUpdateUser() throws MangleException {
        UserAuthentication user1 = mockData.getDummyUser1();

        when(userAuthenticationService.updateUser(any())).thenReturn(user1);

        ResponseEntity<Resource<UserAuthentication>> responseEntity = userAuthenticationController.updateUser(user1);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals((responseEntity.getBody()).getContent(), user1);
        verify(userAuthenticationService, times(1)).updateUser(any());
    }

    /**
     * Test method for {@link AuthenticationController#deleteUsersByName(List)}
     *
     * @throws MangleException
     */
    @Test
    public void testDeleteUserByName() throws MangleException {
        UserAuthentication user1 = mockData.getDummyUser1();
        UserAuthentication user2 = mockData.getDummyUser2();
        List<String> users = new ArrayList<>(Arrays.asList(user1.getUsername()));
        Set<String> failedUsers = new HashSet<>(Arrays.asList(user2.getUsername()));

        when(userAuthenticationService.deleteUsersByUsername(any())).thenReturn(failedUsers);

        ResponseEntity<Resource<String>> responseEntity = userAuthenticationController.deleteUsersByName(users);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        verify(userAuthenticationService, times(1)).deleteUsersByUsername(any());
    }

    /**
     * Test method for {@link AuthenticationController#deleteUsersByName(List)}
     *
     * @throws MangleException
     */
    @Test
    public void testDeleteUserByNameNoFailedUsers() throws MangleException {
        UserAuthentication user1 = mockData.getDummyUser1();
        List<String> users = new ArrayList<>(Arrays.asList(user1.getUsername()));
        Set<String> failedUsers = new HashSet<>(Arrays.asList());

        when(userAuthenticationService.deleteUsersByUsername(any())).thenReturn(failedUsers);

        ResponseEntity<Resource<String>> responseEntity = userAuthenticationController.deleteUsersByName(users);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        verify(userAuthenticationService, times(1)).deleteUsersByUsername(any());
    }

    @Test
    public void testGetAdminPasswordResetStatus() throws MangleException {
        when(passwordResetService.readResetStatus()).thenReturn(true);

        ResponseEntity<Resource<Boolean>> responseEntity = userAuthenticationController.getAdminPasswordResetStatus();
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        verify(passwordResetService, times(1)).readResetStatus();
    }

    @Test
    public void testSetAdminPasswordResetStatus() throws MangleException {
        when(passwordResetService.updateResetStatus()).thenReturn(true);

        ResponseEntity<Resource<Boolean>> responseEntity = userAuthenticationController.setAdminPasswordResetStatus();
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        verify(passwordResetService, times(1)).updateResetStatus();
    }

}
