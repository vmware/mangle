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

package com.vmware.mangle.utils;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;

/**
 * @author dbhat
 *
 */

@Log4j2
public class ApiUtils {
    private ApiUtils() {

    }

    public static boolean isResponseCodeSuccess(int apiResponseStatusCode) {
        log.debug("Validating if the API response : " + apiResponseStatusCode
                + " is an indication of successful request");
        if ((apiResponseStatusCode == HttpStatus.OK.value()) || (apiResponseStatusCode == HttpStatus.CREATED.value())
                || (apiResponseStatusCode == HttpStatus.ACCEPTED.value())
                || (apiResponseStatusCode == HttpStatus.NO_CONTENT.value())) {
            log.debug("Status code indicates the API request was sucessful ");
            return true;
        }
        log.debug("API Response code indicates failure of API");
        return false;
    }
}
