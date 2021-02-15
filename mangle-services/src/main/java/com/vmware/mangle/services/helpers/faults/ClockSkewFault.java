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

import com.vmware.mangle.cassandra.model.faults.specs.ClockSkewSpec;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.task.framework.helpers.faults.SupportedEndpoints;
import com.vmware.mangle.utils.constants.FaultConstants;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author ashrimali
 *
 */
@SupportedEndpoints(endPoints = { EndpointType.MACHINE })
public class ClockSkewFault extends AbstractFault {

    /**
     * @param faultSpec
     * @throws MangleException
     */
    public ClockSkewFault(ClockSkewSpec clockSkewSpec) throws MangleException {
        super(clockSkewSpec, TaskType.INJECTION);
    }

    /* (non-Javadoc)
     * @see com.vmware.mangle.services.helpers.faults.AbstractFault#getFaultSpecificArgs()
     */
    @Override
    public Map<String, String> getFaultSpecificArgs() {

        ClockSkewSpec clockSkewSpec = (ClockSkewSpec) faultSpec;
        Map<String, String> specificArgs = new HashMap<>();
        specificArgs.put(FaultConstants.SECONDS_ARG, String.valueOf(clockSkewSpec.getSeconds()));
        specificArgs.put(FaultConstants.MINUTES_ARG, String.valueOf(clockSkewSpec.getMinutes()));
        specificArgs.put(FaultConstants.HOURS_ARG, String.valueOf(clockSkewSpec.getHours()));
        specificArgs.put(FaultConstants.DAYS_ARG, String.valueOf(clockSkewSpec.getDays()));
        specificArgs.put(FaultConstants.CLOCK_TYPE_ARG, clockSkewSpec.getClockSkewOperation().toString());

        return specificArgs;
    }
}