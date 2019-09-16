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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
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

import com.vmware.mangle.cassandra.model.custom.faults.CustomFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.PluginFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.plugin.CustomFaultDescriptor;
import com.vmware.mangle.cassandra.model.plugin.CustomFaultDescriptorResponse;
import com.vmware.mangle.cassandra.model.plugin.CustomFaultExecutionRequest;
import com.vmware.mangle.cassandra.model.plugin.PluginDetails;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.model.plugin.ExtensionType;
import com.vmware.mangle.model.plugin.PluginAction;
import com.vmware.mangle.model.plugin.PluginInfo;
import com.vmware.mangle.services.FileStorageService;
import com.vmware.mangle.services.PluginDetailsService;
import com.vmware.mangle.services.PluginService;
import com.vmware.mangle.services.helpers.CustomFaultInjectionHelper;
import com.vmware.mangle.task.framework.helpers.faults.AbstractCustomFault;
import com.vmware.mangle.task.framework.skeletons.ITaskHelper;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * PluginController class.
 *
 * @author kumargautam
 */
@RestController
@RequestMapping(value = "/rest/api/v1/plugins")
@Validated
@Log4j2
public class PluginController {

    @Autowired
    private PluginService pluginService;
    @Autowired
    private FileStorageService storageService;
    @Autowired
    private CustomFaultInjectionHelper customFaultInjectionHelper;
    @Autowired
    private PluginDetailsService pluginDetailsService;
    @Autowired
    private SpringPluginManager pluginManager;

    @ApiOperation(value = "API to get plugin details", nickname = "getPlugins")
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getPlugins(
            @RequestParam(value = "pluginId", required = false) String pluginId,
            @RequestParam(value = "extensionType", required = false) ExtensionType extensionType) {
        log.info("PluginController getPlugins() Start.............");
        if (StringUtils.hasLength(pluginId)) {
            return new ResponseEntity<>(pluginService.getExtensions(pluginId, extensionType), HttpStatus.OK);
        }
        return new ResponseEntity<>(pluginService.getExtensions(), HttpStatus.OK);
    }

    @ApiOperation(value = "API to get extension", nickname = "getExtension")
    @GetMapping(value = "/{extensionName:.+}")
    public ResponseEntity<String> getExtensions(@PathVariable("extensionName") String extensionName) {
        log.info("PluginController getExtensions(" + extensionName + ") Start.............");
        ITaskHelper<?> ext = pluginService.getExtension(extensionName);
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
    public ResponseEntity<List<String>> deletePlugin(@RequestParam("pluginId") @NotBlank String pluginId) {
        log.info("PluginController deletePlugin() Start..................");
        pluginService.deletePlugin(pluginId);
        pluginDetailsService.deletePluginDetailsForDeletePlugin(pluginId);
        pluginDetailsService.triggerMultiNodeResync(pluginId);
        return new ResponseEntity<>(pluginService.getExtensionNames(), HttpStatus.OK);
    }

    @ApiOperation(value = "API to get plugin file", nickname = "getAllTheAvailablePluginFiles")
    @GetMapping(value = "/files", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getFiles() throws MangleException {
        log.info("Start execution of getPluginFiles()...");
        return new ResponseEntity<>(storageService.getFiles(), HttpStatus.OK);
    }

    @ApiOperation(value = "API to upload plugin file", nickname = "uploadFile")
    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file)
            throws MangleException {
        log.info("Start execution of uploadFile()...");
        storageService.storeFile(file);
        Map<String, Object> response = new HashMap<>();
        response.put("fileName", file.getOriginalFilename());
        response.put("message", "Your file successfully uploaded!");
        return new ResponseEntity<>(response, HttpStatus.OK);
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
    public ResponseEntity<Map<String, Object>> deleteFile(@RequestParam @NotBlank String fileName) {
        log.info("Start execution of deleteFile()...");
        storageService.deleteFile(fileName);
        Map<String, Object> response = new HashMap<>();
        response.put("fileName", fileName);
        response.put("message", "Your file successfully deleted!");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ApiOperation(value = "API to inject Custom fault on Endpoint", nickname = "injectCustomFault")
    @PostMapping(value = "/custom-fault", produces = "application/json")
    public ResponseEntity<Task<TaskSpec>> injectCustomFault(
            @Validated @RequestBody CustomFaultExecutionRequest customFaultExecutionRequest) throws MangleException {
        CustomFaultSpec customFaultSpec = buildCustomFaultSpec(customFaultExecutionRequest);
        AbstractCustomFault abstractCustomFault = customFaultInjectionHelper.getCustomFault(customFaultSpec);
        PluginFaultSpec faultSpec = customFaultInjectionHelper.getFaultSpec(customFaultSpec, abstractCustomFault);
        customFaultInjectionHelper.validateFields(faultSpec, abstractCustomFault);
        return new ResponseEntity<>(customFaultInjectionHelper.invokeFault(faultSpec,
                (String) customFaultSpec.getExtensionDetails().getTaskExtensionName()), HttpStatus.OK);
    }

    private CustomFaultSpec buildCustomFaultSpec(CustomFaultExecutionRequest customFaultExecutionRequest)
            throws MangleException {
        CustomFaultSpec customFaultSpec = new CustomFaultSpec();
        customFaultSpec.setPluginId(customFaultExecutionRequest.getPluginId());
        customFaultSpec.setFaultName(customFaultExecutionRequest.getFaultName());
        customFaultSpec.setFaultParameters(customFaultExecutionRequest.getFaultParameters());
        customFaultSpec.setEndpointName(customFaultExecutionRequest.getEndpointName());
        customFaultSpec.setExtensionDetails(
                pluginDetailsService.getFaultExtensionDetailsFromPluginDescriptor(customFaultSpec));
        customFaultSpec.setSchedule(customFaultExecutionRequest.getSchedule());
        customFaultSpec.setTags(customFaultExecutionRequest.getTags());
        customFaultSpec.setDockerArguments(customFaultExecutionRequest.getDockerArguments());
        customFaultSpec.setK8sArguments(customFaultExecutionRequest.getK8sArguments());
        return customFaultSpec;

    }

    @ApiOperation(value = "API to get Custom Fault sample request json", nickname = "getRequestJson")
    @GetMapping(value = "/request-json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomFaultDescriptorResponse> getRequestJson(
            @RequestParam("pluginId") @NotBlank String pluginId, @RequestParam("faultName") @NotBlank String faultName)
            throws MangleException {
        CustomFaultDescriptor customFaultDescriptor =
                pluginService.getFaultDetailsFromPluginDescriptor(pluginId, faultName, pluginDetailsService);
        return new ResponseEntity<>(buildCustomFaultDescriptorResponse(customFaultDescriptor), HttpStatus.OK);
    }

    private CustomFaultDescriptorResponse buildCustomFaultDescriptorResponse(
            CustomFaultDescriptor customFaultDescriptor) {
        CustomFaultDescriptorResponse customFaultDescriptorResponse = new CustomFaultDescriptorResponse();
        customFaultDescriptorResponse.setFaultName(customFaultDescriptor.getFaultName());
        customFaultDescriptorResponse.setFaultParameters(customFaultDescriptor.getFaultParameters());
        customFaultDescriptorResponse.setPluginId(customFaultDescriptor.getPluginId());
        customFaultDescriptorResponse.setSupportedEndpoints(customFaultDescriptor.getSupportedEndpoints());
        return customFaultDescriptorResponse;
    }

    @ApiOperation(value = "API to get plugin details", nickname = "getPluginDetails")
    @GetMapping(value = "/plugin-details", produces = "application/json")
    public ResponseEntity<List<PluginDetails>> getPluginDetails(
            @RequestParam(value = "pluginId", required = false) String pluginId) {
        List<PluginDetails> detailsList = new ArrayList<>();
        if (StringUtils.hasLength(pluginId)) {
            PluginDetails pluginDetails = pluginDetailsService.findPluginDetailsByPluginId(pluginId);
            if (pluginDetails != null) {
                detailsList.add(pluginDetails);
            }
        } else {
            detailsList = pluginDetailsService.findAllPluginDetails();
        }
        return new ResponseEntity<>(detailsList, HttpStatus.OK);
    }

    /**
     * @param pluginInfo
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
            pluginId = pluginService.preValidatePlugin(pluginName, pluginPath);
            if (pluginId != null) {
                PluginWrapper retrievedPlugin = pluginManager.getPlugin(pluginId);
                PluginDetails retrievedPluginDetails = pluginDetailsService.findPluginDetailsByPluginId(pluginId);
                if (retrievedPlugin != null && retrievedPluginDetails != null
                        && !retrievedPluginDetails.getIsActive()) {
                    retrievedPluginDetails.setIsLoaded(true);
                    retrievedPluginDetails.setIsActive(true);
                    pluginDetailsService.createPluginDetailsForLoadPlugin(pluginId);
                    break;
                }
                pluginService.loadPlugin(pluginPath);
                pluginService.postValidatePlugin(pluginName, pluginPath);
                PluginDetails pluginDetails = pluginDetailsService
                        .createPluginDetailsForLoadPlugin(pluginService.getIdForPluginName(pluginName));
                pluginDetailsService.triggerMultiNodeResync(pluginDetails.getPluginId());
            }
            break;
        case UNLOAD:
            log.info("PLUGIN UNLOAD Start...");
            pluginId = pluginService.getIdForPluginName(pluginName);
            pluginService.unloadPlugin(pluginId);
            pluginDetailsService.updatePluginDetailsForUnLoadPlugin(pluginId);
            pluginDetailsService.triggerMultiNodeResync(pluginId);
            break;
        case ENABLE:
            log.info("PLUGIN ENABLE Start...");
            pluginId = pluginService.getIdForPluginName(pluginName);
            pluginService.enablePlugin(pluginId);
            pluginDetailsService.updatePluginDetailsForEnablePlugin(pluginId);
            pluginDetailsService.triggerMultiNodeResync(pluginId);
            break;
        case DISABLE:
            log.info("PLUGIN DISABLE Start...");
            pluginId = pluginService.getIdForPluginName(pluginName);
            pluginService.disablePlugin(pluginId);
            pluginDetailsService.updatePluginDetailsForDisablePlugin(pluginId);
            pluginDetailsService.triggerMultiNodeResync(pluginId);
            break;
        default:
            log.error("Plugin action not found, please pass the action in following : {}",
                    Arrays.toString(PluginAction.values()));
        }
    }
}
