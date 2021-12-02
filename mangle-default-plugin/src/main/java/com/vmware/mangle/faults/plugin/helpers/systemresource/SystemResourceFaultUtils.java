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

package com.vmware.mangle.faults.plugin.helpers.systemresource;

import static com.vmware.mangle.utils.constants.FaultConstants.AGENT_PORT;
import static com.vmware.mangle.utils.constants.FaultConstants.BLOCK_SIZE_SCRIPT_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.CLOCK_SKEW_INJECTION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.CLOCK_SKEW_INJECTION_COMMAND_WITH_ARGS_AND_PORT;
import static com.vmware.mangle.utils.constants.FaultConstants.CLOCK_TYPE;
import static com.vmware.mangle.utils.constants.FaultConstants.CLOCK_TYPE_SCRIPT_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.CPU_INJECTION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.CPU_INJECTION_COMMAND_WITH_ARGS_AND_PORT;
import static com.vmware.mangle.utils.constants.FaultConstants.DAYS;
import static com.vmware.mangle.utils.constants.FaultConstants.DAYS_SCRIPT_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.DIRECTORY_PATH;
import static com.vmware.mangle.utils.constants.FaultConstants.DIRECTORY_PATH_SCRIPT_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.DISK_FILL_SIZE;
import static com.vmware.mangle.utils.constants.FaultConstants.DISK_FILL_SIZE_SCRIPT_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.DISK_IO_INJECTION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.DISK_IO_INJECTION_COMMAND_WITH_ARGS_AND_PORT;
import static com.vmware.mangle.utils.constants.FaultConstants.DISK_SPACE_INJECTION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.DISK_SPACE_REMEDIATION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.EXTRACT_AGENT_COMMAND;
import static com.vmware.mangle.utils.constants.FaultConstants.FAULT_NAME;
import static com.vmware.mangle.utils.constants.FaultConstants.FAULT_OPERATION;
import static com.vmware.mangle.utils.constants.FaultConstants.FAULT_OPERATION_SCRIPT_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.FILEHANDLER_INJECTION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.FORWARD_SLASH;
import static com.vmware.mangle.utils.constants.FaultConstants.HOSTS_KEY;
import static com.vmware.mangle.utils.constants.FaultConstants.HOURS;
import static com.vmware.mangle.utils.constants.FaultConstants.HOURS_SCRIPT_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.INFRA_AGENT_NAME;
import static com.vmware.mangle.utils.constants.FaultConstants.INFRA_SUBMIT;
import static com.vmware.mangle.utils.constants.FaultConstants.IO_SIZE;
import static com.vmware.mangle.utils.constants.FaultConstants.JITTER;
import static com.vmware.mangle.utils.constants.FaultConstants.JITTER_SCRIPT_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.KERNELPANIC_INJECTION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.KILL_ALL;
import static com.vmware.mangle.utils.constants.FaultConstants.KILL_PROCESS_REMEDIATION_COMMAND;
import static com.vmware.mangle.utils.constants.FaultConstants.KILL_SERVICE_INJECTION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.LATENCY;
import static com.vmware.mangle.utils.constants.FaultConstants.LATENCY_SCRIPT_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.LOAD;
import static com.vmware.mangle.utils.constants.FaultConstants.LOAD_SCRIPT_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.MEMORY_INJECTION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.MEMORY_INJECTION_COMMAND_WITH_ARGS_AND_PORT;
import static com.vmware.mangle.utils.constants.FaultConstants.MINUTES;
import static com.vmware.mangle.utils.constants.FaultConstants.MINUTES_SCRIPT_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.NETWORK_FAULT_INJECTION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.NETWORK_PARTITION_INJECTION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.NETWORK_PARTITION_REMEDIATION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.NIC_NAME;
import static com.vmware.mangle.utils.constants.FaultConstants.NIC_NAME_SCRIPT_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.OPERATION_REMEDIATE;
import static com.vmware.mangle.utils.constants.FaultConstants.OPERATION_STATUS;
import static com.vmware.mangle.utils.constants.FaultConstants.PERCENTAGE;
import static com.vmware.mangle.utils.constants.FaultConstants.PERCENTAGE_SCRIPT_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.PORT_SCRIPT_ARGUEMENT;
import static com.vmware.mangle.utils.constants.FaultConstants.PROCESS_ID;
import static com.vmware.mangle.utils.constants.FaultConstants.PROCESS_IDENTIFIER;
import static com.vmware.mangle.utils.constants.FaultConstants.SECONDS;
import static com.vmware.mangle.utils.constants.FaultConstants.SECONDS_SCRIPT_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.SERVICE_NAME;
import static com.vmware.mangle.utils.constants.FaultConstants.STOP_SERVICE_INJECTION_COMMAND_WITH_ARGS1;
import static com.vmware.mangle.utils.constants.FaultConstants.STOP_SERVICE_REMEDIATION_COMMAND_WITH_ARGS1;
import static com.vmware.mangle.utils.constants.FaultConstants.TARGET_DIRECTORY;
import static com.vmware.mangle.utils.constants.FaultConstants.TARGET_DIRECTORY_SCRIPT_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.TIMEOUT_IN_MILLI_SEC;
import static com.vmware.mangle.utils.constants.FaultConstants.TIMEOUT_SCRIPT_ARG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.faults.plugin.helpers.FaultConstants;
import com.vmware.mangle.faults.plugin.helpers.KnownFailuresHelper;
import com.vmware.mangle.services.enums.FaultName;
import com.vmware.mangle.task.framework.helpers.faults.FaultsHelper;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author bkaranam,jayasankarr
 *
 */
@Component
@Log4j2
public class SystemResourceFaultUtils {

    private List<String> faultNames;

    public SystemResourceFaultUtils() {
        this.faultNames = getFaultNamesWithNoRemediationSupport();
    }

    public String buildInjectionCommand(Map<String, String> args, String scriptBasePath) {
        Map<String, String> faultArgs = FaultsHelper.parseArgs(CommonUtils.convertMaptoDelimitedString(args, ","));
        String command = "";
        String faultName = faultArgs.get(FAULT_NAME);
        if (faultName == null || faultName.isEmpty()) {
            throw new MangleRuntimeException(ErrorCode.FAULT_NAME_NOT_NULL);
        }
        scriptBasePath = scriptBasePath.endsWith(FORWARD_SLASH) ? scriptBasePath : scriptBasePath + FORWARD_SLASH;
        try {
            switch (FaultName.valueOf(faultName.toUpperCase())) {
            case CPUFAULT:
                command = getCpuInjectionCommand(faultArgs);
                break;
            case MEMORYFAULT:
                command = getMemoryInjectionCommand(faultArgs);
                break;
            case DISKFAULT:
                command = getdiskIOInjectionCommand(faultArgs);
                break;
            case KILLPROCESSFAULT:
                command = getKillServiceInjectionCommand(faultArgs);
                break;
            case STOPSERVICEFAULT:
                command = getStopServiceInjectionCommand(faultArgs);
                break;
            case NETWORKFAULT:
                command = getNetworkFaultInjectionCommand(faultArgs);
                break;
            case FILEHANDLERFAULT:
                command = getFilehandlerFaultnjectionCommand(faultArgs);
                break;
            case DISKSPACEFAULT:
                command = getDiskSpaceInjectionCommand(faultArgs);
                break;
            case KERNELPANICFAULT:
                command = getKernelPanicInjectionCommand(faultArgs);
                break;
            case CLOCKSKEWFAULT:
                command = getClockSkewInjectionCommand(faultArgs);
                break;
            case DBCONNECTIONLEAKFAULT_POSTGRES:
                command = DbFaultUtils2.getDbConnectionLeakInjectionCommand(faultArgs);
                break;
            case DBTRANSACTIONERRORFAULT_POSTGRES:
                command = DbFaultUtils2.getDbTransactionErrorInjectionCommand(faultArgs);
                break;
            case DBTRANSACTIONLATENCYFAULT_POSTGRES:
                command = DbFaultUtils2.getDbTransactionLatencyInjectionCommand(faultArgs);
                break;
            case DBCONNECTIONLEAKFAULT_MONGODB:
                command = DbFaultUtils2.getDbConnectionLeakInjectionCommand(faultArgs);
                break;
            case DBCONNECTIONLEAKFAULT_CASSANDRA:
                command = DbFaultUtils2.getDbConnectionLeakInjectionCommand(faultArgs);
                break;
            case NETWORKPARTITIONFAULT:
                command = getNetworkPartitionInjectionCommand(faultArgs);
                break;
            default:
                command = "";
            }
        } catch (Exception e) {
            throw new MangleRuntimeException(ErrorCode.UNSUPPORTED_FAULT);
        }
        return command;
    }

    private String getClockSkewInjectionCommand(Map<String, String> faultArgs) {
        if (faultArgs.containsKey(AGENT_PORT)) {
            return new StringBuilder()
                    .append(String.format(CLOCK_SKEW_INJECTION_COMMAND_WITH_ARGS_AND_PORT,
                            FaultName.CLOCKSKEWFAULT.getValue(), SECONDS_SCRIPT_ARG, faultArgs.get(SECONDS),
                            MINUTES_SCRIPT_ARG, faultArgs.get(MINUTES), HOURS_SCRIPT_ARG, faultArgs.get(HOURS),
                            DAYS_SCRIPT_ARG, faultArgs.get(DAYS), CLOCK_TYPE_SCRIPT_ARG, faultArgs.get(CLOCK_TYPE),
                            TIMEOUT_SCRIPT_ARG, faultArgs.get(TIMEOUT_IN_MILLI_SEC)))
                    .toString();
        } else {
            return new StringBuilder().append(String.format(CLOCK_SKEW_INJECTION_COMMAND_WITH_ARGS,
                    FaultName.CLOCKSKEWFAULT.getValue(), SECONDS_SCRIPT_ARG, faultArgs.get(SECONDS), MINUTES_SCRIPT_ARG,
                    faultArgs.get(MINUTES), HOURS_SCRIPT_ARG, faultArgs.get(HOURS), DAYS_SCRIPT_ARG,
                    faultArgs.get(DAYS), CLOCK_TYPE_SCRIPT_ARG, faultArgs.get(CLOCK_TYPE), TIMEOUT_SCRIPT_ARG,
                    faultArgs.get(TIMEOUT_IN_MILLI_SEC))).toString();
        }
    }

    private String getMemoryInjectionCommand(Map<String, String> faultArgs) {
        if (faultArgs.containsKey(AGENT_PORT)) {
            return new StringBuilder().append(String.format(MEMORY_INJECTION_COMMAND_WITH_ARGS_AND_PORT,
                    FaultName.MEMORYFAULT.getValue(), LOAD_SCRIPT_ARG, faultArgs.get(LOAD), TIMEOUT_SCRIPT_ARG,
                    faultArgs.get(TIMEOUT_IN_MILLI_SEC), PORT_SCRIPT_ARGUEMENT, faultArgs.get(AGENT_PORT))).toString();
        } else {
            return new StringBuilder().append(
                    String.format(MEMORY_INJECTION_COMMAND_WITH_ARGS, FaultName.MEMORYFAULT.getValue(), LOAD_SCRIPT_ARG,
                            faultArgs.get(LOAD), TIMEOUT_SCRIPT_ARG, faultArgs.get(TIMEOUT_IN_MILLI_SEC)))
                    .toString();
        }
    }

    private String getdiskIOInjectionCommand(Map<String, String> faultArgs) {
        if (faultArgs.containsKey(AGENT_PORT)) {
            return new StringBuilder().append(String.format(DISK_IO_INJECTION_COMMAND_WITH_ARGS_AND_PORT,
                    FaultName.DISKFAULT.getValue(), TARGET_DIRECTORY_SCRIPT_ARG, faultArgs.get(TARGET_DIRECTORY),
                    BLOCK_SIZE_SCRIPT_ARG, faultArgs.get(IO_SIZE), TIMEOUT_SCRIPT_ARG,
                    faultArgs.get(TIMEOUT_IN_MILLI_SEC), PORT_SCRIPT_ARGUEMENT, faultArgs.get(AGENT_PORT))).toString();
        } else {
            return new StringBuilder()
                    .append(String.format(DISK_IO_INJECTION_COMMAND_WITH_ARGS, FaultName.DISKFAULT.getValue(),
                            TARGET_DIRECTORY_SCRIPT_ARG, faultArgs.get(TARGET_DIRECTORY), BLOCK_SIZE_SCRIPT_ARG,
                            faultArgs.get(IO_SIZE), TIMEOUT_SCRIPT_ARG, faultArgs.get(TIMEOUT_IN_MILLI_SEC)))
                    .toString();
        }
    }

    private String getCpuInjectionCommand(Map<String, String> faultArgs) {
        if (faultArgs.containsKey(AGENT_PORT)) {
            return new StringBuilder().append(String.format(CPU_INJECTION_COMMAND_WITH_ARGS_AND_PORT,
                    FaultName.CPUFAULT.getValue(), LOAD_SCRIPT_ARG, faultArgs.get(LOAD), TIMEOUT_SCRIPT_ARG,
                    faultArgs.get(TIMEOUT_IN_MILLI_SEC))).toString();
        } else {
            return new StringBuilder().append(String.format(CPU_INJECTION_COMMAND_WITH_ARGS,
                    FaultName.CPUFAULT.getValue(), LOAD_SCRIPT_ARG, faultArgs.get(LOAD), TIMEOUT_SCRIPT_ARG,
                    faultArgs.get(TIMEOUT_IN_MILLI_SEC), PORT_SCRIPT_ARGUEMENT, faultArgs.get(AGENT_PORT))).toString();
        }
    }

    //sample injection command: infra_submit --operation inject --faultname killProcessFault --processIdentifier "processIdentifier" --killAll yes --processId ""
    //  --remediationCommand  remediationCommand
    private String getKillServiceInjectionCommand(Map<String, String> faultArgs) {
        return new StringBuilder(String.format(KILL_SERVICE_INJECTION_COMMAND_WITH_ARGS,
                FaultName.KILLPROCESSFAULT.getValue(), faultArgs.get(PROCESS_IDENTIFIER), faultArgs.get(KILL_ALL),
                faultArgs.get(PROCESS_ID)))
                        .append(StringUtils.hasLength(faultArgs.get(KILL_PROCESS_REMEDIATION_COMMAND))
                                ? " --remediationCommand \"" + faultArgs.get(KILL_PROCESS_REMEDIATION_COMMAND) + "\""
                                : "")
                        .append(StringUtils.hasLength(faultArgs.get(AGENT_PORT))
                                ? " " + PORT_SCRIPT_ARGUEMENT + " " + faultArgs.get(AGENT_PORT) : "")
                        .toString();
    }

    private String getStopServiceInjectionCommand(Map<String, String> faultArgs) {
        return new StringBuilder(String.format(STOP_SERVICE_INJECTION_COMMAND_WITH_ARGS1, faultArgs.get(SERVICE_NAME),
                faultArgs.get(TIMEOUT_IN_MILLI_SEC)))
                        .append(StringUtils.hasLength(faultArgs.get(AGENT_PORT))
                                ? " " + PORT_SCRIPT_ARGUEMENT + " " + faultArgs.get(AGENT_PORT) : "")
                        .toString();
    }

    private String getNetworkFaultInjectionCommand(Map<String, String> faultArgs) {
        return new StringBuilder(String.format(NETWORK_FAULT_INJECTION_COMMAND_WITH_ARGS,
                FaultName.NETWORKFAULT.getValue(), FAULT_OPERATION_SCRIPT_ARG, faultArgs.get(FAULT_OPERATION),
                LATENCY_SCRIPT_ARG, faultArgs.get(LATENCY), PERCENTAGE_SCRIPT_ARG, faultArgs.get(PERCENTAGE),
                NIC_NAME_SCRIPT_ARG, faultArgs.get(NIC_NAME), TIMEOUT_SCRIPT_ARG, faultArgs.get(TIMEOUT_IN_MILLI_SEC),
                JITTER_SCRIPT_ARG,faultArgs.get(JITTER)))
                        .append(StringUtils.hasLength(faultArgs.get(AGENT_PORT))
                                ? " " + PORT_SCRIPT_ARGUEMENT + " " + faultArgs.get(AGENT_PORT) : "")
                        .toString();
    }

    private static String getFilehandlerFaultnjectionCommand(Map<String, String> faultArgs) {
        return new StringBuilder(String.format(FILEHANDLER_INJECTION_COMMAND_WITH_ARGS,
                FaultName.FILEHANDLERFAULT.getValue(), TIMEOUT_SCRIPT_ARG, faultArgs.get(TIMEOUT_IN_MILLI_SEC)))
                        .append(StringUtils.hasLength(faultArgs.get(AGENT_PORT))
                                ? " " + PORT_SCRIPT_ARGUEMENT + " " + faultArgs.get(AGENT_PORT) : "")
                        .toString();
    }

    public String getDiskSpaceInjectionCommand(Map<String, String> faultArgs) {
        if (faultArgs.containsKey(AGENT_PORT)) {
            return new StringBuilder()
                    .append(String.format(DISK_SPACE_INJECTION_COMMAND_WITH_ARGS, FaultName.DISKSPACEFAULT.getValue(),
                            DIRECTORY_PATH_SCRIPT_ARG, faultArgs.get(DIRECTORY_PATH), TIMEOUT_SCRIPT_ARG,
                            faultArgs.get(TIMEOUT_IN_MILLI_SEC), PORT_SCRIPT_ARGUEMENT, faultArgs.get(AGENT_PORT)))
                    .toString()
                    + (StringUtils.hasLength(faultArgs.get(DISK_FILL_SIZE))
                            ? " " + DISK_FILL_SIZE_SCRIPT_ARG + " " + faultArgs.get(DISK_FILL_SIZE) : "");
        } else {
            return new StringBuilder().append(String.format(DISK_SPACE_INJECTION_COMMAND_WITH_ARGS,
                    FaultName.DISKSPACEFAULT.getValue(), DIRECTORY_PATH_SCRIPT_ARG, faultArgs.get(DIRECTORY_PATH),
                    TIMEOUT_SCRIPT_ARG, faultArgs.get(TIMEOUT_IN_MILLI_SEC))).toString()
                    + (StringUtils.hasLength(faultArgs.get(DISK_FILL_SIZE))
                            ? " " + DISK_FILL_SIZE_SCRIPT_ARG + " " + faultArgs.get(DISK_FILL_SIZE) : "");
        }
    }

    /**
     * @param faultArgs
     * @param scriptBasePath
     * @return
     */
    private String getKernelPanicInjectionCommand(Map<String, String> faultArgs) {
        return new StringBuilder(KERNELPANIC_INJECTION_COMMAND_WITH_ARGS)
                .append(StringUtils.hasLength(faultArgs.get(AGENT_PORT))
                        ? " " + PORT_SCRIPT_ARGUEMENT + " " + faultArgs.get(AGENT_PORT) : "")
                .toString();
    }

    private String getNetworkPartitionInjectionCommand(Map<String, String> faultArgs) {
        return new StringBuilder(String.format(NETWORK_PARTITION_INJECTION_COMMAND_WITH_ARGS,
                getHosts(faultArgs.get(HOSTS_KEY)), faultArgs.get(TIMEOUT_IN_MILLI_SEC)))
                        .append(StringUtils.hasLength(faultArgs.get(AGENT_PORT))
                                ? " " + PORT_SCRIPT_ARGUEMENT + " " + faultArgs.get(AGENT_PORT) : "")
                        .toString();
    }

    private String getRemediationCommand(Map<String, String> faultArgs) {
        return new StringBuilder(INFRA_SUBMIT).append(OPERATION_REMEDIATE)
                .append(StringUtils.hasLength(faultArgs.get(AGENT_PORT))
                        ? " " + PORT_SCRIPT_ARGUEMENT + " " + faultArgs.get(AGENT_PORT) : "")
                .toString();
    }


    private String getStatusCommand(Map<String, String> faultArgs) {
        return new StringBuilder().append(INFRA_SUBMIT).append(OPERATION_STATUS)
                .append(StringUtils.hasLength(faultArgs.get(AGENT_PORT))
                        ? " " + PORT_SCRIPT_ARGUEMENT + " " + faultArgs.get(AGENT_PORT) : "")
                .toString();
    }

    public String buildRemediationCommand(Map<String, String> args, String scriptBasePath) {
        Map<String, String> faultArgs = FaultsHelper.parseArgs(CommonUtils.convertMaptoDelimitedString(args, ","));
        String command = "";
        String faultName = faultArgs.get(FAULT_NAME);
        if (faultName == null || faultName.isEmpty()) {
            throw new MangleRuntimeException(ErrorCode.FAULT_NAME_NOT_NULL);
        }
        try {
            switch (FaultName.valueOf(faultName.toUpperCase())) {
            case CPUFAULT:
                command = getRemediationCommand(faultArgs);
                break;
            case MEMORYFAULT:
                command = getRemediationCommand(faultArgs);
                break;
            case DISKFAULT:
                command = getRemediationCommand(faultArgs);
                break;
            case KILLPROCESSFAULT:
                if (!StringUtils.isEmpty(faultArgs.get(KILL_PROCESS_REMEDIATION_COMMAND))) {
                    command = new StringBuilder(getRemediationCommand(faultArgs)).append(" --remediationCommand \"")
                            .append(faultArgs.get(KILL_PROCESS_REMEDIATION_COMMAND)).append("\"").toString();
                }
                break;
            case STOPSERVICEFAULT:
                command = getStopServiceRemediationCommand(faultArgs);
                break;
            case NETWORKFAULT:
                command = getRemediationCommand(faultArgs);
                break;
            case DISKSPACEFAULT:
                command = getDiskSpaceRemediationCommand(faultArgs);
                break;
            case CLOCKSKEWFAULT:
                command = getRemediationCommand(faultArgs);
                break;
            case DBCONNECTIONLEAKFAULT_POSTGRES:
                command = DbFaultUtils2.getDbConnectionLeakRemediationCommand(faultArgs);
                break;
            case DBTRANSACTIONERRORFAULT_POSTGRES:
                command = DbFaultUtils2.getDbTransactionErrorRemediationCommand(faultArgs);
                break;
            case DBTRANSACTIONLATENCYFAULT_POSTGRES:
                command = DbFaultUtils2.getDbTransactionLatencyRemediationCommand(faultArgs);
                break;
            case DBCONNECTIONLEAKFAULT_MONGODB:
                command = DbFaultUtils2.getDbConnectionLeakRemediationCommand(faultArgs);
                break;
            case DBCONNECTIONLEAKFAULT_CASSANDRA:
                command = DbFaultUtils2.getDbConnectionLeakRemediationCommand(faultArgs);
                break;
            case NETWORKPARTITIONFAULT:
                command = getNetworkPartitionRemediationCommand(faultArgs);
                break;
            default:
                command = "";
            }
        } catch (Exception e) {
            throw new MangleRuntimeException(ErrorCode.UNSUPPORTED_FAULT);
        }
        return command;

    }

    public String buildStatusCommand(Map<String, String> args) {
        Map<String, String> faultArgs = FaultsHelper.parseArgs(CommonUtils.convertMaptoDelimitedString(args, ","));
        String command = "";
        String faultName = faultArgs.get(FAULT_NAME);
        if (faultName == null || faultName.isEmpty()) {
            throw new MangleRuntimeException(ErrorCode.FAULT_NAME_NOT_NULL);
        }
        command = getStatusCommand(faultArgs);
        return command;
    }


    private String getDiskSpaceRemediationCommand(Map<String, String> faultArgs) {
        return new StringBuilder(INFRA_SUBMIT)
                .append(String.format(DISK_SPACE_REMEDIATION_COMMAND_WITH_ARGS, faultArgs.get(DIRECTORY_PATH)))
                .append(StringUtils.hasLength(faultArgs.get(AGENT_PORT))
                        ? " " + PORT_SCRIPT_ARGUEMENT + " " + faultArgs.get(AGENT_PORT) : "")
                .toString();
    }

    private String getStopServiceRemediationCommand(Map<String, String> faultArgs) {
        return new StringBuilder(INFRA_SUBMIT)
                .append(String.format(STOP_SERVICE_REMEDIATION_COMMAND_WITH_ARGS1, faultArgs.get(SERVICE_NAME)))
                .append(StringUtils.hasLength(faultArgs.get(AGENT_PORT))
                        ? " " + PORT_SCRIPT_ARGUEMENT + " " + faultArgs.get(AGENT_PORT) : "")
                .toString();
    }

    private String getNetworkPartitionRemediationCommand(Map<String, String> faultArgs) {
        return new StringBuilder(INFRA_SUBMIT)
                .append(String.format(NETWORK_PARTITION_REMEDIATION_COMMAND_WITH_ARGS,
                        getHosts(faultArgs.get(HOSTS_KEY))))
                .append(StringUtils.hasLength(faultArgs.get(AGENT_PORT))
                        ? " " + PORT_SCRIPT_ARGUEMENT + " " + faultArgs.get(AGENT_PORT) : "")
                .toString();
    }

    public List<SupportScriptInfo> getAgentFaultScripts(CommandExecutionFaultSpec commandExecutorTaskInfo) {
        ArrayList<SupportScriptInfo> agentFaultInjectionScripts = new ArrayList<>();
        for (String scriptFileName : FaultName.valueOf(commandExecutorTaskInfo.getFaultName().toUpperCase())
                .getScriptFileNames()) {
            SupportScriptInfo faultInjectionScriptInfo1 = new SupportScriptInfo();
            faultInjectionScriptInfo1.setScriptFileName(scriptFileName);
            faultInjectionScriptInfo1.setTargetDirectoryPath(commandExecutorTaskInfo.getInjectionHomeDir());
            faultInjectionScriptInfo1.setClassPathResource(true);
            faultInjectionScriptInfo1.setExecutable(true);
            agentFaultInjectionScripts.add(faultInjectionScriptInfo1);
        }
        return agentFaultInjectionScripts;
    }

    public List<SupportScriptInfo> getAgentFaultScriptsPython(String targetDir) {
        ArrayList<SupportScriptInfo> agentFaultInjectionScripts = new ArrayList<>();

        SupportScriptInfo faultInjectionScriptInfo = new SupportScriptInfo();
        faultInjectionScriptInfo.setScriptFileName(FaultConstants.INFRA_AGENT_NAME);
        faultInjectionScriptInfo.setTargetDirectoryPath(targetDir);
        faultInjectionScriptInfo.setClassPathResource(true);
        faultInjectionScriptInfo.setExecutable(false);
        agentFaultInjectionScripts.add(faultInjectionScriptInfo);
        return agentFaultInjectionScripts;
    }

    public String getScriptNameforFault(FaultName faultName) {
        return faultName.getScriptFileName();
    }

    public FaultName getFaultName(Map<String, String> args) {
        Map<String, String> faultArgs = FaultsHelper.parseArgs(CommonUtils.convertMaptoDelimitedString(args, ","));
        return FaultName.valueOf(faultArgs.get(FAULT_NAME).toUpperCase());
    }

    public boolean isManualRemediationSupported(String faultName) {
        this.faultNames = CollectionUtils.isEmpty(faultNames) ? getFaultNamesWithNoRemediationSupport() : faultNames;
        if (getFaultNamesWithNoRemediationSupport().contains(faultName)) {
            log.info(String.format(FaultConstants.MANUAL_REMEDIATION_NOT_SUPPORTED, faultName));
            return false;
        }
        return true;
    }

    private List<String> getFaultNamesWithNoRemediationSupport() {
        this.faultNames = new ArrayList<>();
        faultNames.add(FaultName.FILEHANDLERFAULT.getValue());
        faultNames.add(FaultName.KERNELPANICFAULT.getValue());
        return faultNames;
    }

    private String getHosts(String hosts) {
        return hosts.replace(';', ',');
    }

    public CommandInfo getLinuxPythonAgentExtractCommandInfo(CommandExecutionFaultSpec faultSpec) {
        // Tar extraction command
        return CommandInfo
                .builder(String.format(EXTRACT_AGENT_COMMAND, faultSpec.getInjectionHomeDir(), INFRA_AGENT_NAME))
                .ignoreExitValueCheck(true).expectedCommandOutputList(Arrays.asList("")).build();

    }

    public CommandInfo getLinuxPythonAgentScriptsPermissionsUpdateCommandInfo(CommandExecutionFaultSpec faultSpec) {
        String agentPathInTargetMachine = faultSpec.getInjectionHomeDir() + INFRA_AGENT_NAME;
        // change permission command
        return CommandInfo
                .builder(
                        "chmod -R 777 " + agentPathInTargetMachine + ";chmod -R 777 " + agentPathInTargetMachine + "/*")
                .ignoreExitValueCheck(true).expectedCommandOutputList(Arrays.asList("")).build();
    }

    public CommandInfo getPythonAgentInstallCommandInfo(CommandExecutionFaultSpec faultSpec) {
        CommandInfo.CommandInfoBuilder builder = CommandInfo.builder(getLinuxPythonAgentInstallationCommand(faultSpec));
        getBasePythonAgentInstallationCommandInfo(builder);
        return builder.build();
    }

    private CommandInfo.CommandInfoBuilder getBasePythonAgentInstallationCommandInfo(
            CommandInfo.CommandInfoBuilder builder) {
        return builder.ignoreExitValueCheck(true).expectedCommandOutputList(Arrays.asList(""))
                .knownFailureMap(KnownFailuresHelper.getKnownFailuresOfAgentInstallationRequest());
    }

    private static String getLinuxPythonAgentInstallationCommand(CommandExecutionFaultSpec faultSpec) {
        String installationCommand = "cd %s;%s";
        String agentStartCommand = String.format(installationCommand,
                faultSpec.getInjectionHomeDir() + FaultConstants.INFRA_AGENT_NAME_FOLDER,
                "./infra_agent > /dev/null 2>&1 &");
        log.debug("infraagent start command:", agentStartCommand);
        return agentStartCommand;
    }
}
