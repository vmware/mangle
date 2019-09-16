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
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Async;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.GroupIdentifier;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.ModifyInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.SecurityGroup;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.vmware.mangle.utils.CommonUtils;
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

    public static List<String> getAwsInstances(CustomAwsClient client, HashMap<String, String> awsTags, boolean random)
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

    private static List<String> getInstancesFromReservations(List<Reservation> reservations,
            HashMap<String, String> awsTags, boolean random) throws MangleException {
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

    public static String findSecurityGroup(CustomAwsClient client, String instanceId, String groupName) {
        String vpcId = getVpcId(client, instanceId);

        SecurityGroup found = null;
        List<SecurityGroup> securityGroups = describeSecurityGroups(client, groupName);
        for (SecurityGroup sg : securityGroups) {
            if (StringUtils.hasText(vpcId) && vpcId.equals(sg.getVpcId())) {
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

    public static String createSecurityGroup(CustomAwsClient client, String instanceId, String name,
            String description) {
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
     */
    private static String getVpcId(CustomAwsClient client, String instanceId) {
        Instance awsInstance = describeInstance(client, instanceId);
        String vpcId = null;
        if (null != awsInstance) {
            vpcId = awsInstance.getVpcId();
        }
        return vpcId;
    }

    /**
     * Gets the VPC id for the given instance.
     */
    public static List<String> getSecurityGroupIDs(CustomAwsClient client, String instanceId) {
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
     */
    public static Instance describeInstance(CustomAwsClient client, String instanceId) {
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
     */
    public static List<Instance> describeInstances(CustomAwsClient client, String... instanceIds) {
        if (instanceIds == null || instanceIds.length == 0) {
            log.info("Getting all EC2 instances");
        } else {
            log.info(String.format("Getting EC2 instances for %d ids", instanceIds.length));
        }

        List<Instance> instances = new LinkedList<Instance>();

        AmazonEC2Async ec2Client = client.ec2Client();
        DescribeInstancesRequest request = new DescribeInstancesRequest();
        if (instanceIds != null) {
            request.withInstanceIds(Arrays.asList(instanceIds));
        }
        DescribeInstancesResult result = ec2Client.describeInstances(request);
        for (Reservation reservation : result.getReservations()) {
            instances.addAll(reservation.getInstances());
        }

        log.info(String.format("Got %d EC2 instances", instances.size()));
        return instances;
    }

    public static List<Filter> getFilters(HashMap<String, String> tags) {
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

    public static String getRandomSecurityGroupName(String instanceID) {
        return instanceID + "-" + UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }
}

