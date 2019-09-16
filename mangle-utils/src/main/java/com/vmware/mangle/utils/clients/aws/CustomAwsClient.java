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

import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.ec2.AmazonEC2Async;
import com.amazonaws.services.ec2.AmazonEC2AsyncClientBuilder;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;

import com.vmware.mangle.utils.clients.endpoint.EndpointClient;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Api client for managing AWS services
 *
 * @author bkaranam
 */
public class CustomAwsClient implements EndpointClient {

    /** The region. */
    private final String region;

    /**
     * The constructor allows you to provide your accessKeyId and secretAccesskey
     *
     * @param region
     *            the region
     * @param accessKeyId
     *            the AWS access key id
     * @param secretAccessKey
     *            the AWS secret access key
     */
    public CustomAwsClient(String region, String accessKeyId, String secretAccessKey) {
        this.region = region;
        exportCredentials(accessKeyId, secretAccessKey);
    }

    public void exportCredentials(String accessKeyId, String secretAccessKey) {
        System.setProperty("aws.accessKeyId", accessKeyId);
        System.setProperty("aws.secretKey", secretAccessKey);
    }

    /**
     * Amazon EC2 client. Abstracted to aid testing
     *
     * @return the Amazon EC2 client
     */
    @SuppressWarnings("deprecation")
    public AmazonEC2Async ec2Client() {
        AmazonEC2AsyncClientBuilder builder = AmazonEC2AsyncClientBuilder.standard().withRegion(region);
        return builder.build();

    }

    @Override
    public boolean testConnection() throws MangleException {
        try {
            validateRegion();
            ec2Client().describeAvailabilityZones();
            return true;
        } catch (AmazonEC2Exception exception) {
            if (exception.getMessage().contains(ErrorConstants.AWS_INVALID_CREDS)) {
                throw new MangleException(ErrorCode.AWS_INVALID_CREDENTIALS);
            } else {
                throw new MangleException(ErrorCode.AWS_UNKNOWN_ERROR, exception.getMessage());
            }
        } catch (RuntimeException exception) {
            throw new MangleException(ErrorCode.AWS_UNKNOWN_ERROR, exception.getMessage());
        }
    }

    private void validateRegion() throws MangleException {
        Region availableRegion = RegionUtils.getRegions().stream()
                .filter(regionFound -> this.region.contains(regionFound.getName())).findAny().orElse(null);
        if (null == availableRegion) {
            throw new MangleException(ErrorCode.AWS_INVALID_REGION, this.region);
        }
    }
}
