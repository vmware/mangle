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

package com.vmware.mangle.unittest.plugin.descriptor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import lombok.extern.log4j.Log4j2;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.vmware.mangle.cassandra.model.faults.specs.PluginFaultSpec;
import com.vmware.mangle.cassandra.model.plugin.CustomFaultDescriptor;
import com.vmware.mangle.cassandra.model.plugin.ExtensionDetails;
import com.vmware.mangle.cassandra.model.plugin.ManglePluginDescriptor;
import com.vmware.mangle.model.plugin.ExtensionType;
import com.vmware.mangle.task.framework.helpers.AbstractCommandExecutionTaskHelper;
import com.vmware.mangle.task.framework.helpers.faults.AbstractCustomFault;
import com.vmware.mangle.utils.ReadProperty;
import com.vmware.mangle.utils.clients.restclient.RestTemplateWrapper;
import com.vmware.mangle.utils.exceptions.PluginIllegalArgumentException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * Test Class to verify Integrity of Plugin Meta Information.
 *
 * @author hkilari
 *
 */
@Log4j2
public class PluginDescriptorTest {
    /**
     * Test Method to verify the Semantic and Functional errors in plugin-descriptor.json,
     * plugin.properties
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @Test
    public void testPluginDescriptor() throws IOException, ClassNotFoundException {
        Properties properties = ReadProperty.readProperty(
                "src" + File.separator + "main" + File.separator + "resources" + File.separator + "plugin.properties");
        String pluginId = properties.getProperty("plugin.id");
        String pluginClass = properties.getProperty("plugin.class");
        Assert.assertNotNull(getPluginClass(pluginClass), "Provide PluginClass: " + pluginClass + "not provided");
        String jsonObject = readPluginDescriptorJson();
        ManglePluginDescriptor pluginDescriptor =
                RestTemplateWrapper.jsonToObject(jsonObject, ManglePluginDescriptor.class);
        Assert.assertTrue(validateConstraints(pluginDescriptor));
        Assert.assertEquals(pluginId, pluginDescriptor.getPluginId(),
                "pluginId provided in plugin-descriptor.json, plugin.properties are not matching");
        validatePluginDescriptor(jsonObject);
    }

    public String readPluginDescriptorJson() throws IOException {
        File pluginDescriptorFile = new File("src" + File.separator + "main" + File.separator + "resources"
                + File.separator + "plugin-descriptor.json");
        try (FileReader reader = new FileReader(pluginDescriptorFile)) {
            return IOUtils.toString(reader);
        }
    }

    private boolean validatePluginDescriptor(String jsonObject) throws ClassNotFoundException {
        ManglePluginDescriptor pluginDescriptor =
                RestTemplateWrapper.jsonToObject(jsonObject, ManglePluginDescriptor.class);
        for (CustomFaultDescriptor fault : pluginDescriptor.getFaults()) {
            ExtensionDetails extensionDetails = fault.getExtensionDetails();
            validateExtension(extensionDetails.getModelExtensionName(), ExtensionType.MODEL);
            validateExtension(extensionDetails.getFaultExtensionName(), ExtensionType.FAULT);
            validateExtension(extensionDetails.getTaskExtensionName(), ExtensionType.TASK);
        }
        return true;
    }

    private boolean validateConstraints(ManglePluginDescriptor pluginDescriptor) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<ManglePluginDescriptor>> constraintViolations = validator.validate(pluginDescriptor);
        if (constraintViolations.size() > 0) {
            Iterator<ConstraintViolation<ManglePluginDescriptor>> iterator = constraintViolations.iterator();
            while (iterator.hasNext()) {
                ConstraintViolation<ManglePluginDescriptor> cv = iterator.next();
                log.error(cv.getRootBeanClass().getName() + "." + cv.getPropertyPath() + " " + cv.getMessage());

                log.error(cv.getRootBeanClass().getSimpleName() + "." + cv.getPropertyPath() + " " + cv.getMessage());
            }
            return false;
        } else {
            return true;
        }
    }

    private void validateExtension(String modelExtensionName, ExtensionType extensionType) {
        try {
            switch (extensionType) {
            case MODEL:
                getPluginClass(modelExtensionName).asSubclass(PluginFaultSpec.class);
                break;
            case FAULT:
                getPluginClass(modelExtensionName).asSubclass(AbstractCustomFault.class);
                break;
            case TASK:
                getPluginClass(modelExtensionName).asSubclass(AbstractCommandExecutionTaskHelper.class);
                break;
            default:
                throw new IOException("Invalid Extension Type: " + extensionType);
            }
        } catch (IOException e) {
            log.error(e);
            Assert.assertTrue(false, "Failed to validate provided ExtensionName: " + modelExtensionName);
        }
    }

    private Class<?> getPluginClass(String pluginClass) throws IOException {
        ClassLoader pluginClassLoader = this.getClass().getClassLoader();
        Class<?> loadedClass = null;
        try {
            loadedClass = pluginClassLoader.loadClass(pluginClass);
        } catch (ClassNotFoundException e) {
            throw new PluginIllegalArgumentException(ErrorCode.INVALID_PLUGIN_CLASS, "pluginClass", pluginClass);
        }
        return loadedClass;
    }
}
