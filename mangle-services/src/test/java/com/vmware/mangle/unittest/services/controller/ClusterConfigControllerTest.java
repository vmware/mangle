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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.hazelcast.HazelcastClusterConfig;
import com.vmware.mangle.model.enums.MangleDeploymentMode;
import com.vmware.mangle.services.ClusterConfigService;
import com.vmware.mangle.services.controller.ClusterConfigController;
import com.vmware.mangle.services.mockdata.ClusterConfigMockdata;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 *
 * @author chetanc
 */
public class ClusterConfigControllerTest {

    @Mock
    private ClusterConfigService configService;
    private ClusterConfigController configController;
    private ClusterConfigMockdata mockData = new ClusterConfigMockdata();

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        configController = spy(new ClusterConfigController(configService));

        Link link = mock(Link.class);
        doReturn(link).when(configController).getSelfLink();
    }

    @Test
    public void testGetHazelcastConfig() throws MangleException {
        HazelcastClusterConfig config = mockData.getClusterConfigObject();
        when(configService.getClusterConfiguration()).thenReturn(config);

        ResponseEntity<Resource<HazelcastClusterConfig>> responseEntity = configController.getHazelcastConfig();
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);

        Assert.assertNotNull(responseEntity.getBody());
        HazelcastClusterConfig persisted = responseEntity.getBody().getContent();

        Assert.assertNotNull(persisted);
        Assert.assertEquals(persisted.getValidationToken(), config.getValidationToken());
        Assert.assertEquals(persisted.getClusterName(), config.getClusterName());
        Assert.assertEquals(persisted.getMembers(), config.getMembers());
        verify(configService, times(1)).getClusterConfiguration();
    }

    @Test
    public void testUpdateHazelcastConfig() throws MangleException {
        HazelcastClusterConfig config = mockData.getClusterConfigObject();
        when(configService.getClusterConfiguration()).thenReturn(config);
        when(configService.updateClusterConfiguration(config)).thenReturn(config);
        ResponseEntity<Resource<HazelcastClusterConfig>> responseEntity =
                configController.updateHazelcastConfig(config);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertNotNull(responseEntity.getBody());
        HazelcastClusterConfig persisted = responseEntity.getBody().getContent();
        Assert.assertNotNull(persisted);
        Assert.assertEquals(persisted.getValidationToken(), config.getValidationToken());
        Assert.assertEquals(persisted.getClusterName(), config.getClusterName());
        Assert.assertEquals(persisted.getMembers(), config.getMembers());
        verify(configService, times(1)).getClusterConfiguration();
        verify(configService, times(1)).updateClusterConfiguration(any());
    }

    @Test(expectedExceptions = MangleException.class)
    public void testUpdateHazelcastConfigFailure() throws MangleException {
        HazelcastClusterConfig config = mockData.getClusterConfigObject();
        HazelcastClusterConfig config1 = mockData.getModifiedClusterConfigObject();
        when(configService.getClusterConfiguration()).thenReturn(config);
        when(configService.updateClusterConfiguration(config)).thenReturn(config);
        try {
            configController.updateHazelcastConfig(config1);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.CLUSTER_CONFIG_MEMBER_MODIFICATION);
            verify(configService, times(1)).getClusterConfiguration();
            verify(configService, times(0)).updateClusterConfiguration(any());

            throw e;
        }
    }

    @Test
    public void testUpdateDeploymentMode() throws MangleException {
        HazelcastClusterConfig config = mockData.getClusterConfigObject();

        when(configService.updateMangleDeploymentType(any())).thenReturn(config);

        ResponseEntity<Resource<HazelcastClusterConfig>> responseEntity =
                configController.updateDeploymentMode(MangleDeploymentMode.STANDALONE);

        Assert.assertNotNull(responseEntity.getBody());
        HazelcastClusterConfig persisted = responseEntity.getBody().getContent();

        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(persisted, config);

        verify(configService, times(1)).updateMangleDeploymentType(any());
    }

    @Test
    public void testUpdateQuorum() throws MangleException {
        HazelcastClusterConfig config = mockData.getClusterConfigObject();

        when(configService.updateMangleQuorum(anyInt())).thenReturn(config);

        ResponseEntity<Resource<HazelcastClusterConfig>> responseEntity = configController.updateQuorum(5);

        Assert.assertNotNull(responseEntity.getBody());
        HazelcastClusterConfig persisted = responseEntity.getBody().getContent();

        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(persisted, config);

        verify(configService, times(1)).updateMangleQuorum(anyInt());
    }

}
