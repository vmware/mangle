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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.swagger.annotations.ApiOperation;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.vmware.mangle.cassandra.model.security.ADAuthProviderDto;
import com.vmware.mangle.cassandra.model.security.ADAuthProviderDtoV1;
import com.vmware.mangle.model.response.DeleteOperationResponse;
import com.vmware.mangle.model.response.ErrorDetails;
import com.vmware.mangle.services.ADAuthProviderService;
import com.vmware.mangle.services.MappingService;
import com.vmware.mangle.services.UserService;
import com.vmware.mangle.services.config.ADAuthProvider;
import com.vmware.mangle.services.deletionutils.AuthSourceDeletionService;
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
@RequestMapping("rest/api/")
@NoArgsConstructor
public class AuthProviderController extends ResourceSupport {

    private ADAuthProviderService adAuthProviderService;

    private AuthSourceDeletionService authSourceDeletionService;

    private ADAuthProvider adAuthProvider;

    private UserService userService;

    private MappingService mappingService;

    @Autowired
    public AuthProviderController(ADAuthProviderService adAuthProviderService, ADAuthProvider adAuthProvider,
            UserService userService, MappingService mappingService,
            AuthSourceDeletionService authSourceDeletionService) {
        this.adAuthProviderService = adAuthProviderService;
        this.adAuthProvider = adAuthProvider;
        this.userService = userService;
        this.mappingService = mappingService;
        this.authSourceDeletionService = authSourceDeletionService;
    }

    /**
     * Gets all the AD providers that are configured in mangle
     */
    @ApiOperation(value = "API to get all the AD Authentication providers configured", nickname = "getAuthenticationProvider")
    @GetMapping(value = "v1/auth-provider-management/ad-auth-providers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resources<ADAuthProviderDto>> getAllADAuthProviders() throws MangleException {
        log.debug("Received request to retrieve all configured authentication providers...");
        List<ADAuthProviderDto> authProviderDtos = adAuthProviderService.getAllADAuthProvider();

        Resources<ADAuthProviderDto> authResource = new Resources<>(authProviderDtos);
        authResource.add(getSelfLink(), getAuthAddHateoasLink(), getAuthDeleteHateoasLink(),
                getAuthUpdateHateoasLink());

        return new ResponseEntity<>(authResource, HttpStatus.OK);
    }


    /**
     * Allows to update the existing AD provider
     *
     * exception:
     *
     * 1. when new update details already exists 2. when entry to be updated doesn't exists 3. when
     * test con\nection to the AD server fails
     *
     * @Param ADAuthProviderDto: Auth\Provider instance user provides
     *
     * @deprecated
     *
     */
    @Deprecated
    @ApiOperation(value = "API to update the AD authentication providers", nickname = "updateAuthenticationProvider")
    @PutMapping(value = "v1/auth-provider-management/ad-auth-providers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<ADAuthProviderDto>> updateADAuthProvider(
            @RequestBody ADAuthProviderDtoV1 adAuthProviderDtoV1) throws MangleException {
        log.debug("Received request to update authentication provider");
        ADAuthProviderDto adAuthProviderDto = mappingService.map(adAuthProviderDtoV1, ADAuthProviderDto.class);

        ADAuthProviderDto persisted =
                adAuthProviderService.getADAuthProviderByAdDomain(adAuthProviderDto.getAdDomain());
        preValidateADUpdate(adAuthProviderDto, persisted);

        /*
         * Check if there already doesADAuthExists a same entry
         * */
        if (persisted.getAdUrl().equals(adAuthProviderDto.getAdUrl())) {
            log.error("New authProvider instance configuration already exists in the application, failed to update");
            throw new MangleException(ErrorConstants.DUPLICATE_RECORD, ErrorCode.DUPLICATE_RECORD);
        }

        persisted = handleADUpdate(adAuthProviderDto);
        adAuthProvider.triggerMultiNodeResync(persisted.getAdDomain());
        Resource<ADAuthProviderDto> authResource = new Resource<>(persisted);

        authResource.add(getAuthGetHateoasLink(), getAuthAddHateoasLink(), getAuthDeleteHateoasLink(), getSelfLink());

        return new ResponseEntity<>(authResource, HttpStatus.OK);
    }

    /**
     * Allows to update the existing AD provider that is already configured in the mangle
     *
     * exception: 1. when new AD details provided has the same domain and same URL as the one
     * configured in the application 2. when entry to be updated doesn't exists 3. when test
     * connection to the AD server fails
     *
     * @Param ADAuthProviderDto: Auth\Provider instance user provides
     */
    @ApiOperation(value = "API to update the AD authentication providers", nickname = "updateAuthenticationProvider")
    @PutMapping(value = "v2/auth-provider-management/ad-auth-providers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<ADAuthProviderDto>> updateADAuthProvider(
            @RequestBody ADAuthProviderDto adAuthProviderDto) throws MangleException {
        log.debug("Received request to update authentication provider");
        ADAuthProviderDto persisted =
                adAuthProviderService.getADAuthProviderByAdDomain(adAuthProviderDto.getAdDomain());

        preValidateADUpdate(adAuthProviderDto, persisted);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                adAuthProviderDto.getAdUser() + "@" + adAuthProviderDto.getAdDomain(),
                adAuthProviderDto.getAdUserPassword());
        adAuthProvider.authenticate(authentication);
        persisted = handleADUpdate(adAuthProviderDto);

        Resource<ADAuthProviderDto> authResource = new Resource<>(persisted);

        authResource.add(getAuthGetHateoasLink(), getAuthAddHateoasLink(), getAuthDeleteHateoasLink(), getSelfLink());

        return new ResponseEntity<>(authResource, HttpStatus.OK);

    }

    /**
     * Allows to configure new AD provider to the application
     *
     * exception: 1. when entry to be added already doesADAuthExists 2. when test connection to the
     * AD server fails
     *
     * @Param ADAuthProviderDto: ADAuthProviderDto instance user provides
     *
     * @deprecated
     *
     */
    @Deprecated
    @ApiOperation(value = "API to add the AD authentication provider", nickname = "addAuthenticationProvider")
    @PostMapping(value = "v1/auth-provider-management/ad-auth-providers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<ADAuthProviderDto>> addADAuthProvider(
            @RequestBody ADAuthProviderDtoV1 adAuthProviderDtoV1) throws MangleException {
        log.debug("Received request to add authentication provider");
        ADAuthProviderDto adAuthProviderDto = mappingService.map(adAuthProviderDtoV1, ADAuthProviderDto.class);
        /*
         * Check if there is an entry in the database to update
         * exception if there isn't one with the id
         * */
        preValidateADAddition(adAuthProviderDto);

        return handleADAddition(adAuthProviderDto);
    }

    /**
     * Allows to configure new authentication provider
     *
     * exception: 1. when entry to be added already doesADAuthExists 2. when test connection to the
     * AD server fails
     *
     * @Param ADAuthProviderDto: ADAuthProviderDto instance user provides
     *
     */
    @ApiOperation(value = "API to add the AD authentication provider", nickname = "addAuthenticationProvider")
    @PostMapping(value = "v2/auth-provider-management/ad-auth-providers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<ADAuthProviderDto>> addADAuthProvider(
            @RequestBody ADAuthProviderDto adAuthProviderDto) throws MangleException {
        log.debug("Received request to add authentication provider");
        /*
         * Check if there is an entry in the database to update
         * exception if there isn't one with the id
         * */
        preValidateADAddition(adAuthProviderDto);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                adAuthProviderDto.getAdUser() + "@" + adAuthProviderDto.getAdDomain(),
                adAuthProviderDto.getAdUserPassword());
        try {
            adAuthProvider.authenticate(authentication);
        } catch (BadCredentialsException e) {
            throw new MangleException(ErrorConstants.AD_UPDATE_FAILED_BAD_CREDS, ErrorCode.AD_UPDATE_FAILED_BAD_CREDS);
        }

        return handleADAddition(adAuthProviderDto);
    }

    /**
     * Allows to remove configured auth providers from the application
     *
     * @Param authProviderIds: List of IDs that are to be removed
     */
    @ApiOperation(value = "API to delete the AD authentication provider", nickname = "addAuthenticationProvider")
    @DeleteMapping(value = "v1/auth-provider-management/ad-auth-providers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ErrorDetails> removeADAuthProvider(@RequestParam List<String> domainNames)
            throws MangleException {
        log.debug(String.format("Received request to remove AD Authentication Providers: %s", domainNames.toString()));

        DeleteOperationResponse response = authSourceDeletionService.deleteAuthSourceByAuthProviderNames(domainNames);
        for (String domainName : domainNames) {
            adAuthProvider.triggerMultiNodeResync(domainName);
        }
        ErrorDetails errorDetails = new ErrorDetails();
        if ((response.getAssociations().isEmpty())) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            Map<String, Map<String, List<String>>> associations = new HashMap<>();
            associations.put("associations", response.getAssociations());
            errorDetails.setTimestamp(new Date());
            errorDetails.setDescription(response.getResponseMessage());
            errorDetails.setCode(ErrorCode.DELETE_OPERATION_FAILED.getCode());
            errorDetails.setDetails(associations);
        }
        return new ResponseEntity<>(errorDetails, HttpStatus.PRECONDITION_FAILED);
    }


    @ApiOperation(value = "API to get all the domains contained in the application", nickname = "retrieveAllDomains")
    @GetMapping(value = "v1/auth-provider-management/domains", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resources<String>> getAllDomains() throws MangleException {
        log.debug("Received request to retrieve all Authentication Provider Domains.");
        Set<String> domains = new HashSet<>(adAuthProviderService.getAllDomains());
        domains.add(userService.getDefaultDomainName());

        Resources<String> resources = new Resources<>(domains);
        resources.add(getAuthGetHateoasLink(), getAuthAddHateoasLink(), getAuthDeleteHateoasLink(),
                getAuthUpdateHateoasLink(), getSelfLink());

        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @ApiOperation(value = "API to test connection to the given AD details", nickname = "testConnection")
    @PostMapping(value = "v1/auth-provider-management/test-connection", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Boolean>> testConnection(@RequestBody ADAuthProviderDto adAuthProviderDto)
            throws MangleException {
        log.debug("Received request to test connection to the AD with URL {} and domain {}",
                adAuthProviderDto.getAdUrl(), adAuthProviderDto.getAdDomain());

        adAuthProvider.testConnection(adAuthProviderDto);

        Resource<Boolean> resource = new Resource<>(true);
        Link link = linkTo(methodOn(AuthProviderController.class).testConnection(adAuthProviderDto)).withSelfRel();
        resource.add(link);
        resource.add(link, getAuthGetHateoasLink(), getAuthAddHateoasLink(), getAuthDeleteHateoasLink(),
                getAuthUpdateHateoasLink(), getAuthDomainGetHateoasLink());

        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    private void preValidateADAddition(ADAuthProviderDto adAuthProviderDto) throws MangleException {
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
    }

    private ResponseEntity<Resource<ADAuthProviderDto>> handleADAddition(ADAuthProviderDto adAuthProviderDto)
            throws MangleException {
        ADAuthProviderDto persistedADAuthProviderDto = adAuthProviderService.addADAuthProvider(adAuthProviderDto);

        adAuthProvider.triggerMultiNodeResync(persistedADAuthProviderDto.getAdDomain());
        Resource<ADAuthProviderDto> authResource = new Resource<>(persistedADAuthProviderDto);
        authResource.add(getAuthGetHateoasLink(), getSelfLink(), getAuthDeleteHateoasLink(),
                getAuthUpdateHateoasLink());

        return new ResponseEntity<>(authResource, HttpStatus.CREATED);
    }

    private void preValidateADUpdate(ADAuthProviderDto adAuthProviderDto, ADAuthProviderDto persisted)
            throws MangleException {
        /*
         * Check if there is an entry in the database to update
         * exception if there isn't one with the id
         * */
        if (persisted == null) {
            log.error("Authprovider instance to be updated doesn't exist, failed to update");
            throw new MangleException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.AD_AUTHPROVIDER_ID,
                    adAuthProviderDto.getAdDomain());
        }
    }

    private ADAuthProviderDto handleADUpdate(ADAuthProviderDto adAuthProviderDto) throws MangleException {
        boolean isSuccessfulAdded =
                adAuthProvider.setAdAuthProvider(adAuthProviderDto.getAdUrl(), adAuthProviderDto.getAdDomain());


        /* throw exception if the connection to the AD failed*/
        if (!isSuccessfulAdded) {
            log.error(
                    "Authentication Provider configuration failed. Reason: Failed to Connect to the Authentication Provider");
            throw new MangleException(ErrorConstants.AUTHENTICATION_TEST_CONNECTION_FAILED,
                    ErrorCode.AUTH_TEST_CONNECTION_FAILED);
        }

        ADAuthProviderDto persisted = adAuthProviderService.updateADAuthProvider(adAuthProviderDto);
        adAuthProvider.triggerMultiNodeResync(persisted.getAdDomain());
        return persisted;
    }

    public Link getSelfLink() {
        return new Link(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri().toASCIIString())
                .withSelfRel();
    }

    private Link getAuthAddHateoasLink() throws MangleException {
        return linkTo(methodOn(AuthProviderController.class).addADAuthProvider(new ADAuthProviderDto())).withRel("ADD");
    }

    private Link getAuthUpdateHateoasLink() throws MangleException {
        return linkTo(methodOn(AuthProviderController.class).updateADAuthProvider(new ADAuthProviderDto()))
                .withRel("UPDATE");
    }

    private Link getAuthDeleteHateoasLink() throws MangleException {
        return linkTo(methodOn(AuthProviderController.class).removeADAuthProvider(new LinkedList<>()))
                .withRel("DELETE");
    }

    private Link getAuthGetHateoasLink() throws MangleException {
        return linkTo(methodOn(AuthProviderController.class).getAllADAuthProviders()).withRel("GET");
    }

    private Link getAuthDomainGetHateoasLink() throws MangleException {
        return linkTo(methodOn(AuthProviderController.class).getAllDomains()).withRel("GET-DOMAINS");
    }
}
