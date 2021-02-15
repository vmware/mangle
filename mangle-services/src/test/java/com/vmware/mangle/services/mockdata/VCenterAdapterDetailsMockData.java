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
import java.util.Arrays;
import java.util.List;

import com.vmware.mangle.cassandra.model.endpoint.VCenterAdapterDetails;
import com.vmware.mangle.cassandra.model.endpoint.VCenterAdapterProperties;

/**
 * @author chetanc
 */
public class VCenterAdapterDetailsMockData {
    private String adapterUrl = "https://10.10.10.10:8443/mangle-vc-adapter/";
    private String adapterUser = "dummyUser";
    private String adapterUserCreds = "dummyPassword";
    private String adapterName = "Adapter";
    private String adapterUrl1 = "https://10.10.10.11:8443/mangle-vc-adapter/";
    private String adapterUser1 = "dummyUser1";
    private String adapterUserCreds1 = "dummyPassword1";
    private String adapterName1 = "Adapter1";


    public VCenterAdapterDetails getVcaAdapterMockData() {
        VCenterAdapterDetails vCenterAdapterDetails = new VCenterAdapterDetails();
        vCenterAdapterDetails.setAdapterUrl(adapterUrl);
        vCenterAdapterDetails.setPassword(adapterUserCreds);
        vCenterAdapterDetails.setUsername(adapterUser);
        vCenterAdapterDetails.setName(adapterName);
        return vCenterAdapterDetails;
    }

    public VCenterAdapterDetails getNewVcaAdapterMockData() {
        VCenterAdapterDetails vCenterAdapterDetails = new VCenterAdapterDetails();
        vCenterAdapterDetails.setAdapterUrl(adapterUrl1);
        vCenterAdapterDetails.setPassword(adapterUserCreds1);
        vCenterAdapterDetails.setUsername(adapterUser1);
        vCenterAdapterDetails.setName(adapterName1);
        return vCenterAdapterDetails;
    }

    public List<VCenterAdapterDetails> getVcaAdaptersList() {
        return new ArrayList<>(Arrays.asList(getVcaAdapterMockData(), getNewVcaAdapterMockData()));
    }

    public VCenterAdapterProperties getVCenterAdapterProperties() {
        VCenterAdapterProperties vCenterAdapterProperties = new VCenterAdapterProperties();
        vCenterAdapterProperties.setPassword(adapterUserCreds);
        vCenterAdapterProperties.setUsername(adapterUser);
        vCenterAdapterProperties.setVcAdapterUrl(adapterUrl);
        return vCenterAdapterProperties;
    }

}
