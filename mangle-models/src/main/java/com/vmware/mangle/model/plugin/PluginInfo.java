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

package com.vmware.mangle.model.plugin;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * PluginInfo model.
 *
 * @author kumargautam
 */
@Data
public class PluginInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    @NotEmpty
    @ApiModelProperty(value = "Pass plugin file name", example = "mangle-default-plugin-2.0.0")
    private String pluginName;
    private PluginAction pluginAction;
}
