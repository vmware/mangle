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

package com.vmware.mangle.services.resiliencyscore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.resiliencyscore.QueryDto;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreTask;
import com.vmware.mangle.cassandra.model.resiliencyscore.Service;
import com.vmware.mangle.cassandra.model.scheduler.SchedulerSpec;
import com.vmware.mangle.model.response.DeleteOperationResponse;
import com.vmware.mangle.services.SchedulerService;
import com.vmware.mangle.services.repository.QueryRepository;
import com.vmware.mangle.services.repository.ResiliencyScoreRepository;
import com.vmware.mangle.services.repository.ServiceRepository;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;
import com.vmware.mangle.utils.helpers.resiliencyscore.QueryLastUpdatedTimeSorter;
import com.vmware.mangle.utils.helpers.resiliencyscore.ServiceLastUpdatedTimeSorter;


/**
 * @author dbhat
 */

@Component
@Log4j2
public class ResiliencyScoreService {

    @Autowired
    private ResiliencyScoreRepository repository;
    @Autowired
    private ServiceRepository serviceRepository;
    @Autowired
    private QueryRepository queryRepository;
    @Autowired
    private SchedulerService schedulerService;

    public ResiliencyScoreTask getTaskById(String taskId) throws MangleException {
        log.info("Retrieving task by id : " + taskId);
        if (!StringUtils.isEmpty(taskId)) {
            Optional<ResiliencyScoreTask> optional = repository.findById(taskId);
            if (optional.isPresent()) {
                return optional.get();
            } else {
                throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.TASK_ID, taskId);
            }
        } else {
            log.error(ErrorConstants.TASK_ID + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleRuntimeException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.TASK_ID);
        }
    }

    @SuppressWarnings("unchecked")
    public ResiliencyScoreTask addOrUpdateTask(ResiliencyScoreTask task) throws MangleException {
        if (task != null) {
            log.debug("Creating Task with Id : " + task.getId());
            task.setLastUpdated(System.currentTimeMillis());
            return repository.save(task);
        }
        log.error(ErrorConstants.TASK + ErrorConstants.FIELD_VALUE_EMPTY);
        throw new MangleRuntimeException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.TASK);
    }

    public List<ResiliencyScoreTask> getAllTasks() {
        log.debug(" Retrieving all the resiliency score tasks ");
        return repository.findAll();
    }

    public Service addOrUpdateService(Service serviceSpec) {
        if (null == serviceSpec) {
            log.error(ErrorConstants.SERVICE + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleRuntimeException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.SERVICE);
        }
        return serviceRepository.save(serviceSpec);
    }


    public Service getServiceDetails(String serviceId) {
        return serviceRepository.findById(serviceId).orElse(null);
    }


    public List<Service> getAllServices() {
        log.debug("Retrieving all the service definitions saved");
        Set<Service> uniqueServices = new HashSet<>();
        List<Service> allServices = serviceRepository.findAll();
        for (Service service : allServices) {
            uniqueServices.add(getServiceByName(service.getName()));
        }
        return new ArrayList<>(uniqueServices);
    }

    public List<Service> getServiceDetailsByName(String serviceName) {
        return serviceRepository.findByName(serviceName);
    }

    public boolean deleteResiliencyScoreById(String resiliencyScoreId) {
        log.debug("Deleting the Resiliency score by ID: " + resiliencyScoreId);
        Optional<ResiliencyScoreTask> resiliencyScore = repository.findById(resiliencyScoreId);
        if (resiliencyScore.isPresent()) {
            repository.deleteById(resiliencyScoreId);
            return true;
        }
        log.error(ErrorConstants.RESILIENCY_SCORE_TASK_NOT_FOUND + resiliencyScoreId);
        throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.NO_RECORD_FOUND);
    }

    public Service updateService(String serviceId, Service serviceSpec) throws MangleException {
        if (StringUtils.isEmpty(serviceId) || null == serviceSpec) {
            log.error(ErrorConstants.SERVICE + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.SERVICE);
        }
        log.debug(" Updating the Service definition in DB ");
        Optional<Service> configInDb = serviceRepository.findById(serviceId);
        if (!configInDb.isPresent()) {
            log.error(ErrorConstants.NO_RECORD_FOUND_FOR_SERVICE);
            throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.NO_RECORD_FOUND_FOR_SERVICE);
        }
        return serviceRepository.save(serviceSpec);
    }

    public boolean deleteServiceById(String serviceId) throws MangleException {
        log.debug("Deleting the service Id: " + serviceId);
        if (!StringUtils.isEmpty(serviceId)) {
            Optional<Service> service = serviceRepository.findById(serviceId);
            if (service.isPresent()) {
                serviceRepository.deleteById(serviceId);
                Optional<Service> verifyDeleting = serviceRepository.findById(serviceId);
                return !verifyDeleting.isPresent();
            } else {
                throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.SERVICE, serviceId);
            }
        } else {
            log.error(ErrorConstants.SERVICE_CANNOT_BE_EMPTY + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.SERVICE_CANNOT_BE_EMPTY);
        }
    }

    public boolean deleteServiceByName(String serviceName) throws MangleException {
        log.info("Deleting all the queries with the name: " + serviceName);
        if (StringUtils.isEmpty(serviceName)) {
            log.error(ErrorConstants.SERVICE_CANNOT_BE_EMPTY + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.SERVICE_CANNOT_BE_EMPTY);
        }
        List<Service> allServices = serviceRepository.findByName(serviceName);
        if (CollectionUtils.isEmpty(allServices)) {
            throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.SERVICE, serviceName);
        }
        for (Service service : allServices) {
            if (!deleteServiceById(service.getId())) {
                log.error("Deleting of all the data associated with the queryName: " + serviceName + " failed");
                return false;
            }
        }
        log.info("Deleting of all versions with the name: " + serviceName + " was successful");
        return true;
    }

    public QueryDto addQuery(QueryDto querySpec) {
        if (null == querySpec) {
            log.error(ErrorConstants.QUERY + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleRuntimeException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.QUERY);
        }
        return queryRepository.save(querySpec);
    }

    public List<QueryDto> getAllQueries() {
        log.info("Retrieving all the service definitions saved");
        Set<QueryDto> uniqueQueries = new HashSet<>();
        List<QueryDto> allQueries = queryRepository.findAll();
        for (QueryDto query : allQueries) {
            uniqueQueries.add(getQueryeByName(query.getName()));
        }
        return new ArrayList<>(uniqueQueries);
    }

    public QueryDto updateQuery(String queryName, QueryDto querySpec) throws MangleException {
        if (StringUtils.isEmpty(queryName) || null == querySpec) {
            log.error(ErrorConstants.QUERY + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.QUERY);
        }
        log.debug(" Updating the Service definition in DB ");
        QueryDto queryPersisted = getQueryeByName(queryName);
        querySpec.setId(queryPersisted.getId());
        return queryRepository.save(querySpec);
    }

    public boolean deleteQuery(String queryId) throws MangleException {
        log.debug("Deleting the service Id: " + queryId);
        if (!StringUtils.isEmpty(queryId)) {
            Optional<QueryDto> query = queryRepository.findById(queryId);
            if (query.isPresent()) {
                queryRepository.deleteById(queryId);
                Optional<QueryDto> verifyDeleting = queryRepository.findById(queryId);
                return !verifyDeleting.isPresent();
            } else {
                throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.QUERY, queryId);
            }
        } else {
            log.error(ErrorConstants.QUERY_CANNOT_BE_EMPTY + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.QUERY_CANNOT_BE_EMPTY);
        }
    }

    public boolean deleteQueryByName(String queryName) throws MangleException {
        log.info("Deleting all the queries with the name: " + queryName);
        if (StringUtils.isEmpty(queryName)) {
            log.error(ErrorConstants.QUERY_CANNOT_BE_EMPTY + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.QUERY_CANNOT_BE_EMPTY);
        }
        List<QueryDto> allQueries = queryRepository.findByName(queryName);
        if (CollectionUtils.isEmpty(allQueries)) {
            throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.QUERY, queryName);
        }
        for (QueryDto query : allQueries) {
            if (!deleteQuery(query.getId())) {
                log.error("Deleting of all the data associated with the queryName: " + queryName + " failed");
                return false;
            }
        }
        log.info("Deleting of all versions with the name: " + queryName + " was successful");
        return true;
    }

    public QueryDto getQueryeByName(String queryName) {
        log.debug("Retrieving the Query definitions with name: " + queryName);
        List<QueryDto> queries = queryRepository.findByName(queryName);
        if (CollectionUtils.isEmpty(queries)) {
            throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.SERVICE, queryName);
        }
        queries.sort(new QueryLastUpdatedTimeSorter());
        return queries.get(0);
    }

    public QueryDto getLastUpdatedValueOfQuery(String queryName) {
        log.debug("Retrieving the last updated Query definition for query with name: " + queryName);
        List<QueryDto> queries = queryRepository.findByName(queryName);
        if (CollectionUtils.isEmpty(queries)) {
            throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.SERVICE, queryName);
        }
        queries.sort(new QueryLastUpdatedTimeSorter());
        return queries.get(0);
    }

    public Service getServiceByName(String serviceName) {
        log.debug("Retrieving the service definitions with name: " + serviceName);
        List<Service> services = serviceRepository.findByName(serviceName);
        if (CollectionUtils.isEmpty(services)) {
            throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.SERVICE, serviceName);
        }
        services.sort(new ServiceLastUpdatedTimeSorter());
        return services.get(0);
    }

    public DeleteOperationResponse deleteTasksByIds(List<String> taskIds) throws MangleException {
        log.debug("Deleting the resiliency score tasks");
        DeleteOperationResponse deleteResponse = new DeleteOperationResponse();
        if (!CollectionUtils.isEmpty(taskIds)) {
            List<SchedulerSpec> activeSchedules = schedulerService.getActiveSchedulesForIds(taskIds);
            Map<String, List<String>> associations = new HashMap<>();

            for (SchedulerSpec spec : activeSchedules) {
                associations.put(spec.getId(), Arrays.asList(spec.getStatus().name()));
            }
            deleteResponse.setAssociations(associations);
            if (CollectionUtils.isEmpty(deleteResponse.getAssociations())) {
                List<ResiliencyScoreTask> persistedTasks = repository.findByIds(taskIds);
                List<String> persistedTaskIds =
                        persistedTasks.stream().map(ResiliencyScoreTask::getId).collect(Collectors.toList());
                taskIds.removeAll(persistedTaskIds);
                if (!taskIds.isEmpty()) {
                    throw new MangleException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.TASK, taskIds.toString());
                }
                repository.deleteByIdIn(persistedTaskIds);
            } else {
                deleteResponse.setResponseMessage(ErrorConstants.RESILIENCY_SCORE_TASK_DELETION_PRECHECK_FAIL);
            }
            return deleteResponse;
        } else {
            log.warn(ErrorConstants.TASK_ID + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.TASK_ID);
        }
    }
}
