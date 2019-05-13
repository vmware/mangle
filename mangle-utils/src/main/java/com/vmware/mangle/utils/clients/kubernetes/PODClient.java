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

import static com.vmware.mangle.utils.clients.kubernetes.KubernetesTemplates.GET_ALLPOD_IPS_USING_LABELS;
import static com.vmware.mangle.utils.clients.kubernetes.KubernetesTemplates.GET_ALLPOD_NODES_USING_LABELS;
import static com.vmware.mangle.utils.clients.kubernetes.KubernetesTemplates.GET_ALLPOD_USING_LABELS;
import static com.vmware.mangle.utils.clients.kubernetes.KubernetesTemplates.GET_METADATA_NAME;
import static com.vmware.mangle.utils.clients.kubernetes.KubernetesTemplates.GET_POD;
import static com.vmware.mangle.utils.clients.kubernetes.KubernetesTemplates.GET_PODNODE_MAP;
import static com.vmware.mangle.utils.clients.kubernetes.KubernetesTemplates.GET_PODS_JSONPATH;
import static com.vmware.mangle.utils.clients.kubernetes.KubernetesTemplates.RESTART_PODS_WITH_LABELS;
import static com.vmware.mangle.utils.clients.kubernetes.KubernetesTemplates.RESTART_POD_WITH_NAME;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;

import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.utils.CommandUtils;
import com.vmware.mangle.utils.CommonUtils;

/**
 * @author bkaranam
 *
 *         Class with all the helper methods related to kubernetes pods
 */
@Log4j2
public class PODClient {
    private String kubectl;
    private static final String POD_DELETED_SUCCESS_MESSAGE = "deleted";

    protected PODClient(String kubectl) {
        this.kubectl = kubectl;
    }

    public List<String> getPods() {
        log.info("Getting all pod names as list");
        return Arrays.asList(CommandUtils.runCommand(kubectl + GET_PODS_JSONPATH + GET_METADATA_NAME).getCommandOutput()
                .split("\\s+"));
    }

    public String getPod(String podName) {
        log.info("Getting pod...");
        return CommandUtils.runCommand(kubectl + String.format(GET_POD, podName)).getCommandOutput();
    }

    /**
     * Method to get all pods with labels specified
     *
     * @param labels
     * @return
     */

    public List<String> getPodsWithLabels(String labels) {
        log.info("Getting all pod names labeled as:" + labels);
        String output = new String();
        for (int i = 0; i < 6; i++) {
            output = CommandUtils.runCommand(kubectl + String.format(GET_ALLPOD_USING_LABELS, labels))
                    .getCommandOutput();
            if (!StringUtils.isBlank(output) && !output.contains("error")) {
                break;
            }
            CommonUtils.delayInSeconds(10);
        }
        return StringUtils.isBlank(output) || output.contains("error") ? Arrays.asList()
                : Arrays.asList(output.trim().split("\\s+"));
    }

    /**
     * Method to get all pod IPs with labels specified
     *
     * @param labels
     * @return
     */
    public List<String> getPodIpsWithLabels(String labels) {
        log.info("Getting all pod ips labeled as:" + labels);
        String output = CommandUtils.runCommand(kubectl + String.format(GET_ALLPOD_IPS_USING_LABELS, labels))
                .getCommandOutput();
        return StringUtils.isBlank(output) ? Arrays.asList() : Arrays.asList(output.trim().split("\\s+"));
    }

    /**
     * Method to get all pod NodeNames with labels specified
     *
     * @param labels
     * @return
     */
    public List<String> getPodNodeNamesWithLabels(String labels) {
        log.info("Getting all pod Node Names labeled as:" + labels);
        String output = CommandUtils.runCommand(kubectl + String.format(GET_ALLPOD_NODES_USING_LABELS, labels))
                .getCommandOutput();
        return StringUtils.isBlank(output) ? Arrays.asList() : Arrays.asList(output.trim().split("\\s+"));
    }

    /**
     * Method to get pod and its node
     *
     * @return Map of pod and node names
     */
    public Map<String, String> getPodandNodeMap() {
        log.info("Creating map of Pod and Node of kubernetes cluster");
        String[] commandOutput = CommandUtils.runCommand(kubectl + GET_PODNODE_MAP).getCommandOutput().split("\\s+");
        Map<String, String> nodeMap = new HashMap<>();
        for (String mapString : commandOutput) {
            String[] mapStrings = mapString.split(":");
            nodeMap.put(mapStrings[0], mapStrings[1]);
        }
        return nodeMap;
    }

    /**
     * Method to get unique node names
     *
     * @return Array of unique node names
     */

    public String[] getUniqueNodeswithPods() {
        log.info("Getting unique nodes which have active pods running on them");
        return new HashSet<String>(getPodandNodeMap().values()).toArray(new String[0]);
    }

    public String[] getNodesExcludingPods(List<String> podNames) {
        log.info(
                "Getting unique nodes which have active pods running on them and excluding nodes on which following pods are running");
        log.info(podNames);
        Map<String, String> podtoNodeMap = getPodandNodeMap();
        Set<String> uniqueNodes = new HashSet<>(podtoNodeMap.values());
        for (String podName : podNames) {
            List<String> podList = podtoNodeMap.keySet().stream().filter(s -> s.startsWith(podName.toString()))
                    .collect(Collectors.toList());
            if (!podList.isEmpty()) {
                for (String pod : podList) {
                    uniqueNodes.remove(podtoNodeMap.get(pod));
                }
            }
        }
        return uniqueNodes.toArray(new String[0]);
    }

    /**
     * Method to get all the pods running on specified node
     *
     * @param nodeName
     * @return list of pod names
     */
    public List<String> getallPodsRunningOnNode(String nodeName) {
        log.info("Getting all pods running on node: " + nodeName);
        return getPodandNodeMap().entrySet().stream().filter(e -> e.getValue().equals(nodeName)).map(e -> e.getKey())
                .collect(Collectors.toList());
    }

    /**
     * Restart the pods with their label specified
     */
    public boolean restartPods(String label) {
        log.info("Restarting all the pods with label: " + label);
        List<String> podList = getPodsWithLabels(label);
        if (null == podList || podList.isEmpty()) {
            return false;
        }
        String output =
                CommandUtils.runCommand(kubectl + String.format(RESTART_PODS_WITH_LABELS, label)).getCommandOutput();
        if (!(output.contains(POD_DELETED_SUCCESS_MESSAGE))) {
            return false;
        }
        Collections.sort(podList);
        for (int i = 0; i < 6; i++) {
            List<String> currentPods = getPodsWithLabels(label);
            Collections.sort(currentPods);
            if ((podList.size() == currentPods.size()) && !(podList.equals(currentPods))) {
                return true;
            }
            CommonUtils.delayInSeconds(20);
        }
        return false;
    }

    /**
     * Restart the pods with their label specified
     */
    public boolean restartPod(String podName) {
        log.info("Restarting the pod : " + podName);
        String output = getPod(podName);
        if (null == output || output.contains("not found")) {
            return false;
        }
        output = CommandUtils.runCommand(kubectl + String.format(RESTART_POD_WITH_NAME, podName)).getCommandOutput();
        if (!(output.contains(POD_DELETED_SUCCESS_MESSAGE))) {
            return false;
        }
        for (int i = 0; i < 6; i++) {
            CommonUtils.delayInSeconds(20);
            if (getPod(podName).contains("not found")) {
                return true;
            }
        }
        return false;
    }

    public String getContainerMemoryUsed(String podName, String container) {
        log.info("Getting memory Used by container:" + container);
        String getPodCommandOutput = getPod(podName);
        if (null == getPodCommandOutput || getPodCommandOutput.contains("not found")) {
            return "";
        }
        CommandExecutionResult memoryUsedCommandOutput = CommandUtils.runCommand(
                kubectl + " top pod " + podName + " --containers | awk '$2 == \"" + container + "\" {print $4}'");
        log.info("Memory Used for container " + container + " is " + memoryUsedCommandOutput);
        return memoryUsedCommandOutput.getCommandOutput();
    }

    public String getContainerMemoryLimit(String podName, String container) {
        log.info("Getting memory limit of container:" + container);
        String getPodCommandOutput = getPod(podName);
        if (null == getPodCommandOutput || getPodCommandOutput.contains("not found")) {
            return "";
        }
        CommandExecutionResult memoryLimitCommandOutput = CommandUtils.runCommand(kubectl + " get pod " + podName
                + " -o=jsonpath='{.spec.containers[?(@.name==\"" + container + "\")].resources.limits.memory}'");
        log.info("Memory Limit for container " + container + " is " + memoryLimitCommandOutput);
        return memoryLimitCommandOutput.getCommandOutput();
    }
}

