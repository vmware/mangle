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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.endpoint.CertificatesSpec;
import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.model.response.DeleteOperationResponse;
import com.vmware.mangle.services.repository.EndpointCertificatesRepository;
import com.vmware.mangle.services.repository.EndpointRepository;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author bkaranam
 *
 */
@Service
@Log4j2
public class EndpointCertificatesDeletionService {

    private EndpointRepository endpointRepository;
    private EndpointCertificatesRepository certificatesRepository;

    @Autowired
    public EndpointCertificatesDeletionService(EndpointRepository endpointRepository,
            EndpointCertificatesRepository certificatesRepository) {
        this.endpointRepository = endpointRepository;
        this.certificatesRepository = certificatesRepository;
    }

    public DeleteOperationResponse deleteCertificatesByNames(List<String> certificatesNames) throws MangleException {
        log.info("Deleting Certificates by names : " + certificatesNames);
        if (!CollectionUtils.isEmpty(certificatesNames)) {
            List<CertificatesSpec> persistedCertificates = certificatesRepository.findByNames(certificatesNames);
            List<String> certificates =
                    persistedCertificates.stream().map(CertificatesSpec::getName).collect(Collectors.toList());
            certificatesNames.removeAll(certificates);
            if (certificatesNames.size() > 0) {
                throw new MangleException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.CERTIFICATES_NAME,
                        certificatesNames.toString());
            }
            return deleteCertificatesAndUpdateAssociations(certificates, persistedCertificates);
        } else {
            log.error(ErrorConstants.CERTIFICATES_NAME + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.CERTIFICATES_NAME);
        }
    }

    private DeleteOperationResponse deleteCertificatesAndUpdateAssociations(List<String> certificates,
            List<CertificatesSpec> persistedCertificates) throws MangleException {
        DeleteOperationResponse response = new DeleteOperationResponse();
        for (CertificatesSpec certificateSpec : persistedCertificates) {
            List<String> listOfEndpoints = new ArrayList<>();
            for (EndpointSpec endpointSpec : endpointRepository.findByEndPointType(certificateSpec.getType())) {
                if (endpointSpec.getEndPointType().equals(EndpointType.DOCKER)
                        && endpointSpec.getDockerConnectionProperties().getTlsEnabled().equals(true)
                        && endpointSpec.getDockerConnectionProperties().getCertificatesName()
                                .equals(certificateSpec.getName())) {
                    listOfEndpoints.add(endpointSpec.getName());
                }
            }
            if (!CollectionUtils.isEmpty(listOfEndpoints)) {
                response.getAssociations().put(certificateSpec.getName(), listOfEndpoints);
            }
        }
        if (response.getAssociations().size() == 0) {
            certificatesRepository.deleteByNameIn(certificates);
        }
        return response;
    }

}
