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

package com.vmware.mangle.task.framework.plugin.context;

import org.pf4j.ExtensionPoint;

/**
 * FIExtensionPoint is marker interface which is used to inform the JVM that the classes
 * implementing them will have extension behavior.
 *
 * @author kumargautam
 */
public interface FIExtensionPoint extends ExtensionPoint {

}
