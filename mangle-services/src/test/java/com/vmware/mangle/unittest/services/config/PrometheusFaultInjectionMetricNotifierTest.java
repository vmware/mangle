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

package com.vmware.mangle.unittest.services.config;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.services.config.PrometheusFaultInjectionMetricNotifier;
import com.vmware.mangle.services.dto.FaultEventSpec;

/**
 * Unit test cases for PrometheusFaultInjectionMetricNotifier.
 *
 * @author kumargautam
 */
@PrepareForTest({ Counter.class })
@PowerMockIgnore({ "javax.management.*" })
public class PrometheusFaultInjectionMetricNotifierTest extends PowerMockTestCase {

    @Mock
    private PrometheusMeterRegistry prometheusMeterRegistry;
    @Mock
    private Meter meter;
    @InjectMocks
    private PrometheusFaultInjectionMetricNotifier prometheusMetricNotifier;

    @BeforeClass
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Counter.class);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.config.PrometheusFaultInjectionMetricNotifier#sendEvent(com.vmware.mangle.services.dto.FaultEventSpec)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSendEvent() {
        FaultEventSpec faultEventSpec = getFaultEventSpec();
        when(prometheusMeterRegistry.remove(any(Meter.class))).thenReturn(meter);
        Counter.Builder builder = mock(Counter.Builder.class);
        PowerMockito.mockStatic(Counter.class);
        when(Counter.builder(anyString())).thenReturn(builder);
        when(builder.description(anyString())).thenReturn(builder);
        when(builder.tags(anyCollection())).thenReturn(builder);
        Counter counter = mock(Counter.class);
        when(builder.register(any(MeterRegistry.class))).thenReturn(counter);
        assertTrue(prometheusMetricNotifier.sendEvent(faultEventSpec));
        verify(builder, times(1)).description(anyString());
        PowerMockito.verifyStatic(Counter.class, times(1));
        Counter.builder(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.config.PrometheusFaultInjectionMetricNotifier#sendEvent(com.vmware.mangle.services.dto.FaultEventSpec)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSendEventForNullTimeout() {
        FaultEventSpec faultEventSpec = getFaultEventSpec();
        faultEventSpec.setTimeoutInMilliseconds(null);
        when(prometheusMeterRegistry.remove(any(Meter.class))).thenReturn(meter);
        Counter.Builder builder = mock(Counter.Builder.class);
        PowerMockito.mockStatic(Counter.class);
        when(Counter.builder(anyString())).thenReturn(builder);
        when(builder.description(anyString())).thenReturn(builder);
        when(builder.tags(anyCollection())).thenReturn(builder);
        Counter counter = mock(Counter.class);
        when(builder.register(any(MeterRegistry.class))).thenReturn(counter);
        assertTrue(prometheusMetricNotifier.sendEvent(faultEventSpec));
        assertTrue(prometheusMetricNotifier.closeEvent(faultEventSpec));
        verify(builder, times(1)).description(anyString());
        PowerMockito.verifyStatic(Counter.class, times(1));
        Counter.builder(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.config.PrometheusFaultInjectionMetricNotifier#sendEvent(com.vmware.mangle.services.dto.FaultEventSpec)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSendEventForRemediationTask() {
        FaultEventSpec faultEventSpec = getFaultEventSpec();
        faultEventSpec.setTimeoutInMilliseconds(null);
        faultEventSpec.setFaultName("cpu-" + TaskType.REMEDIATION.name());
        when(prometheusMeterRegistry.remove(any(Meter.class))).thenReturn(meter);
        Counter.Builder builder = mock(Counter.Builder.class);
        PowerMockito.mockStatic(Counter.class);
        when(Counter.builder(anyString())).thenReturn(builder);
        when(builder.description(anyString())).thenReturn(builder);
        when(builder.tags(anyCollection())).thenReturn(builder);
        Counter counter = mock(Counter.class);
        when(builder.register(any(MeterRegistry.class))).thenReturn(counter);
        assertTrue(prometheusMetricNotifier.sendEvent(faultEventSpec));
        verify(builder, times(1)).description(anyString());
        PowerMockito.verifyStatic(Counter.class, times(1));
        Counter.builder(anyString());
    }

    private FaultEventSpec getFaultEventSpec() {
        String id = UUID.randomUUID().toString();
        FaultEventSpec eventSpec = new FaultEventSpec();
        eventSpec.setFaultDescription("cpu fault test");
        eventSpec.setFaultEndTime("2882322");
        eventSpec.setFaultEndTimeInEpoch(22328328932L);
        eventSpec.setFaultEventClassification("cup_test");
        eventSpec.setFaultEventType(TaskType.INJECTION.name());
        eventSpec.setFaultName("cpu-" + TaskType.INJECTION.name() + "-" + id);
        eventSpec.setFaultStartTime("2882315");
        eventSpec.setFaultStartTimeInEpoch(22328328920L);
        eventSpec.setFaultStatus(TaskStatus.COMPLETED.name());
        Map<String, String> tags = new HashMap<>();
        tags.put("endpoint", "rm-test");
        eventSpec.setTags(tags);
        eventSpec.setTaskId(id);
        eventSpec.setTimeoutInMilliseconds(1000);
        return eventSpec;
    }
}
