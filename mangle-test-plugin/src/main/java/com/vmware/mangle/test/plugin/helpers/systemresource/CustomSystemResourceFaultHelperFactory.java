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

package com.vmware.mangle.test.plugin.helpers.systemresource;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;

/**
 * @author jayasankarr
 *
 *         Factory class to get the helper class for system resource fault tasks, it takes endpoint
 *         as an optional argument.
 */
@Component
public class CustomSystemResourceFaultHelperFactory {

    private CustomLinuxSystemResourceFaultHelper linuxSystemResourceFaultHelper;
    private CustomK8sSystemResourceFaultHelper k8sSystemResourceFaultHelper;
    private CustomDockerSystemResourceFaultHelper dockerSystemResourceFaultHelper;

    @Autowired
    public void setLinuxSystemResourceFaultHelper(CustomLinuxSystemResourceFaultHelper linuxSystemResourceFaultHelper) {
        this.linuxSystemResourceFaultHelper = linuxSystemResourceFaultHelper;
    }

    @Autowired
    public void setK8sSystemResourceFaultHelper(CustomK8sSystemResourceFaultHelper k8sSystemResourceFaultHelper) {
        this.k8sSystemResourceFaultHelper = k8sSystemResourceFaultHelper;
    }

    @Autowired
    public void setDockerSystemResourceFaultHelper(CustomDockerSystemResourceFaultHelper dockerSystemResourceFaultHelper) {
        this.dockerSystemResourceFaultHelper = dockerSystemResourceFaultHelper;
    }

    public CustomSystemResourceFaultHelper getHelper(@NonNull EndpointSpec endpoint) {
        switch (endpoint.getEndPointType()) {
        case DOCKER:
            return dockerSystemResourceFaultHelper;
        case K8S_CLUSTER:
            return k8sSystemResourceFaultHelper;
        case MACHINE:
            return linuxSystemResourceFaultHelper;
        default:
            return null;
        }
    }
}