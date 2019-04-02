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

package com.vmware.mangle.model.response;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * Generate the fault response
 *
 * @author kumargautam
 */
@Data
public class MangleResponse<T> {

    protected static final String EMPTY_RESPONSE_MESSAGE = "";
    protected String message;
    protected List<T> response;
    protected ErrorDetails error;
    private Date executionTime;
    private String responseCode;
    private long objectsReturned;
    private static String RESPONSE_CODE_OK = "OK";
    private List<Map<String, String>> links;

    public MangleResponse() {
    }

    public MangleResponse(final String message, final List<T> response, ErrorDetails error) {
        this.message = message == null ? MangleResponse.EMPTY_RESPONSE_MESSAGE : message;
        this.response = response;
        this.error = error;
    }

    public MangleResponse(final String message, final List<T> response) {
        this.message = message == null ? MangleResponse.EMPTY_RESPONSE_MESSAGE : message;
        this.response = response;
        this.error = null;
    }

    public MangleResponse(String code, String message, T response) {
        this(code, message, Arrays.asList(response));
    }

    public MangleResponse(final String message, final T response) {
        this(message, Arrays.asList(response));
    }

    public MangleResponse(final String message, final T response, ErrorDetails error) {
        this(message, Arrays.asList(response), error);
    }

    public MangleResponse(String code, String message) {
        this(code, message, (List) null);
    }

    public MangleResponse(String code, String message, List<T> response) {
        this.executionTime = Calendar.getInstance().getTime();
        this.message = message == null ? EMPTY_RESPONSE_MESSAGE : message;
        this.responseCode = code == null ? RESPONSE_CODE_OK : code;
        this.response = response;
        this.objectsReturned = response == null ? 0L : (long) response.size();
    }

}
