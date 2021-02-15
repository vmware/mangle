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
public class DummyThread extends Thread {
    private static final Logger LOG = Logger.getLogger(DummyThread.class.getName());
    private long duration;
    private byte[] bytes;


    public DummyThread(String threadName, long duration) {
        this.duration = duration;
        this.bytes = new byte[1000];
        this.setName(threadName);
    }

    public void run() {
        try {
            LOG.info(Thread.currentThread().getName() + "sleeping for " + duration);
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            LOG.severe(Thread.currentThread().getName() + "Interrupted");
        }
        LOG.info(Thread.currentThread().getName() + " exiting.");
    }
}