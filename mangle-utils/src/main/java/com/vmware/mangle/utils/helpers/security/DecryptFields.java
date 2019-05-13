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

package com.vmware.mangle.utils.helpers.security;

import java.lang.reflect.Field;

import lombok.extern.log4j.Log4j2;
import org.springframework.util.ReflectionUtils;

import com.vmware.mangle.model.enums.EncryptField;
import com.vmware.mangle.utils.exceptions.MangleSecurityException;

/**
 * @author bkaranam
 *
 */
@Log4j2
public class DecryptFields {

    private DecryptFields() {
    }

    public static Object decrypt(Object source) {
        for (Field field : source.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(EncryptField.class)) {
                ReflectionUtils.makeAccessible(field);
                try {
                    String plainText;
                    Object originalValue = ReflectionUtils.getField(field, source);
                    Object decryptedValue = null;
                    if (null != originalValue) {
                        if (field.getType() == byte[].class) {
                            plainText = new String((byte[]) originalValue);
                            decryptedValue = SecurityHelper.decryptWithDefaultFormat(plainText).getBytes();
                        }
                        if (field.getType().isAssignableFrom(String.class)) {
                            plainText = (String) originalValue;
                            decryptedValue = SecurityHelper.decryptWithDefaultFormat(plainText);
                        }
                        ReflectionUtils.setField(field, source, decryptedValue);
                    }
                } catch (MangleSecurityException e) {
                    log.error("Decrypting " + field.getName() + " field failed with exception. " + e.getMessage()
                            + " Skipped decryption");
                }
            }
        }
        return source;
    }
}
