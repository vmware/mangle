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

package com.vmware.mangle;

import org.testng.Assert;
import org.testng.annotations.Test;

/*
 * Class for mangle-ui sonar validation test
 *
 * @author ranjans
 */

public class MangleUiSonarValidationTest {

    /*
     * method for sonar validation test
     *
     * @throws InterruptedException
     */

    @Test
    public void sonarValidTest() throws InterruptedException {
        MangleUiSonarValidation mangleUiSonarValidation = new MangleUiSonarValidation();
        mangleUiSonarValidation.sonarValid(10);
        Assert.assertEquals(10, mangleUiSonarValidation.i);
    }
}
