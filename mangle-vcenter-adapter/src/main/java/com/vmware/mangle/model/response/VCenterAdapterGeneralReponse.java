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
import java.util.List;

import lombok.Data;

/**
 * @author chetanc
 */

@Data
public class VCenterAdapterGeneralReponse<T> {
    private static String EMPTY_RESPONSE_MESSAGE = "";
    private String message;
    private List<T> response;

    public VCenterAdapterGeneralReponse() {
        // TODO Auto-generated constructor stub
    }

    /**
     * Creates a new instance of Response from a List of Objects
     *
     * @param message
     * @param response
     */
    public VCenterAdapterGeneralReponse(final String message, final List<T> response) {
        this.message = message == null ? VCenterAdapterGeneralReponse.EMPTY_RESPONSE_MESSAGE : message;
        this.response = response;
    }

    /**
     * Creates a new instance of Response from a Object
     *
     * @param message
     * @param response
     */
    public VCenterAdapterGeneralReponse(final String message, final T response) {
        this(message, Arrays.asList(response));
    }

}