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

package com.vmware.mangle.adapter;

import static com.vmware.mangle.utils.VCenterAPIEndpoints.REST_VC_HOST_BY_ID;
import static com.vmware.mangle.utils.VCenterAPIEndpoints.REST_VC_HOST_CONNECT;
import static com.vmware.mangle.utils.VCenterAPIEndpoints.REST_VC_HOST_DISCONNECT;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.vmware.mangle.inventory.helpers.InventoryHelperUtil;
import com.vmware.mangle.model.Host;
import com.vmware.mangle.model.ResourceObject;
import com.vmware.mangle.model.enums.HostConnectionState;
import com.vmware.mangle.model.resource.VMOperationsRepsonse;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author chetanc
 */
@Service
public class HostOperations {

    @SuppressWarnings("unchecked")
    public boolean disconnectHost(VCenterClient client, String hostId) throws MangleException {
        ResponseEntity<VMOperationsRepsonse> response = (ResponseEntity<VMOperationsRepsonse>) client
                .post(String.format(REST_VC_HOST_DISCONNECT, hostId), null, VMOperationsRepsonse.class);
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return true;
        } else {
            throw new MangleException(response.getBody().getValue().getMessages().get(0).getDefault_message());
        }
    }

    @SuppressWarnings("unchecked")
    public boolean connectHost(VCenterClient client, String hostId) throws MangleException {
        ResponseEntity<VMOperationsRepsonse> response = (ResponseEntity<VMOperationsRepsonse>) client
                .post(String.format(REST_VC_HOST_CONNECT, hostId), null, VMOperationsRepsonse.class);
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return true;
        } else {
            throw new MangleException(response.getBody().getValue().getMessages().get(0).getDefault_message());
        }
    }

    @SuppressWarnings("unchecked")
    public boolean isHostConnected(VCenterClient client, String hostId) throws MangleException {
        ResponseEntity<ResourceObject> response = (ResponseEntity<ResourceObject>) client
                .get(String.format(REST_VC_HOST_BY_ID, hostId), ResourceObject.class);
        ResourceObject resourceObject = response.getBody();
        List<Host> hosts = InventoryHelperUtil.convertLinkedHashMapToObjectList(resourceObject.getValue(), Host.class);
        Host host = hosts.get(0);
        if (host.getConnection_state().equals(HostConnectionState.CONNECTED.name())) {
            return true;
        } else {
            return false;
        }
    }
}
