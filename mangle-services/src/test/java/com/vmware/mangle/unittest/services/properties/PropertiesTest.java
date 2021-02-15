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

package com.vmware.mangle.unittest.services.properties;

import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.utils.ReadProperty;

/**
 * Tests to validate the defaults for the check-ins
 *
 * @author chetanc
 *
 */
public class PropertiesTest {

    private String applicationPropertiesFile = "src/main/resources/application.properties";
    private Map<String, String> propertyMap = new HashMap<>();

    @BeforeClass
    public void readPropertyFile() {
        ReadProperty.readPropertiesAsMap(propertyMap, applicationPropertiesFile);
    }

    @Test
    public void verifyCassandraDefaultHost() {
        String property = propertyMap.get("spring.data.cassandra.contact-points");

        Assert.assertNotNull(property, "Cassandra property cannot be null");
        Assert.assertEquals(property, "${cassandraContactPoints:localhost}",
                "spring.data.cassandra.contact-points property has to be localhost for the check-in to proceed");
    }

    @Test
    public void verifyDefaultTomcatDir() {
        String property = propertyMap.get("server.tomcat.basedir");

        Assert.assertNotNull(property, "Tomcat base directory property cannot be null");
        Assert.assertEquals(property, "/home/mangle/var/opt/mangle-tomcat", "Tomcat directory cannot be "
                + "changed");
    }

    @Test
    public void verifyHazelcastDefaultProperties() {
        String configProperty = propertyMap.get("hazelcast.config.public");
        String validationToken = propertyMap.get("hazelcast.config.validationToken");

        Assert.assertNotNull(configProperty, "hazelcast config property cannot be null");
        Assert.assertEquals(configProperty, "${publicAddress:}",
                "hazelcast.config.public property has to be localhost for the check-in to proceed");
        Assert.assertEquals(validationToken, "${clusterValidationToken:}",
                "hazelcast.config.public property has to be left blank for the check-in to proceed");
    }
}
