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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import lombok.extern.log4j.Log4j2;

/**
 * @author saurabhs
 *
 */
@SuppressWarnings("squid:S2093")
@Log4j2
public class ReadProperty {

    private ReadProperty() {
    }

    public static Properties readProperty(String configPropFileName) {
        Properties prop = null;
        String path = configPropFileName;
        File file = new File(path);
        try (InputStream fileInput = file.exists() ? new FileInputStream(file)
                : ReadProperty.class.getClassLoader().getResourceAsStream(configPropFileName)) {
            prop = new Properties();
            prop.load(fileInput);
            fileInput.close();
        } catch (IOException e) {
            log.error(e);
        }
        return prop;
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

    public static void readPropertiesAsMap(Map<String, String> propertymap, String filePath) {
        Properties prop = new Properties();
        prop = readProperties(prop, new File(filePath));
        for (String key : prop.stringPropertyNames()) {
            String value = prop.getProperty(key);
            propertymap.put(key, value);
        }
    }

    public static Properties editProperty(String configPropFileName, String propToRepace, String newValue) {
        Properties prop = new Properties();
        String path = ReadProperty.class.getClassLoader().getResource(configPropFileName).getPath();
        log.info("File path" + path);
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        log.info("Loading properties");
        try {
            fileInputStream = new FileInputStream(path);
            prop.load(fileInputStream);
        } catch (IOException e) {
            log.info("Error in loading properties " + e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    log.info("Error in closing file " + e);
                }
            }
        }
        log.info("Modifying properties");
        try {
            File file = new File(path);
            fileOutputStream = new FileOutputStream(file);
            if (prop.containsKey(propToRepace)) {
                prop.setProperty(propToRepace, newValue);
            } else {
                log.info("Key: " + propToRepace + ",Value :" + newValue);
                prop.put(propToRepace, newValue);
            }
            prop.store(fileOutputStream, "Wrote property");

        } catch (IOException e) {
            log.error(e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    log.info("Error in closing file " + e);
                }
            }
        }
        return prop;
    }
}
