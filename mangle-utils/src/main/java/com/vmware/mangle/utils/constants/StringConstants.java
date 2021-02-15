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

package com.vmware.mangle.utils.constants;

/**
 * @author hkilari
 *
 */
public class StringConstants {
    public static final String SERVICE_NAME = "serviceName";
    public static final String CONTAINER_NAME = "containerName";

    public static final String DEPLOYMENTTYPE_ENV_STRING = "DEPLOYMENTTYPE";

    public static final String REGULAR_EXP_FOR_PASS_MASK = "(?<=(--password|--userName)\\s)[^\\s]*";
    public static final String REPLACEMENT_TXT = "******";
    public static final String REGULAR_EXP_FOR_PASS_MASK_IN_DESC = "(?<=(__password|__userName)=)[^,]*";

    private StringConstants() {

    }
}
