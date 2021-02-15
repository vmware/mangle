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

package com.vmware.mangle.unittest.services.helpers.fault;

import static org.testng.Assert.assertTrue;

import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.slack.Notifier;
import com.vmware.mangle.services.helpers.slack.NotifierClientFactory;
import com.vmware.mangle.services.mockdata.NotifierMockData;
import com.vmware.mangle.utils.notification.SlackClient;

/**
 * Unit test cases for NotifierClientFactoryTest.
 *
 * @author kumargautam
 */
public class NotifierClientFactoryTest extends PowerMockTestCase {

    @InjectMocks
    private NotifierClientFactory clientFactory;
    private Notifier slackInfo;
    private NotifierMockData slackMockData;

    @BeforeClass
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        this.slackMockData = new NotifierMockData();
        this.slackInfo = slackMockData.getSlackInfo("test");
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.helpers.slack.NotifierClientFactory#getNotificationClient(com.vmware.mangle.cassandra.model.slack.Notifier)}.
     */
    @Test
    public void testGetNotificationClient() {
        assertTrue(clientFactory.getNotificationClient(slackInfo) instanceof SlackClient);
    }
}