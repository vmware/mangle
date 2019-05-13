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

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 *
 * @author Chethan C(chetanc)
 *
 *         pojo for retrieving connection details from the api requests
 */
@Data
public class VCenterSpec {
    private String vcServerUrl;
    private String vcUsername;
    private String vcPassword;

    /**
     * Validates if the object holds non-empty attributes in the VC spec
     *
     * @return: true if all of the properties of the VCenter spec are, not null or
     *          not empty; else false
     */
    @JsonIgnore
    public boolean isValidAuthBody() {
        return ((vcServerUrl != null && !vcServerUrl.isEmpty()) && (vcUsername != null && !vcUsername.isEmpty())
                && (vcPassword != null && !vcPassword.isEmpty()));
    }
}
