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

import static org.mockito.Mockito.when;

import lombok.extern.log4j.Log4j2;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.model.task.MangleNodeStatusDto;
import com.vmware.mangle.services.AdministrationService;
import com.vmware.mangle.services.admin.tasks.NodeStatusTask;
import com.vmware.mangle.services.tasks.executor.TaskExecutor;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Unit Test Case for AdministrationService.
 *
 * @author bkaranam
 */
@Log4j2
public class AdministrationServiceTest {
    private AdministrationService administrationService;
    @Mock
    private TaskExecutor<Task<MangleNodeStatusDto>> concurrentTaskRunner;
    @Mock
    private MangleNodeStatusDto nodeStatusUpdateDto;
    @Mock
    private NodeStatusTask<MangleNodeStatusDto> nodeStatusUpdateTask;
    @Mock
    private Task<MangleNodeStatusDto> task;

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUpBeforeMethod() throws Exception {
        MockitoAnnotations.initMocks(this);
        administrationService = new AdministrationService(nodeStatusUpdateTask, concurrentTaskRunner);
    }

    /**
     * Test method for {@link com.vmware.mangle.service.EndpointService#getAllEndpoints()}.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testMoveMangleToMaintenanceMode() {

        try {
            when(nodeStatusUpdateTask.init(nodeStatusUpdateDto)).thenReturn(task);
            when((Task<MangleNodeStatusDto>) concurrentTaskRunner.submitTask(task))
                    .thenReturn(task);
            administrationService.updateMangleNodeStatus(nodeStatusUpdateDto);
        } catch (MangleException e) {
            log.error("testMockTaskInjectionExecution failed with exception: ", e);
            Assert.assertTrue(false);
        }
    }
}
