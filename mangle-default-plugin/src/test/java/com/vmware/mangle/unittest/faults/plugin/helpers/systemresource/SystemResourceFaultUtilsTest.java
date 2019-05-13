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

package com.vmware.mangle.unittest.faults.plugin.helpers.systemresource;


import static org.testng.Assert.assertEquals;

import static com.vmware.mangle.utils.constants.FaultConstants.DEFAULT_TEMP_DIR;
import static com.vmware.mangle.utils.constants.FaultConstants.FAULT_NAME_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.FORWARD_SLASH;
import static com.vmware.mangle.utils.constants.FaultConstants.IO_SIZE_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.KILL_PROCESS_REMEDIATION_COMMAND_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.LOAD_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.PROCESS_IDENTIFIER_UNDERSCORE;
import static com.vmware.mangle.utils.constants.FaultConstants.TARGET_DIRECTORY_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.TIMEOUT_IN_MILLI_SEC_ARG;

import java.util.HashMap;
import java.util.Map;

import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.faults.plugin.helpers.systemresource.SystemResourceFaultUtils;
import com.vmware.mangle.services.enums.FaultName;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Test Class for SystemResourceFaultUtils
 *
 * @author jayasankarr
 *
 */
public class SystemResourceFaultUtilsTest {
    private SystemResourceFaultUtils systemResourceFaultUtils;
    private String scriptBasePath = DEFAULT_TEMP_DIR + FORWARD_SLASH;
    private String load = "50";
    private String timeoutInMilliSec = "100";

    @BeforeClass
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        systemResourceFaultUtils = new SystemResourceFaultUtils();
    }

    @Test
    public void testBuildInjectionCommandForCpuFault() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.CPUFAULT.getValue());
        args.put(LOAD_ARG, load);
        args.put(TIMEOUT_IN_MILLI_SEC_ARG, timeoutInMilliSec);
        String command = systemResourceFaultUtils.buildInjectionCommand(args, scriptBasePath);
        Assert.assertEquals(command, String.format("%s/cpuburn.sh --operation=inject --load=%s --timeout=%s",
                DEFAULT_TEMP_DIR, load, timeoutInMilliSec, DEFAULT_TEMP_DIR));


    }

    @Test
    public void testBuildInjectionCommandForMemoryFault() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.MEMORYFAULT.getValue());
        args.put(LOAD_ARG, load);
        args.put(TIMEOUT_IN_MILLI_SEC_ARG, timeoutInMilliSec);
        String command = systemResourceFaultUtils.buildInjectionCommand(args, scriptBasePath);
        Assert.assertEquals(command, String.format("%s/memoryspike.sh --operation=inject --load=%s --timeout=%s",
                DEFAULT_TEMP_DIR, load, timeoutInMilliSec, DEFAULT_TEMP_DIR));

    }

    @Test(expectedExceptions = { MangleRuntimeException.class, IllegalArgumentException.class })
    public void negativeTestBuildInjectionCommandForUnsupportedFault() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, "dummy");
        try {
            systemResourceFaultUtils.buildInjectionCommand(args, scriptBasePath);
        } catch (MangleRuntimeException e) {
            assertEquals(e.getErrorCode(), ErrorCode.UNSUPPORTED_FAULT);
            throw e;
        }

    }

    @Test(expectedExceptions = { MangleRuntimeException.class, IllegalArgumentException.class })
    public void negativeTestBuildInjectionCommandForFaultNameNull() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, " ");
        try {
            systemResourceFaultUtils.buildInjectionCommand(args, scriptBasePath);
        } catch (MangleRuntimeException e) {
            assertEquals(e.getErrorCode(), ErrorCode.FAULT_NAME_NOT_NULL);
            throw e;
        }
    }

    @Test
    public void testBuildInjectionCommandDiskIOFault() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.DISKFAULT.getValue());
        args.put(IO_SIZE_ARG, "1");
        args.put(TARGET_DIRECTORY_ARG, DEFAULT_TEMP_DIR);
        args.put(TIMEOUT_IN_MILLI_SEC_ARG, timeoutInMilliSec);
        String command = systemResourceFaultUtils.buildInjectionCommand(args, scriptBasePath);
        Assert.assertEquals(command,
                String.format("%s/ioburn.sh --operation=inject --targetDir=%s --blockSize=1 --timeout=%s",
                        DEFAULT_TEMP_DIR, DEFAULT_TEMP_DIR, timeoutInMilliSec, DEFAULT_TEMP_DIR));
    }

    @Test
    public void testBuildRemediationCommandForCpuFault() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.CPUFAULT.getValue());
        args.put(LOAD_ARG, load);
        args.put(TIMEOUT_IN_MILLI_SEC_ARG, timeoutInMilliSec);
        String command = systemResourceFaultUtils.buildRemediationCommand(args, scriptBasePath);
        Assert.assertEquals(command, "/tmp/cpuburn.sh --operation=remediate");

    }

    @Test
    public void testBuildRemediationCommandForMemoryFault() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.MEMORYFAULT.getValue());
        args.put(LOAD_ARG, load);
        args.put(TIMEOUT_IN_MILLI_SEC_ARG, timeoutInMilliSec);
        String command = systemResourceFaultUtils.buildRemediationCommand(args, scriptBasePath);
        Assert.assertEquals(command, "/tmp/memoryspike.sh --operation=remediate");

    }

    @Test
    public void testBuildRemediationCommandForDiskIOFault() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.DISKFAULT.getValue());
        String command = systemResourceFaultUtils.buildRemediationCommand(args, scriptBasePath);
        Assert.assertEquals(command, "/tmp/ioburn.sh --operation=remediate");
    }

    @Test(expectedExceptions = { MangleRuntimeException.class, IllegalArgumentException.class })
    public void negativeTestBuildRemediationCommandForUnsupportedFault() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, "dummy");
        try {
            systemResourceFaultUtils.buildRemediationCommand(args, scriptBasePath);
        } catch (MangleRuntimeException e) {
            assertEquals(e.getErrorCode(), ErrorCode.UNSUPPORTED_FAULT);
            throw e;
        }
    }

    @Test(expectedExceptions = { MangleRuntimeException.class, IllegalArgumentException.class })
    public void negativeTestBuildRemediationCommandForFaultNameNull() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, " ");
        try {
            systemResourceFaultUtils.buildRemediationCommand(args, scriptBasePath);
        } catch (MangleRuntimeException e) {
            assertEquals(e.getErrorCode(), ErrorCode.FAULT_NAME_NOT_NULL);
            throw e;
        }
    }

    @Test
    public void testGetFaultName() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.DISKFAULT.getValue());
        FaultName actualResult = systemResourceFaultUtils.getFaultName(args);
        Assert.assertEquals(actualResult, FaultName.DISKFAULT);
    }

    @Test
    public void testBuildInjectionCommandKillProcessFault() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.KILLPROCESSFAULT.getValue());
        args.put(PROCESS_IDENTIFIER_UNDERSCORE, "test");
        String command = systemResourceFaultUtils.buildInjectionCommand(args, scriptBasePath);
        Assert.assertEquals(command, "/tmp/killprocess.sh --operation=inject --processIdentifier=\"test\"");
    }

    @Test
    public void testBuildRemediationCommandForKillProcessFault() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.KILLPROCESSFAULT.getValue());
        args.put(KILL_PROCESS_REMEDIATION_COMMAND_ARG, "start test");
        String command = systemResourceFaultUtils.buildRemediationCommand(args, scriptBasePath);
        Assert.assertEquals(command, "/tmp/killprocess.sh --operation=remediate --remediationCommand=\"start test\"");
    }
}
