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
import java.util.List;
import java.util.Optional;

import com.datastax.driver.core.PagingState;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.vmware.mangle.cassandra.model.endpoint.CredentialsSpec;
import com.vmware.mangle.cassandra.model.endpoint.K8SCredentials;
import com.vmware.mangle.cassandra.model.endpoint.RemoteMachineCredentials;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.repository.CredentialRepository;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;
import com.vmware.mangle.utils.helpers.security.EncryptFields;

/**
 * Service class Endpoint.
 *
 * @author kumargautam
 */
@Service
@Log4j2
public class CredentialService {

    private CredentialRepository credentialRepository;

    @Autowired
    public CredentialService(CredentialRepository credentialRepository) {
        this.credentialRepository = credentialRepository;
    }

    public List<CredentialsSpec> getAllCredentials() {
        log.info("Geting all Credential...");
        return credentialRepository.findAll();
    }

    public CredentialsSpec getCredentialByName(String name) throws MangleException {
        log.info("Geting Credential by name : " + name);
        if (name != null && !name.isEmpty()) {
            Optional<CredentialsSpec> optional = credentialRepository.findByName(name);
            if (optional.isPresent()) {
                return optional.get();
            } else {
                throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.CREDENTIAL_NAME, name);
            }

        } else {
            log.error(ErrorConstants.CREDENTIAL_NAME + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.CREDENTIAL_NAME);
        }
    }

    public List<CredentialsSpec> getAllCredentialByType(EndpointType endPointType) throws MangleException {
        log.info("Geting Credential by type : " + endPointType);
        if (endPointType != null) {
            List<CredentialsSpec> results = credentialRepository.findByType(endPointType);
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

    public CredentialsSpec addOrUpdateCredentials(CredentialsSpec credentialsSpec) throws MangleException {
        log.info("Adding/Updating Credentials...");
        if (credentialsSpec != null) {
            log.info("Adding/Updating Credentials with credential name : " + credentialsSpec.getName());
            return credentialRepository.save((CredentialsSpec) EncryptFields.encrypt(credentialsSpec));
        } else {
            log.error(ErrorConstants.CREDENTIALS_SPEC + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.CREDENTIALS_SPEC);
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

    public RemoteMachineCredentials generateRemoteMachineCredentialsSpec(String id, String name, String username,
            String password, MultipartFile privateKey) throws IOException {
        RemoteMachineCredentials remoteMachineCredentials = new RemoteMachineCredentials();
        remoteMachineCredentials.setName(name);
        remoteMachineCredentials.setUserName(username);
        remoteMachineCredentials.setPassword(password);
        if (null != privateKey) {
            remoteMachineCredentials.setPrivateKey(privateKey.getBytes());
        }
        if (null != id) {
            remoteMachineCredentials.setId(id);
        }
        return remoteMachineCredentials;
    }

    public K8SCredentials generateK8SCredentialsSpec(String id, String name, MultipartFile kubeConfig)
            throws IOException {
        K8SCredentials k8sCredentials = new K8SCredentials();
        k8sCredentials.setName(name);
        if (null != kubeConfig) {
            k8sCredentials.setKubeConfig(kubeConfig.getBytes());
        }
        if (null != id) {
            k8sCredentials.setId(id);
        }
        return k8sCredentials;

    }

    public Slice<CredentialsSpec> getCredentialsBasedOnPage(int page, int size) {
        log.info("Geting requested page for Credentials...");
        if (page == 1) {
            return credentialRepository.findAll(CassandraPageRequest.of(page - 1, size));
        } else {
            CassandraPageRequest cassandraPageRequest = CassandraPageRequest.of(0, size);
            Slice<CredentialsSpec> slice = credentialRepository.findAll(cassandraPageRequest);
            for (int i = 1; i < page; i++) {
                PagingState pagingState = ((CassandraPageRequest) slice.getPageable()).getPagingState();
                if (pagingState == null) {
                    return slice;
                }
                cassandraPageRequest = CassandraPageRequest.of(slice.getPageable(), pagingState);
                slice = credentialRepository.findAll(cassandraPageRequest);
            }
            return slice;
        }
    }

    public int getTotalPages(Slice<CredentialsSpec> slice) {
        long totalCount = credentialRepository.count();
        return slice.getSize() == 0 ? 1 : (int) Math.ceil((double) totalCount / (double) slice.getSize());
    }
}
