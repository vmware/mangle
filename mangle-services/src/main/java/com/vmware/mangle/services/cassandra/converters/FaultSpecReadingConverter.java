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

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.FaultSpec;
import com.vmware.mangle.services.PluginService;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 *
 *
 * @author chetanc
 */
@Log4j2
public class FaultSpecReadingConverter implements Converter<String, FaultSpec> {
    private Gson gson;
    private PluginService pluginService;

    public FaultSpecReadingConverter(PluginService pluginService) {
        gson = new Gson();
        this.pluginService = pluginService;
    }

    @Override
    public FaultSpec convert(String source) {
        log.debug("Start execution of convert() method...");
        try {
            JSONObject jsonObject = new JSONObject(source);
            String specClassName = jsonObject.getString("specType");
            Class<?> specClass = getSpecClass(specClassName);
            return (FaultSpec) gson.fromJson(source, specClass);
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage());
            return gson.fromJson(source, CommandExecutionFaultSpec.class);
        } catch (Exception e) {
            throw new MangleRuntimeException(e, ErrorCode.GENERIC_ERROR);
        }
    }

    private Class<?> getSpecClass(String specClassName) throws ClassNotFoundException {
        Class<?> specClass;
        try {
            specClass = Class.forName(specClassName);
        } catch (ClassNotFoundException e) {
            try {
                specClass = pluginService.getExtensionForModel(specClassName).getClass();
            } catch (MangleException e1) {
                throw new ClassNotFoundException("Class Not Found :" + specClassName, e1);
            }
        }
        return specClass;
    }
}
