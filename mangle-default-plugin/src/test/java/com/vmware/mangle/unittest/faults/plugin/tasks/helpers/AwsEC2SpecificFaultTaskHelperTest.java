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

package com.vmware.mangle.unittest.faults.plugin.tasks.helpers;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.faults.plugin.helpers.aws.AwsEC2FaultHelper;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.faults.plugin.tasks.helpers.AwsEC2SpecificFaultTaskHelper;
import com.vmware.mangle.model.aws.faults.spec.AwsEC2FaultSpec;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.clients.aws.AWSCommandExecutor;
import com.vmware.mangle.utils.clients.aws.CustomAwsClient;

/**
 *
 *
 * @author bkaranam
 */
public class AwsEC2SpecificFaultTaskHelperTest {
    FaultsMockData faultsMockData = new FaultsMockData();

    @Mock
    AwsEC2FaultHelper ec2FaultHelper;
    @Mock
    EndpointClientFactory endpointClientFactory;
    @InjectMocks
    private AwsEC2SpecificFaultTaskHelper<AwsEC2FaultSpec> awsEC2SpecificFaultTask;
    @Mock
    ApplicationEventPublisher publisher;

    @Mock
    CustomAwsClient customAwsClient;
    @Mock
    AWSCommandExecutor executor;

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInitOfInjection() {
        AwsEC2SpecificFaultTaskHelper<AwsEC2FaultSpec> injectionTask = new AwsEC2SpecificFaultTaskHelper<>();
        injectionTask.setAwsEC2FaultHelper(ec2FaultHelper);
        Task<AwsEC2FaultSpec> task = injectionTask.init(faultsMockData.getAwsEC2InstanceStateFaultSpec());
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
    }
}
