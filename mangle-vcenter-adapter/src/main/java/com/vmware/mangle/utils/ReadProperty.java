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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import lombok.extern.log4j.Log4j2;

/**
 * @author saurabhs
 *
 */
@Log4j2
public class ReadProperty {

    private ReadProperty() {
    }

    private static Properties readProperties(Properties prop, File f) {
        try {
            FileInputStream fis = new FileInputStream(f);
            prop.load(fis);
        } catch (IOException e) {
            log.error(e);
        }
        return prop;
    }

    public static Map<String, String> readPropertiesAsMap(String filePath) {
        Map<String, String> propertyMap = new HashMap<>();
        Properties prop = new Properties();
        prop = readProperties(prop, new File(filePath));
        for (String key : prop.stringPropertyNames()) {
            String value = prop.getProperty(key);
            propertyMap.put(key, value);
        }
        return propertyMap;
    }
}
