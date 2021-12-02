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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import com.vmware.mangle.cassandra.model.MangleAdminConfigurationSpec;
import com.vmware.mangle.cassandra.model.metricprovider.DatadogConnectionProperties;
import com.vmware.mangle.cassandra.model.metricprovider.DynatraceConnectionProperties;
import com.vmware.mangle.cassandra.model.metricprovider.MetricProviderSpec;
import com.vmware.mangle.cassandra.model.metricprovider.WaveFrontConnectionProperties;
import com.vmware.mangle.model.enums.MetricProviderType;
import com.vmware.mangle.utils.ReadProperty;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.MetricProviderConstants;

/**
 * @author ashrimali, dbhat
 *
 */
public class MetricProviderMockData {

    private Properties properties;
    private static final String DYNATRACE_DUMMY_API_KEY = "abcdefjhij1234567890";

    /**
     *
     */
    public MetricProviderMockData() {
        this.properties = ReadProperty.readProperty(Constants.MOCKDATA_FILE);
    }

    /**
     * @return DatadogConnectionProperties
     */
    public DatadogConnectionProperties getDatadogConnectionProperties() {
        DatadogConnectionProperties datadogConnectionProperties = new DatadogConnectionProperties();
        datadogConnectionProperties.setApiKey(this.properties.getProperty("apiKey"));
        datadogConnectionProperties.setApplicationKey(this.properties.getProperty("applicationKey"));
        datadogConnectionProperties.setUri(this.properties.getProperty("uri"));
        Map<String, String> staticTags = new HashMap<>();
        staticTags.put("source", "mangle");
        datadogConnectionProperties.setStaticTags(staticTags);
        return datadogConnectionProperties;
    }

    /**
     * @return DynatraceConnectionProperties
     */
    public DynatraceConnectionProperties getDynatraceConnectionProperties() {
        DynatraceConnectionProperties dynatraceConnectionProperties = getDynatraceConnectionProperties_withNoTags();
        Map<String, String> staticTags = new HashMap<>();
        staticTags.put("source", "mangle");
        dynatraceConnectionProperties.setStaticTags(staticTags);
        return dynatraceConnectionProperties;
    }

    /**
     * @return DynatraceConnectionProperties
     */
    public DynatraceConnectionProperties getDynatraceConnectionProperties_withNoTags() {
        DynatraceConnectionProperties dynatraceConnectionProperties = new DynatraceConnectionProperties();
        dynatraceConnectionProperties.setApiToken(MockDataConstants.DYNATRACE_API_KEY);
        dynatraceConnectionProperties.setDeviceId(MockDataConstants.DYNATRACE_DEVICE_ID);
        dynatraceConnectionProperties.setUri(MockDataConstants.DYNATRACE_URI);
        return dynatraceConnectionProperties;
    }

    /**
     * @return MetricProviderSpec
     */
    public MetricProviderSpec getMetricProviderSpecForDynatrace() {
        MetricProviderSpec dynatraceMetricProviderSpecs = new MetricProviderSpec();
        dynatraceMetricProviderSpecs.setName(MockDataConstants.DYNATRACE_INSTANCE_NAME);
        dynatraceMetricProviderSpecs.setMetricProviderType(MetricProviderType.DYNATRACE);
        dynatraceMetricProviderSpecs.setDynatraceConnectionProperties(getDynatraceConnectionProperties());
        dynatraceMetricProviderSpecs.setId(MockDataConstants.DYNATRACE_ID);
        return dynatraceMetricProviderSpecs;
    }

    /**
     * @return MetricProviderSpec
     */
    public MetricProviderSpec getMetricProviderSpecForDynatrace_NoTags() {
        MetricProviderSpec dynatraceMetricProviderSpecs = new MetricProviderSpec();
        dynatraceMetricProviderSpecs = getMetricProviderSpecForDynatrace();
        dynatraceMetricProviderSpecs.setDynatraceConnectionProperties(getDynatraceConnectionProperties_withNoTags());
        return dynatraceMetricProviderSpecs;
    }

    /**
     * @return WaveFrontConnectionProperties
     */
    public WaveFrontConnectionProperties getWavefrontConnectionProperties() {
        WaveFrontConnectionProperties waveFrontConnectionProperties = new WaveFrontConnectionProperties();
        Map<String, String> staticTags = new HashMap<String, String>();
        staticTags.put(this.properties.getProperty("waveFrontStaticTagKey"),
                this.properties.getProperty("waveFrontStaticTagValue"));
        waveFrontConnectionProperties.setStaticTags(staticTags);
        waveFrontConnectionProperties.setWavefrontAPIToken(this.properties.getProperty("wavefrontAPIToken"));
        waveFrontConnectionProperties.setWavefrontInstance(this.properties.getProperty("wavefrontInstance"));
        return waveFrontConnectionProperties;
    }

    /**
     * @return WaveFrontConnectionProperties
     */
    public WaveFrontConnectionProperties getWavefrontConnectionPropertiesNoTags() {
        WaveFrontConnectionProperties waveFrontConnectionProperties = new WaveFrontConnectionProperties();
        waveFrontConnectionProperties.setWavefrontAPIToken(this.properties.getProperty("wavefrontAPIToken"));
        waveFrontConnectionProperties.setWavefrontInstance(this.properties.getProperty("wavefrontInstance"));
        return waveFrontConnectionProperties;
    }

    /**
     * @return MetricProviderSpec
     */
    public MetricProviderSpec metricProviderWavefront() {
        MetricProviderSpec wavefrontMetricProviderSpecs = new MetricProviderSpec();
        wavefrontMetricProviderSpecs.setName(properties.getProperty("wavefrontMetricReporterName"));
        wavefrontMetricProviderSpecs.setMetricProviderType(MetricProviderType.WAVEFRONT);
        wavefrontMetricProviderSpecs.setWaveFrontConnectionProperties(getWavefrontConnectionProperties());
        wavefrontMetricProviderSpecs.setId(properties.getProperty("wavefrontId"));
        return wavefrontMetricProviderSpecs;
    }

    /**
     * @return MetricProviderSpec
     */
    public MetricProviderSpec metricProviderWavefrontNoTags() {
        MetricProviderSpec wavefrontMetricProviderSpecs = new MetricProviderSpec();
        wavefrontMetricProviderSpecs.setName(properties.getProperty("wavefrontMetricReporterName"));
        wavefrontMetricProviderSpecs.setMetricProviderType(MetricProviderType.WAVEFRONT);
        wavefrontMetricProviderSpecs.setWaveFrontConnectionProperties(getWavefrontConnectionPropertiesNoTags());
        wavefrontMetricProviderSpecs.setId(properties.getProperty("wavefrontId"));
        return wavefrontMetricProviderSpecs;
    }

    public MetricProviderSpec getDynatraceMetricConfig() {
        MetricProviderSpec dynatraceSpec = new MetricProviderSpec();
        dynatraceSpec.setName(MockDataConstants.DYNATRACE_METRIC_PROVIDER_NAME);
        dynatraceSpec.setMetricProviderType(MetricProviderType.DYNATRACE);
        dynatraceSpec.setDynatraceConnectionProperties(getDynatraceConnectionProperties());
        return dynatraceSpec;
    }


    /**
     * @return MetricProviderSpec
     */
    public MetricProviderSpec metricProviderDatadogNoTags() {
        MetricProviderSpec datadogMetricProviderSpecs = new MetricProviderSpec();
        datadogMetricProviderSpecs.setName(properties.getProperty("datadogMetricReporterName"));
        datadogMetricProviderSpecs.setMetricProviderType(MetricProviderType.DATADOG);
        datadogMetricProviderSpecs.setDatadogConnectionProperties(getDatadogConnectionPropertiesNoTags());
        datadogMetricProviderSpecs.setId(properties.getProperty("datadogId"));
        return datadogMetricProviderSpecs;
    }

    /**
     * @return DatadogConnectionProperties
     */
    public DatadogConnectionProperties getDatadogConnectionPropertiesNoTags() {
        DatadogConnectionProperties datadogConnectionProperties = new DatadogConnectionProperties();
        datadogConnectionProperties.setApiKey(this.properties.getProperty("apiKey"));
        datadogConnectionProperties.setApplicationKey(this.properties.getProperty("applicationKey"));
        datadogConnectionProperties.setUri(this.properties.getProperty("uri"));
        return datadogConnectionProperties;
    }

    /**
     * @return MetricProviderSpec
     */
    public MetricProviderSpec wrongSpec() {
        MetricProviderSpec wavefrontMetricProviderSpecs = new MetricProviderSpec();
        wavefrontMetricProviderSpecs.setName("");
        wavefrontMetricProviderSpecs.setMetricProviderType(MetricProviderType.WAVEFRONT);
        wavefrontMetricProviderSpecs.setWaveFrontConnectionProperties(null);
        wavefrontMetricProviderSpecs.setId(properties.getProperty(""));
        return wavefrontMetricProviderSpecs;
    }

    /**
     * @return MetricProviderSpec
     */
    public MetricProviderSpec metricProviderDatadog() {
        MetricProviderSpec datadogMetricProviderSpecs = new MetricProviderSpec();
        datadogMetricProviderSpecs.setName(properties.getProperty("datadogMetricReporterName"));
        datadogMetricProviderSpecs.setMetricProviderType(MetricProviderType.DATADOG);
        datadogMetricProviderSpecs.setDatadogConnectionProperties(getDatadogConnectionProperties());
        datadogMetricProviderSpecs.setId(properties.getProperty("datadogId"));
        return datadogMetricProviderSpecs;
    }

    /**
     * @return MangleAdminConfigurationSpec
     */
    public MangleAdminConfigurationSpec getAdminPropertyForActiveMetricProviderWavefront() {
        MangleAdminConfigurationSpec mangleAdminConfigurationSpec = new MangleAdminConfigurationSpec();
        mangleAdminConfigurationSpec.setPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER);
        mangleAdminConfigurationSpec.setPropertyValue("wavefrontMetricReporterName");
        return mangleAdminConfigurationSpec;
    }

    /**
     * @return MangleAdminConfigurationSpec
     */
    public MangleAdminConfigurationSpec getAdminPropertyForActiveMetricProviderDatadog() {
        MangleAdminConfigurationSpec mangleAdminConfigurationSpec = new MangleAdminConfigurationSpec();
        mangleAdminConfigurationSpec.setPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER);
        mangleAdminConfigurationSpec.setPropertyValue("datadogMetricReporterName");
        return mangleAdminConfigurationSpec;
    }

    /**
     * @return MangleAdminConfigurationSpec
     */
    public MangleAdminConfigurationSpec getAdminPropertyForSendingMetricStatusTrue() {
        MangleAdminConfigurationSpec mangleAdminConfigurationSpec = new MangleAdminConfigurationSpec();
        mangleAdminConfigurationSpec.setPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS);
        mangleAdminConfigurationSpec.setPropertyValue("true");
        return mangleAdminConfigurationSpec;
    }

    /**
     * @return MangleAdminConfigurationSpec
     */
    public MangleAdminConfigurationSpec getAdminPropertyForSendingMetricStatusFalse() {
        MangleAdminConfigurationSpec mangleAdminConfigurationSpec = new MangleAdminConfigurationSpec();
        mangleAdminConfigurationSpec.setPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS);
        mangleAdminConfigurationSpec.setPropertyValue("false");
        return mangleAdminConfigurationSpec;
    }

    /**
     * @return MetricProviderSpec
     */
    public MetricProviderSpec getPrometheusMetricProvider() {
        MetricProviderSpec prometheusMetricProviderSpecs = new MetricProviderSpec();
        prometheusMetricProviderSpecs.setName("prometheus_test");
        prometheusMetricProviderSpecs.setMetricProviderType(MetricProviderType.PROMETHEUS);
        prometheusMetricProviderSpecs.setId(UUID.randomUUID().toString());
        return prometheusMetricProviderSpecs;
    }
}
