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

import java.lang.management.ManagementFactory;

/**
 * @author hkilari
 *
 */
public class RuntimeUtils {
    private static final Runtime runtime = Runtime.getRuntime();

    private RuntimeUtils() {
        throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static int getNoOfProcessors() {
        return runtime.availableProcessors();
    }

    public static long getUsedHeapSpace() {
        return (runtime.totalMemory() - runtime.freeMemory());
    }

    public static long getMaxHeapSpace() {
        return (runtime.maxMemory());
    }

    public static double getCurrentHeapUsageInPercentage() {
        return (getUsedHeapSpace() / getMaxHeapSpace()) * 100;
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static String getPid() {
        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        return jvmName.split("@")[0];
    }

    public static void runGc() {
        for (int i = 0; i < 5; i++) {
            System.gc();
        }
    }
}
