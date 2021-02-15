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

package com.vmware.mangle.custom.serialization;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.vmware.mangle.resiliency.commons.ResiliencyConstants;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;


/**
 * @author ranjans
 *
 */
public class CustomLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser jp, DeserializationContext arg1) throws IOException {
        String dateString = jp.getText();
        LocalDateTime timestamp = LocalDateTime.parse(dateString,
                DateTimeFormat.forPattern(ResiliencyConstants.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS));
        return new LocalDateTime(timestamp);
    }


}
