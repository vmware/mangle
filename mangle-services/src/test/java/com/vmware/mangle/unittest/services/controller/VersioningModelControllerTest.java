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
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.Fault;
import com.vmware.mangle.model.FaultV0;
import com.vmware.mangle.services.FaultService;
import com.vmware.mangle.services.MappingService;
import com.vmware.mangle.services.controller.VersioningModelController;
import com.vmware.mangle.services.mockdata.FaultServiceMockData;
import com.vmware.mangle.services.repository.FaultRepository;

/**
 * Test class for testing version modelling related code
 *
 * @author chetanc
 */
@Log4j2
public class VersioningModelControllerTest {

    @Mock
    private FaultRepository repository;

    private FaultService service;
    private VersioningModelController controller;
    private FaultServiceMockData mockData = new FaultServiceMockData();
    private MappingService mappingService = new MappingService();

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        service = new FaultService(repository);
        controller = new VersioningModelController(service, mappingService);
    }

    /**
     * Test method for {@link VersioningModelController#getAllFaults()}
     */
    @Test
    public void getAllFaultsTest() {
        log.info("Testing getAllFaults method on the versionModelController");
        List<Fault> faults = mockData.getDummyFaultsList();
        when(repository.findAll()).thenReturn(faults);
        ResponseEntity responseEntity = controller.getAllFaults();
        Resources<Fault> body = ((Resources) responseEntity.getBody());
        List<Fault> persisted = new ArrayList<>(body.getContent());
        Assert.assertEquals(persisted.size(), 2);
        verify(repository, atLeast(1)).findAll();
    }

    /**
     * Test method for {@link VersioningModelController#getAllV0Faults()}
     */
    @Test
    public void getAllV0FaultsTest() {
        log.info("Testing getAllV0Faults method on the versionModelController");
        List<Fault> faults = mockData.getDummyFaultsList();
        when(repository.findAll()).thenReturn(faults);
        ResponseEntity responseEntity = controller.getAllV0Faults();
        Resources<FaultV0> persisted = ((Resources) responseEntity.getBody());
        List<FaultV0> faults1 = new ArrayList<FaultV0>();
        faults1.addAll(persisted.getContent());
        Assert.assertEquals(persisted.getContent().size(), 2);
    }

    /**
     * Test method for {@link VersioningModelController#getFault(String)}
     */
    @Test
    public void getFaultTest() {
        log.info("Testing getFault method on the versionModelController");
        Fault fault = mockData.getDummyFault();
        when(repository.findByName(anyString())).thenReturn(fault);
        ResponseEntity responseEntity = controller.getFault(fault.getName());
        Fault persisted = (Fault) ((Resource) responseEntity.getBody()).getContent();
        Assert.assertEquals(persisted, fault);
        verify(repository, atLeast(1)).findByName(anyString());
    }

    /**
     * Test method for {@link VersioningModelController#getV0Fault(String)}
     */
    @Test
    public void getV0FaultTest() {
        log.info("Testing getFault method on the versionModelController");
        Fault fault = mockData.getDummyFault();
        when(repository.findByName(anyString())).thenReturn(fault);
        ResponseEntity responseEntity = controller.getV0Fault(fault.getName());
        FaultV0 persisted = (FaultV0) ((Resource) responseEntity.getBody()).getContent();
        Assert.assertEquals(persisted.getName(), fault.getName());
        verify(repository, atLeastOnce()).findByName(anyString());
    }

    /**
     * Test method for {@link VersioningModelController#getFaultByType(String)}
     */
    @Test
    public void getFaultByTypeTest() {
        log.info("Testing getFaultByTypeTest method on the VersioningModelController#getFaultByType");
        List<Fault> faults = mockData.getDummyFaultsList();
        when(repository.findByType(anyString())).thenReturn(faults);
        ResponseEntity responseEntity = controller.getFaultByType(mockData.getDummyFault().getType());
        Resources<Fault> body = ((Resources) responseEntity.getBody());
        List<Fault> persisted = new ArrayList<>(body.getContent());
        Assert.assertEquals(persisted.size(), 2);
        verify(repository, atLeast(1)).findByType(anyString());
    }

    /**
     * Test method for {@link VersioningModelController#createFault(Fault)}
     */
    @Test
    public void createFaultTest() {
        log.info("Testing createFaultTest method on the VersioningModelController#createFault");
        Fault fault = mockData.getDummyFault();
        when(repository.save(any())).thenReturn(fault);
        ResponseEntity responseEntity = controller.createFault(fault);
        Resource<Fault> body = ((Resource) responseEntity.getBody());
        Fault persisted = body.getContent();
        Assert.assertEquals(persisted, fault);
        verify(repository, atLeast(1)).save(any());
    }

    /**
     * Test method for {@link VersioningModelController#createV0Fault(FaultV0)}
     */
    @Test
    public void createV0FaultTest() {
        log.info("Testing createV0FaultTest method on the VersioningModelController#createV0Fault");
        FaultV0 fault = mockData.getDummyFaultV0();
        Fault fault1 = mockData.getDummyFault();
        when(repository.save(any())).thenReturn(fault1);
        ResponseEntity responseEntity = controller.createV0Fault(fault);
        Resource<FaultV0> body = ((Resource) responseEntity.getBody());
        FaultV0 persisted = body.getContent();
        Assert.assertEquals(persisted.getName(), fault1.getName());
        verify(repository, atLeast(1)).save(any());
    }

}
