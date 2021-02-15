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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import com.vmware.mangle.model.response.ErrorDetails;
import com.vmware.mangle.services.enums.MangleNodeStatus;
import com.vmware.mangle.services.enums.MangleQuorumStatus;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.constants.HazelcastConstants;
import com.vmware.mangle.utils.constants.MetricProviderConstants;
import com.vmware.mangle.utils.constants.URLConstants;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Filter component to have global controll on apis
 *
 * @author bkaranam (bhanukiran karanam)
 */

@Component
public class CORSFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "x-requested-with, Content-Type");
        response.setHeader("Cache-control", "no-store");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", getHttpDateTime());

        if (verifyQuorumPreCheckFailure(request)) {
            ErrorDetails errorDetails = new ErrorDetails(new Date(), ErrorCode.CLUSTER_QUORUM_NOT_MET.getCode(),
                    String.format(ErrorConstants.CLUSTER_QUORUM_NOT_MET,
                            System.getProperty(MetricProviderConstants.NODE_ADDRESS)),
                    "");
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.getWriter().write(RestTemplateWrapper.objectToJson(errorDetails));
        } else if (URLConstants.getMangleNodeCurrentStatus().equals(MangleNodeStatus.ACTIVE)
                || request.getRequestURI().contains("node-status") || request.getRequestURI().contains("swagger")
                || request.getRequestURI().contains("v1/tasks")) {
            chain.doFilter(req, res);
        } else {
            response.setContentType(MediaType.TEXT_HTML_VALUE);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);

            StringBuilder maintenanceMessage = new StringBuilder(
                    "<style>body { text-align: center; padding: 200px; font: 20px Helvetica, sans-serif; color: #333;}</style><section style=\"background:#D9E4EA;\"><br/>");
            if (URLConstants.getMangleNodeCurrentStatus().equals(MangleNodeStatus.PAUSE)) {
                maintenanceMessage.append("<h3>Mangle Is Paused</h3>").append(
                        "<p>Sorry for the inconvenience, Please contact admin, otherwise mangle will be back online shortly!</p><p>&mdash; The Mangle Admin</p><br></section>");
            }
            if (URLConstants.getMangleNodeCurrentStatus().equals(MangleNodeStatus.MAINTENANCE_MODE)) {
                maintenanceMessage.append("<h3>Mangle Is Under Maintenance</h3>").append(
                        "<p>Sorry for the inconvenience, performing some maintenance at the moment.</p><p>Please contact admin, otherwise mangle will be back online shortly!</p><p>&mdash; The Mangle Admin</p><br></section>");
            }
            response.getWriter().write(maintenanceMessage.toString());
            response.getWriter().flush();
            response.getWriter().close();
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
        //Not customized init Method
    }

    @Override
    public void destroy() {
        //Not customized destroy Method
    }

    public boolean verifyQuorumPreCheckFailure(HttpServletRequest request) {
        return (HazelcastConstants.getMangleQourumStatus() == MangleQuorumStatus.NOT_PRESENT
                && !request.getMethod().equals(RequestMethod.GET.name())
                && !(request.getRequestURI().contains(URLConstants.CLUSTER_CONFIG_URL)
                        || request.getRequestURI().contains(URLConstants.DEFAULT_USER_UPDATE_FLAG_URL)
                        || request.getRequestURI().contains(URLConstants.UPDATE_CURRENT_USER_PASSWORD)
                        || request.getRequestURI().contains(URLConstants.MANGLE_USER_LOGIN)));
    }

    private String getHttpDateTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return simpleDateFormat.format(new Date());
    }
}
