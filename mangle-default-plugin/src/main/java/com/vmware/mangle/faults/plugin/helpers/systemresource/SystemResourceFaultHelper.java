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

package com.vmware.mangle.faults.plugin.helpers.systemresource;

import java.util.List;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.tasks.SupportScriptInfo;
import com.vmware.mangle.cassandra.model.tasks.commands.CommandInfo;
import com.vmware.mangle.utils.ICommandExecutor;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 * @author jayasankarr
 *
 */

public abstract class SystemResourceFaultHelper {


    public abstract ICommandExecutor getExecutor(CommandExecutionFaultSpec faultSpec) throws MangleException;

    public abstract List<CommandInfo> getInjectionCommandInfoList(CommandExecutionFaultSpec spec)
            throws MangleException;

    public abstract List<CommandInfo> getRemediationcommandInfoList(CommandExecutionFaultSpec spec)
            throws MangleException;

    public abstract List<SupportScriptInfo> getFaultInjectionScripts(CommandExecutionFaultSpec spec);

    public abstract void checkTaskSpecificPrerequisites(CommandExecutionFaultSpec spec) throws MangleException;
}
