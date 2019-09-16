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

package com.vmware.mangle.services.dto;

/**
 * @author hkilari
 *
 *
 */
public class AgentRuleConstants {
    private AgentRuleConstants() {
    }

    public static final String SERVICES_STRING = "servicesString";
    public static final String SERVICE_SCOPE_STRING = "enableOnLocalRequests";
    public static final String HTTP_METHODS_STRING = "httpMethodsString";
    public static final String EXCEPTION_CLASS_STRING = "exceptionClass";
    public static final String EXCEPTION_MESSAGE_STRING = "exceptionMessage";
    public static final String HELPER_METHOD_INVOCATION_STRING = "$HELPER_METHOD_INVOCATION";
    public static final CharSequence CLASS_NAME_STRING = "$CLASS_NAME";
    public static final CharSequence METHOD_NAME_STRING = "$METHOD_NAME";
    public static final CharSequence RULE_EVENT_STRING = "$RULE_EVENT";
    public static final String LATENCY_STRING = "latency";
    public static final CharSequence AT_ENTRY_STRING = "AT ENTRY";
    public static final String IF_TRUE = "IF true";
    public static final String CLASS_NAME = "className";
    public static final String METHOD_NAME = "methodName";
    public static final String RULE_EVENT = "ruleEvent";
    public static final String EXIT_CODE = "exitCode";
}
