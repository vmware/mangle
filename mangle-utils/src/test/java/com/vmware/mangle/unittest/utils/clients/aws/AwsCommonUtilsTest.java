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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2Async;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.AttachVolumeRequest;
import com.amazonaws.services.ec2.model.AttachVolumeResult;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.DescribeVolumesRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesResult;
import com.amazonaws.services.ec2.model.DetachVolumeRequest;
import com.amazonaws.services.ec2.model.DetachVolumeResult;
import com.amazonaws.services.ec2.model.EbsInstanceBlockDevice;
import com.amazonaws.services.ec2.model.GroupIdentifier;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceBlockDeviceMapping;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.ModifyInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.Volume;
import com.amazonaws.services.ec2.model.VolumeState;
import com.amazonaws.services.rds.AmazonRDSAsync;
import com.amazonaws.services.rds.model.AmazonRDSException;
import com.amazonaws.services.rds.model.DBCluster;
import com.amazonaws.services.rds.model.DBClusterMember;
import com.amazonaws.services.rds.model.DBClusterNotFoundException;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DBInstanceNotFoundException;
import com.amazonaws.services.rds.model.DescribeDBClustersRequest;
import com.amazonaws.services.rds.model.DescribeDBClustersResult;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;
import com.amazonaws.services.rds.model.Endpoint;
import com.amazonaws.services.rds.model.FailoverDBClusterRequest;
import com.amazonaws.services.rds.model.ModifyDBInstanceRequest;
import com.amazonaws.services.rds.model.RebootDBInstanceRequest;
import com.amazonaws.services.rds.model.StartDBClusterRequest;
import com.amazonaws.services.rds.model.StartDBInstanceRequest;
import com.amazonaws.services.rds.model.StopDBClusterRequest;
import com.amazonaws.services.rds.model.StopDBInstanceRequest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.model.aws.AwsRDSInstance;
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

    @Mock
    private AmazonRDSAsync rdsClient;

    private HashMap<String, String> awsTags;

    private String instanceID = "DummyInstanceID";
    private String securityGroupID = "DummySecurityGroupID";
    private String vpcID = "DummyVpcId";
    private String securityGroupName = "DummySecurityGroup";
    private String dbInstanceIdentifier = "instance-1";
    private String dbClusterIdentifier = "cluster-1";

    private Instance instance = null;
    private DBInstance dbInstance = null;

    private int dbPort = 5432;

    /**
     * @throws Exception
     */
    @BeforeMethod
    public void setUpBeforeMethod() throws Exception {
        MockitoAnnotations.initMocks(this);
        awsTags = new HashMap<>();
        awsTags.put("Env", "MockTest");
        DescribeInstancesResult result = new DescribeInstancesResult();
        List<Reservation> reservations = new ArrayList<>();
        instance = new Instance();
        instance.setInstanceId("Dummy");
        instance.setState(new InstanceState().withName("running"));
        reservations.add(new Reservation().withInstances(instance));
        result.setReservations(reservations);
        when(customAwsClient.ec2Client()).thenReturn(ec2Client);
        when(customAwsClient.rdsClient()).thenReturn(rdsClient);
        when(ec2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(result);

        DescribeDBInstancesResult rdsResult = new DescribeDBInstancesResult();
        Collection<DBInstance> dBInstances = new ArrayList<>();
        dbInstance = new DBInstance();
        dbInstance.setDBClusterIdentifier("cluster-1");
        dbInstance.setDBInstanceIdentifier("instance-1");
        dbInstance.setEngine("postgres");
        Endpoint endpoint = new Endpoint();
        endpoint.setPort(dbPort);
        dbInstance.setEndpoint(endpoint);
        dBInstances.add(dbInstance);
        rdsResult.setDBInstances(dBInstances);
        when(rdsClient.describeDBInstances(any(DescribeDBInstancesRequest.class))).thenReturn(rdsResult);
        when(rdsClient.describeDBInstances()).thenReturn(rdsResult);

        DescribeDBClustersResult rdsClusterResult = new DescribeDBClustersResult();
        Collection<DBCluster> dBClusters = new ArrayList<>();
        DBCluster dbCluster = new DBCluster();
        dbCluster.setDBClusterIdentifier(dbInstance.getDBClusterIdentifier());
        Collection<DBClusterMember> dBClusterMembers = new ArrayList<>();
        DBClusterMember clusterMember = new DBClusterMember();
        clusterMember.setDBInstanceIdentifier(dbInstance.getDBInstanceIdentifier());
        clusterMember.setIsClusterWriter(true);
        dBClusterMembers.add(clusterMember);
        dbCluster.setDBClusterMembers(dBClusterMembers);
        dBClusters.add(dbCluster);
        rdsClusterResult.setDBClusters(dBClusters);
        when(rdsClient.describeDBClusters(any(DescribeDBClustersRequest.class))).thenReturn(rdsClusterResult);
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
        when(ec2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(result);
        Assert.assertEquals(AWSCommonUtils.getSecurityGroupIDs(customAwsClient, "DummyInstanceID").get(0),
                "DummySecurityGroupID");
    }

    /**
     * Test method for {@link#AWSCommonUtils#setInstanceSecurityGroups }
     */
    @Test
    public void testSetInstanceSecurityGroups() {
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
     *
     * @throws MangleException
     */
    @Test
    public void testCreateSecurityGroup() throws MangleException {
        DescribeInstancesResult describeInstancesResult = new DescribeInstancesResult();
        List<Reservation> reservations = new ArrayList<>();
        Instance instance = new Instance();
        instance.setInstanceId(instanceID);
        instance.setVpcId(vpcID);
        reservations.add(new Reservation().withInstances(instance));
        describeInstancesResult.setReservations(reservations);
        CreateSecurityGroupResult createSecurityGroupresult = new CreateSecurityGroupResult();
        createSecurityGroupresult.withGroupId(securityGroupID);
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

    /**
     * Test method for {@link#AWSCommonUtils#testGetAttachedVolumes }.
     */
    @Test
    public void testGetAttachedVolumes() {
        InstanceBlockDeviceMapping ibdm = new InstanceBlockDeviceMapping();
        ibdm.setDeviceName("/dev/sdf");
        EbsInstanceBlockDevice ebs = new EbsInstanceBlockDevice();
        ebs.setVolumeId("volumeid-1");
        ibdm.setEbs(ebs);
        instance.setBlockDeviceMappings(Arrays.asList(ibdm));
        try {
            Map<String, String> volume_map =
                    AWSCommonUtils.getAttachedVolumes(customAwsClient, instance.getInstanceId(), true);
            Assert.assertFalse(volume_map.isEmpty());
            Assert.assertEquals(volume_map.get("volumeid-1"), "/dev/sdf");
        } catch (MangleException exception) {
            Assert.fail("testGetAttachedVolumes thrown unexpected exception:" + exception.getMessage());
        }

        try {
            ibdm.setEbs(null);
            Map<String, String> volume_map =
                    AWSCommonUtils.getAttachedVolumes(customAwsClient, instance.getInstanceId(), false);
            Assert.assertTrue(volume_map.isEmpty());
        } catch (MangleException exception) {
            Assert.assertEquals(exception.getErrorCode(), ErrorCode.AWS_DESCRIBE_RESOURCES_FAILED);
        }
    }

    /**
     * Test method for {@link#AWSCommonUtils#testAttachVolumes }.
     */
    @Test
    public void testAttachVolumes() {
        Map<String, String> volume_map = new HashMap<>();
        volume_map.put("volumeid-1", "/dev/sdf");
        DescribeVolumesResult describeVolumeResult = new DescribeVolumesResult();
        Volume volume = new Volume();
        volume.setState(VolumeState.InUse.toString());
        describeVolumeResult.setVolumes(Arrays.asList(volume));

        when(ec2Client.attachVolume(any(AttachVolumeRequest.class))).thenReturn(new AttachVolumeResult());
        when(ec2Client.describeVolumes(any(DescribeVolumesRequest.class))).thenReturn(describeVolumeResult);
        try {
            AWSCommonUtils.attachVolumes(customAwsClient, instance.getInstanceId(), volume_map);
            verify(ec2Client, times(1)).attachVolume(any(AttachVolumeRequest.class));
        } catch (MangleException exception) {
            Assert.fail("testGetAttachedVolumes thrown unexpected exception:" + exception.getMessage());
        }

        when(ec2Client.attachVolume(any(AttachVolumeRequest.class)))
                .thenThrow(new AmazonServiceException("AttachVolumesFailed"));
        when(ec2Client.describeVolumes(any(DescribeVolumesRequest.class))).thenReturn(describeVolumeResult);
        try {
            AWSCommonUtils.attachVolumes(customAwsClient, instance.getInstanceId(), volume_map);
            Assert.fail("Attach volumes test expected to throw expcetion");
        } catch (MangleException exception) {
            Assert.assertEquals(exception.getErrorCode(), ErrorCode.AWS_OPERATION_FAILURE);
            Assert.assertTrue(exception.getMessage().contains("AttachVolumesFailed"));
        }
    }

    /**
     * Test method for {@link#AWSCommonUtils#testDetachVolumes }.
     */
    @Test
    public void testDetachVolumes() {
        Set<String> volumeIds = new HashSet<>();
        volumeIds.add("volumeid-1");
        DescribeVolumesResult describeVolumeResult = new DescribeVolumesResult();
        Volume volume = new Volume();
        volume.setState(VolumeState.Available.toString());
        describeVolumeResult.setVolumes(Arrays.asList(volume));
        when(ec2Client.detachVolume(any(DetachVolumeRequest.class))).thenReturn(new DetachVolumeResult());
        when(ec2Client.describeVolumes(any(DescribeVolumesRequest.class))).thenReturn(describeVolumeResult);
        try {
            AWSCommonUtils.detachVolumes(customAwsClient, instance.getInstanceId(), volumeIds);
            verify(ec2Client, times(1)).detachVolume(any(DetachVolumeRequest.class));
        } catch (MangleException exception) {
            Assert.fail("testGetAttachedVolumes thrown unexpected exception:" + exception.getMessage());
        }

        when(ec2Client.detachVolume(any(DetachVolumeRequest.class)))
                .thenThrow(new AmazonServiceException("DetachVolumesFailed"));
        when(ec2Client.describeVolumes(any(DescribeVolumesRequest.class))).thenReturn(describeVolumeResult);
        try {
            volumeIds.add("volumeid-2");
            AWSCommonUtils.detachVolumes(customAwsClient, instance.getInstanceId(), volumeIds);
            Assert.fail("Detach volumes test expected to throw expcetion");
        } catch (MangleException exception) {
            Assert.assertEquals(exception.getErrorCode(), ErrorCode.AWS_OPERATION_FAILURE);
            Assert.assertTrue(exception.getMessage().contains("DetachVolumesFailed"));
        }
    }

    @Test
    public void testDescribeRDSInstances() throws MangleException {
        List<AwsRDSInstance> instances = AWSCommonUtils.describeRDSInstances(customAwsClient, dbInstanceIdentifier);
        Assert.assertEquals(instances.size(), 1);
        instances = AWSCommonUtils.describeRDSInstances(customAwsClient, null);
        Assert.assertEquals(instances.size(), 1);
        when(rdsClient.describeDBInstances(any(DescribeDBInstancesRequest.class)))
                .thenThrow(new DBInstanceNotFoundException(""));
        instances = AWSCommonUtils.describeRDSInstances(customAwsClient, dbInstanceIdentifier);
        Assert.assertEquals(instances.size(), 1);
        when(rdsClient.describeDBClusters(any(DescribeDBClustersRequest.class)))
                .thenReturn(new DescribeDBClustersResult());
        instances = AWSCommonUtils.describeRDSInstances(customAwsClient, dbInstanceIdentifier);
        Assert.assertEquals(instances.size(), 0);
        when(rdsClient.describeDBClusters(any(DescribeDBClustersRequest.class)))
                .thenThrow(new DBClusterNotFoundException(""));
        try {
            instances = AWSCommonUtils.describeRDSInstances(customAwsClient, dbInstanceIdentifier);
            Assert.fail("Expected mangle exception not thrown");
        } catch (MangleException exception) {
            assertEquals(exception.getErrorCode(), ErrorCode.AWS_DESCRIBE_RESOURCES_FAILED);
        }
    }

    @Test
    public void testVerifyAndSelectInstances() throws MangleException {
        List<AwsRDSInstance> instances = AWSCommonUtils.verifyAndSelectInstances(customAwsClient,
                Arrays.asList(dbInstanceIdentifier, "instance-2"), true);
        assertEquals(instances.size(), 1);
        instances = AWSCommonUtils.verifyAndSelectInstances(customAwsClient,
                Arrays.asList(dbInstanceIdentifier, "instance-2"), false);
        assertEquals(instances.size(), 2);
        instances = AWSCommonUtils.verifyAndSelectInstances(customAwsClient, new ArrayList<>(), false);
        assertEquals(instances.size(), 0);
        instances = AWSCommonUtils.verifyAndSelectInstances(customAwsClient, Arrays.asList(dbInstanceIdentifier), true);
        assertEquals(instances.size(), 1);
        instances =
                AWSCommonUtils.verifyAndSelectInstances(customAwsClient, Arrays.asList(dbInstanceIdentifier), false);
        assertEquals(instances.size(), 1);
    }

    @Test
    public void testStartRDS_instances_with_cluster() throws MangleException {
        when(rdsClient.startDBCluster(any(StartDBClusterRequest.class))).thenReturn(new DBCluster());
        AWSCommonUtils.startRDS_instances(customAwsClient, dbInstanceIdentifier);
        verify(rdsClient, times(1)).startDBCluster(any(StartDBClusterRequest.class));
        verify(rdsClient, times(0)).startDBInstance(any(StartDBInstanceRequest.class));
        when(rdsClient.startDBCluster(any(StartDBClusterRequest.class))).thenThrow(new AmazonRDSException(""));
        try {
            AWSCommonUtils.startRDS_instances(customAwsClient, dbInstanceIdentifier);
            Assert.fail("Test start rds instances not thrown MangleException as expected");
        } catch (MangleException exception) {
            assertEquals(exception.getErrorCode(), ErrorCode.AWS_OPERATION_FAILURE);
            verify(rdsClient, times(2)).startDBCluster(any(StartDBClusterRequest.class));
            verify(rdsClient, times(0)).startDBInstance(any(StartDBInstanceRequest.class));
        }
    }

    @Test
    public void testStartRDS_instances() throws MangleException {
        DescribeDBInstancesResult rdsResult = new DescribeDBInstancesResult();
        Collection<DBInstance> dBInstances = new ArrayList<>();
        dbInstance.setDBClusterIdentifier(null);
        dBInstances.add(dbInstance);
        rdsResult.setDBInstances(dBInstances);
        when(rdsClient.describeDBInstances(any(DescribeDBInstancesRequest.class))).thenReturn(rdsResult);
        when(rdsClient.startDBInstance(any(StartDBInstanceRequest.class))).thenReturn(dbInstance);
        AWSCommonUtils.startRDS_instances(customAwsClient, dbInstanceIdentifier);

        verify(rdsClient, times(0)).startDBCluster(any(StartDBClusterRequest.class));
        verify(rdsClient, times(1)).startDBInstance(any(StartDBInstanceRequest.class));


    }

    @Test
    public void testStopRDS_instances_with_cluster() throws MangleException {
        when(rdsClient.stopDBCluster(any(StopDBClusterRequest.class))).thenReturn(new DBCluster());
        AWSCommonUtils.stopRDS_instances(customAwsClient, dbInstanceIdentifier);
        verify(rdsClient, times(1)).stopDBCluster(any(StopDBClusterRequest.class));
        verify(rdsClient, times(0)).stopDBInstance(any(StopDBInstanceRequest.class));
        when(rdsClient.stopDBCluster(any(StopDBClusterRequest.class))).thenThrow(new AmazonRDSException(""));
        try {
            AWSCommonUtils.stopRDS_instances(customAwsClient, dbInstanceIdentifier);
            Assert.fail("Test stop rds instances not thrown MangleException as expected");
        } catch (MangleException exception) {
            assertEquals(exception.getErrorCode(), ErrorCode.AWS_OPERATION_FAILURE);
            verify(rdsClient, times(2)).stopDBCluster(any(StopDBClusterRequest.class));
            verify(rdsClient, times(0)).stopDBInstance(any(StopDBInstanceRequest.class));
        }
    }

    @Test
    public void testStopRDS_instances() throws MangleException {
        DescribeDBInstancesResult rdsResult = new DescribeDBInstancesResult();
        Collection<DBInstance> dBInstances = new ArrayList<>();
        dbInstance.setDBClusterIdentifier(null);
        dBInstances.add(dbInstance);
        rdsResult.setDBInstances(dBInstances);
        when(rdsClient.describeDBInstances(any(DescribeDBInstancesRequest.class))).thenReturn(rdsResult);
        when(rdsClient.stopDBInstance(any(StopDBInstanceRequest.class))).thenReturn(dbInstance);
        AWSCommonUtils.stopRDS_instances(customAwsClient, dbInstanceIdentifier);

        verify(rdsClient, times(0)).stopDBCluster(any(StopDBClusterRequest.class));
        verify(rdsClient, times(1)).stopDBInstance(any(StopDBInstanceRequest.class));
    }

    @Test
    public void testRebootRDS_instances() throws MangleException {
        when(rdsClient.rebootDBInstance(any(RebootDBInstanceRequest.class))).thenReturn(dbInstance);
        AWSCommonUtils.rebootRDS_instances(customAwsClient, dbInstanceIdentifier);
        verify(rdsClient, times(1)).rebootDBInstance(any(RebootDBInstanceRequest.class));
        when(rdsClient.rebootDBInstance(any(RebootDBInstanceRequest.class))).thenThrow(new AmazonRDSException(""));
        try {
            AWSCommonUtils.rebootRDS_instances(customAwsClient, dbInstanceIdentifier);
            Assert.fail("Test reboot rds instances not thrown MangleException as expected");
        } catch (MangleException exception) {
            assertEquals(exception.getErrorCode(), ErrorCode.AWS_OPERATION_FAILURE);
            verify(rdsClient, times(2)).rebootDBInstance(any(RebootDBInstanceRequest.class));
        }

    }

    @Test
    public void testFailoverRDS_instances_with_cluster() throws MangleException {
        when(rdsClient.failoverDBCluster(any(FailoverDBClusterRequest.class))).thenReturn(new DBCluster());
        AWSCommonUtils.failoverRDS_instances(customAwsClient, dbInstanceIdentifier);
        verify(rdsClient, times(1)).failoverDBCluster(any(FailoverDBClusterRequest.class));

        when(rdsClient.failoverDBCluster(any(FailoverDBClusterRequest.class))).thenThrow(new AmazonRDSException(""));
        try {
            AWSCommonUtils.failoverRDS_instances(customAwsClient, dbInstanceIdentifier);
            Assert.fail("Test failover rds instances not thrown MangleException as expected");
        } catch (MangleException exception) {
            assertEquals(exception.getErrorCode(), ErrorCode.AWS_OPERATION_FAILURE);
            verify(rdsClient, times(2)).failoverDBCluster(any(FailoverDBClusterRequest.class));
        }
    }

    @Test
    public void testFailoverRDS_instances() throws MangleException {
        DescribeDBInstancesResult rdsResult = new DescribeDBInstancesResult();
        Collection<DBInstance> dBInstances = new ArrayList<>();
        dbInstance.setDBClusterIdentifier(null);
        dBInstances.add(dbInstance);
        rdsResult.setDBInstances(dBInstances);
        when(rdsClient.describeDBInstances(any(DescribeDBInstancesRequest.class))).thenReturn(rdsResult);
        when(rdsClient.failoverDBCluster(any(FailoverDBClusterRequest.class))).thenReturn(new DBCluster());
        try {
            AWSCommonUtils.failoverRDS_instances(customAwsClient, dbInstanceIdentifier);
            Assert.fail("Test failover rds instances not thrown MangleException as expected");
        } catch (MangleException exception) {
            assertEquals(exception.getErrorCode(), ErrorCode.AWS_RDS_OPERATION_NOT_SUPPORTED);
            verify(rdsClient, times(0)).failoverDBCluster(any(FailoverDBClusterRequest.class));
        }
    }

    @Test
    public void testManipulateInstancesDBPort() {
        when(rdsClient.modifyDBInstance(any(ModifyDBInstanceRequest.class))).thenReturn(dbInstance);
        try {
            List<AwsRDSInstance> rdsInstances =
                    AWSCommonUtils.manipulateInstancesDBPort(customAwsClient, dbInstanceIdentifier);
            assertEquals(rdsInstances.size(), 1);
            verify(rdsClient, times(1)).modifyDBInstance(any(ModifyDBInstanceRequest.class));
        } catch (MangleException exception) {
            Assert.fail("Test connection loss rds instances failed with exception:" + exception.getMessage());
        }
        when(rdsClient.modifyDBInstance(any(ModifyDBInstanceRequest.class))).thenThrow(AmazonRDSException.class);
        try {
            AWSCommonUtils.manipulateInstancesDBPort(customAwsClient, dbInstanceIdentifier);
            Assert.fail("Test connection loss rds instances not thrown AmazonRDSException as expected");
        } catch (MangleException exception) {
            verify(rdsClient, times(2)).modifyDBInstance(any(ModifyDBInstanceRequest.class));
            assertEquals(exception.getErrorCode(), ErrorCode.AWS_OPERATION_FAILURE);
        }
    }

    @Test
    public void testmodifyRDSInstanceWithDBport() {
        AwsRDSInstance rdsInstance = new AwsRDSInstance();
        rdsInstance.setInstanceIdentifier(dbInstanceIdentifier);
        rdsInstance.setDbPort(dbPort);
        when(rdsClient.modifyDBInstance(any(ModifyDBInstanceRequest.class))).thenReturn(dbInstance);
        try {
            AWSCommonUtils.modifyRDSInstanceWithDBport(customAwsClient, rdsInstance);
            verify(rdsClient, times(1)).modifyDBInstance(any(ModifyDBInstanceRequest.class));
        } catch (MangleException exception) {
            Assert.fail("Test connection reset rds instances failed with exception:" + exception.getMessage());
        }
        when(rdsClient.modifyDBInstance(any(ModifyDBInstanceRequest.class))).thenThrow(AmazonRDSException.class);
        try {
            AWSCommonUtils.modifyRDSInstanceWithDBport(customAwsClient, rdsInstance);
            Assert.fail("Test connection reset rds instances not thrown AmazonRDSException as expected");
        } catch (MangleException exception) {
            verify(rdsClient, times(2)).modifyDBInstance(any(ModifyDBInstanceRequest.class));
            assertEquals(exception.getErrorCode(), ErrorCode.AWS_OPERATION_FAILURE);
        }
    }
}
