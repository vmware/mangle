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

package com.vmware.mangle.unittest.faults.plugin.utils;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.faults.plugin.mockdata.FaultsMockData;
import com.vmware.mangle.faults.plugin.utils.PluginUtils;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 *
 *
 * @author jayasankarr
 */

@PrepareForTest(FileUtils.class)
public class PluginUtilsTest extends PowerMockTestCase {

    private SupportScriptInfo faultInjectionScriptInfo;
    private PluginUtils pluginUtils;
    private FaultsMockData mockData;

    @BeforeClass
    public void setUpBeforeClass() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(FileUtils.class);
        pluginUtils = new PluginUtils();
        mockData = new FaultsMockData();
        faultInjectionScriptInfo = mockData.getSupportScriptInfo();
    }

    @Test(priority = 1)
    void testCopyScriptFileToMangleDirectory() throws Exception {
        PowerMockito.doNothing().when(FileUtils.class);
        FileUtils.copyToFile(any(InputStream.class), any(File.class));
        pluginUtils.copyScriptFileToMangleDirectory(faultInjectionScriptInfo);
        PowerMockito.verifyStatic(FileUtils.class, times(1));
    }

    @Test(priority = 2)
    void testScriptNotInClassPathCopyToMangleDirectory() {
        boolean actualResult = false;
        faultInjectionScriptInfo.setClassPathResource(false);
        try {
            pluginUtils.copyScriptFileToMangleDirectory(faultInjectionScriptInfo);
        } catch (MangleException e) {
            actualResult = true;
        }
        Assert.assertTrue(actualResult);

    }


}
