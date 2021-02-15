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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.VCenterAdapterDetails;
import com.vmware.mangle.services.VCenterAdapterDetailsService;
import com.vmware.mangle.services.controller.VCenterAdapterDetailsController;
import com.vmware.mangle.services.mockdata.VCenterAdapterDetailsMockData;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author chetanc
 */
public class VCenterAdapterDetailsControllerTest {

    @Mock
    private VCenterAdapterDetailsService service;
    private VCenterAdapterDetailsController controller;

    private VCenterAdapterDetailsMockData mockData = new VCenterAdapterDetailsMockData();

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        controller = new VCenterAdapterDetailsController(service);
    }

    @Test
    public void testGetAllAdapterDetails() {
        List<VCenterAdapterDetails> detailsList = mockData.getVcaAdaptersList();
        when(service.getAllVCenterAdapterDetails()).thenReturn(detailsList);

        ResponseEntity<Resources<VCenterAdapterDetails>> responseEntity = controller.getAllAdapterDetails();

        Assert.assertNotNull(responseEntity.getBody());
        Collection<VCenterAdapterDetails> detailsResponse = responseEntity.getBody().getContent();

        Assert.assertEquals(detailsResponse.size(), detailsList.size());
        verify(service, times(1)).getAllVCenterAdapterDetails();
    }

    @Test
    public void testCreateAdapterDetails() throws MangleException {
        VCenterAdapterDetails details = mockData.getVcaAdapterMockData();
        when(service.createVCenterAdapterDetails(details)).thenReturn(details);

        ResponseEntity<Resource<VCenterAdapterDetails>> responseEntity = controller.createAdapterDetails(details);

        Assert.assertNotNull(responseEntity.getBody());
        VCenterAdapterDetails detailsResponse = responseEntity.getBody().getContent();

        Assert.assertEquals(detailsResponse, details);
        verify(service, times(1)).createVCenterAdapterDetails(details);
    }

    @Test
    public void testUpdateAdapterDetails() throws MangleException {
        VCenterAdapterDetails details = mockData.getVcaAdapterMockData();
        when(service.updateVCenterAdapterDetails(details)).thenReturn(details);

        ResponseEntity<Resource<VCenterAdapterDetails>> responseEntity = controller.updateAdapterDetails(details);

        Assert.assertNotNull(responseEntity.getBody());
        VCenterAdapterDetails detailsResponse = responseEntity.getBody().getContent();

        Assert.assertEquals(detailsResponse, details);
        verify(service, times(1)).updateVCenterAdapterDetails(details);
    }

    @Test
    public void testDeleteAdapterDetails() throws MangleException {
        List<VCenterAdapterDetails> detailsList = mockData.getVcaAdaptersList();
        List<String> detailsNameList =
                detailsList.stream().map(VCenterAdapterDetails::getName).collect(Collectors.toList());
        doNothing().when(service).deleteVCenterAdapterDetails(any());

        controller.deleteAdapterDetails(detailsNameList);

        verify(service, times(1)).deleteVCenterAdapterDetails(any());
    }

    @Test
    public void testTestConnection() throws MangleException {
        VCenterAdapterDetails details = mockData.getVcaAdapterMockData();
        when(service.testConnection(details)).thenReturn(true);

        ResponseEntity<Resource<Boolean>> responseEntity = controller.testConnection(details);
        Assert.assertNotNull(responseEntity.getBody());
        Assert.assertTrue(responseEntity.getBody().getContent());
        verify(service, times(1)).testConnection(details);
    }
}
