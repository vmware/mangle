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
public class ApplicationFault {
    private String id;
    private ApplicationFaultInfo faultInfo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ApplicationFaultInfo getFaultInfo() {
        return faultInfo;
    }

    public void setFaultInfo(ApplicationFaultInfo faultInfo) {
        this.faultInfo = faultInfo;
    }
}
