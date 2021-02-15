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

package com.vmware.mangle.plugin.tasks.impl;

import static com.vmware.mangle.utils.constants.FaultConstants.INJECTION_SCRIPTS_FOLDER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.vmware.mangle.cassandra.model.tasks.FaultTask;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandOutputProcessingInfo;
import com.vmware.mangle.plugin.model.faults.specs.HelloMangleFaultSpec;
import com.vmware.mangle.plugin.utils.CustomPluginUtils;
import com.vmware.mangle.task.framework.helpers.AbstractRemoteCommandExecutionTaskHelper;
import com.vmware.mangle.utils.CommandUtils;
import com.vmware.mangle.utils.ConstantsUtils;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Sample Custom Fault Implementation to help Mangle Custom Fault Plugin Developer. The Custom Fault
 * Developer needs to define an Implementation of
 * AbstractRemoteCommandExecutionTaskHelper/AbstractCommandExecutionTaskHelper for each Custom
 * Fault.
 *
 * @author hkilari
 */

@Extension(ordinal = 1)
@Log4j2
public class HelloManglePluginTaskHelper<T extends HelloMangleFaultSpec>
        extends AbstractRemoteCommandExecutionTaskHelper<T> {

    private ICommandExecutor commandExecutor;
    private CustomPluginUtils pluginUtils;

    @Autowired
    public void setPluginUtils(CustomPluginUtils pluginUtils) {
        this.pluginUtils = pluginUtils;
    }

    /**
     * Do not Recommend plugin Developer to modify the default implementation. Default
     * implementation Takes care of Assigning the Injection and remediation Commands Required for
     * the Custom Fault Execution.
     */
    @Override
    public Task<T> init(T faultSpec, String injectionTaskId) {
        Task<T> task = init(new FaultTask<T>(), faultSpec, injectionTaskId);

        if (StringUtils.isEmpty(injectionTaskId)) {
            faultSpec.setInjectionCommandInfoList(getInjectionCommandInfoList(faultSpec));
            faultSpec.setRemediationCommandInfoList(getRemediationcommandInfoList(faultSpec));
        }
        return task;
    }

    /**
     * Do not Recommend plugin Developer to modify the default implementation. Default
     * Implementation Takes care of Assigning the Injection and remediation Commands to Required for
     * the Custom Fault Execution.
     */
    @Override
    public Task<T> init(T faultSpec) throws MangleException {
        return init(new FaultTask<T>(), faultSpec, null);
    }


    /**
     * User expected to customize the method as per the Fault Requirements. Default Implementation,
     * has Injection Command echo text in the Target Machine as per the inputs provided by the User.
     * By default base directory considered as '/tmp' on Fault target. Plugin developer expected to
     * set the {@link HelloMangleFaultSpec#setInjectionHomeDir(String)} explicitly to support the
     * dynamic Home Directory for Mangle Custom Fault Injection
     *
     * @param faultSpec
     * @return
     */
    private List<CommandInfo> getInjectionCommandInfoList(T faultSpec) {
        List<CommandInfo> commandInfoList = new ArrayList<>();
        CommandInfo commandInfo = new CommandInfo();
        commandInfoList.add(commandInfo);
        commandInfo.setCommand("echo \"Injecting Fault. Field-1: " + faultSpec.getField1() + " Field-2: "
                + faultSpec.getField2() + " \"");
        List<CommandOutputProcessingInfo> commandOutputProcessingInfoList = new ArrayList<>();
        CommandOutputProcessingInfo commandOutputProcessingInfo = new CommandOutputProcessingInfo();
        commandOutputProcessingInfoList.add(commandOutputProcessingInfo);
        commandInfo.setCommandOutputProcessingInfoList(null);
        List<String> expectedCommandOutputList = new ArrayList<>();
        expectedCommandOutputList
                .add("Injecting Fault. Field-1: " + faultSpec.getField1() + " Field-2: " + faultSpec.getField2());
        commandInfo.setExpectedCommandOutputList(expectedCommandOutputList);
        commandInfo.setIgnoreExitValueCheck(false);
        commandInfo.setNoOfRetries(2);
        commandInfo.setRetryInterval(1);
        commandInfo.setTimeout(1);
        return commandInfoList;
    }

    /**
     * User expected to customize the method as per the Fault Requirements. Default Implementation,
     * has Remediation Command echo text in the Target Machine as per the inputs provided by the
     * User. By default base directory considered as '/tmp' on Fault target. Plugin developer
     * expected to set the {@link HelloMangleFaultSpec#setInjectionHomeDir(String)} explicitly to
     * support the dynamic Home Directory for Mangle Custom Fault Injection
     *
     * @param faultSpec
     * @return
     */
    private List<CommandInfo> getRemediationcommandInfoList(T faultSpec) {
        List<CommandInfo> commandInfoList = new ArrayList<>();
        CommandInfo commandInfo = new CommandInfo();
        commandInfoList.add(commandInfo);
        commandInfo.setCommand("echo \"Remediating Fault. Field2: $FI_ARG_field2\"");
        List<CommandOutputProcessingInfo> commandOutputProcessingInfoList = new ArrayList<>();
        CommandOutputProcessingInfo commandOutputProcessingInfo = new CommandOutputProcessingInfo();
        commandOutputProcessingInfoList.add(commandOutputProcessingInfo);
        commandInfo.setCommandOutputProcessingInfoList(null);
        List<String> expectedCommandOutputList = new ArrayList<>();
        expectedCommandOutputList.add("Remediating Fault. Field2: " + faultSpec.getField2());
        commandInfo.setExpectedCommandOutputList(expectedCommandOutputList);
        commandInfo.setIgnoreExitValueCheck(false);
        commandInfo.setNoOfRetries(2);
        commandInfo.setRetryInterval(1);
        commandInfo.setTimeout(1);
        return commandInfoList;
    }

    /**
     * User expected to customize the method as per the Fault Requirements. Default Implementation,
     * returns {@link CommandUtils} capable of executing commands in local machine (Mangle). However
     * this won't be sufficient to handle the executions on Remote endpoints. Refer to
     * CustomSystemResourceFaultTaskHelper.getExecutor(Task<T>) in mangle-test-plugin for
     * implementing faults supporting multiple endpoints.
     */
    @Override
    protected ICommandExecutor getExecutor(Task<T> task) throws MangleException {
        if (commandExecutor == null) {
            commandExecutor = new CommandUtils();
        }
        return commandExecutor;
    }


    /**
     * User expected to customize the method as per his Fault Requirements. Default Implementation,
     * returns empty List. However, this won't be sufficient to handle the executions on Remote
     * endpoints. Refer to CustomLinuxSystemResourceFaultHelper.listFaultInjectionScripts(Task<T>
     * task) in mangle-test-plugin for implementing faults requiring Injection
     * scripts/libraries(Java or any) to be copied to the endpoint. The scripts returned here will
     * be passed to prepareEndpoint(Task<T> task, List<SupportScriptInfo>
     * listOfFaultInjectionScripts) method by abstract task execution implementation.
     */
    @Override
    public List<SupportScriptInfo> listFaultInjectionScripts(Task<T> task) {
        return Collections.emptyList();
    }

    /**
     * User expected to customize the method as per his Fault Requirements. However this won't be
     * sufficient to handle the executions on Remote endpoints. Refer to
     * SystemResourceFaultTaskHelper.prepareEndpoint(Task<T> task, List<SupportScriptInfo>
     * listOfFaultInjectionScripts) in mangle-default-plugin for implementing faults requiring
     * endpoint preperation.
     */
    @Override
    protected void prepareEndpoint(Task<T> task, List<SupportScriptInfo> listOfFaultInjectionScripts)
            throws MangleException {
        //No requirement identified
    }

    /**
     * User not expected to change the default Implementation unless he is not happy with default
     * notation.
     */
    @Override
    public String getDescription(Task<T> task) {
        return "Executing Fault: " + task.getTaskData().getFaultName() + " on endpoint: "
                + task.getTaskData().getEndpointName();
    }

    /**
     * User expected to customize the method as per the Fault Requirements. Default Implementation,
     * copy sampleScript.txt from jar resources to temporary directory of local machine (Mangle).
     * For adding a new Script, place the script in InjectionScripts directory of src/main/resources
     * and replace the references of sampleScript.txt in below method to the corresponding name.
     * there is no restriction on the no. of scripts.
     */
    @Override
    protected void checkTaskSpecificPrerequisites(Task<T> task) throws MangleException {
        //No requirement identified
        String scriptName = "sampleScript.txt";
        String filePath = ConstantsUtils.getMangleSupportScriptDirectory() + scriptName;
        pluginUtils.copyFileFromJarToDestination("/" + INJECTION_SCRIPTS_FOLDER + scriptName, filePath);
    }
}
