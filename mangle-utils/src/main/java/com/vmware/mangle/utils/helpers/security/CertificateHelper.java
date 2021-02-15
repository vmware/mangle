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

import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import lombok.extern.log4j.Log4j2;

/**
 * @author ashrimali
 *
 */
@Log4j2
public class CertificateHelper {

    private CertificateHelper() {
    }

    public static SSLSocketFactory validateCert(String hostIP) {
        SSLContext sc = getSSLContextWithValidateCert(hostIP);
        if (null != sc) {
            return sc.getSocketFactory();
        }
        return null;
    }

    public static SSLContext getSSLContextWithValidateCert(String hostIP) {
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> hostname.equals(hostIP));
        TrustManager[] trustAllCerts = new TrustManager[] { getX509TrustManager() };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, trustAllCerts, null);
            return sc;
        } catch (GeneralSecurityException e) {
            log.error(e);
        }
        return null;
    }

    public static X509TrustManager getX509TrustManager() {
        return new X509TrustManager() {

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[] {};
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                //No code changes required in this method
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                //No code changes required in this method
            }
        };
    }

}
