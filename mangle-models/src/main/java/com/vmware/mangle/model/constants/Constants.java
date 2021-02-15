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

package com.vmware.mangle.model.constants;

/**
 * @author chetanc
 */
public class Constants {
    public static final String AUTH_PD_PATTERN = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%!&+=])(?=\\S+$).{8,30}";
    public static final String AUTH_PATTERN_MESSAGE =
            "Should consist of 8-30 characters and minimum of one digit, one lower alpha char, one upper alpha char, and one special character within a set of (@#$%!&+=)";
    public static final String DEFAULT_SENDER_NAME = "Mangle";

    private Constants() {

    }
}
