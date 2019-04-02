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

import com.vmware.mangle.cassandra.model.endpoint.CredentialsSpec;
import com.vmware.mangle.cassandra.model.endpoint.DockerConnectionProperties;
import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.endpoint.K8SConnectionProperties;
import com.vmware.mangle.cassandra.model.endpoint.K8SCredentials;
import com.vmware.mangle.cassandra.model.endpoint.RemoteMachineConnectionProperties;
import com.vmware.mangle.cassandra.model.endpoint.RemoteMachineCredentials;
import com.vmware.mangle.cassandra.model.endpoint.VCenterConnectionProperties;
import com.vmware.mangle.cassandra.model.endpoint.VCenterCredentials;
import com.vmware.mangle.utils.clients.docker.CustomDockerClient;
import com.vmware.mangle.utils.clients.endpoint.EndpointClient;
import com.vmware.mangle.utils.clients.kubernetes.KubernetesCommandLineClient;
import com.vmware.mangle.utils.clients.ssh.SSHUtils;
import com.vmware.mangle.utils.clients.vcenter.VCenterClient;
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

    public EndpointClient getEndPointClient(CredentialsSpec credentials, @NonNull EndpointSpec endpoint) {
        switch (endpoint.getEndPointType()) {
        case DOCKER:
            return getCustomDockerClient(endpoint.getDockerConnectionProperties());
        case K8S_CLUSTER:
            return getK8SClient(credentials, endpoint.getK8sConnectionProperties());
        case MACHINE:
            return getRemoteMachineClient(credentials, endpoint.getRemoteMachineConnectionProperties());
        case VCENTER:
            try {
                return getVCenterEndpoint(credentials, endpoint.getVCenterConnectionProperties());
            } catch (MangleException e) {
                log.error(e.getMessage());
                log.debug(e);
                return null;
            }
        default:
            return null;
        }
    }

    public VCenterClient getVCenterEndpoint(CredentialsSpec credentialsSpec, VCenterConnectionProperties connProperties)
            throws MangleException {
        VCenterCredentials vCenterCredentials = (VCenterCredentials) DecryptFields.decrypt(credentialsSpec);
        VCenterClient client = new VCenterClient(connProperties.getHostname(), vCenterCredentials.getUserName(),
                vCenterCredentials.getPassword(), connProperties.getVCenterAdapterProperties());
        client.getVCenterAdapterClient().testConnection();
        return client;
    }

    private CustomDockerClient getCustomDockerClient(DockerConnectionProperties dockerConnectionProps) {
        return new CustomDockerClient(dockerConnectionProps.getDockerHostname(), dockerConnectionProps.getDockerPort(),
                dockerConnectionProps.isTlsEnabled());
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
        if (null != k8sConnectionProps.getNamespace() && "" != k8sConnectionProps.getNamespace().trim()) {
            client.setNameSpace(k8sConnectionProps.getNamespace());
        }
        return client;
    }

    private SSHUtils getRemoteMachineClient(CredentialsSpec credentials,
            RemoteMachineConnectionProperties remoteMachineConnectionProps) {
        RemoteMachineCredentials remoteMachineCredentials =
                (RemoteMachineCredentials) DecryptFields.decrypt(credentials);
        SSHUtils sshClient = null;
        if (null == remoteMachineCredentials.getPrivateKey()) {
            sshClient = new SSHUtils(remoteMachineConnectionProps.getHost(), remoteMachineCredentials.getUserName(),
                    remoteMachineCredentials.getPassword(), remoteMachineConnectionProps.getSshPort(),
                    remoteMachineConnectionProps.getTimeout());
        } else {
            sshClient = new SSHUtils(remoteMachineConnectionProps.getHost(), remoteMachineCredentials.getUserName(),
                    remoteMachineConnectionProps.getSshPort(), new String(remoteMachineCredentials.getPrivateKey()),
                    remoteMachineConnectionProps.getTimeout());
        }
        return sshClient;
    }

}
