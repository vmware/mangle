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

import io.prometheus.client.exporter.common.TextFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.vmware.mangle.cassandra.model.metricprovider.MetricProviderSpec;
import com.vmware.mangle.model.enums.MetricProviderType;
import com.vmware.mangle.services.config.MangleMetricsConfiguration;
import com.vmware.mangle.services.repository.MetricProviderRepository;

/**
 * {@link Endpoint} that outputs metrics in a format that can be scraped by the Prometheus server.
 *
 * @author hkilari
 * @author kumargautam
 * @since 3.0.0
 */
@Service
@AutoConfigureAfter(PrometheusScrapeEndpoint.class)
@EndpointWebExtension(endpoint = PrometheusScrapeEndpoint.class)
public class PrometheusScrapeEndpointService {

    private PrometheusScrapeEndpoint prometheusScrapeEndpoint;
    private MangleMetricsConfiguration enableMetrics;
    private MetricProviderRepository metricProviderRepository;

    /**
     * Create a new {@link PrometheusScrapeEndpointService} instance.
     *
     * @param prometheusScrapeEndpoint
     */
    @Autowired
    public PrometheusScrapeEndpointService(PrometheusScrapeEndpoint prometheusScrapeEndpoint,
            MangleMetricsConfiguration enableMetrics, MetricProviderRepository metricProviderRepository) {
        Assert.notNull(prometheusScrapeEndpoint, "prometheusScrapeEndpoint must not be null");
        this.prometheusScrapeEndpoint = prometheusScrapeEndpoint;
        this.enableMetrics = enableMetrics;
        this.metricProviderRepository = metricProviderRepository;
    }

    @ReadOperation(produces = TextFormat.CONTENT_TYPE_004)
    public String scrape() {
        if (this.prometheusScrapeEndpoint != null && enableMetrics.getMetricsEnabled() && verifyMetricProviderType()) {
            return this.prometheusScrapeEndpoint.scrape();
        }
        return "Prometheus Metric Provider is not enabled!";
    }

    private boolean verifyMetricProviderType() {
        if (StringUtils.hasText(enableMetrics.getActiveMetricProvider())) {
            MetricProviderSpec metricProviderSpec =
                    metricProviderRepository.findByName(enableMetrics.getActiveMetricProvider()).orElse(null);
            return (metricProviderSpec != null
                    && MetricProviderType.PROMETHEUS == metricProviderSpec.getMetricProviderType());
        }
        return false;
    }
}
