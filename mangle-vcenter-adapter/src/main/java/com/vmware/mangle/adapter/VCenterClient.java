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

package com.vmware.mangle.adapter;

import static com.vmware.mangle.utils.VCenterAPIEndpoints.REST_HEALTH_CHECK;
import static com.vmware.mangle.utils.VCenterAPIEndpoints.REST_SESSION;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.vmware.mangle.model.VCCisSession;
import com.vmware.mangle.model.VCHealth;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.restclienttemplate.RestTemplateWrapper;

/**
 * @author Chethan C(chetanc)
 *
 *         Provides a VC client object, on whichÂ different operations can be triggered using rest
 *         calls
 */
@Log4j2
public class VCenterClient extends RestTemplateWrapper {
    private static final String PROJECT_MANGLE = "mangle";
    private static final String VC_SESSION_ID = "vmware-api-session-id";
    private static final String VC_AUTH_HEADER = "vmware-use-header-authn";
    private static final String VC_AUTH_BASIC = "Basic ";
    private static final String AUTHORIZATION = "Authorization";
    private static final String VC_HEALTH_GREEN = "green";
    private static final String VC_HEALTH_RED = "red";
    private static final String VC_HEALTH_ORANGE = "orange";
    private static String baseURL = "https://%s";

    /**
     * Instantiates VcenterClient object
     */
    public VCenterClient() {
        super();
    }

    public VCenterClient(String vcserverurl, String vcUsername, String vcPassword) throws MangleException {
        super();
        setBaseUrl(String.format(baseURL, vcserverurl));
        setDefaultHeaders(vcUsername, vcPassword);
    }

    /**
     * Authenticates on the vCenter for the given vcUsername and vcPassword
     *
     * @return: authentication token id or vmware-api-session-id
     */
    private String getAuthToken() throws MangleException {
        isVCenterServerReachable();
        VCCisSession response = null;
        try {
            response = (VCCisSession) post(REST_SESSION, null, VCCisSession.class).getBody();
        } catch (Exception e) {
            throw new MangleException(ErrorConstants.VCENTER_AUTHENTICATION_FAILED);
        }
        return response.getValue();
    }

    public void initializeDefaults(String vcserverurl, String vcUsername, String vcPassword) throws MangleException {
        setBaseUrl(String.format(baseURL, vcserverurl));
        setDefaultHeaders(vcUsername, vcPassword);
    }

    /**
     * - assigns default headers that are required for the client to communicate and perform actions on
     * the vCenter - vmware-api-session-id is generated for the given vcUsername and vcPassword
     *
     * @param vcUsername:
     *            vCenter vcUsername
     * @param vcPassword:
     *            vCenter vcPassword
     */
    private void setDefaultHeaders(String vcUsername, String vcPassword) throws MangleException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(AUTHORIZATION,
                VC_AUTH_BASIC + new String(Base64.encodeBase64((vcUsername + ":" + vcPassword).getBytes())));
        headers.set(VC_AUTH_HEADER, PROJECT_MANGLE);
        setHeaders(headers);
        headers.set(VC_SESSION_ID, getAuthToken());
        setHeaders(headers);
    }

    private boolean isVCenterServerReachable() throws MangleException {
        ResponseEntity responseEntity = get(REST_HEALTH_CHECK, Object.class);
        if (responseEntity == null) {
            throw new MangleException(ErrorConstants.VCENTER_NOT_REACHABLE);
        }
        return true;
    }

    /**
     * checks if the vcenter system is healthy
     *
     * @return: true if vCenter is healthy; else false
     */
    public boolean testConnection() throws MangleException {
        try {
            VCHealth health = (VCHealth) get(REST_HEALTH_CHECK, VCHealth.class).getBody();
            String healthStatus = health == null ? VC_HEALTH_RED : health.getValue();
            if (healthStatus.equals(VC_HEALTH_GREEN) || healthStatus.equals(VC_HEALTH_ORANGE)) {
                return true;
            } else {
                throw new MangleException("VCenter health check failed");
            }
        } catch (Exception e) {
            throw new MangleException("VCenter health check failed with exception " + e.getMessage());
        }
    }

    public void terminateConnection() {
        @SuppressWarnings("rawtypes")
        ResponseEntity response = delete(REST_SESSION, Object.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            log.debug("Successfully terminated session with the VCenter: {}" + getBaseUrl());
        } else {
            log.error("Failed to terminate session with the VCenter: {}" + getBaseUrl());
        }
    }

}