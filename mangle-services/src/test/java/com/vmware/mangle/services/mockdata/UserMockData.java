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

package com.vmware.mangle.services.mockdata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vmware.mangle.cassandra.model.security.Privilege;
import com.vmware.mangle.cassandra.model.security.Role;
import com.vmware.mangle.cassandra.model.security.User;
import com.vmware.mangle.cassandra.model.security.UserLoginAttempts;
import com.vmware.mangle.model.UserPasswordUpdateDTO;
import com.vmware.mangle.utils.constants.Constants;

/**
 *
 *
 * @author chetanc
 */
public class UserMockData {
    private static final String USER1 = "DUMMY_USER@" + Constants.LOCAL_DOMAIN_NAME;
    private static final String USER2 = "DUMMY_USER2@" + Constants.LOCAL_DOMAIN_NAME;
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_USER = "ROLE_USER";
    private static final String pwd = UUID.randomUUID().toString();
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private RolesMockData rolesMockData = new RolesMockData();

    public List<String> getUsersList() {
        return new ArrayList<>(Arrays.asList(USER1, USER2));
    }

    public User getMockUser() {
        User user = new User(USER1, pwd, getDummyRole());
        return user;
    }

    public Role getDummyRole() {
        Role role = new Role();
        role.setName(ROLE_ADMIN);
        return role;

    }

    public Role getDummyRole3() {
        Role role = new Role();
        role.setName(ROLE_ADMIN);
        Set<Privilege> privilegeSet = new HashSet<>();
        privilegeSet.add(rolesMockData.getDummyPrivilege());
        privilegeSet.add(rolesMockData.getDummy2Privilege());
        role.setPrivileges(privilegeSet);
        role.setPrivilegeNames(privilegeSet.stream().map(Privilege::getName).collect(Collectors.toSet()));
        return role;

    }

    public User getMockUser2() {
        Role role = new Role();
        role.setName(ROLE_ADMIN);
        User user = new User(USER2, pwd, role);
        return user;
    }


    public User getMockUser3() {
        User user = new User(USER1, pwd, getDummyRole3());
        return user;
    }

    public User getUpdateMockUser() {
        Role role = new Role();
        role.setName(ROLE_USER);
        User user = new User(USER1, pwd, role);
        return user;
    }

    public UserLoginAttempts getUserLoginAttemptsForUser(String username) {
        return new UserLoginAttempts(username, 3, new Date());
    }

    public User getLockedMockUser() {
        User user = getMockUser();
        user.setAccountLocked(true);
        return user;
    }

    public UserPasswordUpdateDTO getUserPasswordUpdateDTO() {
        return new UserPasswordUpdateDTO(pwd, pwd);
    }

    public UserDetails getMockUserDetails() {
        ArrayList<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(rolesMockData.getDummyPrivilege().getName()));
        return new org.springframework.security.core.userdetails.User(USER1, passwordEncoder.encode(pwd), true, true,
                true, true, authorities);
    }

    public UserDetails getMockLockedUserDetails() {
        ArrayList<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(rolesMockData.getDummyPrivilege().getName()));
        return new org.springframework.security.core.userdetails.User(USER1, passwordEncoder.encode(pwd), true, true,
                true, false, authorities);
    }

    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}
