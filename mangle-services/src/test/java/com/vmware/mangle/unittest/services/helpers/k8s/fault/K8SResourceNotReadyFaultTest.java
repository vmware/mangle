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

package com.vmware.mangle.unittest.services.helpers.k8s.fault;


import lombok.extern.log4j.Log4j2;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.K8SResourceNotReadyFaultSpec;
import com.vmware.mangle.services.enums.K8SFaultName;
import com.vmware.mangle.services.enums.K8SResource;
import com.vmware.mangle.services.helpers.k8s.faults.K8SResourceNotReadyFault;
import com.vmware.mangle.services.mockdata.EndpointMockData;
import com.vmware.mangle.services.mockdata.FaultsMockData;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Integration tests for the {@link K8SResourceNotReadyFault}.
 *
 * @author hkilari
 * @since 1.0
 */
@Log4j2
public class K8SResourceNotReadyFaultTest {
    FaultsMockData faultsMockData = new FaultsMockData();

    /**
     * Test method for
     * {@link K8SResourceNotReadyFault#K8SResourceNotReadyFault(K8SResourceNotReadyFaultSpec)}.
     */
    @Test
    public void testConstructor() {
        log.info("Executing test: testConstructor on K8SResourceNotReadyFault#K8SResourceNotReadyFault");
        try {
            new K8SResourceNotReadyFault(faultsMockData.getK8SResourceNotReadyFaultSpec());
        } catch (MangleException e) {
            log.error("K8SResourceNotReadyFault initialization failed with Exception: ", e);
            Assert.assertTrue(false);
        }
    }

    /**
     * Test method for
     * {@link K8SResourceNotReadyFault#K8SResourceNotReadyFault(K8SResourceNotReadyFaultSpec)}.
     */
    @Test
    public void testConstructorwithNullArgs() {
        log.info("Executing test: testConstructorwithNullArgs on K8SResourceNotReadyFault#K8SResourceNotReadyFault");
        K8SResourceNotReadyFault k8SResourceNotReadyFault = null;
        K8SResourceNotReadyFaultSpec faultSpec = null;
        try {
            faultSpec = faultsMockData.getK8SResourceNotReadyFaultSpec();
            faultSpec.setArgs(null);
            k8SResourceNotReadyFault = new K8SResourceNotReadyFault(faultSpec);
        } catch (MangleException e) {
            log.error("K8SResourceNotReadyFault initialization failed with Exception: ", e);
            Assert.assertTrue(false);
        }
        Assert.assertEquals(k8SResourceNotReadyFault.getFaultSpec().getArgs().size(), 1);
        Assert.assertEquals(k8SResourceNotReadyFault.getFaultSpec().getArgs().get("operation"),
                K8SFaultName.NOTREADY_RESOURCE.name());
    }

    /**
     * Test method for
     * {@link K8SResourceNotReadyFault#K8SResourceNotReadyFault(K8SResourceNotReadyFaultSpec)}.
     *
     * @throws MangleException
     */
    @Test(expectedExceptions = { MangleException.class })
    public void testConstructorwithUnsupportedEndpoint() throws MangleException {
        log.info(
                "Executing test: testConstructorwithUnsupportedEndpoint on K8SResourceNotReadyFault#K8SResourceNotReadyFault");
        try {
            K8SResourceNotReadyFaultSpec faultSpec = faultsMockData.getK8SResourceNotReadyFaultSpec();
            faultSpec.setEndpoint(new EndpointMockData().rmEndpointMockData());
            new K8SResourceNotReadyFault(faultSpec);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.UNSUPPORTED_ENDPOINT);
            throw e;
        }
    }

    /**
     * Test method for
     * {@link K8SResourceNotReadyFault#K8SResourceNotReadyFault(K8SResourceNotReadyFaultSpec)}.
     *
     * @throws MangleException
     */
    @Test(expectedExceptions = { MangleException.class })
    public void testConstructorwithUnsupportedResourceType() throws MangleException {
        log.info(
                "Executing test: testConstructorwithUnsupportedResourceType on K8SResourceNotReadyFault#K8SResourceNotReadyFault");
        try {
            K8SResourceNotReadyFaultSpec faultSpec = faultsMockData.getK8SResourceNotReadyFaultSpec();
            faultSpec.setResourceType(K8SResource.DEPLOYMENT);
            new K8SResourceNotReadyFault(faultSpec);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.UNSUPPORTED_K8S_RESOURCE_TYPE);
            throw e;
        }
    }

}
