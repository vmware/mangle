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

package com.vmware.mangle.services.mockdata;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

import com.vmware.mangle.cassandra.model.endpoint.AWSConnectionProperties;
import com.vmware.mangle.cassandra.model.endpoint.AzureConnectionProperties;
import com.vmware.mangle.cassandra.model.endpoint.DatabaseConnectionProperties;
import com.vmware.mangle.cassandra.model.endpoint.DockerConnectionProperties;
import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.endpoint.K8SConnectionProperties;
import com.vmware.mangle.cassandra.model.endpoint.RedisProxyConnectionProperties;
import com.vmware.mangle.cassandra.model.endpoint.RemoteMachineConnectionProperties;
import com.vmware.mangle.cassandra.model.endpoint.VCenterConnectionProperties;
import com.vmware.mangle.cassandra.model.metricprovider.WaveFrontConnectionProperties;
import com.vmware.mangle.model.enums.DatabaseType;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.model.enums.OSType;
import com.vmware.mangle.utils.ReadProperty;
import com.vmware.mangle.utils.constants.Constants;

/**
 * Endpoint Mock Data.
 *
 * @author kumargautam, dbhat
 */
public class EndpointMockData {

    private Properties properties;
    // AWS Connection Properties
    private String awsRegion;

    // Docker Connection Properties
    private String dockerHostname;
    private Integer dockerPort;
    private boolean tlsEnabled;

    // RemoteMachine Connection Properties
    private String host;
    private int sshPort;
    private int timeout;

    // K8s Connection Properties
    private String namespace;

    //vCenter Connection Properties
    private String vcenterIp;

    //Azure Mock Data
    private String azureSubscriptionId;
    private String azureTenant;

    public EndpointMockData() {
        this.properties = ReadProperty.readProperty(Constants.MOCKDATA_FILE);
        this.awsRegion = properties.getProperty("awsRegion");
        this.dockerHostname = properties.getProperty("dockerHostName");
        this.dockerPort = Integer.parseInt(properties.getProperty("dockerPort"));
        this.tlsEnabled = Boolean.parseBoolean(properties.getProperty("dockertlsEnabled"));

        this.host = properties.getProperty("rmHost");
        this.sshPort = Integer.parseInt(properties.getProperty("rmPort"));
        this.timeout = Integer.parseInt(properties.getProperty("rmTimeout"));

        this.namespace = properties.getProperty("k8sNamespace");

        vcenterIp = properties.getProperty("vcenter.ip");

        this.azureSubscriptionId = properties.getProperty("azureSubscriptionId");
        this.azureTenant = properties.getProperty("azureTenant");
    }

    public EndpointSpec k8sEndpointMockData() {
        EndpointSpec k8SEndpointSpec = new EndpointSpec();
        Properties properties = ReadProperty.readProperty(Constants.MOCKDATA_FILE);
        k8SEndpointSpec.setName(properties.getProperty("k8sEndpointName"));
        k8SEndpointSpec.setEndPointType(EndpointType.K8S_CLUSTER);
        k8SEndpointSpec.setCredentialsName(properties.getProperty("k8sName"));
        k8SEndpointSpec.setK8sConnectionProperties(getK8sConnectionProperties());
        return k8SEndpointSpec;
    }

    public EndpointSpec dockerEndpointMockData() {
        EndpointSpec dockerEndpointSpec = new EndpointSpec();
        Properties properties = ReadProperty.readProperty(Constants.MOCKDATA_FILE);
        dockerEndpointSpec.setName(properties.getProperty("dockerEndpointName"));
        dockerEndpointSpec.setEndPointType(EndpointType.DOCKER);
        dockerEndpointSpec.setDockerConnectionProperties(getDockerConnectionProperties());
        return dockerEndpointSpec;
    }

    public EndpointSpec getRedisProxyEndpointMockData() {
        EndpointSpec dockerEndpointSpec = new EndpointSpec();
        dockerEndpointSpec.setName("redisProxyTest");
        dockerEndpointSpec.setEndPointType(EndpointType.REDIS_FI_PROXY);
        dockerEndpointSpec.setRedisProxyConnectionProperties(getRedisProxyConnectionProperties());
        return dockerEndpointSpec;
    }

    public EndpointSpec rmEndpointMockData() {
        EndpointSpec rmEndpointSpec = new EndpointSpec();
        Properties properties = ReadProperty.readProperty(Constants.MOCKDATA_FILE);
        rmEndpointSpec.setName(properties.getProperty("rmEndpointName"));
        rmEndpointSpec.setEndPointType(EndpointType.MACHINE);
        rmEndpointSpec.setCredentialsName(properties.getProperty("rmName"));
        rmEndpointSpec.setRemoteMachineConnectionProperties(getRemoteMachineConnectionProperties());
        return rmEndpointSpec;
    }

    public EndpointSpec rmEndpointGroupMockData() {
        EndpointSpec rmGroupEndpointSpec = new EndpointSpec();
        Properties properties = ReadProperty.readProperty(Constants.MOCKDATA_FILE);
        rmGroupEndpointSpec.setName(properties.getProperty("rmGroupEndpointName"));
        rmGroupEndpointSpec.setEndPointType(EndpointType.ENDPOINT_GROUP);
        rmGroupEndpointSpec.setEndpointNames(Arrays.asList(properties.getProperty("rmEndpointName")));
        return rmGroupEndpointSpec;
    }

    public EndpointSpec rmEndpointMockData(String rmEndpointName, EndpointType endpointType, String rmName,
            RemoteMachineConnectionProperties remoteMachineConnectionProperties) {
        EndpointSpec rmEndpointSpec = new EndpointSpec();
        rmEndpointSpec.setName(rmEndpointName);
        rmEndpointSpec.setEndPointType(endpointType);
        rmEndpointSpec.setCredentialsName(rmName);
        rmEndpointSpec.setRemoteMachineConnectionProperties(remoteMachineConnectionProperties);
        return rmEndpointSpec;
    }


    public K8SConnectionProperties getK8sConnectionProperties() {
        K8SConnectionProperties k8sConnectionProperties = new K8SConnectionProperties();
        k8sConnectionProperties.setNamespace(namespace);
        return k8sConnectionProperties;
    }

    public DockerConnectionProperties getDockerConnectionProperties() {
        DockerConnectionProperties dockerConnectionProperties = new DockerConnectionProperties();
        dockerConnectionProperties.setDockerHostname(dockerHostname);
        dockerConnectionProperties.setDockerPort(dockerPort);
        dockerConnectionProperties.setTlsEnabled(tlsEnabled);
        return dockerConnectionProperties;
    }

    public RedisProxyConnectionProperties getRedisProxyConnectionProperties() {
        RedisProxyConnectionProperties connectionProperties = new RedisProxyConnectionProperties();
        connectionProperties.setHost(host);
        connectionProperties.setPort(sshPort);
        return connectionProperties;
    }

    public RemoteMachineConnectionProperties getRemoteMachineConnectionProperties() {
        RemoteMachineConnectionProperties rmConnectionProperties = new RemoteMachineConnectionProperties();
        rmConnectionProperties.setHost(host);
        rmConnectionProperties.setOsType(OSType.LINUX);
        rmConnectionProperties.setSshPort(sshPort);
        rmConnectionProperties.setTimeout(timeout);
        return rmConnectionProperties;
    }

    public EndpointSpec getVCenterEndpointSpecMock() {
        EndpointSpec vcenterEndpointSpec = new EndpointSpec();
        vcenterEndpointSpec.setName("vCenterMock");
        vcenterEndpointSpec.setEndPointType(EndpointType.VCENTER);
        vcenterEndpointSpec.setCredentialsName("vCenterMockCred");
        vcenterEndpointSpec.setVCenterConnectionProperties(getVCenterConnectionProperties());
        return vcenterEndpointSpec;
    }

    public EndpointSpec awsEndpointSpecMock() {
        EndpointSpec awsEndpointSpec = new EndpointSpec();
        awsEndpointSpec.setName("awsMockEndpoint");
        awsEndpointSpec.setEndPointType(EndpointType.AWS);
        awsEndpointSpec.setCredentialsName("awsCreds");
        awsEndpointSpec.setAwsConnectionProperties(getAwsConnectionPropertiess());
        return awsEndpointSpec;
    }

    public EndpointSpec azureEndpointMockData() {
        EndpointSpec azureEndpointSpec = new EndpointSpec();
        Properties properties = ReadProperty.readProperty(Constants.MOCKDATA_FILE);
        azureEndpointSpec.setName(properties.getProperty("azureEndpointName"));
        azureEndpointSpec.setEndPointType(EndpointType.AZURE);
        azureEndpointSpec.setAzureConnectionProperties(getAzureConnectionProperties());
        return azureEndpointSpec;
    }

    public AzureConnectionProperties getAzureConnectionProperties() {
        AzureConnectionProperties azureConnectionProperties = new AzureConnectionProperties();
        azureConnectionProperties.setSubscriptionId(azureSubscriptionId);
        azureConnectionProperties.setTenant(azureTenant);
        return azureConnectionProperties;
    }

    public EndpointSpec getDockerEndpointSpecMock() {
        EndpointSpec dockerEndpointSpec = new EndpointSpec();
        dockerEndpointSpec.setName("dockerMockEndpoint");
        dockerEndpointSpec.setEndPointType(EndpointType.DOCKER);
        dockerEndpointSpec.setDockerConnectionProperties(getDockerConnectionProperties());
        return dockerEndpointSpec;
    }

    public VCenterConnectionProperties getVCenterConnectionProperties() {
        VCenterConnectionProperties vCenterConnectionProperties = new VCenterConnectionProperties();
        vCenterConnectionProperties.setHostname(vcenterIp);
        return vCenterConnectionProperties;
    }

    public AWSConnectionProperties getAwsConnectionPropertiess() {
        AWSConnectionProperties awsConnectionProperties = new AWSConnectionProperties();
        awsConnectionProperties.setRegion(awsRegion);
        return awsConnectionProperties;
    }

    public WaveFrontConnectionProperties getWaveFrontConnectionPropertiesMock() {
        WaveFrontConnectionProperties waveFrontConnectionProperties = new WaveFrontConnectionProperties();
        waveFrontConnectionProperties.setWavefrontInstance(properties.getProperty("WavefrontInstance"));
        waveFrontConnectionProperties.setWavefrontAPIToken(properties.getProperty("WavefrontAPIToken"));
        waveFrontConnectionProperties.setSource(properties.getProperty("WaveFrontSource"));
        HashMap<String, String> staticTags = new HashMap<>();
        staticTags.put(properties.getProperty("WaveFrontStaticTagKey"),
                properties.getProperty("WaveFrontStaticTagValue"));
        waveFrontConnectionProperties.setStaticTags(staticTags);
        return waveFrontConnectionProperties;
    }

    public EndpointSpec getDatabaseEndpointSpec() {
        EndpointSpec dbEndpointSpec = new EndpointSpec();
        dbEndpointSpec.setEnable(true);
        dbEndpointSpec.setEndPointType(EndpointType.DATABASE);
        dbEndpointSpec.setName("EP_Database_test");
        dbEndpointSpec.setDatabaseConnectionProperties(getDatabaseConnectionProperties());
        dbEndpointSpec.setCredentialsName(new CredentialsSpecMockData().getDatabaseCredentials().getName());
        return dbEndpointSpec;
    }

    public DatabaseConnectionProperties getDatabaseConnectionProperties() {
        DatabaseConnectionProperties connectionProperties = new DatabaseConnectionProperties();
        EndpointSpec endpointSpec = rmEndpointMockData();
        connectionProperties.setParentEndpointName(endpointSpec.getName());
        connectionProperties.setDbType(DatabaseType.POSTGRES);
        connectionProperties.setParentEndpointType(endpointSpec.getEndPointType());
        return connectionProperties;
    }
}
