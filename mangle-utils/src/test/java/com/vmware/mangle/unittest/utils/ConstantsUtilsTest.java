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

package com.vmware.mangle.unittest.utils;

import lombok.extern.log4j.Log4j2;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.vmware.mangle.utils.ConstantsUtils;
import com.vmware.mangle.utils.constants.Constants;

/**
 *
 *
 * @author chetanc
 */
@Log4j2
public class ConstantsUtilsTest {

    private static String taskId = "id-5";

    /**
     * Test method for {@link com.vmware.mangle.utils.ConstantsUtils#getTasksInfo(String)}
     */
    @Test
    public void testGetTasksInfo() {
        log.info(
                "Executing test method testGetTasksInfo method on com.vmware.mangle.utils.ConstantsUtils#getTasksInfo");
        String result = ConstantsUtils.getTasksInfo(taskId);
        String expected = Constants.TASKS + "/" + taskId + Constants.INFO;
        Assert.assertEquals(result, expected);
    }

    /**
     * Test method for {@link com.vmware.mangle.utils.ConstantsUtils#getTask(String)}
     */
    @Test
    public void testGetTask() {
        log.info("Executing test method testGetTask method on com.vmware.mangle.utils.ConstantsUtils#getTask");
        String result = ConstantsUtils.getTask(taskId);
        String expected = Constants.TASKS + "/" + taskId;
        Assert.assertEquals(result, expected);
    }

    /**
     * Test method for {@link com.vmware.mangle.utils.ConstantsUtils#getRemediateDiskTask(String)}
     */
    @Test
    public void testGetRemediateDiskTask() {
        log.info(
                "Executing test method testGetRemediateDiskTask method on com.vmware.mangle.utils.ConstantsUtils#getRemediateDiskTask");
        String result = ConstantsUtils.getRemediateDiskTask(taskId);
        String expected = Constants.REMEDIATE_DISK + "/" + taskId;
        Assert.assertEquals(result, expected);
    }

    /**
     * Test method for {@link ConstantsUtils#getMangleSupportScriptDirectory()}
     */
    @Test
    public void testGetMangleSupportScriptDirectory() {
        log.info(
                "Executing test method testGetMangleSupportScriptDirectory method on com.vmware.mangle.utils.ConstantsUtils#getMangleSupportScriptDirectory");
        String result = ConstantsUtils.getMangleSupportScriptDirectory();
        String expected = Constants.TEMPORARY_DIRECTORY;
        Assert.assertTrue(result.startsWith(expected));
    }
}
