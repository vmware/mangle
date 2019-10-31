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

package com.vmware.mangle.integrationtest.mockdata;

import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.vmware.mangle.utils.restclienttemplate.RestTemplateWrapper;

/**
 *
 *
 * @author chetanc
 */
public class MangleClient extends RestTemplateWrapper {

    private String ip;
    private String port;
    private String userName;
    private String password;

    public MangleClient(String ip, String port, String username, String password, MediaType mediaType) {
        this.ip = ip;
        this.port = port;
        this.userName = username;
        this.password = password;
        init(mediaType);
    }

    public void init(MediaType mediaType) {
        String baseUri = "https://" + ip + ":" + port + "/mangle-vc-adapter/";
        setBaseUrl(baseUri);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization",
                "Basic " + new String(Base64.encodeBase64((this.userName + ":" + this.password).getBytes())));
        headers.setContentType(mediaType);
        setHeaders(headers);
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
