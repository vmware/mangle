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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.vmware.mangle.services.SupportBundleService;

/**
 * @author dbhat
 *
 */

public class SupportBundleServiceTest {
    private String localFileLocation = "src" + File.separator + "main" + File.separator + "resources";
    @Mock
    HttpServletResponse response;
    @Mock
    ServletOutputStream servletStream;
    @Mock
    Environment environment;
    @InjectMocks
    private SupportBundleService supportBundleService;


    private MockHttpServletResponse httpResponse;

    @BeforeTest
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        httpResponse = new MockHttpServletResponse();
    }

    @Test
    public void validateZipFileCreation() throws IOException {
        when(environment.getProperty(anyString())).thenReturn(localFileLocation);
        supportBundleService.getLogZipFile(httpResponse);
        Assert.assertNotNull(httpResponse.getOutputStream());
    }

    @Test(expectedExceptions = { java.lang.IllegalArgumentException.class })
    public void validateZipFileCreationWhenLocationIsNull() throws IOException {
        when(environment.getProperty(anyString())).thenReturn(null);
        supportBundleService.getLogZipFile(httpResponse);
        Assert.assertNotNull(httpResponse.getOutputStream());
    }
}
