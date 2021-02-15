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

package com.vmware.mangle.unittest.services.cassandra.converters;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.datastax.driver.core.Row;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.FaultTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.services.cassandra.converters.TaskReadingConverter;
import com.vmware.mangle.services.mockdata.FaultsMockData;
import com.vmware.mangle.services.mockdata.TasksMockData;

/**
 *
 *
 * @author chetanc
 */
public class TaskSpecReadingConverterTest {

    @Mock
    private MappingCassandraConverter mappingCassandraConverter;

    private TaskReadingConverter taskReadingConverter;

    private FaultsMockData faultsMockData = new FaultsMockData();
    private TasksMockData<CommandExecutionFaultSpec> tasksMockData;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        doNothing().when(mappingCassandraConverter).afterPropertiesSet();
        taskReadingConverter = new TaskReadingConverter(mappingCassandraConverter);
        tasksMockData = new TasksMockData<>(faultsMockData.getVMNicFaultSpec());
    }


    @Test
    public void testConvert() {
        Row row = mock(Row.class);
        when(row.getString(anyString())).thenReturn(FaultTask.class.getName());
        when(mappingCassandraConverter.read(any(), anyString())).thenReturn(tasksMockData.getDummy1Task());

        Task<TaskSpec> task = taskReadingConverter.convert(row);

        Assert.assertEquals(task, tasksMockData.getDummy1Task());
        verify(mappingCassandraConverter, times(1)).read(any(), anyString());
        verify(row, times(2)).getString(anyString());

    }

    @Test
    public void testConvertFailException() {
        Row row = mock(Row.class);
        when(row.getString(anyString())).thenReturn(UUID.randomUUID().toString());
        when(mappingCassandraConverter.read(any(), anyString())).thenReturn(tasksMockData.getDummy1Task());

        Task<TaskSpec> task = taskReadingConverter.convert(row);

        Assert.assertEquals(task, null);
        verify(mappingCassandraConverter, times(0)).read(any(), anyString());
        verify(row, times(2)).getString(anyString());

    }

}
