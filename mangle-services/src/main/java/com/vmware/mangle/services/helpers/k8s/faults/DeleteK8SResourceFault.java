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

package com.vmware.mangle.services.helpers.k8s.faults;

import java.util.HashMap;
import java.util.Map;

import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.enums.K8SFaultName;
import com.vmware.mangle.services.helpers.faults.AbstractFault;
import com.vmware.mangle.services.helpers.faults.SupportedEndpoints;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author bkaranam K8S resource deletion Fault
 */
@SupportedEndpoints(endPoints = { EndpointType.K8S_CLUSTER })
public class DeleteK8SResourceFault extends AbstractFault {

    public DeleteK8SResourceFault(K8SFaultSpec k8SFaultSpec) throws MangleException {
        super(k8SFaultSpec, TaskType.INJECTION);
    }

    @Override
    protected Map<String, String> getFaultSpecificArgs() {
        Map<String, String> specificArgs = new HashMap<>();
        specificArgs.put("operation", K8SFaultName.DELETE_RESOURCE.name());
        return specificArgs;
    }
}
