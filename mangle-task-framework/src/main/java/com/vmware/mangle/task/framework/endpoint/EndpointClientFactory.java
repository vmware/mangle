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

package com.vmware.mangle.task.framework.endpoint;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import com.vmware.mangle.cassandra.model.endpoint.AWSConnectionProperties;
import com.vmware.mangle.cassandra.model.endpoint.AWSCredentials;
import com.vmware.mangle.cassandra.model.endpoint.AzureConnectionProperties;
import com.vmware.mangle.cassandra.model.endpoint.AzureCredentials;
import com.vmware.mangle.cassandra.model.endpoint.CredentialsSpec;
import com.vmware.mangle.cassandra.model.endpoint.DockerCertificates;
import com.vmware.mangle.cassandra.model.endpoint.DockerConnectionProperties;
import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.endpoint.K8SConnectionProperties;
import com.vmware.mangle.cassandra.model.endpoint.K8SCredentials;
import com.vmware.mangle.cassandra.model.endpoint.RedisProxyConnectionProperties;
import com.vmware.mangle.cassandra.model.endpoint.RemoteMachineConnectionProperties;
import com.vmware.mangle.cassandra.model.endpoint.RemoteMachineCredentials;
import com.vmware.mangle.cassandra.model.endpoint.VCenterConnectionProperties;
import com.vmware.mangle.cassandra.model.endpoint.VCenterCredentials;
import com.vmware.mangle.utils.clients.aws.CustomAwsClient;
import com.vmware.mangle.utils.clients.azure.CustomAzureClient;
import com.vmware.mangle.utils.clients.database.DatabaseClient;
import com.vmware.mangle.utils.clients.docker.CustomDockerClient;
import com.vmware.mangle.utils.clients.endpoint.EndpointClient;
import com.vmware.mangle.utils.clients.kubernetes.KubernetesCommandLineClient;
import com.vmware.mangle.utils.clients.redis.RedisProxyClient;
import com.vmware.mangle.utils.clients.ssh.SSHUtils;
import com.vmware.mangle.utils.clients.vcenter.VCenterClient;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;
import com.vmware.mangle.utils.helpers.security.DecryptFields;

/**
 * @author bkaranam
 * @author ashrimali
 */
@Log4j2
@Component
public class EndpointClientFactory {

    public EndpointClient getEndPointClient(CredentialsSpec credentials, @NonNull EndpointSpec endpoint)
            throws MangleException {
        if (null != endpoint.getEnable() && !endpoint.getEnable()) {
            throw new MangleException(ErrorCode.ENDPOINT_DISABLED, endpoint.getName());
        }
        switch (endpoint.getEndPointType()) {
        case DOCKER:
            return getCustomDockerClient(endpoint.getDockerConnectionProperties());
        case K8S_CLUSTER:
            return getK8SClient(credentials, endpoint.getK8sConnectionProperties());
        case MACHINE:
            return getRemoteMachineClient(credentials, endpoint.getRemoteMachineConnectionProperties());
        case VCENTER:
            return getVCenterEndpoint(credentials, endpoint.getVCenterConnectionProperties());
        case AWS:
            return getAwsEndpoint(credentials, endpoint.getAwsConnectionProperties());
        case AZURE:
            return getAzureEndpoint(credentials, endpoint.getAzureConnectionProperties());
        case REDIS_FI_PROXY:
            return getRedisProxyEndpoint(endpoint.getRedisProxyConnectionProperties());
        case DATABASE:
            return new DatabaseClient();
        default:
            return null;
        }
    }

    public VCenterClient getVCenterEndpoint(CredentialsSpec credentialsSpec,
            VCenterConnectionProperties connProperties) {
        VCenterCredentials vCenterCredentials = (VCenterCredentials) DecryptFields.decrypt(credentialsSpec);
        return new VCenterClient(connProperties.getHostname(), vCenterCredentials.getUserName(),
                vCenterCredentials.getPassword(), connProperties.getVCenterAdapterDetails());
    }

    private CustomAzureClient getAzureEndpoint(CredentialsSpec credentialsSpec,
            AzureConnectionProperties connProperties) {
        AzureCredentials azureCredentials = (AzureCredentials) DecryptFields.decrypt(credentialsSpec);
        return new CustomAzureClient(connProperties.getSubscriptionId(), connProperties.getTenant(),
                azureCredentials.getAzureClientId(), azureCredentials.getAzureClientKey());
    }

    public CustomAwsClient getAwsEndpoint(CredentialsSpec credentialsSpec, AWSConnectionProperties connProperties) {
        AWSCredentials awsCredentials = (AWSCredentials) DecryptFields.decrypt(credentialsSpec);
        return new CustomAwsClient(connProperties.getRegion(), awsCredentials.getAccessKeyId(),
                awsCredentials.getSecretKey());
    }

    private CustomDockerClient getCustomDockerClient(DockerConnectionProperties dockerConnectionProps) {
        if (null != dockerConnectionProps.getCertificatesSpec()) {
            DockerCertificates certificates =
                    (DockerCertificates) DecryptFields.decrypt(dockerConnectionProps.getCertificatesSpec());
            String dockerCertPath = createDockerCertPath(certificates);
            return new CustomDockerClient(dockerConnectionProps.getDockerHostname(),
                    dockerConnectionProps.getDockerPort(), dockerConnectionProps.getTlsEnabled(), dockerCertPath);
        } else {
            return new CustomDockerClient(dockerConnectionProps.getDockerHostname(),
                    dockerConnectionProps.getDockerPort(), dockerConnectionProps.getTlsEnabled(), null);
        }
    }

    private String createDockerCertPath(DockerCertificates certificates) {
        String dockerCertPath = System.getProperty("java.io.tmpdir") + File.separator + "dockerCertPath"
                + File.separator + certificates.getName();
        try {
            FileUtils.forceMkdir(new File(dockerCertPath));
            File caCertFile = new File(dockerCertPath + File.separator + "ca.pem");
            File serverCertFile = new File(dockerCertPath + File.separator + "cert.pem");
            File privateKeyFile = new File(dockerCertPath + File.separator + "key.pem");

            if (caCertFile.exists()) {
                Files.delete(caCertFile.toPath());
            }
            FileUtils.writeByteArrayToFile(caCertFile, certificates.getCaCert());

            if (serverCertFile.exists()) {
                Files.delete(serverCertFile.toPath());
            }
            FileUtils.writeByteArrayToFile(serverCertFile, certificates.getServerCert());

            if (privateKeyFile.exists()) {
                Files.delete(privateKeyFile.toPath());
            }
            FileUtils.writeByteArrayToFile(privateKeyFile, certificates.getPrivateKey());
        } catch (IOException e) {
            log.warn(e.getMessage());
            throw new MangleRuntimeException(e, ErrorCode.IO_EXCEPTION);
        }
        return dockerCertPath;
    }

    private KubernetesCommandLineClient getK8SClient(CredentialsSpec credentials,
            K8SConnectionProperties k8sConnectionProps) {
        K8SCredentials k8sCredentials = (K8SCredentials) DecryptFields.decrypt(credentials);
        KubernetesCommandLineClient client = KubernetesCommandLineClient.getClient();
        if (null != k8sCredentials.getKubeConfig()) {
            String kubeConfigLocation = System.getProperty("java.io.tmpdir") + File.separator + "kubeconfig"
                    + File.separator + k8sCredentials.getName();
            File file = new File(kubeConfigLocation);
            try {
                if (file.exists()) {
                    Files.delete(file.toPath());
                }
                FileUtils.writeByteArrayToFile(file, k8sCredentials.getKubeConfig());
                client.setKubeconfig(kubeConfigLocation);
            } catch (IOException e) {
                log.warn(e.getMessage());
                throw new MangleRuntimeException(e, ErrorCode.IO_EXCEPTION);
            }
        }
        if (null != k8sConnectionProps.getNamespace() && !"".equals(k8sConnectionProps.getNamespace().trim())) {
            client.setNameSpace(k8sConnectionProps.getNamespace());
        }
        testConnection(client);
        return client;
    }

    private SSHUtils getRemoteMachineClient(CredentialsSpec credentials,
            RemoteMachineConnectionProperties remoteMachineConnectionProps) {
        RemoteMachineCredentials remoteMachineCredentials =
                (RemoteMachineCredentials) DecryptFields.decrypt(credentials);
        SSHUtils sshClient = null;
        if (null == remoteMachineCredentials.getPrivateKey()) {
            sshClient = new SSHUtils(remoteMachineConnectionProps.getHost(), remoteMachineCredentials.getUsername(),
                    remoteMachineCredentials.getPassword(), remoteMachineConnectionProps.getSshPort(),
                    remoteMachineConnectionProps.getTimeout());
        } else {
            sshClient = new SSHUtils(remoteMachineConnectionProps.getHost(), remoteMachineCredentials.getUsername(),
                    remoteMachineConnectionProps.getSshPort(), new String(remoteMachineCredentials.getPrivateKey()),
                    remoteMachineConnectionProps.getTimeout());
        }
        return sshClient;
    }

    public void testConnection(EndpointClient client) throws MangleRuntimeException {
        try {
            client.testConnection();
        } catch (MangleException exception) {
            throw new MangleRuntimeException(ErrorConstants.TEST_CONNECTION_FAILED, ErrorCode.TEST_CONNECTION_FAILED);
        }
    }

    private EndpointClient getRedisProxyEndpoint(RedisProxyConnectionProperties redisProxyConnectionProperties) {
        RedisProxyClient client = RedisProxyClient.getClient();
        client.setRedisProxyConnectionProperties(redisProxyConnectionProperties);
        return client;
    }
}
