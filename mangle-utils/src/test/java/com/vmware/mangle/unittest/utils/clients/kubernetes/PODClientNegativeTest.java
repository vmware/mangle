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

package com.vmware.mangle.unittest.utils.clients.kubernetes;

import static org.mockito.Matchers.anyString;

import java.util.List;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.utils.CommandUtils;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.clients.kubernetes.KubernetesCommandLineClient;
import com.vmware.mangle.utils.mockdata.CommandResultUtils;


/**
 * @author hkilari
 *
 */
@PrepareForTest(value = { CommonUtils.class, CommandUtils.class })
@PowerMockIgnore({ "com.sun.org.apache.xalan.internal.xsltc.trax.*" })
public class PODClientNegativeTest extends PowerMockTestCase {
    private KubernetesCommandLineClient client;

    @BeforeMethod
    public void init() {
        PowerMockito.mockStatic(Thread.class);
        PowerMockito.mockStatic(CommandUtils.class);
        client = KubernetesCommandLineClient.getClient();

    }

    @Test(description = "retrieve the pods that match the given labels, should return empty list if there doesn't exist any pod matching the label")
    public void testGetPodsWithLabels2() throws Exception {
        Mockito.when(CommandUtils.runCommand(anyString())).thenReturn(CommandResultUtils.getCommandResult(""));
        PowerMockito.spy(Thread.class);
        PowerMockito.doNothing().when(Thread.class);
        Thread.sleep(Mockito.anyLong());
        List<String> result = client.getPODClient().getPodsWithLabels("saas");
        Assert.assertEquals(result.size(), 0);
    }

    @Test(description = "retrieve the pods that match the given labels, should return empty list if there is error while executing the command")
    public void testGetPodsWithLabels3() throws Exception {
        Mockito.when(CommandUtils.runCommand(anyString())).thenReturn(
                CommandResultUtils.getCommandResult("error: the server doesn't have a resource type \"pods\""));
        PowerMockito.spy(Thread.class);
        PowerMockito.doNothing().when(Thread.class);
        Thread.sleep(Mockito.anyLong());
        List<String> result = client.getPODClient().getPodsWithLabels("saas");
        Assert.assertEquals(result.size(), 0);
    }


}
