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

import java.util.List;

import lombok.extern.log4j.Log4j2;

import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.model.aws.AwsRDSInstance;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author bkaranam
 *
 *         Provides methods for orchestrating different fault operations on aws RDS
 *         instances/clusters
 */
@Log4j2
public class RDSFaultOperations {

    private RDSFaultOperations() {
    }

    /**
     * start aws rds instances
     *
     * @param dbInstanceIdentifiers
     * @param CustomAwsClient
     *            client
     * @return CommandExecutionResult object with exit code set to 0 if starting the instances is
     *         successful, else sets it to 1
     */
    public static CommandExecutionResult startInstances(CustomAwsClient client, String dbInstanceIdentifiers) {
        log.debug("Starting rds instances {}", dbInstanceIdentifiers);
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        try {
            AWSCommonUtils.startRDS_instances(client, dbInstanceIdentifiers.split(","));
            commandExecutionResult.setCommandOutput("Successfully started rds instances:" + dbInstanceIdentifiers);
            commandExecutionResult.setExitCode(0);
        } catch (MangleException e) {
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
        }
        return commandExecutionResult;
    }

    /**
     * stop aws rds instances
     *
     * @param dbInstanceIdentifiers
     * @param CustomAwsClient
     *            client
     * @return CommandExecutionResult object with exit code set to 0 if stopping instances is
     *         successful, else sets it to 1
     */
    public static CommandExecutionResult stopInstances(CustomAwsClient client, String dbInstanceIdentifiers) {
        log.debug("Stopping rds instances {}", dbInstanceIdentifiers);
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        try {
            AWSCommonUtils.stopRDS_instances(client, dbInstanceIdentifiers.split(","));
            commandExecutionResult.setCommandOutput("Successfully stopped rds instances:" + dbInstanceIdentifiers);
            commandExecutionResult.setExitCode(0);
        } catch (MangleException e) {
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
        }
        return commandExecutionResult;
    }

    /**
     * reboot aws rds instances
     *
     * @param dbInstanceIdentifiers
     * @param CustomAwsClient
     *            client
     * @return CommandExecutionResult object with exit code set to 0 if rebooting instances is
     *         successful, else sets it to 1
     */
    public static CommandExecutionResult rebootInstances(CustomAwsClient client, String dbInstanceIdentifiers) {
        log.debug("Rebooting rds instances {}", dbInstanceIdentifiers);
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        try {
            AWSCommonUtils.rebootRDS_instances(client, dbInstanceIdentifiers.split(","));
            commandExecutionResult.setCommandOutput("Successfully stopped rds instances:" + dbInstanceIdentifiers);
            commandExecutionResult.setExitCode(0);
        } catch (MangleException e) {
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
        }
        return commandExecutionResult;
    }

    /**
     * failover aws rds db instances in the cluster
     *
     * @param dbInstanceIdentifiers
     * @param CustomAwsClient
     *            client
     * @return CommandExecutionResult object with exit code set to 0 if failover rds instances is
     *         successful, else sets it to 1
     */
    public static CommandExecutionResult failoverDBCluster(CustomAwsClient client, String dbInstanceIdentifiers) {
        log.debug("failover rds instances {}", dbInstanceIdentifiers);
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        try {
            AWSCommonUtils.failoverRDS_instances(client, dbInstanceIdentifiers.split(","));
            commandExecutionResult
                    .setCommandOutput("Successfully triggered failover on rds instances:" + dbInstanceIdentifiers);
            commandExecutionResult.setExitCode(0);
        } catch (MangleException e) {
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
        }
        return commandExecutionResult;
    }

    /**
     * connection loss to aws rds db instances
     *
     * @param dbInstanceIdentifiers
     * @param CustomAwsClient
     *            client
     * @return CommandExecutionResult object with exit code set to 0 if manipulating dbb port to
     *         simulate connection loss to rds instances is successful, else sets it to 1
     */
    public static CommandExecutionResult connectionLoss(CustomAwsClient client, String dbInstanceIdentifiers) {
        log.debug("Simulating connection loss to rds instances {}", dbInstanceIdentifiers);
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        try {
            List<AwsRDSInstance> faultInjectedRDSInstances =
                    AWSCommonUtils.manipulateInstancesDBPort(client, dbInstanceIdentifiers.split(","));
            commandExecutionResult.setCommandOutput("Successfully triggered connection loss fault on rds Instances->"
                    + RestTemplateWrapper.objectToJson(faultInjectedRDSInstances));
            commandExecutionResult.setExitCode(0);
        } catch (MangleException e) {
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
        }
        return commandExecutionResult;
    }

    /**
     * connection reset to aws rds db instances
     *
     * @param dbInstanceIdentifiers
     * @param CustomAwsClient
     *            client
     * @return CommandExecutionResult object with exit code set to 0 if manipulating db port to
     *         simulate connection loss to rds instances is successful, else sets it to 1
     */
    public static CommandExecutionResult connectionReset(CustomAwsClient client, String awsRdsInstancesJson) {
        log.debug("Simulating connection Reset to rds instances {}", awsRdsInstancesJson);

        AwsRDSInstance[] awsRdsInstances =
                RestTemplateWrapper.jsonToObject(awsRdsInstancesJson, AwsRDSInstance[].class);
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        try {
            AWSCommonUtils.modifyRDSInstanceWithDBport(client, awsRdsInstances);
            commandExecutionResult.setCommandOutput(
                    "Successfully triggered connection reset on rds instances:" + awsRdsInstancesJson);
            commandExecutionResult.setExitCode(0);
        } catch (MangleException e) {
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
        }
        return commandExecutionResult;
    }


}
