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

package com.vmware.mangle.unittest.services.helpers;

import java.util.Collections;
import java.util.Map;

import com.vmware.mangle.cassandra.model.faults.specs.PluginFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.task.framework.helpers.faults.AbstractCustomFault;
import com.vmware.mangle.task.framework.helpers.faults.SupportedEndpoints;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author hkilari
 *
 */
@SupportedEndpoints(endPoints = { EndpointType.MACHINE })
public class CustomFault extends AbstractCustomFault {

    /* (non-Javadoc)
     * @see com.vmware.mangle.task.framework.helpers.faults.AbstractCustomFault#getFaultSpecificArgs()
     */
    @Override
    protected Map<String, String> getFaultSpecificArgs() {
        return Collections.emptyMap();
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
    public Class<? extends PluginFaultSpec> getModelClass() {
        return CustomKillProcessFaultSpec.class;
    }
}