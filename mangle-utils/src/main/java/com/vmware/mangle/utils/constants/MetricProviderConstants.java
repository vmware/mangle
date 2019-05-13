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

package com.vmware.mangle.utils.constants;


/**
 * @author dbhat
 *
 *         Utility Class to hold all the constants specific to Metric Provider services like
 *         Wavefront, Datadog etc.
 */

public class MetricProviderConstants {
    private MetricProviderConstants() {
    }

    public static final String MANGLE_FAULT_EVENT_TYPE = "fault-injection";
    public static final String MANGLE_FAULT_EVENT_CLASSIFICATION = "info";
    public static final String SEPERATOR = "; ";
    public static final String DOUBLEQUOTE = "\"";
    public static final String COLON = ":";
    public static final String HYPHEN = "-";
    public static final String NEW_LINE = "\n";
    public static final String METRIC_PROVIDER_PROXY_TEST_CONNECTION_METRIC = "mangle.wavefront.test.connection";

    public static final String DATADOG_API_SEND_EVENT = "/api/v1/events";
    public static final String DATADOG_API_VALIDATE_API_APP_KEYS = "/api/v1/api_key";
    public static final String WAVEFRONT_API_SEND_EVENT = "/api/v2/event";
    public static final String WAVEFRONT_API_TEST_CONNECTION = "/api/v2/source?limit=1";

    public static final String METRIC_PROVIDERS_FOUND = "Successfully got all available Metric Providers";
    public static final String METRIC_PROVIDERS_EMPTY = "Metric provider list is empty";
    public static final String METRIC_PROVIDER_UPDATED = "Metric Provider updated successfully";
    public static final String METRIC_PROVIDER_CREATED = "Metric Provider created successfully";
    public static final String METRIC_PROVIDER_DELETED = "Metric Providers deleted successfully";
    public static final String ACTIVE_METRIC_PROVIDER_FOUND = "Active Metric provider found";
    public static final String METRIC_PROVIDER_ACTIVATED = "Started sending metrics to Metric Provider";
    public static final String METRIC_PROVIDER_DEACTIVATED = "Stopped sending metrics to Metric Provider";
    public static final String ACTIVE_METRIC_PROVIDER_ALREADY_EXISTS = "One Metric Provider is already in Active state";
    public static final String ACTIVE_METRIC_PROVIDER = "activeMetricProvider";
    public static final String SENDING_MANGLE_METRICS = "sendingMangleMetrics";

    public static final short ONE_SECOND_IN_MILLIS = 1000;
    public static final String SENDING_MANGLE_METRICS_STATUS = "Retrieved sendingMangleMetrics status";
    public static final String NODE_ADDRESS = "publicAddress";
    public static final String LOAD_INJECTED = " load injected ";
    public static final String TARGET_DIRECTORY = "Target Directory: ";

    public static final String END_TIME_TEXT = "End Time: ";
    public static final String START_TIME_TEXT = "Start Time: ";
    public static final String STATUS_TEXT = "Status: ";
    public static final String TARGET_VM_TEXT = "Target VM: ";
    public static final String VM_NIC_ID_TEXT = "Nic ID: ";
    public static final String VM_DISK_ID_TEXT = "Disk ID: ";
    public static final String TARGET_CONTAINER_TEXT = "Target Container: ";
}
