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

package com.vmware.mangle.utils.exceptions.handler;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.List;

import lombok.extern.log4j.Log4j2;

/**
 *
 * Mangle Uncaught Exception Handler class.
 *
 * @author kumargautam
 */
@Log4j2
public class MangleUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final String NEW_LINE = "\n";
    private static MangleUncaughtExceptionHandler exceptionHandler;
    private String logDir;
    private boolean exitRequired = true;

    private MangleUncaughtExceptionHandler() {
    }

    public static void register() {
        log.info("MangleUncaughtExceptionHandler registered...");
        Thread.currentThread().setUncaughtExceptionHandler(getExceptionHandler());
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }

    public void setExitRequired(boolean exitRequired) {
        this.exitRequired = exitRequired;
    }

    public static MangleUncaughtExceptionHandler getExceptionHandler() {
        if (exceptionHandler == null) {
            exceptionHandler = new MangleUncaughtExceptionHandler();
        }
        return exceptionHandler;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        try {
            log.error("Uncaught exception caught in thread :" + t.getName(), e);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
            String threadfilePath = logDir + File.separator + "mangle-thread-dump-"
                    + dateFormat.format(System.currentTimeMillis()) + ".log";
            printFullCoreDump(threadfilePath);
        } finally {
            if (exitRequired) {
                Runtime.getRuntime().halt(1);
            }
        }
    }

    public void printFullCoreDump(String threadfilePath) {
        ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
        StringBuilder threadDump = new StringBuilder("Thread-Dump :\n");
        for (ThreadInfo threadInfo : threadMxBean.dumpAllThreads(true, true)) {
            threadDump.append(threadInfo.toString());
        }
        threadDump.append("\nHeap-Info :\n").append(getHeapInfo());
        Path path;
        try {
            path = Paths.get(threadfilePath);
            Files.write(path, threadDump.toString().getBytes());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public static String getHeapInfo() {
        StringBuilder stackTracesBuild = new StringBuilder();
        List<MemoryPoolMXBean> memBeans = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean mpool : memBeans) {
            MemoryUsage usage = mpool.getUsage();

            String name = mpool.getName();
            long used = usage.getUsed();
            long max = usage.getMax();
            int pctUsed = (int) (used * 100 / max);
            stackTracesBuild.append(" ").append(name).append(" total: ").append((max / 1000)).append("K, ")
                    .append(pctUsed).append("% used").append(NEW_LINE);
        }
        return stackTracesBuild.toString();
    }
}