package com.vmware.mangle.metrics.models;

import lombok.Data;

import java.util.Map;

@Data
public class TimeseriesData {
        private Map<String, String> tags;
        private Double[][] data;
}
