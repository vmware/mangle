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

package com.vmware.mangle.services.constants;

/**
 * Add Constants Fields.
 *
 * @author kumargautam
 */
public class CommonConstants {

    private CommonConstants() {
    }

    public static final String NO_RECORD_FOUND_FOR_ID = "Fault not found for given id ";
    public static final String NO_RECORD_FOUND_FOR_NAME = "Fault not found for given name ";
    public static final String ENDPOINTS_RESULT_FOUND = "Successfully got all the endpoints available";
    public static final String ENDPOINTS_UPDATED = "Successfully endpoint updated";
    public static final String ENDPOINTS_CREATED = "Successfully endpoint created";
    public static final String CREDENTIALS_RESULT_FOUND = "Successfully got all the credentials available";
    public static final String CREDENTIALS_CREATED = "Successfully credential created";
    public static final String CREDENTIALS_DELETED = "Successfully credentials deleted";
    public static final String ENDPOINTS_DELETED = "Successfully endpoints deleted";
    public static final String TASKS_DELETED = "Successfully deleted tasks";
    public static final String TEST_CONNECTION_SUCCESS = "Test Connection is success";
    public static final String REQUEST_FAILED_MESSAGE_HEADER = "mangle_request_error";
    public static final String MESSAGE_HEADER = "mangle_message";
    public static final String PF4J_PLUGINS_DIR = "pf4j.pluginsDir";
    public static final String PF4J_MODE = "pf4j.mode";
    public static final String DEFAULT_PLUGIN_ID = "mangle-default-plugin";
    public static final String DEFAULT_BLOCK_SIZE = "8192000";
    public static final String ARGUEMENT_PREFIX = "__";
    public static final String PROCESS_IDENTIFIER_ARG = ARGUEMENT_PREFIX + "processIdentifier";
    public static final String KILL_PROCESS_REMEDIATION_COMMAND_ARG = ARGUEMENT_PREFIX + "remediationCommand";
    public static final String OS_TYPE_ARG = ARGUEMENT_PREFIX + "osType";
    public static final String LOAD_ARG = ARGUEMENT_PREFIX + "load";
    public static final String BLOCKSIZE_ARG = ARGUEMENT_PREFIX + "blockSize";
    public static final String TARGET_DIRECTORY_ARG = ARGUEMENT_PREFIX + "targetDir";
}
