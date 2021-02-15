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

package com.vmware.mangle.unittest.utils.clients.aws;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.rds.AmazonRDSAsync;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.model.aws.AwsRDSInstance;
import com.vmware.mangle.utils.clients.aws.AWSCommonUtils;
import com.vmware.mangle.utils.clients.aws.CustomAwsClient;
import com.vmware.mangle.utils.clients.aws.RDSFaultOperations;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;


/**
 * Unit Test Case for RDSFaultOperations.
 *
 * @author bkaranam
 */
@PrepareForTest(value = { AWSCommonUtils.class })
@PowerMockIgnore({ "javax.net.ssl.*", "javax.xml.parsers.*", "com.sun.org.apache.xerces.internal.jaxp.*",
        "org.apache.logging.log4j.*", "com.sun.org.apache.xalan.internal.xsltc.trax.*" })
public class RDSFaultOperationsTest extends PowerMockTestCase {

    @Mock
    private CustomAwsClient customAwsClient;

    @Mock
    private AmazonRDSAsync rdsClient;

    private String dbInstanceIdentifier = "instance-1";
    private String securityGroupID = "DummySecurityGroupID";

    /**
     * @throws Exception
     */
    @BeforeMethod
    public void setUpBeforeMethod() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(AWSCommonUtils.class);
        when(customAwsClient.rdsClient()).thenReturn(rdsClient);
    }

    /**
     * @throws Exception
     */
    @AfterClass
    public void tearDownAfterClass() throws Exception {
        this.customAwsClient = null;
    }

    /**
     * @throws Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
        validateMockitoUsage();
    }

    /**
     * Test method for {@link#RDSFaultOperations#startInstances}.
     *
     * @throws Exception
     */
    @Test
    public void testStartInstances() throws Exception {
        PowerMockito.doNothing().when(AWSCommonUtils.class);
        AWSCommonUtils.startRDS_instances(any(), any());
        Assert.assertEquals(RDSFaultOperations.startInstances(customAwsClient, dbInstanceIdentifier).getExitCode(), 0);
        PowerMockito.doThrow(new MangleException(ErrorCode.AWS_OPERATION_FAILURE)).when(AWSCommonUtils.class);
        AWSCommonUtils.startRDS_instances(any(), any());
        Assert.assertEquals(RDSFaultOperations.startInstances(customAwsClient, dbInstanceIdentifier).getExitCode(), 1);
    }

    /**
     * Test method for {@link#RDSFaultOperations#stopInstances}.
     *
     * @throws Exception
     */
    @Test
    public void testStopInstances() throws Exception {
        PowerMockito.doNothing().when(AWSCommonUtils.class);
        AWSCommonUtils.stopRDS_instances(any(), any());
        Assert.assertEquals(RDSFaultOperations.stopInstances(customAwsClient, dbInstanceIdentifier).getExitCode(), 0);
        PowerMockito.doThrow(new MangleException(ErrorCode.AWS_OPERATION_FAILURE)).when(AWSCommonUtils.class);
        AWSCommonUtils.stopRDS_instances(any(), any());
        Assert.assertEquals(RDSFaultOperations.stopInstances(customAwsClient, dbInstanceIdentifier).getExitCode(), 1);
    }

    /**
     * Test method for {@link#RDSFaultOperations#rebootInstances}.
     *
     * @throws Exception
     */
    @Test
    public void testRebootInstances() throws Exception {
        PowerMockito.doNothing().when(AWSCommonUtils.class);
        AWSCommonUtils.rebootRDS_instances(any(), any());
        Assert.assertEquals(RDSFaultOperations.rebootInstances(customAwsClient, dbInstanceIdentifier).getExitCode(), 0);
        PowerMockito.doThrow(new MangleException(ErrorCode.AWS_OPERATION_FAILURE)).when(AWSCommonUtils.class);
        AWSCommonUtils.rebootRDS_instances(any(), any());
        Assert.assertEquals(RDSFaultOperations.rebootInstances(customAwsClient, dbInstanceIdentifier).getExitCode(), 1);
    }

    /**
     * Test method for {@link#RDSFaultOperations#failoverDBCluster}.
     *
     * @throws Exception
     */
    @Test
    public void testFailoverDBCluster() throws Exception {
        PowerMockito.doNothing().when(AWSCommonUtils.class);
        AWSCommonUtils.failoverRDS_instances(any(), any());
        Assert.assertEquals(RDSFaultOperations.failoverDBCluster(customAwsClient, dbInstanceIdentifier).getExitCode(),
                0);
        PowerMockito.doThrow(new MangleException(ErrorCode.AWS_OPERATION_FAILURE)).when(AWSCommonUtils.class);
        AWSCommonUtils.failoverRDS_instances(any(), any());
        Assert.assertEquals(RDSFaultOperations.failoverDBCluster(customAwsClient, dbInstanceIdentifier).getExitCode(),
                1);
    }

    /**
     * Test method for {@link#RDSFaultOperations#connectionLoss}.
     *
     * @throws Exception
     */
    @Test
    public void testDBInstanceConnectionLoss() throws Exception {

        PowerMockito.doReturn(new ArrayList<AwsRDSInstance>()).when(AWSCommonUtils.class);
        AWSCommonUtils.manipulateInstancesDBPort(any(), any());
        Assert.assertEquals(RDSFaultOperations.connectionLoss(customAwsClient, dbInstanceIdentifier).getExitCode(), 0);
        PowerMockito.doThrow(new MangleException(ErrorCode.AWS_OPERATION_FAILURE)).when(AWSCommonUtils.class);
        AWSCommonUtils.manipulateInstancesDBPort(any(), any());
        Assert.assertEquals(RDSFaultOperations.connectionLoss(customAwsClient, dbInstanceIdentifier).getExitCode(), 1);
    }

    /**
     * Test method for {@link#RDSFaultOperations#connectionReset}.
     *
     * @throws Exception
     */
    @Test
    public void testDBInstanceConnectionReset() throws Exception {
        AwsRDSInstance rdsInstance = new AwsRDSInstance();
        rdsInstance.setInstanceIdentifier(dbInstanceIdentifier);
        List<AwsRDSInstance> rdsInstances = new ArrayList<>();
        rdsInstances.add(rdsInstance);
        String rdsInstancesJson = RestTemplateWrapper.objectToJson(rdsInstances);
        PowerMockito.doNothing().when(AWSCommonUtils.class);
        AWSCommonUtils.modifyRDSInstanceWithDBport(any(), any());
        Assert.assertEquals(RDSFaultOperations.connectionReset(customAwsClient, rdsInstancesJson).getExitCode(), 0);
        PowerMockito.doThrow(new MangleException(ErrorCode.AWS_OPERATION_FAILURE)).when(AWSCommonUtils.class);
        AWSCommonUtils.modifyRDSInstanceWithDBport(any(), any());
        Assert.assertEquals(RDSFaultOperations.connectionReset(customAwsClient, rdsInstancesJson).getExitCode(), 1);
    }
}
