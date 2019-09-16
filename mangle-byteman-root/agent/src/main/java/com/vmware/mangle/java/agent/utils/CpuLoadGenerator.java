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
 * @author hkilari
 *
 */
public class CpuLoadGenerator implements Runnable {
    private static final Logger LOG = Logger.getLogger(CpuLoadGenerator.class.getName());
    private long duration;
    private int load;

    public CpuLoadGenerator(int load, long duration) {
        this.duration = duration;
        this.load = load;
    }

    public void run() {
        LOG.info("Fiaasco Injecting CPULoad Fault for duration: " + duration + " at Load: " + load);
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < duration) {
            try {
                if (System.currentTimeMillis() % 100 == 0) {
                    Thread.sleep((long) (100 - load));//(long) Math.floor((1 - load) * 100));
                }
            } catch (InterruptedException e) {
                LOG.fine("Exiting the thread");
                break;
            }
        }
        LOG.info("Fiaasco Exiting CPULoad Fault");
    }
}
