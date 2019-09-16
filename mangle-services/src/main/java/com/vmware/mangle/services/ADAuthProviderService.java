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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.security.ADAuthProviderDto;
import com.vmware.mangle.services.repository.ADAuthProviderRepository;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Acts as a wrapper over mongodb repository, and provide some default operations for Active
 * Directory Authentication providers
 *
 * @author chetanc
 */
@Service
@Log4j2
public class ADAuthProviderService {

    @Autowired
    ADAuthProviderRepository adAuthProviderRepository;

    /**
     * returns the AD authentication provider matching the given adDomain
     *
     * @param adDomain
     * @return
     */
    public ADAuthProviderDto getADAuthProviderByAdDomain(String adDomain) {
        log.info(String.format("Fetching AD authentication provider for id: %s", adDomain));
        return adAuthProviderRepository.findByAdDomain(adDomain);
    }

    /**
     *
     * @return list of all the authentication providers that are configured in the DB
     */
    public List<ADAuthProviderDto> getAllADAuthProvider() {
        log.info("Fetching all the AD authentication providers configured");
        return adAuthProviderRepository.findAll();
    }

    /**
     * updates the authentication provider whose id matches the id of the input authentication
     * provider instance
     *
     * @param adAuthProvider
     * @return updated authentication provider after the operation
     */
    public ADAuthProviderDto updateADAuthProvider(ADAuthProviderDto adAuthProvider) {
        log.info("Updating AD authentication provider");
        ADAuthProviderDto persistence = getADAuthProviderByAdDomain(adAuthProvider.getAdDomain());
        persistence.setAdUrl(adAuthProvider.getAdUrl());
        persistence.setAdDomain(adAuthProvider.getAdDomain());
        return adAuthProviderRepository.save(persistence);
    }

    /**
     * create new instance of the AD authentication provider in the db
     *
     * @param adAuthProvider
     * @return persisted instance of the authentication provider
     */
    public ADAuthProviderDto addADAuthProvider(ADAuthProviderDto adAuthProvider) {
        log.info("Configuring a new authentication provider");
        return adAuthProviderRepository.save(adAuthProvider);
    }

    /**
     * Deletes the instance of authentication provider whose id matches the input authProviderId
     *
     * @param authProviderIds
     */
    public List<String> removeADAuthProvider(List<String> authProviderIds) throws MangleException {
        List<ADAuthProviderDto> persistedAdProviders = adAuthProviderRepository.findByAdDomains(authProviderIds);
        List<String> persistedAdProviderNames =
                persistedAdProviders.stream().map(ADAuthProviderDto::getAdDomain).collect(Collectors.toList());
        authProviderIds.removeAll(persistedAdProviderNames);
        if (!CollectionUtils.isEmpty(authProviderIds)) {
            throw new MangleException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.AUTH_PROVIDER,
                    authProviderIds.toString());
        }
        adAuthProviderRepository.deleteByAdDomainIn(persistedAdProviderNames);
        return persistedAdProviderNames;
    }

    /**
     * Check if the given ADAuthProviderDto configuration already exists
     *
     * @param adAuthProvider
     * @return true if already exists; else false
     */
    public boolean doesADAuthExists(ADAuthProviderDto adAuthProvider) {
        ADAuthProviderDto persisted = adAuthProviderRepository.findByAdDomain(adAuthProvider.getAdDomain());
        return (null != persisted);

    }

    /**
     * Retrieve the list of all the AD domains that are configured in the application
     *
     * @return set of AD domains
     */
    public Set<String> getAllDomains() {
        List<ADAuthProviderDto> providers = getAllADAuthProvider();
        Set<String> domains = new HashSet<>();
        for (ADAuthProviderDto provider : providers) {
            domains.add(provider.getAdDomain());
        }
        return domains;
    }
}
