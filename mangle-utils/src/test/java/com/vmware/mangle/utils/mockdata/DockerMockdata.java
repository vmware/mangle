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

package com.vmware.mangle.utils.mockdata;

/**
 * Unit Test Case for DockerClientBuilder.
 *
 * @author chetanc
 */
public class DockerMockdata {
    private final String host = "10.134.211.2";
    private final String port = "2375";
    private final String containerName = "test";
    private final String containerId = "test1";

    public String getMockHost() {
        return this.host;
    }

    public String getMockPort() {
        return this.port;
    }

    public String getMockContainerName() {
        return this.containerName;
    }

    public String getMockContainerId() {
        return this.containerId;
    }
}
