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

package com.vmware.mangle.cassandra.model.endpoint;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import lombok.Data;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import com.vmware.mangle.model.enums.EncryptField;

/**
 *
 * vcenter adapter model class
 *
 * @author chetanc
 */
@Table(value = "VCenterAdapter")
@Data
public class VCenterAdapterDetails {

    @NotEmpty
    @PrimaryKeyColumn(value = "name", ordering = Ordering.ASCENDING, type = PrimaryKeyType.PARTITIONED)
    @Pattern(regexp = "^[A-Za-z0-9-_.]+$", message = "consists only alphanumeric with special characters (_ - .)")
    private String name;
    @NotEmpty
    private String adapterUrl;
    @NotEmpty
    private String username;
    @NotEmpty
    @EncryptField
    private String password;
}
