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

package com.vmware.mangle.unittest.services.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.Fault;
import com.vmware.mangle.model.FaultV0;
import com.vmware.mangle.services.MappingService;
import com.vmware.mangle.services.mockdata.FaultServiceMockData;

/**
 *
 *
 * @author chetanc
 */
public class MappingServiceTest {
    MappingService mappingService = new MappingService();
    FaultServiceMockData mockData = new FaultServiceMockData();

    @Test
    public void testObjectMap() {
        Fault fault = mockData.getDummyFault();
        Object faultV0 = mappingService.map(fault, FaultV0.class);
        Assert.assertTrue(faultV0 instanceof FaultV0);
    }

    @Test
    public void testObjectMap1() {
        Fault fault = mockData.getDummyFault();
        FaultV0 faultV0 = new FaultV0();
        mappingService.map(fault, faultV0);
        Assert.assertEquals(faultV0.getName(), fault.getName());
        Assert.assertEquals(faultV0.getType(), fault.getType());
    }

    @Test
    public void testListMap() {
        List<Fault> faults = mockData.getDummyFaultsList();
        List list = mappingService.map(faults, FaultV0.class);
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            Assert.assertTrue(iterator.next() instanceof FaultV0);
        }
    }

    @Test
    public void testListMap1() {
        List<Fault> faults = mockData.getDummyFaultsList();
        List faultV0s = new ArrayList<>();
        List list = mappingService.map(faults, FaultV0.class, faultV0s);
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            Assert.assertTrue(iterator.next() instanceof FaultV0);
        }
    }

    @Test
    public void testSetMap() {
        List<Fault> faults = mockData.getDummyFaultsList();
        Set<Fault> faultSet = new HashSet<>(faults);
        Set set = mappingService.map(faultSet, FaultV0.class);
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            Assert.assertTrue(iterator.next() instanceof FaultV0);
        }
    }

    @Test
    public void testSetMap1() {
        List<Fault> faults = mockData.getDummyFaultsList();
        Set<Fault> faultSet = new HashSet<>(faults);
        Set faultSet1 = new HashSet<>();
        Set set = mappingService.map(faultSet, FaultV0.class, faultSet1);
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            Assert.assertTrue(iterator.next() instanceof FaultV0);
        }
    }
}
