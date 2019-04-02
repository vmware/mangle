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
import java.util.Properties;
import java.util.UUID;

import com.vmware.mangle.cassandra.model.security.ADAuthProviderDto;
import com.vmware.mangle.utils.ReadProperty;
import com.vmware.mangle.utils.constants.Constants;

/**
 *
 *
 * @author chetanc
 */
public class AuthProviderMockData {

    private String id1;
    private String id2;
    private String adUrl;
    private String newAdUrl;
    private String adDomain;

    public AuthProviderMockData() {
        Properties properties = ReadProperty.readProperty(Constants.MOCKDATA_FILE);

        this.id1 = UUID.randomUUID().toString();
        this.id2 = UUID.randomUUID().toString();
        this.adUrl = properties.getProperty("adUrl1");
        this.newAdUrl = properties.getProperty("adUrl2");
        this.adDomain = properties.getProperty("adDomain1");
    }

    public ADAuthProviderDto getADAuthProviderDto() {
        ADAuthProviderDto adAuthProviderDto = new ADAuthProviderDto();
        adAuthProviderDto.setId(id1);
        adAuthProviderDto.setAdDomain(adDomain);
        adAuthProviderDto.setAdUrl(adUrl);
        return adAuthProviderDto;
    }

    public ADAuthProviderDto getNewADAuthProviderDto() {
        ADAuthProviderDto adAuthProviderDto = new ADAuthProviderDto();
        adAuthProviderDto.setId(id2);
        adAuthProviderDto.setAdDomain(adDomain);
        adAuthProviderDto.setAdUrl(newAdUrl);
        return adAuthProviderDto;
    }

    public List<String> getListOfStrings() {
        List<String> lists = new ArrayList<>();
        lists.add(UUID.randomUUID().toString());
        lists.add(UUID.randomUUID().toString());
        lists.add(UUID.randomUUID().toString());
        return lists;
    }
}
