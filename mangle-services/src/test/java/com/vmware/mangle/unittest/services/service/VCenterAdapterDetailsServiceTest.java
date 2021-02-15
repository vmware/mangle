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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.stubbing.answers.ReturnsArgumentAt;
import org.springframework.util.CollectionUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.VCenterAdapterDetails;
import com.vmware.mangle.cassandra.model.endpoint.VCenterAdapterProperties;
import com.vmware.mangle.services.VCenterAdapterDetailsService;
import com.vmware.mangle.services.mockdata.VCenterAdapterDetailsMockData;
import com.vmware.mangle.services.repository.VCenterAdapterDetailsRepository;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;
import com.vmware.mangle.utils.helpers.security.DecryptFields;

/**
 * @author chetanc
 */
public class VCenterAdapterDetailsServiceTest {

    @Mock
    private VCenterAdapterDetailsRepository repository;

    private VCenterAdapterDetailsService vcaAdapterService;

    private VCenterAdapterDetailsMockData mockData = new VCenterAdapterDetailsMockData();

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        vcaAdapterService = new VCenterAdapterDetailsService(repository);
    }

    @Test
    public void testGetAllAdapterDetails() {
        List<VCenterAdapterDetails> detailsListOrig = mockData.getVcaAdaptersList();
        when(repository.findAll()).thenReturn(detailsListOrig);
        List<VCenterAdapterDetails> detailsList = vcaAdapterService.getAllVCenterAdapterDetails();

        Assert.assertFalse(CollectionUtils.isEmpty(detailsList));
        Assert.assertEquals(detailsList.size(), detailsListOrig.size(),
                "The data returned form the service" + " is not same as the one returned from the repository");
        verify(repository, times(1)).findAll();
    }

    @Test
    public void testGetVCAdapterDetailsByName() {
        VCenterAdapterDetails vCenterAdapterDetails = mockData.getVcaAdapterMockData();
        Optional<VCenterAdapterDetails> optional = Optional.of(vCenterAdapterDetails);
        when(repository.findByName(vCenterAdapterDetails.getName())).thenReturn(optional);

        VCenterAdapterDetails details = vcaAdapterService.getVCAdapterDetailsByName(vCenterAdapterDetails.getName());

        Assert.assertEquals(details, vCenterAdapterDetails);
        verify(repository, times(1)).findByName(vCenterAdapterDetails.getName());
    }

    @Test
    public void testGetVCAdapterDetailsByNameNullInput() {
        VCenterAdapterDetails details = vcaAdapterService.getVCAdapterDetailsByName("");

        Assert.assertNull(details);
        verify(repository, times(0)).findByName(anyString());
    }

    @Test
    public void testFindVCAdapterDetailsByVCAdapterProperties() {
        VCenterAdapterProperties vCenterAdapterProperties = mockData.getVCenterAdapterProperties();
        List<VCenterAdapterDetails> detailsList = mockData.getVcaAdaptersList();
        when(repository.findByAdapterUrl(vCenterAdapterProperties.getVcAdapterUrl())).thenReturn(detailsList);

        VCenterAdapterDetails detailsPersisted =
                vcaAdapterService.findVCAdapterDetailsByVCAdapterProperties(vCenterAdapterProperties);

        Assert.assertNotNull(detailsPersisted);
        Assert.assertEquals(detailsPersisted, detailsList.get(0));
        verify(repository, times(1)).findByAdapterUrl(vCenterAdapterProperties.getVcAdapterUrl());
    }

    @Test
    public void testFindVCAdapterDetailsByVCAdapterPropertiesNoMatchingUser() {
        VCenterAdapterProperties vCenterAdapterProperties = mockData.getVCenterAdapterProperties();
        List<VCenterAdapterDetails> detailsList = mockData.getVcaAdaptersList();
        detailsList.get(0).setUsername(UUID.randomUUID().toString());
        when(repository.findByAdapterUrl(vCenterAdapterProperties.getVcAdapterUrl())).thenReturn(detailsList);

        VCenterAdapterDetails detailsPersisted =
                vcaAdapterService.findVCAdapterDetailsByVCAdapterProperties(vCenterAdapterProperties);

        Assert.assertNull(detailsPersisted);
        verify(repository, times(1)).findByAdapterUrl(vCenterAdapterProperties.getVcAdapterUrl());
    }

    @Test
    public void testFindVCAdapterDetailsByVCAdapterPropertiesNoMatchingPassword() {
        VCenterAdapterProperties vCenterAdapterProperties = mockData.getVCenterAdapterProperties();
        List<VCenterAdapterDetails> detailsList = mockData.getVcaAdaptersList();
        detailsList.get(0).setPassword(UUID.randomUUID().toString());
        when(repository.findByAdapterUrl(vCenterAdapterProperties.getVcAdapterUrl())).thenReturn(detailsList);

        VCenterAdapterDetails detailsPersisted =
                vcaAdapterService.findVCAdapterDetailsByVCAdapterProperties(vCenterAdapterProperties);

        Assert.assertNull(detailsPersisted);
        verify(repository, times(1)).findByAdapterUrl(vCenterAdapterProperties.getVcAdapterUrl());
    }

    @Test
    public void testCreateVCADetailsFromVCAProperties() {
        VCenterAdapterProperties vCenterAdapterProperties = mockData.getVCenterAdapterProperties();
        when(repository.save(any())).then(new ReturnsArgumentAt(0));

        VCenterAdapterDetails details = vcaAdapterService.createVCADetailsFromVCAProperties(vCenterAdapterProperties);

        Assert.assertEquals(details.getPassword(), vCenterAdapterProperties.getPassword());
        Assert.assertEquals(details.getUsername(), vCenterAdapterProperties.getUsername());
        Assert.assertEquals(details.getAdapterUrl(), vCenterAdapterProperties.getVcAdapterUrl());
        verify(repository, times(1)).save(any());
    }

    @Test
    public void testUpdateVCenterAdapterDetails() throws MangleException {
        VCenterAdapterDetails vCenterAdapterDetails = mockData.getVcaAdapterMockData();
        VCenterAdapterDetails vCenterAdapterDetailsNew = mockData.getNewVcaAdapterMockData();
        vCenterAdapterDetails.setName(vCenterAdapterDetailsNew.getName());
        Optional<VCenterAdapterDetails> optional = Optional.of(vCenterAdapterDetails);

        when(repository.findByName(vCenterAdapterDetails.getName())).thenReturn(optional);
        when(repository.save(any())).then(new ReturnsArgumentAt(0));

        VCenterAdapterDetails persistedVCADetails =
                vcaAdapterService.updateVCenterAdapterDetails(vCenterAdapterDetailsNew);

        Assert.assertEquals(persistedVCADetails.getAdapterUrl(), vCenterAdapterDetailsNew.getAdapterUrl());
        Assert.assertEquals(persistedVCADetails.getUsername(), vCenterAdapterDetailsNew.getUsername());

        verify(repository, times(1)).findByName(vCenterAdapterDetails.getName());
        verify(repository, times(1)).save(any());

    }

    @Test(expectedExceptions = MangleException.class)
    public void testUpdateVCenterAdapterDetailsNoEntry() throws MangleException {
        VCenterAdapterDetails vCenterAdapterDetailsNew = mockData.getNewVcaAdapterMockData();

        when(repository.findByName(anyString())).thenReturn(Optional.empty());

        try {
            vcaAdapterService.updateVCenterAdapterDetails(vCenterAdapterDetailsNew);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
            verify(repository, times(1)).findByName(vCenterAdapterDetailsNew.getName());
            verify(repository, times(0)).save(any());

            throw e;
        }
    }


    @Test
    public void testCreateVCenterAdapterDetails() throws MangleException {
        VCenterAdapterDetails vCenterAdapterDetailsNew = mockData.getNewVcaAdapterMockData();

        when(repository.findByName(vCenterAdapterDetailsNew.getName())).thenReturn(Optional.empty());
        when(repository.save(any())).then(new ReturnsArgumentAt(0));

        VCenterAdapterDetails persistedVCADetails = (VCenterAdapterDetails) DecryptFields
                .decrypt(vcaAdapterService.createVCenterAdapterDetails(vCenterAdapterDetailsNew));

        Assert.assertEquals(persistedVCADetails.getAdapterUrl(), vCenterAdapterDetailsNew.getAdapterUrl());
        Assert.assertEquals(persistedVCADetails.getUsername(), vCenterAdapterDetailsNew.getUsername());
        Assert.assertEquals(persistedVCADetails.getPassword(), vCenterAdapterDetailsNew.getPassword());

        verify(repository, times(1)).findByName(vCenterAdapterDetailsNew.getName());
        verify(repository, times(1)).save(any());

    }

    @Test(expectedExceptions = MangleException.class)
    public void testCreateVCenterAdapterDetailsAlreadyExists() throws MangleException {
        VCenterAdapterDetails vCenterAdapterDetailsNew = mockData.getNewVcaAdapterMockData();

        when(repository.findByName(vCenterAdapterDetailsNew.getName()))
                .thenReturn(Optional.of(vCenterAdapterDetailsNew));
        when(repository.save(any())).then(new ReturnsArgumentAt(0));

        try {
            vcaAdapterService.createVCenterAdapterDetails(vCenterAdapterDetailsNew);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.DUPLICATE_RECORD);
            verify(repository, times(1)).findByName(vCenterAdapterDetailsNew.getName());
            verify(repository, times(0)).save(any());

            throw e;
        }
    }

    @Test
    public void testDeleteVCenterAdapterDetails() throws MangleException {
        List<VCenterAdapterDetails> detailsList = mockData.getVcaAdaptersList();
        List<String> detailsNameList =
                detailsList.stream().map(VCenterAdapterDetails::getName).collect(Collectors.toList());

        when(repository.findByNameIn(any())).thenReturn(detailsList);
        doNothing().when(repository).deleteByNameIn(any());

        vcaAdapterService.deleteVCenterAdapterDetails(detailsNameList);

        verify(repository, times(1)).deleteByNameIn(any());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testDeleteVCenterAdapterDetailsNoEntry() throws MangleException {
        List<VCenterAdapterDetails> detailsList = mockData.getVcaAdaptersList();
        List<String> detailsNameList =
                detailsList.stream().map(VCenterAdapterDetails::getName).collect(Collectors.toList());
        detailsNameList.add(UUID.randomUUID().toString());

        when(repository.findByNameIn(any())).thenReturn(detailsList);
        doNothing().when(repository).deleteByNameIn(any());

        try {
            vcaAdapterService.deleteVCenterAdapterDetails(detailsNameList);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
            verify(repository, times(0)).deleteByNameIn(any());

            throw e;
        }
    }


}
