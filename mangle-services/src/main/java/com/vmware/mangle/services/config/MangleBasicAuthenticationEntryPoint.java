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

package com.vmware.mangle.services.config;

import java.io.IOException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.vmware.mangle.model.response.ErrorDetails;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 *
 * MangleBasicAuthenticationEntryPoint for invalid auth
 *
 * @author ranjans
 */

@Log4j2
@Component
public class MangleBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authEx)
            throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.getWriter().write(RestTemplateWrapper.objectToJson(getMangleErrorDetails(request, authEx)));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setRealmName("MANGLE REALM");
        super.afterPropertiesSet();
    }

    private ErrorDetails getMangleErrorDetails(HttpServletRequest httpServletRequest, AuthenticationException authEx) {
        String errorDescription;
        if (authEx instanceof LockedException) {
            errorDescription = authEx.getMessage();
        } else {
            errorDescription = ErrorConstants.AUTHENTICATION_FAILED_ERROR_MSG;
        }
        log.error("Authentication failed with the exception: ", authEx);
        return new ErrorDetails(new Date(), ErrorCode.LOGIN_EXCEPTION.getCode(), errorDescription,
                httpServletRequest.getRequestURI());
    }

}
