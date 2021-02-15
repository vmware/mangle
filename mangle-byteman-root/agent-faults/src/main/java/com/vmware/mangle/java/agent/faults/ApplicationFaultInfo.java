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

/**
 * @author hkilari
 *
 */
public class ApplicationFaultInfo {
    private FaultStatus faultStatus;
    private String ScriptText;

    private long timeout;

    public FaultStatus getFaultStatus() {
        return faultStatus;
    }

    public void setFaultStatus(FaultStatus faultStatus) {
        this.faultStatus = faultStatus;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String getScriptText() {
        return ScriptText;
    }

    public void setScriptText(String scriptText) {
        ScriptText = scriptText;
    }

    @Override
    public String toString() {
        return "FaultInfo [faultStatus=" + faultStatus + ", ScriptText=" + ScriptText + ", timeout=" + timeout + "]";
    }

}
