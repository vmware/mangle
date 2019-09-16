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

package com.vmware.mangle.utils.mockdata;

/**
 * @author dbhat
 */
public class ApiResponseMockData {
    public String getApiResponse(int statusCode) {
        StringBuilder apiResponse = new StringBuilder("{ \"status\": {");
        apiResponse.append("\"result\": \"OK\",");
        apiResponse.append("\"message\": \"dummy api response\",");
        apiResponse.append("\"code\": ");
        apiResponse.append(statusCode);
        apiResponse.append("},\"response\": {");
        apiResponse.append("\"key1\": \"value1\",");
        apiResponse.append("\"dummyResponse\": true");
        apiResponse.append("}}");
        return apiResponse.toString();
    }
}
