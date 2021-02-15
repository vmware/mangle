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
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.testng.PowerMockTestCase;
import org.springframework.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.slack.Notifier;
import com.vmware.mangle.services.NotifierService;
import com.vmware.mangle.services.controller.NotifierController;
import com.vmware.mangle.services.mockdata.NotifierMockData;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Unit test cases for NotifierController.
 *
 * @author kumargautam
 */
public class NotifierControllerTest extends PowerMockTestCase {

    @Mock
    private NotifierService notificationService;
    @InjectMocks
    private NotifierController controller;
    private NotifierMockData slackMockData;
    private Notifier slackInfo;

    @BeforeClass
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        this.slackMockData = new NotifierMockData();
        this.slackInfo = slackMockData.getSlackInfo("test");
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.controller.NotifierController#getNotifier(java.lang.String)}.
     */
    @Test
    public void testGetSlackInfo() {
        when(notificationService.getAllNotificationInfo()).thenReturn(Arrays.asList(slackInfo));
        assertTrue(controller.getNotifier(null).getStatusCode().equals(HttpStatus.OK));
        verify(notificationService, times(1)).getAllNotificationInfo();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.controller.NotifierController#getNotifier(java.lang.String)}.
     */
    @Test
    public void testGetSlackInfoByName() {
        when(notificationService.getByName(anyString())).thenReturn(slackInfo);
        assertTrue(controller.getNotifier(slackInfo.getName()).getStatusCode().equals(HttpStatus.OK));
        verify(notificationService, times(1)).getByName(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.controller.NotifierController#createNotifier(com.vmware.mangle.cassandra.model.slack.Notifier)}.
     *
     * @throws MangleException
     */
    @Test
    public void testCreateSlackInfo() throws MangleException {
        when(notificationService.testConnection(any(Notifier.class))).thenReturn(true);
        when(notificationService.create(any(Notifier.class))).thenReturn(slackInfo);
        assertTrue(controller.createNotifier(slackInfo).getStatusCode().equals(HttpStatus.OK));
        verify(notificationService, times(1)).testConnection(any(Notifier.class));
        verify(notificationService, times(1)).create(any(Notifier.class));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.controller.NotifierController#updateNotificationInfo(com.vmware.mangle.cassandra.model.slack.Notifier)}.
     *
     * @throws MangleException
     */
    @Test
    public void testUpdateSlackInfo() throws MangleException {
        when(notificationService.testConnection(any(Notifier.class))).thenReturn(true);
        when(notificationService.update(any(Notifier.class))).thenReturn(slackInfo);
        assertTrue(controller.updateNotifier(slackInfo).getStatusCode().equals(HttpStatus.OK));
        verify(notificationService, times(1)).testConnection(any(Notifier.class));
        verify(notificationService, times(1)).update(any(Notifier.class));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.controller.NotifierController#deleteNotifierByNames(java.util.List)}.
     *
     * @throws MangleException
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testDeleteSlackInfoByNames() throws MangleException {
        when(notificationService.deleteByNames(anyList())).thenReturn(true);
        assertTrue(controller.deleteNotifierByNames(Arrays.asList(slackInfo.getName())).getStatusCode()
                .equals(HttpStatus.NO_CONTENT));
        verify(notificationService, times(1)).deleteByNames(anyList());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.controller.NotifierController#testConnection(com.vmware.mangle.cassandra.model.slack.Notifier)}.
     *
     * @throws MangleException
     */
    @Test
    public void testTestConnectionSlackInfo() throws MangleException {
        when(notificationService.testConnection(any(Notifier.class))).thenReturn(true);
        assertTrue(controller.testConnection(slackInfo).getStatusCode().equals(HttpStatus.OK));
        verify(notificationService, times(1)).testConnection(any(Notifier.class));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.controller.NotifierController#testConnection(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test
    public void testTestConnectionString() throws MangleException {
        when(notificationService.testConnection(any(Notifier.class))).thenReturn(true);
        when(notificationService.getByName(anyString())).thenReturn(slackInfo);
        assertTrue(controller.testConnection(slackInfo.getName()).getStatusCode().equals(HttpStatus.OK));
        verify(notificationService, times(1)).testConnection(any(Notifier.class));
        verify(notificationService, times(1)).getByName(anyString());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.controller.NotifierController#testConnection(java.lang.String)}.
     *
     * @throws MangleException
     */
    @Test
    public void testTestConnectionStringForMangleRuntimeException() throws MangleException {
        when(notificationService.testConnection(any(Notifier.class))).thenReturn(true);
        when(notificationService.getByName(anyString())).thenReturn(null);
        try {
            controller.testConnection(slackInfo.getName());
        } catch (MangleRuntimeException e) {
            assertEquals(e.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
            verify(notificationService, times(0)).testConnection(any(Notifier.class));
            verify(notificationService, times(1)).getByName(anyString());
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.controller.NotifierController#enableNotifier(java.util.List, java.lang.Boolean)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testEnableSlack() {
        when(notificationService.enableSlacks(anyList(), anyBoolean())).thenReturn(Arrays.asList(slackInfo.getName()));
        assertTrue(controller.enableNotifier(Arrays.asList(slackInfo.getName()), true).getStatusCode()
                .equals(HttpStatus.OK));
        verify(notificationService, times(1)).enableSlacks(anyList(), anyBoolean());
    }
}