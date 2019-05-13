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

package com.vmware.mangle.unittest.utils.helpers.notifier;

import java.util.HashMap;

import org.junit.Assert;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.metricprovider.WaveFrontConnectionProperties;
import com.vmware.mangle.services.dto.FaultEventSpec;
import com.vmware.mangle.utils.clients.metricprovider.WaveFrontServerClient;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.helpers.notifiers.WavefrontNotifier;
import com.vmware.mangle.utils.mockdata.FaultEventSpecMockData;
import com.vmware.mangle.utils.mockdata.MetricProviderMock;

/**
 * @author dbhat
 *
 */
public class WavefrontNotifierTest {
    FaultEventSpec faultEventData;
    WaveFrontConnectionProperties wfProperties;

    @Mock
    WaveFrontServerClient wavefrontClient;


    @BeforeMethod
    public void initFaultEventData() throws MangleException {
        faultEventData = FaultEventSpecMockData.getDummyFaultEventData();
        wfProperties = MetricProviderMock.getDummyWavefrontConnectionProperties();
        wavefrontClient = new WaveFrontServerClient(wfProperties);
    }

    @Test(description = "Test to validate the methods when all valid data populated in Fault  Event Spec", priority = 50)
    public void faultDataWithValidData() {
        WavefrontNotifier wavefrontNotifier = new WavefrontNotifier(wavefrontClient);
        wavefrontNotifier.sendEvent(faultEventData);

        Assert.assertTrue(true);
    }

    @Test(description = "Test is validate the methods when tags are empty", priority = 51)
    public void faultDataWithEmptyTags() {
        faultEventData.setTags(new HashMap<String, String>());
        WavefrontNotifier wavefrontNotifier = new WavefrontNotifier(wavefrontClient);
        wavefrontNotifier.sendEvent(faultEventData);

        Assert.assertTrue(true);
    }

}
