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

package com.vmware.mangle.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vmware.mangle.cassandra.model.Fault;
import com.vmware.mangle.services.repository.FaultRepository;

/**
 *
 *
 * @author chetanc
 */
@Service
public class FaultService {

    FaultRepository faultRepository;

    @Autowired
    public FaultService(FaultRepository faultRepository) {
        this.faultRepository = faultRepository;
    }

    public Fault getFaultByName(String faultName) {
        return faultRepository.findByName(faultName);
    }

    public List<Fault> getFaultsByType(String faultType) {
        return faultRepository.findByType(faultType);
    }

    public List<Fault> getAllFaults() {
        return faultRepository.findAll();
    }

    public Fault createFault(Fault fault) {
        return faultRepository.save(fault);
    }

}
