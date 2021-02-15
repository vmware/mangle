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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.endpoint.VCenterAdapterDetails;
import com.vmware.mangle.cassandra.model.endpoint.VCenterAdapterProperties;
import com.vmware.mangle.services.repository.VCenterAdapterDetailsRepository;
import com.vmware.mangle.utils.clients.vcenter.VCenterAdapterClient;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;
import com.vmware.mangle.utils.helpers.security.DecryptFields;
import com.vmware.mangle.utils.helpers.security.EncryptFields;

/**
 * @author chetanc
 */
@Service
@Log4j2
public class VCenterAdapterDetailsService {

    private VCenterAdapterDetailsRepository repository;

    @Autowired
    public VCenterAdapterDetailsService(VCenterAdapterDetailsRepository repository) {
        this.repository = repository;
    }

    public List<VCenterAdapterDetails> getAllVCenterAdapterDetails() {
        return repository.findAll();
    }

    public VCenterAdapterDetails getVCAdapterDetailsByName(String vCAdapterDetailsName) {
        log.debug("Retrieving VCenterDetails for the name {}", vCAdapterDetailsName);
        VCenterAdapterDetails vCenterAdapterDetails = null;
        if (!StringUtils.isEmpty(vCAdapterDetailsName)) {
            vCenterAdapterDetails = repository.findByName(vCAdapterDetailsName).orElse(null);
        }
        return vCenterAdapterDetails;
    }

    public VCenterAdapterDetails findVCAdapterDetailsByVCAdapterProperties(
            VCenterAdapterProperties vCAdapterProperties) {
        log.debug("Retrieving VCenterDetails for the name VCenterAdapterProperties");
        List<VCenterAdapterDetails> adapterDetailsList =
                repository.findByAdapterUrl(vCAdapterProperties.getVcAdapterUrl());
        List<VCenterAdapterDetails> matchingVCenterAdapterDetails = adapterDetailsList.stream()
                .map(vCenterAdapterDetails -> (VCenterAdapterDetails) DecryptFields.decrypt(vCenterAdapterDetails))
                .filter(vCenterAdapterDetails -> vCAdapterProperties.getUsername()
                        .equals(vCenterAdapterDetails.getUsername())
                        && vCAdapterProperties.getPassword().equals(vCenterAdapterDetails.getPassword()))
                .collect(Collectors.toList());
        return CollectionUtils.isEmpty(matchingVCenterAdapterDetails) ? null : matchingVCenterAdapterDetails.get(0);
    }

    public VCenterAdapterDetails createVCADetailsFromVCAProperties(VCenterAdapterProperties vCenterAdapterProperties) {
        log.debug("Creating VCenterAdapterDetails from VCenterAdapterProperties VCenterAdapterProperties");
        VCenterAdapterDetails vCenterAdapterDetails = new VCenterAdapterDetails();
        vCenterAdapterDetails.setAdapterUrl(vCenterAdapterProperties.getVcAdapterUrl());
        vCenterAdapterDetails.setUsername(vCenterAdapterProperties.getUsername());
        vCenterAdapterDetails.setPassword(vCenterAdapterProperties.getPassword());
        vCenterAdapterDetails.setName(UUID.randomUUID().toString());
        return repository.save(vCenterAdapterDetails);
    }

    public VCenterAdapterDetails updateVCenterAdapterDetails(VCenterAdapterDetails adapterDetails)
            throws MangleException {
        log.debug("Updating VCenterAdapterDetails with the name {}", adapterDetails.getName());
        VCenterAdapterDetails persistedAdapterDetails = repository.findByName(adapterDetails.getName()).orElse(null);
        if (null == persistedAdapterDetails) {
            throw new MangleException(ErrorConstants.NO_RECORD_FOUND, ErrorCode.NO_RECORD_FOUND);
        }
        persistedAdapterDetails.setAdapterUrl(adapterDetails.getAdapterUrl());
        persistedAdapterDetails.setUsername(adapterDetails.getUsername());
        persistedAdapterDetails.setPassword(adapterDetails.getPassword());
        return repository.save((VCenterAdapterDetails) EncryptFields.encrypt(persistedAdapterDetails));
    }

    public VCenterAdapterDetails createVCenterAdapterDetails(VCenterAdapterDetails adapterDetails)
            throws MangleException {
        log.debug("Creating VCenterAdapterDetails with the name {}", adapterDetails.getName());
        VCenterAdapterDetails persistedAdapterDetails = repository.findByName(adapterDetails.getName()).orElse(null);
        if (null != persistedAdapterDetails) {
            throw new MangleException(ErrorConstants.DUPLICATE_RECORD, ErrorCode.DUPLICATE_RECORD);
        }
        return repository.save((VCenterAdapterDetails) EncryptFields.encrypt(adapterDetails));
    }

    public void deleteVCenterAdapterDetails(List<String> vcDetailsNames) throws MangleException {
        log.info("Deleting vCenter Adapter Details: {}", vcDetailsNames);
        List<VCenterAdapterDetails> persistedAdapterDetails = repository.findByNameIn(vcDetailsNames);
        Set<String> persistedAdapterDetailsNames =
                persistedAdapterDetails.stream().map(VCenterAdapterDetails::getName).collect(Collectors.toSet());
        vcDetailsNames.removeAll(persistedAdapterDetailsNames);

        if (!CollectionUtils.isEmpty(vcDetailsNames)) {
            throw new MangleException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.VCENTER_ADAPTER_DETAILS,
                    vcDetailsNames.toString());
        }

        repository.deleteByNameIn(persistedAdapterDetailsNames);
    }

    public boolean testConnection(VCenterAdapterDetails adapterDetails) throws MangleException {
        log.debug("Testing connection to VCenterAdapter: {}", adapterDetails.getAdapterUrl());
        VCenterAdapterClient adapterClient = new VCenterAdapterClient(adapterDetails);
        adapterClient.testConnection();
        return true;
    }

}
