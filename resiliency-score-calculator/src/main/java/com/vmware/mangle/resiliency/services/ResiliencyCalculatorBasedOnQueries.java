package com.vmware.mangle.resiliency.services;

import com.vmware.mangle.metrics.models.ResiliencyScoreProperties;
import com.vmware.mangle.metrics.models.ServiceFamily;
import com.vmware.mangle.metrics.models.Services;

import com.vmware.mangle.resiliency.score.utils.ResiliencyScoreUtils;
import lombok.extern.log4j.Log4j2;


@Log4j2
public class ResiliencyCalculatorBasedOnQueries implements Runnable {
    private long startTime;
    private long endTime;
    private ResiliencyScoreProperties properties;
    private ServiceFamily serviceFamily;
    private Services service;

    public ResiliencyCalculatorBasedOnQueries(ResiliencyScoreProperties properties, ServiceFamily serviceFamily,
            Services service, long startTime, long endTime) {
        this.properties = properties;
        this.serviceFamily = serviceFamily;
        this.service = service;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public void run() {
        log.info("Calculating Resiliency score for the service: " + service.getName());
        ResiliencyScoreUtils
                resiliencyScoreUtils = new ResiliencyScoreUtils(properties, serviceFamily, service, startTime, endTime);
        resiliencyScoreUtils.calculateResiliencyScore();
    }


}
