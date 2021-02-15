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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.vmware.mangle.services.constants.CommonConstants;
import com.vmware.mangle.services.plugin.config.PluginProperties;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * This class is used to perform operation against file.
 *
 * @author kumargautam
 */
@Service
public class FileStorageService {

    private static final CharSequence PARENT_FILE_REFERENCE = "..";
    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(PluginProperties pluginProperties) {
        this.fileStorageLocation = Paths.get(pluginProperties.getUploadDir()).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new MangleRuntimeException(ex, ErrorCode.GENERIC_ERROR, (Object) null);
        }
    }

    public String storeFile(MultipartFile file) throws MangleException {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains(PARENT_FILE_REFERENCE)) {
                throw new MangleRuntimeException(ErrorCode.INVALID_CHAR_IN_FILE_NAME, fileName);
            }
            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            if (targetLocation.toFile().exists()) {
                throw new MangleException(ErrorCode.PRE_EXISTING_PLUGIN_FILE, ErrorConstants.PRE_EXISTING_PLUGIN_FILE);
            }
            List<String> filesList = getZipEntriesList(file.getInputStream());
            for (String childFileName : filesList) {
                if (childFileName.contains(PARENT_FILE_REFERENCE)) {
                    throw new MangleRuntimeException(ErrorCode.INVALID_CHAR_IN_FILE_NAME, childFileName);
                }
            }
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException ex) {
            throw new MangleRuntimeException(ex, ErrorCode.GENERIC_ERROR, (Object) null);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new MangleRuntimeException(ErrorCode.FILE_NAME_NOT_EXIST, fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MangleRuntimeException(ex, ErrorCode.GENERIC_ERROR, (Object) null);
        }
    }

    public String deleteFile(String fileName) {
        Path targetLocation = this.fileStorageLocation.resolve(fileName);
        try {
            Files.delete(targetLocation);

            return fileName;
        } catch (IOException ex) {
            throw new MangleRuntimeException(ex, ErrorCode.GENERIC_ERROR, (Object) null);

        }
    }

    public void createDefaultFile() {
        Path targetLocationForDisabled = this.fileStorageLocation.resolve("disabled.txt");
        Path targetLocationForEnabled = this.fileStorageLocation.resolve("enabled.txt");
        try {
            if (!targetLocationForDisabled.toFile().exists()) {
                Files.createFile(targetLocationForDisabled);
            }
            if (!targetLocationForEnabled.toFile().exists()) {
                Files.createFile(targetLocationForEnabled);
            }
        } catch (IOException ex) {
            throw new MangleRuntimeException(ex, ErrorCode.GENERIC_ERROR, (Object) null);
        }
    }

    public Path getFileStorageLocation() {
        return fileStorageLocation;
    }

    /**
     * @param fileName
     * @return
     */
    public List<String> getFiles() {
        File[] files = new File(this.fileStorageLocation.toString())
                .listFiles((dir, name) -> (name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".jar")));
        return Arrays.asList(files).stream().map(File::getName)
                .filter(fileName -> (!fileName.contains(CommonConstants.DEFAULT_PLUGIN_ID)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * @param fileName
     * @return
     */
    public String getFileName(String fileName) {
        for (String file : getFiles()) {
            if (file.contains(fileName)) {
                return file;
            }
        }
        throw new MangleRuntimeException(ErrorCode.FILE_NAME_NOT_EXIST, fileName);
    }

    /**
     * Utility method to return List with names of Zip File Entries
     *
     * @param fis
     * @return
     */
    private List<String> getZipEntriesList(InputStream fis) {
        ZipEntry zipEntry = null;
        List<String> fileEntries = new ArrayList<>();
        try (ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(fis))) {
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                fileEntries.add(zipEntry.getName());
            }
        } catch (FileNotFoundException e) {
            throw new MangleRuntimeException(ErrorCode.FILE_NAME_NOT_EXIST, e);
        } catch (IOException e) {
            throw new MangleRuntimeException(e, ErrorCode.GENERIC_ERROR, (Object) null);
        }
        return fileEntries;
    }
}
