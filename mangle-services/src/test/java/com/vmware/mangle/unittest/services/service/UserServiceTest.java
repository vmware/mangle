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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.stubbing.answers.ReturnsArgumentAt;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.security.ADAuthProviderDto;
import com.vmware.mangle.cassandra.model.security.PasswordReset;
import com.vmware.mangle.cassandra.model.security.Privilege;
import com.vmware.mangle.cassandra.model.security.Role;
import com.vmware.mangle.cassandra.model.security.User;
import com.vmware.mangle.services.ADAuthProviderService;
import com.vmware.mangle.services.PasswordResetService;
import com.vmware.mangle.services.PrivilegeService;
import com.vmware.mangle.services.RoleService;
import com.vmware.mangle.services.UserService;
import com.vmware.mangle.services.config.CustomActiveDirectoryLdapAuthenticationProvider;
import com.vmware.mangle.services.mockdata.RolesMockData;
import com.vmware.mangle.services.mockdata.UserMockData;
import com.vmware.mangle.services.repository.PasswordResetRepository;
import com.vmware.mangle.services.repository.UserRepository;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * TestNG class to test class UserServiceTest
 *
 * @author chetanc
 */
@PrepareForTest(value = { UserService.class, CustomActiveDirectoryLdapAuthenticationProvider.class })
@PowerMockIgnore(value = { "org.apache.logging.log4j.*", "com.vmware.mangle.utils.exceptions.*" })
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

    @Mock
    private PasswordResetRepository repository;
    private PasswordResetService passwordResetService;

    @Mock
    private CustomActiveDirectoryLdapAuthenticationProvider customProvider;
    private UserMockData dataProvider = new UserMockData();
    private RolesMockData rolesMockData = new RolesMockData();

    private String username = "USERNAME";

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        passwordResetService = spy(new PasswordResetService(repository));
        PowerMockito.mockStatic(CustomActiveDirectoryLdapAuthenticationProvider.class);
        userService = spy(new UserService(userRepository, privilegeService, authProviderService, passwordEncoder,
                passwordResetService));
        userService.setRoleService(roleService);
    }

    /**
     * Test method for {@link UserService#getRoleForUser(String)}
     */
    @Test
    public void getRoleForUserTestNull() {
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
    public void createUserTest() throws Exception {
        User user = dataProvider.getMockUser();
        ADAuthProviderDto adAuthProviderDto = new ADAuthProviderDto();
        adAuthProviderDto.setAdUser(UUID.randomUUID().toString());
        Set<String> domains = new HashSet<>(Collections.singletonList(Constants.LOCAL_DOMAIN_NAME));

        when(userRepository.save(any())).thenReturn(user);
        when(authProviderService.getADAuthProviderByAdDomain(anyString())).thenReturn(adAuthProviderDto);
        when(roleService.getRoleByName(any())).thenReturn(dataProvider.getDummyRole());
        when(authProviderService.getAllDomains()).thenReturn(domains);
        PowerMockito.whenNew(CustomActiveDirectoryLdapAuthenticationProvider.class).withAnyArguments()
                .thenReturn(customProvider);
        when(customProvider.searchForUser(anyString(), anyString(), anyString())).thenReturn(true);


        User persisted = userService.createUser(user);

        Assert.assertEquals(persisted, user);
        verify(userRepository, times(1)).save(any());
        verify(roleService, times(2)).getRoleByName(any());
        verify(authProviderService, times(0)).getADAuthProviderByAdDomain(anyString());
        verify(customProvider, times(0)).searchForUser(anyString(), anyString(), anyString());
    }

    /**
     * Test method for {@link UserService#createUser(User)}
     */
    @Test(expectedExceptions = MangleException.class)
    public void createUserTestFailure() throws Exception {
        User user = dataProvider.getMockUser();
        ADAuthProviderDto adAuthProviderDto = new ADAuthProviderDto();
        adAuthProviderDto.setAdUser(UUID.randomUUID().toString());
        adAuthProviderDto.setAdUrl(UUID.randomUUID().toString());

        when(authProviderService.getADAuthProviderByAdDomain(anyString())).thenReturn(adAuthProviderDto);
        PowerMockito.whenNew(CustomActiveDirectoryLdapAuthenticationProvider.class).withAnyArguments()
                .thenReturn(customProvider);
        when(customProvider.searchForUser(anyString(), anyString(), anyString())).thenReturn(true);

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
        User user = dataProvider.getMockUser();
        Set<String> domains = new HashSet<>(Collections.singletonList(Constants.LOCAL_DOMAIN_NAME));

        when(userRepository.save(any())).thenReturn(user);
        when(roleService.getRoleByName(any())).thenReturn(dataProvider.getDummyRole());
        when(authProviderService.getAllDomains()).thenReturn(domains);
        when(passwordEncoder.encode(anyString())).thenReturn(user.getPassword());
        doReturn(user).when(userService).getUserByName(any());
        User persisted = userService.updateUser(user);

        Assert.assertEquals(persisted, user);
        verify(userRepository, times(1)).save(any());
    }

    /**
     * Test method for {@link UserService#updateUser(User)}
     */
    @Test(expectedExceptions = MangleException.class)
    public void updateUserTestFailure() throws MangleException {
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
    @Test
    public void updatePasswordTest() throws MangleException {
        String newPassword = UUID.randomUUID().toString();
        User user = dataProvider.getMockUser();
        when(userRepository.findByName(anyString())).thenReturn(dataProvider.getMockUser2());
        when(passwordEncoder.matches(eq(user.getPassword()), anyString())).thenReturn(true);
        when(passwordEncoder.matches(eq(newPassword), anyString())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(user);

        userService.updatePassword(user.getName(), user.getPassword(), newPassword);
        verify(userRepository, times(1)).findByName(anyString());
        verify(userRepository, times(1)).save(any());
    }

    /**
     * Test method for {@link UserService#updateFirstTimePassword(String, String)}
     *
     * @throws MangleException
     */
    @Test
    public void updateFirstTimePasswordTestTrue() throws MangleException {
        String newPassword = UUID.randomUUID().toString();
        User user = dataProvider.getMockUser();
        List<PasswordReset> value = dataProvider.getPasswordResetListTrue();
        when(repository.findAll()).thenReturn(value);
        when(userRepository.findByName(anyString())).thenReturn(dataProvider.getMockUser2());
        when(passwordEncoder.matches(eq(user.getPassword()), anyString())).thenReturn(true);
        when(passwordEncoder.matches(eq(newPassword), anyString())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(user);

        try {
            userService.updateFirstTimePassword(user.getName(), user.getPassword());
        } catch (MangleException mangleException) {
            Assert.assertTrue(true);
        }
    }

    /**
     * Test method for {@link UserService#updateFirstTimePassword(String, String)}
     *
     * @throws MangleException
     */
    @Test
    public void updateFirstTimePasswordTestFalse() throws MangleException {
        String newPassword = UUID.randomUUID().toString();
        User user = dataProvider.getMockUser();
        List<PasswordReset> value = dataProvider.getPasswordResetListFalse();
        when(repository.findAll()).thenReturn(value);
        when(userRepository.findByName(anyString())).thenReturn(dataProvider.getMockUser2());
        when(passwordEncoder.matches(eq(user.getPassword()), anyString())).thenReturn(true);
        when(passwordEncoder.matches(eq(newPassword), anyString())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(user);
        userService.updateFirstTimePassword(user.getName(), user.getPassword());
        verify(userRepository, times(1)).findByName(anyString());
        verify(userRepository, times(1)).save(any());
    }

    /**
     * Test method for {@link UserService#updateFirstTimePassword(String, String)}
     *
     * @throws MangleException
     */
    @Test(expectedExceptions = MangleException.class)
    public void updateFirstTimePasswordTestNullDBUser() throws MangleException {
        User user = dataProvider.getMockUser();
        List<PasswordReset> value = dataProvider.getPasswordResetListFalse();
        when(repository.findAll()).thenReturn(value);
        when(userRepository.findByName(anyString())).thenReturn(null);
        try {
            userService.updateFirstTimePassword(user.getName(), user.getPassword());
        } catch (MangleException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.USER_NOT_FOUND);
            verify(userRepository, times(1)).findByName(anyString());
            throw mangleException;
        }

    }

    /**
     * Test method for {@link UserService#updateFirstTimePassword(String, String)}
     *
     * @throws MangleException
     */
    @Test
    public void updateFirstTimePasswordTestNoLocalDomain() throws MangleException {
        User user = dataProvider.getMockUser();
        user.setName("dummyUser@eso.local");
        List<PasswordReset> value = dataProvider.getPasswordResetListFalse();
        when(repository.findAll()).thenReturn(value);
        when(userRepository.findByName(anyString())).thenReturn(dataProvider.getMockUser2());
        try {
            userService.updateFirstTimePassword(user.getName(), user.getPassword());
        } catch (MangleException mangleException) {
            Assert.assertTrue(true);
        }
        verify(userRepository, times(1)).findByName(anyString());
    }

    /**
     * Test method for {@link UserService#updateUser(User)}
     */
    @Test(expectedExceptions = MangleException.class)
    public void testUpdatePasswordWithMatchingCurrentPassword() throws MangleException {
        String newPassword = UUID.randomUUID().toString();
        User user = dataProvider.getMockUser();
        when(userRepository.findByName(anyString())).thenReturn(dataProvider.getMockUser2());
        when(passwordEncoder.matches(eq(user.getPassword()), anyString())).thenReturn(true);
        when(passwordEncoder.matches(eq(newPassword), anyString())).thenReturn(true);
        when(userRepository.save(any())).thenReturn(user);

        try {
            userService.updatePassword(user.getName(), user.getPassword(), newPassword);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.NEW_CREDS_PASSWORD_MATCH_OLD_PASSWORD);
            verify(userRepository, times(1)).findByName(anyString());
            verify(userRepository, times(0)).save(any());
            throw e;
        }
    }

    /**
     * Test method for {@link UserService#updateUser(User)}
     */
    @Test(expectedExceptions = MangleException.class)
    public void testUpdatePasswordForNonLocalUser() throws MangleException {
        String newPassword = UUID.randomUUID().toString();
        User user = dataProvider.getMockUser();
        user.setName("dummyUser@eso.local");
        when(userRepository.findByName(anyString())).thenReturn(dataProvider.getMockUser2());
        when(passwordEncoder.matches(eq(user.getPassword()), anyString())).thenReturn(true);
        when(passwordEncoder.matches(eq(newPassword), anyString())).thenReturn(true);
        when(userRepository.save(any())).thenReturn(user);

        try {
            userService.updatePassword(user.getName(), user.getPassword(), newPassword);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.PASSWORD_CHANGE_FOR_NON_LOCAL_USER);
            verify(userRepository, times(1)).findByName(anyString());
            verify(userRepository, times(0)).save(any());
            throw e;
        }
    }

    /**
     * Test method for {@link UserService#updatePassword(String, String, String)}
     */
    @Test(expectedExceptions = MangleException.class)
    public void updatePasswordTestFailureNoUserFound() throws MangleException {
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
    public void testDeleteUsersByNamesCurrentUser() throws MangleException {
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
        when(userService.getCurrentUserName()).thenReturn(user.getName());
        doNothing().when(userService).terminateUserSession(anyString());
        try {
            userService.deleteUsersByNames(list);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.CURRENT_USER_DELETION_FAILED);
            verify(userRepository, times(0)).deleteByNameIn(any());
            verify(userRepository, times(1)).findByNameIn(any());
            throw e;
        }
    }

    /**
     * Test method for {@link UserService#deleteUsersByNames(List)}
     */
    @Test(expectedExceptions = MangleException.class)
    public void deleteUsersByNamesTestFailureAdminUserDelete() throws MangleException {
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

    @Test
    public void testResync() {
        User user = dataProvider.getMockUser();
        doNothing().when(userService).terminateUserSession(user.getName());
        userService.resync(user.getName());
        verify(userService, times(1)).terminateUserSession(user.getName());
    }

    @Test
    public void testGetUsersWithRole() {
        Role role = dataProvider.getDummyRole();
        User user = dataProvider.getMockUser();
        User user1 = dataProvider.getMockUser2();
        List<User> users = new ArrayList<>(Arrays.asList(user, user1));

        when(userRepository.findByRole(role.getName())).thenReturn(users);

        List<User> dbUsers = userService.getUsersForRole(role.getName());

        Assert.assertEquals(dbUsers.size(), users.size());
        verify(userRepository, times(1)).findByRole(role.getName());
    }

    @Test
    public void testCreateUserInsufficientADDetails() throws Exception {
        User user = dataProvider.getDummyADUser();
        ADAuthProviderDto adAuthProviderDto = dataProvider.getDummyAuthProvider(user);

        Set<String> domains =
                new HashSet<>(new ArrayList<>(Collections.singletonList(adAuthProviderDto.getAdDomain())));

        when(authProviderService.getADAuthProviderByAdDomain(anyString())).thenReturn(adAuthProviderDto);
        PowerMockito.whenNew(CustomActiveDirectoryLdapAuthenticationProvider.class).withAnyArguments()
                .thenReturn(customProvider);
        when(customProvider.searchForUser(anyString(), anyString(), anyString())).thenReturn(true);
        when(userRepository.save(any())).then(new ReturnsArgumentAt(0));
        when(authProviderService.getAllDomains()).thenReturn(domains);
        when(roleService.getRoleByName(anyString())).thenReturn(dataProvider.getDummyRole());

        User user1 = userService.createUser(user);

        Assert.assertEquals(user1, user);

        verify(userRepository, times(1)).save(any());
        verify(customProvider, times(1)).searchForUser(anyString(), anyString(), anyString());
        verify(authProviderService, times(1)).getADAuthProviderByAdDomain(anyString());
        verify(roleService, times(1)).getRoleByName(anyString());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testCreateUserNoUserOnAD() throws Exception {
        User user = dataProvider.getDummyADUser();
        ADAuthProviderDto adAuthProviderDto = dataProvider.getDummyAuthProvider(user);

        Set<String> domains =
                new HashSet<>(new ArrayList<>(Collections.singletonList(adAuthProviderDto.getAdDomain())));

        when(authProviderService.getADAuthProviderByAdDomain(anyString())).thenReturn(adAuthProviderDto);
        PowerMockito.whenNew(CustomActiveDirectoryLdapAuthenticationProvider.class).withAnyArguments()
                .thenReturn(customProvider);
        when(customProvider.searchForUser(anyString(), anyString(), anyString())).thenReturn(false);
        when(userRepository.save(any())).then(new ReturnsArgumentAt(0));
        when(authProviderService.getAllDomains()).thenReturn(domains);
        when(roleService.getRoleByName(anyString())).thenReturn(dataProvider.getDummyRole());

        try {
            userService.createUser(user);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.USER_NOT_FOUND_ON_AD);

            verify(userRepository, times(0)).save(any());
            verify(customProvider, times(1)).searchForUser(anyString(), anyString(), anyString());
            verify(authProviderService, times(1)).getADAuthProviderByAdDomain(anyString());
            verify(roleService, times(0)).getRoleByName(anyString());

            throw e;
        }
    }

    @Test
    public void testValidateADDetailsForUserCreation() throws MangleException {
        User user = dataProvider.getDummyADUser();
        ADAuthProviderDto adAuthProviderDto = dataProvider.getDummyAuthProvider(user);

        when(authProviderService.getADAuthProviderByAdDomain(anyString())).thenReturn(adAuthProviderDto);

        userService.validateADDetailsForUserCreation(user);

        verify(authProviderService, times(1)).getADAuthProviderByAdDomain(anyString());

    }

    @Test(expectedExceptions = MangleException.class)
    public void testValidateADDetailsForUserCreationInsufficientDetails() throws MangleException {
        User user = dataProvider.getDummyADUser();
        ADAuthProviderDto adAuthProviderDto = dataProvider.getDummyAuthProviderForV1();

        when(authProviderService.getADAuthProviderByAdDomain(anyString())).thenReturn(adAuthProviderDto);

        try {
            userService.validateADDetailsForUserCreation(user);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.AD_DETAILS_INSUFFICIENT_FOR_USER_ADDITION);

            verify(authProviderService, times(1)).getADAuthProviderByAdDomain(anyString());
            throw e;
        }
    }

    @Test
    public void testTerminateSessionActiveSession() {
        SessionInformation sessionInformation = mock(SessionInformation.class);
        User user = dataProvider.getMockUser();
        List<SessionInformation> userSessions = Collections.singletonList(sessionInformation);
        Map<String, List<SessionInformation>> usersToSessionsMapping = new HashMap<>();
        usersToSessionsMapping.put(user.getName(), userSessions);

        doReturn(usersToSessionsMapping).when(userService).allUserToSessionsMapping();
        doNothing().when(sessionInformation).expireNow();
        when(sessionInformation.isExpired()).thenReturn(false);

        userService.terminateUserSession(user.getName());

        verify(userService, times(1)).allUserToSessionsMapping();
        verify(sessionInformation, times(1)).expireNow();
        verify(sessionInformation, times(1)).isExpired();

    }

    @Test
    public void testTerminateSessionNoActiveSession() {
        SessionInformation sessionInformation = mock(SessionInformation.class);
        User user = dataProvider.getMockUser();
        List<SessionInformation> userSessions = Collections.singletonList(sessionInformation);
        Map<String, List<SessionInformation>> usersToSessionsMapping = new HashMap<>();
        usersToSessionsMapping.put(user.getName(), userSessions);

        doReturn(usersToSessionsMapping).when(userService).allUserToSessionsMapping();
        doNothing().when(sessionInformation).expireNow();
        when(sessionInformation.isExpired()).thenReturn(true);

        userService.terminateUserSession(user.getName());

        verify(userService, times(1)).allUserToSessionsMapping();
        verify(sessionInformation, times(0)).expireNow();
        verify(sessionInformation, times(1)).isExpired();

    }

    @Test
    public void testTerminateSessionNoRegisteredSession() {
        SessionInformation sessionInformation = mock(SessionInformation.class);
        User user = dataProvider.getMockUser();
        List<SessionInformation> userSessions = Collections.singletonList(sessionInformation);
        Map<String, List<SessionInformation>> usersToSessionsMapping = new HashMap<>();
        usersToSessionsMapping.put(UUID.randomUUID().toString(), userSessions);

        doReturn(usersToSessionsMapping).when(userService).allUserToSessionsMapping();
        doNothing().when(sessionInformation).expireNow();
        when(sessionInformation.isExpired()).thenReturn(true);

        userService.terminateUserSession(user.getName());

        verify(userService, times(1)).allUserToSessionsMapping();
        verify(sessionInformation, times(0)).expireNow();
        verify(sessionInformation, times(0)).isExpired();
    }
}
