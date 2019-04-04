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

import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultSpec;
import com.vmware.mangle.services.enums.K8SFaultName;
import com.vmware.mangle.services.helpers.k8s.faults.DeleteK8SResourceFault;
import com.vmware.mangle.services.mockdata.EndpointMockData;
import com.vmware.mangle.services.mockdata.FaultsMockData;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Integration tests for the {@link DeleteK8SResourceFault}.
 *
 * @author hkilari
 * @since 1.0
 */
@Log4j2
public class DeleteK8SResourceFaultTest {
    FaultsMockData faultsMockData = new FaultsMockData();

    /**
     * Test method for {@link DeleteK8SResourceFault#DeleteK8SResourceFault(K8SFaultSpec)}.
     */
    @Test
    public void testConstructor() {
        log.info("Executing test: testConstructor on DeleteK8SResourceFault#DeleteK8SResourceFault");
        try {
            new DeleteK8SResourceFault(faultsMockData.getDeleteK8SResourceFaultSpec());
        } catch (MangleException e) {
            log.error("DeleteK8SResourceFault initialization failed with Exception: ", e);
            Assert.assertTrue(false);
        }
    }

    /**
     * Test method for {@link DeleteK8SResourceFault#DeleteK8SResourceFault(K8SFaultSpec)}.
     */
    @Test
    public void testConstructorwithNullArgs() {
        log.info("Executing test: testConstructorwithNullArgs on DeleteK8SResourceFault#DeleteK8SResourceFault");
        DeleteK8SResourceFault deleteK8SResourceFault = null;
        K8SFaultSpec faultSpec = null;
        try {
            faultSpec = faultsMockData.getDeleteK8SResourceFaultSpec();
            faultSpec.setArgs(null);
            deleteK8SResourceFault = new DeleteK8SResourceFault(faultSpec);
        } catch (MangleException e) {
            log.error("DeleteK8SResourceFault initialization failed with Exception: ", e);
            Assert.assertTrue(false);
        }
        Assert.assertEquals(deleteK8SResourceFault.getFaultSpec().getArgs().size(), 1);
        Assert.assertEquals(deleteK8SResourceFault.getFaultSpec().getArgs().get("operation"),
                K8SFaultName.DELETE_RESOURCE.name());
    }

    /**
     * Test method for {@link DeleteK8SResourceFault#DeleteK8SResourceFault(K8SFaultSpec)}.
     *
     * @throws MangleException
     */
    @Test(expectedExceptions = { MangleException.class })
    public void testConstructorwithUnsupportedEndpoint() throws MangleException {
        log.info(
                "Executing test: testConstructorwithUnsupportedEndpoint on DeleteK8SResourceFault#DeleteK8SResourceFault");
        try {
            K8SFaultSpec faultSpec = faultsMockData.getDeleteK8SResourceFaultSpec();
            faultSpec.setEndpoint(new EndpointMockData().getVCenterEndpointSpecMock());
            new DeleteK8SResourceFault(faultSpec);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.UNSUPPORTED_ENDPOINT);
            throw e;
        }
    }
}
