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

package com.vmware.mangle.plugin.helpers.faults;

import java.util.HashMap;
import java.util.Map;

import org.pf4j.Extension;

import com.vmware.mangle.cassandra.model.faults.specs.PluginFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.plugin.model.faults.specs.HelloMangleFaultSpec;
import com.vmware.mangle.task.framework.helpers.faults.AbstractCustomFault;
import com.vmware.mangle.task.framework.helpers.faults.SupportedEndpoints;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * HelloMangleFault takes care of passing the user provided Fault Parameters to the Mangle Custom
 * Fault execution. The Custom Fault Developer needs to define an Implementation of
 * AbstractCustomFault for each Custom Fault.
 *
 * @author hkilari
 */
@SupportedEndpoints(endPoints = { EndpointType.MACHINE })
@Extension
public class HelloMangleFault extends AbstractCustomFault {


    /**
     * Method to pass the Inputs required for Custom fault Execution from the User inputs.
     */
    @Override
    public Map<String, String> getFaultSpecificArgs() {
        //Use associated FaultSpec Instead of the HelloMangleFaultSpec.
        HelloMangleFaultSpec localFaultSpec = (HelloMangleFaultSpec) faultSpec;
        Map<String, String> specificArgs = new HashMap<>();
        //Below code should be updated as per the Fields defined in the Associated Model Class
        specificArgs.put("field1", localFaultSpec.getField1());
        specificArgs.put("field2", localFaultSpec.getField2());
        if (EndpointType.MACHINE == faultSpec.getEndpoint().getEndPointType()) {
            specificArgs.put("__osType",
                    faultSpec.getEndpoint().getRemoteMachineConnectionProperties().getOsType().osType());
        }
        return specificArgs;
    }

    /**
     * Plugin developer is not encouraged to modify the default Implementation. Default
     * implementation takes care of making {@link HelloMangleFault#getFaultSpecificArgs()} available
     * at task Execution.
     */
    @Override
    public void init(PluginFaultSpec faultSpec) throws MangleException {
        super.init(faultSpec, TaskType.INJECTION);
    }

    /**
     * Provide the Associated Model Class which is the Custom Implementation of
     * {@link PluginFaultSpec}
     */
    @Override
    public Class<HelloMangleFaultSpec> getModelClass() {
        return HelloMangleFaultSpec.class;
    }
}
