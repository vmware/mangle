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

package com.vmware.mangle.faults.plugin.helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Helper class to support Lifecycle Operations on Byteman Agent Fault
 *
 * @author hkilari
 *
 */
public class FaultsHelper {

    private FaultsHelper() {
    }

    public static Map<String, String> parseArgs(String argsString) {
        Map<String, String> faultArgs = new HashMap<>();
        StringTokenizer stringTokenizer = new StringTokenizer(argsString, ",");
        while (stringTokenizer.hasMoreTokens()) {
            String token = stringTokenizer.nextToken().trim();
            if (token.contains("__")) {
                faultArgs.put(token.substring(2), stringTokenizer.nextToken().trim());
            }
        }
        return faultArgs;
    }
}
