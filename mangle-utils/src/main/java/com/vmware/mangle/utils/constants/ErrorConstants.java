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
 * Insert your comment for ErrorConstants here
 *
 * @author kumargautam
 */
public class ErrorConstants {

    private ErrorConstants() {
    }

    public static final String NO_RECORD_FOUND = "Found No Search Results";
    public static final String NO_RECORD_FOUND_FOR_NAME = "Fault not found for given name ";
    public static final String ERROR_MSG = "Execution Failed!";
    public static final String MEDIA_TYPE_ERROR =
            " Media Type is not supported for this request. Supported Media Types are ";
    public static final String HTTP_METHOD_ERROR = " method is not supported for this request. Supported methods are ";
    public static final String FIELD_VALUE_EMPTY = " should not be empty or null";
    public static final String ENDPOINT_NAME = "EndpointName";
    public static final String ENDPOINT_TYPE = "EndPointType";
    public static final String ENDPOINT_ID = "EndpointId";
    public static final String TASK_ID = "TaskId";
    public static final String TASK_NAME = "TaskName";
    public static final String CREDENTIAL_NAME = "CredentialName";
    public static final String CERTIFICATES_NAME = "CertificatesName";
    public static final String ENDPOINT = "EndpointSpec";
    public static final String TASK = "Task";
    public static final String SCHEDULE = "SCHEDULE";
    public static final String CREDENTIALS_SPEC = "CredentialsSpec";
    public static final String CERTIFICATES_SPEC = "CertificatesSpec";
    public static final String AD_AUTHPROVIDER_ID = "ADAuthProviderDomain";
    public static final String AUTH_PROVIDER = "Authentication Provider";
    public static final String PRIVILEGE_NAME = "Privilege name";
    public static final String USER_NAME = "User name";
    public static final String USERNAME = "Username";
    public static final String USER = "User";
    public static final String USERAUTHENTICATION = "User authentication";
    public static final String ROLE = "Role";
    public static final String DEPLOYMENT_MODE = "deploymentMode";
    public static final String CLUSTER_QUORUM = "quorum";

    public static final String USER_ALREADY_EXISTS = "User %s already exists";
    public static final String TEST_CONNECTION_FAILED =
            "Test Connection failed for endpoint, Please reverify the credentials";
    public static final String TEST_CONNECTION_FAILED_METRICPROVIDER =
            "Test Connection failed for Metric Provider, Please verify connection properties";
    public static final String REQUEST_FAILED_MESSAGE_HEADER = "mangle_request_error";
    public static final String AUTHENTICATION_TEST_CONNECTION_FAILED =
            "Test Connection failed to authentication provider, Please reverify the details";
    public static final String DUPLICATE_RECORD = "Record already exists";
    public static final String DEFAULT_ROLE_DELETE_FAIL = "Default mangle roles cannot be deleted/modified";
    public static final String DEFAULT_MANGLE_USER_DELETE_FAIL = "Default mangle user cannot be deleted";
    public static final String ROLE_NOT_FOUND =
            "role not found, please create role first before assigning it to a user";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_DELETION_FAILED = "Failed to delete the following users: %s";
    public static final String AUTHENTICATION_SETUP =
            "Authentication for the local user does not exist, please configure before assigning the role";
    public static final String INVALID_DOMAIN_NAME = "Domain name information provided for the user is invalid";

    public static final String INVALID_STATE_FOR_REMEDIATION =
            "The state of Task is not valid for Remediation. Task Status should be Completed for remediation to start";
    public static final String EVENT_NOT_FOUND = "Event not found with id: %s";
    public static final String ROLE_DELETION_PRE_CONDITION_FAILURE =
            "Role could not be Deleted. Role has active associations with existing Users";
    public static final String DEFAULT_PLUGIN_ID_ERROR =
            "Deletion operation of the mangle default plugin is not allowed";
    public static final String CLUSTER_CONFIG_MISMATCH_VALIDATION_TOKEN =
            "Node spin up failed, mis-matching validation token with the one configured in db";
    public static final String CLUSTER_CONFIG_MISMATCH_DEPLOYMENT_MODE =
            "Node spin up failed, mis-matching deployment mode with the one configured in db";
    public static final String CLUSTER_CONFIG_LESSER_QUORUM =
            "Quorum could not be updated, below possible quorum value %s";
    public static final String HZ_STANDALONE_ALREADY_EXISTS =
            "Node spin up failed, cluster configured for standalone deployment already has a node %s running, change the deployment type to cluster to add more nodes";
    public static final String IS_SCHEDULED_TASK = "isScheduledTask";
    public static final String CREDENTIALS_DELETION_PRECHECK_FAIL =
            "Credentials delete operation failed, active association with the endpoints";
    public static final String ENDPOINTS_DELETION_PRECHECK_FAIL =
            "Endpoints delete operation failed, active association with the scheduled tasks";
    public static final String METRICPROVIDER_ID = "MetricProviderId";
    public static final String METRICPROVIDER_NAME = "MetricProviderName";
    public static final String METRICPROVIDER_TYPE = "MetricProviderType";
    public static final String NO_ACTIVE_METRIC_PROVIDER = "No Active metric provider found";
    public static final String ALREADY_ACTIVE_METRIC_PROVIDER = "Provided metric provider is already active";
    public static final String TASK_DELETION_PRECHECK_FAIL =
            "Task deletion operation failed, active schedules asscociated for the tasks";
    public static final String INPROGRESS_TASK_DELETION_FAILURE =
            "Task deletion operation failed, in progress task %s can not be deleted";
    public static final String NO_RECORD_FOUND_MSG = "No Record Found for";
    public static final String SAME_RECORD = "Metric Provider of same type is already exists.";
    public static final String LOCAL_USER_EMPTY_ROLE_UPDATE_FAIL =
            "Failed to update authorization for the user %s, at least one role should be defined for local user";
    public static final String USER_EMPTY_ROLE_CREATE_FAIL =
            "Failed to create authorization for the user %s, at least one role should be defined for User";
    public static final String UNEXPECTED_TASK_UPDATE_EVENT = "Received update on Task unknown to HazelCast";
    public static final String VCENTER_ADAPTER_CLIENT_UNREACHABLE = "Couldn't connect to VCenter adapter %s";
    public static final String VCENTER_AUTHENTICATION_FAILED = "Authentication to VCenter %s failed";
    public static final String CUSTOM_ROLE_CREATION_FAILED_NO_PRVILEGES =
            "Custom role creation should have atleast one privilege";

    public static final String PAUSE_CONTAINER_OPERATION_FAILED = "Pause container operation failed on the container";
    public static final String UNPAUSE_CONTAINER_OPERATION_FAILED =
            "UnPause container operation failed on the container";
    public static final String STOP_CONTAINER_OPERATION_FAILED = "Stop container operation failed on the container";
    public static final String START_CONTAINER_OPERATION_FAILED = "Start container operation failed on the container";
    public static final String CONTAINER_NOT_AVAILABLE = "Container not available or not running";
    public static final String DOCKER_CONNECTION_FAILURE =
            "Docker connection failure.The Host is down or docker daemon is not running";
    public static final String CONTAINER_IS_ALREADY_RUNNING = "Container is already running";

    public static final String COMMAND_EXECUTION_FAILURE_FOR_EXPECTED_OUTPUT =
            "Execution of Command does not contain: %s, failed with output: %s";
    public static final String FAULT_REMEDIATION_NOT_SUPPORTED = "%s does not support remediation";
    public static final String CLUSTER_VALIDATION_TOKEN_MISSING =
            "Node spin up failed, mandatory parameter validation token is not provided";
    public static final String PUBLIC_ADDRESS_MISSING =
            "Node spin up failed, mandatory parameter public address for the mangle instance is not provided";
    public static final String PUBLIC_ADDRESS_WRONG_FORMAT =
            "Node spin up failed, mandatory parameter public address provided is not valid ip4 address";
    public static final String PLUGIN_DETAILS = "PluginDetails";
    public static final String INVALID_QUERY_EXCEPTION = "Invalid query found";

    public static final String USER_ACCOUNT_LOCKED_ERROR_MSG = "User account is locked! Contact administrator or retry after 30 minutes";
    public static final String AUTHENTICATION_FAILED_ERROR_MSG = "Invalid username or password";

    // RemoteMAchine endpoint error constants

    public static final String RM_AUTH_FAIL = "Auth fail";
    public static final String RM_CONNECTION_REFUSED = "Connection refused";
    public static final String RM_NO_SUCH_FILE = "No such file";
    public static final String RM_DICRECTORY_PERMISSION_DENIED = "Permission denied";

    // K8S endpoint error constants

    public static final String K8S_SERVER_DOES_NOT_HAVE_RESOURCE_TYPE = "the server doesn't have a resource type";
    public static final String K8S_RESOURCE_NOT_FOUND = "Error from server (NotFound)";
    public static final String K8S_ERROR_LOADING_CONFIG_FILE = "Error loading config file";
    public static final String K8S_ERROR_CONNECTING_SERVER = "Unable to connect to the server";
    public static final String K8S_ERROR = "error";
    public static final String COMMAND_EXEC_EXIT_CODE_ERROR =
            "Execution of Command: %s failed. errorCode: %s output: %s";

    // Docker endpoint error constants

    public static final String DOCKER_BAD_CERTIFICATE = "bad_certificate";
    public static final String CURRENT_PWD_MISMATCH = "Wrong current password";
    public static final String DOCKER_HOST_NAME_NULL = "Host name may not be null";
    public static final String DOCKER_INVALID_ENDPOINT = "Invalid DockerEndpoint";

    //AWS endpoint error constants

    public static final String AWS_INVALID_CREDS = "AWS was not able to validate the provided access credentials";
    public static final String PRE_EXISTING_PLUGIN_FILE =
            "Plugin file with same name is present in Mangle. Either rename your file or Delete the existing file.";
    public static final String INTERNAL_SERVER_ERROR = "Internal server error";
    public static final String FAULT_REMEDIATION_NOT_SUPPORTED_FOR_KERNELPANICFAULT =
            "%s does not support remediation, please restart the target machine manually for remediation";


    //Hazelcast Cluster error constants
    public static final String CLUSTER_QUORUM_NOT_MET =
            "Operation Failed. Error: The host %s is part of a cluster that does not meet quorum.";
    public static final String CLUSTER_ALREADY_IN_STATE = "The cluster already in the requested state";
    public static final String CLUSTER_TYPE_CONFIG_LESSER_QUORUM =
            "Quorum could not be updated, cluster deployment mode cannot have quorum lesser than 2";

}
