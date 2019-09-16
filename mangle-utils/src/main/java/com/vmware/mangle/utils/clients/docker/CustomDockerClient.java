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
import javax.net.ssl.SSLHandshakeException;
import javax.ws.rs.ProcessingException;

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
import com.github.dockerjava.core.DefaultDockerClientConfig.Builder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.SSLConfig;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import lombok.extern.log4j.Log4j2;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.utils.clients.endpoint.EndpointClient;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
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

    public CustomDockerClient(String dockerHost, int dockerPort, boolean tlsEnabled, String dockerCertPath) {
        DockerClientConfig dockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://" + dockerHost + ":" + dockerPort).build();
        if (tlsEnabled) {
            Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerHost("tcp://" + dockerHost + ":" + dockerPort).withDockerTlsVerify(true);
            if (StringUtils.hasText(dockerCertPath)) {
                builder.withDockerCertPath(dockerCertPath);
            } else {
                builder.withCustomSslConfig(getCustomDockerSSLConfig(dockerHost));
            }
            dockerClientConfig = builder.build();
        }
        try {
            this.dockerClient = DockerClientBuilder.getInstance(dockerClientConfig).build();
        } catch (DockerClientException e) {
            log.warn(e.getMessage());
            throw new MangleRuntimeException(e, ErrorCode.IO_EXCEPTION, e.getCause().getMessage());
        }
    }

    public ExecCreateCmd execCreateCmdByContainerName(String containerName) throws MangleException {
        String containerID = findContainerId(containerName);
        return dockerClient.execCreateCmd(containerID);
    }

    public ExecCreateCmd execCreateCmd(String containerId) {
        return dockerClient.execCreateCmd(containerId);
    }

    public CommandExecutionResult execCommandInContainerByName(String containerName, String cmnd)
            throws MangleException {
        return execCommandInContainerByID(findContainerId(containerName), cmnd);
    }

    public CommandExecutionResult execCommandInContainerByID(String containerId, String cmnd) throws MangleException {
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        String[] command = { "bash", "-c", cmnd };
        ExecCreateCmdResponse exec = null;
        try {
            exec = dockerClient.execCreateCmd(containerId).withCmd(command).withTty(false).withAttachStdin(true)
                    .withAttachStdout(true).withAttachStderr(true).exec();
        } catch (ProcessingException e) {
            throw new MangleException(ErrorConstants.DOCKER_CONNECTION_FAILURE, ErrorCode.DOCKER_CONNECTION_FAILURE,
                    e.getMessage());
        }

        OutputStream outputStream = new ByteArrayOutputStream();
        OutputStream errorStream = new ByteArrayOutputStream();
        String output = null;
        try {
            dockerClient.execStartCmd(exec.getId()).withDetach(false).withTty(false)
                    .exec(new ExecStartResultCallback(outputStream, errorStream)).awaitCompletion();
            output = outputStream.toString();

            if (!StringUtils.isEmpty(errorStream.toString())) {
                output = output + errorStream.toString();
            }
            InspectExecResponse inspectExecResponse = execInspectCommand(exec);
            commandExecutionResult.setExitCode(inspectExecResponse.getExitCode().intValue());
            commandExecutionResult.setCommandOutput(output);
        } catch (InterruptedException interruptedException) {
            log.warn("Command {} execution on container {} failed with {}", command, containerId, interruptedException);
            commandExecutionResult.setCommandOutput(interruptedException.getMessage());
            commandExecutionResult.setExitCode(500);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new MangleException(ErrorConstants.DOCKER_CONNECTION_FAILURE, ErrorCode.DOCKER_CONNECTION_FAILURE,
                    e.getMessage());
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

    public void stopContainerByName(String containerName) throws MangleException {
        log.info("Stoping the container: " + containerName);
        String containerID = findContainerId(containerName);
        stopContainerById(containerID);
    }

    public void stopContainerById(String containerId) {
        log.info("Stopping the container: " + containerId);
        dockerClient.stopContainerCmd(containerId).exec();
    }

    public void startContainerByName(String containerName) throws MangleException {
        log.info("Starting the container: " + containerName);
        String containerID = findContainerId(containerName);
        startContainerById(containerID);
    }

    public void startContainerById(String containerId) {
        log.info("Stoping and deleting the container : " + containerId);
        dockerClient.startContainerCmd(containerId).exec();
    }

    public void stopAndDeleteContainerByName(String containerName) throws MangleException {
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
        List<String> statusFilterList = new ArrayList<>();
        statusFilterList.add("paused");
        List<Container> containerPausedIDs = dockerClient.listContainersCmd().withStatusFilter(statusFilterList).exec();
        for (Container containerID : containerPausedIDs) {
            dockerClient.unpauseContainerCmd(containerID.getId()).exec();
        }
    }

    public String[][] listProcessesContainerByName(String containerName) throws MangleException {
        log.info("List processes running inside a container: " + containerName);
        String containerID = findContainerId(containerName);
        return listProcessesContainerByID(containerID);
    }

    public String[][] listProcessesContainerByID(String containerID) {
        log.info("List processes running inside a container: " + containerID);
        return dockerClient.topContainerCmd(containerID).exec().getProcesses();
    }

    public void deleteAllMatchingContainer(String containerName) throws MangleException {
        if (isContainerRunningByName(containerName)) {
            List<String> containers = matchAllRunningContainer(containerName);
            for (String container : containers) {
                stopAndDeleteContainerByName(container.substring(1));
            }
        }
    }

    public List<Image> getAllDockerImages(DockerClient dockerClient) {
        return dockerClient.listImagesCmd().exec();
    }

    public List<Container> getAllContainers() {
        return this.dockerClient.listContainersCmd().exec();
    }

    public List<String> getAllContainerNames() throws MangleException {
        List<Container> containers = null;
        try {
            containers = this.dockerClient.listContainersCmd().exec();
        } catch (ProcessingException e) {
            throw new MangleException(ErrorConstants.DOCKER_CONNECTION_FAILURE, ErrorCode.DOCKER_CONNECTION_FAILURE,
                    e.getMessage());
        }
        List<String> containerList = new ArrayList<String>();
        if (containers != null && !containers.isEmpty()) {
            for (Container container : containers) {
                containerList.add(container.getNames()[0].substring(1));
            }
        }
        return containerList;
    }

    public List<Image> getAllDockerImageNames(DockerClient dockerClient) {
        return dockerClient.listImagesCmd().exec();
    }

    public void pullImage(String imageName) {
        dockerClient.pullImageCmd(imageName).exec(new PullImageResultCallback()).awaitSuccess();
    }

    public String findContainerId(String containerName) throws MangleException {
        List<Container> allContainers = null;
        try {
            allContainers = dockerClient.listContainersCmd().exec();
        } catch (ProcessingException e) {
            throw new MangleException(ErrorConstants.DOCKER_CONNECTION_FAILURE, ErrorCode.DOCKER_CONNECTION_FAILURE,
                    e.getMessage());
        }
        String containerID = null;
        outerLoop: for (Container eachContaier : allContainers) {
            String[] names = eachContaier.getNames();
            for (String each : names) {
                if (each.substring(1).equals(containerName)) {
                    containerID = eachContaier.getId();
                    break outerLoop;
                }
            }
        }
        if (StringUtils.isEmpty(containerID)) {
            throw new MangleException(ErrorConstants.CONTAINER_NOT_AVAILABLE, ErrorCode.CONTAINER_NOT_AVAILABLE,
                    containerName);
        }
        return containerID;
    }

    public Container getContainerByName(String containerName) {
        List<Container> allContainers = dockerClient.listContainersCmd().exec();
        Container container = null;
        for (Container eachContaier : allContainers) {
            String[] names = eachContaier.getNames();
            for (String each : names) {
                if (each.substring(1).equals(containerName)) {
                    container = eachContaier;
                    break;
                }
            }
        }
        return container;
    }

    public boolean isContainerRunningByName(String containerName) throws MangleException {
        return null != findContainerId(containerName);
    }

    public boolean isContainerRunningByID(String containerID) {
        List<Container> allContainers = dockerClient.listContainersCmd().exec();
        for (Container eachContaier : allContainers) {
            if (eachContaier.getId().equals(containerID)) {
                return true;
            }
        }
        return false;
    }

    public boolean isContainerPaused(String containerID) {
        List<String> statusFilterList = new ArrayList<>();
        statusFilterList.add("paused");
        List<Container> allContainers = dockerClient.listContainersCmd().withStatusFilter(statusFilterList).exec();
        for (Container eachContaier : allContainers) {
            if (eachContaier.getId().equals(containerID)) {
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
                if (each.substring(1).equals(containerName)) {
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
        } catch (ProcessingException processingException) {
            if (processingException.getMessage().contains("org.apache.http.client.ClientProtocolException")) {
                throw new MangleException(processingException, ErrorCode.DIRECTORY_NOT_FOUND, destFilePath);
            }
        }
        return true;
    }

    public String getDockerIPByName(String containerName) throws MangleException {
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
    public boolean testConnection() throws MangleException {
        try {
            this.dockerClient.pingCmd().exec();
            return true;
        } catch (ProcessingException exception) {
            if (exception.getCause() instanceof SSLHandshakeException) {
                if (exception.getCause().getMessage().contains(ErrorConstants.DOCKER_BAD_CERTIFICATE)) {
                    throw new MangleException(ErrorCode.DOCKER_BAD_CERTIFICATE_ERROR);
                }
                throw new MangleException(ErrorCode.DOCKER_TLS_VERIFY_ENABLED_ERROR);
            }
            if (exception.getCause() instanceof ClientProtocolException
                    && exception.getCause().getCause() instanceof ProtocolException) {
                throw new MangleException(ErrorCode.DOCKER_TLS_ENABLED_ERROR);
            }
            if (exception.getCause() instanceof IllegalArgumentException
                    && exception.getCause().getMessage().contains(ErrorConstants.DOCKER_HOST_NAME_NULL)) {
                throw new MangleException(ErrorCode.DOCKER_INVALID_HOSTNAME_OR_PORT);
            }

            throw new MangleException(exception.getCause().getMessage(), ErrorCode.DOCKER_CONNECTION_FAILURE,
                    exception.getCause().getMessage());
        } catch (DockerClientException clientException) {
            throw new MangleException(ErrorCode.DOCKER_CONNECTION_FAILURE);
        }
    }

    public void setDockerClient(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }
}