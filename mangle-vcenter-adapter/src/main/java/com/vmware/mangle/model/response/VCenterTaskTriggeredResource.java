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

package com.vmware.mangle.model.response;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.vmware.mangle.controller.VMOperationsTaskQueryController;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author chetanc
 *
 */
@Getter
public class VCenterTaskTriggeredResource extends ResourceSupport {
    private final VCenterTaskTriggeredResponse vcResponse;

    public VCenterTaskTriggeredResource(@NonNull String taskId) throws MangleException {
        this.vcResponse = new VCenterTaskTriggeredResponse(taskId);
        add(ControllerLinkBuilder
                .linkTo(methodOn(VMOperationsTaskQueryController.class).queryTaskDetails(vcResponse.getTaskId()))
                .withRel("task-status"));
        add(new Link(getSelfURI(), "self"));
    }

    public String getSelfURI() {
        return ServletUriComponentsBuilder.fromCurrentRequest().build().toUriString();
    }
}
