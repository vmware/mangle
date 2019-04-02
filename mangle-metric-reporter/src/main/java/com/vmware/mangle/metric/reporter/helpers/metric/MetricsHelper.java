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

package com.vmware.mangle.metric.reporter.helpers.metric;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import lombok.extern.log4j.Log4j2;


/**
 * Class hosting helper methods for metrics. The methods are can be consumed by any reporters (ex:
 * wavefront, Datadog)
 *
 * @author dbhat
 */
@Log4j2
public class MetricsHelper {

    private MetricsHelper() {
    }

    /**
     * Method to validate if the specified input is a null or Empty string.
     *
     * @param input
     *            : String to be validated
     * @return true: if the specified input is an empty string or null false: if the specified input
     *         is NOT empty and NOT null
     */
    public static Boolean isNull(String input) {
        log.info("Checking if the input: " + input + " is non-empty and not null");
        if (null != input && !input.isEmpty()) {
            return false;
        }
        log.error("The specified string is NULL / Empty");
        return true;
    }

    /**
     * Method to validate if the specified metric name is a valid metric name or Not. Monitoring
     * systems like wavefront, mandates the metric name to be in pirticular format. Example:
     * "responseCode" is NOT a valid metric name. But "response.code" is a VALID metric name.
     *
     * @param metricName
     *            : Metric name specified to be sent to monitoring system.
     * @return : Boolean : true : If the specified metric name is a valid metric name. false: If the
     *         specified metric is not a valid metric name
     */
    public static Boolean isAValidMetricName(String metricName) {
        log.info("Validating if the specified metric Name : " + metricName + " is a valid metric Name");
        if (isNull(metricName)) {
            log.error("NOT a valid metric Name. Metric Name should be in the format: metricPart.metricPart ");
            return false;
        }
        if (metricName.contains(".")) {
            if (Pattern.matches(".[a-zA-Z0-9-_.]+", metricName)) {
                log.info("Specified metric name is a valid metric name ");
                return true;
            }
        }
        log.error("NOT a valid metric Name. Metric Name should be in the format: metricPart.metricPart ");
        log.error(" Valid metric can contain following characters only: a-z A-Z 0-9 _ - .");
        return false;
    }

    /**
     * Method to validate if the specified metric value is in the correct format. The monitoring
     * system like, wavefront requires the value to be in the format Double. In this validation, we
     * will validate if the specified metric value is NOT null and can the same be converted to
     * double?
     *
     * @param metricValue
     *            : Metric value specified to be sent to monitoring system like wavefront.
     * @return : Boolean, true: if the specified metric value is NOT null and any format which we
     *         can convert to double. false: if the specified value is NOT in valid format
     */
    public static Boolean isAValidMetricValue(Object metricValue) {
        log.info("Validating if the specified metric Value is a valid metric Value");
        if (null == metricValue) {
            log.error(" NOT a valid metric value. Specified metric value is null");
            return false;
        }
        Boolean valid = ((metricValue instanceof Double) || (metricValue instanceof Integer)
                || (metricValue instanceof Float) || (metricValue instanceof String)) ? true : false;
        if (!valid) {
            log.error("The specified metric value is NOT a valid Metric value. ");
            log.error("Metric value can be of type: Integer, Float, Double , String ");
            return false;
        }
        log.info("Specified metric value is a VALID metric value");
        return true;
    }

    /**
     * Method to validate if the tags specified is Empty?
     *
     * @param tags
     *            : tags specified to be sent to monitoring system (ex: wavefront).
     * @return: boolean, true: if the tags are empty false: if tags are not empty
     */
    public static Boolean isEmptyTag(Map<String, String> tags) {
        log.info("Checking if the tags are empty");
        if (null == tags || tags.isEmpty()) {
            log.error(" Empty tags OR null value for the tags. No tags specified ");
            return true;
        }
        return false;
    }

    /**
     * Method to get current time in epoch format (millis). The monitoring system SDK requires the
     * timestamp to be specified in epoch format. This helper method helps in getting the current
     * time in epoch millis.
     *
     * @return: epoch time in millis
     */
    public static Long getCurrentTimeStampInMillis() {
        Long timeStampInMillis;
        Instant instant = Instant.now();
        timeStampInMillis = instant.toEpochMilli();
        log.info("Returning the current timestamp in Millis : " + timeStampInMillis);
        return timeStampInMillis;
    }

    /**
     * Method to convert the specified input value to Double equivalent. The monitoring system SDKs
     * consumed for sending metrics requires the metric value to be of type: Double. Using this
     * method, we are converting the int, float, String values to be in the Double format rather
     * than failing the metric send operation.
     *
     * @param metricValue:
     *            Metric value specified to be sent to monitoring system like wavefront.
     * @return: double: Double equivalent of specified metric value.
     */
    public static double getDoubleEquivalent(Object metricValue) {
        if (metricValue instanceof String) {
            return Double.parseDouble((String) metricValue);
        }
        if (metricValue instanceof Integer) {
            return Double.valueOf((Integer) metricValue);
        }
        if (metricValue instanceof Float) {
            return Double.valueOf((Float) metricValue);
        }
        return (Double) metricValue;
    }

    public static Map<String, String> stringToHashMap(String tags) {
        HashMap<String, String> map = new HashMap<>();
        if (null == tags || tags.isEmpty()) {
            log.error(" Empty String and cannot convert to map. Returning empty map");
            return map;
        }
        String[] individualTags = tags.split(",");
        if (individualTags.length < 1) {
            log.error(" Input doesn't seems to be in the format. The input string must be seperated by , ");
            log.error("Returning empty map");
            return map;
        }
        for (String tag : individualTags) {
            String[] keyValPair = tag.split("=");
            if (keyValPair.length < 2) {
                log.error(" Cannot retrive the valid tag from: " + tag);
            } else {
                map.put(keyValPair[0], keyValPair[1]);
            }
        }
        return map;
    }


}
