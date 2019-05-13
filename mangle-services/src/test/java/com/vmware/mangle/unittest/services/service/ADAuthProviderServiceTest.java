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
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import lombok.extern.log4j.Log4j2;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.security.ADAuthProviderDto;
import com.vmware.mangle.services.ADAuthProviderService;
import com.vmware.mangle.services.mockdata.AuthProviderMockData;
import com.vmware.mangle.services.repository.ADAuthProviderRepository;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Testing AuthProviderService class
 *
 * @author chetanc
 */
@Log4j2
public class ADAuthProviderServiceTest extends PowerMockTestCase {

    @Mock
    private ADAuthProviderRepository repository;

    @InjectMocks
    private ADAuthProviderService service;

    private AuthProviderMockData data = new AuthProviderMockData();

    @BeforeTest
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }


    /**
     * Test method for {@link ADAuthProviderService#getADAuthProviderByAdDomain(String)}
     */
    @Test
    public void getADAuthProviderByAdDomainTest() {
        log.info("Executing test: getADAuthProviderByIdTest on ADAuthProviderService#getADAuthProviderById");
        ADAuthProviderDto authObj = data.getADAuthProviderDto();
        when(repository.findByAdDomain(anyString())).thenReturn(authObj);

        ADAuthProviderDto persisted = service.getADAuthProviderByAdDomain(authObj.getAdDomain());
        Assert.assertEquals(persisted, authObj);
        verify(repository, atLeastOnce()).findByAdDomain(anyString());
    }

    /**
     * Test method for {@link ADAuthProviderService#getAllADAuthProvider()}
     */
    @Test
    public void getAllADAuthProviderTest() {
        ADAuthProviderDto authObj = data.getADAuthProviderDto();
        ADAuthProviderDto authObj1 = data.getNewADAuthProviderDto();

        List<ADAuthProviderDto> list = new ArrayList<>();
        list.add(authObj);
        list.add(authObj1);

        log.info("Executing test: getAllADAuthProviderTest on ADAuthProviderService#getAllADAuthProvider");

        when(repository.findAll()).thenReturn(list);

        List<ADAuthProviderDto> persisted = service.getAllADAuthProvider();
        Assert.assertEquals(persisted.size(), list.size());
        verify(repository, atLeastOnce()).findAll();
    }

    /**
     * Test method for {@link ADAuthProviderService#addADAuthProvider(ADAuthProviderDto)}
     */
    @Test
    public void addADAuthProviderTest() {
        ADAuthProviderDto authObj = data.getADAuthProviderDto();
        log.info("Executing test: getAllADAuthProviderTest on ADAuthProviderService#getAllADAuthProvider");

        when(repository.save(any())).thenReturn(authObj);

        ADAuthProviderDto persisted = service.addADAuthProvider(authObj);
        Assert.assertEquals(persisted, authObj);
        verify(repository, atLeastOnce()).save(any());
    }

    /**
     * Test method for {@link ADAuthProviderService#removeADAuthProvider(List)}
     */
    @Test
    public void removeADAuthProviderTest() throws MangleException {
        ADAuthProviderDto authObj = data.getADAuthProviderDto();
        log.info("Executing test: getAllADAuthProviderTest on ADAuthProviderService#removeADAuthProvider");

        List<String> ids = new ArrayList<String>();
        ids.add(authObj.getId());

        when(repository.findByAdDomains(Arrays.asList(authObj.getId()))).thenReturn(Arrays.asList(authObj));
        doNothing().when(repository).deleteByAdDomain(any());

        service.removeADAuthProvider(ids);
        verify(repository, atLeastOnce()).deleteByAdDomainIn(anyList());
    }

    /**
     * Test method for {@link ADAuthProviderService#doesADAuthExists(ADAuthProviderDto)}
     */
    @Test
    public void doesADAuthExistsNoDuplicateTest() {
        ADAuthProviderDto authObj = data.getADAuthProviderDto();
        log.info("Executing test: getAllADAuthProviderTest on ADAuthProviderService#doesADAuthExists; no duplicate");

        when(repository.findByAdDomain(anyString())).thenReturn(null);

        boolean isDuplicate = service.doesADAuthExists(authObj);
        Assert.assertFalse(isDuplicate);
        verify(repository, atLeastOnce()).findByAdDomain(anyString());
    }

    /**
     * Test method for {@link ADAuthProviderService#doesADAuthExists(ADAuthProviderDto)}
     */
    @Test
    public void doesADAuthExistsDuplicateTest() {
        ADAuthProviderDto authObj = data.getADAuthProviderDto();
        ADAuthProviderDto anotherAuth = data.getNewADAuthProviderDto();
        log.info(
                "Executing test: doesADAuthExistsDuplicateTest on ADAuthProviderService#doesADAuthExists; duplicate exists");

        when(repository.findByAdDomain(anyString())).thenReturn(anotherAuth);

        boolean isDuplicate = service.doesADAuthExists(authObj);
        Assert.assertTrue(isDuplicate);
        verify(repository, atLeastOnce()).findByAdDomain(anyString());
    }

    /**
     * Test method for {@link ADAuthProviderService#updateADAuthProvider(ADAuthProviderDto)}
     */
    @Test
    public void updateADAuthProviderTest() {
        log.info("Executing test: updateADAuthProviderTest on ADAuthProviderService#updateADAuthProvider;");
        ADAuthProviderDto authObj = data.getADAuthProviderDto();
        ADAuthProviderDto anotherAuth = data.getNewADAuthProviderDto();
        when(repository.findByAdDomain(anyString())).thenReturn(authObj);
        when(repository.save(any())).thenReturn(anotherAuth);

        ADAuthProviderDto persisted = service.updateADAuthProvider(anotherAuth);
        Assert.assertEquals(persisted, anotherAuth);
        verify(repository, atLeastOnce()).save(any());
    }

    /**
     * Test method for {@link ADAuthProviderService#getAllDomains()}
     */
    @Test
    public void getAllDomainsTest() {
        ADAuthProviderDto authObj = data.getADAuthProviderDto();

        List<ADAuthProviderDto> list = new ArrayList<>();
        list.add(authObj);

        log.info("Executing test: getAllADAuthProviderTest on ADAuthProviderService#getAllADAuthProvider");

        when(repository.findAll()).thenReturn(list);

        Set<String> persisted = service.getAllDomains();
        Assert.assertEquals(persisted.size(), 1);
        verify(repository, atLeastOnce()).findAll();
    }
}
