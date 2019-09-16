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

import java.util.Arrays;
import java.util.List;

import org.springframework.core.annotation.AnnotationUtils;

import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.enums.K8SResource;
import com.vmware.mangle.services.helpers.faults.AbstractFault;
import com.vmware.mangle.task.framework.helpers.faults.SupportedEndpoints;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author bkaranam
 *
 *         Abstract K8S Specific Fault to valid fault support on resource types
 */
@SupportedEndpoints(endPoints = { EndpointType.K8S_CLUSTER })
public abstract class AbstractK8SSpecificFault extends AbstractFault {

    public AbstractK8SSpecificFault(K8SFaultSpec k8SFaultSpec) throws MangleException {
        super(k8SFaultSpec, TaskType.INJECTION);
        validateResoureTypeForFault(k8SFaultSpec.getResourceType());
    }

    private void validateResoureTypeForFault(K8SResource resourceType) throws MangleException {
        List<K8SResource> supportedResourceTypes = Arrays
                .asList(AnnotationUtils.findAnnotation(this.getClass(), SupportedResourceTypes.class).resourceTypes());
        if (!supportedResourceTypes.isEmpty() && !supportedResourceTypes.contains(resourceType)) {
            throw new MangleException(ErrorCode.UNSUPPORTED_K8S_RESOURCE_TYPE, this.getClass().getSimpleName(),
                    supportedResourceTypes);
        }
    }
}
