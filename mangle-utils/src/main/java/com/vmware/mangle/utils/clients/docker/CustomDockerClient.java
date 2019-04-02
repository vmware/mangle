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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmd;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectExecResponse;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerNetworkSettings;
import com.github.dockerjava.api.model.ContainerPort;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.SSLConfig;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import lombok.extern.log4j.Log4j2;

import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.utils.clients.endpoint.EndpointClient;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;
import com.vmware.mangle.utils.helpers.security.CertificateHelper;

/**
 * @author ashrimali
 * @author bkaranam
 */
@Log4j2
public class CustomDockerClient implements EndpointClient {

    private DockerClient dockerClient;

    protected CustomDockerClient() {
    }

    public DockerClient getDockerClient() {
        return dockerClient;
    }

    public CustomDockerClient(String dockerHost, String dockerPort, boolean tlsEnabled) {
        DockerClientConfig dockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://" + dockerHost + ":" + dockerPort).build();
        if (tlsEnabled) {
            dockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerHost("tcp://" + dockerHost + ":" + dockerPort).withDockerTlsVerify(true)
                    .withCustomSslConfig(getCustomDockerSSLConfig(dockerHost)).build();
        }
        this.dockerClient = DockerClientBuilder.getInstance(dockerClientConfig).build();
    }

    public ExecCreateCmd execCreateCmdByContainerName(String containerName) {
        String containerID = findContainerId(containerName);
        return dockerClient.execCreateCmd(containerID);
    }

    public ExecCreateCmd execCreateCmd(String containerId) {
        return dockerClient.execCreateCmd(containerId);
    }

    public CommandExecutionResult execCommandInContainerByName(String containerName, String cmnd) {
        return execCommandInContainerByID(findContainerId(containerName), cmnd);
    }

    public CommandExecutionResult execCommandInContainerByID(String containerId, String cmnd) {
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        String[] command = { "bash", "-c", cmnd };
        ExecCreateCmdResponse exec = dockerClient.execCreateCmd(containerId).withCmd(command).withTty(false)
                .withAttachStdin(true).withAttachStdout(true).withAttachStderr(true).exec();
        OutputStream outputStream = new ByteArrayOutputStream();
        String output = null;
        try {
            dockerClient.execStartCmd(exec.getId()).withDetach(false).withTty(true)
                    .exec(new ExecStartResultCallback(outputStream, System.err)).awaitCompletion();
            output = outputStream.toString();
            InspectExecResponse inspectExecResponse = execInspectCommand(exec);
            commandExecutionResult.setExitCode(inspectExecResponse.getExitCode().intValue());
            commandExecutionResult.setCommandOutput(output);
        } catch (InterruptedException interreuptedException) {

            log.warn("Exception executing command {} on container {}" + command + containerId + interreuptedException);
            commandExecutionResult.setCommandOutput(interreuptedException.getMessage());
            commandExecutionResult.setExitCode(500);
            Thread.currentThread().interrupt();
            return commandExecutionResult;
        }
        return commandExecutionResult;
    }

    public String execStartCommand(ExecCreateCmdResponse execCreationID) throws InterruptedException {
        OutputStream outputStream = new ByteArrayOutputStream();
        String output = null;
        dockerClient.execStartCmd(execCreationID.getId()).withDetach(false).withTty(true)
                .exec(new ExecStartResultCallback(outputStream, System.err)).awaitCompletion();
        output = outputStream.toString();
        return output;
    }

    public InspectExecResponse execInspectCommand(ExecCreateCmdResponse execCreationID) {
        return dockerClient.inspectExecCmd(execCreationID.getId()).exec();
    }

    public void stopContainerByName(String containerName) {
        log.info("Stoping the container: " + containerName);
        String containerID = findContainerId(containerName);
        stopContainerById(containerID);
    }

    public void stopContainerById(String containerId) {
        log.info("Stopping the container: " + containerId);
        dockerClient.stopContainerCmd(containerId).exec();
    }

    public void startContainerByName(String containerName) {
        log.info("Starting the container: " + containerName);
        String containerID = findContainerId(containerName);
        startContainerById(containerID);
    }

    public void startContainerById(String containerId) {
        log.info("Stoping and deleting the container : " + containerId);
        dockerClient.startContainerCmd(containerId).exec();
    }

    public void stopAndDeleteContainerByName(String containerName) {
        log.info("Stoping and deleting the container: " + containerName);
        String containerID = findContainerId(containerName);
        stopAndDeleteContainerByID(containerID);
    }

    public void stopAndDeleteContainerByID(String containerID) {
        log.info("Stoping and deleting the container: " + containerID);
        dockerClient.stopContainerCmd(containerID).exec();
        dockerClient.removeContainerCmd(containerID).exec();
    }

    public void unPauseAllContainers() {
        log.info("UnPause all the containers which are Paused: ");
        List<Container> containerPausedIDs = dockerClient.listContainersCmd().withStatusFilter("paused").exec();
        for (Container containerID : containerPausedIDs) {
            dockerClient.unpauseContainerCmd(containerID.getId()).exec();
        }
    }

    public String[][] listProcessesContainerByName(String containerName) {
        log.info("List processes running inside a container: " + containerName);
        String containerID = findContainerId(containerName);
        return listProcessesContainerByID(containerID);
    }

    public String[][] listProcessesContainerByID(String containerID) {
        log.info("List processes running inside a container: " + containerID);
        return dockerClient.topContainerCmd(containerID).exec().getProcesses();
    }

    public void deleteAllMatchingContainer(String containerName) {
        if (isContainerRunningByName(containerName)) {
            List<String> containers = matchAllRunningContainer(containerName);
            for (String container : containers) {
                stopAndDeleteContainerByName(container);
            }
        }
    }

    public List<Image> getAllDockerImages(DockerClient dockerClient) {
        return dockerClient.listImagesCmd().exec();
    }

    public List<Container> getAllContainers() {
        return this.dockerClient.listContainersCmd().exec();
    }

    public List<Image> getAllDockerImageNames(DockerClient dockerClient) {
        return dockerClient.listImagesCmd().exec();
    }

    public void pullImage(String imageName) {
        dockerClient.pullImageCmd(imageName).exec(new PullImageResultCallback()).awaitSuccess();
    }

    public String findContainerId(String containerName) {
        List<Container> allContainers = dockerClient.listContainersCmd().exec();
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

    public Container getContainerByName(String containerName) {
        List<Container> allContainers = dockerClient.listContainersCmd().exec();
        Container container = null;
        for (Container eachContaier : allContainers) {
            String[] names = eachContaier.getNames();
            for (String each : names) {
                if (each.contains(containerName)) {
                    container = eachContaier;
                    break;
                }
            }
        }
        return container;
    }

    public boolean isContainerRunningByName(String containerName) {
        return null != findContainerId(containerName);
    }

    public boolean isContainerRunningByID(String containerID) {
        List<Container> allContainers = dockerClient.listContainersCmd().exec();
        for (Container eachContaier : allContainers) {
            if (eachContaier.getId().contains(containerID)) {
                return true;
            }
        }
        return false;
    }

    public boolean isContainerPaused(String containerID) {
        List<Container> allContainers = dockerClient.listContainersCmd().withStatusFilter("paused").exec();
        for (Container eachContaier : allContainers) {
            if (eachContaier.getId().contains(containerID)) {
                return true;
            }
        }
        return false;
    }

    public List<String> matchAllRunningContainer(String containerName) {
        List<Container> allContainers = dockerClient.listContainersCmd().exec();
        List<String> runningContainers = new ArrayList<>();
        for (int i = 0; i < allContainers.size(); i++) {
            String[] names = allContainers.get(i).getNames();
            for (String each : names) {
                if (each.contains(containerName)) {
                    runningContainers.add(each);
                }
            }
        }
        return runningContainers;
    }

    public Map<Integer, Boolean> getAllPublicPorts(DockerClient dockerClient) {
        List<Container> allContainers = dockerClient.listContainersCmd().exec();
        Map<Integer, Boolean> ports = new HashMap<>();
        for (Container e : allContainers) {
            ContainerPort[] port = e.getPorts();
            // Keeping port[0] because we have only one port to bind with
            // container.
            ports.put(port[0].getPublicPort(), true);
        }
        return ports;
    }

    public boolean isPortAvailableToConsume(Integer requiredPort) {
        Map<Integer, Boolean> ports = getAllPublicPorts(dockerClient);
        if (null == ports.get(requiredPort)) {
            return true;
        }
        return !ports.get(requiredPort);
    }

    public boolean copyFileToContainerByName(String containerName, String srcFilePath, String destFilePath)
            throws MangleException {
        String containerID = findContainerId(containerName);
        return copyFileToContainerByID(containerID, srcFilePath, destFilePath);
    }

    public boolean copyFileToContainerByID(String containerId, String srcFilePath, String destFilePath)
            throws MangleException {
        try {
            dockerClient.copyArchiveToContainerCmd(containerId).withRemotePath(destFilePath)
                    .withHostResource(srcFilePath).exec();
        } catch (DockerClientException exception) {
            log.error("File copy to container:" + containerId + " is failed with exception " + exception.getMessage());
            throw new MangleException(exception, ErrorCode.FILE_TRANSFER_ERROR, srcFilePath, destFilePath);

        } catch (NotFoundException notFoundException) {
            log.error("File copy to container:" + containerId + " is failed with exception "
                    + notFoundException.getMessage());
            throw new MangleException(notFoundException, ErrorCode.DIRECTORY_NOT_FOUND, destFilePath);
        }
        return true;
    }

    public String getDockerIPByName(String containerName) {
        String containerID = findContainerId(containerName);
        return getDockerIP(containerID);
    }

    public String getDockerIP(String containerID) {
        List<Container> allContainers = dockerClient.listContainersCmd().exec();
        for (Container eachContaier : allContainers) {
            if (eachContaier.getId().contains(containerID)) {
                ContainerNetworkSettings containerNetworkSettings = eachContaier.getNetworkSettings();
                if (containerNetworkSettings != null) {
                    return containerNetworkSettings.getNetworks().get("bridge").getIpAddress();
                }
            }
        }
        return null;
    }

    private SSLConfig getCustomDockerSSLConfig(String dockerHost) {
        return new SSLConfig() {
            @Override
            public SSLContext getSSLContext() throws KeyManagementException, UnrecoverableKeyException,
                    NoSuchAlgorithmException, KeyStoreException {
                return CertificateHelper.getSSLContextWithValidateCert(dockerHost);
            }
        };
    }

    @Override
    public boolean testConnection() {
        try {
            this.dockerClient.pingCmd().exec();
            return true;
        } catch (RuntimeException exception) {
            log.error(exception);
            return false;
        }
    }

    public void setDockerClient(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }
}