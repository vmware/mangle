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

package com.vmware.mangle.unittest.plugin.helpers.faults;

import lombok.extern.log4j.Log4j2;

import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.endpoint.RemoteMachineConnectionProperties;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.model.enums.OSType;
import com.vmware.mangle.plugin.helpers.faults.HelloMangleFault;
import com.vmware.mangle.plugin.model.faults.specs.HelloMangleFaultSpec;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Unit tests for the {@link HelloMangleFault}.
 *
 * @author hkilari
 * @since 1.0
 */
@Log4j2
public class HelloMangleFaultTest {
    HelloMangleFaultSpec helloMangleFaultSpec;

    @BeforeTest
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        helloMangleFaultSpec = new HelloMangleFaultSpec();
        helloMangleFaultSpec.setField1("field1");
        helloMangleFaultSpec.setField2("field2");
        EndpointSpec endpoint = new EndpointSpec();
        endpoint.setEndPointType(EndpointType.MACHINE);
        RemoteMachineConnectionProperties remoteMachineConnectionProperties = new RemoteMachineConnectionProperties();
        remoteMachineConnectionProperties.setOsType(OSType.LINUX);
        endpoint.setRemoteMachineConnectionProperties(remoteMachineConnectionProperties);
        helloMangleFaultSpec.setEndpoint(endpoint);
    }

    /**
     * Test method for {@link HelloMangleFault#HelloMangleFault(HelloMangleFaultSpec)}.
     */
    @Test
    public void testConstructor() {
        log.info("Executing test: testConstructor on HelloMangleFault#HelloMangleFault");
        new HelloMangleFault();
    }

    /**
     * Test method for {@link HelloMangleFault#HelloMangleFault(HelloMangleFaultSpec)}.
     */
    @Test
    public void testConstructorwithNullArgs() {
        log.info("Executing test: testConstructorwithNullArgs on HelloMangleFault#HelloMangleFault");
        HelloMangleFault HelloMangleFault = null;
        HelloMangleFaultSpec faultSpec = helloMangleFaultSpec;
        try {
            faultSpec.setArgs(null);
            new HelloMangleFault().init(faultSpec);
        } catch (MangleException e) {
            log.error("HelloMangleFault initialization failed with Exception: ", e);
            Assert.assertTrue(false);
        }
        Assert.assertEquals(faultSpec.getArgs().size(), 3);
        Assert.assertEquals(faultSpec.getArgs().get("field1"),
                String.valueOf(faultSpec.getField1()));
    }

    /**
     * Test method for {@link HelloMangleFault#HelloMangleFault(HelloMangleFaultSpec)}.
     *
     * @throws MangleException
     */
    @Test(expectedExceptions = { MangleException.class })
    public void testConstructorwithUnsupportedEndpoint() throws MangleException {
        log.info("Executing test: testConstructorwithUnsupportedEndpoint on HelloMangleFault#HelloMangleFault");
        HelloMangleFaultSpec faultSpec = helloMangleFaultSpec;
        try {
            EndpointSpec endpoint = new EndpointSpec();
            endpoint.setEndPointType(EndpointType.VCENTER);
            faultSpec.setEndpoint(endpoint);
            new HelloMangleFault().init(faultSpec);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.UNSUPPORTED_ENDPOINT);
            throw e;
        }
    }
}
