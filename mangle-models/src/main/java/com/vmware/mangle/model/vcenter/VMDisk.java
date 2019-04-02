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

package com.vmware.mangle.model.vcenter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import com.vmware.mangle.model.MangleDto;

/**
 * Serves as dto for the VM Disk related operations
 *
 * @author chetanc
 *
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class VMDisk extends MangleDto {
    private String type;
    private Backing backing;

    public VMDisk() {
    }

    public VMDisk(String type, String backingType, String backingVmdkFile) {
        Backing backing = new Backing();
        backing.setVmdk_file(backingVmdkFile);
        backing.setType(backingType);
        this.type = type;
        this.backing = backing;
    }

    public VMDisk(String type, Backing backing) {
        this.type = type;
        this.backing = backing;
    }

    /**
     * Identifies the type of the disk that is backing VM
     */

    @Data
    public class Backing {
        private String type;
        private String vmdk_file;
    }

    public String getDiskInfo() {
        return String.format("type: %s; backing_type: %s; vmdk_file: %s", this.type, this.backing.type,
                this.backing.vmdk_file.replace(" ", "___"));
    }
}
