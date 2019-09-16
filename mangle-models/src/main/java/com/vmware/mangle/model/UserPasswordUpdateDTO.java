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

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 *
 * @author chetanc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPasswordUpdateDTO {

    @NotEmpty
    String currentPassword;
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%!&+=])(?=\\S+$).{8,}", message = "should consists at least 8 chars, one digit, one lower alpha char, one upper alpha char, and one char within a set of special chars (@#$%!&+=)")
    String newPassword;
}
