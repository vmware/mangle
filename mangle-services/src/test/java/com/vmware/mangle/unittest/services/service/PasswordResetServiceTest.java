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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.junit.Assert;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.security.PasswordReset;
import com.vmware.mangle.services.PasswordResetService;
import com.vmware.mangle.services.repository.PasswordResetRepository;

/**
 *
 *
 * @author chetanc
 */
@Log4j2
public class PasswordResetServiceTest {

    @Mock
    private PasswordResetRepository resetRepository;

    private PasswordResetService resetService;

    @BeforeMethod
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        resetService = new PasswordResetService(resetRepository);
    }

    @Test
    public void testReadResetStatus() {
        log.info("Executing testReadResetStatus on PasswordResetService#readResetStatus");
        PasswordReset passwordReset = new PasswordReset();
        List<PasswordReset> resets = Collections.singletonList(passwordReset);
        when(resetRepository.findAll()).thenReturn(resets);
        boolean status = resetService.readResetStatus();
        Assert.assertFalse(status);
        verify(resetRepository, times(1)).findAll();
    }

    @Test
    public void testUpdateResetStatus() {
        log.info("Executing testReadResetStatus on PasswordResetService#updateResetStatus");
        PasswordReset passwordReset = new PasswordReset();
        List<PasswordReset> resets = Collections.singletonList(passwordReset);
        when(resetRepository.findAll()).thenReturn(resets);
        when(resetRepository.save(passwordReset)).thenReturn(passwordReset);
        boolean status = resetService.updateResetStatus();
        Assert.assertTrue(status);
        verify(resetRepository, times(1)).findAll();
        verify(resetRepository, times(1)).save(passwordReset);

    }


}
