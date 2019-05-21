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

package com.vmware.mangle.services.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.vmware.mangle.cassandra.model.security.Privilege;
import com.vmware.mangle.cassandra.model.security.Role;
import com.vmware.mangle.model.enums.DefaultPrivileges;
import com.vmware.mangle.services.CustomUserDetailsService;
import com.vmware.mangle.services.PrivilegeService;
import com.vmware.mangle.services.UserService;

/**
 *
 * Configures security for the current application, defines the authentication and authorization
 * rules for the app
 *
 * @author chetanc
 */
@EnableWebSecurity
@Configuration
public class SecurityConfig implements WebMvcConfigurer {

    UserService userService;
    PrivilegeService privilegeService;
    ADAuthProvider adAuthProvider;
    CustomUserDetailsService customUserDetailsService;
    PasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfig(UserService userService, PrivilegeService privilegeService, ADAuthProvider adAuthProvider,
            CustomUserDetailsService customUserDetailsService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.privilegeService = privilegeService;
        this.adAuthProvider = adAuthProvider;
        this.customUserDetailsService = customUserDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Configuring authentication providers to the application
     *
     * @param auth
     * @throws Exception
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(adAuthProvider);
        auth.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder);
    }

    public List<GrantedAuthority> getAuthoritiesForUser(String username) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        Set<Role> roles = userService.getRoleForUser(username);
        if (roles != null) {
            for (Role role : roles) {
                role.setPrivileges(new HashSet<>(privilegeService.getPrivilegeByNames(role.getPrivilegeNames())));
                for (Privilege privilege : role.getPrivileges()) {
                    authorities.add(new SimpleGrantedAuthority(privilege.getName()));
                }
            }
        }
        return authorities;
    }

    @Configuration
    @Order(1)
    public static class ApiServiceWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        private final String adminReadWrite = DefaultPrivileges.ADMIN_READ_WRITE.name();
        private final String adminRead = DefaultPrivileges.ADMIN_READ.name();
        private final String userReadWrite = DefaultPrivileges.USER_READ_WRITE.name();
        private final String readOnly = DefaultPrivileges.READONLY.name();
        private final String rest = "/rest";
        private final String v1_api = "/api/v1/";
        private final String rest_v1_api = rest + v1_api;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.httpBasic().and().authorizeRequests()
                    .antMatchers("/", "/*.html", "/*.js", "/*.map", "/assets/**", "/*.ico").permitAll()

                    .antMatchers("/application/health").permitAll()

                    .antMatchers(rest_v1_api + "auth-provider-management/domains").permitAll()

                    .antMatchers(HttpMethod.GET, rest_v1_api + "user-management/users/admin").permitAll()

                    .antMatchers(HttpMethod.GET, rest_v1_api + "tasks")
                    .hasAnyAuthority(adminRead, adminReadWrite, readOnly, userReadWrite)

                    .antMatchers(HttpMethod.GET, rest_v1_api + "tasks/**")
                    .hasAnyAuthority(adminRead, adminReadWrite, readOnly, userReadWrite)

                    .antMatchers(HttpMethod.GET, rest_v1_api + "scheduler")
                    .hasAnyAuthority(adminRead, adminReadWrite, readOnly, userReadWrite)

                    .antMatchers(HttpMethod.GET, rest_v1_api + "endpoints")
                    .hasAnyAuthority(adminRead, adminReadWrite, readOnly, userReadWrite)

                    .antMatchers(HttpMethod.GET, rest_v1_api + "endpoints/**")
                    .hasAnyAuthority(adminRead, adminReadWrite, readOnly, userReadWrite)

                    .antMatchers(HttpMethod.GET, rest_v1_api + "role-management/roles")
                    .hasAnyAuthority(adminRead, adminReadWrite, readOnly, userReadWrite)

                    .antMatchers(HttpMethod.GET, rest_v1_api + "role-management/privileges")
                    .hasAnyAuthority(adminRead, adminReadWrite)

                    .antMatchers(rest_v1_api + "role-management/**").hasAuthority(adminReadWrite)

                    .antMatchers(rest_v1_api + "user-management/user")
                    .hasAnyAuthority(adminRead, adminReadWrite, readOnly)

                    .antMatchers(rest_v1_api + "user-management/password")
                    .hasAnyAuthority(adminRead, adminReadWrite, userReadWrite, readOnly)

                    .antMatchers(HttpMethod.GET, rest_v1_api + "user-management/**")
                    .hasAnyAuthority(adminRead, adminReadWrite)

                    .antMatchers(rest_v1_api + "user-management/**").hasAuthority(adminReadWrite)

                    .antMatchers(HttpMethod.GET, rest_v1_api + "auth-provider-management/**")
                    .hasAnyAuthority(adminRead, adminReadWrite)

                    .antMatchers(rest_v1_api + "auth-provider-management/**").hasAuthority(adminReadWrite)

                    .antMatchers(HttpMethod.GET, rest_v1_api + "scheduler/**")
                    .hasAnyAuthority(adminRead, adminReadWrite)

                    .antMatchers(rest_v1_api + "scheduler/**").hasAuthority(adminReadWrite)

                    .antMatchers(rest_v1_api + "endpoints/**").hasAnyAuthority(adminRead, adminReadWrite, userReadWrite)

                    .antMatchers(rest_v1_api + "faults/**").hasAnyAuthority(adminRead, adminReadWrite, userReadWrite)

                    .antMatchers(rest + "/api/**").hasAuthority(adminReadWrite)

                    .antMatchers(rest_v1_api + "administration/**")
                    .hasAnyAuthority(adminRead, adminReadWrite, userReadWrite)

                    .and().logout().deleteCookies("JSESSIONID");
            http.sessionManagement().maximumSessions(5).sessionRegistry(sessionRegistry());
            http.authorizeRequests().anyRequest().authenticated();
            http.csrf().disable();
        }

        @Bean
        public AuthenticationSuccessHandler myAuthenticationSuccessHandler() {
            return new MyAuthenticationSuccessHandler();
        }

        @Bean
        public SessionRegistry sessionRegistry() {
            return new SessionRegistryImpl();
        }

    }

}

