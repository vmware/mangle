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

import com.vmware.mangle.java.agent.faults.helpers.HeapInfoUtils;

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
        LOG.info("MaxUsableHeap: " + RuntimeUtils.getMaxUsableHeapSpace());
        LOG.info("Current HeapUsage: " + RuntimeUtils.getUsedHeapSpace());
        LOG.info("Current HeapUsage Percentage: " + RuntimeUtils.getCurrentHeapUsageInPercentage());
        LOG.info("Current Free Space: " + RuntimeUtils.getFreeHeapSpace());

        long reservedMemory = getReservedSizeinBytes(load);
        LOG.info("Reserved Memory: " + reservedMemory);
        LinkedList<String> loadList = new LinkedList<>();
        long startTime = System.currentTimeMillis();

        threadLocal.set(loadList);
        long count = 0;
        while (System.currentTimeMillis() - startTime < duration) {
            LOG.info("Current HeapUsage: " + RuntimeUtils.getUsedHeapSpace());
            //Loop executes till the freespace available on Max Heap is greater than the ReservedMemory
            while (RuntimeUtils.getFreeHeapSpace() >= reservedMemory) {
                char[] chars = new char[100];
                Arrays.fill(chars, 'a');
                loadList.addFirst(new String(chars));
                //Updates Reserved Memory to handle Runtime expansion of the Heap regions by JVM
                if (count % 100000 == 0) {
                    ThreadUtils.delayInMilliSeconds(10);
                    reservedMemory = getReservedSizeinBytes(load);
                    LOG.info("Reserved Memory: " + reservedMemory);
                }
                count++;
            }
            LOG.info("HeapUsage post injection: " + RuntimeUtils.getUsedHeapSpace());
            LOG.info("Current HeapUsage Percentage: " + RuntimeUtils.getCurrentHeapUsageInPercentage());
            ThreadUtils.delayInSeconds(10);
        }
        RuntimeUtils.runGc();
        LOG.info("Fiaasco Exiting MemoryLoad Fault");
    }

    private long getReservedSizeinBytes(double load) {
        double currentUsage = RuntimeUtils.getCurrentHeapUsageInPercentage();
        //Returns 1 if the Usage target is already met. Else return the maximum free memory can be left before reaching the Targeted Load.
        //The calculation take account of the Pre Reserved regions of JVM
        if (currentUsage >= load) {
            return 1;
        } else {
            double loadToCreateInBytes = (((100 - load) / 100) * (double) RuntimeUtils.getMaxUsableHeapSpace())
                    + (HeapInfoUtils.getFromSpace() * 1024);
            return (long) loadToCreateInBytes;
        }
    }
}
