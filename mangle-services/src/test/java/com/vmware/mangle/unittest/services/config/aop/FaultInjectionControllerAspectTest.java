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

package com.vmware.mangle.unittest.services.config.aop;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.aspectj.lang.JoinPoint;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.DiskSpaceSpec;
import com.vmware.mangle.services.config.aop.FaultInjectionControllerAspect;
import com.vmware.mangle.services.helpers.FaultInjectionHelper;
import com.vmware.mangle.services.mockdata.FaultsMockData;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Unit test cases for FaultInjectionControllerAspect.
 *
 * @author kumargautam
 */
class FaultInjectionControllerAspectTest {

    @Mock
    private FaultInjectionHelper faultInjectionHelper;
    @InjectMocks
    private FaultInjectionControllerAspect injectionControllerAspect;
    private FaultsMockData faultsMockData;

    @BeforeClass
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        this.faultsMockData = new FaultsMockData();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.config.aop.FaultInjectionControllerAspect#beforeFaultInjection(org.aspectj.lang.JoinPoint)}.
     *
     * @throws MangleException
     */
    @Test
    void testBeforeFaultInjection() throws MangleException {
        JoinPoint joinPoint = mock(JoinPoint.class);
        DiskSpaceSpec faultSpec = faultsMockData.getDiskSpaceSpec();
        Object[] inputs = new Object[] { faultSpec };
        when(joinPoint.getArgs()).thenReturn(inputs);
        doNothing().when(faultInjectionHelper).validateSpec(any());
        doNothing().when(faultInjectionHelper).validateEndpointTypeSpecificArguments(any());
        injectionControllerAspect.beforeFaultInjection(joinPoint);
        verify(joinPoint, times(1)).getArgs();
        verify(faultInjectionHelper, times(1)).validateSpec(any());
        verify(faultInjectionHelper, times(1)).validateEndpointTypeSpecificArguments(any());
    }

}
