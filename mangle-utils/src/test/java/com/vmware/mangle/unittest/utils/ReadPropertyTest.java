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

package com.vmware.mangle.unittest.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.testng.annotations.Test;

import com.vmware.mangle.utils.ReadProperty;

/**
 *
 *
 * @author chetanc
 */
public class ReadPropertyTest {

    private static final String filename = "mangle.config";
    private static final String filename1 =
            "src/main/resources/FaultOperationProperties/VCenterFaultOperations.properties";

    @Test
    public void testReadProperty() {
        Properties property = ReadProperty.readProperty(filename);
        String port = property.getProperty("docker_api_default_port");
        Assert.assertEquals("2375", port);
    }

    @Test(description = "verify the retrieval of the given property from a given file, return null if the given file is not valid")
    public void testReadProperty2() {
        Properties property = ReadProperty.readProperty("");
        String port = property.getProperty("docker_api_default_port");
        Assert.assertEquals(null, port);
    }

    @Test
    public void testReadProperties() {
        Map<String, String> map = new HashMap<>();
        ReadProperty.readPropertiesAsMap(map, filename1);
        Assert.assertEquals(10, map.size());
    }

    @Test(description = "Test to verify the failure to retrieve a properties from a file when the file name is invalid")
    public void testReadProperties1() {
        Map<String, String> map = new HashMap<>();
        ReadProperty.readPropertiesAsMap(map, ".");
        Assert.assertEquals(0, map.size());
    }

}
