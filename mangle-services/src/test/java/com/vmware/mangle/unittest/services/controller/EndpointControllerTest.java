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

package com.vmware.mangle.unittest.services.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.stubbing.answers.ReturnsArgumentAt;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Slice;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.AWSCredentials;
import com.vmware.mangle.cassandra.model.endpoint.AzureCredentials;
import com.vmware.mangle.cassandra.model.endpoint.CertificatesSpec;
import com.vmware.mangle.cassandra.model.endpoint.CredentialsSpec;
import com.vmware.mangle.cassandra.model.endpoint.DatabaseCredentials;
import com.vmware.mangle.cassandra.model.endpoint.DockerCertificates;
import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.endpoint.K8SCredentials;
import com.vmware.mangle.cassandra.model.endpoint.RemoteMachineCredentials;
import com.vmware.mangle.cassandra.model.endpoint.VCenterCredentials;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.model.response.DeleteEndpointOperationResponse;
import com.vmware.mangle.model.response.DeleteOperationResponse;
import com.vmware.mangle.model.response.ErrorDetails;
import com.vmware.mangle.services.CredentialService;
import com.vmware.mangle.services.EndpointCertificatesService;
import com.vmware.mangle.services.EndpointService;
import com.vmware.mangle.services.EventService;
import com.vmware.mangle.services.constants.CommonConstants;
import com.vmware.mangle.services.controller.EndpointController;
import com.vmware.mangle.services.deletionutils.CredentialDeletionService;
import com.vmware.mangle.services.deletionutils.EndpointCertificatesDeletionService;
import com.vmware.mangle.services.deletionutils.EndpointDeletionService;
import com.vmware.mangle.services.enums.K8SResource;
import com.vmware.mangle.services.events.web.CustomEventPublisher;
import com.vmware.mangle.services.mockdata.CertificatesSpecMockData;
import com.vmware.mangle.services.mockdata.CredentialsSpecMockData;
import com.vmware.mangle.services.mockdata.EndpointMockData;
import com.vmware.mangle.services.updateutils.EndpointUpdateService;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 *
 *
 * @author chetanc
 */
public class EndpointControllerTest {

    @Mock
    private EndpointService endpointService;

    @Mock
    private CredentialService credentialService;

    @Mock
    private EndpointCertificatesService certificatesService;

    @Mock
    private EndpointCertificatesDeletionService certificatesDeletionService;

    @InjectMocks
    private CustomEventPublisher publisher;

    @Mock
    private EventService eventService;

    @Mock
    private EndpointDeletionService endpointDeletionService;

    @Mock
    private EndpointUpdateService endpointUpdateService;

    @Mock
    private CredentialDeletionService credentialDeletionService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;


    private EndpointController controller;

    private EndpointMockData mockData = new EndpointMockData();
    private CredentialsSpecMockData credentialsSpecMockData = new CredentialsSpecMockData();
    private CertificatesSpecMockData certificatesSpecMockData = new CertificatesSpecMockData();


    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        publisher = new CustomEventPublisher(applicationEventPublisher, eventService);
        controller = spy(new EndpointController(endpointService, credentialService, publisher, endpointDeletionService,
                credentialDeletionService, certificatesService, certificatesDeletionService, endpointUpdateService));
        Link link = mock(Link.class);
        doReturn(link).when(controller).getSelfLink();
        when(eventService.save(any())).then(new ReturnsArgumentAt(0));
    }

    @Test
    public void getEndpointsTest() throws MangleException {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        List<EndpointSpec> list = new ArrayList<>();
        list.add(spec);
        when(endpointService.getAllEndpoints()).thenReturn(list);
        ResponseEntity<Resources<EndpointSpec>> response = controller.getEndpoints("");
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertNotNull(response.getBody());
        Collection<EndpointSpec> responseList = response.getBody().getContent();
        Assert.assertEquals(responseList.size(), 1);
        Assert.assertEquals(responseList.iterator().next(), spec);
        verify(endpointService, atLeast(1)).getAllEndpoints();
    }

    @Test
    public void getEndpointsTestByCredentials() throws MangleException {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        VCenterCredentials credentials = credentialsSpecMockData.getVCenterCredentialsData();

        List<EndpointSpec> list = new ArrayList<>();
        list.add(spec);
        when(endpointService.getEndpointsSpecByCredentialName(credentials.getName())).thenReturn(list);
        ResponseEntity<Resources<EndpointSpec>> response = controller.getEndpoints(credentials.getName());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);

        Assert.assertNotNull(response.getBody());
        Collection<EndpointSpec> responseList = response.getBody().getContent();

        Assert.assertEquals(responseList.size(), 1);
        Assert.assertEquals(responseList.iterator().next(), spec);
        verify(endpointService, atLeast(1)).getEndpointsSpecByCredentialName(anyString());
    }

    @Test
    public void getEndpointByNameTest() throws MangleException {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        when(endpointService.getEndpointByName(anyString())).thenReturn(spec);
        ResponseEntity<Resource<EndpointSpec>> response = controller.getEndpointByName(spec.getName());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertNotNull(response.getBody());
        Assert.assertEquals(spec, response.getBody().getContent());
        verify(endpointService, atLeast(1)).getEndpointByName(anyString());
    }

    @Test
    public void getAllEndpointByTypeTest() throws MangleException {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        List<EndpointSpec> list = new ArrayList<>();
        list.add(spec);
        when(endpointService.getAllEndpointByType(any())).thenReturn(list);
        ResponseEntity<Resources<EndpointSpec>> response = controller.getAllEndpointByType(EndpointType.WAVEFRONT);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertNotNull(response.getBody());
        Collection<EndpointSpec> responseList = response.getBody().getContent();
        Assert.assertEquals(responseList.size(), 1);
        Assert.assertEquals(responseList.iterator().next(), spec);
        verify(endpointService, atLeast(1)).getAllEndpointByType(any());
    }

    @Test
    public void getAllDockerContainersByEndpoint() throws MangleException {
        EndpointSpec spec = mockData.getDockerEndpointSpecMock();
        String containerName = "container1";
        List<String> list = new ArrayList<>();
        list.add(containerName);
        when(endpointService.getAllContainersByEndpointName(any())).thenReturn(list);
        ResponseEntity<Resources<String>> response = controller.getAllDockerContainersByEndpoint(spec.getName());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertNotNull(response.getBody());
        Collection<String> responseList = response.getBody().getContent();
        Assert.assertEquals(responseList.size(), 1);
        Assert.assertEquals(responseList.iterator().next(), containerName);
        verify(endpointService, atLeast(1)).getAllContainersByEndpointName(any());
    }

    @Test
    public void updateEndpointTest() throws MangleException {
        EndpointSpec spec1 = mockData.getVCenterEndpointSpecMock();
        when(endpointUpdateService.updateEndpointByEndpointName(anyString(), any())).thenReturn(spec1);
        when(endpointService.testEndpointConnection(any())).thenReturn(true);
        ResponseEntity<Resource<EndpointSpec>> response = controller.updateEndpoint(spec1);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertNotNull(response.getBody());
        Assert.assertEquals(response.getBody().getContent(), spec1);
        verify(endpointUpdateService, atLeast(1)).updateEndpointByEndpointName(anyString(), any());
    }

    @Test(expectedExceptions = MangleException.class)
    public void updateEndpointTestFailure() throws MangleException {
        EndpointSpec spec1 = mockData.getVCenterEndpointSpecMock();
        when(endpointUpdateService.updateEndpointByEndpointName(anyString(), any())).thenReturn(spec1);
        when(endpointService.testEndpointConnection(any())).thenReturn(false);
        try {
            controller.updateEndpoint(spec1);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.TEST_CONNECTION_FAILED);
            verify(endpointService, atLeast(1)).testEndpointConnection(any());
            throw e;
        }
    }

    @Test
    public void getCredentialsTest() throws MangleException {
        CredentialsSpec credentialsSpec = credentialsSpecMockData.getVCenterCredentialsData();
        List<CredentialsSpec> list = new ArrayList<>();
        list.add(credentialsSpec);
        when(credentialService.getAllCredentials()).thenReturn(list);
        ResponseEntity<Resources<CredentialsSpec>> response = controller.getCredentials();
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertNotNull(response.getBody());
        Collection<CredentialsSpec> responseList = response.getBody().getContent();
        Assert.assertEquals(responseList.size(), 1);
        Assert.assertEquals(responseList.iterator().next(), credentialsSpec);
    }


    /**
     * Test method for {@link EndpointController#addEndpoint(EndpointSpec)}
     *
     */
    @Test
    public void testAddEndPoint() throws MangleException {
        EndpointSpec spec = mockData.getDockerEndpointSpecMock();
        when(endpointService.testEndpointConnection(spec)).thenReturn(true);
        when(endpointService.addOrUpdateEndpoint(spec)).thenReturn(spec);
        ResponseEntity<Resource<EndpointSpec>> responseEntity = controller.addEndpoint(spec);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertNotNull(responseEntity.getBody());
        Assert.assertEquals(responseEntity.getBody().getContent(), spec);
    }


    /**
     * Test method for {@link EndpointController#addEndpoint(EndpointSpec)}
     *
     */
    @Test(expectedExceptions = MangleException.class)
    public void testAddEndPoint1() throws MangleException {
        EndpointSpec spec = mockData.getDockerEndpointSpecMock();
        when(endpointService.testEndpointConnection(spec)).thenReturn(false);
        when(endpointService.addOrUpdateEndpoint(spec)).thenReturn(spec);
        try {
            controller.addEndpoint(spec);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.TEST_CONNECTION_FAILED);
            throw e;
        }
    }

    /**
     * Test method for
     * {@link EndpointController#addCredentials(String, String, String, String, MultipartFile)}
     */
    @Test
    public void testAddCredentials() throws MangleException, IOException {
        MultipartFile privateKey = Mockito.mock(MultipartFile.class);
        String pwd = "pwd";
        String username = "username";
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        RemoteMachineCredentials remoteMachineCredentials = Mockito.mock(RemoteMachineCredentials.class);
        CredentialsSpec credentialsSpec = Mockito.mock(CredentialsSpec.class);
        when(credentialService.addOrUpdateCredentials(any())).thenReturn(credentialsSpec);
        when(credentialService.generateRemoteMachineCredentialsSpec(anyString(), anyString(), anyString(), anyString(),
                any())).thenReturn(remoteMachineCredentials);
        doNothing().when(certificatesService).validateRemoteMachinePrivateKey(any());
        ResponseEntity<Resource<CredentialsSpec>> responseEntity =
                controller.addCredentials(spec.getId(), spec.getName(), username, pwd, privateKey);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        verify(credentialService, times(1)).addOrUpdateCredentials(any());
        verify(certificatesService, times(1)).validateRemoteMachinePrivateKey(any());
        verify(credentialService, times(1)).generateRemoteMachineCredentialsSpec(anyString(), anyString(), anyString(),
                anyString(), any());
    }

    /**
     * Test method for
     * {@link EndpointController#addCredentials(String, String, String, String, MultipartFile)}
     */
    @Test(expectedExceptions = MangleException.class)
    public void testAddCredentialsException() throws MangleException, IOException {
        MultipartFile privateKey = Mockito.mock(MultipartFile.class);
        String pwd = "pwd";
        String username = "username";
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        doThrow(new IOException()).when(credentialService).generateRemoteMachineCredentialsSpec(anyString(),
                anyString(), anyString(), anyString(), any());
        try {
            controller.addCredentials(spec.getId(), spec.getName(), username, pwd, privateKey);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.FILE_SIZE_EXCEEDED);
            verify(credentialService, times(1)).generateRemoteMachineCredentialsSpec(anyString(), anyString(),
                    anyString(), anyString(), any());
            throw e;
        }
    }


    /**
     * Test method for {@link EndpointController#addCredentials(AWSCredentials)}
     */
    @Test
    public void testAddCredentialsAWS() throws MangleException {
        AWSCredentials credentialsSpec = credentialsSpecMockData.getAWSCredentialsData();
        when(credentialService.addOrUpdateCredentials(any())).thenReturn(credentialsSpec);
        ResponseEntity<Resource<CredentialsSpec>> responseEntity = controller.addCredentials(credentialsSpec);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertNotNull(responseEntity.getBody());
        Assert.assertEquals(responseEntity.getBody().getContent(), credentialsSpec);
    }

    /**
     * Test method for {@link EndpointController#addCredentials(AzureCredentials)}
     */
    @Test
    public void testAddCredentialsAzure() throws MangleException {
        CredentialsSpec credentialsSpec = credentialsSpecMockData.getAzureCredentialsData();
        when(credentialService.addOrUpdateCredentials(any())).thenReturn(credentialsSpec);
        ResponseEntity<CredentialsSpec> responseEntity =
                controller.addAzureCredentials((AzureCredentials) credentialsSpec);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseEntity.getBody(), credentialsSpec);
    }

    /**
     * Test method for {@link EndpointController#addCredentials(VCenterCredentials)}
     *
     */
    @Test
    public void testAddCredentialsVCenter() throws MangleException {
        VCenterCredentials credentialsSpec = credentialsSpecMockData.getVCenterCredentialsData();
        when(credentialService.addOrUpdateCredentials(any())).thenReturn(credentialsSpec);
        ResponseEntity<Resource<CredentialsSpec>> responseEntity = controller.addCredentials(credentialsSpec);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertNotNull(responseEntity.getBody());
        Assert.assertEquals(responseEntity.getBody().getContent(), credentialsSpec);
    }

    /**
     * Test method for {@link EndpointController#deleteEndpointsByNames(List)}
     */
    @Test
    public void testDeleteEndpointsByNames() throws MangleException {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        ArrayList<String> list = new ArrayList<>();
        list.add(spec.getName());
        when(endpointDeletionService.deleteEndpointByNames(any())).thenReturn(new DeleteEndpointOperationResponse());
        ResponseEntity<ErrorDetails> responseEntity = controller.deleteEndpointsByNames(list);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.NO_CONTENT);
        verify(endpointDeletionService, times(1)).deleteEndpointByNames(any());
    }

    /**
     * Test method for {@link EndpointController#deleteEndpointsByNames(List)}
     */
    @Test
    public void testDeleteEndpointsByNamesFailed() throws MangleException {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        ArrayList<String> list = new ArrayList<>();
        list.add(spec.getName());
        Map<String, List<String>> associations = new HashMap<>();
        associations.put(spec.getId(), new ArrayList<>());
        DeleteEndpointOperationResponse response = new DeleteEndpointOperationResponse();
        response.setAssociations(associations);


        when(endpointDeletionService.deleteEndpointByNames(any())).thenReturn(response);
        ResponseEntity<ErrorDetails> responseEntity = controller.deleteEndpointsByNames(list);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.PRECONDITION_FAILED);
        verify(endpointDeletionService, times(1)).deleteEndpointByNames(any());
    }

    /**
     * Test method for {@link EndpointController#deleteCredentials(List)}
     */
    @Test
    public void testDeleteCredentials() throws MangleException {
        CredentialsSpec spec = credentialsSpecMockData.getAWSCredentialsData();
        ArrayList<String> list = new ArrayList<>();
        list.add(spec.getName());
        when(credentialDeletionService.deleteCredentialsByNames(any())).thenReturn(new DeleteOperationResponse());
        ResponseEntity<ErrorDetails> responseEntity = controller.deleteCredentials(list);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.NO_CONTENT);
        verify(credentialDeletionService, times(1)).deleteCredentialsByNames(any());
    }

    /**
     * Test method for {@link EndpointController#deleteCredentials(List)}
     */
    @Test
    public void testDeleteCredentialsFailedDeletion() throws MangleException {
        CredentialsSpec spec = credentialsSpecMockData.getAWSCredentialsData();
        ArrayList<String> list = new ArrayList<>();
        list.add(spec.getName());

        Map<String, List<String>> associations = new HashMap<>();
        associations.put(spec.getId(), new ArrayList<>());
        associations.put(spec.getId(), new ArrayList<>());

        DeleteOperationResponse response = new DeleteOperationResponse();
        response.setAssociations(associations);

        when(credentialDeletionService.deleteCredentialsByNames(any())).thenReturn(response);
        ResponseEntity<ErrorDetails> responseEntity = controller.deleteCredentials(list);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.PRECONDITION_FAILED);
        verify(credentialDeletionService, times(1)).deleteCredentialsByNames(any());
    }

    /**
     * Test method for {@link EndpointController#testConnection(String)}
     */
    @Test
    public void testTestConnection() throws MangleException {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        when(endpointService.getEndpointByName(anyString())).thenReturn(spec);
        when(endpointService.testEndpointConnection(any())).thenReturn(true);
        ResponseEntity<Resource<EndpointSpec>> responseEntity = controller.testConnection(spec.getName());
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        verify(endpointService, times(1)).getEndpointByName(anyString());
    }

    /**
     * Test method for {@link EndpointController#testConnection(String)}
     */
    @Test(expectedExceptions = MangleException.class)
    public void testTestConnectionException() throws MangleException {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        when(endpointService.getEndpointByName(anyString())).thenReturn(spec);
        when(endpointService.testEndpointConnection(any())).thenReturn(false);
        ResponseEntity<Resource<EndpointSpec>> responseEntity = controller.testConnection(spec.getName());
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        verify(endpointService, times(1)).getEndpointByName(anyString());
    }

    /**
     * Test method for {@link EndpointController#testConnection(String)}
     */
    @Test
    public void testEndpointTestConnectionByName() throws MangleException {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        when(endpointService.getEndpointByName(spec.getName())).thenReturn(spec);
        when(endpointService.testEndpointConnection(any())).thenReturn(true);
        ResponseEntity<Resource<EndpointSpec>> responseEntity = controller.testConnection(spec.getName());
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        verify(endpointService, times(1)).testEndpointConnection(any());
    }

    /**
     * Test method for {@link EndpointController#endpointTestConnection(EndpointSpec)}
     */
    @Test(expectedExceptions = MangleException.class)
    public void testendpointTestConnection() throws MangleException {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        when(endpointService.getEndpointByName(anyString())).thenReturn(spec);
        when(endpointService.testEndpointConnection(any())).thenReturn(false);
        ResponseEntity<Resource<EndpointSpec>> responseEntity = controller.endpointTestConnection(spec);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        verify(endpointService, times(1)).testEndpointConnection(any());
    }

    @Test
    public void testAddCredentialsKubeConfig() throws MangleException, IOException {
        MultipartFile kubeConfig = Mockito.mock(MultipartFile.class);
        CredentialsSpec credentialsSpec = credentialsSpecMockData.getVCenterCredentialsData();
        K8SCredentials k8SCredentials = credentialsSpecMockData.getk8SCredentialsData();
        doNothing().when(credentialService).validateMultipartFileSize(any(), anyInt());
        when(credentialService.addOrUpdateCredentials(any())).thenReturn(k8SCredentials);
        when(credentialService.generateK8SCredentialsSpec(anyString(), anyString(), any())).thenReturn(k8SCredentials);
        ResponseEntity<Resource<CredentialsSpec>> responseEntity =
                controller.addCredentials(credentialsSpec.getId(), kubeConfig, credentialsSpec.getName());
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertNotNull(responseEntity.getBody());
        Assert.assertEquals(responseEntity.getBody().getContent(), k8SCredentials);
        verify(credentialService, times(1)).addOrUpdateCredentials(any());
        verify(credentialService, times(1)).generateK8SCredentialsSpec(anyString(), anyString(), any());
        verify(credentialService, times(1)).validateMultipartFileSize(any(), anyInt());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testAddCredentialsKubeConfigException() throws MangleException, IOException {
        MultipartFile kubeConfig = Mockito.mock(MultipartFile.class);
        CredentialsSpec credentialsSpec = credentialsSpecMockData.getVCenterCredentialsData();

        doThrow(new IOException()).when(credentialService).generateK8SCredentialsSpec(anyString(), anyString(), any());
        try {
            controller.addCredentials(credentialsSpec.getId(), kubeConfig, credentialsSpec.getName());
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.FILE_SIZE_EXCEEDED);
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetCredentialsBasedOnPage() {
        CredentialsSpec spec = credentialsSpecMockData.getAWSCredentialsData();
        List<CredentialsSpec> list = new ArrayList<>();
        list.add(spec);
        Slice<CredentialsSpec> page = Mockito.mock(Slice.class);
        when(credentialService.getCredentialsBasedOnPage(1, 10)).thenReturn(page);
        when(credentialService.getTotalPages(any(Slice.class))).thenReturn(4);
        when(page.getSize()).thenReturn(5);
        when(page.getContent()).thenReturn(list);
        ResponseEntity<List<CredentialsSpec>> responseEntity = controller.getCredentialsBasedOnPage(1, 10);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        verify(credentialService, times(1)).getCredentialsBasedOnPage(anyInt(), anyInt());
        verify(page, times(1)).getContent();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetEndpointsBasedOnPage() {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        List<EndpointSpec> list = new ArrayList<>();
        list.add(spec);
        Slice<EndpointSpec> page = Mockito.mock(Slice.class);
        when(endpointService.getEndpointBasedOnPage(1, 10)).thenReturn(page);
        when(endpointService.getTotalPages(any(Slice.class))).thenReturn(4);
        when(page.getSize()).thenReturn(5);
        when(page.getContent()).thenReturn(list);
        ResponseEntity<List<EndpointSpec>> responseEntity = controller.getEndpointsBasedOnPage(1, 10);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        verify(endpointService, times(1)).getEndpointBasedOnPage(anyInt(), anyInt());
        verify(page, times(1)).getContent();
    }

    @Test
    public void testEndPointTestConnectionMethod() throws MangleException {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        when(endpointService.testEndpointConnection(any())).thenReturn(true);
        ResponseEntity<Resource<EndpointSpec>> responseEntity = controller.endpointTestConnection(spec);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        verify(endpointService, times(1)).testEndpointConnection(any());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testEndpointTestConnectionMethodFailure() throws MangleException {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        when(endpointService.testEndpointConnection(any())).thenReturn(false);

        try {
            controller.endpointTestConnection(spec);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.TEST_CONNECTION_FAILED);
            verify(endpointService, times(1)).testEndpointConnection(any());
            throw e;
        }
    }

    /**
     * Test method for
     * {@link EndpointController#updateCredentials(String, String, String, String, MultipartFile)}
     */
    @Test
    public void testUpdateRemoteMachineCredentials() throws MangleException {
        RemoteMachineCredentials credentialsSpec = credentialsSpecMockData.getRMCredentialsData();
        when(credentialService.updateCredential(any())).thenReturn(credentialsSpec);
        ResponseEntity<CredentialsSpec> responseEntity = controller.updateCredentials(credentialsSpec.getId(),
                credentialsSpec.getName(), credentialsSpec.getUsername(), credentialsSpec.getPassword(), null);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseEntity.getBody(), credentialsSpec);
        verify(credentialService, times(1)).updateCredential(any());
    }

    /**
     * Test method for
     * {@link EndpointController#updateCredentials(String, String, String, String, MultipartFile)}
     *
     * @throws IOException
     */
    @Test
    public void testUpdateRemoteMachineCredentialsForIOException() throws IOException {
        MultipartFile privateKey = Mockito.mock(MultipartFile.class);
        doThrow(new IOException()).when(credentialService).generateRemoteMachineCredentialsSpec(anyString(),
                anyString(), anyString(), anyString(), any());
        RemoteMachineCredentials credentialsSpec = credentialsSpecMockData.getRMCredentialsData();
        try {
            controller.updateCredentials(credentialsSpec.getId(), credentialsSpec.getName(),
                    credentialsSpec.getUsername(), credentialsSpec.getPassword(), privateKey);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.FILE_SIZE_EXCEEDED);
            verify(credentialService, times(1)).generateRemoteMachineCredentialsSpec(anyString(), anyString(),
                    anyString(), anyString(), any());
        }
    }

    /**
     * Test method for {@link EndpointController#updateCredentials(String, MultipartFile, String)}
     */
    @Test
    public void testUpdateK8SCredentials() throws MangleException {
        MultipartFile kubeConfig = Mockito.mock(MultipartFile.class);
        K8SCredentials credentialsSpec = credentialsSpecMockData.getk8SCredentialsData();
        when(credentialService.updateCredential(any())).thenReturn(credentialsSpec);
        ResponseEntity<CredentialsSpec> responseEntity =
                controller.updateCredentials(credentialsSpec.getId(), kubeConfig, credentialsSpec.getName());
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseEntity.getBody(), credentialsSpec);
        verify(credentialService, times(1)).updateCredential(any());
    }

    /**
     * Test method for {@link EndpointController#updateCredentials(String, MultipartFile, String)}
     *
     * @throws IOException
     */
    @Test
    public void testUpdateK8SCredentialsForIOException() throws IOException {
        MultipartFile kubeConfig = Mockito.mock(MultipartFile.class);
        doThrow(new IOException()).when(credentialService).generateK8SCredentialsSpec(anyString(), anyString(), any());
        K8SCredentials credentialsSpec = credentialsSpecMockData.getk8SCredentialsData();
        try {
            controller.updateCredentials(credentialsSpec.getId(), kubeConfig, credentialsSpec.getName());
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.FILE_SIZE_EXCEEDED);
            verify(credentialService, times(1)).generateK8SCredentialsSpec(anyString(), anyString(), any());
        }
    }

    /**
     * Test method for {@link EndpointController#updateCredentials(VCenterCredentials)}
     *
     * @throws MangleException
     */
    @Test
    public void testUpdateVCenterCredentials() throws MangleException {
        VCenterCredentials credentialsSpec = credentialsSpecMockData.getVCenterCredentialsData();
        when(credentialService.updateCredential(any())).thenReturn(credentialsSpec);
        ResponseEntity<CredentialsSpec> responseEntity = controller.updateCredentials(credentialsSpec);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseEntity.getBody(), credentialsSpec);
        verify(credentialService, times(1)).updateCredential(any());
    }

    /**
     * Test method for {@link EndpointController#getAllEndpointCertificates()}
     *
     * @throws MangleException
     */
    @Test
    public void testGetAllEndpointCertificates() throws MangleException {
        List<CertificatesSpec> certificateSpecs = new ArrayList<>();
        when(certificatesService.getAllCertificates()).thenReturn(certificateSpecs);
        ResponseEntity<Resources<CertificatesSpec>> responseEntity = controller.getAllEndpointCertificates();
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseEntity.getBody(), certificateSpecs);
    }

    /**
     * Test method for {@link EndpointController#deleteCertificates(List)}
     *
     * @throws MangleException
     */
    @Test
    public void testDeleteCertificates() throws MangleException {
        DeleteOperationResponse response = new DeleteOperationResponse();
        response.setAssociations(Collections.emptyMap());
        when(certificatesDeletionService.deleteCertificatesByNames(any())).thenReturn(response);
        ResponseEntity<ErrorDetails> responseEntity = controller.deleteCertificates(Collections.emptyList());
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.NO_CONTENT);
        Map<String, List<String>> associations = new HashMap<>();
        associations.put("DummyAssocitation", new ArrayList<>());
        response.setAssociations(associations);
        responseEntity = controller.deleteCertificates(Collections.emptyList());
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.PRECONDITION_FAILED);
    }

    /**
     * Test method for {@link EndpointController#addDockerEndpointCertificates}
     *
     * @throws MangleException
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testAddDockerEndpointCertificates() throws IOException {
        CertificatesSpec certificatesSpec = certificatesSpecMockData.getDockerCertificatesData();
        DeleteOperationResponse response = new DeleteOperationResponse();
        response.setAssociations(Collections.emptyMap());
        try {
            when(certificatesDeletionService.deleteCertificatesByNames(any())).thenReturn(response);
            doNothing().when(certificatesService).validateDockerCertificates(any(), any(), any());
            when(certificatesService.generateDockerCertificatesSpec(any(), any(), any(), any(), any()))
                    .thenReturn(new DockerCertificates());
            when(certificatesService.addOrUpdateCertificates(any())).thenReturn(certificatesSpec);
            ResponseEntity<Resource<CertificatesSpec>> responseEntity = controller.addDockerEndpointCertificates(
                    "DummyCertificateID", "DummyCertificate", Mockito.mock(MultipartFile.class),
                    Mockito.mock(MultipartFile.class), Mockito.mock(MultipartFile.class));
            Assert.assertNotNull(responseEntity.getBody());
            Assert.assertEquals(responseEntity.getBody().getContent(), certificatesSpec);
            when(certificatesService.generateDockerCertificatesSpec(any(), any(), any(), any(), any()))
                    .thenThrow(IOException.class);
            responseEntity = controller.addDockerEndpointCertificates("DummyCertificateID", "DummyCertificate",
                    Mockito.mock(MultipartFile.class), Mockito.mock(MultipartFile.class),
                    Mockito.mock(MultipartFile.class));
            Assert.assertNotNull(responseEntity.getBody());
            Assert.assertEquals(responseEntity.getBody().getContent(), certificatesSpec);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.IO_EXCEPTION);
        }
    }

    /**
     * Test method for {@link EndpointController#updateDockerEndpointCertificates}
     *
     * @throws MangleException
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateDockerEndpointCertificates() throws IOException {
        CertificatesSpec certificatesSpec = certificatesSpecMockData.getDockerCertificatesData();
        DeleteOperationResponse response = new DeleteOperationResponse();
        response.setAssociations(Collections.emptyMap());
        try {
            when(certificatesDeletionService.deleteCertificatesByNames(any())).thenReturn(response);
            doNothing().when(certificatesService).validateDockerCertificates(any(), any(), any());
            when(certificatesService.generateDockerCertificatesSpec(any(), any(), any(), any(), any()))
                    .thenReturn(new DockerCertificates());
            when(certificatesService.updateCertificates(any())).thenReturn(certificatesSpec);
            ResponseEntity<Resource<CertificatesSpec>> responseEntity = controller.updateDockerEndpointCertificates(
                    "DummyCertificateID", "DummyCertificate", Mockito.mock(MultipartFile.class),
                    Mockito.mock(MultipartFile.class), Mockito.mock(MultipartFile.class));
            Assert.assertNotNull(responseEntity.getBody());
            Assert.assertEquals(responseEntity.getBody().getContent(), certificatesSpec);
            when(certificatesService.generateDockerCertificatesSpec(any(), any(), any(), any(), any()))
                    .thenThrow(IOException.class);
            responseEntity = controller.updateDockerEndpointCertificates("DummyCertificateID", "DummyCertificate",
                    Mockito.mock(MultipartFile.class), Mockito.mock(MultipartFile.class),
                    Mockito.mock(MultipartFile.class));
            Assert.assertNotNull(responseEntity.getBody());
            Assert.assertEquals(responseEntity.getBody().getContent(), certificatesSpec);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.IO_EXCEPTION);
        }
    }

    /**
     * Test method for {@link EndpointController#enableEndpoint(List, Boolean, Map)}
     *
     * @throws MangleException
     */
    @Test
    public void testEnableEndpoint() throws MangleException {
        EndpointSpec spec = mockData.getDockerEndpointSpecMock();
        List<EndpointSpec> list = new ArrayList<>();
        list.add(spec);
        List<String> endpointNames = new ArrayList<>();
        endpointNames.add(list.get(0).getName());
        when(endpointService.enableEndpoints(any(), any(), any(Boolean.class))).thenReturn(endpointNames);

        ResponseEntity<Resources<String>> responseEntity = controller.enableEndpoint(endpointNames, false, null);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseEntity.getBody(), endpointNames);
        Assert.assertEquals(
                Objects.requireNonNull(responseEntity.getHeaders().get(CommonConstants.MESSAGE_HEADER)).get(0),
                CommonConstants.ENDPOINTS_DISABLED);
        verify(endpointService, times(1)).enableEndpoints(any(), any(), any(Boolean.class));
        responseEntity = controller.enableEndpoint(endpointNames, true, null);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(
                Objects.requireNonNull(responseEntity.getHeaders().get(CommonConstants.MESSAGE_HEADER)).get(0),
                CommonConstants.ENDPOINTS_ENABLED);
    }


    /**
     * Test method for {@link EndpointController#addDatabaseCredentials(DatabaseCredentials)}
     *
     */
    @Test
    public void testAddCredentialsDatabase() throws MangleException {
        DatabaseCredentials credentialsSpec = credentialsSpecMockData.getDatabaseCredentials();
        when(credentialService.addOrUpdateCredentials(any())).thenReturn(credentialsSpec);
        ResponseEntity<Resource<CredentialsSpec>> responseEntity = controller.addDatabaseCredentials(credentialsSpec);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertNotNull(responseEntity.getBody());
        Assert.assertEquals(responseEntity.getBody().getContent(), credentialsSpec);
    }

    /**
     * Test method for {@link EndpointController#updateDatabaseCredentials(DatabaseCredentials)}
     *
     */
    @Test
    public void testUpdateCredentialsDatabase() throws MangleException {
        DatabaseCredentials credentialsSpec = credentialsSpecMockData.getDatabaseCredentials();
        when(credentialService.updateCredential(any())).thenReturn(credentialsSpec);
        ResponseEntity<Resource<CredentialsSpec>> responseEntity =
                controller.updateDatabaseCredentials(credentialsSpec);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertNotNull(responseEntity.getBody());
        Assert.assertEquals(responseEntity.getBody().getContent(), credentialsSpec);
    }

    /**
     * Test method for {@link EndpointController#deleteEndpointsByNames(List)}
     */
    @Test
    public void testDeleteEndpointsByNamesFailedForEndpointGroupAssociations() throws MangleException {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        ArrayList<String> list = new ArrayList<>();
        list.add(spec.getName());
        Map<String, List<String>> associations = new HashMap<>();
        associations.put(spec.getId(), new ArrayList<>());
        DeleteEndpointOperationResponse response = new DeleteEndpointOperationResponse();
        response.setEndpointGroupAssociations(associations);

        when(endpointDeletionService.deleteEndpointByNames(any())).thenReturn(response);
        ResponseEntity<ErrorDetails> responseEntity = controller.deleteEndpointsByNames(list);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.PRECONDITION_FAILED);
        verify(endpointDeletionService, times(1)).deleteEndpointByNames(any());
    }

    /**
     * Test method for {@link EndpointController#deleteEndpointsByNames(List)}
     */
    @Test
    public void testDeleteEndpointsByNamesFailedForEndpointChildAssociations() throws MangleException {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        ArrayList<String> list = new ArrayList<>();
        list.add(spec.getName());
        Map<String, List<String>> associations = new HashMap<>();
        associations.put(spec.getId(), Arrays.asList("mongo_test", "cass_test"));
        associations.put(spec.getId() + "1", Arrays.asList("mongo_test1"));
        DeleteEndpointOperationResponse response = new DeleteEndpointOperationResponse();
        response.setEndpointChildAssociations(associations);

        when(endpointDeletionService.deleteEndpointByNames(any())).thenReturn(response);
        ResponseEntity<ErrorDetails> responseEntity = controller.deleteEndpointsByNames(list);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.PRECONDITION_FAILED);
        verify(endpointDeletionService, times(1)).deleteEndpointByNames(any());
    }

    /**
     * Test method for {@link EndpointController#getAllServicesByEndpoint(String)}
     */
    @Test
    public void testGetAllK8sResourcesByEndpointName() throws MangleException {
        EndpointSpec spec = mockData.k8sEndpointMockData();
        K8SResource resourceType = K8SResource.DEPLOYMENT;
        String resourceName = "resource1";
        List<String> list = new ArrayList<>();
        list.add(resourceName);
        when(endpointService.getAllResourcesByEndpointName(any(), any())).thenReturn(list);
        ResponseEntity<Resources<String>> response =
                controller.getAllK8sResourcesByEndpointName(spec.getName(), resourceType);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK,
                "Test failed because the Status code is not 200 OK");
        Assert.assertNotNull(response.getBody(), "Test failed because the response body is NULL");
        Collection<String> responseList = response.getBody().getContent();
        Assert.assertEquals(responseList.size(), 1,
                "Test failed because the actual number of resources differs from the expected number of resources");
        Assert.assertEquals(responseList.iterator().next(), resourceName,
                "Test failed because the first element in the actual result differs from the expected value 'resource1'");
        verify(endpointService, atLeast(1)).getAllResourcesByEndpointName(any(), any());

    }
}
