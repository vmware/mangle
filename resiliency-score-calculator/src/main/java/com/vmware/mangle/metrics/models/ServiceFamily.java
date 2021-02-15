package com.vmware.mangle.metrics.models;

import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
public class ServiceFamily {
    // Service Family Name
    private String name;
    // Common Queries / Alerts to be executed for all the services in the service Family for calculating the RScore
    private List<QueryParameters> commonQueries;
    // Services for which the RScore to be calculated
    private List<Services> services;
    // Common Tags to be associated while sending the resiliency score of all services in the service family.
    private Map<String, String> tags;
}
