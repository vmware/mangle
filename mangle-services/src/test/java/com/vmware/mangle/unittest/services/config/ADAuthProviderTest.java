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

package com.vmware.mangle.unittest.services.config;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.security.ADAuthProviderDto;
import com.vmware.mangle.services.ADAuthProviderService;
import com.vmware.mangle.services.UserService;
import com.vmware.mangle.services.config.ADAuthProvider;
import com.vmware.mangle.services.config.CustomActiveDirectoryLdapAuthenticationProvider;
import com.vmware.mangle.services.mockdata.AuthProviderMockData;

/**
 * TestNG class to test configuration class ADAuthProvider
 *
 * @author chetanc
 */

@Log4j2
public class ADAuthProviderTest extends PowerMockTestCase {
    private ADAuthProvider adAuthProvider;
    private UserService userService;
    private ADAuthProviderService adAuthProviderService;
    private CustomActiveDirectoryLdapAuthenticationProvider provider;

    private AuthProviderMockData dataProvider = new AuthProviderMockData();

    @BeforeClass
    public void initAuthProvider() throws Exception {
        provider = mock(CustomActiveDirectoryLdapAuthenticationProvider.class);
        adAuthProviderService = mock(ADAuthProviderService.class);
        userService = mock(UserService.class);
        log.info("initializing ADAuthProvider instance");
        adAuthProvider = spy(new ADAuthProvider(adAuthProviderService, userService));
    }

    /**
     * Test method for {@link ADAuthProvider#init()}
     */
    @Test(priority = 1)
    public void initTest() {
        log.info("Executing initTest on method: ADAuthProvider#init()");
        ADAuthProviderDto adAuthProviderDto = dataProvider.getADAuthProviderDto();
        List<ADAuthProviderDto> list = new ArrayList<>();
        list.add(adAuthProviderDto);
        when(adAuthProviderService.getAllADAuthProvider()).thenReturn(list);
        doReturn(provider).when(adAuthProvider).activeDirectoryLdapAuthenticationProvider(anyString(), anyString());
        when(provider.supports(UsernamePasswordAuthenticationToken.class)).thenReturn(true);
        adAuthProvider.init();
        boolean result = adAuthProvider.supports(UsernamePasswordAuthenticationToken.class);
        Assert.assertTrue(result);
    }

    /**
     * Test method for {@link ADAuthProvider#authenticate(Authentication)}
     */
    @Test(priority = 2)
    public void authenticateTest() {
        log.info("Executing authenticateTest on method: ADAuthProvider#authenticate(Authentication)");
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("admin@test.local", "");
        when(provider.authenticate(any())).thenReturn(token);
        Authentication result = adAuthProvider.authenticate(token);

        Assert.assertTrue(result instanceof UsernamePasswordAuthenticationToken);
    }

    /**
     * Test method for {@link ADAuthProvider#supports(Class)}
     */
    @Test(priority = 3)
    public void supportTestTrue() {
        log.info("Executing supportTestTrue on method: ADAuthProvider#supports(Class)");
        PowerMockito.when(provider.supports(any())).thenReturn(true);
        boolean result = adAuthProvider.supports(UsernamePasswordAuthenticationToken.class);

        Assert.assertTrue(result);
    }

    /**
     * Test method for {@link ADAuthProvider#supports(Class)}
     */
    @Test(priority = 4)
    public void supportTestFalse() {
        log.info("Executing supportTestFalse on method: ADAuthProvider#supports(Class)");
        when(provider.supports(any())).thenReturn(false);
        boolean result = adAuthProvider.supports(UsernamePasswordAuthenticationToken.class);

        Assert.assertFalse(result);
    }

    /**
     * Test method for {@link ADAuthProvider#setAdAuthProvider(String, String)}
     */
    @Test(priority = 5)
    public void setAdAuthProviderTestFailure() throws Exception {
        log.info(
                "Executing setAdAuthProviderTestFailure on method: ADAuthProvider#setAdAuthProvider(String, String, String)");
        when(provider.testConnection()).thenReturn(false);
        PowerMockito.whenNew(CustomActiveDirectoryLdapAuthenticationProvider.class)
                .withArguments(any(), anyString(), anyString()).thenReturn(provider);
        ADAuthProviderDto auth = dataProvider.getNewADAuthProviderDto();
        boolean result = adAuthProvider.setAdAuthProvider(auth.getAdUrl(), auth.getAdDomain());
        Assert.assertFalse(result);
    }

    /**
     * Test method for {@link ADAuthProvider#setAdAuthProvider(String, String)}
     */
    @Test(priority = 6)
    public void setAdAuthProviderTestSuccessful() throws Exception {
        log.info(
                "Executing setAdAuthProviderTestSuccessful on method: ADAuthProvider#setAdAuthProvider(String, String, String)");
        when(provider.testConnection()).thenReturn(true);
        PowerMockito.whenNew(CustomActiveDirectoryLdapAuthenticationProvider.class)
                .withArguments(any(), anyString(), anyString()).thenReturn(provider);
        ADAuthProviderDto auth = dataProvider.getNewADAuthProviderDto();
        boolean result = adAuthProvider.setAdAuthProvider(auth.getAdUrl(), auth.getAdDomain());
        log.info("result from the method setAdAuthProviderTestSuccessful is: " + result);
        Assert.assertTrue(result);
    }

    /**
     * Test method for {@link ADAuthProvider#removeAdAuthProvider(String)}
     */
    @Test(priority = 7)
    public void removeAdAuthProviderTest() {
        log.info("Executing removeAdAuthProviderTest on method: ADAuthProvider#removeAdAuthProvider(String)");
        ADAuthProviderDto auth = dataProvider.getNewADAuthProviderDto();
        adAuthProvider.removeAdAuthProvider(auth.getId());
    }
}
