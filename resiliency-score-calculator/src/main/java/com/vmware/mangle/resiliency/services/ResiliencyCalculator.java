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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.vmware.mangle.client.restclient.WavefrontMetricProviderHelper;
import com.vmware.mangle.metric.common.Constants;
import com.vmware.mangle.metric.common.Metric;
import com.vmware.mangle.metric.wavefront.reporter.WavefrontMetricReporter;
import com.vmware.mangle.metrics.models.Service;
import com.vmware.mangle.metrics.models.WavefrontConnectionProperties;
import com.vmware.mangle.metrics.models.WavefrontEvent;
import com.vmware.mangle.metrics.models.WavefrontEventResponse;
import com.vmware.mangle.resiliency.commons.ResiliencyConstants;

/**
 * @author chetanc
 *
 *
 */
@Log4j2
public class ResiliencyCalculator implements Runnable {

    private Service service;
    private WavefrontConnectionProperties connectionProperties;
    private WavefrontMetricReporter wavefrontMetricReporter;
    private WavefrontMetricProviderHelper metricProviderHelper;
    public boolean postFunctionScore = false;
    public String startTime;
    public String endTime;
    public List<WavefrontEvent> events;
    double resiliencyWeightage;
    double apiHealthWeightage;
    boolean calculateFS;
    Map<String, WavefrontEvent> eventNameToEventMapping = new HashMap<>();
    HashMap<String, String> outputMetricTags = new HashMap<>();

    ResiliencyCalculator(Service service, WavefrontConnectionProperties connectionProperties) {
        this.service = service;
        this.connectionProperties = connectionProperties;
        metricProviderHelper = getWavefrontServerClient(connectionProperties);
        HashMap<String, String> staticTags = getTags(service.getService(), service.getServiceFamily());
        staticTags.putAll(new HashMap<>(connectionProperties.getTags()));
        wavefrontMetricReporter = new WavefrontMetricReporter(connectionProperties.getWavefrontProxyUrl(),
                Integer.parseInt(connectionProperties.getWavefrontProxyPort()),
                connectionProperties.getWavefrontMetricSource(), staticTags);
        int functionalWeightage = System.getProperty(Constants.API_WEIGHTAGE__ARG_NAME) != null
                ? Integer.parseInt(System.getProperty(Constants.API_WEIGHTAGE__ARG_NAME))
                : 0;
        calculateFS = Boolean.parseBoolean(System.getProperty(Constants.CALCULATE_FUNCTIONAL_SCORE_ARG_NAME));
        resiliencyWeightage = 1 - functionalWeightage / 100.0;
        apiHealthWeightage = 1 - resiliencyWeightage;
        outputMetricTags.put(ResiliencyConstants.SERVICE, this.service.getService());
        outputMetricTags.put(ResiliencyConstants.SERVICE_FAMILY, this.service.getServiceFamily());
    }

    public ResiliencyCalculator(Service service, WavefrontConnectionProperties connectionProperties, String startTime,
            String endTime, List<WavefrontEvent> events) {
        this(service, connectionProperties);
        this.startTime = startTime;
        this.endTime = endTime;
        this.events = events;
    }

    @Override
    public void run() {
        Map<String, Map<String, Double>> overallResiliencyApiMap = new HashMap<>();
        Map<String, Map<String, Double>> resiliencyApiMap = new HashMap<>();
        Map<String, Map<String, Double>> functionalResiliencyApiMap = new HashMap<>();

        if (null == endTime || null == startTime) {
            initializeTimeBoundary();
        }

        if (CollectionUtils.isEmpty(events)) {
            WavefrontEventResponse response = metricProviderHelper.queryEvents(connectionProperties.getTags(),
                    service.getServiceFamily(), service.getService(), startTime, endTime);
            events = response.getEvents();
        }

        log.info("Found {} events", events.size());

        events = filterEvents(events, Long.parseLong(startTime), Long.parseLong(endTime));
        if (!CollectionUtils.isEmpty(events)) {
            extractResiliencyScoreForService(service, events, overallResiliencyApiMap, resiliencyApiMap,
                    functionalResiliencyApiMap);
            sendMetrics(overallResiliencyApiMap, resiliencyApiMap, functionalResiliencyApiMap);
        } else {
            log.info("No events found for the service {} of the service family for resiliency score calculation",
                    service.getService());
        }
    }

    public void sendMetrics(Map<String, Map<String, Double>> overallResiliencyEventMap,
            Map<String, Map<String, Double>> nonFunctionalResiliencyEventMap,
            Map<String, Map<String, Double>> functionalResiliencyEventMap) {

        ArrayList<Metric> metrics = new ArrayList<>();
        for (Map.Entry<String, Map<String, Double>> eventToApiMapEntry : overallResiliencyEventMap.entrySet()) {
            double serviceResiliency;
            Map<String, Double> overallResiliencyApiMap = eventToApiMapEntry.getValue();
            outputMetricTags.put(Constants.FAULT, eventToApiMapEntry.getKey());
            constructApiMetrics(eventToApiMapEntry.getKey(), overallResiliencyApiMap, metrics);
            serviceResiliency =
                    overallResiliencyApiMap.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

            log.info("Resiliency score for the service {} of the service family {} is: {}", service.getService(),
                    service.getServiceFamily(), serviceResiliency);
            if (serviceResiliency > 0 && serviceResiliency <= 1.0) {
                //send resiliency score for the individual URLs
                wavefrontMetricReporter.sendMetrics(metrics);
                //send resiliency score for the overall service
                wavefrontMetricReporter.sendMetric(new Metric(connectionProperties.getOutputMetricName(),
                        serviceResiliency, eventNameToEventMapping.get(eventToApiMapEntry.getKey()).getEnd(),
                        outputMetricTags, connectionProperties.getWavefrontMetricSource()));

                if (postFunctionScore) {
                    sendFunctionalResiliencyScoresForEvent(eventToApiMapEntry.getKey(),
                            nonFunctionalResiliencyEventMap.get(eventToApiMapEntry.getKey()),
                            functionalResiliencyEventMap.get(eventToApiMapEntry.getKey()));
                } else {
                    log.info("Functional resiliency score for the service {} not sent, value is {}",
                            service.getService(), serviceResiliency);
                    log.info("Non Functional resiliency score for the service {} not sent, value is {}",
                            service.getService(), serviceResiliency);
                }
            } else {
                log.info(
                        "Resiliency score for the service {} of the service family {} is not sent, RS value is " + "{}",
                        service.getService(), service.getServiceFamily(), serviceResiliency);
            }
        }

    }

    private void constructApiMetrics(String eventName, Map<String, Double> overallResiliencyApiMap,
            List<Metric> metrics) {
        String urlMetricName = connectionProperties.getOutputUrlMetricName();
        double apiResiliency;
        for (Map.Entry<String, Double> entry : overallResiliencyApiMap.entrySet()) {
            HashMap<String, String> testTags = new HashMap<>();
            testTags.put(Constants.URL_SUFFIX, entry.getKey());
            testTags.put(Constants.FAULT, eventName);
            apiResiliency = entry.getValue();
            Metric metric = new Metric(urlMetricName, apiResiliency, eventNameToEventMapping.get(eventName).getEnd(),
                    testTags, connectionProperties.getWavefrontMetricSource());

            log.debug("The resiliency score of the API {} for the service {} of the service family {} is {}",
                    entry.getKey(), service.getService(), service.getServiceFamily(), apiResiliency);
            metrics.add(metric);
        }
    }

    private void sendFunctionalResiliencyScoresForEvent(String eventName,
            Map<String, Double> nonFunctionalResiliencyApiMap, Map<String, Double> functionalResiliencyApiMap) {
        double functionResiliencyScore =
                functionalResiliencyApiMap.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        double nonFunctionalResiliencyScore =
                nonFunctionalResiliencyApiMap.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        if (functionResiliencyScore > 0.0 && functionResiliencyScore <= 1.0) {
            log.info("Functional resiliency score for the service {} is {}", service.getService(),
                    functionResiliencyScore);
            log.info("Non functional resiliency score for the service {} is {}", service.getService(),
                    nonFunctionalResiliencyScore);

            //Send functional score
            wavefrontMetricReporter.sendMetric(new Metric(connectionProperties.getFunctionScoreMetricName(),
                    functionResiliencyScore, eventNameToEventMapping.get(eventName).getEnd(), outputMetricTags,
                    connectionProperties.getWavefrontMetricSource()));

            //Send non-functional score
            wavefrontMetricReporter.sendMetric(new Metric(connectionProperties.getNonFunctionalScoreMetricName(),
                    nonFunctionalResiliencyScore, eventNameToEventMapping.get(eventName).getEnd(), outputMetricTags,
                    connectionProperties.getWavefrontMetricSource()));
        }
    }


    /**
     * Method extracts resiliency score from the list of events that are available in the metric
     * provider
     *
     * 1. list of events for the service is available over the resiliencyCalculationWindow
     *
     * 2. calculate resiliency score of each api over the entire resiliencyCalculationWindow
     *
     * a. Get the preInjection resiliency for each api over testReferenceWindow before event
     * started.
     *
     * b. Get the postInjection resiliency for each api over testReferenceWindow after event
     * completed.
     *
     * c. Calculate resiliency score of each api across each event.
     *
     *
     *
     * @param service
     * @param events
     * @return
     */
    private void extractResiliencyScoreForService(Service service, List<WavefrontEvent> events,
            Map<String, Map<String, Double>> overallResiliencyEventMap,
            Map<String, Map<String, Double>> nonFunctionalResiliencyEventMap,
            Map<String, Map<String, Double>> functionResiliencyEventMap) {
        log.debug("The resiliency score for the service {} will be calculated over the {} events", service.getService(),
                events.size());

        for (WavefrontEvent event : events) {
            long eventStartTime = flatTimeFromEvent(event.getStart()) / 1000;
            long eventEndTime = flatTimeFromEvent(event.getEnd()) / 1000;
            Map<String, Double> overallResiliencyApiMap = new HashMap<>();
            Map<String, Double> functionalResiliencyApiMap = new HashMap<>();
            Map<String, Double> nonFunctionalResiliencyApiMap = new HashMap<>();
            Map<String, Double> preInjectionResiliency = getPreInjectionScoreForApis(service.getService(),
                    eventStartTime - connectionProperties.getTestReferenceWindow() * 60, eventStartTime,
                    connectionProperties.getMetricQueryGranularity());
            Map<String, Double> postInjectionResiliency = getPostInjectionScoreForApis(service.getService(),
                    eventEndTime, eventEndTime + connectionProperties.getTestReferenceWindow() * 60,
                    connectionProperties.getMetricQueryGranularity());

            calculateResiliencyScoreForPrePostScores(preInjectionResiliency, postInjectionResiliency,
                    overallResiliencyApiMap, nonFunctionalResiliencyApiMap, functionalResiliencyApiMap);

            overallResiliencyEventMap.put(event.getName(), overallResiliencyApiMap);
            nonFunctionalResiliencyEventMap.put(event.getName(), nonFunctionalResiliencyApiMap);
            functionResiliencyEventMap.put(event.getName(), functionalResiliencyApiMap);
            eventNameToEventMapping.put(event.getName(), event);
        }
    }

    private void calculateResiliencyScoreForPrePostScores(Map<String, Double> preInjectionResiliency,
            Map<String, Double> postInjectionResiliency, Map<String, Double> overallResiliencyApiMap,
            Map<String, Double> nonFunctionalResiliencyApiMap, Map<String, Double> functionalResiliencyApiMap) {
        double resiliencyScore;
        String apiUrl;
        for (Map.Entry<String, Double> postInjectionResiliencyScoreEntry : postInjectionResiliency.entrySet()) {
            apiUrl = postInjectionResiliencyScoreEntry.getKey();
            if (StringUtils.isEmpty(preInjectionResiliency.get(apiUrl))) {
                continue;
            }
            if (postInjectionResiliencyScoreEntry.getValue() != 0) {
                resiliencyScore = postInjectionResiliencyScoreEntry.getValue() / preInjectionResiliency.get(apiUrl);
            } else {
                resiliencyScore = 0;
            }
            if (resiliencyScore > 1) {
                resiliencyScore = 1;
            }
            if (!overallResiliencyApiMap.containsKey(apiUrl)) {
                overallResiliencyApiMap.put(apiUrl, 0.0);
            }
            /* If enabled to calculate the functional resiliency score */
            if (calculateFS) {
                double functionalPartValue = apiHealthWeightage * preInjectionResiliency.get(apiUrl);
                double actualResiliencyPartValue = resiliencyWeightage * resiliencyScore;

                if (!functionalResiliencyApiMap.containsKey(apiUrl)) {
                    functionalResiliencyApiMap.put(apiUrl, 0.0);
                }
                if (!nonFunctionalResiliencyApiMap.containsKey(apiUrl)) {
                    nonFunctionalResiliencyApiMap.put(apiUrl, 0.0);
                }

                functionalResiliencyApiMap.put(apiUrl, functionalResiliencyApiMap.get(apiUrl) + functionalPartValue);
                nonFunctionalResiliencyApiMap.put(apiUrl,
                        nonFunctionalResiliencyApiMap.get(apiUrl) + actualResiliencyPartValue);
                overallResiliencyApiMap.put(apiUrl,
                        overallResiliencyApiMap.get(apiUrl) + actualResiliencyPartValue + functionalPartValue);
                postFunctionScore = true;
            } else {
                overallResiliencyApiMap.put(apiUrl, overallResiliencyApiMap.get(apiUrl) + resiliencyScore);
            }
        }
    }

    private List<WavefrontEvent> filterEvents(List<WavefrontEvent> wavefrontEvents, Long startTime, Long endTime) {
        List<WavefrontEvent> filterEvents = wavefrontEvents.stream()
                .filter(event -> event.getEnd() >= startTime && event.getEnd() <= endTime).collect(Collectors.toList());
        log.info("{} events were found out of time bound, startTime: {} and endTime: {}",
                wavefrontEvents.size() - filterEvents.size(), startTime, endTime);
        List<WavefrontEvent> finalEvents = filterEvents.stream()
                .filter(event -> event.getTags().get("details").indexOf("COMPLETED") > 0).collect(Collectors.toList());
        log.info("{} events were not in COMPLETED state, ignoring them", filterEvents.size() - finalEvents.size());
        return finalEvents;
    }

    private WavefrontMetricProviderHelper getWavefrontServerClient(WavefrontConnectionProperties connectionProperties) {
        return new WavefrontMetricProviderHelper(connectionProperties.getWavefrontUrl(),
                connectionProperties.getWavefrontApiToken());
    }

    private Map<String, Double> getPreInjectionScoreForApis(String service, Long startTime, Long endTime,
            String granularity) {
        Map<String, Double> successMap = metricProviderHelper.queryMetricsSuccessCountGroupByUrl(
                connectionProperties.getWavefrontMetricSource(), service, startTime, endTime, granularity);
        Map<String, Double> totalMap = metricProviderHelper.queryMetricsCountGroupByUrl(
                connectionProperties.getWavefrontMetricSource(), service, startTime, endTime, granularity);

        return getRatiosMapForApis(successMap, totalMap);
    }

    private Map<String, Double> getPostInjectionScoreForApis(String service, Long startTime, Long endTime,
            String granularity) {
        Map<String, Double> successMap = metricProviderHelper.queryMetricsSuccessCountGroupByUrl(
                connectionProperties.getWavefrontMetricSource(), service, startTime, endTime, granularity);
        Map<String, Double> totalMap = metricProviderHelper.queryMetricsCountGroupByUrl(
                connectionProperties.getWavefrontMetricSource(), service, startTime, endTime, granularity);

        return getRatiosMapForApis(successMap, totalMap);
    }

    private Map<String, Double> getRatiosMapForApis(Map<String, Double> successMap, Map<String, Double> totalMap) {
        Map<String, Double> resiliencyMap = new HashMap<>();
        for (Map.Entry<String, Double> entry : totalMap.entrySet()) {
            if (entry.getValue() != 0.0) {
                if (!successMap.containsKey(entry.getKey())) {
                    resiliencyMap.put(entry.getKey(), 0.0);
                } else {
                    resiliencyMap.put(entry.getKey(), successMap.get(entry.getKey()) / entry.getValue());
                }
            }
        }
        return resiliencyMap;
    }

    public HashMap<String, String> getTags(String service, String serviceFamily) {
        HashMap<String, String> tags = new HashMap<>();
        tags.put(ResiliencyConstants.SERVICE, service);
        tags.put(ResiliencyConstants.SERVICE_FAMILY, serviceFamily);
        return tags;
    }

    public void initializeTimeBoundary() {
        Calendar calendar = Calendar.getInstance();
        this.endTime = String.valueOf(calendar.getTimeInMillis());
        calendar.add(Calendar.HOUR, -connectionProperties.getResiliencyCalculationWindow());
        this.startTime = String.valueOf(calendar.getTimeInMillis());
    }

    public long flatTimeFromEvent(Long timestamp) {
        Date date = new Date(timestamp);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.SECOND, 0);
        return c.getTimeInMillis();
    }

}
