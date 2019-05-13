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

import javax.ws.rs.ProcessingException;

import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.Container;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.utils.RetryUtils;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * This class is about the Docker Operations methods.
 *
 * @author rpraveen
 */
@Log4j2
public class DockerOperations {

    private DockerOperations() {
    }

    public static CommandExecutionResult dockerPause(CustomDockerClient customDockerClient, String containerName)
            throws MangleException {
        log.info("Inside the pause container method..");
        CommandExecutionResult executionResult = new CommandExecutionResult();
        String containerId = findContainerId(customDockerClient, containerName);
        if (StringUtils.isEmpty(containerId)) {
            return setContainerNotAvailable(executionResult, containerName);
        }
        try {
            customDockerClient.getDockerClient().pauseContainerCmd(containerId).exec();
        } catch (Exception e) {
            throw new MangleException(e.getMessage(), ErrorCode.PAUSE_CONTAINER_OPERATION_FAILED);
        }
        log.info("Checking if the container is paused successfully");
        try {
            executionResult = RetryUtils.retry(() -> {
                CommandExecutionResult result = new CommandExecutionResult();
                if (isContainerPaused(customDockerClient, containerId)) {
                    result.setCommandOutput(Constants.CONTAINER_PAUSE_SUCCESS_MESSAGE + ":" + containerName);
                    result.setExitCode(0);
                    return result;
                } else {
                    throw new MangleException(ErrorConstants.PAUSE_CONTAINER_OPERATION_FAILED,
                            ErrorCode.PAUSE_CONTAINER_OPERATION_FAILED);
                }
            }, new MangleException(ErrorConstants.PAUSE_CONTAINER_OPERATION_FAILED,
                    ErrorCode.PAUSE_CONTAINER_OPERATION_FAILED), 4, 5);
        } catch (MangleException exception) {
            executionResult.setCommandOutput(exception.getMessage() + ":" + containerName);
            executionResult.setExitCode(1);
            return executionResult;
        }
        return executionResult;
    }

    public static CommandExecutionResult dockerUnPause(CustomDockerClient customDockerClient, String containerName)
            throws MangleException {
        log.info("Inside the unpause container method..");
        CommandExecutionResult executionResult = new CommandExecutionResult();
        String containerId = findContainerId(customDockerClient, containerName);
        if (StringUtils.isEmpty(containerId)) {
            return setContainerNotAvailable(executionResult, containerName);
        }
        try {
            customDockerClient.getDockerClient().unpauseContainerCmd(containerId).exec();
        } catch (Exception e) {
            throw new MangleException(e.getMessage(), ErrorCode.UNPAUSE_CONTAINER_OPERATION_FAILED);
        }
        log.info("Checking if the container is unpaused successfully");
        try {
            executionResult = RetryUtils.retry(() -> {
                CommandExecutionResult result = new CommandExecutionResult();
                if (!isContainerPaused(customDockerClient, containerId)) {
                    result.setCommandOutput(Constants.CONTAINER_UNPAUSE_SUCCESS_MESSAGE + ":" + containerName);
                    result.setExitCode(0);
                    return result;
                } else {
                    throw new MangleException(ErrorConstants.UNPAUSE_CONTAINER_OPERATION_FAILED,
                            ErrorCode.UNPAUSE_CONTAINER_OPERATION_FAILED);
                }
            }, new MangleException(ErrorConstants.UNPAUSE_CONTAINER_OPERATION_FAILED,
                    ErrorCode.UNPAUSE_CONTAINER_OPERATION_FAILED), 4, 5);
        } catch (MangleException exception) {
            executionResult.setCommandOutput(exception.getMessage() + ":" + containerName);
            executionResult.setExitCode(1);
            return executionResult;
        }
        return executionResult;
    }

    public static CommandExecutionResult dockerStart(CustomDockerClient customDockerClient, String containerName,
            String containerId) throws MangleException {
        log.info("Inside the start container method..");
        CommandExecutionResult executionResult = new CommandExecutionResult();
        if (StringUtils.isEmpty(containerId)) {
            return setContainerNotAvailable(executionResult, containerName);
        }
        try {
            customDockerClient.getDockerClient().startContainerCmd(containerId).exec();
        } catch (NotModifiedException e) {
            if (null == e.getMessage()) {
                throw new MangleException(ErrorConstants.CONTAINER_IS_ALREADY_RUNNING,
                        ErrorCode.CONTAINER_IS_ALREADY_RUNNING, containerName);
            }
        } catch (Exception exception) {
            throw new MangleException(exception.getMessage(), ErrorCode.START_CONTAINER_OPERATION_FAILED);
        }
        log.info("Checking if the container is started successfully");
        try {
            executionResult = RetryUtils.retry(() -> {
                CommandExecutionResult result = new CommandExecutionResult();
                if (isContainerRunningByName(customDockerClient, containerName)) {
                    result.setCommandOutput(Constants.CONTAINER_START_SUCCESS_MESSAGE + ":" + containerName);
                    result.setExitCode(0);
                    return result;
                } else {
                    throw new MangleException(ErrorConstants.START_CONTAINER_OPERATION_FAILED,
                            ErrorCode.START_CONTAINER_OPERATION_FAILED);
                }
            }, new MangleException(ErrorCode.START_CONTAINER_OPERATION_FAILED), 4, 5);
        } catch (MangleException exception) {
            executionResult.setCommandOutput(exception.getMessage() + ":" + containerName);
            executionResult.setExitCode(1);
            return executionResult;
        }
        return executionResult;
    }

    public static CommandExecutionResult dockerStop(CustomDockerClient customDockerClient, String containerName)
            throws MangleException {
        log.info("Inside the Stop container method..");
        CommandExecutionResult executionResult = new CommandExecutionResult();
        String containerId = findContainerId(customDockerClient, containerName);
        if (StringUtils.isEmpty(containerId)) {
            return setContainerNotAvailable(executionResult, containerName);
        }
        try {
            customDockerClient.getDockerClient().stopContainerCmd(containerId).exec();
        } catch (Exception e) {
            throw new MangleException(e.getMessage(), ErrorCode.STOP_CONTAINER_OPERATION_FAILED);
        }
        log.info("Checking if the container is stopped successfully");
        try {
            executionResult = RetryUtils.retry(() -> {
                CommandExecutionResult result = new CommandExecutionResult();
                if (!isContainerRunningByName(customDockerClient, containerName)) {
                    result.setCommandOutput(containerId);
                    result.setExitCode(0);
                    return result;
                } else {
                    throw new MangleException(ErrorConstants.STOP_CONTAINER_OPERATION_FAILED,
                            ErrorCode.STOP_CONTAINER_OPERATION_FAILED);
                }
            }, new MangleException(ErrorConstants.STOP_CONTAINER_OPERATION_FAILED,
                    ErrorCode.STOP_CONTAINER_OPERATION_FAILED), 4, 5);
        } catch (MangleException exception) {
            executionResult.setCommandOutput(exception.getMessage() + ":" + containerName);
            executionResult.setExitCode(1);
            return executionResult;
        }
        return executionResult;
    }

    private static CommandExecutionResult setContainerNotAvailable(CommandExecutionResult executionResult,
            String containerName) {
        executionResult.setCommandOutput(ErrorConstants.CONTAINER_NOT_AVAILABLE + ":" + containerName);
        executionResult.setExitCode(1);
        return executionResult;
    }


    private static String findContainerId(CustomDockerClient customDockerClient, String containerName)
            throws MangleException {
        List<Container> allContainers = null;
        try {
            allContainers = customDockerClient.getDockerClient().listContainersCmd().exec();
        } catch (ProcessingException e) {
            throw new MangleException(ErrorConstants.DOCKER_CONNECTION_FAILURE, ErrorCode.DOCKER_CONNECTION_FAILURE,
                    e.getCause().getMessage());
        }
        for (Container eachContainer : allContainers) {
            String[] names = eachContainer.getNames();
            for (String each : names) {
                if (each.substring(1).equals(containerName)) {
                    return eachContainer.getId();
                }
            }
        }
        return null;
    }

    private static boolean isContainerRunningByName(CustomDockerClient customDockerClient, String containerName)
            throws MangleException {
        return null != findContainerId(customDockerClient, containerName);
    }

    private static boolean isContainerPaused(CustomDockerClient customDockerClient, String containerID) {
        List<Container> allContainers =
                customDockerClient.getDockerClient().listContainersCmd().withStatusFilter("paused").exec();
        log.info("list of containers:" + allContainers);
        for (Container eachContainer : allContainers) {
            if (eachContainer.getId().equals(containerID)) {
                return true;
            }
        }
        return false;
    }
}
