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

package com.vmware.mangle.model;

import java.util.Set;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import com.vmware.mangle.model.constants.Constants;

/**
 * @author chetanc
 */
@Data
public class UserCreationDTO {
    private String name;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Pattern(regexp = Constants.AUTH_PD_PATTERN, message = Constants.AUTH_PATTERN_MESSAGE)
    private String password;
    private Set<String> roleNames;
}
