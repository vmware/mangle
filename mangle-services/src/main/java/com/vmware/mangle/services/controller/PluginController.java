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

package com.vmware.mangle.services.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vmware.mangle.model.plugin.PluginAction;
import com.vmware.mangle.model.plugin.PluginInfo;
import com.vmware.mangle.services.FileStorageService;
import com.vmware.mangle.services.PluginService;
import com.vmware.mangle.task.framework.skeletons.ITaskHelper;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * PluginController class.
 *
 * @author kumargautam
 */
@RestController
@RequestMapping(value = "/rest/api/v1/plugins")
@Log4j2
public class PluginController {

    @Autowired
    private PluginService pluginService;
    @Autowired
    private FileStorageService storageService;

    @ApiOperation(value = "API to get plugin details", nickname = "getPlugins")
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getPlugins() {
        log.info("PluginController getPlugins() Start.............");
        return new ResponseEntity<>(pluginService.getExtensions(), HttpStatus.OK);
    }

    @ApiOperation(value = "API to get extension", nickname = "getExtension")
    @GetMapping(value = "/{extensionName:.+}")
    public ResponseEntity<String> getExtensions(@PathVariable("extensionName") String extensionName) {
        log.info("PluginController getExtensions(" + extensionName + ") Start.............");
        ITaskHelper ext = pluginService.getExtension(extensionName);
        if (ext != null) {
            return new ResponseEntity<>("Extension Found with msg " + ext.getClass().getName(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Extension Not Found with Name: " + extensionName, HttpStatus.OK);
        }
    }

    @ApiOperation(value = "API to perform plugin actions", nickname = "performPluginActions")
    @PutMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> performPluginActions(@Validated @RequestBody PluginInfo pluginInfo) {
        log.info("PluginController performPluginActions() Start............");
        performPluginAction(pluginInfo);
        return new ResponseEntity<>(pluginService.getExtensionNames(), HttpStatus.OK);
    }

    @ApiOperation(value = "API to delete plugin", nickname = "deletePlugin")
    @DeleteMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> deletePlugin(@RequestParam("pluginId") String pluginId) {
        log.info("PluginController deletePlugin() Start..................");
        pluginService.deletePlugin(pluginId);
        return new ResponseEntity<>(pluginService.getExtensionNames(), HttpStatus.OK);
    }

    @ApiOperation(value = "API to upload plugin file", nickname = "uploadFile")
    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("Start execution of uploadFile()...");
        storageService.storeFile(file);
        return new ResponseEntity<>("You successfully uploaded " + file.getOriginalFilename() + "!", HttpStatus.OK);
    }

    @ApiOperation(value = "API to download plugin file", nickname = "downloadFile")
    @GetMapping("/files/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        log.info("Start execution of downloadFile()...");
        // Load file as Resource
        Resource resource = storageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            throw new MangleRuntimeException(ex, ErrorCode.GENERIC_ERROR, (Object) null);
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @ApiOperation(value = "API to delete plugin file", nickname = "deleteFile")
    @DeleteMapping(value = "/files")
    public ResponseEntity<String> deleteFile(@RequestParam String fileName) {
        log.info("Start execution of deleteFile()...");
        storageService.deleteFile(fileName);
        return ResponseEntity.ok().body("You successfully deleted " + fileName + "!");
    }

    /**
     * @param pluginPath
     * @return
     */
    private void performPluginAction(PluginInfo pluginInfo) {
        String pluginId;
        String pluginName = pluginInfo.getPluginName();
        switch (pluginInfo.getPluginAction()) {
        case LOAD:
            log.info("PLUGIN LOAD Start...");
            pluginName = storageService.getFileName(pluginName);
            Path pluginPath = storageService.getFileStorageLocation().resolve(pluginName);
            pluginService.loadPlugin(pluginPath);
            break;
        case UNLOAD:
            log.info("PLUGIN UNLOAD Start...");
            pluginId = pluginService.getIdForPluginName(pluginName);
            pluginService.unloadPlugin(pluginId);
            break;
        case ENABLE:
            log.info("PLUGIN ENABLE Start...");
            pluginId = pluginService.getIdForPluginName(pluginName);
            pluginService.enablePlugin(pluginId);
            break;
        case DISABLE:
            log.info("PLUGIN DISABLE Start...");
            pluginId = pluginService.getIdForPluginName(pluginName);
            pluginService.disablePlugin(pluginId);
            break;
        default:
            log.error("Plugin action not found, please pass the action in following : {}",
                    Arrays.toString(PluginAction.values()));
        }
    }
}
