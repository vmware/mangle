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

package com.vmware.mangle.services.cassandra.converters;

import com.datastax.driver.core.Row;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.cassandra.core.convert.CassandraConverter;
import org.springframework.data.cassandra.core.convert.MappingCassandraConverter;
import org.springframework.data.convert.ReadingConverter;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;

/**
 * Task Reading Converter.
 *
 * @author chetanc
 */
@ReadingConverter
@Log4j2
public class TaskReadingConverter implements Converter<Row, Task<TaskSpec>> {

    private CassandraConverter cassandraConverter;

    public TaskReadingConverter(MappingCassandraConverter mappingCassandraConverter) {
        this.cassandraConverter = mappingCassandraConverter;
        mappingCassandraConverter.afterPropertiesSet();
    }

    @Override
    public Task<TaskSpec> convert(Row source) {
        String taskClass = source.getString("taskclass");
        log.debug("Task Id : " + source.getString("id"));
        Task<TaskSpec> task = null;
        try {
            task = (Task<TaskSpec>) cassandraConverter.read(Class.forName(taskClass), source);
        } catch (ClassNotFoundException e) {
            log.error("Error converting task object: " + e.getStackTrace());
        }
        return task;
    }
}
