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
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.vmware.mangle.services.plugin.config.PluginProperties;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * This class is used to perform operation against file.
 *
 * @author kumargautam
 */
@Service
public class FileStorageService {

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

    public String storeFile(MultipartFile file) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw new MangleRuntimeException(ErrorCode.INVALID_CHAR_IN_FILE_NAME, fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
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
    public String getFileName(String fileName) {
        File[] files = new File(this.fileStorageLocation.toString())
                .listFiles((dir, name) -> (name.toLowerCase().startsWith(fileName.toLowerCase())
                        && (name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".jar"))));

        for (File file : files) {
            if (file.getName().contains(fileName)) {
                return file.getName();
            }
        }
        throw new MangleRuntimeException(ErrorCode.FILE_NAME_NOT_EXIST, fileName);
    }
}
