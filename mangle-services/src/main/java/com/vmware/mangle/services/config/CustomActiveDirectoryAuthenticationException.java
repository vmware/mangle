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

import org.springframework.security.core.AuthenticationException;

/**
 * <p>
 * Thrown as a translation of an {@link javax.naming.AuthenticationException} when attempting to
 * authenticate against Active Directory using
 * {@link CustomActiveDirectoryLdapAuthenticationProvider }. Typically this error is wrapped by an
 * {@link AuthenticationException} since it does not provide a user friendly message. When wrapped,
 * the original Exception can be caught and
 * {@link org.springframework.security.ldap.authentication.ad.ActiveDirectoryAuthenticationException}
 * can be accessed using {@link AuthenticationException#getCause()} for custom error handling.
 * </p>
 * <p>
 * The {@link #getDataCode()} will return the error code associated with the data portion of the
 * error message. For example, the following error message would return 773 for
 * {@link #getDataCode()}.
 * </p>
 *
 * <pre>
 * javax.naming.AuthenticationException: [LDAP: error code 49 - 80090308: LdapErr: DSID-0C090334, comment: AcceptSecurityContext error, data 775, vece ]
 * </pre>
 * @author chetanc
 * @author Rob Winch
 */
@SuppressWarnings("serial")
public class CustomActiveDirectoryAuthenticationException extends AuthenticationException {
    private final String dataCode;

    public CustomActiveDirectoryAuthenticationException(String dataCode, String message, Throwable cause) {
        super(message, cause);
        this.dataCode = dataCode;
    }

    public String getDataCode() {
        return dataCode;
    }
}
