package com.vmware.mangle.metrics.models;

import lombok.Data;

/**
 * Model to define the Wavefront Monitoring tool properties.
 */
@Data
public class MonitoringToolConnectionProperties {
        private String url;
        private String apiToken;
        private String type;
        private String proxy;
        private int port;
}
