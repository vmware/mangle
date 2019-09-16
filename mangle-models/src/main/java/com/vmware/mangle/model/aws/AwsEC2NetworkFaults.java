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

package com.vmware.mangle.model.aws;

/**
 * @author bkaranam
 *
 *         AWS EC2 Instance Network faults and remediation operations
 */
public enum AwsEC2NetworkFaults {
    BLOCK_ALL_NETWORK_TRAFFIC(AwsEC2FaultRemediation.UNBLOCK_ALL_NETWORK_TRAFFIC);

    private AwsEC2FaultRemediation remediation;

    @Override
    public String toString() {
        return this.name();
    }

    AwsEC2NetworkFaults(AwsEC2FaultRemediation awsEC2FaultRemediation) {
        this.remediation = awsEC2FaultRemediation;
    }

    public AwsEC2FaultRemediation getRemediation() {
        return remediation;
    }

}