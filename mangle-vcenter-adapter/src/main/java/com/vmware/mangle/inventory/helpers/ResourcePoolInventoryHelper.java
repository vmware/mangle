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

import static com.vmware.mangle.utils.VCenterAPIEndpoints.REST_VC_RESOURCE_POOL;
import static com.vmware.mangle.utils.constants.Constants.URL_PARAM_SEPARATOR;
import static com.vmware.mangle.utils.constants.Constants.URL_QUERY_SEPARATOR;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.vmware.mangle.adapter.VCenterClient;
import com.vmware.mangle.model.ResourceList;
import com.vmware.mangle.model.ResourcePool;
import com.vmware.mangle.model.enums.VCenterResources;
import com.vmware.mangle.utils.VCenterAPIEndpoints;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author chetanc
 */
@Component
public class ResourcePoolInventoryHelper {
    private ClusterInventoryHelper clusterInventoryHelper;
    private DCInventoryHelper dcInventoryHelper;

    @Autowired
    public ResourcePoolInventoryHelper(ClusterInventoryHelper clusterInventoryHelper, DCInventoryHelper dcInventoryHelper) {
        this.clusterInventoryHelper = clusterInventoryHelper;
        this.dcInventoryHelper = dcInventoryHelper;
    }

    public List<ResourcePool> getAllResourcePool(VCenterClient client, String dcName, String clusterName)
            throws MangleException {
        String url = REST_VC_RESOURCE_POOL;
        boolean isQueryAdded = false;
        String queryParam = "";

        if (StringUtils.hasText(dcName)) {
            String dcId = dcInventoryHelper.getDataCenterId(client, dcName);
            queryParam = VCenterAPIEndpoints.addDCFilter(dcId);
            isQueryAdded = true;
        }

        if (StringUtils.hasText(clusterName)) {
            String clusterId = clusterInventoryHelper.getClusterId(client, clusterName, dcName);
            if (isQueryAdded) {
                queryParam += URL_PARAM_SEPARATOR + VCenterAPIEndpoints.addClusterFilter(clusterId);
            } else {
                queryParam = VCenterAPIEndpoints.addClusterFilter(clusterId);
            }
            isQueryAdded = true;
        }

        if (isQueryAdded) {
            url += URL_QUERY_SEPARATOR + queryParam;
        }

        ResponseEntity<?> responseEntity = client.get(url, Constants.RSP_RESOURCE_LIST);

        if (responseEntity == null) {
            throw new MangleException(
                    String.format(ErrorConstants.VCENTER_OBJECT_COULD_NOT_FETCH, VCenterResources.RESOURCE_POOL));
        }
        ResourceList<ResourcePool> resourceList = (ResourceList<ResourcePool>) responseEntity.getBody();
        return resourceList == null ? new ArrayList<>() : resourceList.getValue();
    }

    public String getResourcePoolId(VCenterClient client, String clusterName, String dcName,
            String resourcePoolName) throws MangleException {
        List<ResourcePool> rsps = getAllResourcePool(client, dcName, clusterName).stream()
                .filter(resourcePool -> resourcePool.getName().equals(resourcePoolName)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(rsps)) {
            throw new MangleException(String.format(ErrorConstants.RESOURCE_POOL_NOT_FOUND, resourcePoolName));
        }
        if (!CollectionUtils.isEmpty(rsps) && rsps.size() > 1) {
            throw new MangleException(String.format(ErrorConstants.MULTIPLE_RESOURCES_FOUND,
                    VCenterResources.RESOURCE_POOL, clusterName));
        }
        return rsps.get(0).getResource_pool();
    }
}
