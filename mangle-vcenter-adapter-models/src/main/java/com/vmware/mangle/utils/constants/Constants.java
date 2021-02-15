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

package com.vmware.mangle.utils.constants;

import org.springframework.core.ParameterizedTypeReference;

import com.vmware.mangle.model.Cluster;
import com.vmware.mangle.model.Datacenter;
import com.vmware.mangle.model.Folder;
import com.vmware.mangle.model.Host;
import com.vmware.mangle.model.ResourceList;
import com.vmware.mangle.model.ResourcePool;
import com.vmware.mangle.model.VM;
import com.vmware.mangle.model.VMDisk;
import com.vmware.mangle.model.VMNic;

/**
 *
 * @author chetanc
 */
public class Constants {
    private Constants() {
    }

    public static int RETRY_COUNT = 60;
    public static String DEFAULT_USER = "admin";
    public static String URL_PARAM_SEPARATOR = "&";
    public static String URL_QUERY_SEPARATOR = "?";

    public static final ParameterizedTypeReference<ResourceList<Cluster>> CLUSTER_RESOURCE_LIST =
            new ParameterizedTypeReference<ResourceList<Cluster>>() {
            };

    public static final ParameterizedTypeReference<ResourceList<Datacenter>> DATA_CENTER_RESOURCE_LIST =
            new ParameterizedTypeReference<ResourceList<Datacenter>>() {
            };

    public static final ParameterizedTypeReference<ResourceList<Host>> HOST_RESOURCE_LIST =
            new ParameterizedTypeReference<ResourceList<Host>>() {
            };

    public static final ParameterizedTypeReference<ResourceList<Folder>> FOLDER_RESOURCE_LIST =
            new ParameterizedTypeReference<ResourceList<Folder>>() {
            };

    public static final ParameterizedTypeReference<ResourceList<VM>> VM_RESOURCE_LIST =
            new ParameterizedTypeReference<ResourceList<VM>>() {
            };

    public static final ParameterizedTypeReference<ResourceList<ResourcePool>> RSP_RESOURCE_LIST =
            new ParameterizedTypeReference<ResourceList<ResourcePool>>() {
            };

    public static final ParameterizedTypeReference<ResourceList<VMNic>> VM_ETHERNET_LIST =
            new ParameterizedTypeReference<ResourceList<VMNic>>() {
            };

    public static final ParameterizedTypeReference<ResourceList<VMDisk>> VM_DISK_LIST =
            new ParameterizedTypeReference<ResourceList<VMDisk>>() {
            };
}
