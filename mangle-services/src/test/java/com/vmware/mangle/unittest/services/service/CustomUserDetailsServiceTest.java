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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.security.Privilege;
import com.vmware.mangle.cassandra.model.security.Role;
import com.vmware.mangle.cassandra.model.security.User;
import com.vmware.mangle.cassandra.model.security.UserAuthentication;
import com.vmware.mangle.services.CustomUserDetailsService;
import com.vmware.mangle.services.UserService;
import com.vmware.mangle.services.mockdata.RolesMockData;
import com.vmware.mangle.services.mockdata.UserAuthenticationServiceMockData;
import com.vmware.mangle.services.mockdata.UserMockData;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 *
 *
 * @author chetanc
 */
public class CustomUserDetailsServiceTest {

    @Mock
    private UserService userV1Service;

    @Mock
    private UserService userService;

    private CustomUserDetailsService customUserDetailsService;

    private UserAuthenticationServiceMockData authMockData = new UserAuthenticationServiceMockData();
    private UserMockData userMockData = new UserMockData();
    private RolesMockData rolesMockData = new RolesMockData();

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        customUserDetailsService = new CustomUserDetailsService(userService);
    }

    @Test
    public void testGetUserAuthority() throws MangleException {
        Role role = rolesMockData.getDummy2Role();
        List<Privilege> privileges = new ArrayList<>(role.getPrivileges());

        when(userService.getPrivilegeForUser(anyString())).thenReturn(privileges);

        List<GrantedAuthority> authorities =
                customUserDetailsService.getUserAuthority(authMockData.getDummyUser1().getUsername());

        Assert.assertEquals(authorities.size(), 2);
        verify(userService, times(1)).getPrivilegeForUser(anyString());
        verify(userService, times(0)).getDefaultUserRole();
        verify(userService, times(0)).createUser(any());
    }

    @Test
    public void testGetUserAuthorityCreateDummyUser() throws MangleException {
        Role role = rolesMockData.getDummy2Role();
        Role role1 = rolesMockData.getDummyRole();
        User user = userMockData.getMockUser2();
        List<Privilege> privileges = new ArrayList<>(role.getPrivileges());

        when(userService.getPrivilegeForUser(anyString())).thenReturn(privileges);
        when(userService.getDefaultUserRole()).thenReturn(role1);
        when(userService.createUser(any())).thenReturn(user);

        List<GrantedAuthority> authorities =
                customUserDetailsService.getUserAuthority(authMockData.getDummyUser1().getUsername());

        Assert.assertEquals(authorities.size(), 2);
        verify(userService, times(1)).getPrivilegeForUser(anyString());
    }

    @Test
    public void testLoadUserByUsername() throws MangleException {
        Role role = rolesMockData.getDummy2Role();
        List<Privilege> privileges = new ArrayList<>(role.getPrivileges());
        User user = userMockData.getMockUser();

        when(userService.getPrivilegeForUser(anyString())).thenReturn(privileges);
        when(userService.getUserByName(anyString())).thenReturn(user);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getName());
        Assert.assertNotNull(user);
        verify(userService, times(1)).getUserByName(anyString());
        verify(userService, times(1)).getPrivilegeForUser(anyString());
        verify(userService, times(0)).getDefaultUserRole();
        verify(userService, times(0)).createUser(any());
    }

    @Test(expectedExceptions = UsernameNotFoundException.class)
    public void testLoadUserByUsernameUserNull() throws MangleException {
        User user = userMockData.getMockUser();

        when(userService.getUserByName(anyString())).thenReturn(null);

        try {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getName());
        } catch (Exception e) {
            Assert.assertTrue(e instanceof UsernameNotFoundException);
            verify(userService, times(1)).getUserByName(anyString());
            verify(userService, times(0)).getPrivilegeForUser(anyString());
            verify(userService, times(0)).getDefaultUserRole();
            verify(userService, times(0)).createUser(any());
            throw (UsernameNotFoundException) e;
        }


    }

    @Test(expectedExceptions = UsernameNotFoundException.class)
    public void testLoadUserByUserNameGetUserException() throws MangleException {
        UserAuthentication authUser = authMockData.getDummyUser1();

        when(userService.getUserByName(anyString())).thenReturn(null);

        try {
            customUserDetailsService.loadUserByUsername(authUser.getUsername());
        } catch (Exception e) {
            Assert.assertTrue(e instanceof UsernameNotFoundException);
            verify(userService, times(1)).getUserByName(anyString());
            verify(userService, times(0)).getPrivilegeForUser(anyString());
            verify(userService, times(0)).getDefaultUserRole();
            verify(userService, times(0)).createUser(any());
            throw (UsernameNotFoundException) e;
        }


    }


}
