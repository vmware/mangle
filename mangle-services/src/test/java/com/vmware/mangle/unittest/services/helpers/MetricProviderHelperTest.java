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

package com.vmware.mangle.unittest.services.helpers;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Stack;
import java.util.UUID;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CpuFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.metricprovider.MetricProviderSpec;
import com.vmware.mangle.cassandra.model.tasks.RemediableTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.services.MetricProviderService;
import com.vmware.mangle.services.dto.FaultEventSpec;
import com.vmware.mangle.services.helpers.MetricProviderHelper;
import com.vmware.mangle.services.mockdata.FaultEventMockData;
import com.vmware.mangle.services.mockdata.FaultsMockData;
import com.vmware.mangle.services.mockdata.MetricProviderMockData;
import com.vmware.mangle.services.mockdata.TasksMockData;
import com.vmware.mangle.task.framework.metric.providers.MetricProviderClientFactory;
import com.vmware.mangle.utils.PopulateFaultEventData;
import com.vmware.mangle.utils.clients.metricprovider.MetricProviderClient;
import com.vmware.mangle.utils.clients.metricprovider.WaveFrontServerClient;
import com.vmware.mangle.utils.helpers.notifiers.WavefrontNotifier;

/**
 * @author dbhat
 */
@PrepareForTest(MetricProviderHelper.class)
@PowerMockIgnore("javax.management.*")
public class MetricProviderHelperTest extends PowerMockTestCase {

    @Mock
    MetricProviderClientFactory metricProviderFactory;
    @Mock
    MetricProviderService metricProviderService;
    @Mock
    MetricProviderClient metricProviderClient;
    @Mock
    WavefrontNotifier wavefrontNotifier;
    @InjectMocks
    MetricProviderHelper metricProviderHelper;
    @Mock
    PopulateFaultEventData populateFaultEventData;
    @Mock
    TaskTrigger trigger;
    @Mock
    WaveFrontServerClient waveFrontServerClient;

    private FaultsMockData faultsMockData;
    private TasksMockData<TaskSpec> tasksMockData;
    private MetricProviderMockData mockData;
    private MetricProviderSpec wavefrontSpec;
    private FaultEventMockData faultEventSpecMock;
    private K8SFaultTriggerSpec k8sFaultTriggerSpec;

    @BeforeClass
    public void initMockData() {
        faultsMockData = new FaultsMockData();
        mockData = new MetricProviderMockData();
        faultEventSpecMock = new FaultEventMockData();
    }

    @BeforeMethod
    public void initTest() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(description = " Validate send event gets called for K8S task")
    public void sendEventForFaultInjectionTask() throws Exception {
        K8SFaultTriggerSpec faultSpec = faultsMockData.getK8SCPUFaultTriggerSpec();
        tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> task = tasksMockData.getDummyTask();
        task.setTaskType(TaskType.INJECTION);
        wavefrontSpec = mockData.metricProviderWavefront();
        FaultEventSpec faultEventInfo = faultEventSpecMock.getFaultEventMockData();

        when(metricProviderService.getActiveMetricProvider()).thenReturn(wavefrontSpec);
        when(metricProviderFactory.getMetricProviderClient(wavefrontSpec)).thenReturn(waveFrontServerClient);
        PowerMockito.whenNew(WavefrontNotifier.class).withArguments(waveFrontServerClient)
                .thenReturn(wavefrontNotifier);
        when(populateFaultEventData.getFaultEventSpec()).thenReturn(faultEventInfo);
        when(wavefrontNotifier.sendEvent(faultEventInfo)).thenReturn(true);

        metricProviderHelper.sendFaultEvent(task);
        verify(wavefrontNotifier, times(1)).sendEvent(any(FaultEventSpec.class));
        verify(wavefrontNotifier, times(0)).closeEvent(any(String.class));
    }

    @Test(description = " Validate Send event when fault event spec is null")
    public void sendEventWhenFaultEventIsNull() throws Exception {
        CpuFaultSpec faultSpec = faultsMockData.getK8SCPUFaultSpec();
        faultSpec.setEndpoint(null);
        tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> task = tasksMockData.getDummyTask();
        task.setTaskType(TaskType.INJECTION);
        wavefrontSpec = mockData.metricProviderWavefront();
        FaultEventSpec faultEventInfo = faultEventSpecMock.getFaultEventMockData();

        when(metricProviderService.getActiveMetricProvider()).thenReturn(wavefrontSpec);
        when(metricProviderFactory.getMetricProviderClient(wavefrontSpec)).thenReturn(waveFrontServerClient);
        PowerMockito.whenNew(WavefrontNotifier.class).withArguments(waveFrontServerClient)
                .thenReturn(wavefrontNotifier);
        when(wavefrontNotifier.sendEvent(faultEventInfo)).thenReturn(true);

        metricProviderHelper.sendFaultEvent(task);
        verify(wavefrontNotifier, times(0)).sendEvent(any(FaultEventSpec.class));
        verify(wavefrontNotifier, times(0)).closeEvent(any(String.class));
    }

    @Test(description = " Validate Send event when there are no active metric providers in the system")
    public void sendEventWhenNoActiveMetricProviders() throws Exception {
        CpuFaultSpec faultSpec = faultsMockData.getK8SCPUFaultSpec();
        faultSpec.setEndpoint(null);
        tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> task = tasksMockData.getDummyTask();
        task.setTaskType(TaskType.INJECTION);
        wavefrontSpec = mockData.metricProviderWavefront();
        FaultEventSpec faultEventInfo = faultEventSpecMock.getFaultEventMockData();

        when(metricProviderService.getActiveMetricProvider()).thenReturn(null);
        when(metricProviderFactory.getMetricProviderClient(wavefrontSpec)).thenReturn(waveFrontServerClient);
        PowerMockito.whenNew(WavefrontNotifier.class).withArguments(waveFrontServerClient)
                .thenReturn(wavefrontNotifier);
        when(wavefrontNotifier.sendEvent(faultEventInfo)).thenReturn(true);

        metricProviderHelper.sendFaultEvent(task);
        verify(wavefrontNotifier, times(0)).sendEvent(any(FaultEventSpec.class));
        verify(wavefrontNotifier, times(0)).closeEvent(any(String.class));
    }

    @Test(description = " Validate Send event for remediation task")
    public void sendEventForRemediationTask() throws Exception {
        wavefrontSpec = mockData.metricProviderWavefront();
        CpuFaultSpec faultSpec = faultsMockData.getK8SCPUFaultSpec();
        tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> task = tasksMockData.getRemediationTask();
        ((RemediableTask) task).setInjectionTaskId(UUID.randomUUID().toString());
        FaultEventSpec faultEventInfo = faultEventSpecMock.getFaultEventMockData();

        when(metricProviderService.getActiveMetricProvider()).thenReturn(wavefrontSpec);
        when(metricProviderFactory.getMetricProviderClient(wavefrontSpec)).thenReturn(waveFrontServerClient);
        PowerMockito.whenNew(WavefrontNotifier.class).withArguments(waveFrontServerClient)
                .thenReturn(wavefrontNotifier);
        when(wavefrontNotifier.sendEvent(faultEventInfo)).thenReturn(true);
        when(wavefrontNotifier.closeEvent(any(String.class))).thenReturn(true);

        metricProviderHelper.sendFaultEvent(task);
        verify(wavefrontNotifier, times(1)).sendEvent(any(FaultEventSpec.class));
        verify(wavefrontNotifier, times(1)).closeEvent(any(String.class));
    }

    @Test(description = "Validate not sending event for parent Tasks ")
    public void sendEventForParentTask() throws Exception {
        wavefrontSpec = mockData.metricProviderWavefront();
        CpuFaultSpec faultSpec = faultsMockData.getK8SCPUFaultSpec();
        faultSpec.setEndpoint(null);
        tasksMockData = new TasksMockData<>(faultSpec);
        Task<TaskSpec> task = tasksMockData.getDummyTask();

        Stack<TaskTrigger> triggers = new Stack<TaskTrigger>();
        TaskTrigger trigger = task.getTriggers().peek();
        trigger.setChildTaskIDs(new ArrayList<String>());
        triggers.add(trigger);
        task.setTriggers(triggers);

        FaultEventSpec faultEventInfo = faultEventSpecMock.getFaultEventMockData();

        when(metricProviderService.getActiveMetricProvider()).thenReturn(wavefrontSpec);
        when(metricProviderFactory.getMetricProviderClient(wavefrontSpec)).thenReturn(waveFrontServerClient);
        PowerMockito.whenNew(WavefrontNotifier.class).withArguments(waveFrontServerClient)
                .thenReturn(wavefrontNotifier);
        when(wavefrontNotifier.sendEvent(faultEventInfo)).thenReturn(true);
        when(wavefrontNotifier.closeEvent(any(String.class))).thenReturn(true);

        metricProviderHelper.sendFaultEvent(task);
        verify(wavefrontNotifier, times(0)).sendEvent(any(FaultEventSpec.class));
        verify(wavefrontNotifier, times(0)).closeEvent(any(String.class));
    }
}
