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

package com.vmware.mangle.utils.clients.ssh;

/**
 * @author uchikkegowda,jayasankarr
 */
public class SSHCommands {
    public static final String REMOVE_COMMAND = "rm -rf ";
    public static final String DEL_COMMAND = "DEL /F ";
    public static final String DISKFREE_COMMAND = "df -h %s | tail -1";
    public static final String DISKFREE_FILL_FSUTIL = "fsutil file createnew ";
    public static final String DISKFREE_COMMAND_WINDOWS = "wmic logicaldisk get size,freespace,caption";
    public static final String DB_PROMOTE_COMMAND = "vcac-config cluster-promote-master";
    public static final String NODE_RESET_COMMAND = "vcac-config  cluster-config-replica --reset";

    private SSHCommands() {

    }
}
