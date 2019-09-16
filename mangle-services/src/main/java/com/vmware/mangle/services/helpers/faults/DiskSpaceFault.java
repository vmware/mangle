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

import java.util.HashMap;
import java.util.Map;

import com.vmware.mangle.cassandra.model.faults.specs.DiskSpaceSpec;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.constants.CommonConstants;
import com.vmware.mangle.task.framework.helpers.faults.SupportedEndpoints;
import com.vmware.mangle.utils.constants.FaultConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 *
 * @author kumargautam
 */
@SupportedEndpoints(endPoints = { EndpointType.MACHINE, EndpointType.DOCKER, EndpointType.K8S_CLUSTER })
public class DiskSpaceFault extends AbstractFault {

    public DiskSpaceFault(DiskSpaceSpec faultSpec) throws MangleException {
        super(faultSpec, TaskType.INJECTION);
        validateDiskFillSizeRange(faultSpec);
    }

    @Override
    public Map<String, String> getFaultSpecificArgs() {
        DiskSpaceSpec diskSpaceSpec = (DiskSpaceSpec) faultSpec;
        Map<String, String> specificArgs = new HashMap<>();
        specificArgs.put(FaultConstants.DIRECTORY_PATH_ARG, diskSpaceSpec.getDirectoryPath());
        if (diskSpaceSpec.getDiskFillSize() != null) {
            specificArgs.put(FaultConstants.DISK_FILL_SIZE_ARG, String.valueOf(diskSpaceSpec.getDiskFillSize()));
        }
        return specificArgs;
    }

    /**
     * @param faultSpec
     * @throws MangleException
     */
    private void validateDiskFillSizeRange(DiskSpaceSpec faultSpec) throws MangleException {
        if (faultSpec.getDiskFillSize() != null
                && !(faultSpec.getDiskFillSize() >= 1 && faultSpec.getDiskFillSize() <= 100)) {
            throw new MangleException(CommonConstants.DISK_FILL_SIZE_RANGE_ERROR, ErrorCode.GENERIC_ERROR);
        }
    }
}