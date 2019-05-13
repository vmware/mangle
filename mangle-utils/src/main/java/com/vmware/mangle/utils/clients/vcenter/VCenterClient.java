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

package com.vmware.mangle.utils.clients.vcenter;

import lombok.Getter;

import com.vmware.mangle.cassandra.model.endpoint.VCenterAdapterProperties;
import com.vmware.mangle.model.vcenter.VCenterSpec;
import com.vmware.mangle.utils.clients.endpoint.EndpointClient;
import com.vmware.mangle.utils.exceptions.MangleException;


/**
 * Serves as an endpoint for all of the Vcenter related faults
 *
 * @author chetanc
 */
public class VCenterClient implements EndpointClient {
    private VCenterSpec vCenterSpec;

    @Getter
    private VCenterAdapterClient vCenterAdapterClient;

    public VCenterClient(String vcServerUrl, String vcUsername, String vcPassword,
            VCenterAdapterProperties vCenterAdapterProperties) {
        vCenterSpec = new VCenterSpec(vcServerUrl, vcUsername, vcPassword);
        vCenterAdapterClient = new VCenterAdapterClient(vCenterAdapterProperties);
    }

    @Override
    public boolean testConnection() throws MangleException {
        if (vCenterAdapterClient.testConnection()) {
            return VMOperations.testConnection(vCenterAdapterClient, getVCenterSpec());
        }
        return false;
    }

    public VCenterSpec getVCenterSpec() {
        return vCenterSpec;
    }
}
