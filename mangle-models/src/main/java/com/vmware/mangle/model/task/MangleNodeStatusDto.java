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

package com.vmware.mangle.model.task;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.services.enums.MangleNodeStatus;

/**
 * @author bkaranam (bhanukiran karanam)
 *
 *         DTO for updating MangleNodeStatus.
 */

@Data
@EqualsAndHashCode(callSuper = true)
@ToString
public class MangleNodeStatusDto extends TaskSpec implements Serializable {
    private static final long serialVersionUID = 1L;
    private MangleNodeStatus nodeStatus;

    public MangleNodeStatusDto() {
        setSpecType(this.getClass().getName());
    }

    @Override
    @JsonIgnore
    public void setId(String id) {
        super.setId(id);
    }

}
