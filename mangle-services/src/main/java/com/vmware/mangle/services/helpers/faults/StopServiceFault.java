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

import com.vmware.mangle.cassandra.model.faults.specs.StopServiceFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.task.framework.helpers.faults.SupportedEndpoints;
import com.vmware.mangle.utils.constants.FaultConstants;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * StopServiceFault
 *
 * @author rpraveen
 */
@SupportedEndpoints(endPoints = { EndpointType.MACHINE })
public class StopServiceFault extends AbstractFault {

    /**
     * @param faultSpec
     * @throws MangleException
     */
    public StopServiceFault(StopServiceFaultSpec stopServicefaultSpec) throws MangleException {
        super(stopServicefaultSpec, TaskType.INJECTION);

    }

    @Override
    public Map<String, String> getFaultSpecificArgs() {
        StopServiceFaultSpec stopServiceSpec = (StopServiceFaultSpec) faultSpec;
        Map<String, String> specificArgs = new HashMap<>();
        specificArgs.put(FaultConstants.SERVICE_NAME_ARG, String.valueOf(stopServiceSpec.getServiceName()));
        specificArgs.put(FaultConstants.TIMEOUT_IN_MILLI_SEC_ARG,
                String.valueOf(stopServiceSpec.getTimeoutInMilliseconds()));
        return specificArgs;
    }

}