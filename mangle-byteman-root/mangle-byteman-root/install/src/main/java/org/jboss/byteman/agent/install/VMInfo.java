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

package org.jboss.byteman.agent.install;

/**
 * Auxiliary class used by Install to provide clients with ids and display names of attachable JVMs.
 *
 * @author Andrew Dinn
 * @author hkilari
 */
public class VMInfo {
    private String id;
    private String displayName;

    public VMInfo(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }
}
