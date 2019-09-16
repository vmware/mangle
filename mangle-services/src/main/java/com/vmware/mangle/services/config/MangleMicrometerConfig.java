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

package com.vmware.mangle.services.config;

import io.micrometer.core.instrument.Clock;
import io.micrometer.datadog.DatadogMeterRegistry;
import io.micrometer.wavefront.WavefrontMeterRegistry;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to initialise Meter Registers
 *
 * @author ashrimali
 *
 */
@Configuration
@Log4j2
public class MangleMicrometerConfig {

    /**
     * @param mangleDatadogConfig
     * @return DatadogMeterRegistry
     */
    @Bean
    public DatadogMeterRegistry initializeDatadogMeterRegistry(MangleDatadogConfig mangleDatadogConfig) {
        try {
            DatadogMeterRegistry datadogMeterRegistry = new DatadogMeterRegistry(mangleDatadogConfig, Clock.SYSTEM);
            datadogMeterRegistry.stop();
            return datadogMeterRegistry;
        } catch (Exception exception) {
            log.error("Failed to initialize Datadog meter registry bean. " + exception.getMessage());
        }
        return null;
    }

    /**
     * @param mangleWavefrontConfig
     * @return WavefrontMeterRegistry
     */
    @Bean
    public WavefrontMeterRegistry initializeWavefrontMeterRegistry(MangleWavefrontConfig mangleWavefrontConfig) {
        try {
            WavefrontMeterRegistry wavefrontMeterRegistry =
                    new WavefrontMeterRegistry(mangleWavefrontConfig, Clock.SYSTEM);
            wavefrontMeterRegistry.stop();
            return wavefrontMeterRegistry;
        } catch (Exception exception) {
            log.error("Failed to initialize wavefront meter registry bean. " + exception.getMessage());
        }
        return null;
    }
}
