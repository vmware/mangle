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

    // TIMEOUT_CONSTANTS
    public static final int TIMEOUT_SEC_VERY_LONG = 10800;
    public static final int TIMEOUT_SEC_LONG = 3600;
    public static final int TIMEOUT_SEC_SHORT = 300;
    public static final int NO_OF_RETRIES = 6;
    public static final int RETRY_COUNT = 10;

    // Unit in Seconds
    public static final int SSH_CONNECT_TIMEOUT = 500;
    // Unit in Seconds
    public static final int SSH_KEX_TIMEOUT = 100;
    // Unit in Seconds
    public static final int DELAY_AFTER_DEPLOYMENT = 300;
    public static final boolean RC_TRUE = true;
    public static final boolean RC_FALSE = false;
    public static final String AD_HOST_PARAMETER = "host";
    public static final String AD_PORT_PARAMETER = "port";
    public static final String AD_BASE_PARAMETER = "ldapBase";
    public static final String AD_SSL_PARAMETER = "useSSL";
    public static final String AD_DOMAIN_PARAMETER = "defaultDomain";
    public static final String AD_SESSION_PARAMETER = "useSharedSession";
    public static final String AD_USERNAME_PARAMETER = "sharedUserName";
    public static final String AD_PASSWORD_PARAMETER = "sharedUserPassword";
    public static final String AD_CERTIFICATE_CONFIRMATION_PARAMETER = "confirmSslCertificateImport";

    public enum VmPowerState {
        poweredOn, poweredOff, suspended
    }

    // Exception Reason Strings
    public static final String SSH_FAILURE_STRING =
            "Reason: Check if SSH is enabled and Correct SSH credentials are provided";

    public static final String LOG_LINE_DECORATOR = "#####################################################";
    public static final String NAME_VALIDATION_FAILURE = "Should Not Contain '/, :' Characters";
    public static final String FAULTSET_NAME_VALIDATION_FAILURE = "Fault Name " + NAME_VALIDATION_FAILURE;

    public static final String TESTBED_NAME_VALIDATION_FAILURE = "TestBed Name " + NAME_VALIDATION_FAILURE;

    public static final String SIMIANARMY_CLIENTCONFIG_NAME_VALIDATION_FAILURE =
            "Simian Client Configuration Name " + NAME_VALIDATION_FAILURE;
    public static final String MCM_AWS_ACCOUNTNAME_VALIDATION_FAILURE = "AWS account name " + NAME_VALIDATION_FAILURE;
    public static final String BUILD_FILE_TYPE_OVF = "ovf";
    public static final String BUILD_FILE_TYPE_OVA = "ova";

    public static final String HTTP_STATUS_200_OK = "200";
    public static final String HTTP_STATUS_500 = "500";
    public static final String HTTP_STATUS_204_NO_CONTENT = "204";
    public static final String HTTP_STATUS_201_CREATED = "201";
    public static final String HTTP_STATUS_412_PRECONDITIONFAILED = "412";
    public static final String HTTP_STATUS_405_METHOD_NOT_ALLOWED = "405";
    public static final String HTTP_STATUS_400_BAD_REQUEST = "400";
    public static final String HTTP_STATUS_403 = "403";

    public static final String LINUX_STRING = "linux";
    public static final String WINDOWS_STRING = "windows";
    public static final String KUBERNETES_STRING = "kubernetes";

    // Mangle URL constants for Testing purpose
    public static final String FAULTSET_SERVICE = "/faultsets/";
    public static final String TESTBED_SERVICE = "/testbeds";
    public static final String TRIGGER_SERVICE = "/executor/task";
    public static final String ALLUREREPORT_SERVICE = "/allure/reports/info/";
    public static final String TASK_SERVICE = "/tasks";
    public static final String SUCCESSSTRING = "Success";
    public static final String FAILURESTRING = "Failed";
    public static final String SUCCESSFUL_STATE = "SUCCESSFUL";

    public static final String MOCKDATA_FILE = "mangleMockData.properties";
    public static final String ALLURE_REPORT_DTO_MOCKDATA = "mockdata/AllureReportDtoMockData.properties";
    public static final String FAULT_MOCKDATA_FILE = "mockdata/FaultMockData.properties";
    public static final String VRA_NETWORK_LATENCY = "vraNetworkLatencyTests.properties";
    public static final String TESTMACHINE_MOCKDATA_FILE = "mockdata/TestMachine.properties";
    public static final String COBERTURA_FLUSH = "/cobertura/flush";
    public static final String MAIN_THREAD_NAME = "main";
    public static final String COMMON_WORKFLOW_PROPERTY_FILE_NAME = "CommonWorkflow.properties";

    public static final String FAULTS = "/faults";
    public static final String DISK = FAULTS + "/disk";
    public static final String DISK_FAULTS = DISK + "/space";
    public static final String DISK_IO_FAULTS = DISK + "/stressIO";
    public static final String TASKS = "/tasks";
    public static final String INFO = "/info";

    public static final String REMEDIATE_DISK = DISK + "/remediate";

    public static final String SCHEDULER = "/scheduler";
    public static final String SCHEDULE_TASK = SCHEDULER + "/schedule";
    public static final String CANCEL_SCHEDULED_TASKS = SCHEDULER + "/cancel/";
    public static final String CHEDULABLE_TASKS = SCHEDULER + "/schedulableJobs";

    public static final String DISKTASK_PAUSED_STRING = "mangleDiskTaskPaused";

    public static final String MCM = "/mcm";
    public static final String ADD_AWS_ACCOUNT = MCM + "/awsAccounts";
    public static final String TERMINATE_SERVICE_URL = FAULTS + MCM + "/terminateService";

    public static final String LINUX_COMMAND_FOR_DISK_IO_FAULT = "%s" + "/" + "%s -F 1 -t %s -d %s";

    public static final String WINDOWS_COMMAND_FOR_DISK_IO_FAULT = "%s" + "\\" + "%s -F 1 -t %s -d %s";
    public static final String TEMPORARY_DIRECTORY = System.getProperty("java.io.tmpdir");

    public static final String SET_JAVA_HOME_CMD = "export JAVA_HOME=";
    public static final String LINUX_FILE_EXIST_CHECK_COMMAND =
            "[ -f FILE_LOCATION ] && echo \"FILE_LOCATION exist\" || echo \"FILE_LOCATION does not exist\"";
    public static final String LINUX_FILE_REMOVE_COMMAND = "rm -rf %s";
    public static final String LINUX_TARGET_CONFIG_FILE_LOCATION = "/var/log/mangle.lck";
    public static final String LINUX_FILE_TOUCH_COMMAND = "touch %s";

    public static final String LINUX_COMMAND_FOR_ASSIGN_EXECUTE_PERMISSION = "chmod u+x %s";

    public static final String LINUX_COMMAND_FOR_ASSIGN_EXECUTE_PERMISSION_RECURSIVELY = "chmod -R u+x %s";

    public static final String LINUX_COMMAND_EXECUTION_PROCESS_IDS =
            "ps -ef | grep -i \"%s\" | grep -v grep |awk '{print $2}'";

    public static final String LINUX_IOBLAZER_APP_NAME = "ioblazer-linux-x64";

    public static final String LINUX_COMMAND_KILL_EXECUTING_PROCESS =
            "ps -ef | grep -i \"%s\" | grep -v grep |awk '{print $2}' | xargs kill -9";
    public static final String PROCESS_KILL_COMMAND_USING_PGREP = " pgrep -f \"%s\" | xargs kill -9";

    public static final String WINDOWS_COMMAND_KILL_EXECUTING_PROCESS = "Taskkill /IM %s /F";

    public static final String WINDOWS_IOBLAZER_APP_NAME = "IOBlazer.exe";
    public static final String PERSONA_IN_USE_STRING = "personaInUse";

    public static final String K8S_CONTAINER_OPTION = " -c ";
    public static final String KUBE_FAULT_EXEC_STRING = "exec -it %s" + K8S_CONTAINER_OPTION + "%s -- %s";

    public static final String PREINJECTIONTITLE = "PREINJECTION";
    public static final String POSTINJECTIONTITLE = "POSTINJECTION";
    public static final String KEY_CLUSTER = "cluster";
    public static final String KEY_SUBSERVICE = "sub_service";
    public static final String KEY_CONTAINER = "containerName";
    public static final String KEY_FAULTTYPE = "faultType";
    public static final String KEY_SERVICE = "service";
    public static final String RESILIENCY_SCORE_METRIC_NAME = "mangle.metrics.resiliencyscore";
    public static final String JVM_AGENT_JAR = "byteman-download-3.0.10";
    public static final String SUCESSFUL_FAULT_CREATION_MESSAGE = "Created Fault Successfully";
    public static final String LOCAL_DOMAIN_NAME = "mangle.local";
    public static final String FIAACO_CMD_ARG_EXPRESSION = "$FI_ARG_";
    public static final String FIAACO_CMD_ADD_INFO_EXPRESSION = "$FI_ADD_INFO_";
    public static final CharSequence FIAACO_CMD_STACK_EXPRESSION = "$FI_STACK";
    public static final int COMMAND_EXECUTION_RETRY_INTERVAL = 1;

    public static final String DEFAULT_DATE_FORMAT = "dd MMM yyyy HH:mm:ss ZZZ";
    public static final String HTTPS = "https://";

    public static final String DATADOG_API_KEY = "api_key=";
    public static final String DATADOG_APPLICATION_KEY = "application_key=";
    public static final String DATADOG_VALIDATE_CONNECTION_API = "/api/v1/validate";
    public static final String AND_OPERATOR = "&";
    public static final String SH_COMMAND_PREFIX = "/bin/sh ";
}
