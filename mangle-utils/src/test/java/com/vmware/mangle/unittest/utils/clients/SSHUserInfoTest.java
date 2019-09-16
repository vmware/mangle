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

package com.vmware.mangle.unittest.utils.clients;

import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.vmware.mangle.utils.clients.ssh.SSHUserInfo;

/**
 *
 *
 * @author chetanc
 */
public class SSHUserInfoTest {
    private String username = "username";
    private String pwd = "pwd";
    private String passphrase = UUID.randomUUID().toString();

    @Test
    public void testSSHUserInfo() {
        SSHUserInfo sshUserInfo = new SSHUserInfo(username, pwd, passphrase);
        Assert.assertEquals(sshUserInfo.getPassphrase(), passphrase);
        Assert.assertEquals(sshUserInfo.getPassword(), pwd);
        Assert.assertEquals(sshUserInfo.getUsername(), username);
        Assert.assertTrue(sshUserInfo.promptPassphrase(passphrase));
        Assert.assertTrue(sshUserInfo.promptPassword(pwd));
        Assert.assertFalse(sshUserInfo.promptYesNo(pwd));
        Assert.assertTrue(sshUserInfo.toString().contains(username));
    }
}
