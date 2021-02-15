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

import java.io.Serializable;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

/**
 * VCenter Connection Properties model class
 *
 * @author kumargautam
 */
@UserDefinedType("vCenterConnectionProperties")
@Data
@ApiModel(description = "VCenter connection properties should be specified if endpoint type is VCENTER")
public class VCenterConnectionProperties implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "VCenter Server IP or Hostname")
    @NotEmpty
    private String hostname;

    @ApiModelProperty(value = "VCenter adapter details name that has to be used for delegating VCENTER faults")
    @NotEmpty
    private String vCenterAdapterDetailsName;

    @JsonIgnore
    @Transient
    private transient VCenterAdapterDetails vCenterAdapterDetails;

    @JsonIgnore
    public VCenterAdapterDetails getVCenterAdapterDetails() {
        return vCenterAdapterDetails;
    }

    @JsonIgnore
    public VCenterAdapterProperties getVCenterAdapterProperties() {
        VCenterAdapterProperties adapterProperties = new VCenterAdapterProperties();
        if (null != vCenterAdapterDetails) {
            adapterProperties.setVcAdapterUrl(vCenterAdapterDetails.getAdapterUrl());
            adapterProperties.setUsername(vCenterAdapterDetails.getUsername());
            adapterProperties.setPassword(vCenterAdapterDetails.getPassword());
        }
        return adapterProperties;
    }
}
