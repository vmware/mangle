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

package com.vmware.mangle.unittest.faults.plugin.helpers.endpoint;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.testng.Assert.assertEquals;

import java.util.Optional;

import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.CredentialsSpec;
import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.endpoint.K8SCredentials;
import com.vmware.mangle.cassandra.model.endpoint.RemoteMachineCredentials;
import com.vmware.mangle.faults.plugin.mockdata.CredentialsSpecMockData;
import com.vmware.mangle.faults.plugin.mockdata.EndpointMockData;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.clients.azure.CustomAzureClient;
import com.vmware.mangle.utils.clients.database.DatabaseClient;
import com.vmware.mangle.utils.clients.docker.CustomDockerClient;
import com.vmware.mangle.utils.clients.endpoint.EndpointClient;
import com.vmware.mangle.utils.clients.kubernetes.KubernetesCommandLineClient;
import com.vmware.mangle.utils.clients.redis.RedisProxyClient;
import com.vmware.mangle.utils.clients.ssh.SSHUtils;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Insert your comment for EndpointClientFactoryTest here
 *
 * @author kumargautam
 */
@PowerMockIgnore(value = { "javax.net.ssl.*", "org.apache.logging.log4j.*" })
public class EndpointClientFactoryTest extends PowerMockTestCase {

    private EndpointMockData mockData = new EndpointMockData();
    private EndpointSpec endpointSpec;


    private CredentialsSpecMockData credentialsSpecMockData = new CredentialsSpecMockData();
    private CredentialsSpec credentialsSpec;
    private EndpointClientFactory endpointClientFactory;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        endpointClientFactory = new EndpointClientFactory();
        this.credentialsSpec = credentialsSpecMockData.getRMCredentialsData();
        this.endpointSpec = mockData.rmEndpointMockData();

    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public void tearDownAfterClass() {
        this.credentialsSpec = null;
        this.endpointSpec = null;
        this.mockData = null;
        this.credentialsSpecMockData = null;
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() {
        validateMockitoUsage();
    }

    /**
     * Test method for
     * {@link EndpointClientFactory#getEndPointClient(CredentialsSpec, EndpointSpec)}
     *
     * @throws MangleException
     */
    @Test
    public void testGetEndPointClient() {
        EndpointSpec rmEndpointSpec = mockData.rmEndpointMockData();
        rmEndpointSpec.setEnable(false);
        try {
            endpointClientFactory.getEndPointClient(credentialsSpec, rmEndpointSpec);
            Assert.fail("test getEndpointClient() with enable false failed");
        } catch (MangleException e) {
            assertEquals(e.getErrorCode(), ErrorCode.ENDPOINT_DISABLED,
                    "test getEndpointClient() failed with different error code" + e.getErrorCode());
        }
        rmEndpointSpec.setEnable(null);
        try {
            EndpointClient endpointClient = endpointClientFactory.getEndPointClient(credentialsSpec, rmEndpointSpec);
            Assert.assertTrue(endpointClient instanceof SSHUtils, "expecting sshutils client failed");
        } catch (MangleException e) {
            Assert.fail("test getEndpointClient() with enable null failed");
        }

    }

    /**
     * Test method for
     * {@link EndpointClientFactory#getEndPointClient(CredentialsSpec, EndpointSpec)}
     *
     * @throws MangleException
     */
    @Test
    public void testGetEndPointClientForMachine() throws MangleException {
        EndpointClient endpointClient = endpointClientFactory.getEndPointClient(credentialsSpec, endpointSpec);
        boolean expectedResult = false;
        if (endpointClient instanceof SSHUtils) {
            expectedResult = true;
        }
        Assert.assertTrue(expectedResult);
    }

    /**
     * Test method for
     * {@link EndpointClientFactory#getEndPointClient(CredentialsSpec, EndpointSpec)}
     *
     * @throws MangleException
     */
    @Test
    public void testGetEndPointClientForMachine2() throws MangleException {
        ((RemoteMachineCredentials) credentialsSpec).setPrivateKey(new byte[] {});
        EndpointClient endpointClient = endpointClientFactory.getEndPointClient(credentialsSpec, endpointSpec);

        boolean expectedResult = false;
        if (endpointClient instanceof SSHUtils) {
            expectedResult = true;
        }
        Assert.assertTrue(expectedResult);
    }

    /**
     * Test method for
     * {@link EndpointClientFactory#getEndPointClient(CredentialsSpec, EndpointSpec)}
     *
     * @throws Exception
     */
    @Test
    public void testGetEndPointClientEndpointSpecForK8S() throws Exception {
        CredentialsSpec credentialsSpec = credentialsSpecMockData.getk8SCredentialsData();
        EndpointSpec endpointSpec = mockData.k8sEndpointMockData();
        EndpointClientFactory endpointClientFactorySpy = PowerMockito.spy(endpointClientFactory);
        PowerMockito.doNothing().when(endpointClientFactorySpy).testConnection(any());

        EndpointClient endpointClient = endpointClientFactorySpy.getEndPointClient(credentialsSpec, endpointSpec);
        boolean expectedResult = false;
        if (endpointClient instanceof KubernetesCommandLineClient) {
            expectedResult = true;
        }
        Assert.assertTrue(expectedResult);
    }

    /**
     * Test method for
     * {@link EndpointClientFactory#getEndPointClient(CredentialsSpec, EndpointSpec)}
     *
     * @throws MangleException
     */
    @Test
    public void testGetEndPointClientEndpointSpecForK8S1() throws MangleException {
        K8SCredentials credentialsSpec = credentialsSpecMockData.getk8SCredentialsData();
        credentialsSpec.setKubeConfig(new byte[] {});
        EndpointSpec endpointSpec = mockData.k8sEndpointMockData();
        EndpointClientFactory endpointClientFactorySpy = PowerMockito.spy(endpointClientFactory);
        PowerMockito.doNothing().when(endpointClientFactorySpy).testConnection(any());
        Optional<CredentialsSpec> optional = Optional.of(credentialsSpec);
        EndpointClient endpointClient = endpointClientFactorySpy.getEndPointClient(credentialsSpec, endpointSpec);
        boolean expectedResult = false;
        if (endpointClient instanceof KubernetesCommandLineClient) {
            expectedResult = true;
        }
        Assert.assertTrue(expectedResult);
    }

    /**
     * Test method for
     * {@link EndpointClientFactory#getEndPointClient(CredentialsSpec, EndpointSpec)}
     *
     * @throws MangleException
     */
    @Test
    public void testGetEndPointClientEndpointSpecForAzure() throws MangleException {
        EndpointSpec endpointSpec = mockData.azureEndpointMockData();
        EndpointClient endpointClient = endpointClientFactory
                .getEndPointClient(credentialsSpecMockData.getAzureCredentialsData(), endpointSpec);
        Assert.assertTrue(endpointClient instanceof CustomAzureClient, "Test for azure endpoint spec failed");
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.EndpointClientFactory.endpoint.EndpointClientFactory#getEndPointClient(CredentialsSpec, EndpointSpec)}
     *
     * @throws MangleException
     */
    @Test
    public void testGetEndPointClientEndpointSpecForDocker() throws MangleException {
        EndpointSpec endpointSpec = mockData.dockerEndpointMockData();
        EndpointClient endpointClient = endpointClientFactory.getEndPointClient(null, endpointSpec);
        boolean expectedResult = false;
        if (endpointClient instanceof CustomDockerClient) {
            expectedResult = true;
        }
        Assert.assertTrue(expectedResult);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.task.framework.endpoint.EndpointClientFactory#getEndPointClient(CredentialsSpec, EndpointSpec)}
     *
     * @throws MangleException
     */
    @Test
    public void testGetEndPointClientEndpointSpecForRedis() throws MangleException {
        EndpointSpec endpointSpec = mockData.getRedisProxyEndpointMockData();
        EndpointClient endpointClient = endpointClientFactory.getEndPointClient(null, endpointSpec);

        Assert.assertTrue(endpointClient instanceof RedisProxyClient);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.task.framework.endpoint.EndpointClientFactory#getEndPointClient(CredentialsSpec, EndpointSpec)}
     *
     * @throws MangleException
     */
    @Test
    public void testGetEndPointClientEndpointSpecForDatabase() throws MangleException {
        EndpointSpec endpointSpec = mockData.getDatabaseEndpointSpec();
        EndpointClient endpointClient = endpointClientFactory.getEndPointClient(null, endpointSpec);

        Assert.assertTrue(endpointClient instanceof DatabaseClient);
    }
}
