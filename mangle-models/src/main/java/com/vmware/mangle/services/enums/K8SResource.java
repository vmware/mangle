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

package com.vmware.mangle.services.enums;

/**
 * Enum to store all the K8S resources types
 *
 * @author bkaranam
 */
public enum K8SResource {
    POD, SERVICE, NODE, DEPLOYMENT, STATEFULSET, SECRET, DAEMONSET, CONFIGMAP, JOB, REPLICASET, REPLICATIONCONTROLLER, PV, PVC;

    public String getValue() {
        return this.name().toLowerCase();
    }
}
