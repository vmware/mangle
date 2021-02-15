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

package com.vmware.mangle.unittest.services.service.updateutils;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
import com.vmware.mangle.cassandra.model.faults.specs.EndpointGroupFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.FaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.SchedulerService;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.events.web.CustomEventPublisher;
import com.vmware.mangle.services.mockdata.EndpointMockData;
import com.vmware.mangle.services.mockdata.TasksMockData;
import com.vmware.mangle.services.repository.EndpointRepository;
import com.vmware.mangle.services.updateutils.EndpointUpdateService;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author bkaranam
 *
 *
 */
public class EndpointUpdateServiceTest {

    @Mock
    private EndpointRepository endpointRepository;

    @Mock
    private SchedulerService schedulerService;

    @Mock
    CustomEventPublisher eventPublisher;

    @Mock
    private TaskService taskService;
    private EndpointMockData mockData = new EndpointMockData();
    private EndpointSpec endpointSpec;
    private EndpointSpec rmGroupEndpointSpec;
    private TasksMockData<TaskSpec> tasksMockData;

    private EndpointUpdateService updateService;


    @BeforeClass
    public void initMockData() {
        tasksMockData = new TasksMockData<>(new CommandExecutionFaultSpec());
    }


    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        updateService = new EndpointUpdateService(endpointRepository, schedulerService, taskService, eventPublisher);
        this.endpointSpec = mockData.rmEndpointMockData();
        this.rmGroupEndpointSpec = mockData.rmEndpointGroupMockData();
    }

    /**
     * Test method for
     * {@link EndpointUpdateService#updateEndpointByEndpointName(String, EndpointSpec)}
     *
     * @throws MangleException
     */
    @Test
    public void testUpdateEndpointByEndpointName() throws MangleException {
        endpointSpec = mockData.rmEndpointMockData();

        List<Task<TaskSpec>> tasks = tasksMockData.getDummy1Tasks();
        List<Task<TaskSpec>> updatedTasks = tasks.stream().map(taskSpecTask -> {
            ((FaultSpec) taskSpecTask.getTaskData()).setEndpointName(endpointSpec.getName());
            return taskSpecTask;
        }).collect(Collectors.toList());

        Optional<EndpointSpec> optional = Optional.of(endpointSpec);
        when(endpointRepository.findByName(anyString())).thenReturn(optional);
        when(endpointRepository.save(any(EndpointSpec.class))).thenReturn(endpointSpec);
        when(schedulerService.getActiveScheduleJobs()).thenReturn(Arrays.asList(updatedTasks.get(0).getId()));
        when(taskService.getTasksByIds(any())).thenReturn(updatedTasks);
        when(endpointRepository.findByEndPointType(EndpointType.ENDPOINT_GROUP))
                .thenReturn(Arrays.asList(rmGroupEndpointSpec));

        doNothing().when(eventPublisher).publishEvent(any());
        EndpointSpec actualResult =
                updateService.updateEndpointByEndpointName(endpointSpec.getName(), this.endpointSpec);
        verify(endpointRepository, times(1)).findByName(anyString());
        verify(endpointRepository, times(1)).save(any(EndpointSpec.class));
        Assert.assertEquals(actualResult, endpointSpec);
    }


    /**
     * Test method for
     * {@link EndpointUpdateService#updateEndpointByEndpointName(String, EndpointSpec)}
     *
     * @throws MangleException
     */
    @Test
    public void testUpdateEndpointByEndpointNameWithActiveSchedulesOnK8S() throws MangleException {
        endpointSpec = mockData.rmEndpointMockData();
        TasksMockData<TaskSpec> tasksMockData = new TasksMockData<>(new K8SFaultTriggerSpec());
        List<Task<TaskSpec>> tasks = tasksMockData.getDummy1Tasks();
        for (Task<TaskSpec> task : tasks) {
            ((K8SFaultTriggerSpec) task.getTaskData()).setFaultSpec(new CommandExecutionFaultSpec());
            ((K8SFaultTriggerSpec) task.getTaskData()).getFaultSpec().setEndpointName(UUID.randomUUID().toString());
        }
        Optional<EndpointSpec> optional = Optional.of(endpointSpec);
        when(endpointRepository.findByName(anyString())).thenReturn(optional);
        when(endpointRepository.save(any(EndpointSpec.class))).thenReturn(endpointSpec);
        when(schedulerService.getActiveScheduleJobs()).thenReturn(Arrays.asList(tasks.get(0).getId()));
        when(taskService.getTasksByIds(any())).thenReturn(tasks);
        when(endpointRepository.findByEndPointType(EndpointType.ENDPOINT_GROUP))
                .thenReturn(Arrays.asList(rmGroupEndpointSpec));

        doNothing().when(eventPublisher).publishEvent(any());
        EndpointSpec actualResult =
                updateService.updateEndpointByEndpointName(endpointSpec.getName(), this.endpointSpec);
        verify(endpointRepository, times(1)).findByName(anyString());
        verify(endpointRepository, times(1)).save(any(EndpointSpec.class));
        Assert.assertEquals(actualResult, endpointSpec);
    }

    /**
     * Test method for
     * {@link EndpointUpdateService#updateEndpointByEndpointName(String, EndpointSpec)}
     *
     * @throws MangleException
     */
    @Test
    public void testUpdateEndpointByEndpointNameWithActiveSchedulesOnEndpointGroup() throws MangleException {
        endpointSpec = mockData.rmEndpointMockData();
        TasksMockData<TaskSpec> tasksMockData = new TasksMockData<>(new EndpointGroupFaultTriggerSpec());
        List<Task<TaskSpec>> tasks = tasksMockData.getDummy1Tasks();
        for (Task<TaskSpec> task : tasks) {
            ((EndpointGroupFaultTriggerSpec) task.getTaskData()).setFaultSpec(new CommandExecutionFaultSpec());
            ((EndpointGroupFaultTriggerSpec) task.getTaskData()).getFaultSpec()
                    .setEndpointName(UUID.randomUUID().toString());
        }
        Optional<EndpointSpec> optional = Optional.of(endpointSpec);
        when(endpointRepository.findByName(anyString())).thenReturn(optional);
        when(endpointRepository.save(any(EndpointSpec.class))).thenReturn(endpointSpec);
        when(schedulerService.getActiveScheduleJobs()).thenReturn(Arrays.asList(tasks.get(0).getId()));
        when(taskService.getTasksByIds(any())).thenReturn(tasks);
        when(endpointRepository.findByEndPointType(EndpointType.ENDPOINT_GROUP))
                .thenReturn(Arrays.asList(rmGroupEndpointSpec));

        doNothing().when(eventPublisher).publishEvent(any());
        EndpointSpec actualResult =
                updateService.updateEndpointByEndpointName(endpointSpec.getName(), this.endpointSpec);
        verify(endpointRepository, times(1)).findByName(anyString());
        verify(endpointRepository, times(1)).save(any(EndpointSpec.class));
        Assert.assertEquals(actualResult, endpointSpec);
    }

    /**
     * Test method for
     * {@link EndpointUpdateService#updateEndpointByEndpointName(String, EndpointSpec)}
     *
     * @throws MangleException
     */
    @Test
    public void testUpdateEndpointByEndpointNameWithDuplicateRecord() throws MangleException {
        endpointSpec = mockData.rmEndpointMockData();

        List<Task<TaskSpec>> tasks = tasksMockData.getDummy1Tasks();
        List<Task<TaskSpec>> updatedTasks = tasks.stream().map(taskSpecTask -> {
            ((FaultSpec) taskSpecTask.getTaskData()).setEndpointName(endpointSpec.getName());
            return taskSpecTask;
        }).collect(Collectors.toList());

        Optional<EndpointSpec> optional = Optional.of(endpointSpec);
        when(endpointRepository.findByName(anyString())).thenReturn(optional);
        try {
            updateService.updateEndpointByEndpointName(endpointSpec.getName(), mockData.dockerEndpointMockData());
            fail("UpdateEndpointByEndpointName with duplicate endpoint on different endpoint type failed to throw "
                    + ErrorCode.DUPLICATE_RECORD_FOR_ENDPOINT + " exception");
        } catch (MangleException exception) {
            assertEquals(exception.getErrorCode(), ErrorCode.DUPLICATE_RECORD_FOR_ENDPOINT);
            verify(endpointRepository, times(1)).findByName(anyString());
            verify(endpointRepository, times(0)).save(any(EndpointSpec.class));
        }
    }


    /**
     * Test method for
     * {@link EndpointUpdateService#updateEndpointByEndpointName(String, EndpointSpec)} =
     *
     * @throws MangleException
     */
    @Test
    public void testUpdateEndpointByEndpointNameWithNull() {
        endpointSpec = mockData.rmEndpointMockData();
        Optional<EndpointSpec> optional = Optional.of(endpointSpec);
        when(endpointRepository.findByName(anyString())).thenReturn(optional);
        when(endpointRepository.save(any(EndpointSpec.class))).thenReturn(endpointSpec);
        when(schedulerService.getActiveScheduleJobs()).thenReturn(new ArrayList<>());
        doNothing().when(eventPublisher).publishEvent(any());
        boolean actualResult = false;
        try {
            updateService.updateEndpointByEndpointName(null, this.endpointSpec);
        } catch (Exception e) {
            actualResult = true;
        }
        verify(endpointRepository, times(0)).findByName(anyString());
        verify(endpointRepository, times(0)).save(any(EndpointSpec.class));
        Assert.assertTrue(actualResult);
    }

    /**
     * Test method for
     * {@link EndpointUpdateService#updateEndpointByEndpointName(String, EndpointSpec)}
     *
     * @throws MangleException
     */
    @Test
    public void testUpdateEndpointByEndpointNameWithEmpty() {
        endpointSpec = mockData.rmEndpointMockData();
        Optional<EndpointSpec> optional = Optional.empty();
        when(endpointRepository.findByName(anyString())).thenReturn(optional);
        when(endpointRepository.save(any(EndpointSpec.class))).thenReturn(endpointSpec);
        when(schedulerService.getActiveScheduleJobs()).thenReturn(new ArrayList<>());
        doNothing().when(eventPublisher).publishEvent(any());
        boolean actualResult = false;
        try {
            updateService.updateEndpointByEndpointName(endpointSpec.getName(), this.endpointSpec);
        } catch (Exception e) {
            actualResult = true;
        }
        verify(endpointRepository, times(1)).findByName(anyString());
        verify(endpointRepository, times(0)).save(any(EndpointSpec.class));
        Assert.assertTrue(actualResult);
    }

}
