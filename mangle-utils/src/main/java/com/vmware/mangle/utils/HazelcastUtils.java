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

package com.vmware.mangle.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

/**
 * Utility class for hazelcast cluster related helper methods
 *
 * @author chetanc
 *
 */
public class HazelcastUtils {

    private HazelcastUtils() {
    }

    /**
     * Method to retrieve hazelcast cluster members list
     *
     */
    public static List<String> getMembersList(String hazelcastMembers) {
        List<String> members = new ArrayList<>();
        if (!StringUtils.isEmpty(hazelcastMembers)) {
            StringTokenizer tokens = new StringTokenizer(hazelcastMembers, ",");
            while (tokens.hasMoreElements()) {
                members.add(tokens.nextToken());
            }
        }
        return members;
    }

    public static String getApplicationValidationToken(String validationToken) {
        if (StringUtils.isEmpty(validationToken)) {
            validationToken = UUID.randomUUID().toString();
        }
        return validationToken;
    }

}
