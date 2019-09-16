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

import static com.vmware.mangle.services.constants.CommonConstants.FAULT_OPERATION_ARG;
import static com.vmware.mangle.services.constants.CommonConstants.LATENCY_ARG;
import static com.vmware.mangle.services.constants.CommonConstants.NIC_NAME_ARG;
import static com.vmware.mangle.services.constants.CommonConstants.PERCENTAGE_ARG;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vmware.mangle.cassandra.model.faults.specs.NetworkFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.enums.NetworkFaultType;
import com.vmware.mangle.task.framework.helpers.faults.SupportedEndpoints;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * NetworkFault model.
 *
 * @author kumargautam,jayasankarr
 */
@SupportedEndpoints(endPoints = { EndpointType.MACHINE })
public class NetworkFault extends AbstractFault {

    public NetworkFault(NetworkFaultSpec networkLatencySpec) throws MangleException {
        super(networkLatencySpec, TaskType.INJECTION);
        validateForNetworkFaultTypeSupport(networkLatencySpec);
    }

    @Override
    protected Map<String, String> getFaultSpecificArgs() {
        NetworkFaultSpec networkFaultSpec = (NetworkFaultSpec) faultSpec;
        Map<String, String> specificArgs = new HashMap<>();
        specificArgs.put(FAULT_OPERATION_ARG, networkFaultSpec.getFaultOperation().networkFaultType());
        specificArgs.put(LATENCY_ARG, String.valueOf(networkFaultSpec.getLatency()));
        specificArgs.put(PERCENTAGE_ARG, String.valueOf(networkFaultSpec.getPercentage()));
        specificArgs.put(NIC_NAME_ARG, networkFaultSpec.getNicName());
        return specificArgs;
    }

    private boolean validateForNetworkFaultTypeSupport(NetworkFaultSpec networkFaultSpec) throws MangleException {
        List<NetworkFaultType> supportedNetworkFaultTypes = Arrays.asList(NetworkFaultType.values());
        if (!supportedNetworkFaultTypes.contains(networkFaultSpec.getFaultOperation())) {
            throw new MangleException(ErrorCode.INCORRECT_NETWORK_FAULT_TYPE, supportedNetworkFaultTypes);
        }
        return true;
    }

}
