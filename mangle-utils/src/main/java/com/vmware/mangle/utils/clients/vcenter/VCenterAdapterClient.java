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

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.vmware.mangle.cassandra.model.endpoint.VCenterAdapterProperties;
import com.vmware.mangle.model.vcenter.VCenterAdapterHealthStatus;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.constants.VCenterConstants;
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
        headers.set(AUTHORIZATION, VC_AUTH_BASIC + new String(Base64.encodeBase64(
                (vCenterAdapterProperties.getUsername() + ":" + vCenterAdapterProperties.getPassword()).getBytes())));
        setHeaders(headers);
    }

    public boolean testConnection() throws MangleException {
        ResponseEntity<VCenterAdapterHealthStatus> testConnectionResponse =
                (ResponseEntity<VCenterAdapterHealthStatus>) get(VCenterConstants.VC_ADAPTER_HEALTH_CHECK,
                        VCenterAdapterHealthStatus.class);
        if (null != testConnectionResponse && testConnectionResponse.getStatusCode() == HttpStatus.OK
                && testConnectionResponse.getBody() != null
                && testConnectionResponse.getBody().getStatus().equals("UP")) {
            return true;
        } else {
            log.error(ErrorConstants.VCENTER_ADAPTER_CLIENT_UNREACHABLE);
            throw new MangleException(String.format(ErrorConstants.VCENTER_ADAPTER_CLIENT_UNREACHABLE, getBaseUrl()) ,
                    ErrorCode.VCENTER_ADAPTER_CLIENT_UNREACHABLE);
        }
    }
}
