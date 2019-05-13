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

package com.vmware.mangle.adapter;

import static com.vmware.mangle.utils.VCenterAPIEndpoints.REST_VC_VM;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.http.ResponseEntity;

import com.vmware.mangle.model.ResourceList;
import com.vmware.mangle.model.VM;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.restclienttemplate.RestTemplateWrapper;

/**
 ** @author Chethan C(chetanc)
 *
 */
public class InventoryHelper {
    private InventoryHelper() {
    }

    /**
     * Queries the list of VM that are provisioned/managed by the vcenter
     *
     * @param client
     * @param dcName
     * @throws MangleException
     * @return: list of VM Objects in the VC
     */
    public static List<VM> getAllVMs(VCenterClient client, String dcName) throws MangleException {
        ResponseEntity<ResourceList> responseEntity =
                (ResponseEntity<ResourceList>) client.get(REST_VC_VM, ResourceList.class);
        if (responseEntity == null) {
            throw new MangleException(String.format(ErrorConstants.VCENTER_OBJECT_COULD_NOT_FETCH, Constants.VM));
        }
        ResourceList<VM> resouceList = responseEntity.getBody();
        return convertLinkedHashMapToObjectList(resouceList.getValue(), VM.class);
    }

    /*
     * /** Queries the list of host that are managed by the vCenter
     *
     * @param client
     *
     * @throws MangleException
     *
     * @return: list of host objects in the VC
     *//*
         * public static List<Host> getAllHosts(VCenterClient client) throws
         * MangleException {
         *
         * @SuppressWarnings("unchecked") ResourceList<Host> resouceList =
         * (ResourceList<Host>) client.get(REST_VC_HOST, ResourceList.class).getBody();
         * return convertLinkedHashMapToObjectList(resouceList.getValue(), Host.class);
         * }
         */

    /**
     * Resolves the VM ID for the given VM name
     *
     * @param client
     * @param vmname
     * @param dcName
     * @throws MangleException
     * @return: VM ID for the given VM name
     */
    public static String getVMID(VCenterClient client, String vmname, String dcName) throws MangleException {
        for (VM vm : getAllVMs(client, dcName)) {
            if (vm.getName().equals(vmname)) {
                return vm.getVm();
            }
        }
        throw new MangleException(String.format(ErrorConstants.VM_NOT_FOUND, vmname));
    }

    /**
     * Converts response value object to the object of the given class
     *
     * @param object:
     *            source object
     * @param tClass:
     *            resulting object class
     * @param <T>
     * @return: object of the resulting class
     */
    public static <T> T convertLinkedHashMapToObject(Object object, Class<T> tClass) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(object, tClass);
    }

    /**
     * Converts response value object to the list of object of the given class
     *
     * @param object:
     *            source object
     * @param tClass:
     *            resulting object class
     * @param <T>
     * @return: list of objects of the resulting class
     */
    public static <T> List<T> convertLinkedHashMapToObjectList(Object object, Class<T> tClass) throws MangleException {
        ObjectMapper mapper = new ObjectMapper();
        CollectionType listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, tClass);
        List<T> listOfObjects;
        try {
            listOfObjects = mapper.readValue(RestTemplateWrapper.objectToJson(object), listType);
        } catch (IOException e) {
            throw new MangleException(
                    "Converting Linked Hashmap to List of Resources id Failed with Exception " + e.getMessage());
        }
        return listOfObjects;
    }
}
