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

package com.vmware.mangle.faults.plugin.helpers;

import java.util.Map;

import lombok.extern.log4j.Log4j2;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.utils.CommonUtils;

/**
 * @author hkilari
 *
 */
@Log4j2
public class MultiTaskHelper<T extends TaskSpec, U extends TaskSpec> {

    public void processTaskMap(Map<String, Task<T>> taskObjmap, Task<U> taskConfig) {
        log.info("Parent Task: " + taskConfig.getId() + "is waiting for completion of child tasks.");
        taskConfig.updateTaskOutPut("PODS affeted by the Task");
        /**
         * Loop to process taskobjMap to update the status of the task for each pod and same will be
         * updated as taskOutput
         */
        for (String podName : taskObjmap.keySet()) {
            @SuppressWarnings("unchecked")
            Task<TaskSpec> taskObj = (Task<TaskSpec>) taskObjmap.get(podName);

            int loopCounter = 0;
            if (taskObj != null) {
                while (loopCounter < 360 && (taskObj.getTaskStatus() != TaskStatus.COMPLETED
                        && taskObj.getTaskStatus() != TaskStatus.FAILED)) {
                    CommonUtils.delayInSeconds(10);
                    loopCounter++;
                }
                if (taskObj.getTaskStatus() == TaskStatus.COMPLETED) {
                    taskConfig.updateTaskOutPut(
                            " Pod: " + podName + " Result: " + "SUCCESS" + " Agent Task Id: " + taskObj.getId());
                } else if (taskObj.getTaskStatus() == TaskStatus.FAILED) {
                    taskConfig.updateTaskOutPut(
                            " Pod: " + podName + " Result: " + "FAILED" + " Agent Task Id: " + taskObj.getId());
                    taskConfig.updateTaskFailureReason(" Injection Task with Id: " + taskObj.getId() + " on Pod: "
                            + podName + " Failed. Reason: " + taskObj.getTaskFailureReason());
                }
            }
        }
        log.info("Parent Task: " + taskConfig.getId() + "is exiting post the completion of child tasks.");
    }
}
