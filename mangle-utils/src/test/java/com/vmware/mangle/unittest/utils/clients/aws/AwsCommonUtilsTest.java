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
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2Async;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.GroupIdentifier;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.ModifyInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.SecurityGroup;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.utils.clients.aws.AWSCommonUtils;
import com.vmware.mangle.utils.clients.aws.CustomAwsClient;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;


/**
 * Unit Test Case for AwsCommonUtils.
 *
 * @author bkaranam
 */
public class AwsCommonUtilsTest {

    @Mock
    private CustomAwsClient customAwsClient;

    @Mock
    private AmazonEC2Async ec2Client;

    private HashMap<String, String> awsTags;

    private String instanceID = "DummyInstanceID";
    private String securityGroupID = "DummySecurityGroupID";
    private String vpcID = "DummyVpcId";
    private String securityGroupName = "DummySecurityGroup";

    /**
     * @throws Exception
     */
    @BeforeMethod
    public void setUpBeforeMethod() throws Exception {
        MockitoAnnotations.initMocks(this);
        awsTags = new HashMap<>();
        awsTags.put("Env", "MockTest");
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
     * Test method for {@link#AWSCommonUtils#getAwsInstances }.
     */
    @Test
    public void testGetAwsInstances() {
        DescribeInstancesResult result = new DescribeInstancesResult();
        List<Reservation> reservations = new ArrayList<>();
        Instance instance = new Instance();
        instance.setInstanceId("Dummy");
        instance.setState(new InstanceState().withName("running"));
        reservations.add(new Reservation().withInstances(instance));
        result.setReservations(reservations);
        when(customAwsClient.ec2Client()).thenReturn(ec2Client);
        when(ec2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(result);
        try {
            Assert.assertEquals(AWSCommonUtils.getAwsInstances(customAwsClient, awsTags, true).get(0), "Dummy");
            Assert.assertEquals(AWSCommonUtils.getAwsInstances(customAwsClient, awsTags, false).get(0), "Dummy");
            Assert.assertEquals(AWSCommonUtils.getAwsInstances(customAwsClient, null, false).size(), 0);
        } catch (MangleException exception) {
            Assert.assertTrue(false);
        }
    }

    /**
     * Test method for {@link#AWSCommonUtils#findSecurityGroup }.
     *
     * @throws Exception
     */
    @Test
    public void testFindSecurityGroup() throws Exception {
        DescribeInstancesResult result = new DescribeInstancesResult();
        List<Reservation> reservations = new ArrayList<>();
        Instance instance = new Instance();
        instance.setInstanceId("Dummy");
        instance.setVpcId("DummyVpcId");
        DescribeSecurityGroupsResult securityGroupResult = new DescribeSecurityGroupsResult();
        SecurityGroup securityGroup = new SecurityGroup();
        securityGroup.setGroupId("DummySecurityGroupID");
        securityGroup.setVpcId("DummyVpcId");
        securityGroupResult.withSecurityGroups(securityGroup);

        reservations.add(new Reservation().withInstances(instance));
        result.setReservations(reservations);
        when(customAwsClient.ec2Client()).thenReturn(ec2Client);
        when(ec2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(result);
        when(ec2Client.describeSecurityGroups(any(DescribeSecurityGroupsRequest.class)))
                .thenReturn(securityGroupResult);
        Assert.assertEquals(AWSCommonUtils.findSecurityGroup(customAwsClient, "Dummy", "DummySecurityGroup"),
                "DummySecurityGroupID");
    }

    /**
     * Test method for {@link#AWSCommonUtils#getSecurityGroupIDs }.
     *
     * @throws Exception
     */
    @Test
    public void testGetSecurityGroupIDs() throws Exception {
        DescribeInstancesResult result = new DescribeInstancesResult();
        List<Reservation> reservations = new ArrayList<>();
        Instance instance = new Instance();
        instance.setInstanceId("DummyInstanceID");
        GroupIdentifier securityGroupIdentifier = new GroupIdentifier();
        securityGroupIdentifier.setGroupId("DummySecurityGroupID");
        instance.withSecurityGroups(securityGroupIdentifier);
        reservations.add(new Reservation().withInstances(instance));
        result.setReservations(reservations);
        when(customAwsClient.ec2Client()).thenReturn(ec2Client);
        when(ec2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(result);
        Assert.assertEquals(AWSCommonUtils.getSecurityGroupIDs(customAwsClient, "DummyInstanceID").get(0),
                "DummySecurityGroupID");
    }

    /**
     * Test method for {@link#AWSCommonUtils#setInstanceSecurityGroups }
     */
    @Test
    public void testSetInstanceSecurityGroups() {
        when(customAwsClient.ec2Client()).thenReturn(ec2Client);
        when(ec2Client.modifyInstanceAttribute(any(ModifyInstanceAttributeRequest.class))).thenReturn(null);
        try {
            AWSCommonUtils.setInstanceSecurityGroups(customAwsClient, instanceID,
                    Stream.of(securityGroupID).collect(Collectors.toList()));
        } catch (MangleException exception) {
            Assert.assertTrue(false);
        }
        AmazonServiceException awsServiceException = new AmazonServiceException("AmazonServiceException");
        awsServiceException.setErrorCode("ERROR_CODE");
        when(ec2Client.modifyInstanceAttribute(any(ModifyInstanceAttributeRequest.class)))
                .thenThrow(awsServiceException);
        try {
            AWSCommonUtils.setInstanceSecurityGroups(customAwsClient, instanceID,
                    Stream.of(securityGroupID).collect(Collectors.toList()));
        } catch (MangleException exception) {
            Assert.assertTrue(true);
            Assert.assertEquals(exception.getErrorCode(), ErrorCode.AWS_UNKNOWN_ERROR);
        }

        awsServiceException.setErrorCode("InvalidInstanceID.NotFound");
        try {
            AWSCommonUtils.setInstanceSecurityGroups(customAwsClient, instanceID,
                    Stream.of(securityGroupID).collect(Collectors.toList()));
        } catch (MangleException exception) {
            Assert.assertTrue(true);
            Assert.assertEquals(exception.getErrorCode(), ErrorCode.AWS_INSTANCE_NOT_FOUND);
        }


    }

    /**
     * Test method for {@link#AWSCommonUtils#createSecurityGroup }.
     */
    @Test
    public void testCreateSecurityGroup() {
        DescribeInstancesResult describeInstancesResult = new DescribeInstancesResult();
        List<Reservation> reservations = new ArrayList<>();
        Instance instance = new Instance();
        instance.setInstanceId(instanceID);
        instance.setVpcId(vpcID);
        reservations.add(new Reservation().withInstances(instance));
        describeInstancesResult.setReservations(reservations);
        CreateSecurityGroupResult createSecurityGroupresult = new CreateSecurityGroupResult();
        createSecurityGroupresult.withGroupId(securityGroupID);
        when(customAwsClient.ec2Client()).thenReturn(ec2Client);
        when(ec2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(describeInstancesResult);
        when(ec2Client.createSecurityGroup(any(CreateSecurityGroupRequest.class)))
                .thenReturn(createSecurityGroupresult);
        Assert.assertEquals(AWSCommonUtils.createSecurityGroup(customAwsClient, instanceID, securityGroupName,
                "Security Group Create for Mangle Test"), securityGroupID);
    }


    /**
     * Test method for {@link#AWSCommonUtils#deleteSecurityGroup }.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testDeleteSecurityGroup() {
        when(customAwsClient.ec2Client()).thenReturn(ec2Client);
        when(ec2Client.deleteSecurityGroup(any(DeleteSecurityGroupRequest.class))).thenReturn(null);
        try {
            AWSCommonUtils.deleteSecurityGroup(customAwsClient, securityGroupID);
        } catch (MangleException exception) {
            Assert.assertTrue(false);
        }
        try {
            when(ec2Client.deleteSecurityGroup(any(DeleteSecurityGroupRequest.class)))
                    .thenThrow(AmazonEC2Exception.class);
            AWSCommonUtils.deleteSecurityGroup(customAwsClient, securityGroupID);
        } catch (MangleException exception) {
            Assert.assertEquals(ErrorCode.AWS_FAILED_TO_DELETE_SECURITY_GROUP, exception.getErrorCode());
            Assert.assertTrue(true);
        }
    }


    /**
     * Test method for {@link#AWSCommonUtils#testDescribeSecurityGroups }.
     */
    @Test
    public void testDescribeSecurityGroups() {
        AmazonServiceException awsServiceException = new AmazonServiceException("AmazonServiceException");
        awsServiceException.setErrorCode("InvalidGroup.NotFound");
        when(customAwsClient.ec2Client()).thenReturn(ec2Client);
        when(ec2Client.describeSecurityGroups(any(DescribeSecurityGroupsRequest.class))).thenThrow(awsServiceException);
        try {
            Assert.assertTrue(AWSCommonUtils.describeSecurityGroups(customAwsClient, securityGroupName).isEmpty());
        } catch (MangleRuntimeException exception) {
            Assert.assertTrue(false);
        }
        awsServiceException.setErrorCode("InvalidError");
        try {
            AWSCommonUtils.describeSecurityGroups(customAwsClient, securityGroupName);
        } catch (MangleRuntimeException exception) {
            Assert.assertTrue(true);
        }
    }

}
