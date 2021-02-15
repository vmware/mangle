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

package com.vmware.mangle.services.config.aop;

import java.util.Arrays;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.services.helpers.FaultInjectionHelper;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * Aspect bean for FaultInjectionController.
 *
 * @author kumargautam
 */
@Aspect
@Log4j2
@Component
public class FaultInjectionControllerAspect {

    @Autowired
    private FaultInjectionHelper faultInjectionHelper;

    @Before(value = "execution(public * com.vmware.mangle.services.controller.FaultInjectionController.*(..))")
    public void beforeFaultInjection(JoinPoint joinPoint) throws MangleException {
        Object[] inputs = joinPoint.getArgs();
        log.debug("Before calling fault injection, validating and updating fault spec : {}", Arrays.toString(inputs));
        if (inputs.length >= 1 && inputs[0] instanceof CommandExecutionFaultSpec) {
            CommandExecutionFaultSpec faultSpec = (CommandExecutionFaultSpec) inputs[0];
            faultInjectionHelper.validateSpec(faultSpec);
            faultInjectionHelper.validateEndpointTypeSpecificArguments(faultSpec);
        }
    }
}
