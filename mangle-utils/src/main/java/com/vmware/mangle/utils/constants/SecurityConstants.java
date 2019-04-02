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
 * @author bkaranam
 *
 *
 */
public class SecurityConstants {
    private SecurityConstants() {
    }

    public static final int PASSWORD_ITERATION_COUNT = 65536;
    public static final int KEY_SIZE_IN_BYTES = 128;
    public static final int DEFAULT_SALT_SIZE_IN_BYTES = 20;
    public static final String CIPHER_ALGORITHM = "AES/GCM/PKCS5Padding";
    public static final String SECRETKEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA1";
    public static final String SECRETKEY_ALGORITHM = "AES";
    public static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`!@#$%^*()-_=+[{]}\\|;:\'\",<.>/?";
    public static final int DEFAULT_PASSWORD_SIZE = 15;
    public static final String STORABLE_ENCRYPTED_TEXT_FORMAT = "%d&%s%s%s&%d";
}
