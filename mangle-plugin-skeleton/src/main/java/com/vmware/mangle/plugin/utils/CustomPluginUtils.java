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

package com.vmware.mangle.plugin.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;

import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.utils.ConstantsUtils;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;


/**
 * @author jayasankarr
 */
public class CustomPluginUtils {


    /**
     * Utility method to copy Script File to Mangle Support Scripts directory from jar
     *
     * @param faultInjectionScriptInfo
     * @throws MangleException
     */
    public void copyScriptFileToMangleDirectory(SupportScriptInfo faultInjectionScriptInfo) throws MangleException {
        String filePath = ConstantsUtils.getMangleSupportScriptDirectory() + File.separator
                + faultInjectionScriptInfo.getScriptFileName();
        File destFile = new File(filePath);
        if (faultInjectionScriptInfo.isClassPathResource()) {
            copyFileFromJarToDestination("/" + faultInjectionScriptInfo.getScriptFileName(), filePath);
        } else {
            throw new MangleException(ErrorCode.SUPPORT_SCRIPT_FILE_NOT_FOUND, destFile.getName());
        }

    }

    /**
     * Utility method to copy Script File to Specific Destination Path
     *
     * @param faultInjectionScriptInfo
     * @throws MangleException
     */
    public void copyFileFromJarToDestination(String scriptPath, String destinationFilePath) throws MangleException {
        File destFile = new File(destinationFilePath);
        //Checking if the file is already available in destination folder
        if (!destFile.exists()) {
            try {
                InputStream in;
                in = CustomPluginUtils.class.getResourceAsStream(scriptPath);
                FileUtils.copyToFile(in, destFile);
            } catch (IOException e) {
                throw new MangleException(ErrorCode.FILE_NAME_NOT_EXIST, scriptPath);
            }
        }

    }

}
