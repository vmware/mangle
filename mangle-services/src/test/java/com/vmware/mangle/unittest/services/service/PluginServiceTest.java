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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.pf4j.util.FileUtils;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.services.FileStorageService;
import com.vmware.mangle.services.PluginDetailsService;
import com.vmware.mangle.services.PluginService;
import com.vmware.mangle.services.constants.CommonConstants;
import com.vmware.mangle.task.framework.helpers.faults.AbstractCustomFault;
import com.vmware.mangle.task.framework.skeletons.ITaskHelper;
import com.vmware.mangle.unittest.services.helpers.CustomFault;
import com.vmware.mangle.unittest.services.helpers.CustomTaskHelper;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Unit Test for PluginService.
 *
 * @author kumargautam
 */
@PrepareForTest({ FileUtils.class, Paths.class })
@PowerMockIgnore("javax.management.*")
public class PluginServiceTest extends PowerMockTestCase {

    @InjectMocks
    private PluginService pluginService;
    @Mock
    private SpringPluginManager pluginManager;
    @Mock
    private ApplicationContext appContext;
    @Mock
    private FileStorageService storageService;
    @Mock
    private PluginDetailsService pluginDetailsService;
    private String pluginId = "plugin-test";
    private static final String RESOURCES = "resources";
    private String pathname = new StringBuilder("src").append(File.separator).append("test").append(File.separator)
            .append(RESOURCES).toString();

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Paths.class);
        PowerMockito.mockStatic(FileUtils.class);
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
     * Test method for {@link com.vmware.mangle.services.PluginService#getExtensions()}.
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
     * {@link com.vmware.mangle.services.PluginService#getExtension(java.lang.String)}.
     */
    @SuppressWarnings("rawtypes")
    @Test
    public void testGetPluginsString() {
        List<ITaskHelper> listFaultInjectionTask = new ArrayList<>();
        ITaskHelper<?> injectionTask = mock(ITaskHelper.class);
        listFaultInjectionTask.add(injectionTask);
        when(pluginManager.getExtensions(eq(ITaskHelper.class))).thenReturn(listFaultInjectionTask);
        when(appContext.getBean(anyString())).thenReturn(null);
        ITaskHelper<?> actualResult = pluginService.getExtension("IFaultInjectionTask");
        Assert.assertNull(actualResult);
        verify(pluginManager, times(1)).getExtensions(eq(ITaskHelper.class));
    }

    /**
     * Test method for {@link com.vmware.mangle.services.PluginService#getExtensionNames()}.
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
        Assert.assertTrue(!actualResult.isEmpty());
        verify(pluginManager, times(1)).getStartedPlugins();
        verify(pluginManager, times(1)).getExtensionClassNames(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.PluginService#loadPlugin(java.lang.String)}.
     */
    @Test(description = "Verifying positive case for test load plugin")
    public void testLoadPluginCase1() {
        when(pluginManager.loadPlugin(any(Path.class))).thenReturn(pluginId);
        when(pluginManager.startPlugin(anyString())).thenReturn(PluginState.STARTED);
        Path path = mock(Path.class);
        when(path.resolve(anyString())).thenReturn(path);
        when(storageService.getFileStorageLocation()).thenReturn(path);
        String actualResult = pluginService.loadPlugin(path);
        Assert.assertEquals(actualResult, pluginId);

        verify(pluginManager, times(1)).loadPlugin(any(Path.class));
        verify(pluginManager, times(1)).startPlugin(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.PluginService#loadPlugin(java.lang.String)}.
     */
    @Test(description = "Verifying failure case if loadPlugin throwing exception")
    public void testLoadPluginCase2() {
        when(pluginManager.loadPlugin(any(Path.class))).thenReturn(pluginId);
        doThrow(new RuntimeException("start plugin failed")).when(pluginManager).startPlugin(anyString());
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
        verify(pluginManager, times(1)).startPlugin(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.PluginService#loadPlugin(java.lang.String)}.
     */
    @Test(description = "Verifying failure case if loadPlugin returning pluginId as null")
    public void testLoadPluginCase3() {
        when(pluginManager.loadPlugin(any(Path.class))).thenReturn(null);
        when(pluginManager.startPlugin(anyString())).thenReturn(PluginState.STARTED);
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
        verify(pluginManager, times(1)).startPlugin(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.PluginService#unloadPlugin(java.lang.String)}.
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
     * {@link com.vmware.mangle.services.PluginService#unloadPlugin(java.lang.String)}.
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
     * {@link com.vmware.mangle.services.PluginService#unloadPlugin(java.lang.String)}.
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
     * {@link com.vmware.mangle.services.PluginService#disablePlugin(java.lang.String)}.
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
     * {@link com.vmware.mangle.services.PluginService#disablePlugin(java.lang.String)}.
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
     * {@link com.vmware.mangle.services.PluginService#disablePlugin(java.lang.String)}.
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
     * {@link com.vmware.mangle.services.PluginService#enablePlugin(java.lang.String)}.
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
     * {@link com.vmware.mangle.services.PluginService#enablePlugin(java.lang.String)}.
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
     * {@link com.vmware.mangle.services.PluginService#enablePlugin(java.lang.String)}.
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
     * {@link com.vmware.mangle.services.PluginService#deletePlugin(java.lang.String)}.
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
     * {@link com.vmware.mangle.services.PluginService#deletePlugin(java.lang.String)}.
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
     * {@link com.vmware.mangle.services.PluginService#deletePlugin(java.lang.String)}.
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
     * {@link com.vmware.mangle.services.PluginService#deletePlugin(java.lang.String)}.
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

    /**
     * Test method for
     * {@link com.vmware.mangle.services.PluginService#deletePlugin(java.lang.String)}.
     */
    @Test(description = "DeletePlugin failure case for Invalid plugin id")
    public void testDeletePluginCase5() {
        doThrow(new IllegalArgumentException("Invalid plugin id")).when(pluginManager).deletePlugin(anyString());
        boolean actualResult = false;
        try {
            pluginService.deletePlugin(pluginId);
        } catch (MangleRuntimeException e) {
            assertEquals(ErrorCode.PLUGIN_ID_NOT_LOADED, e.getErrorCode());
            actualResult = true;
        }
        Assert.assertTrue(actualResult);
        verify(pluginManager, times(1)).deletePlugin(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.PluginService#validatePlugin(String pluginName, Path pluginPath)}.
     *
     * @throws IOException
     */
    @SuppressWarnings("rawtypes")
    @Test
    public void testPostValidatePlugin() throws IOException {
        List<ITaskHelper> listFaultInjectionTask = new ArrayList<>();
        ITaskHelper<?> injectionTask = new CustomTaskHelper();
        listFaultInjectionTask.add(injectionTask);
        when(pluginManager.getExtensions(eq(ITaskHelper.class), anyString())).thenReturn(listFaultInjectionTask);
        List<CommandExecutionFaultSpec> listCommandExecutionFaultSpec = new ArrayList<>();
        CommandExecutionFaultSpec commandExecutionFaultSpec = new CommandExecutionFaultSpec();
        listCommandExecutionFaultSpec.add(commandExecutionFaultSpec);
        when(pluginManager.getExtensions(eq(CommandExecutionFaultSpec.class), anyString()))
                .thenReturn(listCommandExecutionFaultSpec);
        List<AbstractCustomFault> listAbstractCustomFault = new ArrayList<>();
        CustomFault abstractCustomFault = new CustomFault();
        listAbstractCustomFault.add(abstractCustomFault);
        when(pluginManager.getExtensions(eq(AbstractCustomFault.class), anyString()))
                .thenReturn(listAbstractCustomFault);

        File file = new File(pathname);
        Path path = file.toPath();
        PowerMockito.mockStatic(Paths.class);
        when(Paths.get(anyString())).thenReturn(path);
        PowerMockito.mockStatic(FileUtils.class);
        when(FileUtils.expandIfZip(any(Path.class))).thenReturn(path);

        assertTrue(pluginService.postValidatePlugin(pluginId, path));
        verify(pluginManager, times(1)).getExtensions(eq(ITaskHelper.class), anyString());
        verify(pluginManager, times(1)).getExtensions(eq(CommandExecutionFaultSpec.class), anyString());
        verify(pluginManager, times(1)).getExtensions(eq(AbstractCustomFault.class), anyString());

    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.PluginService#validatePlugin(String pluginName, Path pluginPath)}.
     */
    @Test
    public void testValidatePluginForDefaultPlugin() {
        File file = new File(pathname);
        Path path = file.toPath();
        assertEquals(pluginService.preValidatePlugin(CommonConstants.DEFAULT_PLUGIN_ID, path),
                CommonConstants.DEFAULT_PLUGIN_ID);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.PluginService#validatePlugin(String pluginName, Path pluginPath)}.
     *
     * @throws Exception
     */
    @Test
    public void testValidatePluginForIllegalArgumentException() throws Exception {
        File file = new File(pathname + File.separatorChar + "test1");
        Path path = mock(Path.class);
        PowerMockito.mockStatic(Paths.class);
        when(Paths.get(anyString())).thenReturn(path);
        PowerMockito.mockStatic(FileUtils.class);
        when(FileUtils.expandIfZip(any(Path.class))).thenReturn(path);

        File file2 = mock(File.class);
        when(path.toFile()).thenReturn(file2);
        when(file2.isDirectory()).thenReturn(true);
        when(file2.isHidden()).thenReturn(true);
        PowerMockito.mockStatic(FileUtils.class);
        when(FileUtils.findWithEnding(any(Path.class), anyString(), anyString(), anyString())).thenReturn(path);
        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.doNothing().when(FileUtils.class, "optimisticDelete", (any(Path.class)));
        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.doNothing().when(FileUtils.class, "delete", (any(Path.class)));
        try {
            pluginService.preValidatePlugin("test1", file.toPath());
        } catch (MangleRuntimeException e) {
            assertEquals(ErrorCode.IO_EXCEPTION, e.getErrorCode());
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.PluginService#validatePlugin(String pluginName, Path pluginPath)}.
     *
     * @throws IOException
     */
    @Test
    public void testValidatePluginForIOException() throws IOException {
        File file = new File("src" + File.separatorChar + "test");
        Path path = file.toPath();
        PowerMockito.mockStatic(Paths.class);
        when(Paths.get(anyString())).thenReturn(path);
        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.doThrow(new IOException()).when(FileUtils.class);
        FileUtils.expandIfZip(any(Path.class));

        try {
            pluginService.preValidatePlugin("test", path);
        } catch (MangleRuntimeException e) {
            assertEquals(ErrorCode.IO_EXCEPTION, e.getErrorCode());
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.PluginService#validatePlugin(String pluginName, Path pluginPath)}.
     *
     * @throws IOException
     */
    @Test
    public void testValidatePluginForClassNotFound() throws IOException {
        File file = new File(pathname);
        Path path = file.toPath();
        PowerMockito.mockStatic(Paths.class);
        when(Paths.get(anyString())).thenReturn(path);
        PowerMockito.mockStatic(FileUtils.class);
        when(FileUtils.expandIfZip(any(Path.class))).thenReturn(path);
        PluginWrapper pluginWrapper = mock(PluginWrapper.class);
        when(pluginWrapper.getPluginPath()).thenReturn(path);
        when(pluginWrapper.getPluginId()).thenReturn(pluginId);
        List<PluginWrapper> lPluginWrappers = new ArrayList<>();
        lPluginWrappers.add(pluginWrapper);
        when(pluginManager.getPlugins()).thenReturn(lPluginWrappers);
        when(pluginManager.deletePlugin(anyString())).thenReturn(true);

        try {
            pluginService.postValidatePlugin(RESOURCES, path);
        } catch (MangleRuntimeException e) {
            assertEquals(ErrorCode.EXTENSION_NOT_FOUND, e.getErrorCode());
            verify(pluginManager, times(1)).getPlugins();
            verify(pluginManager, times(1)).deletePlugin(anyString());
        }
    }

    /**
     * Test method for {@link com.vmware.mangle.services.PluginService#isFileExist(Path)}.
     *
     */
    @Test(expectedExceptions = { IllegalArgumentException.class })
    public void testIsFileExist() {
        Path pluginPath = mock(Path.class);
        File file = mock(File.class);
        when(file.exists()).thenReturn(false);
        when(pluginPath.toFile()).thenReturn(file);
        pluginService.isFileExist(pluginPath);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.PluginService#getPluginDescriptorPath(Path, String)}.
     *
     */
    @Test
    public void testGetPluginDescriptorPath() throws IOException {
        Path pluginPath = mock(Path.class);
        File file = mock(File.class);
        when(file.isDirectory()).thenReturn(false);
        when(pluginPath.toFile()).thenReturn(file);
        PowerMockito.mockStatic(Paths.class);
        when(Paths.get(anyString())).thenReturn(pluginPath);
        PowerMockito.mockStatic(FileUtils.class);
        when(FileUtils.getPath(any(Path.class), anyString())).thenReturn(pluginPath);
        assertNotNull(pluginService.getFilePath(pluginPath, pluginId));
        verify(pluginPath, times(1)).toFile();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.PluginService#getPluginDescriptorPath(Path, String)}.
     *
     */
    @Test
    public void testGetPluginDescriptorPathForIOException() throws IOException {
        Path pluginPath = mock(Path.class);
        File file = mock(File.class);
        when(file.isDirectory()).thenReturn(false);
        when(pluginPath.toFile()).thenReturn(file);
        PowerMockito.mockStatic(Paths.class);
        when(Paths.get(anyString())).thenReturn(pluginPath);
        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.doThrow(new IOException()).when(FileUtils.class);
        FileUtils.getPath(any(Path.class), anyString());
        try {
            pluginService.getFilePath(pluginPath, pluginId);
        } catch (MangleRuntimeException e) {
            assertEquals(ErrorCode.IO_EXCEPTION, e.getErrorCode());
        }
    }

    /**
     * Test method for {@link com.vmware.mangle.services.PluginService#getIdForPluginName(String)}.
     *
     */
    @Test
    public void testGetIdForPluginName() {
        File file = mock(File.class);
        when(file.getName()).thenReturn(pathname);
        Path pluginPath = mock(Path.class);
        when(pluginPath.toFile()).thenReturn(file);
        PluginWrapper pluginWrapper = mock(PluginWrapper.class);
        when(pluginWrapper.getPluginPath()).thenReturn(pluginPath);
        when(pluginWrapper.getPluginId()).thenReturn(pluginId);
        List<PluginWrapper> lPluginWrappers = new ArrayList<>();
        lPluginWrappers.add(pluginWrapper);
        when(pluginManager.getPlugins()).thenReturn(lPluginWrappers);
        try {
            pluginService.getIdForPluginName(pluginId);
        } catch (MangleRuntimeException e) {
            assertEquals(ErrorCode.PLUGIN_NAME_NOT_LOADED, e.getErrorCode());
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.PluginService#getExtensions(String, com.vmware.mangle.model.plugin.ExtensionType)}.
     */
    @Test
    public void testGetExtensions() {
        Set<String> extSet = new HashSet<>();
        extSet.add("com.vmware.mangle.TestExtName");
        when(pluginManager.getExtensionClassNames(anyString())).thenReturn(extSet);
        Map<String, Object> actualResult = pluginService.getExtensions(pluginId, null);
        Assert.assertNotNull(actualResult);
        verify(pluginManager, times(1)).getExtensionClassNames(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.PluginService#getExtensionForModel(String)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetExtensionForModel() throws MangleException {
        List<CommandExecutionFaultSpec> listCommandExecutionFaultSpec = new ArrayList<>();
        CommandExecutionFaultSpec commandExecutionFaultSpec = new CommandExecutionFaultSpec();
        listCommandExecutionFaultSpec.add(commandExecutionFaultSpec);
        when(pluginManager.getExtensions(eq(CommandExecutionFaultSpec.class)))
                .thenReturn(listCommandExecutionFaultSpec);
        CommandExecutionFaultSpec actualResult = pluginService
                .getExtensionForModel("com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec");
        Assert.assertNotNull(actualResult);
        verify(pluginManager, times(1)).getExtensions(eq(CommandExecutionFaultSpec.class));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.PluginService#getExtensionForModel(String)}.
     */
    @Test
    public void testGetExtensionForModelWithMangleException() {
        List<CommandExecutionFaultSpec> listCommandExecutionFaultSpec = new ArrayList<>();
        CommandExecutionFaultSpec commandExecutionFaultSpec = new CommandExecutionFaultSpec();
        listCommandExecutionFaultSpec.add(commandExecutionFaultSpec);
        when(pluginManager.getExtensions(eq(CommandExecutionFaultSpec.class)))
                .thenReturn(listCommandExecutionFaultSpec);
        try {
            pluginService
                    .getExtensionForModel("com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec1");
        } catch (MangleException e) {
            assertEquals(ErrorCode.EXTENSION_NOT_FOUND, e.getErrorCode());
            verify(pluginManager, times(1)).getExtensions(eq(CommandExecutionFaultSpec.class));
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.PluginService#getExtensionForCustomFault(String)}.
     */
    @Test
    public void testGetExtensionForCustomFaultWithMangleException() {
        List<AbstractCustomFault> listAbstractCustomFault = new ArrayList<>();
        AbstractCustomFault customFault = mock(AbstractCustomFault.class);
        listAbstractCustomFault.add(customFault);
        when(pluginManager.getExtensions(eq(AbstractCustomFault.class))).thenReturn(listAbstractCustomFault);
        try {
            pluginService
                    .getExtensionForCustomFault("com.vmware.mangle.task.framework.helpers.faults.AbstractCustomFault");
        } catch (MangleException e) {
            assertEquals(ErrorCode.EXTENSION_NOT_FOUND, e.getErrorCode());
            verify(pluginManager, times(1)).getExtensions(eq(AbstractCustomFault.class));
        }
    }

    /**
     * Test method for {@link com.vmware.mangle.services.PluginService#deletePluginFile(Path)}.
     *
     * @throws Exception
     */
    @Test
    public void testDeletePluginFile() throws Exception {
        File file = mock(File.class);
        when(file.isDirectory()).thenReturn(true);
        when(file.isHidden()).thenReturn(false);
        Path pluginPath = mock(Path.class);
        when(pluginPath.toFile()).thenReturn(file);
        PowerMockito.mockStatic(FileUtils.class);
        when(FileUtils.findWithEnding(any(Path.class), anyString(), anyString(), anyString())).thenReturn(pluginPath);
        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.doNothing().when(FileUtils.class, "optimisticDelete", (any(Path.class)));

        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.doThrow(new NoSuchFileException("src/test")).when(FileUtils.class);
        FileUtils.delete(any(Path.class));
        assertFalse(pluginService.deletePluginFile(pluginPath));
    }
}