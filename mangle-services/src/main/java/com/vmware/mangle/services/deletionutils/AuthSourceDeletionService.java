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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.security.ADAuthProviderDto;
import com.vmware.mangle.cassandra.model.security.User;
import com.vmware.mangle.model.response.DeleteOperationResponse;
import com.vmware.mangle.services.UserService;
import com.vmware.mangle.services.repository.ADAuthProviderRepository;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author rpraveen
 *
 *
 */
@Log4j2
@Service
public class AuthSourceDeletionService {

    private ADAuthProviderRepository authProviderRepository;
    private UserService userService;

    @Autowired
    public AuthSourceDeletionService(ADAuthProviderRepository authProviderRepository, UserService userService) {
        this.authProviderRepository = authProviderRepository;
        this.userService = userService;
    }

    /**
     * Deletes the given list of adAuthProviders from the repository
     *
     * @param adAuthProviders
     * @throws MangleException
     */
    public DeleteOperationResponse deleteAuthSourceByAuthProviderNames(List<String> authProviders)
            throws MangleException {
        log.info("Deleting the AuthProviders: " + authProviders.toString());
        DeleteOperationResponse response = new DeleteOperationResponse();
        Map<String, List<String>> associations = new HashMap<>();
        List<ADAuthProviderDto> toBeDeletedAuthSources = new ArrayList<>();

        List<ADAuthProviderDto> persistedAuthProviders = authProviderRepository.findByAdDomains(authProviders);
        if (persistedAuthProviders.isEmpty()) {
            throw new MangleException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.AUTH_PROVIDER,
                    authProviders.toString());
        }
        List<String> domainNames =
                persistedAuthProviders.stream().map(ADAuthProviderDto::getAdDomain).collect(Collectors.toList());
        for (ADAuthProviderDto authProvider : persistedAuthProviders) {
            List<String> associatedUsers = getUserAssociationForAuthSource(authProvider.getAdDomain());
            if (!CollectionUtils.isEmpty(associatedUsers)) {
                associations.put(authProvider.getAdDomain(), associatedUsers);
            } else {
                toBeDeletedAuthSources.add(authProvider);
            }
        }
        if (CollectionUtils.isEmpty(associations)) {
            log.info("Pre-check successful, Deleting following AuthProviders: {}", domainNames.toString());
            authProviderRepository.deleteAll(toBeDeletedAuthSources);
        } else {
            response.setAssociations(associations);
            response.setResponseMessage(ErrorConstants.AUTHSOURCE_DELETION_PRE_CONDITION_FAILURE);
        }
        return response;
    }

    private List<String> getUserAssociationForAuthSource(String adDomain) {
        List<User> allUsers = userService.getAllUsers();
        List<User> associatedUsers = new ArrayList<>();
        for (User user : allUsers) {
            if (user.getName().contains(adDomain)) {
                associatedUsers.add(user);
            }
        }
        if (!CollectionUtils.isEmpty(associatedUsers)) {
            return associatedUsers.stream().map(User::getName).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
