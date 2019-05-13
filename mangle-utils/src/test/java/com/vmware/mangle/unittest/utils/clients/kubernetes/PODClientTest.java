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

package com.vmware.mangle.unittest.utils.clients.kubernetes;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.utils.CommandUtils;
import com.vmware.mangle.utils.clients.kubernetes.KubernetesCommandLineClient;
import com.vmware.mangle.utils.mockdata.CommandResultUtils;

/**
 *
 *
 * @author chetanc
 */
@PrepareForTest(value = { CommandUtils.class })
public class PODClientTest extends PowerMockTestCase {

    private String podNames = "testpod1 testPod2 testpod3 mangle";
    private String ips = "10.10.10.10 10.10.10.11 10.10.10.12";
    private String podName = "mangleWEB";
    private String podNodeMap = "testpod1:10.10.10.10 testPod2:10.10.10.11 mangle:10.10.10.12";
    private KubernetesCommandLineClient client;

    @BeforeMethod
    public void init() {
        PowerMockito.mockStatic(CommandUtils.class);
        client = KubernetesCommandLineClient.getClient();

    }

    @Test(description = "retrieve the pods using the pod client")
    public void testGetPods() {
        when(CommandUtils.runCommand(any())).thenReturn(CommandResultUtils.getCommandResult(podNames));
        List<String> pods = client.getPODClient().getPods();
        Assert.assertEquals(pods.size(), 4);
    }

    @Test(description = "retrieve the pod name using the pod client, that is matching the pod name, it is to verify if the pod with the name is running")
    public void testGetPod() {
        when(CommandUtils.runCommand(any())).thenReturn(CommandResultUtils.getCommandResult(podName));
        String pod = client.getPODClient().getPod(podName);
        Assert.assertEquals(pod, podName);
    }

    @Test(description = "retrieve the pods that match the given labels")
    public void testGetPodsWithLabels() {
        when(CommandUtils.runCommand(anyString())).thenReturn(CommandResultUtils.getCommandResult(podNames));
        List<String> result = client.getPODClient().getPodsWithLabels("saas");
        Assert.assertEquals(result.size(), 4);
    }

    @Test(description = "retrieve the ips of the pods that have the given matching label")
    public void testGetPodsIPsWithLabels() {
        when(CommandUtils.runCommand(anyString())).thenReturn(CommandResultUtils.getCommandResult(ips));
        List<String> resultIps = client.getPODClient().getPodIpsWithLabels("saas");
        Assert.assertEquals(resultIps.size(), 3);
    }

    @Test(description = "verify that the method getPodsIpsWithLabels return empty list when there doesn't exist pods matching the given label")
    public void testGetPodsIPsWithLabels2() {
        when(CommandUtils.runCommand(anyString())).thenReturn(CommandResultUtils.getCommandResult(""));
        List<String> resultIps = client.getPODClient().getPodIpsWithLabels("saas");
        Assert.assertEquals(resultIps.size(), 0);
    }

    @Test(description = "retrieve the names of the pods that have the given matching label")
    public void testGetPodNodeNamesWithLabels1() {
        when(CommandUtils.runCommand(anyString())).thenReturn(CommandResultUtils.getCommandResult(podNames));
        List<String> resultIps = client.getPODClient().getPodNodeNamesWithLabels("saas");
        Assert.assertEquals(resultIps.size(), 4);
    }

    @Test(description = "verify that the method getPodNodeNamesWithLabels return empty list when there doesn't exist pods matching the given label")
    public void testGetPodNodeNamesWithLabels2() {
        when(CommandUtils.runCommand(anyString())).thenReturn(CommandResultUtils.getCommandResult(""));
        List<String> resultIps = client.getPODClient().getPodNodeNamesWithLabels("saas");
        Assert.assertEquals(resultIps.size(), 0);
    }

    @Test(description = "get the list of all pod to node map in the given kubernetes cluster")
    public void testGetPodandNodeMap() {
        when(CommandUtils.runCommand(anyString())).thenReturn(CommandResultUtils.getCommandResult(podNodeMap));
        Map<String, String> map = client.getPODClient().getPodandNodeMap();
        Assert.assertEquals(map.size(), 3);
    }

    @Test(description = "get the list of all the unique nodes which contains pods")
    public void testGetUniqueNodeswithPods() {
        when(CommandUtils.runCommand(anyString())).thenReturn(CommandResultUtils.getCommandResult(podNodeMap));
        String[] uniquePods = client.getPODClient().getUniqueNodeswithPods();
        Assert.assertEquals(uniquePods.length, 3);
    }

    @Test(description = "get the list of all the unique nodes that doesn't have a given list of pods")
    public void testGetNodesExcludingPods() {
        List<String> pods = new ArrayList<>(Arrays.asList(podNames.split("\\s+")));
        when(CommandUtils.runCommand(anyString())).thenReturn(CommandResultUtils.getCommandResult(podNodeMap));
        String[] uniquePods = client.getPODClient().getNodesExcludingPods(pods);
        Assert.assertEquals(uniquePods.length, 0);
    }

    @Test(description = "verifying the list of all the pods that are running on the given node")
    public void testGetAllPodsRunningOnNode() {
        when(CommandUtils.runCommand(anyString())).thenReturn(CommandResultUtils.getCommandResult(podNodeMap));
        List<String> pods = client.getPODClient().getallPodsRunningOnNode("10.10.10.10");
        Assert.assertEquals(pods.size(), 1);
    }

    @Test(description = "verify the restarting of a pod matching the given pod name")
    public void testRestartPod() {
        when(CommandUtils.runCommand(anyString()))
                .thenReturn(CommandResultUtils.getCommandResult("All pods are deleted"));
        when(CommandUtils.runCommand(anyString())).thenReturn(CommandResultUtils.getCommandResult(podNames));
        Assert.assertFalse(client.getPODClient().restartPod("mangle"));
    }

    @Test(description = "verify the failure of restarting of a pod matching the given pod name")
    public void testRestartPod1() {
        when(CommandUtils.runCommand(anyString())).thenReturn(null);
        when(CommandUtils.runCommand(anyString())).thenReturn(CommandResultUtils.getCommandResult(podNames));
        Assert.assertFalse(client.getPODClient().restartPod("mangle"));
    }

    @Test(description = "verify the failure of restarting of a pod matching the given pod name")
    public void testRestartPods() {
        when(CommandUtils.runCommand(anyString()))
                .thenReturn(CommandResultUtils.getCommandResult("All pods are deleted"));
        when(CommandUtils.runCommand(anyString())).thenReturn(CommandResultUtils.getCommandResult(podNames));
        Assert.assertFalse(client.getPODClient().restartPods("mangle"));
    }

    @Test(description = "verify the retrieval of amount of the memory used by a given container in a given pod")
    public void testGetContainerMemoryUsed() {
        when(CommandUtils.runCommand(anyString())).thenReturn(CommandResultUtils.getCommandResult(podName));
        when(CommandUtils.runCommand(anyString())).thenReturn(CommandResultUtils.getCommandResult("546"));
        String memory = client.getPODClient().getContainerMemoryUsed("", "");
        Assert.assertEquals(memory, "546");

    }

    @Test(description = "verify the retrieval of amount of the memory used by a given container in a given pod")
    public void testGetContainerMemoryUsedFailure() {
        when(CommandUtils.runCommand(anyString())).thenReturn(CommandResultUtils.getCommandResult(null));
        String memory = client.getPODClient().getContainerMemoryUsed("", "");
        Assert.assertEquals(memory, "");

    }

    @Test(description = "verify the retrieval of amount of the memory used by a given container in a given pod")
    public void testGetContainerMemoryUsedFailure2() {
        when(CommandUtils.runCommand(anyString())).thenReturn(CommandResultUtils.getCommandResult("not found"));
        String memory = client.getPODClient().getContainerMemoryUsed("", "");
        Assert.assertEquals(memory, "");

    }

    @Test(description = "verify the retrieval of amount of the memory limit container to a given container in a given pod")
    public void testGetContainerMemoryLimit() {
        ;
        when(CommandUtils.runCommand(anyString())).thenReturn(CommandResultUtils.getCommandResult("546"));
        String memory = client.getPODClient().getContainerMemoryLimit("", "");
        Assert.assertEquals(memory, "546");

    }

    @Test(description = "verify the retrieval of amount of the memory limit container to a given container in a given pod")
    public void testGetContainerMemoryLimitFailure() {
        when(CommandUtils.runCommand(anyString())).thenReturn(CommandResultUtils.getCommandResult(null));
        String memory = client.getPODClient().getContainerMemoryLimit("", "");
        Assert.assertEquals(memory, "");

    }

    @Test(description = "verify the retrieval of amount of the memory limit container to a given container in a given pod")
    public void testGetContainerMemoryLimitFailure2() {
        when(CommandUtils.runCommand(anyString())).thenReturn(CommandResultUtils.getCommandResult("not found"));
        String memory = client.getPODClient().getContainerMemoryLimit("", "");
        Assert.assertEquals(memory, "");

    }
}
