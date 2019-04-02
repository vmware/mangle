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

package com.vmware.mangle.model.notifier;

import java.util.ArrayList;

import lombok.Data;

/**
 * @author dbhat
 *
 *         Fields required to be present while forming JSON body to send events to Datadog. Datadog
 *         events doesn't take endtime as inputs. It's always instant of time when the event is
 *         generated. By Default, the time is "now"
 *
 */
@Data
public class DatadogEventDto {
    private String title;
    private String text;
    private ArrayList<String> tags;
    private String alert_type;
    private String source_type_name;
}
