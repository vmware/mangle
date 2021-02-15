package com.vmware.mangle.metrics.models;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Services {
    // Name of the service
    private String name;
    // Set of queries (alert conditions) to be queried for calculating RScore
    private List<QueryParameters> serviceSpecificQueries;
    private Map<String, String> tags;
}
