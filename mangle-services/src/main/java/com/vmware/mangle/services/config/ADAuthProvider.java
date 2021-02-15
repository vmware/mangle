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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.security.ADAuthProviderDto;
import com.vmware.mangle.cassandra.model.security.UsernameDomain;
import com.vmware.mangle.services.ADAuthProviderService;
import com.vmware.mangle.services.PrivilegeService;
import com.vmware.mangle.services.UserLoginAttemptsService;
import com.vmware.mangle.services.UserService;
import com.vmware.mangle.services.hazelcast.HazelcastClusterSyncAware;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Custom Active dirctory authentication provider, maintains multiple AD authenticator details
 *
 * @author chetanc
 */
@Log4j2
@Component
public class ADAuthProvider implements AuthenticationProvider, HazelcastClusterSyncAware {

    private static Map<String, AuthenticationProvider> adAuthProviderMap = new HashMap<>();

    private UserService userService;
    private ADAuthProviderService adAuthProviderService;
    private PrivilegeService privilegeService;
    private UserLoginAttemptsService userLoginAttemptsService;

    @Autowired
    public ADAuthProvider(ADAuthProviderService adAuthProviderService, UserService userService,
            PrivilegeService privilegeService, UserLoginAttemptsService userLoginAttemptsService) {
        this.userService = userService;
        this.adAuthProviderService = adAuthProviderService;
        this.privilegeService = privilegeService;
        this.userLoginAttemptsService = userLoginAttemptsService;
    }

    @PostConstruct
    public void init() {
        List<ADAuthProviderDto> adAuthProviderDtos = adAuthProviderService.getAllADAuthProvider();
        adAuthProviderMap.clear();
        log.debug(String.format("Added %d authentication providers from db", adAuthProviderDtos.size()));
        for (ADAuthProviderDto adAuthProviderDto : adAuthProviderDtos) {
            if (adAuthProviderDto != null) {
                AuthenticationProvider provider = activeDirectoryLdapAuthenticationProvider(
                        adAuthProviderDto.getAdUrl(), adAuthProviderDto.getAdDomain());
                adAuthProviderMap.put(adAuthProviderDto.getAdDomain(), provider);
            }
        }
    }

    /**
     * Provides implementation for the default authenticate method goes through each authentication
     * provider configured, and tries to authenticate against each one of them
     *
     * @param authentication
     * @return
     * @throws AuthenticationException:
     *             when authentication for the given username/password combination failed
     */
    @Override
    public Authentication authenticate(Authentication authentication) {
        Authentication result = null;
        String userAndDomain = authentication.getPrincipal().toString();
        UsernameDomain usernameDomain = extractUserAndDomain(userAndDomain);
        String domain = usernameDomain.getDomain();
        if (domain != null) {
            AuthenticationProvider authenticationProvider = adAuthProviderMap.get(domain);
            if (authenticationProvider != null) {
                result = authenticationProvider.authenticate(authentication);
            }
        }
        return result;
    }

    /**
     * Check if configured authentication providers support given type of authentication
     *
     * @param authentication
     * @return true, if any of the authprovider supports the authentication
     */
    @Override
    public boolean supports(Class<?> authentication) {
        boolean result = false;
        for (Map.Entry<String, AuthenticationProvider> entry : adAuthProviderMap.entrySet()) {
            result = entry.getValue().supports(authentication);
            if (result) {
                break;
            }
        }
        return result;
    }

    /**
     * Adds authentication provider to the providers list, if the given authprovider is reachable
     *
     * @param adURL
     * @param adDomain
     * @return true if test-connection is successful, else false
     */
    public boolean setAdAuthProvider(String adURL, String adDomain) {
        log.info(String.format("Setting up a new authentication provider with adURL %s, and adDomain %s", adURL,
                adDomain));
        AuthenticationProvider authProvider = activeDirectoryLdapAuthenticationProvider(adURL, adDomain);
        if (!((CustomActiveDirectoryLdapAuthenticationProvider) authProvider).testConnection()) {
            return false;
        }
        adAuthProviderMap.put(adDomain, authProvider);
        log.debug(String.format("Added new authenication provider %s, with domain %s", adURL, adDomain));
        return true;
    }

    public boolean testConnection(ADAuthProviderDto adAuthProviderDto) throws MangleException {
        log.info(String.format("Testing connection to authentication provider with adURL %s, and "
                        + "adDomain %s",
                adAuthProviderDto.getAdUrl(),
                adAuthProviderDto.getAdDomain()));
        AuthenticationProvider authProvider = activeDirectoryLdapAuthenticationProvider(adAuthProviderDto.getAdUrl(),
                adAuthProviderDto.getAdDomain());
        return ((CustomActiveDirectoryLdapAuthenticationProvider) authProvider)
                .testConnection(adAuthProviderDto.getAdUser(), adAuthProviderDto.getAdUserPassword());
    }

    /**
     * removes the authentication provider from the list of the configured auth providers
     *
     * @param domainName
     */
    public void removeAdAuthProvider(String domainName) {
        log.debug(String.format("removing authentication provider with the id %s from the providers list", domainName));
        adAuthProviderMap.remove(domainName);
    }

    /**
     * creates an instance of the AD authentication provider
     *
     * @param adUrl
     * @param adDomain
     * @return
     */
    public AuthenticationProvider activeDirectoryLdapAuthenticationProvider(String adUrl, String adDomain) {
        log.debug(String.format("Instantiating new AD provider with AD url %s and AD domain %s", adUrl, adDomain));
        CustomActiveDirectoryLdapAuthenticationProvider provider = new CustomActiveDirectoryLdapAuthenticationProvider(
                userService, userLoginAttemptsService, privilegeService, adDomain, adUrl);
        provider.setConvertSubErrorCodesToExceptions(true);
        provider.setUseAuthenticationRequestCredentials(true);

        return provider;
    }

    /**
     * Decodes the header into a username and password.
     *
     * @throws BadCredentialsException
     *             if the Basic header is not present or is not valid Base64
     */
    private UsernameDomain extractUserAndDomain(String userAndDomain) {
        int delim = userAndDomain.indexOf('@');
        if (delim == -1) {
            throw new BadCredentialsException("Invalid basic authentication token");
        }
        String username = userAndDomain.substring(0, delim);
        String domain = userAndDomain.substring(delim + 1);
        return new UsernameDomain(username, domain);
    }

    public void refreshAdAuthProviderForDomain(String adDomain) {
        log.debug("Refreshing the authProvider for domain: {}", adDomain);
        ADAuthProviderDto adAuthProviderDto = adAuthProviderService.getADAuthProviderByAdDomain(adDomain);
        if (adAuthProviderDto != null) {
            AuthenticationProvider provider = activeDirectoryLdapAuthenticationProvider(adAuthProviderDto.getAdUrl(),
                    adAuthProviderDto.getAdDomain());
            adAuthProviderMap.put(adAuthProviderDto.getAdDomain(), provider);
        } else {
            adAuthProviderMap.remove(adDomain);
        }
    }

    @Override
    public void resync(String objectIdentifier) {
        if (StringUtils.isEmpty(objectIdentifier)) {
            init();
        } else {
            refreshAdAuthProviderForDomain(objectIdentifier);
        }
    }
}
