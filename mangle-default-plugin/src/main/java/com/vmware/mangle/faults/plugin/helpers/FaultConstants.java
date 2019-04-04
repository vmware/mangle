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
    public static final String BASH = "/bin/sh";

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
    public static final String FAULT_NAME_MISSING = "Fault Name is Wrong.Please check your command";
    public static final String SUBMIT_COMMAND = BASH + " %s" + AGENT_NAME + "/bin/bmsubmit.sh";
    public static final String SUBMIT_COMMAND_WITH_PORT = SUBMIT_COMMAND + " -p %s ";
    public static final String REMEDIATION_COMMAND = SUBMIT_COMMAND + "-rf %s";
    public static final String GET_FAULT_COMMAND = SUBMIT_COMMAND + "-gf %s";
    public static final String PID_AGENT_COMMAND = SUBMIT_COMMAND + "-if %s";
    public static final String REMEDIATION_COMMAND_WITH_PORT = SUBMIT_COMMAND_WITH_PORT + "-rf %s";
    public static final String GET_FAULT_COMMAND_WITH_PORT = SUBMIT_COMMAND_WITH_PORT + "-gf %s";
    public static final String PID_AGENT_COMMAND_WITH_PORT = SUBMIT_COMMAND_WITH_PORT + "-if %s";
    public static final String SUCCESSFUL_BYTEMAN_AGENT_INSTALLATION_MESSAGE = "Started Byteman Listener Successfully";
    public static final String BYTEMAN_AGENT_INSTALLATION_RETRY_MESSAGE =
            "Agent JAR loaded but agent failed to initialize";
    public static final String SUCCESSFUL_TROUBLESHOOTING_ENABLED_MESSAGE =
            "install rule Trace - Capture Troubleshooting bundle.";
    public static final String ENABLE_TROUBLESHOOTING_RETRY_MESSAGE = "Troubleshooting Already Enabled.";
    public static final String TASK_ID = "taskId";
    public static final String JAVA_HOME_PATH = "javaHomePath";
    public static final String FI_ADD_INFO_FAULTID = "$FI_ADD_INFO_faultId";

    public static final String ARGUEMENT_PREFIX = "__";
    public static final String SCRIPT_ARGUEMENT_PREFIX = "--";
    public static final String FAULT_NAME = "faultName";
    public static final String FAULT_TYPE = "faultType";
    public static final String FAULT_NAME_ARG = ARGUEMENT_PREFIX + FAULT_NAME;
    public static final String TIMEOUT_IN_MILLI_SEC = "timeOutInMilliSeconds";
    public static final String TIMEOUT_IN_MILLI_SEC_ARG = ARGUEMENT_PREFIX + TIMEOUT_IN_MILLI_SEC;
    public static final String TIMEOUT = "timeout";
    public static final String TIMEOUT_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + TIMEOUT;
    public static final String DB_NAME = "dbName";
    public static final String PASSWORD_KEY = "password";
    public static final String USERNAME = "userName";
    public static final String REMEDIATE = " remediate";
    public static final String TARGET_DIRECTORY = "targetDir";
    public static final String LONG_LASTING = "longLasting";
    public static final String LONG_LASTING_ARG = ARGUEMENT_PREFIX + LONG_LASTING;
    public static final String USER = "user";
    public static final String PROCESS = "process";
    public static final String DEFAULT_TEMP_DIR = "/tmp";
    public static final String FORWARD_SLASH = "/";
    public static final String LOAD = "load";
    public static final String LOAD_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + LOAD;
    public static final String LOAD_ARG = ARGUEMENT_PREFIX + LOAD;
    public static final String OPERATION = "operation";
    public static final String PORT_9090 = "9090";
    public static final String PORT_9091 = "9091";
    public static final String BLOCK_SIZE = "blockSize";
    public static final String AVG_IO_SIZE = "avgIOSize";
    public static final String AVG_IO_SIZE_ARG = ARGUEMENT_PREFIX + AVG_IO_SIZE;
    public static final String BLOCK_SIZE_ARG = ARGUEMENT_PREFIX + BLOCK_SIZE;
    public static final String BLOCK_SIZE_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + BLOCK_SIZE;
    public static final String DIRECTORY_PATH = "directoryPath";
    public static final String TARGET_DIRECTORY_ARG = ARGUEMENT_PREFIX + TARGET_DIRECTORY;
    public static final String TARGET_DIRECTORY_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + TARGET_DIRECTORY;
    public static final String PROCESS_IDENTIFIER = "processIdentifier";
    public static final String PROCESS_IDENTIFIER_UNDERSCORE = ARGUEMENT_PREFIX + PROCESS_IDENTIFIER;
    public static final String KILL_PROCESS_REMEDIATION_COMMAND = "remediationCommand";
    public static final String KILL_PROCESS_REMEDIATION_COMMAND_ARG =
            ARGUEMENT_PREFIX + KILL_PROCESS_REMEDIATION_COMMAND;

    public static final String SCRIPT_WITH_PARAMS = " %s %s > %s 2>&1";
    public static final String OPERATION_INJECT = " --operation=inject ";
    public static final String OPERATION_REMEDIATE = " --operation=remediate";

    public static final String FILEHANDLER_SCRIPT_WITH_PARAMS = " %s > %s 2>&1";
    public static final String MEMORY_INJECTION_COMMAND_WITH_ARGS =
            new StringBuilder(FaultName.MEMORYFAULT.getScriptFileName()).append(OPERATION_INJECT)
                    .append("%s=%s %s=%s").toString();
    public static final String DISK_IO_INJECTION_COMMAND_WITH_ARGS =
            new StringBuilder(FaultName.DISKFAULT.getScriptFileName()).append(OPERATION_INJECT)
                    .append("%s=%s %s=%s %s=%s").toString();
    public static final String FILEHANDLER_INJECTION_COMMAND_WITH_ARGS =
            FaultName.FILEHANDLERFAULT.getScriptFileName() + FILEHANDLER_SCRIPT_WITH_PARAMS;
    public static final String CPU_INJECTION_COMMAND_WITH_ARGS =
            new StringBuilder(FaultName.CPUFAULT.getScriptFileName()).append(OPERATION_INJECT)
                    .append("%s=%s %s=%s").toString();
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

}
