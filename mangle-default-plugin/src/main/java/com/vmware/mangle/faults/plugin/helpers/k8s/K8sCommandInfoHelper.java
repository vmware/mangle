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
        return CommandInfo.builder(String.format(KubernetesTemplates.IS_CONTAINER_READY, resourceName, containerName))
                .ignoreExitValueCheck(false).noOfRetries(30).retryInterval(10)
                .expectedCommandOutputList(Arrays.asList("true")).build();
    }

    static CommandInfo getK8sPatchContainerWithOriginalImageCommandInfo(String resourceName, String containerName,
            String originalImage) {
        return CommandInfo.builder(String.format(KubernetesTemplates.PATCH_CONTAINER_IMAGE_OF_POD, resourceName,
                containerName, originalImage)).ignoreExitValueCheck(false).build();
    }

    static CommandInfo getK8sPatchContainerWithNginxImageCommandInfo(String resourceName, String containerName) {
        return CommandInfo.builder(String.format(KubernetesTemplates.PATCH_CONTAINER_IMAGE_OF_POD, resourceName,
                containerName, KubernetesTemplates.NGINX_CONTAINER_IMAGE)).ignoreExitValueCheck(false).build();
    }

    static CommandInfo getK8sServicePatchWithSelectorsCommandInfo(String resourceName, String selectors) {
        return CommandInfo
                .builder(String.format(KubernetesTemplates.PATCH_SERVICE_WITH_SELECTORS, resourceName, selectors))
                .ignoreExitValueCheck(false).build();
    }

    static CommandInfo getK8sNodeUnCordonCommand(String resourceName) {
        return CommandInfo.builder(KubernetesTemplates.UNCORDON + resourceName).ignoreExitValueCheck(false).build();
    }

    static CommandInfo getK8sCheckForReadinessProbeCommand(String resourceName, String containerName) {
        return CommandInfo
                .builder(String.format(KubernetesTemplates.IS_CONTAINER_HAS_READINESS_PROBE, resourceName,
                        containerName, containerName))
                .expectedCommandOutputList(Arrays.asList("ReadinessProbe Configured")).build();
    }

    static CommandInfo getK8sCheckContainerStateCommandInfo(String resourceName, String containerName) {
        return CommandInfo.builder(String.format(KubernetesTemplates.IS_CONTAINER_READY, resourceName, containerName))
                .ignoreExitValueCheck(false).noOfRetries(30).retryInterval(10)
                .expectedCommandOutputList(Arrays.asList("false")).build();
    }

    static CommandInfo getK8sServiceEndpointsCommandInfo(String resourceName, String... expectedOutPut) {
        return CommandInfo.builder(String.format(KubernetesTemplates.GET_SERVICE_ENDPOINTS, resourceName))
                .ignoreExitValueCheck(false).noOfRetries(30).retryInterval(10)
                .expectedCommandOutputList(Arrays.asList(expectedOutPut)).build();
    }
}