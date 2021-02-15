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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.security.ADAuthProviderDto;
import com.vmware.mangle.services.ADAuthProviderService;
import com.vmware.mangle.services.PrivilegeService;
import com.vmware.mangle.services.UserLoginAttemptsService;
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
@PrepareForTest(value = ADAuthProvider.class)
@PowerMockIgnore(value = {"com.sun.org.apache.xerces.*", "javax.xml.parsers.*", "javax.xml.parsers"
        + ".DocumentBuilder.*", "org.apache.logging.log4j.*" })
public class ADAuthProviderTest extends PowerMockTestCase {
    private ADAuthProvider adAuthProvider;
    private UserService userService;
    private PrivilegeService privilegeService;
    private ADAuthProviderService adAuthProviderService;
    private CustomActiveDirectoryLdapAuthenticationProvider provider;

    @Mock
    private UserLoginAttemptsService userLoginAttemptsService;

    private AuthProviderMockData dataProvider = new AuthProviderMockData();

    @BeforeMethod
    public void initMocks() {
        PowerMockito.mockStatic(ADAuthProvider.class);
    }

    @BeforeClass
    public void initAuthProvider() throws Exception {
        provider = mock(CustomActiveDirectoryLdapAuthenticationProvider.class);
        adAuthProviderService = mock(ADAuthProviderService.class);
        userService = mock(UserService.class);
        log.info("initializing ADAuthProvider instance");
        adAuthProvider =
                spy(new ADAuthProvider(adAuthProviderService, userService, privilegeService, userLoginAttemptsService));
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
        Assert.assertTrue(result,
                "Testcase for ADAuthProvider.init() failed when the provider is UsernamePasswordAuthenticationToken");
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

        Assert.assertTrue(result instanceof UsernamePasswordAuthenticationToken,
                "Testcase for ADAuthProvider.authenticate(Authentication) failed when the expected provider is UsernamePasswordAuthenticationToken");
    }

    /**
     * Test method for {@link ADAuthProvider#authenticate(Authentication) for BadCredentials}
     */
    @Test(priority = 3)
    public void authenticateTestForBadCredentials() {
        log.info("Executing authenticateTest on method: ADAuthProvider#authenticate(Authentication)");
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("admintest.local", "");
        when(provider.authenticate(any())).thenReturn(token);
        boolean actualResult = false;
        try {
            Authentication result = adAuthProvider.authenticate(token);
        } catch (Exception e) {
            actualResult = true;
        }
        Assert.assertTrue(actualResult,
                "Testcase for ADAuthProvider.authenticate(Authentication) failed when BadCredentials Exception expected like extraction of user and domain from username");
    }

    /**
     * Test method for {@link ADAuthProvider#supports(Class)}
     */
    @Test(priority = 4)
    public void supportTestTrue() {
        log.info("Executing supportTestTrue on method: ADAuthProvider#supports(Class)");
        PowerMockito.when(provider.supports(any())).thenReturn(true);
        boolean result = adAuthProvider.supports(UsernamePasswordAuthenticationToken.class);

        Assert.assertTrue(result,
                "Testcase for ADAuthProvider.supports(Class) failed when the expected provider is UsernamePasswordAuthenticationToken");
    }

    /**
     * Test method for {@link ADAuthProvider#supports(Class)}
     */
    @Test(priority = 5)
    public void supportTestFalse() {
        log.info("Executing supportTestFalse on method: ADAuthProvider#supports(Class)");
        when(provider.supports(any())).thenReturn(false);
        boolean result = adAuthProvider.supports(UsernamePasswordAuthenticationToken.class);

        Assert.assertFalse(result,
                "Testcase for ADAuthProvider.supports(Class) failed when it returns the UsernamePasswordAuthenticationToken");
    }

    /**
     * Test method for {@link ADAuthProvider#setAdAuthProvider(String, String)}
     */
    @Test(priority = 6)
    public void setAdAuthProviderTestFailure() throws Exception {
        log.info(
                "Executing setAdAuthProviderTestFailure on method: ADAuthProvider#setAdAuthProvider(String, String, String)");
        when(provider.testConnection()).thenReturn(false);
        PowerMockito.whenNew(CustomActiveDirectoryLdapAuthenticationProvider.class)
                .withArguments(any(), any(), any(), anyString(), anyString()).thenReturn(provider);
        ADAuthProviderDto auth = dataProvider.getNewADAuthProviderDto();
        boolean result = adAuthProvider.setAdAuthProvider(auth.getAdUrl(), auth.getAdDomain());
        Assert.assertFalse(result,
                "Testcase for ADAuthProvider#setAdAuthProvider(String, String, String) failed when the testconnection returns true");
    }

    /**
     * Test method for {@link ADAuthProvider#setAdAuthProvider(String, String)}
     */
    @Test(priority = 7)
    public void setAdAuthProviderTestSuccessful() throws Exception {
        log.info(
                "Executing setAdAuthProviderTestSuccessful on method: ADAuthProvider#setAdAuthProvider(String, String, String)");
        when(provider.testConnection()).thenReturn(true);
        PowerMockito.whenNew(CustomActiveDirectoryLdapAuthenticationProvider.class)
                .withArguments(any(), any(), any(), anyString(), anyString()).thenReturn(provider);
        ADAuthProviderDto auth = dataProvider.getNewADAuthProviderDto();
        boolean result = adAuthProvider.setAdAuthProvider(auth.getAdUrl(), auth.getAdDomain());
        log.info("result from the method setAdAuthProviderTestSuccessful is: " + result);
        Assert.assertTrue(result,
                "Testcase for ADAuthProvider#setAdAuthProvider(String, String, String) failed when the testconnection returns false");
    }

    /**
     * Test method for {@link ADAuthProvider#removeAdAuthProvider(String)}
     */
    @Test(priority = 8)
    public void removeAdAuthProviderTest() {
        log.info("Executing removeAdAuthProviderTest on method: ADAuthProvider#removeAdAuthProvider(String)");
        ADAuthProviderDto auth = dataProvider.getNewADAuthProviderDto();
        adAuthProvider.removeAdAuthProvider(auth.getId());
    }

    @Test(priority = 9)
    public void testResyncWithEmptyIdentifier() {
        doNothing().when(adAuthProvider).init();
        adAuthProvider.resync("");
        verify(adAuthProvider, times(2)).init();
    }

    @Test(priority = 10)
    public void testResyncWithDomain() {
        ADAuthProviderDto auth = dataProvider.getNewADAuthProviderDto();
        doNothing().when(adAuthProvider).refreshAdAuthProviderForDomain(auth.getAdDomain());
        adAuthProvider.resync(auth.getAdDomain());
        verify(adAuthProvider, times(1)).refreshAdAuthProviderForDomain(auth.getAdDomain());
    }

    /**
     * Test method for {@link ADAuthProvider#refreshAdAuthProviderForDomain(String)}
     */
    @Test(priority = 11)
    public void testRefreshAdAuthProviderForDomainWithInvalidDomain() {
        ADAuthProviderDto auth = dataProvider.getNewADAuthProviderDto();
        adAuthProvider.refreshAdAuthProviderForDomain("");
        verify(adAuthProvider, times(0)).activeDirectoryLdapAuthenticationProvider("", "");
    }

    @Test(priority = 12)
    public void testTestConnection() throws Exception {
        ADAuthProviderDto adAuthProviderDto = dataProvider.getDummyAuthProvider();

        PowerMockito.whenNew(CustomActiveDirectoryLdapAuthenticationProvider.class).withAnyArguments().thenReturn(provider);
        when(provider.testConnection(anyString(), anyString())).thenReturn(true);

        boolean result = adAuthProvider.testConnection(adAuthProviderDto);

        Assert.assertTrue(result);
        verify(provider, times(1)).testConnection(anyString(), anyString());
    }

    @Test(priority = 13)
    public void testRefreshAdAuthProviderForDomain() throws Exception {
        ADAuthProviderDto adAuthProviderDto = dataProvider.getDummyAuthProvider();

        when(adAuthProviderService.getADAuthProviderByAdDomain(anyString())).thenReturn(adAuthProviderDto);
        PowerMockito.whenNew(CustomActiveDirectoryLdapAuthenticationProvider.class).withAnyArguments().thenReturn(provider);

        adAuthProvider.refreshAdAuthProviderForDomain(adAuthProviderDto.getAdDomain());

        verify(adAuthProviderService, times(2)).getADAuthProviderByAdDomain(anyString());
    }

}
