package com.vmware.mangle.metrics.models;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MetricProviderType {
        WAVEFRONT("wavefront");

        private final String name;

        @Override
        public String toString() {
                return this.name;
        }
}
