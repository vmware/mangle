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

package com.vmware.mangle.utils.mockdata;

import com.vmware.mangle.cassandra.model.metricprovider.DatadogConnectionProperties;
import com.vmware.mangle.cassandra.model.metricprovider.WaveFrontConnectionProperties;

/**
 * @author dbhat
 *
 */
public class MetricProviderMock {
    private MetricProviderMock() {

    }

    public static String WAVEFRONT_INSTANCE = "https://try.wavefront.com";
    public static String dummyApiKey = "dummy-api-key";
    public static String dummyAppKey = "dummyAppKey";
    public static String dummyPrefix = "metric-prefix";
    public static String source = "mangle";
    public static String datadogInstance = "https://api.datadoghq.com";
    public static String WAVEFRONT_PROXY_PORT = "2878";

    public static WaveFrontConnectionProperties getDummyWavefrontConnectionProperties() {
        WaveFrontConnectionProperties properties = new WaveFrontConnectionProperties();
        properties.setSource(source);
        properties.setWavefrontAPIToken(dummyApiKey);
        properties.setWavefrontInstance(WAVEFRONT_INSTANCE);
        return properties;
    }

    public static DatadogConnectionProperties getDummyDatadogConnectionProperties() {
        DatadogConnectionProperties properties = new DatadogConnectionProperties();
        properties.setApiKey(dummyApiKey);
        properties.setApplicationKey(dummyAppKey);
        properties.setUri(datadogInstance);
        return properties;
    }

}
