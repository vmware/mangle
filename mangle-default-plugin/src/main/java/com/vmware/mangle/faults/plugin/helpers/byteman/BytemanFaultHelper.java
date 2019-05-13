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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.services.dto.AgentRuleConstants;
import com.vmware.mangle.services.enums.BytemanFaultType;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;


/**
 * @author bkaranam
 *
 */
@Log4j2
public abstract class BytemanFaultHelper {

    private static final String LINE_SEPERATOR = System.getProperty("line.separator");

    public abstract ICommandExecutor getExecutor(CommandExecutionFaultSpec faultSpec) throws MangleException;

    public abstract List<CommandInfo> getInjectionCommandInfoList(CommandExecutionFaultSpec spec)
            throws MangleException;

    public abstract List<CommandInfo> getRemediationCommandInfoList(CommandExecutionFaultSpec jvmAgentFaultSpec)
            throws MangleException;

    public abstract List<SupportScriptInfo> getAgentFaultInjectionScripts(CommandExecutionFaultSpec jvmAgentFaultSpec);

    public abstract void checkTaskSpecificPrerequisites() throws MangleException;

    protected String generateRule(CommandExecutionFaultSpec jvmAgentFaultSpec) throws MangleException {
        String ruleString = "";
        try {
            ruleString = FileUtils.readFileToString(
                    new File(
                            BytemanFaultHelper.class.getClassLoader().getResource("BytemanRuleTemplate.txt").getFile()),
                    Charset.defaultCharset());
        } catch (IOException e) {
            log.error(e);
        }
        String bindString = null;
        BytemanFaultType bytemanFaultType = BytemanFaultType.valueOf(jvmAgentFaultSpec.getFaultType());
        switch (bytemanFaultType) {
        case SPRING_SERVICE_LATENCY:
            ruleString = ruleString.replace(AgentRuleConstants.HELPER_METHOD_INVOCATION_STRING,
                    "sleep(" + jvmAgentFaultSpec.getArgs().get(AgentRuleConstants.LATENCY_STRING) + ");");
            ruleString = ruleString.replace(AgentRuleConstants.CLASS_NAME_STRING, "javax.servlet.http.HttpServlet");
            ruleString = ruleString.replace(AgentRuleConstants.METHOD_NAME_STRING,
                    "service(HttpServletRequest,HttpServletResponse)");
            ruleString = ruleString.replace(AgentRuleConstants.RULE_EVENT_STRING, AgentRuleConstants.AT_ENTRY_STRING);
            bindString = new StringBuilder().append("BIND req:javax.servlet.http.HttpServletRequest=").append("\\")
                    .append("$1;").append(LINE_SEPERATOR).append("   pathInfo:java.lang.String=req.getRequestURI();")
                    .append(LINE_SEPERATOR).append("   method:java.lang.String=req.getMethod();").toString();
            ruleString = ruleString.replace(AgentRuleConstants.IF_TRUE,
                    new StringBuilder().append("IF isFaultEnabled(\\\"")
                            .append(jvmAgentFaultSpec.getArgs().get(AgentRuleConstants.SERVICES_STRING).trim())
                            .append("\\\",pathInfo.toString(),\\\"")
                            .append(jvmAgentFaultSpec.getArgs().get(AgentRuleConstants.HTTP_METHODS_STRING).trim())
                            .append("\\\",method)").toString());
            break;
        case SPRING_SERVICE_EXCEPTION:
            ruleString = ruleString.replace(AgentRuleConstants.HELPER_METHOD_INVOCATION_STRING, new StringBuilder()
                    .append("throwException(\\\"")
                    .append(jvmAgentFaultSpec.getArgs().get(AgentRuleConstants.EXCEPTION_CLASS_STRING))
                    .append("\\\", \\\"")
                    .append(jvmAgentFaultSpec.getArgs().get(AgentRuleConstants.EXCEPTION_MESSAGE_STRING) + "\\\")")
                    .toString());
            ruleString = ruleString.replace(AgentRuleConstants.CLASS_NAME_STRING, "javax.servlet.http.HttpServlet");
            ruleString = ruleString.replace(AgentRuleConstants.METHOD_NAME_STRING,
                    "service(HttpServletRequest,HttpServletResponse)");
            ruleString = ruleString.replace(AgentRuleConstants.RULE_EVENT_STRING, AgentRuleConstants.AT_ENTRY_STRING);
            bindString = new StringBuilder().append("BIND req:javax.servlet.http.HttpServletRequest=").append("\\")
                    .append("$1;").append(LINE_SEPERATOR).append("   pathInfo:java.lang.String=req.getRequestURI();")
                    .append(LINE_SEPERATOR).append("   method:java.lang.String=req.getMethod();").toString();
            ruleString = ruleString.replace(AgentRuleConstants.IF_TRUE,
                    new StringBuilder().append("IF isFaultEnabled(\\\"")
                            .append(jvmAgentFaultSpec.getArgs().get(AgentRuleConstants.SERVICES_STRING).trim())
                            .append("\\\",pathInfo.toString(),\\\"")
                            .append(jvmAgentFaultSpec.getArgs().get(AgentRuleConstants.HTTP_METHODS_STRING).trim())
                            .append("\\\",method)").toString());
            break;
        case XENON_SERVICE_LATENCY:
            ruleString = ruleString.replace(AgentRuleConstants.HELPER_METHOD_INVOCATION_STRING,
                    new StringBuilder().append("serviceLatency(\"")
                            .append(jvmAgentFaultSpec.getArgs().get(AgentRuleConstants.SERVICES_STRING))
                            .append("\",operation.toString(),")
                            .append(jvmAgentFaultSpec.getArgs().get(AgentRuleConstants.LATENCY_STRING) + ")")
                            .toString());
            ruleString = ruleString.replace(AgentRuleConstants.CLASS_NAME_STRING,
                    "com.vmware.xenon.common.http.netty.NettyHttpClientRequestHandler");
            ruleString = ruleString.replace(AgentRuleConstants.METHOD_NAME_STRING, "submitRequest");
            ruleString = ruleString.replace(AgentRuleConstants.RULE_EVENT_STRING, AgentRuleConstants.AT_ENTRY_STRING);
            if (Boolean.parseBoolean(jvmAgentFaultSpec.getArgs().get("enableOnLocalRequests"))) {
                bindString = "BIND operation:com.vmware.xenon.common.Operation=$2";
            } else {
                bindString = new StringBuilder().append("BIND operation:com.vmware.xenon.common.Operation=$2;")
                        .append(LINE_SEPERATOR).append("context:io.netty.channel.ChannelHandlerContext=$1;")
                        .append(LINE_SEPERATOR)
                        .append("inetLocalAddress:java.net.InetSocketAddress=context.channel().localAddress();")
                        .append(LINE_SEPERATOR)
                        .append("inetRemoteAddress:java.net.InetSocketAddress=context.channel().remoteAddress();")
                        .append(LINE_SEPERATOR)
                        .append("localAddress:java.net.InetAddress=inetLocalAddress.getAddress();")
                        .append(LINE_SEPERATOR)
                        .append("remoteAddress:java.net.InetAddress=inetRemoteAddress.getAddress();").toString();
                ruleString = ruleString.replace(AgentRuleConstants.IF_TRUE, "IF !localAddress.equals(remoteAddress)");
            }
            break;
        case JAVA_METHOD_LATENCY:
            ruleString = ruleString.replace(AgentRuleConstants.HELPER_METHOD_INVOCATION_STRING,
                    "sleep(" + jvmAgentFaultSpec.getArgs().get(AgentRuleConstants.LATENCY_STRING) + ")");
            break;
        case EXCEPTION:
            ruleString = ruleString.replace(AgentRuleConstants.HELPER_METHOD_INVOCATION_STRING,
                    "throwException(\"" + jvmAgentFaultSpec.getArgs().get("exceptionClass") + "\", \""
                            + jvmAgentFaultSpec.getArgs().get("exceptionMessage") + "\")");
            break;
        case THREAD_INTERRUPTION:
            ruleString = ruleString.replace(AgentRuleConstants.HELPER_METHOD_INVOCATION_STRING, "interrupt()");
            break;
        case KILL_JVM:
            ruleString = ruleString.replace(AgentRuleConstants.HELPER_METHOD_INVOCATION_STRING,
                    "killJVM(" + jvmAgentFaultSpec.getArgs().get("exitCode") + ")");
            break;
        case KILL_THREAD:
            ruleString = ruleString.replace(AgentRuleConstants.HELPER_METHOD_INVOCATION_STRING, "killThread()");
            break;
        case MANIPULATE_RETURN_OBJECT:
            String returnValue =
                    jvmAgentFaultSpec.getArgs().get("returnValueJsonNotation").replaceAll("\\\"", "\\\\\"");
            bindString = new StringBuilder().append("BIND returnValue:")
                    .append(jvmAgentFaultSpec.getArgs().get("returnClasName")).append("=jsonToObject(\"")
                    .append(jvmAgentFaultSpec.getArgs().get("returnClasName")).append("\",\"").append(returnValue)
                    .append("\")").toString();
            ruleString = ruleString.replace(AgentRuleConstants.HELPER_METHOD_INVOCATION_STRING, "return returnValue");
            break;
        case TRACE_OBJECT:
            bindString = new StringBuilder().append("BIND objectValue:java.lang.String=objectToJson(")
                    .append(jvmAgentFaultSpec.getArgs().get("fieldName")).append(");").toString();
            ruleString = ruleString.replace(AgentRuleConstants.HELPER_METHOD_INVOCATION_STRING,
                    "System.out.println(objectValue)");
            break;
        case MOCK_XENON_SERVICE_RESPONSE_CODES:
            ruleString = ruleString.replace(AgentRuleConstants.HELPER_METHOD_INVOCATION_STRING,
                    "response.setStatus(io.netty.handler.codec.http.HttpResponseStatus.SERVICE_UNAVAILABLE);");
            ruleString = ruleString.replace(AgentRuleConstants.CLASS_NAME_STRING,
                    "com.vmware.xenon.common.http.netty.NettyHttpClientRequestHandler");
            ruleString = ruleString.replace(AgentRuleConstants.METHOD_NAME_STRING, "writeResponse");
            ruleString = ruleString.replace(AgentRuleConstants.RULE_EVENT_STRING, AgentRuleConstants.AT_ENTRY_STRING);
            bindString = new StringBuilder().append("BIND operation:com.vmware.xenon.common.Operation=$2;")
                    .append(LINE_SEPERATOR).append(" response:io.netty.handler.codec.http.DefaultFullHttpResponse=$3;")
                    .toString();
            ruleString = ruleString.replace(AgentRuleConstants.IF_TRUE, new StringBuilder()
                    .append("IF applyPercentageFilter(\"")
                    .append(jvmAgentFaultSpec.getArgs().get(AgentRuleConstants.SERVICES_STRING))
                    .append("\",operation.toString()," + jvmAgentFaultSpec.getArgs().get("failurePercentage") + ")")
                    .toString());
            break;
        default:
            throw new MangleException(ErrorCode.UNSUPPORTED_BYTEMAN_FAULT, bytemanFaultType);
        }
        ruleString = ruleString.replace("$RULE_NAME", jvmAgentFaultSpec.getArgs().get(TASK_ID));
        if (bytemanFaultType != BytemanFaultType.XENON_SERVICE_LATENCY
                && bytemanFaultType != BytemanFaultType.SPRING_SERVICE_LATENCY
                && bytemanFaultType != BytemanFaultType.SPRING_SERVICE_EXCEPTION) {
            ruleString = ruleString.replace(AgentRuleConstants.CLASS_NAME_STRING,
                    jvmAgentFaultSpec.getArgs().get(CLASS_NAME));
            ruleString = ruleString.replace(AgentRuleConstants.METHOD_NAME_STRING,
                    jvmAgentFaultSpec.getArgs().get(METHOD_NAME));
            ruleString = ruleString.replace(AgentRuleConstants.RULE_EVENT_STRING,
                    jvmAgentFaultSpec.getArgs().get(RULE_EVENT));
        }
        ruleString =
                ruleString.replace("$HELPER_CLASS_NAME", "com.vmware.mangle.java.faults.helpers.BytemanRuleHelper");

        if (bindString != null) {
            ruleString = ruleString.replace("$BIND", bindString);
        } else {
            ruleString = ruleString.replace("$BIND", "");
        }
        log.info("Generated rule string: " + ruleString);
        return ruleString;
    }

    protected static ArrayList<SupportScriptInfo> getAgentFaultScripts(String targetDirectoryPath,
            String scriptFileName) {
        ArrayList<SupportScriptInfo> agentFaultInjectionScripts = new ArrayList<>();
        SupportScriptInfo faultInjectionScriptInfo = new SupportScriptInfo();
        faultInjectionScriptInfo.setScriptFileName(scriptFileName);
        faultInjectionScriptInfo.setTargetDirectoryPath(targetDirectoryPath);
        faultInjectionScriptInfo.setClassPathResource(true);
        faultInjectionScriptInfo.setExecutable(false);
        agentFaultInjectionScripts.add(faultInjectionScriptInfo);
        return agentFaultInjectionScripts;
    }
}

