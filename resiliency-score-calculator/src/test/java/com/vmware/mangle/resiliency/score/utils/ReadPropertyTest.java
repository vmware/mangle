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

package com.vmware.mangle.resiliency.score.utils;

import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests for class ReadProperty
 * 
 * @author ranjans
 */
@PowerMockIgnore("javax.management.*")
@PrepareForTest(ReadProperty.class)
public class ReadPropertyTest extends PowerMockTestCase {

    @Test
    public void readPropertyTest() {
        String file = "application.properties";
        Assert.assertTrue(!ReadProperty.readProperty(file).isEmpty());
        Assert.assertTrue(!ReadProperty
                .readProperty(ReadPropertyTest.class.getClassLoader().getResource(file).getPath()).isEmpty());
    }

}
