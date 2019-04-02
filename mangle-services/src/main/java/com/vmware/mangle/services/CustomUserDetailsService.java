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

package com.vmware.mangle.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.vmware.mangle.cassandra.model.security.Privilege;
import com.vmware.mangle.cassandra.model.security.Role;
import com.vmware.mangle.cassandra.model.security.User;
import com.vmware.mangle.cassandra.model.security.UserAuthentication;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 *
 *
 * @author chetanc
 */
@Log4j2
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private UserAuthenticationService userAuthenticationService;
    private UserService userService;

    @Autowired
    public CustomUserDetailsService(UserAuthenticationService service, UserService userService) {
        this.userAuthenticationService = service;
        this.userService = userService;

    }

    /**
     * creates the userDetails object as required by the spring security This method queries the
     * user in db for given username, and user authorities
     *
     * @param username
     *            for which user details need to be loaded
     * @return
     * @throws UsernameNotFoundException
     *             if the user entry is not present in the db
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        UserAuthentication userAuthentication;

        try {
            userAuthentication = userAuthenticationService.getUserByUsername(username);
        } catch (MangleException e) {
            throw new UsernameNotFoundException(e.getMessage());
        }

        if (userAuthentication != null) {
            List<GrantedAuthority> authorities = getUserAuthority(username);
            return buildUserForAuthentication(userAuthentication, authorities);
        } else {
            throw new UsernameNotFoundException("username not found");
        }
    }

    /**
     * Get the list of spring security granted authorities for a given user, if the username is not
     * already configured with a roles, he will be created with a default user entry with default
     * role: ROLE_READONLY
     *
     * @param username
     *            username for which the list of authorities need to be fetched
     * @return list of granted authorities
     */
    public List<GrantedAuthority> getUserAuthority(String username) {
        ArrayList<GrantedAuthority> authorities = new ArrayList<>();
        List<Privilege> privileges = userService.getPrivilegeForUser(username);
        if (!privileges.isEmpty()) {
            for (Privilege privilege : privileges) {
                authorities.add(new SimpleGrantedAuthority(privilege.getName()));
            }
        } else {
            User user = new User();
            user.setName(username);
            Set<Role> userRoles = new HashSet<>();
            Role defaultRole = userService.getDefaultUserRole();
            userRoles.add(defaultRole);
            user.setRoles(userRoles);
            Set<String> userRoleName = new HashSet<>();
            userRoleName.add(defaultRole.getName());
            user.setRoleNames(userRoleName);
            try {
                userService.createUser(user);
            } catch (MangleException e) {
                log.info(String.format("New local user creation failed for username %s", username));
            }
            for (Privilege privilege : defaultRole.getPrivileges()) {
                authorities.add(new SimpleGrantedAuthority(privilege.getName()));
            }
        }
        return authorities;
    }

    /**
     * Creates a user object for the authentication as required for the spring
     *
     * @param user
     * @param authorities
     * @return UserDetails object as required by the spring security
     */
    private UserDetails buildUserForAuthentication(UserAuthentication user, List<GrantedAuthority> authorities) {
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
                authorities);
    }
}
