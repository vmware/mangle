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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.ResourceSupport;

import com.vmware.mangle.model.VCenterVMObject;

/**
 * @author chetanc
 *
 * created for each of the fault injection api call, and stored against the task
 * id in VMOperationsTaskStore
 */
@RequiredArgsConstructor
@NoArgsConstructor
@Getter
public class VCenterOperationTask extends ResourceSupport {
    @NonNull
    private String taskID;
    @Setter
    @NonNull
    private String taskStatus;
    @Setter
    private String responseMessage;
    @Setter
    private VCenterVMObject VCenterVMObject;
}
