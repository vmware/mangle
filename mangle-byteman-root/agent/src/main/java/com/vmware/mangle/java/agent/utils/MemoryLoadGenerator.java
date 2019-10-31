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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author hkilari
 *
 */
public class MemoryLoadGenerator implements Runnable {
    private long duration;
    private int load;

    private static final Logger LOG = Logger.getLogger(MemoryLoadGenerator.class.getName());
    private ThreadLocal<List<String>> threadLocal = new ThreadLocal<>();

    public MemoryLoadGenerator(int load, long duration) {
        this.duration = duration;
        this.load = load;
    }

    public void run() {
        LOG.info("Fiaasco Injecting MemoryLoad Fault for duration: " + duration + " at Load: " + load);
        LOG.info("MaxHeapSize Available: " + RuntimeUtils.getMaxHeapSpace());
        LOG.info("Current HeapUsage: " + RuntimeUtils.getUsedHeapSpace());
        LOG.info("Current HeapUsage Percentage: " + RuntimeUtils.getCurrentHeapUsageInPercentage());

        long size = ThreadUtils.getSizeinBytes(load);
        LOG.info("Requested Consumption: " + size );
        LinkedList<String> loadList = new LinkedList<>();
        long startTime = System.currentTimeMillis();

        threadLocal.set(loadList);
        while (System.currentTimeMillis() - startTime < duration) {
            LOG.info("Current HeapUsage: " + RuntimeUtils.getUsedHeapSpace());
            while (RuntimeUtils.getUsedHeapSpace() <= size) {
                char[] chars = new char[100];
                Arrays.fill(chars, 'a');
                loadList.addFirst(new String(chars));
            }
            LOG.info("HeapUsage post injection: " + RuntimeUtils.getUsedHeapSpace());
            LOG.info("Current HeapUsage Percentage: " + RuntimeUtils.getCurrentHeapUsageInPercentage());
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                LOG.info("Exiting the thread");
                break;
            }
        }
        RuntimeUtils.runGc();
        LOG.info("Fiaasco Exiting MemoryLoad Fault");
    }


}
