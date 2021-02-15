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

package com.vmware.mangle.services.helpers.slack;

import java.util.ArrayList;
import java.util.List;

import allbegray.slack.type.Attachment;
import allbegray.slack.type.Color;
import allbegray.slack.type.Field;
import allbegray.slack.webapi.method.chats.ChatPostMessageMethod;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.JVMAgentFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskStatus;
import com.vmware.mangle.cassandra.model.tasks.TaskTrigger;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.model.constants.Constants;
import com.vmware.mangle.services.constants.CommonConstants;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.constants.MetricProviderConstants;
import com.vmware.mangle.utils.task.TaskUtils;

/**
 * Populate Notification Event.
 *
 * @author kumargautam
 */
public class NotifierHelper<T extends Task<TaskSpec>> {
    private static final String DEFAULT_CHANNEL_NAME = "general";
    private CommandExecutionFaultSpec commandExecutionFaultSpec;
    private T task;
    private TaskTrigger trigger;

    public NotifierHelper(T task) {
        this.commandExecutionFaultSpec = TaskUtils.getFaultSpec(task.getTaskData());
        this.task = task;
        this.trigger = task.getTriggers().peek();
    }

    public ChatPostMessageMethod populateSlackMessage() {
        ChatPostMessageMethod method = new ChatPostMessageMethod(DEFAULT_CHANNEL_NAME,
                CommonConstants.SLACK_HELLO_MSG + commandExecutionFaultSpec.getEndpointName() + "*");
        method.setUsername(Constants.DEFAULT_SENDER_NAME);
        method.setAs_user(false);
        List<Attachment> attachments = method.getAttachments();
        Attachment attachment = new Attachment();
        attachments.add(attachment);
        updateAttachment(attachment);
        List<Field> fields = new ArrayList<>();
        attachment.setFields(fields);
        addField(fields, CommonConstants.SLACK_MSG_TASK_ID, task.getId());
        addField(fields, CommonConstants.SLACK_MSG_FAULT_NAME,
                commandExecutionFaultSpec.getFaultName() + MetricProviderConstants.HYPHEN + task.getTaskType());
        addField(fields, CommonConstants.SLACK_MSG_TASK_NAME, task.getTaskName());
        addField(fields, CommonConstants.SLACK_MSG_FAULT_STATUS, trigger.getTaskStatus().name());
        addField(fields, CommonConstants.SLACK_MSG_FAULT_START_TIME,
                CommonUtils.getDateInCurrentTimeZone(trigger.getStartTime()));
        updateFaultEndTime(fields);
        if (trigger.getTaskStatus().equals(TaskStatus.FAILED)) {
            addField(fields, CommonConstants.SLACK_MSG_FAULT_FAILURE_REASON, trigger.getTaskFailureReason());
            fields.get(fields.size() - 1).set_short(false);
        }
        addField(fields, CommonConstants.SLACK_MSG_FAULT_DESCRIPTION, task.getTaskDescription());
        fields.get(fields.size() - 1).set_short(false);
        return method;
    }

    private void updateAttachment(Attachment attachment) {
        attachment.setTitle("Fault Summary");
        attachment.setFooter("Thanks,\n Mangle");
        attachment.setColor(Color.WARNING);
    }

    private void addField(List<Field> fields, String title, String value) {
        fields.add(new Field(title, value, true));
    }

    private void updateFaultEndTime(List<Field> fields) {
        Integer faultTimeOut = commandExecutionFaultSpec.getTimeoutInMilliseconds();
        if (!(commandExecutionFaultSpec instanceof JVMAgentFaultSpec) && faultTimeOut == null) {
            updateFaultEventEndTimeAsNow(fields);
            return;
        }
        if (faultTimeOut != null && task.getTaskType() == TaskType.INJECTION) {
            if (task.getTaskStatus() == TaskStatus.COMPLETED) {
                long faultTimeOutInMilis = CommonUtils.getDateObjectFor(trigger.getEndTime()).getTime() + faultTimeOut;
                addField(fields, CommonConstants.SLACK_MSG_FAULT_END_TIME,
                        CommonUtils.getDateInCurrentTimeZone(CommonUtils.getTime(faultTimeOutInMilis)));
                return;
            }
            updateFaultEventEndTimeAsNow(fields);
        } else if (task.getTaskType() == TaskType.REMEDIATION) {
            updateFaultEventEndTimeAsNow(fields);
        } else if (task.getTaskStatus() == TaskStatus.FAILED) {
            updateFaultEventEndTimeAsNow(fields);
        }
    }

    private void updateFaultEventEndTimeAsNow(List<Field> fields) {
        String endTime = CommonUtils.getTime(System.currentTimeMillis() + MetricProviderConstants.ONE_SECOND_IN_MILLIS);
        addField(fields, CommonConstants.SLACK_MSG_FAULT_END_TIME, CommonUtils.getDateInCurrentTimeZone(endTime));
    }
}