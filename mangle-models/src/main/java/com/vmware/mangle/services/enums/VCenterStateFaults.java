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

package com.vmware.mangle.services.enums;

/**
 * @author chetanc
 *
 *         VCenter VM State change faults and remediation operations
 */
public enum VCenterStateFaults {
    POWEROFF_VM(VCenterFaultRemediation.POWERON_VM),
    SUSPEND_VM(VCenterFaultRemediation.POWERON_VM),
    RESTART_VM(null);

    private VCenterFaultRemediation remediation;

    @Override
    public String toString() {
        return this.name();
    }

    VCenterStateFaults(VCenterFaultRemediation vCenterFaultRemediation) {
        this.remediation = vCenterFaultRemediation;
    }

    public VCenterFaultRemediation getRemediation() {
        return remediation;
    }

}

