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

package com.vmware.mangle.services.mockdata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j2;

import com.vmware.mangle.cassandra.model.custom.faults.CustomFaultSpec;
import com.vmware.mangle.cassandra.model.plugin.CustomFaultDescriptor;
import com.vmware.mangle.cassandra.model.plugin.CustomFaultExecutionRequest;
import com.vmware.mangle.cassandra.model.plugin.ExtensionDetails;
import com.vmware.mangle.cassandra.model.plugin.PluginDetails;
import com.vmware.mangle.model.enums.EndpointType;
import com.vmware.mangle.services.constants.CommonConstants;

/**
 * Mock date for CustomFault.
 *
 * @author kumargautam
 */
@Log4j2
public class CustomFaultMockData {

    public CustomFaultSpec getCustomFaultSpec() {
        CustomFaultSpec faultSpec = new CustomFaultSpec();
        faultSpec.setPluginId("mangle-test-plugin");
        faultSpec.setFaultName("mangle-test-plugin-customkillprocessfault");
        Map<String, String> faultParameters = new HashMap<>();
        faultSpec.setEndpointName("endpointTestLinux");
        faultParameters.put("injectionHomeDir", "/tmp/");
        faultParameters.put("processIdentifier", "sleep");
        faultParameters.put("remediationCommand", "sleep 2m");
        ExtensionDetails extensionDetail = new ExtensionDetails();
        faultSpec.setExtensionDetails(extensionDetail);
        faultSpec.setFaultParameters(faultParameters);
        extensionDetail
                .setModelExtensionName("com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec");
        extensionDetail.setFaultExtensionName("com.vmware.mangle.task.framework.helpers.faults.AbstractCustomFault");
        extensionDetail.setTaskExtensionName("com.vmware.mangle.task.framework.skeletons.ITaskHelper");
        return faultSpec;
    }

    public CustomFaultExecutionRequest getCustomFaultExecutionRequest() {
        CustomFaultSpec faultSpec = getCustomFaultSpec();
        CustomFaultExecutionRequest customFaultExecutionRequest = new CustomFaultExecutionRequest();
        customFaultExecutionRequest.setEndpointName(null);
        customFaultExecutionRequest.setFaultName(faultSpec.getFaultName());
        customFaultExecutionRequest.setFaultParameters(faultSpec.getFaultParameters());
        customFaultExecutionRequest.setPluginId(faultSpec.getPluginId());
        return customFaultExecutionRequest;
    }

    public CustomFaultDescriptor getCustomFaultDescriptor() {
        CustomFaultSpec faultSpec = getCustomFaultSpec();
        CustomFaultDescriptor customFaultDescriptor = new CustomFaultDescriptor();
        customFaultDescriptor.setExtensionDetails(faultSpec.getExtensionDetails());
        customFaultDescriptor.setFaultName(faultSpec.getFaultName());
        customFaultDescriptor.setFaultParameters(faultSpec.getFaultParameters());
        customFaultDescriptor.setPluginId(faultSpec.getPluginId());
        List<EndpointType> supportedEndpoints = new ArrayList<>();
        supportedEndpoints.add(EndpointType.MACHINE);
        customFaultDescriptor.setSupportedEndpoints(supportedEndpoints);
        return customFaultDescriptor;
    }

    public PluginDetails getPluginDetails() {
        PluginDetails details = new PluginDetails();
        CustomFaultSpec faultSpec = getCustomFaultSpec();
        details.setPluginId(faultSpec.getPluginId());
        details.setPluginName("mangle-test-plugin-2.0.0");
        details.setIsActive(true);
        details.setIsLoaded(true);
        details.setPluginVersion("2.0.0");
        details.setPluginPath(new StringBuilder("src").append(File.separator).append("test").append(File.separator)
                .append("resources").append(File.separator).append(CommonConstants.PLUGIN_DESCRIPTOR_FILE_NAME)
                .toString());
        Map<String, CustomFaultDescriptor> customFaultDescriptorMap = new HashMap<>();
        customFaultDescriptorMap.put(faultSpec.getFaultName(), getCustomFaultDescriptor());
        details.setCustomFaultDescriptorMap(customFaultDescriptorMap);
        File file = new File(details.getPluginPath());
        try {
            details.setPluginFile(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            log.info(e);
        }
        return details;
    }
}