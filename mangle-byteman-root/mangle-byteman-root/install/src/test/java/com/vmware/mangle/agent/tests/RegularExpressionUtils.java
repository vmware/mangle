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

package com.vmware.mangle.agent.tests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hkilari
 *
 */
public class RegularExpressionUtils {
    private RegularExpressionUtils() {
        throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * @param latestCommandOutput
     * @param regExpression
     * @return
     */
    public static String extractField(String latestCommandOutput, String regExpression) {
        Pattern p = Pattern.compile(regExpression);
        Matcher m = p.matcher(latestCommandOutput);
        if (m.find()) {
            return m.group(0);
        }
        return null;
    }
}
