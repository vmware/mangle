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

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.pf4j.DefaultPluginDescriptor;
import org.pf4j.DefaultPluginLoader;
import org.pf4j.PluginClassLoader;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPluginManager;
import org.pf4j.util.AndFileFilter;
import org.pf4j.util.DirectoryFileFilter;
import org.pf4j.util.FileUtils;
import org.pf4j.util.HiddenFilter;
import org.pf4j.util.NotFileFilter;
import org.pf4j.util.OrFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.plugin.CustomFaultDescriptor;
import com.vmware.mangle.cassandra.model.plugin.ExtensionDetails;
import com.vmware.mangle.cassandra.model.plugin.ManglePluginDescriptor;
import com.vmware.mangle.cassandra.model.plugin.PluginDetails;
import com.vmware.mangle.model.plugin.ExtensionType;
import com.vmware.mangle.services.constants.CommonConstants;
import com.vmware.mangle.task.framework.helpers.faults.AbstractCustomFault;
import com.vmware.mangle.task.framework.skeletons.ITaskHelper;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.PluginIllegalArgumentException;
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
    private static final String EXTENSIONS_KEY = "extensions";

    public Map<String, Object> getExtensions() {
        List<PluginWrapper> startedPlugins = pluginManager.getStartedPlugins();
        List<String> extNames = new ArrayList<>();
        for (PluginWrapper plugin : startedPlugins) {
            log.info("Registering extensions of the plugin '{}' as beans", plugin.getPluginId());
            Set<String> extensionClassNames = pluginManager.getExtensionClassNames(plugin.getPluginId());
            for (String extensionClassName : extensionClassNames) {
                log.debug("Register extension '{}' as bean", extensionClassName);
                extNames.add(extensionClassName);
            }
        }
        Map<String, Object> extMap = new HashMap<>();
        extMap.put(EXTENSIONS_KEY, extNames);
        extMap.put("defaultExtensions", pluginManager.getExtensionClassNames(null));
        List<PluginWrapper> pluginWrapperList = pluginManager.getPlugins();
        List<Object> plugins = new ArrayList<>();
        pluginWrapperList.forEach(pluginWrapper -> plugins.add(pluginWrapper.getDescriptor()));
        extMap.put("plugins", plugins);
        return extMap;
    }

    public Map<String, Object> getExtensions(String pluginId, ExtensionType extensionType) {
        List<String> extNames;
        if (extensionType != null) {
            switch (extensionType) {
            case MODEL:
                extNames = pluginManager.getExtensions(CommandExecutionFaultSpec.class, pluginId).stream()
                        .map(spec -> spec.getClass().getName()).collect(Collectors.toList());
                break;
            case FAULT:
                extNames = pluginManager.getExtensions(AbstractCustomFault.class, pluginId).stream()
                        .map(spec -> spec.getClass().getName()).collect(Collectors.toList());
                break;
            case TASK:
                extNames = pluginManager.getExtensions(ITaskHelper.class, pluginId).stream()
                        .map(spec -> spec.getClass().getName()).collect(Collectors.toList());
                break;
            default:
                extNames = new ArrayList<>();
                break;
            }
        } else {
            extNames = pluginManager.getExtensionClassNames(pluginId).stream().collect(Collectors.toList());
        }
        Map<String, Object> extMap = new HashMap<>();
        extMap.put(EXTENSIONS_KEY, extNames);
        return extMap;
    }

    @SuppressWarnings("rawtypes")
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
                log.debug("Register extension '{}' as bean", extensionClassName);
                extNames.add(extensionClassName);
            }
        }
        return extNames;
    }

    public String loadPlugin(Path pluginPath) {
        String pluginId = null;
        Map<String, PluginWrapper> pluginWrappersMap = pluginManager.getStartedPlugins().stream()
                .collect(Collectors.toMap(pluginWrapper -> pluginWrapper.getPluginPath().toString() + ".zip",
                        pluginWrapper -> pluginWrapper));
        try {
            pluginId = pluginManager.loadPlugin(pluginPath);
            pluginManager.startPlugin(pluginId);
        } catch (PluginRuntimeException e) {
            if (pluginWrappersMap.keySet().contains(pluginPath.toString())) {
                throw new MangleRuntimeException(e, ErrorCode.PLUGIN_OPERATION_ERROR, "load");
            } else {
                String pluginSimpleFileName = pluginPath.getFileName().toString();
                pluginId = pluginSimpleFileName.substring(0, pluginSimpleFileName.lastIndexOf('-'));
                deletePlugin(pluginId);
                loadPlugin(pluginPath);
            }
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
            checkPluginStateBeforeDelete(pluginId);
            if (!(CommonConstants.DEFAULT_PLUGIN_ID.equals(pluginId))) {
                status = pluginManager.deletePlugin(pluginId);
            } else {
                throw new MangleRuntimeException(ErrorConstants.DEFAULT_PLUGIN_ID_ERROR,
                        ErrorCode.PLUGIN_OPERATION_ERROR);
            }
        } catch (MangleRuntimeException e) {
            throw new MangleRuntimeException(e.getMessage(), ErrorCode.GENERIC_ERROR);
        } catch (IllegalArgumentException e) {
            throw new MangleRuntimeException(e, ErrorCode.PLUGIN_ID_NOT_LOADED, pluginId);
        } catch (Exception e) {
            throw new MangleRuntimeException(e, ErrorCode.PLUGIN_OPERATION_ERROR, "delete");
        }
        if (!status) {
            throw new MangleRuntimeException(ErrorCode.PLUGIN_OPERATION_ERROR, "delete");
        }
        return status;
    }

    private void checkPluginStateBeforeDelete(String pluginId) {
        PluginWrapper pluginWrapper = pluginManager.getPlugin(pluginId);
        if (pluginWrapper != null) {
            PluginState pluginState = pluginWrapper.getPluginState();
            if (PluginState.DISABLED == pluginState) {
                enablePlugin(pluginId);
            }
        }
    }

    /**
     * Get pluginId for already loaded plugins on given pluginName.
     *
     * @param pluginName
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

    public AbstractCustomFault getExtensionForCustomFault(String extensionName) throws MangleException {
        List<AbstractCustomFault> faults = pluginManager.getExtensions(AbstractCustomFault.class);
        AbstractCustomFault ext = null;
        for (AbstractCustomFault fault : faults) {
            if (extensionName.equals(fault.getClass().getName())) {
                ext = fault;
                break;
            }
        }
        if (ext == null) {
            throw new MangleException(ErrorCode.EXTENSION_NOT_FOUND, extensionName);
        }
        return ext;
    }

    public CommandExecutionFaultSpec getExtensionForModel(String extensionName) throws MangleException {
        List<CommandExecutionFaultSpec> specs = pluginManager.getExtensions(CommandExecutionFaultSpec.class);
        CommandExecutionFaultSpec ext = null;
        for (CommandExecutionFaultSpec spec : specs) {
            if (extensionName.equals(spec.getClass().getName())) {
                ext = spec;
                break;
            }
        }
        if (ext == null) {
            throw new MangleException(ErrorCode.EXTENSION_NOT_FOUND, extensionName);
        }
        return ext;
    }

    /**
     * @param pluginName
     * @param pluginPath
     * @return boolean
     */
    public boolean postValidatePlugin(String pluginName, Path pluginPath) {
        if ((pluginName.contains(CommonConstants.DEFAULT_PLUGIN_ID))) {
            return true;
        }
        try {
            isFileExist(pluginPath);
            pluginPath = FileUtils.expandIfZip(pluginPath);
            return postValidatePluginDescriptor(readPluginDescriptorJson(pluginPath));
        } catch (IOException e) {
            log.error(e.getMessage());
            deletePlugin(getIdForPluginName(pluginName));
            throw new MangleRuntimeException(e, ErrorCode.IO_EXCEPTION);
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage());
            deletePlugin(getIdForPluginName(pluginName));
            throw new MangleRuntimeException(ErrorCode.EXTENSION_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            deletePlugin(getIdForPluginName(pluginName));
            throw new MangleRuntimeException(e.getMessage(), ErrorCode.IO_EXCEPTION);
        }
    }

    /**
     * @param pluginName
     * @param pluginPath
     * @return boolean
     */
    public String preValidatePlugin(String pluginName, Path pluginPath) {
        String pluginId;
        String pluginClass;
        if ((pluginName.contains(CommonConstants.DEFAULT_PLUGIN_ID))) {
            return CommonConstants.DEFAULT_PLUGIN_ID;
        }
        try {
            isFileExist(pluginPath);
            pluginPath = FileUtils.expandIfZip(pluginPath);
            Properties properties = readPluginProperties(pluginPath);
            pluginId = properties.getProperty("plugin.id");
            pluginClass = properties.getProperty("plugin.class");
            validatePluginClass(pluginClass, pluginPath);
            if (!pluginId.equals(getPluginFileName(pluginPath.toString()))) {
                throw new PluginIllegalArgumentException(ErrorCode.MALFORMED_PLUGIN_PROPERTIES, "pluginId", pluginId,
                        "pluginPath", pluginPath.toString());
            }
            ManglePluginDescriptor pluginDescriptor = RestTemplateWrapper
                    .jsonToObject(readPluginDescriptorJson(pluginPath), ManglePluginDescriptor.class);
            if (pluginDescriptor == null) {
                throw new JSONException("Empty JSON provided");
            }
            validateDuplicateExtensions(pluginId, pluginPath);
        } catch (IOException e) {
            log.error(e.getMessage());
            deletePluginFile(pluginPath);
            throw new MangleRuntimeException(e, ErrorCode.IO_EXCEPTION);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            deletePluginFile(pluginPath);
            throw new MangleRuntimeException(e.getMessage(), ErrorCode.IO_EXCEPTION);
        } catch (PluginIllegalArgumentException e) {
            log.error(e.getErrorCode());
            deletePluginFile(pluginPath);
            throw new MangleRuntimeException(e.getErrorCode(), e.getArgs());
        } catch (Exception e) {
            log.error(e.getMessage());
            deletePluginFile(pluginPath);
            throw new MangleRuntimeException(e, ErrorCode.MALFORMED_PLUGIN_DESCRIPTOR);
        }
        return pluginId;
    }

    private void validatePluginClass(String pluginClass, Path pluginPath) throws IOException {
        PluginClassLoader pluginClassLoader = getPluginClassLoader(pluginPath);
        try {
            pluginClassLoader.loadClass(pluginClass);
        } catch (ClassNotFoundException e) {
            throw new PluginIllegalArgumentException(ErrorCode.INVALID_PLUGIN_CLASS, "pluginClass", pluginClass);
        } finally {
            pluginClassLoader.close();
        }
    }

    /**
     * @param pluginId
     * @param pluginPath
     * @throws IOException
     */
    private void validateDuplicateExtensions(String pluginId, Path pluginPath) throws IOException {
        PluginClassLoader pluginClassLoader = getPluginClassLoader(pluginPath);
        List<PluginWrapper> startedPlugins = pluginManager.getStartedPlugins();
        Map<String, List<String>> duplicateExtensionMap = new HashMap<>();
        for (PluginWrapper plugin : startedPlugins) {
            String loadedPluginId = plugin.getPluginId();
            if (!pluginId.equals(loadedPluginId)) {
                Set<String> extensionClassNames = pluginManager.getExtensionClassNames(loadedPluginId);
                for (String extensionClassName : extensionClassNames) {
                    log.trace("Extension '{}' as bean", extensionClassName);
                    try {
                        pluginClassLoader.loadClass(extensionClassName);
                        addExtensionInMap(loadedPluginId, extensionClassName, duplicateExtensionMap);
                    } catch (ClassNotFoundException e) {
                        log.trace(e.getMessage());
                    }
                }
            }
        }
        pluginClassLoader.close();
        if (!CollectionUtils.isEmpty(duplicateExtensionMap)) {
            throw new PluginIllegalArgumentException(ErrorCode.DUPLICATE_EXTENSIONS_FOUND, duplicateExtensionMap);
        }
    }

    /**
     * @param pluginPath
     * @return
     */
    private PluginClassLoader getPluginClassLoader(Path pluginPath) {
        DefaultPluginLoader defaultPluginLoader = new DefaultPluginLoader(pluginManager);
        return (PluginClassLoader) defaultPluginLoader.loadPlugin(pluginPath, new DefaultPluginDescriptor());
    }

    /**
     * @param loadedPluginId
     * @param extensionClassName
     * @param extensionsMap
     */
    private void addExtensionInMap(String loadedPluginId, String extensionClassName,
            Map<String, List<String>> extensionsMap) {
        if (extensionsMap.containsKey(loadedPluginId)) {
            List<String> extensionClassList = extensionsMap.get(loadedPluginId);
            extensionClassList.add(extensionClassName);
            extensionsMap.put(loadedPluginId, extensionClassList);
        } else {
            List<String> extensionClassList = new ArrayList<>();
            extensionClassList.add(extensionClassName);
            extensionsMap.put(loadedPluginId, extensionClassList);
        }
    }

    private String getPluginFileName(String pluginPath) {
        return pluginPath.substring(pluginPath.lastIndexOf(File.separator) + 1, pluginPath.lastIndexOf('-'));
    }

    /**
     * @param pluginPath
     * @return JSONObject
     * @throws IOException
     * @throws JSONException
     */
    public String readPluginDescriptorJson(Path pluginPath) throws IOException {
        Path pluginDescriptorPath = getFilePath(pluginPath, CommonConstants.PLUGIN_DESCRIPTOR_FILE_NAME);
        try (FileReader reader = new FileReader(pluginDescriptorPath.toFile())) {
            return IOUtils.toString(reader);
        }
    }

    /**
     * @param pluginPath
     * @return
     * @return JSONObject
     * @throws IOException
     * @throws JSONException
     */
    public Properties readPluginProperties(Path pluginPath) throws IOException {
        Path pluginPropertiesPath = getFilePath(pluginPath, CommonConstants.PLUGIN_PROPERTIES_FILE_NAME);
        Properties properties = new Properties();
        FileInputStream inStream = new FileInputStream(pluginPropertiesPath.toString());
        properties.load(inStream);
        inStream.close();
        return properties;
    }

    /**
     * @param pluginPath
     * @param fileName
     * @return
     */
    public Path getFilePath(Path pluginPath, String fileName) {
        if (pluginPath.toFile().isDirectory()) {
            Path filePath = pluginPath.resolve(Paths.get(fileName));
            log.debug("Lookup plugin descriptor in '{}'", filePath);
            isFileExist(filePath);
            return filePath;
        } else {
            // it's a jar file
            try {
                return FileUtils.getPath(pluginPath, fileName);
            } catch (IOException e) {
                log.error(e.getMessage());
                throw new MangleRuntimeException(e, ErrorCode.IO_EXCEPTION);
            }
        }
    }

    /**
     * @param pluginPath
     */
    public void isFileExist(Path pluginPath) {
        if ((pluginPath == null) || !pluginPath.toFile().exists()) {
            throw new IllegalArgumentException(String.format("Specified file path %s does not exist!", pluginPath));
        }
    }

    /**
     * @param pluginPath
     * @param jsonObject
     * @param pluginName
     * @return boolean
     * @throws ClassNotFoundException
     * @throws JSONException
     */
    private boolean postValidatePluginDescriptor(String jsonObject) throws ClassNotFoundException {
        ManglePluginDescriptor pluginDescriptor =
                RestTemplateWrapper.jsonToObject(jsonObject, ManglePluginDescriptor.class);
        for (CustomFaultDescriptor fault : pluginDescriptor.getFaults()) {
            ExtensionDetails extensionDetails = fault.getExtensionDetails();
            validateExtension(extensionDetails.getModelExtensionName(), pluginDescriptor.getPluginId(),
                    ExtensionType.MODEL);
            validateExtension(extensionDetails.getFaultExtensionName(), pluginDescriptor.getPluginId(),
                    ExtensionType.FAULT);
            validateExtension(extensionDetails.getTaskExtensionName(), pluginDescriptor.getPluginId(),
                    ExtensionType.TASK);
        }
        return true;
    }

    /**
     * @param extensionName
     * @param pluginId
     * @param extensionType
     * @throws ClassNotFoundException
     */
    private void validateExtension(String extensionName, String pluginId, ExtensionType extensionType)
            throws ClassNotFoundException {
        Map<String, Object> extensionMap = getExtensions(pluginId, extensionType);
        if (extensionMap != null && ((ArrayList<?>) extensionMap.get(EXTENSIONS_KEY)).stream()
                .noneMatch(name -> ((String) name).equals(extensionName))) {
            throw new ClassNotFoundException(extensionName);
        }
    }

    /**
     * @param customFaultSpec
     * @return
     * @throws MangleException
     */
    public CustomFaultDescriptor getFaultDetailsFromPluginDescriptor(String pluginId, String faultName,
            PluginDetailsService pluginDetailsService) throws MangleException {
        PluginDetails pluginDetails = pluginDetailsService.getActivePluginDetailsByPluginId(pluginId);
        if (pluginDetails == null) {
            throw new MangleRuntimeException(ErrorCode.PLUGIN_ID_NOT_LOADED, pluginId);
        }
        if (pluginDetails.getCustomFaultDescriptorMap().containsKey(faultName)) {
            CustomFaultDescriptor customFaultDescriptor = pluginDetails.getCustomFaultDescriptorMap().get(faultName);
            customFaultDescriptor.setPluginId(pluginDetails.getPluginId());
            return customFaultDescriptor;
        } else {
            throw new MangleException(ErrorCode.FAULT_NAME_NOT_FOUND_IN_PLUGIN_DESCRIPTOR, faultName);
        }
    }

    public boolean deletePluginFile(Path path) {
        try {
            FileUtils.optimisticDelete(FileUtils.findWithEnding(path, ".zip", ".ZIP", ".Zip"));
            return deletePluginPath(path);
        } catch (Exception ex) {
            log.error(ex);
        }
        return false;
    }

    private boolean deletePluginPath(Path pluginPath) throws IOException {
        AndFileFilter pluginsFilter = new AndFileFilter(new DirectoryFileFilter());
        pluginsFilter.addFileFilter(new NotFileFilter(createHiddenPluginFilter()));
        if (!pluginsFilter.accept(pluginPath.toFile())) {
            return false;
        }
        try {
            FileUtils.delete(pluginPath);
            return true;
        } catch (NoSuchFileException e) {
            return false;
        }
    }

    private FileFilter createHiddenPluginFilter() {
        return new OrFileFilter(new HiddenFilter());
    }
}