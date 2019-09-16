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

package com.vmware.mangle.faults.plugin.helpers.k8s;

import static com.vmware.mangle.utils.constants.FaultConstants.OPERATION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SResourceNotReadyFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.services.enums.K8SFaultName;
import com.vmware.mangle.services.enums.K8SResource;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.skeletons.ICommandExecutionFaultHelper;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.RetryUtils;
import com.vmware.mangle.utils.clients.kubernetes.KubernetesTemplates;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author bkaranam Implementation of {@link ICommandExecutionFaultHelper} to support K8S specific
 *         faults
 */
@Log4j2
@Component
public class K8sFaultHelper implements ICommandExecutionFaultHelper {
    EndpointClientFactory endpointClientFactory;

    @Autowired
    public K8sFaultHelper(EndpointClientFactory endpointClientFactory) {
        this.endpointClientFactory = endpointClientFactory;
    }

    @Override
    public ICommandExecutor getExecutor(CommandExecutionFaultSpec k8sFaultSpec) throws MangleException {
        return (ICommandExecutor) endpointClientFactory.getEndPointClient(k8sFaultSpec.getCredentials(),
                k8sFaultSpec.getEndpoint());
    }

    @Override
    public List<CommandInfo> getInjectionCommandInfoList(ICommandExecutor executor,
            CommandExecutionFaultSpec k8sFaultSpec) throws MangleException {
        return buildInjectionCommandList(executor, (K8SFaultSpec) k8sFaultSpec);
    }

    @Override
    public List<CommandInfo> getRemediationCommandInfoList(ICommandExecutor executor,
            CommandExecutionFaultSpec k8sFaultSpec) throws MangleException {
        List<CommandInfo> commandList = null;
        String operation = k8sFaultSpec.getArgs().get(OPERATION);
        switch (K8SFaultName.valueOf(operation)) {
        case NOTREADY_RESOURCE:
            commandList = getResourceNotReadyFaultRemediationCommand(executor, (K8SFaultSpec) k8sFaultSpec);
            break;
        case DELETE_RESOURCE:
            commandList = Collections.emptyList();
            break;
        case SERVICE_UNAVAILABLE:
            commandList = getServiceUnavailableFaultRemediationCommand(executor, (K8SFaultSpec) k8sFaultSpec);
            break;
        default:
            throw new MangleException(ErrorCode.UNSUPPORTED_FAULT, operation);
        }
        return commandList;
    }

    @Override
    public List<SupportScriptInfo> getAgentFaultInjectionScripts() {
        //Right now no Injection scripts required for this task
        return Collections.emptyList();
    }

    @Override
    public void checkTaskSpecificPrerequisites() throws MangleException {
        //Right not identified any task specific prerequisites to implement
    }

    /**
     * Method to construct injection command using ResoureType, Operation and Labels if specified
     *
     * @param k8sFaultSpec
     *
     * @return {@link String} command
     * @throws MangleException
     */
    private List<CommandInfo> buildInjectionCommandList(ICommandExecutor executor, K8SFaultSpec k8sFaultSpec)
            throws MangleException {
        String operation = k8sFaultSpec.getArgs().get(OPERATION);
        List<CommandInfo> commandList = null;
        switch (K8SFaultName.valueOf(operation)) {
        case NOTREADY_RESOURCE:
            commandList = getResourceNotReadyFaultInjectionCommand(k8sFaultSpec);
            break;
        case DELETE_RESOURCE:
            commandList = getInjectionCommandList(k8sFaultSpec);
            break;
        case SERVICE_UNAVAILABLE:
            commandList = getServiceUnavailableFaultInjectionCommand(executor, k8sFaultSpec);
            break;
        default:
            throw new MangleException(ErrorCode.UNSUPPORTED_FAULT, operation);
        }
        return commandList;
    }

    /**
     * Method to generate remediation commands for ResourceNotReady fault
     *
     * @param executor
     * @param
     */
    private List<CommandInfo> getResourceNotReadyFaultRemediationCommand(ICommandExecutor executor,
            K8SFaultSpec k8sFaultSpec) throws MangleException {
        log.info("Generating Remediation command for K8S ResourceNotReady fault");
        List<CommandInfo> commandInfoList = new ArrayList<>();
        for (String resourceName : k8sFaultSpec.getResourcesList()) {
            if (k8sFaultSpec.getResourceType().equals(K8SResource.POD)) {
                String containerName = ((K8SResourceNotReadyFaultSpec) (k8sFaultSpec)).getAppContainerName();
                String originalImage = executor
                        .executeCommand(
                                String.format(KubernetesTemplates.GET_CONTAINER_IMAGE, resourceName, containerName))
                        .getCommandOutput().trim();

                CommandInfo k8sPatchContainerCommandInfo = K8sCommandInfoHelper
                        .getK8sPatchContainerWithOriginalImageCommandInfo(resourceName, containerName, originalImage);
                commandInfoList.add(k8sPatchContainerCommandInfo);
                commandInfoList.add(K8sCommandInfoHelper.getK8sCheckContainerStateCommand(resourceName, containerName));

                log.debug("Remediation command for K8S " + k8sFaultSpec.getResourceType().name() + "not ready fault:"
                        + k8sPatchContainerCommandInfo.getCommand());

            } else if (k8sFaultSpec.getResourceType().equals(K8SResource.NODE)) {
                CommandInfo nodeUnCordonCommandInfo = K8sCommandInfoHelper.getK8sNodeUnCordonCommand(resourceName);
                commandInfoList.add(nodeUnCordonCommandInfo);
                log.debug("Remediation command for K8S " + k8sFaultSpec.getResourceType().name() + "not ready fault:"
                        + nodeUnCordonCommandInfo.getCommand());
            } else {
                throw new MangleException(ErrorCode.UNSUPPORTED_K8S_RESOURCE_TYPE);
            }


        }
        return commandInfoList;
    }

    /**
     * Method to generate remediation commands for Service Unavailable fault
     *
     * @param executor
     * @param k8sFaultSpec
     * @return list of remediation commands
     */
    private List<CommandInfo> getServiceUnavailableFaultRemediationCommand(ICommandExecutor executor,
            K8SFaultSpec k8sFaultSpec) throws MangleException {
        String message = "Generating Remediation command for K8S Service Unavailable fault";
        log.info(message);
        List<CommandInfo> commandInfoList = new ArrayList<>();
        for (String resourceName : k8sFaultSpec.getResourcesList()) {
            String originalServiceSelectors =
                    executor.executeCommand(String.format(KubernetesTemplates.GET_SERVICE_SELECTORS, resourceName))
                            .getCommandOutput().trim();
            if (StringUtils.startsWithIgnoreCase(originalServiceSelectors, KubernetesTemplates.SELECTORS_PREFIX)
                    && originalServiceSelectors.split(KubernetesTemplates.SELECTORS_PREFIX).length > 0) {
                originalServiceSelectors = originalServiceSelectors.split(KubernetesTemplates.SELECTORS_PREFIX)[1];

                CommandInfo k8sServicePatchCommandInfo = K8sCommandInfoHelper
                        .getK8sServicePatchWithSelectorsCommandInfo(resourceName, originalServiceSelectors);
                commandInfoList.add(k8sServicePatchCommandInfo);

                commandInfoList.add(K8sCommandInfoHelper.getK8sServiceEndpointsCommandInfo(resourceName, "addresses"));

                log.debug("Remediation command for K8S " + k8sFaultSpec.getResourceType().name() + " unavailable fault:"
                        + k8sServicePatchCommandInfo.getCommand());
            }
        }
        return commandInfoList;
    }

    /**
     * Method to generate injection commands for ResourceNotReady fault
     *
     * @throws MangleException
     */
    private List<CommandInfo> getResourceNotReadyFaultInjectionCommand(K8SFaultSpec k8sFaultSpec)
            throws MangleException {
        List<CommandInfo> commandInfoList = new ArrayList<>();
        for (String resourceName : k8sFaultSpec.getResourcesList()) {

            if (k8sFaultSpec.getResourceType().equals(K8SResource.POD)) {

                String containerName = ((K8SResourceNotReadyFaultSpec) (k8sFaultSpec)).getAppContainerName();

                commandInfoList
                        .add(K8sCommandInfoHelper.getK8sCheckForReadinessProbeCommand(resourceName, containerName));

                CommandInfo k8sPatchCommandInfo =
                        K8sCommandInfoHelper.getK8sPatchContainerWithNginxImageCommandInfo(resourceName, containerName);
                commandInfoList.add(k8sPatchCommandInfo);
                commandInfoList
                        .add(K8sCommandInfoHelper.getK8sCheckContainerStateCommandInfo(resourceName, containerName));
                log.debug("Injection command for K8S " + k8sFaultSpec.getResourceType().name() + " not ready fault:"
                        + k8sPatchCommandInfo.getCommand());
            } else if (k8sFaultSpec.getResourceType().equals(K8SResource.NODE)) {
                CommandInfo nodeCordonCommandInfo = new CommandInfo();
                nodeCordonCommandInfo.setIgnoreExitValueCheck(false);
                String command = KubernetesTemplates.CORDON + resourceName;
                nodeCordonCommandInfo.setCommand(command);
                commandInfoList.add(nodeCordonCommandInfo);
                log.debug("Injection command for K8S " + k8sFaultSpec.getResourceType().name() + " not ready fault:"
                        + nodeCordonCommandInfo.getCommand());
            } else {
                throw new MangleException(ErrorCode.UNSUPPORTED_K8S_RESOURCE_TYPE);
            }
        }
        return commandInfoList;
    }

    /**
     * Method to generate injection commands for Service Unavailable fault
     *
     * @throws MangleException
     */
    private List<CommandInfo> getServiceUnavailableFaultInjectionCommand(ICommandExecutor executor,
            K8SFaultSpec k8sFaultSpec) throws MangleException {
        List<CommandInfo> commandInfoList = new ArrayList<>();
        for (String resourceName : k8sFaultSpec.getResourcesList()) {
            String originalServiceKey =
                    executor.executeCommand(String.format(KubernetesTemplates.GET_SERVICE_SELECTORS_KEY, resourceName))
                            .getCommandOutput().trim();
            if (StringUtils.startsWithIgnoreCase(originalServiceKey, KubernetesTemplates.SERVICE_KEY_PREFIX)
                    && originalServiceKey.split(KubernetesTemplates.SERVICE_KEY_PREFIX).length > 0) {
                originalServiceKey = originalServiceKey.split(KubernetesTemplates.SERVICE_KEY_PREFIX)[1];
                CommandInfo k8sServicePatchCommandInfo =
                        K8sCommandInfoHelper.getK8sServicePatchWithSelectorsCommandInfo(resourceName, String
                                .format(KubernetesTemplates.DEFAULT_MANGLE_SERVICE_SELECTORS, originalServiceKey));
                commandInfoList.add(k8sServicePatchCommandInfo);
                commandInfoList.add(K8sCommandInfoHelper.getK8sServiceEndpointsCommandInfo(resourceName, "<no value>"));
                log.debug("Injection command for K8S " + k8sFaultSpec.getResourceType().name() + " unavailable fault:"
                        + k8sServicePatchCommandInfo.getCommand());
            }
        }
        return commandInfoList;
    }

    /**
     * Method to generate Injection commands for generic K8S resource fault
     */
    private List<CommandInfo> getInjectionCommandList(K8SFaultSpec k8sFaultSpec) {

        List<CommandInfo> commandInfoList = new ArrayList<>();
        CommandInfo k8sCommandInfo = new CommandInfo();
        k8sCommandInfo.setIgnoreExitValueCheck(false);
        String command = KubernetesTemplates.DELETE + " " + k8sFaultSpec.getResourceType().getValue() + " ";
        //condition to check randonInjection and use -l option with labels accordingly
        if (k8sFaultSpec.getResourcesList().size() == 1) {
            command += k8sFaultSpec.getResourcesList().get(0);
        } else {
            command += "-l " + CommonUtils.maptoDelimitedKeyValuePairString(k8sFaultSpec.getResourceLabels(), ",");
        }
        k8sCommandInfo.setCommand(command);
        commandInfoList.add(k8sCommandInfo);
        return commandInfoList;
    }

    /**
     * Method to select resource randomly if resource labels specified in the input
     *
     * @return {@link String} resourceName
     * @throws MangleException
     */
    public List<String> getResouceList(ICommandExecutor executor, K8SFaultSpec k8sFaultSpec) throws MangleException {
        List<String> resources = new ArrayList<>();
        if (StringUtils.isNotEmpty(k8sFaultSpec.getResourceName())) {
            resources.add(k8sFaultSpec.getResourceName());
            return resources;
        }
        if (CollectionUtils.isEmpty(k8sFaultSpec.getResourceLabels())) {
            throw new MangleException(ErrorCode.INVALID_RESOURCE_LABELS, k8sFaultSpec.getResourceLabels());
        }
        String errorMessage =
                "No " + k8sFaultSpec.getResourceType().getValue() + " resources identified with given Labels:"
                        + CommonUtils.maptoDelimitedKeyValuePairString(k8sFaultSpec.getResourceLabels(), ",");
        String command =
                String.format(KubernetesTemplates.GET_RESOURCES_WITH_LABELS, k8sFaultSpec.getResourceType().getValue(),
                        CommonUtils.maptoDelimitedKeyValuePairString(k8sFaultSpec.getResourceLabels(), ","));
        String commandOutput = RetryUtils.retry(() -> {
            CommandExecutionResult result = executor.executeCommand(command);
            if (result.getExitCode() == 0 && !StringUtils.isBlank(result.getCommandOutput())
                    && !result.getCommandOutput().contains("error")) {
                return result.getCommandOutput();
            } else {
                throw new MangleException(result.getCommandOutput(), ErrorCode.NO_PODS_IDENTIFIED);
            }
        }, new MangleException(errorMessage, ErrorCode.INVALID_RESOURCE_LABELS, k8sFaultSpec.getResourceLabels()), 6,
                10);
        resources = Arrays.asList(commandOutput.trim().split("\\s+"));
        if (k8sFaultSpec.isRandomInjection() && !k8sFaultSpec.getResourceLabels().isEmpty()) {
            String randomResource = resources.get((new Random().nextInt(resources.size())));
            resources = new ArrayList<>();
            resources.add(randomResource);
        }
        return resources;
    }
}
