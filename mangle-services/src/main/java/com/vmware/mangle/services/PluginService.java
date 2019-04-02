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

package com.vmware.mangle.services;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.log4j.Log4j2;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.vmware.mangle.services.constants.CommonConstants;
import com.vmware.mangle.task.framework.skeletons.ITaskHelper;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Service class for Plugin controller.
 *
 * @author kumargautam
 */
@Service
@Log4j2
public class PluginService {

    @Autowired
    private SpringPluginManager pluginManager;
    @Autowired
    private ApplicationContext appContext;

    public Map<String, Object> getExtensions() {
        List<PluginWrapper> startedPlugins = pluginManager.getStartedPlugins();
        List<String> extNames = new ArrayList<>();
        for (PluginWrapper plugin : startedPlugins) {
            log.info("Registering extensions of the plugin '{}' as beans", plugin.getPluginId());
            Set<String> extensionClassNames = pluginManager.getExtensionClassNames(plugin.getPluginId());
            for (String extensionClassName : extensionClassNames) {
                log.info("Register extension '{}' as bean", extensionClassName);
                extNames.add(extensionClassName);
            }
        }
        Map<String, Object> extMap = new HashMap<>();
        extMap.put("extensions", extNames);
        extMap.put("defaultExtensions", pluginManager.getExtensionClassNames(null));
        List<PluginWrapper> pluginWrapperList = pluginManager.getPlugins();
        List<Object> plugins = new ArrayList<>();
        pluginWrapperList.forEach(pluginWrapper -> plugins.add(pluginWrapper.getDescriptor()));
        extMap.put("plugins", plugins);
        return extMap;
    }

    public ITaskHelper getExtension(String extensionName) {
        List<ITaskHelper> tasks = pluginManager.getExtensions(ITaskHelper.class);
        ITaskHelper ext = null;
        for (ITaskHelper task : tasks) {
            if (extensionName.equals(task.getClass().getName())) {
                ext = task;
                break;
            }
        }
        if (null == ext) {
            Object taskObj = appContext
                    .getBean(extensionName.substring(extensionName.lastIndexOf('.') + 1, extensionName.length()));
            if (taskObj instanceof ITaskHelper) {
                ext = (ITaskHelper) taskObj;
            }
        }
        return ext;
    }

    public List<String> getExtensionNames() {
        List<PluginWrapper> startedPlugins = pluginManager.getStartedPlugins();
        List<String> extNames = new ArrayList<>();
        for (PluginWrapper plugin : startedPlugins) {
            log.info("Registering extensions of the plugin '{}' as beans", plugin.getPluginId());
            Set<String> extensionClassNames = pluginManager.getExtensionClassNames(plugin.getPluginId());
            for (String extensionClassName : extensionClassNames) {
                log.info("Register extension '{}' as bean", extensionClassName);
                extNames.add(extensionClassName);
            }
        }
        return extNames;
    }

    public String loadPlugin(Path pluginPath) {
        String pluginId;
        try {
            pluginId = pluginManager.loadPlugin(pluginPath);
            pluginManager.startPlugins();
        } catch (Exception e) {
            throw new MangleRuntimeException(e, ErrorCode.PLUGIN_OPERATION_ERROR, "load");
        }
        if (pluginId == null) {
            throw new MangleRuntimeException(ErrorCode.PLUGIN_OPERATION_ERROR, "load");
        }
        return pluginId;
    }

    public boolean unloadPlugin(String pluginId) {
        boolean status = false;
        try {
            status = pluginManager.unloadPlugin(pluginId);
        } catch (Exception e) {
            throw new MangleRuntimeException(e, ErrorCode.PLUGIN_OPERATION_ERROR, "unload");
        }
        if (!status) {
            throw new MangleRuntimeException(ErrorCode.PLUGIN_OPERATION_ERROR, "unload");
        }
        return status;
    }

    public boolean disablePlugin(String pluginId) {
        boolean status = false;
        try {
            status = pluginManager.disablePlugin(pluginId);
        } catch (Exception e) {
            throw new MangleRuntimeException(e, ErrorCode.PLUGIN_OPERATION_ERROR, "disable");
        }
        if (!status) {
            throw new MangleRuntimeException(ErrorCode.PLUGIN_OPERATION_ERROR, "disable");
        }
        return status;
    }

    public boolean enablePlugin(String pluginId) {
        boolean status = false;
        PluginState state;
        try {
            status = pluginManager.enablePlugin(pluginId);
            state = pluginManager.startPlugin(pluginId);
        } catch (Exception e) {
            throw new MangleRuntimeException(e, ErrorCode.PLUGIN_OPERATION_ERROR, "enable");
        }
        if (!status && !state.equals(PluginState.STARTED)) {
            throw new MangleRuntimeException(ErrorCode.PLUGIN_OPERATION_ERROR, "enable");
        }
        return status;
    }

    public boolean deletePlugin(String pluginId) {
        boolean status = false;
        try {
            if (!(CommonConstants.DEFAULT_PLUGIN_ID.equals(pluginId))) {
                status = pluginManager.deletePlugin(pluginId);
            } else {
                throw new MangleRuntimeException(ErrorConstants.DEFAULT_PLUGIN_ID_ERROR,
                        ErrorCode.PLUGIN_OPERATION_ERROR);
            }
        } catch (MangleRuntimeException e) {
            throw new MangleRuntimeException(e.getMessage(), ErrorCode.GENERIC_ERROR);
        } catch (Exception e) {
            throw new MangleRuntimeException(e, ErrorCode.PLUGIN_OPERATION_ERROR, "delete");
        }
        if (!status) {
            throw new MangleRuntimeException(ErrorCode.PLUGIN_OPERATION_ERROR, "delete");
        }
        return status;
    }

    /**
     * Get pluginId for already loaded plugins on given pluginName.
     *
     * @param pluginPath
     *            the path to investigate
     * @return id of plugin or null if not loaded
     */
    public String getIdForPluginName(String pluginName) {
        for (PluginWrapper plugin : pluginManager.getPlugins()) {
            if (pluginName.contains(plugin.getPluginPath().toFile().getName())) {
                return plugin.getPluginId();
            }
        }
        throw new MangleRuntimeException(ErrorCode.PLUGIN_NAME_NOT_LOADED, pluginName);
    }
}
