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

package com.vmware.mangle.faults.plugin.helpers.systemresource;

import org.springframework.beans.factory.annotation.Autowired;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.ssh.SSHUtils;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author jayasankarr
 *
 */

public class LinuxSystemResourceFaultHelper extends SystemResourceFaultHelper {

    private EndpointClientFactory endpointClientFactory;

    @Autowired
    public LinuxSystemResourceFaultHelper(EndpointClientFactory endpointClientFactory,
            SystemResourceFaultUtils systemResourceFaultUtils) {
        super(systemResourceFaultUtils);
        this.endpointClientFactory = endpointClientFactory;
    }

    @Override
    public ICommandExecutor getExecutor(CommandExecutionFaultSpec faultSpec) throws MangleException {
        return (SSHUtils) endpointClientFactory.getEndPointClient(faultSpec.getCredentials(), faultSpec.getEndpoint());
    }
}