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

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import com.vmware.mangle.cassandra.model.endpoint.CertificatesSpec;
import com.vmware.mangle.cassandra.model.endpoint.CredentialsSpec;
import com.vmware.mangle.cassandra.model.endpoint.DockerCertificates;
import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.endpoint.K8SCredentials;
import com.vmware.mangle.cassandra.model.endpoint.RemoteMachineCredentials;
import com.vmware.mangle.cassandra.model.endpoint.VCenterCredentials;
import com.vmware.mangle.model.enums.EndpointType;
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
import com.vmware.mangle.services.events.web.CustomEventPublisher;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Controller class for Endpoint Service.
 *
 * @author kumargautam
 */
@RestController
@RequestMapping("/rest/api/v1/endpoints")
@Api("/rest/api/v1/endpoints")
@Log4j2
public class EndpointController {

    private static final int MAX_KUBECONFIG_FILE_SIZE = 20 * 1024;
    private static final int MAX_PRIVATEKEY_FILE_SIZE = 5 * 1024;
    private EndpointService endpointService;
    private CredentialService credentialService;
    private CustomEventPublisher publisher;
    private EndpointDeletionService endpointDeletionService;
    private CredentialDeletionService credentialDeletionService;
    private EndpointCertificatesService certificatesService;
    private EndpointCertificatesDeletionService certificatesDeletionService;

    @Autowired
    public EndpointController(EndpointService endpointService, CredentialService credentialService,
            CustomEventPublisher publisher, EndpointDeletionService endpointDeletionService,
            CredentialDeletionService credentialDeletionService, EndpointCertificatesService certificatesService,
            EndpointCertificatesDeletionService certificatesDeletionService) {
        this.credentialService = credentialService;
        this.endpointService = endpointService;
        this.publisher = publisher;
        this.endpointDeletionService = endpointDeletionService;
        this.credentialDeletionService = credentialDeletionService;
        this.certificatesService = certificatesService;
        this.certificatesDeletionService = certificatesDeletionService;
    }

    @ApiOperation(value = "API to get all the endpoints or filter the endpoints by credentials", nickname = "getEndPoints")
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EndpointSpec>> getEndpoints(
            @RequestParam(value = "credentialName", required = false) String credentialName) {
        log.info("Start execution of getEndpoints() method");
        List<EndpointSpec> endPoints;

        if (StringUtils.isEmpty(credentialName)) {
            endPoints = endpointService.getAllEndpoints();
        } else {
            endPoints = endpointService.getEndpointsSpecByCredentialName(credentialName);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.ENDPOINTS_RESULT_FOUND);
        return new ResponseEntity<>(endPoints, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to get all the endpoints by endpointName", nickname = "getEndpointByName")
    @GetMapping(value = "/{endpointName}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<EndpointSpec> getEndpointByName(@PathVariable("endpointName") String endpointName)
            throws MangleException {
        log.info("Start execution of getEndpointByName() method");
        EndpointSpec endPoint = endpointService.getEndpointByName(endpointName);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.ENDPOINTS_RESULT_FOUND);
        return new ResponseEntity<>(endPoint, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to get all the endpoints by endPointType", nickname = "getAllEndpointByType")
    @GetMapping(value = "/type/{endPointType}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<EndpointSpec>> getAllEndpointByType(
            @ApiParam(value = "Select endPointType") @PathVariable("endPointType") EndpointType endPointType)
            throws MangleException {
        log.info("Start execution of getAllEndpointByType() method");
        List<EndpointSpec> endPoint = endpointService.getAllEndpointByType(endPointType);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.ENDPOINTS_RESULT_FOUND);
        return new ResponseEntity<>(endPoint, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to update a existing endpoint", nickname = "updateEndPoint")
    @PutMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EndpointSpec> updateEndpoint(@Validated @RequestBody EndpointSpec endpointSpec)
            throws MangleException {
        log.info("Start execution of updateEndpoint() method");
        if (!endpointService.testEndpointConnection(endpointSpec)) {
            throw new MangleException(ErrorCode.TEST_CONNECTION_FAILED, endpointSpec.getName());
        }
        EndpointSpec resEndpointSpec =
                endpointService.updateEndpointByEndpointName(endpointSpec.getName(), endpointSpec);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.ENDPOINTS_UPDATED);
        publisher.publishAnEvent(
                new EntityUpdatedEvent(resEndpointSpec.getPrimaryKey(), resEndpointSpec.getClass().getName()));
        return new ResponseEntity<>(resEndpointSpec, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to add a new endpoint", nickname = "addEndPoint")
    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EndpointSpec> addEndpoint(@Validated @RequestBody EndpointSpec endpointSpec)
            throws MangleException {
        log.info("Start execution of addEndpoint() method");
        if (!endpointService.testEndpointConnection(endpointSpec)) {
            throw new MangleException(ErrorConstants.TEST_CONNECTION_FAILED, ErrorCode.TEST_CONNECTION_FAILED,
                    endpointSpec.getName());
        }
        EndpointSpec resEndpointSpec = endpointService.addOrUpdateEndpoint(endpointSpec);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.ENDPOINTS_CREATED);
        publisher.publishAnEvent(
                new EntityCreatedEvent(resEndpointSpec.getPrimaryKey(), resEndpointSpec.getClass().getName()));
        return new ResponseEntity<>(resEndpointSpec, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to get an endpoint credentials", nickname = "getCredentials")
    @GetMapping(value = "/credentials", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CredentialsSpec>> getCredentials() {
        log.info("Start execution of getCredentials() method");
        List<CredentialsSpec> credentialsSpec = credentialService.getAllCredentials();
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.CREDENTIALS_RESULT_FOUND);
        return new ResponseEntity<>(credentialsSpec, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to add RemoteMachine endpoint credentials", nickname = "addRemoteMachineCredentials")
    @PostMapping(value = "/credentials/remotemachine", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CredentialsSpec> addCredentials(@RequestParam(name = "id", required = false) String id,
            @RequestParam("name") String name, @RequestParam("username") String username,
            @RequestParam(name = "password", required = false) String password,
            @RequestParam(name = "privateKey", required = false) MultipartFile privateKey) throws MangleException {
        log.info("Start execution of addCredentials(remoteMachineCredentials) method for RemoteMachine");
        credentialService.validatePasswordOrPrivateKeyNotNull(password, privateKey);
        credentialService.validateMultipartFileSize(privateKey, MAX_PRIVATEKEY_FILE_SIZE);
        if (null != privateKey) {
            certificatesService.validateRemoteMachinePrivateKey(privateKey);
        }
        try {
            RemoteMachineCredentials remoteMachineCredentials =
                    credentialService.generateRemoteMachineCredentialsSpec(id, name, username, password, privateKey);
            CredentialsSpec credentialsSpec = credentialService.addOrUpdateCredentials(remoteMachineCredentials);
            HttpHeaders headers = new HttpHeaders();
            headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.CREDENTIALS_CREATED);
            publisher.publishAnEvent(
                    new EntityCreatedEvent(credentialsSpec.getPrimaryKey(), credentialsSpec.getClass().getName()));
            return new ResponseEntity<>(credentialsSpec, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new MangleException(e, ErrorCode.FILE_SIZE_EXCEEDED, privateKey.getOriginalFilename());

        }
    }

    @ApiOperation(value = "API to add a K8S_CLUSTER endpoint credentials", nickname = "addK8SCredentials")
    @PostMapping(value = "/credentials/k8s", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CredentialsSpec> addCredentials(@RequestParam(name = "id", required = false) String id,
            @ApiParam(value = "Mandatory: Only if mangle is not deployed in same k8s cluster") @RequestParam(name = "kubeConfig", required = false) MultipartFile kubeConfig,
            @RequestParam("name") String name) throws MangleException {
        log.info("Start execution of addCredentials(k8sCredentials) method for K8S_CLUSTER");
        credentialService.validateMultipartFileSize(kubeConfig, MAX_KUBECONFIG_FILE_SIZE);
        credentialService.validateK8SConfigFile(kubeConfig);
        try {
            K8SCredentials k8sCredentials = credentialService.generateK8SCredentialsSpec(id, name, kubeConfig);
            log.info("kubeConfig : " + kubeConfig);
            CredentialsSpec credentialsSpec = credentialService.addOrUpdateCredentials(k8sCredentials);
            HttpHeaders headers = new HttpHeaders();
            headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.CREDENTIALS_CREATED);
            publisher.publishAnEvent(
                    new EntityCreatedEvent(credentialsSpec.getPrimaryKey(), credentialsSpec.getClass().getName()));
            return new ResponseEntity<>(credentialsSpec, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new MangleException(e, ErrorCode.FILE_SIZE_EXCEEDED, kubeConfig.getOriginalFilename());
        }
    }

    //Will be uncommented when we started supporting AWS endpoint

    /* @ApiOperation(value = "API to add a AWS endpoint credentials", nickname = "addAWSCredentials")
    @PostMapping(value = "/credentials/aws", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CredentialsSpec> addCredentials(@RequestBody AWSCredentials awsCredentials)
            throws MangleException {
        log.info("Start execution of addCredentials() method for AWS");
        CredentialsSpec credentialsSpec = credentialService.addOrUpdateCredentials(awsCredentials);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.CREDENTIALS_CREATED);
        publisher.publishAnEvent(
                new EntityCreatedEvent(credentialsSpec.getPrimaryKey(), credentialsSpec.getClass().getName()));
        return new ResponseEntity<>(credentialsSpec, headers, HttpStatus.OK);
    }*/

    @ApiOperation(value = "API to add a Vcenter endpoint credentials", nickname = "addVCenterCredentials")
    @PostMapping(value = "/credentials/vcenter", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CredentialsSpec> addCredentials(@Validated @RequestBody VCenterCredentials vcenterCredentials)
            throws MangleException {
        log.info("Start execution of addCredentials(vcenterCredentials) method for Vcenter");
        CredentialsSpec credentialsSpec = credentialService.addOrUpdateCredentials(vcenterCredentials);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.CREDENTIALS_CREATED);
        publisher.publishAnEvent(
                new EntityCreatedEvent(credentialsSpec.getPrimaryKey(), credentialsSpec.getClass().getName()));
        return new ResponseEntity<>(credentialsSpec, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to delete the endpoints by names", nickname = "deleteEndPoints")
    @DeleteMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ErrorDetails> deleteEndpointsByNames(@RequestParam List<String> endpointNames)
            throws MangleException {
        log.info("Start execution of deleteEndpointsByNames() method");
        DeleteOperationResponse response = endpointDeletionService.deleteEndpointByNames(endpointNames);
        ErrorDetails errorDetails = new ErrorDetails();
        if (response.getAssociations().size() == 0) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            Map<String, Map<String, List<String>>> associations = new HashMap<>();
            associations.put("associations", response.getAssociations());
            errorDetails.setTimestamp(new Date());
            errorDetails.setDescription(ErrorConstants.ENDPOINTS_DELETION_PRECHECK_FAIL);
            errorDetails.setCode(ErrorCode.DELETE_OPERATION_FAILED.getCode());
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
    @DeleteMapping(value = "/credentials", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ErrorDetails> deleteCredentials(@RequestParam List<String> credentialNames)
            throws MangleException {
        log.info("Start execution of deleteCredentials() method");
        DeleteOperationResponse response = credentialDeletionService.deleteCredentialsByNames(credentialNames);
        ErrorDetails errorDetails = new ErrorDetails();
        if (response.getAssociations().size() == 0) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            Map<String, Map<String, List<String>>> associations = new HashMap<>();
            associations.put("associations", response.getAssociations());
            errorDetails.setTimestamp(new Date());
            errorDetails.setDescription(ErrorConstants.CREDENTIALS_DELETION_PRECHECK_FAIL);
            errorDetails.setCode(ErrorCode.DELETE_OPERATION_FAILED.getCode());
            errorDetails.setDetails(associations);


        }

        return new ResponseEntity<>(errorDetails, HttpStatus.PRECONDITION_FAILED);
    }

    @ApiOperation(value = "API to get all the endpoints based on page", nickname = "getEndpointsBasedOnPage")
    @GetMapping(value = "/page", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
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
    @GetMapping(value = "/credentials/page", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
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
    @PostMapping(value = "/testConnection", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EndpointSpec> testConnection(@RequestParam String endpointName) throws MangleException {
        log.info("Start execution of testConnection() method");
        EndpointSpec endpointSpec = endpointService.getEndpointByName(endpointName);
        if (!endpointService.testEndpointConnection(endpointSpec)) {
            throw new MangleException(ErrorConstants.TEST_CONNECTION_FAILED, ErrorCode.TEST_CONNECTION_FAILED,
                    endpointSpec.getName());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.TEST_CONNECTION_SUCCESS);
        return new ResponseEntity<>(endpointSpec, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to test the endpoint connection", nickname = "testEndPointConnection", hidden = true)
    @PostMapping(value = "/testEndpoint", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EndpointSpec> endpointTestConnection(@Validated @RequestBody EndpointSpec endpointSpec)
            throws MangleException {
        log.info("Start execution of endpointTestConnection() method");
        if (!endpointService.testEndpointConnection(endpointSpec)) {
            throw new MangleException(ErrorConstants.TEST_CONNECTION_FAILED, ErrorCode.TEST_CONNECTION_FAILED,
                    endpointSpec.getName());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.TEST_CONNECTION_SUCCESS);
        return new ResponseEntity<>(endpointSpec, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to update RemoteMachine endpoint credentials", nickname = "updateRemoteMachineCredentials")
    @PutMapping(value = "/credentials/remotemachine", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CredentialsSpec> updateCredentials(@RequestParam(name = "id", required = false) String id,
            @RequestParam("name") String name, @RequestParam("username") String username,
            @RequestParam(name = "password", required = false) String password,
            @RequestParam(name = "privateKey", required = false) MultipartFile privateKey) throws MangleException {
        log.info("Start execution of addCredentials() method for RemoteMachine");
        credentialService.validatePasswordOrPrivateKeyNotNull(password, privateKey);
        credentialService.validateMultipartFileSize(privateKey, MAX_PRIVATEKEY_FILE_SIZE);
        if (null != privateKey) {
            certificatesService.validateRemoteMachinePrivateKey(privateKey);
        }
        try {
            RemoteMachineCredentials remoteMachineCredentials =
                    credentialService.generateRemoteMachineCredentialsSpec(id, name, username, password, privateKey);
            CredentialsSpec credentialsSpec = credentialService.updateCredential(remoteMachineCredentials);
            publisher.publishAnEvent(
                    new EntityUpdatedEvent(credentialsSpec.getPrimaryKey(), credentialsSpec.getClass().getName()));
            return new ResponseEntity<>(credentialsSpec, HttpStatus.OK);
        } catch (IOException e) {
            throw new MangleException(e, ErrorCode.FILE_SIZE_EXCEEDED, privateKey.getOriginalFilename());

        }
    }

    @ApiOperation(value = "API to update a K8S_CLUSTER endpoint credentials", nickname = "updateK8SCredentials")
    @PutMapping(value = "/credentials/k8s", produces = MediaType.APPLICATION_JSON_VALUE)
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
    @PutMapping(value = "/credentials/vcenter", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CredentialsSpec> updateCredentials(
            @Validated @RequestBody VCenterCredentials vcenterCredentials) throws MangleException {
        log.info("Start execution of updateCredentials(vcenterCredentials) method for Vcenter");
        CredentialsSpec credentialsSpec = credentialService.updateCredential(vcenterCredentials);
        publisher.publishAnEvent(
                new EntityUpdatedEvent(credentialsSpec.getPrimaryKey(), credentialsSpec.getClass().getName()));
        return new ResponseEntity<>(credentialsSpec, HttpStatus.OK);
    }

    @ApiOperation(value = "API to get an endpoint certificates", nickname = "getAllEndpointCertificates")
    @GetMapping(value = "/certificates", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CertificatesSpec>> getAllEndpointCertificates() {
        log.info("Start execution of getAllEndpointCertificates() method");
        List<CertificatesSpec> certificatesSpec = certificatesService.getAllCerficates();
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.CERTIFICATES_RESULT_FOUND);
        return new ResponseEntity<>(certificatesSpec, headers, HttpStatus.OK);
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
    @DeleteMapping(value = "/certificates", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ErrorDetails> deleteCertificates(@RequestParam List<String> certificatesNames)
            throws MangleException {
        log.info("Start execution of deleteCertificates() method");
        DeleteOperationResponse response = certificatesDeletionService.deleteCertificatesByNames(certificatesNames);
        ErrorDetails errorDetails = new ErrorDetails();
        if (response.getAssociations().size() == 0) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            Map<String, Map<String, List<String>>> associations = new HashMap<>();
            associations.put("associations", response.getAssociations());
            errorDetails.setTimestamp(new Date());
            errorDetails.setDescription(ErrorConstants.CREDENTIALS_DELETION_PRECHECK_FAIL);
            errorDetails.setCode(ErrorCode.DELETE_OPERATION_FAILED.getCode());
            errorDetails.setDetails(associations);
        }
        return new ResponseEntity<>(errorDetails, HttpStatus.PRECONDITION_FAILED);
    }

    @ApiOperation(value = "API to add docker endpoint certificates", nickname = "addDockerEndpointCertificates")
    @PostMapping(value = "/certificates/docker", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CertificatesSpec> addDockerEndpointCertificates(
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
            return new ResponseEntity<>(certificatesSpec, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new MangleException(e, ErrorCode.IO_EXCEPTION, e.getMessage());

        }
    }

    @ApiOperation(value = "API to update docker endpoint certificates", nickname = "updateDockerEndpointCertificates")
    @PutMapping(value = "/certificates/docker", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CertificatesSpec> updateDockerEndpointCertificates(
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
            return new ResponseEntity<>(certificatesSpec, headers, HttpStatus.OK);
        } catch (IOException e) {
            throw new MangleException(e, ErrorCode.IO_EXCEPTION, e.getMessage());

        }
    }
}