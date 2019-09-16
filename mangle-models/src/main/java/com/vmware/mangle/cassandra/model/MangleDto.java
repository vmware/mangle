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

package com.vmware.mangle.cassandra.model;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;


/**
 *
 * @author kumargautam
 */
@Data
public abstract class MangleDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    public abstract String getPrimaryKey();

    public String generateId() {
        return UUID.randomUUID().toString();
    }
}
