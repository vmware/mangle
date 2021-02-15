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

/**
 * @author bkaranam Utility class to provide a logic to retry a block of code
 */
@Log4j2
public class RetryUtils {
    private static final int RETRY = 3;
    private static final int DELAY = 1000;

    private RetryUtils() {
    }

    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws MangleException;
    }

    public static void retry(RunnableWithException runnable, Throwable throwable, int retryCount, int retryDelay)
            throws MangleException {
        retryLogics(() -> {
            runnable.run();
            return null;
        }, throwable, retryCount, retryDelay);
    }

    /**
     * Method to retry a block of code with custom retry variables
     *
     * @param callable
     * @param throwable
     * @return
     * @throws MangleException
     */
    public static <V> V retry(Callable<V> callable, Throwable throwable, int retryCount, int retryDelay)
            throws MangleException {
        return retryLogics(callable, throwable, retryCount, retryDelay);
    }

    /**
     * Method has a logic to retry whenever a exception occurs in the block of code specified
     *
     * @param callable
     * @param throwable
     * @param retryCount
     * @param retryDelay
     * @return
     * @throws MangleException
     */


    private static <T> T retryLogics(Callable<T> callable, Throwable throwable, int retryCount, int retryDelay)
            throws MangleException {
        int counter = 0;
        Throwable exception = new Throwable();
        while (counter < retryCount) {
            try {
                return callable.call();
            } catch (MangleException mangleException) {
                if (mangleException.getMessage().startsWith("FAILED")) {
                    throw mangleException;
                }
                exception = mangleException;
                log.error(String.format("retry %s / %s, %s", ++counter, retryCount, mangleException.getMessage()));
            } catch (Exception e) {
                if (e.getMessage().startsWith("FAILED")) {
                    throw new MangleException(e.getMessage());
                }
                exception = e;
                log.error(String.format("retry %s / %s, %s", ++counter, retryCount, e.getMessage()));
            }
            CommonUtils.delayInSeconds(retryDelay);
        }
        if (null == throwable) {
            throwable = exception;
        }
        if (throwable instanceof MangleException) {
            throw (MangleException) throwable;
        } else {
            throw new MangleException(throwable.getMessage());
        }
    }
}
