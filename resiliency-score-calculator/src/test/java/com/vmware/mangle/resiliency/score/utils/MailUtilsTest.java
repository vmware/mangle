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

package com.vmware.mangle.resiliency.score.utils;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.common.MockDataConstants;
import com.vmware.mangle.exception.MangleMailerException;

/**
 * Unit tests for class MailUtils
 * 
 * @author ranjans
 */
@PowerMockIgnore("javax.management.*")
@PrepareForTest({ MailUtils.class, Session.class })
public class MailUtilsTest extends PowerMockTestCase {

    @Mock
    private Session session;
    @Mock
    private Transport transport;

    @BeforeMethod
    public void initTests() throws Exception {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Session.class);
        Mockito.when(Session.getInstance(any(Properties.class))).thenReturn(session);
        Mockito.when(session.getTransport("smtp")).thenReturn(transport);
        PowerMockito.doNothing().when(transport).connect();
        PowerMockito.doNothing().when(transport).sendMessage(any(Message.class), any(Address[].class));
    }

    @AfterMethod
    public void tearDown() {
        validateMockitoUsage();
    }

    @Test
    public void mailTest() throws MangleMailerException, MessagingException {
        String[] recepientAddresses = { MockDataConstants.ANY_STR };
        String[] attachmentPaths = { MockDataConstants.ANY_STR };
        Assert.assertTrue(MailUtils.mail(recepientAddresses, MockDataConstants.ANY_STR, MockDataConstants.ANY_STR,
                attachmentPaths));
        verify(session, times(1)).getTransport("smtp");
        verify(transport, times(1)).connect();
        verify(transport, times(1)).sendMessage(any(Message.class), any(Address[].class));
    }

    @Test
    public void mailTestWithImage() throws MangleMailerException, MessagingException {
        String[] recepientAddresses = { MockDataConstants.ANY_STR };
        String[] attachmentPaths = { MockDataConstants.ANY_STR };
        Map<String, String> images = new HashMap<>();
        images.put(MockDataConstants.ANY_STR, MockDataConstants.ANY_STR);
        Assert.assertTrue(MailUtils.mail(recepientAddresses, MockDataConstants.ANY_STR, MockDataConstants.ANY_STR,
                attachmentPaths, images));
        verify(session, times(1)).getTransport("smtp");
        verify(transport, times(1)).connect();
        verify(transport, times(1)).sendMessage(any(Message.class), any(Address[].class));
    }

}