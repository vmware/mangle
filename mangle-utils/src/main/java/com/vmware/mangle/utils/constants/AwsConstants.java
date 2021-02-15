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

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

/**
 * @author bkaranam
 *
 *         Constants that are used in Aws utils
 */

public class AwsConstants {

    private AwsConstants() {
    }

    public static final String SUBSCRIPTIONS = "subscriptions";
    public static final String RESOURCEGROUPS = "resourceGroups";
    public static final String PROVIDERS = "providers";
    public static final String VIRTUALMACHINES = "virtualMachines";
    public static final List<String> linuxDeviceNames = new ArrayList<>();
    public static final List<String> windowsDeviceNames = new ArrayList<>();

    public static final List<String> linuxDeviceNames() {
        if (!CollectionUtils.isEmpty(linuxDeviceNames)) {
            linuxDeviceNames.add("/dev/sdf");
            linuxDeviceNames.add("/dev/sdg");
            linuxDeviceNames.add("/dev/sdh");
            linuxDeviceNames.add("/dev/sdi");
            linuxDeviceNames.add("/dev/sdj");
            linuxDeviceNames.add("/dev/sdk");
            linuxDeviceNames.add("/dev/sdl");
            linuxDeviceNames.add("/dev/sdm");
            linuxDeviceNames.add("/dev/sdn");
            linuxDeviceNames.add("/dev/sdo");
            linuxDeviceNames.add("/dev/sdp");
        }
        return linuxDeviceNames;
    }

    public static final List<String> windowsDeviceNames() {
        if (!CollectionUtils.isEmpty(windowsDeviceNames)) {
            windowsDeviceNames.add("xvdf");
            windowsDeviceNames.add("xvdg");
            windowsDeviceNames.add("xvdh");
            windowsDeviceNames.add("xvdi");
            windowsDeviceNames.add("xvdj");
            windowsDeviceNames.add("xvdk");
            windowsDeviceNames.add("xvdl");
            windowsDeviceNames.add("xvdm");
            windowsDeviceNames.add("xvdn");
            windowsDeviceNames.add("xvdo");
            windowsDeviceNames.add("xvdp");
        }
        return windowsDeviceNames;
    }
}
