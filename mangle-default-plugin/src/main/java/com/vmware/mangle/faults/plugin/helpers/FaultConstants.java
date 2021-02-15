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

package com.vmware.mangle.faults.plugin.helpers;

import com.vmware.mangle.services.enums.FaultName;

/**
 * @author jayasankarr
 *
 *
 */
@SuppressWarnings("squid:S2068")
public class FaultConstants {
    private FaultConstants() {
    }

    public static final String AGENT_NAME = "byteman-download-3.0.10";
    public static final String AGENT_JAR_EXTENSION = "-full.tar.gz";
    public static final String INJECTION_SCRIPTS_FOLDER = "InjectionScripts/";
    public static final String INFRA_AGENT_NAME = "infra-agent.tar.gz";
    public static final String BASH = "/bin/sh";
    public static final String INFRA_AGENT_NAME_FOLDER = "/infra_agent";

    public static final String KUBE_FAULT_EXEC_STRING = "exec -it %s -c %s -- %s";
    public static final String EXTRACT_AGENT_COMMAND = "cd %s;tar -zxvf %s";
    public static final String REMEDIATION_SUCCESSFUL_STRING = "Received Remediation Request Successfully";
    public static final String EXPECTED_REMEDIATION_MESSAGE_FOR_FILE_NOT_FOUND = "No such file or directory";
    public static final String MESSAGE_THROWN_FOR_EXPECTED_REMEDIATION_FAILURE = "Fault has been remediated already";
    public static final String FAULT_COMPLETION_STRING = "\"faultStatus\":\"COMPLETED\"";
    public static final String PID_ATTACH_MXBEANS_COMMAND = BASH + " %s/" + AGENT_NAME + "/bin/bminstall.sh -s -b %s";
    public static final String PID_K8S_ATTACH_MXBEANS_COMMAND =
            "exec -it %s -c %s -- sh %s/" + AGENT_NAME + "/bin/bminstall.sh -s -b %s";
    public static final String PID_ATTACH_MXBEANS_COMMAND_WITH_PORT =
            BASH + " %s/" + AGENT_NAME + "/bin/bminstall.sh -p %s -s -b %s";
    public static final String PID_K8S_ATTACH_MXBEANS_COMMAND_WITH_PORT =
            "exec -it %s -c %s -- " + BASH + " %s" + AGENT_NAME + "/bin/bminstall.sh -p %s -s -b %s";
    public static final String SCRIPT_WITH_PARAMS = " %s %s > %s 2>&1";
    public static final String OPERATION_INJECT = " --operation=inject ";
    public static final String OPERATION_REMEDIATE = " --operation=remediate";
    public static final String INFRA_AGENT_SUBMIT_COMMAND = "cd %s;./%s --faultId %s";

    public static final String FILEHANDLER_SCRIPT_WITH_PARAMS = " %s > %s 2>&1";
    public static final String MEMORY_INJECTION_COMMAND_WITH_ARGS =
            new StringBuilder(FaultName.MEMORYFAULT.getScriptFileName()).append(OPERATION_INJECT).append("%s=%s %s=%s")
                    .toString();
    public static final String DISK_IO_INJECTION_COMMAND_WITH_ARGS =
            new StringBuilder(FaultName.DISKFAULT.getScriptFileName()).append(OPERATION_INJECT)
                    .append("%s=%s %s=%s %s=%s").toString();
    public static final String FILEHANDLER_INJECTION_COMMAND_WITH_ARGS =
            FaultName.FILEHANDLERFAULT.getScriptFileName() + FILEHANDLER_SCRIPT_WITH_PARAMS;
    public static final String CPU_INJECTION_COMMAND_WITH_ARGS =
            new StringBuilder(FaultName.CPUFAULT.getScriptFileName()).append(OPERATION_INJECT).append("%s=%s %s=%s")
                    .toString();
    public static final String KILL_SERVICE_INJECTION_COMMAND_WITH_ARGS =
            new StringBuilder(FaultName.KILLPROCESSFAULT.getScriptFileName()).append(OPERATION_INJECT)
                    .append("--processIdentifier=\"%s\"").toString();
    public static final String KILL_SERVICE_INJECTION_COMMAND_WIN_WITH_ARGS =
            FaultName.KILLPROCESSFAULT_WINDOWS.getScriptFileName() + " \"" + "%s" + "\"" + "> %s 2>&1";
    public static final String DISK_LATENCY_INJECTION_COMMAND_WITH_ARGS =
            "python " + "%s" + FaultName.DISKFUSEFAULT.getScriptFileName() + " -o " + "%s" + " -l " + "%s " + " -p "
                    + "%s " + ">  %s 2>&1";
    public static final String DISK_LATENCY_REMEDIATION_COMMAND_WITH_ARGS =
            "python " + FaultName.DISKFUSEFAULT.getScriptFileName() + " -o remediate" + " -p " + "%s " + ">  %s 2>&1";
    public static final String DISK_FUSE_FAULT_DIRECTORY = "diskFUSEFaultDirectory";
    public static final String DB_CONNECTION_LEAK_INJECTION_COMMAND_WITH_ARGS = "%s %s %s %s %s %s > %s 2>&1";
    public static final String DB_TRANSACTION_LATENCY_INJECTION_COMMAND_WITH_ARGS =
            "%s %s %s %s %s %s %s %s %s > %s 2>&1";
    public static final String DB_TRANSACTION_ERROR_INJECTION_COMMAND_WITH_ARGS =
            "%s %s %s %s %s %s %s %s %s > %s 2>&1";
    public static final String MANUAL_REMEDIATION_NOT_SUPPORTED =
            "Manual remediation of %s is not supported. Hence setting the remediation commandinfo list to be empty.";
    public static final String INFRA_SUBMIT = "infra_submit ";
}
