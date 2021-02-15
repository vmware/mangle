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

package com.vmware.mangle.utils.serialization;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import ru.yandex.qatools.allure.plugins.PluginData;

/**
 * @author mangle
 *
 */
public class CustomPluginDataDeserializer extends JsonDeserializer<PluginData> {

    @Override
    public PluginData deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        Object pluginString = jsonParser.readValuesAs(Object.class).next();
        Map<String, Object> pluginData = (Map<String, Object>) pluginString;
        return new PluginData((String) pluginData.get("name"), pluginData.get("data"));
    }

}
