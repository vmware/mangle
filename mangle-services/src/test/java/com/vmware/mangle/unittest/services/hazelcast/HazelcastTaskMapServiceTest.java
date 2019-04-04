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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.stubbing.answers.ReturnsArgumentAt;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.services.hazelcast.HazelcastTaskCache;
import com.vmware.mangle.services.hazelcast.HazelcastTaskMapListener;


/**
 *
 *
 * @author chetanc
 */
public class HazelcastTaskMapServiceTest {

    @Mock
    private HazelcastTaskMapListener listener;

    private HazelcastInstance hz;

    @Mock
    IMap<Object, Object> map;

    @InjectMocks
    private HazelcastTaskCache mapService;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddToMap() {
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();

        hz = Mockito.mock(HazelcastInstance.class);
        when(hz.getMap(any())).thenReturn(map);
        when(map.addLocalEntryListener(listener)).thenReturn(key);
        when(map.putIfAbsent(anyString(), anyString())).thenReturn(new ReturnsArgumentAt(1));

        mapService.setHazelcastInstance(hz);
        String result = mapService.addTaskToCache(key, value);
        Assert.assertEquals(result, key);
        verify(map, times(1)).putIfAbsent(anyString(), anyString());
    }

    @Test
    public void testUpdateTaskStatusIfKeyAbsent() {
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();

        hz = Mockito.mock(HazelcastInstance.class);
        when(hz.getMap(any())).thenReturn(map);
        when(map.addLocalEntryListener(listener)).thenReturn(key);
        when(map.replace(anyString(), anyString())).thenReturn(new ReturnsArgumentAt(1));

        mapService.setHazelcastInstance(hz);
        mapService.updateTaskCache(key, value);
        verify(map, times(1)).putIfAbsent(anyString(), anyString());
    }

    @Test
    public void testUpdateTaskStatusIfKeyExists() {
        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();

        hz = Mockito.mock(HazelcastInstance.class);
        when(hz.getMap(any())).thenReturn(map);
        when(map.addLocalEntryListener(listener)).thenReturn(key);
        when(map.containsKey(key)).thenReturn(true);
        when(map.replace(anyString(), anyString())).thenReturn(new ReturnsArgumentAt(1));

        mapService.setHazelcastInstance(hz);
        mapService.updateTaskCache(key, value);
        verify(map, times(1)).replace(anyString(), anyString());
    }

    @Test
    public void testDeleteFromTaskCache() {
        String key = UUID.randomUUID().toString();

        hz = Mockito.mock(HazelcastInstance.class);

        when(hz.getMap(any())).thenReturn(map);
        when(map.addLocalEntryListener(listener)).thenReturn(key);
        when(map.remove(key)).thenReturn("");
        when(map.replace(anyString(), anyString())).thenReturn(new ReturnsArgumentAt(1));

        mapService.setHazelcastInstance(hz);
        mapService.deleteFromTaskCache(key);
        verify(map, times(1)).remove(anyString());
        verify(map, times(0)).replace(anyString(), anyString());
    }


}
