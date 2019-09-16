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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.ec2.AmazonEC2Async;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.RebootInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author bkaranam
 *
 *         Provides methods for orchestrating different fault operations on aws EC2 instances
 */
@Log4j2
public class EC2InstanceFaultOperations {

    private EC2InstanceFaultOperations() {
    }


    /**
     * Terminate aws instances
     *
     * @param instanceIDs
     * @param CustomAwsClient
     *            client
     * @return CommandExecutionResult object with exit code set to 0 if terminating instances is
     *         successful, else sets it to 1
     */
    public static CommandExecutionResult terminateInstances(CustomAwsClient client, String... instanceIDs) {
        log.debug("Terminating instances {}", Arrays.asList(instanceIDs));
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        AmazonEC2Async ec2Client = client.ec2Client();
        try {
            TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest().withInstanceIds(instanceIDs);
            Future<TerminateInstancesResult> result = ec2Client.terminateInstancesAsync(terminateRequest,
                    new AsyncHandler<TerminateInstancesRequest, TerminateInstancesResult>() {
                        @Override
                        public void onError(Exception exception) {
                            commandExecutionResult.setCommandOutput(exception.getMessage());
                            commandExecutionResult.setExitCode(1);
                        }

                        @Override
                        public void onSuccess(TerminateInstancesRequest request, TerminateInstancesResult result) {
                            commandExecutionResult.setCommandOutput(result.getTerminatingInstances().toString());
                            commandExecutionResult.setExitCode(0);
                        }
                    });
            result.get();
        } catch (InterruptedException e) {
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
        } finally {
            ec2Client.shutdown();
        }
        return commandExecutionResult;
    }

    /**
     * start aws instances
     *
     * @param instanceIDs
     * @param CustomAwsClient
     *            client
     * @return CommandExecutionResult object with exit code set to 0 if starting the instances is
     *         successful, else sets it to 1
     */
    public static CommandExecutionResult startInstances(CustomAwsClient client, String... instanceIDs) {
        log.debug("Starting instances {}", Arrays.asList(instanceIDs));
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        AmazonEC2Async ec2Client = client.ec2Client();
        try {

            StartInstancesRequest startRequest = new StartInstancesRequest().withInstanceIds(instanceIDs);

            Future<StartInstancesResult> result = ec2Client.startInstancesAsync(startRequest,
                    new AsyncHandler<StartInstancesRequest, StartInstancesResult>() {
                        @Override
                        public void onError(Exception exception) {
                            commandExecutionResult.setCommandOutput(exception.getMessage());
                            commandExecutionResult.setExitCode(1);
                        }

                        @Override
                        public void onSuccess(StartInstancesRequest request, StartInstancesResult result) {
                            commandExecutionResult.setCommandOutput(result.getStartingInstances().toString());
                            commandExecutionResult.setExitCode(0);
                        }
                    });
            result.get();
        } catch (InterruptedException e) {
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
        } finally {
            ec2Client.shutdown();
        }
        return commandExecutionResult;
    }

    /**
     * stop aws instances
     *
     * @param instanceIDs
     * @param CustomAwsClient
     *            client
     * @return CommandExecutionResult object with exit code set to 0 if stopping instances is
     *         successful, else sets it to 1
     */
    public static CommandExecutionResult stopInstances(CustomAwsClient client, String... instanceIDs) {
        log.debug("Stopping instances {}", Arrays.asList(instanceIDs));
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        AmazonEC2Async ec2Client = client.ec2Client();
        try {

            StopInstancesRequest terminateRequest = new StopInstancesRequest().withInstanceIds(instanceIDs);
            Future<StopInstancesResult> result = ec2Client.stopInstancesAsync(terminateRequest,
                    new AsyncHandler<StopInstancesRequest, StopInstancesResult>() {
                        @Override
                        public void onError(Exception exception) {
                            commandExecutionResult.setCommandOutput(exception.getMessage());
                            commandExecutionResult.setExitCode(1);
                        }

                        @Override
                        public void onSuccess(StopInstancesRequest request, StopInstancesResult result) {
                            commandExecutionResult.setCommandOutput(result.getStoppingInstances().toString());
                            commandExecutionResult.setExitCode(0);
                        }
                    });
            result.get();
        } catch (InterruptedException e) {
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
        } finally {
            ec2Client.shutdown();
        }
        return commandExecutionResult;
    }

    /**
     * reboot aws instances
     *
     * @param instanceIDs
     * @param CustomAwsClient
     *            client
     * @return CommandExecutionResult object with exit code set to 0 if rebooting instances is
     *         successful, else sets it to 1
     */
    public static CommandExecutionResult rebootInstances(CustomAwsClient client, String... instanceIDs) {
        log.debug("Rebooting instances {}", Arrays.asList(instanceIDs));
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        AmazonEC2Async ec2Client = client.ec2Client();
        try {
            RebootInstancesRequest terminateRequest = new RebootInstancesRequest().withInstanceIds(instanceIDs);
            Future<RebootInstancesResult> result = ec2Client.rebootInstancesAsync(terminateRequest,
                    new AsyncHandler<RebootInstancesRequest, RebootInstancesResult>() {
                        @Override
                        public void onError(Exception exception) {
                            commandExecutionResult.setCommandOutput(exception.getMessage());
                            commandExecutionResult.setExitCode(1);
                        }

                        @Override
                        public void onSuccess(RebootInstancesRequest request, RebootInstancesResult result) {
                            commandExecutionResult
                                    .setCommandOutput("Successfully rebooted instances: " + Arrays.asList(instanceIDs));
                            commandExecutionResult.setExitCode(0);
                        }
                    });
            result.get();
        } catch (InterruptedException e) {
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            commandExecutionResult.setCommandOutput(e.getMessage());
            commandExecutionResult.setExitCode(1);
        } finally {
            ec2Client.shutdown();
        }
        return commandExecutionResult;
    }

    /**
     * Block all the network traffic to specified aws instances by assigning dummy security group
     *
     * @param instanceIDs
     * @param CustomAwsClient
     *            client
     * @return CommandExecutionResult object with exit code set to 0 if blocking all the network is
     *         successful, else sets it to 1
     */
    public static CommandExecutionResult blockAllNetworkTraffic(CustomAwsClient client, String... instanceIDs) {
        log.debug("Blocking all network traffic for instances {}", Arrays.asList(instanceIDs));
        List<String> affectedInstancesWithSGs = new ArrayList<>();
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        for (String instanceID : instanceIDs) {
            String dummySecurityGroupID = null;
            try {
                List<String> existingSecurityGroupIDs = AWSCommonUtils.getSecurityGroupIDs(client, instanceID);
                dummySecurityGroupID = AWSCommonUtils.createSecurityGroup(client, instanceID,
                        AWSCommonUtils.getRandomSecurityGroupName(instanceID),
                        "Dummy Security Group Created by Mangle for instance" + instanceID);
                AWSCommonUtils.setInstanceSecurityGroups(client, instanceID, Arrays.asList(dummySecurityGroupID));
                affectedInstancesWithSGs.add(
                        (instanceID + "#" + String.join(",", existingSecurityGroupIDs) + "#" + dummySecurityGroupID));
            } catch (MangleException e) {
                commandExecutionResult.setCommandOutput(e.getMessage());
                commandExecutionResult.setExitCode(1);
            }
        }

        if (!CollectionUtils.isEmpty(affectedInstancesWithSGs)) {
            commandExecutionResult.setCommandOutput(
                    "Successfully blocked all the network to Instances->" + String.join("&", affectedInstancesWithSGs));
            commandExecutionResult.setExitCode(0);
        }
        return commandExecutionResult;
    }

    /**
     * unblock all the network traffic to specified aws instances by reassigning existing security
     * groups
     *
     * @param instanceIDsWithSecurityGroups
     * @param CustomAwsClient
     *            client
     * @return CommandExecutionResult object with exit code set to 0 if unblocking all the networks
     *         is successful, else sets it to 1
     */
    public static CommandExecutionResult unblockAllNetworkTraffic(CustomAwsClient client,
            String... instanceIDsWithSecurtyGroups) {
        log.debug("Unblocking all network traffic for instances with security groups {}",
                Arrays.asList(instanceIDsWithSecurtyGroups));
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        Set<String> securityGroupsToRemove = new HashSet<>();
        for (String instanceIDsWithSecurityGroup : instanceIDsWithSecurtyGroups) {
            for (String instanceIDWithSecurityGroup : instanceIDsWithSecurityGroup.split("&")) {
                String[] instanceAndSecurityGroups = instanceIDWithSecurityGroup.split("#");
                String instanceID = instanceAndSecurityGroups[0];
                List<String> securityGroupsToApply = Arrays.asList(instanceAndSecurityGroups[1].split(":"));
                securityGroupsToRemove.add(instanceAndSecurityGroups[2]);
                try {
                    AWSCommonUtils.setInstanceSecurityGroups(client, instanceID, securityGroupsToApply);
                    commandExecutionResult.setCommandOutput("Successfully unblocked instance " + instanceID + "\n");
                } catch (MangleException e) {
                    if (StringUtils.hasText(commandExecutionResult.getCommandOutput())) {
                        commandExecutionResult
                                .setCommandOutput(commandExecutionResult.getCommandOutput() + "\n" + e.getMessage());
                    } else {
                        commandExecutionResult.setCommandOutput(e.getMessage());
                    }
                    commandExecutionResult.setExitCode(1);
                }
            }
        }

        for (String securityGroupToRemove : securityGroupsToRemove) {
            try {
                AWSCommonUtils.deleteSecurityGroup(client, securityGroupToRemove);
            } catch (MangleException e) {
                if (StringUtils.hasText(commandExecutionResult.getCommandOutput())) {
                    commandExecutionResult
                            .setCommandOutput(commandExecutionResult.getCommandOutput() + "\n" + e.getMessage());
                } else {
                    commandExecutionResult.setCommandOutput(e.getMessage());
                }
                commandExecutionResult.setExitCode(1);
            }
        }
        return commandExecutionResult;
    }
}
