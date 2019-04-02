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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.stubbing.answers.ReturnsArgumentAt;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.AWSCredentials;
import com.vmware.mangle.cassandra.model.endpoint.CredentialsSpec;
import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.endpoint.K8SCredentials;
import com.vmware.mangle.cassandra.model.endpoint.RemoteMachineCredentials;
import com.vmware.mangle.cassandra.model.endpoint.VCenterCredentials;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.model.enums.OperationStatus;
import com.vmware.mangle.model.response.DeleteOperationResponse;
import com.vmware.mangle.services.CredentialService;
import com.vmware.mangle.services.EndpointService;
import com.vmware.mangle.services.EventService;
import com.vmware.mangle.services.controller.EndpointController;
import com.vmware.mangle.services.deletionutils.CredentialDeletionService;
import com.vmware.mangle.services.deletionutils.EndpointDeletionService;
import com.vmware.mangle.services.events.web.CustomEventPublisher;
import com.vmware.mangle.services.mockdata.CredentialsSpecMockData;
import com.vmware.mangle.services.mockdata.EndpointMockData;
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


    @InjectMocks
    private CustomEventPublisher publisher;

    @Mock
    private EventService eventService;

    @Mock
    private EndpointDeletionService endpointDeletionService;

    @Mock
    private CredentialDeletionService credentialDeletionService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;


    private EndpointController controller;

    private EndpointMockData mockData = new EndpointMockData();
    private CredentialsSpecMockData credentialsSpecMockData = new CredentialsSpecMockData();


    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        publisher = new CustomEventPublisher(applicationEventPublisher, eventService);
        controller = new EndpointController(endpointService, credentialService, publisher, endpointDeletionService,
                credentialDeletionService);
        when(eventService.save(any())).then(new ReturnsArgumentAt(0));
    }

    @Test
    public void getEndpointsTest() {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        List<EndpointSpec> list = new ArrayList<>(Arrays.asList(spec));
        when(endpointService.getAllEndpoints()).thenReturn(list);
        ResponseEntity<List<EndpointSpec>> response = controller.getEndpoints("");
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        List<EndpointSpec> responseList = response.getBody();
        Assert.assertEquals(responseList.size(), 1);
        Assert.assertEquals(responseList.get(0), spec);
        verify(endpointService, atLeast(1)).getAllEndpoints();
    }

    @Test
    public void getEndpointsTestByCredentials() {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        VCenterCredentials credentials = credentialsSpecMockData.getVCenterCredentialsData();

        List<EndpointSpec> list = new ArrayList<>(Arrays.asList(spec));
        when(endpointService.getEndpointsSpecByCredentialName(credentials.getName())).thenReturn(list);
        ResponseEntity<List<EndpointSpec>> response = controller.getEndpoints(credentials.getName());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        List<EndpointSpec> responseList = response.getBody();
        Assert.assertEquals(responseList.size(), 1);
        Assert.assertEquals(responseList.get(0), spec);
        verify(endpointService, atLeast(1)).getEndpointsSpecByCredentialName(anyString());
    }

    @Test
    public void getEndpointByNameTest() throws MangleException {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        when(endpointService.getEndpointByName(anyString())).thenReturn(spec);
        ResponseEntity<EndpointSpec> response = controller.getEndpointByName(spec.getName());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(spec, response.getBody());
        verify(endpointService, atLeast(1)).getEndpointByName(anyString());
    }

    @Test
    public void getAllEndpointByTypeTest() throws MangleException {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        List<EndpointSpec> list = new ArrayList<>(Arrays.asList(spec));
        when(endpointService.getAllEndpointByType(any())).thenReturn(list);
        ResponseEntity<List<EndpointSpec>> response = controller.getAllEndpointByType(EndpointType.WAVEFRONT);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        List<EndpointSpec> responseList = response.getBody();
        Assert.assertEquals(responseList.size(), 1);
        Assert.assertEquals(responseList.get(0), spec);
        verify(endpointService, atLeast(1)).getAllEndpointByType(any());
    }

    @Test
    public void updateEndpointTest() throws MangleException {
        EndpointSpec spec1 = mockData.getVCenterEndpointSpecMock();
        when(endpointService.updateEndpointByEndpointName(anyString(), any())).thenReturn(spec1);
        when(endpointService.testEndpointConnection(any())).thenReturn(true);
        ResponseEntity<EndpointSpec> response = controller.updateEndpoint(spec1);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(), spec1);
        verify(endpointService, atLeast(1)).updateEndpointByEndpointName(anyString(), any());
    }

    @Test(expectedExceptions = MangleException.class)
    public void updateEndpointTestFailure() throws MangleException {
        EndpointSpec spec1 = mockData.getVCenterEndpointSpecMock();
        when(endpointService.updateEndpointByEndpointName(anyString(), any())).thenReturn(spec1);
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
    public void getCredentialsTest() {
        CredentialsSpec credentialsSpec = credentialsSpecMockData.getVCenterCredentialsData();
        List<CredentialsSpec> list = new ArrayList<>(Arrays.asList(credentialsSpec));
        when(credentialService.getAllCredentials()).thenReturn(list);
        ResponseEntity<List<CredentialsSpec>> response = controller.getCredentials();
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        List<CredentialsSpec> responseList = response.getBody();
        Assert.assertEquals(responseList.size(), 1);
        Assert.assertEquals(responseList.get(0), credentialsSpec);
    }


    /**
     * Test method for
     * {@link com.vmware.mangle.controller.EndpointController#addEndpoint(EndpointSpec)}
     *
     */
    @Test
    public void testAddEndPoint() throws MangleException {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        when(endpointService.testEndpointConnection(spec)).thenReturn(true);
        when(endpointService.addOrUpdateEndpoint(spec)).thenReturn(spec);
        ResponseEntity<EndpointSpec> responseEntity = controller.addEndpoint(spec);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseEntity.getBody(), spec);
    }


    /**
     * Test method for
     * {@link com.vmware.mangle.controller.EndpointController#addEndpoint(EndpointSpec)}
     *
     */
    @Test(expectedExceptions = MangleException.class)
    public void testAddEndPoint1() throws MangleException {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
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
     * {@link com.vmware.mangle.controller.EndpointController#addCredentials(String, String, String, String, MultipartFile)}
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
        ResponseEntity<CredentialsSpec> responseEntity =
                controller.addCredentials(spec.getId(), spec.getName(), username, pwd, privateKey);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        verify(credentialService, times(1)).addOrUpdateCredentials(any());
        verify(credentialService, times(1)).generateRemoteMachineCredentialsSpec(anyString(), anyString(), anyString(),
                anyString(), any());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.controller.EndpointController#addCredentials(String, String, String, String, MultipartFile)}
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
     * Test method for
     * {@link com.vmware.mangle.controller.EndpointController#addCredentials(AWSCredentials)}
     */
    @Test
    public void testAddCredentialsAWS() throws MangleException {
        CredentialsSpec credentialsSpec = credentialsSpecMockData.getAWSCredentialsData();
        when(credentialService.addOrUpdateCredentials(any())).thenReturn(credentialsSpec);
        ResponseEntity<CredentialsSpec> responseEntity = controller.addCredentials((AWSCredentials) credentialsSpec);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseEntity.getBody(), credentialsSpec);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.controller.EndpointController#addCredentials(VCenterCredentials)}
     *
     */
    @Test
    public void testAddCredentialsVCenter() throws MangleException {
        CredentialsSpec credentialsSpec = credentialsSpecMockData.getVCenterCredentialsData();
        when(credentialService.addOrUpdateCredentials(any())).thenReturn(credentialsSpec);
        ResponseEntity<CredentialsSpec> responseEntity =
                controller.addCredentials((VCenterCredentials) credentialsSpec);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseEntity.getBody(), credentialsSpec);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.controller.EndpointController#deleteEndpointsByNames(List)}
     */
    @Test
    public void testDeleteEndpointsByNames() throws MangleException {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        ArrayList<String> list = new ArrayList<>(Arrays.asList(spec.getName()));
        when(endpointDeletionService.deleteEndpointByNames(any())).thenReturn(new DeleteOperationResponse());
        ResponseEntity<DeleteOperationResponse> responseEntity = controller.deleteEndpointsByNames(list);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseEntity.getBody().getResult(), OperationStatus.SUCCESS);
        verify(endpointDeletionService, times(1)).deleteEndpointByNames(any());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.controller.EndpointController#deleteEndpointsByNames(List)}
     */
    @Test
    public void testDeleteEndpointsByNamesFailed() throws MangleException {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        ArrayList<String> list = new ArrayList<>(Arrays.asList(spec.getName()));
        Map<String, List<String>> associations = new HashMap<>();
        associations.put(spec.getId(), new ArrayList<>());
        DeleteOperationResponse response = new DeleteOperationResponse();
        response.setAssociations(associations);


        when(endpointDeletionService.deleteEndpointByNames(any())).thenReturn(response);
        ResponseEntity<DeleteOperationResponse> responseEntity = controller.deleteEndpointsByNames(list);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.PRECONDITION_FAILED);
        Assert.assertEquals(responseEntity.getBody().getResult(), OperationStatus.FAILED);
        verify(endpointDeletionService, times(1)).deleteEndpointByNames(any());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.controller.EndpointController#deleteCredentials(List)}
     */
    @Test
    public void testDeleteCredentials() throws MangleException {
        CredentialsSpec spec = credentialsSpecMockData.getAWSCredentialsData();
        ArrayList<String> list = new ArrayList<>(Arrays.asList(spec.getName()));
        when(credentialDeletionService.deleteCredentialsByNames(any())).thenReturn(new DeleteOperationResponse());
        ResponseEntity<DeleteOperationResponse> responseEntity = controller.deleteCredentials(list);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        verify(credentialDeletionService, times(1)).deleteCredentialsByNames(any());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.controller.EndpointController#deleteCredentials(List)}
     */
    @Test
    public void testDeleteCredentialsFailedDeletion() throws MangleException {
        CredentialsSpec spec = credentialsSpecMockData.getAWSCredentialsData();
        ArrayList<String> list = new ArrayList<>(Arrays.asList(spec.getName()));

        Map<String, List<String>> associations = new HashMap<>();
        associations.put(spec.getId(), new ArrayList<>());
        associations.put(spec.getId(), new ArrayList<>());

        DeleteOperationResponse response = new DeleteOperationResponse();
        response.setAssociations(associations);

        when(credentialDeletionService.deleteCredentialsByNames(any())).thenReturn(response);
        ResponseEntity<DeleteOperationResponse> responseEntity = controller.deleteCredentials(list);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.PRECONDITION_FAILED);
        verify(credentialDeletionService, times(1)).deleteCredentialsByNames(any());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.controller.EndpointController#testConnection(String)}
     */
    @Test
    public void testTestConnection() throws MangleException {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        when(endpointService.getEndpointByName(anyString())).thenReturn(spec);
        when(endpointService.testEndpointConnection(any())).thenReturn(true);
        ResponseEntity<EndpointSpec> responseEntity = controller.testConnection(spec.getName());
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        verify(endpointService, times(1)).getEndpointByName(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.controller.EndpointController#testConnection(String)}
     */
    @Test(expectedExceptions = MangleException.class)
    public void testTestConnectionException() throws MangleException {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        when(endpointService.getEndpointByName(anyString())).thenReturn(spec);
        when(endpointService.testEndpointConnection(any())).thenReturn(false);
        ResponseEntity<EndpointSpec> responseEntity = controller.testConnection(spec.getName());
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        verify(endpointService, times(1)).getEndpointByName(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.controller.EndpointController#testConnection(EndpointSpec)}
     */
    @Test
    public void testEndpointTestConnectionByName() throws MangleException {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        when(endpointService.testEndpointConnection(any())).thenReturn(true);
        ResponseEntity<EndpointSpec> responseEntity = controller.testConnection(spec.getName());
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        verify(endpointService, times(1)).testEndpointConnection(any());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.controller.EndpointController#endpointTestConnection(EndpointSpec)}
     */
    @Test(expectedExceptions = MangleException.class)
    public void testendpointTestConnection() throws MangleException {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        when(endpointService.getEndpointByName(anyString())).thenReturn(spec);
        when(endpointService.testEndpointConnection(any())).thenReturn(false);
        ResponseEntity<EndpointSpec> responseEntity = controller.endpointTestConnection(spec);
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
        ResponseEntity<CredentialsSpec> responseEntity =
                controller.addCredentials(credentialsSpec.getId(), kubeConfig, credentialsSpec.getName());
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(responseEntity.getBody(), k8SCredentials);
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

    @Test
    public void testGetCredentialsBasedOnPage() {
        CredentialsSpec spec = credentialsSpecMockData.getAWSCredentialsData();
        List<CredentialsSpec> list = new ArrayList<>(Arrays.asList(spec));
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

    @Test
    public void testGetEndpointsBasedOnPage() {
        EndpointSpec spec = mockData.getVCenterEndpointSpecMock();
        List<EndpointSpec> list = new ArrayList<>(Arrays.asList(spec));
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
        ResponseEntity<EndpointSpec> responseEntity = controller.endpointTestConnection(spec);
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
}
