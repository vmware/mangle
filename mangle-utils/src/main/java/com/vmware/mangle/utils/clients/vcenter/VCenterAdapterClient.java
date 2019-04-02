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

package com.vmware.mangle.utils.clients.vcenter;

import java.net.MalformedURLException;
import java.net.URL;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.vmware.mangle.cassandra.model.endpoint.VCenterAdapterProperties;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Client that talks to VCenter Adapter, to delegate the vcenter related faults/remediation
 *
 * @author chetanc
 */
@Log4j2
public class VCenterAdapterClient extends RestTemplateWrapper {

    private VCenterAdapterProperties vCenterAdapterProperties;

    private static final String VC_AUTH_BASIC = "Basic ";
    private static final String AUTHORIZATION = "Authorization";

    public VCenterAdapterClient(VCenterAdapterProperties vCenterAdapterProperties) {
        this.vCenterAdapterProperties = vCenterAdapterProperties;
        setBaseUrl(vCenterAdapterProperties.getVcAdapterUrl());
        setDefaultHeaders();
    }

    /**
     * Does the default header configuration for the client
     */
    public void setDefaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(AUTHORIZATION, VC_AUTH_BASIC
                + new String(Base64.encodeBase64((vCenterAdapterProperties.getUsername() + ":" + vCenterAdapterProperties.getPassword()).getBytes())));
        setHeaders(headers);
    }

    public boolean testConnection() throws MangleException {
        boolean returnValue = false;
        URL aURL;
        try {
            aURL = new URL(getBaseUrl());
            if (CommonUtils.isServerListening(aURL.getHost(), aURL.getPort())) {
                returnValue = true;
            } else {
                log.debug(
                        "Failed to communicate with the vCenter Adapter, Please verify that the vCenter adapter is active and running");
            }
        } catch (MalformedURLException e) {
            log.error("Invalid vCenter adapter URL provided : {}", getBaseUrl());
            throw new MangleException(ErrorCode.INVALID_URL, getBaseUrl());
        }
        return returnValue;
    }
}
