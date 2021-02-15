package com.vmware.mangle.metrics.models;

import lombok.Data;

/**
 * @author dbhat
 */

/**
 * Model class to define the alert condition to be queried from Monitoring tool defined. The
 * condition/query will have associated weightage as well in calculating the Resiliency score.
 */

@Data
public class QueryParameters {
    private float weight;
    private String query;
}
