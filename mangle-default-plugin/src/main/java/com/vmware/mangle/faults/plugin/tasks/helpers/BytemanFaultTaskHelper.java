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

package com.vmware.mangle.faults.plugin.tasks.helpers;

import static com.vmware.mangle.services.dto.AgentRuleConstants.CLASS_NAME;
import static com.vmware.mangle.services.dto.AgentRuleConstants.METHOD_NAME;
import static com.vmware.mangle.services.dto.AgentRuleConstants.RULE_EVENT;
import static com.vmware.mangle.utils.constants.FaultConstants.AGENT_NAME;
import static com.vmware.mangle.utils.constants.FaultConstants.FAULT_NAME_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.FAULT_TYPE;
import static com.vmware.mangle.utils.constants.FaultConstants.JAVA_HOME_PATH;
import static com.vmware.mangle.utils.constants.FaultConstants.LONG_LASTING_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.PROCESS;
import static com.vmware.mangle.utils.constants.FaultConstants.TASK_ID;
import static com.vmware.mangle.utils.constants.FaultConstants.TIMEOUT_IN_MILLI_SEC_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.USER;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j2;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMAgentFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMCodeLevelFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.FaultTask;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.faults.plugin.helpers.byteman.BytemanFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.byteman.BytemanFaultHelperFactory;
import com.vmware.mangle.faults.plugin.utils.PluginUtils;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.helpers.AbstractRemoteCommandExecutionTaskHelper;
import com.vmware.mangle.task.framework.utils.TaskDescriptionUtils;
import com.vmware.mangle.utils.CommandUtils;
import com.vmware.mangle.utils.ConstantsUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.docker.CustomDockerClient;
import com.vmware.mangle.utils.clients.ssh.SSHUtils;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Implementation of AbstractRemoteCommandExecutionTaskHelper to Support Injection of Faults
 * Provided by Byteman.
 *
 * @author bkaranam
 */
@Extension(ordinal = 1)
@Log4j2
public class BytemanFaultTaskHelper<T extends CommandExecutionFaultSpec>
        extends AbstractRemoteCommandExecutionTaskHelper<T> {
    private BytemanFaultHelper bytemanFaultHelper;
    private BytemanFaultHelperFactory bytemanFaultHelperFactory;
    private PluginUtils pluginUtils;
    private EndpointClientFactory endpointClientFactory;


    @Autowired(required = true)
    public void setPluginUtils(PluginUtils pluginUtils) {
        this.pluginUtils = pluginUtils;
    }

    @Autowired(required = true)
    public void setBytemanFaultHelperFactory(BytemanFaultHelperFactory bytemanFaultHelperFactory) {
        this.bytemanFaultHelperFactory = bytemanFaultHelperFactory;
    }

    @Autowired(required = true)
    private void setEndpointClientFactory(EndpointClientFactory endpointClientFactory) {
        this.endpointClientFactory = endpointClientFactory;
    }

    public Task<T> init(T faultSpec) throws MangleException {
        return init(faultSpec, null);
    }

    public Task<T> init(T taskData, String injectedTaskId) throws MangleException {
        Task<T> task = init(new FaultTask<T>(), taskData, injectedTaskId);
        setMandatoryBytemanCommandArgs(task);
        bytemanFaultHelper = bytemanFaultHelperFactory.getHelper(taskData.getEndpoint());
        if (task.getTaskType().equals(TaskType.INJECTION)
                && CollectionUtils.isEmpty(taskData.getInjectionCommandInfoList())) {
            taskData.setInjectionCommandInfoList(bytemanFaultHelper.getInjectionCommandInfoList(task.getTaskData()));
            taskData.setRemediationCommandInfoList(
                    bytemanFaultHelper.getRemediationCommandInfoList(task.getTaskData()));
        }
        return task;
    }

    /**
     * Overridden implementation of getExecutor() to return the executor based on User Input
     *
     * @throws MangleException
     */
    @Override
    protected ICommandExecutor getExecutor(Task<T> task) throws MangleException {
        return bytemanFaultHelper.getExecutor(task.getTaskData());
    }

    @Override
    public String getDescription(Task<T> task) {
        return TaskDescriptionUtils.getDescription(task);
    }

    @Override
    public List<SupportScriptInfo> listFaultInjectionScripts(Task<T> task) {
        return bytemanFaultHelper.getAgentFaultInjectionScripts(task.getTaskData());
    }

    @Override
    protected void checkTaskSpecificPrerequisites(Task<T> task) throws MangleException {
        bytemanFaultHelper.checkTaskSpecificPrerequisites();
    }

    @Override
    protected void prepareEndpoint(Task<T> task, List<SupportScriptInfo> listOfFaultInjectionScripts)
            throws MangleException {
        EndpointSpec endpoint = task.getTaskData().getEndpoint();
        if (!CollectionUtils.isEmpty(listOfFaultInjectionScripts) && task.getTaskType() != TaskType.REMEDIATION
                && endpoint.getEndPointType().equals(EndpointType.MACHINE)) {
            for (SupportScriptInfo faultInjectionScriptInfo : listOfFaultInjectionScripts) {
                String filePath = ConstantsUtils.getMangleSupportScriptDirectory() + File.separator
                        + faultInjectionScriptInfo.getScriptFileName();
                pluginUtils.copyScriptFileToMangleDirectory(faultInjectionScriptInfo);
                SSHUtils sshUtils = (SSHUtils) (endpointClientFactory
                        .getEndPointClient(task.getTaskData().getCredentials(), task.getTaskData().getEndpoint()));
                sshUtils.putFile(filePath, faultInjectionScriptInfo.getTargetDirectoryPath());
                log.info("Copied support script file: " + faultInjectionScriptInfo.getScriptFileName()
                        + " to remote machine");
                commandInfoExecutionHelper.makeExecutable(getExecutor(task), faultInjectionScriptInfo);
            }
        }
        if (!CollectionUtils.isEmpty(listOfFaultInjectionScripts) && task.getTaskType() != TaskType.REMEDIATION
                && endpoint.getEndPointType().equals(EndpointType.DOCKER)) {
            for (SupportScriptInfo faultInjectionScriptInfo : listOfFaultInjectionScripts) {
                String filePath = ConstantsUtils.getMangleSupportScriptDirectory() + File.separator + AGENT_NAME;
                pluginUtils.copyScriptFileToMangleDirectory(faultInjectionScriptInfo);
                CommandUtils.runCommand("chmod u+x " + filePath);
                CustomDockerClient customDockerClient = (CustomDockerClient) (endpointClientFactory
                        .getEndPointClient(task.getTaskData().getCredentials(), task.getTaskData().getEndpoint()));
                customDockerClient.copyFileToContainerByName(
                        ((JVMAgentFaultSpec) task.getTaskData()).getDockerArguments().getContainerName(), filePath,
                        faultInjectionScriptInfo.getTargetDirectoryPath());
                log.info("Copied support script file: " + faultInjectionScriptInfo.getScriptFileName()
                        + " to docker machine");
            }
        }
    }

    private void setMandatoryBytemanCommandArgs(Task<T> task) {
        JVMAgentFaultSpec taskData = (JVMAgentFaultSpec) task.getTaskData();
        Map<String, String> args = new HashMap<>();
        args.put(PROCESS, taskData.getJvmProperties().getJvmprocess());
        if (null != taskData.getJvmProperties().getUser()) {
            args.put(USER, taskData.getJvmProperties().getUser());
        }
        args.put(FAULT_NAME_ARG, taskData.getFaultName());
        args.put(LONG_LASTING_ARG, "false");
        args.put(JAVA_HOME_PATH, taskData.getJvmProperties().getJavaHomePath());
        args.put(TIMEOUT_IN_MILLI_SEC_ARG, String.valueOf(taskData.getTimeoutInMilliseconds()));
        if (taskData instanceof JVMCodeLevelFaultSpec) {
            args.put(CLASS_NAME, ((JVMCodeLevelFaultSpec) taskData).getClassName());
            args.put(METHOD_NAME, ((JVMCodeLevelFaultSpec) taskData).getMethodName());
            args.put(RULE_EVENT, ((JVMCodeLevelFaultSpec) taskData).getRuleEvent());
            args.put(FAULT_TYPE, taskData.getFaultType());
        }
        args.put(TASK_ID, task.getId());
        getArgs(task).putAll(args);
    }


    @Override
    public void executeTask(Task<T> task) throws MangleException {
        bytemanFaultHelper = bytemanFaultHelperFactory.getHelper(task.getTaskData().getEndpoint());
        super.executeTask(task);
    }

}
