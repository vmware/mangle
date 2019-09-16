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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

import lombok.extern.log4j.Log4j2;
import org.springframework.mock.web.MockMultipartFile;

import com.vmware.mangle.cassandra.model.endpoint.DockerCertificates;
import com.vmware.mangle.utils.ReadProperty;
import com.vmware.mangle.utils.constants.Constants;

/**
 * CertificatesSpec Mock Data.
 *
 * @author bkaranam
 */
@Log4j2
public class CertificatesSpecMockData {

    private String certificatesName;

    private String caCertPath;
    private String serverCertPath;
    private String privateKeyPath;


    public CertificatesSpecMockData() {
        Properties properties = ReadProperty.readProperty(Constants.MOCKDATA_FILE);
        this.caCertPath = properties.getProperty("caCertPath");
        this.serverCertPath = properties.getProperty("serverCertPath");
        this.privateKeyPath = properties.getProperty("dockerPrivateKeyPath");
        this.certificatesName = properties.getProperty("dockerCertificatesName");
    }

    public DockerCertificates getDockerCertificatesData() {
        DockerCertificates dockerCertificates = new DockerCertificates();
        dockerCertificates.setName(certificatesName);
        try {
            dockerCertificates.setPrivateKey(new MockMultipartFile(Paths.get(privateKeyPath).getFileName().toString(),
                    new FileInputStream(new File(privateKeyPath))).getBytes());
            dockerCertificates.setCaCert(new MockMultipartFile(Paths.get(caCertPath).getFileName().toString(),
                    new FileInputStream(new File(privateKeyPath))).getBytes());
            dockerCertificates.setServerCert(new MockMultipartFile(Paths.get(serverCertPath).getFileName().toString(),
                    new FileInputStream(new File(privateKeyPath))).getBytes());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return dockerCertificates;
    }

    public byte[] getInValidCertificatesAsbyteArray() {
        return new String("-----BEGIN CERTIFICATE-----\nInvalid Certificate\n-----END CERTIFICATE-----").getBytes();
    }

    public byte[] getInValidPrivateKey() {
        return new String("-----BEGIN RSA PRIVATE KEY-----\nInvalid PrivateKey\n-----END RSA PRIVATE KEY-----")
                .getBytes();
    }

    public byte[] getEmptyByteArray() {
        return "".getBytes();
    }
}
