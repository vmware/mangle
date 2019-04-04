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

package com.vmware.mangle.unittest.faults.plugin.helpers.systemresource;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.faults.plugin.helpers.systemresource.LinuxSystemResourceFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.systemresource.SystemResourceFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.systemresource.SystemResourceFaultHelperFactory;
import com.vmware.mangle.faults.plugin.mockdata.EndpointMockData;
import com.vmware.mangle.model.enums.EndpointType;

/**
 * Unit tests for SystemResourceFaultHelperFactoryTest
 *
 * @author jayasankarr
 */

public class SystemResourceFaultHelperFactoryTest {


    @InjectMocks
    private SystemResourceFaultHelperFactory systemResourceFaultHelperFactory;

    @Mock
    private LinuxSystemResourceFaultHelper linuxBytemanFaultHelper;

    private EndpointMockData mockData = new EndpointMockData();
    private EndpointSpec endpointSpec;

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        endpointSpec = mockData.rmEndpointMockData();
    }

    @Test(priority = 1)
    public void testGetLinuxSystemResourceFaultHelper() throws Exception {
        SystemResourceFaultHelper helper = systemResourceFaultHelperFactory.getHelper(endpointSpec);
        Assert.assertTrue(helper instanceof LinuxSystemResourceFaultHelper);

    }

    @Test(priority = 2)
    public void testNegativeCase() {
        endpointSpec.setEndPointType(EndpointType.VCENTER);
        SystemResourceFaultHelper helper = systemResourceFaultHelperFactory.getHelper(endpointSpec);
        Assert.assertNull(helper);
    }

}
