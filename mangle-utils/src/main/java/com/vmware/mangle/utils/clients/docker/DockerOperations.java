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

package com.vmware.mangle.utils.clients.docker;

import java.util.List;

import com.github.dockerjava.api.model.Container;
import lombok.extern.log4j.Log4j2;

import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.utils.CommonUtils;

/**
 * This class is about the Docker Operations methods.
 *
 * @author rpraveen
 */
@Log4j2
public class DockerOperations {

    private DockerOperations() {
    }

    public static CommandExecutionResult dockerPause(CustomDockerClient customDockerClient, String containerName) {
        log.info("Inside the pause container method..");
        CommandExecutionResult executionResult = new CommandExecutionResult();
        String containerId = findContainerId(customDockerClient, containerName);
        if (containerId != null) {
            customDockerClient.getDockerClient().pauseContainerCmd(containerId).exec();
            log.info("Sleeping for 20 seconds after pausing the container!");
            CommonUtils.delayInMilliSeconds(20000);
            log.info("Checking if the container is paused successfully");
            if (isContainerPaused(customDockerClient, containerId)) {
                executionResult.setCommandOutput("The container with : " + containerName + "is paused successfully");
                executionResult.setExitCode(0);
            }
        } else {
            executionResult.setCommandOutput("Pause container operation failed: " + containerName);
            executionResult.setExitCode(1);
        }

        return executionResult;
    }

    public static CommandExecutionResult dockerUnPause(CustomDockerClient customDockerClient, String containerName) {
        log.info("Inside the unpause container method..");
        CommandExecutionResult executionResult = new CommandExecutionResult();
        String containerId = findContainerId(customDockerClient, containerName);
        if (containerId != null) {
            customDockerClient.getDockerClient().unpauseContainerCmd(containerId).exec();
            log.info("Sleeping for 20 seconds after unpausing the container!");
            CommonUtils.delayInMilliSeconds(20000);
            log.info("Checking if the container is unpaused successfully");
            if (!isContainerPaused(customDockerClient, containerId)) {
                executionResult.setCommandOutput("The container with : " + containerName + "is unpaused successfully");
                executionResult.setExitCode(0);
            }
        } else {
            executionResult.setCommandOutput("UnPause container operation failed: " + containerName);
            executionResult.setExitCode(1);
        }
        return executionResult;
    }

    public static CommandExecutionResult dockerStart(CustomDockerClient customDockerClient, String containerName,
            String containerId) {
        log.info("Inside the start container method..");
        CommandExecutionResult executionResult = new CommandExecutionResult();

        if (containerId != null) {
            customDockerClient.getDockerClient().startContainerCmd(containerId).exec();
            executionResult.setCommandOutput("The container with ID: " + containerId + " is started succesfully");
            executionResult.setExitCode(0);
            log.info("Sleeping for 20 seconds after stopping the container!");
            CommonUtils.delayInMilliSeconds(20000);
            log.info("Checking if the container is started successfully");
            if (isContainerRunningByID(customDockerClient, containerName)) {
                executionResult.setCommandOutput("The container : " + containerName + " is started succesfully");
                executionResult.setExitCode(0);
            }

        } else {
            executionResult.setCommandOutput("Couldnt get the containerID for the container");
            executionResult.setExitCode(1);
        }
        return executionResult;
    }

    public static CommandExecutionResult dockerStop(CustomDockerClient customDockerClient, String containerName) {
        log.info("Inside the Stop container method..");
        CommandExecutionResult executionResult = new CommandExecutionResult();
        String containerId = findContainerId(customDockerClient, containerName);
        if (containerId != null) {
            customDockerClient.getDockerClient().stopContainerCmd(containerId).exec();
            log.info("Sleeping for 20 seconds after stopping the container!");
            CommonUtils.delayInMilliSeconds(20000);
            log.info("Checking if the container is stopped successfully");
            if (!isContainerRunningByName(customDockerClient, containerName)) {
                executionResult.setCommandOutput(containerId);
                executionResult.setExitCode(0);
            }

        } else {
            executionResult.setCommandOutput("Stop container operation failed: " + containerName);
            executionResult.setExitCode(1);
        }
        return executionResult;
    }

    private static String findContainerId(CustomDockerClient customDockerClient, String containerName) {
        List<Container> allContainers = customDockerClient.getDockerClient().listContainersCmd().exec();
        String containerID = null;
        outerLoop: for (Container eachContaier : allContainers) {
            String[] names = eachContaier.getNames();
            for (String each : names) {
                if (each.contains(containerName)) {
                    containerID = eachContaier.getId();
                    break outerLoop;
                }
            }
        }
        return containerID;
    }

    private static boolean isContainerRunningByName(CustomDockerClient customDockerClient, String containerName) {
        return null != findContainerId(customDockerClient, containerName);
    }

    private static boolean isContainerRunningByID(CustomDockerClient customDockerClient, String containerID) {
        List<Container> allContainers = customDockerClient.getDockerClient().listContainersCmd().exec();
        for (Container eachContaier : allContainers) {
            if (eachContaier.getId().contains(containerID)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isContainerPaused(CustomDockerClient customDockerClient, String containerID) {
        List<Container> allContainers =
                customDockerClient.getDockerClient().listContainersCmd().withStatusFilter("paused").exec();
        log.info("list of containers:" + allContainers);
        for (Container eachContainer : allContainers) {
            if (eachContainer.getId().contains(containerID)) {
                return true;
            }
        }
        return false;
    }

}
