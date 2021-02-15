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

package com.vmware.mangle.utils.clients.vcenter;

import lombok.experimental.UtilityClass;
import org.springframework.http.ResponseEntity;

import com.vmware.mangle.model.response.VCenterOperationTaskQueryResponse;
import com.vmware.mangle.utils.constants.VCenterConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author hkilari
 *
 */
@SuppressWarnings("squid:S1118")
@UtilityClass
public class VCenterTaskUtils {

    public static ResponseEntity<VCenterOperationTaskQueryResponse> getTaskStatus(VCenterAdapterClient clientAdapter,
            String taskId) throws MangleException {
        ResponseEntity<VCenterOperationTaskQueryResponse> responeEntity = null;
        do {
            try {
                responeEntity = (ResponseEntity<VCenterOperationTaskQueryResponse>) clientAdapter.get(
                        String.format(VCenterConstants.TASK_STATUS, taskId), VCenterOperationTaskQueryResponse.class);
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new MangleException(e.getMessage(), ErrorCode.GENERIC_ERROR);
            }

        } while (responeEntity.getBody().getTaskStatus().equals(VCenterConstants.TASK_STATUS_TRIGGERED));

        return responeEntity;
    }
}
