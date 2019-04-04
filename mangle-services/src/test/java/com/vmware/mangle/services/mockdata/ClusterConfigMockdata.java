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

package com.vmware.mangle.services.mockdata;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.vmware.mangle.cassandra.model.hazelcast.HazelcastClusterConfig;

/**
 * @author chetanc
 *
 *
 */
public class ClusterConfigMockdata {

    private String validationToken = UUID.randomUUID().toString();
    private String clusterName = "Mangle";
    private String ip = "127.0.0.1";
    private String ip1 = "127.0.0.2";


    public HazelcastClusterConfig getClusterConfigObject() {
        HazelcastClusterConfig config = new HazelcastClusterConfig();
        config.setClusterName(clusterName);
        config.setValidationToken(validationToken);
        Set<String> members = new HashSet<>();
        members.add(ip);
        config.setMembers(members);
        return config;
    }

    public HazelcastClusterConfig getModifiedClusterConfigObject() {
        HazelcastClusterConfig config = getClusterConfigObject();
        config.getMembers().add(ip1);
        return config;
    }
}
