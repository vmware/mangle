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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.model.task.MangleNodeStatusDto;
import com.vmware.mangle.services.AdministrationService;
import com.vmware.mangle.services.controller.AdministrationController;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 *
 *
 * @author bkaranam (bhanukiran karanam)
 */
public class AdministrationControllerTest {

    @Mock
    private AdministrationService administrationService;

    @Mock
    private MangleNodeStatusDto nodeStatusUpdateDto;

    @Mock
    private Task<MangleNodeStatusDto> task;

    private AdministrationController controller;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        controller = new AdministrationController(administrationService);
    }

    @Test
    public void movechangeMangleNodeStatusTest() throws MangleException {
        when(administrationService.updateMangleNodeStatus(nodeStatusUpdateDto)).thenReturn(task);
        ResponseEntity<Task<MangleNodeStatusDto>> response =
                controller.updateMangleNodeStatus(nodeStatusUpdateDto);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(), task);
        verify(administrationService, times(1)).updateMangleNodeStatus(nodeStatusUpdateDto);
    }
}
