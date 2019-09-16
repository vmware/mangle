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

package com.vmware.mangle.unittest.faults.plugin.helpers;

import static com.vmware.mangle.utils.constants.FaultConstants.DEFAULT_TEMP_DIR;
import static com.vmware.mangle.utils.constants.FaultConstants.FAULT_NAME_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.FI_ADD_INFO_FAULTID;
import static com.vmware.mangle.utils.constants.FaultConstants.GET_FAULT_COMMAND;
import static com.vmware.mangle.utils.constants.FaultConstants.GET_FAULT_COMMAND_WITH_PORT;
import static com.vmware.mangle.utils.constants.FaultConstants.LOAD_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.PID_AGENT_COMMAND_WITH_PORT;
import static com.vmware.mangle.utils.constants.FaultConstants.PORT_9090;
import static com.vmware.mangle.utils.constants.FaultConstants.REMEDIATION_COMMAND;
import static com.vmware.mangle.utils.constants.FaultConstants.REMEDIATION_COMMAND_WITH_PORT;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.faults.plugin.helpers.JavaAgentFaultUtils;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Test Class for JavaAgentFaultUtils
 *
 * @author jayasankarr
 *
 */
public class JavaAgentFaultUtilsTest {

    private JavaAgentFaultUtils agentFaultUtils;

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        agentFaultUtils = new JavaAgentFaultUtils();
    }

    @Test
    public void testBuildInjectionCommand() throws MangleException {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, "dummy");
        args.put(LOAD_ARG, "10");
        String command = agentFaultUtils.buildInjectionCommand(args, DEFAULT_TEMP_DIR, PORT_9090);
        Assert.assertEquals(String.format(PID_AGENT_COMMAND_WITH_PORT, DEFAULT_TEMP_DIR, PORT_9090,
                FAULT_NAME_ARG + " dummy " + LOAD_ARG + " 10"), command);

    }

    @Test
    public void testBuildRemediationCommand() {
        String command = agentFaultUtils.buildRemediationCommand(DEFAULT_TEMP_DIR);
        Assert.assertEquals(String.format(REMEDIATION_COMMAND, DEFAULT_TEMP_DIR, FI_ADD_INFO_FAULTID), command);
    }

    @Test
    public void testBuildRemediationCommandWithPort() {
        String command = agentFaultUtils.buildRemediationCommand(DEFAULT_TEMP_DIR, PORT_9090);
        Assert.assertEquals(
                String.format(REMEDIATION_COMMAND_WITH_PORT, DEFAULT_TEMP_DIR, PORT_9090, FI_ADD_INFO_FAULTID),
                command);
    }

    @Test
    public void testBuildGetFaultCommand() {
        String command = agentFaultUtils.buildGetFaultCommand(DEFAULT_TEMP_DIR);
        Assert.assertEquals(String.format(GET_FAULT_COMMAND, DEFAULT_TEMP_DIR, FI_ADD_INFO_FAULTID), command);
    }

    @Test
    public void testBuildGetFaultCommandWithPort() {
        String command = agentFaultUtils.buildGetFaultCommand(DEFAULT_TEMP_DIR, PORT_9090);
        Assert.assertEquals(
                String.format(GET_FAULT_COMMAND_WITH_PORT, DEFAULT_TEMP_DIR, PORT_9090, FI_ADD_INFO_FAULTID), command);

    }
}
