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

package com.vmware.mangle.unittest.services.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPluginManager;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.services.FileStorageService;
import com.vmware.mangle.services.PluginService;
import com.vmware.mangle.services.constants.CommonConstants;
import com.vmware.mangle.task.framework.skeletons.ITaskHelper;

/**
 * Unit Test for PluginService.
 *
 * @author kumargautam
 */
public class PluginServiceTest extends PowerMockTestCase {

    @InjectMocks
    private PluginService pluginService;
    @Mock
    private SpringPluginManager pluginManager;
    @Mock
    private ApplicationContext appContext;
    @Mock
    private FileStorageService storageService;
    private String pluginId = "plugin-test";

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public void tearDownAfterClass() {
        pluginManager = null;
        pluginId = null;
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
    public void tearDown() {
        validateMockitoUsage();
    }

    /**
     * Test method for {@link com.vmware.mangle.service.PluginService#getExtensions()}.
     */
    @Test
    public void testGetPlugins() {
        PluginWrapper pluginWrapper = mock(PluginWrapper.class);
        List<PluginWrapper> lPluginWrappers = new ArrayList<>();
        lPluginWrappers.add(pluginWrapper);
        when(pluginManager.getStartedPlugins()).thenReturn(lPluginWrappers);
        when(pluginWrapper.getPluginId()).thenReturn(pluginId);
        Set<String> extSet = new HashSet<>();
        extSet.add("com.vmware.mangle.TestExt");
        when(pluginManager.getExtensionClassNames(anyString())).thenReturn(extSet);
        when(pluginManager.getPlugins()).thenReturn(lPluginWrappers);
        PluginDescriptor pluginDescriptor = mock(PluginDescriptor.class);
        when(pluginWrapper.getDescriptor()).thenReturn(pluginDescriptor);
        Map<String, Object> actualResult = pluginService.getExtensions();
        Assert.assertTrue(actualResult.size() > 0);
        verify(pluginManager, times(1)).getStartedPlugins();
        verify(pluginManager, times(1)).getPlugins();
        verify(pluginManager, times(2)).getExtensionClassNames(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.PluginService#getExtension(java.lang.String)}.
     */
    @Test
    public void testGetPluginsString() {
        List<ITaskHelper> listFaultInjectionTask = new ArrayList<>();
        ITaskHelper injectionTask = mock(ITaskHelper.class);
        listFaultInjectionTask.add(injectionTask);
        when(pluginManager.getExtensions(eq(ITaskHelper.class))).thenReturn(listFaultInjectionTask);
        when(appContext.getBean(anyString())).thenReturn(null);
        ITaskHelper actualResult = pluginService.getExtension("IFaultInjectionTask");
        Assert.assertNull(actualResult);
        verify(pluginManager, times(1)).getExtensions(eq(ITaskHelper.class));
    }

    /**
     * Test method for {@link com.vmware.mangle.service.PluginService#getExtensionNames()}.
     */
    @Test
    public void testGetExtensionNames() {
        PluginWrapper pluginWrapper = mock(PluginWrapper.class);
        List<PluginWrapper> lPluginWrappers = new ArrayList<>();
        lPluginWrappers.add(pluginWrapper);
        when(pluginManager.getStartedPlugins()).thenReturn(lPluginWrappers);
        when(pluginWrapper.getPluginId()).thenReturn(pluginId);
        Set<String> extSet = new HashSet<>();
        extSet.add("com.vmware.mangle.TestExt");
        when(pluginManager.getExtensionClassNames(anyString())).thenReturn(extSet);
        List<String> actualResult = pluginService.getExtensionNames();
        Assert.assertTrue(actualResult.size() > 0);
        verify(pluginManager, times(1)).getStartedPlugins();
        verify(pluginManager, times(1)).getExtensionClassNames(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.PluginService#loadPlugin(java.lang.String)}.
     */
    @Test(description = "Verifying positive case for test load plugin")
    public void testLoadPluginCase1() {
        when(pluginManager.loadPlugin(any(Path.class))).thenReturn(pluginId);
        doNothing().when(pluginManager).startPlugins();
        Path path = mock(Path.class);
        when(path.resolve(anyString())).thenReturn(path);
        when(storageService.getFileStorageLocation()).thenReturn(path);
        String actualResult = pluginService.loadPlugin(path);
        Assert.assertEquals(actualResult, pluginId);

        verify(pluginManager, times(1)).loadPlugin(any(Path.class));
        verify(pluginManager, times(1)).startPlugins();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.PluginService#loadPlugin(java.lang.String)}.
     */
    @Test(description = "Verifying failure case if loadPlugin throwing exception")
    public void testLoadPluginCase2() {
        when(pluginManager.loadPlugin(any(Path.class))).thenReturn(pluginId);
        doThrow(new RuntimeException("start plugin failed")).when(pluginManager).startPlugins();
        Path path = mock(Path.class);
        when(path.resolve(anyString())).thenReturn(path);
        when(storageService.getFileStorageLocation()).thenReturn(path);
        boolean actualResult = false;
        try {
            pluginService.loadPlugin(path);
        } catch (Exception e) {
            actualResult = true;
        }
        Assert.assertTrue(actualResult);
        verify(pluginManager, times(1)).loadPlugin(any(Path.class));
        verify(pluginManager, times(1)).startPlugins();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.PluginService#loadPlugin(java.lang.String)}.
     */
    @Test(description = "Verifying failure case if loadPlugin returning pluginId as null")
    public void testLoadPluginCase3() {
        when(pluginManager.loadPlugin(any(Path.class))).thenReturn(null);
        doNothing().when(pluginManager).startPlugins();
        Path path = mock(Path.class);
        when(path.resolve(anyString())).thenReturn(path);
        when(storageService.getFileStorageLocation()).thenReturn(path);
        boolean actualResult = false;
        try {
            pluginService.loadPlugin(path);
        } catch (Exception e) {
            actualResult = true;
        }
        Assert.assertTrue(actualResult);
        verify(pluginManager, times(1)).loadPlugin(any(Path.class));
        verify(pluginManager, times(1)).startPlugins();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.PluginService#unloadPlugin(java.lang.String)}.
     */
    @Test(description = "Verifying positive case for test unload plugin")
    public void testUnloadPluginCase1() {
        when(pluginManager.unloadPlugin(anyString())).thenReturn(true);
        boolean actualResult = pluginService.unloadPlugin(pluginId);
        Assert.assertTrue(actualResult);
        verify(pluginManager, times(1)).unloadPlugin(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.PluginService#unloadPlugin(java.lang.String)}.
     */
    @Test(description = "Verifying failure case if unloadPlugin returning false")
    public void testUnloadPluginCase2() {
        when(pluginManager.unloadPlugin(anyString())).thenReturn(false);
        boolean actualResult = false;
        try {
            pluginService.unloadPlugin(pluginId);
        } catch (Exception e) {
            actualResult = true;
        }
        Assert.assertTrue(actualResult);
        verify(pluginManager, times(1)).unloadPlugin(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.PluginService#unloadPlugin(java.lang.String)}.
     */
    @Test(description = "Verifying failure case if unloadPlugin throwing exception")
    public void testUnloadPluginCase3() {
        doThrow(new RuntimeException("unload plugin failed")).when(pluginManager).unloadPlugin(anyString());
        boolean actualResult = false;
        try {
            pluginService.unloadPlugin(pluginId);
        } catch (Exception e) {
            actualResult = true;
        }
        Assert.assertTrue(actualResult);
        verify(pluginManager, times(1)).unloadPlugin(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.PluginService#disablePlugin(java.lang.String)}.
     */
    @Test(description = "Verifying positive case for disablePlugin")
    public void testDisablePluginCase1() {
        when(pluginManager.disablePlugin(anyString())).thenReturn(true);
        boolean actualResult = pluginService.disablePlugin(pluginId);
        Assert.assertTrue(actualResult);
        verify(pluginManager, times(1)).disablePlugin(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.PluginService#disablePlugin(java.lang.String)}.
     */
    @Test(description = "Verifying failure case if disablePlugin returning false")
    public void testDisablePluginCase2() {
        when(pluginManager.disablePlugin(anyString())).thenReturn(false);
        boolean actualResult = false;
        try {
            pluginService.disablePlugin(pluginId);
        } catch (Exception e) {
            actualResult = true;
        }
        Assert.assertTrue(actualResult);
        verify(pluginManager, times(1)).disablePlugin(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.PluginService#disablePlugin(java.lang.String)}.
     */
    @Test(description = "Verifying failure case if disablePlugin throwing exception")
    public void testDisablePluginCase3() {
        doThrow(new RuntimeException("disable plugin failed")).when(pluginManager).disablePlugin(anyString());
        boolean actualResult = false;
        try {
            pluginService.disablePlugin(pluginId);
        } catch (Exception e) {
            actualResult = true;
        }
        Assert.assertTrue(actualResult);
        verify(pluginManager, times(1)).disablePlugin(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.PluginService#enablePlugin(java.lang.String)}.
     */
    @Test(description = "Verifying positive case for enablePlugin")
    public void testEnablePluginCase1() {
        when(pluginManager.enablePlugin(anyString())).thenReturn(true);
        when(pluginManager.startPlugin(anyString())).thenReturn(PluginState.STARTED);
        boolean actualResult = pluginService.enablePlugin(pluginId);
        Assert.assertTrue(actualResult);
        verify(pluginManager, times(1)).enablePlugin(anyString());
        verify(pluginManager, times(1)).startPlugin(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.PluginService#enablePlugin(java.lang.String)}.
     */
    @Test(description = "Verifying failure case if enablePlugin returning false")
    public void testEnablePluginCase2() {
        when(pluginManager.enablePlugin(anyString())).thenReturn(false);
        when(pluginManager.startPlugin(anyString())).thenReturn(PluginState.DISABLED);
        boolean actualResult = false;
        try {
            pluginService.enablePlugin(pluginId);
        } catch (Exception e) {
            actualResult = true;
        }
        Assert.assertTrue(actualResult);
        verify(pluginManager, times(1)).enablePlugin(anyString());
        verify(pluginManager, times(1)).startPlugin(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.PluginService#enablePlugin(java.lang.String)}.
     */
    @Test(description = "Verifying failure case if enablePlugin throwing exception")
    public void testEnablePluginCase3() {
        when(pluginManager.enablePlugin(anyString())).thenReturn(false);
        doThrow(new RuntimeException("start plugin failed")).when(pluginManager).startPlugin(anyString());
        boolean actualResult = false;
        try {
            pluginService.enablePlugin(pluginId);
        } catch (Exception e) {
            actualResult = true;
        }
        Assert.assertTrue(actualResult);
        verify(pluginManager, times(1)).enablePlugin(anyString());
        verify(pluginManager, times(1)).startPlugin(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.PluginService#deletePlugin(java.lang.String)}.
     */
    @Test(description = "Verifying positive case for deletePlugin")
    public void testDeletePluginCase1() {
        when(pluginManager.deletePlugin(anyString())).thenReturn(true);
        boolean actualResult = pluginService.deletePlugin(pluginId);
        Assert.assertTrue(actualResult);
        verify(pluginManager, times(1)).deletePlugin(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.PluginService#deletePlugin(java.lang.String)}.
     */
    @Test(description = "Verifying failure case if deletePlugin returning false")
    public void testDeletePluginCase2() {
        when(pluginManager.deletePlugin(anyString())).thenReturn(false);
        boolean actualResult = false;
        try {
            pluginService.deletePlugin(pluginId);
        } catch (Exception e) {
            actualResult = true;
        }
        Assert.assertTrue(actualResult);
        verify(pluginManager, times(1)).deletePlugin(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.PluginService#deletePlugin(java.lang.String)}.
     */
    @Test(description = "Verifying failure case if deletePlugin throwing exception")
    public void testDeletePluginCase3() {
        doThrow(new RuntimeException("delete plugin failed")).when(pluginManager).deletePlugin(anyString());
        boolean actualResult = false;
        try {
            pluginService.deletePlugin(pluginId);
        } catch (Exception e) {
            actualResult = true;
        }
        Assert.assertTrue(actualResult);
        verify(pluginManager, times(1)).deletePlugin(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.service.PluginService#deletePlugin(java.lang.String)}.
     */
    @Test(description = "Verifying failure case if deletePluginId same as mangle-default-plugin throwing exception")
    public void testDeletePluginCase4() {
        boolean actualResult = false;
        try {
            pluginService.deletePlugin(CommonConstants.DEFAULT_PLUGIN_ID);
        } catch (Exception e) {
            actualResult = true;
        }
        Assert.assertTrue(actualResult);
    }
}
