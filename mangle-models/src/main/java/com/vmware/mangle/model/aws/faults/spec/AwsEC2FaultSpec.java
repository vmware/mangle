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

package com.vmware.mangle.model.aws.faults.spec;

import java.util.HashMap;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author bkaranam
 *
 *         Fault spec for all the AWS EC2 instance state related faults and remediation
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class AwsEC2FaultSpec extends AwsFaultSpec {
    private static final long serialVersionUID = 1L;
    @JsonIgnore
    private List<String> instanceIds;

    public AwsEC2FaultSpec() {
        setSpecType(this.getClass().getName());
    }

    @NotEmpty
    @Override
    public HashMap<String, String> getAwsTags() {
        return super.getAwsTags();
    }
}
