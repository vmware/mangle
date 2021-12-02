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

import static com.vmware.mangle.services.dto.AgentRuleConstants.CLASS_NAME;
import static com.vmware.mangle.services.dto.AgentRuleConstants.METHOD_NAME;
import static com.vmware.mangle.services.dto.AgentRuleConstants.RULE_EVENT;
import static com.vmware.mangle.utils.constants.FaultConstants.TASK_ID;

import lombok.experimental.UtilityClass;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.services.dto.AgentRuleConstants;
import com.vmware.mangle.services.enums.BytemanFaultType;
import com.vmware.mangle.services.enums.SysExitCodes;

/**
 * Class is used to generate BytemanFault Rule.
 *
 * @author kumargautam
 */
@UtilityClass
public class BytemanFaultRuleFactory {

    private static final String LINE_SEPERATOR = System.getProperty("line.separator");
    private static final String LINE_SEPERATOR_3 = "\\\"";

    public String getSpringServiceLatencyRule(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        StringBuilder builder = new StringBuilder(getRuleName(jvmAgentFaultSpec));
        builder.append(getClassName(jvmAgentFaultSpec));
        builder.append(getMethodName(jvmAgentFaultSpec));
        builder.append(getHelperClassName());
        builder.append(getRuleEvent(jvmAgentFaultSpec));
        String bindString = new StringBuilder().append("BIND req:javax.servlet.http.HttpServletRequest=\\$1;")
                .append(LINE_SEPERATOR).append("   pathInfo:java.lang.String=req.getRequestURI();")
                .append(LINE_SEPERATOR).append("   method:java.lang.String=req.getMethod();").toString();
        builder.append(getBindString(bindString));
        String ifStr = new StringBuilder().append("IF isFaultEnabled(\\\"")
                .append(jvmAgentFaultSpec.getArgs().get(AgentRuleConstants.SERVICES_STRING).trim())
                .append("\\\",pathInfo.toString(),\\\"")
                .append(jvmAgentFaultSpec.getArgs().get(AgentRuleConstants.HTTP_METHODS_STRING).trim())
                .append("\\\",method)").toString();
        builder.append(getIfCondition(ifStr));
        String helperMethodStr = "sleep(" + jvmAgentFaultSpec.getArgs().get(AgentRuleConstants.LATENCY_STRING) + ");";
        builder.append(getHelperMethod(helperMethodStr));
        return builder.toString();
    }

    public String getSpringServiceExceptionRule(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        StringBuilder builder = new StringBuilder(getRuleName(jvmAgentFaultSpec));
        builder.append(getClassName(jvmAgentFaultSpec));
        builder.append(getMethodName(jvmAgentFaultSpec));
        builder.append(getHelperClassName());
        builder.append(getRuleEvent(jvmAgentFaultSpec));
        String bindString = new StringBuilder().append("BIND req:javax.servlet.http.HttpServletRequest=\\$1;")
                .append(LINE_SEPERATOR).append("   pathInfo:java.lang.String=req.getRequestURI();")
                .append(LINE_SEPERATOR).append("   method:java.lang.String=req.getMethod();").toString();
        builder.append(getBindString(bindString));
        String ifStr = new StringBuilder().append("IF isFaultEnabled(\\\"")
                .append(jvmAgentFaultSpec.getArgs().get(AgentRuleConstants.SERVICES_STRING).trim())
                .append("\\\",pathInfo.toString(),\\\"")
                .append(jvmAgentFaultSpec.getArgs().get(AgentRuleConstants.HTTP_METHODS_STRING).trim())
                .append("\\\",method)").toString();
        builder.append(getIfCondition(ifStr));
        String helperMethodStr = new StringBuilder().append("throwException(\\\"")
                .append(jvmAgentFaultSpec.getArgs().get(AgentRuleConstants.EXCEPTION_CLASS_STRING)).append("\\\", \\\"")
                .append(jvmAgentFaultSpec.getArgs().get(AgentRuleConstants.EXCEPTION_MESSAGE_STRING) + LINE_SEPERATOR_3
                        + ")")
                .toString();
        builder.append(getHelperMethod(helperMethodStr));
        return builder.toString();
    }

    public String getJavaMethodLatencyRule(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        StringBuilder builder = new StringBuilder(getRuleName(jvmAgentFaultSpec));
        builder.append(getClassName(jvmAgentFaultSpec));
        builder.append(getMethodName(jvmAgentFaultSpec));
        builder.append(getHelperClassName());
        builder.append(getRuleEvent(jvmAgentFaultSpec));

        builder.append(getBindString(null));

        builder.append(getIfCondition(null));
        String helperMethodStr = "sleep(" + jvmAgentFaultSpec.getArgs().get(AgentRuleConstants.LATENCY_STRING) + ")";
        builder.append(getHelperMethod(helperMethodStr));
        return builder.toString();
    }

    public String getExceptionRule(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        StringBuilder builder = new StringBuilder(getRuleName(jvmAgentFaultSpec));
        builder.append(getClassName(jvmAgentFaultSpec));
        builder.append(getMethodName(jvmAgentFaultSpec));
        builder.append(getHelperClassName());
        builder.append(getRuleEvent(jvmAgentFaultSpec));

        builder.append(getBindString(null));

        builder.append(getIfCondition(null));
        String helperMethodStr = "throw new " + jvmAgentFaultSpec.getArgs().get("exceptionClass") + "(\\\""
                + jvmAgentFaultSpec.getArgs().get("exceptionMessage") + "\\\");";
        builder.append(getHelperMethod(helperMethodStr));
        return builder.toString();
    }

    public String getThreadInterruptionRule(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        StringBuilder builder = new StringBuilder(getRuleName(jvmAgentFaultSpec));
        builder.append(getClassName(jvmAgentFaultSpec));
        builder.append(getMethodName(jvmAgentFaultSpec));
        builder.append(getHelperClassName());
        builder.append(getRuleEvent(jvmAgentFaultSpec));

        builder.append(getBindString(null));

        builder.append(getIfCondition(null));

        String helperMethodStr = "interrupt()";
        builder.append(getHelperMethod(helperMethodStr));
        return builder.toString();
    }

    public String getKillJvmRule(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        StringBuilder builder = new StringBuilder(getRuleName(jvmAgentFaultSpec));
        builder.append(getClassName(jvmAgentFaultSpec));
        builder.append(getMethodName(jvmAgentFaultSpec));
        builder.append(getHelperClassName());
        builder.append(getRuleEvent(jvmAgentFaultSpec));

        builder.append(getBindString(null));

        builder.append(getIfCondition(null));

        String enumExitCode =
                SysExitCodes.fromCode(Integer.parseInt(jvmAgentFaultSpec.getArgs().get("exitCode"))).toString();
        String helperMethodStr = new StringBuilder()
                .append("System.out.println(\\\"Calling killJVM with exitCode " + enumExitCode + "\\\")")
                .append("; killJVM(" + jvmAgentFaultSpec.getArgs().get("exitCode") + ")").toString();
        builder.append(getHelperMethod(helperMethodStr));
        return builder.toString();
    }

    public String getKillThreadRule(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        StringBuilder builder = new StringBuilder(getRuleName(jvmAgentFaultSpec));
        builder.append(getClassName(jvmAgentFaultSpec));
        builder.append(getMethodName(jvmAgentFaultSpec));
        builder.append(getHelperClassName());
        builder.append(getRuleEvent(jvmAgentFaultSpec));

        builder.append(getBindString(null));

        builder.append(getIfCondition(null));

        String helperMethodStr = "killThread()";
        builder.append(getHelperMethod(helperMethodStr));
        return builder.toString();
    }

    public String getManipulateReturnObjectRule(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        StringBuilder builder = new StringBuilder(getRuleName(jvmAgentFaultSpec));
        builder.append(getClassName(jvmAgentFaultSpec));
        builder.append(getMethodName(jvmAgentFaultSpec));
        builder.append(getHelperClassName());
        builder.append(getRuleEvent(jvmAgentFaultSpec));

        String returnValue = jvmAgentFaultSpec.getArgs().get("returnValueJsonNotation").replaceAll("\\\"", "\\\\\"");
        String bindString = new StringBuilder().append("BIND returnValue:")
                .append(jvmAgentFaultSpec.getArgs().get("returnClasName")).append("=jsonToObject(\"")
                .append(jvmAgentFaultSpec.getArgs().get("returnClasName")).append("\",\"").append(returnValue)
                .append("\")").toString();
        builder.append(getBindString(bindString));

        builder.append(getIfCondition(null));

        String helperMethodStr = "return returnValue";
        builder.append(getHelperMethod(helperMethodStr));
        return builder.toString();
    }

    public String getTraceObjectRule(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        StringBuilder builder = new StringBuilder(getRuleName(jvmAgentFaultSpec));
        builder.append(getClassName(jvmAgentFaultSpec));
        builder.append(getMethodName(jvmAgentFaultSpec));
        builder.append(getHelperClassName());
        builder.append(getRuleEvent(jvmAgentFaultSpec));

        String bindString = new StringBuilder().append("BIND objectValue:java.lang.String=objectToJson(")
                .append(jvmAgentFaultSpec.getArgs().get("fieldName")).append(");").toString();
        builder.append(getBindString(bindString));

        builder.append(getIfCondition(null));

        String helperMethodStr = "System.out.println(objectValue)";
        builder.append(getHelperMethod(helperMethodStr));
        return builder.toString();
    }

    private String getRuleName(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        return new StringBuilder(AgentRuleConstants.RULE_STRING).append(jvmAgentFaultSpec.getArgs().get(TASK_ID))
                .append("\n").toString();
    }

    private String getClassName(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        StringBuilder builder = new StringBuilder();
        BytemanFaultType bytemanFaultType = BytemanFaultType.valueOf(jvmAgentFaultSpec.getFaultType());
        builder.append(AgentRuleConstants.CLASS_STRING);
        if (bytemanFaultType.equals(BytemanFaultType.SPRING_SERVICE_LATENCY)
                || bytemanFaultType.equals(BytemanFaultType.SPRING_SERVICE_EXCEPTION)) {
            builder.append("javax.servlet.http.HttpServlet");
        }
        if (bytemanFaultType != BytemanFaultType.XENON_SERVICE_LATENCY
                && bytemanFaultType != BytemanFaultType.SPRING_SERVICE_LATENCY
                && bytemanFaultType != BytemanFaultType.SPRING_SERVICE_EXCEPTION) {
            builder.append(jvmAgentFaultSpec.getArgs().get(CLASS_NAME));
        }
        builder.append("\n");
        return builder.toString();
    }

    private String getMethodName(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        StringBuilder builder = new StringBuilder();
        BytemanFaultType bytemanFaultType = BytemanFaultType.valueOf(jvmAgentFaultSpec.getFaultType());
        builder.append(AgentRuleConstants.METHOD_STRING);
        if (bytemanFaultType.equals(BytemanFaultType.SPRING_SERVICE_LATENCY)
                || bytemanFaultType.equals(BytemanFaultType.SPRING_SERVICE_EXCEPTION)) {
            builder.append("service(HttpServletRequest,HttpServletResponse)");
        }
        if (bytemanFaultType != BytemanFaultType.XENON_SERVICE_LATENCY
                && bytemanFaultType != BytemanFaultType.SPRING_SERVICE_LATENCY
                && bytemanFaultType != BytemanFaultType.SPRING_SERVICE_EXCEPTION) {
            builder.append(jvmAgentFaultSpec.getArgs().get(METHOD_NAME));
        }
        builder.append("\n");
        return builder.toString();
    }

    private String getHelperClassName() {
        return new StringBuilder().append(AgentRuleConstants.HELPER_CLASS_STRING)
                .append("com.vmware.mangle.java.agent.faults.helpers.BytemanRuleHelper").append("\n").toString();

    }

    private String getRuleEvent(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        StringBuilder builder = new StringBuilder();
        BytemanFaultType bytemanFaultType = BytemanFaultType.valueOf(jvmAgentFaultSpec.getFaultType());
        if (bytemanFaultType.equals(BytemanFaultType.SPRING_SERVICE_LATENCY)
                || bytemanFaultType.equals(BytemanFaultType.SPRING_SERVICE_EXCEPTION)) {
            builder.append(AgentRuleConstants.AT_ENTRY_STRING);
        }
        if (bytemanFaultType != BytemanFaultType.XENON_SERVICE_LATENCY
                && bytemanFaultType != BytemanFaultType.SPRING_SERVICE_LATENCY
                && bytemanFaultType != BytemanFaultType.SPRING_SERVICE_EXCEPTION) {
            builder.append(jvmAgentFaultSpec.getArgs().get(RULE_EVENT));
        }
        builder.append("\n");
        return builder.toString();
    }

    private String getBindString(String bindStr) {
        StringBuilder builder = new StringBuilder();
        if (bindStr != null) {
            builder.append(bindStr);
        } else {
            builder.append("");
        }
        builder.append("\n");
        return builder.toString();
    }

    private String getIfCondition(String str) {
        StringBuilder builder = new StringBuilder();
        if (str != null) {
            builder.append(str);
        } else {
            builder.append(AgentRuleConstants.IF_TRUE);
        }
        builder.append("\n");
        return builder.toString();
    }

    private String getHelperMethod(String str) {
        StringBuilder builder = new StringBuilder();
        builder.append("DO ").append(str).append("\n").append("ENDRULE");
        return builder.toString();
    }
}
