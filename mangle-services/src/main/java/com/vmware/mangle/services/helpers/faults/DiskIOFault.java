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

package com.vmware.mangle.services.helpers.faults;

import static com.vmware.mangle.services.constants.CommonConstants.BLOCKSIZE_ARG;
import static com.vmware.mangle.services.constants.CommonConstants.DEFAULT_BLOCK_SIZE;
import static com.vmware.mangle.services.constants.CommonConstants.TARGET_DIRECTORY_ARG;

import java.util.HashMap;
import java.util.Map;

import com.vmware.mangle.cassandra.model.faults.specs.DiskIOFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author hkilari
 *
 */
@SupportedEndpoints(endPoints = { EndpointType.K8S_CLUSTER, EndpointType.DOCKER, EndpointType.MACHINE })
public class DiskIOFault extends AbstractFault {

    public DiskIOFault(DiskIOFaultSpec faultSpec) throws MangleException {
        super(faultSpec, TaskType.INJECTION);
    }

    @Override
    protected Map<String, String> getFaultSpecificArgs() {
        DiskIOFaultSpec localFaultSpec = (DiskIOFaultSpec) faultSpec;
        Map<String, String> specificArgs = new HashMap<>();
        if ("0".equals(localFaultSpec.getIoSize())) {
            specificArgs.put(BLOCKSIZE_ARG, DEFAULT_BLOCK_SIZE);
        } else {
            specificArgs.put(BLOCKSIZE_ARG, localFaultSpec.getIoSize());
        }
        specificArgs.put(TARGET_DIRECTORY_ARG, localFaultSpec.getTargetDir());
        return specificArgs;
    }

}
