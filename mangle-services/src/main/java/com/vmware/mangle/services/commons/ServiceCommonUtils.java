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

package com.vmware.mangle.services.commons;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * @author chetanc
 *
 *
 */
public class ServiceCommonUtils {

    private ServiceCommonUtils() {

    }

    private static ApplicationContext applicationContext;

    public static void setApplicationContext(ApplicationContext appContext) throws BeansException {
        applicationContext = appContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static Object getBean(Class aClass) {
        return applicationContext.getBean(aClass);
    }
}
