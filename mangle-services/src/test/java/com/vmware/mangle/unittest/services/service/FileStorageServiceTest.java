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


import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.web.multipart.MultipartFile;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.services.FileStorageService;
import com.vmware.mangle.services.plugin.config.PluginProperties;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Unit test cases for FileStorageService.
 *
 * @author kumargautam
 */
@PrepareForTest({ Files.class })
public class FileStorageServiceTest extends PowerMockTestCase {

    private FileStorageService fileStorageService;
    @Mock
    private PluginProperties pluginProperties;
    private static final String RESOURCES = "resources";
    private String pathname = new StringBuilder("src").append(File.separator).append("test").append(File.separator)
            .append(RESOURCES).toString();

    @BeforeClass
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Files.class);
        mockFileStorageService();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.FileStorageService#storeFile(org.springframework.web.multipart.MultipartFile)}.
     *
     * @throws MangleException
     */
    @Test
    public void testStoreFileForInvalidCharactersInFileName() throws MangleException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("plugin..test");
        String response = null;
        try {
            response = fileStorageService.storeFile(file);
        } catch (MangleRuntimeException e) {
            assertEquals(e.getErrorCode(), ErrorCode.INVALID_CHAR_IN_FILE_NAME);
        }
        verify(file, times(1)).getOriginalFilename();
        assertNull(response);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.FileStorageService#getFileName(java.lang.String)}.
     */
    @Test
    public void testGetFileName() {
        try {
            fileStorageService.getFileName("xyz.text");
        } catch (MangleRuntimeException e) {
            assertEquals(e.getErrorCode(), ErrorCode.FILE_NAME_NOT_EXIST);
        }
    }

    private void mockFileStorageService() {
        when(pluginProperties.getUploadDir()).thenReturn(pathname);
        File file = new File(pathname);
        Path path = file.toPath();
        try {
            PowerMockito.mockStatic(Files.class);
            when(Files.createDirectories(any(Path.class))).thenReturn(path);
        } catch (IOException e) {
        }
        this.fileStorageService = new FileStorageService(pluginProperties);
    }

}
