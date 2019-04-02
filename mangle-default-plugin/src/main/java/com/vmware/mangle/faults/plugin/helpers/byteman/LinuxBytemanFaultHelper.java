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

import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.EXTRACT_AGENT_COMMAND;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.FAULT_COMPLETION_STRING;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.FI_ADD_INFO_FAULTID;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.JAVA_HOME_PATH;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.PID_ATTACH_MXBEANS_COMMAND_WITH_PORT;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.PROCESS;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.REMEDIATION_SUCCESSFUL_STRING;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.SUBMIT_COMMAND_WITH_PORT;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.TASK_ID;
import static com.vmware.mangle.faults.plugin.helpers.FaultConstants.USER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMAgentFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMCodeLevelFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandOutputProcessingInfo;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.ssh.SSHUtils;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author hkilari
 *
 */
public class LinuxBytemanFaultHelper extends BytemanFaultHelper {

    private EndpointClientFactory endpointClientFactory;

    @Autowired
    public LinuxBytemanFaultHelper(EndpointClientFactory endpointClientFactory) {
        this.endpointClientFactory = endpointClientFactory;

    }

    @Override
    public ICommandExecutor getExecutor(CommandExecutionFaultSpec spec) throws MangleException {
        return (SSHUtils) endpointClientFactory.getEndPointClient(spec.getCredentials(), spec.getEndpoint());
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.mangle.services.helpers.IAgentFaultHelper#
     * getInjectedCommandInfoList()
     */
    @Override
    public List<CommandInfo> getInjectionCommandInfoList(CommandExecutionFaultSpec jvmAgentFaultSpec)
            throws MangleException {

        String bmSubmitCommand = setBytemanSubmitCommand(jvmAgentFaultSpec);
        String bmInstallCommand = setBytemanInstallCommand(jvmAgentFaultSpec);
        String agentPathInTargetMachine = jvmAgentFaultSpec.getInjectionHomeDir() + "/" + Constants.JVM_AGENT_JAR;

        CommandInfo extractAgentCommandInfo = new CommandInfo();
        // Tar extraction command
        extractAgentCommandInfo.setCommand(String.format(EXTRACT_AGENT_COMMAND, jvmAgentFaultSpec.getInjectionHomeDir(),
                Constants.JVM_AGENT_JAR + "-full.tar.gz"));
        extractAgentCommandInfo.setIgnoreExitValueCheck(true);
        extractAgentCommandInfo.setExpectedCommandOutputList(Arrays.asList(""));

        CommandInfo changePermissionCommandInfo = new CommandInfo();
        // change permission command
        changePermissionCommandInfo.setCommand(
                "chmod -R 777 " + agentPathInTargetMachine + ";chmod -R 777 " + agentPathInTargetMachine + "/*");
        changePermissionCommandInfo.setIgnoreExitValueCheck(true);
        changePermissionCommandInfo.setExpectedCommandOutputList(Arrays.asList(""));

        CommandInfo attachAgentCommandInfo = new CommandInfo();
        // Byteman install command
        attachAgentCommandInfo.setCommand(bmInstallCommand);
        attachAgentCommandInfo.setIgnoreExitValueCheck(true);
        attachAgentCommandInfo.setExpectedCommandOutputList(Arrays.asList(""));

        // Condition to check for Byteman rule based fault
        if (jvmAgentFaultSpec instanceof JVMCodeLevelFaultSpec) {
            List<CommandInfo> commandInfoList = new ArrayList<>();

            CommandInfo createBytemanRuleCommandInfo = new CommandInfo();
            CommandInfo injectFaultCommandInfo = new CommandInfo();

            // Creating Byteman Rule Command
            createBytemanRuleCommandInfo.setExpectedCommandOutputList(Arrays.asList(""));
            createBytemanRuleCommandInfo.setCommand("echo \"" + generateRule(jvmAgentFaultSpec) + "\" > "
                    + jvmAgentFaultSpec.getInjectionHomeDir() + "/" + jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm");

            // Byteman Submit or injection command
            injectFaultCommandInfo.setCommand(bmSubmitCommand.replace("%s",
                    jvmAgentFaultSpec.getInjectionHomeDir() + "/" + jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm"));
            injectFaultCommandInfo.setIgnoreExitValueCheck(false);
            injectFaultCommandInfo.setExpectedCommandOutputList(
                    Arrays.asList("install rule " + jvmAgentFaultSpec.getArgs().get(TASK_ID)));

            // Adding all the commandinfos to list
            commandInfoList.add(extractAgentCommandInfo);
            commandInfoList.add(changePermissionCommandInfo);
            commandInfoList.add(attachAgentCommandInfo);
            commandInfoList.add(createBytemanRuleCommandInfo);
            commandInfoList.add(injectFaultCommandInfo);
            return commandInfoList;
        } else {
            List<CommandInfo> commandInfoList = new ArrayList<>();
            CommandInfo injectFaultCommandInfo = new CommandInfo();
            // TODO Validating Fault arguments validateFaultArgs(args);
            injectFaultCommandInfo.setCommand(bmSubmitCommand.replace("%s",
                    "-if " + CommonUtils.convertMaptoDelimitedString(jvmAgentFaultSpec.getArgs(), " ")));
            injectFaultCommandInfo.setIgnoreExitValueCheck(true);
            injectFaultCommandInfo
                    .setExpectedCommandOutputList(Arrays.asList(Constants.SUCESSFUL_FAULT_CREATION_MESSAGE));
            List<CommandOutputProcessingInfo> commandOutputProcessingInfoList = new ArrayList<>();

            CommandOutputProcessingInfo commandOutputProcessingInfo = new CommandOutputProcessingInfo();
            commandOutputProcessingInfo.setExtractedPropertyName("faultId");
            commandOutputProcessingInfo.setRegExpression("[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}");
            commandOutputProcessingInfoList.add(commandOutputProcessingInfo);
            injectFaultCommandInfo.setCommandOutputProcessingInfoList(commandOutputProcessingInfoList);

            commandInfoList.add(extractAgentCommandInfo);
            commandInfoList.add(changePermissionCommandInfo);
            commandInfoList.add(attachAgentCommandInfo);
            commandInfoList.add(injectFaultCommandInfo);
            return commandInfoList;

        }
    }


    @Override
    public List<CommandInfo> getRemediationCommandInfoList(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        String bmSubmitCommand = setBytemanSubmitCommand(jvmAgentFaultSpec);
        // Condition to check for Byteman rule based fault
        if (jvmAgentFaultSpec instanceof JVMCodeLevelFaultSpec) {
            List<CommandInfo> commandInfoList = new ArrayList<>();
            CommandInfo bmSubmitCommandInfo = new CommandInfo();
            bmSubmitCommandInfo.setCommand(bmSubmitCommand.replace("%s", "-u " + jvmAgentFaultSpec.getInjectionHomeDir()
                    + "/" + jvmAgentFaultSpec.getArgs().get(TASK_ID) + ".btm"));
            bmSubmitCommandInfo.setIgnoreExitValueCheck(true);
            bmSubmitCommandInfo.setExpectedCommandOutputList(
                    Arrays.asList("uninstall RULE " + jvmAgentFaultSpec.getArgs().get(TASK_ID)));
            commandInfoList.add(bmSubmitCommandInfo);
            return commandInfoList;
        } else {
            List<CommandInfo> commandInfoList = new ArrayList<>();

            CommandInfo bmSubmitCommandInfo = new CommandInfo();

            bmSubmitCommandInfo.setIgnoreExitValueCheck(false);
            bmSubmitCommandInfo.setCommand(bmSubmitCommand.replace("%s", "-rf " + FI_ADD_INFO_FAULTID));
            bmSubmitCommandInfo.setIgnoreExitValueCheck(false);
            bmSubmitCommandInfo.setExpectedCommandOutputList(Arrays.asList(REMEDIATION_SUCCESSFUL_STRING));

            CommandInfo bmGetFaultcommndInfo = new CommandInfo();
            bmGetFaultcommndInfo.setCommand(bmSubmitCommand.replace("%s", "-gf " + FI_ADD_INFO_FAULTID));
            bmGetFaultcommndInfo.setNoOfRetries(6);
            bmGetFaultcommndInfo.setRetryInterval(10);
            bmGetFaultcommndInfo.setIgnoreExitValueCheck(false);
            bmGetFaultcommndInfo.setExpectedCommandOutputList(Arrays.asList(FAULT_COMPLETION_STRING));

            commandInfoList.add(bmSubmitCommandInfo);
            commandInfoList.add(bmGetFaultcommndInfo);

            return commandInfoList;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.mangle.services.helpers.IAgentFaultHelper#
     * getAgentFaultInjectionScripts()
     */
    @Override
    public List<SupportScriptInfo> getAgentFaultInjectionScripts(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        return getAgentFaultScripts(jvmAgentFaultSpec.getInjectionHomeDir(), Constants.JVM_AGENT_JAR + "-full.tar.gz");
    }

    @Override
    public void checkTaskSpecificPrerequisites() throws MangleException {
        // Found No Task Specific Requirements
    }

    private String setBytemanInstallCommand(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        String localSocketPort = ((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort();
        String bmInstallCommand = String.format(PID_ATTACH_MXBEANS_COMMAND_WITH_PORT,
                jvmAgentFaultSpec.getInjectionHomeDir(), localSocketPort, jvmAgentFaultSpec.getArgs().get(PROCESS));
        if (null != jvmAgentFaultSpec.getArgs().get(JAVA_HOME_PATH)) {
            bmInstallCommand = Constants.SET_JAVA_HOME_CMD + jvmAgentFaultSpec.getArgs().get(JAVA_HOME_PATH)
                    + ";export PATH=$JAVA_HOME/bin:$PATH;" + bmInstallCommand;
        }
        if (null != jvmAgentFaultSpec.getArgs().get(USER)) {

            bmInstallCommand =
                    "sudo -u " + jvmAgentFaultSpec.getArgs().get(USER) + " bash -c \"" + bmInstallCommand + "\"";
        }
        return bmInstallCommand;
    }

    private String setBytemanSubmitCommand(CommandExecutionFaultSpec jvmAgentFaultSpec) {
        String localSocketPort = ((JVMAgentFaultSpec) jvmAgentFaultSpec).getJvmProperties().getPort();
        String bmSubmitCommand =
                String.format(SUBMIT_COMMAND_WITH_PORT, jvmAgentFaultSpec.getInjectionHomeDir(), localSocketPort) + "%s";
        if (null != jvmAgentFaultSpec.getArgs().get(JAVA_HOME_PATH)) {
            bmSubmitCommand = Constants.SET_JAVA_HOME_CMD + jvmAgentFaultSpec.getArgs().get(JAVA_HOME_PATH)
                    + ";export PATH=$JAVA_HOME/bin:$PATH;" + bmSubmitCommand;
        }
        if (null != jvmAgentFaultSpec.getArgs().get(USER)) {
            bmSubmitCommand =
                    "sudo -u " + jvmAgentFaultSpec.getArgs().get(USER) + " bash -c \"" + bmSubmitCommand + "\"";
        }
        return bmSubmitCommand;
    }

}
