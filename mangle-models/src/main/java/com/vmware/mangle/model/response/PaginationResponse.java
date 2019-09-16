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
import lombok.EqualsAndHashCode;

/**
 * Model for PaginationResponse.
 *
 * @author kumargautam
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PaginationResponse<T> extends MangleResponse<T> {
    private int totalPage;

    public PaginationResponse() {
    }

    public PaginationResponse(final String message, final List<T> response, int totalPage) {
        this.message = message == null ? PaginationResponse.EMPTY_RESPONSE_MESSAGE : message;
        this.response = response;
        this.totalPage = totalPage;
    }

    public PaginationResponse(final String message, final T response, int totalPage) {
        this(message, Arrays.asList(response), totalPage);
    }
}
