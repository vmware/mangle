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

import static com.vmware.mangle.services.dto.AgentRuleConstants.EXCEPTION_CLASS_STRING;
import static com.vmware.mangle.services.dto.AgentRuleConstants.EXCEPTION_MESSAGE_STRING;
import static com.vmware.mangle.services.dto.AgentRuleConstants.HTTP_METHODS_STRING;
import static com.vmware.mangle.services.dto.AgentRuleConstants.SERVICES_STRING;
import static com.vmware.mangle.services.dto.AgentRuleConstants.SERVICE_SCOPE_STRING;

import java.util.HashMap;
import java.util.Map;

import com.vmware.mangle.cassandra.model.faults.specs.SpringServiceExceptionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.task.framework.helpers.faults.SupportedEndpoints;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author rpraveen
 *
 */
@SupportedEndpoints(endPoints = { EndpointType.K8S_CLUSTER, EndpointType.DOCKER, EndpointType.MACHINE })
public class SpringServiceExceptionFault extends AbstractFault {

    public SpringServiceExceptionFault(SpringServiceExceptionFaultSpec faultSpec) throws MangleException {
        super(faultSpec, TaskType.INJECTION);
    }

    @Override
    public Map<String, String> getFaultSpecificArgs() {
        SpringServiceExceptionFaultSpec localFaultSpec = (SpringServiceExceptionFaultSpec) faultSpec;
        Map<String, String> specificArgs = new HashMap<>();
        specificArgs.put(EXCEPTION_CLASS_STRING, localFaultSpec.getExceptionClass());
        specificArgs.put(EXCEPTION_MESSAGE_STRING, localFaultSpec.getExceptionMessage());
        specificArgs.put(SERVICES_STRING, localFaultSpec.getServicesString());
        specificArgs.put(SERVICE_SCOPE_STRING, Boolean.toString(localFaultSpec.isEnableOnLocalRequests()));
        specificArgs.put(HTTP_METHODS_STRING, localFaultSpec.getHttpMethodsString());
        return specificArgs;
    }
}