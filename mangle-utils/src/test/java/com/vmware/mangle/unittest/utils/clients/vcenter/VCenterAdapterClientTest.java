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

package com.vmware.mangle.unittest.utils.clients.vcenter;

import static org.mockito.Matchers.anyString;

import java.net.Socket;

import org.junit.Assert;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.VCenterAdapterProperties;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.clients.docker.CustomDockerClient;
import com.vmware.mangle.utils.clients.vcenter.VCenterAdapterClient;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;


/**
 *
 *
 * @author chetanc
 */
@PrepareForTest(value = { CommonUtils.class, VCenterAdapterClient.class })
public class VCenterAdapterClientTest extends PowerMockTestCase {

    private String ip = "10.10.10.10";

    @Mock
    Socket socket;
    @Mock
    CustomDockerClient customDockerClient;

    private VCenterAdapterProperties vCenterAdapterProperties = new VCenterAdapterProperties();

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetInstance() throws Exception {
        PowerMockito.whenNew(CustomDockerClient.class).withAnyArguments().thenReturn(customDockerClient);
        PowerMockito.when(customDockerClient.getDockerIPByName(anyString())).thenReturn(ip);
        VCenterAdapterClient client = new VCenterAdapterClient(vCenterAdapterProperties);
        Assert.assertNotNull(client);
    }

    @Test(expectedExceptions = MangleException.class, enabled = false)
    public void testTestConnectionFailure() throws MangleException {
        try {
            VCenterAdapterClient client = new VCenterAdapterClient(vCenterAdapterProperties);
            client.testConnection();
        } catch (MangleException e) {
            Assert.assertEquals(ErrorCode.INVALID_URL, e.getErrorCode());
            throw e;
        }

    }
}
