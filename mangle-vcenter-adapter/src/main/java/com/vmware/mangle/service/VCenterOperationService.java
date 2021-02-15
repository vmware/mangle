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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vmware.mangle.adapter.VCenterClient;
import com.vmware.mangle.model.VCenterSpec;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author chetanc
 */
@Log4j2
@Service
public class VCenterOperationService {

    @Autowired
    VCenterClientInstantiationService clientInstantiationService;

    public boolean testConnection(VCenterSpec vCenterSpec) throws MangleException {
        VCenterClient client = clientInstantiationService.getVCenterClient(vCenterSpec);
        return client.testConnection();
    }
}
