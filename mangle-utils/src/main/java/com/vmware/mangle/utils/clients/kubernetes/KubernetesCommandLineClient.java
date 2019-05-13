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

package com.vmware.mangle.utils.clients.kubernetes;

import static com.vmware.mangle.utils.clients.kubernetes.KubernetesTemplates.BUSYBOX_POD_NAME_TEMPLATE;
import static com.vmware.mangle.utils.clients.kubernetes.KubernetesTemplates.KUBECTL_RUN_BUSYBOX_CURL_TEMPLATE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.log4j.Log4j2;
import org.joda.time.DateTimeUtils;

import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.utils.CommandUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.endpoint.EndpointClient;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author bkaranam Class to create KubernetesCommandLine client
 */
@Log4j2
public class KubernetesCommandLineClient implements ICommandExecutor, EndpointClient {
    private static KubernetesCommandLineClient kubeClient = null;
    private String kubectl = null;
    private PODClient podClient;
    private ServiceClient serviceClient;
    private NodeClient nodeClient;
    private String kubeConfigLocation = null;
    private String nameSpace = null;

    private KubernetesCommandLineClient() {
    }

    /**
     * Static method to get Kubernetes Command line CLient with deault kube configuration located at
     * user home
     *
     * @return
     */
    public static KubernetesCommandLineClient getClient() {
        log.info("Creating kubernetes commandline client");
        if (null == kubeClient || null == kubeClient.kubeConfigLocation) {
            kubeClient = new KubernetesCommandLineClient();
            kubeClient.kubectl = "kubectl";
            setClients(kubeClient);
        }
        return kubeClient;
    }

    /**
     * Static method to get Kubernetes Command line CLient with deault kube configuration located at
     * user home
     *
     * @return
     */
    public KubernetesCommandLineClient setNameSpace(String namespace) {
        if (namespace != null) {
            this.nameSpace = namespace;
            KubernetesCommandLineClient.kubeClient.kubectl += " --namespace " + namespace;
            setClients(kubeClient);
        }
        return kubeClient;
    }

    /**
     * Static method to get Kubernetes Command line CLient with kubeconfig
     *
     * @return KubernetesCommandLineClient
     */
    public KubernetesCommandLineClient setKubeconfig(String kubeConfigYaml) {
        if (kubeConfigYaml != null) {
            KubernetesCommandLineClient.kubeClient.kubectl += " --kubeconfig " + kubeConfigYaml;
            setClients(kubeClient);
        }
        return kubeClient;
    }

    /**
     * Static method to get Kubernetes Command line CLient
     *
     * @param kubeConfigFileLocation-
     *            Location to Kubernetes Configuration file
     * @return
     */
    public static KubernetesCommandLineClient getClient(String kubeConfigFileLocation) {
        log.info("Creating kubernetes commandline client");
        if (null == kubeClient || null != kubeClient.kubeConfigLocation
                || !kubeConfigFileLocation.equals(kubeClient.kubeConfigLocation)) {
            kubeClient = new KubernetesCommandLineClient();
            kubeClient.kubectl = "kubectl --kubeconfig " + kubeConfigFileLocation;
            kubeClient.kubeConfigLocation = kubeConfigFileLocation;
            setClients(kubeClient);
        }
        return kubeClient;
    }

    private static void setClients(KubernetesCommandLineClient client) {
        client.podClient = new PODClient(client.kubectl);
        client.serviceClient = new ServiceClient(client.kubectl);
        client.nodeClient = new NodeClient(client.kubectl);
    }

    public PODClient getPODClient() {
        return podClient;
    }

    public NodeClient getNodeClient() {
        return nodeClient;
    }

    public ServiceClient getServiceClient() {
        return serviceClient;
    }

    public String runKubectlBusyboxCurlCommand(String inputCommand, String podName, long timeout) {
        if (null == podName) {
            podName = String.format(BUSYBOX_POD_NAME_TEMPLATE, DateTimeUtils.currentTimeMillis());
        }
        String output = CommandUtils
                .runCommand(kubectl + " " + String.format(KUBECTL_RUN_BUSYBOX_CURL_TEMPLATE, podName) + inputCommand,
                        timeout)
                .getCommandOutput().replaceAll("\n", " ");
        Matcher matcher = Pattern.compile("(.*?) pod \"" + podName + "\" deleted").matcher(output);
        return matcher.find() ? matcher.group(1) : output;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    @Override
    public CommandExecutionResult executeCommand(String command) {
        CommandExecutionResult result = CommandUtils.runCommand(kubectl + " " + command);
        log.trace("Executed command: " + kubectl + " " + command + "Command execution Result: " + result);
        return result;
    }

    @Override
    public boolean testConnection() throws MangleException {
        String getNamespaceCommand = KubernetesTemplates.GET + KubernetesTemplates.NAMESPACE;
        CommandExecutionResult output = CommandUtils.runCommand(this.kubectl + getNamespaceCommand + getNameSpace());
        return validateK8SCommandOutput(output.getCommandOutput());
    }

    private boolean validateK8SCommandOutput(String commandOutput) throws MangleException {
        if (commandOutput.contains(ErrorConstants.K8S_ERROR_LOADING_CONFIG_FILE)) {
            throw new MangleException(ErrorCode.K8S_INVALID_CONFIG_FILE);
        } else if (commandOutput.contains(ErrorConstants.K8S_RESOURCE_NOT_FOUND)) {
            throw new MangleException(ErrorCode.K8S_NAMESPACE_NOT_FOUND, getNameSpace());
        } else if (commandOutput.contains(ErrorConstants.K8S_ERROR_CONNECTING_SERVER)) {
            throw new MangleException(ErrorCode.K8S_ERROR_FROM_SERVER, commandOutput);
        } else if (commandOutput.contains(ErrorConstants.K8S_SERVER_DOES_NOT_HAVE_RESOURCE_TYPE)) {
            throw new MangleException(ErrorCode.K8S_TEST_CONNECTION_ERROR);
        } else if (commandOutput.toLowerCase().contains(ErrorConstants.K8S_ERROR)) {
            throw new MangleException(ErrorCode.K8S_ERROR_FROM_SERVER, commandOutput);
        } else {
            return true;
        }
    }

    public static void validateConfigFile(String configFilePath) throws MangleException {
        CommandExecutionResult output = CommandUtils.runCommand("kubectl --kubeconfig " + configFilePath + " version");
        if (output.getCommandOutput().contains(ErrorConstants.K8S_ERROR_LOADING_CONFIG_FILE)) {
            throw new MangleException(ErrorCode.K8S_INVALID_CONFIG_FILE);
        }
    }
}
