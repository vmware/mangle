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

package com.vmware.mangle.utils;

import java.util.concurrent.Callable;

import lombok.extern.log4j.Log4j2;

import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author bkaranam Utility class to provide a logic to retry a block of code
 */
@Log4j2
public class RetryUtils {
    private static final int RETRY = 3;
    private static final long DELAY = 1000L;

    private RetryUtils() {
    }

    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }

    /**
     * Method to retry a block of code using default retry limits
     *
     * @param callable
     * @param throwable
     * @param message
     * @return
     * @throws MangleException
     */
    public static <V> V retry(Callable<V> callable, Throwable throwable) throws MangleException {
        return retryLogics(callable, throwable, RETRY, DELAY);
    }

    /**
     * Method to retry a block of code with custom retry variables
     *
     * @param callable
     * @param throwable
     * @param message
     * @return
     * @throws MangleException
     */
    public static <V> V retry(Callable<V> callable, Throwable throwable, int retryCount, long retryDelay)
            throws MangleException {
        return retryLogics(callable, throwable, retryCount, retryDelay);
    }

    public static void retry(RunnableWithException runnable, Throwable throwable) throws MangleException {
        retryLogics(() -> {
            runnable.run();
            return null;
        }, throwable, RETRY, DELAY);
    }

    public static void retry(RunnableWithException runnable, Throwable throwable, int retryCount, long retryDelay)
            throws MangleException {
        retryLogics(() -> {
            runnable.run();
            return null;
        }, throwable, retryCount, retryDelay);
    }

    /**
     * Method has a logic to retry whenever a exception occurs in the block of code specified
     *
     * @param callable
     * @param throwable
     * @param message
     * @param retryCount
     * @param retryDelay
     * @return
     * @throws MangleException
     */


    private static <T> T retryLogics(Callable<T> callable, Throwable throwable, int retryCount, long retryDelay)
            throws MangleException {
        int counter = 0;

        while (counter < retryCount) {
            try {
                return callable.call();
            } catch (Exception e) {
                counter++;
                log.error(String.format("retry %s / %s, %s", counter, RETRY, e.getMessage()));
                CommonUtils.delayInSecondsWithDebugLog(retryDelay);
            }
        }

        throw new MangleException(ErrorCode.RETRY_LOGICS_FAILED, throwable.getMessage());
    }
}
