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

import static com.vmware.mangle.utils.clients.kubernetes.KubernetesTemplates.GET_METADATA_NAME;
import static com.vmware.mangle.utils.clients.kubernetes.KubernetesTemplates.GET_SERVICES_JSONPATH;
import static com.vmware.mangle.utils.clients.kubernetes.KubernetesTemplates.GET_SERVICE_APP_NAMES;
import static com.vmware.mangle.utils.clients.kubernetes.KubernetesTemplates.GET_SERVICE_LB_HOSTNAME;
import static com.vmware.mangle.utils.clients.kubernetes.KubernetesTemplates.TEMPLATE_ERROR_MESSAGE;

import java.util.Arrays;
import java.util.List;

import lombok.extern.log4j.Log4j2;

import com.vmware.mangle.utils.CommandUtils;

/**
 * @author bkaranam
 *
 *         Class with all the helper methods related to kubernetes services
 */
@Log4j2
public class ServiceClient {
    private String kubectl;

    protected ServiceClient(String kubectl) {
        this.kubectl = kubectl;
    }

    public List<String> getServices() {
        log.info("Getting all kubernetes services as list");
        return Arrays.asList(CommandUtils.runCommand(kubectl + GET_SERVICES_JSONPATH + GET_METADATA_NAME)
                .getCommandOutput().split("\\s+"));
    }

    public String getServiceLoadBalancerHostName(String labels) {
        log.info("Getting loadbalancer Hostname of a service with labels " + labels);
        String output = CommandUtils.runCommand(kubectl + String.format(GET_SERVICE_LB_HOSTNAME, labels))
                .getCommandOutput();
        if (output.contains(TEMPLATE_ERROR_MESSAGE) || output.isEmpty()) {
            return null;
        }
        log.info("LoadBalancer Hostname found: " + output);
        return output;
    }

    /**
     * Method to get the APP Names
     *
     * @return list of apps
     */
    public List<String> getAppNames() {
        log.info("Get the AppNames");
        return Arrays.asList(CommandUtils.runCommand(kubectl + GET_SERVICE_APP_NAMES).getCommandOutput()
                .split("\\s+"));
    }

}
