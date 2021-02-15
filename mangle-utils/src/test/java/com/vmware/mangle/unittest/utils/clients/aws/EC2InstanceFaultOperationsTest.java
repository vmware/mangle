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
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.ec2.AmazonEC2Async;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.RebootInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import org.apache.commons.lang3.concurrent.ConcurrentUtils;
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

import com.vmware.mangle.utils.clients.aws.AWSCommonUtils;
import com.vmware.mangle.utils.clients.aws.CustomAwsClient;
import com.vmware.mangle.utils.clients.aws.EC2InstanceFaultOperations;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;


/**
 * Unit Test Case for EC2InstanceFaultOperations.
 *
 * @author bkaranam
 */
@PrepareForTest(value = { AWSCommonUtils.class })
@PowerMockIgnore({ "javax.net.ssl.*", "javax.xml.parsers.*", "com.sun.org.apache.xerces.internal.jaxp.*",
        "org.apache.logging.log4j.*", "com.sun.org.apache.xalan.internal.xsltc.trax.*" })
public class EC2InstanceFaultOperationsTest extends PowerMockTestCase {

    @Mock
    private CustomAwsClient customAwsClient;

    @Mock
    private AmazonEC2Async ec2Client;

    private String instanceID = "DummyInstanceID";
    private String securityGroupID = "DummySecurityGroupID";

    /**
     * @throws Exception
     */
    @BeforeMethod
    public void setUpBeforeMethod() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(AWSCommonUtils.class);
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
     * Test method for {@link#EC2InstanceFaultOperations#terminateInstances }.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testTerminateInstances() {
        Future<TerminateInstancesResult> result = getFutureObjectForTerminateInstances();
        when(customAwsClient.ec2Client()).thenReturn(ec2Client);
        when(ec2Client.terminateInstancesAsync(any(TerminateInstancesRequest.class), any(AsyncHandler.class)))
                .thenReturn(result);
        Assert.assertEquals(EC2InstanceFaultOperations.terminateInstances(customAwsClient, instanceID).getExitCode(),
                0);
        when(ec2Client.terminateInstancesAsync(any(TerminateInstancesRequest.class), any(AsyncHandler.class)))
                .thenThrow(ExecutionException.class);
        Assert.assertEquals(EC2InstanceFaultOperations.terminateInstances(customAwsClient, instanceID).getExitCode(),
                1);
    }

    /**
     * Test method for {@link#EC2InstanceFaultOperations#startInstances }.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testStartInstances() {
        Future<StartInstancesResult> result = getFutureObjectForStartInstances();
        when(customAwsClient.ec2Client()).thenReturn(ec2Client);
        when(ec2Client.startInstancesAsync(any(StartInstancesRequest.class), any(AsyncHandler.class)))
                .thenReturn(result);
        Assert.assertEquals(EC2InstanceFaultOperations.startInstances(customAwsClient, instanceID).getExitCode(), 0);
        when(ec2Client.startInstancesAsync(any(StartInstancesRequest.class), any(AsyncHandler.class)))
                .thenThrow(ExecutionException.class);
        Assert.assertEquals(EC2InstanceFaultOperations.startInstances(customAwsClient, instanceID).getExitCode(), 1);
    }

    /**
     * Test method for {@link#EC2InstanceFaultOperations#stopInstances }.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testStopInstances() {
        Future<StopInstancesResult> result = getFutureObjectForStopInstances();
        when(customAwsClient.ec2Client()).thenReturn(ec2Client);
        when(ec2Client.stopInstancesAsync(any(StopInstancesRequest.class), any(AsyncHandler.class))).thenReturn(result);
        Assert.assertEquals(EC2InstanceFaultOperations.stopInstances(customAwsClient, instanceID).getExitCode(), 0);
        when(ec2Client.stopInstancesAsync(any(StopInstancesRequest.class), any(AsyncHandler.class)))
                .thenThrow(ExecutionException.class);
        Assert.assertEquals(EC2InstanceFaultOperations.stopInstances(customAwsClient, instanceID).getExitCode(), 1);
    }

    /**
     * Test method for {@link#EC2InstanceFaultOperations#rebootInstances }.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testRebootInstances() {
        Future<RebootInstancesResult> result = getFutureObjectForRebootInstances();
        when(customAwsClient.ec2Client()).thenReturn(ec2Client);
        when(ec2Client.rebootInstancesAsync(any(RebootInstancesRequest.class), any(AsyncHandler.class)))
                .thenReturn(result);
        Assert.assertEquals(EC2InstanceFaultOperations.rebootInstances(customAwsClient, instanceID).getExitCode(), 0);
        when(ec2Client.rebootInstancesAsync(any(RebootInstancesRequest.class), any(AsyncHandler.class)))
                .thenThrow(ExecutionException.class);
        Assert.assertEquals(EC2InstanceFaultOperations.rebootInstances(customAwsClient, instanceID).getExitCode(), 1);
    }

    /**
     * Test method for {@link#EC2InstanceFaultOperations#blockAllNetworkTraffic }.
     *
     * @throws MangleException
     *
     * @throws Exception
     */
    @Test
    public void testBlockAllNetworkTraffic() throws Exception {
        List<String> securityGroups = new ArrayList<>();
        securityGroups.add(securityGroupID);
        when(customAwsClient.ec2Client()).thenReturn(ec2Client);
        when(ec2Client.rebootInstancesAsync(any(), any())).thenReturn(ConcurrentUtils.constantFuture(new RebootInstancesResult()));
        PowerMockito.when(AWSCommonUtils.getSecurityGroupIDs(any(), any())).thenReturn(securityGroups);
        PowerMockito.when(AWSCommonUtils.createSecurityGroup(any(), any(), any(), any())).thenReturn(securityGroupID);
        PowerMockito.doNothing().when(AWSCommonUtils.class, "setInstanceSecurityGroups", any(), any(), any());
        try {
            Assert.assertEquals(
                    EC2InstanceFaultOperations.blockAllNetworkTraffic(customAwsClient, instanceID).getExitCode(), 0);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
        try {
            PowerMockito.doThrow(new MangleException(ErrorCode.AWS_INSTANCE_NOT_FOUND, instanceID))
                    .when(AWSCommonUtils.class, "setInstanceSecurityGroups", any(), any(), any());
            EC2InstanceFaultOperations.blockAllNetworkTraffic(customAwsClient, instanceID);
        } catch (MangleException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.AWS_INSTANCE_NOT_FOUND);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * Test method for {@link#EC2InstanceFaultOperations#unblockAllNetworkTraffic }.
     *
     * @throws Exception
     */
    @Test
    public void testUnblockAllNetworkTraffic() {
        try {
            PowerMockito.doNothing().when(AWSCommonUtils.class, "setInstanceSecurityGroups", any(), any(), any());
            PowerMockito.doNothing().when(AWSCommonUtils.class, "deleteSecurityGroup", any(), any());
            PowerMockito.when(AWSCommonUtils.createSecurityGroup(any(), any(), any(), any()))
                    .thenReturn(securityGroupID);
            Assert.assertEquals(EC2InstanceFaultOperations.unblockAllNetworkTraffic(customAwsClient,
                    instanceID + "#" + securityGroupID + "#" + securityGroupID).getExitCode(), 0);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
        try {
            PowerMockito.doThrow(new MangleException(ErrorCode.AWS_INSTANCE_NOT_FOUND, instanceID))
                    .when(AWSCommonUtils.class, "setInstanceSecurityGroups", any(), any(), any());
            Assert.assertEquals(EC2InstanceFaultOperations.unblockAllNetworkTraffic(customAwsClient,
                    instanceID + "#" + securityGroupID + "#" + securityGroupID).getExitCode(), 1);
        } catch (Exception exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * Test method for {@link#EC2InstanceFaultOperations#detachVolumesFromInstance }.
     *
     * @throws Exception
     */
    @Test
    public void testDetachVolumesFromInstance() {
        Map<String, String> attachedVolumes = new HashMap<>();
        attachedVolumes.put("volume-1", "/dev/sdf");
        try {
            PowerMockito.when(AWSCommonUtils.getAttachedVolumes(any(), any(), anyBoolean()))
                    .thenReturn(attachedVolumes);
            PowerMockito.doNothing().when(AWSCommonUtils.class, "detachVolumes", any(), any(), any());
            Assert.assertEquals(EC2InstanceFaultOperations
                    .detachVolumesFromInstance(customAwsClient, "true", instanceID).getExitCode(), 0);
            PowerMockito.verifyStatic(AWSCommonUtils.class, times(1));
            AWSCommonUtils.detachVolumes(any(), any(), any());
        } catch (Exception exception) {
            Assert.fail("Test deatch volumes thrown unexpected expcetion" + exception.getMessage());
        }

        try {
            PowerMockito.when(AWSCommonUtils.getAttachedVolumes(any(), any(), anyBoolean()))
                    .thenReturn(attachedVolumes);
            PowerMockito.doThrow(new MangleException("TestDetachVolumeFailed", ErrorCode.AWS_OPERATION_FAILURE))
                    .when(AWSCommonUtils.class);
            AWSCommonUtils.detachVolumes(any(), any(), any());
            Assert.assertEquals(EC2InstanceFaultOperations
                    .detachVolumesFromInstance(customAwsClient, "true", instanceID).getExitCode(), 1);
        } catch (Exception exception) {
            Assert.fail("Test deatch volumes thrown unexpected expcetion" + exception.getMessage());
        }
    }

    /**
     * Test method for {@link#EC2InstanceFaultOperations#attachVolumesFromInstance }.
     *
     * @throws Exception
     */
    @Test
    public void testAttachVolumesFromInstance() {
        Map<String, String> attachedVolumes = new HashMap<>();
        attachedVolumes.put("volume-1", "/dev/sdf");
        Future<RebootInstancesResult> result = getFutureObjectForRebootInstances();
        when(customAwsClient.ec2Client()).thenReturn(ec2Client);
        when(ec2Client.rebootInstancesAsync(any(RebootInstancesRequest.class), any(AsyncHandler.class)))
                .thenReturn(result);
        try {
            PowerMockito.when(AWSCommonUtils.getAttachedVolumes(any(), any(), anyBoolean()))
                    .thenReturn(attachedVolumes);
            PowerMockito.doNothing().when(AWSCommonUtils.class, "attachVolumes", any(), any(), any());
            Assert.assertEquals(EC2InstanceFaultOperations
                    .attachVolumesToInstance(customAwsClient, instanceID + "#" + "volume-1=/dev/sdf").getExitCode(), 0);
            PowerMockito.verifyStatic(AWSCommonUtils.class, times(1));
            AWSCommonUtils.attachVolumes(any(), any(), any());
        } catch (Exception exception) {
            exception.printStackTrace();
            Assert.fail("Test attach volumes thrown unexpected expcetion" + exception.getMessage());
        }

        try {
            PowerMockito.when(AWSCommonUtils.getAttachedVolumes(any(), any(), anyBoolean()))
                    .thenReturn(attachedVolumes);
            PowerMockito.doThrow(new MangleException("TestAttachVolumeFailed", ErrorCode.AWS_OPERATION_FAILURE))
                    .when(AWSCommonUtils.class);
            AWSCommonUtils.attachVolumes(any(), any(), any());
            Assert.assertEquals(EC2InstanceFaultOperations
                    .attachVolumesToInstance(customAwsClient, instanceID + "#" + "volume-1=/dev/sdf").getExitCode(), 1);
        } catch (Exception exception) {
            Assert.fail("Test deatch volumes thrown unexpected expcetion" + exception.getMessage());
        }
    }

    private Future<TerminateInstancesResult> getFutureObjectForTerminateInstances() {
        return new Future<TerminateInstancesResult>() {

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public TerminateInstancesResult get() throws InterruptedException, ExecutionException {
                return new TerminateInstancesResult()
                        .withTerminatingInstances(new InstanceStateChange().withInstanceId(instanceID));
            }

            @Override
            public TerminateInstancesResult get(long timeout, TimeUnit unit)
                    throws InterruptedException, ExecutionException, TimeoutException {
                return new TerminateInstancesResult()
                        .withTerminatingInstances(new InstanceStateChange().withInstanceId(instanceID));
            }
        };
    }

    private Future<StartInstancesResult> getFutureObjectForStartInstances() {
        return new Future<StartInstancesResult>() {

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public StartInstancesResult get() throws InterruptedException, ExecutionException {
                return new StartInstancesResult()
                        .withStartingInstances(new InstanceStateChange().withInstanceId(instanceID));
            }

            @Override
            public StartInstancesResult get(long timeout, TimeUnit unit)
                    throws InterruptedException, ExecutionException, TimeoutException {
                return new StartInstancesResult()
                        .withStartingInstances(new InstanceStateChange().withInstanceId(instanceID));
            }
        };
    }

    private Future<StopInstancesResult> getFutureObjectForStopInstances() {
        return new Future<StopInstancesResult>() {

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public StopInstancesResult get() throws InterruptedException, ExecutionException {
                return new StopInstancesResult()
                        .withStoppingInstances(new InstanceStateChange().withInstanceId(instanceID));
            }

            @Override
            public StopInstancesResult get(long timeout, TimeUnit unit)
                    throws InterruptedException, ExecutionException, TimeoutException {
                return new StopInstancesResult()
                        .withStoppingInstances(new InstanceStateChange().withInstanceId(instanceID));
            }
        };
    }

    private Future<RebootInstancesResult> getFutureObjectForRebootInstances() {
        return new Future<RebootInstancesResult>() {

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public RebootInstancesResult get() throws InterruptedException, ExecutionException {
                return new RebootInstancesResult();
            }

            @Override
            public RebootInstancesResult get(long timeout, TimeUnit unit)
                    throws InterruptedException, ExecutionException, TimeoutException {
                return new RebootInstancesResult();
            }
        };
    }
}
