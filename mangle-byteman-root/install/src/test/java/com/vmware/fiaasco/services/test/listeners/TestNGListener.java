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

package com.vmware.fiaasco.services.test.listeners;

import java.util.logging.Logger;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import com.vmware.mangle.java.agent.faults.helpers.FaultsHelper;

/**
 * @author hkilari
 *
 */
public class TestNGListener extends TestListenerAdapter {
    private static final Logger LOG = Logger.getLogger(FaultsHelper.class.getName());
    public static final String INFORMATION_DEMARKER =
            "-------------------------------------------------------------------";

    @Override
    public void onTestStart(ITestResult tr) {
        LOG.info(INFORMATION_DEMARKER);
        LOG.info("Test " + tr.getName() + " Started....");
        LOG.info(INFORMATION_DEMARKER);
    }

    @Override
    public void onTestSuccess(ITestResult tr) {
        LOG.info(INFORMATION_DEMARKER);
        LOG.info("Test '" + tr.getName() + "' PASSED");

        // This will print the class name in which the method is present
        LOG.info(tr.getTestClass().getName());

        // This will print the priority of the method.
        // If the priority is not defined it will print the default priority as
        // 'o'
        LOG.info("Priority of this method is " + tr.getMethod().getPriority());

        LOG.info(INFORMATION_DEMARKER);
    }

    @Override
    public void onTestFailure(ITestResult tr) {
        LOG.info(INFORMATION_DEMARKER);
        LOG.info("Test '" + tr.getName() + "' FAILED");
        LOG.info("Priority of this method is " + tr.getMethod().getPriority());
        LOG.info(INFORMATION_DEMARKER);
    }

    @Override
    public void onTestSkipped(ITestResult tr) {
        LOG.info(INFORMATION_DEMARKER);
        LOG.info("Test '" + tr.getName() + "' SKIPPED");
        LOG.info(INFORMATION_DEMARKER);
    }
}