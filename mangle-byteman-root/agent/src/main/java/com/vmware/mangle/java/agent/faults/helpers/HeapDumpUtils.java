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

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;

/**
 * Utiltiy Class to capture Heap dump of Java Application
 * 
 * @author hkilari
 *
 */
public class HeapDumpUtils {
    // This is the name of the HotSpot Diagnostic MBean
    private static final String HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";


    /*   Call this method from your application whenever you 
      want to dump the heap snapshot into a file.
     
      @param fileName name of the heap dump file
      @param live flag that tells whether to dump
                  only the live objects*/

    // get the hotspot diagnostic MBean from the
    // platform MBean server
    @SuppressWarnings("restriction")
    public static void dumpHeap(String fileName, boolean live) {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            ManagementFactory
                    .newPlatformMXBeanProxy(server, HOTSPOT_BEAN_NAME, com.sun.management.HotSpotDiagnosticMXBean.class)
                    .dumpHeap(fileName, live);
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }
}
