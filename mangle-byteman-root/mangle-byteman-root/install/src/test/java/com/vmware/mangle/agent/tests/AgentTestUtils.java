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
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.jboss.byteman.agent.install.Install;
import org.jboss.byteman.agent.submit.Submit;
import org.testng.Assert;

import com.vmware.mangle.java.agent.utils.RuntimeUtils;

/**
 * @author hkilari
 *
 */
public class AgentTestUtils {
    private AgentTestUtils() {
        throw new java.lang.UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void forceExit(ByteArrayOutputStream baos, PrintStream standard) {
        baos.reset();
        Submit.main(new String[] { "-forceExit" });
        Assert.assertTrue(baos.toString().contains("TransformListener() :  closing port"), baos.toString());
        System.out.println("Now the output is redirected to System.out");
        System.setOut(System.out);
        System.out.println(baos.toString());
        try {
            baos.flush();
            baos.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void installAgent(ByteArrayOutputStream baos) {
        String userDir = new File(new File(".").getAbsolutePath()).getParentFile().getAbsolutePath();
        System.out.println(userDir);
        String bytemanHome = userDir.substring(0, userDir.lastIndexOf(File.separator) + 1) + "agent" + File.separator
                + "target" + File.separator + "mangle-byteman-3.0.0.jar";
        System.out.println(bytemanHome);
        System.setProperty("org.jboss.byteman.jar", userDir.substring(0, userDir.lastIndexOf(File.separator) + 1)
                + "agent" + File.separator + "target" + File.separator + "mangle-byteman-3.0.0.jar");
        System.out.println("Now the output is redirected to internal Stream");
        System.setProperty("org.jboss.test.execution", "true");
        PrintStream printStream = new PrintStream(baos);
        System.setOut(printStream);
        System.setErr(printStream);

        Install.main(new String[] { "-s", "-b", "-p", "9091", RuntimeUtils.getPid() });

        Assert.assertTrue((baos.toString().contains("Started Byteman Listener Successfully")), baos.toString());
    }

}
