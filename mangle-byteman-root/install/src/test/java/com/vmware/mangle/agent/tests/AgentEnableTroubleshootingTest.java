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

/**
 * @author hkilari
 *
 */
public class AgentEnableTroubleshootingTest {
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
    public void bytemanRuleInstallTest() {
        baos.reset();
        Submit.main(new String[] { "-enableTroubleshooting" });
        Assert.assertTrue(baos.toString().contains("install rule Trace - Capture Troubleshooting bundle."),
                baos.toString());
        try {
            baos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
