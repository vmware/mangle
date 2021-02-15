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

package com.vmware.mangle.utils.notification;

import allbegray.slack.SlackClientFactory;
import allbegray.slack.webapi.SlackWebApiClient;
import lombok.extern.log4j.Log4j2;

import com.vmware.mangle.cassandra.model.slack.Notifier;
import com.vmware.mangle.utils.clients.endpoint.EndpointClient;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * SlackClient is used to send notification to slack channel.
 *
 * @author kumargautam
 */
@Log4j2
public class SlackClient implements EndpointClient {

    private Notifier notification;

    public SlackClient(Notifier notifier) {
        this.notification = notifier;
    }

    public SlackWebApiClient getClient() {
        return SlackClientFactory.createWebApiClient(notification.getSlackInfo().getToken());
    }

    @Override
    public boolean testConnection() throws MangleException {
        SlackWebApiClient client = null;
        try {
            client = getClient();
            client.auth();
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new MangleException(ErrorCode.NOTIFICATION_CONNECTION_FAILED, e.getMessage());
        } finally {
            shutdown(client);
        }
    }

    public void shutdown(SlackWebApiClient client) {
        try {
            if (client != null) {
                client.shutdown();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
