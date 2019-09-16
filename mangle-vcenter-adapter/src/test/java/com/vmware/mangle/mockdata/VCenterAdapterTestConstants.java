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

package com.vmware.mangle.mockdata;

/**
 * @author chetanc
 *
 *
 */
public class VCenterAdapterTestConstants {
    private VCenterAdapterTestConstants() {
    }

    public static final String POWER_OFF = "api/v1/vcenter/vm/%s/poweroff";
    public static final String POWER_ON = "api/v1/vcenter/vm/%s/poweron";
    public static final String REBOOT_VM = "api/v1/vcenter/vm/%s/reset";
    public static final String SUSPEND_VM = "api/v1/vcenter/vm/%s/suspend";
    public static final String DISCONNECT_NIC = "api/v1/vcenter/vm/%s/nic/%s/disconnect";
    public static final String CONNECT_NIC = "api/v1/vcenter/vm/%s/nic/%s/connect";
    public static final String TEST_CONNECTION = "api/v1/vcenter/testconnection";
    public static final String TASK_QUERY = "api/v1/task/%s";
}
