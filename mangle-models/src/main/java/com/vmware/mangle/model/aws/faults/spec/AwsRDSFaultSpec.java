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

import com.datastax.driver.core.DataType.Name;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.cassandra.core.mapping.CassandraType;

import com.vmware.mangle.model.aws.AwsRDSFaults;
import com.vmware.mangle.model.aws.AwsRDSInstance;

/**
 * @author bkaranam
 *
 *         Fault spec for all the AWS RDS faults and remediation
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class AwsRDSFaultSpec extends AwsFaultSpec {
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private List<AwsRDSInstance> selectedRDSInstances;
    @NotEmpty
    private List<String> dbIdentifiers;

    @CassandraType(type = Name.VARCHAR)
    private AwsRDSFaults fault;

    public AwsRDSFaultSpec() {
        setSpecType(this.getClass().getName());
    }

    @JsonIgnore
    @Override
    public void setAwsTags(HashMap<String, String> awsTags) {
        super.setAwsTags(new HashMap<>());
    }
}
