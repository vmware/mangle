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
import org.json.JSONObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * TaskSpec Reading Converter.
 *
 * @author kumargautam
 */
@ReadingConverter
@Log4j2
public class TaskSpecReadingConverter implements Converter<String, TaskSpec> {

    private Gson gson;

    public TaskSpecReadingConverter() {
        gson = new Gson();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public TaskSpec convert(String source) {
        log.debug("Start execution of convert() method...");
        try {


            JSONObject jsonObject = new JSONObject(source);
            String specClassName = jsonObject.getString("specType");
            Class specClass = Class.forName(specClassName);
            TaskSpec taskSpec = (TaskSpec) gson.fromJson(source, specClass);
            if (specClassName.equals(K8SFaultTriggerSpec.class.getName())) {
                String childSpecClassName = jsonObject.getString("childSpecType");
                Class childSpecClass = Class.forName(childSpecClassName);
                CommandExecutionFaultSpec childSpec = (CommandExecutionFaultSpec) gson
                        .fromJson(jsonObject.get("faultSpec").toString(), childSpecClass);
                ((K8SFaultTriggerSpec) taskSpec).setFaultSpec(childSpec);
            }
            return taskSpec;
        } catch (Exception e) {
            throw new MangleRuntimeException(e, ErrorCode.GENERIC_ERROR);
        }
    }

}
