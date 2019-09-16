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

package com.vmware.mangle.unittest.faults.plugin.helpers.byteman;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.faults.plugin.helpers.byteman.BytemanFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.byteman.BytemanFaultHelperFactory;
import com.vmware.mangle.faults.plugin.helpers.byteman.DockerBytemanFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.byteman.K8sBytemanFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.byteman.LinuxBytemanFaultHelper;
import com.vmware.mangle.faults.plugin.mockdata.EndpointMockData;
import com.vmware.mangle.model.enums.EndpointType;

/**
 * Unit tests for BytemanFaultHelperFactoryTest
 *
 * @author jayasankarr
 */
public class BytemanFaultHelperFactoryTest {


    @Mock
    private K8sBytemanFaultHelper k8sBytemanFaultHelper;

    @InjectMocks
    private BytemanFaultHelperFactory bytemanFaultHelperFactory;

    @Mock
    private LinuxBytemanFaultHelper linuxBytemanFaultHelper;

    @Mock
    private DockerBytemanFaultHelper dockerBytemanFaultHelper;

    private EndpointMockData mockData = new EndpointMockData();
    private EndpointSpec endpointSpec;

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        endpointSpec = mockData.rmEndpointMockData();
    }

    @Test(priority = 1)
    public void testGetLinuxBytemanFaultHelper() throws Exception {
        BytemanFaultHelper helper = bytemanFaultHelperFactory.getHelper(endpointSpec);
        Assert.assertTrue(helper instanceof LinuxBytemanFaultHelper);

    }

    @Test(priority = 2)
    public void testGetK8sBytemanFaultHelper() {
        endpointSpec.setEndPointType(EndpointType.K8S_CLUSTER);
        BytemanFaultHelper helper = bytemanFaultHelperFactory.getHelper(endpointSpec);
        Assert.assertTrue(helper instanceof K8sBytemanFaultHelper);
    }

    @Test(priority = 3)
    public void testGetDockerBytemanFaultHelper() {
        endpointSpec.setEndPointType(EndpointType.DOCKER);
        BytemanFaultHelper helper = bytemanFaultHelperFactory.getHelper(endpointSpec);
        Assert.assertTrue(helper instanceof DockerBytemanFaultHelper);
    }

    @Test(priority = 4)
    public void testNegativeCase() {
        endpointSpec.setEndPointType(EndpointType.VCENTER);
        BytemanFaultHelper helper = bytemanFaultHelperFactory.getHelper(endpointSpec);
        Assert.assertNull(helper);
    }

    @Test(priority = 5, expectedExceptions = {
            NullPointerException.class }, expectedExceptionsMessageRegExp = "endpoint is marked @NonNull but is null")
    public void testforNullEndpoint() {
        BytemanFaultHelper helper = bytemanFaultHelperFactory.getHelper(null);
    }

}
