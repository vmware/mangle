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

package com.vmware.mangle.model.enums;

/**
 * @author chetanc
 */
public enum VCenterResources {
    VM("VM"), CLUSTER("Cluster"), DATA_CENTER("Data Center"), HOST("Host"), RESOURCE_POOL("Resource Pool"), FOLDER(
            "Folder"), VM_NIC("VM NIC"), VM_DISK("VM Disk");

    private VCenterResources(String name) {
        this.name = name;
    }

    private String name;

    private String getName() {
        return this.name;
    }


    @Override
    public String toString() {
        return this.name;
    }
}
