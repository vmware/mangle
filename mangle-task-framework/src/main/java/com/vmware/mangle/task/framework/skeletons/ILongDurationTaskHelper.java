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

package com.vmware.mangle.task.framework.skeletons;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.utils.exceptions.MangleException;


/**
 * Interface should be Implemented by any Mangle Task that run for long durations.
 *
 * @author hkilari
 *
 */
public interface ILongDurationTaskHelper<T extends TaskSpec> {
    abstract boolean isTaskSupportingPause();

    boolean pauseTask(Task<T> task) throws MangleException;

    boolean resumeTask(Task<T> task) throws MangleException;
}
