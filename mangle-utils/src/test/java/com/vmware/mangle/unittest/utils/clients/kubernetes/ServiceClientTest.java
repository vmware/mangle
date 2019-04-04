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

import org.junit.Assert;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.tasks.commands.CommandExecutionResult;
import com.vmware.mangle.utils.CommandUtils;
import com.vmware.mangle.utils.clients.kubernetes.KubernetesCommandLineClient;
import com.vmware.mangle.utils.mockdata.CommandResultUtils;

/**
 *
 *
 * @author chetanc
 */
@PrepareForTest(value = { KubernetesCommandLineClient.class, CommandUtils.class })
public class ServiceClientTest extends PowerMockTestCase {

    private String namespace = "kubeconfig-namespace";
    private String kubeconfig = "src/test/resources/MockFile.properties";
    private String kubeCommand = "kubectl get pod";

    /**
     * Test method for {@link KubernetesCommandLineClient#getClient()}
     */
    @Test(description = "verify the creation of the kubenetesCommandClient")
    public void testGetClient() {
        KubernetesCommandLineClient client = KubernetesCommandLineClient.getClient();
        Assert.assertNotNull(client);
    }

    /**
     * Test method for {@link KubernetesCommandLineClient#setNameSpace(String)}
     */
    @Test(description = "verify the creation of the KubernetesCommandLineClient, and setter/getter methods of the property namespace")
    public void testSetNameSpace() {
        KubernetesCommandLineClient client = KubernetesCommandLineClient.getClient();
        KubernetesCommandLineClient client1 = client.setNameSpace(namespace);
        Assert.assertEquals(client1.getNameSpace(), namespace);
    }

    /**
     * Test method for {@link KubernetesCommandLineClient#setNameSpace(String)}
     */
    @Test(description = "verify the creation of the KubernetesCommandLineClient, and setter/getter methods of the property namespace, getter should return empty string even when the value of the namespace is null")
    public void testSetNameSpace1() {
        KubernetesCommandLineClient client = KubernetesCommandLineClient.getClient();
        KubernetesCommandLineClient client1 = client.setNameSpace(null);
        Assert.assertNotNull(client1.getNameSpace());
    }

    /**
     * Test method for {@link KubernetesCommandLineClient#setKubeconfig(String)}
     */
    @Test(description = "verify the creation of the KubernetesCommandLineClient, and setter methods of the property Kubeconfig")
    public void testSetKubeconfig() {
        KubernetesCommandLineClient client = KubernetesCommandLineClient.getClient();
        KubernetesCommandLineClient client1 = client.setKubeconfig(kubeconfig);
        Assert.assertNotNull(client1);
    }

    /**
     * Test method for {@link KubernetesCommandLineClient#setKubeconfig(String)}
     */
    @Test(description = "verify the creation of the KubernetesCommandLineClient, and setter methods of the property Kubeconfig")
    public void testSetKubeconfig1() {
        KubernetesCommandLineClient client = KubernetesCommandLineClient.getClient();
        KubernetesCommandLineClient client1 = client.setKubeconfig(null);
        Assert.assertNotNull(client1);
    }

    /**
     * Test method for {@link KubernetesCommandLineClient#getClient(String)}
     */
    @Test(description = "verify that the kubernetescommandlineclient initialization initializes NodeClient, PodClient, ServiceClient as well")
    public void testGetClientParameterizedConstructor() {
        KubernetesCommandLineClient client = KubernetesCommandLineClient.getClient(kubeconfig);
        Assert.assertNotNull(client);
        Assert.assertNotNull(client.getNodeClient());
        Assert.assertNotNull(client.getPODClient());
        Assert.assertNotNull(client.getServiceClient());
    }

    /**
     * Test method for {@link KubernetesCommandLineClient#executeCommand(String)}
     */
    @Test(description = "verify the execution of a command on the kubernetes cluster node")
    public void testExecuteCommand() {
        PowerMockito.mockStatic(CommandUtils.class);
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        commandExecutionResult.setCommandOutput("Successful");
        commandExecutionResult.setExitCode(0);
        PowerMockito.when(CommandUtils.runCommand(anyString())).thenReturn(commandExecutionResult);
        KubernetesCommandLineClient client = KubernetesCommandLineClient.getClient(kubeconfig);
        CommandExecutionResult result = client.executeCommand(kubeCommand);
        Assert.assertEquals(0, result.getExitCode());
        Assert.assertEquals("Successful", result.getCommandOutput());

    }

    /**
     * Test method for {@link KubernetesCommandLineClient#testConnection()}
     */
    @Test(description = "verify the test connection to the kubernetes cluster")
    public void testTestConnection() {
        PowerMockito.mockStatic(CommandUtils.class);
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        commandExecutionResult.setCommandOutput("Client Successfully running");
        commandExecutionResult.setExitCode(0);
        PowerMockito.when(CommandUtils.runCommand(anyString())).thenReturn(commandExecutionResult);
        KubernetesCommandLineClient client = KubernetesCommandLineClient.getClient(kubeconfig);
        boolean result = client.testConnection();
        Assert.assertTrue(result);
    }

    /**
     * Test method for {@link KubernetesCommandLineClient#testConnection()}
     */
    @Test(description = "verify the test connection failure to the kubernetes cluster")
    public void testTestConnectionFailure() {
        PowerMockito.mockStatic(CommandUtils.class);
        CommandExecutionResult commandExecutionResult = new CommandExecutionResult();
        commandExecutionResult.setCommandOutput("Client Successfully");
        commandExecutionResult.setExitCode(0);
        PowerMockito.when(CommandUtils.runCommand(anyString())).thenReturn(commandExecutionResult);
        KubernetesCommandLineClient client = KubernetesCommandLineClient.getClient(kubeconfig);
        boolean result = client.testConnection();
        Assert.assertFalse(result);
    }

    /**
     * Test method for {@link com.vmware.mangle.clients.kubernetes.ServiceClient#getServices()}
     */
    @Test(description = "verify the retrieval of services from kubernetes service client")
    public void testGetServices() {
        PowerMockito.mockStatic(CommandUtils.class);
        PowerMockito.when(CommandUtils.runCommand(anyString()))
                .thenReturn(CommandResultUtils.getCommandResult("MangleWEB MangleDB"));
        KubernetesCommandLineClient client = KubernetesCommandLineClient.getClient(kubeconfig);
        List<String> lists = client.getServiceClient().getServices();
        Assert.assertEquals(2, lists.size());
    }

    /**
     * Test method for {@link com.vmware.mangle.clients.kubernetes.ServiceClient#getServices()}
     */
    @Test(description = "verify the retrieval of app names from kubernetes service client")
    public void testGetAppNames() {
        PowerMockito.mockStatic(CommandUtils.class);
        CommandExecutionResult result = new CommandExecutionResult();
        result.setCommandOutput("MangleWEB MangleDB");
        PowerMockito.when(CommandUtils.runCommand(anyString())).thenReturn(result);
        KubernetesCommandLineClient client = KubernetesCommandLineClient.getClient(kubeconfig);
        List<String> lists = client.getServiceClient().getAppNames();
        Assert.assertEquals(2, lists.size());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.kubernetes.ServiceClient#getServiceLoadBalancerHostName(String)}
     */
    @Test(description = "verify the retrieval of the load balancer host name for the given service/container using kubernetes service client")
    public void testGetServiceLoadBalancerHostNameFailure() {
        PowerMockito.mockStatic(CommandUtils.class);
        PowerMockito.when(CommandUtils.runCommand(anyString())).thenReturn(CommandResultUtils.getCommandResult(""));
        KubernetesCommandLineClient client = KubernetesCommandLineClient.getClient(kubeconfig);
        String result = client.getServiceClient().getServiceLoadBalancerHostName("MangleWEB");
        Assert.assertNull(result);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.kubernetes.ServiceClient#getServiceLoadBalancerHostName(String)}
     */
    @Test(description = "verify the retrieval of the load balancer host name for the given service/container using kubernetes service client")
    public void testGetServiceLoadBalancerHostName() {
        PowerMockito.mockStatic(CommandUtils.class);
        String replyResult = "Mangle Loadbalancer";
        PowerMockito.when(CommandUtils.runCommand(anyString()))
                .thenReturn(CommandResultUtils.getCommandResult(replyResult));
        KubernetesCommandLineClient client = KubernetesCommandLineClient.getClient(kubeconfig);
        String result = client.getServiceClient().getServiceLoadBalancerHostName("MangleWEB");
        Assert.assertEquals(result, replyResult);
    }


}
