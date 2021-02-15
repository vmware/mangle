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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

import com.vmware.mangle.java.agent.faults.exception.FiaascoException;
import com.vmware.mangle.java.agent.utils.RuntimeUtils;
import com.vmware.mangle.java.agent.utils.ThreadUtils;

/**
 * @author hkilari
 *
 */
public abstract class AgentFault extends Thread {
    private static final Logger LOG = Logger.getLogger(AgentFault.class.getName());
    protected List<Future<?>> futureList;
    private String id;
    protected FaultInfo faultInfo;

    public boolean isLongLasting() {
        return faultInfo.isLongLasting();
    }

    public AgentFault(Map<String, String> faultArgs) throws FiaascoException {
        faultInfo = new FaultInfo();
        initFault();
        String id = (faultArgs.get("taskId") == null ? UUID.randomUUID().toString() : faultArgs.get("taskId"));
        setUid(id);
        faultInfo.setFaultName(faultArgs.get("faultName") + "-" + id.toString());
        faultInfo.setFaultArgs(faultArgs);
        faultInfo.setLongLasting(getBoolean(getFaultArgs().get("longLasting")));
        futureList = new ArrayList<Future<?>>();
        if (!verifyParams()) {
            failTask("Insufficeient or Wrong Params. Expected Params: " + getParams());
            throw new FiaascoException("Insufficeient or Wrong Params. Expected Params: " + getParams());
        }
    }

    public void remediateFault() {
        appendTaskActivity("Starting the Fault Remediation");
        if (isLongLasting()) {
            faultInfo.setLongLasting(false);
        }
        for (Future<?> future : futureList) {
            if (!(future.isDone()) && !(future.isCancelled())) {
                future.cancel(true);
            }
        }
        if (isGCRequired()) {
            LOG.info("Requesting GC as part of remediation");
            RuntimeUtils.runGc();
        }
        appendTaskActivity("Completed the Fault Remediation");
        faultInfo.setFaultStatus(FaultStatus.COMPLETED);
    }

    public void start() {
        appendTaskActivity("Starting the Fault Injection");
        faultInfo.setFaultStatus(FaultStatus.IN_PROGRESS);
        if (isLongLasting()) {
            new Thread() {
                public void run() {
                    long timeoutVariance = getLong(getFaultArgs().get("timeoutVariance"));
                    long gap = getLong(getFaultArgs().get("gap"));
                    long gapVariance = getLong(getFaultArgs().get("gapVariance"));
                    long counter = 0;
                    while (isLongLasting()) {
                        appendTaskActivity(" Starting Fault Invocation Instance: " + ++counter);
                        randomizedTaskSpecificData();
                        getFaultArgs().put("timeOut",
                                ThreadLocalRandom.current().nextLong(0, timeoutVariance + 1) + "");
                        populateFutureList();
                        long randomgap = ThreadLocalRandom.current().nextLong(gap - gapVariance, gap + gapVariance + 1);
                        try {
                            Thread.sleep(randomgap);
                        } catch (InterruptedException e) {
                            failTask(e.getMessage());
                        }
                        appendTaskActivity(" Completed Fault Invocation Instance: " + counter);
                    }
                }

            }.start();
        } else {
            populateFutureList();
            new Thread() {
                public void run() {
                    ThreadUtils.delayInMilliSeconds(getLong(getFaultArgs().get("timeOutInMilliSeconds")));
                    remediateFault();
                }
            }.start();
        }
    }

    private void randomizedTaskSpecificData() {
        for (String varianceParam : getFaultSpecificParams().keySet()) {
            if (varianceParam.contains("Variance")) {
                long varianceValue = getLong(getFaultArgs().get(varianceParam));
                String paramName = varianceParam.substring(0, varianceParam.indexOf("Variance"));
                long configValue = getLong(getFaultArgs().get(paramName));
                getFaultArgs().put(paramName, ThreadLocalRandom.current().nextLong(configValue - varianceValue,
                        configValue + varianceValue + 1) + "");
            }
        }
    }

    protected boolean getBoolean(String value) {
        return Boolean.parseBoolean(value);
    }

    protected int getInt(String value) {
        return Integer.parseInt(value);
    }

    protected long getLong(String value) {
        return Long.parseLong(value);
    }

    public String getFaultName() {
        return getFaultArgs().get("faultName");
    }

    public Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        params.put("longLasting", "");
        if (isLongLasting()) {
            params.put("timeoutVariance", "");
            params.put("gap", "");
            params.put("gapVariance", "");
        }
        params.put("timeOutInMilliSeconds", "");
        params.putAll(getFaultSpecificParams());
        return params;
    }

    public abstract Map<String, String> getFaultSpecificParams();

    public boolean verifyParams() {
        for (String param : getParams().keySet()) {
            if (null == getFaultArgs().get(param)) {
                failTask("Missing Required Parameter: " + param);
                return false;
            }
        }
        return true;
    }

    public String getUid() {
        return id;
    }

    public void setUid(String id) {
        this.id = id;
    }

    protected abstract void populateFutureList();

    public FaultInfo getFaultInfo() {
        return faultInfo;
    }

    protected Map<String, String> getFaultArgs() {
        return faultInfo.getFaultArgs();
    }

    private void failTask(String message) {
        faultInfo.setFaultStatus(FaultStatus.FAILED);
        LOG.severe(message);
        faultInfo.appendTaskActivity(message);
    }


    public void appendTaskActivity(String message) {
        faultInfo.appendTaskActivity(message);
        LOG.severe(message);
    }

    private void initFault() {
        LOG.info("Initializing the Fault");
        faultInfo.appendTaskActivity("Initializing the Fault");
        faultInfo.setFaultStatus(FaultStatus.INITIALIZING);
    }

    protected boolean isGCRequired() {
        return false;
    }
}
