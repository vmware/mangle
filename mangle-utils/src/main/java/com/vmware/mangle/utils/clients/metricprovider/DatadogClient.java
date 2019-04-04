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

import com.vmware.mangle.cassandra.model.metricprovider.DatadogConnectionProperties;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author dbhat
 *
 */
@EnableConfigurationProperties
@Log4j2
public class DatadogClient extends RestTemplateWrapper implements MetricProviderClient {
    private String apiKey;
    private String applicationKey;
    private String datadogApiInstanceUrl;

    /**
     * @param datadogConnectionProperties
     * @throws MangleException
     */
    public DatadogClient(DatadogConnectionProperties datadogConnectionProperties) throws MangleException {
        this.apiKey = datadogConnectionProperties.getApiKey();
        this.applicationKey = datadogConnectionProperties.getApplicationKey();
        this.datadogApiInstanceUrl = datadogConnectionProperties.getUri();
        if (!isInputDataValid()) {
            throw new MangleException("Invalid field data for Datadog API Key or Application Key",
                    ErrorCode.FIELD_VALUE_EMPTY);
        }
        setHeadersAndBaseUrl();
    }

    /**
     * Set headers and base url for Datadog client
     */
    private void setHeadersAndBaseUrl() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept-Charset", "UTF-8");
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");
        turnOffSslChecking();
        setHeaders(headers);
        setBaseUrl(datadogApiInstanceUrl);
        setCommonQueryParameter("?" + Constants.DATADOG_API_KEY + this.apiKey + Constants.AND_OPERATOR
                + Constants.DATADOG_APPLICATION_KEY + this.applicationKey);
    }

    /**
     * Method to validate the Datadog API Instance, Datadog API Key and Application tokens for null
     * value
     *
     * @return true: if the validation is successful and false: if the validation is a failure
     */
    private boolean isInputDataValid() {
        if (StringUtils.isEmpty(apiKey)) {
            log.error("Invalid API Key provided. The API key cannot be null or blank");
            return false;
        }
        if (StringUtils.isEmpty(applicationKey)) {
            log.error("Application Key is Invalid. The key cannot be null or Empty");
            return false;
        }
        if (StringUtils.isEmpty(datadogApiInstanceUrl)) {
            log.error("Datadog instance is empty. Datadog instance must be defined in application.properties file");
            return false;
        }
        return true;
    }

    /**
     * Validate test connection with Datadog using the specified tokens.
     *
     * @return boolean
     */
    @Override
    public boolean testConnection() {
        log.debug("Validating test connection with Datadog using the specified tokens");
        ResponseEntity<String> response =
                (ResponseEntity<String>) this.get(Constants.DATADOG_VALIDATE_CONNECTION_API, String.class);
        if (response == null) {
            log.error("Test connection with Datadog has failed.");
            return false;
        }
        log.debug("Test connection completed and status: " + response.getStatusCode().value());
        return response.getStatusCode().value() == 200;
    }

}
