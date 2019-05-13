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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.endpoint.VCenterAdapterProperties;
import com.vmware.mangle.utils.clients.vcenter.VCenterAdapterClient;
import com.vmware.mangle.utils.clients.vcenter.VCenterClient;
import com.vmware.mangle.utils.clients.vcenter.VMOperations;

/**
 *
 *
 * @author chetanc
 */
@PrepareForTest(value = { VCenterClient.class, VMOperations.class })
public class VCenterClientTest extends PowerMockTestCase {

    @Mock
    private VCenterAdapterClient vCenterAdapterClient;

    private VCenterAdapterProperties vCenterAdapterProperties = new VCenterAdapterProperties();

    private VCenterClient vCenterClient;

    @BeforeClass
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testVcenterClientInstantiation() throws Exception {
        whenNew(VCenterAdapterClient.class).withAnyArguments().thenReturn(vCenterAdapterClient);
        vCenterClient = new VCenterClient("", "", "", vCenterAdapterProperties);
        Assert.assertEquals(vCenterClient.getVCenterSpec().getVcUsername(), "");
        Assert.assertEquals(vCenterClient.getVCenterSpec().getVcPassword(), "");
        Assert.assertEquals(vCenterClient.getVCenterSpec().getVcServerUrl(), "");
        Assert.assertEquals(vCenterClient.getVCenterAdapterClient(), vCenterAdapterClient);

    }

    @Test
    public void testTestConnection() throws Exception {
        mockStatic(VMOperations.class);

        when(vCenterAdapterClient.testConnection()).thenReturn(true);
        when(VMOperations.testConnection(any(), any())).thenReturn(true);
        whenNew(VCenterAdapterClient.class).withAnyArguments().thenReturn(vCenterAdapterClient);
        vCenterClient = new VCenterClient("", "", "", vCenterAdapterProperties);

        boolean result = vCenterClient.testConnection();
        Assert.assertTrue(result);

    }


}
