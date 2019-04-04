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
import java.util.UUID;

import com.datastax.driver.core.PagingState;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.CredentialsSpec;
import com.vmware.mangle.cassandra.model.endpoint.K8SCredentials;
import com.vmware.mangle.cassandra.model.endpoint.RemoteMachineCredentials;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.CredentialService;
import com.vmware.mangle.services.mockdata.CredentialsSpecMockData;
import com.vmware.mangle.services.repository.CredentialRepository;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Insert your comment for CredentialServiceTest here
 *
 * @author kumargautam
 */
public class CredentialServiceTest extends PowerMockTestCase {

    private CredentialService credentialService;
    @Mock
    private CredentialRepository credentialRepository;

    private CredentialsSpecMockData credentialsSpecMockData = new CredentialsSpecMockData();
    private CredentialsSpec credentialsSpec;


    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        credentialService = new CredentialService(credentialRepository);
        this.credentialsSpec = credentialsSpecMockData.getRMCredentialsData();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public void tearDownAfterClass() throws Exception {
        this.credentialsSpec = null;
        this.credentialsSpecMockData = null;
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterTest
    public void tearDownAfterTest() throws Exception {
        validateMockitoUsage();
    }

    /**
     * Test method for {@link CredentialService#getAllCredentials()}.
     */
    @Test(description = "Test to verify the retrieving of the all credentials from the db")
    public void testGetAllCredentials() {
        CredentialsSpec credentialsSpec1 = credentialsSpecMockData.getAWSCredentialsData();
        List<CredentialsSpec> credentialList = new ArrayList<>();
        credentialList.add(credentialsSpec);
        credentialList.add(credentialsSpec1);
        when(credentialRepository.findAll()).thenReturn(credentialList);
        List<CredentialsSpec> actualCredentialsSpecList = credentialService.getAllCredentials();
        verify(credentialRepository, times(1)).findAll();
        int expectedCount = 2;
        Assert.assertEquals(actualCredentialsSpecList.size(), expectedCount);
    }

    /**
     * Test method for {@link CredentialService#getCredentialByName(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test(description = "Test to verify the retrieving of a particular credentials by name from the db")
    public void testGetCredentialByName() throws MangleException {
        Optional<CredentialsSpec> optional = Optional.of(credentialsSpec);
        when(credentialRepository.findByName(anyString())).thenReturn(optional);
        CredentialsSpec actualResult = credentialService.getCredentialByName(credentialsSpec.getName());
        verify(credentialRepository, times(1)).findByName(anyString());
        Assert.assertEquals(actualResult, credentialsSpec);
    }

    /**
     * Test method for {@link CredentialService#getCredentialByName(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test(description = "Test to verify that an exception is thrown when the credentials for the given name is not found in the db")
    public void testGetCredentialByNameWithEmpty() throws MangleException {
        Optional<CredentialsSpec> optional = Optional.empty();
        when(credentialRepository.findByName(anyString())).thenReturn(optional);
        boolean actualResult = false;
        try {
            credentialService.getCredentialByName("");
        } catch (Exception e) {
            actualResult = true;
        }
        verify(credentialRepository, times(0)).findByName(anyString());
        Assert.assertTrue(actualResult);
    }

    /**
     * Test method for {@link CredentialService#getCredentialByName(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test(description = "Test to verify that an exception in thrown when the null object is tried to be queried from db")
    public void testGetCredentialByNameWithNull() throws MangleException {
        Optional<CredentialsSpec> optional = Optional.empty();
        when(credentialRepository.findByName(anyString())).thenReturn(optional);
        boolean actualResult = false;
        try {
            credentialService.getCredentialByName(null);
        } catch (Exception e) {
            actualResult = true;
        }
        verify(credentialRepository, times(0)).findByName(anyString());
        Assert.assertTrue(actualResult);
    }

    /**
     * Test method for
     * {@link CredentialService#getAllCredentialByType(com.vmware.mangle.model.enums.EndpointType)}.
     *
     * @throws MangleException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Test(description = "Test to verify that service returns all the credentials in the db")
    public void testGetAllCredentialByType() throws MangleException {
        List<CredentialsSpec> credentialList = new ArrayList<>();
        credentialList.add(credentialsSpec);
        credentialList.add(credentialsSpec);
        when(credentialRepository.findByType(any())).thenReturn(credentialList);
        List<CredentialsSpec> actualResult = credentialService.getAllCredentialByType(credentialsSpec.getType());
        verify(credentialRepository, times(1)).findByType(any());
        Assert.assertEquals(actualResult.size(), 2);
    }

    /**
     * Test method for
     * {@link CredentialService#getAllCredentialByType(com.vmware.mangle.model.enums.EndpointType)}.
     *
     * @throws MangleException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Test(description = "Test to verify that service returns all the credentials in the db", expectedExceptions = MangleRuntimeException.class)
    public void testGetAllCredentialByTypeNullCredentials() throws MangleException {
        when(credentialRepository.findByType(any())).thenReturn(null);
        try {
            credentialService.getAllCredentialByType(credentialsSpec.getType());
        } catch (MangleRuntimeException e) {
            verify(credentialRepository, times(1)).findByType(any());
            Assert.assertEquals(e.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
            throw e;
        }


    }

    /**
     * Test method for {@link CredentialService#getAllCredentialByType(EndpointType)}
     *
     * @throws MangleException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @Test(description = "Test to verify that the exception is thrown when the credentials are not found in the db")
    public void testGetAllCredentialByTypeWithNull() throws MangleException {
        List<CredentialsSpec> credentialList = new ArrayList<>();
        when(credentialRepository.findByType(any())).thenReturn(credentialList);
        boolean actualResult = false;
        try {
            credentialService.getAllCredentialByType(null);
        } catch (Exception e) {
            actualResult = true;
        }
        verify(credentialRepository, times(0)).findByType(any());
        Assert.assertTrue(actualResult);
    }

    /**
     * Test method for {@link CredentialService#addOrUpdateCredentials(CredentialsSpec)}
     *
     * @throws MangleException
     */
    @Test(description = "Test to verify that the credentials spec is successfully saved into db")
    public void testAddOrUpdateCredentials() throws MangleException {
        when(credentialRepository.save(any())).thenReturn(credentialsSpec);
        CredentialsSpec actualResult = credentialService.addOrUpdateCredentials(credentialsSpec);
        verify(credentialRepository, times(1)).save(any());
        Assert.assertNotNull(actualResult);
    }

    /**
     * Test method for {@link CredentialService#addOrUpdateCredentials(CredentialsSpec)}
     *
     * @throws MangleException
     */
    @Test(description = "Test to verify that method throws an exception when null object is tried to save into db")
    public void testAddOrUpdateCredentialsWithEmpty() throws MangleException {
        when(credentialRepository.save(any())).thenReturn(credentialsSpec);
        boolean actualResult = false;
        try {
            credentialService.addOrUpdateCredentials(null);
        } catch (Exception e) {
            actualResult = true;
        }
        verify(credentialRepository, times(0)).save(any());
        Assert.assertTrue(actualResult);
    }

    /**
     * Test method for
     * {@link CredentialService#validateMultipartFileSize(org.springframework.web.multipart.MultipartFile, int)}.
     *
     * @throws MangleException
     * @throws IOException
     */
    @Test(description = "Test to verify the multipart file size")
    public void testValidateMultipartFileSize() throws MangleException, IOException {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("kube.config");
        when(file.getBytes()).thenReturn(new byte[] { (byte) 2048 });
        credentialService.validateMultipartFileSize(file, 2000);
        verify(file, times(1)).getBytes();
        verify(file, atMost(1)).getOriginalFilename();
    }

    /**
     * Test method for
     * {@link CredentialService#validateMultipartFileSize(org.springframework.web.multipart.MultipartFile, int)}.
     *
     * @throws MangleException
     * @throws IOException
     */
    @Test
    public void testValidateMultipartFileSizeWithException() throws IOException {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("kube.config");
        when(file.getBytes()).thenReturn(new byte[] { (byte) 2000 });
        boolean actualResult = false;
        try {
            credentialService.validateMultipartFileSize(file, 0);
        } catch (Exception e) {
            actualResult = true;
        }
        Assert.assertTrue(actualResult);
        verify(file, times(1)).getBytes();
        verify(file, atMost(1)).getOriginalFilename();
    }

    /**
     * Test method for
     * {@link CredentialService#validateMultipartFileSize(org.springframework.web.multipart.MultipartFile, int)}.
     *
     * @throws MangleException
     * @throws IOException
     */
    @Test(expectedExceptions = MangleException.class, description = "Test to verify that an exception is thrown when the file size is greater than the specified size")
    public void testValidateMultipartFileSizeWithException1() throws MangleException, IOException {
        MultipartFile file = Mockito.mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("kube.config");
        doThrow(new IOException()).when(file).getBytes();
        try {
            credentialService.validateMultipartFileSize(file, 0);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.FILE_SIZE_EXCEEDED);
            verify(file, times(1)).getBytes();
            throw e;
        }
    }

    public void testValidateMultipartFileSizeNull() throws MangleException {
        credentialService.validateMultipartFileSize(null, 0);
        Assert.assertTrue(true);
    }

    /**
     * Test method for
     * {@link CredentialService#generateRemoteMachineCredentialsSpec(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.springframework.web.multipart.MultipartFile)}.
     *
     * @throws IOException
     */
    @Test
    public void testGenerateRemoteMachineCredentialsSpec() throws IOException {
        MultipartFile privateKey = Mockito.mock(MultipartFile.class);
        when(privateKey.getBytes()).thenReturn(new byte[] { (byte) 2048 });
        RemoteMachineCredentials remoteMachineCredentials = credentialService.generateRemoteMachineCredentialsSpec(null,
                credentialsSpec.getName(), "root", "vaware", privateKey);
        verify(privateKey, times(1)).getBytes();
        Assert.assertNotNull(remoteMachineCredentials);
    }

    /**
     * Test method for
     * {@link CredentialService#generateRemoteMachineCredentialsSpec(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.springframework.web.multipart.MultipartFile)}.
     *
     * @throws IOException
     */
    @Test(description = "Test to verify the creation of remoteMachineCredentialsSpec object")
    public void testGenerateRemoteMachineCredentialsSpec1() throws IOException {
        String dummyId = UUID.randomUUID().toString();
        RemoteMachineCredentials remoteMachineCredentials = credentialService
                .generateRemoteMachineCredentialsSpec(dummyId, credentialsSpec.getName(), "root", "vmware", null);
        Assert.assertNotNull(remoteMachineCredentials);
        Assert.assertEquals(remoteMachineCredentials.getId(), dummyId);
        Assert.assertNull(remoteMachineCredentials.getPrivateKey());
    }

    /**
     * Test method for
     * {@link CredentialService#generateK8SCredentialsSpec(java.lang.String, java.lang.String, org.springframework.web.multipart.MultipartFile)}.
     *
     * @throws IOException
     */
    @Test
    public void testGenerateK8SCredentialsSpec() throws IOException {
        MultipartFile kubeConfig = Mockito.mock(MultipartFile.class);
        when(kubeConfig.getBytes()).thenReturn(new byte[] { (byte) 2048 });
        K8SCredentials k8sCredentials = credentialService.generateK8SCredentialsSpec(null, "localk8s", kubeConfig);
        verify(kubeConfig, times(1)).getBytes();
        Assert.assertNotNull(k8sCredentials);

    }

    /**
     * Test method for
     * {@link CredentialService#generateK8SCredentialsSpec(java.lang.String, java.lang.String, org.springframework.web.multipart.MultipartFile)}.
     *
     * @throws IOException
     */
    @Test(description = "Test to verify the creation of K8SCredentialsSpec object")
    public void testGenerateK8SCredentialsSpec1() throws IOException {
        String dummyId = UUID.randomUUID().toString();
        K8SCredentials k8sCredentials = credentialService.generateK8SCredentialsSpec(dummyId, "localk8s", null);
        Assert.assertNotNull(k8sCredentials);
        Assert.assertNull(k8sCredentials.getKubeConfig());
        Assert.assertEquals(k8sCredentials.getId(), dummyId);

    }

    /**
     * Test method for {@link CredentialService#getCredentialsBasedOnPage(int, int)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetCredentialsBasedOnPage() {
        Slice<CredentialsSpec> page = Mockito.mock(Slice.class);
        when(page.getSize()).thenReturn(4);
        when(credentialRepository.findAll(any(Pageable.class))).thenReturn(page);

        Slice<CredentialsSpec> actualResult = credentialService.getCredentialsBasedOnPage(1, 4);
        verify(credentialRepository, times(1)).findAll(any(Pageable.class));
        Assert.assertEquals(actualResult.getSize(), 4);
        verify(page, times(1)).getSize();
    }

    /**
     * Test method for {@link CredentialService#getCredentialsBasedOnPage(int, int)}.
     */
    @SuppressWarnings("unchecked")
    @Test(description = "Test to get the data from 2 page with size 4")
    public void testGetEndpointBasedOnPageCase1() {
        Slice<CredentialsSpec> slice = Mockito.mock(Slice.class);
        when(slice.getSize()).thenReturn(4);
        when(credentialRepository.findAll(any(Pageable.class))).thenReturn(slice);
        CassandraPageRequest pageable = Mockito.mock(CassandraPageRequest.class);
        when(slice.getPageable()).thenReturn(pageable);
        when(pageable.getPagingState()).thenReturn(null);
        Slice<CredentialsSpec> actualResult = credentialService.getCredentialsBasedOnPage(2, 4);
        verify(credentialRepository, times(1)).findAll(any(Pageable.class));
        Assert.assertEquals(actualResult.getSize(), 4);
        verify(slice, times(1)).getSize();
    }

    /**
     * Test method for {@link CredentialService#getCredentialsBasedOnPage(int, int)}.
     */
    @SuppressWarnings("unchecked")
    @Test(description = "Test to get the data from 3 page with size 4")
    public void testGetEndpointBasedOnPageCase2() {
        Slice<CredentialsSpec> slice = Mockito.mock(Slice.class);
        when(slice.getSize()).thenReturn(4);
        when(credentialRepository.findAll(any(Pageable.class))).thenReturn(slice);
        CassandraPageRequest pageable = Mockito.mock(CassandraPageRequest.class);
        when(slice.getPageable()).thenReturn(pageable);
        when(pageable.getPageNumber()).thenReturn(3);
        when(pageable.getPageSize()).thenReturn(4);
        PagingState pagingState = Mockito.mock(PagingState.class);
        when(pageable.getPagingState()).thenReturn(pagingState);
        Slice<CredentialsSpec> actualResult = credentialService.getCredentialsBasedOnPage(3, 4);
        verify(credentialRepository, times(3)).findAll(any(Pageable.class));
        Assert.assertEquals(actualResult.getSize(), 4);
        verify(slice, times(1)).getSize();
    }

}
