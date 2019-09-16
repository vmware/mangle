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

package com.vmware.mangle.utils.exceptions;

import lombok.Getter;

import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 *
 * @author kumargautam
 */
@Getter
public class MangleException extends Exception {

    private static final long serialVersionUID = 1L;
    private ErrorCode errorCode;
    private transient Object[] args;

    public MangleException(ErrorCode errorCode, Object... args) {
        super();
        this.errorCode = errorCode;
        this.args = args;
    }

    public MangleException(String msg, ErrorCode errorCode, Object... args) {
        super(msg);
        this.errorCode = errorCode;
        this.args = args;
    }

    public MangleException(String msg, Throwable e, ErrorCode errorCode, Object... args) {
        super(msg, e);
        this.errorCode = errorCode;
        this.args = args;
    }

    public MangleException(Exception e, ErrorCode errorCode, Object... args) {
        super(e);
        this.errorCode = errorCode;
        this.args = args;
    }
}
