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

package com.vmware.mangle.cassandra.model.tasks.commands;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Transient;

import com.vmware.mangle.cassandra.model.tasks.Fault;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;

/**
 * @author hkilari
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
@SuppressWarnings("squid:S1948")
public class CommandExecutionFault extends Fault {

    private static final long serialVersionUID = 1L;
    protected List<CommandInfo> injectionCommandInfoList;
    protected List<CommandInfo> remediationCommandInfoList;
    protected List<CommandInfo> testMachinePreperationCommandInfoList;
    protected List<CommandInfo> cleanUpCommandInfoList;
    protected List<SupportScriptInfo> supportScriptInfo;
    @Transient
    protected Map<String, String> args;
    @Transient
    protected String baseDirectory;
}
