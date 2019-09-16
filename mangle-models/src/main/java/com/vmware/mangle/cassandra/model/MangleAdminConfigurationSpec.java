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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * Table for Mangle Administrator Configuration properties
 *
 * @author ashrimali
 *
 */
@Table(value = "mangleConfiguration")
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(value = { "primaryKey" })
public class MangleAdminConfigurationSpec extends MangleDto implements Serializable {

    @ApiModelProperty(position = 0, value = "Mangle Configuration PropertyName")
    @PrimaryKey
    private String propertyName;

    @ApiModelProperty(position = 1, value = "Mangle Configuration PropertyValue")
    @Column
    private String propertyValue;

    @Override
    @JsonIgnore
    public String getPrimaryKey() {
        return propertyName;
    }

}