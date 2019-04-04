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

package com.vmware.mangle.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author bkaranam
 *
 *
 */
public class StreamGobbler extends Thread {
    InputStream is;
    private String output;

    public StreamGobbler(InputStream is) {
        this.is = is;
    }

    public String getOutput() {
        if (null == this.output) {
            return "";
        }
        return this.output;
    }

    private void setOutput(String output) {
        this.output = output;
    }

    @Override
    public void run() {
        StringBuffer outputBuffer = new StringBuffer();
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                outputBuffer.append(line + "\n");
            }
        } catch (IOException ioe) {
            outputBuffer.append(ioe.getMessage());
        }
        setOutput(outputBuffer.toString());
    }
}
