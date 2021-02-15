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

package com.vmware.mangle.unittest.utils.clients.azure;

import com.microsoft.azure.management.compute.CachingTypes;
import com.microsoft.azure.management.compute.DataDisk;
import com.microsoft.azure.management.compute.DiskCreateOptionTypes;
import com.microsoft.azure.management.compute.StorageAccountTypes;
import com.microsoft.azure.management.compute.VirtualMachineDataDisk;

/**
 * Dummy virtual machine implementation for testing
 *
 * @author bkaranam
 */
public class DummyVirtualMachineDiskImpl implements VirtualMachineDataDisk {
    public static final String DISK_ID = "DummyDataDiskId";
    public static final String DISK_NAME = "DummyDataDisk";

    @Override
    public DataDisk inner() {
        return null;
    }

    @Override
    public String name() {
        return DISK_NAME;
    }

    @Override
    public String id() {
        return DISK_ID;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public int lun() {
        return 0;
    }

    @Override
    public CachingTypes cachingType() {
        return CachingTypes.READ_WRITE;
    }

    @Override
    public DiskCreateOptionTypes creationMethod() {
        return null;
    }

    @Override
    public StorageAccountTypes storageAccountType() {
        return null;
    }

}
