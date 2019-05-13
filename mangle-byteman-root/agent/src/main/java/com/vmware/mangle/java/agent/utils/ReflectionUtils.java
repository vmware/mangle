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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * @author hkilari
 *
 */
public class ReflectionUtils {

    private static final Logger LOG = Logger.getLogger(ReflectionUtils.class.getName());

    private ReflectionUtils() {
        throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    private static Method getMethod(Class<?> cls, String methodName, Class<?>[] params) {
        try {
            return cls.getDeclaredMethod(methodName, params);

        } catch (IllegalArgumentException | NoSuchMethodException | SecurityException e) {
            LOG.severe(e.getLocalizedMessage());
        }
        return null;
    }

    public static Object invokeMethod(Object object, String methodName, Class<?>[] paramsType, Object[] params) {
        Method method = getMethod(object.getClass(), methodName, paramsType);
        try {
            return method.invoke(object, params);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Object getSingletonInstance(String className) {
        Class<?> cls;
        try {
            cls = getSystemClassLoader().loadClass(className);

            Method method = cls.getMethod("getInstance", new Class[0]);
            return method.invoke(null, new Object[0]);
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            LOG.severe(e.getLocalizedMessage());
        }

        return null;
    }

    public static Object getObject(String className) {
        try {
            Class<?> cls = getSystemClassLoader().loadClass(className);
            return cls.newInstance();
        } catch (ClassNotFoundException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ClassLoader getSystemClassLoader() {
        return ClassLoader.getSystemClassLoader();
    }
}
