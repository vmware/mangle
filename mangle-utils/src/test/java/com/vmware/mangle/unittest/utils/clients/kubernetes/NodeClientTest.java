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

import static org.mockito.Matchers.anyString;

import java.util.List;
import java.util.Map;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.utils.CommandUtils;
import com.vmware.mangle.utils.clients.kubernetes.KubernetesCommandLineClient;
import com.vmware.mangle.utils.clients.kubernetes.NodeClient;
import com.vmware.mangle.utils.mockdata.CommandResultUtils;

/**
 *
 *
 * @author chetanc
 */
@PrepareForTest(value = { CommandUtils.class })
public class NodeClientTest extends PowerMockTestCase {

    private String ips = "10.10.10.10 10.10.10.11 10.10.10.12";
    private String dummyCIDR = "172.10.10.2";
    private KubernetesCommandLineClient client;

    @BeforeMethod
    public void init() {
        client = KubernetesCommandLineClient.getClient();
    }

    /**
     * Test method for {@link NodeClient#getNodes()}
     */
    @Test
    public void testGetNodes() {
        PowerMockito.mockStatic(CommandUtils.class);
        PowerMockito.when(CommandUtils.runCommand(anyString())).thenReturn(CommandResultUtils.getCommandResult(ips));
        List<String> lists = client.getNodeClient().getNodes();
        Assert.assertEquals(lists.size(), 3);
    }

    /**
     * Test method for {@link NodeClient#getPodCIDR(String)}
     */
    @Test
    public void testGetPodCIDR() {
        PowerMockito.mockStatic(CommandUtils.class);
        PowerMockito.when(CommandUtils.runCommand(anyString()))
                .thenReturn(CommandResultUtils.getCommandResult(dummyCIDR));
        String cidr = client.getNodeClient().getPodCIDR("MangleWEB");
        Assert.assertEquals(cidr, dummyCIDR);
    }

    /**
     * Test method for {@link NodeClient#getPodCIDR(String)}
     */
    @Test
    public void testCheckAvailabilityNode() {
        PowerMockito.mockStatic(CommandUtils.class);
        CommandExecutionResult result = new CommandExecutionResult();
        result.setCommandOutput("10.10.10.10 10.10.10.11");
        result.setExitCode(0);
        PowerMockito.when(CommandUtils.runCommand(anyString())).thenReturn(result);
        List<String> nodes = client.getNodeClient().checkAvailabilityNode();
        Assert.assertEquals(nodes.size(), 2);
    }

    /**
     * Test method for {@link NodeClient#getNodeandExternalIPMap()}
     */
    @Test
    public void testGetNodeandExternalMap() {
        PowerMockito.mockStatic(CommandUtils.class);
        CommandExecutionResult result = new CommandExecutionResult();
        result.setCommandOutput("node1,10.10.10.11 node2,10.10.10.12");
        result.setExitCode(0);
        PowerMockito.when(CommandUtils.runCommand(anyString())).thenReturn(result);
        Map<String, String> nodes = client.getNodeClient().getNodeandExternalIPMap();
        Assert.assertEquals(nodes.size(), 2);
    }
}
