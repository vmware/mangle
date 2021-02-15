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

import static com.vmware.mangle.utils.VCenterAPIEndpoints.REST_VC_FOLDER;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.vmware.mangle.adapter.VCenterClient;
import com.vmware.mangle.model.Folder;
import com.vmware.mangle.model.ResourceList;
import com.vmware.mangle.model.enums.VCenterResources;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author chetanc
 */
@Component
public class FolderInventoryHelper {
    private FolderInventoryHelper() {
    }

    public static List<Folder> getAllFolder(VCenterClient client, String folderType) throws MangleException {
        if (!StringUtils.hasText(folderType)) {
            throw new MangleException(ErrorConstants.FOLDER_TYPE_NOT_PROVIDED);
        }
        ResponseEntity<?> responseEntity =
                client.get(String.format(REST_VC_FOLDER, folderType), Constants.FOLDER_RESOURCE_LIST);
        if (responseEntity == null) {
            throw new MangleException(
                    String.format(ErrorConstants.VCENTER_OBJECT_COULD_NOT_FETCH, VCenterResources.FOLDER));
        }
        ResourceList<Folder> resourceList = (ResourceList<Folder>) responseEntity.getBody();
        return resourceList == null ? new ArrayList<>() : resourceList.getValue();
    }

    public static String getFolderId(VCenterClient client, String folderName, String folderType)
            throws MangleException {
        List<Folder> dataCenters = getAllFolder(client, folderType).stream()
                .filter(folder -> folder.getName().equals(folderName)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(dataCenters)) {
            throw new MangleException(
                    String.format(ErrorConstants.RESOURCE_NOT_FOUND, VCenterResources.FOLDER, folderName));
        }
        if (!CollectionUtils.isEmpty(dataCenters) && dataCenters.size() > 1) {
            throw new MangleException(
                    String.format(ErrorConstants.MULTIPLE_RESOURCES_FOUND, VCenterResources.FOLDER, folderName));
        }
        return dataCenters.get(0).getFolder();
    }
}
