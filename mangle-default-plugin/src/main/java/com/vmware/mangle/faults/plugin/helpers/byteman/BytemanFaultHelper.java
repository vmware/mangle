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

package com.vmware.mangle.faults.plugin.helpers.byteman;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.services.enums.BytemanFaultType;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;


/**
 * @author bkaranam, dbhat
 *
 */
@Log4j2
public abstract class BytemanFaultHelper {

    public abstract ICommandExecutor getExecutor(CommandExecutionFaultSpec faultSpec) throws MangleException;

    public abstract List<CommandInfo> getInjectionCommandInfoList(CommandExecutionFaultSpec spec)
            throws MangleException;

    public abstract List<CommandInfo> getRemediationCommandInfoList(CommandExecutionFaultSpec jvmAgentFaultSpec)
            throws MangleException;

    public abstract List<SupportScriptInfo> getAgentFaultInjectionScripts(CommandExecutionFaultSpec jvmAgentFaultSpec);

    public abstract void checkTaskSpecificPrerequisites() throws MangleException;

    protected String generateRule(CommandExecutionFaultSpec jvmAgentFaultSpec) throws MangleException {
        String ruleString = "";
        BytemanFaultType bytemanFaultType = BytemanFaultType.valueOf(jvmAgentFaultSpec.getFaultType());
        switch (bytemanFaultType) {
        case SPRING_SERVICE_LATENCY:
            ruleString = BytemanFaultRuleFactory.getSpringServiceLatencyRule(jvmAgentFaultSpec);
            break;
        case SPRING_SERVICE_EXCEPTION:
            ruleString = BytemanFaultRuleFactory.getSpringServiceExceptionRule(jvmAgentFaultSpec);
            break;
        case JAVA_METHOD_LATENCY:
            ruleString = BytemanFaultRuleFactory.getJavaMethodLatencyRule(jvmAgentFaultSpec);
            break;
        case EXCEPTION:
            ruleString = BytemanFaultRuleFactory.getExceptionRule(jvmAgentFaultSpec);
            break;
        case THREAD_INTERRUPTION:
            ruleString = BytemanFaultRuleFactory.getThreadInterruptionRule(jvmAgentFaultSpec);
            break;
        case KILL_JVM:
            ruleString = BytemanFaultRuleFactory.getKillJvmRule(jvmAgentFaultSpec);
            break;
        case KILL_THREAD:
            ruleString = BytemanFaultRuleFactory.getKillThreadRule(jvmAgentFaultSpec);
            break;
        case MANIPULATE_RETURN_OBJECT:
            ruleString = BytemanFaultRuleFactory.getManipulateReturnObjectRule(jvmAgentFaultSpec);
            break;
        case TRACE_OBJECT:
            ruleString = BytemanFaultRuleFactory.getTraceObjectRule(jvmAgentFaultSpec);
            break;
        default:
            throw new MangleException(ErrorCode.UNSUPPORTED_BYTEMAN_FAULT, bytemanFaultType);
        }
        log.info("Generated rule string: " + ruleString);
        return ruleString;
    }

    protected static ArrayList<SupportScriptInfo> getAgentFaultScripts(CommandExecutionFaultSpec jvmAgentFaultSpec,
            String targetDirectoryPath, String scriptFileName) {
        SupportScriptInfo faultInjectionScriptInfo = new SupportScriptInfo();
        faultInjectionScriptInfo.setScriptFileName(scriptFileName);
        faultInjectionScriptInfo.setTargetDirectoryPath(targetDirectoryPath);
        faultInjectionScriptInfo.setClassPathResource(true);
        faultInjectionScriptInfo.setExecutable(false);
        if (CollectionUtils.isEmpty(jvmAgentFaultSpec.getSupportScriptInfo())) {
            ArrayList<SupportScriptInfo> agentFaultInjectionScripts = new ArrayList<>();
            agentFaultInjectionScripts.add(faultInjectionScriptInfo);
            return agentFaultInjectionScripts;
        }
        jvmAgentFaultSpec.getSupportScriptInfo().add(faultInjectionScriptInfo);
        return (ArrayList<SupportScriptInfo>) jvmAgentFaultSpec.getSupportScriptInfo();
    }
}

