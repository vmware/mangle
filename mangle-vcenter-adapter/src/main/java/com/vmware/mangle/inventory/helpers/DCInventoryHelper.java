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

package com.vmware.mangle.inventory.helpers;

import static com.vmware.mangle.utils.VCenterAPIEndpoints.REST_VC_DATCENTER;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.adapter.VCenterClient;
import com.vmware.mangle.model.Datacenter;
import com.vmware.mangle.model.ResourceList;
import com.vmware.mangle.model.enums.VCenterResources;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author chetanc
 */
@Component
public class DCInventoryHelper {
    private DCInventoryHelper() {
    }

    public List<Datacenter> getAllDataCenter(VCenterClient client) throws MangleException {
        ResponseEntity<?> responseEntity = client.get(REST_VC_DATCENTER, Constants.DATA_CENTER_RESOURCE_LIST);
        if (responseEntity == null) {
            throw new MangleException(
                    String.format(ErrorConstants.VCENTER_OBJECT_COULD_NOT_FETCH, VCenterResources.DATA_CENTER));
        }
        ResourceList<Datacenter> resourceList = (ResourceList<Datacenter>) responseEntity.getBody();
        return resourceList == null ? new ArrayList<>() : resourceList.getValue();
    }

    public String getDataCenterId(VCenterClient client, String dcName) throws MangleException {
        List<Datacenter> dataCenters = getAllDataCenter(client).stream()
                .filter(cluster -> cluster.getName().equals(dcName)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(dataCenters)) {
            throw new MangleException(
                    String.format(ErrorConstants.RESOURCE_NOT_FOUND, VCenterResources.DATA_CENTER, dcName));
        }
        if (!CollectionUtils.isEmpty(dataCenters) && dataCenters.size() > 1) {
            throw new MangleException(
                    String.format(ErrorConstants.MULTIPLE_RESOURCES_FOUND, VCenterResources.DATA_CENTER, dcName));
        }
        return dataCenters.get(0).getDatacenter();
    }
}
