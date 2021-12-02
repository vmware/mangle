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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.metricprovider.DynatraceConnectionProperties;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.constants.MetricProviderConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * API client for Dynatrace Metric monitoring system.
 *
 * @author dbhat
 */

@EnableConfigurationProperties
@Log4j2
public class DynatraceApiClient extends RestTemplateWrapper implements MetricProviderClient {
    private String apiToken;
    private String uri;

    public DynatraceApiClient(DynatraceConnectionProperties dynatraceConnectionProperties) throws MangleException {
        this.apiToken = dynatraceConnectionProperties.getApiToken();
        this.uri = dynatraceConnectionProperties.getUri();

        if (StringUtils.isEmpty(this.apiToken) || StringUtils.isEmpty(this.uri)) {
            throw new MangleException("Invalid input: API Token, URI cannot be empty or null",
                    ErrorCode.FIELD_VALUE_EMPTY);
        }
        initApiHeaders();
    }

    /**
     * Details on the headers and Auth token configuration can be found at :
     * https://www.dynatrace.com/support/help/dynatrace-api/basics/dynatrace-api-authentication/
     */

    private void initApiHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept-Charset", "UTF-8");
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");
        headers.set(MetricProviderConstants.AUTHORIZATION,
                MetricProviderConstants.API_TOKEN + MetricProviderConstants.SPACE + apiToken);
        turnOffSslChecking();
        setHeaders(headers);
        setBaseUrl(uri);
    }

    @Override
    public boolean testConnection() throws MangleException {
        ResponseEntity<String> response = (ResponseEntity<String>) this
                .get(MetricProviderConstants.DYNATRACE_API_GET_CLUSTER_VERSION, String.class);
        log.debug("Dynatrace cluster info API returned : " + response);
        if (StringUtils.isEmpty(response)) {
            throw new MangleException(ErrorCode.UNABLE_TO_CONNECT_TO_DYNATRACE_INSTANCE);
        }
        /**
         * As per the API documentation of Dynatrace, the GET APIs will return 200. Hence, making a
         * hard check of status code 200 here. Reference:
         * https://www.dynatrace.com/support/help/dynatrace-api/basics/dynatrace-api-response-codes/
         */
        if (response.getStatusCode().value() != 200) {
            throw new MangleException(ErrorCode.AUTH_FAILURE_TO_DYNATRACE);
        }
        return response.getStatusCode().value() == 200;
    }

}
