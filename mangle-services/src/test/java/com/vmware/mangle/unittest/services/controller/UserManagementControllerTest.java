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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import lombok.extern.log4j.Log4j2;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.security.User;
import com.vmware.mangle.model.UserPasswordUpdateDTO;
import com.vmware.mangle.services.PasswordResetService;
import com.vmware.mangle.services.UserService;
import com.vmware.mangle.services.controller.UserManagementController;
import com.vmware.mangle.services.mockdata.UserMockData;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * TestNG class to test class UserManagementController
 *
 * @author chetanc
 */
@Log4j2
public class UserManagementControllerTest extends PowerMockTestCase {

    @InjectMocks
    private UserManagementController userManagementController;
    @Mock
    private UserService userService;

    @Mock
    private PasswordResetService passwordResetService;

    @Mock
    private HazelcastInstance hazelcastInstance;

    private UserMockData dataProvider = new UserMockData();


    @BeforeClass
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for {@link UserManagementController#getAllUsers()}
     */
    @Test
    public void getAllUsersTest() throws MangleException {
        log.info("Executing test: getAllUsersTest on UserManagementController#getAllUsers()");

        User user = dataProvider.getMockUser();
        User newUser = dataProvider.getUpdateMockUser();
        List<User> users = new ArrayList<>();
        users.add(user);
        users.add(newUser);

        when(userService.getAllUsers()).thenReturn(users);
        ResponseEntity<Resources<User>> response = userManagementController.getAllUsers();
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Resources<User> resource = response.getBody();
        Assert.assertEquals(resource.getContent().size(), users.size());
        verify(userService, times(1)).getAllUsers();
    }

    /**
     * Test method for {@link UserManagementController#createUser(User)}
     */
    @Test
    public void createUserTestSuccessful() throws MangleException {
        log.info("Executing test: createUserTestSuccessful on UserManagementController#createUser(User)");
        User user = dataProvider.getMockUser();

        when(userService.createUser(any())).thenReturn(user);
        when(userService.getDefaultDomainName()).thenReturn("mangle.local");
        when(userService.getUserByName(anyString())).thenReturn(null);

        ResponseEntity<Resource<User>> response = userManagementController.createUser(user);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.CREATED);

        Resource<User> resource = response.getBody();
        Assert.assertEquals(resource.getContent(), user);
        verify(userService, times(1)).createUser(any());
    }

    /**
     * Test method for {@link UserManagementController#createUser(User)}
     */
    @Test(expectedExceptions = MangleException.class)
    public void createUserTestFailure() throws MangleException {
        log.info("Executing test: createUserTestFailure on UserManagementController#createUser(User)");
        User user = dataProvider.getMockUser();

        doThrow(new MangleException(ErrorCode.USER_NOT_FOUND)).when(userService).createUser(any());
        when(userService.getUserByName(anyString())).thenReturn(null);
        when(userService.getDefaultDomainName()).thenReturn("mangle.local");
        try {
            userManagementController.createUser(user);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.USER_NOT_FOUND);
            verify(userService, times(1)).createUser(any());
            throw e;
        }
    }

    /**
     * Test method for {@link UserManagementController#createUser(User)}
     */
    @Test(expectedExceptions = MangleException.class)
    public void createUserTestFailureUserNotFound() throws MangleException {
        log.info("Executing test: createUserTestFailureUserNotFound on UserManagementController#createUser(User)");
        User user = dataProvider.getMockUser();

        doThrow(new MangleException(ErrorCode.USER_NOT_FOUND)).when(userService).createUser(any());
        when(userService.getUserByName(anyString())).thenReturn(user);
        try {
            userManagementController.createUser(user);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.USER_ALREADY_EXISTS);
            verify(userService, times(0)).createUser(any());
            throw e;
        }
    }

    /**
     * Test method for {@link UserManagementController#createUser(User)}
     */
    @Test
    public void updateUserTestSuccessful() throws MangleException {
        log.info("Executing test: createUserTestSuccessful on UserManagementController#createUser(User)");
        User user = dataProvider.getMockUser();
        Cluster cluster = mock(Cluster.class);
        Member member = mock(Member.class);

        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getLocalMember()).thenReturn(member);
        when(userService.updateUser(any())).thenReturn(user);

        ResponseEntity<Resource<User>> response = userManagementController.updateUser(user);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);

        Resource<User> resource = response.getBody();
        Assert.assertEquals(resource.getContent(), user);
        verify(userService, times(1)).updateUser(any());
    }

    /**
     * Test method for {@link UserManagementController#createUser(User)}
     */
    @Test(expectedExceptions = MangleException.class)
    public void updateUserTestFailure() throws MangleException {
        log.info("Executing test: createUserTestFailure on UserManagementController#createUser(User)");
        User user = dataProvider.getMockUser();

        doThrow(new MangleException(ErrorCode.USER_NOT_FOUND)).when(userService).updateUser(any());
        try {
            userManagementController.updateUser(user);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.USER_NOT_FOUND);
            verify(userService, times(1)).updateUser(any());
            throw e;
        }
    }

    @Test
    public void testGetCurrentUser() throws MangleException {
        log.info("Executing test: testGetCurrentUser on UserManagementController#getCurrentUser");
        User user = dataProvider.getMockUser();

        when(userService.getCurrentUser()).thenReturn(user);

        ResponseEntity<Resource<User>> responseEntity = userManagementController.getCurrentUser();
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        verify(userService, times(1)).getCurrentUser();
    }

    @Test
    public void testResetAdminCredsForFirstLogin() throws MangleException {
        User user = dataProvider.getMockUser();

        when(userService.updateUser(user)).thenReturn(user);
        when(passwordResetService.updateResetStatus()).thenReturn(true);

        ResponseEntity responseEntity = userManagementController.resetAdminCredsForFirstLogin(user);

        Assert.assertNotNull(responseEntity);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        verify(userService, times(1)).updateUser(user);
        verify(passwordResetService, times(1)).updateResetStatus();
    }

    @Test
    public void testGetAdminPasswordResetStatus() {
        when(passwordResetService.readResetStatus()).thenReturn(true);

        ResponseEntity responseEntity = userManagementController.getAdminPasswordResetStatus();
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(((Resource) responseEntity.getBody()).getContent(), true);
        verify(passwordResetService, times(1)).readResetStatus();
    }

    @Test
    public void testUpdateUserPassword() throws MangleException {
        User user = dataProvider.getMockUser();
        UserPasswordUpdateDTO updateDTO = dataProvider.getUserPasswordUpdateDTO();

        when(userService.getCurrentUserName()).thenReturn(user.getName());
        doNothing().when(userService).updatePassword(anyString(), anyString(), anyString());
        doNothing().when(userService).terminateUserSession(user.getName());
        doNothing().when(userService).triggerMultiNodeResync(anyString());

        ResponseEntity responseEntity = userManagementController.updateUserPassword(updateDTO);

        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.NO_CONTENT);

        verify(userService, times(1)).getCurrentUserName();
        verify(userService, times(1)).updatePassword(anyString(), anyString(), anyString());
        verify(userService, times(1)).terminateCurrentSession();
        verify(userService, times(1)).triggerMultiNodeResync(anyString());
    }

    @Test
    public void testDeleteUsersByNames() throws MangleException {
        List<String> usersList = dataProvider.getUsersList();

        doNothing().when(userService).deleteUsersByNames(usersList);
        doNothing().when(userService).triggerMultiNodeResync(any());

        ResponseEntity responseEntity = userManagementController.deleteUsersByNames(usersList);

        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.NO_CONTENT);

        verify(userService, times(1)).deleteUsersByNames(usersList);


    }
}
