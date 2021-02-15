package com.vmware.mangle.metrics.models;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ResiliencyCalculatorOperation {
    RESILIENCY_CALCULATION("resiliency"), EMAIL("email"), RESILIENCY_CALCULATION_USING_QUERIES(
            "resiliencyCalculatorBasedOnQueries");

    private final String name;

    @Override
    public String toString() {
        return this.name;
    }
}
