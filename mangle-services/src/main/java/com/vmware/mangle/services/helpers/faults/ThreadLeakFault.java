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

import static com.vmware.mangle.services.constants.CommonConstants.ENABLE_OUT_OF_MEMORY_ARG;

import java.util.HashMap;
import java.util.Map;

import com.vmware.mangle.cassandra.model.faults.specs.ThreadLeakFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.task.framework.helpers.faults.SupportedEndpoints;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author jayasankarr
 *
 */
@SupportedEndpoints(endPoints = { EndpointType.K8S_CLUSTER, EndpointType.DOCKER, EndpointType.MACHINE })
public class ThreadLeakFault extends AbstractFault {
    public ThreadLeakFault(ThreadLeakFaultSpec faultSpec) throws MangleException {
        super(faultSpec, TaskType.INJECTION);
    }

    @Override
    public Map<String, String> getFaultSpecificArgs() {
        ThreadLeakFaultSpec threadLeakFaultSpec = (ThreadLeakFaultSpec) faultSpec;
        Map<String, String> specificArgs = new HashMap<>();
        specificArgs.put(ENABLE_OUT_OF_MEMORY_ARG, String.valueOf(threadLeakFaultSpec.isEnableOOM()));
        return specificArgs;
    }

}
