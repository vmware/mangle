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

package com.vmware.mangle.services.mockdata;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.vmware.mangle.cassandra.model.Fault;
import com.vmware.mangle.model.FaultV0;

/**
 *
 *
 * @author chetanc
 */
public class FaultServiceMockData {
    private String endPointName = "ENDPOINT1";
    private String endPointName1 = "ENDPOINT2";
    private String endPointType = "VCENTER";
    private String endPointType1 = "DOCKER";
    private String faultName = "FAULT";
    private String faultName1 = "FAULT1";
    private String id = UUID.randomUUID().toString();
    private String id1 = UUID.randomUUID().toString();


    public Fault getDummyFault() {
        Fault fault = new Fault();
        fault.setEndpointName(endPointName);
        fault.setName(faultName);
        fault.setType(endPointType);
        fault.setId(id);
        return fault;
    }

    public Fault getDummyFault1() {
        Fault fault = new Fault();
        fault.setEndpointName(endPointName1);
        fault.setName(faultName1);
        fault.setType(endPointType1);
        fault.setId(id1);
        return fault;
    }

    public List<Fault> getDummyFaultsList() {
        List<Fault> list = new ArrayList<>();
        list.add(getDummyFault());
        list.add(getDummyFault1());
        return list;
    }

    public FaultV0 getDummyFaultV0() {
        FaultV0 fault = new FaultV0();
        fault.setName(faultName);
        fault.setType(endPointType);
        return fault;
    }

    public FaultV0 getDummyFault1V0() {
        FaultV0 fault = new FaultV0();
        fault.setName(faultName1);
        fault.setType(endPointType1);
        return fault;
    }

    public List<FaultV0> getDummyFaultsListV0() {
        List<FaultV0> list = new ArrayList<>();
        list.add(getDummyFaultV0());
        list.add(getDummyFault1V0());
        return list;
    }
}
