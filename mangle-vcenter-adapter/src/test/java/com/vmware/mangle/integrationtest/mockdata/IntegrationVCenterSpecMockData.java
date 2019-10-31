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

package com.vmware.mangle.integrationtest.mockdata;

import java.util.Map;

import com.vmware.mangle.model.VCenterSpec;
import com.vmware.mangle.utils.ReadProperty;

/**
 * @author Chethan C(chetanc)
 */
public class IntegrationVCenterSpecMockData {

    private String mockDataFile = "src/test/resources/mockdata.properties";
    Map<String, String> mockData;

    private String vcHostName;
    private String vcAdapterPort;
    private String vcHostUserName;
    private String vcHostPassword;
    private String vcHostVmName;
    private String vcHostvmNicId;

    private String vcAdapterHost;
    private String vcAdapterUsername;
    private String vcAdapterPassword;

    public IntegrationVCenterSpecMockData() {
        mockData = ReadProperty.readPropertiesAsMap(mockDataFile);
        vcAdapterHost = mockData.get("vcAdapterHost");
        vcAdapterPort = mockData.get("vcAdapterPort");
        vcAdapterUsername = mockData.get("vcAdapterUsername");
        vcAdapterPassword = mockData.get("vcAdapterPassword");
        vcHostName = mockData.get("vcHostName");
        vcHostUserName = mockData.get("vcHostUsername");
        vcHostPassword = mockData.get("vcHostPassword");
        vcHostVmName = mockData.get("vcHostVmName");
        vcHostvmNicId = mockData.get("vcHostVmNicId");
    }

    public VCenterSpec getVCenterSpecForIntegration() {
        VCenterSpec vCenterSpec = new VCenterSpec();
        vCenterSpec.setVcPassword(vcHostPassword);
        vCenterSpec.setVcUsername(vcHostUserName);
        vCenterSpec.setVcServerUrl(vcHostName);
        return vCenterSpec;
    }

    public String getVMName() {
        return vcHostVmName;
    }

    public String getVMNicId() {
        return vcHostvmNicId;
    }

    public String getVcAdapterHost() {
        return vcAdapterHost;
    }

    public String getVcAdapterUsername() {
        return vcAdapterUsername;
    }

    public String getVcAdapterPassword() {
        return vcAdapterPassword;
    }

    public String getVcAdapterPort() {
        return vcAdapterPort;
    }
}
