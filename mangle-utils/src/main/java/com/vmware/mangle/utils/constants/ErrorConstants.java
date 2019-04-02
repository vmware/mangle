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
    public static final String CREDENTIAL_NAME = "CredentialName";
    public static final String ENDPOINT = "EndpointSpec";
    public static final String TASK = "Task";
    public static final String CREDENTIALS_SPEC = "CredentialsSpec";
    public static final String AD_AUTHPROVIDER_ID = "ADAuthProviderDomain";
    public static final String PRIVILEGE_NAME = "Privilege name";
    public static final String USER_NAME = "User name";
    public static final String USERNAME = "Username";
    public static final String USERAUTHENTICATION = "User authentication";

    public static final String TEST_CONNECTION_FAILED =
            "Test Connection failed for endpoint, Please reverify the credentials";
    public static final String TEST_CONNECTION_FAILED_METRICPROVIDER =
            "Test Connection failed for Metric Provider, Please reverify";
    public static final String REQUEST_FAILED_MESSAGE_HEADER = "mangle_request_error";
    public static final String AUTHENTICATION_TEST_CONNECTION_FAILED =
            "Test Connection failed to authentication provider, Please reverify the details";
    public static final String DUPLICATE_RECORD = "Record already exists";
    public static final String DEFAULT_ROLE_DELETE = "Default mangle roles cannot be deleted/modified";
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
    public static final String CLUSTER_CONFIG_MISMATCH =
            "Node spin up failed, mis-matching validation token with the one configured in db, provided validation token: %s";
    public static final String IS_SCHEDULED_TASK = "isScheduledTask";
    public static final String CREDENTIALS_DELETION_PRECHECK_FAIL =
            "Credentials delete operation failed, active association with the endpoints";
    public static final String ENDPOINTS_DELETION_PRECHECK_FAIL =
            "Endpoints delete operation failed, active association with the scheduled tasks";
    public static final String METRICPROVIDER_ID = "MetricProviderId";
    public static final String METRICPROVIDER_NAME = "MetricProviderName";
    public static final String METRICPROVIDER_TYPE = "MetricProviderType";
    public static final String METRICPROVIDER_ACTIVE = "ActiveMetricProvider";
    public static final String TASK_DELETION_PRECHECK_FAIL =
            "Task deletion operation failed, active schedules asscociated for the tasks";
    public static final String NO_RECORD_FOUND_MSG = "No Record Found for";
    public static final String LOCAL_USER_EMPTY_ROLE_UPDATE_FAIL = "Failed to update authorization for the user %s, at least one role should be defined for local user";
    public static final String USER_EMPTY_ROLE_CREATE_FAIL = "Failed to create authorization for the user %s, at least one role should be defined for User";
}
