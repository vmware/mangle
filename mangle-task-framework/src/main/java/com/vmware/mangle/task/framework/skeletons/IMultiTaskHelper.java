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

import java.util.Map;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author hkilari
 *
 */
public interface IMultiTaskHelper<T extends K8SFaultTriggerSpec, U extends CommandExecutionFaultSpec> {
    /**
     * Method should be Overriden by concrete classes to return the child Tasks Spanned during its
     * execution.
     *
     * @return
     * @throws MangleException
     */
    public Map<String, Task<U>> getChildTasks(Task<T> task);

    public boolean isReadyForChildExecution(Task<T> task);
}
