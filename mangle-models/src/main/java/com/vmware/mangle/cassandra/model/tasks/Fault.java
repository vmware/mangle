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

package com.vmware.mangle.cassandra.model.tasks;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import com.vmware.mangle.cassandra.model.MangleDto;


/**
 * @author hkilari
 *
 */
@Table(value = "MangleFault")
@Data
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(value = { "primaryKey" })
public class Fault extends MangleDto {

    private static final long serialVersionUID = 1L;
    @PrimaryKey
    private String id;
    protected String faultName;
    protected String faultAction;
    protected String faultDescription;
    protected String faultType;

    public Fault() {
        this.id = super.generateId();
    }

    @JsonIgnore
    @Override
    public String getPrimaryKey() {
        return this.id;
    }
}
