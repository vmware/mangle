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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.datastax.driver.core.PagingState;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.vmware.mangle.cassandra.model.endpoint.CredentialsSpec;
import com.vmware.mangle.cassandra.model.endpoint.K8SCredentials;
import com.vmware.mangle.cassandra.model.endpoint.RemoteMachineCredentials;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.repository.CredentialRepository;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.clients.kubernetes.KubernetesCommandLineClient;
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
        log.debug("Received request to get all Credential...");
        return credentialRepository.findAll();
    }

    public CredentialsSpec getCredentialByName(String name) throws MangleException {
        log.debug("Received request to get Credential by name : {}", name);
        if (StringUtils.hasText(name)) {
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
        log.debug("Received request to get Credential by type : {}", endPointType);
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
        log.debug("Received request to Add Credentials...");
        if (credentialsSpec != null && containsLetterOrDigit(credentialsSpec.getName())) {
            log.debug("Adding Credentials with credential name : {}", credentialsSpec.getName());
            validateCredentialsBeforeSave(credentialsSpec,
                    credentialRepository.findByName(credentialsSpec.getName()).orElse(null));
            return credentialRepository.save((CredentialsSpec) EncryptFields.encrypt(credentialsSpec));
        } else {
            log.error(ErrorConstants.CREDENTIALS_SPEC + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.CREDENTIAL_NAME_NOT_VALID,
                    (credentialsSpec != null ? credentialsSpec.getName() : null));
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

    public void validateK8SConfigFile(MultipartFile file) throws MangleException {
        if (null != file) {
            File temp = null;
            try {
                temp = File.createTempFile(file.getOriginalFilename(), ".config");
                FileCopyUtils.copy(file.getBytes(), temp);
                KubernetesCommandLineClient.validateConfigFile(temp.getAbsolutePath());
            } catch (IOException e) {
                throw new MangleException(ErrorCode.IO_EXCEPTION, e.getMessage());
            } finally {
                if (null != temp && temp.exists()) {
                    FileSystemUtils.deleteRecursively(temp);
                }
            }

        }
    }

    public RemoteMachineCredentials generateRemoteMachineCredentialsSpec(String id, String name, String username,
            String password, MultipartFile privateKey) throws IOException {
        RemoteMachineCredentials remoteMachineCredentials = new RemoteMachineCredentials();
        remoteMachineCredentials.setName(name);
        remoteMachineCredentials.setUsername(username);
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
        log.debug("Received request to get page for Credentials...");
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

    /**
     * @param credentialsSpec
     * @return
     * @throws MangleException
     */
    public CredentialsSpec updateCredential(CredentialsSpec credentialsSpec) throws MangleException {
        log.debug("Received request to update Credential");
        if (credentialsSpec != null && containsLetterOrDigit(credentialsSpec.getName())) {
            log.debug("Updating Credentials with credential name : {}", credentialsSpec.getName());
            Optional<CredentialsSpec> optional = credentialRepository.findByName(credentialsSpec.getName());
            if (!optional.isPresent()) {
                throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.CREDENTIAL_NAME,
                        credentialsSpec.getName());
            }
            validateCredentialsBeforeSave(credentialsSpec, optional.orElse(null));
            return credentialRepository.save((CredentialsSpec) EncryptFields.encrypt(credentialsSpec));
        } else {
            log.error(ErrorConstants.CREDENTIAL_NAME + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.CREDENTIAL_NAME_NOT_VALID,
                    (credentialsSpec != null ? credentialsSpec.getName() : null));
        }
    }

    /**
     * @param credentialsSpec
     * @throws MangleException
     */
    private void validateCredentialsBeforeSave(CredentialsSpec credentialsSpec, CredentialsSpec dbCredentialsSpec) {
        if (dbCredentialsSpec != null && !credentialsSpec.getType().equals(dbCredentialsSpec.getType())) {
            throw new MangleRuntimeException(ErrorCode.DUPLICATE_RECORD_FOR_CREDENTIAL, credentialsSpec.getName(),
                    dbCredentialsSpec.getType());
        }
    }

    /**
     * Method is used to check weather given <code>string</code> contains at-least one valid
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

    /**
     * @param password
     * @param privateKey
     */
    public void validatePasswordOrPrivateKeyNotNull(String password, MultipartFile privateKey) {
        if (!StringUtils.hasText(password) && ObjectUtils.isEmpty(privateKey)) {
            throw new MangleRuntimeException(ErrorCode.RM_CREDENTIAL_WITH_NEITHER_PASSWORD_NOR_PRIVATEKEY);
        }
    }
}