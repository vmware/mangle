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

package com.vmware.mangle.utils.helpers.notifiers;

import com.vmware.mangle.services.dto.FaultEventSpec;
import com.vmware.mangle.utils.constants.MetricProviderConstants;

/*
 * @author dbhat
 *
 */
public interface MetricProviderNotifier {
    public boolean sendEvent(FaultEventSpec faultEventSpec);

    public boolean closeEvent(FaultEventSpec faultEventInfo, String taskID, String taskExtension);

    default String getEventDetails(FaultEventSpec faultEventInfo) {
        StringBuilder details = new StringBuilder();
        details.append(MetricProviderConstants.START_TIME_TEXT + faultEventInfo.getFaultStartTime());
        details.append(MetricProviderConstants.SEPERATOR + MetricProviderConstants.END_TIME_TEXT
                + faultEventInfo.getFaultEndTime());
        details.append(MetricProviderConstants.SEPERATOR + faultEventInfo.getFaultDescription());
        details.append(MetricProviderConstants.NEW_LINE + MetricProviderConstants.STATUS_TEXT
                + faultEventInfo.getFaultStatus());
        return details.toString();
    }
}
