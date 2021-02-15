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

package com.vmware.mangle.agent.tests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.jboss.byteman.agent.submit.Submit;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.vmware.mangle.java.agent.utils.ThreadUtils;

/**
 * @author hkilari
 *
 */
public class AgentRemediateFaultTest {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream standard = System.out;
    String userDir = null;

    @BeforeClass
    public void installAgent() {
        AgentTestUtils.installAgent(baos);
    }

    @AfterClass(alwaysRun = true)
    public void clearAgent() {
        AgentTestUtils.forceExit(baos, standard);
    }

    @Test
    public void bytemanFaultRemediationTest() {
        Submit.main(new String[] { "-if", "__timeoutVariance", "2000", "__gap", "20000", "__gapVariance", "5000",
                "__loadVariance", "30", "__longLasting", "true", "__faultName", "cpuFault", "__timeOutInMilliSeconds",
                "10000", "__load", "30" });

        Assert.assertTrue(baos.toString().contains("Created Fault Successfully"), baos.toString());
        String faultId =
                RegularExpressionUtils.extractField(baos.toString(), "[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}");
        Assert.assertTrue(faultId != null && !faultId.trim().equals(""));
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Submit.main(new String[] { "-llf" });
        Assert.assertTrue(baos.toString().contains("{\"" + faultId + "\":\"cpuFault\"}"), baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ThreadUtils.delayInSeconds(2);
        Submit.main(new String[] { "-rf", faultId });
        Assert.assertTrue(baos.toString().contains("Received Remediation Request Successfully"), baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Submit.main(new String[] { "-rf", faultId });
        Assert.assertTrue(baos.toString().contains("Requested Fault is already Remediated."), baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Submit.main(new String[] { "-rf", "bcdadaa0-33ea-4ef5-b19e-d25a66a6bd70" });
        Assert.assertTrue(
                baos.toString().contains("No fault found with provided ID: bcdadaa0-33ea-4ef5-b19e-d25a66a6bd70"),
                baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Submit.main(new String[] { "-rf", "" });
        Assert.assertTrue(baos.toString().contains("No fault found with provided ID:"), baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Submit.main(new String[] { "-rf", " " });
        Assert.assertTrue(baos.toString().contains("No fault found with provided ID:"), baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
