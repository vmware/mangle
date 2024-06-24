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

package com.vmware.mangle.services.mockdata;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import lombok.Getter;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.K8SFaultTriggerSpec;
import com.vmware.mangle.cassandra.model.slack.Notifier;
import com.vmware.mangle.cassandra.model.slack.NotifierType;
import com.vmware.mangle.cassandra.model.slack.SlackInfo;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.utils.constants.Constants;

/**
 * Mock data for Notifier test.
 *
 * @author kumargautam
 */
public class NotifierMockData {

    @Getter
    private FaultsMockData faultsMockData;
    private TasksMockData<CommandExecutionFaultSpec> tasksMockData;

    public NotifierMockData() {
        this.faultsMockData = new FaultsMockData();
        this.tasksMockData = new TasksMockData<>(faultsMockData.getK8SCPUFaultSpec());
    }

    public Notifier getSlackInfo(String name) {
        Notifier info = new Notifier();
        info.setName(name);
        info.setEnable(true);
        info.setNotifierType(NotifierType.SLACK);
        SlackInfo slackInfo = new SlackInfo();
        info.setSlackInfo(slackInfo);
        slackInfo.setToken("xxx");
        slackInfo.setChannels(Arrays.asList("dev"));
        slackInfo.setSenderName("Test");
        return info;
    }

    public Task<CommandExecutionFaultSpec> getTask(TaskType taskType, TaskStatus taskStatus) {
        Task<CommandExecutionFaultSpec> task = tasksMockData.getDummy1Task();
        task.setTaskType(taskType);
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);
        task.getTriggers().peek().setEndTime(sdf.format(new Date()));
        task.getTriggers().peek().setTaskStatus(taskStatus);
        task.getTaskData().setNotifierNames(new HashSet<>(Arrays.asList("mangle-test")));
        return task;
    }

    public Task<K8SFaultTriggerSpec> getTaskForK8SFaultTriggerSpec(TaskType taskType, TaskStatus taskStatus) {
        TasksMockData<K8SFaultTriggerSpec> mockData = new TasksMockData<>(faultsMockData.getK8SCPUFaultTriggerSpec());
        Task<K8SFaultTriggerSpec> task = mockData.getDummy1Task();
        task.setTaskType(taskType);
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DEFAULT_DATE_FORMAT);
        task.getTriggers().peek().setEndTime(sdf.format(new Date()));
        task.getTriggers().peek().setTaskStatus(taskStatus);
        task.getTaskData().getFaultSpec().setNotifierNames(new HashSet<>(Arrays.asList("mangle")));
        return task;
    }

    public ChatPostMessageResponse getSlackChatPostMessageResponse(){
        ChatPostMessageResponse chatPostMessageResponse = new ChatPostMessageResponse();
        chatPostMessageResponse.setOk(true);
        chatPostMessageResponse.setChannel("dev");
        chatPostMessageResponse.setTs("1645427127.335539");
        return chatPostMessageResponse;
    }
}
