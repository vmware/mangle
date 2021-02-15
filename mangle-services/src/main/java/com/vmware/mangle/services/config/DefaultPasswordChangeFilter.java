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
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import com.vmware.mangle.model.response.ErrorDetails;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.constants.URLConstants;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author chetanc
 */
public class DefaultPasswordChangeFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        if (!Constants.isDefaultPasswordResetStatus()
                && !request.getRequestURI().endsWith(URLConstants.DEFAULT_USER_UPDATE_FLAG_URL)
                && !request.getRequestURI().endsWith(URLConstants.APPLICATION_SERVLET_CONTEXT_PATH)
                && !request.getRequestURI().endsWith(URLConstants.APPLICATION_HEALTH_PATH)
                && !request.getRequestURI().endsWith(".js")
                && !request.getRequestURI().contains("swagger")) {
            ErrorDetails errorDetails = new ErrorDetails(new Date(), ErrorCode.DEFAULT_PASSWORD_NOT_RESET.getCode(),
                    ErrorConstants.DEFAULT_USER_CRED_NOT_RESET, "");
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.getWriter().println(RestTemplateWrapper.objectToJson(errorDetails));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().flush();
            response.getWriter().close();
        } else {
            chain.doFilter(req, res);
        }
    }
}
