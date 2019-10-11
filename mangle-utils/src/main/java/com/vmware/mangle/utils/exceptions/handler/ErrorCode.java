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

package com.vmware.mangle.utils.exceptions.handler;

/**
 * Insert your comment for ErrorCode here
 *
 * @author kumargautam
 */
public enum ErrorCode {
    UNKNOWN_ERROR("FI500"),
    METHOD_NOT_ALLOWED("FI501"),
    UNSUPPORTED_MEDIA_TYPE("FI502"),
    METHOD_ARGUMENT_TYPE_MISMATCH("FI503"),
    BAD_REQUEST("FI504"),
    GENERIC_ERROR("FI505"),
    DB_ERROR("FI506"),
    NO_RECORD_FOUND("FI0001"),
    FIELD_VALUE_EMPTY("FI0002"),
    SUPPORT_SCRIPT_FILE_NOT_FOUND("FI0003"),
    TEST_CONNECTION_FAILED("FI0004"),
    FILE_SIZE_EXCEEDED("FI0005"),
    READING_FILE_FAILED("FI0006"),
    READ_FILE_AS_STRING_FAILED("FI00001"),
    SCORE_EMAIL_SENT_FAILED("FI00002"),
    DEFAULT_ROLE_DELETE("FI0009"),
    TASK_EXECUTION_FAILED("FI0010"),
    TASK_NOT_INTIALIAZED("FI0011"),
    TASK_NOT_BELONGS_TO_RUNNER("FI0012"),
    TASK_NOT_BEEN_STARTED("FI0013"),
    TASK_REMOVAL_FAILED("FI0014"),
    COMMAND_EXEC_EXIT_CODE_ERROR("FI0015"),
    COMMAND_EXEC_OUTPUT_ERROR("FI0016"),
    REMEDIATION_NOT_INJ_TASK_FAILURE("FI0017"),
    INVALID_STATE_FOR_REMEDIATION("FI0018"),
    USER_ALREADY_EXISTS("FI0019"),
    AUTH_TEST_CONNECTION_FAILED("FI0020"),
    DUPLICATE_RECORD("FI0021"),
    ROLE_NOT_FOUND("FI0022"),
    USER_NOT_FOUND("FI0023"),
    AUTHENTICATION_SETUP("FI0024"),
    INVALID_DOMAIN_NAME("FI0025"),
    PLUGIN_OPERATION_ERROR("FI0026"),
    INVALID_CHAR_IN_FILE_NAME("FI0027"),
    FILE_NAME_NOT_EXIST("FI0028"),
    PLUGIN_NAME_NOT_LOADED("FI0029"),
    INVALID_K8S_RESOURCE_TYPES("FI0030"),
    PROVIDED_INVALID_RESOURCE_LABELS("FI0031"),
    RETRY_LOGICS_FAILED("FI0032"),
    CLASS_NOT_FOUND("FI0033"),
    TASK_FINDER_FAILURE("FI0034"),
    CRON_JOB_SCHEDULE_FAILURE("FI0035"),
    SIMPLE_JOB_SCHEDULE_FAILURE("FI0036"),
    JOB_NOT_ACTIVE("FI0037"),
    NO_ACTIVE_JOBS("FI0038"),
    MISFIRED_JOB_TRIGGER_FAILURE("FI0039"),
    CONCURRENT_TASK_EXECUTION_FAILURE("FI0040"),
    DISK_SPACE_FAULT_TASK_INPUT_VALIDATION_FAILURE("FI0041"),
    SUCCESSFULY_TASK_CREATED("FI0042"),
    FAILED_TO_CREATE_REMEDIATION_TASK("FI0043"),
    NO_TASK_FOUND("FI0044"),
    NOT_A_INJECTION_TASK("FI0045"),
    FAULT_ALREADY_REMEDIATED("FI0046"),
    DISK_SPACE_FAULT_DESCRIPTION("FI0047"),
    DISK_SPACE_FAULT_REMEDIATION_DESCRIPTION("FI0048"),
    DISK_IO_FAULT_DESCRIPTION("FI0049"),
    DISK_IO_FAULT_REMEDIATION_DESCRIPTION("FI0050"),
    FAULT_REMEDIATION_NOT_SUPPORTED("FI0051"),
    AGENT_EXTRACTION_FAILED("FI0052"),
    UNSUPPORTED_BYTEMAN_FAULT("FI0053"),
    REMEDIATION_FAILURE_WITH_EXCEPTION("FI0054"),
    NO_PODS_IDENTIFIED("FI0055"),
    IO_EXCEPTION("FI0056"),
    ENTITY_NOT_FOUND("FI0057"),
    INVALID_URL("FI0058"),
    UNSUPPORTED_ENDPOINT("FI0059"),
    INVALID_RESOURCE_LABELS("FI0060"),
    UNSUPPORTED_K8S_RESOURCE_TYPE("FI0061"),
    EXTENSION_NOT_FOUND("FI0062"),
    MISSING_REFERENCE_VALUES("FI0063"),
    FAULT_NAME_NOT_NULL("FI0064"),
    UNSUPPORTED_FAULT("FI0065"),
    INJECTION_SCRIPT_COPY_FAILED("FI0066"),
    ROLE_DELETION_PRE_CONDITION_FAILURE("FI0067"),
    CREATE_FILE_OPERATION_FAILURE("FI0068"),
    CLUSTER_CONFIG_MISMATCH_VALIDATION_TOKEN("FI0069"),
    CLUSTER_CONFIG_MEMBER_MODIFICATION("FI0070"),
    MISSING_COMMANDS("FI0072"),
    COMMAND_OUTPUT_PROCESSING_ERROR("FI0074"),
    MANGLE_IN_MAINTENANCE_MODE_FAILED("FI0075"),
    DIRECTORY_NOT_FOUND("FI0076"),
    FILE_TRANSFER_ERROR("FI0077"),
    DUPLICATE_RECORD_FOR_ENDPOINT("FI0078"),
    INVALID_CRON_EXPRESSION("FI0079"),
    INVALID_SCHEDULE_INPUTS("FI0080"),
    LOCAL_USER_EMPTY_ROLE_UPDATE_FAIL("FI0081"),
    USER_EMPTY_ROLE_CREATE_FAIL("FI0082"),
    DELETE_OPERATION_FAILED("FI0083"),
    VCENTER_ADAPTER_CLIENT_UNREACHABLE("FI0084"),
    CUSTOM_ROLE_CREATION_FAILED_NO_PRVILEGES("FI0085"),
    PAUSE_CONTAINER_OPERATION_FAILED("FI0086"),
    UNPAUSE_CONTAINER_OPERATION_FAILED("FI0087"),
    STOP_CONTAINER_OPERATION_FAILED("FI0088"),
    START_CONTAINER_OPERATION_FAILED("FI0089"),
    CONTAINER_NOT_AVAILABLE("FI0090"),
    DUPLICATE_RECORD_FOR_CREDENTIAL("FI0091"),
    CREDENTIAL_NAME_NOT_VALID("FI0092"),
    METER_REGISTERY_FAILED("FI0093"),
    SAME_RECORD("FI0094"),
    NO_ACTIVE_METRIC_PROVIDER("FI0095"),
    CURRENT_USER_DELETION_FAILED("FI0096"),
    REMEDIATION_K8S_TASK("FI0097"),
    DOCKER_CONNECTION_FAILURE("FI0098"),
    CERTIFICATES_NAME_NOT_VALID("FI0099"),
    DUPLICATE_RECORD_FOR_ENDPOINT_CERTIFICATES("FI0100"),
    COMMAND_EXEC_ERROR_WITH_KNOWN_FAILURE("FI0101"),
    CURRENT_PASSWORD_MISMATCH("FI0102"),
    DEFAULT_MANGLE_USER_DELETE_FAIL("FI0103"),
    SCHEDULED_JOBIDS_NOT_FOUND("FI0104"),
    NOT_FOUND_AND_INVALID_STATE_SCHEDULED_JOBIDS("FI0105"),
    INVALID_STATE_SCHEDULED_JOBIDS("FI0106"),
    K8S_SPECIFIC_ARGUMENTS_REQUIRED("FI0107"),
    DOCKER_SPECIFIC_ARGUMENTS_REQUIRED("FI0108"),
    CLUSTER_MANDATORY_PARAMETER_NOT_PROVIDED("FI0109"),
    CLUSTER_MANDATORY_PARAMETER_ERROR("FI0110"),
    ALREADY_ACTIVE_METRIC_PROVIDER("FI0111"),
    UNABLE_TO_CONNECT_TO_WAVEFRONT_INSTANCE("FI0112"),
    UNABLE_TO_CONNECT_TO_DATADOG_INSTANCE("FI0113"),
    AUTH_FAILURE_TO_DATADOG("FI0114"),
    AUTH_FAILURE_TO_WAVEFRONT("FI0115"),
    PROVIDE_CONNECTION_PROPERTIES_FOR_ENDPOINT("FI0116"),
    CONTAINER_IS_ALREADY_RUNNING("FI0117"),
    CANNOT_RERUN_FAULT("FI0118"),
    JSON_KEY_NOT_FOUND("FI0119"),
    MALFORMED_PLUGIN_DESCRIPTOR("FI0120"),

    FAULT_NAME_NOT_FOUND_IN_PLUGIN_DESCRIPTOR("FI0121"),
    PLUGIN_ID_NOT_LOADED("FI0122"),
    INCORRECT_NETWORK_FAULT_TYPE("FI0123"),
    LATENCY_REQUIRED_FOR_NETWORK_LATENCY_FAULT("FI0124"),
    PERCENTAGE_REQUIRED_FOR_NETWORK_PACKET_RELATED_FAULT("FI0125"),
    PRE_EXISTING_PLUGIN_FILE("FI0126"),
    MALFORMED_PLUGIN_PROPERTIES("FI0127"),
    CUSTOM_FAULT_RE_RUN_FAILURE_DUE_TO_PLUGIN_STATE("FI0128"),
    CUSTOM_FAULT_EXECUTION_FAILURE_DUE_TO_PLUGIN_STATE("FI0129"),
    INPROGRESS_TASK_DELETION_FAILURE("FI0130"),
    DUPLICATE_EXTENSIONS_FOUND("FI0131"),
    LOGIN_EXCEPTION("FI0132"),

    //RemoteMachine endpoint error codes
    RM_CONNECTION_REFUSED("FIRM01"),
    RM_INVALID_CREDENTIALS("FIRM02"),
    RM_SFTP_NOT_ENABLED("FIRM03"),
    RM_CONNECTION_EXCEPTION("FIRM04"),
    RM_CREDENTIAL_WITH_NEITHER_PASSWORD_NOR_PRIVATEKEY("FIRM05"),
    RM_DIRECTORY_PERMISSION_DENIED("FIRM06"),
    RM_INVALID_PRIVATEKEY("FIRM07"),
    //K8S endpoint error codes
    K8S_ERROR_FROM_SERVER("FIK8S01"),
    K8S_INVALID_CONFIG_FILE("FIK8S02"),
    K8S_NAMESPACE_NOT_FOUND("FIK8S03"),
    K8S_TEST_CONNECTION_ERROR("FIK8S04"),
    //Docker endpoint error codes
    DOCKER_INVALID_CERTIFICATE("FIDOCKER01"),
    DOCKER_INVALID_PRIVATEKEY("FIDOCKER02"),
    DOCKER_TLS_ENABLED_ERROR("FIDOCKER03"),
    DOCKER_TLS_VERIFY_ENABLED_ERROR("FIDOCKER04"),
    DOCKER_BAD_CERTIFICATE_ERROR("FIDOCKER05"),
    DOCKER_INVALID_HOSTNAME_OR_PORT("FIDOCKER06"),
    DOCKER_INVALID_ENDPOINT("FIDOCKER07"),
    //VCENTER endpoint error codes
    VCENTER_AUTHENTICATION_FAILED("FIVC001"),

    //AWS endpoint error codes
    AWS_UNKNOWN_ERROR("FIAWS01"),
    AWS_INVALID_CREDENTIALS("FIAWS02"),
    AWS_INVALID_REGION("FIAWS03"),
    AWS_INSTANCE_NOT_FOUND("FIAWS04"),
    AWS_FAILED_TO_DELETE_SECURITY_GROUP("FIAWS05"),
    AWS_SECURITY_GROUP_NOT_FOUND("FIAWS06"),
    AWS_NO_INSTANCES_FOUND("FIAWS07"),
    AWS_NO_RUNNING_INSTANCES_FOUND("FIAWS08"),

    HZ_STANDALONE_ALREADY_EXISTS("FIHZ001"),
    CLUSTER_CONFIG_MISMATCH_DEPLOYMENT_TYPE("FIHZ002"),
    CLUSTER_CONFIG_LESSER_QUORUM("FIHZ003"),
    CLUSTER_QUORUM_NOT_MET("FIHZ004"),
    CLUSTER_ALREADY_IN_STATE("FIHZ005"),
    CLUSTER_TYPE_CONFIG_LESSER_QUORUM("FIHZ006");
    private final String code;

    private ErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}