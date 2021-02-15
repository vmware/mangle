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

package com.vmware.mangle.unittest.services.helpers;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.resiliencyscore.ResiliencyScoreTask;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.cassandra.model.tasks.TaskType;
import com.vmware.mangle.services.helpers.ResiliencyScoreTaskHelper;
import com.vmware.mangle.services.mockdata.ResiliencyScoreMockData;

/**
 * @author dbhat
 */
public class ResiliencyScoreTaskHelperTest {
    @Test
    public void validateGetTask() {
        ResiliencyScoreTask resiliencyScoreTask = ResiliencyScoreMockData.getResiliencyScoreTask1();
        ResiliencyScoreTaskHelper helper = new ResiliencyScoreTaskHelper();

        Task<TaskSpec> task = helper.init(resiliencyScoreTask);
        Assert.assertEquals(task.getId(), resiliencyScoreTask.getId());
        Assert.assertEquals(task.getTaskType(), TaskType.RESILIENCY_SCORE);
    }
}
