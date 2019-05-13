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

package com.vmware.mangle.java.agent.faults.helpers;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Utility Class to capture thread Dump of Java Application
 *
 * @author hkilari
 *
 */
public interface ThreadDumpUtils {
    static final Logger LOG = Logger.getLogger(ThreadDumpUtils.class.getName());

    static void dump(String filePath) {
        ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
        StringBuffer threadDump = new StringBuffer();
        for (ThreadInfo threadInfo : threadMxBean.dumpAllThreads(true, true)) {
            threadDump.append(threadInfo.toString());
        }

        Path path;
        try {
            path = Paths.get(filePath);
            Files.write(path, threadDump.toString().getBytes());
        } catch (IOException e) {
            LOG.severe(e.getMessage());
        }
    }
}
