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

import com.vmware.mangle.services.enums.MangleQuorumStatus;

/**
 *
 *
 * @author chetanc
 */
public class HazelcastConstants {

    private HazelcastConstants() {

    }

    private static MangleQuorumStatus mangleQourumStatus = MangleQuorumStatus.NOT_PRESENT;
    private static int mangleQourum = 1;

    public static final String HAZELCAST_NODE_TASKS_MAP = "nodeTasks";
    public static final String HAZELCAST_TASKS_MAP = "tasks";
    public static final String MANGLE_APPLICATION_STATUS_MAP = "mangleApplicationStatusMap";
    public static final String HAZELCAST_MANGLE_NODE_CURRENT_STATUS_ATTRIBUTE = "mangleNodeCurrentStatus";
    public static final String HAZELCAST_MANGLE_SYNC_TOPIC_NAME = "syncOperationListener";
    public static final String HAZELCAST_PROPERTY_DEPLOYMENT_MODE = "deploymentType";

    public static MangleQuorumStatus getMangleQourumStatus() {
        return mangleQourumStatus;
    }

    public static void setMangleQourumStatus(MangleQuorumStatus mangleQourumStatus) {
        HazelcastConstants.mangleQourumStatus = mangleQourumStatus;
    }

    public static int getMangleQourum() {
        return mangleQourum;
    }

    public static void setMangleQourum(int mangleQourum) {
        HazelcastConstants.mangleQourum = mangleQourum;
    }
}
