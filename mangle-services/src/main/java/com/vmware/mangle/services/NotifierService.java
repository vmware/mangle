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

package com.vmware.mangle.services;

import java.util.List;
import java.util.stream.Collectors;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.slack.Notifier;
import com.vmware.mangle.cassandra.model.slack.NotifierType;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.services.helpers.slack.NotifierClientFactory;
import com.vmware.mangle.services.helpers.slack.NotifierHelper;
import com.vmware.mangle.services.repository.NotifierRepository;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;
import com.vmware.mangle.utils.helpers.security.EncryptFields;
import com.vmware.mangle.utils.notification.SlackClient;
import com.vmware.mangle.utils.task.TaskUtils;

/**
 * NotifierService class.
 *
 * @author kumargautam
 */
@Service
@Log4j2
public class NotifierService {

    @Autowired
    private NotifierClientFactory clientFactory;
    @Autowired
    private NotifierRepository notifierRepository;

    public boolean testConnection(Notifier notifier) throws MangleException {
        return clientFactory.getNotificationClient(notifier).testConnection();
    }

    public Notifier getByName(@NonNull String name) {
        return notifierRepository.findByName(name).orElse(null);
    }

    public List<Notifier> getAllNotificationInfo() {
        return notifierRepository.findAll();
    }

    public Notifier create(Notifier notifier) {
        if (!notifierRepository.findByName(notifier.getName()).isPresent()) {
            return notifierRepository.save((Notifier) EncryptFields.encrypt(notifier));
        } else {
            throw new MangleRuntimeException(ErrorCode.NOTIFICATION_NAME_ALREADY_EXISTS, notifier.getName());
        }
    }

    public Notifier update(Notifier notification) {
        if (notifierRepository.findByName(notification.getName()).isPresent()) {
            return notifierRepository.save(notification);
        } else {
            throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.SLACK_NAME,
                    notification.getName());
        }
    }

    public boolean deleteByNames(List<String> names) {
        notifierRepository.deleteByNameIn(names);
        List<String> dbNames = getAllNotificationInfo().stream().map(Notifier::getName).collect(Collectors.toList());
        names.retainAll(dbNames);
        if (!CollectionUtils.isEmpty(names)) {
            throw new MangleRuntimeException(ErrorCode.FAILED_TO_DELETE_NOTIFICATION_NAMES,
                    names.stream().collect(Collectors.joining(", ")));
        }
        return true;
    }

    public List<String> enableSlacks(List<String> names, boolean enable) {
        List<Notifier> dbSlackInfos = notifierRepository.findByNameIn(names);
        if (!CollectionUtils.isEmpty(dbSlackInfos)) {
            dbSlackInfos.stream().forEach(slack -> slack.setEnable(enable));
            notifierRepository.saveAll(dbSlackInfos);
            return dbSlackInfos.stream().map(Notifier::getName).collect(Collectors.toList());
        }
        throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.SLACK_NAME,
                names.stream().collect(Collectors.joining(", ")));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void sendNotification(Task task) {
        CommandExecutionFaultSpec faultSpec = TaskUtils.getFaultSpec(task.getTaskData());
        if (validateTaskBeforeSendingNotification(task, faultSpec) && faultSpec != null) {
            List<Notifier> dbSlackInfos = notifierRepository.findByNameIn(faultSpec.getNotifierNames()).stream()
                    .filter(Notifier::getEnable).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(dbSlackInfos)) {
                NotifierHelper helper = new NotifierHelper(task);
                for (Notifier notification : dbSlackInfos) {
                    routeNotification(notification, helper);
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private boolean validateTaskBeforeSendingNotification(Task task, CommandExecutionFaultSpec faultSpec) {
        if (!hasChildTasks(task)) {
            log.debug("Task is of type: Parent and contains Child tasks. We will not send event for Parent task");
            return false;
        }
        if (faultSpec != null && CollectionUtils.isEmpty(faultSpec.getNotifierNames())) {
            log.info("No Notifier is selected for task : {}", task.getId());
            return false;
        }
        return true;
    }

    @SuppressWarnings("rawtypes")
    private void routeNotification(Notifier notification, NotifierHelper helper) {
        if (NotifierType.SLACK.equals(notification.getNotifierType())) {
            sendNotificationToChannel(notification, helper);
        }
    }

    @SuppressWarnings("rawtypes")
    private boolean hasChildTasks(Task task) {
        if (null != task.getTriggers().peek()) {
            TaskTrigger trigger = (TaskTrigger) task.getTriggers().peek();
            return (null == trigger.getChildTaskIDs());
        }
        log.warn("The task doesn't have triggers even. Can't have child tasks");
        return false;
    }

    @SuppressWarnings("rawtypes")
    private void sendNotificationToChannel(Notifier notification, NotifierHelper helper) {
        SlackClient client = (SlackClient) clientFactory.getNotificationClient(notification);
        MethodsClient apiClient = client.getClient();
        ChatPostMessageRequest method = helper.populateSlackMessage();
        method.setUsername(notification.getSlackInfo().getSenderName());
        for (String channel : notification.getSlackInfo().getChannels()) {
            try {
                method.setChannel(channel);
                apiClient.chatPostMessage(method);
            } catch (Exception e) {
                log.error("Not able to send notification to slack : {}, causes : {}", notification.getName(),
                        e.getMessage());
            }
        }
        client.shutdown();
    }
}