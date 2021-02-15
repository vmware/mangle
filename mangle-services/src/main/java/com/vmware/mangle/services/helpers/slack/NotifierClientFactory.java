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

package com.vmware.mangle.services.helpers.slack;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.vmware.mangle.cassandra.model.slack.Notifier;
import com.vmware.mangle.cassandra.model.slack.NotifierType;
import com.vmware.mangle.utils.clients.endpoint.EndpointClient;
import com.vmware.mangle.utils.helpers.security.DecryptFields;
import com.vmware.mangle.utils.notification.SlackClient;

/**
 * Notifier Client Factory class.
 *
 * @author kumargautam
 */
@Component
public class NotifierClientFactory {

    public EndpointClient getNotificationClient(@NonNull Notifier notifier) {
        Notifier decryptNotification = (Notifier) DecryptFields.decrypt(notifier);
        if (notifier.getNotifierType().equals(NotifierType.SLACK)) {
            return new SlackClient(decryptNotification);
        } else {
            return null;
        }
    }
}