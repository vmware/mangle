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

import static com.vmware.mangle.utils.constants.FaultConstants.FAULT_NAME_ARG;
import static com.vmware.mangle.utils.constants.FaultConstants.INJECTION_SCRIPTS_FOLDER;
import static com.vmware.mangle.utils.constants.FaultConstants.TIMEOUT_IN_MILLI_SEC_ARG;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j2;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.vmware.mangle.cassandra.model.endpoint.EndpointSpec;
import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.FaultTask;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.faults.plugin.helpers.systemresource.SystemResourceFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.systemresource.SystemResourceFaultHelperFactory;
import com.vmware.mangle.faults.plugin.helpers.systemresource.SystemResourceFaultUtils;
import com.vmware.mangle.faults.plugin.utils.PluginUtils;
import com.vmware.mangle.faults.plugin.utils.TaskDescriptionUtils;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.enums.FaultName;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.helpers.AbstractRemoteCommandExecutionTaskHelper;
import com.vmware.mangle.utils.ConstantsUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.clients.docker.CustomDockerClient;
import com.vmware.mangle.utils.clients.ssh.SSHUtils;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Implementation of AbstractRemoteCommandExecutionTaskHelper to Support Injection of Faults at
 * infra level
 *
 * @author jayasankarr
 */
@Extension(ordinal = 1)
@Log4j2
public class SystemResourceFaultTaskHelper<T extends CommandExecutionFaultSpec>
        extends AbstractRemoteCommandExecutionTaskHelper<T> {

    private SystemResourceFaultHelper systemResourceFaultHelper;
    private ICommandExecutor commandExecutor = null;
    private EndpointClientFactory endpointClientFactory;
    private SystemResourceFaultHelperFactory systemResourceFaultHelperFactory;
    private SystemResourceFaultUtils systemResourceFaultUtils;
    private PluginUtils pluginUtils;


    @Autowired
    public void setPluginUtils(PluginUtils pluginUtils) {
        this.pluginUtils = pluginUtils;
    }

    @Autowired
    public void setSystemResourceFaultUtils(SystemResourceFaultUtils systemResourceFaultUtils) {
        this.systemResourceFaultUtils = systemResourceFaultUtils;
    }

    @Autowired
    public void setSystemResourceFaultHelperFactory(SystemResourceFaultHelperFactory systemResourceFaultHelperFactory) {
        this.systemResourceFaultHelperFactory = systemResourceFaultHelperFactory;
    }

    @Autowired(required = true)
    private void setEndpointClientFactory(EndpointClientFactory endpointClientFactory) {
        this.endpointClientFactory = endpointClientFactory;
    }

    @Override
    public Task<T> init(T faultSpec) throws MangleException {
        return init(faultSpec, null);
    }

    @Override
    public Task<T> init(T taskData, String injectedTaskId) throws MangleException {
        Task<T> task = init(new FaultTask<T>(), taskData, injectedTaskId);
        setMandatorySystemResourceCommandArgs(task);
        systemResourceFaultHelper = systemResourceFaultHelperFactory.getHelper(taskData.getEndpoint());
        if (task.getTaskType().equals(TaskType.INJECTION)) {
            taskData.setInjectionCommandInfoList(systemResourceFaultHelper.getInjectionCommandInfoList(taskData));
            taskData.setRemediationCommandInfoList(systemResourceFaultHelper.getRemediationcommandInfoList(taskData));
        }
        return task;
    }

    @Override
    public void executeTask(Task<T> task) throws MangleException {
        systemResourceFaultHelper = systemResourceFaultHelperFactory.getHelper(task.getTaskData().getEndpoint());
        super.executeTask(task);
    }


    @Override
    protected ICommandExecutor getExecutor(Task<T> task) throws MangleException {
        if (commandExecutor == null) {
            commandExecutor = systemResourceFaultHelper.getExecutor(task.getTaskData());
        }
        return commandExecutor;
    }

    @Override
    protected void checkTaskSpecificPrerequisites(Task<T> task) throws MangleException {
        String scriptName = systemResourceFaultUtils
                .getScriptNameforFault(FaultName.valueOf(task.getTaskData().getFaultName().toUpperCase()));
        String filePath = ConstantsUtils.getMangleSupportScriptDirectory() + scriptName;
        pluginUtils.copyFileFromJarToDestination("/" + INJECTION_SCRIPTS_FOLDER + scriptName, filePath);
    }

    @Override
    protected void prepareEndpoint(Task<T> task, List<SupportScriptInfo> listOfFaultInjectionScripts)
            throws MangleException {
        EndpointSpec endpoint = task.getTaskData().getEndpoint();
        if (listOfFaultInjectionScripts != null && !listOfFaultInjectionScripts.isEmpty()
                && task.getTaskType() != TaskType.REMEDIATION
                && endpoint.getEndPointType().equals(EndpointType.MACHINE)) {
            for (SupportScriptInfo faultInjectionScriptInfo : listOfFaultInjectionScripts) {
                String filePath = ConstantsUtils.getMangleSupportScriptDirectory()/* + File.separator*/
                        + faultInjectionScriptInfo.getScriptFileName();
                SSHUtils sshUtils = (SSHUtils) (endpointClientFactory
                        .getEndPointClient(task.getTaskData().getCredentials(), task.getTaskData().getEndpoint()));
                StringBuilder copiedMsg = new StringBuilder("Copied Support Script File: ")
                        .append(faultInjectionScriptInfo.getScriptFileName()).append(" to Remote Machine");

                sshUtils.putFile(filePath, faultInjectionScriptInfo.getTargetDirectoryPath());
                log.info(copiedMsg);
                commandInfoExecutionHelper.makeExecutable(getExecutor(task), faultInjectionScriptInfo);
                commandInfoExecutionHelper.runCommands(getExecutor(task), getDefaultRemoteMachinePreperationCommands(),
                        task.getTaskTroubleShootingInfo(), getArgs(task));
            }
        }
        if (listOfFaultInjectionScripts != null && !listOfFaultInjectionScripts.isEmpty()
                && task.getTaskType() != TaskType.REMEDIATION
                && endpoint.getEndPointType().equals(EndpointType.DOCKER)) {
            for (SupportScriptInfo faultInjectionScriptInfo : listOfFaultInjectionScripts) {
                String filePath = ConstantsUtils.getMangleSupportScriptDirectory() + File.separator
                        + faultInjectionScriptInfo.getScriptFileName();
                CustomDockerClient customDockerClient = (CustomDockerClient) (endpointClientFactory
                        .getEndPointClient(task.getTaskData().getCredentials(), task.getTaskData().getEndpoint()));
                customDockerClient.copyFileToContainerByName(task.getTaskData().getDockerArguments().getContainerName(),
                        filePath, faultInjectionScriptInfo.getTargetDirectoryPath());
                log.info("Copied Support Script File: " + faultInjectionScriptInfo.getScriptFileName()
                        + " to Docker Machine");
                commandInfoExecutionHelper.makeExecutable(getExecutor(task), faultInjectionScriptInfo);
            }
        }
    }


    @Override
    public String getDescription(Task<T> task) {
        return TaskDescriptionUtils.getDescription(task);
    }

    @Override
    public List<SupportScriptInfo> listFaultInjectionScripts(Task<T> task) {
        return systemResourceFaultHelper.getFaultInjectionScripts(task.getTaskData());
    }


    private void setMandatorySystemResourceCommandArgs(Task<T> task) {
        if (task.getTaskType().equals(TaskType.INJECTION)) {
            Map<String, String> commandInfo = new HashMap<>();
            task.getTaskData().setInjectionHomeDir(task.getTaskData().getInjectionHomeDir());
            String faultName = task.getTaskData().getFaultName();
            commandInfo.put(FAULT_NAME_ARG, faultName);
            commandInfo.put(TIMEOUT_IN_MILLI_SEC_ARG, String.valueOf(task.getTaskData().getTimeoutInMilliseconds()));
            commandInfo.put("id", task.getId());
            getArgs(task).putAll(commandInfo);
        }
    }
}
