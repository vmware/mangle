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

import java.util.Arrays;

import lombok.experimental.UtilityClass;

import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.utils.clients.kubernetes.KubernetesTemplates;

/**
 * @author hkilari
 * @author bkaranam
 *
 */
@UtilityClass

public class K8sCommandInfoHelper {

    static CommandInfo getK8sCheckContainerStateCommand(String resourceName, String containerName) {
        CommandInfo checkContainerStateInfo = new CommandInfo();
        checkContainerStateInfo.setIgnoreExitValueCheck(false);
        checkContainerStateInfo.setNoOfRetries(30);
        checkContainerStateInfo.setRetryInterval(10);
        checkContainerStateInfo
                .setCommand(String.format(KubernetesTemplates.IS_CONTAINER_READY, resourceName, containerName));
        checkContainerStateInfo.setExpectedCommandOutputList(Arrays.asList("true"));
        return checkContainerStateInfo;
    }


    static CommandInfo getK8sPatchContainerWithOriginalImageCommandInfo(String resourceName, String containerName,
            String originalImage) {
        CommandInfo k8sPatchCommandInfo = new CommandInfo();
        k8sPatchCommandInfo.setIgnoreExitValueCheck(false);
        k8sPatchCommandInfo.setCommand(String.format(KubernetesTemplates.PATCH_CONTAINER_IMAGE_OF_POD, resourceName,
                containerName, originalImage));
        return k8sPatchCommandInfo;
    }

    static CommandInfo getK8sPatchContainerWithNginxImageCommandInfo(String resourceName, String containerName) {
        CommandInfo k8sPatchCommandInfo = new CommandInfo();
        k8sPatchCommandInfo.setIgnoreExitValueCheck(false);
        k8sPatchCommandInfo.setCommand(String.format(KubernetesTemplates.PATCH_CONTAINER_IMAGE_OF_POD, resourceName,
                containerName, KubernetesTemplates.NGINX_CONTAINER_IMAGE));
        return k8sPatchCommandInfo;
    }

    static CommandInfo getK8sServicePatchWithSelectorsCommandInfo(String resourceName, String selectors) {
        CommandInfo k8sServicePatchCommandInfo = new CommandInfo();
        k8sServicePatchCommandInfo.setIgnoreExitValueCheck(false);
        k8sServicePatchCommandInfo
                .setCommand(String.format(KubernetesTemplates.PATCH_SERVICE_WITH_SELECTORS, resourceName, selectors));
        return k8sServicePatchCommandInfo;
    }

    static CommandInfo getK8sNodeUnCordonCommand(String resourceName) {
        CommandInfo nodeUnCordonCommandInfo = new CommandInfo();
        nodeUnCordonCommandInfo.setIgnoreExitValueCheck(false);
        nodeUnCordonCommandInfo.setCommand(KubernetesTemplates.UNCORDON + resourceName);
        return nodeUnCordonCommandInfo;
    }

    static CommandInfo getK8sCheckForReadinessProbeCommand(String resourceName, String containerName) {
        CommandInfo checkForReadinessProbeCommand = new CommandInfo();
        checkForReadinessProbeCommand.setCommand(String.format(KubernetesTemplates.IS_CONTAINER_HAS_READINESS_PROBE,
                resourceName, containerName, containerName));
        checkForReadinessProbeCommand.setExpectedCommandOutputList(Arrays.asList("ReadinessProbe Configured"));
        return checkForReadinessProbeCommand;
    }

    static CommandInfo getK8sCheckContainerStateCommandInfo(String resourceName, String containerName) {
        CommandInfo checkContainerStateInfo = new CommandInfo();
        checkContainerStateInfo.setIgnoreExitValueCheck(false);
        checkContainerStateInfo.setNoOfRetries(30);
        checkContainerStateInfo.setRetryInterval(10);
        checkContainerStateInfo
                .setCommand(String.format(KubernetesTemplates.IS_CONTAINER_READY, resourceName, containerName));
        checkContainerStateInfo.setExpectedCommandOutputList(Arrays.asList("false"));
        return checkContainerStateInfo;
    }

    static CommandInfo getK8sServiceEndpointsCommandInfo(String resourceName,String ... expectedOutPut) {
        CommandInfo checkContainerStateInfo = new CommandInfo();
        checkContainerStateInfo.setIgnoreExitValueCheck(false);
        checkContainerStateInfo.setNoOfRetries(30);
        checkContainerStateInfo.setRetryInterval(10);
        checkContainerStateInfo.setCommand(String.format(KubernetesTemplates.GET_SERVICE_ENDPOINTS, resourceName));
        checkContainerStateInfo.setExpectedCommandOutputList(Arrays.asList(expectedOutPut));
        return checkContainerStateInfo;
    }


}
