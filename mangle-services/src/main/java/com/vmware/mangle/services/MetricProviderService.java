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

import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import io.micrometer.core.instrument.util.NamedThreadFactory;
import io.micrometer.datadog.DatadogMeterRegistry;
import io.micrometer.wavefront.WavefrontMeterRegistry;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vmware.mangle.cassandra.model.MangleAdminConfigurationSpec;
import com.vmware.mangle.cassandra.model.metricprovider.MetricProviderSpec;
import com.vmware.mangle.model.enums.MetricProviderType;
import com.vmware.mangle.services.hazelcast.HazelcastClusterSyncAware;
import com.vmware.mangle.services.repository.AdminConfigurationRepository;
import com.vmware.mangle.services.repository.MetricProviderRepository;
import com.vmware.mangle.task.framework.metric.providers.MetricProviderClientFactory;
import com.vmware.mangle.utils.constants.ErrorConstants;
import com.vmware.mangle.utils.constants.MetricProviderConstants;
import com.vmware.mangle.utils.exceptions.MangleException;
import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Service Class for Metric Provider
 *
 * @author ashrimali
 */
@Service
@Log4j2
public class MetricProviderService implements HazelcastClusterSyncAware {

    @Autowired
    private MetricProviderRepository metricProviderRepository;

    @Autowired
    private WavefrontMeterRegistry wavefrontMeterRegistry;

    @Autowired
    private DatadogMeterRegistry datadogMeterRegistry;

    @Autowired
    private MetricProviderClientFactory mangleMetricProviderClientFactory;

    @Autowired
    private AdminConfigurationRepository adminConfigurationRepository;

    /**
     * This method will check if sendingMetric property is set to true at boot time. If yes it will
     * resume sending the metrics to active metric provider.
     *
     */
    @PostConstruct
    public void initializeMeterRegistryAtBoot() {
        try {
            Optional<MangleAdminConfigurationSpec> sendingMetricProperty = this.adminConfigurationRepository
                    .findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS);
            if (sendingMetricProperty.isPresent()) {
                MangleAdminConfigurationSpec sendingMetricPropertySpec = sendingMetricProperty.get();
                if (sendingMetricPropertySpec.getPropertyValue().equals("true")) {
                    this.sendMetrics();
                }
            }
        } catch (MangleException mangleException) {
            log.debug(mangleException.getMessage());
        }
    }

    /**
     * @return List<MetricProviderSpec>
     */
    public List<MetricProviderSpec> getAllMetricProviders() {
        log.info("Getting all Metric Providers...");
        return this.metricProviderRepository.findAll();
    }

    /**
     * @return boolean if all metric providers deleted or not
     * @throws MangleException
     */
    public boolean deleteAllMetricProviders() throws MangleException {
        log.info("Deleting all Metric Providers...");
        this.metricProviderRepository.deleteAll();
        resetAdminConfigDetails();
        return this.metricProviderRepository.findAll().isEmpty();
    }

    /**
     * Method to get active Metric Provider Details
     *
     * @return MetricProviderSpec
     * @throws MangleException
     */
    public MetricProviderSpec getActiveMetricProvider() {
        log.debug("Finding active metric provider.");
        Optional<MangleAdminConfigurationSpec> optional =
                this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER);
        MangleAdminConfigurationSpec adminConfiguration;
        if (optional.isPresent()) {
            adminConfiguration = optional.get();
            String activeMetricProvider = adminConfiguration.getPropertyValue();
            if (StringUtils.isNotEmpty(activeMetricProvider)) {
                Optional<MetricProviderSpec> optionalMetricProvider =
                        this.metricProviderRepository.findByName(activeMetricProvider);
                return optionalMetricProvider.isPresent() ? optionalMetricProvider.get() : null;
            } else {
                log.warn(ErrorConstants.NO_ACTIVE_METRIC_PROVIDER);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * @return status if all metric registriy's connection closed or not.
     */
    public boolean closeAllMetricCollection() {
        changeMetricSendingStatus(false);
        this.wavefrontMeterRegistry.stop();
        this.datadogMeterRegistry.stop();
        return true;
    }

    /**
     * @param metricProviderType
     * @return List<MetricProviderSpec>
     * @throws MangleException
     */
    public List<MetricProviderSpec> getMetricProviderByType(MetricProviderType metricProviderType)
            throws MangleException {
        log.info("Getting all Metric Provider by Type");
        List<MetricProviderSpec> metricProviders;
        if (metricProviderType != null && !metricProviderType.name().isEmpty()) {
            metricProviders = this.metricProviderRepository.findByMetricProviderType(metricProviderType);
        } else {
            log.error(ErrorConstants.METRICPROVIDER_TYPE + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.METRICPROVIDER_TYPE);
        }
        return metricProviders;

    }

    /**
     * @param metricProviderName
     * @return MetricProviderSpec
     * @throws MangleException
     */
    public MetricProviderSpec getMetricProviderByName(String metricProviderName) throws MangleException {
        log.info("Getting Metric Provider by Name: " + metricProviderName);
        if (!StringUtils.isEmpty(metricProviderName)) {
            Optional<MetricProviderSpec> optional = this.metricProviderRepository.findByName(metricProviderName);
            if (optional.isPresent()) {
                return optional.get();
            } else {
                throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.METRICPROVIDER_NAME,
                        metricProviderName);
            }
        } else {
            log.error(ErrorConstants.METRICPROVIDER_NAME + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.METRICPROVIDER_NAME);
        }
    }

    /**
     * @param metricProviderSpec
     * @return MetricProviderSpec
     * @throws MangleException
     */
    public MetricProviderSpec addMetricProvider(MetricProviderSpec metricProviderSpec) throws MangleException {
        log.info("Adding  Metric Provider...");
        if (null != metricProviderSpec) {
            log.info("Adding Metric Provider with Name: " + metricProviderSpec.getName());
            return this.metricProviderRepository.save(metricProviderSpec);
        } else {
            log.error(ErrorConstants.METRICPROVIDER_NAME + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.METRICPROVIDER_NAME);
        }

    }

    /**
     * @param metricProviderName
     * @return is metric provider deleted or not
     * @throws MangleException
     */
    public boolean deleteMetricProvider(String metricProviderName) throws MangleException {
        log.info("Deleting Metric Provider: " + metricProviderName);
        if (!StringUtils.isEmpty(metricProviderName)) {
            Optional<MetricProviderSpec> optional = this.metricProviderRepository.findByName(metricProviderName);
            if (optional.isPresent()) {
                MetricProviderSpec activeMetricProviderSpec = this.getActiveMetricProvider();
                if (null != activeMetricProviderSpec && activeMetricProviderSpec.getName().equals(metricProviderName)) {
                    resetAdminConfigDetails();
                }
                this.metricProviderRepository.deleteByName(metricProviderName);
                Optional<MetricProviderSpec> verifyMetricProvider =
                        this.metricProviderRepository.findByName(metricProviderName);
                if (!verifyMetricProvider.isPresent()) {
                    return true;
                }
            } else {
                throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.METRICPROVIDER_NAME,
                        metricProviderName);
            }
        } else {
            log.error(ErrorConstants.METRICPROVIDER_NAME + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.METRICPROVIDER_NAME);
        }
        return false;
    }

    private void resetAdminConfigDetails() throws MangleException {
        changeActiveMetricProvider("");
        changeMetricSendingStatus(false);
    }

    /**
     * @param id
     * @param metricProviderSpec
     * @return updated MetricProviderSpec
     * @throws MangleException
     */
    public MetricProviderSpec updateMetricProviderById(String id, MetricProviderSpec metricProviderSpec)
            throws MangleException {
        log.info("Updating Metric Provider by Id: " + id);
        if (!StringUtils.isEmpty(id)) {
            Optional<MetricProviderSpec> metricProvider = this.metricProviderRepository.findById(id);
            MetricProviderSpec metricProviderSpecToUpdate;
            if (metricProvider.isPresent()) {
                metricProviderSpecToUpdate = metricProvider.get();
            } else {
                throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.METRICPROVIDER_ID, id);
            }
            log.info("Updating: " + metricProviderSpec.getId() + " ...");
            return this.metricProviderRepository.save(getUpdatedSpec(metricProviderSpecToUpdate, metricProviderSpec));
        } else {
            log.error(ErrorConstants.METRICPROVIDER_NAME + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.METRICPROVIDER_NAME);
        }
    }

    /**
     * @param metricProviderName
     * @param metricProviderSpec
     * @return updated MetricProviderSpec
     * @throws MangleException
     */
    public MetricProviderSpec updateMetricProviderByName(String metricProviderName,
            MetricProviderSpec metricProviderSpec) throws MangleException {
        log.info("Updating Metric Provider by Name: " + metricProviderName);
        if (!StringUtils.isEmpty(metricProviderName)) {
            Optional<MetricProviderSpec> metricProvider = this.metricProviderRepository.findByName(metricProviderName);
            MetricProviderSpec metricProviderSpecToUpdate;
            if (metricProvider.isPresent()) {
                metricProviderSpecToUpdate = metricProvider.get();
            } else {
                throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.METRICPROVIDER_ID,
                        metricProviderName);
            }
            log.info("Updating: " + metricProviderSpec.getName() + " ...");
            return this.metricProviderRepository.save(getUpdatedSpec(metricProviderSpecToUpdate, metricProviderSpec));
        } else {
            log.error(ErrorConstants.METRICPROVIDER_NAME + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.METRICPROVIDER_NAME);
        }

    }

    /**
     * @param metricProviderSpec
     * @return Is test connection to metric provider is success or not
     * @throws MangleException
     */
    public boolean testConnectionMetricProvider(MetricProviderSpec metricProviderSpec) throws MangleException {
        log.info("Checking Connection with  Metric Provider: " + metricProviderSpec.getName());
        if (!StringUtils.isEmpty(metricProviderSpec.getName())) {
            boolean status =
                    this.mangleMetricProviderClientFactory.getMetricProviderClient(metricProviderSpec).testConnection();
            if (status) {
                return true;
            } else {
                log.error(ErrorConstants.METRICPROVIDER_NAME + ErrorConstants.TEST_CONNECTION_FAILED_METRICPROVIDER);
                throw new MangleException(ErrorConstants.TEST_CONNECTION_FAILED_METRICPROVIDER,
                        ErrorCode.TEST_CONNECTION_FAILED, metricProviderSpec.getName());
            }
        } else {
            log.error(ErrorConstants.METRICPROVIDER_NAME + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.METRICPROVIDER_NAME);
        }
    }

    /**
     * @param metricProviderName
     * @return MangleAdminConfigurationSpec
     * @throws MangleException
     */
    public MangleAdminConfigurationSpec enableMetricProviderByName(String metricProviderName) throws MangleException {
        log.info("Enabling the  Metric Provider: " + metricProviderName + " for Sending Metrics");
        if (!StringUtils.isEmpty(metricProviderName)) {
            Optional<MetricProviderSpec> metricProviderOptional =
                    this.metricProviderRepository.findByName(metricProviderName);
            MangleAdminConfigurationSpec adminConfigSpec = null;
            if (metricProviderOptional.isPresent()) {
                MetricProviderSpec activeMetricProvider = this.getActiveMetricProvider();
                //Enable operation of already active metric provider will disable it.
                if (null != activeMetricProvider && activeMetricProvider.getName().equals(metricProviderName)) {
                    return disableMetricProvider();
                } else {
                    adminConfigSpec = changeActiveMetricProvider(metricProviderName);
                    changeMetricMeterRegistry(metricProviderOptional.get());
                    return adminConfigSpec;
                }
            } else {
                throw new MangleRuntimeException(ErrorCode.NO_RECORD_FOUND, ErrorConstants.METRICPROVIDER_NAME,
                        metricProviderName);
            }
        } else {
            log.error(ErrorConstants.METRICPROVIDER_NAME + ErrorConstants.FIELD_VALUE_EMPTY);
            throw new MangleException(ErrorCode.FIELD_VALUE_EMPTY, ErrorConstants.METRICPROVIDER_NAME);
        }
    }

    private void changeMetricMeterRegistry(MetricProviderSpec metricProviderSpec) throws MangleException {
        Optional<MangleAdminConfigurationSpec> optional =
                this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS);
        if (optional.isPresent() && optional.get().getPropertyValue().equals("true")) {
            log.info("Changing Metric Meter Registry...");
            if (!enableMetricsBasedOnType(metricProviderSpec)) {
                throw new MangleRuntimeException(ErrorCode.METER_REGISTERY_FAILED);
            }
        }
    }

    private MangleAdminConfigurationSpec changeActiveMetricProvider(String name) throws MangleException {
        Optional<MangleAdminConfigurationSpec> adminConfigOptional =
                this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER);
        MangleAdminConfigurationSpec adminConfigSpec = null;
        if (adminConfigOptional.isPresent() && !adminConfigOptional.get().getPropertyValue().isEmpty()) {
            MetricProviderSpec metricProviderSpec = this.getActiveMetricProvider();
            if (metricProviderSpec.getMetricProviderType().equals(MetricProviderType.DATADOG)) {
                this.datadogMeterRegistry.stop();
            } else if (metricProviderSpec.getMetricProviderType().equals(MetricProviderType.WAVEFRONT)) {
                this.wavefrontMeterRegistry.stop();
            }
            adminConfigSpec = adminConfigOptional.get();
            adminConfigSpec.setPropertyValue(name);
            adminConfigSpec = this.adminConfigurationRepository.save(adminConfigSpec);

        } else if (adminConfigOptional.isPresent() && adminConfigOptional.get().getPropertyValue().isEmpty()) {
            adminConfigSpec = adminConfigOptional.get();
            adminConfigSpec.setPropertyValue(name);
            adminConfigSpec = this.adminConfigurationRepository.save(adminConfigSpec);
        } else {
            adminConfigSpec = new MangleAdminConfigurationSpec();
            adminConfigSpec.setPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER);
            adminConfigSpec.setPropertyValue(name);
            adminConfigSpec = this.adminConfigurationRepository.save(adminConfigSpec);
        }
        return adminConfigSpec;
    }

    private MangleAdminConfigurationSpec disableMetricProvider() {
        Optional<MangleAdminConfigurationSpec> adminConfigOptional =
                this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.ACTIVE_METRIC_PROVIDER);
        MangleAdminConfigurationSpec adminConfigSpec = null;
        if (adminConfigOptional.isPresent() && !adminConfigOptional.get().getPropertyValue().isEmpty()) {
            MetricProviderSpec metricProviderSpec = this.getActiveMetricProvider();
            if (metricProviderSpec.getMetricProviderType().equals(MetricProviderType.DATADOG)) {
                this.datadogMeterRegistry.stop();
            } else if (metricProviderSpec.getMetricProviderType().equals(MetricProviderType.WAVEFRONT)) {
                this.wavefrontMeterRegistry.stop();
            }
            adminConfigSpec = adminConfigOptional.get();
            adminConfigSpec.setPropertyValue("");
            changeMetricSendingStatus(false);
            adminConfigSpec = this.adminConfigurationRepository.save(adminConfigSpec);

        }
        return adminConfigSpec;
    }

    private MetricProviderSpec getUpdatedSpec(MetricProviderSpec metricProviderSpecToUpdate,
            MetricProviderSpec metricProviderSpec) {

        metricProviderSpecToUpdate.setName(metricProviderSpec.getName());
        metricProviderSpecToUpdate.setDatadogConnectionProperties(metricProviderSpec.getDatadogConnectionProperties());
        metricProviderSpecToUpdate.setMetricProviderType(metricProviderSpec.getMetricProviderType());
        metricProviderSpecToUpdate
                .setWaveFrontConnectionProperties(metricProviderSpec.getWaveFrontConnectionProperties());
        return metricProviderSpecToUpdate;
    }

    /**
     * @return Is Mangle Metrics enabled or not to send via active metric provider.
     * @throws MangleException
     */
    public boolean sendMetrics() throws MangleException {
        log.info("Enabling Mangle Metrics...");
        MetricProviderSpec metricProviderSpec = this.getActiveMetricProvider();
        if (null != metricProviderSpec) {
            changeMetricSendingStatus(true);
            return enableMetricsBasedOnType(metricProviderSpec);
        } else {
            throw new MangleException(ErrorCode.NO_ACTIVE_METRIC_PROVIDER, ErrorConstants.NO_ACTIVE_METRIC_PROVIDER);
        }
    }

    /**
     * @return Giving status if Mangle is sending Metrics or not
     */
    public boolean isMangleMetricsEnabled() {
        Optional<MangleAdminConfigurationSpec> optional =
                this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS);
        if (optional.isPresent()) {
            String status = optional.get().getPropertyValue();
            if (null == status) {
                return false;
            } else if (status.equals("false")) {
                return false;
            } else if (status.equals("true")) {
                return true;
            }
        }
        return false;
    }

    private boolean enableMetricsBasedOnType(MetricProviderSpec metricProviderSpec) {
        switch (metricProviderSpec.getMetricProviderType()) {
        case DATADOG:
            return enableDatadogMetrics();
        case WAVEFRONT:
            return enableWavefrontMetrics();
        default:
            return false;
        }
    }

    private boolean enableWavefrontMetrics() {
        this.wavefrontMeterRegistry.start(new NamedThreadFactory("wavefront-metrics-publisher"));
        return true;
    }

    private boolean enableDatadogMetrics() {
        this.datadogMeterRegistry.start(new NamedThreadFactory("datadog-metrics-publisher"));
        return true;
    }

    private void changeMetricSendingStatus(boolean status) {
        Optional<MangleAdminConfigurationSpec> optional =
                this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS);
        MangleAdminConfigurationSpec adminConfigSpec;
        if (optional.isPresent()) {
            adminConfigSpec = optional.get();
        } else {
            adminConfigSpec = new MangleAdminConfigurationSpec();
            adminConfigSpec.setPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS);
        }

        adminConfigSpec.setPropertyValue(String.valueOf(status));
        this.adminConfigurationRepository.save(adminConfigSpec);
    }

    @Override
    public void resync(String objectIdentifier) {
        log.debug("Enabling send metrics on the current node");
        Optional<MangleAdminConfigurationSpec> optional =
                this.adminConfigurationRepository.findByPropertyName(MetricProviderConstants.SENDING_MANGLE_METRICS);
        MangleAdminConfigurationSpec configurationSpec = optional.orElse(null);
        if (configurationSpec != null) {
            if (Boolean.valueOf(configurationSpec.getPropertyValue())) {
                try {
                    sendMetrics();
                } catch (MangleException e) {
                    log.error("Enabling of the send metrics failed");
                }
            } else {
                closeAllMetricCollection();
            }

        }

    }
}
