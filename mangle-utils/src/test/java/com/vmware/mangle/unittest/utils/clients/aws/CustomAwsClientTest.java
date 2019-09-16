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

package com.vmware.mangle.unittest.utils.clients.aws;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.vmware.mangle.utils.clients.aws.CustomAwsClient;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 *
 *
 * @author bkaranam
 */
public class CustomAwsClientTest {

    private final String AWS_REGION = "DummyRegion";
    private final String AWS_ACCESS_KEY_ID = "DummyAccessKeyId";
    private final String AWS_SECRET_ACCESS_KEY = "DummySecretAccessKey";
    private CustomAwsClient customAwsClient;

    @Test
    public void testVcenterClientInstantiation() throws Exception {
        customAwsClient = new CustomAwsClient(AWS_REGION, AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);
        Assert.assertEquals(System.getProperty("aws.accessKeyId"), AWS_ACCESS_KEY_ID);
        Assert.assertEquals(System.getProperty("aws.secretKey"), AWS_SECRET_ACCESS_KEY);
    }

    @Test
    public void testTestConnectionWithInvalidRegion() throws Exception {
        customAwsClient = new CustomAwsClient(AWS_REGION, AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);
        boolean result = false;
        try {
            result = customAwsClient.testConnection();
        } catch (MangleException exception) {
            Assert.assertEquals(exception.getErrorCode(), ErrorCode.AWS_INVALID_REGION);
        }
        Assert.assertFalse(result);
    }

    @Test
    public void testTestConnectionWithValidRegion() throws Exception {
        customAwsClient = new CustomAwsClient("us-west-1", AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY);
        boolean result = false;
        try {
            result = customAwsClient.testConnection();
        } catch (MangleException exception) {
            Assert.assertEquals(exception.getErrorCode(), ErrorCode.AWS_INVALID_CREDENTIALS);
        }
        Assert.assertFalse(result);
    }

}
