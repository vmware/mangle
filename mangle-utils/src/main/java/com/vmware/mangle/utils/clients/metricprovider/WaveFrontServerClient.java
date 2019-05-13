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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.metricprovider.WaveFrontConnectionProperties;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.constants.MetricProviderConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author ranjans
 *
 *         Endpoint client for WaveFront Server
 */
@Log4j2
public class WaveFrontServerClient extends RestTemplateWrapper implements MetricProviderClient {

    private WaveFrontConnectionProperties wfConnProperties;

    public WaveFrontServerClient(String wavefrontUrl, String wavefrontAPIToken) {
        setHeadersAndBaseUrl(wavefrontUrl, wavefrontAPIToken);
    }

    public WaveFrontServerClient(WaveFrontConnectionProperties properties) {
        this(properties.getWavefrontInstance(), properties.getWavefrontAPIToken());
        this.wfConnProperties = properties;
    }

    @Override
    public boolean testConnection() throws MangleException {
        ResponseEntity<String> response =
                (ResponseEntity<String>) this.get(MetricProviderConstants.WAVEFRONT_API_TEST_CONNECTION, String.class);
        log.debug("Response on trying to test connection with Wavefront instance: \n" + response);
        if (StringUtils.isEmpty(response)) {
            throw new MangleException(ErrorCode.UNABLE_TO_CONNECT_TO_WAVEFRONT_INSTANCE);
        }
        if (response.getStatusCode().value() != 200) {
            throw new MangleException(ErrorCode.AUTH_FAILURE_TO_WAVEFRONT);
        }
        return response.getStatusCode().value() == 200;
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
