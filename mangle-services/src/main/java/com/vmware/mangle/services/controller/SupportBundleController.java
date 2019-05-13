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

package com.vmware.mangle.services.controller;

import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vmware.mangle.services.SupportBundleService;
import com.vmware.mangle.services.constants.CommonConstants;
import com.vmware.mangle.utils.constants.Constants;

/**
 * Support Bundle Controller.
 *
 * @author dbhat, kumargautam
 */

@RestController
@RequestMapping("/application")
@Api("/application")
@Log4j2
public class SupportBundleController {

    @Autowired
    private SupportBundleService supportBundleService;

    @ApiOperation(value = "API to get support bundle for Mangle ", nickname = "getSupportBundle")
    @GetMapping(value = "/zip", produces = "application/zip")
    public void getLogBundle(HttpServletResponse response) {
        log.debug("Received request to create Support Bundle zip file");
        response.setStatus(HttpServletResponse.SC_OK);
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_HIPHEN_SEPERATED);
        String fileName = CommonConstants.MANGLE_SUPPORT_BUNDLE_FILE_NAME
                + dateFormat.format(System.currentTimeMillis()) + ".zip";
        response.addHeader("Content-Disposition", "attachment; filename=" + fileName);
        supportBundleService.getLogZipFile(response);
    }
}