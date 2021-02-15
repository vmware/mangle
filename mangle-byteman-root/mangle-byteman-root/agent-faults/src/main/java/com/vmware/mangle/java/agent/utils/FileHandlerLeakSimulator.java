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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author hkilari
 *
 */
public class FileHandlerLeakSimulator implements Runnable {
    private static final Logger LOG = Logger.getLogger(FileHandlerLeakSimulator.class.getName());
    private long duration;

    public FileHandlerLeakSimulator(long duration) {
        this.duration = duration;
    }

    public void run() {
        LOG.info("Fiaasco Injecting FileHandlerLeak Fault for duration: " + duration);
        long startTime = System.currentTimeMillis();
        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "FileHandlerTest.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                LOG.severe(e.getMessage());
            }
        }
        List<InputStream> streams = new ArrayList<>();
        while (System.currentTimeMillis() - startTime < duration) {
            try {
                streams.add(file.toURI().toURL().openStream());
            } catch (Exception e) {
                LOG.severe(e.getMessage());
                break;
            }
        }
        LOG.info("Total File Handlers Created: " + streams.size());
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
        LOG.info("Fiaasco Exiting FileHandlerLeak");
    }
}
