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

package com.vmware.mangle.services.controller;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vmware.mangle.cassandra.model.security.ADAuthProviderDto;
import com.vmware.mangle.services.ADAuthProviderService;
import com.vmware.mangle.services.UserService;
import com.vmware.mangle.services.config.ADAuthProvider;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * CRUD operations for authentication providers
 *
 * @author chetanc
 */
@Log4j2
@RestController
@RequestMapping("rest/api/v1/auth-provider-management")
public class AuthProviderController extends ResourceSupport {

    @Autowired
    ADAuthProviderService adAuthProviderService;

    @Autowired
    ADAuthProvider adAuthProvider;

    @Autowired
    UserService userService;

    /**
     * Gets all the AD providers that are configured in mangle
     */
    @ApiOperation(value = "API to get all the AD Authentication providers configured", nickname = "getAuthenticationProvider")
    @GetMapping(value = "/ad-auth-providers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resources<ADAuthProviderDto>> getAllADAuthProviders() {
        log.info("Received request to retrieve all configured authentication providers...");
        List<ADAuthProviderDto> authProviderDtos = adAuthProviderService.getAllADAuthProvider();

        Resources<ADAuthProviderDto> authResource = new Resources<>(authProviderDtos);
        Link link = linkTo(methodOn(AuthProviderController.class).getAllADAuthProviders()).withSelfRel();
        authResource.add(link);

        return new ResponseEntity<>(authResource, HttpStatus.OK);
    }


    /**
     * Allows to update the existing AD provider that is already configured in the mangle
     *
     * exception: 1. when new update details already doesADAuthExists 2. when entry to be updated
     * doesn't doesADAuthExists 3. when test connection to the AD server fails
     *
     * @Param ADAuthProviderDto: Auth\Provider instance user provides
     */
    @ApiOperation(value = "API to update the AD authentication providers", nickname = "updateAuthenticationProvider")
    @PutMapping(value = "/ad-auth-providers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<ADAuthProviderDto>> updateADAuthProvider(
            @RequestBody ADAuthProviderDto adAuthProviderDto) throws MangleException {
        log.info("Received request to update authentication provider");

        ADAuthProviderDto persisted =
                adAuthProviderService.getADAuthProviderByAdDomain(adAuthProviderDto.getAdDomain());
        /*
         * Check if there is an entry in the database to update
         * exception if there isn't one with the id
         * */
        if (persisted == null) {
            log.error("Authprovider instance to be updated doesn't exist, failed to update");
            throw new MangleException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.AD_AUTHPROVIDER_ID,
                    adAuthProviderDto.getAdDomain());
        }
        /*
         * Check if there already doesADAuthExists a same entry
         * */
        if (persisted.getAdUrl().equals(adAuthProviderDto.getAdUrl())) {
            log.error("New authProvider instance configuration already exists in the application, failed to update");
            throw new MangleException(ErrorConstants.DUPLICATE_RECORD, ErrorCode.DUPLICATE_RECORD);
        }


        boolean isSuccessfullAdded =
                adAuthProvider.setAdAuthProvider(adAuthProviderDto.getAdUrl(), adAuthProviderDto.getAdDomain());


        /* throw exception if the connection to the AD failed*/
        if (!isSuccessfullAdded) {
            log.error(
                    "Authentication Provider configuration failed. Reason: Failed to Connect to the Authentication Provider");
            throw new MangleException(ErrorConstants.AUTHENTICATION_TEST_CONNECTION_FAILED,
                    ErrorCode.AUTH_TEST_CONNECTION_FAILED);
        }

        persisted = adAuthProviderService.updateADAuthProvider(adAuthProviderDto);

        Resource<ADAuthProviderDto> authResource = new Resource<>(persisted);

        Link link = linkTo(methodOn(AuthProviderController.class).updateADAuthProvider(null)).withSelfRel();

        authResource.add(link);

        return new ResponseEntity<>(authResource, HttpStatus.OK);
    }

    /**
     * Allows to configure new AD provider to the application
     *
     * exception: 1. when entry to be added already doesADAuthExists 2. when test connection to the
     * AD server fails
     *
     * @Param ADAuthProviderDto: ADAuthProviderDto instance user provides
     */
    @ApiOperation(value = "API to add the AD authentication provider", nickname = "addAuthenticationProvider")
    @PostMapping(value = "/ad-auth-providers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<ADAuthProviderDto>> addADAuthProvider(
            @RequestBody ADAuthProviderDto adAuthProviderDto) throws MangleException {
        log.info("Received request to add authentication provider");
        /*
         * Check if there is an entry in the database to update
         * exception if there isn't one with the id
         * */
        if (adAuthProviderService.doesADAuthExists(adAuthProviderDto)) {
            log.error("New authProvider instance configuration already exists in the application, failed to update");
            throw new MangleException(ErrorConstants.DUPLICATE_RECORD, ErrorCode.DUPLICATE_RECORD);
        }


        boolean isSuccessfullAdded =
                adAuthProvider.setAdAuthProvider(adAuthProviderDto.getAdUrl(), adAuthProviderDto.getAdDomain());

        /* throw exception if the connection to the AD failed*/
        if (!isSuccessfullAdded) {
            log.error(
                    "Authentication Provider configuration failed. Reason:  Failed to Connect to the Authentication Provider");
            throw new MangleException(ErrorConstants.AUTHENTICATION_TEST_CONNECTION_FAILED,
                    ErrorCode.AUTH_TEST_CONNECTION_FAILED);
        }

        ADAuthProviderDto persistedADAuthProviderDto = adAuthProviderService.addADAuthProvider(adAuthProviderDto);

        Resource<ADAuthProviderDto> authResource = new Resource<>(persistedADAuthProviderDto);
        Link link = linkTo(methodOn(AuthProviderController.class).addADAuthProvider(null)).withSelfRel();
        authResource.add(link);

        return new ResponseEntity<>(authResource, HttpStatus.CREATED);
    }

    /**
     * Allows to remove configured AD providers from the application
     *
     * @Param authProviderIds: List of IDs that are to be removed
     */
    @ApiOperation(value = "API to delete the AD authentication provider", nickname = "addAuthenticationProvider")
    @DeleteMapping(value = "/ad-auth-providers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity removeADAuthProvider(@RequestParam List<String> domainNames)
            throws MangleException {
        log.info(String.format("Received request to remove AD Authentication Providers: %s", domainNames.toString()));
        adAuthProviderService.removeADAuthProvider(domainNames);
        for (String domainName : domainNames) {
            adAuthProvider.removeAdAuthProvider(domainName);
        }

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @ApiOperation(value = "API to get all the domains contained in the application", nickname = "retrieveAllDomains")
    @GetMapping(value = "/domains", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resources<String>> getAlldomains() {
        log.info("Received request to retrieve all Authentication Provider Domains.");
        Set<String> domains = new HashSet<>();
        domains.addAll(adAuthProviderService.getAllDomains());
        domains.add(userService.getDefaultDomainName());

        Resources<String> resources = new Resources<>(domains);
        Link link = linkTo(methodOn(AuthProviderController.class).getAlldomains()).withSelfRel();
        resources.add(link);

        return new ResponseEntity<>(resources, HttpStatus.OK);
    }
}
