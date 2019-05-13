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

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * @author bkaranam
 * @author anjalir
 *
 *
 */
@Setter
@Getter
@Log4j2
public class RemoteBase {
    private String host;
    private String userName;
    private String password;
    private int port;
    private int timeout;
    private String privateKey;

    public RemoteBase(String host, String userName, String password, int port) {
        setHost(host);
        setUserName(userName);
        setPassword(password);
        setPort(port);
    }

    public RemoteBase(String host, String userName, String password, int port, int timeout) {
        this(host, userName, password, port);
        setTimeout(timeout);
    }

    public RemoteBase(String host, String userName, int port, String privateKey) {
        this.host = host;
        this.userName = userName;
        this.port = port;
        this.privateKey = privateKey;
    }

    public RemoteBase(String host, String userName, int port, String privateKey, int timeout) {
        this(host, userName, port, privateKey);
        setTimeout(timeout);
    }

    public RemoteBase(String host, String userName, String password, int port, String privateKey) {
        this.host = host;
        this.userName = userName;
        this.password = password;
        this.port = port;
        this.privateKey = privateKey;
    }

    public RemoteBase(String host, String userName, String password, int port, String privateKey, int timeout) {
        this(host, userName, password, port, privateKey);
        this.timeout = timeout;
    }

    protected Session getSession() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(getUserName(), getHost(), getPort());
        if (getPrivateKey() != null && getPassword() != null) {
            jsch.addIdentity(null, getPrivateKey().getBytes(), null, getPassword().getBytes());
            log.info("identity added ");
        } else if (getPrivateKey() != null) {
            jsch.addIdentity(null, getPrivateKey().getBytes(), null, null);
            log.info("identity added ");
        } else if (getPassword() != null) {
            session.setPassword(getPassword());
            log.info("Password");
        }
        session.setTimeout(getTimeout());
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        config.put("PreferredAuthentications", "publickey,keyboard-interactive,password");
        session.setConfig(config);
        return session;
    }
}
