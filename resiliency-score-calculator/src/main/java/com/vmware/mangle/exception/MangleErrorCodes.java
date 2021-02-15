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

package com.vmware.mangle.exception;

import lombok.Getter;

/**
 * @author ranjans
 *
 */
public enum MangleErrorCodes {

    READ_FILE_AS_STRING_FAILED("FI00001", "Not able to read file as String. "), SCORE_EMAIL_SENT_FAILED("FI00002",
            "Resiliency score email sending failed to: ");

    @Getter
    private final String id;
    @Getter
    private final String msg;

    MangleErrorCodes(String id, String msg) {
        this.id = id;
        this.msg = msg;
    }

}
