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

package com.vmware.mangle.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.vmware.mangle.exception.MangleException;
import com.vmware.mangle.metrics.models.QueryEvent;
import com.vmware.mangle.metrics.models.QueryResult;
import com.vmware.mangle.metrics.models.Service;
import com.vmware.mangle.metrics.models.Timeseries;
import com.vmware.mangle.metrics.models.WavefrontConnectionProperties;
import com.vmware.mangle.metrics.models.WavefrontEvent;
import com.vmware.mangle.metrics.models.WavefrontEventResponse;
import com.vmware.mangle.resiliency.services.ResiliencyCalculatorTest;

/**
 * Mock data for DTOs
 * 
 * @author ranjans
 */
public class DtoMockData {

    private DtoMockData() {

    }

    public static WavefrontEventResponse getWavefrontEventResponse() {
        WavefrontEventResponse wavefrontEventResponse = new WavefrontEventResponse();
        wavefrontEventResponse.setGranularity(1);
        List<WavefrontEvent> events = getWavefrontEventList();
        wavefrontEventResponse.setEvents(events);
        return wavefrontEventResponse;
    }

    public static List<WavefrontEvent> getWavefrontEventList() {
        List<WavefrontEvent> events = new ArrayList<>();
        events.add(getWavefrontEvent());
        return events;
    }

    public static WavefrontEvent getWavefrontEvent() {
        WavefrontEvent wavefrontEvent = new WavefrontEvent();
        wavefrontEvent.setName(MockDataConstants.ANY_STR);
        wavefrontEvent.setStart(new DateTime().minusDays(7).getMillis() / 1000);
        wavefrontEvent.setEnd(new DateTime().getMillis() / 1000);
        return wavefrontEvent;
    }

    public static Service getService() {
        Service serviceMock = new Service();
        serviceMock.setService(MockDataConstants.ANY_STR);
        serviceMock.setServiceFamily(MockDataConstants.ANY_STR);
        return serviceMock;
    }

    public static QueryResult getQueryResultMockData() {
        QueryResult queryResult = new QueryResult();
        queryResult.setName(MockDataConstants.ANY_STR);
        queryResult.setGranularity(0);
        queryResult.setQuery(MockDataConstants.ANY_STR);
        QueryEvent queryEvent = new QueryEvent();
        queryEvent.setName(MockDataConstants.ANY_STR);
        queryEvent.setStart(0);
        queryEvent.setEnd(0);
        queryEvent.setEphemeral(true);
        String[] hosts = { MockDataConstants.ANY_STR };
        queryEvent.setHosts(hosts);
        queryEvent.setSummarized(0);
        queryEvent.setTags(MockDataConstants.ANY_STR);
        QueryEvent[] events = { queryEvent };
        queryResult.setEvents(events);
        queryResult.setStats(null);
        Timeseries timeseries = new Timeseries();
        Double[][] data = new Double[1][2];
        data[0][0] = 1111111d;
        data[0][1] = 1.0;
        timeseries.setData(data);
        timeseries.setHost(MockDataConstants.ANY_STR);
        timeseries.setLabel(MockDataConstants.ANY_STR);
        Map<String, String> tags = new HashMap<>();
        tags.put(MockDataConstants.ANY_STR, MockDataConstants.ANY_STR);
        timeseries.setTags(tags);
        Timeseries[] timeseriess = { timeseries };
        queryResult.setTimeseries(timeseriess);
        queryResult.setWarnings(MockDataConstants.ANY_STR);
        return queryResult;
    }

    public static WavefrontConnectionProperties getWavefrontConnectionProperties() throws MangleException {
        WavefrontConnectionProperties wavefrontConnectionProperties;
        Yaml yaml = new Yaml(new Constructor(WavefrontConnectionProperties.class));
        try {
            FileInputStream fis = new FileInputStream(
                    ResiliencyCalculatorTest.class.getClassLoader().getResource("wavefront-properties.yaml").getPath());
            wavefrontConnectionProperties = yaml.load(fis);
        } catch (FileNotFoundException e) {
            throw new MangleException(e.getMessage());
        }
        return wavefrontConnectionProperties;
    }

}
