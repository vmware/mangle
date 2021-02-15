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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.datastax.driver.core.PagingState;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.core.DockerClientBuilder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.CredentialsSpec;
import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.CredentialService;
import com.vmware.mangle.services.EndpointCertificatesService;
import com.vmware.mangle.services.EndpointService;
import com.vmware.mangle.services.MappingService;
import com.vmware.mangle.services.SchedulerService;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.VCenterAdapterDetailsService;
import com.vmware.mangle.services.mockdata.CredentialsSpecMockData;
import com.vmware.mangle.services.mockdata.EndpointMockData;
import com.vmware.mangle.services.repository.EndpointRepository;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.clients.docker.CustomDockerClient;
import com.vmware.mangle.utils.clients.endpoint.EndpointClient;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Unit Test Case for EndpointService.
 *
 * @author kumargautam
 */
@PrepareForTest(value = { DockerClientBuilder.class, ExposedPort.class, EndpointService.class })
@PowerMockIgnore({ "javax.net.ssl.*,javax.xml.*", "org.xml.sax.*", "org.apache.logging.log4j.*" })
public class EndpointServiceTest extends PowerMockTestCase {

    private EndpointService endpointService;
    @Mock
    private EndpointRepository repository;
    @Mock
    private CredentialService credentialService;
    @Mock
    private EndpointCertificatesService certificatesService;
    @Mock
    private EndpointClientFactory endpointClientFactory;
    @Mock
    private VCenterAdapterDetailsService vcaAdapterService;
    @Mock
    private MappingService mappingService;
    @Mock
    private SchedulerService schedulerService;
    @Mock
    private TaskService taskService;

    private EndpointMockData mockData = new EndpointMockData();
    private EndpointSpec endpointSpec;
    private EndpointSpec dockerEndpointSpec;
    private CredentialsSpecMockData credentialsSpecMockData = new CredentialsSpecMockData();

    @Mock
    private CustomDockerClient customDockerClient;

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(DockerClientBuilder.class);
        PowerMockito.mockStatic(EndpointService.class);
        endpointService = new EndpointService(repository, credentialService, endpointClientFactory, certificatesService,
                mappingService, vcaAdapterService);
        this.endpointSpec = mockData.rmEndpointMockData();
        this.dockerEndpointSpec = mockData.dockerEndpointMockData();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public void tearDownAfterClass() {
        this.endpointSpec = null;
        this.mockData = null;
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterTest
    public void tearDown() {
        validateMockitoUsage();
    }

    /**
     * Test method for {@link EndpointService#getAllEndpoints()}.
     */
    @Test
    public void testGetAllEndpoints() {
        List<EndpointSpec> endpointSpecList = new ArrayList<>();
        endpointSpecList.add(endpointSpec);
        endpointSpecList.add(mockData.k8sEndpointMockData());
        when(repository.findAll()).thenReturn(endpointSpecList);
        List<EndpointSpec> actualResult = endpointService.getAllEndpoints();
        Assert.assertEquals(actualResult.size(), 2);
        verify(repository, times(1)).findAll();
    }

    /**
     * Test method for {@link EndpointService#getEndpointByName(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetEndpointByName() throws MangleException {
        Optional<EndpointSpec> optional = Optional.of(endpointSpec);
        when(repository.findByName(anyString())).thenReturn(optional);
        EndpointSpec actualResult = endpointService.getEndpointByName(endpointSpec.getName());
        verify(repository, times(1)).findByName(anyString());
        Assert.assertEquals(actualResult, endpointSpec);
    }

    /**
     * Test method for {@link EndpointService#getEndpointByName(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetEndpointByNameFailure() {
        Optional<EndpointSpec> optional = Optional.empty();
        when(repository.findByName(anyString())).thenReturn(optional);
        boolean actualResult = false;
        try {
            endpointService.getEndpointByName(endpointSpec.getName());
        } catch (Exception e) {
            actualResult = true;
        }
        verify(repository, times(1)).findByName(anyString());
        Assert.assertTrue(actualResult);
    }

    /**
     * Test method for {@link EndpointService#getEndpointByName(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetEndpointByNameFailure1() {
        Optional<EndpointSpec> optional = Optional.empty();
        when(repository.findByName(anyString())).thenReturn(optional);
        boolean actualResult = false;
        try {
            endpointService.getEndpointByName(null);
        } catch (Exception e) {
            actualResult = true;
        }
        verify(repository, times(0)).findByName(anyString());
        Assert.assertTrue(actualResult);
    }

    /**
     * Test method for
     * {@link EndpointService#getAllEndpointByType(com.vmware.mangle.model.enums.EndpointType)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetAllEndpointByType() throws MangleException {
        List<EndpointSpec> endpointSpecList = new ArrayList<>();
        endpointSpecList.add(endpointSpec);
        endpointSpecList.add(endpointSpec);
        when(repository.findByEndPointType(any(EndpointType.class))).thenReturn(endpointSpecList);
        List<EndpointSpec> actualResult = endpointService.getAllEndpointByType(endpointSpec.getEndPointType());
        Assert.assertEquals(actualResult.size(), 2);
        verify(repository, times(1)).findByEndPointType(any(EndpointType.class));
    }

    /**
     * Test method for
     * {@link EndpointService#getAllEndpointByType(com.vmware.mangle.model.enums.EndpointType)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetAllEndpointByTypeFailureWithNull() {
        List<EndpointSpec> endpointSpecList = new ArrayList<>();
        when(repository.findByEndPointType(any(EndpointType.class))).thenReturn(endpointSpecList);
        boolean actualResult = false;
        try {
            endpointService.getAllEndpointByType(null);
        } catch (Exception e) {
            actualResult = true;
        }
        Assert.assertTrue(actualResult);
        verify(repository, times(0)).findByEndPointType(any(EndpointType.class));
    }

    /**
     * Test method for {@link EndpointService#addOrUpdateEndpoint(EndpointSpec)}
     *
     * @throws MangleException
     */
    @Test
    public void testAddOrUpdateEndpoint() throws MangleException {
        when(repository.save(any(EndpointSpec.class))).thenReturn(endpointSpec);
        Optional<EndpointSpec> optional = Optional.of(endpointSpec);
        when(repository.findByName(anyString())).thenReturn(optional);
        EndpointSpec actualResult = endpointService.addOrUpdateEndpoint(endpointSpec);
        verify(repository, times(1)).save(any(EndpointSpec.class));
        verify(repository, times(1)).findByName(anyString());
        Assert.assertEquals(actualResult, endpointSpec);
    }

    /**
     * Test method for {@link EndpointService#addOrUpdateEndpoint(EndpointSpec)}
     *
     */
    @Test
    public void testAddOrUpdateEndpointWithSameNameAndDifferentType() {
        when(repository.save(any(EndpointSpec.class))).thenReturn(endpointSpec);
        EndpointSpec dockerSpec = mockData.dockerEndpointMockData();
        dockerSpec.setName(endpointSpec.getName());
        Optional<EndpointSpec> optional = Optional.of(dockerSpec);
        when(repository.findByName(anyString())).thenReturn(optional);
        try {
            endpointService.addOrUpdateEndpoint(endpointSpec);
        } catch (MangleException ex) {
            Assert.assertEquals(ex.getErrorCode(), ErrorCode.DUPLICATE_RECORD_FOR_ENDPOINT);
            verify(repository, times(1)).findByName(anyString());
        }
    }

    /**
     * Test method for {@link EndpointService#addOrUpdateEndpoint(EndpointSpec)}
     *
     * @throws MangleException
     */
    @Test
    public void testAddOrUpdateEndpointWithNull() {
        when(repository.save(any(EndpointSpec.class))).thenReturn(endpointSpec);
        boolean actualResult = false;
        try {
            endpointService.addOrUpdateEndpoint(null);
        } catch (Exception e) {
            actualResult = true;
        }
        verify(repository, times(0)).save(any(EndpointSpec.class));
        Assert.assertTrue(actualResult);
    }


    /**
     * Test method for {@link EndpointService#getEndpointBasedOnPage(int, int)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetEndpointBasedOnPage() {
        Slice<EndpointSpec> page = Mockito.mock(Slice.class);
        when(page.getSize()).thenReturn(4);
        when(repository.findAll(any(Pageable.class))).thenReturn(page);
        when(repository.count()).thenReturn(10L);

        Slice<EndpointSpec> actualResult = endpointService.getEndpointBasedOnPage(1, 4);
        Assert.assertEquals(endpointService.getTotalPages(page), 3);
        verify(repository, times(1)).findAll(any(Pageable.class));
        Assert.assertEquals(actualResult.getSize(), 4);
        verify(page, times(3)).getSize();
    }

    /**
     * Test method for {@link EndpointService#getEndpointBasedOnPage(int, int)}.
     */
    @SuppressWarnings("unchecked")
    @Test(description = "Test to get the data from 2 page with size 4")
    public void testGetEndpointBasedOnPageCase1() {
        Slice<EndpointSpec> slice = Mockito.mock(Slice.class);
        when(slice.getSize()).thenReturn(4);
        when(repository.findAll(any(Pageable.class))).thenReturn(slice);
        CassandraPageRequest pageable = Mockito.mock(CassandraPageRequest.class);
        when(slice.getPageable()).thenReturn(pageable);
        when(pageable.getPagingState()).thenReturn(null);
        Slice<EndpointSpec> actualResult = endpointService.getEndpointBasedOnPage(2, 4);
        verify(repository, times(1)).findAll(any(Pageable.class));
        Assert.assertEquals(actualResult.getSize(), 4);
        verify(slice, times(1)).getSize();
    }

    /**
     * Test method for {@link EndpointService#getEndpointBasedOnPage(int, int)}.
     */
    @SuppressWarnings("unchecked")
    @Test(description = "Test to get the data from 3 page with size 4")
    public void testGetEndpointBasedOnPageCase2() {
        Slice<EndpointSpec> slice = Mockito.mock(Slice.class);
        when(slice.getSize()).thenReturn(4);
        when(repository.findAll(any(Pageable.class))).thenReturn(slice);
        CassandraPageRequest pageable = Mockito.mock(CassandraPageRequest.class);
        when(slice.getPageable()).thenReturn(pageable);
        when(pageable.getPageNumber()).thenReturn(3);
        when(pageable.getPageSize()).thenReturn(4);
        PagingState pagingState = Mockito.mock(PagingState.class);
        when(pageable.getPagingState()).thenReturn(pagingState);
        Slice<EndpointSpec> actualResult = endpointService.getEndpointBasedOnPage(3, 4);
        verify(repository, times(3)).findAll(any(Pageable.class));
        Assert.assertEquals(actualResult.getSize(), 4);
        verify(slice, times(1)).getSize();
    }

    /**
     * Test method for {@link EndpointService#testEndpointConnection(EndpointSpec)}
     *
     */
    @Test
    public void testTestEndpointConnectionWithNullCredentialsName() {
        EndpointSpec endpointSpecV11 = mockData.rmEndpointMockData();
        endpointSpecV11.setCredentialsName(null);
        try {
            endpointService.testEndpointConnection(endpointSpecV11);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
        }
    }

    /**
     * Test method for {@link EndpointService#testEndpointConnection(EndpointSpec)}
     *
     * @throws MangleException
     *
     */
    @Test
    public void testTestEndpointConnectionForDocker() throws MangleException {
        EndpointSpec endpointSpecV11 = mockData.dockerEndpointMockData();
        EndpointClient client = Mockito.mock(EndpointClient.class);
        when(client.testConnection()).thenReturn(true);
        when(endpointClientFactory.getEndPointClient(any(CredentialsSpec.class), any(EndpointSpec.class)))
                .thenReturn(client);
        assertTrue(endpointService.testEndpointConnection(endpointSpecV11));
        verify(endpointClientFactory, times(1)).getEndPointClient(any(CredentialsSpec.class), any(EndpointSpec.class));
    }

    /**
     * Test method for {@link EndpointService#testEndpointConnection(EndpointSpec)}
     *
     * @throws MangleException
     *
     */
    @Test
    public void testTestEndpointConnectionForRedis() throws MangleException {
        EndpointSpec endpointSpecV11 = mockData.getRedisProxyEndpointMockData();
        EndpointClient client = Mockito.mock(EndpointClient.class);
        when(client.testConnection()).thenReturn(true);
        when(endpointClientFactory.getEndPointClient(any(CredentialsSpec.class), any(EndpointSpec.class)))
                .thenReturn(client);
        assertTrue(endpointService.testEndpointConnection(endpointSpecV11));
        verify(endpointClientFactory, times(1)).getEndPointClient(any(CredentialsSpec.class), any(EndpointSpec.class));
    }

    /**
     * Test method for {@link EndpointService#testEndpointConnection(EndpointSpec)}
     *
     * @throws MangleException
     *
     */
    @Test
    public void testTestEndpointConnectionForRemoteMachine() throws MangleException {
        when(credentialService.getCredentialByName(anyString()))
                .thenReturn(credentialsSpecMockData.getRMCredentialsData());
        EndpointClient client = Mockito.mock(EndpointClient.class);
        when(client.testConnection()).thenReturn(true);
        when(endpointClientFactory.getEndPointClient(any(CredentialsSpec.class), any(EndpointSpec.class)))
                .thenReturn(client);
        assertTrue(endpointService.testEndpointConnection(endpointSpec));
        verify(endpointClientFactory, times(1)).getEndPointClient(any(CredentialsSpec.class), any(EndpointSpec.class));
        verify(credentialService, times(1)).getCredentialByName(anyString());
    }

    /**
     * Test method for {@link EndpointService#testEndpointConnection(EndpointSpec)}
     *
     */
    @Test
    public void testTestEndpointConnectionWithNullConnectionProperties() {
        EndpointSpec endpointSpecV11 = mockData.rmEndpointMockData();
        endpointSpecV11.setRemoteMachineConnectionProperties(null);
        try {
            endpointService.testEndpointConnection(endpointSpecV11);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.PROVIDE_CONNECTION_PROPERTIES_FOR_ENDPOINT);
        }
    }

    /**
     * Test method for {@link EndpointService#testEndpointConnection(EndpointSpec)}
     *
     * @throws MangleException
     *
     */
    @Test
    public void testTestEndpointConnectionForVcenter() throws MangleException {
        when(credentialService.getCredentialByName(anyString()))
                .thenReturn(credentialsSpecMockData.getVCenterCredentialsData());
        EndpointClient client = Mockito.mock(EndpointClient.class);
        when(client.testConnection()).thenReturn(true);
        when(endpointClientFactory.getEndPointClient(any(CredentialsSpec.class), any(EndpointSpec.class)))
                .thenReturn(client);
        assertTrue(endpointService.testEndpointConnection(mockData.getVCenterEndpointSpecMock()));
        verify(endpointClientFactory, times(1)).getEndPointClient(any(CredentialsSpec.class), any(EndpointSpec.class));
        verify(credentialService, times(1)).getCredentialByName(anyString());
    }

    /**
     * Test method for {@link EndpointService#testEndpointConnection(EndpointSpec)}
     *
     * @throws MangleException
     *
     */
    @Test
    public void testTestEndpointConnectionForDatabase() throws MangleException {
        when(credentialService.getCredentialByName(anyString()))
                .thenReturn(credentialsSpecMockData.getDatabaseCredentials());
        EndpointSpec endpointSpecV11 = mockData.getDatabaseEndpointSpec();
        EndpointClient client = Mockito.mock(EndpointClient.class);
        when(client.testConnection()).thenReturn(true);
        when(endpointClientFactory.getEndPointClient(any(CredentialsSpec.class), any(EndpointSpec.class)))
                .thenReturn(client);
        assertTrue(endpointService.testEndpointConnection(endpointSpecV11));
        verify(endpointClientFactory, times(1)).getEndPointClient(any(CredentialsSpec.class), any(EndpointSpec.class));
        verify(credentialService, times(1)).getCredentialByName(anyString());
    }

    /**
     * Test method for {@link EndpointService#preProcessVCenterEndpointSpec(vCenterEndpointSpec)}
     *
     * @throws MangleException
     *
     */
    @Test
    public void testpreProcessVCenterEndpointSpec_Throws_Exception() throws MangleException {
        when(vcaAdapterService.getVCAdapterDetailsByName("VCAdapterName")).thenReturn(null);
        try {
            endpointService.preProcessVCenterEndpointSpec(mockData.getVCenterEndpointSpecMock());
        } catch (MangleException e) {
            verify(vcaAdapterService, times(1)).getVCAdapterDetailsByName(anyString());
            assertThrows(MangleException.class,
                    () -> endpointService.preProcessVCenterEndpointSpec(mockData.getVCenterEndpointSpecMock()));
        }
    }

    /**
     * Test method for {@link EndpointService#testEndpointConnection(EndpointSpec)}
     *
     * @throws MangleException
     *
     */
    @Test
    public void testTestEndpointConnectionForK8S() throws MangleException {
        when(credentialService.getCredentialByName(anyString()))
                .thenReturn(credentialsSpecMockData.getk8SCredentialsData());
        EndpointClient client = Mockito.mock(EndpointClient.class);
        when(client.testConnection()).thenReturn(true);
        when(endpointClientFactory.getEndPointClient(any(CredentialsSpec.class), any(EndpointSpec.class)))
                .thenReturn(client);
        assertTrue(endpointService.testEndpointConnection(mockData.k8sEndpointMockData()));
        verify(endpointClientFactory, times(1)).getEndPointClient(any(CredentialsSpec.class), any(EndpointSpec.class));
        verify(credentialService, times(1)).getCredentialByName(anyString());
    }

    /**
     * Test method for {@link EndpointService#getAllContainersByEndpointName(java.lang.String)}.
     *
     * Description: Positive test case which will take the DockerEndpointName and gives the list of
     * containers present in Docker Host.
     *
     */
    @Test
    public void testgetAllContainersByEndpointName() throws Exception {
        Optional<EndpointSpec> optional = Optional.of(dockerEndpointSpec);
        List<String> allContainers = new ArrayList<>();
        allContainers.add("mangle");

        when(repository.findByName(anyString())).thenReturn(optional);
        PowerMockito.whenNew(CustomDockerClient.class).withAnyArguments().thenReturn(customDockerClient);
        when(customDockerClient.getAllContainerNames()).thenReturn(allContainers);

        List<String> actualResult = endpointService.getAllContainersByEndpointName(dockerEndpointSpec.getName());

        verify(repository, times(1)).findByName(anyString());
        Assert.assertEquals(actualResult.size(), 1);
        Assert.assertEquals(actualResult, allContainers);
        Assert.assertEquals(actualResult.get(0), "mangle");
    }

    /**
     * Test method for {@link EndpointService#getAllContainersByEndpointName(java.lang.String)}.
     *
     * Description: Test case to validate if the EndpointSpec returned is not the DockerEndpoint.
     *
     */
    @Test
    public void testgetAllContainersByEndpointName_InvalidDockerEP() throws Exception {
        Optional<EndpointSpec> optional = Optional.of(endpointSpec);

        when(repository.findByName(anyString())).thenReturn(optional);
        PowerMockito.whenNew(CustomDockerClient.class).withAnyArguments().thenReturn(customDockerClient);
        try {
            endpointService.getAllContainersByEndpointName(dockerEndpointSpec.getName());
        } catch (MangleException exception) {
            verify(repository, times(1)).findByName(anyString());
            Assert.assertEquals("Invalid DockerEndpoint", exception.getMessage());
        }
    }

    /**
     * Test method for {@link EndpointService#getAllContainersByEndpointName(java.lang.String)}.
     *
     * Description: Test case to validate the result when the EndpointName is Empty/Null.
     *
     */
    @Test
    public void testgetAllContainersByEndpointName_EPNameEmpty() throws Exception {
        Optional<EndpointSpec> optional = Optional.of(endpointSpec);
        when(repository.findByName(anyString())).thenReturn(optional);
        PowerMockito.whenNew(CustomDockerClient.class).withAnyArguments().thenReturn(customDockerClient);
        List<String> actualResult = endpointService.getAllContainersByEndpointName("");
        verify(repository, times(0)).findByName(anyString());
        Assert.assertEquals(actualResult, Collections.emptyList());
    }

    /**
     * Test method for {@link EndpointService#getAllContainersByEndpointName(java.lang.String)}.
     *
     * Description: Test case to validate the result when the EndpointSpec returned from Repository
     * is Null/Empty.
     *
     */
    @Test
    public void testgetAllContainersByEndpointName_NoRecordFound() throws Exception {
        Optional<EndpointSpec> optional = Optional.empty();

        when(repository.findByName(anyString())).thenReturn(optional);
        PowerMockito.whenNew(CustomDockerClient.class).withAnyArguments().thenReturn(customDockerClient);
        try {
            endpointService.getAllContainersByEndpointName(dockerEndpointSpec.getName());
        } catch (MangleException exception) {
            verify(repository, times(1)).findByName(anyString());
            Assert.assertEquals("Found No Search Results", exception.getMessage());
        }
    }

    /**
     * Test method for {@link EndpointService#enableEndpoints(List, Map, boolean)}
     *
     * Description: Test case to validate enable and disable endpoint for fault injection when only
     * list of names passed as filters
     *
     */
    @Test
    public void testEnableEndpointsWithNames() {
        List<EndpointSpec> endpointSpecList = new ArrayList<>();
        endpointSpecList.add(endpointSpec);
        endpointSpecList.add(mockData.k8sEndpointMockData());
        List<String> endpointNames = new ArrayList<>();
        endpointNames.add(endpointSpecList.get(0).getName());
        endpointNames.add(endpointSpecList.get(1).getName());
        when(repository.findAll()).thenReturn(endpointSpecList);
        when(repository.findByNames(any())).thenReturn(endpointSpecList);
        when(repository.saveAll(any())).thenReturn(endpointSpecList);
        try {
            List<String> updatedEndpoints = endpointService.enableEndpoints(endpointNames, null, false);
            Assert.assertEquals(updatedEndpoints, endpointNames,
                    "test enableEndpoint method with only names as filter is failed");
            verify(repository, times(0)).findAll();
            verify(repository, times(1)).findByNames(any());
        } catch (MangleException exception) {
            Assert.fail("test enableEndpoint method is failed with exception" + exception.getMessage());
        }
    }

    /**
     * Test method for {@link EndpointService#enableEndpoints(List, Map, boolean)}
     *
     * Description: Test case to validate enable and disable endpoint for fault injection when only
     * tags passed as filter
     *
     */
    @Test
    public void testEnableEndpointsWithTags() {
        Map<String, String> tags = new HashMap<>();
        tags.put("ip", endpointSpec.getRemoteMachineConnectionProperties().getHost());
        endpointSpec.setTags(tags);
        List<EndpointSpec> endpointSpecList = new ArrayList<>();
        endpointSpecList.add(endpointSpec);
        List<String> endpointNames = new ArrayList<>();
        endpointNames.add(endpointSpecList.get(0).getName());
        when(repository.findAll()).thenReturn(endpointSpecList);
        when(repository.findByNames(any())).thenReturn(endpointSpecList);
        when(repository.saveAll(any())).thenReturn(endpointSpecList);
        try {
            List<String> updatedEndpoints = endpointService.enableEndpoints(null, tags, false);
            Assert.assertEquals(updatedEndpoints, endpointNames,
                    "test enableEndpoint method with only tags as filter is failed");
            verify(repository, times(1)).findAll();
            verify(repository, times(0)).findByNames(any());
        } catch (MangleException exception) {
            Assert.fail("test enableEndpoint method is failed with exception" + exception.getMessage());
        }
    }

    /**
     * Test method for {@link EndpointService#enableEndpoints(List, Map, boolean)}
     *
     * Description: Test case to validate enable and disable endpoint for fault injection when both
     * names and tags sent as filters
     *
     */
    @Test
    public void testEnableEndpointsWithBothTagsAndNames() {
        List<EndpointSpec> endpointSpecList = new ArrayList<>();
        endpointSpecList.add(endpointSpec);
        List<String> endpointNames = new ArrayList<>();
        endpointNames.add(endpointSpecList.get(0).getName());
        when(repository.findAll()).thenReturn(endpointSpecList);
        when(repository.findByNames(any())).thenReturn(endpointSpecList);
        when(repository.saveAll(any())).thenReturn(endpointSpecList);
        try {
            List<String> updatedEndpoints =
                    endpointService.enableEndpoints(endpointNames, endpointSpecList.get(0).getTags(), false);
            Assert.assertEquals(updatedEndpoints, endpointNames,
                    "test enableEndpoint method with both names and tags as filters is failed");
            verify(repository, times(0)).findAll();
            verify(repository, times(1)).findByNames(any());
        } catch (MangleException exception) {
            Assert.fail("test enableEndpoint method is failed with exception" + exception.getMessage());
        }
    }

    /**
     * Test method for {@link EndpointService#enableEndpoints(List, Map, boolean)}
     *
     * Description: Test case to validate enable and disable endpoint for fault injection when both
     * names and tags not sent as filters
     *
     */
    @Test
    public void testEnableEndpointsWithoutTagsAndNames() {
        List<EndpointSpec> endpointSpecList = new ArrayList<>();
        endpointSpecList.add(endpointSpec);
        List<String> endpointNames = new ArrayList<>();
        endpointNames.add(endpointSpecList.get(0).getName());
        when(repository.findAll()).thenReturn(endpointSpecList);
        when(repository.findByNames(any())).thenReturn(endpointSpecList);
        when(repository.saveAll(any())).thenReturn(endpointSpecList);
        try {
            List<String> updatedEndpoints = endpointService.enableEndpoints(null, null, false);
            Assert.assertEquals(updatedEndpoints, endpointNames,
                    "test enableEndpoint method without names and tags as filters is failed");
            verify(repository, times(1)).findAll();
            verify(repository, times(0)).findByNames(any());
        } catch (MangleException exception) {
            Assert.fail("test enableEndpoint method is failed with exception" + exception.getMessage());
        }

        try {
            when(repository.findAll()).thenReturn(new ArrayList<>());
            endpointService.enableEndpoints(null, null, false);
            Assert.fail("test enableEndpoint failed to throw exception whe no endpoints found");
        } catch (MangleException exception) {
            Assert.assertEquals(exception.getErrorCode(), ErrorCode.NO_ENDPOINTS_FOUND,
                    "Failed to throw expection eexception wtih error code:" + ErrorCode.NO_ENDPOINTS_FOUND);
        }
    }

    /**
     * Test method for {@link EndpointService#preProcessDatabaseEndpointSpec(EndpointSpec)}.
     *
     * @throws MangleException
     */
    @Test
    public void testPreProcessDatabaseEndpointSpec() throws MangleException {
        EndpointSpec endpointSpec = mockData.getDatabaseEndpointSpec();
        when(repository.findByName(anyString())).thenReturn(Optional.of(endpointSpec));
        assertEquals(endpointService.preProcessDatabaseEndpointSpec(endpointSpec), endpointSpec);
        verify(repository, times(1)).findByName(anyString());
    }
}
