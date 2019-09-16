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

package com.vmware.mangle.unittest.services.controller;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.plugin.PluginDetails;
import com.vmware.mangle.model.plugin.ExtensionType;
import com.vmware.mangle.services.FileStorageService;
import com.vmware.mangle.services.PluginDetailsService;
import com.vmware.mangle.services.PluginService;
import com.vmware.mangle.services.controller.PluginController;
import com.vmware.mangle.services.helpers.CustomFaultInjectionHelper;
import com.vmware.mangle.services.mockdata.CustomFaultMockData;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;

/**
 * Unit Test cases for PluginController.
 *
 * @author kumargautam
 */
public class PluginControllerTest {

    @Mock
    private PluginService pluginService;
    @Mock
    private FileStorageService storageService;
    @Mock
    private CustomFaultInjectionHelper customFaultInjectionHelper;
    @Mock
    private PluginDetailsService pluginDetailsService;
    @InjectMocks
    private PluginController pluginController;
    private CustomFaultMockData customFaultMockData;
    private String pluginId = "plugin-test";

    @BeforeMethod
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        this.customFaultMockData = new CustomFaultMockData();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.controller.PluginController#getPlugins(java.lang.String, com.vmware.mangle.model.plugin.ExtensionType)}.
     */
    @Test
    public void testGetPlugins() {
        List<String> extNames = new ArrayList<>();
        extNames.add("com.vmware.mangle.TestExt");
        Map<String, Object> extMap = new HashMap<>();
        extMap.put("extensions", extNames);
        when(pluginService.getExtensions(anyString(), any(ExtensionType.class))).thenReturn(extMap);
        ResponseEntity<Map<String, Object>> response = pluginController.getPlugins(pluginId, ExtensionType.TASK);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertNotNull(response.getBody());
        verify(pluginService, times(1)).getExtensions(anyString(), any(ExtensionType.class));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.controller.PluginController#getPlugins(java.lang.String, com.vmware.mangle.model.plugin.ExtensionType)}.
     *
     * @throws MangleException
     */
    @Test
    public void testGetPluginFiles() throws MangleException {
        when(storageService.getFiles()).thenReturn(Arrays.asList("mangle-test-plgin"));
        ResponseEntity<List<String>> response = pluginController.getFiles();
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(response.getBody().get(0), "mangle-test-plgin");
        verify(storageService, times(1)).getFiles();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.controller.PluginController#getExtensions(java.lang.String)}.
     */
    @Test
    public void testGetExtensions() {
        when(pluginService.getExtension(anyString())).thenReturn(null);
        ResponseEntity<String> response = pluginController.getExtensions(pluginId);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        verify(pluginService, times(1)).getExtension(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.controller.PluginController#downloadFile(java.lang.String, javax.servlet.http.HttpServletRequest)}.
     *
     * @throws IOException
     */
    @Test
    public void testDownloadFile() throws IOException {
        Resource resource = mock(Resource.class);
        when(storageService.loadFileAsResource(anyString())).thenReturn(resource);
        doThrow(new IOException()).when(resource).getFile();
        try {
            pluginController.downloadFile(pluginId, new MockHttpServletRequest());
        } catch (MangleRuntimeException e) {
            assertTrue(e.getCause() instanceof IOException);
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.controller.PluginController#getPluginDetails(String)}.
     */
    @Test
    public void testGetPluginDetailsByPluginId() {
        PluginDetails pluginDetails = customFaultMockData.getPluginDetails();
        when(pluginDetailsService.findPluginDetailsByPluginId(anyString())).thenReturn(pluginDetails);
        ResponseEntity<List<PluginDetails>> response = pluginController.getPluginDetails(pluginDetails.getPluginId());
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        verify(pluginDetailsService, times(1)).findPluginDetailsByPluginId(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.controller.PluginController#getPluginDetails(String)}.
     */
    @Test
    public void testGetPluginDetails() {
        PluginDetails pluginDetails = customFaultMockData.getPluginDetails();
        List<PluginDetails> list = new ArrayList<>();
        list.add(pluginDetails);
        when(pluginDetailsService.findAllPluginDetails()).thenReturn(list);
        ResponseEntity<List<PluginDetails>> response = pluginController.getPluginDetails(null);
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        verify(pluginDetailsService, times(1)).findAllPluginDetails();
    }
}