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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.vmware.mangle.services.SupportBundleService;
import com.vmware.mangle.services.controller.SupportBundleController;

/**
 * @author dbhat
 *
 */

public class SupportBundleControllerTest {
    @Mock
    SupportBundleService supportBundleService;

    private MockHttpServletResponse httpResponse;

    @InjectMocks
    SupportBundleController controller;

    @BeforeTest
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        httpResponse = new MockHttpServletResponse();
    }

    @Test
    public void validateCreateSupportBundle() {
        doNothing().when(supportBundleService).getLogZipFile(any());
        controller.getLogBundle(httpResponse);
        Mockito.verify(supportBundleService, times(1)).getLogZipFile(any());
    }

}
