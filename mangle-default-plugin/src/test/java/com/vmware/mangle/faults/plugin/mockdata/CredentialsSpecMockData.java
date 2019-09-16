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

package com.vmware.mangle.faults.plugin.mockdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import com.vmware.mangle.cassandra.model.endpoint.AWSCredentials;
import com.vmware.mangle.cassandra.model.endpoint.K8SCredentials;
import com.vmware.mangle.cassandra.model.endpoint.RemoteMachineCredentials;
import com.vmware.mangle.cassandra.model.endpoint.VCenterCredentials;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.utils.ReadProperty;
import com.vmware.mangle.utils.constants.Constants;

/**
 * CredentialsSpec Mock Data.
 *
 * @author kumargautam
 */
@Log4j2
public class CredentialsSpecMockData {

    // AWS Mock Data
    private String awsCredentialsName;
    private String accessKeyId;
    private String secretKey;

    // RemoteMachine Mock Data
    private String rmName;
    private String rmUserName;
    private String rmPassword;
    private String rmPrivateKeyFile;

    // K8s Mock Data
    private String k8sName;
    private String k8sConfigFile;

    //vCenter Mock Data
    private String vcenterUsername;
    private String vcenterPassword;

    public CredentialsSpecMockData() {
        Properties properties = ReadProperty.readProperty(Constants.MOCKDATA_FILE);
        this.accessKeyId = properties.getProperty("awsAccessKeyID");
        this.secretKey = properties.getProperty("awsSecretkey");
        this.awsCredentialsName = properties.getProperty("awsName");

        this.rmName = properties.getProperty("rmName");
        this.rmUserName = properties.getProperty("rmUserName");
        this.rmPassword = properties.getProperty("rmPassword");
        this.rmPrivateKeyFile = properties.getProperty("rmPrivateKey");

        this.k8sName = properties.getProperty("k8sName");
        this.k8sConfigFile = properties.getProperty("k8sKubeConfig");

        vcenterUsername = properties.getProperty("vcenter.username");
        vcenterPassword = properties.getProperty("vcenter.password");
    }

    public AWSCredentials getAWSCredentialsData() {
        AWSCredentials awsCredentials = new AWSCredentials();
        awsCredentials.setName(awsCredentialsName);
        awsCredentials.setAccessKeyId(accessKeyId);
        awsCredentials.setSecretKey(secretKey);
        return awsCredentials;
    }

    public K8SCredentials getk8SCredentialsData() {
        K8SCredentials k8sCredentials = new K8SCredentials();
        k8sCredentials.setName(k8sName);
        MultipartFile multipartFile;
        try {
            File file = new File(k8sConfigFile);
            log.info("k8sConfigFile Path : " + file.getAbsolutePath());
            multipartFile = new MockMultipartFile(file.getName(), new FileInputStream(file));
            k8sCredentials.setKubeConfig(multipartFile.getBytes());
        } catch (FileNotFoundException e) {
            log.error("k8sConfigFile " + e);
        } catch (IOException e) {
            log.error("k8sConfigFile " + e);
        }
        return k8sCredentials;
    }

    public RemoteMachineCredentials getRMCredentialsData() {
        RemoteMachineCredentials rmCredentials = new RemoteMachineCredentials();
        rmCredentials.setName(rmName);
        rmCredentials.setUsername(rmUserName);
        rmCredentials.setPassword(rmPassword);
        MultipartFile multipartFile;
        try {
            Path path = Paths.get(rmPrivateKeyFile);
            multipartFile = new MockMultipartFile(path.getFileName().toString(),
                    new FileInputStream(new File(rmPrivateKeyFile)));
            rmCredentials.setPrivateKey(multipartFile.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rmCredentials;
    }

    public VCenterCredentials getVCenterCredentialsData() {
        VCenterCredentials vCenterCredentials = new VCenterCredentials();
        vCenterCredentials.setName("vCenterMockCred");
        vCenterCredentials.setUserName(vcenterUsername);
        vCenterCredentials.setPassword(vcenterPassword);
        vCenterCredentials.setType(EndpointType.VCENTER);
        return vCenterCredentials;
    }

    public AWSCredentials getAwsCredentialsData() {
        AWSCredentials awsCredentials = new AWSCredentials();
        awsCredentials.setName("awsCreds");
        awsCredentials.setAccessKeyId("dummyAccessKeyId");
        awsCredentials.setSecretKey("dummySecretAccessKey");
        return awsCredentials;
    }

    public void getK8SKubeConfig(LinkedMultiValueMap<String, Object> multiPartMap) {
        File file = new File(k8sConfigFile);
        if (file.exists()) {
            multiPartMap.add("kubeConfig", new FileSystemResource(file));
        }
    }
}
