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

package com.vmware.mangle.unittest.faults.plugin.tasks.helpers;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.JVMAgentFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VCenterFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMDiskFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMNicFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.VMStateFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.faults.plugin.helpers.byteman.BytemanFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.byteman.BytemanFaultHelperFactory;
import com.vmware.mangle.faults.plugin.helpers.systemresource.SystemResourceFaultHelper;
import com.vmware.mangle.faults.plugin.helpers.systemresource.SystemResourceFaultHelperFactory;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.faults.plugin.tasks.helpers.BytemanFaultTaskHelper;
import com.vmware.mangle.faults.plugin.tasks.helpers.SystemResourceFaultTaskHelper;
import com.vmware.mangle.faults.plugin.tasks.helpers.VCenterSpecificFaultTaskHelper;
import com.vmware.mangle.faults.plugin.tasks.helpers.VCenterSpecificFaultTriggerTaskHelper;
import com.vmware.mangle.model.vcenter.VCenterSpec;
import com.vmware.mangle.model.vcenter.VM;
import com.vmware.mangle.model.vcenter.VMDisk;
import com.vmware.mangle.model.vcenter.VMNic;
import com.vmware.mangle.task.framework.endpoint.EndpointClientFactory;
import com.vmware.mangle.task.framework.events.TaskSubstageEvent;
import com.vmware.mangle.task.framework.helpers.CommandInfoExecutionHelper;
import com.vmware.mangle.unittest.faults.plugin.helpers.k8s.K8sFaultHelperTest;
import com.vmware.mangle.utils.clients.kubernetes.KubernetesCommandLineClient;
import com.vmware.mangle.utils.clients.kubernetes.PODClient;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.clients.vcenter.VCenterAdapterClient;
import com.vmware.mangle.utils.clients.vcenter.VCenterClient;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author hkilari
 *
 */
@Log4j2
public class VCenterSpecificFaultTriggerTaskHelperTest {
    FaultsMockData faultsMockData = new FaultsMockData();
    private VCenterSpecificFaultTaskHelper<VMStateFaultSpec> vCenterSpecificFaultTaskHelper =
            new VCenterSpecificFaultTaskHelper<>();
    @Mock
    private BytemanFaultHelperFactory bytemanFaultHelperFactory;
    @Mock
    private BytemanFaultHelper bytemanFaultHelper;
    @Mock
    private SystemResourceFaultHelper systemResourceFaultHelper;
    @Mock
    private SystemResourceFaultHelperFactory systemResourceFaultHelperFactory;

    @Mock
    ApplicationEventPublisher publisher;
    @Mock
    EndpointClientFactory endpointClientFactory;
    @Mock
    KubernetesCommandLineClient kubernetesCommandLineClient;
    @Spy
    BytemanFaultTaskHelper<JVMAgentFaultSpec> bytemanFaultTask;
    @Spy
    SystemResourceFaultTaskHelper<JVMAgentFaultSpec> systemResourceFaultTask;
    @Spy
    CommandInfoExecutionHelper commandInfoExecutionHelper;
    @Mock
    PODClient podClient;
    @Mock
    private VCenterClient vCenterClient;
    @Mock
    private VCenterAdapterClient vCenterAdapterClient;

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInitOfInjection() throws MangleException {
        VCenterSpecificFaultTriggerTaskHelper<VCenterFaultTriggerSpec, VMStateFaultSpec> injectionTask =
                new VCenterSpecificFaultTriggerTaskHelper<VCenterFaultTriggerSpec, VMStateFaultSpec>(
                        vCenterSpecificFaultTaskHelper, endpointClientFactory);

        VCenterFaultTriggerSpec faulTriggerTask = faultsMockData.getVcenterFaultTriggerSpecForVMstateFault();
        Task<VCenterFaultTriggerSpec> task = injectionTask.init(faulTriggerTask, null);
        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        Assert.assertEquals(task.getTaskData().getFaultSpec(), faulTriggerTask.getFaultSpec());
        Assert.assertEquals(task.getTaskDescription(),
                "Executing Fault: POWEROFF_VM on VCenter: null. More Details: [ VMStateFaultSpec(vmName=vm, fault=POWEROFF_VM) ]");
        task.getTriggers().add(new TaskTrigger());
    }

    @Test
    public void testExecutionOfVMStateFault() throws MangleException {
        VCenterSpecificFaultTriggerTaskHelper<VCenterFaultTriggerSpec, VMStateFaultSpec> injectionTask =
                new VCenterSpecificFaultTriggerTaskHelper<VCenterFaultTriggerSpec, VMStateFaultSpec>(
                        vCenterSpecificFaultTaskHelper, endpointClientFactory);
        VCenterFaultTriggerSpec faulTriggerTask = faultsMockData.getVcenterFaultTriggerSpecForVMstateFault();
        Task<VCenterFaultTriggerSpec> task = injectionTask.init(faulTriggerTask, null);

        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        Assert.assertEquals(task.getTaskDescription(),
                "Executing Fault: POWEROFF_VM on VCenter: null. More Details: [ VMStateFaultSpec(vmName=vm, fault=POWEROFF_VM) ]");
        task.getTriggers().add(new TaskTrigger());
        injectionTask.setEventPublisher(publisher);
        Mockito.when(endpointClientFactory.getEndPointClient(task.getTaskData().getFaultSpec().getCredentials(),
                task.getTaskData().getFaultSpec().getEndpoint())).thenReturn(vCenterClient);
        Mockito.when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        VCenterSpec vCenterSpec = new VCenterSpec("vcServerUrl", "vcUsername", "vcPassword");
        Mockito.when(vCenterClient.getVCenterSpec()).thenReturn(vCenterSpec);
        ResponseEntity reponseEntity = new ResponseEntity<>(getVMList(), HttpStatus.OK);
        Mockito.when(vCenterAdapterClient.post("mangle-vc-adapter/api/v1/vcenter/vm/vm",
                RestTemplateWrapper.objectToGson(vCenterSpec), Object.class)).thenReturn(reponseEntity);
        Mockito.when(kubernetesCommandLineClient.getPODClient()).thenReturn(podClient);
        Mockito.when(podClient.getPodsWithLabels(Mockito.any())).thenReturn(K8sFaultHelperTest.getPodsAsList());
        Mockito.when(bytemanFaultHelperFactory.getHelper(Mockito.any())).thenReturn(bytemanFaultHelper);

        injectionTask.executeTask(task);
        log.info(RestTemplateWrapper.objectToJson(task));
        Assert.assertEquals(injectionTask.getChildTasks(task).size(), 1);
        Assert.assertEquals(injectionTask.isReadyForChildExecution(task), true);
        Assert.assertEquals(task.getTaskSubstage(), "TRIGGER_CHILD_TASKS");
        Mockito.verify(publisher, Mockito.times(2)).publishEvent(Mockito.any(TaskSubstageEvent.class));
    }

    private Object getVMList() {
        List<VM> vmIdList = new ArrayList<>();
        VM vm = new VM();
        vm.setName("vm");
        vm.setVm("vm-100");
        vmIdList.add(vm);
        return vmIdList;
    }

    @Test
    public void testExecutionOfVMDiskFault() throws MangleException {
        VCenterSpecificFaultTaskHelper<VMDiskFaultSpec> vCenterVMDiskFaultTaskHelper =
                new VCenterSpecificFaultTaskHelper<>();

        VCenterSpecificFaultTriggerTaskHelper<VCenterFaultTriggerSpec, VMDiskFaultSpec> injectionTask =
                new VCenterSpecificFaultTriggerTaskHelper<VCenterFaultTriggerSpec, VMDiskFaultSpec>(
                        vCenterVMDiskFaultTaskHelper, endpointClientFactory);
        VCenterFaultTriggerSpec faulTriggerTask = faultsMockData.getVcenterFaultTriggerSpecForVMDiskFault();
        Task<VCenterFaultTriggerSpec> task = injectionTask.init(faulTriggerTask, null);

        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        Assert.assertEquals(task.getTaskDescription(),
                "Executing Fault: DISCONNECT_DISK on VCenter: null. More Details: "
                        + "[ VMDiskFaultSpec(super=VMFaultSpec(enableRandomInjection=true, filters={}), vmName=vm, fault=DISCONNECT_DISK) ]");
        task.getTriggers().add(new TaskTrigger());
        injectionTask.setEventPublisher(publisher);
        Mockito.when(endpointClientFactory.getEndPointClient(task.getTaskData().getFaultSpec().getCredentials(),
                task.getTaskData().getFaultSpec().getEndpoint())).thenReturn(vCenterClient);
        Mockito.when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        VCenterSpec vCenterSpec = new VCenterSpec("vcServerUrl", "vcUsername", "vcPassword");
        Mockito.when(vCenterClient.getVCenterSpec()).thenReturn(vCenterSpec);
        ResponseEntity reponseEntity = new ResponseEntity<>(getVMList(), HttpStatus.OK);
        Mockito.when(vCenterAdapterClient.post("mangle-vc-adapter/api/v1/vcenter/vm/vm",
                RestTemplateWrapper.objectToGson(vCenterSpec), Object.class)).thenReturn(reponseEntity);
        ResponseEntity diskReponseEntity = new ResponseEntity<>(getVMDiskList(), HttpStatus.OK);
        Mockito.when(vCenterAdapterClient.post("mangle-vc-adapter/api/v1/vcenter/vm/vm-100/disk",
                RestTemplateWrapper.objectToGson(vCenterSpec), Object.class)).thenReturn(diskReponseEntity);

        Mockito.when(kubernetesCommandLineClient.getPODClient()).thenReturn(podClient);
        Mockito.when(podClient.getPodsWithLabels(Mockito.any())).thenReturn(K8sFaultHelperTest.getPodsAsList());
        Mockito.when(bytemanFaultHelperFactory.getHelper(Mockito.any())).thenReturn(bytemanFaultHelper);

        injectionTask.executeTask(task);
        log.info(RestTemplateWrapper.objectToJson(task));
        Assert.assertEquals(injectionTask.getChildTasks(task).size(), 1);
        Assert.assertEquals(injectionTask.isReadyForChildExecution(task), true);
        Assert.assertEquals(task.getTaskSubstage(), "TRIGGER_CHILD_TASKS");
        Mockito.verify(publisher, Mockito.times(2)).publishEvent(Mockito.any(TaskSubstageEvent.class));
    }


    private List<VMDisk> getVMDiskList() {
        List<VMDisk> list = new ArrayList<VMDisk>();
        VMDisk vmDisk = new VMDisk();
        vmDisk.setDisk("2000");
        list.add(vmDisk);
        return list;
    }

    private List<VMNic> getVMNicList() {
        List<VMNic> list = new ArrayList<VMNic>();
        VMNic vmNic = new VMNic();
        vmNic.setNic("4000");
        list.add(vmNic);
        return list;
    }

    @Test
    public void testExecutionOfVMNicFault() throws MangleException {
        VCenterSpecificFaultTaskHelper<VMNicFaultSpec> vCenterVMNicFaultTaskHelper =
                new VCenterSpecificFaultTaskHelper<>();
        ;
        VCenterSpecificFaultTriggerTaskHelper<VCenterFaultTriggerSpec, VMNicFaultSpec> injectionTask =
                new VCenterSpecificFaultTriggerTaskHelper<VCenterFaultTriggerSpec, VMNicFaultSpec>(
                        vCenterVMNicFaultTaskHelper, endpointClientFactory);
        VCenterFaultTriggerSpec faulTriggerTask = faultsMockData.getVcenterFaultTriggerSpecForVMNicFault();
        Task<VCenterFaultTriggerSpec> task = injectionTask.init(faulTriggerTask, null);

        Assert.assertTrue(task.isInitialized());
        Assert.assertEquals(task.getTaskType(), TaskType.INJECTION);
        Assert.assertEquals(task.getTaskDescription(), "Executing Fault: DISCONNECT_NIC on VCenter: null. "
                + "More Details: [ VMNicFaultSpec(vmName=vm, fault=DISCONNECT_NIC) ]");
        task.getTriggers().add(new TaskTrigger());
        injectionTask.setEventPublisher(publisher);
        Mockito.when(endpointClientFactory.getEndPointClient(task.getTaskData().getFaultSpec().getCredentials(),
                task.getTaskData().getFaultSpec().getEndpoint())).thenReturn(vCenterClient);
        Mockito.when(vCenterClient.getVCenterAdapterClient()).thenReturn(vCenterAdapterClient);
        VCenterSpec vCenterSpec = new VCenterSpec("vcServerUrl", "vcUsername", "vcPassword");
        Mockito.when(vCenterClient.getVCenterSpec()).thenReturn(vCenterSpec);
        ResponseEntity reponseEntity = new ResponseEntity<>(getVMList(), HttpStatus.OK);
        Mockito.when(vCenterAdapterClient.post("mangle-vc-adapter/api/v1/vcenter/vm/vm",
                RestTemplateWrapper.objectToGson(vCenterSpec), Object.class)).thenReturn(reponseEntity);
        ResponseEntity nicReponseEntity = new ResponseEntity<>(getVMNicList(), HttpStatus.OK);
        Mockito.when(vCenterAdapterClient.post("mangle-vc-adapter/api/v1/vcenter/vm/vm-100/nic",
                RestTemplateWrapper.objectToGson(vCenterSpec), Object.class)).thenReturn(nicReponseEntity);
        Mockito.when(kubernetesCommandLineClient.getPODClient()).thenReturn(podClient);
        Mockito.when(podClient.getPodsWithLabels(Mockito.any())).thenReturn(K8sFaultHelperTest.getPodsAsList());
        Mockito.when(bytemanFaultHelperFactory.getHelper(Mockito.any())).thenReturn(bytemanFaultHelper);

        injectionTask.executeTask(task);
        log.info(RestTemplateWrapper.objectToJson(task));
        Assert.assertEquals(injectionTask.getChildTasks(task).size(), 1);
        Assert.assertEquals(injectionTask.isReadyForChildExecution(task), true);
        Assert.assertEquals(task.getTaskSubstage(), "TRIGGER_CHILD_TASKS");
        Mockito.verify(publisher, Mockito.times(2)).publishEvent(Mockito.any(TaskSubstageEvent.class));
    }
}