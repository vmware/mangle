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

package com.vmware.mangle.services.enums;

/**
 * @author hkilari
 *
 */
public enum BytemanFaultType {
    SPRING_SERVICE_LATENCY, MOCK_SPRING_SERVICE_RESPONSE_CODES, SPRING_SERVICE_MANIPULATE_RESPONSE, SPRING_SERVICE_EXCEPTION, XENON_SERVICE_LATENCY, JAVA_METHOD_LATENCY, EXCEPTION, THREAD_INTERRUPTION, MANIPULATE_RETURN_OBJECT, TRACE_OBJECT, KILL_JVM, KILL_THREAD, MOCK_XENON_SERVICE_RESPONSE_CODES;

    public static boolean contains(String value) {
        for (BytemanFaultType faultName : BytemanFaultType.values()) {
            if (faultName.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
