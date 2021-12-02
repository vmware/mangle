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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.vmware.mangle.cassandra.model.endpoint.AWSCredentials;
import com.vmware.mangle.cassandra.model.endpoint.AzureCredentials;
import com.vmware.mangle.cassandra.model.endpoint.CertificatesSpec;
import com.vmware.mangle.cassandra.model.endpoint.CredentialsSpec;
import com.vmware.mangle.cassandra.model.endpoint.DatabaseCredentials;
import com.vmware.mangle.cassandra.model.endpoint.DockerCertificates;
import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.endpoint.EndpointSpecV1;
import com.vmware.mangle.cassandra.model.endpoint.K8SCredentials;
import com.vmware.mangle.cassandra.model.endpoint.RemoteMachineCredentials;
import com.vmware.mangle.cassandra.model.endpoint.VCenterCredentials;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.model.enums.HateoasOperations;
import com.vmware.mangle.model.response.DeleteEndpointOperationResponse;
import com.vmware.mangle.model.response.DeleteOperationResponse;
import com.vmware.mangle.model.response.ErrorDetails;
import com.vmware.mangle.services.CredentialService;
import com.vmware.mangle.services.EndpointCertificatesService;
import com.vmware.mangle.services.EndpointService;
import com.vmware.mangle.services.cassandra.model.events.basic.EntityCreatedEvent;
import com.vmware.mangle.services.cassandra.model.events.basic.EntityUpdatedEvent;
import com.vmware.mangle.services.constants.CommonConstants;
import com.vmware.mangle.services.deletionutils.CredentialDeletionService;
import com.vmware.mangle.services.deletionutils.EndpointCertificatesDeletionService;
import com.vmware.mangle.services.deletionutils.EndpointDeletionService;
import com.vmware.mangle.services.enums.K8SResource;
import com.vmware.mangle.services.events.web.CustomEventPublisher;
import com.vmware.mangle.services.updateutils.EndpointUpdateService;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Controller class for Endpoint Service.
 *
 * @author kumargautam
 */
@RestController
@RequestMapping("/rest/api/")
@Api("/rest/api/")
@Log4j2
@SuppressWarnings("squid:S00107")
public class EndpointController {

    private static final int MAX_KUBECONFIG_FILE_SIZE = 20 * 1024;
    private static final int MAX_PRIVATEKEY_FILE_SIZE = 5 * 1024;
    private static final String ASSCOCIATIONS = "associations";
    private EndpointService endpointService;
    private CredentialService credentialService;
    private CustomEventPublisher publisher;
    private EndpointDeletionService endpointDeletionService;
    private EndpointUpdateService endpointUpdateService;
    private CredentialDeletionService credentialDeletionService;
    private EndpointCertificatesService certificatesService;
    private EndpointCertificatesDeletionService certificatesDeletionService;

    @Autowired
    public EndpointController(EndpointService endpointService, CredentialService credentialService,
            CustomEventPublisher publisher, EndpointDeletionService endpointDeletionService,
            CredentialDeletionService credentialDeletionService, EndpointCertificatesService certificatesService,
            EndpointCertificatesDeletionService certificatesDeletionService,
            EndpointUpdateService endpointUpdateService) {
        this.credentialService = credentialService;
        this.endpointService = endpointService;
        this.publisher = publisher;
        this.endpointDeletionService = endpointDeletionService;
        this.credentialDeletionService = credentialDeletionService;
        this.certificatesService = certificatesService;
        this.certificatesDeletionService = certificatesDeletionService;
        this.endpointUpdateService = endpointUpdateService;
    }

    @ApiOperation(value = "API to get all the endpoints or filter the endpoints by credentials", nickname = "getEndPoints")
    @GetMapping(value = "v1/endpoints", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resources<EndpointSpec>> getEndpoints(
            @RequestParam(value = "credentialName", required = false) String credentialName) throws MangleException {
        log.info("Start execution of getEndpoints() method");
        List<EndpointSpec> endPoints;

        if (StringUtils.isEmpty(credentialName)) {
            endPoints = endpointService.getAllEndpoints();
        } else {
            endPoints = endpointService.getEndpointsSpecByCredentialName(credentialName);
        }

        Resources<EndpointSpec> resources = new Resources<>(endPoints);
        resources.add(getSelfLink(), getHateoasLinkForAddEndpoint(), getHateoasLinkForEnableEndpoint(),
                getHateoasLinkForUpdateEndpoint(), getHateoasLinkForGetEndpoints(),
                getHateoasLinkForGetEndpointByName(), getHateoasLinkForGetEndpointByType(),
                getHateoasLinkForDeleteEndpoints());

        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.ENDPOINTS_RESULT_FOUND);
        return new ResponseEntity<>(resources, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to get all the endpoints by endpointName", nickname = "getEndpointByName")
    @GetMapping(value = "v1/endpoints/{endpointName:.+}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Resource<EndpointSpec>> getEndpointByName(@PathVariable("endpointName") String endpointName)
            throws MangleException {
        log.info("Start execution of getEndpointByName() method");
        EndpointSpec endPoint = endpointService.getEndpointByName(endpointName);

        Resource<EndpointSpec> resource = new Resource<>(endPoint);
        resource.add(getSelfLink(), getHateoasLinkForAddEndpoint(), getHateoasLinkForEnableEndpoint(),
                getHateoasLinkForUpdateEndpoint(), getHateoasLinkForGetEndpoints(),
                getHateoasLinkForGetEndpointByType(), getHateoasLinkForDeleteEndpoints());

        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.ENDPOINTS_RESULT_FOUND);
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to enable or disable the endpoints for fault injections by endpointNames or endpoint tags", nickname = "enableEndpoint")
    @PostMapping(value = "v1/endpoints/enable", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Resources<String>> enableEndpoint(
            @RequestParam(value = "names", required = false) List<String> names,
            @RequestParam(value = "enable") Boolean enable, @RequestBody(required = false) Map<String, String> tags)
            throws MangleException {
        log.info("Start execution of enable() method");
        List<String> updatedEndpoints = endpointService.enableEndpoints(names, tags, enable);
        HttpHeaders headers = new HttpHeaders();
        if (enable) {
            headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.ENDPOINTS_ENABLED);
        } else {
            headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.ENDPOINTS_DISABLED);
        }

        Resources<String> resources = new Resources<>(updatedEndpoints);
        resources.add(getSelfLink(), getHateoasLinkForAddEndpoint(), getHateoasLinkForUpdateEndpoint(),
                getHateoasLinkForGetEndpoints(), getHateoasLinkForGetEndpointByName(),
                getHateoasLinkForGetEndpointByType(), getHateoasLinkForDeleteEndpoints());

        return new ResponseEntity<>(resources, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to get all the endpoints by endPointType", nickname = "getAllEndpointByType")
    @GetMapping(value = "v1/endpoints/type/{endPointType}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Resources<EndpointSpec>> getAllEndpointByType(
            @ApiParam(value = "Select endPointType") @PathVariable("endPointType") EndpointType endPointType)
            throws MangleException {
        log.info("Start execution of getAllEndpointByType() method");
        List<EndpointSpec> endPoint = endpointService.getAllEndpointByType(endPointType);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.ENDPOINTS_RESULT_FOUND);

        Resources<EndpointSpec> resources = new Resources<>(endPoint);
        resources.add(getSelfLink(), getHateoasLinkForAddEndpoint(), getHateoasLinkForEnableEndpoint(),
                getHateoasLinkForUpdateEndpoint(), getHateoasLinkForGetEndpoints(),
                getHateoasLinkForGetEndpointByName(), getHateoasLinkForDeleteEndpoints());

        return new ResponseEntity<>(resources, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to get all the containers of dockerHost based on DockerEndpointName", nickname = "getAllDockerContainersByEndpoint")
    @GetMapping(value = "v1/endpoints/docker/containers/{endPointName:.+}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Resources<String>> getAllDockerContainersByEndpoint(
            @ApiParam(value = "Select endPointName") @PathVariable("endPointName") String endpointName)
            throws MangleException {
        log.info("Start execution of getAllDockerContainersByEndpoint() method");
        List<String> containers = endpointService.getAllContainersByEndpointName(endpointName);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.CONTAINERS_RESULT_FOUND);

        Resources<String> resources = new Resources<>(containers);
        resources.add(getSelfLink(), getHateoasLinkForAddEndpoint(), getHateoasLinkForEnableEndpoint(),
                getHateoasLinkForUpdateEndpoint(), getHateoasLinkForGetEndpoints(),
                getHateoasLinkForGetEndpointByName(), getHateoasLinkForGetEndpointByType(),
                getHateoasLinkForDeleteEndpoints());

        return new ResponseEntity<>(resources, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to get all the resources of kubernetes cluster based on K8sEndpointName and resourceType", nickname = "getAllK8sResourcesByEndpoint")
    @GetMapping(value = "v1/endpoints/k8s/resources/", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Resources<String>> getAllK8sResourcesByEndpointName(
            @RequestParam("endpointName") String endpointName, @RequestParam("resourceType") K8SResource resourceType)
            throws MangleException {
        if (resourceType.equals(K8SResource.POD)) {
            throw new MangleException(ErrorConstants.PODS_NOT_SUPPORTED, ErrorCode.PODS_NOT_SUPPORTED);
        }
        log.info("Retrieving all resources of type " + resourceType + " from K8s endpoint " + endpointName);
        List<String> k8sResources = endpointService.getAllResourcesByEndpointName(endpointName, resourceType);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.RESOURCES_RESULT_FOUND + resourceType);

        Resources<String> resources = new Resources<>(k8sResources);
        resources.add(getSelfLink(), getHateoasLinkForAddEndpoint(), getHateoasLinkForEnableEndpoint(),
                getHateoasLinkForUpdateEndpoint(), getHateoasLinkForGetEndpoints(),
                getHateoasLinkForGetEndpointByName(), getHateoasLinkForGetEndpointByType(),
                getHateoasLinkForDeleteEndpoints());

        return new ResponseEntity<>(resources, headers, HttpStatus.OK);
    }


    @ApiOperation(value = "API to update a existing endpoint", nickname = "updateEndPoint")
    @PutMapping(value = "v2/endpoints", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<EndpointSpec>> updateEndpoint(@Validated @RequestBody EndpointSpec endpointSpec)
            throws MangleException {
        log.info("Start execution of updateEndpoint() method");
        preProcessEndpointSpec(endpointSpec);
        return handleEndpointUpdate(endpointSpec);
    }

    /**
     * @deprecated (since = "3.0.0", "to support the association of vCenter adapter instead of
     *             composition", forRemoval=true)
     * @param endpointSpecV1
     * @return
     * @throws MangleException
     */
    @Deprecated
    @ApiOperation(value = "API to update a existing endpoint", nickname = "updateEndPoint")
    @PutMapping(value = "v1/endpoints", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<EndpointSpecV1>> updateEndpoint(
            @Validated @RequestBody EndpointSpecV1 endpointSpecV1) throws MangleException {
        log.info("Start execution of updateEndpoint() method");
        EndpointSpec endpointSpec = endpointService.getV1EndpointToEndpointSpec(endpointSpecV1);
        ResponseEntity<Resource<EndpointSpec>> endpointSpecResource = handleEndpointUpdate(endpointSpec);
        Resource<EndpointSpecV1> resource = new Resource<>(endpointSpecV1);
        resource.add(Objects.requireNonNull(endpointSpecResource.getBody()).getLinks());
        return new ResponseEntity<>(resource, endpointSpecResource.getHeaders(), HttpStatus.OK);
    }

    /**
     * @deprecated (since = "3.0.0", "to support the association of vCenter adapter instead of
     *             composition", forRemoval=true)
     * @param endpointSpecV1
     * @return
     * @throws MangleException
     */
    @ApiOperation(value = "API to add a new endpoint", nickname = "addEndPoint")
    @Deprecated
    @PostMapping(value = "v1/endpoints", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<EndpointSpecV1>> addEndpoint(@Validated @RequestBody EndpointSpecV1 endpointSpecV1)
            throws MangleException {
        log.info("Start execution of addEndpoint() method");

        EndpointSpec endpointSpec = endpointService.getV1EndpointToEndpointSpec(endpointSpecV1);
        ResponseEntity<Resource<EndpointSpec>> endpointSpecResource = handleEndpointAddition(endpointSpec);
        Resource<EndpointSpecV1> resource = new Resource<>(endpointSpecV1);
        resource.add(Objects.requireNonNull(endpointSpecResource.getBody()).getLinks());
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * @param endpointSpec
     * @return
     * @throws MangleException
     */
    @ApiOperation(value = "API to add a new endpoint", nickname = "addEndPoint")
    @PostMapping(value = "v2/endpoints", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<EndpointSpec>> addEndpoint(@Validated @RequestBody EndpointSpec endpointSpec)
            throws MangleException {
        EndpointSpec endpointSpecPersisted = preProcessEndpointSpec(endpointSpec);

        return handleEndpointAddition(endpointSpecPersisted);
    }

    @ApiOperation(value = "API to get an endpoint credentials", nickname = "getCredentials")
    @GetMapping(value = "v1/endpoints/credentials", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resources<CredentialsSpec>> getCredentials() throws MangleException {
        log.info("Start execution of getCredentials() method");
        List<CredentialsSpec> credentialsSpec = credentialService.getAllCredentials();
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.CREDENTIALS_RESULT_FOUND);

        Resources<CredentialsSpec> resources = new Resources<>(credentialsSpec);
        resources.add(getSelfLink(), getHateoasLinkForDeleteCredentials());

        return new ResponseEntity<>(resources, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to add RemoteMachine endpoint credentials", nickname = "addRemoteMachineCredentials")
    @PostMapping(value = "v1/endpoints/credentials/remotemachine", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<CredentialsSpec>> addCredentials(
            @RequestParam(name = "id", required = false) String id, @RequestParam("name") String name,
            @RequestParam("username") String username,
            @RequestParam(name = "password", required = false) String password,
            @RequestParam(name = "privateKey", required = false) MultipartFile privateKey) throws MangleException {
        log.info("Start execution of addCredentials(remoteMachineCredentials) method for RemoteMachine");
        credentialService.validatePasswordOrPrivateKeyNotNull(password, privateKey);
        credentialService.validateMultipartFileSize(privateKey, MAX_PRIVATEKEY_FILE_SIZE);
        String privateKeyOriginalFileName = null;
        if (null != privateKey) {
            certificatesService.validateRemoteMachinePrivateKey(privateKey);
            privateKeyOriginalFileName = privateKey.getOriginalFilename();
        }
        try {
            RemoteMachineCredentials remoteMachineCredentials =
                    credentialService.generateRemoteMachineCredentialsSpec(id, name, username, password, privateKey);

            return addCredentials(remoteMachineCredentials);
        } catch (IOException e) {
            throw new MangleException(e, ErrorCode.FILE_SIZE_EXCEEDED, privateKeyOriginalFileName);

        }
    }

    @ApiOperation(value = "API to add a K8S_CLUSTER endpoint credentials", nickname = "addK8SCredentials")
    @PostMapping(value = "v1/endpoints/credentials/k8s", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<CredentialsSpec>> addCredentials(
            @RequestParam(name = "id", required = false) String id,
            @ApiParam(value = "Mandatory: Only if mangle is not deployed in same k8s cluster") @RequestParam(name = "kubeConfig", required = false) MultipartFile kubeConfig,
            @RequestParam("name") String name) throws MangleException {
        log.info("Start execution of addCredentials(k8sCredentials) method for K8S_CLUSTER");
        credentialService.validateMultipartFileSize(kubeConfig, MAX_KUBECONFIG_FILE_SIZE);
        credentialService.validateK8SConfigFile(kubeConfig);
        try {
            K8SCredentials k8sCredentials = credentialService.generateK8SCredentialsSpec(id, name, kubeConfig);
            log.info("kubeConfig : " + kubeConfig);
            return addCredentials(k8sCredentials);
        } catch (IOException e) {
            throw new MangleException(e, ErrorCode.FILE_SIZE_EXCEEDED, kubeConfig.getOriginalFilename());
        }
    }

    @ApiOperation(value = "API to add a AWS endpoint credentials", nickname = "addAWSCredentials")
    @PostMapping(value = "v1/endpoints/credentials/aws", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<CredentialsSpec>> addCredentials(@RequestBody AWSCredentials awsCredentials)
            throws MangleException {
        log.info("Start execution of addCredentials() method for AWS");
        return addCredentials((CredentialsSpec) awsCredentials);
    }

    @ApiOperation(value = "API to add a Azure endpoint credentials", nickname = "addAzureCredentials")
    @PostMapping(value = "v1/endpoints/credentials/azure", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CredentialsSpec> addAzureCredentials(@RequestBody AzureCredentials azureCredentials)
            throws MangleException {
        log.info("Start execution of addCredentials() method for Azure");
        CredentialsSpec credentialsSpec = credentialService.addOrUpdateCredentials(azureCredentials);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.CREDENTIALS_CREATED);
        publisher.publishAnEvent(
                new EntityCreatedEvent(credentialsSpec.getPrimaryKey(), credentialsSpec.getClass().getName()));
        return new ResponseEntity<>(credentialsSpec, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to add a Vcenter endpoint credentials", nickname = "addVCenterCredentials")
    @PostMapping(value = "v1/endpoints/credentials/vcenter", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<CredentialsSpec>> addCredentials(
            @Validated @RequestBody VCenterCredentials vcenterCredentials) throws MangleException {
        log.info("Start execution of addCredentials(vcenterCredentials) method for Vcenter");
        return addCredentials((CredentialsSpec) vcenterCredentials);
    }

    @ApiOperation(value = "API to add a Database endpoint credentials", nickname = "addDatabaseCredentials")
    @PostMapping(value = "v1/endpoints/credentials/database", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<CredentialsSpec>> addDatabaseCredentials(
            @Validated @RequestBody DatabaseCredentials databaseCredentials) throws MangleException {
        log.info("Start execution of addDatabaseCredentials() method");
        CredentialsSpec credentialsSpec = credentialService.addOrUpdateCredentials(databaseCredentials);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.CREDENTIALS_CREATED);
        publisher.publishAnEvent(
                new EntityCreatedEvent(credentialsSpec.getPrimaryKey(), credentialsSpec.getClass().getName()));
        Resource<CredentialsSpec> resource = new Resource<>(credentialsSpec);
        resource.add(getSelfLink(), getHateoasLinkForGetCredentials(), getHateoasLinkForDeleteCredentials());
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    private ResponseEntity<Resource<CredentialsSpec>> addCredentials(CredentialsSpec credentialsSpec)
            throws MangleException {
        CredentialsSpec persistedCredentialsSpec = credentialService.addOrUpdateCredentials(credentialsSpec);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.CREDENTIALS_CREATED);
        publisher.publishAnEvent(
                new EntityCreatedEvent(credentialsSpec.getPrimaryKey(), credentialsSpec.getClass().getName()));
        Resource<CredentialsSpec> resource = new Resource<>(persistedCredentialsSpec);
        resource.add(getSelfLink(), getHateoasLinkForGetCredentials(), getHateoasLinkForDeleteCredentials());
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to delete the endpoints by names", nickname = "deleteEndPoints")
    @DeleteMapping(value = "v1/endpoints", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ErrorDetails> deleteEndpointsByNames(@RequestParam List<String> endpointNames)
            throws MangleException {
        log.info("Start execution of deleteEndpointsByNames() method");
        DeleteEndpointOperationResponse response = endpointDeletionService.deleteEndpointByNames(endpointNames);
        ErrorDetails errorDetails = new ErrorDetails();
        if (response.getAssociations().size() == 0 && response.getEndpointGroupAssociations().size() == 0
                && response.getEndpointChildAssociations().size() == 0) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            Map<String, Map<String, List<String>>> associations = new HashMap<>();
            errorDetails.setTimestamp(new Date());
            errorDetails.setCode(ErrorCode.DELETE_OPERATION_FAILED.getCode());
            if (!CollectionUtils.isEmpty(response.getEndpointGroupAssociations())) {
                associations.put(ASSCOCIATIONS, response.getEndpointGroupAssociations());
                errorDetails.setDescription(ErrorConstants.ENDPOINTS_DELETION_PRECHECK_FAIL_WITH_ENDPOINTGROUPS);
            } else if (!CollectionUtils.isEmpty(response.getEndpointChildAssociations())) {
                associations.put(ASSCOCIATIONS, response.getEndpointChildAssociations());
                errorDetails.setDescription(String.format(ErrorConstants.ENDPOINTS_DELETION_PRECHECK_FAIL_WITH_DATABASE,
                        response.getEndpointChildAssociations()));
            } else {
                associations.put(ASSCOCIATIONS, response.getAssociations());
                errorDetails.setDescription(ErrorConstants.ENDPOINTS_DELETION_PRECHECK_FAIL);
            }
            errorDetails.setDetails(associations);
        }
        return new ResponseEntity<>(errorDetails, HttpStatus.PRECONDITION_FAILED);
    }

    /**
     * The credentials deletion operation expects that there exists no endpoints to credentials
     * associations for any of the credential that are part of the list given that are to be deleted
     *
     * @param credentialNames
     *            list of the credentials that are to be deleted
     *
     * @return DeleteOperationResponse with the result as success, if no active association exists
     *         else failed with the list of the credentials to endpoint mapping
     *
     * @throws MangleException
     */
    @ApiOperation(value = "API to delete an endpoint credentials", nickname = "deleteCredentials")
    @DeleteMapping(value = "v1/endpoints/credentials", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ErrorDetails> deleteCredentials(@RequestParam List<String> credentialNames)
            throws MangleException {
        log.info("Start execution of deleteCredentials() method");
        DeleteOperationResponse response = credentialDeletionService.deleteCredentialsByNames(credentialNames);
        ErrorDetails errorDetails = new ErrorDetails();
        if (response.getAssociations().size() == 0) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            Map<String, Map<String, List<String>>> associations = new HashMap<>();
            associations.put(ASSCOCIATIONS, response.getAssociations());
            errorDetails.setTimestamp(new Date());
            errorDetails.setDescription(ErrorConstants.CREDENTIALS_DELETION_PRECHECK_FAIL);
            errorDetails.setCode(ErrorCode.DELETE_OPERATION_FAILED.getCode());
            errorDetails.setDetails(associations);
        }

        return new ResponseEntity<>(errorDetails, HttpStatus.PRECONDITION_FAILED);
    }

    @ApiOperation(value = "API to get all the endpoints based on page", nickname = "getEndpointsBasedOnPage")
    @GetMapping(value = "v1/endpoints/page", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<EndpointSpec>> getEndpointsBasedOnPage(
            @RequestParam(value = "pageNo", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "4") int size) {
        log.info("Start execution of getEndpointsBasedOnPage() method");
        Slice<EndpointSpec> result = endpointService.getEndpointBasedOnPage(page, size);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.ENDPOINTS_RESULT_FOUND);
        headers.add("totalPage", String.valueOf(endpointService.getTotalPages(result)));
        return new ResponseEntity<>(result.getContent(), headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to get all the credentials based on page", nickname = "getCredentialsBasedOnPage")
    @GetMapping(value = "v1/endpoints/credentials/page", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<CredentialsSpec>> getCredentialsBasedOnPage(
            @RequestParam(value = "pageNo", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "4") int size) {
        log.info("Start execution of getCredentialsBasedOnPage() method");
        Slice<CredentialsSpec> result = credentialService.getCredentialsBasedOnPage(page, size);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.CREDENTIALS_RESULT_FOUND);
        headers.add("totalPage", String.valueOf(credentialService.getTotalPages(result)));
        return new ResponseEntity<>(result.getContent(), headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to test the endpoint connection", nickname = "testEndPointConnection")
    @PostMapping(value = "v1/endpoints/testConnection", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<EndpointSpec>> testConnection(@RequestParam String endpointName)
            throws MangleException {
        log.info("Start execution of testConnection() method");
        EndpointSpec endpointSpec = endpointService.getEndpointByName(endpointName);
        return testConnection(endpointSpec);
    }

    @ApiOperation(value = "API to test the endpoint connection", nickname = "testEndPointConnection", hidden = true)
    @PostMapping(value = "v1/endpoints/testEndpoint", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<EndpointSpec>> endpointTestConnection(
            @Validated @RequestBody EndpointSpec endpointSpec) throws MangleException {
        log.info("Start execution of endpointTestConnection() method");
        return testConnection(endpointSpec);
    }

    private ResponseEntity<Resource<EndpointSpec>> testConnection(EndpointSpec endpointSpec) throws MangleException {
        EndpointSpec endpointSpecPersisted = preProcessEndpointSpec(endpointSpec);
        if (!endpointService.testEndpointConnection(endpointSpecPersisted)) {
            throw new MangleException(ErrorConstants.TEST_CONNECTION_FAILED, ErrorCode.TEST_CONNECTION_FAILED,
                    endpointSpec.getName());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.TEST_CONNECTION_SUCCESS);

        Resource<EndpointSpec> resource = new Resource<>(endpointSpec);
        resource.add(getSelfLink(), getHateoasLinkForAddEndpoint(), getHateoasLinkForEnableEndpoint(),
                getHateoasLinkForGetEndpoints(), getHateoasLinkForGetEndpointByName(),
                getHateoasLinkForGetEndpointByType(), getHateoasLinkForDeleteEndpoints());

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to update RemoteMachine endpoint credentials", nickname = "updateRemoteMachineCredentials")
    @PutMapping(value = "v1/endpoints/credentials/remotemachine", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CredentialsSpec> updateCredentials(@RequestParam(name = "id", required = false) String id,
            @RequestParam("name") String name, @RequestParam("username") String username,
            @RequestParam(name = "password", required = false) String password,
            @RequestParam(name = "privateKey", required = false) MultipartFile privateKey) throws MangleException {
        log.info("Start execution of addCredentials() method for RemoteMachine");
        credentialService.validatePasswordOrPrivateKeyNotNull(password, privateKey);
        credentialService.validateMultipartFileSize(privateKey, MAX_PRIVATEKEY_FILE_SIZE);
        String privateKeyOriginalFileName = null;
        if (null != privateKey) {
            certificatesService.validateRemoteMachinePrivateKey(privateKey);
            privateKeyOriginalFileName = privateKey.getOriginalFilename();
        }
        try {
            RemoteMachineCredentials remoteMachineCredentials =
                    credentialService.generateRemoteMachineCredentialsSpec(id, name, username, password, privateKey);
            CredentialsSpec credentialsSpec = credentialService.updateCredential(remoteMachineCredentials);
            publisher.publishAnEvent(
                    new EntityUpdatedEvent(credentialsSpec.getPrimaryKey(), credentialsSpec.getClass().getName()));
            return new ResponseEntity<>(credentialsSpec, HttpStatus.OK);
        } catch (IOException e) {
            throw new MangleException(e, ErrorCode.FILE_SIZE_EXCEEDED, privateKeyOriginalFileName);

        }
    }

    @ApiOperation(value = "API to update a K8S_CLUSTER endpoint credentials", nickname = "updateK8SCredentials")
    @PutMapping(value = "v1/endpoints/credentials/k8s", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CredentialsSpec> updateCredentials(@RequestParam(name = "id", required = false) String id,
            @RequestParam(name = "kubeConfig", required = false) MultipartFile kubeConfig,
            @RequestParam("name") String name) throws MangleException {
        log.info("Start execution of updateCredentials(k8sCredentials) method for K8S_CLUSTER");
        credentialService.validateMultipartFileSize(kubeConfig, MAX_KUBECONFIG_FILE_SIZE);
        credentialService.validateK8SConfigFile(kubeConfig);
        try {
            K8SCredentials k8sCredentials = credentialService.generateK8SCredentialsSpec(id, name, kubeConfig);
            log.info("kubeConfig : " + kubeConfig);
            CredentialsSpec credentialsSpec = credentialService.updateCredential(k8sCredentials);
            publisher.publishAnEvent(
                    new EntityUpdatedEvent(credentialsSpec.getPrimaryKey(), credentialsSpec.getClass().getName()));
            return new ResponseEntity<>(credentialsSpec, HttpStatus.OK);
        } catch (IOException e) {
            throw new MangleException(e, ErrorCode.FILE_SIZE_EXCEEDED, kubeConfig.getOriginalFilename());
        }
    }

    @ApiOperation(value = "API to update a Vcenter endpoint credentials", nickname = "updateVCenterCredentials")
    @PutMapping(value = "v1/endpoints/credentials/vcenter", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CredentialsSpec> updateCredentials(
            @Validated @RequestBody VCenterCredentials vcenterCredentials) throws MangleException {
        log.info("Start execution of updateCredentials(vcenterCredentials) method for Vcenter");
        CredentialsSpec credentialsSpec = credentialService.updateCredential(vcenterCredentials);
        publisher.publishAnEvent(
                new EntityUpdatedEvent(credentialsSpec.getPrimaryKey(), credentialsSpec.getClass().getName()));
        return new ResponseEntity<>(credentialsSpec, HttpStatus.OK);
    }

    @ApiOperation(value = "API to update a Database endpoint credentials", nickname = "updateDatabaseCredentials")
    @PutMapping(value = "v1/endpoints/credentials/database", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<CredentialsSpec>> updateDatabaseCredentials(
            @Validated @RequestBody DatabaseCredentials databaseCredentials) throws MangleException {
        log.info("Start execution of updateDatabaseCredentials(databaseCredentials) method");
        CredentialsSpec credentialsSpec = credentialService.updateCredential(databaseCredentials);
        publisher.publishAnEvent(
                new EntityUpdatedEvent(credentialsSpec.getPrimaryKey(), credentialsSpec.getClass().getName()));
        Resource<CredentialsSpec> resource = new Resource<>(credentialsSpec);
        resource.add(getSelfLink(), getHateoasLinkForGetCredentials(), getHateoasLinkForDeleteCredentials());
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    @ApiOperation(value = "API to get an endpoint certificates", nickname = "getAllEndpointCertificates")
    @GetMapping(value = "v1/endpoints/certificates", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resources<CertificatesSpec>> getAllEndpointCertificates() throws MangleException {
        log.info("Start execution of getAllEndpointCertificates() method");
        List<CertificatesSpec> certificatesSpec = certificatesService.getAllCertificates();
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.CERTIFICATES_RESULT_FOUND);

        Resources<CertificatesSpec> resources = new Resources<>(certificatesSpec);
        resources.add(getSelfLink(), getHateoasLinkForAddCertificate(), getHateoasLinkForDeleteCertificate(),
                getHateoasLinkForUpdateCertificate(), getHateoasLinkForGetCertificates());
        return new ResponseEntity<>(resources, headers, HttpStatus.OK);
    }

    /**
     * The certificates deletion operation expects that there exists no endpoints to credentials
     * associations for any of the certificates that are part of the list given that are to be
     * deleted
     *
     * @param certificatesNames
     *            list of the certificates that are to be deleted
     *
     * @return DeleteOperationResponse with the result as success, if no active association exists
     *         else failed with the list of the certificates to endpoint mapping
     *
     * @throws MangleException
     */
    @ApiOperation(value = "API to delete an endpoint certificates", nickname = "deleteCertificates")
    @DeleteMapping(value = "v1/endpoints/certificates", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ErrorDetails> deleteCertificates(@RequestParam List<String> certificatesNames)
            throws MangleException {
        log.info("Start execution of deleteCertificates() method");
        DeleteOperationResponse response = certificatesDeletionService.deleteCertificatesByNames(certificatesNames);
        ErrorDetails errorDetails = new ErrorDetails();
        if (response.getAssociations().size() == 0) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            Map<String, Map<String, List<String>>> associations = new HashMap<>();
            associations.put(ASSCOCIATIONS, response.getAssociations());
            errorDetails.setTimestamp(new Date());
            errorDetails.setDescription(ErrorConstants.CERTIFICATES_DELETION_PRECHECK_FAIL);
            errorDetails.setCode(ErrorCode.DELETE_OPERATION_FAILED.getCode());
            errorDetails.setDetails(associations);
        }
        return new ResponseEntity<>(errorDetails, HttpStatus.PRECONDITION_FAILED);
    }

    @ApiOperation(value = "API to add docker endpoint certificates", nickname = "addDockerEndpointCertificates")
    @PostMapping(value = "v1/endpoints/certificates/docker", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<CertificatesSpec>> addDockerEndpointCertificates(
            @RequestParam(name = "id", required = false) String id, @RequestParam("name") String name,
            @RequestParam(name = "caCert", required = false) MultipartFile caCert,
            @RequestParam(name = "serverCert", required = false) MultipartFile serverCert,
            @RequestParam(name = "privateKey", required = false) MultipartFile privateKey) throws MangleException {
        log.info("Start execution of addDockerEndpointCertificates() method for Docker");
        try {
            certificatesService.validateDockerCertificates(caCert.getBytes(), serverCert.getBytes(),
                    privateKey.getBytes());
            DockerCertificates dockerCertificates =
                    certificatesService.generateDockerCertificatesSpec(id, name, caCert, serverCert, privateKey);
            CertificatesSpec certificatesSpec = certificatesService.addOrUpdateCertificates(dockerCertificates);
            HttpHeaders headers = new HttpHeaders();
            headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.CERTIFICATES_CREATED);
            publisher.publishAnEvent(
                    new EntityCreatedEvent(certificatesSpec.getPrimaryKey(), certificatesSpec.getClass().getName()));

            Resource<CertificatesSpec> resource = new Resource<>(certificatesSpec);
            resource.add(getSelfLink(), getHateoasLinkForDeleteCertificate(), getHateoasLinkForUpdateCertificate(),
                    getHateoasLinkForGetCertificates());

            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new MangleException(e, ErrorCode.IO_EXCEPTION, e.getMessage());
        }
    }

    @ApiOperation(value = "API to update docker endpoint certificates", nickname = "updateDockerEndpointCertificates")
    @PutMapping(value = "v1/endpoints/certificates/docker", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<CertificatesSpec>> updateDockerEndpointCertificates(
            @RequestParam(name = "id", required = false) String id, @RequestParam("name") String name,
            @RequestParam(name = "caCert", required = false) MultipartFile caCert,
            @RequestParam(name = "serverCert", required = false) MultipartFile serverCert,
            @RequestParam(name = "privateKey", required = false) MultipartFile privateKey) throws MangleException {
        log.info("Start execution of updateDockerEndpointCertificates() method for Docker");
        try {
            certificatesService.validateDockerCertificates(caCert.getBytes(), serverCert.getBytes(),
                    privateKey.getBytes());
            DockerCertificates dockerCertificates =
                    certificatesService.generateDockerCertificatesSpec(id, name, caCert, serverCert, privateKey);
            CertificatesSpec certificatesSpec = certificatesService.updateCertificates(dockerCertificates);
            HttpHeaders headers = new HttpHeaders();
            headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.CERTIFICATES_CREATED);
            publisher.publishAnEvent(
                    new EntityCreatedEvent(certificatesSpec.getPrimaryKey(), certificatesSpec.getClass().getName()));

            Resource<CertificatesSpec> resource = new Resource<>(certificatesSpec);
            resource.add(getSelfLink(), getHateoasLinkForAddCertificate(), getHateoasLinkForDeleteCertificate(),
                    getHateoasLinkForGetCertificates());

            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new MangleException(e, ErrorCode.IO_EXCEPTION, e.getMessage());
        }
    }

    private EndpointSpec preProcessEndpointSpec(EndpointSpec endpointSpec) throws MangleException {
        if (endpointSpec.getEndPointType() == EndpointType.VCENTER
                && null != endpointSpec.getVCenterConnectionProperties()) {
            return endpointService.preProcessVCenterEndpointSpec(endpointSpec);
        }
        if (endpointSpec.getEndPointType() == EndpointType.DATABASE
                && null != endpointSpec.getDatabaseConnectionProperties()) {
            EndpointSpec parentEndpointSpec = endpointService.preProcessDatabaseEndpointSpec(endpointSpec);
            endpointSpec.getDatabaseConnectionProperties().setParentEndpointType(parentEndpointSpec.getEndPointType());
            if (!CollectionUtils.isEmpty(parentEndpointSpec.getTags())) {
                if (!CollectionUtils.isEmpty(endpointSpec.getTags())) {
                    endpointSpec.getTags().putAll(parentEndpointSpec.getTags());
                } else {
                    endpointSpec.setTags(parentEndpointSpec.getTags());
                }
            }
        }
        return endpointSpec;
    }

    private ResponseEntity<Resource<EndpointSpec>> handleEndpointAddition(EndpointSpec endpointSpec)
            throws MangleException {
        if (!endpointService.testEndpointConnection(endpointSpec)) {
            throw new MangleException(ErrorConstants.TEST_CONNECTION_FAILED, ErrorCode.TEST_CONNECTION_FAILED,
                    endpointSpec.getName());
        }
        EndpointSpec resEndpointSpec = endpointService.addOrUpdateEndpoint(endpointSpec);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.ENDPOINTS_CREATED);
        publisher.publishAnEvent(
                new EntityCreatedEvent(resEndpointSpec.getPrimaryKey(), resEndpointSpec.getClass().getName()));
        Resource<EndpointSpec> resource = new Resource<>(resEndpointSpec);
        resource.add(getSelfLink(), getHateoasLinkForEnableEndpoint(), getHateoasLinkForUpdateEndpoint(),
                getHateoasLinkForGetEndpoints(), getHateoasLinkForGetEndpointByName(),
                getHateoasLinkForGetEndpointByType(), getHateoasLinkForDeleteEndpoints());
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    private ResponseEntity<Resource<EndpointSpec>> handleEndpointUpdate(EndpointSpec endpointSpec)
            throws MangleException {
        if (!endpointService.testEndpointConnection(endpointSpec)) {
            throw new MangleException(ErrorCode.TEST_CONNECTION_FAILED, endpointSpec.getName());
        }
        EndpointSpec resEndpointSpec =
                endpointUpdateService.updateEndpointByEndpointName(endpointSpec.getName(), endpointSpec);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.ENDPOINTS_UPDATED);
        publisher.publishAnEvent(
                new EntityUpdatedEvent(resEndpointSpec.getPrimaryKey(), resEndpointSpec.getClass().getName()));
        Resource<EndpointSpec> resource = new Resource<>(resEndpointSpec);
        resource.add(getSelfLink(), getHateoasLinkForEnableEndpoint(), getHateoasLinkForAddEndpoint(),
                getHateoasLinkForGetEndpoints(), getHateoasLinkForGetEndpointByName(),
                getHateoasLinkForGetEndpointByType(), getHateoasLinkForDeleteEndpoints());
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    /* Endpoint related Hateoas links*/
    public Link getSelfLink() {
        return new Link(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri().toASCIIString())
                .withSelfRel();
    }

    private Link getHateoasLinkForGetEndpoints() throws MangleException {
        return linkTo(methodOn(EndpointController.class).getEndpoints("")).withRel("GET-ALL");
    }

    private Link getHateoasLinkForGetEndpointByName() throws MangleException {
        return linkTo(methodOn(EndpointController.class).getEndpointByName("")).withRel("GET-ENDPOINT-BY-NAME");
    }

    private Link getHateoasLinkForEnableEndpoint() throws MangleException {
        return linkTo(methodOn(EndpointController.class).enableEndpoint(new ArrayList<>(), false, new HashMap<>()))
                .withRel("ENABLE");
    }

    private Link getHateoasLinkForGetEndpointByType() throws MangleException {
        return linkTo(methodOn(EndpointController.class).getAllEndpointByType(EndpointType.VCENTER))
                .withRel("GET-BY-TYPE");
    }

    private Link getHateoasLinkForUpdateEndpoint() throws MangleException {
        return linkTo(methodOn(EndpointController.class).updateEndpoint(new EndpointSpec()))
                .withRel(HateoasOperations.UPDATE.name());
    }

    private Link getHateoasLinkForAddEndpoint() throws MangleException {
        return linkTo(methodOn(EndpointController.class).addEndpoint(new EndpointSpec())).withRel("ADD");
    }

    private Link getHateoasLinkForDeleteEndpoints() throws MangleException {
        return linkTo(methodOn(EndpointController.class).deleteEndpointsByNames(new ArrayList<>()))
                .withRel(HateoasOperations.DELETE.name());
    }

    /* Credentials related Hateoas links*/
    private Link getHateoasLinkForGetCredentials() throws MangleException {
        return linkTo(methodOn(EndpointController.class).getCredentials()).withRel(HateoasOperations.GET.name());
    }

    private Link getHateoasLinkForDeleteCredentials() throws MangleException {
        return linkTo(methodOn(EndpointController.class).deleteCredentials(new ArrayList<>()))
                .withRel(HateoasOperations.DELETE.name());
    }

    /* Endpoint Certificates related Hateoas links*/
    private Link getHateoasLinkForUpdateCertificate() throws MangleException {
        return linkTo(methodOn(EndpointController.class).updateDockerEndpointCertificates("", "", null, null, null))
                .withRel(HateoasOperations.UPDATE.name());
    }

    private Link getHateoasLinkForGetCertificates() throws MangleException {
        return linkTo(methodOn(EndpointController.class).getAllEndpointCertificates())
                .withRel(HateoasOperations.GET.name());
    }

    private Link getHateoasLinkForAddCertificate() throws MangleException {
        return linkTo(methodOn(EndpointController.class).addDockerEndpointCertificates("", "", null, null, null))
                .withRel("ADD");
    }

    private Link getHateoasLinkForDeleteCertificate() throws MangleException {
        return linkTo(methodOn(EndpointController.class).updateDockerEndpointCertificates("", "", null, null, null))
                .withRel(HateoasOperations.DELETE.name());
    }

    @ApiOperation(value = "API to get all the ready nodes of a kubernetes cluster based on the k8sclustername", nickname = "getallk8SNodesByEndpoint")
    @GetMapping(value = "v1/endpoints/k8s/nodes/ready", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resources<String>> getAllK8SReadyNodesByEndpoint(@RequestParam("endpointName") String endpointName)
            throws MangleException {
        log.info("Start execution of getting all ready k8s nodes");
        List<String> nodes = endpointService.getAllK8SNodesByEndpointName(endpointName);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.READY_NODES_RESULT_FOUND);
        Resources<String> resources = new Resources<>(nodes);
        resources.add(getSelfLink(), getHateoasLinkForAddEndpoint(), getHateoasLinkForEnableEndpoint(),
                getHateoasLinkForUpdateEndpoint(), getHateoasLinkForGetEndpointByName(),
                getHateoasLinkForGetEndpointByType(), getHateoasLinkForDeleteEndpoints());
        return new ResponseEntity<>(resources, headers, HttpStatus.OK);

    }

}