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

package com.vmware.mangle.java.agent.faults.helpers;

/**
 * @author hkilari
 *
 */
public enum JavaFaultName {
    INJECTCPULOAD("Name:injectCPULoad Args:timeOutInMilliSeconds,load(in %) (ex: 10000, 10 (10% cpu load))"),
    REMEDIATECPULOAD("Name:remediateCPULoad Args:Not Required"),
    INJECTMEMORYLOAD(
            "Name:injectMemoryLoad Args:timeOutInMilliSeconds,load(in %) (ex: 10000, 10 (10% java heap memory to fill))"),
    REMEDIATEMEMORYLOAD("Name:remediateMemoryLoad Args:Not Required"),
    INJECTDISKIOLOAD("Name:injectDiskIOLoad Args:timeOutInMilliSeconds"),
    REMEDIATEDISKIOLOAD("Name:remediateDiskIOLoad Args:Not Required"),
    INJECTFILEHANDLERLEAK("Name:injectFileHandlerLeak Args:timeOutInMilliSeconds"),
    REMEDIATEFILEHANDLERLEAK("Name:remediateFileHandlerLeak Args:Not Required"),
    INJECTASPECTFAULT(
            "Name:injectAspectFault Args:id(UUID string),delayInterval(Long Value),exceptionName(ExceptionClass name along with Package),"
                    + "exceptionMessage(String value),enableThreadInterrupt(Boolean Value),servicesString(Service URI Strings seperated using '#') "
                    + "(ex: xx-xx-xx, 100L,null,null,flase,null))"),
    REMEDIATEASPECTFAULT("Name:remediateAspectFault Args:AspectID(UUID value)"),
    VERIFYMEMORYLOAD("Name:verifyMemoryLoad "),
    VERIFYCPULOAD("Name:verifyCPULoad "),
    CLEANFAULTBEANS("Name:cleanFaultBeans Args:Not Required");
    String faultName;

    JavaFaultName(String faultName) {
        this.faultName = faultName;
    }

    public String getFaultName() {
        return faultName;
    }
}
