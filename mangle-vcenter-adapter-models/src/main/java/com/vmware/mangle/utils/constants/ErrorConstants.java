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

package com.vmware.mangle.utils.constants;

/**
 * Insert your comment for ErrorConstants here
 *
 * @author kumargautam
 */
public class ErrorConstants {

    private ErrorConstants() {
    }

    public static final String RESOURCE_POOL_NOT_FOUND = "Resource pool with name %s not found";
    public static final String RESOURCE_NOT_FOUND = "%s with name %s not found";
    public static final String MULTIPLE_RESOURCES_FOUND = "Multiple resources of type %s with name %s found";
    public static final String FOLDER_TYPE_NOT_PROVIDED = "Folders query failed, Folder type not provided";
    public static final String VM_POWER_OFF_SUCCESSFUL = "VM power off is completed successfully on VM %s";
    public static final String VM_POWER_ON_SUCCESSFUL = "VM power on is completed successfully on VM %s";
    public static final String HOST_DISCONNECTED_SUCCESSFUL = "Host %s is disconnected successfully" ;
    public static final String HOST_CONNECTED_SUCCESSFUL = "Host %s is connected successfully" ;
    public static final String VM_REBOOT_SUCCESSFUL = "VM reset operation is completed successfully on VM %s";
    public static final String VM_SUSPEND_SUCCESSFUL = "VM suspend operation is completed successfully on VM %s";
    public static final String VM_DISCONNECT_DISK_SUCCESSFUL =
            "VM delete disk operation is completed successfully on VM %s for disk id %s";
    public static final String VM_CONNECT_DISK_SUCCESSFUL =
            "VM add disk %s on VM %s is completed successfully";
    public static final String VM_DISCONNECT_NIC_SUCCESSFUL = "VM delete NIC %s on VM %s is completed successfully";
    public static final String VM_CONNECT_NIC_SUCCESSFUL = "VM add NIC %s on VM %s is completed successfully";
    public static final String VC_DEVICE_NOT_FOUND_ID = "com.vmware.api.vcenter.vm.device.not_found";
    public static final String VC_DEVICE_NOT_FOUND_ERROR =
            "Device with identifier %s does not exist on the virtual machine %s";
    public static final String VCENTER_NOT_REACHABLE = "VCenter server connection could not be established";
    public static final String VCENTER_AUTHENTICATION_FAILED = "VCenter Authentication Failed";
    public static final String VCENTER_OBJECT_COULD_NOT_FETCH = "VCenter %s object could not be fetched";
}
