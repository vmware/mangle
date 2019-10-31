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

import static com.vmware.mangle.utils.constants.FaultConstants.BLOCK_SIZE_SCRIPT_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.CPU_INJECTION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.DIRECTORY_PATH;
import static com.vmware.mangle.utils.constants.FaultConstants.DISK_FILL_SIZE;
import static com.vmware.mangle.utils.constants.FaultConstants.DISK_FILL_SIZE_SCRIPT_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.DISK_IO_INJECTION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.DISK_SPACE_INJECTION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.DISK_SPACE_REMEDIATION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.FAULT_NAME;
import static com.vmware.mangle.utils.constants.FaultConstants.FAULT_OPERATION;
import static com.vmware.mangle.utils.constants.FaultConstants.FAULT_OPERATION_SCRIPT_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.FILEHANDLER_INJECTION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.FORWARD_SLASH;
import static com.vmware.mangle.utils.constants.FaultConstants.IO_SIZE;
import static com.vmware.mangle.utils.constants.FaultConstants.KERNELPANIC_INJECTION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.KILL_ALL;
import static com.vmware.mangle.utils.constants.FaultConstants.KILL_PROCESS_REMEDIATION_COMMAND;
import static com.vmware.mangle.utils.constants.FaultConstants.KILL_SERVICE_INJECTION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.LATENCY;
import static com.vmware.mangle.utils.constants.FaultConstants.LATENCY_SCRIPT_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.LOAD;
import static com.vmware.mangle.utils.constants.FaultConstants.LOAD_SCRIPT_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.MEMORY_INJECTION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.NETWORK_FAULT_INJECTION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.utils.constants.FaultConstants.NIC_NAME;
import static com.vmware.mangle.utils.constants.FaultConstants.NIC_NAME_SCRIPT_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.OPERATION_REMEDIATE;
import static com.vmware.mangle.utils.constants.FaultConstants.PERCENTAGE;
import static com.vmware.mangle.utils.constants.FaultConstants.PERCENTAGE_SCRIPT_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.PROCESS_ID;
import static com.vmware.mangle.utils.constants.FaultConstants.PROCESS_IDENTIFIER;
import static com.vmware.mangle.utils.constants.FaultConstants.TARGET_DIRECTORY;
import static com.vmware.mangle.utils.constants.FaultConstants.TARGET_DIRECTORY_SCRIPT_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.TIMEOUT_IN_MILLI_SEC;
import static com.vmware.mangle.utils.constants.FaultConstants.TIMEOUT_SCRIPT_ARG;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.faults.plugin.helpers.FaultConstants;
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
                command = getCpuInjectionCommand(faultArgs, scriptBasePath);
                break;
            case MEMORYFAULT:
                command = getMemoryInjectionCommand(faultArgs, scriptBasePath);
                break;
            case DISKFAULT:
                command = getdiskIOInjectionCommand(faultArgs, scriptBasePath);
                break;
            case KILLPROCESSFAULT:
                command = getKillServiceInjectionCommand(faultArgs, scriptBasePath);
                break;
            case NETWORKFAULT:
                command = getNetworkFaultInjectionCommand(faultArgs, scriptBasePath);
                break;
            case FILEHANDLERFAULT:
                command = getFilehandlerFaultnjectionCommand(faultArgs, scriptBasePath);
                break;
            case DISKSPACEFAULT:
                command = getDiskSpaceInjectionCommand(faultArgs, scriptBasePath);
                break;
            case KERNELPANICFAULT:
                command = getKernelPanicInjectionCommand(scriptBasePath);
                break;
            default:
                command = "";
            }
        } catch (Exception e) {
            throw new MangleRuntimeException(ErrorCode.UNSUPPORTED_FAULT);
        }
        return command;
    }

    private String getMemoryInjectionCommand(Map<String, String> faultArgs, String scriptBasePath) {
        return new StringBuilder(scriptBasePath).append(String.format(MEMORY_INJECTION_COMMAND_WITH_ARGS,
                LOAD_SCRIPT_ARG, faultArgs.get(LOAD), TIMEOUT_SCRIPT_ARG, faultArgs.get(TIMEOUT_IN_MILLI_SEC)))
                .toString();
    }

    private String getdiskIOInjectionCommand(Map<String, String> faultArgs, String scriptBasePath) {
        return new StringBuilder(scriptBasePath).append(String.format(DISK_IO_INJECTION_COMMAND_WITH_ARGS,
                TARGET_DIRECTORY_SCRIPT_ARG, faultArgs.get(TARGET_DIRECTORY), BLOCK_SIZE_SCRIPT_ARG,
                faultArgs.get(IO_SIZE), TIMEOUT_SCRIPT_ARG, faultArgs.get(TIMEOUT_IN_MILLI_SEC))).toString();
    }

    private String getCpuInjectionCommand(Map<String, String> faultArgs, String scriptBasePath) {
        return new StringBuilder(scriptBasePath).append(String.format(CPU_INJECTION_COMMAND_WITH_ARGS, LOAD_SCRIPT_ARG,
                faultArgs.get(LOAD), TIMEOUT_SCRIPT_ARG, faultArgs.get(TIMEOUT_IN_MILLI_SEC))).toString();
    }

    private String getKillServiceInjectionCommand(Map<String, String> faultArgs, String scriptBasePath) {
        return new StringBuilder(scriptBasePath).append(String.format(KILL_SERVICE_INJECTION_COMMAND_WITH_ARGS,
                faultArgs.get(PROCESS_IDENTIFIER), faultArgs.get(KILL_ALL), faultArgs.get(PROCESS_ID))).toString();
    }

    private String getNetworkFaultInjectionCommand(Map<String, String> faultArgs, String scriptBasePath) {
        return new StringBuilder(scriptBasePath).append(String.format(NETWORK_FAULT_INJECTION_COMMAND_WITH_ARGS,
                FAULT_OPERATION_SCRIPT_ARG, faultArgs.get(FAULT_OPERATION), LATENCY_SCRIPT_ARG, faultArgs.get(LATENCY),
                PERCENTAGE_SCRIPT_ARG, faultArgs.get(PERCENTAGE), NIC_NAME_SCRIPT_ARG, faultArgs.get(NIC_NAME),
                TIMEOUT_SCRIPT_ARG, faultArgs.get(TIMEOUT_IN_MILLI_SEC))).toString();
    }


    private static String getFilehandlerFaultnjectionCommand(Map<String, String> faultArgs, String scriptBasePath) {
        return new StringBuilder(scriptBasePath).append(String.format(FILEHANDLER_INJECTION_COMMAND_WITH_ARGS,
                TIMEOUT_SCRIPT_ARG, faultArgs.get(TIMEOUT_IN_MILLI_SEC))).toString();
    }

    public String getDiskSpaceInjectionCommand(Map<String, String> faultArgs, String scriptBasePath) {
        return scriptBasePath
                + String.format(DISK_SPACE_INJECTION_COMMAND_WITH_ARGS, faultArgs.get(DIRECTORY_PATH),
                        faultArgs.get(TIMEOUT_IN_MILLI_SEC))
                + (StringUtils.hasLength(faultArgs.get(DISK_FILL_SIZE))
                        ? " " + DISK_FILL_SIZE_SCRIPT_ARG + "=" + faultArgs.get(DISK_FILL_SIZE)
                        : "");
    }

    /**
     * @param faultArgs
     * @param scriptBasePath
     * @return
     */
    private String getKernelPanicInjectionCommand(String scriptBasePath) {
        return new StringBuilder(scriptBasePath).append(KERNELPANIC_INJECTION_COMMAND_WITH_ARGS).toString();
    }

    private String getRemediationCommand(String scriptFileName, String scriptBasePath) {
        return new StringBuilder(scriptBasePath).append(scriptFileName).append(OPERATION_REMEDIATE).toString();
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
                command = getRemediationCommand(getScriptNameforFault(FaultName.CPUFAULT), scriptBasePath);
                break;
            case MEMORYFAULT:
                command = getRemediationCommand(getScriptNameforFault(FaultName.MEMORYFAULT), scriptBasePath);
                break;
            case DISKFAULT:
                command = getRemediationCommand(getScriptNameforFault(FaultName.DISKFAULT), scriptBasePath);
                break;
            case KILLPROCESSFAULT:
                if (!StringUtils.isEmpty(faultArgs.get(KILL_PROCESS_REMEDIATION_COMMAND))) {
                    command = new StringBuilder(
                            getRemediationCommand(FaultName.KILLPROCESSFAULT.getScriptFileName(), scriptBasePath))
                                    .append(" --remediationCommand=\"")
                                    .append(faultArgs.get(KILL_PROCESS_REMEDIATION_COMMAND)).append("\"").toString();
                }
                break;
            case NETWORKFAULT:
                command = getRemediationCommand(getScriptNameforFault(FaultName.NETWORKFAULT), scriptBasePath);
                break;
            case DISKSPACEFAULT:
                command = getDiskSpaceRemediationCommand(getScriptNameforFault(FaultName.DISKSPACEFAULT),
                        scriptBasePath, faultArgs);
                break;
            default:
                command = "";
            }
        } catch (Exception e) {
            throw new MangleRuntimeException(ErrorCode.UNSUPPORTED_FAULT);
        }
        return command;

    }

    private String getDiskSpaceRemediationCommand(String scriptFileName, String scriptBasePath,
            Map<String, String> faultArgs) {
        return new StringBuilder(scriptBasePath).append(scriptFileName)
                .append(String.format(DISK_SPACE_REMEDIATION_COMMAND_WITH_ARGS, faultArgs.get(DIRECTORY_PATH)))
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
}
