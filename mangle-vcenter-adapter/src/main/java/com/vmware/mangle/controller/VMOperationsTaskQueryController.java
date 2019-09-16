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

package com.vmware.mangle.controller;

import io.swagger.annotations.Api;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vmware.mangle.model.response.VCenterOperationTask;
import com.vmware.mangle.service.VMOperationsTaskStore;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author Chethan C(chetanc)
 *
 */
@RestController
@RequestMapping("/api/v1/task/")
@Api(tags = "Task Details", description = "Retrieve status of the task using the Task ID")
public class VMOperationsTaskQueryController {

    /**
     * serves as the endpoint for the query /v1/api/task/{taskid}
     *
     * @param taskid:
     *            refers to the UUID generated for the VC fault injection task
     * @return task details for the given task
     * @throws MangleException
     */
    @GetMapping(value = "{taskid}")
    public ResponseEntity queryTaskDetails(@PathVariable String taskid) {
        VCenterOperationTask vCenterOperationTask = null;
        try {
            vCenterOperationTask = VMOperationsTaskStore.getTask(taskid);
            return new ResponseEntity(vCenterOperationTask, HttpStatus.OK);
        } catch (MangleException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
