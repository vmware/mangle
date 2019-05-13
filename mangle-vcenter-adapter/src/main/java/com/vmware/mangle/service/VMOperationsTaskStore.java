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

package com.vmware.mangle.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.vmware.mangle.model.VCenterVMObject;
import com.vmware.mangle.model.response.VCenterOperationTask;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author Chethan C(chetanc)
 *
 *         local store that holds the task object against the task id that is
 *         generated for each VC operation triggered
 */
public class VMOperationsTaskStore {
    private VMOperationsTaskStore() {
    }

    private static Map<String, VCenterOperationTask> lMap = new HashMap<>();
    private static String TASK_NOT_FOUND = "Task not found";

    /**
     * Add new task object to the store
     *
     * @param taskId
     * @param taskStatus
     * @return
     * @throws MangleException : if there exists a task already with the task id, by
     *                           default generateTaskId method used to generate
     *                           taskId returns unique task id, which is not already
     *                           present in the store
     */
    public static VCenterOperationTask addTask(String taskId, String taskStatus) throws MangleException {
        if (!lMap.containsKey(taskId)) {
            VCenterOperationTask task = new VCenterOperationTask(taskId, taskStatus);
            lMap.put(taskId, task);
            return task;
        } else {
            throw new MangleException("Task already exists");
        }
    }

    /**
     * returns task object present in the store
     *
     * @param taskId
     * @return
     * @throws MangleException : if task not found
     */
    public static VCenterOperationTask getTask(String taskId) throws MangleException {
        if (lMap.containsKey(taskId)) {
            return lMap.get(taskId);
        } else {
            throw new MangleException(TASK_NOT_FOUND);
        }
    }

    /**
     * returns the task status for the given taskId
     *
     * @param taskId
     * @return
     * @throws MangleException : if task object not found for the given taskId
     */
    public static String getTaskStatus(String taskId) throws MangleException {
        if (lMap.containsKey(taskId)) {
            return lMap.get(taskId).getTaskStatus();
        } else {
            throw new MangleException(TASK_NOT_FOUND);
        }
    }

    /**
     * updates the task status and message, on the task object identified by taskId
     *
     * @param taskId
     * @param newTaskStatus
     * @param message
     * @param VCenterVMObject
     * @return
     * @throws MangleException : if task object is not found for the given task ID
     */
    public static String updateTaskStatus(String taskId, String newTaskStatus, String message,
            VCenterVMObject VCenterVMObject) throws MangleException {
        if (lMap.containsKey(taskId)) {
            VCenterOperationTask task = lMap.get(taskId);
            task.setTaskStatus(newTaskStatus);
            task.setResponseMessage(message);
            task.setVCenterVMObject(VCenterVMObject);
            return newTaskStatus;
        } else {
            throw new MangleException(TASK_NOT_FOUND);
        }
    }

    /**
     * updates the task status and message, on the task object identified by taskId
     *
     * @param taskId
     * @param newTaskStatus
     * @param message
     * @return
     * @throws MangleException : if task object is not found for the given task ID
     */
    public static String updateTaskStatus(String taskId, String newTaskStatus, String message) throws MangleException {
        return updateTaskStatus(taskId, newTaskStatus, message, null);
    }

    /**
     * generates an unique alpha-numeric UUID
     *
     * @return: unique UUID
     */
    public static String generateTaskId() {
        String taskId = null;
        do {
            taskId = UUID.randomUUID().toString();
        } while (lMap.containsKey(taskId));
        return taskId;
    }
}
