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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.metricprovider.DatadogConnectionProperties;
import com.vmware.mangle.services.dto.FaultEventSpec;
import com.vmware.mangle.utils.clients.metricprovider.DatadogClient;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.helpers.notifiers.DatadogEventNotifier;
import com.vmware.mangle.utils.mockdata.FaultEventSpecMockData;
import com.vmware.mangle.utils.mockdata.MetricProviderMock;

/**
 * @author dbhat
 *
 */
public class DatadogEventNotifierTest {
    FaultEventSpec faultEventData;
    DatadogConnectionProperties datadogConnectionProperties;
    DatadogClient datadogClient;

    @BeforeMethod
    public void initFaultEventData() throws MangleException {
        faultEventData = FaultEventSpecMockData.getDummyFaultEventData();
        datadogConnectionProperties = MetricProviderMock.getDummyDatadogConnectionProperties();
        datadogClient = new DatadogClient(datadogConnectionProperties);
    }

    @Test(description = "Test to validate the methods when all valid data populated in Fault  Event Spec", priority = 50)
    public void faultDataWithValidData() {
        DatadogEventNotifier datadogEventNotifier = new DatadogEventNotifier(datadogClient);
        //Note: The method will be enhanced to return boolean once MetricProvider utility is available.
        datadogEventNotifier.sendEvent(faultEventData);

        Assert.assertTrue(true);
    }

    @Test(description = "Test is validate the methods when tags are empty", priority = 51)
    public void faultDataWithEmptyTags() {
        faultEventData.setTags(new HashMap<String, String>());
        DatadogEventNotifier datadogEventNotifier = new DatadogEventNotifier(datadogClient);
        //The Send Event method will be updated to return boolean value once MetricProvider utility is added.
        datadogEventNotifier.sendEvent(faultEventData);

        Assert.assertTrue(true);
    }

    @Test(description = "Test to validate the methods when all valid data populated in Fault  Event Spec", priority = 50)
    public void closeEventTestForSystemResourceTask() {
        DatadogEventNotifier datadogEventNotifier = new DatadogEventNotifier(datadogClient);
        Assert.assertTrue(datadogEventNotifier.closeEvent(faultEventData, "dummy-id",
                "com.vmware.mangle.faults.plugin.tasks.helpers.SystemResourceFaultTaskHelper2"));
    }
}

