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

package com.vmware.mangle.resiliency.services;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.vmware.mangle.client.restclient.WavefrontMetricProviderHelper;
import com.vmware.mangle.exception.MangleException;
import com.vmware.mangle.metrics.models.Service;
import com.vmware.mangle.metrics.models.WavefrontConnectionProperties;
import com.vmware.mangle.metrics.models.WavefrontEvent;
import com.vmware.mangle.metrics.models.WavefrontEventResponse;
import com.vmware.mangle.resiliency.commons.ResiliencyConstants;

/**
 * @author chetanc
 *
 */
@Log4j2
public class ResiliencyCalculatorHelper {

    private static String startTime;
    private static String endTime;

    public void calculateResiliencyScore() throws MangleException {
        log.info("Initializing the resiliency calculator");
        WavefrontConnectionProperties connectionProperties = readConfiguration();
        List<Thread> threadList = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, - 3);
        endTime = String.valueOf(calendar.getTimeInMillis());
        calendar.add(Calendar.HOUR, -(connectionProperties.getResiliencyCalculationWindow() + 3));
        startTime = String.valueOf(calendar.getTimeInMillis());

        Map<String, List<String>> serviceFamilyToServicesMap = extractServiceFamilyMap(connectionProperties);
        Map<String, List<WavefrontEvent>> serviceFamilyToEventsMap =
                extractEvents(serviceFamilyToServicesMap.keySet(), connectionProperties);


        for (String serviceFamily : serviceFamilyToServicesMap.keySet()) {
            //Events that belong to the common services are added to all other serviceFamily
            //This ensures the calculation of resiliency score when there is a fault injection when a
            //fault is run on common service
            if (!serviceFamily.equals(ResiliencyConstants.COMMON_SERVICE_FAMILY_NAME)
                    && serviceFamilyToEventsMap.containsKey(ResiliencyConstants.COMMON_SERVICE_FAMILY_NAME)
                    && !CollectionUtils.isEmpty(
                    serviceFamilyToEventsMap.get(ResiliencyConstants.COMMON_SERVICE_FAMILY_NAME))) {
                serviceFamilyToEventsMap.get(serviceFamily)
                        .addAll(serviceFamilyToEventsMap.get(ResiliencyConstants.COMMON_SERVICE_FAMILY_NAME));
            }
            for (String service : serviceFamilyToServicesMap.get(serviceFamily)) {
                if (serviceFamilyToEventsMap.containsKey(serviceFamily)
                        && !CollectionUtils.isEmpty(serviceFamilyToEventsMap.get(serviceFamily))) {
                    ResiliencyCalculator calculator = new ResiliencyCalculator(new Service(serviceFamily, service),
                            connectionProperties, startTime, endTime, serviceFamilyToEventsMap.get(serviceFamily));
                    String threadName = "Thread: service - " + service;
                    Thread thread = new Thread(calculator, threadName);
                    thread.start();
                    threadList.add(thread);
                }
            }
        }

        for (Thread thread : threadList) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                log.info("Calculation of the resiliency score failed with the exception: {}", e.getMessage());
            }
        }
    }

    private static WavefrontConnectionProperties readConfiguration() throws MangleException {
        log.info("Reading the configuration for resiliency calculator");
        String propertyFile = System.getProperty(ResiliencyConstants.PROPERTY_FILE);
        WavefrontConnectionProperties wavefrontConnectionProperties;
        if (!StringUtils.hasText(propertyFile)) {
            propertyFile = ResiliencyConstants.LOCAL_PROPERTY_FILE;
        }
        Yaml yaml = new Yaml(new Constructor(WavefrontConnectionProperties.class));
        try {
            FileInputStream fis = new FileInputStream(propertyFile);
            wavefrontConnectionProperties = yaml.load(fis);
        } catch (FileNotFoundException e) {
            throw new MangleException(e.getMessage());
        }
        return wavefrontConnectionProperties;
    }

    private static Map<String, List<String>> extractServiceFamilyMap(
            WavefrontConnectionProperties connectionProperties) {
        Map<String, List<String>> serviceFamilyMap = new HashMap<>();
        for (Service service : connectionProperties.getServices()) {
            if (!serviceFamilyMap.containsKey(service.getServiceFamily())) {
                serviceFamilyMap.put(service.getServiceFamily(), new ArrayList<>());
            }
            serviceFamilyMap.get(service.getServiceFamily()).add(service.getService());
        }
        return serviceFamilyMap;
    }

    private static Map<String, List<WavefrontEvent>> extractEvents(Set<String> serviceFamilies,
            WavefrontConnectionProperties connectionProperties) {
        WavefrontMetricProviderHelper wavefrontHelper = new WavefrontMetricProviderHelper(
                connectionProperties.getWavefrontUrl(), connectionProperties.getWavefrontApiToken());
        Map<String, List<WavefrontEvent>> serviceFamilyEvents = new HashMap<>();
        for (String serviceFamily : serviceFamilies) {
            WavefrontEventResponse eventResponse = wavefrontHelper.queryEvents(connectionProperties.getTags(),
                    serviceFamily, null, startTime, endTime);
            if (null != eventResponse) {
                serviceFamilyEvents.put(serviceFamily, eventResponse.getEvents());
            }
        }

        return serviceFamilyEvents;
    }
}
