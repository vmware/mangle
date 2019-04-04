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

import com.vmware.mangle.cassandra.model.faults.specs.K8SResourceNotReadyFaultSpec;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.enums.K8SFaultName;
import com.vmware.mangle.services.enums.K8SResource;
import com.vmware.mangle.services.helpers.faults.SupportedEndpoints;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author hkilari
 *
 */
@SupportedEndpoints(endPoints = { EndpointType.K8S_CLUSTER })
@SupportedResourceTypes(resourceTypes = { K8SResource.POD, K8SResource.NODE })
public class K8SResourceNotReadyFault extends AbstractK8SSpecificFault {

    public K8SResourceNotReadyFault(K8SResourceNotReadyFaultSpec k8SFaultSpec) throws MangleException {
        super(k8SFaultSpec);
    }

    @Override
    protected Map<String, String> getFaultSpecificArgs() {
        Map<String, String> specificArgs = new HashMap<>();
        specificArgs.put("operation", K8SFaultName.NOTREADY_RESOURCE.name());
        return specificArgs;
    }
}
