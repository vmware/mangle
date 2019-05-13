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

import com.vmware.mangle.cassandra.model.endpoint.CertificatesSpec;
import com.vmware.mangle.model.response.DeleteOperationResponse;
import com.vmware.mangle.services.deletionutils.EndpointCertificatesDeletionService;
import com.vmware.mangle.services.mockdata.CertificatesSpecMockData;
import com.vmware.mangle.services.repository.EndpointCertificatesRepository;
import com.vmware.mangle.services.repository.EndpointRepository;
import com.vmware.mangle.utils.exceptions.MangleException;


/**
 * @author bkaranam
 */
public class CertificatesDeletionServiceTest {
    @Mock
    private EndpointRepository endpointRepository;
    @Mock
    private EndpointCertificatesRepository certificatesRepository;

    private EndpointCertificatesDeletionService certificatesDeletionService;
    private CertificatesSpecMockData certificatesSpecMockData = new CertificatesSpecMockData();
    private CertificatesSpec certificatesSpec;

    @BeforeClass
    public void initCertificatesSpec() {
        this.certificatesSpec = certificatesSpecMockData.getDockerCertificatesData();
    }

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        certificatesDeletionService =
                new EndpointCertificatesDeletionService(endpointRepository, certificatesRepository);
    }

    @Test(description = "Test to verify that deleteCertificatesByNames method successfully deletes the certificates from db")
    public void deleteCertificatesByNames() throws MangleException {
        doNothing().when(certificatesRepository).deleteByNameIn(any());
        List<String> certificatesList = new ArrayList<>();
        certificatesList.add(certificatesSpec.getName());
        when(certificatesRepository.findByNames(anyList())).thenReturn(Collections.singletonList(certificatesSpec));
        DeleteOperationResponse actualResult = certificatesDeletionService.deleteCertificatesByNames(certificatesList);
        verify(certificatesRepository, times(1)).deleteByNameIn(any());
        Assert.assertEquals(actualResult.getAssociations().size(), 0);

    }

    @Test(description = "Test to verify that an exception is thrown when empty list is sent to delete from the db")
    public void testDeleteCertificatesByNamesEmptyList() throws MangleException {
        doNothing().when(certificatesRepository).deleteByNameIn(any());
        List<String> certificatesList = new ArrayList<>();
        boolean actualResult = false;
        try {
            certificatesDeletionService.deleteCertificatesByNames(certificatesList);
        } catch (Exception e) {
            actualResult = true;
        }
        verify(certificatesRepository, times(0)).deleteByNameIn(any());
        Assert.assertTrue(actualResult);

    }

}
