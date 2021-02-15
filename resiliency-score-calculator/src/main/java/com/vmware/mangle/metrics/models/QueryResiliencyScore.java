package com.vmware.mangle.metrics.models;

import lombok.Data;

import java.util.Map;

/**
 * VO to store the Resiliency score of each query getting executed.
 */

@Data
public class QueryResiliencyScore {
    private Map<String, String> tags;
    private Double rScore;
}
