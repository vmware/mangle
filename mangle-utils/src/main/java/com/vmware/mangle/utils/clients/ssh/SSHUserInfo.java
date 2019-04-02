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

import com.jcraft.jsch.UserInfo;

/**
 * @author tsimchev
 *
 *         Represents SSH credentials for a remote machine
 *
 */
public class SSHUserInfo implements UserInfo {

    private final String username;
    private final String passowrd;
    private final String passphrase;

    public SSHUserInfo(String username, String password, String passphrase) {
        this.username = username;
        this.passowrd = password;
        this.passphrase = passphrase;
    }

    public SSHUserInfo(String username, String password) {
        this(username, password, "");
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String getPassphrase() {
        return passphrase;
    }

    @Override
    public String getPassword() {
        return passowrd;
    }

    @Override
    public boolean promptPassphrase(String message) {
        return true;
    }

    @Override
    public boolean promptPassword(String message) {
        return true;
    }

    @Override
    public boolean promptYesNo(String message) {
        return false;
    }

    @Override
    public void showMessage(String arg0) {
        //Not customised showMessage method
    }

    @Override
    public String toString() {
        return String.format("username=%s", username);
    }

}
