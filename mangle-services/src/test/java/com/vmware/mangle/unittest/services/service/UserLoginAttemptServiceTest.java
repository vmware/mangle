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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.security.User;
import com.vmware.mangle.cassandra.model.security.UserLoginAttempts;
import com.vmware.mangle.services.UserLoginAttemptsService;
import com.vmware.mangle.services.UserService;
import com.vmware.mangle.services.mockdata.UserMockData;
import com.vmware.mangle.services.repository.UserLoginAttemptsRepository;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author chetanc
 */
public class UserLoginAttemptServiceTest {


    @Mock
    private UserLoginAttemptsRepository repository;

    @Mock
    private UserService userService;

    private UserLoginAttemptsService service;
    private UserMockData userMockData = new UserMockData();

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        service = new UserLoginAttemptsService(repository, userService);
    }

    @Test
    public void testGetUserAttemptForUser() {
        User user = userMockData.getMockUser();
        UserLoginAttempts loginAttempts = userMockData.getUserLoginAttemptsForUser(user.getName());

        when(repository.findByUsername(user.getName())).thenReturn(loginAttempts);

        UserLoginAttempts userLoginAttempts = service.getUserAttemptsForUser(user.getName());

        Assert.assertEquals(userLoginAttempts, loginAttempts,
                "Should have return the same user object which is in DB without any modification");

        verify(repository, times(1)).findByUsername(user.getName());
    }

    @Test
    public void testUpdateFailAttemptsLockedUser() throws MangleException {
        User user = userMockData.getMockUser();
        UserLoginAttempts loginAttempts = userMockData.getUserLoginAttemptsForUser(user.getName());
        loginAttempts.setAttempts(4);

        when(repository.findByUsername(user.getName())).thenReturn(loginAttempts);
        when(userService.updateUser(any())).then(AdditionalAnswers.returnsFirstArg());
        when(userService.getUserByName(user.getName())).thenReturn(user);

        service.updateFailAttempts(user.getName());

        Assert.assertEquals(user.getAccountLocked(), Boolean.TRUE,
                "Calling updateFailAttempts should have updated the account lock status to locked");
        Assert.assertEquals(loginAttempts.getAttempts(), 5,
                "Calling updateFailAttempts method should have increased the user account's number of failed"
                        + " attempts by 1");

        verify(repository, times(1)).findByUsername(user.getName());
        verify(repository, times(1)).save(any());
        verify(userService, times(1)).getUserByName(user.getName());
        verify(userService, times(1)).updateUser(any());
        verify(userService, times(1)).terminateUserSession(user.getName());

    }

    @Test
    public void testUpdateFailAttemptsLockedUserMangleException() throws MangleException {
        User user = userMockData.getMockUser();
        UserLoginAttempts loginAttempts = userMockData.getUserLoginAttemptsForUser(user.getName());
        loginAttempts.setAttempts(4);

        when(repository.findByUsername(user.getName())).thenReturn(loginAttempts);
        doThrow(new MangleException("", ErrorCode.GENERIC_ERROR)).when(userService).updateUser(any());
        when(userService.getUserByName(user.getName())).thenReturn(user);

        service.updateFailAttempts(user.getName());

        Assert.assertEquals(user.getAccountLocked(), Boolean.TRUE,
                "Calling updateFailAttempts should have updated the account lock status to locked");
        Assert.assertEquals(loginAttempts.getAttempts(), 5,
                "Calling updateFailAttempts method should have increased the user account's number of failed"
                        + " attempts by 1");

        verify(repository, times(1)).findByUsername(user.getName());
        verify(repository, times(1)).save(any());
        verify(userService, times(1)).getUserByName(user.getName());
        verify(userService, times(1)).updateUser(any());
        verify(userService, times(1)).terminateUserSession(user.getName());
    }

    @Test
    public void testUpdateFailAttempts() throws MangleException {
        User user = userMockData.getMockUser();
        UserLoginAttempts loginAttempts = userMockData.getUserLoginAttemptsForUser(user.getName());

        when(repository.findByUsername(user.getName())).thenReturn(loginAttempts);
        when(userService.updateUser(any())).then(AdditionalAnswers.returnsFirstArg());
        when(userService.getUserByName(user.getName())).thenReturn(user);


        service.updateFailAttempts(user.getName());

        Assert.assertEquals(user.getAccountLocked(), Boolean.TRUE,
                "User account should have been locked on whom updateFailAttempts method is called");
        Assert.assertTrue(loginAttempts.getAttempts() > 0,
                "User account is locked, though the number of failed login attempts are lesser than 1");


        verify(repository, times(1)).findByUsername(user.getName());
        verify(repository, times(1)).save(any());
        verify(userService, times(1)).getUserByName(user.getName());
        verify(userService, times(1)).updateUser(any());
        verify(userService, times(1)).terminateUserSession(user.getName());
    }

    @Test
    public void testUpdateFailAttemptsNoAttemptUser() throws MangleException {
        User user = userMockData.getMockUser();

        when(repository.findByUsername(user.getName())).thenReturn(null);
        when(userService.updateUser(any())).then(AdditionalAnswers.returnsFirstArg());
        when(userService.getUserByName(user.getName())).thenReturn(user);


        service.updateFailAttempts(user.getName());

        Assert.assertEquals(user.getAccountLocked(), Boolean.TRUE,
                "User account is not locked when the loginAttempt object for a given username is not found. "
                        + "A new instance for a given should have been created and the account should have been locked");

        verify(repository, times(1)).findByUsername(user.getName());
        verify(repository, times(1)).save(any());
        verify(userService, times(1)).getUserByName(user.getName());
        verify(userService, times(1)).updateUser(any());
        verify(userService, times(1)).terminateUserSession(user.getName());
    }

    @Test
    public void testUpdateFailAttemptsNoUserFound() throws MangleException {
        User user = userMockData.getMockUser();
        UserLoginAttempts loginAttempts = userMockData.getUserLoginAttemptsForUser(user.getName());
        int noOfFailedLoginAttempts = loginAttempts.getAttempts();
        when(repository.findByUsername(user.getName())).thenReturn(loginAttempts);
        when(userService.updateUser(any())).then(AdditionalAnswers.returnsFirstArg());
        when(userService.getUserByName(user.getName())).thenReturn(null);


        service.updateFailAttempts(user.getName());

        Assert.assertEquals(loginAttempts.getAttempts(), noOfFailedLoginAttempts + 1,
                "The login attempts were to be updated for a given user even when the corresponding user entry "
                        + "is not found in the DB");

        verify(repository, times(1)).findByUsername(user.getName());
        verify(repository, times(1)).save(any());
        verify(userService, times(1)).getUserByName(user.getName());
        verify(userService, times(0)).updateUser(any());
        verify(userService, times(0)).terminateUserSession(user.getName());
    }

    @Test
    public void testResetFailAttempts() throws MangleException {
        User user = userMockData.getMockUser();
        UserLoginAttempts loginAttempts = userMockData.getUserLoginAttemptsForUser(user.getName());
        loginAttempts.setAttempts(4);

        when(repository.findByUsername(user.getName())).thenReturn(loginAttempts);
        when(userService.updateUser(any())).then(AdditionalAnswers.returnsFirstArg());
        when(userService.getUserByName(user.getName())).thenReturn(user);

        service.resetFailAttempts(user.getName());

        Assert.assertEquals(user.getAccountLocked(), Boolean.FALSE,
                "Account status is still locked. Calling resetFailAttempts for a given username should have "
                        + "unlocked the user");
        Assert.assertEquals(loginAttempts.getAttempts(), 0,
                "Number of failed login attempts should have been 0. Calling resetFailAttempts for a given "
                        + "username should have reset the value to 0");

        verify(repository, times(1)).findByUsername(user.getName());
        verify(repository, times(1)).save(any());
        verify(userService, times(0)).updateUser(any());
    }

    @Test
    public void testResetFailAttemptsLockUser() throws MangleException {
        User user = userMockData.getLockedMockUser();
        UserLoginAttempts loginAttempts = userMockData.getUserLoginAttemptsForUser(user.getName());
        loginAttempts.setAttempts(4);


        when(repository.findByUsername(user.getName())).thenReturn(loginAttempts);
        when(userService.updateUser(any())).then(AdditionalAnswers.returnsFirstArg());
        when(userService.getUserByName(user.getName())).thenReturn(user);

        service.resetFailAttempts(user.getName());

        Assert.assertEquals(user.getAccountLocked(), Boolean.FALSE,
                "Account status is still locked. Calling resetFailAttempts for a given username should have "
                        + "unlocked the user");
        Assert.assertEquals(loginAttempts.getAttempts(), 0,
                "Number of failed login attempts should have been 0. Calling resetFailAttempts for a given "
                        + "username should have reset the value to 0");

        verify(repository, times(1)).findByUsername(user.getName());
        verify(repository, times(1)).save(any());
        verify(userService, times(1)).updateUser(any());

    }

    @Test
    public void testResetFailAttemptsNoLoginAttemptFound() throws MangleException {
        User user = userMockData.getMockUser();

        when(repository.findByUsername(user.getName())).thenReturn(null);
        when(userService.updateUser(any())).then(AdditionalAnswers.returnsFirstArg());
        when(userService.getUserByName(user.getName())).thenReturn(user);

        service.resetFailAttempts(user.getName());

        Assert.assertEquals(user.getAccountLocked(), Boolean.FALSE,
                "Account status is still locked. Calling resetFailAttempts for a given username should have "
                        + "unlocked the user");

        verify(repository, times(1)).findByUsername(user.getName());
        verify(repository, times(1)).save(any());
        verify(userService, times(1)).getUserByName(user.getName());
    }

    @Test
    public void testResetFailAttemptsNoUserObjectFound() throws MangleException {
        User user = userMockData.getMockUser();
        UserLoginAttempts loginAttempts = userMockData.getUserLoginAttemptsForUser(user.getName());

        when(repository.findByUsername(user.getName())).thenReturn(loginAttempts);
        doThrow(new MangleException(ErrorCode.NO_RECORD_FOUND)).when(userService).updateUser(any());
        when(userService.getUserByName(user.getName())).thenReturn(null);

        service.resetFailAttempts(user.getName());

        Assert.assertEquals(user.getAccountLocked(), Boolean.FALSE,
                "Account status is still locked. Calling resetFailAttempts for a given username should have "
                        + "unlocked the user");
        Assert.assertEquals(loginAttempts.getAttempts(), 0,
                "Number of failed login attempts should have been 0. Calling resetFailAttempts for a given "
                        + "username should have reset the value to 0");

        verify(repository, times(1)).findByUsername(user.getName());
        verify(repository, times(1)).save(any());
        verify(userService, times(1)).getUserByName(user.getName());
        verify(userService, times(0)).updateUser(any());
    }

    @Test
    public void testResetFailAttemptsForLockedStatusNull() throws MangleException {
        User user = userMockData.getMockUser();
        UserLoginAttempts loginAttempts = userMockData.getUserLoginAttemptsForUser(user.getName());
        user.setAccountLocked(null);

        when(repository.findByUsername(user.getName())).thenReturn(loginAttempts);
        doThrow(new MangleException(ErrorCode.NO_RECORD_FOUND)).when(userService).updateUser(any());
        when(userService.getUserByName(user.getName())).thenReturn(user);

        service.resetFailAttempts(user.getName());

        Assert.assertEquals(user.getAccountLocked(), Boolean.FALSE,
                "Account status is still locked. Calling resetFailAttempts for a given username should have "
                        + "unlocked the user");
        Assert.assertEquals(loginAttempts.getAttempts(), 0,
                "Number of failed login attempts should have been 0. Calling resetFailAttempts for a given "
                        + "username should have reset the value to 0");

        verify(repository, times(1)).findByUsername(user.getName());
        verify(repository, times(1)).save(any());
        verify(userService, times(1)).getUserByName(user.getName());
        verify(userService, times(1)).updateUser(any());
    }

    @Test
    public void testResetFailAttemptsFailUpdateUser() throws MangleException {
        User user = userMockData.getLockedMockUser();
        UserLoginAttempts loginAttempts = userMockData.getUserLoginAttemptsForUser(user.getName());
        loginAttempts.setAttempts(4);

        when(repository.findByUsername(user.getName())).thenReturn(loginAttempts);
        doThrow(new MangleException(ErrorCode.USER_NOT_FOUND)).when(userService).updateUser(any());
        when(userService.getUserByName(user.getName())).thenReturn(user);

        service.resetFailAttempts(user.getName());

        Assert.assertEquals(user.getAccountLocked(), Boolean.FALSE,
                "Account status is still locked. Calling resetFailAttempts for a given username should have "
                        + "unlocked the user");
        Assert.assertEquals(loginAttempts.getAttempts(), 0,
                "Number of failed login attempts should have been 0. Calling resetFailAttempts for a given "
                        + "username should have reset the value to 0");

        verify(repository, times(1)).findByUsername(user.getName());
        verify(repository, times(1)).save(any());
        verify(userService, times(1)).updateUser(any());
    }

}
