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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import lombok.extern.log4j.Log4j2;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import com.vmware.mangle.cassandra.model.custom.faults.CustomFaultSpec;
import com.vmware.mangle.cassandra.model.faults.specs.CommandExecutionFaultSpec;
import com.vmware.mangle.cassandra.model.plugin.CustomFaultDescriptor;
import com.vmware.mangle.cassandra.model.plugin.ExtensionDetails;
import com.vmware.mangle.cassandra.model.plugin.ManglePluginDescriptor;
import com.vmware.mangle.cassandra.model.plugin.PluginDetails;
import com.vmware.mangle.cassandra.model.plugin.PluginMetaInfo;
import com.vmware.mangle.services.constants.CommonConstants;
import com.vmware.mangle.services.hazelcast.HazelcastClusterSyncAware;
import com.vmware.mangle.services.repository.PluginDetailsRepository;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * PluginDetailsService class is used to sync the plugins details with db.
 *
 * @author kumargautam
 */
@Service
@Validated
@Log4j2
public class PluginDetailsService implements HazelcastClusterSyncAware {

    @Autowired
    private PluginDetailsRepository pluginDetailsRepository;
    @Autowired
    private PluginService pluginService;
    @Autowired
    private SpringPluginManager pluginManager;
    @Autowired
    private FileStorageService storageService;

    /**
     * Method is used to sync the plugins details from db to local plugin repo and load in context,
     * if plugin details dosn't exist in local plugin repo.
     *
     * @param pluginWrappers
     */
    public void runToSyncPlugins(List<PluginWrapper> pluginWrappers) {
        new Thread(() -> syncPlugins(pluginWrappers)).start();
    }

    /**
     * Method is used to sync the plugins details from db to local plugin repo and load in context,
     * if plugins details dosn't exist in local plugin repo.
     *
     * @param pluginWrappers
     */
    public void syncPlugins(List<PluginWrapper> pluginWrappers) {
        log.info("Plugins sync inprogress...");
        List<PluginDetails> dbPlugins = findAllPluginDetails();
        List<String> toBeSyncedPluginIds = getAllPluginIds(dbPlugins);
        for (PluginWrapper pluginWrapper : pluginWrappers) {
            String pluginId = pluginWrapper.getPluginId();
            // skip to sync the plugins details from db, if plugin details already exist in local plugin repo.
            if (toBeSyncedPluginIds.contains(pluginId)) {
                // Removing pluginId, because already exist in local plugin repo
                toBeSyncedPluginIds.remove(pluginWrapper.getPluginId());
                PluginDetails details = dbPlugins.stream()
                        .filter(pluginDetail -> pluginId.equals(pluginDetail.getPluginId())).findAny().orElse(null);
                // Updating plugin state into current node based on plugins details from db.
                updatePluginStateInContext(details);
            } else if (!CommonConstants.DEFAULT_PLUGIN_ID.equals(pluginId)) {
                // Deleting plugin which is not available in db.
                pluginService.deletePlugin(pluginId);
            }
        }
        //sync the plugins details from db.
        syncPluginsFromDb(dbPlugins, toBeSyncedPluginIds);
        log.info("Plugins sync completed!");
    }

    /**
     * @param pluginDetails
     */
    private void updatePluginStateInContext(PluginDetails pluginDetails) {
        if (pluginDetails != null) {
            if (pluginDetails.getIsActive() && pluginDetails.getIsLoaded()) {
                handleResyncForActivePlugin(pluginDetails);
            } else if (!pluginDetails.getIsLoaded() && !pluginDetails.getIsActive()) {
                log.trace("Unloading plugin {} on the the current node", pluginDetails.getPluginName());
                pluginService.unloadPlugin(pluginDetails.getPluginId());
            } else if (pluginDetails.getIsLoaded() && !pluginDetails.getIsActive()) {
                log.trace("Disabling plugin {} on the the current node", pluginDetails.getPluginName());
                pluginService.disablePlugin(pluginDetails.getPluginId());
            }
        }
    }

    /**
     * Method is used to create the plugins details into db.
     *
     * @param pluginWrapper
     * @return
     */
    public PluginDetails syncPlugins(PluginWrapper pluginWrapper) {
        log.debug("Received request to sync Plugins...");
        if (!CommonConstants.DEFAULT_PLUGIN_ID.equals(pluginWrapper.getPluginId())) {
            PluginDetails pluginDetails = getPluginDetails(pluginWrapper);
            if (pluginDetails != null) {
                pluginDetails.setPluginId(pluginWrapper.getPluginId());
                pluginDetails.setIsLoaded(true);
                pluginDetails.setIsActive(true);
                pluginDetails.setPluginName(pluginWrapper.getPluginPath().toFile().getName());
                pluginDetails.setPluginVersion(pluginWrapper.getDescriptor().getVersion());
                createPluginDetails(pluginDetails);
            }
            return pluginDetails;
        }
        return null;
    }

    /**
     * Method is used to sync the plugins details from db to local plugin repo and load in context.
     *
     * @param dbPlugins
     * @param dbPluginIds
     */
    private void syncPluginsFromDb(List<PluginDetails> dbPlugins, List<String> dbPluginIds) {
        log.debug("Received request to sync Plugins from Db...");
        dbPlugins = dbPlugins.stream().filter(pluginDetail -> dbPluginIds.contains(pluginDetail.getPluginId()))
                .collect(Collectors.toList());
        loadPlugins(dbPlugins);
    }

    /**
     * Method is used to load plugins in context.
     *
     * @param dbPlugins
     */
    private void loadPlugins(List<PluginDetails> dbPlugins) {
        log.debug("Received request to load Plugins...");
        for (PluginDetails pluginDetails : dbPlugins) {
            try {
                writeFileInPluginRepo(pluginDetails);
                String pluginId = pluginManager.loadPlugin(new File(pluginDetails.getPluginPath()).toPath());
                pluginManager.startPlugin(pluginId);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    public List<String> getAllPluginIds(List<PluginDetails> pluginDetails) {
        return pluginDetails.stream().map(PluginDetails::getPluginId).collect(Collectors.toList());
    }

    public List<PluginDetails> findAllPluginDetails() {
        log.debug("Received request to find all Plugin Details...");
        return pluginDetailsRepository.findAll();
    }

    public PluginDetails findPluginDetailsByPluginId(String pluginId) {
        log.debug("Received request to find Plugin Details by pluginId...");
        return pluginDetailsRepository.findByPluginId(pluginId).orElse(null);
    }

    public PluginDetails createPluginDetails(@Valid PluginDetails pluginDetails) {
        log.debug("Received request to save Plugin Details...");
        if (pluginDetails != null) {
            return pluginDetailsRepository.save(pluginDetails);
        } else {
            log.error(ErrorConstants.PLUGIN_DETAILS + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleRuntimeException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.PLUGIN_DETAILS);
        }
    }

    public boolean deletePluginDetailsByPluginId(String pluginId) {
        log.debug("Received request to delete Plugin Details by pluginId...");
        pluginDetailsRepository.deleteByPluginId(pluginId);
        return !(pluginDetailsRepository.findByPluginId(pluginId).isPresent());
    }

    /**
     * Method is used to create PluginDetails instance from Plugin Descriptor Json file.
     *
     * @param pluginWrapper
     * @return
     */
    public PluginDetails getPluginDetails(PluginWrapper pluginWrapper) {
        log.debug("Received request to form Plugin details...");
        PluginDetails pluginDetails = null;
        try {
            ManglePluginDescriptor pluginDescriptor = RestTemplateWrapper.jsonToObject(
                    pluginService.readPluginDescriptorJson(pluginWrapper.getPluginPath()),
                    ManglePluginDescriptor.class);
            pluginDetails = new PluginDetails();
            Map<String, CustomFaultDescriptor> extensionMap = new HashMap<>();
            pluginDetails.setCustomFaultDescriptorMap(extensionMap);
            pluginDetails.setPluginPath(pluginWrapper.getPluginPath().toFile().getAbsolutePath() + ".zip");
            pluginDetails.setPluginName(pluginWrapper.getPluginPath().toFile().getName());
            for (CustomFaultDescriptor fault : pluginDescriptor.getFaults()) {
                extensionMap.put(fault.getFaultName(), fault);
            }
            readPluginFile(pluginDetails);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return pluginDetails;
    }

    public void readPluginFile(PluginDetails pluginDetails) throws IOException {
        log.debug("Received request to read Plugin file...");
        pluginDetails.setPluginFile(Files.readAllBytes(getPluginFilePath(pluginDetails.getPluginName())));
    }

    public void writeFileInPluginRepo(PluginDetails pluginDetails) throws IOException {
        log.debug("Received request to write Plugin file in Plugin Repo...");
        Files.write(getPluginFilePath(pluginDetails.getPluginName()), pluginDetails.getPluginFile());
    }

    public PluginDetails createPluginDetailsForLoadPlugin(String pluginId) {
        return syncPlugins(pluginManager.getPlugin(pluginId));
    }

    public PluginDetails updatePluginDetailsForUnLoadPlugin(String pluginId) {
        PluginDetails pluginDetails = findPluginDetailsByPluginId(pluginId);
        if (pluginDetails != null) {
            pluginDetails.setIsLoaded(false);
            pluginDetails.setIsActive(false);
            createPluginDetails(pluginDetails);
        }
        return pluginDetails;
    }

    public PluginDetails updatePluginDetailsForEnablePlugin(String pluginId) {
        PluginDetails pluginDetails = findPluginDetailsByPluginId(pluginId);
        if (pluginDetails != null) {
            pluginDetails.setIsActive(true);
            createPluginDetails(pluginDetails);
        }
        return pluginDetails;
    }

    public PluginDetails updatePluginDetailsForDisablePlugin(String pluginId) {
        PluginDetails pluginDetails = findPluginDetailsByPluginId(pluginId);
        if (pluginDetails != null) {
            pluginDetails.setIsActive(false);
            createPluginDetails(pluginDetails);
        }
        return pluginDetails;
    }

    public boolean deletePluginDetailsForDeletePlugin(String pluginId) {
        return deletePluginDetailsByPluginId(pluginId);
    }

    /**
     * @param pluginId
     * @param faultSpec
     */
    public void updatePluginInformationInFaultSpec(CustomFaultSpec customFaultSpec,
            CommandExecutionFaultSpec faultSpec) {
        PluginDetails pluginDetails = findPluginDetailsByPluginId(customFaultSpec.getPluginId());
        PluginMetaInfo pluginMetaInfo = new PluginMetaInfo();
        pluginMetaInfo.setPluginId(customFaultSpec.getPluginId());
        pluginMetaInfo.setPluginName(pluginDetails.getPluginName());
        pluginMetaInfo.setPluginVersion(pluginDetails.getPluginVersion());
        pluginMetaInfo.setFaultName(customFaultSpec.getFaultName());
        faultSpec.setPluginMetaInfo(pluginMetaInfo);
    }

    public List<PluginWrapper> getRegisteredPlugins() {
        return pluginManager.getStartedPlugins();
    }

    public ExtensionDetails getFaultExtensionDetailsFromPluginDescriptor(CustomFaultSpec customFaultSpec)
            throws MangleException {
        return getExtensionDetailsFromPluginDescriptor(customFaultSpec);
    }

    /**
     * @param customFaultSpec
     * @return
     * @throws MangleException
     */
    public ExtensionDetails getExtensionDetailsFromPluginDescriptor(CustomFaultSpec customFaultSpec)
            throws MangleException {
        PluginDetails pluginDetails = getActivePluginDetailsByPluginId(customFaultSpec.getPluginId());
        if (pluginDetails == null) {
            throw new MangleRuntimeException(ErrorCode.PLUGIN_ID_NOT_LOADED, customFaultSpec.getPluginId());
        }
        if (pluginDetails.getCustomFaultDescriptorMap().containsKey(customFaultSpec.getFaultName())) {
            return pluginDetails.getCustomFaultDescriptorMap().get(customFaultSpec.getFaultName())
                    .getExtensionDetails();
        }
        throw new MangleException(ErrorCode.FAULT_NAME_NOT_FOUND_IN_PLUGIN_DESCRIPTOR, customFaultSpec.getFaultName());
    }

    public Boolean isPluginAvailable(PluginMetaInfo pluginMetaInfo) {
        PluginDetails retrievedPluginInfo = getActivePluginDetailsByPluginId(pluginMetaInfo.getPluginId());
        if (retrievedPluginInfo != null) {
            return (pluginMetaInfo.getPluginName().equals(retrievedPluginInfo.getPluginName())
                    && pluginMetaInfo.getPluginVersion().equals(retrievedPluginInfo.getPluginVersion()));
        } else {
            return false;
        }
    }

    /**
     * @param
     * @param pluginId
     * @return
     */
    public Boolean isPluginAvailable(String pluginId) {
        PluginDetails retrievedPluginInfo = findPluginDetailsByPluginId(pluginId);
        return (retrievedPluginInfo != null && retrievedPluginInfo.getIsLoaded());
    }

    /**
     * Handles re-sync on the current node, called only if the resync is not triggered on the
     * current node
     *
     * According to the status of the plugin saved in the db, the plugin is
     *
     * 1. loaded: if active and loaded; and is not loaded into the context
     *
     * 2. enabled: if active and loaded; and loaded into the context, but not enabled
     *
     * 3. unloaded: if not active and not loaded
     *
     * 4. disabled: if loaded but not active
     *
     * 5. deleted: if the plugin details is not available in db
     *
     * 6. table re-sync: if the object identifier provided is an empty string.
     *
     * @param objectIdentifier:
     *            primary key which uniquely identifies the plugin details saved in DB
     */
    @Override
    public void resync(String objectIdentifier) {
        if (StringUtils.hasText(objectIdentifier)) {
            PluginDetails pluginDetails = findPluginDetailsByPluginId(objectIdentifier);
            if (pluginDetails != null) {
                if (pluginDetails.getIsActive() && pluginDetails.getIsLoaded()) {
                    handleResyncForActivePlugin(pluginDetails);
                } else if (!pluginDetails.getIsLoaded() && !pluginDetails.getIsActive()) {
                    log.trace("Unloading plugin {} on the the current node", pluginDetails.getPluginName());
                    pluginService.unloadPlugin(pluginDetails.getPluginId());
                } else {
                    log.trace("Disabling plugin {} on the the current node", pluginDetails.getPluginName());
                    pluginService.disablePlugin(pluginDetails.getPluginId());
                }
            } else {
                pluginService.deletePlugin(objectIdentifier);
            }
        } else {
            syncPlugins(pluginManager.getPlugins());
        }
    }

    /**
     * Called if the persisted plugin status is active and loaded
     *
     * 1. loaded: if active and loaded; and is not loaded into the context 2. enabled: if active and
     * loaded; and loaded into the context, but not enabled
     *
     * @param pluginDetails
     */
    private void handleResyncForActivePlugin(PluginDetails pluginDetails) {
        File file = new File(getPluginFilePath(pluginDetails.getPluginName()).toUri());
        if (!file.exists()) {
            try {
                writeFileInPluginRepo(pluginDetails);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        try {
            pluginService.enablePlugin(pluginService.getIdForPluginName(pluginDetails.getPluginName()));
            log.trace("Enabled plugin with name {} is on the current node", pluginDetails.getPluginName());
        } catch (MangleRuntimeException e) {
            log.trace("Loading plugin with name {} is on the current node", pluginDetails.getPluginName());
            pluginService.loadPlugin(getPluginFilePath(pluginDetails.getPluginName()));
        }
    }

    private Path getPluginFilePath(String pluginFileName) {
        return storageService.getFileStorageLocation().resolve(pluginFileName + ".zip");
    }

    public PluginDetails getActivePluginDetailsByPluginId(String pluginId) {
        PluginDetails pluginDetails = findPluginDetailsByPluginId(pluginId);
        pluginDetails =
                (pluginDetails != null && pluginDetails.getIsLoaded() && pluginDetails.getIsActive()) ? pluginDetails
                        : null;
        return pluginDetails;
    }
}