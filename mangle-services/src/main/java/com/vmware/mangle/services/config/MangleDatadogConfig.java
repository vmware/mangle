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

import javax.validation.constraints.NotEmpty;

import io.micrometer.datadog.DatadogConfig;
import lombok.Data;
import org.springframework.context.annotation.Configuration;


/**
 * Configuration class for Mangle Datadog
 * @author ashrimali
 *
 */
@Configuration
@Data
@NotEmpty
public class MangleDatadogConfig implements DatadogConfig {

    private String apiKey = "";

    private String applicationKey = "";

    private String uri = "";


    @Override
    public String get(String key) {
        //There is no significance of this value here hence passing null.
        return null;
    }

    @Override
    public String apiKey() {
        return getApiKey();
    }

    @Override
    public String applicationKey() {
        return getApplicationKey();
    }

    @Override
    public String uri() {
        return getUri();
    }
}
