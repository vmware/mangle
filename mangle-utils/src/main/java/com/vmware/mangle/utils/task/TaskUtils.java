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

package com.vmware.mangle.utils.task;

import lombok.experimental.UtilityClass;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.EndpointGroupFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VCenterFaultTriggerSpec;

/**
 * Utility methods for Task.
 *
 * @author kumargautam
 */
@UtilityClass
public class TaskUtils {

    /**
     * @param spec
     * @return {@code CommandExecutionFaultSpec}
     */
    public CommandExecutionFaultSpec getFaultSpec(TaskSpec spec) {
        if (spec instanceof K8SFaultTriggerSpec) {
            return ((K8SFaultTriggerSpec) spec).getFaultSpec();
        } else if (spec instanceof VCenterFaultTriggerSpec) {
            return ((VCenterFaultTriggerSpec) spec).getFaultSpec();
        } else if (spec instanceof EndpointGroupFaultTriggerSpec) {
            return ((EndpointGroupFaultTriggerSpec) spec).getFaultSpec();
        } else {
            return (CommandExecutionFaultSpec) spec;
        }
    }
}