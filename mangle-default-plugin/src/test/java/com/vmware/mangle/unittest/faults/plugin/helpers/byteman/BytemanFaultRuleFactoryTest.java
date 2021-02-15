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

package com.vmware.mangle.unittest.faults.plugin.helpers.byteman;

import static org.testng.Assert.assertTrue;

import static com.vmware.mangle.services.dto.AgentRuleConstants.CLASS_NAME;
import static com.vmware.mangle.services.dto.AgentRuleConstants.METHOD_NAME;
import static com.vmware.mangle.services.dto.AgentRuleConstants.RULE_EVENT;
import static com.vmware.mangle.utils.constants.FaultConstants.TASK_ID;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.faults.plugin.helpers.byteman.BytemanFaultRuleFactory;
import com.vmware.mangle.services.dto.AgentRuleConstants;
import com.vmware.mangle.services.enums.BytemanFaultType;

/**
 * Unit test cases for BytemanFaultRuleFactory.
 *
 * @author kumargautam
 */
public class BytemanFaultRuleFactoryTest {

    @BeforeClass
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.faults.plugin.helpers.byteman.BytemanFaultRuleFactory#getExceptionRule(com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec)}.
     */
    @Test
    public void testGetExceptionRule() {
        CommandExecutionFaultSpec faultSpec = getCommandExecutionFaultSpec();
        faultSpec.setFaultType(BytemanFaultType.EXCEPTION.name());
        assertTrue(BytemanFaultRuleFactory.getExceptionRule(faultSpec).contains("throwException"));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.faults.plugin.helpers.byteman.BytemanFaultRuleFactory#getThreadInterruptionRule(com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec)}.
     */
    @Test
    public void testGetThreadInterruptionRule() {
        CommandExecutionFaultSpec faultSpec = getCommandExecutionFaultSpec();
        faultSpec.setFaultType(BytemanFaultType.THREAD_INTERRUPTION.name());
        assertTrue(BytemanFaultRuleFactory.getThreadInterruptionRule(faultSpec).contains("interrupt()"));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.faults.plugin.helpers.byteman.BytemanFaultRuleFactory#getKillJvmRule(com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec)}.
     */
    @Test
    public void testGetKillJvmRule() {
        CommandExecutionFaultSpec faultSpec = getCommandExecutionFaultSpec();
        faultSpec.setFaultType(BytemanFaultType.KILL_JVM.name());
        faultSpec.getArgs().put("exitCode", "-1");
        assertTrue(BytemanFaultRuleFactory.getKillJvmRule(faultSpec).contains("Calling killJVM with exitCode"));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.faults.plugin.helpers.byteman.BytemanFaultRuleFactory#getKillThreadRule(com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec)}.
     */
    @Test
    public void testGetKillThreadRule() {
        CommandExecutionFaultSpec faultSpec = getCommandExecutionFaultSpec();
        faultSpec.setFaultType(BytemanFaultType.KILL_THREAD.name());
        assertTrue(BytemanFaultRuleFactory.getKillThreadRule(faultSpec).contains("killThread()"));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.faults.plugin.helpers.byteman.BytemanFaultRuleFactory#getManipulateReturnObjectRule(com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec)}.
     */
    @Test
    public void testGetManipulateReturnObjectRule() {
        CommandExecutionFaultSpec faultSpec = getCommandExecutionFaultSpec();
        faultSpec.setFaultType(BytemanFaultType.MANIPULATE_RETURN_OBJECT.name());
        faultSpec.getArgs().put("returnValueJsonNotation", "\\test");
        assertTrue(BytemanFaultRuleFactory.getManipulateReturnObjectRule(faultSpec).contains("return returnValue"));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.faults.plugin.helpers.byteman.BytemanFaultRuleFactory#getTraceObjectRule(com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec)}.
     */
    @Test
    public void testGetTraceObjectRule() {
        CommandExecutionFaultSpec faultSpec = getCommandExecutionFaultSpec();
        faultSpec.setFaultType(BytemanFaultType.TRACE_OBJECT.name());
        assertTrue(BytemanFaultRuleFactory.getTraceObjectRule(faultSpec).contains("System.out.println(objectValue)"));
    }

    private CommandExecutionFaultSpec getCommandExecutionFaultSpec() {
        CommandExecutionFaultSpec faultSpec = new CommandExecutionFaultSpec();
        Map<String, String> args = new HashMap<>();
        args.put(TASK_ID, UUID.randomUUID().toString());
        args.put(CLASS_NAME, "com.vmware.mangle.Test");
        args.put(METHOD_NAME, "test()");
        args.put(RULE_EVENT, AgentRuleConstants.AT_ENTRY_STRING.toString());
        faultSpec.setArgs(args);
        return faultSpec;
    }

}
