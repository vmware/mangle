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

import org.springframework.http.HttpHeaders;

import com.vmware.mangle.cassandra.model.metricprovider.WaveFrontConnectionProperties;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;

/**
 * @author ranjans
 *
 *         Endpoint client for WaveFront Server
 */
public class WaveFrontServerClient extends RestTemplateWrapper implements MetricProviderClient {

    public WaveFrontServerClient(String wavefrontUrl, String wavefrontAPIToken) {
        setHeadersAndBaseUrl(wavefrontUrl, wavefrontAPIToken);
    }

    public WaveFrontServerClient(WaveFrontConnectionProperties properties) {
        this(properties.getWavefrontInstance(), properties.getWavefrontAPIToken());
    }

    @Override
    public boolean testConnection() {
        return this.get("/api/v2/source?limit=10", String.class).getStatusCode().value() == 200;
    }

    private void setHeadersAndBaseUrl(String wavefrontUrl, String wavefrontAPIToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept-Charset", "UTF-8");
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");
        headers.set("Authorization", "Bearer " + wavefrontAPIToken);
        turnOffSslChecking();
        setHeaders(headers);
        setBaseUrl(wavefrontUrl);
    }

}
