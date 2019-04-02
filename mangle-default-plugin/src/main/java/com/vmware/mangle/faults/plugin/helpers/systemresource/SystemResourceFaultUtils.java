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

import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.BLOCK_SIZE;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.BLOCK_SIZE_SCRIPT_ARG;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.CPU_INJECTION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.DISK_IO_INJECTION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.FAULT_NAME;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.FORWARD_SLASH;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.KILL_PROCESS_REMEDIATION_COMMAND;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.KILL_SERVICE_INJECTION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.LOAD;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.LOAD_SCRIPT_ARG;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.MEMORY_INJECTION_COMMAND_WITH_ARGS;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.OPERATION_REMEDIATE;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.PROCESS_IDENTIFIER;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.TARGET_DIRECTORY;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.TARGET_DIRECTORY_SCRIPT_ARG;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.TIMEOUT_IN_MILLI_SEC;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.TIMEOUT_SCRIPT_ARG;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.faults.plugin.helpers.FaultsHelper;
import com.vmware.mangle.services.enums.FaultName;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author bkaranam
 *
 */
@Component
public class SystemResourceFaultUtils {


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
                faultArgs.get(BLOCK_SIZE), TIMEOUT_SCRIPT_ARG, faultArgs.get(TIMEOUT_IN_MILLI_SEC))).toString();
    }


    private String getCpuInjectionCommand(Map<String, String> faultArgs, String scriptBasePath) {
        return new StringBuilder(scriptBasePath).append(String.format(CPU_INJECTION_COMMAND_WITH_ARGS, LOAD_SCRIPT_ARG,
                faultArgs.get(LOAD), TIMEOUT_SCRIPT_ARG, faultArgs.get(TIMEOUT_IN_MILLI_SEC))).toString();
    }

    private String getKillServiceInjectionCommand(Map<String, String> faultArgs, String scriptBasePath) {
        return new StringBuilder(scriptBasePath)
                .append(String.format(KILL_SERVICE_INJECTION_COMMAND_WITH_ARGS, faultArgs.get(PROCESS_IDENTIFIER)))
                .toString();
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
                command = new StringBuilder(
                        getRemediationCommand(FaultName.KILLPROCESSFAULT.getScriptFileName(), scriptBasePath))
                                .append(" --remediationCommand=\"")
                                .append(faultArgs.get(KILL_PROCESS_REMEDIATION_COMMAND)).append("\"").toString();
                break;
            default:
                command = "";
            }
        } catch (Exception e) {
            throw new MangleRuntimeException(ErrorCode.UNSUPPORTED_FAULT);
        }
        return command;

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
}
