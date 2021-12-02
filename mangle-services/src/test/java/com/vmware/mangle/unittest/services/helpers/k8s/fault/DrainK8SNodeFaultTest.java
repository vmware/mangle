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

import com.vmware.mangle.cassandra.model.faults.specs.K8SDrainNodeFaultSpec;
import com.vmware.mangle.services.enums.K8SFaultName;
import com.vmware.mangle.services.helpers.k8s.faults.DrainK8SNodeFault;
import com.vmware.mangle.services.mockdata.EndpointMockData;
import com.vmware.mangle.services.mockdata.FaultsMockData;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Unit tests for the {@link DrainK8SNodeFault}.
 *
 * @author pragya
 * @since 1.0
 */
@Log4j2
public class DrainK8SNodeFaultTest {
    FaultsMockData faultsMockData = new FaultsMockData();

    /**
     * Test method for {@link DrainK8SNodeFault(K8SDrainNodeFaultSpec)}.
     */
    @Test
    public void testConstructor() {
        log.info("Executing test: testConstructor on DrainK8NodeFault");
        try {
            new DrainK8SNodeFault(faultsMockData.getDrainK8SNodeFaultSpec());
        } catch (MangleException e) {
            log.error("DrainK8NodeFault initialization failed with Exception: ", e);
            Assert.assertTrue(false);
        }
    }

    /**
     * Test method for {@link DrainK8NodeFault#DrainK8NodeFault(K8SDrainNodeFaultSpec)}.
     */
    @Test
    public void testConstructorwithNullArgs() {
        log.info("Executing test: testConstructorwithNullArgs on DrainK8SNodeFault");
        DrainK8SNodeFault drainK8SNodeFault = null;
        K8SDrainNodeFaultSpec faultSpec = null;
        try {
            faultSpec = faultsMockData.getDrainK8SNodeFaultSpec();
            faultSpec.setArgs(null);
            drainK8SNodeFault = new DrainK8SNodeFault(faultSpec);
        } catch (MangleException e) {
            log.error("DrainK8SNodeFault initialization failed with Exception: ", e);
            Assert.assertTrue(false);
        }
        Assert.assertEquals(drainK8SNodeFault.getFaultSpec().getArgs().size(), 1);
        Assert.assertEquals(drainK8SNodeFault.getFaultSpec().getArgs().get("operation"),
                K8SFaultName.DRAIN_NODE.name());
    }

    /**
     * Test method for {@link DrainK8SNodeFault#DrainK8SNodeFault(K8SFaultSpec)}
     * (K8SDrainNodeFaultSpec)}.
     *
     * @throws MangleException
     */
    @Test(expectedExceptions = { MangleException.class })
    public void testConstructorwithUnsupportedEndpoint() throws MangleException {
        log.info("Executing test: testConstructorwithUnsupportedEndpoint on DrainK8SNodeFault");
        try {
            K8SDrainNodeFaultSpec faultSpec = faultsMockData.getDrainK8SNodeFaultSpec();
            faultSpec.setEndpoint(new EndpointMockData().getVCenterEndpointSpecMock());
            new DrainK8SNodeFault(faultSpec);
        } catch (MangleException e) {
            log.error("DrainK8SNodeFault failed due to unsupported endpoint with Exception: ", e);
            Assert.assertEquals(e.getErrorCode(), ErrorCode.UNSUPPORTED_ENDPOINT);
            throw e;
        }
    }
}
