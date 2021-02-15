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

import static com.vmware.mangle.utils.VCenterAPIEndpoints.REST_VC_HOST;
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
import com.vmware.mangle.model.Host;
import com.vmware.mangle.model.ResourceList;
import com.vmware.mangle.model.enums.FolderType;
import com.vmware.mangle.model.enums.VCenterResources;
import com.vmware.mangle.utils.VCenterAPIEndpoints;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author chetanc
 */
@Component
public class HostInventoryHelper {

    private ClusterInventoryHelper clusterInventoryHelper;
    private DCInventoryHelper dcInventoryHelper;

    @Autowired
    public HostInventoryHelper(ClusterInventoryHelper clusterInventoryHelper, DCInventoryHelper dcInventoryHelper) {
        this.clusterInventoryHelper = clusterInventoryHelper;
        this.dcInventoryHelper = dcInventoryHelper;
    }

    public List<Host> getAllHost(VCenterClient client, String clusterName, String dcName, String folderName)
            throws MangleException {
        String url = REST_VC_HOST;
        boolean isQueryAdded = false;

        String queryParam = "";
        if (StringUtils.hasText(dcName)) {
            String dcId = dcInventoryHelper.getDataCenterId(client, dcName);
            queryParam = VCenterAPIEndpoints.addDCFilter(dcId);
            isQueryAdded = true;
        }

        if (StringUtils.hasText(clusterName)) {
            String clusterId = clusterInventoryHelper.getClusterId(client, clusterName, null);
            if (isQueryAdded) {
                queryParam += URL_PARAM_SEPARATOR + VCenterAPIEndpoints.addClusterFilter(clusterId);
            } else {
                queryParam = VCenterAPIEndpoints.addClusterFilter(clusterId);
            }
            isQueryAdded = true;
        }

        if (StringUtils.hasText(folderName)) {
            String folderId = FolderInventoryHelper.getFolderId(client, folderName, FolderType.HOST.name());
            if (isQueryAdded) {
                queryParam += URL_PARAM_SEPARATOR + VCenterAPIEndpoints.addFolderFilter(folderId);
            } else {
                queryParam = VCenterAPIEndpoints.addClusterFilter(folderId);
            }
            isQueryAdded = true;
        }


        if (isQueryAdded) {
            url += URL_QUERY_SEPARATOR + queryParam;
        }

        ResponseEntity<?> responseEntity = client.get(url, Constants.HOST_RESOURCE_LIST);

        if (responseEntity == null) {
            throw new MangleException(
                    String.format(ErrorConstants.VCENTER_OBJECT_COULD_NOT_FETCH, VCenterResources.HOST));
        }
        ResourceList<Host> resourceList = (ResourceList<Host>) responseEntity.getBody();
        return resourceList == null ? new ArrayList<>() : resourceList.getValue();
    }

    public Host getHostByName(VCenterClient client, String host, String clusterName, String dcName,
            String folderName) throws MangleException {
        List<Host> hosts = getAllHost(client, clusterName, dcName, folderName).stream()
                .filter(hostObject -> hostObject.getName().equals(host)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(hosts)) {
            throw new MangleException(String.format(ErrorConstants.RESOURCE_NOT_FOUND, VCenterResources.HOST, host));
        }
        if (!CollectionUtils.isEmpty(hosts) && hosts.size() > 1) {
            throw new MangleException(
                    String.format(ErrorConstants.MULTIPLE_RESOURCES_FOUND, VCenterResources.HOST, host));
        }
        return hosts.get(0);
    }

    public String getHostId(VCenterClient client, String hostName, String clusterName, String dcName,
            String folderName) throws MangleException {
        Host host = getHostByName(client, hostName, clusterName, dcName, folderName);
        return host.getHost();
    }
}
