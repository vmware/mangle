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

package com.vmware.mangle.java.agent.faults;

import java.util.Map;
import java.util.logging.Logger;

import com.vmware.mangle.java.agent.faults.exception.FiaascoException;

/**
 * @author hkilari
 *
 */
public class AgentFaultFactory {
    private static final Logger LOG = Logger.getLogger(AgentFaultFactory.class.getName());

    public static AgentFault getFault(Map<String, String> faultArgs) throws Exception {
        String faultName = faultArgs.get("faultName");
        try {
            if (faultName == null) {
                throw new FiaascoException("No Fault name provided.");
            }
            switch (faultName) {
            case "cpuFault":
                return new CpuFault(faultArgs);
            case "memoryFault":
                return new MemoryFaults(faultArgs);
            case "fileHandlerFault":
                return new FileHandlerFault(faultArgs);
            default:
                throw new FiaascoException("Unsupported Fault name: " + faultName);
            }
        } catch (Exception e) {
            LOG.severe(e.getMessage());
            throw e;
        }
    }

}
