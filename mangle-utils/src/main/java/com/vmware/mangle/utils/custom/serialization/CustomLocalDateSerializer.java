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

package com.vmware.mangle.utils.custom.serialization;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.joda.time.LocalDate;

/**
 * @author Dinesh Babu TG (dgnaneswaran)
 */
public class CustomLocalDateSerializer extends StdSerializer<LocalDate> {
    private static final long serialVersionUID = 1L;

    public CustomLocalDateSerializer() {
        super(LocalDate.class);
    }

    @Override
    public void serialize(LocalDate localDate, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        jsonGenerator.writeStartArray();
        jsonGenerator.writeNumber(localDate.getYear());
        jsonGenerator.writeNumber(localDate.getMonthOfYear());
        jsonGenerator.writeNumber(localDate.getDayOfMonth());
        jsonGenerator.writeEndArray();
    }
}
