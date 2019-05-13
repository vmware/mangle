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

package com.vmware.mangle.faults.plugin.utils;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskType;

/**
 * @author hkilari
 *
 */
public class TaskDescriptionUtils {
    private TaskDescriptionUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String getDescription(Task<? extends CommandExecutionFaultSpec> task) {
        String description = new StringBuffer().append(task.getTaskData().getFaultName()).append(" on endpoint: ")
                .append(task.getTaskData().getEndpointName()).append(". More Details: [ ").append(task.getTaskData())
                .append(" ]").toString();
        if (task.getTaskData().getDockerArguments() != null) {
            description += ", [ " + task.getTaskData().getDockerArguments() + " ]";
        } else if (task.getTaskData().getK8sArguments() != null) {
            description += ", [ " + task.getTaskData().getK8sArguments() + " ]";
        }

        description = removeNullMembersFromString(description);
        if (task.getTaskType() == TaskType.INJECTION) {
            return "Executing Fault: " + description;
        } else {
            return "Remediating Fault: " + description;
        }
    }

    public static String removeNullMembersFromString(String description) {
        return description.replaceAll(", [^,]*?=null", "")
                .replaceAll("super=JVMAgentFaultSpec\\(jvmProperties=null\\),* *", "");
    }
}
