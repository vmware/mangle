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

package com.vmware.mangle.java.agent.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.vmware.mangle.java.agent.faults.helpers.HeapInfoUtils;

/**
 * @author hkilari
 *
 */
public class ThreadUtils {
    private static final Logger LOG = Logger.getLogger(ThreadUtils.class.getName());

    private ThreadUtils() {
        throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static List<Future<?>> triggerCpuLoadGenerator(int noOfThreads, long timeOut, int load) {
        List<Future<?>> futureList = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(noOfThreads);
        for (int i = 0; i < noOfThreads; i++) {
            Runnable worker = new CpuLoadGenerator(load, timeOut);
            futureList.add(executor.submit(worker));
        }
        executor.shutdown();
        return futureList;
    }

    public static List<Future<?>> triggerMemoryLoadGenerator(long timeOut, int load) {
        List<Future<?>> futureList = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Runnable worker = new MemoryLoadGenerator(load, timeOut);
        futureList.add(executor.submit(worker));
        executor.shutdown();
        return futureList;
    }

    public static List<Future<?>> triggerFileHandlerLeakSimulator(long timeOut) {
        List<Future<?>> futureList = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Runnable worker = new FileHandlerLeakSimulator(timeOut);
        futureList.add(executor.submit(worker));
        executor.shutdown();
        return futureList;
    }

    public static List<Future<?>> triggerThreadLeakSimulator(long timeOut, boolean enableOutOfMemory) {
        List<Future<?>> futureList = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(1);
        Runnable worker = new ThreadLeakSimulator(timeOut, enableOutOfMemory);
        futureList.add(executor.submit(worker));
        executor.shutdown();
        return futureList;
    }

    public static void delayInSeconds(int seconds) {
        try {
            LOG.info("Sleeping for " + seconds + " seconds");
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            LOG.severe(e.getMessage());
        }
    }

    public static void delayInMilliSeconds(long milliSeconds) {
        try {
            LOG.info("Sleeping for " + milliSeconds + " milliSeconds");
            Thread.sleep(milliSeconds);
        } catch (InterruptedException e) {
            LOG.severe(e.getMessage());
        }
    }

    public static void interrupt() {
        LOG.info("Fiaasco agent Interrupting Thread");
        Thread.interrupted();
    }

    public static long getSizeinBytes(double load) {
        double currentUsage = RuntimeUtils.getCurrentHeapUsageInPercentage();
        if (currentUsage >= load) {
            return 1;
        } else {
            double loadToCreateInBytes = (((100 - load) / 100) * (double) RuntimeUtils.getMaxUsableHeapSpace())
                    + (HeapInfoUtils.getFromSpace() * 1024);
            return (long) loadToCreateInBytes;
        }
    }
}
