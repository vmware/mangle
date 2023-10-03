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

package com.vmware.mangle.utils.constants;

import com.vmware.mangle.services.enums.FaultName;

/**
 * @author jayasankarr
 *
 */
@SuppressWarnings("squid:S2068")
public class FaultConstants {
    private FaultConstants() {
    }

    public static final String AGENT_NAME = "mangle-java-agent-3.5.0";
    public static final String AGENT_JAR_EXTENSION = "-full.tar.gz";
    public static final String INJECTION_SCRIPTS_FOLDER = "InjectionScripts/";
    public static final String INFRA_AGENT_NAME = "infra-agent.tar.gz";
    public static final String INFRA_AGENT_FILE = "infra_agent";
    public static final String INFRA_SUBMIT = "infra_submit ";
    public static final String BASH = "/bin/sh";
    public static final String DELETE_COMMAND = "rm -rf ";

    public static final String KUBE_FAULT_EXEC_STRING = "exec -it %s -c %s -- %s";
    public static final String EQUALS_ARG = "=\"%s\"";
    public static final String EXTRACT_AGENT_COMMAND = "cd %s;tar -zxvf %s";
    public static final String REMEDIATION_REQUEST_SUCCESSFUL_STRING = "Received Remediation Request Successfully";
    public static final String AGENT_NOT_AVAILABLE_STRING =
            "Failed to process request: java.net.ConnectException: Connection refused";
    public static final String EXPECTED_MESSAGE_FOR_KILL_OPERATION_NOT_PERMITTED = "Operation not permitted";
    public static final String MESSAGE_THROWN_FOR_KILL_OPERATION_NOT_PERMITTED = "Kill Service Operation not permitted";
    public static final String FAULT_COMPLETION_STRING = "faultStatus:COMPLETED";
    public static final String PID_ATTACH_MXBEANS_COMMAND = BASH + " %s/" + AGENT_NAME + "/bin/bminstall.sh -s -b %s";
    public static final String PID_K8S_ATTACH_MXBEANS_COMMAND =
            "exec -it %s -c %s -- sh %s/" + AGENT_NAME + "/bin/bminstall.sh -s -b %s";
    public static final String PID_ATTACH_MXBEANS_COMMAND_WITH_PORT =
            BASH + " %s" + AGENT_NAME + "/bin/bminstall.sh -p %s -s -b %s";
    public static final String PID_K8S_ATTACH_MXBEANS_COMMAND_WITH_PORT =
            "exec -it %s -c %s -- " + BASH + " %s" + AGENT_NAME + "/bin/bminstall.sh -p %s -s -b %s";
    public static final String PID_K8S_ATTACH_MXBEANS_COMMAND_WITH_PORT_JAVA_HOME =
            "exec -it %s -c %s -- " + BASH + " -c \"%s" + AGENT_NAME + "/bin/bminstall.sh -p %s -s -b %s\"";
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
            "Agent is already running on requested process";
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
    public static final String FAULT_ID = "faultId";
    public static final String FAULT_ID_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + FAULT_ID;
    public static final String OPERATION = "operation";
    public static final String PORT_9090 = "9090";
    public static final String PORT_9091 = "9091";
    public static final String BLOCK_SIZE = "blockSize";
    public static final String IO_SIZE = "ioSize";
    public static final String AVG_IO_SIZE = "avgIOSize";
    public static final String AVG_IO_SIZE_ARG = ARGUEMENT_PREFIX + AVG_IO_SIZE;
    public static final String IO_SIZE_ARG = ARGUEMENT_PREFIX + IO_SIZE;
    public static final String BLOCK_SIZE_ARG = ARGUEMENT_PREFIX + BLOCK_SIZE;
    public static final String BLOCK_SIZE_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + BLOCK_SIZE;
    public static final String DIRECTORY_PATH = "directoryPath";
    public static final String TARGET_DIRECTORY_ARG = ARGUEMENT_PREFIX + TARGET_DIRECTORY;
    public static final String TARGET_DIRECTORY_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + TARGET_DIRECTORY;
    public static final String PROCESS_IDENTIFIER = "processIdentifier";
    public static final String SERVICE_NAME = "serviceName";
    public static final String SERVICE_NAME_ARG = ARGUEMENT_PREFIX + SERVICE_NAME;
    public static final String SERVICE_NAME_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + SERVICE_NAME;
    public static final String KILL_ALL = "killAll";
    public static final String PROCESS_ID = "processId";
    public static final String PROCESS_IDENTIFIER_UNDERSCORE = ARGUEMENT_PREFIX + PROCESS_IDENTIFIER;
    public static final String PROCESS_ID_UNDERSCORE = ARGUEMENT_PREFIX + PROCESS_ID;
    public static final String KILL_PROCESS_REMEDIATION_COMMAND = "remediationCommand";
    public static final String KILL_PROCESS_REMEDIATION_COMMAND_ARG =
            ARGUEMENT_PREFIX + KILL_PROCESS_REMEDIATION_COMMAND;
    public static final String FAULT_OPERATION = "faultOperation";
    public static final String FAULT_OPERATION_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + FAULT_OPERATION;
    public static final String FAULT_OPERATION_ARG = ARGUEMENT_PREFIX + FAULT_OPERATION;
    public static final String LATENCY = "latency";
    public static final String LATENCY_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + LATENCY;
    public static final String JITTER = "jitter";
    public static final String JITTER_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + JITTER;
    public static final String JITTER_ARG = ARGUEMENT_PREFIX + JITTER;
    public static final String LATENCY_ARG = ARGUEMENT_PREFIX + LATENCY;
    public static final String PERCENTAGE = "percentage";
    public static final String PERCENTAGE_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + PERCENTAGE;
    public static final String PERCENTAGE_ARG = ARGUEMENT_PREFIX + PERCENTAGE;
    public static final String NIC_NAME = "nicName";
    public static final String NIC_NAME_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + NIC_NAME;
    public static final String NIC_NAME_ARG = ARGUEMENT_PREFIX + NIC_NAME;

    public static final String SECONDS = "seconds";
    public static final String SECONDS_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + SECONDS;
    public static final String SECONDS_ARG = ARGUEMENT_PREFIX + SECONDS;

    public static final String MINUTES = "minutes";
    public static final String MINUTES_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + MINUTES;
    public static final String MINUTES_ARG = ARGUEMENT_PREFIX + MINUTES;

    public static final String HOURS = "hours";
    public static final String HOURS_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + HOURS;
    public static final String HOURS_ARG = ARGUEMENT_PREFIX + HOURS;

    public static final String DAYS = "days";
    public static final String DAYS_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + DAYS;
    public static final String DAYS_ARG = ARGUEMENT_PREFIX + DAYS;

    public static final String CLOCK_TYPE = "type";
    public static final String CLOCK_TYPE_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + CLOCK_TYPE;
    public static final String CLOCK_TYPE_ARG = ARGUEMENT_PREFIX + CLOCK_TYPE;

    public static final String SCRIPT_WITH_PARAMS = " %s %s > %s 2>&1";
    public static final String OPERATION_INJECT = " --operation inject ";
    public static final String OPERATION_REMEDIATE = " --operation remediate ";

    public static final String OPERATION_STATUS = " --operation status ";

    public static final String AGENT_PORT = "agentPort";
    public static final String PORT_SCRIPT_ARGUEMENT = SCRIPT_ARGUEMENT_PREFIX + AGENT_PORT;
    public static final String OPERATION_STATUS_WITH_PORT = OPERATION_STATUS + PORT_SCRIPT_ARGUEMENT + " %s";
    public static final String OPERATION_REMEDIATE_WITH_PORT = OPERATION_REMEDIATE + PORT_SCRIPT_ARGUEMENT + " %s";

    public static final String MEMORY_INJECTION_COMMAND_WITH_ARGS = new StringBuilder(INFRA_SUBMIT)
            .append(OPERATION_INJECT).append("--faultname %s ").append("%s %s %s %s").toString();
    public static final String DISK_IO_INJECTION_COMMAND_WITH_ARGS = new StringBuilder(INFRA_SUBMIT)
            .append(OPERATION_INJECT).append("--faultname %s ").append("%s %s %s %s %s %s").toString();
    public static final String CLOCK_SKEW_INJECTION_COMMAND_WITH_ARGS =
            new StringBuilder(INFRA_SUBMIT).append(OPERATION_INJECT).append("--faultname %s ")
                    .append("%s %s %s %s %s %s %s %s %s %s %s %s").toString();

    public static final String MEMORY_INJECTION_COMMAND_WITH_ARGS_AND_PORT = new StringBuilder(INFRA_SUBMIT)
            .append(OPERATION_INJECT).append("--faultname %s ").append("%s %s %s %s %s %s").toString();
    public static final String DISK_IO_INJECTION_COMMAND_WITH_ARGS_AND_PORT = new StringBuilder(INFRA_SUBMIT)
            .append(OPERATION_INJECT).append("--faultname %s ").append("%s %s %s %s %s %s %s %s").toString();
    public static final String CLOCK_SKEW_INJECTION_COMMAND_WITH_ARGS_AND_PORT =
            new StringBuilder("infra_submit ").append(OPERATION_INJECT).append("--faultname %s ")
                    .append("%s %s %s %s %s %s %s %s %s %s %s %s %s %s").toString();
    public static final String CLOCK_SKEW_REMEDIATION_COMMAND_WITH_ARGS =
            new StringBuilder(FaultName.CLOCKSKEWFAULT.getScriptFileName()).append(OPERATION_REMEDIATE).append("%s=%s")
                    .toString();
    public static final String CPU_INJECTION_COMMAND_WITH_ARGS_AND_PORT = new StringBuilder(INFRA_SUBMIT)
            .append(OPERATION_INJECT).append("--faultname %s ").append("%s %s %s %s %s %s").toString();

    public static final String FILEHANDLER_INJECTION_COMMAND_WITH_ARGS = new StringBuilder(INFRA_SUBMIT)
            .append("--faultname %s ").append(OPERATION_INJECT).append("%s %s").toString();
    public static final String FILEHANDLER_INJECTION_COMMAND_WITH_ARGS_AND_PORT =
            FILEHANDLER_INJECTION_COMMAND_WITH_ARGS + " %s %s";

    public static final String CPU_INJECTION_COMMAND_WITH_ARGS = new StringBuilder(INFRA_SUBMIT)
            .append(OPERATION_INJECT).append("--faultname %s ").append("%s %s %s %s").toString();
    public static final String DISK_SPACE_INJECTION_COMMAND_WITH_ARGS_AND_PORT = new StringBuilder(INFRA_SUBMIT)
            .append(OPERATION_INJECT).append("--faultname %s ").append("%s %s %s %s %s %s").toString();
    public static final String DISK_SPACE_INJECTION_COMMAND_WITH_ARGS = new StringBuilder(INFRA_SUBMIT)
            .append(OPERATION_INJECT).append("--faultname %s ").append("%s %s %s %s").toString();

    public static final String KILL_SERVICE_INJECTION_COMMAND_WITH_ARGS =
            new StringBuilder(INFRA_SUBMIT).append(OPERATION_INJECT).append("--faultname %s ")
                    .append("--processIdentifier \"%s\" --killAll %s --processId \"%s\"").toString();
    /*public static final String KILL_SERVICE_INJECTION_COMMAND_WITH_ARGS =
            new StringBuilder(FaultName.KILLPROCESSFAULT.getScriptFileName()).append(OPERATION_INJECT)
                    .append("--processIdentifier=\"%s\" --killAll=%s --processId=\"%s\"").toString();*/
    public static final String NETWORK_FAULT_INJECTION_COMMAND_WITH_ARGS = new StringBuilder(INFRA_SUBMIT)
            .append(OPERATION_INJECT).append("--faultname %s ").append("%s %s %s %s %s %s %s %s %s %s %s %s").toString();
    public static final String NETWORK_FAULT_INJECTION_COMMAND_WITH_ARGS_WITH_PORT =
            new StringBuilder(INFRA_SUBMIT).append(OPERATION_INJECT).append("--faultname %s ")
                    .append("%s %s %s %s %s %s %s %s %s %s %s %s").toString();

    public static final String STOP_SERVICE_INJECTION_COMMAND_WITH_ARGS =
            new StringBuilder(FaultName.STOPSERVICEFAULT.getScriptFileName()).append(OPERATION_INJECT + " ")
                    .append(SERVICE_NAME_SCRIPT_ARG).append(EQUALS_ARG + " ").append(TIMEOUT_SCRIPT_ARG)
                    .append(EQUALS_ARG).toString();
    public static final String STOP_SERVICE_REMEDIATION_COMMAND_WITH_ARGS =
            new StringBuilder(FaultName.STOPSERVICEFAULT.getScriptFileName()).append(OPERATION_REMEDIATE + " ")
                    .append(SERVICE_NAME_SCRIPT_ARG).append(EQUALS_ARG).toString();
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

    public static final String FAULT_PARAMETERS_KEY = "faultParameters";
    public static final String DIRECTORY_PATH_ARG = ARGUEMENT_PREFIX + DIRECTORY_PATH;
    public static final String DISK_FILL_SIZE = "diskFillSize";
    public static final String DISK_FILL_SIZE_ARG = ARGUEMENT_PREFIX + DISK_FILL_SIZE;
    public static final String DISK_FILL_SIZE_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + DISK_FILL_SIZE;
    public static final String DIRECTORY_PATH_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + DIRECTORY_PATH;

    public static final String DISK_SPACE_REMEDIATION_COMMAND_WITH_ARGS =
            OPERATION_REMEDIATE + " " + DIRECTORY_PATH_SCRIPT_ARG + " %s";
    public static final String KERNELPANIC_INJECTION_COMMAND_WITH_ARGS =
            new StringBuilder(INFRA_SUBMIT).append(OPERATION_INJECT).append("--faultname ")
                    .append(FaultName.KERNELPANICFAULT.getValue()).toString().trim();
    public static final String DB_NAME_ARG = ARGUEMENT_PREFIX + DB_NAME;
    public static final String DB_USER_NAME_ARG = ARGUEMENT_PREFIX + USERNAME;
    public static final String DB_PASSWORD_ARG = ARGUEMENT_PREFIX + PASSWORD_KEY;
    public static final String DB_PORT = "port";
    public static final String DB_PORT_ARG = ARGUEMENT_PREFIX + DB_PORT;
    public static final String DB_TYPE_KEY = "dbType";
    public static final String DB_TYPE_ARG = ARGUEMENT_PREFIX + DB_TYPE_KEY;
    public static final String DB_USER_NAME_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + USERNAME;
    public static final String DB_PASSWORD_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + PASSWORD_KEY;
    public static final String DB_PORT_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + DB_PORT;
    public static final String DB_NAME_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + DB_NAME;
    public static final String DB_SSL_ENABLED = "sslEnabled";
    public static final String DB_SSL_ENABLED_ARG = ARGUEMENT_PREFIX + DB_SSL_ENABLED;
    public static final String DB_SSL_ENABLED_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + DB_SSL_ENABLED;
    public static final String DATABASE_CONNECTION_LEAK_INJECTION_COMMAND_WITH_ARGS = OPERATION_INJECT
            + DB_NAME_SCRIPT_ARG + "=%s " + DB_USER_NAME_SCRIPT_ARG + "=%s " + DB_PASSWORD_SCRIPT_ARG + "=%s "
            + DB_PORT_SCRIPT_ARG + "=%s " + DB_SSL_ENABLED_SCRIPT_ARG + "=%s " + TIMEOUT_SCRIPT_ARG + "=%s";
    public static final String DATABASE_CONNECTION_LEAK_REMEDIATION_COMMAND_WITH_ARGS =
            OPERATION_REMEDIATE + " " + DB_NAME_SCRIPT_ARG + "=%s " + DB_USER_NAME_SCRIPT_ARG + "=%s "
                    + DB_SSL_ENABLED_SCRIPT_ARG + "=%s " + DB_PORT_SCRIPT_ARG + "=%s";
    public static final String DB_TABLE_NAME_KEY = "tableName";
    public static final String DB_TABLE_NAME_ARG = ARGUEMENT_PREFIX + DB_TABLE_NAME_KEY;
    public static final String DB_TABLE_NAME_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + DB_TABLE_NAME_KEY;
    public static final String DB_ERROR_CODE_KEY = "errorCode";
    public static final String DB_ERROR_CODE_ARG = ARGUEMENT_PREFIX + DB_ERROR_CODE_KEY;
    public static final String DB_ERROR_CODE_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + DB_ERROR_CODE_KEY;
    public static final String DB_PERCENTAGE_KEY = "percentage";
    public static final String DB_PERCENTAGE_ARG = ARGUEMENT_PREFIX + DB_PERCENTAGE_KEY;
    public static final String DB_PERCENTAGE_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + DB_PERCENTAGE_KEY;
    public static final String DB_LATENCY_KEY = "latency";
    public static final String DB_LATENCY_ARG = ARGUEMENT_PREFIX + DB_LATENCY_KEY;
    public static final String DB_LATENCY_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + DB_LATENCY_KEY;
    public static final String DATABASE_TRANSACTION_ERROR_INJECTION_COMMAND_WITH_ARGS = OPERATION_INJECT
            + DB_NAME_SCRIPT_ARG + "=%s " + DB_USER_NAME_SCRIPT_ARG + "=%s " + DB_PASSWORD_SCRIPT_ARG + "=%s "
            + DB_PORT_SCRIPT_ARG + "=%s " + DB_TABLE_NAME_SCRIPT_ARG + "=%s " + DB_ERROR_CODE_SCRIPT_ARG + "=%s "
            + DB_PERCENTAGE_SCRIPT_ARG + "=%s " + DB_SSL_ENABLED_SCRIPT_ARG + "=%s " + TIMEOUT_SCRIPT_ARG + "=%s";
    public static final String DATABASE_TRANSACTION_ERROR_REMEDIATION_COMMAND_WITH_ARGS = OPERATION_REMEDIATE + " "
            + DB_NAME_SCRIPT_ARG + "=%s " + DB_USER_NAME_SCRIPT_ARG + "=%s " + DB_PASSWORD_SCRIPT_ARG + "=%s "
            + DB_PORT_SCRIPT_ARG + "=%s " + DB_SSL_ENABLED_SCRIPT_ARG + "=%s " + DB_TABLE_NAME_SCRIPT_ARG + "=%s";
    public static final String DATABASE_TRANSACTION_LATENCY_INJECTION_COMMAND_WITH_ARGS = OPERATION_INJECT
            + DB_NAME_SCRIPT_ARG + "=%s " + DB_USER_NAME_SCRIPT_ARG + "=%s " + DB_PASSWORD_SCRIPT_ARG + "=%s "
            + DB_PORT_SCRIPT_ARG + "=%s " + DB_TABLE_NAME_SCRIPT_ARG + "=%s " + DB_LATENCY_SCRIPT_ARG + "=%s "
            + DB_PERCENTAGE_SCRIPT_ARG + "=%s " + DB_SSL_ENABLED_SCRIPT_ARG + "=%s " + TIMEOUT_SCRIPT_ARG + "=%s";
    public static final String DATABASE_TRANSACTION_LATENCY_REMEDIATION_COMMAND_WITH_ARGS = OPERATION_REMEDIATE + " "
            + DB_NAME_SCRIPT_ARG + "=%s " + DB_USER_NAME_SCRIPT_ARG + "=%s " + DB_PASSWORD_SCRIPT_ARG + "=%s "
            + DB_PORT_SCRIPT_ARG + "=%s " + DB_SSL_ENABLED_SCRIPT_ARG + "=%s " + DB_TABLE_NAME_SCRIPT_ARG + "=%s";
    public static final String HOSTS_KEY = "hosts";
    public static final String HOSTS_ARG = ARGUEMENT_PREFIX + HOSTS_KEY;
    public static final String HOSTS_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + HOSTS_KEY;
    public static final String NETWORK_PARTITION_INJECTION_COMMAND_WITH_ARGS = new StringBuilder(INFRA_SUBMIT)
            .append(OPERATION_INJECT + "--faultname ").append(FaultName.NETWORKPARTITIONFAULT.getValue() + " ")
            .append(HOSTS_SCRIPT_ARG + " %s " + TIMEOUT_SCRIPT_ARG + " %s").toString();

    public static final String NETWORK_PARTITION_REMEDIATION_COMMAND_WITH_ARGS =
            OPERATION_REMEDIATE + HOSTS_SCRIPT_ARG + " %s";
    public static final String STOP_SERVICE_REMEDIATION_COMMAND_WITH_ARGS1 =
            OPERATION_REMEDIATE + SERVICE_NAME_SCRIPT_ARG + " %s";
    public static final String STOP_SERVICE_INJECTION_COMMAND_WITH_ARGS1 = new StringBuilder(INFRA_SUBMIT)
            .append(OPERATION_INJECT + "--faultname ").append(FaultName.STOPSERVICEFAULT.getValue() + " ")
            .append(SERVICE_NAME_SCRIPT_ARG).append(" %s ").append(TIMEOUT_SCRIPT_ARG).append(" %s").toString();
    public static final String FAULT_NAME_SCRIPT_ARG = SCRIPT_ARGUEMENT_PREFIX + "faultname";
    public static final String DATABASE_CONNECTION_LEAK_INJECTION_COMMAND_WITH_ARGS2 =
            INFRA_SUBMIT + OPERATION_INJECT + FAULT_NAME_SCRIPT_ARG + " %s " + DB_NAME_SCRIPT_ARG + " %s "
                    + DB_USER_NAME_SCRIPT_ARG + " %s " + DB_PASSWORD_SCRIPT_ARG + " %s " + DB_PORT_SCRIPT_ARG + " %s "
                    + DB_SSL_ENABLED_SCRIPT_ARG + " %s " + TIMEOUT_SCRIPT_ARG + " %s";
    public static final String DATABASE_TRANSACTION_ERROR_INJECTION_COMMAND_WITH_ARGS2 =
            INFRA_SUBMIT + OPERATION_INJECT + FAULT_NAME_SCRIPT_ARG + " %s " + DB_NAME_SCRIPT_ARG + " %s "
                    + DB_USER_NAME_SCRIPT_ARG + " %s " + DB_PASSWORD_SCRIPT_ARG + " %s " + DB_PORT_SCRIPT_ARG + " %s "
                    + DB_TABLE_NAME_SCRIPT_ARG + " %s " + DB_ERROR_CODE_SCRIPT_ARG + " %s " + DB_PERCENTAGE_SCRIPT_ARG
                    + " %s " + DB_SSL_ENABLED_SCRIPT_ARG + " %s " + TIMEOUT_SCRIPT_ARG + " %s";
    public static final String DATABASE_TRANSACTION_LATENCY_INJECTION_COMMAND_WITH_ARGS2 =
            INFRA_SUBMIT + OPERATION_INJECT + FAULT_NAME_SCRIPT_ARG + " %s " + DB_NAME_SCRIPT_ARG + " %s "
                    + DB_USER_NAME_SCRIPT_ARG + " %s " + DB_PASSWORD_SCRIPT_ARG + " %s " + DB_PORT_SCRIPT_ARG + " %s "
                    + DB_TABLE_NAME_SCRIPT_ARG + " %s " + DB_LATENCY_SCRIPT_ARG + " %s " + DB_PERCENTAGE_SCRIPT_ARG
                    + " %s " + DB_SSL_ENABLED_SCRIPT_ARG + " %s " + TIMEOUT_SCRIPT_ARG + " %s";
    public static final String DATABASE_CONNECTION_LEAK_REMEDIATION_COMMAND_WITH_ARGS2 =
            INFRA_SUBMIT + OPERATION_REMEDIATE + DB_NAME_SCRIPT_ARG + " %s " + DB_USER_NAME_SCRIPT_ARG + " %s "
                    + DB_SSL_ENABLED_SCRIPT_ARG + " %s " + DB_PORT_SCRIPT_ARG + " %s";
    public static final String DATABASE_TRANSACTION_ERROR_REMEDIATION_COMMAND_WITH_ARGS2 =
            INFRA_SUBMIT + OPERATION_REMEDIATE + DB_NAME_SCRIPT_ARG + " %s " + DB_USER_NAME_SCRIPT_ARG + " %s "
                    + DB_PASSWORD_SCRIPT_ARG + " %s " + DB_PORT_SCRIPT_ARG + " %s " + DB_SSL_ENABLED_SCRIPT_ARG + " %s "
                    + DB_TABLE_NAME_SCRIPT_ARG + " %s";
    public static final String DATABASE_TRANSACTION_LATENCY_REMEDIATION_COMMAND_WITH_ARGS2 =
            INFRA_SUBMIT + OPERATION_REMEDIATE + DB_NAME_SCRIPT_ARG + " %s " + DB_USER_NAME_SCRIPT_ARG + " %s "
                    + DB_PASSWORD_SCRIPT_ARG + " %s " + DB_PORT_SCRIPT_ARG + " %s " + DB_SSL_ENABLED_SCRIPT_ARG + " %s "
                    + DB_TABLE_NAME_SCRIPT_ARG + " %s";
}