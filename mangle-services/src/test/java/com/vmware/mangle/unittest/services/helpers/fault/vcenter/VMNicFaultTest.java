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

package com.vmware.mangle.unittest.services.helpers.fault.vcenter;

import org.junit.Assert;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMDiskFaultSpec;
import com.vmware.mangle.services.helpers.faults.vcenter.VMDiskFault;
import com.vmware.mangle.services.mockdata.EndpointMockData;
import com.vmware.mangle.services.mockdata.FaultsMockData;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 *
 *
 * @author chetanc
 */
public class VMNicFaultTest {
    private FaultsMockData faultsMockData = new FaultsMockData();
    private EndpointMockData endpointMockData = new EndpointMockData();

    @Test
    public void testVMNicFault() throws MangleException {
        VMDiskFaultSpec nicFaultSpec = faultsMockData.getVMDiskFaultSpec();
        EndpointSpec endpointSpec = endpointMockData.getVCenterEndpointSpecMock();
        nicFaultSpec.setEndpoint(endpointSpec);
        VMDiskFault vmDiskFault = new VMDiskFault(nicFaultSpec);

        Assert.assertNotNull(vmDiskFault);
    }
}
