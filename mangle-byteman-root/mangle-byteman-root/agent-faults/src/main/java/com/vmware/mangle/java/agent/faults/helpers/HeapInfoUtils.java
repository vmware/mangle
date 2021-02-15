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

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.management.DiagnosticCommandMBean;
import sun.management.ManagementFactoryHelper;

import com.vmware.mangle.java.agent.utils.MemoryLoadGenerator;

/**
 * Utiltiy Class to capture Heap Information of Java Application
 *
 * @author hkilari
 *
 */
public class HeapInfoUtils {
    private static final Logger LOG = Logger.getLogger(MemoryLoadGenerator.class.getName());
    private static DiagnosticCommandMBean dcmd = ManagementFactoryHelper.getDiagnosticCommandMBean();

    private HeapInfoUtils() {
        throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Call this method from your application whenever you want to find the gcHeapInfo
     *
     * @param fileName
     * @param live
     * @return
     */
    public static String getHeapInfo() {
        String[] signature = new String[] { String[].class.getName() };
        Object[] params = new Object[1];
        try {
            //Invoking the 'gcHeapInfo' operation on DiagnosticCommandMBean
            String result = (String) dcmd.invoke("gcHeapInfo", params, signature);
            //TODO should be disabled post the Test Verifications on Multiple Systems.
            LOG.info(result);
            return result;
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return null;
    }

    /**
     * Utility Method to find the size of the Memory reserved by JVM in Heap for GC Operations
     *
     * @return long
     */
    public static long getFromSpace() {
        Pattern fromSpacePattern = Pattern.compile("from space \\d*K");
        Matcher m = fromSpacePattern.matcher(getHeapInfo());
        if (m.find()) {
            Pattern fromSpaceLengthPattern = Pattern.compile("[0-9]+");
            String fromSpace = m.group(0);
            Matcher fromSpaceLength = fromSpaceLengthPattern.matcher(fromSpace);
            if (fromSpaceLength.find()) {
                return Long.parseLong(fromSpaceLength.group(0));
            }
        }
        return 0;
    }
}
