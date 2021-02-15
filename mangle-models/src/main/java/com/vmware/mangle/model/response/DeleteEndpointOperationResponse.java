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

package com.vmware.mangle.model.response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 *
 * @author bkaranam
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeleteEndpointOperationResponse extends DeleteOperationResponse {
    private Map<String, List<String>> endpointGroupAssociations = new HashMap<>();
    private Map<String, List<String>> endpointChildAssociations = new HashMap<>();
}
