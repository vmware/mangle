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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author chetanc
 *
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VM implements VCenterVMObject {
    private Integer memory_size_MiB;
    private String vm;
    private String name;
    private String power_state;
    private Integer cpu_count;
}
