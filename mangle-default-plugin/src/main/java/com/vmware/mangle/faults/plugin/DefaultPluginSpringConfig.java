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

package com.vmware.mangle.faults.plugin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vmware.mangle.faults.plugin.helpers.JavaAgentFaultUtils;
import com.vmware.mangle.faults.plugin.helpers.aws.AwsEC2FaultHelper;
import com.vmware.mangle.faults.plugin.helpers.byteman.BytemanFaultHelperFactory;
import com.vmware.mangle.faults.plugin.helpers.byteman.DockerBytemanFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.byteman.K8sBytemanFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.byteman.LinuxBytemanFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.docker.DockerFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.k8s.K8sFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.systemresource.DockerSystemResourceFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.systemresource.K8sSystemResourceFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.systemresource.LinuxSystemResourceFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.systemresource.SystemResourceFaultHelperFactory;
import com.vmware.mangle.faults.plugin.helpers.systemresource.SystemResourceFaultUtils;
import com.vmware.mangle.faults.plugin.helpers.vcenter.VCenterFaultHelper;
import com.vmware.mangle.faults.plugin.tasks.helpers.BytemanFaultTaskHelper;
import com.vmware.mangle.faults.plugin.tasks.helpers.SystemResourceFaultTaskHelper;
import com.vmware.mangle.faults.plugin.utils.PluginUtils;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.helpers.CommandInfoExecutionHelper;

/**
 * DefaultPlugin spring Configuration class.
 *
 * @author kumargautam
 */
@Configuration
public class DefaultPluginSpringConfig {

    @Bean
    public EndpointClientFactory endpointClientFactory() {
        return new EndpointClientFactory();
    }

    @Bean
    public K8sFaultHelper k8sFaultHelper(EndpointClientFactory endpointClientFactory) {
        return new K8sFaultHelper(endpointClientFactory);
    }

    @Bean
    public LinuxBytemanFaultHelper linuxBytemanFaultHelper() {
        return new LinuxBytemanFaultHelper();
    }

    @Bean
    public K8sBytemanFaultHelper k8sBytemanFaultHelper(EndpointClientFactory endpointClientFactory,
            JavaAgentFaultUtils javaAgentFaultUtils, PluginUtils pluginUtils) {
        return new K8sBytemanFaultHelper(endpointClientFactory, javaAgentFaultUtils, pluginUtils);

    }

    @Bean
    public BytemanFaultHelperFactory bytemanFaultHelperFactory() {
        return new BytemanFaultHelperFactory();
    }

    @Bean
    public VCenterFaultHelper vCenterFaultHelper(EndpointClientFactory endpointClientFactory) {
        return new VCenterFaultHelper(endpointClientFactory);
    }

    @Bean
    public AwsEC2FaultHelper awsEC2FaultHelper(EndpointClientFactory endpointClientFactory) {
        return new AwsEC2FaultHelper(endpointClientFactory);
    }

    @Bean
    public JavaAgentFaultUtils javaAgentFaultUtils() {
        return new JavaAgentFaultUtils();
    }

    @Bean
    public CommandInfoExecutionHelper commandInfoExecutionHelper() {
        return new CommandInfoExecutionHelper();
    }

    @Bean
    public BytemanFaultTaskHelper bytemanFaultTask() {
        return new BytemanFaultTaskHelper();
    }

    @Bean
    public SystemResourceFaultTaskHelper systemResourceFaultTask() {
        return new SystemResourceFaultTaskHelper();
    }

    @Bean
    public DockerFaultHelper dockerFaultHelper(EndpointClientFactory endpointClientFactory) {
        return new DockerFaultHelper(endpointClientFactory);
    }


    @Bean
    public DockerBytemanFaultHelper dockerBytemanFaultHelper() {
        return new DockerBytemanFaultHelper();
    }

    @Bean
    public SystemResourceFaultUtils systemResourceFaultUtils() {
        return new SystemResourceFaultUtils();
    }

    @Bean
    public LinuxSystemResourceFaultHelper linuxSystemResourceFaultHelper(EndpointClientFactory endpointClientFactory,
            SystemResourceFaultUtils systemResourceFaultUtils) {
        return new LinuxSystemResourceFaultHelper(endpointClientFactory, systemResourceFaultUtils);
    }

    @Bean
    public DockerSystemResourceFaultHelper dockerSystemResourceFaultHelper(EndpointClientFactory endpointClientFactory,
            SystemResourceFaultUtils systemResourceFaultUtils) {
        return new DockerSystemResourceFaultHelper(endpointClientFactory, systemResourceFaultUtils);
    }

    @Bean
    public SystemResourceFaultHelperFactory systemResourceFaultHelperFactory() {
        return new SystemResourceFaultHelperFactory();

    }

    @Bean
    public PluginUtils pluginUtils() {
        return new PluginUtils();
    }

    @Bean
    public K8sSystemResourceFaultHelper k8sSystemResourceFaultHelper(EndpointClientFactory endpointClientFactory,
            SystemResourceFaultUtils systemResourceFaultUtils) {
        return new K8sSystemResourceFaultHelper(endpointClientFactory, systemResourceFaultUtils);
    }
}
