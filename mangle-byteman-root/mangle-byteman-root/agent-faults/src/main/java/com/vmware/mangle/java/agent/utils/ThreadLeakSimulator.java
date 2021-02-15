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

import java.util.logging.Logger;

/**
 * @author jayasankarr
 *
 */
public class ThreadLeakSimulator implements Runnable {
    private static final Logger LOG = Logger.getLogger(ThreadLeakSimulator.class.getName());
    private long duration;
    private boolean enableOutOfMemory;
    private static String MANGLE_THREAD_LEAK_NAME = "mangle-thread-leak";
    private static int MAXIMUM_THREAD_LEAK_LOAD = 85;


    public ThreadLeakSimulator(long duration, boolean enableOutOfMemory) {
        this.duration = duration;
        this.enableOutOfMemory = enableOutOfMemory;
    }

    @Override
    public void run() {
        LOG.info("Mangle Injecting ThreadLeak Fault for duration: " + duration);
        LOG.info("MaxHeapSize Available: " + RuntimeUtils.getMaxHeapSpace() / 100 + " kb");
        LOG.info("Current HeapUsage: " + RuntimeUtils.getUsedHeapSpace());
        long size = ThreadUtils.getSizeinBytes(MAXIMUM_THREAD_LEAK_LOAD);
        LOG.info("Maximum Consumption: " + size / 100);
        long startTime = System.currentTimeMillis();
        if (enableOutOfMemory) {
            while (System.currentTimeMillis() - startTime < duration) {
                createThread();
            }
        } else {
            while ((RuntimeUtils.getUsedHeapSpace() <= size) && (System.currentTimeMillis() - startTime < duration)) {
                createThread();
            }
        }
        while (System.currentTimeMillis() - startTime < duration) {
            try {
                LOG.info("Waiting till Timeout....");
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                LOG.severe(e.getMessage());
                break;
            }
        }
        RuntimeUtils.runGc();
        LOG.info("Mangle Exiting ThreadLeak");
    }

    private void createThread() {
        try {
            new DummyThread(MANGLE_THREAD_LEAK_NAME + System.currentTimeMillis(), duration).start();
        } catch (Exception e) {
            LOG.severe(e.getMessage());
        }
    }

}