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

package com.vmware.mangle.services.deletionutils;

import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.endpoint.CredentialsSpec;
import com.vmware.mangle.model.response.DeleteOperationResponse;
import com.vmware.mangle.services.EndpointService;
import com.vmware.mangle.services.repository.CredentialRepository;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author chetanc
 *
 */
@Service
@Log4j2
public class CredentialDeletionService {

    private EndpointService endpointService;
    private CredentialRepository credentialRepository;

    @Autowired
    public CredentialDeletionService(EndpointService endpointService, CredentialRepository credentialRepository) {
        this.endpointService = endpointService;
        this.credentialRepository = credentialRepository;
    }

    public DeleteOperationResponse deleteCredentialsByNames(List<String> credentialNames) throws MangleException {
        log.info("Deleting Credentials by names : " + credentialNames);
        DeleteOperationResponse model = new DeleteOperationResponse();


        if (!CollectionUtils.isEmpty(credentialNames)) {

            List<CredentialsSpec> persistedCredentials = credentialRepository.findByNames(credentialNames);
            List<String> credentials =
                    persistedCredentials.stream().map(CredentialsSpec::getName).collect(Collectors.toList());
            credentialNames.removeAll(credentials);
            if (credentialNames.size() > 0) {
                throw new MangleException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.CREDENTIAL_NAME,
                        credentialNames.toString());
            }


            for (String credentialName : credentials) {
                List<String> endpoints = endpointService.getEndpointsByCredentialName(credentialName);
                if (!endpoints.isEmpty()) {
                    model.getAssociations().put(credentialName, endpoints);
                }
            }

            if (model.getAssociations().isEmpty()) {
                credentialRepository.deleteByNameIn(credentials);
            }

            return model;
        } else {
            log.error(ErrorConstants.CREDENTIAL_NAME + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.CREDENTIAL_NAME);
        }
    }

}
