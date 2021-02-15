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
import java.util.stream.Collectors;

import com.vmware.mangle.cassandra.model.faults.specs.NetworkPartitionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.task.framework.helpers.faults.SupportedEndpoints;
import com.vmware.mangle.utils.constants.FaultConstants;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Fault class for {@link NetworkPartitionFaultSpec}.
 *
 * @author kumargautam
 */
@SupportedEndpoints(endPoints = { EndpointType.MACHINE })
public class NetworkPartitionFault extends AbstractFault {


    public NetworkPartitionFault(NetworkPartitionFaultSpec networkPartitionFaultSpec) throws MangleException {
        super(networkPartitionFaultSpec, TaskType.INJECTION);
    }

    @Override
    public Map<String, String> getFaultSpecificArgs() {
        NetworkPartitionFaultSpec networkPartitionFaultSpec = (NetworkPartitionFaultSpec) faultSpec;
        Map<String, String> specificArgs = new HashMap<>();
        specificArgs.put(FaultConstants.HOSTS_ARG,
                networkPartitionFaultSpec.getHosts().stream().collect(Collectors.joining(";")));
        return specificArgs;
    }
}
