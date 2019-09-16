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

import org.junit.Assert;
import org.testng.annotations.Test;

import com.vmware.mangle.utils.clients.ssh.SSHCommandResult;
import com.vmware.mangle.utils.exceptions.CommandFailedException;

/**
 *
 *
 * @author chetanc
 */
public class SSHCommandResultTest {

    @Test
    public void testSSHCommandResult() {
        SSHCommandResult sshCommandResult = new SSHCommandResult(SSHCommandResult.SUCCESS_EXIT_CODE, "Success");
        Assert.assertTrue(sshCommandResult.isSucceeded());
        Assert.assertEquals("Success", sshCommandResult.getStdOut());
    }

    @Test
    public void testSSHCommandResult2() {
        SSHCommandResult sshCommandResult =
                new SSHCommandResult(SSHCommandResult.SUCCESS_EXIT_CODE, "Success", "Nothing");
        Assert.assertTrue(sshCommandResult.isSucceeded());
        Assert.assertEquals("Success", sshCommandResult.getStdOut());
        Assert.assertEquals("Nothing", sshCommandResult.getStdError());
        String result = sshCommandResult.toString();
        Assert.assertTrue(result.contains("Success") && result.contains("Nothing")
                && result.contains(String.valueOf(SSHCommandResult.SUCCESS_EXIT_CODE)));
    }

    @Test(expectedExceptions = CommandFailedException.class)
    public void testSSHCommandResult3() {
        SSHCommandResult sshCommandResult =
                new SSHCommandResult(SSHCommandResult.FAILURE_EXIT_CODE, "Failure", "Failed to execute the command");
        Assert.assertFalse(sshCommandResult.isSucceeded());
        sshCommandResult.didCommandFail();
    }
}
