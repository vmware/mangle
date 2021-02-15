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

/**
 * @author bkaranam
 *
 *
 */
public class Constants {
    private Constants() {
    }

    private static boolean schemaMigrated = false;
    private static boolean defaultPasswordResetStatus = false;
    public static final int NO_OF_RETRIES = 6;
    public static final int RETRY_WAIT_INTERVAL = 10;
    public static final int ONE_MINUTE_IN_MILLIS = 60000;
    public static final int RETRIGGER_THRESHOLD_TIME_IN_MINS = 30;
    public static final int DEFAULT_THREAD_POOL_SIZE = 5;
    public static final String DEFAULT_TASK_SCHEDULER_THREAD_POOL_NAME = "MangleThreadPoolTaskScheduler";

    // Unit in Seconds
    public static final int SSH_CONNECT_TIMEOUT = 500;
    // Unit in Seconds
    public static final int SSH_KEX_TIMEOUT = 100;

    public static final String MOCKDATA_FILE = "mangleMockData.properties";

    public static final String FAULTS = "/faults";
    public static final String DISK = FAULTS + "/disk";
    public static final String TASKS = "/tasks";
    public static final String INFO = "/info";

    public static final String REMEDIATE_DISK = DISK + "/remediate";
    public static final String TEMPORARY_DIRECTORY = System.getProperty("java.io.tmpdir");

    public static final String SET_JAVA_HOME_CMD = "export JAVA_HOME=";
    public static final String LINUX_FILE_EXIST_CHECK_COMMAND =
            "[ -f FILE_LOCATION ] && echo \"FILE_LOCATION exist\" || echo \"FILE_LOCATION does not exist\"";
    public static final String LINUX_TARGET_CONFIG_FILE_LOCATION = "/var/log/mangle.lck";
    public static final String LINUX_COMMAND_FOR_ASSIGN_EXECUTE_PERMISSION_RECURSIVELY = "chmod -R u+x %s";

    public static final String WINDOWS_IOBLAZER_APP_NAME = "IOBlazer.exe";
    public static final String PERSONA_IN_USE_STRING = "personaInUse";

    public static final String K8S_CONTAINER_OPTION = " -c ";
    public static final String KUBE_FAULT_EXEC_STRING = "exec -it %s" + K8S_CONTAINER_OPTION + "%s -- %s";

    public static final String K8S_SHELL_COMMAND_ARGUMENT = "-c \"";

    public static final String SUCESSFUL_FAULT_CREATION_MESSAGE = "Created Fault Successfully";
    public static final String LOCAL_DOMAIN_NAME = "mangle.local";
    public static final String FIAACO_CMD_ARG_EXPRESSION = "$FI_ARG_";
    public static final String FIAACO_CMD_ADD_INFO_EXPRESSION = "$FI_ADD_INFO_";
    public static final CharSequence FIAACO_CMD_STACK_EXPRESSION = "$FI_STACK";
    public static final int COMMAND_EXECUTION_RETRY_INTERVAL = 1;

    public static final String DEFAULT_DATE_FORMAT = "dd MMM yyyy HH:mm:ss ZZZ";
    public static final String CURRENT_DATE_FORMAT = "dd MMM yyyy HH:mm:ss";
    public static final String GMT_DATE_FORMAT = "dd MMM yyyy HH:mm:ss z";
    public static final String DATE_FORMAT_HIPHEN_SEPERATED = "yyyy-MM-dd-HH-mm-ss-SSS";
    public static final String GMT = "GMT";
    public static final String HTTPS = "https://";

    public static final String DATADOG_API_KEY = "api_key=";
    public static final String DATADOG_APPLICATION_KEY = "application_key=";
    public static final String AND_OPERATOR = "&";
    public static final String SH_COMMAND_PREFIX = "/bin/sh ";
    public static final String CONTAINER_PAUSE_SUCCESS_MESSAGE = "The container is paused successfully";
    public static final String CONTAINER_UNPAUSE_SUCCESS_MESSAGE = "The container is unpaused successfully";
    public static final String CONTAINER_START_SUCCESS_MESSAGE = "The container is started successfully";
    public static final String MANGLE_DEFAULT_USER = "admin@mangle.local";
    public static final String NODESTATUS_TASK_NAME = "NodeStatusTask";

    public static final String TASK_SIZE = "taskSize";
    public static final String TASK_LIST = "taskList";
    public static final String VM_NAME_ARG = "--vmName";
    public static final String HOST_NAME_ARG = "--hostName";
    public static final String VM_DISK_ARG = "--vmDisk";
    public static final String VM_NIC_ARG = "--vmNic";

    public static boolean isSchemaMigrated() {
        return schemaMigrated;
    }

    public static void setDefaultPasswordResetStatus(boolean status) {
        defaultPasswordResetStatus = status;
    }

    public static boolean isDefaultPasswordResetStatus() {
        return defaultPasswordResetStatus;
    }

    public static void setSchemaMigrated(boolean schemaMigrated) {
        Constants.schemaMigrated = schemaMigrated;
    }
}
