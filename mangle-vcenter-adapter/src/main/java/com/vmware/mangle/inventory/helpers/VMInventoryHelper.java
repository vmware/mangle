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

import static com.vmware.mangle.utils.VCenterAPIEndpoints.REST_VC_VM;
import static com.vmware.mangle.utils.VCenterAPIEndpoints.REST_VC_VM_DISK;
import static com.vmware.mangle.utils.VCenterAPIEndpoints.REST_VC_VM_NETWORK;
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
import com.vmware.mangle.model.VM;
import com.vmware.mangle.model.VMDisk;
import com.vmware.mangle.model.VMNic;
import com.vmware.mangle.model.enums.FolderType;
import com.vmware.mangle.model.enums.VCenterResources;
import com.vmware.mangle.utils.VCenterAPIEndpoints;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author Chethan C(chetanc)
 */
@Component
public class VMInventoryHelper {
    private ClusterInventoryHelper clusterInventoryHelper;
    private HostInventoryHelper hostInventoryHelper;
    private ResourcePoolInventoryHelper rsPoolInventoryHelper;
    private DCInventoryHelper dcInventoryHelper;

    @Autowired
    public VMInventoryHelper(ClusterInventoryHelper clusterInventoryHelper, HostInventoryHelper hostInventoryHelper,
            ResourcePoolInventoryHelper rsPoolInventoryHelper, DCInventoryHelper dcInventoryHelper) {
        this.clusterInventoryHelper = clusterInventoryHelper;
        this.hostInventoryHelper = hostInventoryHelper;
        this.rsPoolInventoryHelper = rsPoolInventoryHelper;
        this.dcInventoryHelper = dcInventoryHelper;
    }

    /**
     * Queries the list of VM that are provisioned/managed by the vcenter
     *
     * @param client
     * @param dcName
     * @throws MangleException
     * @return: list of VM Objects in the VC
     */
    public List<VM> getAllVMs(VCenterClient client, String dcName) throws MangleException {
        ResponseEntity<ResourceList> responseEntity =
                (ResponseEntity<ResourceList>) client.get(REST_VC_VM, ResourceList.class);
        if (responseEntity == null) {
            throw new MangleException(
                    String.format(ErrorConstants.VCENTER_OBJECT_COULD_NOT_FETCH, VCenterResources.VM));
        }
        ResourceList<VM> resourceList = responseEntity.getBody();
        return InventoryHelperUtil.convertLinkedHashMapToObjectList(resourceList.getValue(), VM.class);
    }


    public List<VM> getAllVM(VCenterClient client, String host, String clusterName, String dcName, String folderName,
            String resourcePoolName) throws MangleException {
        String url = REST_VC_VM;
        boolean isQueryAdded = false;

        String queryParam = "";
        if (StringUtils.hasText(host) && !host.equals("null")) {
            String hostId = hostInventoryHelper.getHostId(client, host, "", "", "");
            queryParam = VCenterAPIEndpoints.addHostFilter(hostId);
            isQueryAdded = true;
        }

        if (StringUtils.hasText(dcName) && !dcName.equals("null")) {
            String dcId = dcInventoryHelper.getDataCenterId(client, dcName);
            queryParam = VCenterAPIEndpoints.addDCFilter(dcId);
            isQueryAdded = true;
        }

        if (StringUtils.hasText(clusterName) && !clusterName.equals("null")) {
            String clusterId = clusterInventoryHelper.getClusterId(client, clusterName, dcName);
            if (isQueryAdded) {
                queryParam += URL_PARAM_SEPARATOR + VCenterAPIEndpoints.addClusterFilter(clusterId);
            } else {
                queryParam = VCenterAPIEndpoints.addClusterFilter(clusterId);
            }
            isQueryAdded = true;
        }

        if (StringUtils.hasText(folderName) && !folderName.equals("null")) {
            String folderId = FolderInventoryHelper.getFolderId(client, folderName, FolderType.VIRTUAL_MACHINE.name());
            if (isQueryAdded) {
                queryParam += URL_PARAM_SEPARATOR + VCenterAPIEndpoints.addFolderFilter(folderId);
            } else {
                queryParam = VCenterAPIEndpoints.addClusterFilter(folderId);
            }
            isQueryAdded = true;
        }

        if (StringUtils.hasText(resourcePoolName) && !resourcePoolName.equals("null")) {
            String folderId = rsPoolInventoryHelper.getResourcePoolId(client, clusterName, dcName, resourcePoolName);
            if (isQueryAdded) {
                queryParam += URL_PARAM_SEPARATOR + VCenterAPIEndpoints.addResourcePoolFilter(folderId);
            } else {
                queryParam = VCenterAPIEndpoints.addResourcePoolFilter(folderId);
            }
            isQueryAdded = true;
        }


        if (isQueryAdded) {
            url += URL_QUERY_SEPARATOR + queryParam;
        }

        ResponseEntity<?> responseEntity = client.get(url, Constants.VM_RESOURCE_LIST);

        if (responseEntity == null) {
            throw new MangleException(
                    String.format(ErrorConstants.VCENTER_OBJECT_COULD_NOT_FETCH, VCenterResources.VM));
        }
        ResourceList<VM> resourceList = (ResourceList<VM>) responseEntity.getBody();
        return resourceList == null ? new ArrayList<>() : resourceList.getValue();
    }

    /**
     * Resolves the VM ID for the given VM name
     *
     * @param client
     * @param vmname
     * @param dcName
     * @throws MangleException
     * @return: VM ID for the given VM name
     */
    public String getVMID(VCenterClient client, String vmname, String dcName) throws MangleException {
        for (VM vm : getAllVMs(client, dcName)) {
            if (vm.getName().equals(vmname)) {
                return vm.getVm();
            }
        }
        throw new MangleException(String.format(ErrorConstants.RESOURCE_NOT_FOUND, VCenterResources.VM, vmname));
    }

    public VM getVMByName(VCenterClient client, String vmName, String host, String clusterName, String dcName,
            String folderName, String resourcePoolName) throws MangleException {
        List<VM> vms = getAllVM(client, host, clusterName, dcName, folderName, resourcePoolName).stream()
                .filter(hostObject -> hostObject.getName().equals(vmName)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(vms)) {
            throw new MangleException(String.format(ErrorConstants.RESOURCE_NOT_FOUND, VCenterResources.VM, vmName));
        }
        if (!CollectionUtils.isEmpty(vms) && vms.size() > 1) {
            throw new MangleException(
                    String.format(ErrorConstants.MULTIPLE_RESOURCES_FOUND, VCenterResources.VM, host));
        }
        return vms.get(0);
    }

    public String getVMId(VCenterClient client, String vmName, String host, String clusterName, String dcName,
            String folderName, String resourcePoolName) throws MangleException {
        VM vm = getVMByName(client, vmName, host, clusterName, dcName, folderName, resourcePoolName);
        return vm.getVm();
    }

    public List<VMNic> getEthernetConnectedToVM(VCenterClient client, String vmId) throws MangleException {
        String urlSuffix = String.format(REST_VC_VM_NETWORK, vmId);
        ResponseEntity<ResourceList> responseEntity =
                (ResponseEntity<ResourceList>) client.get(urlSuffix, Constants.VM_ETHERNET_LIST);
        if (responseEntity == null) {
            throw new MangleException(
                    String.format(ErrorConstants.VCENTER_OBJECT_COULD_NOT_FETCH, VCenterResources.VM_NIC));
        }
        ResourceList<VMNic> resourceList = (ResourceList<VMNic>) responseEntity.getBody();
        return resourceList == null ? new ArrayList<>() : resourceList.getValue();
    }

    public List<VMDisk> getDiskConnectedToVM(VCenterClient client, String vmId) throws MangleException {
        String urlSuffix = String.format(REST_VC_VM_DISK, vmId);
        ResponseEntity<ResourceList> responseEntity =
                (ResponseEntity<ResourceList>) client.get(urlSuffix, Constants.VM_DISK_LIST);
        if (responseEntity == null) {
            throw new MangleException(
                    String.format(ErrorConstants.VCENTER_OBJECT_COULD_NOT_FETCH, VCenterResources.VM_DISK));
        }
        ResourceList<VMDisk> resourceList = (ResourceList<VMDisk>) responseEntity.getBody();
        return resourceList == null ? new ArrayList<>() : resourceList.getValue();
    }
}
