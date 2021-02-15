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

package com.vmware.mangle.unittest.services.service.deletionutils;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.FaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.model.response.DeleteEndpointOperationResponse;
import com.vmware.mangle.model.response.DeleteOperationResponse;
import com.vmware.mangle.services.SchedulerService;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.deletionutils.EndpointDeletionService;
import com.vmware.mangle.services.mockdata.EndpointMockData;
import com.vmware.mangle.services.mockdata.TasksMockData;
import com.vmware.mangle.services.repository.EndpointRepository;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author chetanc
 *
 *
 */
public class EndpointDeletionServiceTest {

    @Mock
    private EndpointRepository endpointRepository;

    @Mock
    private SchedulerService schedulerService;

    @Mock
    private TaskService taskService;
    private EndpointMockData mockData = new EndpointMockData();
    private EndpointSpec endpointSpec;
    private TasksMockData<TaskSpec> tasksMockData;

    private EndpointDeletionService deletionService;


    @BeforeClass
    public void initMockData() {
        tasksMockData = new TasksMockData<>(new CommandExecutionFaultSpec());
    }


    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        deletionService = new EndpointDeletionService(endpointRepository, schedulerService, taskService);
        this.endpointSpec = mockData.rmEndpointMockData();
    }


    @Test
    public void testDeleteEndpointByNames() throws MangleException {

        List<String> endpointNameList = new ArrayList<>();
        endpointNameList.add(endpointSpec.getName());

        doNothing().when(endpointRepository).deleteByNameIn(any());
        when(taskService.getTasksByIds(any())).thenReturn(new ArrayList<>());
        when(schedulerService.getActiveScheduleJobs()).thenReturn(new ArrayList<>());
        when(endpointRepository.findByNames(endpointNameList)).thenReturn(Collections.singletonList(endpointSpec));


        DeleteOperationResponse actualResult = deletionService.deleteEndpointByNames(endpointNameList);
        Assert.assertEquals(0, actualResult.getAssociations().size());
        verify(endpointRepository, times(1)).deleteByNameIn(any());
        verify(taskService, times(1)).getTasksByIds(any());
        verify(schedulerService, times(1)).getActiveScheduleJobs();

        Assert.assertTrue(true);
    }

    @Test(expectedExceptions = MangleException.class)
    public void testDeleteEndpointByNamesWithNoRecordFoundError() throws MangleException {

        List<String> endpointNameList = new ArrayList<>();
        endpointNameList.add(endpointSpec.getName());

        doNothing().when(endpointRepository).deleteByNameIn(any());
        when(taskService.getTasksByIds(any())).thenReturn(new ArrayList<>());
        when(schedulerService.getActiveScheduleJobs()).thenReturn(new ArrayList<>());
        when(endpointRepository.findByNames(endpointNameList)).thenReturn(Collections.emptyList());

        try {
            deletionService.deleteEndpointByNames(endpointNameList);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
            verify(endpointRepository, times(0)).deleteByNameIn(any());
            verify(taskService, times(0)).getTasksByIds(any());
            verify(schedulerService, times(0)).getActiveScheduleJobs();
            verify(endpointRepository, times(1)).findByNames(any());
            throw e;
        }


        Assert.assertTrue(true);
    }

    @Test
    public void testDeleteEndpointByNamesWithNull() {
        doNothing().when(endpointRepository).deleteByNameIn(any());
        List<String> endpointNameList = new ArrayList<>();
        boolean actualResult = false;
        try {
            deletionService.deleteEndpointByNames(endpointNameList);
        } catch (Exception e) {
            actualResult = true;
        }
        verify(endpointRepository, times(0)).deleteByNameIn(any());
        Assert.assertTrue(actualResult);
    }

    @Test
    public void testDeleteEndpointByName() throws MangleException {
        doNothing().when(endpointRepository).deleteByName(anyString());
        boolean actualResult = deletionService.deleteEndpointByName(endpointSpec.getName());
        verify(endpointRepository, times(1)).deleteByName(anyString());
        Assert.assertTrue(actualResult);
    }

    @Test
    public void testEndpointDeletionForPrecheckOfNoAssociationWithActiveSchedule() throws MangleException {
        List<Task<TaskSpec>> tasks = tasksMockData.getDummy1Tasks();

        List<String> endpointNameList = new ArrayList<>();
        endpointNameList.add(endpointSpec.getName());

        doNothing().when(endpointRepository).deleteByNameIn(any());
        when(taskService.getTasksByIds(any())).thenReturn(tasks);
        when(schedulerService.getActiveScheduleJobs()).thenReturn(new ArrayList<>());
        when(endpointRepository.findByNames(endpointNameList)).thenReturn(Collections.singletonList(endpointSpec));

        DeleteOperationResponse actualResult = deletionService.deleteEndpointByNames(endpointNameList);
        Assert.assertEquals(actualResult.getAssociations().size(), 0);
        verify(endpointRepository, times(1)).deleteByNameIn(any());
        verify(taskService, times(1)).getTasksByIds(any());
        verify(schedulerService, times(1)).getActiveScheduleJobs();

        Assert.assertTrue(true);
    }

    @Test
    public void testEndpointDeletionForPrecheckOfNoAssociationWithActiveScheduleOnK8s() throws MangleException {
        TasksMockData<TaskSpec> tasksMockData = new TasksMockData<>(new K8SFaultTriggerSpec());
        List<Task<TaskSpec>> tasks = tasksMockData.getDummy1Tasks();
        for (Task<TaskSpec> task : tasks) {
            ((K8SFaultTriggerSpec) task.getTaskData()).setFaultSpec(new CommandExecutionFaultSpec());
            ((K8SFaultTriggerSpec) task.getTaskData()).getFaultSpec().setEndpointName(UUID.randomUUID().toString());
        }
        List<String> endpointNameList = new ArrayList<>();
        endpointNameList.add(endpointSpec.getName());

        doNothing().when(endpointRepository).deleteByNameIn(any());
        when(taskService.getTasksByIds(any())).thenReturn(tasks);
        when(schedulerService.getActiveScheduleJobs()).thenReturn(new ArrayList<>());
        when(endpointRepository.findByNames(endpointNameList)).thenReturn(Collections.singletonList(endpointSpec));

        DeleteOperationResponse actualResult = deletionService.deleteEndpointByNames(endpointNameList);
        Assert.assertEquals(actualResult.getAssociations().size(), 0);
        verify(endpointRepository, times(1)).deleteByNameIn(any());
        verify(taskService, times(1)).getTasksByIds(any());
        verify(schedulerService, times(1)).getActiveScheduleJobs();

        Assert.assertTrue(true);
    }

    @Test(dependsOnMethods = { "testEndpointDeletionForPrecheckOfNoAssociationWithActiveSchedule",
            "testEndpointDeletionForPrecheckOfNoAssociationWithActiveScheduleOnK8s" })
    public void testEndpointDeletionForPrecheckOfAssociationWithActiveSchedules() throws MangleException {
        List<Task<TaskSpec>> tasks = tasksMockData.getDummy1Tasks();
        List<Task<TaskSpec>> updatedTasks = tasks.stream().map(taskSpecTask -> {
            ((FaultSpec) taskSpecTask.getTaskData()).setEndpointName(endpointSpec.getName());
            return taskSpecTask;
        }).collect(Collectors.toList());

        List<String> endpointNameList = new ArrayList<>();
        endpointNameList.add(endpointSpec.getName());

        doNothing().when(endpointRepository).deleteByNameIn(any());
        when(taskService.getTasksByIds(any())).thenReturn(updatedTasks);
        when(schedulerService.getActiveScheduleJobs()).thenReturn(new ArrayList<>());
        when(endpointRepository.findByNames(endpointNameList)).thenReturn(Collections.singletonList(endpointSpec));

        DeleteOperationResponse actualResult = deletionService.deleteEndpointByNames(endpointNameList);
        Assert.assertEquals(actualResult.getAssociations().size(), 1);
        verify(endpointRepository, times(0)).deleteByNameIn(any());
        verify(taskService, times(1)).getTasksByIds(any());
        verify(schedulerService, times(1)).getActiveScheduleJobs();

        Assert.assertTrue(true);
    }

    @Test
    public void testDeleteEndpointByNameWithNull() throws MangleException {
        doNothing().when(endpointRepository).deleteByName(anyString());
        boolean actualResult = false;
        try {
            deletionService.deleteEndpointByName(null);
        } catch (Exception e) {
            actualResult = true;
        }
        verify(endpointRepository, times(0)).deleteByName(anyString());
        Assert.assertTrue(actualResult);
    }

    @Test
    public void testEndpointDeletionForPrecheckOfAssociationWithChildEndpoint() throws MangleException {
        EndpointSpec dbEndpointSpec = mockData.getDatabaseEndpointSpec();
        dbEndpointSpec.setEndpointNames(Collections.emptyList());
        EndpointSpec dbEndpointSpec1 = mockData.getDatabaseEndpointSpec();
        dbEndpointSpec1.setName(dbEndpointSpec1.getName() + "1");
        dbEndpointSpec1.getDatabaseConnectionProperties().setParentEndpointName(
                dbEndpointSpec1.getDatabaseConnectionProperties().getParentEndpointName() + "_1");
        dbEndpointSpec1.setEndpointNames(Collections.emptyList());
        List<Task<TaskSpec>> tasks = tasksMockData.getDummy1Tasks();

        List<String> endpointNameList = new ArrayList<>();
        endpointNameList.add(endpointSpec.getName());
        endpointNameList.add(dbEndpointSpec.getName());

        doNothing().when(endpointRepository).deleteByNameIn(any());
        when(taskService.getTasksByIds(any())).thenReturn(tasks);
        when(schedulerService.getActiveScheduleJobs()).thenReturn(new ArrayList<>());
        when(endpointRepository.findByNames(endpointNameList)).thenReturn(Arrays.asList(endpointSpec, dbEndpointSpec));
        when(endpointRepository.findByEndPointType(any(EndpointType.class)))
                .thenReturn(Arrays.asList(dbEndpointSpec, dbEndpointSpec1));

        DeleteEndpointOperationResponse actualResult = deletionService.deleteEndpointByNames(endpointNameList);
        Assert.assertEquals(actualResult.getEndpointChildAssociations().size(), 1);
        verify(endpointRepository, times(0)).deleteByNameIn(any());
        verify(endpointRepository, times(2)).findByEndPointType(any(EndpointType.class));
        verify(taskService, times(1)).getTasksByIds(any());
        verify(schedulerService, times(1)).getActiveScheduleJobs();

        Assert.assertTrue(true);
    }

    @Test
    public void testEndpointDeletionForPrecheckOfAssociationWithEndpointGroups() throws MangleException {
        EndpointSpec dbEndpointSpec = mockData.getDatabaseEndpointSpec();
        dbEndpointSpec.setEndpointNames(Arrays.asList(endpointSpec.getName()));
        EndpointSpec dbEndpointSpec1 = mockData.getDatabaseEndpointSpec();
        dbEndpointSpec1.setName(dbEndpointSpec1.getName() + "1");
        dbEndpointSpec1.getDatabaseConnectionProperties().setParentEndpointName(
                dbEndpointSpec1.getDatabaseConnectionProperties().getParentEndpointName() + "_1");
        dbEndpointSpec1.setEndpointNames(Collections.emptyList());
        List<Task<TaskSpec>> tasks = tasksMockData.getDummy1Tasks();

        List<String> endpointNameList = new ArrayList<>();
        endpointNameList.add(endpointSpec.getName());

        doNothing().when(endpointRepository).deleteByNameIn(any());
        when(taskService.getTasksByIds(any())).thenReturn(tasks);
        when(schedulerService.getActiveScheduleJobs()).thenReturn(new ArrayList<>());
        when(endpointRepository.findByNames(endpointNameList)).thenReturn(Collections.singletonList(endpointSpec));
        when(endpointRepository.findByEndPointType(any(EndpointType.class)))
                .thenReturn(Arrays.asList(dbEndpointSpec, dbEndpointSpec1));

        DeleteEndpointOperationResponse actualResult = deletionService.deleteEndpointByNames(endpointNameList);
        Assert.assertEquals(actualResult.getEndpointGroupAssociations().size(), 1);
        verify(endpointRepository, times(0)).deleteByNameIn(any());
        verify(endpointRepository, times(1)).findByEndPointType(any(EndpointType.class));
        verify(taskService, times(1)).getTasksByIds(any());
        verify(schedulerService, times(1)).getActiveScheduleJobs();

        Assert.assertTrue(true);
    }
}
