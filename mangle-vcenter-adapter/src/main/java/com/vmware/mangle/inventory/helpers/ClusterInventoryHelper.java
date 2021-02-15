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

import static com.vmware.mangle.utils.VCenterAPIEndpoints.REST_VC_CLUSTER;
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
import com.vmware.mangle.model.Cluster;
import com.vmware.mangle.model.ResourceList;
import com.vmware.mangle.model.enums.VCenterResources;
import com.vmware.mangle.utils.VCenterAPIEndpoints;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author chetanc
 */
@Component
public class ClusterInventoryHelper {
    private DCInventoryHelper dcInventoryHelper;

    @Autowired
    public ClusterInventoryHelper(DCInventoryHelper dcInventoryHelper) {
        this.dcInventoryHelper = dcInventoryHelper;
    }

    public List<Cluster> getAllCluster(VCenterClient client, String dcName) throws MangleException {
        String url = REST_VC_CLUSTER;
        if (StringUtils.hasText(dcName)) {
            String dcId = dcInventoryHelper.getDataCenterId(client, dcName);
            url = url + URL_QUERY_SEPARATOR + VCenterAPIEndpoints.addDCFilter(dcId);
        }
        ResponseEntity<?> responseEntity = client.get(url, Constants.CLUSTER_RESOURCE_LIST);

        if (responseEntity == null) {
            throw new MangleException(
                    String.format(ErrorConstants.VCENTER_OBJECT_COULD_NOT_FETCH, VCenterResources.CLUSTER));
        }
        ResourceList<Cluster> resourceList = (ResourceList<Cluster>) responseEntity.getBody();
        return resourceList == null ? new ArrayList<>() : resourceList.getValue();
    }

    public String getClusterId(VCenterClient client, String clusterName, String dcName) throws MangleException {
        List<Cluster> clusters = getAllCluster(client, dcName).stream()
                .filter(cluster -> cluster.getName().equals(clusterName)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(clusters)) {
            throw new MangleException(
                    String.format(ErrorConstants.RESOURCE_NOT_FOUND, VCenterResources.CLUSTER, clusterName));
        }
        if (!CollectionUtils.isEmpty(clusters) && clusters.size() > 1) {
            throw new MangleException(
                    String.format(ErrorConstants.MULTIPLE_RESOURCES_FOUND, VCenterResources.CLUSTER, clusterName));
        }
        return clusters.get(0).getCluster();
    }
}
