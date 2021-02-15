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

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.services.dto.OperationMetaData;

/**
 * @author bkaranam
 *
 *
 */
@Log4j2
public class ReadFaultOperationProperties {
    public static final String PROPERTYFILE_FOLDER = "FaultOperationProperties";
    public static final String VCENTER_FAULT_OPERATION_PROPERTIES_FILE = "VCenterFaultOperations.properties";
    public static final String DOCKER_FAULT_OPERATION_PROPERTIES_FILE = "dockerFaultOperations.properties";
    public static final String AWS_EC2_FAULT_OPERATION_PROPERTIES_FILE = "awsEC2FaultOperations.properties";
    public static final String AWS_RDS_FAULT_OPERATION_PROPERTIES_FILE = "awsRDSFaultOperations.properties";
    public static final String AZURE_FAULT_OPERATION_PROPERTIES_FILE = "azureVMFaultOperations.properties";
    private static Map<String, OperationMetaData> vCenterFaultOperationMap = new HashMap<>();
    private static Map<String, OperationMetaData> dockerFaultOperationMap = new HashMap<>();
    private static Map<String, OperationMetaData> awsEC2FaultOperationMap = new HashMap<>();
    private static Map<String, OperationMetaData> awsRDSFaultOperationMap = new HashMap<>();
    private static Map<String, OperationMetaData> azureEC2FaultOperationMap = new HashMap<>();

    private ReadFaultOperationProperties() {
    }

    public static synchronized Map<String, OperationMetaData> getAwsRDSFaultOperationMap() {
        if (CollectionUtils.isEmpty(awsRDSFaultOperationMap)) {
            Properties awsRDSFaultproperties = ReadProperty
                    .readProperty(PROPERTYFILE_FOLDER + File.separator + AWS_RDS_FAULT_OPERATION_PROPERTIES_FILE);
            awsRDSFaultOperationMap = extractFaultOperationMap(awsRDSFaultproperties);
        }
        return awsRDSFaultOperationMap;
    }

    public static synchronized Map<String, OperationMetaData> getAwsEC2FaultOperationMap() {
        if (CollectionUtils.isEmpty(awsEC2FaultOperationMap)) {
            Properties awsEC2Faultproperties = ReadProperty
                    .readProperty(PROPERTYFILE_FOLDER + File.separator + AWS_EC2_FAULT_OPERATION_PROPERTIES_FILE);
            awsEC2FaultOperationMap = extractFaultOperationMap(awsEC2Faultproperties);
        }
        return awsEC2FaultOperationMap;
    }

    public static synchronized Map<String, OperationMetaData> getAzureFaultOperationMap() {
        if (CollectionUtils.isEmpty(azureEC2FaultOperationMap)) {
            Properties azureEC2Faultproperties = ReadProperty
                    .readProperty(PROPERTYFILE_FOLDER + File.separator + AZURE_FAULT_OPERATION_PROPERTIES_FILE);
            azureEC2FaultOperationMap = extractFaultOperationMap(azureEC2Faultproperties);
        }
        return azureEC2FaultOperationMap;
    }

    public static synchronized Map<String, OperationMetaData> getVcenterFaultOperationMap() {
        if (CollectionUtils.isEmpty(vCenterFaultOperationMap)) {
            Properties vCenterFaultproperties = ReadProperty
                    .readProperty(PROPERTYFILE_FOLDER + File.separator + VCENTER_FAULT_OPERATION_PROPERTIES_FILE);
            vCenterFaultOperationMap = extractFaultOperationMap(vCenterFaultproperties);
        }
        return vCenterFaultOperationMap;
    }

    public static synchronized Map<String, OperationMetaData> getDockerFaultOperationMap() {
        if (CollectionUtils.isEmpty(dockerFaultOperationMap)) {
            Properties dockerFaultproperties = ReadProperty
                    .readProperty(PROPERTYFILE_FOLDER + File.separator + DOCKER_FAULT_OPERATION_PROPERTIES_FILE);
            dockerFaultOperationMap = extractFaultOperationMap(dockerFaultproperties);
        }
        return dockerFaultOperationMap;
    }

    private static synchronized Map<String, OperationMetaData> extractFaultOperationMap(Properties properties) {
        Map<String, OperationMetaData> vCenterFaultOperationMap = new HashMap<>();
        @SuppressWarnings("unchecked")
        Enumeration<String> enums = (Enumeration<String>) properties.propertyNames();
        while (enums.hasMoreElements()) {
            String opperation = enums.nextElement();
            vCenterFaultOperationMap.put(opperation, getOperationMetaData(properties.getProperty(opperation)));
        }
        return vCenterFaultOperationMap;
    }

    private static OperationMetaData getOperationMetaData(String metaDataString) {
        OperationMetaData metaData = new OperationMetaData();
        String[] metaDataArray = metaDataString.split("#");
        metaData.setClassName(metaDataArray[0]);
        metaData.setMethodName(metaDataArray[1]);
        metaData.setMethodArgTypes(getArgTypeClassList(metaDataArray[2]));
        return metaData;
    }

    private static List<Class<?>> getArgTypeClassList(String paramTypesString) {
        String[] paramTypeArray = paramTypesString.split(",");
        List<Class<?>> paramTypeClassList = new ArrayList<>();
        try {
            for (String paramType : paramTypeArray) {
                paramTypeClassList.add(Class.forName(paramType));
            }
        } catch (ClassNotFoundException e) {
            log.error("Parameters retrieval failed with an exception: {}", e.getMessage());
        }
        return paramTypeClassList;
    }

}
