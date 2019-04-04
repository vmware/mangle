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

package com.vmware.mangle.java.agent.faults;

import java.util.Map;

/**
 * @author hkilari
 *
 */
public class FaultInfo {
    private boolean longLasting;
    private Map<String, String> faultArgs;
    private FaultStatus faultStatus;
    private String faultName;
    private StringBuilder taskActivity = new StringBuilder();

    public boolean isLongLasting() {
        return longLasting;
    }

    public void setLongLasting(boolean longLasting) {
        this.longLasting = longLasting;
    }

    public Map<String, String> getFaultArgs() {
        return faultArgs;
    }

    public void setFaultArgs(Map<String, String> faultArgs) {
        this.faultArgs = faultArgs;
    }

    public FaultStatus getFaultStatus() {
        return faultStatus;
    }

    public void setFaultStatus(FaultStatus faultStatus) {
        this.faultStatus = faultStatus;
    }

    public StringBuilder getTaskActivity() {
        return taskActivity;
    }

    public void appendTaskActivity(String message) {
        this.taskActivity.append("\n" + message);
    }

    public String getFaultName() {
        return faultName;
    }

    public void setFaultName(String faultName) {
        this.faultName = faultName;
    }

    @Override
    public String toString() {
        return "FaultInfo [longLasting=" + longLasting + ", faultArgs=" + faultArgs + ", faultStatus=" + faultStatus
                + ", faultName=" + faultName + ", taskActivity=" + taskActivity + "]";
    }

}
