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

package com.vmware.mangle.client.restclient;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.vmware.mangle.custom.serialization.CustomLocalDateDeserializer;
import com.vmware.mangle.custom.serialization.CustomLocalDateSerializer;
import com.vmware.mangle.custom.serialization.CustomLocalDateTimeSerializer;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author ranjans
 *
 */
public class ObjectMapperFactory implements FactoryBean<ObjectMapper> {
    @SuppressWarnings("deprecation")
    @Override
    public ObjectMapper getObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SimpleModule simpleModule = new SimpleModule("SimpleModule", new Version(1, 0, 0, null));
        simpleModule.addSerializer(new CustomLocalDateSerializer());
        simpleModule.addDeserializer(LocalDate.class, new CustomLocalDateDeserializer());
        simpleModule.addSerializer(new CustomLocalDateTimeSerializer());
        mapper.registerModule(simpleModule);
        return mapper;
    }

    @Override
    public Class<?> getObjectType() {
        return ObjectMapper.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
