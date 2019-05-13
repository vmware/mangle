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
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.datastax.driver.core.PagingState;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.CertificatesSpec;
import com.vmware.mangle.cassandra.model.endpoint.DockerCertificates;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.EndpointCertificatesService;
import com.vmware.mangle.services.mockdata.CertificatesSpecMockData;
import com.vmware.mangle.services.repository.EndpointCertificatesRepository;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Unit Test case for EndpointCdertificatesService.
 *
 * @author bkaranam
 */
public class EndpointCertificatesServiceTest extends PowerMockTestCase {


    private EndpointCertificatesService certificatesService;
    @Mock
    private EndpointCertificatesRepository certificatesRepository;

    private CertificatesSpecMockData certificatesSpecMockData = new CertificatesSpecMockData();
    private CertificatesSpec certificatesSpec;

    @BeforeMethod
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        certificatesService = new EndpointCertificatesService(certificatesRepository);
        this.certificatesSpec = certificatesSpecMockData.getDockerCertificatesData();
    }

    @AfterClass
    public void tearDownAfterClass() {
        this.certificatesSpec = null;
        this.certificatesSpecMockData = null;
    }

    @AfterTest
    public void tearDownAfterTest() {
        validateMockitoUsage();
    }

    /**
     * Test method for {@link EndpointCertificatesService#getAllCertificates()}.
     */
    @Test(description = "Test to verify the retrieving of the all certificates from the db")
    public void testGetAllCertificates() {
        CertificatesSpec certificatesSpec1 = certificatesSpecMockData.getDockerCertificatesData();
        List<CertificatesSpec> certificatesList = new ArrayList<>();
        certificatesList.add(certificatesSpec);
        certificatesList.add(certificatesSpec1);
        when(certificatesRepository.findAll()).thenReturn(certificatesList);
        List<CertificatesSpec> actualCertificatesSpecList = certificatesService.getAllCerficates();
        verify(certificatesRepository, times(1)).findAll();
        int expectedCount = 2;
        Assert.assertEquals(actualCertificatesSpecList.size(), expectedCount);
    }

    /**
     * Test method for {@link EndpointCertificatesService#getCertificatesByName(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test(description = "Test to verify the retrieving of a particular certificates by name from the db")
    public void testGetCertificatesByName() throws MangleException {
        Optional<CertificatesSpec> optional = Optional.of(certificatesSpec);
        when(certificatesRepository.findByName(anyString())).thenReturn(optional);
        CertificatesSpec actualResult = certificatesService.getCertificatesByName(certificatesSpec.getName());
        verify(certificatesRepository, times(1)).findByName(anyString());
        Assert.assertEquals(actualResult, certificatesSpec);
    }

    /**
     * Test method for {@link EndpointCertificatesService#getCertificatesByName(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test(description = "Test to verify that an exception is thrown when the certificates for the given name is not found in the db")
    public void testGetCertificatesByNameWithEmpty() throws MangleException {
        Optional<CertificatesSpec> optional = Optional.empty();
        when(certificatesRepository.findByName(anyString())).thenReturn(optional);
        boolean actualResult = false;
        try {
            certificatesService.getCertificatesByName("");
        } catch (Exception e) {
            actualResult = true;
        }
        verify(certificatesRepository, times(0)).findByName(anyString());
        Assert.assertTrue(actualResult);
    }

    /**
     * Test method for {@link EndpointCertificatesService#getCertificatesByName(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test(description = "Test to verify that an exception is thrown when the certificates for the given name is not found in the db")
    public void testGetCertificatesByNameForNotfoundCertificate() throws MangleException {
        Optional<CertificatesSpec> optional = Optional.empty();

        when(certificatesRepository.findByName(anyString())).thenReturn(optional);
        boolean actualResult = false;
        try {
            certificatesService.getCertificatesByName("DummyCertificate");
        } catch (MangleRuntimeException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
            verify(certificatesRepository, times(1)).findByName(anyString());
        }
    }

    /**
     * Test method for {@link EndpointCertificatesService#getCertificatesByName(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test(description = "Test to verify that an exception in thrown when the null object is tried to be queried from db")
    public void testGetCertificatesByNameWithNull() throws MangleException {
        Optional<CertificatesSpec> optional = Optional.empty();
        when(certificatesRepository.findByName(anyString())).thenReturn(optional);
        boolean actualResult = false;
        try {
            certificatesService.getCertificatesByName(null);
        } catch (Exception e) {
            actualResult = true;
        }
        verify(certificatesRepository, times(0)).findByName(anyString());
        Assert.assertTrue(actualResult);
    }

    /**
     * Test method for
     * {@link EndpointCertificatesService#getAllCertificatesByType(com.vmware.mangle.model.enums.EndpointType)}.
     *
     * @throws MangleException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Test(description = "Test to verify that service returns all the certificates in the db")
    public void testGetAllCertificatesByType() throws MangleException {
        List<CertificatesSpec> certificatesList = new ArrayList<>();
        certificatesList.add(certificatesSpec);
        certificatesList.add(certificatesSpec);
        when(certificatesRepository.findByType(any())).thenReturn(certificatesList);
        List<CertificatesSpec> actualResult = certificatesService.getAllCertificatesByType(certificatesSpec.getType());
        verify(certificatesRepository, times(1)).findByType(any());
        Assert.assertEquals(actualResult.size(), 2);
    }

    /**
     * Test method for
     * {@link EndpointCertificatesService#getAllCertificatesByType(com.vmware.mangle.model.enums.EndpointType)}.
     *
     * @throws MangleException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Test(description = "Test to verify that service returns all the certificates in the db", expectedExceptions = MangleRuntimeException.class)
    public void testGetAllCertificatesByTypeNullCertificates() throws MangleException {
        when(certificatesRepository.findByType(any())).thenReturn(null);
        try {
            certificatesService.getAllCertificatesByType(certificatesSpec.getType());
        } catch (MangleRuntimeException e) {
            verify(certificatesRepository, times(1)).findByType(any());
            Assert.assertEquals(e.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
            throw e;
        }
    }

    /**
     * Test method for
     * {@link EndpointCertificatesService#getAllCertificatesByType(com.vmware.mangle.model.enums.EndpointType)}.
     *
     * @throws MangleException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Test(description = "Test to verify that service returns all the certificates in the db", expectedExceptions = MangleRuntimeException.class)
    public void testGetAllCertificatesByTypeEmptyCertificates() throws MangleException {
        List<CertificatesSpec> certificatesList = new ArrayList<>();
        when(certificatesRepository.findByType(any())).thenReturn(certificatesList);
        try {
            certificatesService.getAllCertificatesByType(certificatesSpec.getType());
        } catch (MangleRuntimeException e) {
            verify(certificatesRepository, times(1)).findByType(any());
            Assert.assertEquals(e.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
            throw e;
        }
    }

    /**
     * Test method for {@link EndpointCertificatesService#getAllCertificatesByType(EndpointType)}
     *
     * @throws MangleException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Test(description = "Test to verify that the exception is thrown when the certificates are not found in the db")
    public void testGetAllCertificatesByTypeWithNull() throws MangleException {
        List<CertificatesSpec> certificatesList = new ArrayList<>();
        when(certificatesRepository.findByType(any())).thenReturn(certificatesList);
        boolean actualResult = false;
        try {
            certificatesService.getAllCertificatesByType(null);
        } catch (Exception e) {
            actualResult = true;
        }
        verify(certificatesRepository, times(0)).findByType(any());
        Assert.assertTrue(actualResult);
    }

    /**
     * Test method for {@link EndpointCertificatesService#addOrUpdateCertificates(CertificatesSpec)}
     *
     * @throws MangleException
     */
    @Test(description = "Test to verify that the certificates spec is successfully saved into db")
    public void testAddOrUpdateCertificates() throws MangleException {
        when(certificatesRepository.save(any())).thenReturn(certificatesSpec);
        Optional<CertificatesSpec> optional = Optional.of(certificatesSpec);
        when(certificatesRepository.findByName(anyString())).thenReturn(optional);
        CertificatesSpec actualResult = certificatesService.addOrUpdateCertificates(certificatesSpec);
        verify(certificatesRepository, times(1)).save(any());
        verify(certificatesRepository, times(1)).findByName(anyString());
        Assert.assertNotNull(actualResult);
    }

    /**
     * Test method for {@link EndpointCertificatesService#addOrUpdateCertificates(CertificatesSpec)}
     *
     * @throws MangleException
     */
    @Test(description = "Test to verify that method throws an exception when null object is tried to save into db")
    public void testAddOrUpdateCertificatesWithEmpty() throws MangleException {
        when(certificatesRepository.save(any())).thenReturn(certificatesSpec);
        boolean actualResult = false;
        try {
            certificatesService.addOrUpdateCertificates(null);
        } catch (Exception e) {
            actualResult = true;
        }
        verify(certificatesRepository, times(0)).save(any());
        Assert.assertTrue(actualResult);
    }

    /**
     * Test method for
     * {@link EndpointCertificatesService#validateMultipartFileSize(org.springframework.web.multipart.MultipartFile, int)}.
     *
     * @throws MangleException
     * @throws IOException
     */
    @Test(description = "Test to verify the multipart file size")
    public void testValidateMultipartFileSize() throws MangleException, IOException {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("ca.pem");
        when(file.getBytes()).thenReturn(new byte[] { (byte) 2048 });
        certificatesService.validateMultipartFileSize(file, 2000);
        verify(file, times(1)).getBytes();
        verify(file, atMost(1)).getOriginalFilename();
        certificatesService.validateMultipartFileSize(null, 2000);
    }

    /**
     * Test method for
     * {@link EndpointCertificatesService#validateMultipartFileSize(org.springframework.web.multipart.MultipartFile, int)}.
     *
     * @throws MangleException
     * @throws IOException
     */
    @Test
    public void testValidateMultipartFileSizeWithException() throws IOException {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("ca.pem");
        when(file.getBytes()).thenReturn(new byte[] { (byte) 2000 });
        boolean actualResult = false;
        try {
            certificatesService.validateMultipartFileSize(file, 0);
        } catch (Exception e) {
            actualResult = true;
        }
        Assert.assertTrue(actualResult);
        verify(file, times(1)).getBytes();
        verify(file, atMost(1)).getOriginalFilename();
    }

    /**
     * Test method for
     * {@link EndpointCertificatesService#validateMultipartFileSize(org.springframework.web.multipart.MultipartFile, int)}.
     *
     * @throws MangleException
     * @throws IOException
     */
    @Test(expectedExceptions = MangleException.class, description = "Test to verify that an exception is thrown when the file size is greater than the specified size")
    public void testValidateMultipartFileSizeWithException1() throws MangleException, IOException {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("ca.pem");
        doThrow(new IOException()).when(file).getBytes();
        try {
            certificatesService.validateMultipartFileSize(file, 0);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.FILE_SIZE_EXCEEDED);
            verify(file, times(1)).getBytes();
            throw e;
        }
    }

    public void testValidateMultipartFileSizeNull() throws MangleException {
        certificatesService.validateMultipartFileSize(null, 0);
        Assert.assertTrue(true);
    }


    /**
     * Test method for {@link EndpointCertificatesService#getCertificatesBasedOnPage(int, int)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetCertificatesBasedOnPage() {
        Slice<CertificatesSpec> page = Mockito.mock(Slice.class);
        when(page.getSize()).thenReturn(4);
        when(certificatesRepository.findAll(any(Pageable.class))).thenReturn(page);

        Slice<CertificatesSpec> actualResult = certificatesService.getCertificatesBasedOnPage(1, 4);
        verify(certificatesRepository, times(1)).findAll(any(Pageable.class));
        Assert.assertEquals(actualResult.getSize(), 4);
        verify(page, times(1)).getSize();
    }

    /**
     * Test method for {@link EndpointCertificatesService#getCertificatesBasedOnPage(int, int)}.
     */
    @SuppressWarnings("unchecked")
    @Test(description = "Test to get the data from 2 page with size 4")
    public void testGetCertificatesBasedOnPageCase1() {
        Slice<CertificatesSpec> slice = Mockito.mock(Slice.class);
        when(slice.getSize()).thenReturn(4);
        when(certificatesRepository.findAll(any(Pageable.class))).thenReturn(slice);
        CassandraPageRequest pageable = Mockito.mock(CassandraPageRequest.class);
        when(slice.getPageable()).thenReturn(pageable);
        when(pageable.getPagingState()).thenReturn(null);
        Slice<CertificatesSpec> actualResult = certificatesService.getCertificatesBasedOnPage(2, 4);
        verify(certificatesRepository, times(1)).findAll(any(Pageable.class));
        Assert.assertEquals(actualResult.getSize(), 4);
        verify(slice, times(1)).getSize();
    }

    /**
     * Test method for {@link EndpointCertificatesService#getCertificatesBasedOnPage(int, int)}.
     */
    @SuppressWarnings("unchecked")
    @Test(description = "Test to get the data from 3 page with size 4")
    public void testGetCertificatesBasedOnPage2() {
        Slice<CertificatesSpec> slice = Mockito.mock(Slice.class);
        when(slice.getSize()).thenReturn(4);
        when(certificatesRepository.findAll(any(Pageable.class))).thenReturn(slice);
        CassandraPageRequest pageable = Mockito.mock(CassandraPageRequest.class);
        when(slice.getPageable()).thenReturn(pageable);
        when(pageable.getPageNumber()).thenReturn(3);
        when(pageable.getPageSize()).thenReturn(4);
        PagingState pagingState = Mockito.mock(PagingState.class);
        when(pageable.getPagingState()).thenReturn(pagingState);
        Slice<CertificatesSpec> actualResult = certificatesService.getCertificatesBasedOnPage(3, 4);
        verify(certificatesRepository, times(3)).findAll(any(Pageable.class));
        Assert.assertEquals(actualResult.getSize(), 4);
        verify(slice, times(1)).getSize();
    }

    /**
     * Test method for {@link EndpointCertificatesService#updateCertificates(CertificatesSpec)}
     *
     * @throws MangleException
     */
    @Test
    public void testUpdateCertificatesByName() throws MangleException {
        when(certificatesRepository.save(any())).thenReturn(certificatesSpec);
        Optional<CertificatesSpec> optional = Optional.of(certificatesSpec);
        when(certificatesRepository.findByName(anyString())).thenReturn(optional);
        CertificatesSpec actualResult = certificatesService.updateCertificates(certificatesSpec);
        verify(certificatesRepository, times(1)).save(any());
        verify(certificatesRepository, times(1)).findByName(anyString());
        Assert.assertEquals(actualResult, certificatesSpec);
    }

    /**
     * Test method for {@link EndpointCertificatesService#updateCertificates(CertificatesSpec)}
     *
     * @throws MangleException
     */
    @Test
    public void testUpdateCertificatesWithNoneExistingName() throws MangleException {
        Optional<CertificatesSpec> optional = Optional.empty();
        when(certificatesRepository.findByName(anyString())).thenReturn(optional);
        boolean actualResult = false;
        try {
            certificatesService.updateCertificates(certificatesSpec);
        } catch (MangleRuntimeException e) {
            actualResult = true;
            Assert.assertEquals(e.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
        }
        verify(certificatesRepository, times(1)).findByName(anyString());
        Assert.assertTrue(actualResult);
    }

    /**
     * Test method for {@link EndpointCertificatesService#updateCertificates(CertificatesSpec)}
     *
     */
    @Test
    public void testUpdateCertificatesWithNullName() {
        CertificatesSpec certificatesSpec1 = certificatesSpecMockData.getDockerCertificatesData();
        certificatesSpec1.setName("null");
        boolean actualResult = false;
        try {
            certificatesService.updateCertificates(certificatesSpec1);
        } catch (MangleException e) {
            actualResult = true;
            Assert.assertEquals(e.getErrorCode(), ErrorCode.CERTIFICATES_NAME_NOT_VALID);
        }
        Assert.assertTrue(actualResult);
    }

    /**
     * Test method for {@link EndpointCertificatesService#updateCertificates(CertificatesSpec)}
     *
     * @throws MangleException
     */
    @Test
    public void testUpdateCertificatesWithSameNameAndDifferentType() throws MangleException {
        DockerCertificates dockerCertificates = certificatesSpecMockData.getDockerCertificatesData();
        dockerCertificates.setName(certificatesSpec.getName());
        dockerCertificates.setType(EndpointType.K8S_CLUSTER);
        Optional<CertificatesSpec> optional = Optional.of(dockerCertificates);
        when(certificatesRepository.findByName(anyString())).thenReturn(optional);
        boolean actualResult = false;
        try {
            certificatesService.updateCertificates(certificatesSpec);
        } catch (MangleRuntimeException e) {
            actualResult = true;
            Assert.assertEquals(e.getErrorCode(), ErrorCode.DUPLICATE_RECORD_FOR_ENDPOINT_CERTIFICATES);
        }
        verify(certificatesRepository, times(1)).findByName(anyString());
        Assert.assertTrue(actualResult);
    }


    /**
     * Test method for
     * {@link EndpointCertificatesService#testgenerateDockerCertificatesSpec(CertificatesSpec)}
     *
     * @throws IOException
     *
     * @throws MangleException
     */
    @Test
    public void testGenerateDockerCertificatesSpec() throws IOException {
        DockerCertificates certificates =
                certificatesService.generateDockerCertificatesSpec(null, null, null, null, null);
        Assert.assertNotNull(certificates);
        Assert.assertNotNull(certificates.getId());
        MockMultipartFile multiPartPrivateKeyFile =
                new MockMultipartFile("DummyPrivateKey", "DummyPrivateKey".getBytes());
        MockMultipartFile multiPartCaCertFile = new MockMultipartFile("DummyCaCert", "DummyCaCert".getBytes());
        MockMultipartFile multiPartServerCertFile =
                new MockMultipartFile("DummyServerCert", "DummyServerCert".getBytes());
        certificates = certificatesService.generateDockerCertificatesSpec("id", "TestDockerCertificates",
                multiPartCaCertFile, multiPartServerCertFile, multiPartPrivateKeyFile);
        Assert.assertNotNull(certificates);
        Assert.assertNotNull(certificates.getId(), "id");
    }

    /**
     * Test method for
     * {@link EndpointCertificatesService#validatePasswordOrPrivateKeyNotNull(String, MultipartFile)}
     *
     */
    @Test
    public void testValidateDockerCACertificates() {
        try {
            byte[] caCert = certificatesSpecMockData.getInValidCertificatesAsbyteArray();
            certificatesService.validateDockerCertificate(caCert, "caCert");
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.DOCKER_INVALID_CERTIFICATE);
        }
        try {
            certificatesService.validateDockerCertificate(certificatesSpecMockData.getEmptyByteArray(), "caCert");
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.DOCKER_INVALID_CERTIFICATE);
        }
    }

    /**
     * Test method for
     * {@link EndpointCertificatesService#validatePasswordOrPrivateKeyNotNull(String, MultipartFile)}
     *
     */
    @Test
    public void testValidateDockerServerCertificate() {
        try {
            byte[] serverCert = certificatesSpecMockData.getInValidCertificatesAsbyteArray();
            certificatesService.validateDockerCertificate(serverCert, "serverCert");
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.DOCKER_INVALID_CERTIFICATE);
        }
        try {
            certificatesService.validateDockerCertificate(certificatesSpecMockData.getEmptyByteArray(), "serverCert");
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.DOCKER_INVALID_CERTIFICATE);
        }

    }

    /**
     * Test method for
     * {@link EndpointCertificatesService#validatePasswordOrPrivateKeyNotNull(String, MultipartFile)}
     *
     */
    @Test
    public void testValidateDockerPrivateKey() {
        try {
            byte[] privateKey = certificatesSpecMockData.getInValidPrivateKey();
            certificatesService.validateDockerPrivateKey(privateKey);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.DOCKER_INVALID_PRIVATEKEY);
        }
        try {
            certificatesService.validateDockerPrivateKey(certificatesSpecMockData.getEmptyByteArray());
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.DOCKER_INVALID_PRIVATEKEY);
        }
    }

    /**
     * Test method for
     * {@link EndpointCertificatesService#validatePasswordOrPrivateKeyNotNull(String, MultipartFile)}
     *
     */
    @Test
    public void testValidateInvalidRemoteMachinePrivateKey() {
        MultipartFile file = new MockMultipartFile("Test", new byte[2]);
        try {
            certificatesService.validateRemoteMachinePrivateKey(file);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.RM_INVALID_PRIVATEKEY);
        }

    }


    @Test
    public void testValidateRemoteMachinePrivateKeyForIOException() throws IOException {
        MultipartFile privateKey = Mockito.mock(MultipartFile.class);
        doThrow(IOException.class).when(privateKey).getBytes();
        try {
            certificatesService.validateRemoteMachinePrivateKey(privateKey);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.IO_EXCEPTION);
        }

    }
}