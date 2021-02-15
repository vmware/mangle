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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.vmware.mangle.cassandra.model.scheduler.SchedulerInfo;
import com.vmware.mangle.cassandra.model.scheduler.SchedulerRequestStatus;
import com.vmware.mangle.cassandra.model.scheduler.SchedulerSpec;
import com.vmware.mangle.model.enums.SchedulerJobType;
import com.vmware.mangle.model.enums.SchedulerStatus;
import com.vmware.mangle.utils.ReadProperty;
import com.vmware.mangle.utils.constants.Constants;

/**
 *
 * Class to generate Mock data for Mangle Scheduler Tests
 *
 * @author ashrimali
 */
public class SchedulerControllerMockData {


    private String cronExpression;
    private String description;
    private String jobId1;
    private String jobId2;
    private List<String> jobIds;
    private String simpleTime;
    private static final String JOB_CANCELLED_MESSAGE = "Cancelled Successfully";
    private static final String JOB_PAUSED_MESSAGE = "Paused Successfully";
    private static final String JOB_RESUMED_MESSAGE = "resumed Successfully";

    public SchedulerControllerMockData() {
        Properties properties = ReadProperty.readProperty(Constants.MOCKDATA_FILE);
        this.cronExpression = properties.getProperty("scheduler.cronExpression");
        this.simpleTime = properties.getProperty("scheduler.scheduledTime");
        this.description = properties.getProperty("scheduler.description");
        this.jobId1 = properties.getProperty("sheduler.jobid1");
        this.jobId2 = properties.getProperty("sheduler.jobid2");
        this.jobIds = getJobIds();
    }

    public SchedulerInfo getSchedulerInfo() {
        SchedulerInfo schedulerInfo = new SchedulerInfo();
        schedulerInfo.setCronExpression(this.cronExpression);
        schedulerInfo.setDescription(this.description);
        return schedulerInfo;
    }

    public List<String> getJobIds() {
        List<String> jobIds = new ArrayList<>();
        jobIds.add(jobId1);
        jobIds.add(jobId2);
        return jobIds;
    }

    public Map<String, SchedulerRequestStatus> getCancelledScheduledStatusMap() {
        return getScheduledStatusMap(SchedulerStatus.CANCELLED, JOB_CANCELLED_MESSAGE);

    }

    public Map<String, SchedulerRequestStatus> getPausedScheduledStatusMap() {
        return getScheduledStatusMap(SchedulerStatus.PAUSED, JOB_PAUSED_MESSAGE);

    }

    public Map<String, SchedulerRequestStatus> getResumedScheduledStatusMap() {
        return getScheduledStatusMap(SchedulerStatus.SCHEDULED, JOB_RESUMED_MESSAGE);

    }

    private Map<String, SchedulerRequestStatus> getScheduledStatusMap(SchedulerStatus schedulerStatus,
            String jobMessage) {
        Map<String, SchedulerRequestStatus> scheduledStatusMap = new HashMap<>();
        SchedulerRequestStatus schedulerRequestStatus = new SchedulerRequestStatus();
        schedulerRequestStatus.setMessage(jobMessage);
        scheduledStatusMap.put(this.jobIds.get(0), schedulerRequestStatus);
        scheduledStatusMap.put(this.jobIds.get(1), schedulerRequestStatus);
        return scheduledStatusMap;
    }

    public SchedulerSpec getMangleSchedulerSpec() {
        SchedulerSpec schedulerSpec = new SchedulerSpec();
        schedulerSpec.setId(jobId1);
        schedulerSpec.setCronExpression(this.cronExpression);
        schedulerSpec.setDescription(this.description);
        schedulerSpec.setStatus(SchedulerStatus.CANCELLED);
        return schedulerSpec;
    }

    public SchedulerSpec getMangleSchedulerSpecScheduled() {
        SchedulerSpec schedulerSpec = new SchedulerSpec();
        schedulerSpec.setCronExpression(this.cronExpression);
        schedulerSpec.setDescription(this.description);
        schedulerSpec.setStatus(SchedulerStatus.SCHEDULED);
        schedulerSpec.setId("1");
        return schedulerSpec;
    }

    public SchedulerSpec getMangleSchedulerSpecScheduledCron() {
        SchedulerSpec schedulerSpec = new SchedulerSpec();
        schedulerSpec.setCronExpression(this.cronExpression);
        schedulerSpec.setDescription(this.description);
        schedulerSpec.setStatus(SchedulerStatus.SCHEDULED);
        schedulerSpec.setJobType(SchedulerJobType.CRON);
        schedulerSpec.setId("1");
        return schedulerSpec;
    }

    public SchedulerSpec getMangleSchedulerSpecScheduledSimple() {
        SchedulerSpec schedulerSpec = new SchedulerSpec();
        schedulerSpec.setScheduledTime(Long.parseLong(this.simpleTime));
        schedulerSpec.setDescription(this.description);
        schedulerSpec.setStatus(SchedulerStatus.SCHEDULED);
        schedulerSpec.setJobType(SchedulerJobType.SIMPLE);
        schedulerSpec.setId("1");
        return schedulerSpec;
    }

    public SchedulerSpec getMangleSchedulerSpecScheduledCronWrong() {
        SchedulerSpec schedulerSpec = new SchedulerSpec();
        schedulerSpec.setCronExpression(null);
        schedulerSpec.setDescription(this.description);
        schedulerSpec.setStatus(SchedulerStatus.SCHEDULED);
        schedulerSpec.setJobType(SchedulerJobType.CRON);
        schedulerSpec.setId("1");
        return schedulerSpec;
    }

    public List<SchedulerSpec> getListOfSchedulerSpec() {
        List<SchedulerSpec> schedulerSpecList = new ArrayList<>();
        schedulerSpecList.add(this.getMangleSchedulerSpec());
        return schedulerSpecList;
    }

    public Map<String, String> getAllMangleTasks() {
        Map<String, String> taskMap = new HashMap<>();
        taskMap.put("Key", "Value");
        return taskMap;
    }

}
