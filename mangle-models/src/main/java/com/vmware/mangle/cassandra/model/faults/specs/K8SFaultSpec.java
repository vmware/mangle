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

package com.vmware.mangle.cassandra.model.faults.specs;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.datastax.driver.core.DataType.Name;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.cassandra.core.mapping.CassandraType;

import com.vmware.mangle.cassandra.model.tasks.DockerSpecificArguments;
import com.vmware.mangle.cassandra.model.tasks.K8SSpecificArguments;
import com.vmware.mangle.services.enums.K8SResource;


/**
 * @author bkaranam
 *
 *         Api payload specification for K8S specific faults
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({ "timeoutinMilliseconds", "k8sArguments", "dockerArguments" })
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class K8SFaultSpec extends CommandExecutionFaultSpec implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "Kubernetes resource type. Please select the supported one from enum")
    @CassandraType(type = Name.VARCHAR)
    protected K8SResource resourceType;
    @ApiModelProperty(value = "Resource labels as a key:value pair ex: {\"app\":\"mangle\",\"build\":\"4.0.0.1\"}")
    protected Map<String, String> resourceLabels;
    @ApiModelProperty(value = "Name Kubernetes resource , if specified it will have highest priority than  resource labels")
    protected String resourceName;
    @ApiModelProperty(value = "true or false , specify this option along with resourceLabels", example = "true")
    protected boolean randomInjection = true;
    @JsonIgnore
    private List<String> resourcesList;

    @JsonIgnore
    @Override
    public void setTimeoutInMilliseconds(Integer timeoutinMilliseconds) {
        super.setTimeoutInMilliseconds(timeoutinMilliseconds);
    }

    @JsonIgnore
    @Override
    public void setK8sArguments(K8SSpecificArguments k8sArguments) {
        super.setK8sArguments(k8sArguments);
    }

    @JsonIgnore
    @Override
    public void setDockerArguments(DockerSpecificArguments dockerArguments) {
        super.setDockerArguments(dockerArguments);
    }
}
