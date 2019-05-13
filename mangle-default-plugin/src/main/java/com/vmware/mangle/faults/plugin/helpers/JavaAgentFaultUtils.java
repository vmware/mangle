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

import static com.vmware.mangle.utils.constants.FaultConstants.FI_ADD_INFO_FAULTID;
import static com.vmware.mangle.utils.constants.FaultConstants.GET_FAULT_COMMAND;
import static com.vmware.mangle.utils.constants.FaultConstants.GET_FAULT_COMMAND_WITH_PORT;
import static com.vmware.mangle.utils.constants.FaultConstants.PID_AGENT_COMMAND_WITH_PORT;
import static com.vmware.mangle.utils.constants.FaultConstants.REMEDIATION_COMMAND;
import static com.vmware.mangle.utils.constants.FaultConstants.REMEDIATION_COMMAND_WITH_PORT;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.vmware.mangle.utils.CommonUtils;

/**
 * @author hkilari
 *
 */
@Component
@SuppressWarnings("squid:CommentedOutCodeLine")
public class JavaAgentFaultUtils {

    public String buildInjectionCommand(Map<String, String> args, String agentBasePath, String port) {
        // validateFaultArgs(args);
        return String.format(getInjectionCommnadWithArgsWithPort(), agentBasePath, port,
                CommonUtils.convertMaptoDelimitedString(args, " "));
    }

    /*public static void validateFaultArgs(Map<String, String> args) throws MangleException {
        AgentFault fault;
        try {
            fault = AgentFaultFactory
                    .getFault(FaultsHelper.parseArgs(CommonUtils.convertMaptoDelimitedString(args, ",")));
        } catch (Exception e) {
            throw new MangleException(e);
        }
        if (fault == null) {
            throw new MangleException("Insufficient Args Provided.");
        }
    }*/


    private String getInjectionCommnadWithArgsWithPort() {
        return PID_AGENT_COMMAND_WITH_PORT;
    }

    /**
     * Utility Method to build Command Required for Remediating a Fault using Mangle-Agent
     *
     * @param agentBasePath
     * @return
     */
    public String buildRemediationCommand(String agentBasePath) {
        return String.format(REMEDIATION_COMMAND, agentBasePath, FI_ADD_INFO_FAULTID);
    }

    public String buildRemediationCommand(String agentBasePath, String port) {
        return String.format(REMEDIATION_COMMAND_WITH_PORT, agentBasePath, port, FI_ADD_INFO_FAULTID);
    }

    /**
     * @param agentBasePath
     * @return
     */
    public String buildGetFaultCommand(String agentBasePath) {
        return String.format(GET_FAULT_COMMAND, agentBasePath, FI_ADD_INFO_FAULTID);
    }

    /**
     * @param agentBasePath
     * @return
     */
    public String buildGetFaultCommand(String agentBasePath, String port) {
        return String.format(GET_FAULT_COMMAND_WITH_PORT, agentBasePath, port, FI_ADD_INFO_FAULTID);
    }
}
