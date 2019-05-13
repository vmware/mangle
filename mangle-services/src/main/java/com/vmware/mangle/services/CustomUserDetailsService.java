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
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.vmware.mangle.cassandra.model.security.Privilege;
import com.vmware.mangle.cassandra.model.security.User;

/**
 *
 *
 * @author chetanc
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private UserService userService;

    @Autowired
    public CustomUserDetailsService(UserService userService) {
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
        User user = userService.getUserByName(username);

        if (user != null) {
            List<GrantedAuthority> authorities = getUserAuthority(username);
            return buildUserForAuthentication(user, authorities);
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
    private UserDetails buildUserForAuthentication(User user, List<GrantedAuthority> authorities) {
        return new org.springframework.security.core.userdetails.User(user.getName(), user.getPassword(), authorities);
    }
}
