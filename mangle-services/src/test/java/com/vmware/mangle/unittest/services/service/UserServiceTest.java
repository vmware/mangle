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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.extern.log4j.Log4j2;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.security.Privilege;
import com.vmware.mangle.cassandra.model.security.Role;
import com.vmware.mangle.cassandra.model.security.User;
import com.vmware.mangle.services.ADAuthProviderService;
import com.vmware.mangle.services.PrivilegeService;
import com.vmware.mangle.services.RoleService;
import com.vmware.mangle.services.UserService;
import com.vmware.mangle.services.mockdata.RolesMockData;
import com.vmware.mangle.services.mockdata.UserMockData;
import com.vmware.mangle.services.repository.UserRepository;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * TestNG class to test class UserServiceTest
 *
 * @author chetanc
 */
@Log4j2
public class UserServiceTest extends PowerMockTestCase {

    private UserService userService;

    @Mock
    private RoleService roleService;

    @Mock
    private PrivilegeService privilegeService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ADAuthProviderService authProviderService;

    private UserMockData dataProvider = new UserMockData();
    private RolesMockData rolesMockData = new RolesMockData();

    private String username = "USERNAME";

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        userService = spy(new UserService(userRepository, privilegeService, authProviderService, passwordEncoder));
        userService.setRoleService(roleService);
    }

    /**
     * Test method for {@link UserService#getRoleForUser(String)}
     */
    @Test
    public void getRoleForUserTestNull() {
        log.info("Executing test: getRoleForUserTestNull on UserService#getRoleForUser(String)");

        User user = dataProvider.getMockUser();
        when(userRepository.findByName(anyString())).thenReturn(user);

        Set<Role> roles = userService.getRoleForUser(user.getName());

        Assert.assertEquals(roles.size(), user.getRoles().size());
        verify(userRepository, times(1)).findByName(anyString());
    }

    /**
     * Test method for {@link UserService#getRoleForUser(String)}
     */
    @Test
    public void getRoleForUserTestSuccess() {
        log.info("Executing test: getRoleForUserTestSuccess on UserService#getRoleForUser(String)");

        User user = dataProvider.getMockUser();
        when(userRepository.findByName(anyString())).thenReturn(null);

        Set<Role> roles = userService.getRoleForUser(user.getName());

        Assert.assertNull(roles);
        verify(userRepository, times(1)).findByName(anyString());
    }

    /**
     * Test method for {@link UserService#getUserByName(String)}
     */
    @Test
    public void getUserByNameTest() {
        log.info("Executing test: getUserByNameTest on UserService#getUserByName(String)");

        User user = dataProvider.getMockUser();
        when(userRepository.findByName(anyString())).thenReturn(user);

        User persisted = userService.getUserByName(user.getName());

        Assert.assertEquals(persisted, user);
        verify(userRepository, times(1)).findByName(anyString());
    }

    /**
     * Test method for {@link UserService#getAllUsers()}
     */
    @Test
    public void getAllUsersTest() {
        log.info("Executing test: getAllUsersTest on UserService#getAllUsersTest()");

        User user = dataProvider.getMockUser();
        User userNew = dataProvider.getUpdateMockUser();
        List<User> list = new ArrayList<>();
        list.add(user);
        list.add(userNew);

        when(userRepository.findAll()).thenReturn(list);
        List<User> persisted = userService.getAllUsers();

        Assert.assertEquals(persisted, list);
        verify(userRepository, times(1)).findAll();
    }

    /**
     * Test method for {@link UserService#createUser(User)}
     */
    @Test
    public void createUserTest() throws MangleException {
        log.info("Executing test: createUserTest on UserService#createUser(User)");

        User user = dataProvider.getMockUser();
        Set<String> domains = new HashSet<>(Collections.singletonList(Constants.LOCAL_DOMAIN_NAME));

        when(userRepository.save(any())).thenReturn(user);
        when(roleService.getRoleByName(any())).thenReturn(dataProvider.getDummyRole());
        when(authProviderService.getAllDomains()).thenReturn(domains);

        User persisted = userService.createUser(user);

        Assert.assertEquals(persisted, user);
        verify(userRepository, times(1)).save(any());
        verify(roleService, times(2)).getRoleByName(any());
    }

    /**
     * Test method for {@link UserService#createUser(User)}
     */
    @Test(expectedExceptions = MangleException.class)
    public void createUserTestFailure() throws MangleException {
        log.info("Executing test: createUserTestFailure on UserService#createUser(User)");

        User user = dataProvider.getMockUser();
        when(roleService.getRoleByName(any())).thenReturn(null);

        try {
            userService.createUser(user);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.ROLE_NOT_FOUND);
            verify(roleService, times(1)).getRoleByName(any());
            throw e;
        }
    }

    /**
     * Test method for {@link UserService#createUser(User)}
     */
    @Test(expectedExceptions = MangleException.class)
    public void createUserTestFailureInvalidDomain() throws MangleException {
        log.info("Executing test: createUserTestFailure on UserService#createUser(User)");

        User user = dataProvider.getMockUser();
        user.setName(UUID.randomUUID().toString());

        try {
            userService.createUser(user);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.INVALID_DOMAIN_NAME);
            verify(roleService, times(0)).getRoleByName(any());
            throw e;
        }
    }

    /**
     * Test method for {@link UserService#createUser(User)}
     */
    @Test
    public void updateUserTest() throws MangleException {
        log.info("Executing test: createUserTest on UserService#updateUser(User)");

        User user = dataProvider.getMockUser();
        Set<String> domains = new HashSet<>(Collections.singletonList(Constants.LOCAL_DOMAIN_NAME));

        when(userRepository.save(any())).thenReturn(user);
        when(roleService.getRoleByName(any())).thenReturn(dataProvider.getDummyRole());
        when(authProviderService.getAllDomains()).thenReturn(domains);
        doReturn(user).when(userService).getUserByName(any());
        User persisted = userService.updateUser(user);

        Assert.assertEquals(persisted, user);
        verify(userRepository, times(1)).save(any());
        verify(roleService, times(2)).getRoleByName(any());
    }

    /**
     * Test method for {@link UserService#updateUser(User)}
     */
    @Test(expectedExceptions = MangleException.class)
    public void updateUserTestFailure() throws MangleException {
        log.info("Executing test: updateUserTestFailure on UserService#updateUser(User)");

        User user = dataProvider.getMockUser();
        when(userService.getUserByName(any())).thenReturn(null);

        try {
            userService.updateUser(user);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.USER_NOT_FOUND);
            verify(userRepository, times(1)).findByName(any());
            throw e;
        }
    }

    /**
     * Test method for {@link UserService#updateUser(User)}
     */
    @Test(expectedExceptions = MangleException.class)
    public void updatePasswordTest() throws MangleException {
        log.info("Executing test: updateUserTestFailure on UserService#updateUser(User)");

        User user = dataProvider.getMockUser();
        when(userRepository.findByName(anyString())).thenReturn(dataProvider.getMockUser2());
        when(userRepository.save(any())).thenReturn(user);

        userService.updatePassword(user.getName(), user.getPassword(), UUID.randomUUID().toString());

        verify(userRepository, times(1)).findByName(anyString());
        verify(userRepository, times(1)).save(any());
    }

    /**
     * Test method for {@link UserService#updatePassword(String, String, String)}
     */
    @Test(expectedExceptions = MangleException.class)
    public void updatePasswordTestFailureNoUserFound() throws MangleException {
        log.info("Executing test: updateUserTestFailure on UserService#updateUser(User)");

        User user = dataProvider.getMockUser();
        when(userService.getUserByName(any())).thenReturn(null);

        try {
            userService.updatePassword(user.getName(), user.getPassword(), UUID.randomUUID().toString());
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.USER_NOT_FOUND);
            verify(userRepository, times(1)).findByName(any());
            throw e;
        }
    }

    /**
     * Test method for {@link UserService#updatePassword(String, String, String)}
     */
    @Test(expectedExceptions = MangleException.class)
    public void updatePasswordTestFailurePasswordMismatch() throws MangleException {
        log.info("Executing test: updateUserTestFailure on UserService#updateUser(User)");

        User user = dataProvider.getMockUser();
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        when(userRepository.findByName(anyString())).thenReturn(dataProvider.getMockUser2());

        try {
            userService.updatePassword(user.getName(), user.getPassword(), UUID.randomUUID().toString());
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.CURRENT_PASSWORD_MISMATCH);
            verify(userRepository, times(1)).findByName(any());
            throw e;
        }
    }

    /**
     * Test method for {@link UserService#deleteUsersByNames(List)}
     */
    @Test
    public void deleteUsersByNamesTest() throws MangleException {
        log.info("Executing test: deleteUsersByNamesTest on UserService#deleteUsersByNames(List)");
        User user = dataProvider.getMockUser();
        User userNew = dataProvider.getUpdateMockUser();
        List<String> list = new ArrayList<>();
        list.add(user.getName());
        list.add(userNew.getName());

        List<User> users = new ArrayList<>();
        users.add(user);
        users.add(userNew);

        when(userRepository.findByNameIn(list)).thenReturn(users);
        doNothing().when(userRepository).delete(any());
        doNothing().when(userService).terminateUserSession(anyString());
        userService.deleteUsersByNames(list);

        verify(userRepository, times(1)).deleteByNameIn(any());
        verify(userRepository, times(1)).findByNameIn(any());
    }

    /**
     * Test method for {@link UserService#deleteUsersByNames(List)}
     */
    @Test(expectedExceptions = MangleException.class)
    public void deleteUsersByNamesTestFailureAdminUserDelete() throws MangleException {
        log.info("Executing test: deleteUsersByNamesTest on UserService#deleteUsersByNames(List)");
        User user = dataProvider.getMockUser();
        User userNew = dataProvider.getUpdateMockUser();
        List<String> list = new ArrayList<>();
        user.setName(Constants.MANGLE_DEFAULT_USER);
        list.add(user.getName());
        list.add(userNew.getName());

        List<User> users = new ArrayList<>();
        users.add(user);
        users.add(userNew);

        when(userRepository.findByNameIn(list)).thenReturn(users);
        doNothing().when(userService).terminateUserSession(anyString());
        doNothing().when(userRepository).delete(any());
        try {
            userService.deleteUsersByNames(list);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.DEFAULT_MANGLE_USER_DELETE_FAIL);
            verify(userRepository, times(0)).deleteByNameIn(any());
            verify(userRepository, times(1)).findByNameIn(any());
            throw e;
        }
    }

    /**
     * Test method for {@link UserService#deleteUsersByNames(List)}
     */
    @Test(expectedExceptions = MangleException.class)
    public void deleteUsersByNamesTestFailureUserDoesnotExist() throws MangleException {
        log.info("Executing test: deleteUsersByNamesTest on UserService#deleteUsersByNames(List)");
        User user = dataProvider.getMockUser();
        User userNew = dataProvider.getUpdateMockUser();
        List<String> list = new ArrayList<>();
        list.add(user.getName());
        list.add(userNew.getName());
        list.add(UUID.randomUUID().toString());

        List<User> users = new ArrayList<>();
        users.add(user);
        users.add(userNew);

        when(userRepository.findByNameIn(list)).thenReturn(users);
        doNothing().when(userRepository).delete(any());
        try {
            userService.deleteUsersByNames(list);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
            verify(userRepository, times(0)).deleteByNameIn(any());
            verify(userRepository, times(1)).findByNameIn(any());
            throw e;
        }
    }

    /**
     * Test method for {@link UserService#deleteUsersByNames(List)}
     */
    @Test
    public void testDeleteUsersByNames() throws MangleException {
        log.info("Executing test: deleteUsersByNamesTest on UserService#deleteUsersByNames(List)");
        User user = dataProvider.getMockUser();
        User userNew = dataProvider.getUpdateMockUser();
        List<String> list = new ArrayList<>();
        list.add(user.getName());
        list.add(userNew.getName());
        List<User> users = new ArrayList<>(Arrays.asList(user, userNew));

        when(userRepository.findByNameIn(any())).thenReturn(users);
        doNothing().when(userRepository).deleteByNameIn(any());
        doNothing().when(userService).terminateUserSession(anyString());

        userService.deleteUsersByNames(list);
        verify(userRepository, times(1)).deleteByNameIn(any());
        verify(userRepository, times(1)).findByNameIn(any());
    }

    @Test
    public void testGetCurentUser() {
        List<GrantedAuthority> list = new ArrayList<>();
        UserDetails user =
                new org.springframework.security.core.userdetails.User(username, UUID.randomUUID().toString(), list);
        User mockUser = dataProvider.getMockUser();
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, user.getPassword());
        doReturn(authentication).when(userService).getCurrentAuthentication();
        when(userRepository.findByName(anyString())).thenReturn(mockUser);
        User persistedUser = userService.getCurrentUser();
        Assert.assertNotNull(persistedUser);
    }

    @Test
    public void testGetCurentUserNonUserDetails() {
        List<GrantedAuthority> list = new ArrayList<>();
        UserDetails user =
                new org.springframework.security.core.userdetails.User(username, UUID.randomUUID().toString(), list);
        User mockUser = dataProvider.getMockUser();
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword(), user.getAuthorities());
        doReturn(authentication).when(userService).getCurrentAuthentication();
        when(userRepository.findByName(anyString())).thenReturn(mockUser);
        User persistedUser = userService.getCurrentUser();
        Assert.assertNotNull(persistedUser);
    }

    @Test
    public void testGetDefaultRole() {
        Role role = rolesMockData.getDummyRole();

        when(roleService.getRoleByName(anyString())).thenReturn(role);

        Role persistedRole = userService.getDefaultUserRole();
        Assert.assertEquals(persistedRole, role);
        verify(roleService, times(1)).getRoleByName(anyString());
    }

    @Test
    public void testGetPrivilegeForUser() {
        User user = dataProvider.getMockUser3();

        when(userRepository.findByName(anyString())).thenReturn(user);
        when(roleService.getRolesByNames(any())).thenReturn(new ArrayList<>(user.getRoles()));
        List<Privilege> privilegeList = new ArrayList<>();
        user.getRoles().stream().map(Role::getPrivileges).forEach((pri) -> privilegeList.addAll(pri));
        when(privilegeService.getPrivilegeByNames(any())).thenReturn(privilegeList);

        List<Privilege> privileges = userService.getPrivilegeForUser(username);
        Assert.assertEquals(privileges.size(), 2);
    }


}
