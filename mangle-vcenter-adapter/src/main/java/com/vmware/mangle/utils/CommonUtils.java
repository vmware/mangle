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

import lombok.extern.log4j.Log4j2;

/**
 * @author chetanc
 *
 *
 */
@Log4j2
public class CommonUtils {

    private CommonUtils() {
    }

    public static void delayInSeconds(int second) {
        try {
            log.info("Sleeping for " + second + " seconds");
            Thread.sleep(second * 1000L);
        } catch (InterruptedException e) {
            log.error(e);
            Thread.currentThread().interrupt();
        }
    }
}