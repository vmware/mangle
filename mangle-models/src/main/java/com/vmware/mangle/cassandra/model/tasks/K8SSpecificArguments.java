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

import java.io.Serializable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

/**
 * @author bkaranam
 *
 *
 */
@UserDefinedType("K8SSpecificArguments")
@Data
@EqualsAndHashCode(callSuper = false)
public class K8SSpecificArguments implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "Value for Label with key as app of the pod")
    @NotEmpty
    private String podLabels;
    @ApiModelProperty(value = "Target container name")
    @NotEmpty
    private String containerName;

    @ApiModelProperty(value = "Enable/Disable Random injection", example = "true")
    @NotNull
    private Boolean enableRandomInjection = true;
    @JsonIgnore
    private String podInAction;
}
