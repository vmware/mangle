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

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Error Details bean.
 *
 * @author kumargautam
 */
@Data
@NoArgsConstructor
public class ErrorDetails {

    private Date timestamp;
    private String code;
    private String description;
    private Object details;

    public ErrorDetails(Date timestamp, String code, String description, String details) {
        super();
        this.timestamp = timestamp;
        this.code = code;
        this.description = description;
        this.details = details;
    }
}
