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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import lombok.extern.log4j.Log4j2;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.security.ADAuthProviderDto;
import com.vmware.mangle.cassandra.model.security.ADAuthProviderDtoV1;
import com.vmware.mangle.model.response.DeleteOperationResponse;
import com.vmware.mangle.model.response.ErrorDetails;
import com.vmware.mangle.services.ADAuthProviderService;
import com.vmware.mangle.services.MappingService;
import com.vmware.mangle.services.UserService;
import com.vmware.mangle.services.config.ADAuthProvider;
import com.vmware.mangle.services.controller.AuthProviderController;
import com.vmware.mangle.services.deletionutils.AuthSourceDeletionService;
import com.vmware.mangle.services.mockdata.AuthProviderMockData;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 *
 *
 * @author chetanc
 */
@Log4j2
public class AuthProviderControllerTest {

    private AuthProviderController authProviderController;

    @Mock
    private ADAuthProviderService adAuthProviderService;

    @Mock
    private AuthSourceDeletionService authSourceDeletionService;

    @Mock
    private ADAuthProvider adAuthProvider;

    @Mock
    private UserService userService;

    @Mock
    private MappingService mappingService;

    @Mock
    private HazelcastInstance hazelcastInstance;

    @Mock
    private DeleteOperationResponse deleteResponse;

    private AuthProviderMockData authProviderMockData = new AuthProviderMockData();

    @BeforeTest
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        authProviderController = spy(new AuthProviderController(adAuthProviderService, adAuthProvider, userService,
                mappingService, authSourceDeletionService));
        Link link = mock(Link.class);
        doReturn(link).when(authProviderController).getSelfLink();
    }

    /**
     * Test method for {@link AuthProviderController#getAllADAuthProviders()}
     */
    @Test
    public void getAllADAuthProvidersTest() throws MangleException {
        log.info("Executing test: getAllADAuthProvidersTest");
        ADAuthProviderDto adAuth = authProviderMockData.getADAuthProviderDto();
        List<ADAuthProviderDto> adAuthProviderDtos = new ArrayList<>();
        adAuthProviderDtos.add(adAuth);
        when(adAuthProviderService.getAllADAuthProvider()).thenReturn(adAuthProviderDtos);
        ResponseEntity<Resources<ADAuthProviderDto>> response = authProviderController.getAllADAuthProviders();
        Resources<ADAuthProviderDto> resources = response.getBody();

        Assert.assertNotNull(resources);
        Assert.assertEquals(resources.getContent().size(), 1);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);

        verify(adAuthProviderService, Mockito.times(1)).getAllADAuthProvider();
    }

    /**
     * Test method for {@link AuthProviderController#updateADAuthProvider(ADAuthProviderDto)} ()}
     */
    @Test(expectedExceptions = MangleException.class)
    public void updateADAuthProviderTestDuplicateFailure() throws MangleException {
        log.info("Executing test: updateADAuthProvider failure; duplicate record");
        ADAuthProviderDto adAuth = authProviderMockData.getADAuthProviderDto();

        when(adAuthProviderService.doesADAuthExists(adAuth)).thenReturn(true);
        when(adAuthProviderService.getADAuthProviderByAdDomain(anyString())).thenReturn(adAuth);
        try {
            authProviderController.updateADAuthProvider(adAuth);
        } catch (MangleException e) {
            verify(adAuthProviderService, Mockito.atLeastOnce()).doesADAuthExists(Mockito.any());
            Assert.assertEquals(e.getErrorCode(), ErrorCode.AUTH_TEST_CONNECTION_FAILED);
            throw e;
        }
    }

    /**
     * Test method for {@link AuthProviderController#updateADAuthProvider(ADAuthProviderDto)} ()}
     */
    @Test(expectedExceptions = MangleException.class)
    public void updateADAuthProviderTestDuplicateFailureV1() throws MangleException {
        log.info("Executing test: updateADAuthProvider failure; duplicate record");
        ADAuthProviderDto adAuth = authProviderMockData.getADAuthProviderDto();
        ADAuthProviderDtoV1 adAuthProviderDtoV1 = authProviderMockData.getADAuthProviderDtoV1();

        when(mappingService.map(adAuthProviderDtoV1, ADAuthProviderDto.class)).thenReturn(adAuth);
        when(adAuthProviderService.doesADAuthExists(adAuth)).thenReturn(true);
        when(adAuthProviderService.getADAuthProviderByAdDomain(anyString())).thenReturn(adAuth);

        try {
            authProviderController.updateADAuthProvider(adAuthProviderDtoV1);
        } catch (MangleException e) {
            verify(adAuthProviderService, Mockito.atLeastOnce()).doesADAuthExists(Mockito.any());
            Assert.assertEquals(e.getErrorCode(), ErrorCode.DUPLICATE_RECORD);
            throw e;
        }
    }

    /**
     * Test method for {@link AuthProviderController#updateADAuthProvider(ADAuthProviderDto)} ()}
     */
    @Test(expectedExceptions = MangleException.class)
    public void updateADAuthProviderTestCommunicationFailure() throws MangleException {
        log.info("Executing test: updateADAuthProvider failure; Test connection failed to AD");
        ADAuthProviderDto adAuth = authProviderMockData.getADAuthProviderDto();
        ADAuthProviderDto adNewAuth = authProviderMockData.getNewADAuthProviderDto();
        Cluster cluster = mock(Cluster.class);
        Member member = mock(Member.class);

        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getLocalMember()).thenReturn(member);
        when(adAuthProviderService.doesADAuthExists(adNewAuth)).thenReturn(false);
        when(adAuthProviderService.getADAuthProviderByAdDomain(adNewAuth.getAdDomain())).thenReturn(adNewAuth);
        when(adAuthProvider.setAdAuthProvider(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
        try {
            authProviderController.updateADAuthProvider(adAuth);
        } catch (MangleException e) {

            Assert.assertEquals(e.getErrorCode(), ErrorCode.AUTH_TEST_CONNECTION_FAILED);
            throw e;
        } finally {
            verify(adAuthProviderService, Mockito.atLeastOnce()).getADAuthProviderByAdDomain(Mockito.anyString());
            verify(adAuthProviderService, Mockito.atLeastOnce()).doesADAuthExists(Mockito.any());
            verify(adAuthProvider, Mockito.atLeastOnce()).setAdAuthProvider(Mockito.anyString(), Mockito.anyString());
        }
    }

    /**
     * Test method for {@link AuthProviderController#updateADAuthProvider(ADAuthProviderDto)} ()}
     */
    @Test(expectedExceptions = MangleException.class)
    public void updateADAuthProviderTestNoRecordFailure() throws MangleException {
        log.info("Executing test: updateADAuthProvider failure; No record found for the given id");
        ADAuthProviderDto adAuth = authProviderMockData.getADAuthProviderDto();
        Cluster cluster = mock(Cluster.class);
        Member member = mock(Member.class);

        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getLocalMember()).thenReturn(member);
        when(adAuthProviderService.doesADAuthExists(adAuth)).thenReturn(false);
        when(adAuthProviderService.getADAuthProviderByAdDomain(adAuth.getAdDomain())).thenReturn(null);
        try {
            authProviderController.updateADAuthProvider(adAuth);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
            throw e;
        } finally {
            verify(adAuthProviderService, Mockito.atLeastOnce()).getADAuthProviderByAdDomain(Mockito.anyString());
            verify(adAuthProviderService, Mockito.atLeastOnce()).doesADAuthExists(Mockito.any());
        }
    }

    /**
     * Test method for {@link AuthProviderController#updateADAuthProvider(ADAuthProviderDto)} ()}
     */
    @Test
    public void updateADAuthProviderV1TestSuccessfull() throws MangleException {
        log.info("Executing test: updateADAuthProvider successful");
        ADAuthProviderDtoV1 adAuthV1 = authProviderMockData.getADAuthProviderDtoV1();
        ADAuthProviderDto adAuth = authProviderMockData.getADAuthProviderDto();
        ADAuthProviderDto adNewAuth = authProviderMockData.getNewADAuthProviderDto();
        Cluster cluster = mock(Cluster.class);
        Member member = mock(Member.class);

        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getLocalMember()).thenReturn(member);
        when(adAuthProviderService.doesADAuthExists(adNewAuth)).thenReturn(false);
        when(adAuthProviderService.getADAuthProviderByAdDomain(anyString())).thenReturn(adNewAuth);
        when(adAuthProviderService.updateADAuthProvider(any(ADAuthProviderDto.class))).thenReturn(adAuth);
        when(adAuthProvider.setAdAuthProvider(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        when(mappingService.map(adAuthV1, ADAuthProviderDto.class)).thenReturn(adAuth);

        ResponseEntity<Resource<ADAuthProviderDto>> response = authProviderController.updateADAuthProvider(adAuthV1);
        Resource<ADAuthProviderDto> resource = response.getBody();

        Assert.assertNotNull(resource);
        Assert.assertEquals(resource.getContent().getId(), adAuth.getId());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    /**
     * Test method for {@link AuthProviderController#updateADAuthProvider(ADAuthProviderDto)} ()}
     */
    @Test
    public void updateADAuthProviderTestSuccessfull() throws MangleException {
        log.info("Executing test: updateADAuthProvider successful");
        ADAuthProviderDto adAuth = authProviderMockData.getADAuthProviderDto();
        ADAuthProviderDto adNewAuth = authProviderMockData.getNewADAuthProviderDto();
        Cluster cluster = mock(Cluster.class);
        Member member = mock(Member.class);

        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getLocalMember()).thenReturn(member);
        when(adAuthProviderService.doesADAuthExists(adNewAuth)).thenReturn(false);
        when(adAuthProviderService.getADAuthProviderByAdDomain(anyString())).thenReturn(adNewAuth);
        when(adAuthProviderService.updateADAuthProvider(any(ADAuthProviderDto.class))).thenReturn(adAuth);
        when(adAuthProvider.setAdAuthProvider(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        ResponseEntity<Resource<ADAuthProviderDto>> response = authProviderController.updateADAuthProvider(adAuth);
        Resource<ADAuthProviderDto> resource = response.getBody();
        Assert.assertNotNull(resource);
        Assert.assertEquals(resource.getContent().getId(), adAuth.getId());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    /**
     * Test method for {@link AuthProviderController#addADAuthProvider(ADAuthProviderDto)} }
     */
    @Test(expectedExceptions = MangleException.class)
    public void addADAuthProviderTestCommunicationFailure() throws MangleException {
        log.info("Executing test: addADAuthProviderTestCommunicationFailure; Test connection failed to AD");
        ADAuthProviderDto adAuth = authProviderMockData.getADAuthProviderDto();

        when(adAuthProviderService.doesADAuthExists(adAuth)).thenReturn(false);
        when(adAuthProviderService.addADAuthProvider(adAuth)).thenReturn(adAuth);
        when(adAuthProvider.setAdAuthProvider(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

        try {
            authProviderController.addADAuthProvider(adAuth);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.AUTH_TEST_CONNECTION_FAILED);
            throw e;
        } finally {
            verify(adAuthProviderService, Mockito.atLeastOnce()).doesADAuthExists(Mockito.any());
            verify(adAuthProviderService, Mockito.times(0)).addADAuthProvider(Mockito.any());
            verify(adAuthProvider, Mockito.atLeastOnce()).setAdAuthProvider(Mockito.anyString(), Mockito.anyString());
        }
    }

    /**
     * Test method for {@link AuthProviderController#addADAuthProvider(ADAuthProviderDto)} ()}
     */
    @Test(expectedExceptions = MangleException.class)
    public void addADAuthProviderTestDuplicateFailure() throws MangleException {
        log.info("Executing test: addADAuthProviderTestDuplicateFailure; duplicate record");
        ADAuthProviderDto adAuth = authProviderMockData.getADAuthProviderDto();
        when(adAuthProviderService.doesADAuthExists(adAuth)).thenReturn(true);
        try {
            authProviderController.addADAuthProvider(adAuth);
        } catch (MangleException e) {
            verify(adAuthProviderService, Mockito.atLeastOnce()).doesADAuthExists(Mockito.any());
            Assert.assertEquals(e.getErrorCode(), ErrorCode.DUPLICATE_RECORD);
            throw e;
        }
    }


    /**
     * Test method for {@link AuthProviderController#updateADAuthProvider(ADAuthProviderDto)} ()}
     */
    @Test
    public void addADAuthProviderTestSuccessfull() throws MangleException {
        log.info("Executing test: addADAuthProviderTestSuccessfull;");
        ADAuthProviderDto adAuth = authProviderMockData.getADAuthProviderDto();
        Cluster cluster = mock(Cluster.class);
        Member member = mock(Member.class);

        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        when(cluster.getLocalMember()).thenReturn(member);
        when(adAuthProviderService.doesADAuthExists(adAuth)).thenReturn(false);
        when(adAuthProviderService.addADAuthProvider(adAuth)).thenReturn(adAuth);
        when(adAuthProvider.setAdAuthProvider(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        ResponseEntity<Resource<ADAuthProviderDto>> response = authProviderController.addADAuthProvider(adAuth);
        Resource<ADAuthProviderDto> resource = response.getBody();

        Assert.assertNotNull(resource);
        Assert.assertEquals(resource.getContent().getId(), adAuth.getId());
        Assert.assertEquals(response.getStatusCode(), HttpStatus.CREATED);
    }


    @Test
    public void removeADAuthProviderSuccessful() throws MangleException {
        log.info("Executing test: removeADAuthProviderSuccessful");
        List<String> authProvidersId = authProviderMockData.getListOfStrings();
        when(authSourceDeletionService.deleteAuthSourceByAuthProviderNames(any()))
                .thenReturn(new DeleteOperationResponse());
        ResponseEntity<ErrorDetails> response = authProviderController.removeADAuthProvider(authProvidersId);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.NO_CONTENT);
        Mockito.verify(authSourceDeletionService, times(1)).deleteAuthSourceByAuthProviderNames(any());
    }

    @Test
    public void removeADAuthProviderUnSuccessful() throws MangleException {
        log.info("Executing test: removeADAuthProviderUnSuccessful");
        List<String> authProvidersId = authProviderMockData.getListOfStrings();
        Map<String, List<String>> associations = new HashMap<>();
        List<String> associationList = new ArrayList();
        associationList.add("authUser");
        associations.put("association", associationList);
        when(deleteResponse.getAssociations()).thenReturn(associations);
        when(authSourceDeletionService.deleteAuthSourceByAuthProviderNames(any())).thenReturn(deleteResponse);
        ResponseEntity<ErrorDetails> response = authProviderController.removeADAuthProvider(authProvidersId);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.PRECONDITION_FAILED);
        Mockito.verify(authSourceDeletionService, times(2)).deleteAuthSourceByAuthProviderNames(any());
        Mockito.verify(deleteResponse, times(2)).getAssociations();
    }

    @Test
    public void testGetAllDomains() throws MangleException {
        log.info("Executing test: testGetAllDomains");
        Set<String> domainNames = new HashSet<>();
        when(adAuthProviderService.getAllDomains()).thenReturn(domainNames);
        when(userService.getDefaultDomainName()).thenReturn(Constants.LOCAL_DOMAIN_NAME);
        ResponseEntity<Resources<String>> responseEntity = authProviderController.getAllDomains();
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Collection<String> content = (Objects.requireNonNull(responseEntity.getBody())).getContent();
        Assert.assertEquals(content.size(), 1);
        verify(userService, times(1)).getDefaultDomainName();
        verify(adAuthProviderService, times(1)).getAllDomains();
    }

    @Test
    public void testTestConnection() throws MangleException {
        log.info("Executing test: testTestConnection");
        ADAuthProviderDto adAuthProviderDto = new ADAuthProviderDto();

        when(adAuthProvider.testConnection(any())).thenReturn(true);

        ResponseEntity<Resource<Boolean>> responseEntity = authProviderController.testConnection(adAuthProviderDto);

        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        verify(adAuthProvider, times(1)).testConnection(any());
    }

}
