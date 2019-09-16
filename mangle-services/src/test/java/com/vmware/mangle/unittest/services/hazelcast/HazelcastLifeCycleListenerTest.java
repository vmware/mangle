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

package com.vmware.mangle.unittest.services.hazelcast;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hazelcast.core.LifecycleEvent;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.services.ClusterConfigService;
import com.vmware.mangle.services.MetricProviderService;
import com.vmware.mangle.services.PluginDetailsService;
import com.vmware.mangle.services.commons.ServiceCommonUtils;
import com.vmware.mangle.services.config.ADAuthProvider;
import com.vmware.mangle.services.hazelcast.HazelcastLifeCycleListener;
import com.vmware.mangle.utils.constants.MetricProviderConstants;

/**
 * @author chetanc
 */
public class HazelcastLifeCycleListenerTest {
    @Mock
    private ClusterConfigService configService;

    @Mock
    private ADAuthProvider authProvider;

    @Mock
    private MetricProviderService metricProviderService;

    @Mock
    private PluginDetailsService pluginDetailsService;

    @Mock
    private ApplicationContext applicationContext;

    private HazelcastLifeCycleListener lifeCycleListener;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        lifeCycleListener = new HazelcastLifeCycleListener();
    }

    @Test
    public void testStateChanged() {
        LifecycleEvent event = new LifecycleEvent(LifecycleEvent.LifecycleState.MERGING);
        System.setProperty(MetricProviderConstants.NODE_ADDRESS, "127.0.0.1");
        ServiceCommonUtils.setApplicationContext(applicationContext);

        when(applicationContext.getBean(ClusterConfigService.class)).thenReturn(configService);
        when(applicationContext.getBean(ADAuthProvider.class)).thenReturn(authProvider);
        when(applicationContext.getBean(MetricProviderService.class)).thenReturn(metricProviderService);
        when(applicationContext.getBean(PluginDetailsService.class)).thenReturn(pluginDetailsService);

        doNothing().when(configService).resync(any());
        doNothing().when(authProvider).resync(any());
        doNothing().when(metricProviderService).resync(any());
        doNothing().when(pluginDetailsService).resync(any());

        lifeCycleListener.stateChanged(event);

        verify(configService, times(1)).resync(any());
        verify(authProvider, times(1)).resync(any());
        verify(metricProviderService, times(1)).resync(any());
        verify(pluginDetailsService, times(1)).resync(any());


    }
}
