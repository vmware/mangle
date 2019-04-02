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

package com.vmware.mangle.utils.clients.metricprovider;

import lombok.extern.log4j.Log4j2;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.metricprovider.DatadogConnectionProperties;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author dbhat Test to validate Datadog API client
 *
 */
@Log4j2
public class DatadogClientTest {

    /**
     * @throws MangleException
     *             Test is to validate the exception "MangleException" when null data is passed for
     *             API key and Application key
     */
    @Test(expectedExceptions = MangleException.class, description = "Validate the exception when invalid api tokens are passed")
    public void datadogConnectionWithNullTokens() throws MangleException {
        DatadogClient dgClient = new DatadogClient(new DatadogConnectionProperties());
        dgClient.testConnection();
    }

    /**
     * @throws MangleException
     *             Test is to validate the exception "MangleException" when null data is passed for
     *             Application key
     */
    @Test(expectedExceptions = MangleException.class, description = "Validate the exception when null application token is specified")
    public void datadogConnectionWithNullAppToken() throws MangleException {
        DatadogConnectionProperties datadogConnectionProperties = getDatadogClientProperty();
        datadogConnectionProperties.setApplicationKey("");
        DatadogClient dgClient = new DatadogClient(datadogConnectionProperties);
        dgClient.testConnection();
    }

    /**
     * @throws MangleException
     *             Test is to validate the exception "MangleException" when null data is passed for
     *             Datadog API Instance name
     */
    @Test(expectedExceptions = MangleException.class, description = "Validate the exception when null datadog api instance is specified")
    public void datadogConnectionWithNullDatadogInstance() throws MangleException {
        DatadogConnectionProperties datadogConnectionProperties = getDatadogClientProperty();
        datadogConnectionProperties.setUri("");
        DatadogClient dgClient = new DatadogClient(datadogConnectionProperties);
        dgClient.testConnection();
    }

    /**
     * @throws MangleException
     *             Test is to validate the exception "MangleException" when null data is passed for
     *             API key
     */
    @Test(expectedExceptions = MangleException.class, description = "Validate the exception when null api token is specified")
    public void datadogConnectionWithNullApiToken() throws MangleException {
        DatadogConnectionProperties datadogConnectionProperties = getDatadogClientProperty();
        datadogConnectionProperties.setApiKey("");
        DatadogClient dgClient = new DatadogClient(datadogConnectionProperties);
        dgClient.testConnection();
    }

    /**
     * @throws MangleException
     *             Test is to validate Datadog test connection returning failure when invalid/wrong
     *             tokens are specified. The test will also validate the Request URL used.
     */
    @Test(description = "Validating of Datadog test connection when invalid tokens are specified")
    public void dataDogConnectionWithInvalidTokends() throws MangleException {
        DatadogConnectionProperties datadogConnectionProperties = getDatadogClientProperty();
        String expectedApiUrl = datadogConnectionProperties.getUri() + "/api/v1/validate?api_key="
                + datadogConnectionProperties.getApiKey() + "&application_key="
                + datadogConnectionProperties.getApplicationKey();
        DatadogClient dgClient = new DatadogClient(datadogConnectionProperties);
        Assert.assertFalse(dgClient.testConnection());
        Assert.assertEquals(dgClient.getRequestUrl(), expectedApiUrl);
    }

    private DatadogConnectionProperties getDatadogClientProperty() {
        DatadogConnectionProperties datadogConnectionProperties = new DatadogConnectionProperties();
        datadogConnectionProperties.setApiKey("dummy-key-is-here");
        datadogConnectionProperties.setApplicationKey("dummy-app-key-is-again");
        datadogConnectionProperties.setUri("https://api.datadoghq.com");
        return datadogConnectionProperties;
    }

}
