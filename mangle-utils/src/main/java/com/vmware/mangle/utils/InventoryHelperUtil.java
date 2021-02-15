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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.stereotype.Component;

import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author chetanc
 */
@Component
public class InventoryHelperUtil {

    private InventoryHelperUtil() {
    }

    /**
     * Converts response value object to the object of the given class
     *
     * @param object:
     *            source object
     * @param tClass:
     *            resulting object class
     * @param <T>
     * @return: object of the resulting class
     */
    public static <T> T convertLinkedHashMapToObject(Object object, Class<T> tClass) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(object, tClass);
    }

    /**
     * Converts response value object to the list of object of the given class
     *
     * @param object:
     *            source object
     * @param tClass:
     *            resulting object class
     * @param <T>
     * @return: list of objects of the resulting class
     */
    public static <T> List<T> convertLinkedHashMapToObjectList(Object object, Class<T> tClass) throws
            MangleException {
        ObjectMapper mapper = new ObjectMapper();
        CollectionType listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, tClass);
        List<T> listOfObjects;
        try {
            listOfObjects = mapper.readValue(RestTemplateWrapper.objectToJson(object), listType);
        } catch (IOException e) {
            throw new MangleException(ErrorCode.IO_EXCEPTION);
        }
        return listOfObjects;
    }
}
