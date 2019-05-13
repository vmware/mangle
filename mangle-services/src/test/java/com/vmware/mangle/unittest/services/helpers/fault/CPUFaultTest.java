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

package com.vmware.mangle.unittest.services.helpers.fault;


import lombok.extern.log4j.Log4j2;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CpuFaultSpec;
import com.vmware.mangle.services.helpers.faults.CPUFault;
import com.vmware.mangle.services.mockdata.EndpointMockData;
import com.vmware.mangle.services.mockdata.FaultsMockData;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Integration tests for the {@link CPUFault}.
 *
 * @author hkilari
 * @since 1.0
 */
@Log4j2
public class CPUFaultTest {
    FaultsMockData faultsMockData = new FaultsMockData();

    /**
     * Test method for {@link CPUFault#CPUFault(CpuFaultSpec)}.
     */
    @Test
    public void testConstructor() {
        log.info("Executing test: testConstructor on CPUFault#CPUFault");
        try {
            new CPUFault(faultsMockData.getK8SCPUFaultSpec());
        } catch (MangleException e) {
            log.error("CPUFault initialization failed with Exception: ", e);
            Assert.assertTrue(false);
        }
    }

    /**
     * Test method for {@link CPUFault#CPUFault(CpuFaultSpec)}.
     */
    @Test
    public void testConstructorwithNullArgs() {
        log.info("Executing test: testConstructorwithNullArgs on CPUFault#CPUFault");
        CPUFault cpuFault = null;
        CpuFaultSpec faultSpec = null;
        try {
            faultSpec = faultsMockData.getK8SCPUFaultSpec();
            faultSpec.setArgs(null);
            cpuFault = new CPUFault(faultSpec);
        } catch (MangleException e) {
            log.error("CPUFault initialization failed with Exception: ", e);
            Assert.assertTrue(false);
        }
        Assert.assertEquals(cpuFault.getFaultSpec().getArgs().size(), 1);
        Assert.assertEquals(cpuFault.getFaultSpec().getArgs().get("__load"), String.valueOf(faultSpec.getCpuLoad()));
    }

    /**
     * Test method for {@link CPUFault#CPUFault(CpuFaultSpec)}.
     *
     * @throws MangleException
     */
    @Test(expectedExceptions = { MangleException.class })
    public void testConstructorwithUnsupportedEndpoint() throws MangleException {
        log.info("Executing test: testConstructorwithUnsupportedEndpoint on CPUFault#CPUFault");
        try {
            CpuFaultSpec faultSpec = faultsMockData.getK8SCPUFaultSpec();
            faultSpec.setEndpoint(new EndpointMockData().getVCenterEndpointSpecMock());
            new CPUFault(faultSpec);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.UNSUPPORTED_ENDPOINT);
            throw e;
        }
    }
}
