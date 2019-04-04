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

import com.google.gson.Gson;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;

/**
 * TaskSpec Writing Converter.
 *
 * @author kumargautam
 */
@WritingConverter
@Log4j2
public class TaskSpecWritingConverter implements Converter<TaskSpec, String> {
    private Gson gson;

    public TaskSpecWritingConverter() {
        gson = new Gson();
    }

    @Override
    public String convert(TaskSpec source) {
        log.debug("Start execution of convert() method...");
        return gson.toJson(source);
    }
}
