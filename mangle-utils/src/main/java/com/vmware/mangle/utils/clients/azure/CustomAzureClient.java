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

package com.vmware.mangle.utils.clients.azure;

import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import lombok.extern.log4j.Log4j2;

import com.vmware.mangle.utils.clients.endpoint.EndpointClient;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Api client for managing Azure services
 *
 * @author bkaranam
 */
@Log4j2
public class CustomAzureClient implements EndpointClient {

    /** The client ID. */
    private final String clientId;

    /** The client secret key. */
    private final String clientKey;

    /** The subscription ID. */
    private final String subscriptionId;

    /** The tenant */
    private final String tenant;

    /**
     * The constructor allows you to provide your subscriptionId ,tenant,clientId and clientKey
     *
     * @param tenant
     *            tenant id
     * @param subscriptionId
     *            azure subscription id
     * @param clientId
     *            the application/client id
     * @param clientKey
     *            the client sceret key
     */
    public CustomAzureClient(String subscriptionId, String tenant, String clientId, String clientKey) {
        this.clientId = clientId;
        this.clientKey = clientKey;
        this.subscriptionId = subscriptionId;
        this.tenant = tenant;
    }


    /**
     * Azure client
     *
     * @return azure client
     */
    @SuppressWarnings("deprecation")
    public Azure getClient() {
        ApplicationTokenCredentials credentials =
                new ApplicationTokenCredentials(this.clientId, this.tenant, this.clientKey, AzureEnvironment.AZURE);
        Azure azure = Azure.authenticate(credentials).withSubscription(this.subscriptionId);
        return azure;
    }

    @Override
    public boolean testConnection() throws MangleException {
        try {
            getClient().subscriptions().list();
        } catch (RuntimeException exception) {
            validateAzureException(exception);
        }
        return true;
    }

    private void validateAzureException(RuntimeException exception) throws MangleException {

        try {
            if (null != exception.getCause().getCause().getCause()
                    && exception.getCause().getCause().getCause() instanceof AuthenticationException) {
                log.error(exception.getCause().getCause().getCause().getMessage());
                throw new MangleException(ErrorCode.AZURE_INVALID_CREDENTIALS, exception.getMessage());

            }
            throw new MangleException(ErrorCode.AZURE_UNKNOWN_ERROR, exception.getMessage());
        } catch (NullPointerException npe) {
            throw new MangleException(ErrorCode.AZURE_UNKNOWN_ERROR, exception.getMessage());
        }
    }
}
