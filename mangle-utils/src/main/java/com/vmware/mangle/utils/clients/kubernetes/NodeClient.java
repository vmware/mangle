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

import static com.vmware.mangle.utils.clients.kubernetes.KubernetesTemplates.CHECK_AVAILABILITY_NODE;
import static com.vmware.mangle.utils.clients.kubernetes.KubernetesTemplates.GET_METADATA_NAME;
import static com.vmware.mangle.utils.clients.kubernetes.KubernetesTemplates.GET_NODES_JSONPATH;
import static com.vmware.mangle.utils.clients.kubernetes.KubernetesTemplates.GET_NODE_EXTERNAL_IP_MAP;
import static com.vmware.mangle.utils.clients.kubernetes.KubernetesTemplates.GET_NODE_PODCIDR;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j2;

import com.vmware.mangle.utils.CommandUtils;

/**
 * @author bkaranam Class with all the helper methods related to kubernetes nodes
 */
@Log4j2
public class NodeClient {
    private String kubectl;

    protected NodeClient(String kubectl) {
        this.kubectl = kubectl;
    }

    @SuppressWarnings("deprecation")
    public List<String> getNodes() {
        log.info("Getting all node names as list");
        return Arrays.asList(CommandUtils.runCommand(kubectl + GET_NODES_JSONPATH + GET_METADATA_NAME)
                .getCommandOutput().split("\\s+"));
    }

    @SuppressWarnings("deprecation")
    public String getPodCIDR(String nodeName) {
        log.info("Getting Pod CIDR of node " + nodeName);
        return CommandUtils.runCommand(kubectl + String.format(GET_NODE_PODCIDR, nodeName)).getCommandOutput().trim();
    }

    /**
     * Method to get NodeName and its externalIP
     *
     * @return Map of Node Names and external IPs
     */
    public Map<String, String> getNodeandExternalIPMap() {
        log.info("Creating map of Node and ExternalIP of kubernetes cluster");
        String commandOutput = CommandUtils.runCommand(kubectl + GET_NODE_EXTERNAL_IP_MAP).getCommandOutput();
        List<String> commandOutputList = Arrays.asList(commandOutput.split("\\s+"));
        Map<String, String> nodeMap = new HashMap<>();
        commandOutputList.forEach(item -> nodeMap.put(item.replaceAll("^\\[|\\]$", "").split(",")[0],
                item.replaceAll("^\\[|\\]$", "").split(",")[1]));
        return nodeMap;
    }

    /**
     * Method to get the Nodes which have the 'Ready' status as 'False'
     *
     * @return list of nodes
     */
    public List<String> checkAvailabilityNode() {
        log.info("Get the Nodes which have the 'Ready' status as True");
        return Arrays
                .asList(CommandUtils.runCommand(kubectl + CHECK_AVAILABILITY_NODE).getCommandOutput().split("\\s+"));
    }

}
