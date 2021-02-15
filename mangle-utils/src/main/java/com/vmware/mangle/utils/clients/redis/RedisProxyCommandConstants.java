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

package com.vmware.mangle.utils.clients.redis;

/**
 * Command Constants for Redis FI Proxy.
 *
 * @author kumargautam
 */
public class RedisProxyCommandConstants {

    private RedisProxyCommandConstants() {
    }

    public static final String RULE_LIST = "rulelist";
    public static final String RULE_ADD = "ruleadd ";
    public static final String RULE_DELETE = "ruledel ";
    public static final String RULE_ADD_FOR_DELAY = RULE_ADD + "%s delay=%s percentage=%s";
    public static final String RULE_ADD_FOR_RETURN_ERROR = RULE_ADD + "%s return_err=%s percentage=%s";
    public static final String REMEDIATION_COMMAND = RULE_DELETE + "%s";
    public static final String RULE_ADD_FOR_RETURN_EMPTY = RULE_ADD + "%s return_empty=true percentage=%s";
    public static final String RULE_ADD_FOR_DROP_CONNECTION = RULE_ADD + "%s drop=true percentage=%s";
    public static final String REDIS_FAULT_PARALLEL_EXECUTION_ERROR =
            "Parallel execution of the Redis fault is not supported, cause : %s";
}
