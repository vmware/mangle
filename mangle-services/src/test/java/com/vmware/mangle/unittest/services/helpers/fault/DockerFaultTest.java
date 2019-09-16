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


import org.testng.Assert;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.faults.specs.DockerFaultSpec;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.helpers.faults.DockerFault;
import com.vmware.mangle.services.mockdata.EndpointMockData;
import com.vmware.mangle.services.mockdata.FaultsMockData;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Unit tests for the {@link DockerFault}.
 *
 * @author rpraveen
 * @since 1.0
 */

public class DockerFaultTest {
    private FaultsMockData faultsMockData = new FaultsMockData();
    private EndpointMockData endpointMockData = new EndpointMockData();

    @Test
    public void testDockerFault() throws MangleException {
        DockerFaultSpec dockerFaultSpec = faultsMockData.getDockerPauseFaultSpec();
        EndpointSpec endpointSpec = endpointMockData.dockerEndpointMockData();
        dockerFaultSpec.setEndpoint(endpointSpec);
        DockerFault dockerFault = new DockerFault(dockerFaultSpec);
        Assert.assertNotNull(dockerFault);
        Assert.assertEquals(EndpointType.DOCKER, dockerFault.getFaultSpec().getEndpoint().getEndPointType());
        Assert.assertNotNull(dockerFault.getFaultSpec().getDockerArguments());
        Assert.assertNotNull(dockerFault.getFaultSpec().getEndpoint().getDockerConnectionProperties());
    }
}
