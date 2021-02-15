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

package com.vmware.mangle.unittest.services.service.resiliencyscore;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.resiliencyscore.QueryDto;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreTask;
import com.vmware.mangle.cassandra.model.resiliencyscore.Service;
import com.vmware.mangle.cassandra.model.scheduler.SchedulerSpec;
import com.vmware.mangle.model.response.DeleteOperationResponse;
import com.vmware.mangle.services.SchedulerService;
import com.vmware.mangle.services.mockdata.MockDataConstants;
import com.vmware.mangle.services.mockdata.ResiliencyScoreMockData;
import com.vmware.mangle.services.mockdata.SchedulerControllerMockData;
import com.vmware.mangle.services.repository.QueryRepository;
import com.vmware.mangle.services.repository.ResiliencyScoreRepository;
import com.vmware.mangle.services.repository.ServiceRepository;
import com.vmware.mangle.services.resiliencyscore.ResiliencyScoreService;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author dbhat
 */
public class ResiliencyScoreServiceTest {
    @Mock
    private ResiliencyScoreRepository resiliencyScoreRepository;
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private QueryRepository queryRepository;
    @Mock
    private SchedulerService schedulerService;

    @InjectMocks
    private ResiliencyScoreService resiliencyScoreService;

    @BeforeMethod
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(priority = 1, description = "Validating retrieval of tasks when specified task ID is null")
    public void getTaskWhenTaskIdIsNull() throws MangleException {
        try {
            resiliencyScoreService.getTaskById(null);
            Assert.fail("Expected MangleRunTime Exception is not thrown");
        } catch (MangleRuntimeException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
        }
    }

    @Test(priority = 2, description = " Retrieval of non existing task")
    public void getTaskWhenSpecifiedTaskDoesnotExists() throws MangleException {
        ResiliencyScoreTask resiliencyScoreTask = ResiliencyScoreMockData.getResiliencyScoreTask1();
        Optional<ResiliencyScoreTask> optional = Optional.empty();
        when(resiliencyScoreRepository.findById(resiliencyScoreTask.getId())).thenReturn(optional);

        try {
            resiliencyScoreService.getTaskById(resiliencyScoreTask.getId());
            Assert.fail("Expected MangleRunTime Exception is not thrown");
        } catch (MangleRuntimeException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
        }
    }

    @Test(priority = 3, description = " Retrieval of existing task")
    public void getTask() throws MangleException {
        ResiliencyScoreTask resiliencyScoreTask = ResiliencyScoreMockData.getResiliencyScoreTask1();
        Optional<ResiliencyScoreTask> optional = Optional.of(resiliencyScoreTask);
        when(resiliencyScoreRepository.findById(resiliencyScoreTask.getId())).thenReturn(optional);

        ResiliencyScoreTask data = resiliencyScoreService.getTaskById(resiliencyScoreTask.getId());
        Assert.assertEquals(data.getTaskStatus(), resiliencyScoreTask.getTaskStatus());
    }

    @Test(priority = 4, description = "Validate adding task when task is null")
    public void addTaskWhenTaskIsNull() throws MangleException {
        try {
            resiliencyScoreService.addOrUpdateTask(null);
            Assert.fail("Expected MangleRunTime Exception is not thrown");
        } catch (MangleRuntimeException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
        }
    }

    @Test(priority = 5, description = "Save the task to DB ")
    public void addTaskToDb() throws MangleException {
        ResiliencyScoreTask resiliencyScoreTask = ResiliencyScoreMockData.getResiliencyScoreTask1();
        when(resiliencyScoreRepository.save(resiliencyScoreTask)).thenReturn(resiliencyScoreTask);
        ResiliencyScoreTask saved = resiliencyScoreService.addOrUpdateTask(resiliencyScoreTask);
        Assert.assertEquals(saved.getTaskStatus(), resiliencyScoreTask.getTaskStatus());
    }

    @Test(priority = 6, description = "Get Service details by service name")
    public void getServiceByName() {
        Service service = ResiliencyScoreMockData.getServiceProperties();
        Optional<Service> optional = Optional.of(service);

        when(serviceRepository.findById(anyString())).thenReturn(optional);
        Service searchResult = resiliencyScoreService.getServiceDetails(service.getName());
        Assert.assertEquals(searchResult.getName(), service.getName());
    }

    @Test(priority = 7, description = "Retrieve non existing service")
    public void getNonExistingService() {
        Service service = ResiliencyScoreMockData.getServiceProperties();
        Optional<Service> optionalEmpty = Optional.empty();

        when(serviceRepository.findById(anyString())).thenReturn(optionalEmpty);
        Service serviceResult = resiliencyScoreService.getServiceDetails(service.getName());
        Assert.assertNull(serviceResult);
    }

    @Test(priority = 8, description = "Delete non existing resiliency score task ")
    public void deleteNonExistingTask() {
        Optional<ResiliencyScoreTask> optionalEmpty = Optional.empty();
        when(resiliencyScoreRepository.findById(anyString())).thenReturn(optionalEmpty);
        try {
            resiliencyScoreService.deleteResiliencyScoreById(ResiliencyScoreMockData.getRandomUUID());
            Assert.fail("Expected MangleRunTime Exception is not thrown");
        } catch (MangleRuntimeException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
        }
    }

    @Test(priority = 9, description = "Delete existing resiliency score task ")
    public void deleteTask() throws MangleRuntimeException {
        ResiliencyScoreTask resiliencyScoreTask = ResiliencyScoreMockData.getResiliencyScoreTask1();
        Optional<ResiliencyScoreTask> optional = Optional.of(resiliencyScoreTask);
        when(resiliencyScoreRepository.findById(anyString())).thenReturn(optional);

        boolean status = resiliencyScoreService.deleteResiliencyScoreById(ResiliencyScoreMockData.getRandomUUID());
        Assert.assertTrue(status, "Deleting of Resiliency score task has failed");
    }

    @Test(priority = 10, description = "Retrieve all the services")
    public void getAllServices() {
        Service service = ResiliencyScoreMockData.getServiceProperties();
        List<Service> services = new ArrayList<>();
        services.add(service);
        when(serviceRepository.findAll()).thenReturn(services);
        when(serviceRepository.findByName(any())).thenReturn(services);

        List<Service> response = resiliencyScoreService.getAllServices();
        Assert.assertTrue(response.size() > 0);
    }

    @Test(priority = 11, description = "Validating save data when service is null")
    public void saveServiceWhenNull() {
        Service service = ResiliencyScoreMockData.getServiceProperties();
        when(serviceRepository.save(any())).thenReturn(service);
        try {
            resiliencyScoreService.addOrUpdateService(null);
            Assert.fail("Expected MangleRunTime Exception is not thrown");
        } catch (MangleRuntimeException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
        }
    }

    @Test(priority = 12, description = "Validating persisting of Service data")
    public void saveService() {
        Service service = ResiliencyScoreMockData.getServiceProperties();
        when(serviceRepository.save(any())).thenReturn(service);

        Service response = resiliencyScoreService.addOrUpdateService(service);
        Assert.assertEquals(response.getName(), service.getName());
    }

    @Test(priority = 13, description = "Get all services")
    public void updateServiceWithNullId() {
        Service service = ResiliencyScoreMockData.getServiceProperties();
        when(serviceRepository.save(any())).thenReturn(service);
        try {
            resiliencyScoreService.updateService(null, service);
            Assert.fail("Expected mangle exception as the specified ID was null");
        } catch (MangleException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
            verify(serviceRepository, times(0)).save(any());
            verify(serviceRepository, times(0)).findById(anyString());
        }
    }

    @Test(priority = 14, description = "Get all services when the specified service spec is null")
    public void updateServiceWithNullSpec() {
        Service service = ResiliencyScoreMockData.getServiceProperties();
        when(serviceRepository.save(any())).thenReturn(service);
        try {
            resiliencyScoreService.updateService(service.getId(), null);
            Assert.fail("Expected mangle exception as the specified spec was null");
        } catch (MangleException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
            verify(serviceRepository, times(0)).save(any());
            verify(serviceRepository, times(0)).findById(anyString());
        }
    }

    @Test(priority = 15, description = "Get all services spec with specified ID is not found")
    public void updateServiceWhenNoSpecsFound() throws MangleException {
        Service service = ResiliencyScoreMockData.getServiceProperties();
        Optional<Service> optional = Optional.empty();

        when(serviceRepository.findById(anyString())).thenReturn(optional);
        when(serviceRepository.save(any())).thenReturn(service);
        try {
            resiliencyScoreService.updateService(service.getId(), service);
            Assert.fail(" Expected mangle exception when there are no specified service ids found ");
        } catch (MangleRuntimeException mangleRunTimeException) {
            Assert.assertEquals(mangleRunTimeException.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
            verify(serviceRepository, times(1)).findById(anyString());
            verify(serviceRepository, times(0)).save(any());
        }
    }

    @Test(priority = 16, description = "Get all services with all valid data")
    public void updateService() throws MangleException {
        Service service = ResiliencyScoreMockData.getServiceProperties();
        Optional<Service> optional = Optional.of(service);

        when(serviceRepository.findById(anyString())).thenReturn(optional);
        when(serviceRepository.save(any())).thenReturn(service);

        Service serviceUpdated = resiliencyScoreService.updateService(service.getId(), service);

        verify(serviceRepository, times(1)).findById(anyString());
        verify(serviceRepository, times(1)).save(any());
        Assert.assertEquals(serviceUpdated.getName(), service.getName());
    }

    @Test(priority = 17, description = "Deleting of service when specified service ID is not present")
    public void deleteNonExistingService() throws MangleException {
        Optional<Service> optional = Optional.empty();
        when(serviceRepository.findById(anyString())).thenReturn(optional);
        try {
            resiliencyScoreService.deleteServiceById(ResiliencyScoreMockData.getRandomUUID());
            Assert.fail("Expected MangleRunTimeException as the specified ID is not in Db");
        } catch (MangleRuntimeException mangleRunTimeException) {
            Assert.assertEquals(mangleRunTimeException.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
            verify(serviceRepository, times(1)).findById(anyString());
            verify(serviceRepository, times(0)).deleteById(anyString());
        }
    }

    @Test(priority = 18, description = "Deleting of service when specified service ID is null")
    public void deleteNullServiceId() {
        try {
            resiliencyScoreService.deleteServiceById(null);
            Assert.fail("Expected MangleRunTimeException as the specified ID is null");
        } catch (MangleException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
            verify(serviceRepository, times(0)).findById(anyString());
            verify(serviceRepository, times(0)).deleteById(anyString());
        }
    }

    @Test(priority = 19, description = "Delete service with all valid data")
    public void deleteServiceId() throws MangleException {
        Service service = ResiliencyScoreMockData.getServiceProperties();
        Optional<Service> optional = Optional.of(service);
        Optional<Service> optionalEmpty = Optional.empty();

        when(serviceRepository.findById(anyString())).thenReturn(optional).thenReturn(optionalEmpty);
        doNothing().when(serviceRepository).deleteById(anyString());

        boolean status = resiliencyScoreService.deleteServiceById(service.getId());

        verify(serviceRepository, times(2)).findById(anyString());
        verify(serviceRepository, times(1)).deleteById(anyString());
        Assert.assertTrue(status);
    }

    @Test(priority = 20, description = "Add query with null spec ")
    public void addNullQuerySpec() {
        try {
            resiliencyScoreService.addQuery(null);
            Assert.fail("Expected exception haven't observed. We cannot save a null spec");
        } catch (MangleRuntimeException mangleRunTimeException) {
            Assert.assertEquals(mangleRunTimeException.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
            verify(queryRepository, times(0)).save(any());
        }
    }

    @Test(priority = 21, description = "Validate adding a query definition ")
    public void addQuerySpec() throws MangleRuntimeException {
        QueryDto querySpec = ResiliencyScoreMockData.getQueryProperties();
        when(queryRepository.save(any())).thenReturn(querySpec);
        QueryDto queryPersisted = resiliencyScoreService.addQuery(querySpec);

        verify(queryRepository, times(1)).save(any());
        Assert.assertEquals(queryPersisted.getId(), querySpec.getId());
    }

    @Test(priority = 22, description = "Get all the Queries in the system")
    public void getAllQueries() {
        QueryDto query = ResiliencyScoreMockData.getQueryProperties();
        List<QueryDto> queries = new ArrayList<>();
        queries.add(query);

        when(queryRepository.findAll()).thenReturn(queries);
        when(queryRepository.findByName(anyString())).thenReturn(queries);
        List<QueryDto> allQueries = resiliencyScoreService.getAllQueries();
        verify(queryRepository, times(1)).findAll();
        Assert.assertTrue(allQueries.size() > 0);
    }

    @Test(priority = 23, description = "Validate updating query for null query name")
    public void updateQueryWithNullQueryName() {
        QueryDto querySpec = ResiliencyScoreMockData.getQueryProperties();
        try {
            resiliencyScoreService.updateQuery(null, querySpec);
            Assert.fail("Expected an exception as we tried to save query spec with null input");
        } catch (MangleException mangleException) {
            verify(queryRepository, times(0)).save(any());
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
        }
    }

    @Test(priority = 24, description = "Validate updating query with null querySpec")
    public void updateQueryWithNullQuerySpec() {
        try {
            resiliencyScoreService.updateQuery(MockDataConstants.QUERY_NAME, null);
            Assert.fail("Expected an exception as we tried to save query spec with null input");
        } catch (MangleException mangleException) {
            verify(queryRepository, times(0)).save(any());
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
        }
    }

    @Test(priority = 25, description = "Update queryspec with valid data")
    public void updateQuerySpec() throws MangleException {
        QueryDto querySpec = ResiliencyScoreMockData.getQueryProperties();
        List<QueryDto> queries = new ArrayList<>();
        queries.add(querySpec);

        when(queryRepository.findByName(any())).thenReturn(queries);
        when(queryRepository.save(any())).thenReturn(querySpec);
        QueryDto querySaved = resiliencyScoreService.updateQuery(querySpec.getName(), querySpec);

        Assert.assertEquals(querySaved.getId(), querySpec.getId());
        verify(queryRepository, times(1)).findByName(anyString());
        verify(queryRepository, times(1)).save(any());
    }

    @Test(priority = 26, description = "Validate Delete query with empty queryID")
    public void deleteQueryWithNullId() {
        try {
            resiliencyScoreService.deleteQuery(null);
            Assert.fail("Expected exception due to invalid input query ID");
        } catch (MangleException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
            verify(queryRepository, times(0)).findById(anyString());
            verify(queryRepository, times(0)).deleteById(anyString());
        }
    }

    @Test(priority = 27, description = "Validate Delete query DB operation failure")
    public void deleteQueryFailure() throws MangleException {
        QueryDto query = ResiliencyScoreMockData.getQueryProperties();
        Optional<QueryDto> optionalQuery = Optional.of(query);
        when(queryRepository.findById(anyString())).thenReturn(optionalQuery).thenReturn(optionalQuery);
        doNothing().when(queryRepository).deleteById(anyString());

        boolean status = resiliencyScoreService.deleteQuery(ResiliencyScoreMockData.getRandomUUID());
        verify(queryRepository, times(2)).findById(anyString());
        verify(queryRepository, times(1)).deleteById(anyString());
        Assert.assertFalse(status);
    }

    @Test(priority = 28, description = "Validate Delete query non existing ID")
    public void deleteQueryForNonExistingId() throws MangleException {
        Optional<QueryDto> optionalQuery = Optional.empty();
        when(queryRepository.findById(anyString())).thenReturn(optionalQuery).thenReturn(optionalQuery);

        try {
            resiliencyScoreService.deleteQuery(ResiliencyScoreMockData.getRandomUUID());
            Assert.fail("Expected exception due to invalid input query ID");
        } catch (MangleRuntimeException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
            verify(queryRepository, times(1)).findById(anyString());
            verify(queryRepository, times(0)).deleteById(anyString());
        }
    }

    @Test(priority = 29, description = "Delete query by invalid query name")
    public void deleteQueryWithInvalidName() {
        try {
            resiliencyScoreService.deleteQueryByName(null);
            Assert.fail("expected exception for the invalid input data");
        } catch (MangleException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
            verify(queryRepository, times(0)).findByName(anyString());
        }
    }

    @Test(priority = 30, description = "Validate deleting of query when specified query doesn't exist")
    public void deleteQueryWhenNoQueryExists() throws MangleException {
        when(queryRepository.findByName(anyString())).thenReturn(null);
        try {
            resiliencyScoreService.deleteQueryByName(MockDataConstants.QUERY_NAME);
            Assert.fail("expected exception for the invalid input data");
        } catch (MangleRuntimeException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
            verify(queryRepository, times(1)).findByName(anyString());
        }
    }

    @Test(priority = 31, description = "Validate deleting of all the versions of specified query name")
    public void deleteAllVersionOfQuery() throws MangleException {
        List<QueryDto> queries = new ArrayList<>();
        QueryDto query = ResiliencyScoreMockData.getQueryProperties();
        queries.add(query);
        queries.add(ResiliencyScoreMockData.getQueryProperties());
        Optional<QueryDto> optionalQuery = Optional.of(query);

        when(queryRepository.findByName(anyString())).thenReturn(queries);
        when(queryRepository.findById(anyString())).thenReturn(optionalQuery).thenReturn(Optional.empty())
                .thenReturn(optionalQuery).thenReturn(Optional.empty());
        doNothing().when(queryRepository).deleteById(anyString());

        boolean status = resiliencyScoreService.deleteQueryByName(query.getName());
        verify(queryRepository, times(4)).findById(anyString());
        verify(queryRepository, times(2)).deleteById(anyString());
        Assert.assertTrue(status);
    }

    @Test(priority = 32, description = "Get service by name")
    public void getLastUpdatedentryForServiceName() {
        Service service1 = ResiliencyScoreMockData.getServiceProperties();
        Service service2 = ResiliencyScoreMockData.getServiceProperties();
        List<Service> services = new ArrayList<>();
        services.add(service1);
        services.add(service2);
        when(serviceRepository.findByName(anyString())).thenReturn(services);

        Service serviceLastUpdated = resiliencyScoreService.getServiceByName(service1.getName());

        Assert.assertEquals(serviceLastUpdated.getLastUpdatedTime(), service2.getLastUpdatedTime());
        verify(serviceRepository, times(1)).findByName(anyString());
    }

    @Test(priority = 33, description = "Get service by name when no records found")
    public void getServiceWhenNoRecordsFound() {
        List<Service> services = new ArrayList<>();
        when(serviceRepository.findByName(anyString())).thenReturn(services);
        try {
            Service serviceLastUpdated = resiliencyScoreService.getServiceByName(MockDataConstants.SERVICE_NAME);
            Assert.fail(
                    "Expected mangle run time exception as we expect no records to be returned for the service name");
        } catch (MangleRuntimeException mangleRunTimeException) {
            Assert.assertEquals(mangleRunTimeException.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
            verify(serviceRepository, times(1)).findByName(anyString());
        }
    }

    @Test(priority = 34, description = "Validate delete Tasks operation when empty Task lists is provided")
    public void deleteTasksByIdsForEmptyTaskIds() {
        try {
            resiliencyScoreService.deleteTasksByIds(new ArrayList<>());
            Assert.fail("Expected exception isn't captured.");
        } catch (MangleException mangleException) {
            Assert.assertEquals(mangleException.getErrorCode(), ErrorCode.FIELD_VALUE_EMPTY);
        }
    }

    @Test(priority = 35, description = "Validate delete tasks when few tasks are associated with active schedule")
    public void deleteTasksAssociatedWithSchedule() throws MangleException {
        SchedulerControllerMockData schedulerMockData = new SchedulerControllerMockData();
        List<SchedulerSpec> activeSchedules = schedulerMockData.getListOfSchedulerSpec();
        List<ResiliencyScoreTask> tasks = getTasks();
        List<String> taskIds = getTaskIds(tasks);

        when(schedulerService.getActiveSchedulesForIds(taskIds)).thenReturn(activeSchedules);
        when(resiliencyScoreRepository.findByIds(any())).thenReturn(tasks);
        doNothing().when(resiliencyScoreRepository).deleteByIdIn(taskIds);

        DeleteOperationResponse deleteResponse = resiliencyScoreService.deleteTasksByIds(taskIds);
        Assert.assertFalse(deleteResponse.getAssociations().isEmpty());
        verify(schedulerService, times(1)).getActiveSchedulesForIds(any());
        verify(resiliencyScoreRepository, times(0)).findByIds(any());
        verify(resiliencyScoreRepository, times(0)).deleteByIdIn(any());
    }

    @Test(priority = 36, description = "Validate delete tasks when no tasks are associated with any schedules")
    public void deleteTasksWithoutHavingSchedule() throws MangleException {
        List<SchedulerSpec> activeSchedules = new ArrayList<>();
        List<ResiliencyScoreTask> tasks = getTasks();
        List<String> taskIds = getTaskIds(tasks);

        when(schedulerService.getActiveSchedulesForIds(taskIds)).thenReturn(activeSchedules);
        when(resiliencyScoreRepository.findByIds(any())).thenReturn(tasks);
        doNothing().when(resiliencyScoreRepository).deleteByIdIn(taskIds);

        DeleteOperationResponse deleteResponse = resiliencyScoreService.deleteTasksByIds(taskIds);
        Assert.assertTrue(deleteResponse.getAssociations().isEmpty());
        verify(schedulerService, times(1)).getActiveSchedulesForIds(any());
        verify(resiliencyScoreRepository, times(1)).findByIds(any());
        verify(resiliencyScoreRepository, times(1)).deleteByIdIn(any());
    }

    @Test(priority = 37, description = "Validate delete of non persisted tasks ")
    public void deleteNonPersistedTaskIds() throws MangleException {
        List<SchedulerSpec> activeSchedules = new ArrayList<>();
        List<ResiliencyScoreTask> tasks = getTasks();
        List<String> taskIds = getTaskIds(tasks);
        DeleteOperationResponse deleteResponse = new DeleteOperationResponse();

        when(schedulerService.getActiveSchedulesForIds(taskIds)).thenReturn(activeSchedules);
        when(resiliencyScoreRepository.findByIds(any())).thenReturn(new ArrayList<>());
        doNothing().when(resiliencyScoreRepository).deleteByIdIn(taskIds);

        try {
            deleteResponse = resiliencyScoreService.deleteTasksByIds(taskIds);
            Assert.fail("Expected the no record found exception");
        } catch (MangleException mangleException) {
            Assert.assertTrue(deleteResponse.getAssociations().isEmpty());
            verify(schedulerService, times(1)).getActiveSchedulesForIds(any());
            verify(resiliencyScoreRepository, times(1)).findByIds(any());
            verify(resiliencyScoreRepository, times(0)).deleteByIdIn(any());
        }
    }

    private List<ResiliencyScoreTask> getTasks() {
        List<ResiliencyScoreTask> tasks = new ArrayList<>();
        tasks.add(ResiliencyScoreMockData.getResiliencyScoreTask1());
        return tasks;
    }

    private List<String> getTaskIds(List<ResiliencyScoreTask> tasks) {
        List<String> taskIds = new ArrayList<>();
        for (ResiliencyScoreTask task : tasks) {
            taskIds.add(task.getId());
        }
        return taskIds;
    }
}
