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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.security.Privilege;
import com.vmware.mangle.services.PrivilegeService;
import com.vmware.mangle.services.RoleService;
import com.vmware.mangle.services.repository.PrivilegeRepository;

/**
 *
 *
 * @author chetanc
 */
@Log4j2
public class PrivilegeServiceTest {
    private static String privilegeName = "privilege";

    @Mock
    private PrivilegeRepository repository;

    private PrivilegeService service;
    @Mock
    private RoleService roleService;

    @BeforeClass
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        service = new PrivilegeService(repository);
    }

    /**
     * Test method for {@link PrivilegeService#getPrivilege(String)}
     */
    @Test
    public void getPrivilegeTest() {
        log.info("Executing test getPrivilegeTest on PrivilegeService#getPrivilege");
        Privilege privilege = new Privilege();
        when(repository.findByName(anyString())).thenReturn(privilege);
        Privilege persisted = service.getPrivilege(privilegeName);
        Assert.assertEquals(persisted, privilege);
        verify(repository, times(1)).findByName(anyString());
    }

    /**
     * Test method for {@link PrivilegeService#getPrivilegeByNames(Set)}
     */
    @Test
    public void getPrivilegeByNamesTest() {
        log.info("Executing test getPrivilegeByNamesTest on PrivilegeService#getPrivilegeByNames");
        Privilege privilege = new Privilege();
        privilege.setName("dummy_privi");
        List<Privilege> priList = new ArrayList<>();
        priList.add(privilege);
        when(repository.findByNameIn(any())).thenReturn(priList);
        List<Privilege> persisted =
                service.getPrivilegeByNames(priList.stream().map(Privilege::getName).collect(Collectors.toSet()));
        Assert.assertEquals(persisted.get(0), privilege);
        verify(repository, times(1)).findByNameIn(any());
    }

    /**
     * Test method for {@link PrivilegeService#getAllPrivileges()}
     */
    @Test
    public void getAllPrivilegesTest() {
        log.info("Executing test getAllPrivilegesTest on PrivilegeService#getAllPrivileges()");
        Privilege privilege1 = new Privilege();
        Privilege privilege2 = new Privilege();
        List<Privilege> list = new ArrayList<>(Arrays.asList(privilege1, privilege2));
        when(repository.findAll()).thenReturn(list);
        List<Privilege> persisted = service.getAllPrivileges();
        Assert.assertEquals(persisted.size(), 2);
        verify(repository, times(1)).findAll();
    }
}
