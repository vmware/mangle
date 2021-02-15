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

package com.vmware.mangle.resiliency;

import com.vmware.mangle.metrics.models.ResiliencyCalculatorOperation;
import com.vmware.mangle.resiliency.services.RScoreHelper;
import com.vmware.mangle.resiliency.services.ResiliencyEmailHelper;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;

import com.vmware.mangle.exception.MangleException;
import com.vmware.mangle.resiliency.commons.ResiliencyConstants;
import com.vmware.mangle.resiliency.services.ResiliencyCalculatorHelper;

/**
 * @author chetanc, dbhat
 *
 */
@Log4j2
public class Application {

    public static void main(String[] args) throws MangleException {
        log.info("initializing resiliency score calculator");
        String operation = System.getProperty(ResiliencyConstants.OPERATION);
        if (StringUtils.hasText(operation)) {
            ResiliencyCalculatorOperation operationSpecified = ResiliencyCalculatorOperation.valueOf(operation);
            switch (operationSpecified) {
            case RESILIENCY_CALCULATION:
                ResiliencyCalculatorHelper resiliencyCalculatorHelper = new ResiliencyCalculatorHelper();
                resiliencyCalculatorHelper.calculateResiliencyScore();
                break;
            case RESILIENCY_CALCULATION_USING_QUERIES:
                RScoreHelper rScoreHelper = new RScoreHelper();
                rScoreHelper.calculateRScore();
                break;
            case EMAIL:
                ResiliencyEmailHelper resiliencyEmailHelper = new ResiliencyEmailHelper();
                resiliencyEmailHelper.sendResiliencyScoreEmail();
                break;
            default:
                log.error("Operation specified is NOT supported by the resiliency score calculator.");
                break;
            }
        } else {
            log.error(
                    "No operations are specified. Please provide: " + ResiliencyConstants.OPERATION + " as parameter");
        }
        System.exit(0);
    }
}
