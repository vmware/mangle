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

package com.vmware.mangle.unittest.services.service.deletionutils;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.CredentialsSpec;
import com.vmware.mangle.model.response.DeleteOperationResponse;
import com.vmware.mangle.services.EndpointService;
import com.vmware.mangle.services.deletionutils.CredentialDeletionService;
import com.vmware.mangle.services.mockdata.CredentialsSpecMockData;
import com.vmware.mangle.services.repository.CredentialRepository;
import com.vmware.mangle.utils.exceptions.MangleException;


/**
 * @author chetanc
 *
 *
 */
public class CredentialDeletionServiceTest {
    @Mock
    private EndpointService endpointService;
    @Mock
    private CredentialRepository credentialRepository;

    private CredentialDeletionService credentialDeletionService;
    private CredentialsSpecMockData credentialsSpecMockData = new CredentialsSpecMockData();
    private CredentialsSpec credentialsSpec;

    @BeforeClass
    public void initCredSpec() {
        this.credentialsSpec = credentialsSpecMockData.getRMCredentialsData();
    }

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        credentialDeletionService = new CredentialDeletionService(endpointService, credentialRepository);
    }

    @Test(description = "Test to verify that deleteCredentialsByNames method successfully deletes the credentials from db")
    public void testDeleteCredentialsByNames() throws MangleException {
        doNothing().when(credentialRepository).deleteByNameIn(any());
        when(endpointService.getEndpointsByCredentialName(anyString())).thenReturn(new ArrayList<>());
        List<String> credentialList = new ArrayList<>();
        credentialList.add(credentialsSpec.getName());
        when(credentialRepository.findByNames(anyList())).thenReturn(Collections.singletonList(credentialsSpec));
        DeleteOperationResponse actualResult = credentialDeletionService.deleteCredentialsByNames(credentialList);
        verify(credentialRepository, times(1)).deleteByNameIn(any());
        Assert.assertEquals(actualResult.getAssociations().size(), 0);

    }

    @Test(description = "Test to verify that an exception is thrown when empty list is sent to delete from the db")
    public void testDeleteCredentialsByNamesEmptyList() throws MangleException {
        doNothing().when(credentialRepository).deleteByNameIn(any());
        List<String> credentialList = new ArrayList<>();
        boolean actualResult = false;
        try {
            credentialDeletionService.deleteCredentialsByNames(credentialList);
        } catch (Exception e) {
            actualResult = true;
        }
        verify(credentialRepository, times(0)).deleteByNameIn(any());
        Assert.assertTrue(actualResult);

    }

}
