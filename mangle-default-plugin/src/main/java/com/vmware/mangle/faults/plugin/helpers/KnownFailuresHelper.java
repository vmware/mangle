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

import static com.vmware.mangle.utils.constants.FaultConstants.EXPECTED_MESSAGE_FOR_KILL_OPERATION_NOT_PERMITTED;
import static com.vmware.mangle.utils.constants.FaultConstants.MESSAGE_THROWN_FOR_KILL_OPERATION_NOT_PERMITTED;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_COPY_SUPPORT_SCRIPT_FILE_COPY_FAILURE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_FAULT_CONCURRENT_INJECTION_FAILURE_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_FAULT_CONCURRENT_INJECTION_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_FILES_ARE_MISSING_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_AGENT_INITIALIZATION_FAILURE_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_AGENT_INITIALIZATION_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_CONCURRENT_PORT_USAGE_FAILURE_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_CONCURRENT_PORT_USAGE_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_EXPECTED_MESSAGE_FOR_AGENT_INSTALL_SCRIPT_NOT_FOUND_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_GIVEN_AGENT_PORT_IN_USE_FAILURE_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_GIVEN_AGENT_PORT_IN_USE_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_INVALID_JAVAHOME_FAILURE_2_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_INVALID_JAVAHOME_FAILURE_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_INVALID_JAVAHOME_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_INVALID_PATHS_FAILURE_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_INVALID_PATHS_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_INVALID_PID_FAILURE_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_INVALID_PID_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_JAVA_HOME_NOT_AVAILABLE_FAILURE_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_JAVA_HOME_NOT_AVAILABLE_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_NO_SUCH_PID_FAILURE_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_NO_SUCH_PID_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_SOCKET_CONNECTION_TIMEOUT_FAILURE_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_SOCKET_CONNECTION_TIMEOUT_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_SUDO_NOT_AVAILABLE_FAILURE_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_SUDO_NOT_AVAILABLE_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_UNKNOWN_USER_FAILURE_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_UNKNOWN_USER_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_UNSUPPORTED_PROCESS_FAILURE_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_UNSUPPORTED_PROCESS_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_USER_WITHOUT_PERMISSION_FAILURE_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_INSTALL_USER_WITHOUT_PERMISSION_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_NOT_AVAILABLE_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_SUBMIT_EXPECTED_MESSAGE_FOR_AGENT_SUBMIT_SCRIPT_NOT_FOUND_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_SUBMIT_INVALID_JAVAHOME_FAILURE_2_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_SUBMIT_INVALID_JAVAHOME_FAILURE_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_SUBMIT_INVALID_JAVAHOME_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.AGENT_SUBMIT_JAVA_NET_CONNECT_EXCEPTION_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.DISKIO_FAULT_IOSIZE_GREATER_THAN_DISK_SIZE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.DISKIO_FAULT_TARGET_DIRECTORY_DOESNT_HAVE_PERMISSION_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.DISKIO_FAULT_TARGET_DIRECTORY_NOT_FOUND_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.DISKIO_FAULT_TARGET_DIRECTORY_NOT_FOUND_OUTPUT_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.DISK_SPACE_FILL_PERCENTAGE_GREATER_THAN_USED_DISK_PERCENTAGE_FAILURE_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.DISK_SPACE_FILL_PERCENTAGE_GREATER_THAN_USED_DISK_PERCENTAGE_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.DISK_SPACE_NOT_FOUND_DIRECTORY_PATH_FAILURE_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.DISK_SPACE_NOT_FOUND_DIRECTORY_PATH_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.DOCKER_CONNECTION_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.DOCKER_CONTAINER_IS_ALREADY_PAUSED_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.DOCKER_CONTAINER_IS_ALREADY_PAUSED_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.DOCKER_CONTAINER_IS_ALREADY_RUNNING_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.DOCKER_CONTAINER_IS_NOT_PAUSED_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.DOCKER_CONTAINER_IS_NOT_PAUSED_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.DOCKER_CONTAINER_IS_NOT_RUNNING_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.DOCKER_CONTAINER_IS_NOT_RUNNING_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.DOCKER_CONTAINER_NOT_AVAILABLE_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.DOCKER_CONTAINER_START_FAILURE_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.DOCKER_CONTAINER_START_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.DOCKER_STOP_CONTAINER_ON_PAUSED_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.DOCKER_STOP_CONTAINER_ON_PAUSED_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.EXPECTED_MESSAGE_FOR_FILE_NOT_FOUND;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.FAULT_ALREADY_REMEDIATED;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.K8S_INVALID_POD_CONTAINER_MAPPING_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.K8S_POD_TAR_NOT_AVAILABLE_AGENT_COPY_FAILURE_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.K8S_POD_TAR_NOT_AVAILABLE_AGENT_COPY_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.KILL_PROCESS_FOUND_MORE_THAN_ONE_PROCESS_FAILURE_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.KILL_PROCESS_FOUND_MORE_THAN_ONE_PROCESS_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.KILL_PROCESS_NOT_FOUND_IDENTIFIER_FAILURE_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.KILL_PROCESS_NOT_FOUND_IDENTIFIER_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.MEMORY_FAULT_CURRENT_MEMORY_USAGE_GREATER_THAN_REQUESTED_MEMORY_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.MEMORY_FAULT_CURRENT_MEMORY_USAGE_GREATER_THAN_REQUESTED_MEMORY_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.NETWORK_FAULT_INVALID_NIC_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.NETWORK_FAULT_INVALID_NIC_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.NETWORK_FAULT_REMEDIATION_FAIL_SOCKET_NOT_ESTABLISHED_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.NETWORK_FAULT_REMEDIATION_FAIL_SOCKET_NOT_ESTABLISHED_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.SYSTEM_RESOURCE_FAULT_ALREADY_REMEDIATED_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.SYSTEM_RESOURCE_FAULT_ALREADY_RUNNING_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.SYSTEM_RESOURCE_FAULT_DIRECTORY_NOT_FOUND_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.SYSTEM_RESOURCE_FAULT_PARALLEL_EXECUTION_NOT_SUPPORTED_MESSAGE;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.SYSTEM_RESOURCE_FAULT_PRECHECK_FAILED_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.SYSTEM_RESOURCE_SHELL_SCRIPT_FILE_NOT_FOUND_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.VCENTER_ADAPTER_CONNECTION_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.VCENTER_DISCONNECTED_DISK_NIC_ALREADY_INJECTED_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.VCENTER_DISCONNECT_DISK_NIC_FAULT_ALREADY_REMEDIATED_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.VCENTER_NIC_POWERED_OFF_VM_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.VCENTER_POWER_OFF_ALREADY_INJECTED_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.VCENTER_RESET_POWERED_OFF_VM_ALREADY_INJECTED_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.VCENTER_SERVER_CONNECTION_FAILURE_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.VCENTER_STATE_FAULT_ALREADY_REMEDIATED_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.VCENTER_SUSPENDED_ALREADY_INJECTED_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.VCENTER_SUSPEND_POWERED_OFF_VM_ALREADY_INJECTED_OUTPUT;
import static com.vmware.mangle.utils.constants.KnownFailureConstants.VCENTER_VM_NOT_FOUND_OUTPUT;

import java.util.HashMap;
import java.util.Map;

import lombok.experimental.UtilityClass;

/**
 * @author hkilari
 *
 */
@UtilityClass
public class KnownFailuresHelper {

    public Map<String, String> getKnownFailuresOfAgentInstallationRequest() {
        Map<String, String> knownFailuresforAgentInstallation = new HashMap<>();
        knownFailuresforAgentInstallation.put(AGENT_INSTALL_GIVEN_AGENT_PORT_IN_USE_FAILURE_OUTPUT,
                AGENT_INSTALL_GIVEN_AGENT_PORT_IN_USE_FAILURE_MESSAGE);
        knownFailuresforAgentInstallation.put(AGENT_INSTALL_UNSUPPORTED_PROCESS_FAILURE_OUTPUT,
                AGENT_INSTALL_UNSUPPORTED_PROCESS_FAILURE_MESSAGE);
        knownFailuresforAgentInstallation.put(AGENT_INSTALL_JAVA_HOME_NOT_AVAILABLE_FAILURE_OUTPUT,
                AGENT_INSTALL_JAVA_HOME_NOT_AVAILABLE_FAILURE_MESSAGE);
        knownFailuresforAgentInstallation.put(AGENT_INSTALL_AGENT_INITIALIZATION_FAILURE_OUTPUT,
                AGENT_INSTALL_AGENT_INITIALIZATION_FAILURE_MESSAGE);
        knownFailuresforAgentInstallation.put(AGENT_INSTALL_INVALID_PID_FAILURE_OUTPUT,
                AGENT_INSTALL_INVALID_PID_FAILURE_MESSAGE);
        knownFailuresforAgentInstallation.put(AGENT_INSTALL_NO_SUCH_PID_FAILURE_OUTPUT,
                AGENT_INSTALL_NO_SUCH_PID_FAILURE_MESSAGE);
        knownFailuresforAgentInstallation.put(AGENT_INSTALL_CONCURRENT_PORT_USAGE_FAILURE_OUTPUT,
                AGENT_INSTALL_CONCURRENT_PORT_USAGE_FAILURE_MESSAGE);
        knownFailuresforAgentInstallation.put(AGENT_INSTALL_INVALID_JAVAHOME_FAILURE_OUTPUT,
                AGENT_INSTALL_INVALID_JAVAHOME_FAILURE_MESSAGE);
        knownFailuresforAgentInstallation.put(AGENT_INSTALL_INVALID_JAVAHOME_FAILURE_2_OUTPUT,
                AGENT_INSTALL_INVALID_JAVAHOME_FAILURE_MESSAGE);
        knownFailuresforAgentInstallation.put(AGENT_INSTALL_SOCKET_CONNECTION_TIMEOUT_FAILURE_OUTPUT,
                AGENT_INSTALL_SOCKET_CONNECTION_TIMEOUT_FAILURE_MESSAGE);
        knownFailuresforAgentInstallation.put(AGENT_INSTALL_INVALID_PATHS_FAILURE_OUTPUT,
                AGENT_INSTALL_INVALID_PATHS_FAILURE_MESSAGE);
        knownFailuresforAgentInstallation.put(AGENT_INSTALL_UNKNOWN_USER_FAILURE_OUTPUT,
                AGENT_INSTALL_UNKNOWN_USER_FAILURE_MESSAGE);
        knownFailuresforAgentInstallation.put(AGENT_INSTALL_USER_WITHOUT_PERMISSION_FAILURE_OUTPUT,
                AGENT_INSTALL_USER_WITHOUT_PERMISSION_FAILURE_MESSAGE);
        knownFailuresforAgentInstallation.put(AGENT_INSTALL_SUDO_NOT_AVAILABLE_FAILURE_OUTPUT,
                AGENT_INSTALL_SUDO_NOT_AVAILABLE_FAILURE_MESSAGE);
        knownFailuresforAgentInstallation.put(AGENT_INSTALL_EXPECTED_MESSAGE_FOR_AGENT_INSTALL_SCRIPT_NOT_FOUND_OUTPUT,
                K8S_POD_TAR_NOT_AVAILABLE_AGENT_COPY_FAILURE_MESSAGE);
        return knownFailuresforAgentInstallation;
    }

    public static Map<String, String> getKnownFailuresOfAgentCopyOnK8sPod() {
        Map<String, String> knownFailuresforAgentCopyOnK8sPod = new HashMap<>();
        knownFailuresforAgentCopyOnK8sPod.put(EXPECTED_MESSAGE_FOR_FILE_NOT_FOUND,
                AGENT_COPY_SUPPORT_SCRIPT_FILE_COPY_FAILURE);
        knownFailuresforAgentCopyOnK8sPod.put(K8S_INVALID_POD_CONTAINER_MAPPING_FAILURE_OUTPUT, null);
        knownFailuresforAgentCopyOnK8sPod.put(K8S_POD_TAR_NOT_AVAILABLE_AGENT_COPY_FAILURE_OUTPUT,
                K8S_POD_TAR_NOT_AVAILABLE_AGENT_COPY_FAILURE_MESSAGE);

        return knownFailuresforAgentCopyOnK8sPod;
    }

    public Map<String, String> getKnownFailuresOfAgentFaultRemediationRequest() {
        Map<String, String> knownFailuresforAgentFaultRemediation = new HashMap<>();
        knownFailuresforAgentFaultRemediation.put(
                AGENT_SUBMIT_EXPECTED_MESSAGE_FOR_AGENT_SUBMIT_SCRIPT_NOT_FOUND_OUTPUT,
                AGENT_FILES_ARE_MISSING_MESSAGE);
        knownFailuresforAgentFaultRemediation.put(AGENT_SUBMIT_JAVA_NET_CONNECT_EXCEPTION_OUTPUT,
                AGENT_NOT_AVAILABLE_MESSAGE);
        knownFailuresforAgentFaultRemediation.put(FAULT_ALREADY_REMEDIATED, FAULT_ALREADY_REMEDIATED);
        knownFailuresforAgentFaultRemediation.put(AGENT_SUBMIT_INVALID_JAVAHOME_FAILURE_OUTPUT,
                AGENT_SUBMIT_INVALID_JAVAHOME_FAILURE_MESSAGE);

        return knownFailuresforAgentFaultRemediation;
    }

    public Map<String, String> getKnownFailuresOfAgentFaultInjectionRequest() {
        Map<String, String> knownFailuresforAgentFaultInjection = new HashMap<>();
        knownFailuresforAgentFaultInjection.put(AGENT_FAULT_CONCURRENT_INJECTION_FAILURE_OUTPUT,
                AGENT_FAULT_CONCURRENT_INJECTION_FAILURE_MESSAGE);
        knownFailuresforAgentFaultInjection.put(AGENT_SUBMIT_INVALID_JAVAHOME_FAILURE_OUTPUT,
                AGENT_SUBMIT_INVALID_JAVAHOME_FAILURE_MESSAGE);
        knownFailuresforAgentFaultInjection.put(AGENT_SUBMIT_INVALID_JAVAHOME_FAILURE_2_OUTPUT,
                AGENT_SUBMIT_INVALID_JAVAHOME_FAILURE_MESSAGE);
        return knownFailuresforAgentFaultInjection;
    }


    public Map<String, String> getKnownFailureOfDockerFaultInjectionRequest() {
        Map<String, String> knownFailuresForDockerFaultInjection = new HashMap<>();
        knownFailuresForDockerFaultInjection.put(DOCKER_CONTAINER_IS_ALREADY_PAUSED_FAILURE_OUTPUT,
                DOCKER_CONTAINER_IS_ALREADY_PAUSED_MESSAGE);
        knownFailuresForDockerFaultInjection.put(DOCKER_STOP_CONTAINER_ON_PAUSED_OUTPUT,
                DOCKER_STOP_CONTAINER_ON_PAUSED_MESSAGE);
        knownFailuresForDockerFaultInjection.put(DOCKER_CONTAINER_IS_NOT_RUNNING_FAILURE_OUTPUT,
                DOCKER_CONTAINER_IS_NOT_RUNNING_MESSAGE);
        knownFailuresForDockerFaultInjection.put(DOCKER_CONNECTION_FAILURE_OUTPUT, DOCKER_CONNECTION_FAILURE_OUTPUT);
        knownFailuresForDockerFaultInjection.put(DOCKER_CONTAINER_NOT_AVAILABLE_FAILURE_OUTPUT, null);
        return knownFailuresForDockerFaultInjection;
    }

    public Map<String, String> getKnownFailureOfDockerFaultRemediationRequest() {
        Map<String, String> knownFailuresForDockerFaultRemediation = new HashMap<>();
        knownFailuresForDockerFaultRemediation.put(DOCKER_CONTAINER_IS_NOT_PAUSED_FAILURE_OUTPUT,
                DOCKER_CONTAINER_IS_NOT_PAUSED_MESSAGE);
        knownFailuresForDockerFaultRemediation.put(DOCKER_CONTAINER_START_FAILURE_OUTPUT,
                DOCKER_CONTAINER_START_FAILURE_MESSAGE);
        knownFailuresForDockerFaultRemediation.put(DOCKER_CONTAINER_IS_NOT_RUNNING_FAILURE_OUTPUT,
                DOCKER_CONTAINER_IS_NOT_RUNNING_MESSAGE);
        knownFailuresForDockerFaultRemediation.put(DOCKER_CONNECTION_FAILURE_OUTPUT, DOCKER_CONNECTION_FAILURE_OUTPUT);
        knownFailuresForDockerFaultRemediation.put(DOCKER_CONTAINER_NOT_AVAILABLE_FAILURE_OUTPUT, null);
        knownFailuresForDockerFaultRemediation.put(DOCKER_CONTAINER_IS_ALREADY_RUNNING_FAILURE_OUTPUT, null);

        return knownFailuresForDockerFaultRemediation;
    }

    public Map<String, String> getKnownFailuresOfSystemResourceFaultInjectionRequest() {
        Map<String, String> knownFailureMap = new HashMap<>();
        knownFailureMap.put(SYSTEM_RESOURCE_FAULT_PRECHECK_FAILED_OUTPUT, null);
        knownFailureMap.put(SYSTEM_RESOURCE_FAULT_ALREADY_RUNNING_OUTPUT,
                SYSTEM_RESOURCE_FAULT_PARALLEL_EXECUTION_NOT_SUPPORTED_MESSAGE);
        knownFailureMap.put(SYSTEM_RESOURCE_FAULT_DIRECTORY_NOT_FOUND_OUTPUT, null);
        knownFailureMap.put(DISKIO_FAULT_IOSIZE_GREATER_THAN_DISK_SIZE_OUTPUT, null);
        knownFailureMap.put(DISKIO_FAULT_TARGET_DIRECTORY_NOT_FOUND_OUTPUT,
                DISKIO_FAULT_TARGET_DIRECTORY_NOT_FOUND_OUTPUT_MESSAGE);
        knownFailureMap.put(DISKIO_FAULT_TARGET_DIRECTORY_DOESNT_HAVE_PERMISSION_OUTPUT, null);
        knownFailureMap.put(MEMORY_FAULT_CURRENT_MEMORY_USAGE_GREATER_THAN_REQUESTED_MEMORY_OUTPUT,
                MEMORY_FAULT_CURRENT_MEMORY_USAGE_GREATER_THAN_REQUESTED_MEMORY_MESSAGE);
        knownFailureMap.put(EXPECTED_MESSAGE_FOR_KILL_OPERATION_NOT_PERMITTED,
                MESSAGE_THROWN_FOR_KILL_OPERATION_NOT_PERMITTED);
        knownFailureMap.put(KILL_PROCESS_FOUND_MORE_THAN_ONE_PROCESS_FAILURE_OUTPUT,
                KILL_PROCESS_FOUND_MORE_THAN_ONE_PROCESS_FAILURE_MESSAGE);
        knownFailureMap.put(KILL_PROCESS_NOT_FOUND_IDENTIFIER_FAILURE_OUTPUT,
                KILL_PROCESS_NOT_FOUND_IDENTIFIER_FAILURE_MESSAGE);
        knownFailureMap.put(NETWORK_FAULT_INVALID_NIC_OUTPUT, NETWORK_FAULT_INVALID_NIC_MESSAGE);
        knownFailureMap.put(DISK_SPACE_NOT_FOUND_DIRECTORY_PATH_FAILURE_OUTPUT,
                DISK_SPACE_NOT_FOUND_DIRECTORY_PATH_FAILURE_MESSAGE);
        knownFailureMap.put(DISK_SPACE_FILL_PERCENTAGE_GREATER_THAN_USED_DISK_PERCENTAGE_FAILURE_OUTPUT,
                DISK_SPACE_FILL_PERCENTAGE_GREATER_THAN_USED_DISK_PERCENTAGE_FAILURE_MESSAGE);

        return knownFailureMap;
    }

    public Map<String, String> getKnownFailuresOfSystemResourceFaultRemediationRequest() {
        Map<String, String> knownFailureMap = new HashMap<>();
        knownFailureMap.put(EXPECTED_MESSAGE_FOR_FILE_NOT_FOUND, FAULT_ALREADY_REMEDIATED);
        knownFailureMap.put(SYSTEM_RESOURCE_FAULT_PRECHECK_FAILED_OUTPUT, null);
        knownFailureMap.put(SYSTEM_RESOURCE_FAULT_ALREADY_REMEDIATED_OUTPUT, FAULT_ALREADY_REMEDIATED);
        knownFailureMap.put(DISK_SPACE_NOT_FOUND_DIRECTORY_PATH_FAILURE_OUTPUT,
                DISK_SPACE_NOT_FOUND_DIRECTORY_PATH_FAILURE_MESSAGE);
        knownFailureMap.put(SYSTEM_RESOURCE_SHELL_SCRIPT_FILE_NOT_FOUND_OUTPUT, FAULT_ALREADY_REMEDIATED);
        knownFailureMap.put(NETWORK_FAULT_REMEDIATION_FAIL_SOCKET_NOT_ESTABLISHED_OUTPUT,
                NETWORK_FAULT_REMEDIATION_FAIL_SOCKET_NOT_ESTABLISHED_MESSAGE);
        return knownFailureMap;
    }

    public Map<String, String> getKnownFailuresOfSystemResourceK8SCopyRequest() {
        Map<String, String> knownFailureMap = new HashMap<>();
        knownFailureMap.put(EXPECTED_MESSAGE_FOR_FILE_NOT_FOUND, AGENT_COPY_SUPPORT_SCRIPT_FILE_COPY_FAILURE);
        knownFailureMap.put(K8S_INVALID_POD_CONTAINER_MAPPING_FAILURE_OUTPUT, null);
        return knownFailureMap;
    }

    public Map<String, String> getKnownFailureOfVCenterFaultRemediationRequest() {
        Map<String, String> knownFailuresForAgentFaultRemediation = new HashMap<>();
        knownFailuresForAgentFaultRemediation.put(VCENTER_STATE_FAULT_ALREADY_REMEDIATED_OUTPUT,
                FAULT_ALREADY_REMEDIATED);
        knownFailuresForAgentFaultRemediation.put(VCENTER_ADAPTER_CONNECTION_FAILURE_OUTPUT, null);
        knownFailuresForAgentFaultRemediation.put(VCENTER_DISCONNECT_DISK_NIC_FAULT_ALREADY_REMEDIATED_OUTPUT,
                FAULT_ALREADY_REMEDIATED);
        knownFailuresForAgentFaultRemediation.put(VCENTER_VM_NOT_FOUND_OUTPUT, null);
        return knownFailuresForAgentFaultRemediation;
    }

    public Map<String, String> getKnownFailureOfVCenterFaultInjectionRequest() {
        Map<String, String> knownFailuresForAgentFaultInjection = new HashMap<>();
        knownFailuresForAgentFaultInjection.put(VCENTER_POWER_OFF_ALREADY_INJECTED_OUTPUT, null);
        knownFailuresForAgentFaultInjection.put(VCENTER_SUSPENDED_ALREADY_INJECTED_OUTPUT, null);
        knownFailuresForAgentFaultInjection.put(VCENTER_DISCONNECTED_DISK_NIC_ALREADY_INJECTED_OUTPUT, null);
        knownFailuresForAgentFaultInjection.put(VCENTER_VM_NOT_FOUND_OUTPUT, null);
        knownFailuresForAgentFaultInjection.put(VCENTER_NIC_POWERED_OFF_VM_FAILURE_OUTPUT, null);
        knownFailuresForAgentFaultInjection.put(VCENTER_RESET_POWERED_OFF_VM_ALREADY_INJECTED_OUTPUT, null);
        knownFailuresForAgentFaultInjection.put(VCENTER_SUSPEND_POWERED_OFF_VM_ALREADY_INJECTED_OUTPUT, null);
        knownFailuresForAgentFaultInjection.put(VCENTER_ADAPTER_CONNECTION_FAILURE_OUTPUT, null);
        knownFailuresForAgentFaultInjection.put(VCENTER_SERVER_CONNECTION_FAILURE_OUTPUT, null);
        return knownFailuresForAgentFaultInjection;
    }
}
