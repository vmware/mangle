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

package com.vmware.mangle.services.hazelcast;

import java.util.HashSet;
import java.util.Set;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.scheduler.SchedulerSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.model.enums.SchedulerStatus;
import com.vmware.mangle.services.CredentialService;
import com.vmware.mangle.services.EndpointService;
import com.vmware.mangle.services.SchedulerService;
import com.vmware.mangle.services.TaskService;
import com.vmware.mangle.services.helpers.FaultTaskFactory;
import com.vmware.mangle.services.tasks.executor.TaskExecutor;
import com.vmware.mangle.utils.constants.URLConstants;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 *
 * i) When fault injected will follow the following steps of execution 1. Construct the task object
 * 2. Store in the DB 3. Trigger TaskCreatedEvent 4. TaskCreatedListener will add the task to the
 * hazelcast cluster cache 5. Hazelcast will put this task into its distributed-map 6. Depending on
 * which partition this task was added into, it will trigger an entryAdded event in its respective
 * node 7. Listener to this event will trigger the execution of the task on that particular node
 *
 * ii) When new member joins the cluster 1. The partition ownership of some of the partitions are
 * moved to new cluster member 2. No re-triggering of the tasks will be carried out 3. Task that
 * were being executed by the old owner of the partition will remain to be executed on the old owner
 * Summary: only partition ownership will be transferred
 *
 * iii) When an existing cluster member leaves the cluster 1. Membership event is generated - Tasks
 * that were still running on the old owner, but whose ownership were assigned to the new owner will
 * be re-triggered 2. Migration event is generated - Partitions owned by the dead node will be
 * distributed across the existing cluster nodes - Tasks that are part of dead cluster member will
 * be re-triggered, on the new owner of the respective partitions
 *
 *
 * @author chetanc
 *
 */
@Service
@Log4j2
public class HazelcastTaskService {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskExecutor<Task<? extends TaskSpec>> concurrentTaskRunner;

    @Autowired
    private SchedulerService schedulerService;

    @Autowired
    EndpointService endpointService;

    @Autowired
    CredentialService credentialService;

    @Autowired
    FaultTaskFactory faultTaskFactory;

    @Setter
    private HazelcastInstance hazelcastInstance;

    /**
     * If a task is is not completed/failed, or if it is a scheduled task verify it's status is
     * still scheduled, then will re-trigger it on the new node
     *
     * @param taskId
     *            Id of the task that has to be retriggered on the different cluster node
     * @throws MangleException
     *             If the endpoint/credentials for the task to be executed is not found
     */
    public void triggerTask(String taskId) throws MangleException {
        log.debug("Re-triggering the fault with the id: {} on the current node", taskId);

        Task<?> persistedData = taskService.getTaskById(taskId);
        boolean triggerTask = true;

        if (persistedData == null) {
            triggerTask = false;
        } else if (!CollectionUtils.isEmpty(persistedData.getTriggers())
                && (!persistedData.isScheduledTask() && (persistedData.getTaskStatus() == TaskStatus.COMPLETED
                        || persistedData.getTaskStatus() == TaskStatus.FAILED))) {
            log.debug("Task {} has completed execution with the status {}, will be removing from the cluster cache",
                    taskId, persistedData.getTaskStatus());
            triggerTask = false;
        } else if (persistedData.isScheduledTask()) {
            SchedulerSpec schedularSpec = schedulerService.getSchedulerDetailsById(persistedData.getId());
            if (!(schedularSpec.getStatus() == SchedulerStatus.SCHEDULED
                    || schedularSpec.getStatus() == SchedulerStatus.INITIALIZING)) {
                log.debug(
                        "Scheduled task {} has finished execution with the status {}, will be removed from the cluster cache",
                        taskId, schedularSpec.getStatus().name());
                triggerTask = false;
            }
        }

        if (triggerTask) {
            submitTask(persistedData);
        } else {
            IMap<String, Set<String>> taskMap = hazelcastInstance.getMap(URLConstants.HAZELCAST_TASKS_MAP);
            taskMap.remove(taskId);
        }
    }

    private void submitTask(Task<?> task) throws MangleException {
        String taskId = task.getId();
        updateFaultSpec(task.getTaskData());
        log.info("Submitting task to concurrent task runner");
        concurrentTaskRunner.submitTask(task);
        log.info("Submitted task to concurrent task runner");
        addTaskToClusterNodeCache(taskId);
    }

    private void addTaskToClusterNodeCache(String taskId) {
        Member currentInstance = hazelcastInstance.getCluster().getLocalMember();
        log.debug("Added task to the mangle cluster node {} for execution", currentInstance.getAddress());
        String host = currentInstance.getUuid();
        IMap<String, Set<String>> nodeToTaskMapping = hazelcastInstance.getMap("nodeTasks");
        Set<String> tasks = new HashSet<>();
        if (nodeToTaskMapping.containsKey(host)) {
            tasks = nodeToTaskMapping.get(host);
            tasks.add(taskId);
            nodeToTaskMapping.put(host, tasks);
        } else {
            tasks.add(taskId);
            nodeToTaskMapping.put(host, tasks);
        }
    }

    private CommandExecutionFaultSpec updateFaultSpec(TaskSpec spec) throws MangleException {
        log.debug("Constructing fault execution spec for the task {}", spec.getId());
        CommandExecutionFaultSpec commandExecutionFaultSpec;

        if (spec instanceof K8SFaultTriggerSpec) {
            commandExecutionFaultSpec = ((K8SFaultTriggerSpec) spec).getFaultSpec();
        } else {
            commandExecutionFaultSpec = (CommandExecutionFaultSpec) spec;
        }

        commandExecutionFaultSpec
                .setEndpoint(endpointService.getEndpointByName(commandExecutionFaultSpec.getEndpointName()));
        if (commandExecutionFaultSpec.getEndpoint().getEndPointType() != EndpointType.DOCKER) {
            commandExecutionFaultSpec.setCredentials(credentialService
                    .getCredentialByName(commandExecutionFaultSpec.getEndpoint().getCredentialsName()));
        }

        return commandExecutionFaultSpec;
    }

}
