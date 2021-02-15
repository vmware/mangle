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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.vmware.mangle.resiliency.commons.ResiliencyConstants;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author ranjans
 *
 */
public class CustomLocalDateTimeSerializer extends StdSerializer<LocalDateTime> {
    private static final long serialVersionUID = 1L;

    public CustomLocalDateTimeSerializer() {
        super(LocalDateTime.class);
    }

    @Override
    public void serialize(LocalDateTime date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {

        DateTimeFormatter pattern = DateTimeFormat.forPattern(ResiliencyConstants.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS);
        jsonGenerator.writeNumber("\"" + date.toString(pattern) + "\"");
    }
}
