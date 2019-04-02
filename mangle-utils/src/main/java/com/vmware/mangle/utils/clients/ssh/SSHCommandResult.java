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

import com.vmware.mangle.utils.exceptions.CommandFailedException;

/**
 * @author tsimchev
 *
 *         Represents SSH command output and exit code
 *
 */
public class SSHCommandResult {
    private int exitCode;
    private String stdOut;
    private String stdError;

    public static final int FAILURE_EXIT_CODE = 1;
    public static final int SUCCESS_EXIT_CODE = 0;

    public SSHCommandResult(int exitcode, String output) {
        exitCode = exitcode;
        stdOut = output;
    }

    public SSHCommandResult(int exitCode, String stdOut, String stdError) {
        this.exitCode = exitCode;
        this.stdOut = stdOut;
        this.stdError = stdError;
    }

    public SSHCommandResult() {
        exitCode = FAILURE_EXIT_CODE;
    }

    @Override
    public String toString() {
        return String.format("STDOUT%n%s STDERROR%n%s EXIT_CODE%n%s", stdOut, stdError, exitCode);
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public boolean isSucceeded() {
        return exitCode == SUCCESS_EXIT_CODE;
    }

    public String getStdOut() {
        return stdOut;
    }

    public String getStdError() {
        return stdError;
    }

    public void setStdOut(String stdOut) {
        this.stdOut = stdOut;
    }

    public void setStdError(String stdError) {
        this.stdError = stdError;
    }

    public void didCommandFail() {
        if (!this.isSucceeded()) {
            throw new CommandFailedException(this.getStdError());
        }
    }
}
