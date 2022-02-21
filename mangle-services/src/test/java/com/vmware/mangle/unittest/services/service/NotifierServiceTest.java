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

package com.vmware.mangle.unittest.services.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.slack.Notifier;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.services.NotifierService;
import com.vmware.mangle.services.helpers.slack.NotifierClientFactory;
import com.vmware.mangle.services.mockdata.NotifierMockData;
import com.vmware.mangle.services.repository.NotifierRepository;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;
import com.vmware.mangle.utils.notification.SlackClient;

/**
 * Unit test cases for NotifierService.
 *
 * @author kumargautam
 */
public class NotifierServiceTest extends PowerMockTestCase {

    @Mock
    private NotifierClientFactory clientFactory;
    @Mock
    private NotifierRepository repository;
    @Mock
    private MethodsClient methodsClient;
    @InjectMocks
    private NotifierService notificationService;
    private NotifierMockData slackMockData;
    private Notifier slackInfo;
    private ChatPostMessageResponse chatPostMessageResponse;

    @BeforeClass
    public void setUpBeforeClass() {
        MockitoAnnotations.initMocks(this);
        this.slackMockData = new NotifierMockData();
        this.slackInfo = slackMockData.getSlackInfo("test");
        this.chatPostMessageResponse = slackMockData.getSlackChatPostMessageResponse();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.NotifierService#testConnection(com.vmware.mangle.cassandra.model.slack.Notifier)}.
     *
     * @throws MangleException
     */
    @Test
    public void testTestConnection() throws MangleException {
        SlackClient client = spy(new SlackClient(slackInfo));
        when(clientFactory.getNotificationClient(any(Notifier.class))).thenReturn(client);
        doReturn(methodsClient).when(client).getClient();
        doReturn(true).when(client).testConnection();
        doNothing().when(client).shutdown();
        assertTrue(notificationService.testConnection(slackInfo));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.NotifierService#getByName(java.lang.String)}.
     */
    @Test
    public void testGetByName() {
        when(repository.findByName(anyString())).thenReturn(Optional.of(slackInfo));
        assertEquals(notificationService.getByName(slackInfo.getName()), slackInfo);
        verify(repository, times(1)).findByName(anyString());
    }

    /**
     * Test method for {@link com.vmware.mangle.services.NotifierService#getAllNotificationInfo()}.
     */
    @Test
    public void testGetAllSlackInfo() {
        when(repository.findAll()).thenReturn(Arrays.asList(slackInfo));
        assertTrue(!notificationService.getAllNotificationInfo().isEmpty());
        verify(repository, times(1)).findAll();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.NotifierService#create(com.vmware.mangle.cassandra.model.slack.Notifier)}.
     */
    @Test
    public void testCreate() {
        when(repository.findByName(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Notifier.class))).thenReturn(slackInfo);
        assertEquals(notificationService.create(slackInfo), slackInfo);
        verify(repository, times(1)).findByName(anyString());
        verify(repository, times(1)).save(any(Notifier.class));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.NotifierService#create(com.vmware.mangle.cassandra.model.slack.Notifier)}.
     */
    @Test
    public void testCreateForMangleRuntimeException() {
        when(repository.findByName(anyString())).thenReturn(Optional.of(slackInfo));
        when(repository.save(any(Notifier.class))).thenReturn(slackInfo);
        try {
            notificationService.create(slackInfo);
        } catch (MangleRuntimeException e) {
            assertEquals(e.getErrorCode(), ErrorCode.NOTIFICATION_NAME_ALREADY_EXISTS);
            verify(repository, times(1)).findByName(anyString());
            verify(repository, times(0)).save(any(Notifier.class));
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.NotifierService#update(com.vmware.mangle.cassandra.model.slack.Notifier)}.
     */
    @Test
    public void testUpdate() {
        when(repository.findByName(anyString())).thenReturn(Optional.of(slackInfo));
        when(repository.save(any(Notifier.class))).thenReturn(slackInfo);
        assertEquals(notificationService.update(slackInfo), slackInfo);
        verify(repository, times(1)).findByName(anyString());
        verify(repository, times(1)).save(any(Notifier.class));
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.NotifierService#update(com.vmware.mangle.cassandra.model.slack.Notifier)}.
     */
    @Test
    public void testUpdateForMangleRuntimeException() {
        when(repository.findByName(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Notifier.class))).thenReturn(slackInfo);
        try {
            notificationService.update(slackInfo);
        } catch (MangleRuntimeException e) {
            assertEquals(e.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
            verify(repository, times(1)).findByName(anyString());
            verify(repository, times(0)).save(any(Notifier.class));
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.NotifierService#deleteByNames(java.util.List)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testDeleteByNames() {
        when(repository.findAll()).thenReturn(Arrays.asList(slackInfo));
        doNothing().when(repository).deleteByNameIn(anyCollection());
        try {
            notificationService.deleteByNames(Arrays.asList(slackInfo.getName()));
        } catch (MangleRuntimeException e) {
            assertEquals(e.getErrorCode(), ErrorCode.FAILED_TO_DELETE_NOTIFICATION_NAMES);
            verify(repository, times(1)).findAll();
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.NotifierService#enableSlacks(java.util.List, boolean)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testEnableSlacks() {
        when(repository.findByNameIn(anyCollection())).thenReturn(Arrays.asList(slackInfo));
        when(repository.saveAll(anyList())).thenReturn(Arrays.asList(slackInfo));
        assertTrue(!notificationService.enableSlacks(Arrays.asList(slackInfo.getName()), true).isEmpty());
        verify(repository, times(1)).findByNameIn(anyCollection());
        verify(repository, times(1)).saveAll(anyList());
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.NotifierService#enableSlacks(java.util.List, boolean)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testEnableSlacksForMangleRuntimeException() {
        when(repository.findByNameIn(anyCollection())).thenReturn(Collections.emptyList());
        when(repository.saveAll(anyList())).thenReturn(Arrays.asList(slackInfo));
        try {
            notificationService.enableSlacks(Arrays.asList(slackInfo.getName()), true);
        } catch (MangleRuntimeException e) {
            assertEquals(e.getErrorCode(), ErrorCode.NO_RECORD_FOUND);
            verify(repository, times(1)).findByNameIn(anyCollection());
            verify(repository, times(0)).saveAll(anyList());
        }
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.NotifierService#sendNotification(com.vmware.mangle.cassandra.model.tasks.Task)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSendNotification() throws SlackApiException, IOException {
        Task<CommandExecutionFaultSpec> task = slackMockData.getTask(TaskType.INJECTION, TaskStatus.COMPLETED);
        when(repository.findByNameIn(anyCollection())).thenReturn(Arrays.asList(slackInfo, slackInfo));
        SlackClient client = spy(new SlackClient(slackInfo));
        when(clientFactory.getNotificationClient(any(Notifier.class))).thenReturn(client);
        doReturn(methodsClient).when(client).getClient();
        when(methodsClient.chatPostMessage(any(ChatPostMessageRequest.class))).thenReturn(chatPostMessageResponse);
        doNothing().when(client).shutdown();
        notificationService.sendNotification(task);
        verify(repository, times(1)).findByNameIn(anyCollection());
        verify(client, times(2)).getClient();
        verify(methodsClient, times(2)).chatPostMessage(any(ChatPostMessageRequest.class));
        verify(client, times(2)).shutdown();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.NotifierService#sendNotification(com.vmware.mangle.cassandra.model.tasks.Task)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSendNotificationForFailedTask() throws SlackApiException, IOException {
        Task<CommandExecutionFaultSpec> task = slackMockData.getTask(TaskType.INJECTION, TaskStatus.FAILED);
        task.setTaskData(slackMockData.getFaultsMockData().getK8sCpuJvmAgentFaultSpec());
        task.getTaskData().setNotifierNames(new HashSet<>(Arrays.asList("mangle1")));
        when(repository.findByNameIn(anyCollection())).thenReturn(Arrays.asList(slackInfo, slackInfo));
        SlackClient client = spy(new SlackClient(slackInfo));
        when(clientFactory.getNotificationClient(any(Notifier.class))).thenReturn(client);
        doReturn(methodsClient).when(client).getClient();
        when(methodsClient.chatPostMessage(any(ChatPostMessageRequest.class))).thenReturn(chatPostMessageResponse);
        doNothing().when(client).shutdown();
        notificationService.sendNotification(task);
        verify(repository, times(1)).findByNameIn(anyCollection());
        verify(client, times(2)).getClient();
        verify(methodsClient, times(2)).chatPostMessage(any(ChatPostMessageRequest.class));
        verify(client, times(2)).shutdown();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.NotifierService#sendNotification(com.vmware.mangle.cassandra.model.tasks.Task)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSendNotificationForTimeoutNull() throws SlackApiException, IOException {
        Task<CommandExecutionFaultSpec> task = slackMockData.getTask(TaskType.INJECTION, TaskStatus.FAILED);
        task.setTaskData(slackMockData.getFaultsMockData().getDockerPauseFaultSpec());
        task.getTaskData().setNotifierNames(new HashSet<>(Arrays.asList("mangle")));
        when(repository.findByNameIn(anyCollection())).thenReturn(Arrays.asList(slackInfo, slackInfo));
        SlackClient client = spy(new SlackClient(slackInfo));
        when(clientFactory.getNotificationClient(any(Notifier.class))).thenReturn(client);
        doReturn(methodsClient).when(client).getClient();
        when(methodsClient.chatPostMessage(any(ChatPostMessageRequest.class))).thenReturn(chatPostMessageResponse);
        doNothing().when(client).shutdown();
        notificationService.sendNotification(task);
        verify(repository, times(1)).findByNameIn(anyCollection());
        verify(client, times(2)).getClient();
        verify(methodsClient, times(2)).chatPostMessage(any(ChatPostMessageRequest.class));
        verify(client, times(2)).shutdown();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.NotifierService#sendNotification(com.vmware.mangle.cassandra.model.tasks.Task)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSendNotificationForRemediation() throws SlackApiException, IOException {
        Task<CommandExecutionFaultSpec> task = slackMockData.getTask(TaskType.REMEDIATION, TaskStatus.FAILED);
        task.setTaskData(slackMockData.getFaultsMockData().getK8SCPUFaultSpec());
        task.getTaskData().setNotifierNames(new HashSet<>(Arrays.asList("mangle2")));
        when(repository.findByNameIn(anyCollection())).thenReturn(Arrays.asList(slackInfo, slackInfo));
        SlackClient client = spy(new SlackClient(slackInfo));
        when(clientFactory.getNotificationClient(any(Notifier.class))).thenReturn(client);
        doReturn(methodsClient).when(client).getClient();
        when(methodsClient.chatPostMessage(any(ChatPostMessageRequest.class))).thenReturn(chatPostMessageResponse);
        doNothing().when(client).shutdown();
        notificationService.sendNotification(task);
        verify(repository, times(1)).findByNameIn(anyCollection());
        verify(client, times(2)).getClient();
        verify(methodsClient, times(2)).chatPostMessage(any(ChatPostMessageRequest.class));
        verify(client, times(2)).shutdown();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.NotifierService#sendNotification(com.vmware.mangle.cassandra.model.tasks.Task)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSendNotificationForK8SFaultTriggerSpec() throws SlackApiException, IOException {
        Task<K8SFaultTriggerSpec> task =
                slackMockData.getTaskForK8SFaultTriggerSpec(TaskType.REMEDIATION, TaskStatus.FAILED);
        when(repository.findByNameIn(anyCollection())).thenReturn(Arrays.asList(slackInfo, slackInfo));
        SlackClient client = spy(new SlackClient(slackInfo));
        when(clientFactory.getNotificationClient(any(Notifier.class))).thenReturn(client);
        doReturn(methodsClient).when(client).getClient();
        when(methodsClient.chatPostMessage(any(ChatPostMessageRequest.class))).thenReturn(chatPostMessageResponse);
        doNothing().when(client).shutdown();
        notificationService.sendNotification(task);
        verify(repository, times(1)).findByNameIn(anyCollection());
        verify(client, times(2)).getClient();
        verify(methodsClient, times(2)).chatPostMessage(any(ChatPostMessageRequest.class));
        verify(client, times(2)).shutdown();
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.NotifierService#sendNotification(com.vmware.mangle.cassandra.model.tasks.Task)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSendNotificationForChildTasks() throws SlackApiException, IOException {
        Task<K8SFaultTriggerSpec> task =
                slackMockData.getTaskForK8SFaultTriggerSpec(TaskType.REMEDIATION, TaskStatus.FAILED);
        TaskTrigger trigger = (TaskTrigger) task.getTriggers().peek();
        trigger.setChildTaskIDs(Arrays.asList("643"));
        when(repository.findByNameIn(anyCollection())).thenReturn(Arrays.asList(slackInfo, slackInfo));
        SlackClient client = spy(new SlackClient(slackInfo));
        when(clientFactory.getNotificationClient(any(Notifier.class))).thenReturn(client);
        doReturn(methodsClient).when(client).getClient();
        when(methodsClient.chatPostMessage(any(ChatPostMessageRequest.class))).thenReturn(chatPostMessageResponse);
        doNothing().when(client).shutdown();
        notificationService.sendNotification(task);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.NotifierService#sendNotification(com.vmware.mangle.cassandra.model.tasks.Task)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSendNotificationForNoNotifier() throws SlackApiException, IOException {
        Task<K8SFaultTriggerSpec> task =
                slackMockData.getTaskForK8SFaultTriggerSpec(TaskType.REMEDIATION, TaskStatus.FAILED);
        task.getTaskData().setNotifierNames(null);
        when(repository.findByNameIn(anyCollection())).thenReturn(Arrays.asList(slackInfo, slackInfo));
        SlackClient client = spy(new SlackClient(slackInfo));
        when(clientFactory.getNotificationClient(any(Notifier.class))).thenReturn(client);
        doReturn(methodsClient).when(client).getClient();
        when(methodsClient.chatPostMessage(any(ChatPostMessageRequest.class))).thenReturn(chatPostMessageResponse);
        doNothing().when(client).shutdown();
        notificationService.sendNotification(task);
    }

    /**
     * Test method for
     * {@link com.vmware.mangle.services.NotifierService#sendNotification(com.vmware.mangle.cassandra.model.tasks.Task)}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSendNotificationForTaskDataNull() throws SlackApiException, IOException {
        Task<K8SFaultTriggerSpec> task =
                slackMockData.getTaskForK8SFaultTriggerSpec(TaskType.REMEDIATION, TaskStatus.FAILED);
        task.setTaskData(null);
        when(repository.findByNameIn(anyCollection())).thenReturn(Arrays.asList(slackInfo, slackInfo));
        SlackClient client = spy(new SlackClient(slackInfo));
        when(clientFactory.getNotificationClient(any(Notifier.class))).thenReturn(client);
        doReturn(methodsClient).when(client).getClient();
        when(methodsClient.chatPostMessage(any(ChatPostMessageRequest.class))).thenReturn(chatPostMessageResponse);
        doNothing().when(client).shutdown();
        notificationService.sendNotification(task);
    }
}