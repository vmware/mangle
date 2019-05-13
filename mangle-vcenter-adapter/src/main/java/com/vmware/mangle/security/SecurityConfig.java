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

package com.vmware.mangle.security;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vmware.mangle.service.CustomerUserDetailsService;

/**
 * @author Chethan C(chetanc)
 */

/**
 * @author chetanc
 *
 *         Configures security for the current application, defines the
 *         authentication and authorization rules for the app
 */
@EnableWebSecurity
@Log4j2
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final CustomerUserDetailsService customerUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfig(CustomerUserDetailsService customerUserDetailsService,
            PasswordEncoder passwordEncoder) {
        this.customerUserDetailsService = customerUserDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Disables cors, csrf Enables httpBasic authentication on the rest endpoints
     *
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        log.debug("Disabling cors, csrf. Enabling http basic authentication for rest endpoints");
        http.cors().and().csrf().disable().authorizeRequests().antMatchers("/api/v2/vcenter/**").hasAnyRole("USER")
                .antMatchers("/v2/api-docs").authenticated().and().httpBasic();
    }

    /**
     * Setting default passwordEncoder and userDetailsService
     *
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        log.debug(
                "Setting UserDetailsService and BCryptPasswordEncoder as the default for UserDaoAuthentication Provider");
        auth.authenticationProvider(new UserDaoAuthenticationProvider(customerUserDetailsService, passwordEncoder));
    }
}
