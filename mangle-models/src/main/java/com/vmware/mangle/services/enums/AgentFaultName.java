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
public enum AgentFaultName {
    INJECT_CPU_FAULT("cpuFault"), INJECT_MEMORY_FAULT("memoryFault"), INJECT_DISK_IO_FAULT(
            "diskFault"), INJECT_FILE_HANDLER_FAULT("fileHandlerFault"), INJECT_NETWORK_LATENCY_FAULT(
                    "networkFault"), INJECT_DISK_SPACE_FAULT("diskSpaceFault"), INJECT_KILL_PROCESS_FAULT(
                            "killProcessFault"), INJECT_DISK_FUSE_FAULT("diskFUSEFault"), INJECT_THREAD_LEAK_FAULT(
                                    "threadLeakFault"), KERNEL_PANIC_FAULT("kernelPanicFault");

    private String value;

    AgentFaultName(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.getValue();
    }

    public static boolean contains(String value) {
        for (AgentFaultName agentFaultName : AgentFaultName.values()) {
            if (agentFaultName.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
