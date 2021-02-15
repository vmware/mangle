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

package com.vmware.mangle.utils.clients.aws;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Async;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.AttachVolumeRequest;
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
import com.amazonaws.services.ec2.model.EbsInstanceBlockDevice;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.GroupIdentifier;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceBlockDeviceMapping;
import com.amazonaws.services.ec2.model.ModifyInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.Volume;
import com.amazonaws.services.ec2.model.VolumeState;
import com.amazonaws.services.rds.model.AmazonRDSException;
import com.amazonaws.services.rds.model.DBCluster;
import com.amazonaws.services.rds.model.DBClusterNotFoundException;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DBInstanceNotFoundException;
import com.amazonaws.services.rds.model.DescribeDBClustersRequest;
import com.amazonaws.services.rds.model.DescribeDBClustersResult;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;
import com.amazonaws.services.rds.model.FailoverDBClusterRequest;
import com.amazonaws.services.rds.model.ModifyDBInstanceRequest;
import com.amazonaws.services.rds.model.RebootDBInstanceRequest;
import com.amazonaws.services.rds.model.StartDBClusterRequest;
import com.amazonaws.services.rds.model.StartDBInstanceRequest;
import com.amazonaws.services.rds.model.StopDBClusterRequest;
import com.amazonaws.services.rds.model.StopDBInstanceRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.vmware.mangle.model.aws.AwsRDSFaults;
import com.vmware.mangle.model.aws.AwsRDSInstance;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.RetryUtils;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author bkaranam
 */
@Log4j2
public class AWSCommonUtils {
    private AWSCommonUtils() {

    }

    public static List<String> getAwsInstances(CustomAwsClient client, Map<String, String> awsTags, boolean random)
            throws MangleException {
        if (CollectionUtils.isEmpty(awsTags)) {
            log.debug("awsTags should not be null or empty map");
            return Collections.emptyList();
        }

        AmazonEC2Async ec2Client = client.ec2Client();
        DescribeInstancesRequest describeInstancesRequest =
                new DescribeInstancesRequest().withFilters(getFilters(awsTags));
        DescribeInstancesResult result = ec2Client.describeInstances(describeInstancesRequest);
        if (CollectionUtils.isEmpty(result.getReservations())) {
            throw new MangleException(ErrorCode.AWS_NO_INSTANCES_FOUND,
                    CommonUtils.maptoDelimitedKeyValuePairString(awsTags, ","));
        }
        return getInstancesFromReservations(result.getReservations(), awsTags, random);
    }

    public static String findSecurityGroup(CustomAwsClient client, String instanceId, String groupName)
            throws MangleException {
        String vpcId = getVpcId(client, instanceId);

        SecurityGroup found = null;
        List<SecurityGroup> securityGroups = describeSecurityGroups(client, groupName);
        for (SecurityGroup sg : securityGroups) {
            if (null != vpcId && StringUtils.hasText(vpcId) && vpcId.equals(sg.getVpcId())) {
                if (found != null) {
                    throw new IllegalStateException("Duplicate security groups found");
                }
                found = sg;
            }
        }
        if (found == null) {
            return null;
        }
        return found.getGroupId();
    }

    /**
     * Describe security groups.
     *
     * @param groupNames
     *            list of security group names to search
     *
     * @return list of security groups found
     */
    public static List<SecurityGroup> describeSecurityGroups(CustomAwsClient client, String... groupNames) {
        AmazonEC2Async ec2Client = client.ec2Client();
        DescribeSecurityGroupsRequest request = new DescribeSecurityGroupsRequest();

        if (groupNames == null || groupNames.length == 0) {
            log.info("Getting all EC2 security groups in region");
        } else {
            log.info(String.format("Getting EC2 security groups for %d names", groupNames.length));
            request.withGroupNames(groupNames);
        }

        DescribeSecurityGroupsResult result;
        try {
            result = ec2Client.describeSecurityGroups(request);
        } catch (AmazonServiceException e) {
            if (e.getErrorCode().equals("InvalidGroup.NotFound")) {
                log.info("Got InvalidGroup.NotFound error for security groups; returning empty list");
                return Collections.emptyList();
            }
            throw new MangleRuntimeException(ErrorCode.AWS_UNKNOWN_ERROR, e.getMessage());
        }

        List<SecurityGroup> securityGroups = result.getSecurityGroups();
        log.info(String.format("Got %d EC2 security groups", securityGroups.size()));
        return securityGroups;
    }

    public static String createSecurityGroup(CustomAwsClient client, String instanceId, String name, String description)
            throws MangleException {
        String vpcId = getVpcId(client, instanceId);

        AmazonEC2 ec2Client = client.ec2Client();
        CreateSecurityGroupRequest request = new CreateSecurityGroupRequest();
        request.setGroupName(name);
        request.setDescription(description);
        request.setVpcId(vpcId);

        log.info(String.format("Creating EC2 security group %s.", name));

        CreateSecurityGroupResult result = ec2Client.createSecurityGroup(request);
        return result.getGroupId();
    }

    public static void deleteSecurityGroup(CustomAwsClient client, String securityGroupID) throws MangleException {

        AmazonEC2 ec2Client = client.ec2Client();
        DeleteSecurityGroupRequest request = new DeleteSecurityGroupRequest();
        request.setGroupId(securityGroupID);

        log.info(String.format("Deleting EC2 security group %s.", securityGroupID));
        try {
            ec2Client.deleteSecurityGroup(request);
        } catch (AmazonEC2Exception exception) {
            log.error(exception.getMessage());
            throw new MangleException(ErrorCode.AWS_FAILED_TO_DELETE_SECURITY_GROUP,
                    securityGroupID + " " + exception.getErrorCode());
        }
    }

    /**
     * Gets the VPC id for the given instance.
     *
     * @throws MangleException
     */
    public static List<String> getSecurityGroupIDs(CustomAwsClient client, String instanceId) throws MangleException {
        List<String> securityGroupIDs = new ArrayList<>();
        Instance awsInstance = describeInstance(client, instanceId);
        if (null != awsInstance) {
            List<GroupIdentifier> groupIdentifiers = awsInstance.getSecurityGroups();
            if (CollectionUtils.isEmpty(groupIdentifiers)) {
                return securityGroupIDs;
            }
            for (GroupIdentifier identifier : groupIdentifiers) {
                securityGroupIDs.add(identifier.getGroupId());
            }
        }
        return securityGroupIDs;
    }

    /**
     * Wrapper around describeInstances, for a single instance id.
     *
     * @param instanceId
     *            id of instance to find
     * @return the instance info, or null if instance not found
     * @throws MangleException
     */
    public static Instance describeInstance(CustomAwsClient client, String instanceId) throws MangleException {
        Instance instance = null;
        for (Instance i : describeInstances(client, instanceId)) {
            if (instance != null) {
                throw new IllegalStateException("Duplicate instance: " + instanceId);
            }
            instance = i;
        }
        return instance;
    }


    /**
     * Describe a set of specific instances.
     *
     * @param instanceIds
     *            the instance ids
     * @return the instances
     * @throws MangleException
     */
    public static List<Instance> describeInstances(CustomAwsClient client, String... instanceIds)
            throws MangleException {
        if (instanceIds == null || instanceIds.length == 0) {
            log.info("Getting all EC2 instances");
        } else {
            log.info(String.format("Getting EC2 instances for %d ids", instanceIds.length));
        }

        List<Instance> instances = new LinkedList<>();

        AmazonEC2Async ec2Client = client.ec2Client();
        DescribeInstancesRequest request = new DescribeInstancesRequest();
        if (instanceIds != null) {
            request.withInstanceIds(Arrays.asList(instanceIds));
        }
        try {
            DescribeInstancesResult result = ec2Client.describeInstances(request);
            for (Reservation reservation : result.getReservations()) {
                instances.addAll(reservation.getInstances());
            }
        } catch (AmazonServiceException serviceException) {
            throw new MangleException(ErrorCode.AWS_DESCRIBE_RESOURCES_FAILED, AwsResourceType.INSTANCE,
                    serviceException.getMessage());
        }
        log.info(String.format("Got %d EC2 instances", instances.size()));
        return instances;
    }

    public static List<Filter> getFilters(Map<String, String> tags) {
        List<Filter> filters = new ArrayList<>();
        for (Entry<String, String> entrySet : tags.entrySet()) {
            Filter filter = new Filter("tag:" + entrySet.getKey()).withValues(entrySet.getValue());
            filters.add(filter);
        }
        return filters;
    }

    public static void setInstanceSecurityGroups(CustomAwsClient client, String instanceId, List<String> groupIds)
            throws MangleException {
        try {
            AmazonEC2Async ec2Client = client.ec2Client();
            ModifyInstanceAttributeRequest request = new ModifyInstanceAttributeRequest();
            request.setInstanceId(instanceId);
            request.setGroups(groupIds);
            ec2Client.modifyInstanceAttribute(request);
        } catch (AmazonServiceException e) {
            if (e.getErrorCode().equals("InvalidInstanceID.NotFound")) {
                throw new MangleException(ErrorCode.AWS_INSTANCE_NOT_FOUND, instanceId);
            }
            throw new MangleException(ErrorCode.AWS_UNKNOWN_ERROR, e.getMessage());
        }
    }

    public static Map<String, String> getAttachedVolumes(CustomAwsClient client, String instanceId, boolean random)
            throws MangleException {
        Map<String, String> volumeIdsMap = new HashMap<>();
        try {
            for (Instance instance : describeInstances(client, instanceId)) {
                volumeIdsMap.putAll(getVolumeIdMap(instance));
            }

        } catch (AmazonServiceException serviceException) {
            throw new MangleException(ErrorCode.AWS_DESCRIBE_RESOURCES_FAILED, AwsResourceType.VOLUME,
                    serviceException.getMessage());
        }
        if (CollectionUtils.isEmpty(volumeIdsMap)) {
            throw new MangleException("no external volume attached to instance:" + instanceId,
                    ErrorCode.AWS_DESCRIBE_RESOURCES_FAILED);
        }
        if (random && volumeIdsMap.size() > 1) {
            List<Map.Entry<String, String>> list = new ArrayList<Map.Entry<String, String>>(volumeIdsMap.entrySet());
            Collections.shuffle(list);
            volumeIdsMap.clear();
            volumeIdsMap.put(list.get(0).getKey(), list.get(0).getValue());
        }
        return volumeIdsMap;
    }


    public static void attachVolume(CustomAwsClient client, String instanceId, String volumeId, String deviceName)
            throws MangleException {
        try {
            AttachVolumeRequest attachVolumeRequest =
                    new AttachVolumeRequest().withInstanceId(instanceId).withDevice(deviceName).withVolumeId(volumeId);
            client.ec2Client().attachVolume(attachVolumeRequest);
            verifyVolumeStatus(client, volumeId, VolumeState.InUse);
        } catch (AmazonServiceException serviceException) {
            throw new MangleException(serviceException.getMessage(), ErrorCode.AWS_ATTACH_VOLUME_FAILED, volumeId,
                    instanceId, serviceException.getMessage());
        }
    }

    public static void detachVolume(CustomAwsClient client, String instanceId, String volumeId) throws MangleException {
        try {
            DetachVolumeRequest detachVolumeRequest =
                    new DetachVolumeRequest().withInstanceId(instanceId).withVolumeId(volumeId).withForce(true);
            client.ec2Client().detachVolume(detachVolumeRequest);
            verifyVolumeStatus(client, volumeId, VolumeState.Available);
        } catch (AmazonServiceException serviceException) {
            throw new MangleException(serviceException.getMessage(), ErrorCode.AWS_DETACH_VOLUME_FAILED, volumeId,
                    instanceId, serviceException.getMessage());
        }
    }

    public static void attachVolumes(CustomAwsClient client, String instanceId, Map<String, String> volumeIdsMap)
            throws MangleException {

        List<AwsFailedResource> failedResources = new ArrayList<>();
        volumeIdsMap.entrySet().stream().forEach(entry -> {
            try {
                attachVolume(client, instanceId, entry.getKey(), entry.getValue());
            } catch (MangleException exception) {
                failedResources.add(new AwsFailedResource(AwsResourceType.VOLUME, AwsResourceOperation.ATTACH,
                        entry.getKey(), exception.getMessage()));
            }
        });

        if (!CollectionUtils.isEmpty(failedResources)) {
            throw new MangleException(failedResources.toString(), ErrorCode.AWS_OPERATION_FAILURE,
                    failedResources.toString());
        }
    }

    public static void detachVolumes(CustomAwsClient client, String instanceId, Set<String> volumeIds)
            throws MangleException {
        List<AwsFailedResource> failedResources = new ArrayList<>();
        volumeIds.stream().forEach(id -> {
            try {
                detachVolume(client, instanceId, id);
            } catch (MangleException exception) {
                failedResources.add(new AwsFailedResource(AwsResourceType.VOLUME, AwsResourceOperation.DETACH, id,
                        exception.getMessage()));
            }
        });

        if (!CollectionUtils.isEmpty(failedResources)) {
            throw new MangleException(failedResources.toString(), ErrorCode.AWS_OPERATION_FAILURE,
                    failedResources.toString());
        }
    }


    public static String getRandomSecurityGroupName(String instanceID) {
        return instanceID + "-" + UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }

    /**
     * Describe rds instances using db identifiers
     *
     * @param dbIdentifiers
     *            rds db identifiers
     * @return the rds instances
     * @throws MangleException
     */
    public static List<AwsRDSInstance> describeRDSInstances(CustomAwsClient client, String dBIdentifier)
            throws MangleException {
        if (!StringUtils.hasText(dBIdentifier)) {
            log.info("Getting all RDS instances");
            return getAwsRdsInstancesWithRoles(describeAll_DBInstances(client));
        }
        log.debug("Getting RDS instances for db identifier:{}", dBIdentifier);

        DescribeDBInstancesRequest instancesRequest =
                new DescribeDBInstancesRequest().withDBInstanceIdentifier(dBIdentifier);
        try {
            DescribeDBInstancesResult resultInstances = client.rdsClient().describeDBInstances(instancesRequest);
            return getAwsRdsInstancesWithRoles(resultInstances.getDBInstances());
        } catch (DBInstanceNotFoundException instanceNotFoundException) {
            return getDBInstancesFromCluster(client, dBIdentifier, true);
        }
    }

    public static List<AwsRDSInstance> verifyAndSelectInstances(CustomAwsClient client, List<String> dbIdentifiers,
            boolean selectRandomInstance) throws MangleException {
        List<AwsRDSInstance> awsRdsInstances = new ArrayList<>();
        if (!CollectionUtils.isEmpty(dbIdentifiers) && dbIdentifiers.size() > 1 && selectRandomInstance) {
            Collections.shuffle(dbIdentifiers);
            return describeRDSInstances(client, dbIdentifiers.get(0));
        }
        for (String identifier : dbIdentifiers) {
            awsRdsInstances.addAll(describeRDSInstances(client, identifier));
        }
        return awsRdsInstances;
    }

    public static void startRDS_instances(CustomAwsClient client, String... dbInstanceIdentifiers)
            throws MangleException {
        for (String dbInstanceIdentifier : dbInstanceIdentifiers) {
            try {
                String clusterIdentifier = getClusterIdentifier(client, dbInstanceIdentifier);
                if (StringUtils.hasText(clusterIdentifier)) {
                    StartDBClusterRequest startDBClusterRequest =
                            new StartDBClusterRequest().withDBClusterIdentifier(clusterIdentifier);
                    client.rdsClient().startDBCluster(startDBClusterRequest);
                } else {
                    StartDBInstanceRequest startDBInstanceRequest =
                            new StartDBInstanceRequest().withDBInstanceIdentifier(dbInstanceIdentifier);
                    client.rdsClient().startDBInstance(startDBInstanceRequest);
                }
            } catch (AmazonRDSException rdsException) {
                throw new MangleException(rdsException.getMessage(), ErrorCode.AWS_OPERATION_FAILURE,
                        rdsException.getMessage());
            }
        }
    }

    public static void stopRDS_instances(CustomAwsClient client, String... dbInstanceIdentifiers)
            throws MangleException {
        for (String dbInstanceIdentifier : dbInstanceIdentifiers) {
            try {
                String clusterIdentifier = getClusterIdentifier(client, dbInstanceIdentifier);
                if (StringUtils.hasText(clusterIdentifier)) {
                    StopDBClusterRequest startDBClusterRequest =
                            new StopDBClusterRequest().withDBClusterIdentifier(clusterIdentifier);
                    client.rdsClient().stopDBCluster(startDBClusterRequest);
                } else {
                    StopDBInstanceRequest stopDBInstanceRequest =
                            new StopDBInstanceRequest().withDBInstanceIdentifier(dbInstanceIdentifier);
                    client.rdsClient().stopDBInstance(stopDBInstanceRequest);
                }
            } catch (AmazonRDSException rdsException) {
                throw new MangleException(rdsException.getMessage(), ErrorCode.AWS_OPERATION_FAILURE,
                        rdsException.getMessage());
            }
        }
    }

    public static void rebootRDS_instances(CustomAwsClient client, String... dbInstanceIdentifiers)
            throws MangleException {
        for (String dbInstanceIdentifier : dbInstanceIdentifiers) {
            try {
                RebootDBInstanceRequest rebootDBInstanceRequest =
                        new RebootDBInstanceRequest().withDBInstanceIdentifier(dbInstanceIdentifier);
                client.rdsClient().rebootDBInstance(rebootDBInstanceRequest);
            } catch (AmazonRDSException rdsException) {
                throw new MangleException(rdsException.getMessage(), ErrorCode.AWS_OPERATION_FAILURE,
                        rdsException.getMessage());
            }
        }
    }

    public static void failoverRDS_instances(CustomAwsClient client, String... dbInstanceIdentifiers)
            throws MangleException {
        for (String dbInstanceIdentifier : dbInstanceIdentifiers) {
            try {
                String clusterIdentifier = getClusterIdentifier(client, dbInstanceIdentifier);
                if (StringUtils.hasText(clusterIdentifier)) {
                    FailoverDBClusterRequest failoverDBClusterRequest =
                            new FailoverDBClusterRequest().withTargetDBInstanceIdentifier(dbInstanceIdentifier)
                                    .withDBClusterIdentifier(clusterIdentifier);
                    client.rdsClient().failoverDBCluster(failoverDBClusterRequest);
                } else {
                    String errorMessage = String.format(ErrorConstants.AWS_RDS_OPERATION_NOT_SUPPORTED,
                            AwsRDSFaults.FAILOVER_INSTANCES.name(), dbInstanceIdentifier);
                    log.debug(errorMessage);
                    throw new MangleException(errorMessage, ErrorCode.AWS_RDS_OPERATION_NOT_SUPPORTED,
                            AwsRDSFaults.FAILOVER_INSTANCES.name(), dbInstanceIdentifier);
                }
            } catch (AmazonRDSException rdsException) {
                throw new MangleException(rdsException.getMessage(), ErrorCode.AWS_OPERATION_FAILURE,
                        rdsException.getMessage());
            }
        }
    }

    /**
     * Gets the VPC id for the given instance.
     *
     * @throws MangleException
     */
    private static String getVpcId(CustomAwsClient client, String instanceId) throws MangleException {
        Instance awsInstance = describeInstance(client, instanceId);
        String vpcId = null;
        if (null != awsInstance) {
            vpcId = awsInstance.getVpcId();
        }
        return vpcId;
    }

    private static Map<String, String> getVolumeIdMap(Instance instance) {
        Map<String, String> volumeIdsMap = new HashMap<>();
        String rootDeviceName = instance.getRootDeviceName();
        for (InstanceBlockDeviceMapping ibdm : instance.getBlockDeviceMappings()) {
            EbsInstanceBlockDevice ebs = ibdm.getEbs();
            if (ebs == null) {
                continue;
            }
            String volumeId = ebs.getVolumeId();
            if (StringUtils.isEmpty(volumeId)) {
                continue;
            }

            if (rootDeviceName != null && rootDeviceName.equals(ibdm.getDeviceName())) {
                continue;
            }
            volumeIdsMap.put(volumeId, ibdm.getDeviceName());
        }
        return volumeIdsMap;
    }

    private static List<String> getInstancesFromReservations(List<Reservation> reservations,
            Map<String, String> awsTags, boolean random) throws MangleException {
        List<String> instanceIds = new ArrayList<>();
        for (Reservation reservation : reservations) {
            for (Instance instance : reservation.getInstances()) {
                if (instance.getState().getName().equals("running")) {
                    instanceIds.add(instance.getInstanceId());
                }
            }
        }
        if (CollectionUtils.isEmpty(instanceIds)) {
            throw new MangleException(ErrorCode.AWS_NO_RUNNING_INSTANCES_FOUND,
                    CommonUtils.maptoDelimitedKeyValuePairString(awsTags, ","));
        }
        if (random) {
            Random rand = new Random();
            return Arrays.asList(instanceIds.get(rand.nextInt(instanceIds.size())));
        } else {
            return instanceIds;
        }
    }

    private static void verifyVolumeStatus(CustomAwsClient client, String volumeId, VolumeState state)
            throws MangleException {
        RetryUtils.retry(() -> {
            DescribeVolumesRequest describeVolumeRequest =
                    new DescribeVolumesRequest().withVolumeIds(Arrays.asList(volumeId));
            DescribeVolumesResult describeVolumesResult = client.ec2Client().describeVolumes(describeVolumeRequest);
            Volume volume = describeVolumesResult.getVolumes().get(0);
            String errorMessage = String.format(ErrorConstants.AWS_VOLUME_STATUS_CHANGE_FAILED, volumeId,
                    volume.getState(), state.toString());
            if (!volume.getState().equals(state.toString())) {
                throw new MangleException(errorMessage, ErrorCode.AWS_VOLUME_STATUS_CHANGE_FAILED, errorMessage);
            }
        }, null, 12, 5);
    }

    private static List<AwsRDSInstance> getAwsRdsInstancesWithRoles(List<DBInstance> dbInstances) {
        List<AwsRDSInstance> awsRdsInstances = new ArrayList<>();
        dbInstances.forEach(instance -> {
            AwsRDSInstance awsRdsInstance = new AwsRDSInstance();
            awsRdsInstance.setInstanceIdentifier(instance.getDBInstanceIdentifier());
            awsRdsInstance.setDbEngine(instance.getEngine());
            awsRdsInstance.setClusterIdentifier(instance.getDBClusterIdentifier());
            awsRdsInstance.setDbPort(instance.getEndpoint().getPort());
            if (CollectionUtils.isEmpty(instance.getReadReplicaDBInstanceIdentifiers())) {
                awsRdsInstance.setDbRole(AwsRDSRole.INSTANCE.toString());
                if (StringUtils.hasText(instance.getReadReplicaSourceDBInstanceIdentifier())) {
                    awsRdsInstance.setDbRole(AwsRDSRole.REPLICA.toString());
                }
            } else {
                awsRdsInstance.setDbRole(AwsRDSRole.MASTER.toString());
            }
            awsRdsInstances.add(awsRdsInstance);
        });
        return awsRdsInstances;
    }

    private static List<AwsRDSInstance> getDBInstancesFromCluster(CustomAwsClient client, String clusterIdentifier,
            boolean selectRandomInstance) throws MangleException {
        List<AwsRDSInstance> rdsInstances;
        DescribeDBClustersRequest clustersRequest =
                new DescribeDBClustersRequest().withDBClusterIdentifier(clusterIdentifier);
        Map<String, String> instanceIdentifiersMap = new HashMap<>();
        try {
            DescribeDBClustersResult resultClusters = client.rdsClient().describeDBClusters(clustersRequest);
            instanceIdentifiersMap = getDBIdentifierMapFromCluster(resultClusters.getDBClusters());
        } catch (DBClusterNotFoundException clusterNotFoundException) {
            log.error(ErrorConstants.AWS_DESCRIBE_RDS_INSTANCES_FAILED + "{}", clusterIdentifier);
            throw new MangleException(ErrorConstants.AWS_DESCRIBE_RDS_INSTANCES_FAILED + clusterIdentifier,
                    ErrorCode.AWS_DESCRIBE_RESOURCES_FAILED, AwsResourceType.RDS_INSTANCE,
                    ErrorConstants.AWS_DESCRIBE_RDS_INSTANCES_FAILED + clusterIdentifier);
        }
        List<String> instanceIdentifiers = new ArrayList<String>(instanceIdentifiersMap.keySet());
        if (selectRandomInstance && !CollectionUtils.isEmpty(instanceIdentifiers)) {
            Collections.shuffle(instanceIdentifiers);
            rdsInstances = getAwsRdsInstancesWithRoles(
                    getDBInstancesWithInstanceIdentifiers(client, Arrays.asList(instanceIdentifiers.get(0))));
        } else {
            rdsInstances =
                    getAwsRdsInstancesWithRoles(getDBInstancesWithInstanceIdentifiers(client, instanceIdentifiers));
        }
        return updateWithClusterRoles(instanceIdentifiersMap, rdsInstances);
    }

    private static List<AwsRDSInstance> updateWithClusterRoles(Map<String, String> instanceIdentifiersMap,
            List<AwsRDSInstance> rdsInstances) {
        rdsInstances.stream().forEach(instance -> {
            instance.setDbRole(instanceIdentifiersMap.get(instance.getInstanceIdentifier()));
        });
        return rdsInstances;
    }

    private static Map<String, String> getDBIdentifierMapFromCluster(List<DBCluster> dbClusters) {
        Map<String, String> instanceIdentifiersMap = new HashMap<>();
        dbClusters.stream().forEach(cluster -> {
            cluster.getDBClusterMembers().stream().forEach(member -> {
                String role = AwsRDSRole.READER.toString();
                if (member.isClusterWriter()) {
                    role = AwsRDSRole.WRITER.toString();
                }
                instanceIdentifiersMap.put(member.getDBInstanceIdentifier(), role);
            });
        });
        return instanceIdentifiersMap;
    }

    private static List<DBInstance> describeAll_DBInstances(CustomAwsClient client) {
        return client.rdsClient().describeDBInstances().getDBInstances();
    }

    private static List<DBInstance> getDBInstancesWithInstanceIdentifiers(CustomAwsClient client,
            List<String> instanceIdentifiers) {
        List<DBInstance> dbInstances = new ArrayList<>();
        client.rdsClient().describeDBInstances().getDBInstances().stream().forEach(instance -> {
            if (instanceIdentifiers.contains(instance.getDBInstanceIdentifier())) {
                dbInstances.add(instance);
            }
        });
        return dbInstances;
    }

    private static String getClusterIdentifier(CustomAwsClient client, String dbInstanceIdentifier) {
        DescribeDBInstancesResult result = client.rdsClient()
                .describeDBInstances(new DescribeDBInstancesRequest().withDBInstanceIdentifier(dbInstanceIdentifier));
        return result.getDBInstances().get(0).getDBClusterIdentifier();
    }

    public static List<AwsRDSInstance> manipulateInstancesDBPort(CustomAwsClient client,
            String... dbInstanceIdentifiers) throws MangleException {

        List<AwsRDSInstance> faultInjectedRdsInstances = new ArrayList<>();
        for (String dbInstanceIdentifier : dbInstanceIdentifiers) {
            try {
                List<AwsRDSInstance> awsRdsInstances = describeRDSInstances(client, dbInstanceIdentifier);
                ModifyDBInstanceRequest modifyDBInstanceRequest =
                        new ModifyDBInstanceRequest().withDBInstanceIdentifier(dbInstanceIdentifier)
                                .withDBPortNumber(awsRdsInstances.get(0).getDbPort() + 1);
                client.rdsClient().modifyDBInstance(modifyDBInstanceRequest);
                faultInjectedRdsInstances.addAll(awsRdsInstances);
            } catch (AmazonRDSException rdsException) {
                throw new MangleException(rdsException.getMessage(), ErrorCode.AWS_OPERATION_FAILURE,
                        rdsException.getMessage());
            }
        }
        return faultInjectedRdsInstances;
    }

    public static void modifyRDSInstanceWithDBport(CustomAwsClient client, AwsRDSInstance... awsRdsInstances)
            throws MangleException {
        for (AwsRDSInstance rdsInstance : awsRdsInstances) {
            try {
                ModifyDBInstanceRequest modifyDBInstanceRequest =
                        new ModifyDBInstanceRequest().withDBInstanceIdentifier(rdsInstance.getInstanceIdentifier())
                                .withDBPortNumber(rdsInstance.getDbPort());
                client.rdsClient().modifyDBInstance(modifyDBInstanceRequest);
            } catch (AmazonRDSException rdsException) {
                throw new MangleException(rdsException.getMessage(), ErrorCode.AWS_OPERATION_FAILURE,
                        rdsException.getMessage());
            }
        }
    }
}