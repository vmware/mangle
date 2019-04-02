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

import org.junit.Assert;
import org.testng.annotations.Test;

import com.vmware.mangle.utils.ApiUtils;

/**
 * @author dbhat
 *
 */
public class ApiUtilsTest {

    @Test(description = "Test to validate response codes other than 2xx are validated as failure")
    public void validateOtherThan2xxCodes() {
        Assert.assertFalse(ApiUtils.isResponseCodeSuccess(301));
        Assert.assertFalse(ApiUtils.isResponseCodeSuccess(304));
        Assert.assertFalse(ApiUtils.isResponseCodeSuccess(401));
        Assert.assertFalse(ApiUtils.isResponseCodeSuccess(403));
        Assert.assertFalse(ApiUtils.isResponseCodeSuccess(404));
        Assert.assertFalse(ApiUtils.isResponseCodeSuccess(500));
    }

    @Test(description = "Test to validate response codes 2xx are validated as Success")
    public void validate2xxStatusCodes() {
        Assert.assertTrue(ApiUtils.isResponseCodeSuccess(200));
        Assert.assertTrue(ApiUtils.isResponseCodeSuccess(201));
        Assert.assertTrue(ApiUtils.isResponseCodeSuccess(202));
        Assert.assertTrue(ApiUtils.isResponseCodeSuccess(204));
    }

}
