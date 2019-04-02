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

package com.vmware.mangle.unittest.services.service.deletionutils;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.security.Role;
import com.vmware.mangle.model.enums.MangleScopeEnum;
import com.vmware.mangle.services.UserService;
import com.vmware.mangle.services.deletionutils.RoleDeletionService;
import com.vmware.mangle.services.mockdata.RolesMockData;
import com.vmware.mangle.services.repository.RoleRepository;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
*
*
* @author chetanc
*/
@Log4j2
public class RoleDeletionServiceTest {
    private RoleDeletionService roleDeletionService;

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserService userService;

    private RolesMockData rolesMockData = new RolesMockData();

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        roleDeletionService = new RoleDeletionService(roleRepository, userService);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.deletionutils.RoleDeletionService#deleteRolesByNames(List)}.
     *
     */
    @Test
    public void deleteRolesByNamesTestSuccess() throws MangleException {
        log.info("Executing test: deleteRolesByNamesTestSuccess on RoleService#getRoleByName");
        Role role = rolesMockData.getDummyRole();
        List<String> roles = new ArrayList<>();
        roles.add(role.getName());

        when(roleRepository.findByName(anyString())).thenReturn(role);
        Mockito.doNothing().when(roleRepository).delete(Mockito.any());
        when(userService.getUsersForRole(role.getName())).thenReturn(new ArrayList<>());

        roleDeletionService.deleteRolesByNames(roles);

        Mockito.verify(roleRepository, Mockito.atLeastOnce()).findByName(anyString());
        verify(userService, times(1)).getUsersForRole(role.getName());
    }

    /**
     * Test method for {@link RoleDeletionService#deleteRolesByNames(List)}.
     *
     */
    @Test(expectedExceptions = MangleException.class)
    public void deleteRolesByNamesTestFailureDefaultRole() throws MangleException {
        log.info("Executing test: deleteRolesByNamesTestFailureDefaultRole on RoleService#getRoleByName");
        Role role = rolesMockData.getDummyRole();
        role.setType(MangleScopeEnum.MANGLE_DEFAULT);
        List<String> roles = new ArrayList<>();
        roles.add(role.getName());


        when(roleRepository.findByName(anyString())).thenReturn(role);

        try {
            roleDeletionService.deleteRolesByNames(roles);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.DEFAULT_ROLE_DELETE);
            Mockito.verify(roleRepository, Mockito.atLeastOnce()).findByName(anyString());
            throw e;
        }
    }

    /**
     * Test method for {@link RoleDeletionService#deleteRolesByNames(List)}.
     *
     */
    @Test(expectedExceptions = MangleException.class)
    public void deleteRolesByNamesTestFailureNoRecord() throws MangleException {
        log.info("Executing test: deleteRolesByNamesTestFailureDefaultRole on RoleService#deleteRolesByNames");
        Role role = rolesMockData.getDummyRole();
        role.setType(MangleScopeEnum.MANGLE_DEFAULT);
        List<String> roles = new ArrayList<>();
        roles.add(role.getName());


        when(roleRepository.findByName(anyString())).thenReturn(null);

        try {
            roleDeletionService.deleteRolesByNames(roles);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.ROLE_NOT_FOUND);
            Mockito.verify(roleRepository, Mockito.atLeastOnce()).findByName(anyString());
            throw e;
        }
    }

    /**
     * Test method for {@link RoleDeletionService#deleteRolesByNames(List)}.
     *
     */
    @Test(expectedExceptions = MangleException.class)
    public void deleteRoleByNameTestFailure() throws MangleException {
        log.info("Executing test: deleteRoleByNameTestFailure on RoleService#deleteRolesByNames");
        Role role = rolesMockData.getDummyRole();
        role.setType(MangleScopeEnum.MANGLE_DEFAULT);
        List<String> roles = new ArrayList<>();
        roles.add(role.getName());


        when(roleRepository.findByName(anyString())).thenReturn(role);
        Mockito.doNothing().when(roleRepository).delete(Mockito.any());

        try {
            roleDeletionService.deleteRoleByName(role.getName());
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.DEFAULT_ROLE_DELETE);
            Mockito.verify(roleRepository, Mockito.atLeastOnce()).findByName(anyString());
            throw e;
        }
    }

    /**
     * Test method for {@link RoleDeletionService#deleteRolesByNames(List)}.
     *
     */
    @Test
    public void deleteRoleByNameTestSuccess() throws MangleException {
        log.info("Executing test: deleteRoleByNameTestSuccess on RoleService#deleteRolesByNames");
        Role role = rolesMockData.getDummyRole();
        List<String> roles = new ArrayList<>();
        roles.add(role.getName());

        when(roleRepository.findByName(anyString())).thenReturn(role);
        Mockito.doNothing().when(roleRepository).delete(Mockito.any());
        when(userService.getUsersForRole(role.getName())).thenReturn(new ArrayList<>());

        roleDeletionService.deleteRoleByName(role.getName());
        Mockito.verify(roleRepository, Mockito.atLeastOnce()).findByName(anyString());
        Mockito.verify(roleRepository, Mockito.times(1)).delete(Mockito.any());
        verify(userService, times(1)).getUsersForRole(role.getName());
    }
}
