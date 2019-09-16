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

package com.vmware.mangle.unittest.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import lombok.extern.log4j.Log4j2;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.vmware.mangle.utils.StreamGobbler;

/**
 *
 *
 * @author chetanc
 */
@Log4j2
public class StreamGobblerTest {
    private static final String filename2 = "src/main/resources/FaultOperationProperties/VCenterFaultOperations.properties";

    /**
     * Test for the method {@link StreamGobbler}
     *
     * @throws FileNotFoundException
     */
    @Test
    public void testStreamGobbler() throws FileNotFoundException {
        log.info("Executing StreamGobblerTest for the class StreamGobbler");
        File f = new File(filename2);
        InputStream inputStream = new FileInputStream(f);
        StreamGobbler streamGobbler = new StreamGobbler(inputStream);
        streamGobbler.start();
        Assert.assertEquals(streamGobbler.getOutput(), "");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info(streamGobbler.getOutput());
    }
}
