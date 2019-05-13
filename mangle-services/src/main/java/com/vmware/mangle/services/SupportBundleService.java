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

package com.vmware.mangle.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;

import com.vmware.mangle.services.constants.CommonConstants;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author dbhat, kumargautam
 *
 */

@Service
@Log4j2
public class SupportBundleService {

    public static final String PATH_PROPERTY = "logging.path";

    @Autowired
    private Environment environment;

    /**
     * Method is used to download mangle server logs as zip file.
     *
     * @param response
     */
    public void getLogZipFile(HttpServletResponse response) {
        try (
            ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
            log.debug("getting the list of support files to be zipped");
            List<Resource> resources = getLogFileResource();
            log.debug("Packaging the files to Zip");
            for (Resource resource : resources) {
                zipOutputStream.putNextEntry(new ZipEntry(resource.getFilename()));
                StreamUtils.copy(resource.getInputStream(), zipOutputStream);
                zipOutputStream.closeEntry();
            }
        } catch (IOException e) {
            throw new MangleRuntimeException(e, ErrorCode.IO_EXCEPTION);
        }
    }

    /**
     * Method to get all the files from the LOG directory.
     *
     * @return : List of support files located under mangle-tomcat/log
     */
    public List<Resource> getLogFileResource() {
        List<Resource> resources = new ArrayList<>();
        String path = environment.getProperty(PATH_PROPERTY);
        Assert.hasText(path, CommonConstants.SUPPORT_BUNDLE_PATH_NOT_NULL);
        log.debug("Getting all the files from directory: " + path);
        if (!path.endsWith("/")) {
            path = path + File.separatorChar;
        }
        for (String fileName : getFileNames(path)) {
            resources.add(new FileSystemResource(path + fileName));
        }
        return resources;
    }

    /**
     * Method is used to get file names of given directory.
     *
     * @param path:
     *            Files to be considered from the specified path
     *
     * @return
     */
    private List<String> getFileNames(@NonNull String path) {
        List<String> allFiles = new ArrayList<>();
        File[] files = new File(path).listFiles();
        for (File file : files) {
            if (file.isFile()) {
                allFiles.add(file.getName());
            }
        }
        return allFiles;
    }
}
