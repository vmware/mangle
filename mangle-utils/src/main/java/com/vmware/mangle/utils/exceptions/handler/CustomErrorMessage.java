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

package com.vmware.mangle.utils.exceptions.handler;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;

/**
 * Read custom error message form error-codes.properties file.
 *
 * @author kumargautam
 */
@Component
@PropertySource("classpath:error-codes.properties")
public class CustomErrorMessage {

    @Autowired
    private Environment env;

    /**
     * Get custom error message.
     *
     * @param exception
     * @return
     */
    public String getErrorMessage(MangleException exception) {
        String errorMsg = exception.getLocalizedMessage();
        String errorCode = exception.getErrorCode().getCode();
        Object[] args = exception.getArgs();
        errorMsg = getErrorMessage(errorCode, errorMsg, args);
        return errorMsg;
    }

    /**
     * Get custom error message.
     *
     * @param noRecordFoundException
     * @return
     */
    public String getErrorMessage(MangleRuntimeException noRecordFoundException) {
        String errorMsg = noRecordFoundException.getLocalizedMessage();
        String errorCode = noRecordFoundException.getErrorCode().getCode();
        Object[] args = noRecordFoundException.getArgs();
        errorMsg = getErrorMessage(errorCode, errorMsg, args);
        return errorMsg;
    }

    private String getErrorMessage(String errorCode, String errorMsg, Object[] args) {
        if (env != null && env.containsProperty(errorCode)) {
            errorMsg = getErrorMessage(errorCode);
        }

        if (StringUtils.isNotEmpty(errorMsg) && ArrayUtils.isNotEmpty(args)) {
            errorMsg = formatErrorMessage(errorMsg, args);
        }
        return errorMsg;
    }

    public String getErrorMessage(String errorCode) {
        return env.getProperty(errorCode);
    }

    /**
     * Get error msg with passing args. <br>
     * eg; errorMsg : Field length should be > {0}, args : 4 <br>
     * Output : Field length should be > 4.
     *
     * @param errorMsg
     * @param args
     * @return
     */
    public String formatErrorMessage(String errorMsg, Object... args) {
        StringBuilder msgBuilder = new StringBuilder(errorMsg);
        for (int i = 0; i < args.length; i++) {
            String regex = "{" + i + "}";
            int index = msgBuilder.indexOf(regex);
            if (index != -1) {
                msgBuilder.replace(index, (index + regex.length()), String.valueOf(args[i]));
            }
        }
        return msgBuilder.toString();
    }
}
