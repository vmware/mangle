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

package com.vmware.mangle.java.agent.faults;

import java.util.HashMap;
import java.util.Map;

import com.vmware.mangle.java.agent.faults.exception.FiaascoException;
import com.vmware.mangle.java.agent.utils.RuntimeUtils;
import com.vmware.mangle.java.agent.utils.ThreadUtils;

/**
 * @author hkilari
 *
 */
public class CpuFault extends AgentFault {

    public CpuFault(Map<String, String> faultArgs) throws FiaascoException {
        super(faultArgs);
    }

    @Override
    protected void populateFutureList() {
        int processors = RuntimeUtils.getNoOfProcessors();
        futureList.addAll(ThreadUtils.triggerCpuLoadGenerator(processors,
                getLong(getFaultArgs().get("timeOutInMilliSeconds")), getInt(getFaultArgs().get("load"))));
    }

    @Override
    public Map<String, String> getFaultSpecificParams() {
        Map<String, String> params = new HashMap<>();
        params.put("load", "Integer value Representing CPU Usage %. Range: 1 to 100 More than 100 Considered as 100.");
        if (isLongLasting()) {
            params.put("loadVariance",
                    "Integer Value. The value should satisfy load+/-loadVariance in the range 1 to 100.");
        }
        return params;
    }
}
