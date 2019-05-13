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

import com.vmware.mangle.services.enums.MangleNodeStatus;

/**
 *
 *
 * @author chetanc
 */
public class URLConstants {

    private URLConstants() {
    }

    private static MangleNodeStatus mangleNodeCurrentStatus = MangleNodeStatus.ACTIVE;
    public static final String MANGLE_CURRENT_STATUS_MESSAGE = "Current Mangle status:";
    //Due to some of the testexecutor tasks are taking more time so increasing the wait time for task to complete while upgrading mangle.
    public static final int TASKS_WAIT_TIME_SECONDS = 3600;
    public static final int RETRIGGER_THRESHOLD_TIME_IN_MINS = 30;
    public static final String SYSTEM_STATUS_OK = "Healthy";

    public static class UserUrl {
        public static final String USER = "/user";
    }


    public static final String FAILED_EXECUTION_STATUS =
            "Execution is failed, could not persist reports and logs failed to send email!";
    public static final String SERVICE_NAME = "serviceName";
    public static final String CONTAINER_NAME = "containerName";

    public static final String API_V1 = "/api/v1/";

    public static final String INJECTION_URL = API_V1 + "faults";
    public static final String ENDPOINT_URL = API_V1 + "endpoints";

    public static final String VCENTER_STATE_FAULT_API = INJECTION_URL + "/vcenter/state";
    public static final String VCENTER_NIC_FAULT_API = INJECTION_URL + "/vcenter/nic";
    public static final String VCENTER_DISK_FAULT_API = INJECTION_URL + "/vcenter/disk";
    public static final String DOCKER_FAULT_API = INJECTION_URL + "/docker";

    public static final String HAZELCAST_NODE_TASKS_MAP = "nodeTasks";
    public static final String HAZELCAST_TASKS_MAP = "tasks";
    public static final String HAZELCAST_MANGLE_NODE_CURRENT_STATUS_ATTRIBUTE = "mangleNodeCurrentStatus";


    public static MangleNodeStatus getMangleNodeCurrentStatus() {
        return mangleNodeCurrentStatus;
    }

    public static void setMangleNodeStatus(MangleNodeStatus mangleNodeCurrentStatusParameter) {
        mangleNodeCurrentStatus = mangleNodeCurrentStatusParameter;
    }
}
