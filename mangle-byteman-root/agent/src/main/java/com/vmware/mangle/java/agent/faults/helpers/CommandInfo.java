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

package com.vmware.mangle.java.agent.faults.helpers;

/**
 * @author hkilari
 *
 */
public class CommandInfo {

    private String command;
    private boolean ignoreExitValueCheck;
    private String expectedCommandOutput;
    private int noOfArgs;
    private int timeout;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public boolean isIgnoreExitValueCheck() {
        return ignoreExitValueCheck;
    }

    public void setIgnoreExitValueCheck(boolean ignoreExitValueCheck) {
        this.ignoreExitValueCheck = ignoreExitValueCheck;
    }

    public String getExpectedCommandOutput() {
        return expectedCommandOutput;
    }

    public void setExpectedCommandOutput(String expectedCommandOutput) {
        this.expectedCommandOutput = expectedCommandOutput;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getNoOfArgs() {
        return noOfArgs;
    }

    public void setNoOfArgs(int noOfArgs) {
        this.noOfArgs = noOfArgs;
    }

    @Override
    public String toString() {
        return "CommandInfo [command=" + command + ", ignoreExitValueCheck=" + ignoreExitValueCheck
                + ", expectedCommandOutput=" + expectedCommandOutput + ", noOfArgs=" + noOfArgs + ", timeout=" + timeout
                + "]";
    }
}
