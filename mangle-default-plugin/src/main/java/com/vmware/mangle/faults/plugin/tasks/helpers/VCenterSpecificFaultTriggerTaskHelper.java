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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VCenterFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMDiskFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMNicFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMStateFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.FaultTriggeringTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.vcenter.specs.HostFaultSpec;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.events.TaskSubstageEvent;
import com.vmware.mangle.task.framework.helpers.AbstractTaskHelper;
import com.vmware.mangle.task.framework.skeletons.IMultiTaskHelper;
import com.vmware.mangle.task.framework.utils.TaskDescriptionUtils;
import com.vmware.mangle.utils.clients.vcenter.HostOperations;
import com.vmware.mangle.utils.clients.vcenter.VCenterClient;
import com.vmware.mangle.utils.clients.vcenter.VMOperations;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author chetanc
 */
@Extension(ordinal = 1)
@Log4j2
@NoArgsConstructor
public class VCenterSpecificFaultTriggerTaskHelper<T extends VCenterFaultTriggerSpec, S extends CommandExecutionFaultSpec>
        extends AbstractTaskHelper<T> implements IMultiTaskHelper<T, S> {
    private EndpointClientFactory endpointClientFactory;
    private static final String VM_ID_COMMAND = "--vmId";

    private VCenterSpecificFaultTaskHelper<S> vCenterSpecificFaultTaskHelper;

    @Autowired
    public VCenterSpecificFaultTriggerTaskHelper(VCenterSpecificFaultTaskHelper<S> vCenterSpecificFaultTaskHelper,
            EndpointClientFactory endpointClientFactory) {
        this.vCenterSpecificFaultTaskHelper = vCenterSpecificFaultTaskHelper;
        this.endpointClientFactory = endpointClientFactory;
    }

    @Autowired
    public void setvCenterSpecificFaultTaskHelper(VCenterSpecificFaultTaskHelper<S> vCenterSpecificFaultTaskHelper) {
        this.vCenterSpecificFaultTaskHelper = vCenterSpecificFaultTaskHelper;
    }

    @Autowired
    public void setEndpointClientFactory(EndpointClientFactory endpointClientFactory) {
        this.endpointClientFactory = endpointClientFactory;
    }

    @Override
    public Task<T> init(T taskSpec) throws MangleException {
        return init(taskSpec, null);
    }

    @Override
    public Task<T> init(T taskSpec, String injectionId) throws MangleException {
        FaultTriggeringTask<T, S> task = new FaultTriggeringTask<>();
        init(task, taskSpec, injectionId);
        return task;
    }

    @Override
    public Map<String, Task<S>> getChildTasks(Task<T> task) {
        return ((FaultTriggeringTask<T, S>) task).getTaskObjmap();
    }

    @Override
    public boolean isReadyForChildExecution(Task<T> task) {
        return task.getTaskData().isReadyForChildExecution();
    }

    @Override
    public void executeTask(Task<T> task) throws MangleException {
        updateSubstage(task, SubStage.INITIALISED);
        this.getPublisher().publishEvent(new TaskSubstageEvent(task));
        triggerChildTasks(task);
    }

    private void triggerChildTasks(Task<T> task) throws MangleException {
        if (task.getTaskType() == TaskType.INJECTION) {
            if (task.getTaskData().getFaultSpec() instanceof VMFaultSpec) {
                addVMChildTask(task);
            } else {
                addHostChildTasks(task);
            }
        }
        if (task.getTaskType() == TaskType.REMEDIATION) {
            Map<String, String> childTaskMap = new HashMap<>();
            task.getTaskData().setChildTaskMap(childTaskMap);
        }
        task.getTaskData().setReadyForChildExecution(true);
        log.info("Completed Triggering child tasks");
        updateSubstage(task, SubStage.TRIGGER_CHILD_TASKS);
        this.getPublisher().publishEvent(new TaskSubstageEvent(task));
    }

    private void addVMChildTask(Task<T> task) throws MangleException {
        VMFaultSpec faultSpec = (VMFaultSpec) task.getTaskData().getFaultSpec();
        if (faultSpec instanceof VMStateFaultSpec) {
            addVMStateChildTasks(task);
        } else if (faultSpec instanceof VMNicFaultSpec) {
            addVMNicChildTasks(task);
        } else if (faultSpec instanceof VMDiskFaultSpec) {
            addVMDiskChildTasks(task);
        }
    }

    private void addVMStateChildTasks(Task<T> task) throws MangleException {
        VCenterFaultTriggerSpec vCenterFaultTriggerSpec = task.getTaskData();
        List<String> listOfVMs = getVMInstanceIds(vCenterFaultTriggerSpec.getFaultSpec(),
                ((VMStateFaultSpec) vCenterFaultTriggerSpec.getFaultSpec()).getVmName(),
                vCenterFaultTriggerSpec.getEnableRandomInjection());
        Map<String, String> childTaskMap = new HashMap<>();
        ((FaultTriggeringTask<T, S>) task).setTaskObjmap(new HashMap<>());
        int childCounter = 1;
        for (String vmId : listOfVMs) {
            Task<S> childTask;
            S childFaultSpec = createVCenterStateFaultSpec(task, vmId);
            childFaultSpec.setArgs(getVCenterVMStateFaultSpecificArgs(childFaultSpec, vmId));
            childTask = vCenterSpecificFaultTaskHelper.init(childFaultSpec);
            childTask.setTaskName(childTask.getTaskName() + "-" + childCounter++);
            ((FaultTriggeringTask<T, S>) task).getTaskObjmap().put(vmId, childTask);
            childTaskMap.put(vmId, childTask.getId());
        }
        task.setChildTaskIDs(new ArrayList<>(childTaskMap.values()));
        task.getTaskData().setChildTaskMap(childTaskMap);
    }

    private void addVMNicChildTasks(Task<T> task) throws MangleException {
        VCenterFaultTriggerSpec vCenterFaultTriggerSpec = task.getTaskData();
        List<String> listOfVMs = getVMInstanceIds(vCenterFaultTriggerSpec.getFaultSpec(),
                ((VMNicFaultSpec) vCenterFaultTriggerSpec.getFaultSpec()).getVmName(),
                vCenterFaultTriggerSpec.getEnableRandomInjection());

        if (CollectionUtils.isEmpty(listOfVMs)) {
            return;
        }
        String vmId = listOfVMs.get(0);
        List<String> vmNics = getNicIds(vCenterFaultTriggerSpec.getFaultSpec(), vmId,
                vCenterFaultTriggerSpec.getEnableRandomInjection());

        Map<String, String> childTaskMap = new HashMap<>();
        ((FaultTriggeringTask<T, S>) task).setTaskObjmap(new HashMap<>());
        int childCounter = 1;
        for (String nicId : vmNics) {
            Task<S> childTask;
            S childFaultSpec = createVCenterNicFaultSpec(task, vmId);
            childFaultSpec.setArgs(getVCenterVMNicFaultSpecificArgs(childFaultSpec, vmId, nicId));
            childTask = vCenterSpecificFaultTaskHelper.init(childFaultSpec);
            childTask.setTaskName(childTask.getTaskName() + "-" + childCounter++);
            ((FaultTriggeringTask<T, S>) task).getTaskObjmap().put(nicId, childTask);
            childTaskMap.put(nicId, childTask.getId());
        }
        task.setChildTaskIDs(new ArrayList<>(childTaskMap.values()));
        task.getTaskData().setChildTaskMap(childTaskMap);
    }

    private void addVMDiskChildTasks(Task<T> task) throws MangleException {
        VCenterFaultTriggerSpec vCenterFaultTriggerSpec = task.getTaskData();
        List<String> listOfVMs = getVMInstanceIds(vCenterFaultTriggerSpec.getFaultSpec(),
                ((VMDiskFaultSpec) vCenterFaultTriggerSpec.getFaultSpec()).getVmName(),
                vCenterFaultTriggerSpec.getEnableRandomInjection());

        if (CollectionUtils.isEmpty(listOfVMs)) {
            return;
        }
        String vmId = listOfVMs.get(0);
        List<String> vmDisks = getDiskIds(vCenterFaultTriggerSpec.getFaultSpec(), vmId,
                vCenterFaultTriggerSpec.getEnableRandomInjection());

        Map<String, String> childTaskMap = new HashMap<>();
        ((FaultTriggeringTask<T, S>) task).setTaskObjmap(new HashMap<>());
        int childCounter = 1;
        for (String diskId : vmDisks) {
            Task<S> childTask;
            S childFaultSpec = createVCenterDiskFaultSpec(task, vmId);
            childFaultSpec.setArgs(getVCenterVMDiskFaultSpecificArgs(childFaultSpec, vmId, diskId));
            childTask = vCenterSpecificFaultTaskHelper.init(childFaultSpec);
            childTask.setTaskName(childTask.getTaskName() + "-" + childCounter++);
            ((FaultTriggeringTask<T, S>) task).getTaskObjmap().put(diskId, childTask);
            childTaskMap.put(diskId, childTask.getId());
        }
        task.setChildTaskIDs(new ArrayList<>(childTaskMap.values()));
        task.getTaskData().setChildTaskMap(childTaskMap);
    }

    private void addHostChildTasks(Task<T> task) throws MangleException {
        VCenterFaultTriggerSpec vCenterFaultTriggerSpec = task.getTaskData();
        List<String> listOfVMs = getHostInstanceIds(vCenterFaultTriggerSpec.getFaultSpec(),
                vCenterFaultTriggerSpec.getEnableRandomInjection());
        Map<String, String> childTaskMap = new HashMap<>();
        ((FaultTriggeringTask<T, S>) task).setTaskObjmap(new HashMap<>());
        int childCounter = 1;
        for (String vmId : listOfVMs) {
            Task<S> childTask;
            S childFaultSpec = createVCenterHostSpec(task);
            childFaultSpec.setArgs(getVCenterVMStateFaultSpecificArgs(childFaultSpec, vmId));
            childTask = vCenterSpecificFaultTaskHelper.init(childFaultSpec);
            childTask.setTaskName(childTask.getTaskName() + "-" + childCounter++);
            ((FaultTriggeringTask<T, S>) task).getTaskObjmap().put(vmId, childTask);
            childTaskMap.put(vmId, childTask.getId());
        }
        task.setChildTaskIDs(new ArrayList<>(childTaskMap.values()));
        task.getTaskData().setChildTaskMap(childTaskMap);
    }

    private S createVCenterHostSpec(Task<T> task) {
        HostFaultSpec hostFaultSpec = new HostFaultSpec();
        HostFaultSpec parentSpec = (HostFaultSpec) task.getTaskData().getFaultSpec();
        hostFaultSpec.setEnableRandomInjection(parentSpec.isEnableRandomInjection());
        hostFaultSpec.setEndpoint(parentSpec.getEndpoint());
        hostFaultSpec.setFault(parentSpec.getFault());
        hostFaultSpec.setFaultType(parentSpec.getFaultType());
        hostFaultSpec.setFaultName(parentSpec.getFaultName());
        hostFaultSpec.setCredentials(parentSpec.getCredentials());
        hostFaultSpec.setEndpointName(parentSpec.getEndpointName());
        hostFaultSpec.setTags(parentSpec.getTags());
        hostFaultSpec.setFilters(parentSpec.getFilters());
        hostFaultSpec.setTaskName(parentSpec.getTaskName());
        return (S) hostFaultSpec;
    }

    private S createVCenterStateFaultSpec(Task<T> task, String vmId) {
        VMStateFaultSpec VMStateFaultSpec = new VMStateFaultSpec();
        VMStateFaultSpec parentSpec = (VMStateFaultSpec) task.getTaskData().getFaultSpec();
        VMStateFaultSpec.setEnableRandomInjection(parentSpec.isEnableRandomInjection());
        VMStateFaultSpec.setEndpoint(parentSpec.getEndpoint());
        VMStateFaultSpec.setFault(parentSpec.getFault());
        VMStateFaultSpec.setFaultType(parentSpec.getFaultType());
        VMStateFaultSpec.setFaultName(parentSpec.getFaultName());
        VMStateFaultSpec.setCredentials(parentSpec.getCredentials());
        VMStateFaultSpec.setEndpointName(parentSpec.getEndpointName());
        VMStateFaultSpec.setTags(parentSpec.getTags());
        VMStateFaultSpec.setFilters(parentSpec.getFilters());
        VMStateFaultSpec.setTaskName(parentSpec.getTaskName());
        VMStateFaultSpec.setVmName(vmId);
        return (S) VMStateFaultSpec;
    }

    private S createVCenterNicFaultSpec(Task<T> task, String vmId) {
        VMNicFaultSpec VMNicFaultSpec = new VMNicFaultSpec();
        VMNicFaultSpec parentSpec = (VMNicFaultSpec) task.getTaskData().getFaultSpec();
        VMNicFaultSpec.setEnableRandomInjection(parentSpec.isEnableRandomInjection());
        VMNicFaultSpec.setEndpoint(parentSpec.getEndpoint());
        VMNicFaultSpec.setFault(parentSpec.getFault());
        VMNicFaultSpec.setFaultType(parentSpec.getFaultType());
        VMNicFaultSpec.setFaultName(parentSpec.getFaultName());
        VMNicFaultSpec.setCredentials(parentSpec.getCredentials());
        VMNicFaultSpec.setEndpointName(parentSpec.getEndpointName());
        VMNicFaultSpec.setTags(parentSpec.getTags());
        VMNicFaultSpec.setFilters(parentSpec.getFilters());
        VMNicFaultSpec.setTaskName(parentSpec.getTaskName());
        VMNicFaultSpec.setVmName(vmId);
        return (S) VMNicFaultSpec;
    }

    private S createVCenterDiskFaultSpec(Task<T> task, String vmId) {
        VMDiskFaultSpec VMDiskFaultSpec = new VMDiskFaultSpec();
        VMDiskFaultSpec parentSpec = (VMDiskFaultSpec) task.getTaskData().getFaultSpec();
        VMDiskFaultSpec.setEnableRandomInjection(parentSpec.isEnableRandomInjection());
        VMDiskFaultSpec.setEndpoint(parentSpec.getEndpoint());
        VMDiskFaultSpec.setFault(parentSpec.getFault());
        VMDiskFaultSpec.setFaultType(parentSpec.getFaultType());
        VMDiskFaultSpec.setFaultName(parentSpec.getFaultName());
        VMDiskFaultSpec.setCredentials(parentSpec.getCredentials());
        VMDiskFaultSpec.setEndpointName(parentSpec.getEndpointName());
        VMDiskFaultSpec.setTags(parentSpec.getTags());
        VMDiskFaultSpec.setFilters(parentSpec.getFilters());
        VMDiskFaultSpec.setTaskName(parentSpec.getTaskName());
        VMDiskFaultSpec.setVmName(vmId);
        return (S) VMDiskFaultSpec;
    }

    @Override
    public String getDescription(Task<T> task) {
        String description = task.getTaskData().getFaultSpec().getFaultName() + " on VCenter: "
                + task.getTaskData().getFaultSpec().getEndpointName() + ". More Details: [ "
                + task.getTaskData().getFaultSpec() + " ]";
        description = TaskDescriptionUtils.removeNullMembersFromString(description);
        if (task.getTaskType() == TaskType.INJECTION) {
            return "Executing Fault: " + description;
        }
        return "";
    }

    public List<String> getVMInstanceIds(CommandExecutionFaultSpec vmFaultSpec, String vmName, boolean random)
            throws MangleException {
        VCenterClient vCenterAdapterClient = (VCenterClient) endpointClientFactory
                .getEndPointClient(vmFaultSpec.getCredentials(), vmFaultSpec.getEndpoint());
        List<String> vms;

        if (StringUtils.hasText(vmName)) {
            vms = VMOperations.getVMsList(vCenterAdapterClient, vmName, ((VMFaultSpec) vmFaultSpec).getFilters());
        } else {
            vms = VMOperations.getVMsList(vCenterAdapterClient, random, ((VMFaultSpec) vmFaultSpec).getFilters());
        }
        if (CollectionUtils.isEmpty(vms)) {
            throw new MangleException(ErrorCode.VCENTER_VM_NOT_FOUND);
        }
        return vms;
    }

    public List<String> getNicIds(CommandExecutionFaultSpec vmFaultSpec, String vmId, boolean random)
            throws MangleException {
        VCenterClient vCenterAdapterClient = (VCenterClient) endpointClientFactory
                .getEndPointClient(vmFaultSpec.getCredentials(), vmFaultSpec.getEndpoint());
        List<String> vmNics;

        if (StringUtils.hasText(((VMNicFaultSpec) vmFaultSpec).getVmNicId())) {
            return new ArrayList<>(Arrays.asList(((VMNicFaultSpec) vmFaultSpec).getVmNicId()));
        }

        vmNics = VMOperations.getVMNicList(vCenterAdapterClient, vmId, random);

        if (CollectionUtils.isEmpty(vmNics)) {
            throw new MangleException(ErrorCode.VCENTER_VM_NOT_FOUND);
        }
        return vmNics;
    }

    public List<String> getDiskIds(CommandExecutionFaultSpec vmFaultSpec, String vmId, boolean random)
            throws MangleException {
        VCenterClient vCenterAdapterClient = (VCenterClient) endpointClientFactory
                .getEndPointClient(vmFaultSpec.getCredentials(), vmFaultSpec.getEndpoint());
        List<String> vmDisks;

        if (StringUtils.hasText(((VMDiskFaultSpec) vmFaultSpec).getVmDiskId())) {
            return new ArrayList<>(Arrays.asList(((VMDiskFaultSpec) vmFaultSpec).getVmDiskId()));
        }

        vmDisks = VMOperations.getVMDiskList(vCenterAdapterClient, vmId, random);

        if (CollectionUtils.isEmpty(vmDisks)) {
            throw new MangleException(ErrorCode.VCENTER_VM_NOT_FOUND);
        }
        return vmDisks;
    }

    public List<String> getHostInstanceIds(CommandExecutionFaultSpec faultSpec, boolean random) throws MangleException {
        VCenterClient vCenterAdapterClient = (VCenterClient) endpointClientFactory
                .getEndPointClient(faultSpec.getCredentials(), faultSpec.getEndpoint());
        List<String> hosts;
        if (StringUtils.hasText(((HostFaultSpec) faultSpec).getHostName())) {
            hosts = HostOperations.getHostList(vCenterAdapterClient, ((HostFaultSpec) faultSpec).getHostName(),
                    ((HostFaultSpec) faultSpec).getFilters());
        } else {
            hosts = HostOperations.getHostList(vCenterAdapterClient, random, ((HostFaultSpec) faultSpec).getFilters());
        }

        if (CollectionUtils.isEmpty(hosts)) {
            throw new MangleException(ErrorCode.VCENTER_HOST_NOT_FOUND);
        }
        return hosts;
    }

    protected Map<String, String> getVCenterVMStateFaultSpecificArgs(CommandExecutionFaultSpec faultSpec, String vmId) {
        Map<String, String> specificArgs = new LinkedHashMap<>();
        specificArgs.put(VM_ID_COMMAND, vmId);
        if (!CollectionUtils.isEmpty(faultSpec.getArgs())) {
            specificArgs.putAll(faultSpec.getArgs());
        }
        return specificArgs;
    }

    protected Map<String, String> getVCenterVMNicFaultSpecificArgs(CommandExecutionFaultSpec faultSpec, String vmId,
            String vmNicId) {
        Map<String, String> specificArgs = new LinkedHashMap<>();
        specificArgs.put(VM_ID_COMMAND, vmId);
        specificArgs.put("--vmNic", vmNicId);
        if (!CollectionUtils.isEmpty(faultSpec.getArgs())) {
            specificArgs.putAll(faultSpec.getArgs());
        }
        return specificArgs;
    }

    protected Map<String, String> getVCenterVMDiskFaultSpecificArgs(CommandExecutionFaultSpec faultSpec, String vmId,
            String vmNicId) {
        Map<String, String> specificArgs = new LinkedHashMap<>();
        specificArgs.put(VM_ID_COMMAND, vmId);
        specificArgs.put("--vmDisk", vmNicId);
        if (!CollectionUtils.isEmpty(faultSpec.getArgs())) {
            specificArgs.putAll(faultSpec.getArgs());
        }
        return specificArgs;
    }
}
