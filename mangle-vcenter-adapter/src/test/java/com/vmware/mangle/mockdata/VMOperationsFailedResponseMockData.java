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

package com.vmware.mangle.mockdata;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;

import com.vmware.mangle.model.Message;
import com.vmware.mangle.model.Messages;
import com.vmware.mangle.model.resource.VMOperationsRepsonse;

/**
 * @author Chethan C(chetanc)
 */
public class VMOperationsFailedResponseMockData {
    private VMOperationsFailedResponseMockData() {
    }

    public static ResponseEntity getVMOperationsFailedResponseObj() {
        List<Message> lMessages = new ArrayList<Message>();
        lMessages.add(new Message());
        Messages messages = new Messages();
        messages.setMessages(lMessages);
        VMOperationsRepsonse vmOperationsRepsonse = new VMOperationsRepsonse();
        vmOperationsRepsonse.setValue(messages);

        return ResponseEntity.badRequest().body(vmOperationsRepsonse);
    }
}
