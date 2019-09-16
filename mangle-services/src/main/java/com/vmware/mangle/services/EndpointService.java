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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.datastax.driver.core.PagingState;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.endpoint.CredentialsSpec;
import com.vmware.mangle.cassandra.model.endpoint.DockerCertificates;
import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.repository.EndpointRepository;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.clients.docker.CustomDockerClient;
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
@Component
@Log4j2
public class EndpointService {
    private EndpointRepository endpointRepository;
    private CredentialService credentialService;
    private EndpointCertificatesService certificatesService;
    private EndpointClientFactory endpointClientFactory;

    @Autowired
    public EndpointService(EndpointRepository endpointRepository, CredentialService credentialService,
            EndpointClientFactory endpointClientFactory, EndpointCertificatesService certificatesService) {
        this.endpointRepository = endpointRepository;
        this.credentialService = credentialService;
        this.endpointClientFactory = endpointClientFactory;
        this.certificatesService = certificatesService;
    }

    public List<EndpointSpec> getAllEndpoints() {
        log.info("Retrieving all Endpoints...");
        return endpointRepository.findAll();
    }

    public EndpointSpec getEndpointByName(String endpointName) throws MangleException {
        log.info("Retrieving endpoint by name : " + endpointName);
        if (endpointName != null && !endpointName.isEmpty()) {
            Optional<EndpointSpec> optional = endpointRepository.findByName(endpointName);
            if (optional.isPresent()) {
                return optional.get();
            } else {
                throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.ENDPOINT_NAME, endpointName);
            }

        } else {
            log.error(ErrorConstants.ENDPOINT_NAME + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.ENDPOINT_NAME);
        }
    }

    public List<EndpointSpec> getAllEndpointByType(EndpointType endPointType) throws MangleException {
        log.info("Geting endpoint by type : " + endPointType);
        if (endPointType != null && !endPointType.name().isEmpty()) {
            return endpointRepository.findByEndPointType(endPointType);
        } else {
            log.error(ErrorConstants.ENDPOINT_TYPE + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.ENDPOINT_TYPE);
        }
    }

    public List<String> getAllContainersByEndpointName(String endPointName) throws MangleException {
        log.info("Geting all containers by endPointName : " + endPointName);
        if (endPointName != null && !endPointName.isEmpty()) {
            Optional<EndpointSpec> endpointSpec = endpointRepository.findByName(endPointName);
            if (!endpointSpec.isPresent()) {
                throw new MangleException(ErrorConstants.NO_RECORD_FOUND, ErrorCode.NO_RECORD_FOUND,
                        ErrorConstants.ENDPOINT_NAME, endPointName);
            }
            if (endpointSpec.get().getEndPointType() != (EndpointType.DOCKER)) {
                throw new MangleException(ErrorConstants.DOCKER_INVALID_ENDPOINT,
                        ErrorCode.DOCKER_INVALID_ENDPOINT, endPointName);
            }
            if (endpointSpec.get().getEndPointType().equals(EndpointType.DOCKER)) {
                CustomDockerClient dockerClient =
                        new CustomDockerClient(endpointSpec.get().getDockerConnectionProperties().getDockerHostname(),
                                endpointSpec.get().getDockerConnectionProperties().getDockerPort(),
                                endpointSpec.get().getDockerConnectionProperties().getTlsEnabled(),
                                endpointSpec.get().getDockerConnectionProperties().getCertificatesName());
                return dockerClient.getAllContainerNames();
            }
        }
        return Collections.emptyList();
    }

    public EndpointSpec updateEndpointByEndpointName(String name, EndpointSpec endpointSpec) throws MangleException {
        log.info("Updating Endpoint by Endpoint name : " + name);
        if (name != null && endpointSpec != null) {
            Optional<EndpointSpec> optional = endpointRepository.findByName(name);
            if (!optional.isPresent()) {
                throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.ENDPOINT_NAME, name);
            }
            validateEndpointBeforeSave(endpointSpec, optional.orElse(null));
            return endpointRepository.save(endpointSpec);
        } else {
            log.error(ErrorConstants.ENDPOINT_NAME + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.ENDPOINT_NAME);
        }
    }

    public boolean testEndpointConnection(EndpointSpec endpoint) throws MangleException {
        log.info("Start execution of testEndpointConnection() method");
        validateCredentialsName(endpoint);
        validateEndpointConnectionProperties(endpoint);
        CredentialsSpec credentialsSpec = null;
        if (!StringUtils.isEmpty(endpoint.getCredentialsName())) {
            credentialsSpec = credentialService.getCredentialByName(endpoint.getCredentialsName());
        }
        if (endpoint.getEndPointType() == EndpointType.DOCKER
                && StringUtils.hasText(endpoint.getDockerConnectionProperties().getCertificatesName())) {
            DockerCertificates certificatesSpec = (DockerCertificates) certificatesService
                    .getCertificatesByName(endpoint.getDockerConnectionProperties().getCertificatesName());

            endpoint.getDockerConnectionProperties().setCertificatesSpec(certificatesSpec);
        }
        return endpointClientFactory.getEndPointClient(credentialsSpec, endpoint).testConnection();
    }

    public EndpointSpec addOrUpdateEndpoint(EndpointSpec endpointSpec) throws MangleException {
        log.info("Adding/Updating Endpoint...");
        if (endpointSpec != null) {
            log.info("Adding/Updating Endpoint with endpoint name : " + endpointSpec.getName());
            validateEndpointBeforeSave(endpointSpec,
                    endpointRepository.findByName(endpointSpec.getName()).orElse(null));
            return endpointRepository.save((EndpointSpec) EncryptFields.encrypt(endpointSpec));
        } else {
            log.error(ErrorConstants.ENDPOINT + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.ENDPOINT);
        }
    }

    public Slice<EndpointSpec> getEndpointBasedOnPage(int page, int size) {
        log.info("Geting requested page for Endpoints...");
        if (page == 1) {
            return endpointRepository.findAll(CassandraPageRequest.of(page - 1, size));
        } else {
            CassandraPageRequest cassandraPageRequest = CassandraPageRequest.of(0, size);
            Slice<EndpointSpec> slice = endpointRepository.findAll(cassandraPageRequest);
            for (int i = 1; i < page; i++) {
                PagingState pagingState = ((CassandraPageRequest) slice.getPageable()).getPagingState();
                if (pagingState == null) {
                    return slice;
                }
                cassandraPageRequest = CassandraPageRequest.of(slice.getPageable(), pagingState);
                slice = endpointRepository.findAll(cassandraPageRequest);
            }
            return slice;
        }
    }

    public int getTotalPages(Slice<EndpointSpec> slice) {
        long totalCount = endpointRepository.count();
        return slice.getSize() == 0 ? 1 : (int) Math.ceil((double) totalCount / (double) slice.getSize());
    }

    public List<String> getEndpointsByCredentialName(String credentialName) {
        List<EndpointSpec> endpointSpecs = getEndpointsSpecByCredentialName(credentialName);
        return endpointSpecs.stream().map(EndpointSpec::getName).collect(Collectors.toList());
    }

    public List<EndpointSpec> getEndpointsSpecByCredentialName(String credentialName) {
        return endpointRepository.findByCredentialsName(credentialName);
    }

    /**
     * @param endpointSpec
     * @param dbEndpointSpec
     * @throws MangleException
     */
    private void validateEndpointBeforeSave(EndpointSpec endpointSpec, EndpointSpec dbEndpointSpec)
            throws MangleException {
        if (dbEndpointSpec != null && !endpointSpec.getEndPointType().equals(dbEndpointSpec.getEndPointType())) {
            throw new MangleException(ErrorCode.DUPLICATE_RECORD_FOR_ENDPOINT, endpointSpec.getName(),
                    dbEndpointSpec.getEndPointType());
        }
    }

    /**
     * @param endpoint
     * @throws MangleException
     */
    private void validateCredentialsName(EndpointSpec endpoint) throws MangleException {
        if (endpoint.getEndPointType() != EndpointType.DOCKER && !StringUtils.hasText(endpoint.getCredentialsName())) {
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.CREDENTIAL_NAME);
        }
    }

    /**
     * @param endpoint
     * @throws MangleException
     */
    private void validateEndpointConnectionProperties(EndpointSpec endpoint) throws MangleException {
        if (getConnectionProperties(endpoint) == null) {
            throw new MangleException(ErrorCode.PROVIDE_CONNECTION_PROPERTIES_FOR_ENDPOINT, endpoint.getEndPointType());
        }
    }

    /**
     * @param endpoint
     */
    private Object getConnectionProperties(EndpointSpec endpoint) {
        switch (endpoint.getEndPointType()) {
        case DOCKER:
            return endpoint.getDockerConnectionProperties();
        case K8S_CLUSTER:
            return endpoint.getK8sConnectionProperties();
        case MACHINE:
            return endpoint.getRemoteMachineConnectionProperties();
        case VCENTER:
            return endpoint.getVCenterConnectionProperties();
        case AWS:
            return endpoint.getAwsConnectionProperties();
        default:
            return null;
        }
    }
}