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
import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.repository.EndpointRepository;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
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
    private EndpointClientFactory endpointClientFactory;

    @Autowired
    public EndpointService(EndpointRepository endpointRepository, CredentialService credentialService,
            EndpointClientFactory endpointClientFactory) {
        this.endpointRepository = endpointRepository;
        this.credentialService = credentialService;
        this.endpointClientFactory = endpointClientFactory;
    }

    public List<EndpointSpec> getAllEndpoints() {
        log.info("Geting all Endpoints...");
        return endpointRepository.findAll();
    }

    public EndpointSpec getEndpointByName(String endpointName) throws MangleException {
        log.info("Geting endpoint by name : " + endpointName);
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
            List<EndpointSpec> results = endpointRepository.findByEndPointType(endPointType);
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

    public EndpointSpec updateEndpointByEndpointName(String name, EndpointSpec endpointSpec) throws MangleException {
        log.info("Updating Endpoint by Endpoint name : " + name);
        if (name != null && endpointSpec != null) {
            EndpointSpec dbEndpointSpec;
            Optional<EndpointSpec> optional = endpointRepository.findByName(name);
            validateEndpointBeforeSave(endpointSpec, optional.orElse(null));
            if (optional.isPresent()) {
                dbEndpointSpec = optional.get();
            } else {
                throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.ENDPOINT_NAME, name);
            }

            dbEndpointSpec.setName(endpointSpec.getName());
            dbEndpointSpec.setCredentialsName(endpointSpec.getCredentialsName());
            dbEndpointSpec.setEndPointType(endpointSpec.getEndPointType());
            dbEndpointSpec.setDockerConnectionProperties(endpointSpec.getDockerConnectionProperties());
            dbEndpointSpec.setAwsConnectionProperties(endpointSpec.getAwsConnectionProperties());
            dbEndpointSpec.setRemoteMachineConnectionProperties(endpointSpec.getRemoteMachineConnectionProperties());
            dbEndpointSpec.setK8sConnectionProperties(endpointSpec.getK8sConnectionProperties());
            dbEndpointSpec.setTags(endpointSpec.getTags());
            return endpointRepository.save(dbEndpointSpec);
        } else {
            log.error(ErrorConstants.ENDPOINT_ID + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.ENDPOINT_ID);
        }
    }

    public boolean testEndpointConnection(EndpointSpec endpoint) throws MangleException {
        log.info("Start execution of testEndpointConnection() method");
        CredentialsSpec credentialsSpec = null;
        if (!StringUtils.isEmpty(endpoint.getCredentialsName())) {
            credentialsSpec = credentialService.getCredentialByName(endpoint.getCredentialsName());
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
}
