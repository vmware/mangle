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

package com.vmware.mangle.unittest.utils;

import java.util.concurrent.Callable;

import lombok.extern.log4j.Log4j2;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.vmware.mangle.utils.RetryUtils;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 *
 *
 * @author hkilari
 */
@Log4j2
public class RetryUtilsTest {

    private static int counter;

    @BeforeMethod
    public void resetCounter() {
        counter = 0;
    }

    class CallableExample implements Callable<Object> {
        public Object call() throws Exception {
            log.info("Incrementing Counter");
            return counter++;
        }
    }

    class CallableExampleWithException implements Callable<Object> {
        public Object call() throws Exception {
            log.info("Incrementing Counter");
            counter++;
            throw new Exception();
        }
    }

    private void incrementCounterWithException() throws Exception {
        log.info("Incrementing Counter");
        counter++;
        throw new Exception();
    }

    private void incrementCounter() {
        log.info("Incrementing Counter");
        counter++;
    }

    @Test(expectedExceptions = MangleException.class)
    public void testRetryUtilsForException() throws MangleException {
        log.info("Executing RetryUtils on DummyMethod");
        Throwable exception = new Throwable();
        RetryUtils.retry(() -> {
            incrementCounterWithException();
        }, exception, 3, 1L);
        Assert.assertEquals(3, counter);
    }

    @Test
    public void testRetry() throws MangleException {
        log.info("Executing RetryUtils on DummyMethod");
        Throwable exception = new Throwable();
        RetryUtils.retry(() -> {
            incrementCounter();
        }, exception);
        Assert.assertEquals(1, counter);
    }

    @Test
    public void testRetryWithCallable() throws MangleException {
        log.info("Executing RetryUtils on DummyCallable");
        Throwable exception = new Throwable();
        RetryUtils.retry(new CallableExample(), exception);
        Assert.assertEquals(1, counter);
    }

    @Test
    public void testRetryWithCallableWithException() {
        log.info("Executing RetryUtils on DummyCallable");
        Throwable exception = new Throwable();
        try {
            RetryUtils.retry(new CallableExampleWithException(), exception, 3, 1L);
        } catch (MangleException e) {
            Assert.assertEquals(e.getErrorCode(), ErrorCode.RETRY_LOGICS_FAILED);
        }
        Assert.assertEquals(3, counter);
    }
}
