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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.vmware.mangle.java.agent.faults.AgentFault;
import com.vmware.mangle.java.agent.faults.helpers.FaultsHelper;
import com.vmware.mangle.java.agent.faults.helpers.HeapDumpUtils;
import com.vmware.mangle.java.agent.faults.helpers.ThreadDumpUtils;

/**
 * @author hkilari
 *
 */
public class DumpUtils {

    private static final Logger LOG = Logger.getLogger(DumpUtils.class.getName());

    private DumpUtils() {
        throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static String captureThreadDump(String filePath) {
        String threadfilePath = filePath + "-ThreadDump-" + Calendar.getInstance().getTimeInMillis() + ".txt";
        String faultsInfoPath = filePath + "-FaultsInfo-" + Calendar.getInstance().getTimeInMillis() + ".json";
        for (Entry<String, AgentFault> entry : FaultsHelper.getInstance().getRunningFaults().entrySet()) {
            entry.getValue().appendTaskActivity("Thread dump captured at " + threadfilePath);
        }
        LOG.info("Faults information captured at " + faultsInfoPath);
        Path path;
        try {
            path = Paths.get(faultsInfoPath);
            Files.write(path, FaultsHelper.objectToJson(FaultsHelper.getInstance().getAllFaultsInfo()).getBytes());
        } catch (IOException e) {
            LOG.severe(e.getMessage());
            return "Failed to Capture Thread Dump. Reason: " + e.getMessage();
        }
        ThreadDumpUtils.dump(threadfilePath);
        return "Captured Thread Dump Successfully";
    }

    public static String captureHeapDump(String filePath) {
        String heapfilePath = filePath + "-HeapDump-" + Calendar.getInstance().getTimeInMillis() + ".hprof";
        String faultsInfoPath = filePath + "-FaultsInfo-" + Calendar.getInstance().getTimeInMillis() + ".json";
        for (Entry<String, AgentFault> entry : FaultsHelper.getInstance().getRunningFaults().entrySet()) {
            entry.getValue().appendTaskActivity("Heap dump captured at " + heapfilePath);
        }
        LOG.info("Faults information captured at " + faultsInfoPath);
        Path path;
        try {
            path = Paths.get(faultsInfoPath);
            Files.write(path, FaultsHelper.objectToJson(FaultsHelper.getInstance().getAllFaultsInfo()).getBytes());
        } catch (IOException e) {
            LOG.severe(e.getMessage());
            return "Failed to Capture Heap Dump. Reason: " + e.getMessage();
        }
        HeapDumpUtils.dumpHeap(heapfilePath, true);
        return "Captured Heap Dump Successfully";
    }
}
