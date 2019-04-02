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

package com.vmware.mangle.utils.clients.restclient;

import java.io.IOException;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

/**
 * @author rthomas
 *
 */
@Log4j2
public class RestErrorHandler implements ResponseErrorHandler {

    private static String responseBody;
    private static HttpStatus status;

    /* (non-Javadoc)
     * @see org.springframework.web.client.ResponseErrorHandler#hasError(org.springframework.http.client.ClientHttpResponse)
     */
    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode() != HttpStatus.OK && response.getStatusCode() != HttpStatus.NO_CONTENT;
    }

    /* (non-Javadoc)
     * @see org.springframework.web.client.ResponseErrorHandler#handleError(org.springframework.http.client.ClientHttpResponse)
     */
    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        log.error(response.getStatusText());
        setStatus(response.getStatusCode());
        setResponseBody(response.getStatusText());
        //              StringWriter writer = new StringWriter();
        //              IOUtils.copy(response.getBody(), writer);
        //              setResponseBody(writer.toString());

    }

    /**
     * @return the responseBody
     */
    public static String getResponseBody() {
        return responseBody;
    }

    /**
     * @param responseBody
     *            the responseBody to set
     */
    public static void setResponseBody(String responseBody) {
        RestErrorHandler.responseBody = responseBody;
    }

    /**
     * @return the status
     */
    public static HttpStatus getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public static void setStatus(HttpStatus status) {
        RestErrorHandler.status = status;
    }

}
