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

package com.vmware.mangle.test.plugin.helpers.faults;

import static com.vmware.mangle.test.plugin.constants.CommonConstants.KILL_PROCESS_REMEDIATION_COMMAND_ARG;
import static com.vmware.mangle.test.plugin.constants.CommonConstants.OS_TYPE_ARG;
import static com.vmware.mangle.test.plugin.constants.CommonConstants.PROCESS_IDENTIFIER_ARG;

import java.util.HashMap;
import java.util.Map;

import org.pf4j.Extension;

import com.vmware.mangle.cassandra.model.faults.specs.PluginFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.cassandra.test.plugin.model.faults.specs.CustomKillProcessFaultSpec;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.task.framework.helpers.faults.AbstractCustomFault;
import com.vmware.mangle.task.framework.helpers.faults.SupportedEndpoints;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * KillProcessFault model.
 *
 * @author kumargautam
 */
@SupportedEndpoints(endPoints = { EndpointType.MACHINE, EndpointType.K8S_CLUSTER, EndpointType.DOCKER })
@Extension
public class CustomKillProcessFault extends AbstractCustomFault {

    @Override
    public Map<String, String> getFaultSpecificArgs() {
        CustomKillProcessFaultSpec localFaultSpec = (CustomKillProcessFaultSpec) faultSpec;
        Map<String, String> specificArgs = new HashMap<>();
        specificArgs.put(KILL_PROCESS_REMEDIATION_COMMAND_ARG, localFaultSpec.getRemediationCommand());
        specificArgs.put(PROCESS_IDENTIFIER_ARG, localFaultSpec.getProcessIdentifier());
        if (EndpointType.MACHINE == faultSpec.getEndpoint().getEndPointType()) {
            specificArgs.put(OS_TYPE_ARG,
                    faultSpec.getEndpoint().getRemoteMachineConnectionProperties().getOsType().osType());
        }
        return specificArgs;
    }

    /* (non-Javadoc)
     * @see com.vmware.mangle.task.framework.helpers.faults.AbstractCustomFault#init(com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec)
     */
    @Override
    public void init(PluginFaultSpec faultSpec) throws MangleException {
        super.init(faultSpec, TaskType.INJECTION);
    }

    /* (non-Javadoc)
     * @see com.vmware.mangle.task.framework.helpers.faults.AbstractCustomFault#getModelClass()
     */
    @Override
    public Class<CustomKillProcessFaultSpec> getModelClass() {
        return CustomKillProcessFaultSpec.class;
    }
}
