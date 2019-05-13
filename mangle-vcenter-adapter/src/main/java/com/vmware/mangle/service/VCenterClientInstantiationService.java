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

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import com.vmware.mangle.adapter.VCenterClient;
import com.vmware.mangle.model.VCenterSpec;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author chetanc
 *
 *         acts as a wrapper, and provides some of the methods to assist in the creation of the
 *         VCenterClient, and retrieve them from the local VCenterConnectionStore
 */
@Component
@Log4j2
public class VCenterClientInstantiationService {

    /**
     * Returns the VcenterClient if exists in the VCenterConnectionStore, if it doesn't exist, calls
     * for the creation of new instance of the VCenterClient. This also checks if the connection
     * holds active session token, if not creates new instance of client and replaces the old one
     */
    public VCenterClient getVCenterClient(VCenterSpec vCenterSpec) throws MangleException {
        VCenterClient client = createNewVCenterBean(vCenterSpec);
        log.debug(String.format("Establing a new session on vCenter %s, for the user %s", vCenterSpec.getVcServerUrl(),
                vCenterSpec.getVcUsername()));
        return client;
    }

    /**
     * Creates a new VCenterClient instance for the given vcenter spec pushes the instance to the
     * local connection store, so that it can be retrieved at the later instance of time, when
     * needed
     */
    public VCenterClient createNewVCenterBean(VCenterSpec vCenterSpec) throws MangleException {
        VCenterClient VCenterClient = new VCenterClient(vCenterSpec.getVcServerUrl(), vCenterSpec.getVcUsername(),
                vCenterSpec.getVcPassword());
        return VCenterClient;
    }

}
