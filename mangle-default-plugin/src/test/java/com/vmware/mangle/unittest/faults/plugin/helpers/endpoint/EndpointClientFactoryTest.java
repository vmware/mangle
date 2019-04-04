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

import static org.mockito.Mockito.validateMockitoUsage;

import java.util.Optional;

import org.mockito.MockitoAnnotations;
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
import com.vmware.mangle.utils.clients.docker.CustomDockerClient;
import com.vmware.mangle.utils.clients.endpoint.EndpointClient;
import com.vmware.mangle.utils.clients.kubernetes.KubernetesCommandLineClient;
import com.vmware.mangle.utils.clients.ssh.SSHUtils;
import com.vmware.mangle.utils.exceptions.MangleException;

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
    public void tearDownAfterClass() throws Exception {
        this.credentialsSpec = null;
        this.endpointSpec = null;
        this.mockData = null;
        this.credentialsSpecMockData = null;
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
        validateMockitoUsage();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.EndpointClientFactory.endpoint.EndpointClientFactory#getEndPointClient(CredentialsSpec, EndpointSpec)}
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
     * {@link com.vmware.mangle.EndpointClientFactory.endpoint.EndpointClientFactory#getEndPointClient(CredentialsSpec, EndpointSpec)}
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
     * {@link com.vmware.mangle.EndpointClientFactory.endpoint.EndpointClientFactory#getEndPointClient(CredentialsSpec, EndpointSpec)}
     *
     * @throws MangleException
     */
    @Test
    public void testGetEndPointClientEndpointSpecForK8S() throws MangleException {
        CredentialsSpec credentialsSpec = credentialsSpecMockData.getk8SCredentialsData();
        EndpointSpec endpointSpec = mockData.k8sEndpointMockData();
        Optional<CredentialsSpec> optional = Optional.of(credentialsSpec);
        EndpointClient endpointClient = endpointClientFactory.getEndPointClient(credentialsSpec, endpointSpec);
        boolean expectedResult = false;
        if (endpointClient instanceof KubernetesCommandLineClient) {
            expectedResult = true;
        }
        Assert.assertTrue(expectedResult);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.EndpointClientFactory.endpoint.EndpointClientFactory#getEndPointClient(CredentialsSpec, EndpointSpec)}
     *
     * @throws MangleException
     */
    @Test
    public void testGetEndPointClientEndpointSpecForK8S1() throws MangleException {
        CredentialsSpec credentialsSpec = credentialsSpecMockData.getk8SCredentialsData();
        ((K8SCredentials) credentialsSpec).setKubeConfig(new byte[] {});
        EndpointSpec endpointSpec = mockData.k8sEndpointMockData();
        Optional<CredentialsSpec> optional = Optional.of(credentialsSpec);
        EndpointClient endpointClient = endpointClientFactory.getEndPointClient(credentialsSpec, endpointSpec);
        boolean expectedResult = false;
        if (endpointClient instanceof KubernetesCommandLineClient) {
            expectedResult = true;
        }
        Assert.assertTrue(expectedResult);
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
}
