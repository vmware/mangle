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

import static org.mockito.Mockito.spy;

import lombok.extern.log4j.Log4j2;
import org.testng.annotations.Test;

import com.vmware.mangle.utils.CommandUtils;


/**
 *
 *
 * @author chetanc
 */
@Log4j2
public class CommandUtilsTest {
    private static String dummyCommand = "echo \"Testing command utils\"";

    CommandUtils commandUtils = spy(new CommandUtils());

    /***
     * Test method {@link CommandUtils#executeCommand(String)}
     */
    @Test
    public void executeCommandTest() {
        commandUtils.executeCommand(dummyCommand);
        log.info("Executing test executeCommandTest on the method CommandUtils#executeCommand");
    }

    /***
     * Test method {@link CommandUtils#runCommand(String, long)}
     */
    @Test
    public void runCommandTest() {
        CommandUtils.runCommand(dummyCommand, 5000).getCommandOutput();
        log.info("Executing test executeCommandTest on the method CommandUtils#runCommand");
    }
}
