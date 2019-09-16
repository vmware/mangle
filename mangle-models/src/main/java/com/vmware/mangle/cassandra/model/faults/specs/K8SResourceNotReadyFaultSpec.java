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

package com.vmware.mangle.cassandra.model.faults.specs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.vmware.mangle.services.enums.K8SFaultName;

/**
 * @author bkaranam
 *
 *         Api payload specification for K8S Delete Resource fault spec
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class K8SResourceNotReadyFaultSpec extends K8SFaultSpec {
    private static final long serialVersionUID = 1L;

    @JsonProperty(required = false)
    private String appContainerName;

    public K8SResourceNotReadyFaultSpec() {
        setFaultName(K8SFaultName.NOTREADY_RESOURCE.name());
        setSpecType(this.getClass().getName());
    }
}
