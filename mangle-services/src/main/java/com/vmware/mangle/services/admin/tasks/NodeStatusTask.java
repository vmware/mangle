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

package com.vmware.mangle.services.admin.tasks;

import static com.vmware.mangle.utils.constants.HazelcastConstants.HAZELCAST_MANGLE_NODE_CURRENT_STATUS_ATTRIBUTE;
import static com.vmware.mangle.utils.constants.URLConstants.TASKS_WAIT_TIME_SECONDS;

import java.util.Calendar;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskInfo;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.model.task.MangleNodeStatusDto;
import com.vmware.mangle.task.framework.skeletons.ITaskHelper;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.CustomErrorMessage;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;
import com.vmware.mangle.utils.messages.tasks.TaskExecutionMessages;

/**
 * @author bkaranam (bhanukiran karanam)
 */
@Log4j2
@Component(value = Constants.NODESTATUS_TASK_NAME)
public class NodeStatusTask<T extends MangleNodeStatusDto> implements ITaskHelper<T> {
    @Autowired
    private CustomErrorMessage customErrorMessage;
    @Autowired
    private HazelcastInstance instance;

    private ApplicationEventPublisher eventPublisher;

    public Task<T> init(T taskData) {
        return init(new Task<T>(), taskData);
    }

    public Task<T> init(Task<T> task, T taskData) {
        if (task.isInitialized()) {
            return task;
        }
        task.setId(UUID.randomUUID().toString());
        task.setTaskClass(task.getClass().getName());
        String className = this.getClass().getName();
        task.setExtensionName(this.getClass().getName());
        task.setTaskData(taskData);
        task.setTaskName(
                className.substring(className.lastIndexOf('.') + 1) + "-" + Calendar.getInstance().getTimeInMillis());
        if (task.getTriggers() == null) {
            task.setTriggers(new Stack<>());
        }
        task.setTaskDescription(getDescription(task));
        TaskTrigger trigger = new TaskTrigger();
        trigger.setTaskStatus(TaskStatus.INITIALIZING);
        task.getTriggers().add(trigger);
        task.setInitialized(true);
        return task;
    }

    @Override
    public void executeTask(Task<T> task) throws MangleException {
        instance.getCluster().getLocalMember().setStringAttribute(HAZELCAST_MANGLE_NODE_CURRENT_STATUS_ATTRIBUTE,
                task.getTaskData().getNodeStatus().name());
        boolean completed = false;
        int count = 1;
        Set<Member> clusterMembers = instance.getCluster().getMembers();
        while (count++ <= TASKS_WAIT_TIME_SECONDS / 10) {
            int memberChangedCount = 0;
            for (Member member : clusterMembers) {
                if (member.getStringAttribute(HAZELCAST_MANGLE_NODE_CURRENT_STATUS_ATTRIBUTE)
                        .equals(task.getTaskData().getNodeStatus().name())) {
                    memberChangedCount++;
                }
            }
            if (memberChangedCount == clusterMembers.size()) {
                completed = true;
                break;
            }
            CommonUtils.delayInSeconds(10);
        }
        if (!completed) {
            throw new MangleException(ErrorCode.MANGLE_IN_MAINTENANCE_MODE_FAILED);
        }
    }

    @Override
    public String getDescription(Task task) {
        return "Task to change the status of mangle nodes in the cluster";
    }

    @Override
    public void run(Task task) throws MangleException {
        try {
            executeTask(task);
        } catch (MangleException e) {
            log.error(TaskExecutionMessages.TASK_EXECUTION_FAILED_MESSAGE + "Error Code : " + e.getErrorCode().getCode()
                    + ", Error Message : " + customErrorMessage.getErrorMessage(e), e);
            throw new MangleRuntimeException(e, ErrorCode.TASK_EXECUTION_FAILED);
        }
    }

    @Override
    public TaskInfo getInfo(Task task) throws MangleException {
        return task.getMangleTaskInfo();
    }

    @Override
    public void cancel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public ApplicationEventPublisher getEventPublisher() {
        return eventPublisher;
    }

}
