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

package com.vmware.mangle.java.agent.faults.helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.google.gson.Gson;

import com.vmware.mangle.java.agent.faults.AgentFault;
import com.vmware.mangle.java.agent.faults.AgentFaultFactory;
import com.vmware.mangle.java.agent.faults.FaultInfo;
import com.vmware.mangle.java.agent.faults.FaultStatus;
import com.vmware.mangle.java.agent.faults.agent.Constants;
import com.vmware.mangle.java.agent.faults.exception.FiaascoException;
import com.vmware.mangle.java.agent.utils.ReflectionUtils;

/**
 * Helper class to support Lifecycle Operations on Byteman Agent Fault
 *
 * @author hkilari
 *
 */
public class FaultsHelper {
    private static final Logger LOG = Logger.getLogger(FaultsHelper.class.getName());

    private Map<String, AgentFault> faultsMap;

    private FaultsHelper() {
        faultsMap = new HashMap<>();
    }

    private static class SingletonHelper {
        private static final FaultsHelper INSTANCE = new FaultsHelper();
    }

    public static FaultsHelper getInstance() {
        return SingletonHelper.INSTANCE;
    }

    public String injectFault(Map<String, String> faultArgs) {
        AgentFault fault = null;
        String faultName = faultArgs.get("faultName");
        try {
            for (Entry<String, String> faultInfo : getLiveFaults().entrySet()) {
                if (faultName.equals(faultInfo.getValue())) {
                    throw new FiaascoException(faultName
                            + " is already running on Target. Agent does not support paralell execution of same Fault");
                }
            }
            fault = AgentFaultFactory.getFault(faultArgs);
        } catch (Exception e) {
            LOG.severe("Failed FaultInjection. Reason: " + e.getMessage());
            return "Failed FaultInjection. Reason: " + e.getMessage();
        }
        fault.start();
        faultsMap.put(fault.getUid().toString(), fault);
        return String.format(Constants.INJECT_FAULT_RESPONSE, fault.getUid().toString());
    }

    public String injectFault(String argsString) {
        Map<String, String> faultArgs = parseArgs(argsString);
        return injectFault(faultArgs);
    }

    public static Map<String, String> parseArgs(String argsString) {
        Map<String, String> faultArgs = new HashMap<>();
        StringTokenizer stringTokenizer = new StringTokenizer(argsString, ",");
        while (stringTokenizer.hasMoreTokens()) {
            String token = stringTokenizer.nextToken().trim();
            if (token.contains("__")) {
                faultArgs.put(token.substring(2), stringTokenizer.nextToken().trim());
            }
        }
        return faultArgs;
    }

    public String remediateFault(String faultId) {
        AgentFault fault = faultsMap.get(faultId);
        if (fault != null) {
            if (fault.getFaultInfo().getFaultStatus() != FaultStatus.COMPLETED) {
                faultsMap.get(faultId).remediateFault();
                return "Received Remediation Request Successfully";
            } else {
                return "Requested Fault is already Remediated.";
            }
        }
        return "No fault found with provided ID: " + faultId;
    }

    public Map<String, String> getLiveFaults() {
        Map<String, String> faultsInfo = new HashMap<>();
        for (AgentFault fault : faultsMap.values()) {
            if (fault.getFaultInfo().getFaultStatus() == FaultStatus.IN_PROGRESS
                    || fault.getFaultInfo().getFaultStatus() == FaultStatus.INITIALIZING) {
                faultsInfo.put(fault.getUid().toString(), fault.getFaultName());
            }
        }
        return faultsInfo;
    }

    public Map<String, AgentFault> getRunningFaults() {
        Map<String, AgentFault> faultsInfo = new HashMap<>();
        for (AgentFault fault : faultsMap.values()) {
            if (fault.getFaultInfo().getFaultStatus() == FaultStatus.IN_PROGRESS
                    || fault.getFaultInfo().getFaultStatus() == FaultStatus.INITIALIZING) {
                faultsInfo.put(fault.getUid().toString(), fault);
            }
        }
        return faultsInfo;
    }

    public Map<String, String> getAllFaults() {
        Map<String, String> faultsInfo = new HashMap<>();
        for (AgentFault fault : faultsMap.values()) {
            faultsInfo.put(fault.getUid().toString(), fault.getFaultName());
        }
        return faultsInfo;
    }

    public String getFault(String id) {
        AgentFault fault = faultsMap.get(id);
        return fault != null ? objectToJson(fault.getFaultInfo()) : "No fault fault found with provided ID: " + id;
    }

    public String getAllFaultsInfo() {
        Map<String, FaultInfo> faultsInfo = new HashMap<>();
        for (AgentFault fault : faultsMap.values()) {
            faultsInfo.put(fault.getUid().toString(), fault.getFaultInfo());
        }
        return objectToJson(faultsInfo);
    }

    public static String objectToJson(Object object) {
        Gson gson = new Gson();
        return gson.toJson(object);
    }

    public static Object jsonToObject(String className, String jsonString) {
        Gson gson = new Gson();
        return gson.fromJson(jsonString, ReflectionUtils.getObject(className).getClass());
    }
}
