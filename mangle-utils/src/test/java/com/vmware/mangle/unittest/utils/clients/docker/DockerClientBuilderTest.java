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

package com.vmware.mangle.unittest.utils.clients.docker;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig.Builder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.utils.clients.docker.DockerClientBuilder;
import com.vmware.mangle.utils.clients.docker.JerseyDockerCmdExecFactory;

/**
 * Unit Test Case for DockerClientBuilder.
 *
 * @author kumargautam
 */

@PrepareForTest(value = { DockerClientImpl.class })
public class DockerClientBuilderTest extends PowerMockTestCase {

    @Mock
    private DockerCmdExecFactory dockerCmdExecFactory;
    @Mock
    private DockerClientImpl dockerClient;
    private DockerClientBuilder dockerClientBuilder;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mock(DockerClientImpl.class);
        this.dockerClientBuilder = new DockerClientBuilder(dockerClient);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public void tearDownAfterClass() throws Exception {
        this.dockerClientBuilder = null;
        this.dockerClient = null;
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
     * {@link com.vmware.mangle.clients.docker.DockerClientBuilder#withDockerCmdExecFactory(com.github.dockerjava.api.command.DockerCmdExecFactory)}.
     */
    @Test(priority = 1)
    public void testWithDockerCmdExecFactory() {
        this.dockerClientBuilder = new DockerClientBuilder(dockerClient);
        Assert.assertNotNull(dockerClientBuilder.withDockerCmdExecFactory(dockerCmdExecFactory));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.DockerClientBuilder#withDockerCmdExecFactory(com.github.dockerjava.api.command.DockerCmdExecFactory)}.
     */
    @Test(priority = 1, description = "verify the creation of docker client")
    public void testWithDockerCmdExecFactory2() {
        this.dockerClientBuilder = new DockerClientBuilder(dockerClient);
        this.dockerClientBuilder.withDockerCmdExecFactory(dockerCmdExecFactory);
        DockerClient dockerClient = this.dockerClientBuilder.build();
        Assert.assertNotNull(dockerClient);
    }

    /**
     * Test method for {@link com.vmware.mangle.clients.docker.DockerClientBuilder#build()}.
     */
    @Test(priority = 2, dependsOnMethods = "testWithDockerCmdExecFactory", enabled = false)
    public void testBuild() {
        this.dockerClientBuilder = new DockerClientBuilder(dockerClient);
        when(dockerClient.withDockerCmdExecFactory(any(DockerCmdExecFactory.class))).thenReturn(dockerClient);
        Assert.assertNotNull(dockerClientBuilder.build());
        verify(dockerClient, times(1)).withDockerCmdExecFactory(any(DockerCmdExecFactory.class));
    }

    /**
     * Test method for {@link com.vmware.mangle.clients.docker.DockerClientBuilder#build()}.
     */
    @Test(priority = 3, enabled = false)
    public void testBuild1() {
        this.dockerClientBuilder = new DockerClientBuilder(dockerClient);
        dockerClientBuilder.withDockerCmdExecFactory(null);
        when(dockerClient.withDockerCmdExecFactory(any(JerseyDockerCmdExecFactory.class))).thenReturn(dockerClient);
        Assert.assertNotNull(dockerClientBuilder.build());
        verify(dockerClient, times(1)).withDockerCmdExecFactory(any(JerseyDockerCmdExecFactory.class));
    }

    /**
     * Test method for {@link com.vmware.mangle.clients.docker.DockerClientBuilder#getInstance()}.
     */
    @Test(priority = 4)
    public void testGetInstance() {
        PowerMockito.mockStatic(DockerClientImpl.class);
        PowerMockito.when(DockerClientImpl.getInstance()).thenReturn(dockerClient);
        Assert.assertNotNull(DockerClientBuilder.getInstance());
        PowerMockito.verifyStatic(DockerClientImpl.class, times(1));
        DockerClientImpl.getInstance();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.DockerClientBuilder#getInstance(com.github.dockerjava.core.DefaultDockerClientConfig.Builder)}.
     */
    @Test(priority = 5)
    public void testGetInstanceBuilder() {
        Builder builder = mock(Builder.class);
        DockerClientConfig dockerClientConfig = mock(DefaultDockerClientConfig.class);
        when(builder.build()).thenReturn((DefaultDockerClientConfig) dockerClientConfig);
        PowerMockito.mockStatic(DockerClientImpl.class);
        PowerMockito.when(DockerClientImpl.getInstance(any(DockerClientConfig.class))).thenReturn(dockerClient);
        Assert.assertNotNull(DockerClientBuilder.getInstance(builder));
        PowerMockito.verifyStatic(DockerClientImpl.class, times(1));
        DockerClientImpl.getInstance(any(DockerClientConfig.class));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.DockerClientBuilder#getInstance(com.github.dockerjava.core.DockerClientConfig)}.
     */
    @Test(priority = 6)
    public void testGetInstanceDockerClientConfig() {
        DockerClientConfig dockerClientConfig = mock(DefaultDockerClientConfig.class);
        PowerMockito.mockStatic(DockerClientImpl.class);
        PowerMockito.when(DockerClientImpl.getInstance(any(DockerClientConfig.class))).thenReturn(dockerClient);
        Assert.assertNotNull(DockerClientBuilder.getInstance(dockerClientConfig));
        PowerMockito.verifyStatic(DockerClientImpl.class, times(1));
        DockerClientImpl.getInstance(any(DockerClientConfig.class));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.DockerClientBuilder#getInstance(java.lang.String)}.
     */
    @Test(priority = 7)
    public void testGetInstanceString() {
        PowerMockito.mockStatic(DockerClientImpl.class);
        PowerMockito.when(DockerClientImpl.getInstance(anyString())).thenReturn(dockerClient);
        Assert.assertNotNull(DockerClientBuilder.getInstance("10.2.3.45"));
        PowerMockito.verifyStatic(DockerClientImpl.class, times(1));
        DockerClientImpl.getInstance(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.clients.docker.DockerClientBuilder#getDefaultDockerCmdExecFactory()}.
     */
    @Test(priority = 8, enabled = false)
    public void testGetDefaultDockerCmdExecFactory() {
        Assert.assertNotNull(DockerClientBuilder.getDefaultDockerCmdExecFactory());
    }

}
