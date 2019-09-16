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

package com.vmware.mangle.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.model.task.MangleNodeStatusDto;
import com.vmware.mangle.services.admin.tasks.NodeStatusTask;
import com.vmware.mangle.services.tasks.executor.TaskExecutor;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Operations to support administration controller
 *
 * @author bkaranam (bhanukiran karanam)
 */
@Service
public class AdministrationService {

    private TaskExecutor<Task<MangleNodeStatusDto>> concurrentTaskRunner;
    private NodeStatusTask<MangleNodeStatusDto> nodeStatusUpdateTask;

    @Autowired
    public AdministrationService(NodeStatusTask<MangleNodeStatusDto> maintenanceTask,
            TaskExecutor<Task<MangleNodeStatusDto>> concurrentTaskRunner) {
        this.nodeStatusUpdateTask = maintenanceTask;
        this.concurrentTaskRunner = concurrentTaskRunner;
    }

    @SuppressWarnings("unchecked")
    public Task<MangleNodeStatusDto> updateMangleNodeStatus(
            MangleNodeStatusDto nodeStatusUpdateDto) throws MangleException {
        Task<MangleNodeStatusDto> task = nodeStatusUpdateTask.init(nodeStatusUpdateDto);
        return (Task<MangleNodeStatusDto>) concurrentTaskRunner.submitTask(task);
    }
}
