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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.datastax.driver.core.PagingState;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.CredentialService;
import com.vmware.mangle.services.EndpointService;
import com.vmware.mangle.services.mockdata.EndpointMockData;
import com.vmware.mangle.services.repository.EndpointRepository;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Unit Test Case for EndpointService.
 *
 * @author kumargautam
 */
@PowerMockIgnore(value = { "org.apache.logging.log4j.*" })
public class EndpointServiceTest extends PowerMockTestCase {

    private EndpointService endpointService;
    @Mock
    private EndpointRepository repository;
    @Mock
    private CredentialService credentialService;
    @Mock
    private EndpointClientFactory endpointClientFactory;

    private EndpointMockData mockData = new EndpointMockData();
    private EndpointSpec endpointSpec;

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        endpointService = new EndpointService(repository, credentialService, endpointClientFactory);
        this.endpointSpec = mockData.rmEndpointMockData();
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
     * Test method for {@link com.vmware.mangle.service.EndpointService#getAllEndpoints()}.
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
     * Test method for
     * {@link com.vmware.mangle.service.EndpointService#getEndpointByName(java.lang.String)}.
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
     * Test method for
     * {@link com.vmware.mangle.service.EndpointService#getEndpointByName(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetEndpointByNameFailure() throws MangleException {
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
     * Test method for
     * {@link com.vmware.mangle.service.EndpointService#getEndpointByName(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetEndpointByNameFailure1() throws MangleException {
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
     * {@link com.vmware.mangle.service.EndpointService#getAllEndpointByType(com.vmware.mangle.model.enums.EndpointType)}.
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
     * {@link com.vmware.mangle.service.EndpointService#getAllEndpointByType(com.vmware.mangle.model.enums.EndpointType)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetAllEndpointByTypeFailure() throws MangleException {
        List<EndpointSpec> endpointSpecList = new ArrayList<>();
        when(repository.findByEndPointType(any(EndpointType.class))).thenReturn(endpointSpecList);
        boolean actualResult = false;
        try {
            endpointService.getAllEndpointByType(endpointSpec.getEndPointType());
        } catch (Exception e) {
            actualResult = true;
        }
        Assert.assertTrue(actualResult);
        verify(repository, times(1)).findByEndPointType(any(EndpointType.class));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.EndpointService#getAllEndpointByType(com.vmware.mangle.model.enums.EndpointType)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetAllEndpointByTypeFailureWithNull() throws MangleException {
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
     * Test method for
     * {@link com.vmware.mangle.service.EndpointService#updateEndpointByEndpointName(String, EndpointSpec)}
     *
     * @throws MangleException
     */
    @Test
    public void testUpdateEndpointByEndpointName() throws MangleException {
        endpointSpec = mockData.rmEndpointMockData();
        Optional<EndpointSpec> optional = Optional.of(endpointSpec);
        when(repository.findByName(anyString())).thenReturn(optional);
        when(repository.save(any(EndpointSpec.class))).thenReturn(endpointSpec);
        EndpointSpec actualResult =
                endpointService.updateEndpointByEndpointName(endpointSpec.getName(), this.endpointSpec);
        verify(repository, times(1)).findByName(anyString());
        verify(repository, times(1)).save(any(EndpointSpec.class));
        Assert.assertEquals(actualResult, endpointSpec);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.EndpointService#updateEndpointByEndpointName(String, EndpointSpec)}
     * =
     *
     * @throws MangleException
     */
    @Test
    public void testUpdateEndpointByEndpointNameWithNull() throws MangleException {
        endpointSpec = mockData.rmEndpointMockData();
        Optional<EndpointSpec> optional = Optional.of(endpointSpec);
        when(repository.findByName(anyString())).thenReturn(optional);
        when(repository.save(any(EndpointSpec.class))).thenReturn(endpointSpec);
        boolean actualResult = false;
        try {
            endpointService.updateEndpointByEndpointName(null, this.endpointSpec);
        } catch (Exception e) {
            actualResult = true;
        }
        verify(repository, times(0)).findByName(anyString());
        verify(repository, times(0)).save(any(EndpointSpec.class));
        Assert.assertTrue(actualResult);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.EndpointService#updateEndpointByEndpointName(String, EndpointSpec)}
     *
     * @throws MangleException
     */
    @Test
    public void testUpdateEndpointByEndpointNameWithEmpty() throws MangleException {
        endpointSpec = mockData.rmEndpointMockData();
        Optional<EndpointSpec> optional = Optional.empty();
        when(repository.findByName(anyString())).thenReturn(optional);
        when(repository.save(any(EndpointSpec.class))).thenReturn(endpointSpec);
        boolean actualResult = false;
        try {
            endpointService.updateEndpointByEndpointName(endpointSpec.getName(), this.endpointSpec);
        } catch (Exception e) {
            actualResult = true;
        }
        verify(repository, times(1)).findByName(anyString());
        verify(repository, times(0)).save(any(EndpointSpec.class));
        Assert.assertTrue(actualResult);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.EndpointService#addOrUpdateEndpoint(EndpointSpec)}
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
     * Test method for
     * {@link com.vmware.mangle.service.EndpointService#addOrUpdateEndpoint(EndpointSpec)}
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
     * Test method for
     * {@link com.vmware.mangle.service.EndpointService#addOrUpdateEndpoint(EndpointSpec)}
     *
     * @throws MangleException
     */
    @Test
    public void testAddOrUpdateEndpointWithNull() throws MangleException {
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
     * Test method for
     * {@link com.vmware.mangle.service.EndpointService#getEndpointBasedOnPage(int, int)}.
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
     * Test method for
     * {@link com.vmware.mangle.service.EndpointService#getEndpointBasedOnPage(int, int)}.
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
     * Test method for
     * {@link com.vmware.mangle.service.EndpointService#getEndpointBasedOnPage(int, int)}.
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
}
