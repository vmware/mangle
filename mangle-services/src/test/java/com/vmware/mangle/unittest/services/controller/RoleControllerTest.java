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
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j2;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.security.Privilege;
import com.vmware.mangle.cassandra.model.security.Role;
import com.vmware.mangle.cassandra.model.security.User;
import com.vmware.mangle.model.response.DeleteOperationResponse;
import com.vmware.mangle.services.PrivilegeService;
import com.vmware.mangle.services.RoleService;
import com.vmware.mangle.services.controller.RoleController;
import com.vmware.mangle.services.deletionutils.RoleDeletionService;
import com.vmware.mangle.services.mockdata.RolesMockData;
import com.vmware.mangle.services.mockdata.UserMockData;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 *
 *
 * @author chetanc
 */
@Log4j2
public class RoleControllerTest extends PowerMockTestCase {

    @InjectMocks
    private RoleController roleController;

    @Mock
    private RoleService roleService;

    @Mock
    private PrivilegeService privilegeService;

    @Mock
    private RoleDeletionService roleDeletionService;

    private RolesMockData rolesMockData = new RolesMockData();
    private UserMockData userMockData = new UserMockData();

    @BeforeTest
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for {@link RoleController#getAllRoles()}.
     *
     */
    @Test
    public void getAllRolesTest() {
        log.info("Executing test: getAllRolesTest on RoleController#getAllRoles");
        Role role = rolesMockData.getDummyRole();
        List<Role> roles = new ArrayList<>();
        roles.add(role);

        when(roleService.getAllRoles()).thenReturn(roles);
        ResponseEntity<Resources<Role>> response = roleController.getAllRoles(null);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);

        Resources<Role> resources = response.getBody();
        Assert.assertEquals(resources.getContent().size(), roles.size());
    }

    /**
     * Test method for {@link RoleController#getAllRoles()}.
     *
     */
    @SuppressWarnings("unchecked")
    @Test
    public void getAllRolesUsingNamesTest() {
        log.info("Executing test: getAllRolesTest on RoleController#getAllRoles");
        Role role = rolesMockData.getDummyRole();
        List<Role> roles = new ArrayList<>();
        roles.add(role);

        when(roleService.getRolesByNames(anyList())).thenReturn(roles);
        ResponseEntity<Resources<Role>> response = roleController.getAllRoles(Arrays.asList(role.getName()));
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);

        Resources<Role> resources = response.getBody();
        Assert.assertEquals(resources.getContent().size(), roles.size());
    }

    /**
     * Test method for {@link RoleController#getPrivileges()}.
     *
     */
    @Test
    public void getPrivilegesTest() {
        log.info("Executing test: getPrivilegesTest on RoleController#getPrivileges()");
        Role role = rolesMockData.getDummyRole();
        List<Privilege> privileges = new ArrayList<>();
        privileges.addAll(role.getPrivileges());

        when(privilegeService.getAllPrivileges()).thenReturn(privileges);
        ResponseEntity<Resources<Privilege>> response = roleController.getPrivileges();
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);

        Resources<Privilege> resources = response.getBody();
        Assert.assertEquals(resources.getContent().size(), privileges.size());
        Mockito.verify(privilegeService, Mockito.atLeastOnce()).getAllPrivileges();
    }

    /**
     * Test method for {@link RoleController#deleteRoles(List)}.
     *
     */
    @Test
    public void deleteRolesTest() throws MangleException {
        log.info("Executing test: getPrivilegesTest on RoleController#deleteRoles(List)");
        Role role = rolesMockData.getDummyRole();
        List<String> roleNames = new ArrayList<>();
        roleNames.add(role.getName());

        when(roleDeletionService.deleteRolesByNames(roleNames)).thenReturn(new DeleteOperationResponse());

        ResponseEntity<DeleteOperationResponse> response = roleController.deleteRoles(roleNames);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody().getAssociations().size(), 0);
        Mockito.verify(roleDeletionService, times(1)).deleteRolesByNames(any());
    }

    /**
     * Test method for {@link RoleController#deleteRoles(List)}.
     *
     */
    @Test
    public void deleteRolesTestActiveAssociations() throws MangleException {
        log.info("Executing test: getPrivilegesTest on RoleController#deleteRoles(List)");
        Role role = rolesMockData.getDummyRole();
        List<String> roleNames = new ArrayList<>();
        roleNames.add(role.getName());

        User user = userMockData.getMockUser();
        Map<String, List<String>> associations = new HashMap<>();
        associations.put(role.getName(), Arrays.asList(user.getName()));
        DeleteOperationResponse deleteOperationResponse = new DeleteOperationResponse();
        deleteOperationResponse.setAssociations(associations);
        deleteOperationResponse.setResponseMessage(ErrorConstants.ROLE_DELETION_PRE_CONDITION_FAILURE);

        when(roleDeletionService.deleteRolesByNames(roleNames)).thenReturn(deleteOperationResponse);

        ResponseEntity<DeleteOperationResponse> response = roleController.deleteRoles(roleNames);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.PRECONDITION_FAILED);
        Assert.assertEquals(response.getBody().getAssociations().size(), 1);
        Mockito.verify(roleDeletionService, times(1)).deleteRolesByNames(any());
    }

    /**
     * Test method for {@link RoleController#createRole(Role)}.
     *
     */
    @Test
    public void createRoleTest() throws MangleException {
        log.info("Executing test: getPrivilegesTest on RoleController#createRole(Role)");
        Role role = rolesMockData.getDummyRole();
        when(roleService.createRole(Mockito.any())).thenReturn(role);

        ResponseEntity<Resource<Role>> response = roleController.createRole(role);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.CREATED);

        Resource<Role> resource = response.getBody();
        Assert.assertEquals(resource.getContent(), role);

        Mockito.verify(roleService, Mockito.times(1)).createRole(Mockito.any());
    }

    /**
     * Test method for {@link RoleController#createRole(Role)}.
     *
     */
    @Test(expectedExceptions = MangleException.class)
    public void createRoleTestFailure() throws MangleException {
        log.info("Executing test: updateRoleTest on RoleController#createRole(Role)");
        Role role = rolesMockData.getDummyRole();
        Mockito.doThrow(new MangleException(ErrorCode.NO_RECORD_FOUND)).when(roleService).createRole(Mockito.any());

        try {
            roleController.createRole(role);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
            Mockito.verify(roleService, Mockito.times(1)).createRole(Mockito.any());
            throw e;

        }
    }

    /**
     * Test method for {@link RoleController#createRole(Role)}.
     *
     */
    @Test
    public void updateRoleTest() throws MangleException {
        log.info("Executing test: updateRoleTest on RoleController#createRole(Role)");
        Role role = rolesMockData.getDummyRole();
        when(roleService.updateRole(Mockito.any())).thenReturn(role);

        ResponseEntity<Resource<Role>> response = roleController.updateRole(role);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);

        Resource<Role> resource = response.getBody();
        Assert.assertEquals(resource.getContent(), role);

        Mockito.verify(roleService, Mockito.atLeastOnce()).updateRole(Mockito.any());
    }

    /**
     * Test method for {@link RoleController#createRole(Role)}.
     *
     */
    @Test(expectedExceptions = MangleException.class)
    public void updateRoleTestException() throws MangleException {
        log.info("Executing test: updateRoleTest on RoleController#createRole(Role)");
        Role role = rolesMockData.getDummyRole();
        Mockito.doThrow(new MangleException(ErrorCode.NO_RECORD_FOUND)).when(roleService).updateRole(Mockito.any());

        try {
            roleController.updateRole(role);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
            Mockito.verify(roleService, Mockito.atLeastOnce()).updateRole(Mockito.any());
            throw e;

        }
    }
}
