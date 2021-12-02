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

import static com.vmware.mangle.utils.constants.FaultConstants.CLOCK_TYPE_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.DAYS_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.DB_LATENCY_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.DB_NAME_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.DB_PASSWORD_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.DB_PERCENTAGE_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.DB_PORT_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.DB_SSL_ENABLED_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.DB_TABLE_NAME_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.DB_TYPE_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.DB_USER_NAME_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.DEFAULT_TEMP_DIR;
import static com.vmware.mangle.utils.constants.FaultConstants.DIRECTORY_PATH_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.FAULT_NAME_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.FAULT_OPERATION_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.FORWARD_SLASH;
import static com.vmware.mangle.utils.constants.FaultConstants.HOSTS_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.HOURS_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.IO_SIZE_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.JITTER_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.KILL_PROCESS_REMEDIATION_COMMAND_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.LATENCY_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.LOAD_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.MINUTES_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.NIC_NAME_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.PROCESS_IDENTIFIER_UNDERSCORE;
import static com.vmware.mangle.utils.constants.FaultConstants.PROCESS_ID_UNDERSCORE;
import static com.vmware.mangle.utils.constants.FaultConstants.SECONDS_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.TARGET_DIRECTORY_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.TIMEOUT_IN_MILLI_SEC_ARG;

import java.util.HashMap;
import java.util.Map;

import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CpuFaultSpec;
import com.vmware.mangle.faults.plugin.helpers.systemresource.SystemResourceFaultUtils;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.model.enums.DatabaseType;
import com.vmware.mangle.services.enums.FaultName;
import com.vmware.mangle.services.enums.NetworkFaultType;
import com.vmware.mangle.utils.constants.FaultConstants;
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
    private FaultsMockData faultsMockData;
    private CpuFaultSpec remoteMachineCpuFaultSpec;

    @BeforeClass
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        systemResourceFaultUtils = new SystemResourceFaultUtils();
        faultsMockData = new FaultsMockData();
        remoteMachineCpuFaultSpec = faultsMockData.getLinuxCpuJvmAgentFaultSpec();
        remoteMachineCpuFaultSpec.setJvmProperties(null);
    }

    @Test
    public void testBuildInjectionCommandForCpuFault() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.CPUFAULT.getValue());
        args.put(LOAD_ARG, load);
        args.put(TIMEOUT_IN_MILLI_SEC_ARG, timeoutInMilliSec);
        String command = systemResourceFaultUtils.buildInjectionCommand(args, scriptBasePath);
        Assert.assertEquals(command,
                String.format("infra_submit  --operation inject --faultname %s --load %s --timeout %s",
                        FaultName.CPUFAULT.getValue(), load, timeoutInMilliSec));


    }

    @Test
    public void testBuildInjectionCommandForMemoryFault() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.MEMORYFAULT.getValue());
        args.put(LOAD_ARG, load);
        args.put(TIMEOUT_IN_MILLI_SEC_ARG, timeoutInMilliSec);
        String command = systemResourceFaultUtils.buildInjectionCommand(args, scriptBasePath);
        Assert.assertEquals(command,
                String.format("infra_submit  --operation inject --faultname %s --load %s --timeout %s",
                        FaultName.MEMORYFAULT.getValue(), load, timeoutInMilliSec));

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
                String.format(
                        "infra_submit  --operation inject --faultname %s --targetDir %s --blockSize 1 --timeout %s",
                        FaultName.DISKFAULT.getValue(), DEFAULT_TEMP_DIR, timeoutInMilliSec));
    }

    @Test
    public void testBuildInjectionCommandNetworkFault() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.NETWORKFAULT.getValue());
        args.put(FAULT_OPERATION_ARG, NetworkFaultType.NETWORK_DELAY_MILLISECONDS.networkFaultType());
        args.put(LATENCY_ARG, "1000");
        args.put(NIC_NAME_ARG, "eth0");
        args.put(TIMEOUT_IN_MILLI_SEC_ARG, timeoutInMilliSec);
        args.put(JITTER_ARG, "10");
        String command = systemResourceFaultUtils.buildInjectionCommand(args, scriptBasePath);
        Assert.assertEquals(command, String.format(
                "infra_submit  --operation inject --faultname %s --faultOperation %s --latency %s --percentage null --nicName %s --timeout %s --jitter %s",
                FaultName.NETWORKFAULT.getValue(), NetworkFaultType.NETWORK_DELAY_MILLISECONDS.networkFaultType(), 1000,
                "eth0", timeoutInMilliSec,10));
    }

    @Test
    public void testBuildRemediationCommandForCpuFault() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.CPUFAULT.getValue());
        args.put(LOAD_ARG, load);
        args.put(TIMEOUT_IN_MILLI_SEC_ARG, timeoutInMilliSec);
        String command = systemResourceFaultUtils.buildRemediationCommand(args, scriptBasePath);
        Assert.assertEquals(command, "infra_submit  --operation remediate ");

    }

    @Test
    public void testBuildRemediationCommandForMemoryFault() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.MEMORYFAULT.getValue());
        args.put(LOAD_ARG, load);
        args.put(TIMEOUT_IN_MILLI_SEC_ARG, timeoutInMilliSec);
        String command = systemResourceFaultUtils.buildRemediationCommand(args, scriptBasePath);
        Assert.assertEquals(command, "infra_submit  --operation remediate ");

    }

    @Test
    public void testBuildRemediationCommandForDiskIOFault() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.DISKFAULT.getValue());
        String command = systemResourceFaultUtils.buildRemediationCommand(args, scriptBasePath);
        Assert.assertEquals(command, "infra_submit  --operation remediate ");
    }

    @Test
    public void testBuildRemediationCommandForNetworkFault() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.NETWORKFAULT.getValue());
        String command = systemResourceFaultUtils.buildRemediationCommand(args, scriptBasePath);
        Assert.assertEquals(command, "infra_submit  --operation remediate ");
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
        args.put(PROCESS_ID_UNDERSCORE, "12345");
        String command = systemResourceFaultUtils.buildInjectionCommand(args, scriptBasePath);
        Assert.assertEquals(command,
                String.format(
                        "infra_submit  --operation inject --faultname %s --processIdentifier \"test\" --killAll null --processId \"12345\"",
                        FaultName.KILLPROCESSFAULT.getValue()));
    }

    @Test
    public void testBuildRemediationCommandForKillProcessFault() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.KILLPROCESSFAULT.getValue());
        args.put(KILL_PROCESS_REMEDIATION_COMMAND_ARG, "start test");
        String command = systemResourceFaultUtils.buildRemediationCommand(args, scriptBasePath);
        Assert.assertEquals(command, "infra_submit  --operation remediate  --remediationCommand \"start test\"");
    }

    @Test
    public void testBuildInjectionCommandDiskSpaceFault() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.DISKSPACEFAULT.getValue());
        args.put(DIRECTORY_PATH_ARG, "/test");
        args.put(TIMEOUT_IN_MILLI_SEC_ARG, timeoutInMilliSec);
        args.put(FaultConstants.DISK_FILL_SIZE_ARG, String.valueOf(50));
        String command = systemResourceFaultUtils.buildInjectionCommand(args, scriptBasePath);
        Assert.assertEquals(command,
                String.format(
                        "infra_submit  --operation inject --faultname %s --directoryPath /test --timeout 100 --diskFillSize 50",
                        FaultName.DISKSPACEFAULT.getValue()));
    }

    @Test
    public void testBuildRemediationCommandForDiskSpaceFault() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.DISKSPACEFAULT.getValue());
        args.put(DIRECTORY_PATH_ARG, "/test");
        String command = systemResourceFaultUtils.buildRemediationCommand(args, scriptBasePath);
        Assert.assertEquals(command, "infra_submit  --operation remediate  --directoryPath /test");
    }

    @Test
    public void testBuildInjectionCommandKernelPanicFault() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.KERNELPANICFAULT.getValue());
        String command = systemResourceFaultUtils.buildInjectionCommand(args, scriptBasePath);
        Assert.assertEquals(command, "infra_submit  --operation inject --faultname kernelPanicFault");
    }

    @Test
    public void testBuildInjectionCommandForDbConnectionLeak() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.DBCONNECTIONLEAKFAULT_POSTGRES.getValue());
        args.put(DB_NAME_ARG, "test");
        args.put(DB_USER_NAME_ARG, "abc");
        args.put(DB_PASSWORD_ARG, "xyz");
        args.put(DB_PORT_ARG, "5432");
        args.put(DB_SSL_ENABLED_ARG, "false");
        args.put(DB_TYPE_ARG, DatabaseType.POSTGRES.toString());
        args.put(TIMEOUT_IN_MILLI_SEC_ARG, timeoutInMilliSec);
        args.put(FaultConstants.DISK_FILL_SIZE_ARG, String.valueOf(50));
        String command = systemResourceFaultUtils.buildInjectionCommand(args, scriptBasePath);
        Assert.assertEquals(command,
                String.format(
                        "infra_submit  --operation inject --faultname %s --dbName test --userName abc --password xyz --port 5432 --sslEnabled false --timeout 100",
                        FaultName.DBCONNECTIONLEAKFAULT_POSTGRES.getValue()));
    }

    @Test
    public void testBuildRemediationCommandForDbConnectionLeak() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.DBCONNECTIONLEAKFAULT_POSTGRES.getValue());
        args.put(DB_NAME_ARG, "test");
        args.put(DB_USER_NAME_ARG, "admin");
        args.put(DB_PORT_ARG, "5432");
        args.put(DB_SSL_ENABLED_ARG, "false");
        args.put(DB_TYPE_ARG, DatabaseType.POSTGRES.toString());
        String command = systemResourceFaultUtils.buildRemediationCommand(args, scriptBasePath);
        Assert.assertEquals(command,
                "infra_submit  --operation remediate --dbName test --userName admin --sslEnabled false --port 5432");
    }

    @Test
    public void testBuildInjectionCommandForDbTransactionError() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.DBTRANSACTIONERRORFAULT_POSTGRES.getValue());
        args.put(DB_NAME_ARG, "test");
        args.put(DB_USER_NAME_ARG, "abc");
        args.put(DB_PASSWORD_ARG, "xyz");
        args.put(DB_PORT_ARG, "5432");
        args.put(DB_SSL_ENABLED_ARG, "false");
        args.put(DB_TYPE_ARG, DatabaseType.POSTGRES.toString());
        args.put(FaultConstants.DB_TABLE_NAME_ARG, "emp");
        args.put(FaultConstants.DB_ERROR_CODE_ARG, "25000");
        args.put(FaultConstants.DB_PERCENTAGE_ARG, "60");
        args.put(TIMEOUT_IN_MILLI_SEC_ARG, timeoutInMilliSec);
        args.put(FaultConstants.DISK_FILL_SIZE_ARG, String.valueOf(50));
        String command = systemResourceFaultUtils.buildInjectionCommand(args, scriptBasePath);
        Assert.assertEquals(command,
                String.format(
                        "infra_submit  --operation inject --faultname %s --dbName test --userName abc --password xyz --port 5432 --tableName emp --errorCode 25000 --percentage 60 --sslEnabled false --timeout 100",
                        FaultName.DBTRANSACTIONERRORFAULT_POSTGRES.getValue()));
    }

    @Test
    public void testBuildRemediationCommandForDbTransactionError() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.DBTRANSACTIONERRORFAULT_POSTGRES.getValue());
        args.put(DB_NAME_ARG, "test");
        args.put(DB_USER_NAME_ARG, "admin");
        args.put(DB_PORT_ARG, "5432");
        args.put(DB_PASSWORD_ARG, "xyz");
        args.put(DB_SSL_ENABLED_ARG, "false");
        args.put(DB_TYPE_ARG, DatabaseType.POSTGRES.toString());
        args.put(FaultConstants.DB_TABLE_NAME_ARG, "emp");
        String command = systemResourceFaultUtils.buildRemediationCommand(args, scriptBasePath);
        Assert.assertEquals(command,
                "infra_submit  --operation remediate --dbName test --userName admin --password xyz --port 5432 --sslEnabled false --tableName emp");
    }

    @Test
    public void testBuildInjectionCommandClockSkewFault() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.CLOCKSKEWFAULT.getValue());
        args.put(SECONDS_ARG, "1");
        args.put(MINUTES_ARG, "1");
        args.put(HOURS_ARG, "1");
        args.put(DAYS_ARG, "1");
        args.put(CLOCK_TYPE_ARG, "FUTURE");
        args.put(TIMEOUT_IN_MILLI_SEC_ARG, "5000");
        String command = systemResourceFaultUtils.buildInjectionCommand(args, scriptBasePath);
        Assert.assertEquals(command,
                "infra_submit  --operation inject --faultname clockSkewFault --seconds 1 --minutes 1 --hours 1 --days 1 --type FUTURE --timeout 5000");
    }

    @Test
    public void testBuildRemediateClockSkewFault() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.CLOCKSKEWFAULT.getValue());
        String command = systemResourceFaultUtils.buildRemediationCommand(args, scriptBasePath);
        Assert.assertEquals(command, "infra_submit  --operation remediate ");
    }

    @Test
    public void testBuildInjectionCommandForDbTransactionLatency() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.DBTRANSACTIONLATENCYFAULT_POSTGRES.getValue());
        args.put(DB_NAME_ARG, "test");
        args.put(DB_USER_NAME_ARG, "vmware");
        args.put(DB_PASSWORD_ARG, "test");
        args.put(DB_PORT_ARG, "5432");
        args.put(DB_TYPE_ARG, DatabaseType.POSTGRES.toString());
        args.put(DB_TABLE_NAME_ARG, "emp");
        args.put(DB_LATENCY_ARG, "1000");
        args.put(DB_PERCENTAGE_ARG, "60");
        args.put(DB_SSL_ENABLED_ARG, "false");
        args.put(TIMEOUT_IN_MILLI_SEC_ARG, timeoutInMilliSec);
        args.put(FaultConstants.DISK_FILL_SIZE_ARG, String.valueOf(50));
        String command = systemResourceFaultUtils.buildInjectionCommand(args, scriptBasePath);
        Assert.assertEquals(command,
                String.format(
                        "infra_submit  --operation inject --faultname %s --dbName test --userName vmware --password test --port 5432 --tableName emp --latency 1000 --percentage 60 --sslEnabled false --timeout 100",
                        FaultName.DBTRANSACTIONLATENCYFAULT_POSTGRES.getValue()));
    }

    @Test
    public void testBuildRemediationCommandForDbTransactionLatency() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.DBTRANSACTIONLATENCYFAULT_POSTGRES.getValue());
        args.put(DB_NAME_ARG, "test1");
        args.put(DB_USER_NAME_ARG, "admin1");
        args.put(DB_PORT_ARG, "5432");
        args.put(DB_PASSWORD_ARG, "xyz1");
        args.put(DB_SSL_ENABLED_ARG, "false");
        args.put(DB_TYPE_ARG, DatabaseType.POSTGRES.toString());
        args.put(FaultConstants.DB_TABLE_NAME_ARG, "emp");
        String command = systemResourceFaultUtils.buildRemediationCommand(args, scriptBasePath);
        Assert.assertEquals(command,
                "infra_submit  --operation remediate --dbName test1 --userName admin1 --password xyz1 --port 5432 --sslEnabled false --tableName emp");
    }

    @Test
    public void testBuildInjectionCommandForMongoDbConnectionLeak() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.DBCONNECTIONLEAKFAULT_MONGODB.getValue());
        args.put(DB_NAME_ARG, "test");
        args.put(DB_USER_NAME_ARG, "abc");
        args.put(DB_PASSWORD_ARG, "xyz");
        args.put(DB_PORT_ARG, "5432");
        args.put(DB_SSL_ENABLED_ARG, "false");
        args.put(DB_TYPE_ARG, DatabaseType.MONGODB.toString());
        args.put(TIMEOUT_IN_MILLI_SEC_ARG, timeoutInMilliSec);
        args.put(FaultConstants.DISK_FILL_SIZE_ARG, String.valueOf(50));
        String command = systemResourceFaultUtils.buildInjectionCommand(args, scriptBasePath);
        Assert.assertEquals(command,
                String.format(
                        "infra_submit  --operation inject --faultname %s --dbName test --userName abc --password xyz --port 5432 --sslEnabled false --timeout 100",
                        FaultName.DBCONNECTIONLEAKFAULT_MONGODB.getValue()));
    }

    @Test
    public void testBuildRemediationCommandForMongoDbConnectionLeak() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.DBCONNECTIONLEAKFAULT_MONGODB.getValue());
        args.put(DB_NAME_ARG, "test");
        args.put(DB_USER_NAME_ARG, "admin");
        args.put(DB_PORT_ARG, "5432");
        args.put(DB_SSL_ENABLED_ARG, "false");
        args.put(DB_TYPE_ARG, DatabaseType.MONGODB.toString());
        String command = systemResourceFaultUtils.buildRemediationCommand(args, scriptBasePath);
        Assert.assertEquals(command,
                "infra_submit  --operation remediate --dbName test --userName admin --sslEnabled false --port 5432");
    }

    @Test
    public void testBuildInjectionCommandForCassandraDbConnectionLeak() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.DBCONNECTIONLEAKFAULT_CASSANDRA.getValue());
        args.put(DB_NAME_ARG, "test");
        args.put(DB_USER_NAME_ARG, "abc");
        args.put(DB_PASSWORD_ARG, "xyz");
        args.put(DB_PORT_ARG, "9042");
        args.put(DB_SSL_ENABLED_ARG, "false");
        args.put(DB_TYPE_ARG, DatabaseType.CASSANDRA.toString());
        args.put(TIMEOUT_IN_MILLI_SEC_ARG, timeoutInMilliSec);
        String command = systemResourceFaultUtils.buildInjectionCommand(args, scriptBasePath);
        Assert.assertEquals(command,
                String.format(
                        "infra_submit  --operation inject --faultname %s --dbName test --userName abc --password xyz --port 9042 --sslEnabled false --timeout 100",
                        FaultName.DBCONNECTIONLEAKFAULT_CASSANDRA.getValue()));
    }

    @Test
    public void testBuildRemediationCommandForCassandraDbConnectionLeak() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.DBCONNECTIONLEAKFAULT_CASSANDRA.getValue());
        args.put(DB_NAME_ARG, "test");
        args.put(DB_USER_NAME_ARG, "admin");
        args.put(DB_PORT_ARG, "9042");
        args.put(DB_SSL_ENABLED_ARG, "false");
        args.put(DB_TYPE_ARG, DatabaseType.CASSANDRA.toString());
        String command = systemResourceFaultUtils.buildRemediationCommand(args, scriptBasePath);
        Assert.assertEquals(command,
                "infra_submit  --operation remediate --dbName test --userName admin --sslEnabled false --port 9042");
    }

    @Test
    public void testBuildInjectionCommandForNetworkPartition() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.NETWORKPARTITIONFAULT.getValue());
        args.put(HOSTS_ARG, "10.2.3.4;10.2.3.5");
        args.put(TIMEOUT_IN_MILLI_SEC_ARG, timeoutInMilliSec);
        String command = systemResourceFaultUtils.buildInjectionCommand(args, scriptBasePath);
        Assert.assertEquals(command,
                String.format("infra_submit  --operation inject --faultname %s --hosts 10.2.3.4,10.2.3.5 --timeout 100",
                        FaultName.NETWORKPARTITIONFAULT.getValue()));
    }

    @Test
    public void testBuildRemediationCommandForNetworkPartition() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.NETWORKPARTITIONFAULT.getValue());
        args.put(HOSTS_ARG, "10.2.3.4;10.2.3.5");
        String command = systemResourceFaultUtils.buildRemediationCommand(args, scriptBasePath);
        Assert.assertEquals(command, "infra_submit  --operation remediate --hosts 10.2.3.4,10.2.3.5");
    }

    @Test
    public void testBuildStatusCommand() {
        Map<String, String> args = new HashMap<>();
        args.put(FAULT_NAME_ARG, FaultName.NETWORKPARTITIONFAULT.getValue());
        args.put(HOSTS_ARG, "");
        String command = systemResourceFaultUtils.buildStatusCommand(args);
        Assert.assertEquals(command, "infra_submit  --operation status ");
    }
}