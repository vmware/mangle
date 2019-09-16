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

/**
 * @author chetanc
 *
 *         Constants that are used in VCenter Adapter Faults
 */

public class VCenterConstants {

    private VCenterConstants() {
    }

    public static final String VC_ADAPTER_CONTEXT_PATH = "mangle-vc-adapter";
    public static final String VC_ADAPTER_HEALTH_CHECK = VC_ADAPTER_CONTEXT_PATH + "/application/health";
    public static final String TEST_CONNECTION = VC_ADAPTER_CONTEXT_PATH + "/api/v1/vcenter/testconnection";
    public static final String POWER_OFF = VC_ADAPTER_CONTEXT_PATH + "/api/v1/vcenter/vm/%s/poweroff";
    public static final String POWER_ON = VC_ADAPTER_CONTEXT_PATH + "/api/v1/vcenter/vm/%s/poweron";
    public static final String SUSPEND_VM = VC_ADAPTER_CONTEXT_PATH + "/api/v1/vcenter/vm/%s/suspend";
    public static final String RESET_VM = VC_ADAPTER_CONTEXT_PATH + "/api/v1/vcenter/vm/%s/reset";
    public static final String DISCONNECT_NIC = VC_ADAPTER_CONTEXT_PATH + "/api/v1/vcenter/vm/%s/nic/%s/disconnect";
    public static final String CONNECT_NIC = VC_ADAPTER_CONTEXT_PATH + "/api/v1/vcenter/vm/%s/nic/%s/connect";
    public static final String DISCONNECT_DISK = VC_ADAPTER_CONTEXT_PATH + "/api/v1/vcenter/vm/%s/disk/%s/disconnect";
    public static final String CONNECT_DISK = VC_ADAPTER_CONTEXT_PATH + "/api/v1/vcenter/vm/%s/disk/%s/connect";
    public static final String TASK_STATUS = VC_ADAPTER_CONTEXT_PATH + "/api/v1/task/%s";

    public static final String TASK_STATUS_COMPLETED = "Completed";
    public static final String TASK_STATUS_FAILED = "Failed";
    public static final String TASK_STATUS_TRIGGERED = "Triggered";

    public static final String ALREADY_POWERED_OFF = "Virtual machine is already powered off";
    public static final String ALREADY_POWERED_ON = "Virtual machine is already powered on";
}
