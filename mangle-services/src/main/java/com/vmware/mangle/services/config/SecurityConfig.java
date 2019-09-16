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
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.vmware.mangle.cassandra.model.security.Privilege;
import com.vmware.mangle.cassandra.model.security.Role;
import com.vmware.mangle.model.enums.DefaultPrivileges;
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
    CustomAuthenticationProvider customAuthenticationProvider;

    @Autowired
    public SecurityConfig(UserService userService, PrivilegeService privilegeService, ADAuthProvider adAuthProvider,
            CustomAuthenticationProvider customAuthenticationProvider) {
        this.userService = userService;
        this.privilegeService = privilegeService;
        this.adAuthProvider = adAuthProvider;
        this.customAuthenticationProvider = customAuthenticationProvider;
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
        auth.authenticationProvider(customAuthenticationProvider);
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

        private static final String REST = "/rest";
        private static final String V1_API = "/api/v1/";
        private static final String REST_V1_API = REST + V1_API;
        private final String adminReadWrite = DefaultPrivileges.ADMIN_READ_WRITE.name();
        private final String adminRead = DefaultPrivileges.ADMIN_READ.name();
        private final String userReadWrite = DefaultPrivileges.USER_READ_WRITE.name();
        private final String readOnly = DefaultPrivileges.READONLY.name();
        @Autowired
        private MangleBasicAuthenticationEntryPoint authenticationEntryPoint;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.httpBasic().authenticationEntryPoint(authenticationEntryPoint).and().authorizeRequests()

                    .antMatchers("/", "/*.html", "/*.js", "/*.map", "/assets/**", "/*.ico").permitAll()
                    .antMatchers("/webjars/**", "/swagger-resources/**", "/swagger", "/swagger-ui.html",
                            "/swagger-resources", "/csrf")
                    .permitAll().antMatchers("/application/health").permitAll()
                    .antMatchers(REST_V1_API + "auth-provider-management/domains").permitAll()
                    .antMatchers(HttpMethod.GET, REST_V1_API + "user-management/users/admin").permitAll()

                    .antMatchers(HttpMethod.GET, REST_V1_API + "tasks")
                    .hasAnyAuthority(adminRead, adminReadWrite, readOnly, userReadWrite)

                    .antMatchers(HttpMethod.GET, REST_V1_API + "tasks/**")
                    .hasAnyAuthority(adminRead, adminReadWrite, readOnly, userReadWrite)

                    .antMatchers(HttpMethod.GET, REST_V1_API + "scheduler")
                    .hasAnyAuthority(adminRead, adminReadWrite, readOnly, userReadWrite)

                    .antMatchers(HttpMethod.GET, REST_V1_API + "endpoints")
                    .hasAnyAuthority(adminRead, adminReadWrite, readOnly, userReadWrite)

                    .antMatchers(HttpMethod.GET, REST_V1_API + "endpoints/**")
                    .hasAnyAuthority(adminRead, adminReadWrite, readOnly, userReadWrite)

                    .antMatchers(HttpMethod.GET, REST_V1_API + "plugins")
                    .hasAnyAuthority(adminRead, adminReadWrite, readOnly, userReadWrite)

                    .antMatchers(HttpMethod.GET, REST_V1_API + "plugins/**")
                    .hasAnyAuthority(adminRead, adminReadWrite, readOnly, userReadWrite)

                    .antMatchers(HttpMethod.GET, REST_V1_API + "role-management/roles")
                    .hasAnyAuthority(adminRead, adminReadWrite, readOnly, userReadWrite)

                    .antMatchers(HttpMethod.GET, REST_V1_API + "role-management/privileges")
                    .hasAnyAuthority(adminRead, adminReadWrite)

                    .antMatchers(REST_V1_API + "role-management/**").hasAuthority(adminReadWrite)

                    .antMatchers(REST_V1_API + "user-management/user")
                    .hasAnyAuthority(adminRead, adminReadWrite, readOnly)

                    .antMatchers(REST_V1_API + "user-management/password")
                    .hasAnyAuthority(adminRead, adminReadWrite, userReadWrite, readOnly)

                    .antMatchers(HttpMethod.GET, REST_V1_API + "user-management/**")
                    .hasAnyAuthority(adminRead, adminReadWrite)

                    .antMatchers(REST_V1_API + "user-management/**").hasAuthority(adminReadWrite)

                    .antMatchers(HttpMethod.GET, REST_V1_API + "auth-provider-management/**")
                    .hasAnyAuthority(adminRead, adminReadWrite)

                    .antMatchers(REST_V1_API + "auth-provider-management/**").hasAuthority(adminReadWrite)

                    .antMatchers(HttpMethod.GET, REST_V1_API + "scheduler/**")
                    .hasAnyAuthority(adminRead, adminReadWrite)

                    .antMatchers(REST_V1_API + "scheduler/**").hasAuthority(adminReadWrite)

                    .antMatchers(REST_V1_API + "cluster-config").hasAnyAuthority(adminReadWrite)

                    .antMatchers(REST_V1_API + "endpoints/**").hasAnyAuthority(adminRead, adminReadWrite, userReadWrite)

                    .antMatchers(REST_V1_API + "faults/**").hasAnyAuthority(adminRead, adminReadWrite, userReadWrite)

                    .antMatchers(REST + "/api/**").hasAuthority(adminReadWrite)

                    .antMatchers(REST_V1_API + "administration/**")
                    .hasAnyAuthority(adminRead, adminReadWrite, userReadWrite)

                    .antMatchers("/application/logfile").hasAuthority(adminReadWrite)

                    .anyRequest().authenticated().and().logout().deleteCookies("JSESSIONID");

            http.sessionManagement().maximumSessions(5).sessionRegistry(sessionRegistry());
            http.csrf().disable();
        }

        @Bean
        public SessionRegistry sessionRegistry() {
            return new SessionRegistryImpl();
        }

    }

}

