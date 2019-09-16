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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.security.Role;
import com.vmware.mangle.model.enums.MangleScopeEnum;
import com.vmware.mangle.services.PrivilegeService;
import com.vmware.mangle.services.RoleService;
import com.vmware.mangle.services.UserService;
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
public class RoleServiceTest extends PowerMockTestCase {

    @InjectMocks
    private RoleService roleService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PrivilegeService privilegeService;

    @Mock
    private UserService userService;

    private RolesMockData rolesMockData = new RolesMockData();

    @BeforeTest
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for {@link RoleService#getAllRoles()}.
     *
     */
    @Test
    public void getAllRolesServiceTest() {
        log.info("Executing test: getAllRolesServiceTest on RoleService#getAllRoles");
        List<Role> roles = rolesMockData.getRoles();
        when(roleRepository.findAll()).thenReturn(roles);
        List<Role> response = roleService.getAllRoles();
        Mockito.verify(roleRepository, Mockito.times(1)).findAll();
        Assert.assertEquals(response.size(), roles.size());
    }

    /**
     * Test method for {@link RoleService#getRoleByName(String)}.
     *
     */
    @Test
    public void getRoleByNameTest() {
        log.info("Executing test: getRoleByNameTest on RoleService#getRoleByName");
        Role role = rolesMockData.getDummyRole();
        when(roleRepository.findByName(anyString())).thenReturn(role);
        Role persistedTest = roleService.getRoleByName(role.getName());
        Mockito.verify(roleRepository, Mockito.atLeastOnce()).findByName(anyString());
        Assert.assertEquals(persistedTest, role);
    }

    /**
     * Test method for {@link RoleService#getRolesByNames(Collection)}
     *
     */
    @Test
    public void getRolesByNamesTest() {
        log.info("Executing test: getRolesByNamesTest on RoleService#getgetRolesByNames");
        List<Role> roles = new ArrayList<>();
        Role role = rolesMockData.getDummyRole();
        roles.add(role);
        List<String> roleNames = new ArrayList<>();
        roleNames.add(role.getName());
        when(roleRepository.findByNameIn(any())).thenReturn(roles);
        List<Role> persistedTest = roleService.getRolesByNames(roleNames);
        Mockito.verify(roleRepository, Mockito.atLeastOnce()).findByNameIn(any());
        Assert.assertEquals(persistedTest.get(0), role);
    }

    /**
     * Test method for {@link RoleService#createRole(Role)}
     *
     */
    @Test
    public void createRoleTest() throws MangleException {
        log.info("Executing test: createRoleTest on method RoleService#createRole(Role)");
        Role role = rolesMockData.getDummyRole();
        when(roleRepository.save(Mockito.any())).thenReturn(role);
        when(privilegeService.getPrivilege(anyString())).thenReturn(rolesMockData.getDummyPrivilege());
        Role persistedTest = roleService.createRole(role);
        Mockito.verify(roleRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(privilegeService, Mockito.times(1)).getPrivilege(Mockito.anyString());
        Assert.assertEquals(persistedTest, role);
    }

    /**
     * Test method for {@link RoleService#createRole(Role)}
     *
     */
    @Test(expectedExceptions = MangleException.class)
    public void createRoleTestFailure() throws MangleException {
        log.info("Executing test: createRoleTest on method RoleService#createRole(Role)");
        Role role = rolesMockData.getDummyRole();
        when(roleRepository.save(Mockito.any())).thenReturn(role);
        when(privilegeService.getPrivilege(anyString())).thenReturn(null);
        try {
            roleService.createRole(role);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
            Mockito.verify(privilegeService, Mockito.times(1)).getPrivilege(Mockito.anyString());
            throw e;
        }
    }

    /**
     * Test method for {@link RoleService#createRole(Role)}
     *
     */
    @Test(expectedExceptions = MangleException.class)
    public void createRoleTestFailureEmptyPrivileges() throws MangleException {
        log.info("Executing test: createRoleTest on method RoleService#createRole(Role)");
        Role role = rolesMockData.getDummyRole();
        role.setPrivilegeNames(new HashSet<>());
        try {
            roleService.createRole(role);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.CUSTOM_ROLE_CREATION_FAILED_NO_PRVILEGES);
            Mockito.verify(privilegeService, Mockito.times(0)).getPrivilege(Mockito.anyString());
            throw e;
        }
    }

    /**
     * Test method for {@link RoleService#updateRole(Role)}
     *
     */
    @Test(expectedExceptions = MangleException.class)
    public void updateRoleTestFailureNoRecord() throws MangleException {
        log.info("Executing test: updateRoleTestFailureNoRecord on method RoleService#updateRole(Role)");
        Role role = rolesMockData.getDummyRole();
        when(roleRepository.findByName(anyString())).thenReturn(null);
        try {
            roleService.updateRole(role);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.ROLE_NOT_FOUND);
            Mockito.verify(roleRepository, Mockito.times(1)).findByName(Mockito.any());
            throw e;
        }
    }

    /**
     * Test method for {@link RoleService#updateRole(Role)}
     *
     */
    @Test(expectedExceptions = MangleException.class)
    public void updateRoleTestFailureDefaultDelete() throws MangleException {
        log.info("Executing test: updateRoleTestFailureDefaultDelete on method RoleService#updateRole(Role)");
        Role role = rolesMockData.getDummyRole();
        role.setType(MangleScopeEnum.MANGLE_DEFAULT);

        when(privilegeService.getPrivilege(anyString())).thenReturn(rolesMockData.getDummyPrivilege());
        when(roleRepository.findByName(anyString())).thenReturn(role);
        try {
            roleService.updateRole(role);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.DEFAULT_ROLE_DELETE);
            Mockito.verify(roleRepository, Mockito.times(1)).findByName(Mockito.any());
            throw e;
        }
    }


    /**
     * Test method for {@link RoleService#updateRole(Role)}
     *
     */
    @Test
    public void udpateRoleTest() throws MangleException {
        log.info("Executing test: udpateRoleTest on method RoleService#updateRole(Role)");
        Role role = rolesMockData.getDummyRole();
        when(roleRepository.save(Mockito.any())).thenReturn(role);
        when(roleRepository.findByName(anyString())).thenReturn(role);
        when(privilegeService.getPrivilege(anyString())).thenReturn(rolesMockData.getDummyPrivilege());
        Role persistedTest = roleService.updateRole(role);
        Mockito.verify(roleRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(privilegeService, Mockito.times(1)).getPrivilegeByNames(Mockito.any());
        Mockito.verify(roleRepository, Mockito.times(1)).findByName(Mockito.any());
        Assert.assertEquals(persistedTest, role);
    }
}
