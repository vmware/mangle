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

package com.vmware.mangle.services;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Optional;

import com.datastax.driver.core.PagingState;
import com.github.dockerjava.core.util.CertificateUtils;
import lombok.extern.log4j.Log4j2;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.DecoderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.vmware.mangle.cassandra.model.endpoint.CertificatesSpec;
import com.vmware.mangle.cassandra.model.endpoint.DockerCertificates;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.repository.EndpointCertificatesRepository;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;
import com.vmware.mangle.utils.helpers.security.EncryptFields;

/**
 * Service class Endpoint Certificates.
 *
 * @author bkaranam
 */
@Service
@Log4j2
public class EndpointCertificatesService {

    private EndpointCertificatesRepository certificatesRepository;
    private static final String CA_CERT_NAME = "caCert";
    private static final String SERVER_CERT_NAME = "serverCert";


    @Autowired
    public EndpointCertificatesService(EndpointCertificatesRepository certificatesRepository) {
        this.certificatesRepository = certificatesRepository;
    }

    public List<CertificatesSpec> getAllCertificates() {
        log.debug("Received request to get all Certificates...");
        return certificatesRepository.findAll();
    }

    public CertificatesSpec getCertificatesByName(String name) throws MangleException {
        log.debug("Received request to get Certificates by name : {}", name);
        if (StringUtils.hasText(name)) {
            Optional<CertificatesSpec> optional = certificatesRepository.findByName(name);
            if (optional.isPresent()) {
                return optional.get();
            } else {
                throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.CERTIFICATES_NAME, name);
            }

        } else {
            log.error(ErrorConstants.CERTIFICATES_NAME + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.CERTIFICATES_NAME);
        }
    }

    public List<CertificatesSpec> getAllCertificatesByType(EndpointType endPointType) throws MangleException {
        log.debug("Received request to get Certificates by type : {}", endPointType);
        if (endPointType != null) {
            List<CertificatesSpec> results = certificatesRepository.findByType(endPointType);
            if (results != null && !results.isEmpty()) {
                return results;
            } else {
                throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.ENDPOINT_TYPE, endPointType);
            }

        } else {
            log.error(ErrorConstants.ENDPOINT_TYPE + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.ENDPOINT_TYPE);
        }
    }

    public CertificatesSpec addOrUpdateCertificates(CertificatesSpec certificatesSpec) throws MangleException {
        log.debug("Received request to Add Certificates...");
        if (certificatesSpec != null && containsLetterOrDigit(certificatesSpec.getName())) {
            log.debug("Adding Certificates with certificates name : {}", certificatesSpec.getName());
            validateCertificatesBeforeSave(certificatesSpec,
                    certificatesRepository.findByName(certificatesSpec.getName()).orElse(null));
            return certificatesRepository.save((CertificatesSpec) EncryptFields.encrypt(certificatesSpec));
        } else {
            log.error(ErrorConstants.CERTIFICATES_SPEC + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.CERTIFICATES_NAME_NOT_VALID,
                    (certificatesSpec != null ? certificatesSpec.getName() : null));
        }
    }

    public void validateMultipartFileSize(MultipartFile file, int expectedMaxFileSize) throws MangleException {
        if (null != file) {
            try {
                if (file.getBytes().length > expectedMaxFileSize) {
                    throw new MangleException(ErrorCode.FILE_SIZE_EXCEEDED, expectedMaxFileSize);
                }
            } catch (IOException e) {
                throw new MangleException(e, ErrorCode.FILE_SIZE_EXCEEDED, file.getOriginalFilename());
            }
        }
    }

    public DockerCertificates generateDockerCertificatesSpec(String id, String name, MultipartFile caCert,
            MultipartFile serverCert, MultipartFile privateKey) throws IOException {
        DockerCertificates dockerCertificates = new DockerCertificates();
        dockerCertificates.setName(name);
        if (null != caCert) {
            dockerCertificates.setCaCert(caCert.getBytes());
        }
        if (null != serverCert) {
            dockerCertificates.setServerCert(serverCert.getBytes());
        }
        if (null != privateKey) {
            dockerCertificates.setPrivateKey(privateKey.getBytes());
        }
        if (null != id) {
            dockerCertificates.setId(id);
        }
        return dockerCertificates;
    }

    public Slice<CertificatesSpec> getCertificatesBasedOnPage(int page, int size) {
        log.debug("Received request to get page for Certificates...");
        if (page == 1) {
            return certificatesRepository.findAll(CassandraPageRequest.of(page - 1, size));
        } else {
            CassandraPageRequest cassandraPageRequest = CassandraPageRequest.of(0, size);
            Slice<CertificatesSpec> slice = certificatesRepository.findAll(cassandraPageRequest);
            for (int i = 1; i < page; i++) {
                PagingState pagingState = ((CassandraPageRequest) slice.getPageable()).getPagingState();
                if (pagingState == null) {
                    return slice;
                }
                cassandraPageRequest = CassandraPageRequest.of(slice.getPageable(), pagingState);
                slice = certificatesRepository.findAll(cassandraPageRequest);
            }
            return slice;
        }
    }

    /**
     * @param certificatesSpec
     * @return
     * @throws MangleException
     */
    public CertificatesSpec updateCertificates(CertificatesSpec certificatesSpec) throws MangleException {
        log.debug("Received request to update Certificates");
        if (certificatesSpec != null && containsLetterOrDigit(certificatesSpec.getName())) {
            log.debug("Updating Certificates with certificates name : {}", certificatesSpec.getName());
            Optional<CertificatesSpec> optional = certificatesRepository.findByName(certificatesSpec.getName());
            if (!optional.isPresent()) {
                throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.CERTIFICATES_NAME,
                        certificatesSpec.getName());
            }
            validateCertificatesBeforeSave(certificatesSpec, optional.orElse(null));
            return certificatesRepository.save((CertificatesSpec) EncryptFields.encrypt(certificatesSpec));
        } else {
            log.error(ErrorConstants.CERTIFICATES_NAME + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.CERTIFICATES_NAME_NOT_VALID,
                    (certificatesSpec != null ? certificatesSpec.getName() : null));
        }
    }

    /**
     * @param certificatesSpec
     * @throws MangleException
     */
    private void validateCertificatesBeforeSave(CertificatesSpec certificatesSpec,
            CertificatesSpec dbCertificatesSpec) {
        if (dbCertificatesSpec != null && !certificatesSpec.getType().equals(dbCertificatesSpec.getType())) {
            throw new MangleRuntimeException(ErrorCode.DUPLICATE_RECORD_FOR_ENDPOINT_CERTIFICATES,
                    certificatesSpec.getName(), dbCertificatesSpec.getType());
        }
    }

    /**
     * Method is used to check whether given <code>string</code> contains at-least one valid
     * character.
     *
     * @param str
     * @return true if given <code>string</code> contains at-least one letter or digit otherwise
     *         false.
     */
    private boolean containsLetterOrDigit(String str) {
        boolean flag = false;
        if (str != null && !"null".equals(str)) {
            for (int i = 0; i < str.length(); i++) {
                if (Character.isLetterOrDigit(str.charAt(i))) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                flag = CommonUtils.validateName(str);
            }
        }
        return flag;
    }

    public void validateDockerCertificates(byte[] caCert, byte[] serverCert, byte[] privateKey) throws MangleException {
        Security.addProvider(new BouncyCastleProvider());
        validateDockerCertificate(caCert, CA_CERT_NAME);
        validateDockerCertificate(serverCert, SERVER_CERT_NAME);
        validateDockerPrivateKey(privateKey);
    }

    public void validateDockerCertificate(byte[] certificate, String certName) throws MangleException {
        try {
            List<Certificate> certificates = CertificateUtils.loadCertificates(new String(certificate));
            if (CollectionUtils.isEmpty(certificates)) {
                throw new MangleException(ErrorCode.DOCKER_INVALID_CERTIFICATE, certName);
            }
        } catch (CertificateException | IOException | DecoderException e) {
            throw new MangleException(ErrorCode.DOCKER_INVALID_CERTIFICATE, certName);
        }
    }

    public void validateDockerPrivateKey(byte[] privateKey) throws MangleException {
        try {
            PrivateKey key = CertificateUtils.loadPrivateKey(new String(privateKey));
            if (null == key) {
                throw new MangleException(ErrorCode.DOCKER_INVALID_PRIVATEKEY);
            }
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | DecoderException e) {
            throw new MangleException(ErrorCode.DOCKER_INVALID_PRIVATEKEY, e.getMessage());
        }
    }

    /**
     * The validation procedure may change in future,keeping a similar method as above for now.
     *
     * @param privateKey
     * @throws MangleException
     */
    public void validateRemoteMachinePrivateKey(MultipartFile privateKey) throws MangleException {
        Security.addProvider(new BouncyCastleProvider());
        try {
            PrivateKey key = CertificateUtils.loadPrivateKey(new String(privateKey.getBytes()));
            if (null == key) {
                throw new MangleException(ErrorCode.RM_INVALID_PRIVATEKEY);
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | DecoderException e) {
            throw new MangleException(ErrorCode.RM_INVALID_PRIVATEKEY, e.getMessage());
        } catch (IOException ioException) {
            throw new MangleException(ioException, ErrorCode.IO_EXCEPTION);
        }
    }
}