package com.vmware.mangle.resiliency.score.utils;

import com.vmware.mangle.metrics.models.MetricProviderType;
import com.vmware.mangle.metrics.models.MonitoringToolConnectionProperties;
import lombok.extern.log4j.Log4j2;

/**
 * @author dbhat
 * 
 *         Get Active metric provider based on the configurations defined.
 */

@Log4j2
public class MetricProviderFactory {

    private MetricProviderFactory() {

    }

    /**
     * Method to retrieve instance of metric provider based on the configuration / properties provided.
     * The method will be enhanced each time when new Metric provider support is added.
     * 
     * @param properties
     *            : Monitoring tool connection properties having details on monitoring tool.
     * @return : Metric provider instance.
     */
    public static IMetricProviderHelper getActiveMetricProvider(MonitoringToolConnectionProperties properties) {
        String metricProvider = properties.getType();
        MetricProviderType metricProviderType = MetricProviderType.valueOf(metricProvider);
        switch (metricProviderType) {
        case WAVEFRONT:
            return new WavefrontDataSourceHelper(properties);
        default:
            log.error(" Specified metric Provider type: " + metricProvider + " is NOT supported.");
            break;
        }
        return null;
    }
}
