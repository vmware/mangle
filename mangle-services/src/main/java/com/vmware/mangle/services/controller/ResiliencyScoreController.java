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

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.vmware.mangle.cassandra.model.resiliencyscore.QueryDto;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreConfigSpec;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreMetricConfig;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreTask;
import com.vmware.mangle.cassandra.model.resiliencyscore.Service;
import com.vmware.mangle.model.enums.HateoasOperations;
import com.vmware.mangle.model.response.ResiliencyScoreDeleteResponse;
import com.vmware.mangle.services.constants.CommonConstants;
import com.vmware.mangle.services.helpers.ResiliencyScoreHelper;
import com.vmware.mangle.services.resiliencyscore.ResiliencyScoreMetricConfigService;
import com.vmware.mangle.services.resiliencyscore.ResiliencyScoreService;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;


/**
 * @author dbhat
 *
 */
@Log4j2
@RestController
@RequestMapping("/rest/api/v1/resiliencyscore")
@Api("/rest/api/v1/resiliencyscore")
public class ResiliencyScoreController {
    private ResiliencyScoreHelper resiliencyScoreHelper;
    private ResiliencyScoreService resiliencyScoreService;
    private ResiliencyScoreMetricConfigService resiliencyScoreMetricConfigService;

    @Autowired
    public ResiliencyScoreController(ResiliencyScoreHelper resiliencyScoreHelper,
            ResiliencyScoreService resiliencyScoreService,
            ResiliencyScoreMetricConfigService resiliencyScoreMetricConfigService) {
        this.resiliencyScoreHelper = resiliencyScoreHelper;
        this.resiliencyScoreService = resiliencyScoreService;
        this.resiliencyScoreMetricConfigService = resiliencyScoreMetricConfigService;
    }

    @ApiOperation(value = "API to trigger Resiliency score calculation", nickname = "calculateResiliencyScore")
    @PostMapping(value = "/calculate", produces = "application/json")
    public ResponseEntity<ResiliencyScoreTask> calculateResiliencyScore(
            @Validated @RequestBody ResiliencyScoreConfigSpec configSpec) throws MangleException {
        log.debug("Triggering Resiliency score calculation");
        ResiliencyScoreTask resiliencyScoreTask = resiliencyScoreHelper.calculateResiliencyScore(configSpec);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.RESILIENCY_SCORE_CALCULATING_TRIGGERED);
        return new ResponseEntity<>(resiliencyScoreTask, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to get resiliency score task details from Mangle using its id", nickname = "getTaskInfo")
    @GetMapping(value = "/{taskId}", produces = "application/json")
    public ResponseEntity<ResiliencyScoreTask> getTask(@PathVariable String taskId) throws MangleException {
        log.info("Received request to retrieve details for the task with the taskId: {}", taskId);
        if (StringUtils.isEmpty(taskId)) {
            throw new MangleException(ErrorCode.NO_TASK_FOUND, taskId);
        }
        ResiliencyScoreTask task = resiliencyScoreService.getTaskById(taskId);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.RESILIENCY_SCORE_TASK_FOUND);
        return new ResponseEntity<>(task, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to get details of all Resiliency score Tasks Executed by Mangle", nickname = "getAllTasks")
    @GetMapping(value = "", produces = "application/json")
    public ResponseEntity<List<ResiliencyScoreTask>> getAllTasks() {
        log.info("Received request to retrieve details of all the tasks");
        return new ResponseEntity<>(resiliencyScoreService.getAllTasks(), HttpStatus.OK);
    }

    @ApiOperation(value = "API to delete resiliency score task by ID", nickname = "deleteResiliencyScore")
    @DeleteMapping(value = "/{resiliencyScoreTaskId}")
    public ResponseEntity<ResiliencyScoreDeleteResponse> deleteResiliencyScore(
            @PathVariable String resiliencyScoreTaskId) throws MangleException {
        log.info("Deleting the Resiliency score by ID " + resiliencyScoreTaskId);
        if (StringUtils.isEmpty(resiliencyScoreTaskId)) {
            throw new MangleException(ErrorCode.NO_TASK_FOUND, resiliencyScoreTaskId);
        }
        boolean status = this.resiliencyScoreService.deleteResiliencyScoreById(resiliencyScoreTaskId);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.RESILIENCY_SCORE_DELETED);
        ResiliencyScoreDeleteResponse mangleResponse = new ResiliencyScoreDeleteResponse();
        mangleResponse.setResultStatus(status);
        return new ResponseEntity<>(mangleResponse, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to create Resiliency Score Metric Configuration", nickname = "createResiliencyScoreMetricConfig")
    @PostMapping(value = "/config", produces = "application/json")
    public ResponseEntity<ResiliencyScoreMetricConfig> createResiliencyScoreMetricConfig(
            @Validated @RequestBody ResiliencyScoreMetricConfig configSpec) throws MangleException {
        log.debug("Creating resiliency score metric configuration");
        ResiliencyScoreMetricConfig specSaved = resiliencyScoreMetricConfigService.addMetricConfig(configSpec);

        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.RESILIENCY_SCORE_METRIC_CONFIG_UPDATED_MESSAGE);
        return new ResponseEntity<>(specSaved, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to delete Resiliency Score Metric Configuration", nickname = "deleteResiliencyScoreMetricConfig")
    @DeleteMapping(value = "/config", produces = "application/json")
    public ResponseEntity<ResiliencyScoreDeleteResponse> deleteResiliencyScoreMetricConfig(
            @RequestParam("metricConfigName") String metricConfigName) throws MangleException {
        log.debug("Deleting resiliency score metric configuration");
        if (StringUtils.isEmpty(metricConfigName)) {
            throw new MangleException(ErrorCode.CONFIG_METRIC_NAME_CANNOT_BE_EMPTY, metricConfigName);
        }

        boolean status = resiliencyScoreMetricConfigService.deleteMetricConfig(metricConfigName);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.RESILIENCY_SCORE_METRIC_CONFIG_DELETED);
        ResiliencyScoreDeleteResponse mangleResponse = new ResiliencyScoreDeleteResponse();
        mangleResponse.setResultStatus(status);
        return new ResponseEntity<>(mangleResponse, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to get resiliency score metric configurations defined", nickname = "getResiliencyScoreMetricConfig")
    @GetMapping(value = "/config", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ResiliencyScoreMetricConfig>> getAllMetricConfigs() {
        log.info("Getting Resiliency Score metric configurations");
        List<ResiliencyScoreMetricConfig> metricConfigs =
                resiliencyScoreMetricConfigService.getAllResiliencyScoreMetricConfigs();

        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.RESILIENCY_SCORE_METRIC_CONFIG_RETRIEVED);
        return new ResponseEntity<>(metricConfigs, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to update an existing Resiliency score metric configuration", nickname = "updateResiliencyScoreMetricConfig")
    @PutMapping(value = "/config", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResiliencyScoreMetricConfig> updateMetricConfig(
            @Validated @RequestBody ResiliencyScoreMetricConfig metricConfig) throws MangleException {
        log.info("Updating the Resiliency Score metric configuration");

        ResiliencyScoreMetricConfig updateMetricConfig =
                resiliencyScoreMetricConfigService.updateMetricConfig(metricConfig.getName(), metricConfig);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.RESILIENCY_SCORE_METRIC_CONFIG_UPDATED_MESSAGE);
        return new ResponseEntity<>(updateMetricConfig, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to get all the services defined", nickname = "getAllServices")
    @GetMapping(value = "/service", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resources<Service>> getAllServices() throws MangleException {
        log.info("Getting all the service definitions ");
        List<Service> services = resiliencyScoreService.getAllServices();
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.SERVICES_RETRIEVED);

        Resources<Service> resources = new Resources<>(services);
        resources.add(getSelfLink(), getHateoasLinkForAddService(), getHateoasLinkForUpdateService(),
                getHateoasLinkForDeleteService());
        return new ResponseEntity<>(resources, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to get latest definition of service specified", nickname = "getServiceDetails")
    @GetMapping(value = "/service/{serviceName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resources<Service>> getServiceDetails(@PathVariable String serviceName)
            throws MangleException {
        log.info("Getting Last updated value for the service: " + serviceName);
        List<Service> service = resiliencyScoreService.getServiceDetailsByName(serviceName);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.SERVICES_RETRIEVED);

        Resources<Service> resource = new Resources<>(service);
        resource.add(getSelfLink(), getHateoasLinkForAddService(), getHateoasLinkForUpdateService(),
                getHateoasLinkForDeleteService());
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to add Service definition", nickname = "addService")
    @PostMapping(value = "/service", produces = "application/json")
    public ResponseEntity<Resource<Service>> addService(@Validated @RequestBody Service serviceSpec)
            throws MangleException {
        log.debug("Creating Service definition");
        Service specSaved = resiliencyScoreService.addOrUpdateService(serviceSpec);

        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.SERVICE_ADDED);

        Resource<Service> resource = new Resource<>(specSaved);
        resource.add(getSelfLink(), getHateoasLinkForGetService(), getHateoasLinkForUpdateService(),
                getHateoasLinkForDeleteService());
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to update an existing service definition ", nickname = "updateService")
    @PutMapping(value = "/service", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<Service>> updateService(@Validated @RequestBody Service serviceSpec)
            throws MangleException {
        log.info("Updating existing service definition with ID: " + serviceSpec.getId());

        Service serviceUpdated = resiliencyScoreService.updateService(serviceSpec.getId(), serviceSpec);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.SERVICE_UPDATED);

        Resource<Service> resource = new Resource<>(serviceUpdated);
        resource.add(getSelfLink(), getHateoasLinkForGetService(), getHateoasLinkForAddService(),
                getHateoasLinkForDeleteService());
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to delete specified version of service definition", nickname = "deleteServiceById")
    @DeleteMapping(value = "/service/deleteById", produces = "application/json")
    public ResponseEntity<Resource<ResiliencyScoreDeleteResponse>> deleteServiceById(
            @RequestParam("serviceId") String serviceId) throws MangleException {
        log.debug("Deleting service with ID: " + serviceId);

        boolean status = resiliencyScoreService.deleteServiceById(serviceId);
        HttpHeaders headers = getHeaderForDeleteOperation(CommonConstants.SERVICE_DELETED);
        ResiliencyScoreDeleteResponse mangleResponse = getDeleteResponse(status);

        Resource<ResiliencyScoreDeleteResponse> resource = new Resource<>(mangleResponse);
        resource.add(getSelfLink(), getHateoasLinkForGetService(), getHateoasLinkForAddService(),
                getHateoasLinkForUpdateService());
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to delete all versions specified service definition", nickname = "deleteServiceByName")
    @DeleteMapping(value = "/service/deleteByName", produces = "application/json")
    public ResponseEntity<Resource<ResiliencyScoreDeleteResponse>> deleteServiceByName (
            @RequestParam("serviceName") String serviceName) throws MangleException {
        log.debug("Deleting service with Name: " + serviceName);

        boolean status = resiliencyScoreService.deleteServiceByName(serviceName);
        ResiliencyScoreDeleteResponse mangleResponse = getDeleteResponse(status);
        HttpHeaders headers = getHeaderForDeleteOperation(CommonConstants.SERVICE_DELETED);

        Resource<ResiliencyScoreDeleteResponse> resource = new Resource<>(mangleResponse);
        resource.add(getSelfLink(), getHateoasLinkForGetService(), getHateoasLinkForAddService(),
                getHateoasLinkForUpdateService());
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to get all the query defined", nickname = "getAllQueries")
    @GetMapping(value = "/query", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resources<QueryDto>> getAllQueries() throws MangleException {
        log.info("Getting all the Query definitions ");
        List<QueryDto> queries = resiliencyScoreService.getAllQueries();
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.QUERIES_RETRIEVED);

        Resources<QueryDto> resources = new Resources<>(queries);
        resources.add(getSelfLink(), getHateoasLinkForAddQuery(), getHateoasLinkForUpdateQuery(),
                getHateoasLinkForDeleteQuery());
        return new ResponseEntity<>(resources, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to add Query definition", nickname = "addQuery")
    @PostMapping(value = "/query", produces = "application/json")
    public ResponseEntity<Resource<QueryDto>> addQuery(@Validated @RequestBody QueryDto querySpec)
            throws MangleException {
        log.info("Creating Query definition");
        QueryDto specSaved = resiliencyScoreService.addQuery(querySpec);

        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.QUERY_ADDED);

        Resource<QueryDto> resource = new Resource<>(specSaved);
        resource.add(getSelfLink(), getHateoasLinkForGetQuery(), getHateoasLinkForUpdateQuery(),
                getHateoasLinkForDeleteQuery());
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to update an existing Query definition ", nickname = "updateQuery")
    @PutMapping(value = "/query", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<QueryDto>> updateQuery(@Validated @RequestBody QueryDto querySpec)
            throws MangleException {
        log.info("Updating existing Query definition with name: " + querySpec.getName());

        QueryDto queryUpdated = resiliencyScoreService.updateQuery(querySpec.getName(), querySpec);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.QUERY_UPDATED);

        Resource<QueryDto> resource = new Resource<>(queryUpdated);
        resource.add(getSelfLink(), getHateoasLinkForGetQuery(), getHateoasLinkForAddQuery(),
                getHateoasLinkForDeleteQuery());
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to delete specified version of Query definition", nickname = "deleteQueryById")
    @DeleteMapping(value = "/query/deleteQueryById", produces = "application/json")
    public ResponseEntity<Resource<ResiliencyScoreDeleteResponse>> deleteQueryById(
            @RequestParam("queryId") String queryId) throws MangleException {
        log.info("Deleting Query with ID: " + queryId);
        if (StringUtils.isEmpty(queryId)) {
            throw new MangleException(ErrorCode.EMPTY_QUERY_ID);
        }

        boolean status = resiliencyScoreService.deleteQuery(queryId);
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, CommonConstants.QUERY_DELETED);
        ResiliencyScoreDeleteResponse mangleResponse = new ResiliencyScoreDeleteResponse();
        mangleResponse.setResultStatus(status);

        Resource<ResiliencyScoreDeleteResponse> resource = new Resource<>(mangleResponse);
        resource.add(getSelfLink(), getHateoasLinkForGetQuery(), getHateoasLinkForAddQuery(),
                getHateoasLinkForUpdateQuery());
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "API to delete Query with all it's versions", nickname = "deleteQueryByName")
    @DeleteMapping(value = "/query/deleteByName", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Resource<ResiliencyScoreDeleteResponse>> deleteQueryByName(
            @RequestParam("queryName") String queryName) throws MangleException {
        log.info("Deleting the query with name: " + queryName);
        boolean statusOfDeleting = false;
        String headerMessage;
        try {
            statusOfDeleting = resiliencyScoreService.deleteQueryByName(queryName);
            headerMessage = CommonConstants.QUERY_DELETED;
            log.info("Delete operation was successful");
        } catch (MangleException mangleException) {
            statusOfDeleting = false;
            headerMessage = CommonConstants.QUERY_DELETING_FAILED;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, headerMessage);
        ResiliencyScoreDeleteResponse mangleResponse = new ResiliencyScoreDeleteResponse();
        mangleResponse.setResultStatus(statusOfDeleting);

        Resource<ResiliencyScoreDeleteResponse> resource = new Resource<>(mangleResponse);
        resource.add(getSelfLink(), getHateoasLinkForGetQuery(), getHateoasLinkForAddQuery(),
                getHateoasLinkForUpdateQuery());
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);

    }

    private HttpHeaders getHeaderForDeleteOperation(String headerMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(CommonConstants.MESSAGE_HEADER, headerMessage);
        return headers;
    }

    private ResiliencyScoreDeleteResponse getDeleteResponse(boolean statusOfOperation) {
        ResiliencyScoreDeleteResponse mangleResponse = new ResiliencyScoreDeleteResponse();
        mangleResponse.setResultStatus(statusOfOperation);
        return mangleResponse;
    }

    public Link getSelfLink() {
        return new Link(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri().toASCIIString())
                .withSelfRel();
    }

    /* Hateoas links for Service APIs */
    private Link getHateoasLinkForDeleteService() throws MangleException {
        return linkTo(methodOn(ResiliencyScoreController.class).deleteServiceById("id"))
                .withRel(HateoasOperations.DELETE.name());
    }

    private Link getHateoasLinkForGetService() throws MangleException {
        return linkTo(methodOn(ResiliencyScoreController.class).getAllServices()).withRel(HateoasOperations.GET.name());
    }

    private Link getHateoasLinkForUpdateService() throws MangleException {
        return linkTo(methodOn(ResiliencyScoreController.class).updateService(new Service()))
                .withRel(HateoasOperations.UPDATE.name());
    }

    private Link getHateoasLinkForAddService() throws MangleException {
        return linkTo(methodOn(ResiliencyScoreController.class).addService(new Service()))
                .withRel(HateoasOperations.CREATE.name());
    }

    /* Hateoas links for Query APIs */
    private Link getHateoasLinkForDeleteQuery() throws MangleException {
        return linkTo(methodOn(ResiliencyScoreController.class).deleteQueryByName("queryName"))
                .withRel(HateoasOperations.DELETE.name());
    }

    private Link getHateoasLinkForGetQuery() throws MangleException {
        return linkTo(methodOn(ResiliencyScoreController.class).getAllQueries()).withRel(HateoasOperations.GET.name());
    }

    private Link getHateoasLinkForUpdateQuery() throws MangleException {
        return linkTo(methodOn(ResiliencyScoreController.class).updateQuery(new QueryDto()))
                .withRel(HateoasOperations.UPDATE.name());
    }

    private Link getHateoasLinkForAddQuery() throws MangleException {
        return linkTo(methodOn(ResiliencyScoreController.class).addQuery(new QueryDto()))
                .withRel(HateoasOperations.CREATE.name());
    }

}
